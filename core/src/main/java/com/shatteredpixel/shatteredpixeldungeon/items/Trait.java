/*
 * Lexicographic-Abyss by 许玄
 * Copyright (C) 2024-2026 许玄
 *
 * This is a modified version of Shattered Pixel Dungeon.
 * Shattered Pixel Dungeon: Copyright (C) 2014-2026 Evan Debenham
 * Pixel Dungeon: Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;

public class Trait implements Bundlable {

	private static final String TRAIT_ID        = "trait_id";
	private static final String TRAIT_NAME      = "trait_name";
	private static final String TRAIT_DESC      = "trait_desc";
	private static final String TRAIT_LEVEL     = "trait_level";
	private static final String TRAIT_POSITIVE  = "trait_positive";
	private static final String TRAIT_EFFECT_TYPE = "trait_effect_type";
	private static final String TRAIT_EFFECT_VALUE = "trait_effect_value";

	private int id;
	private String name;
	// 描述模板（用于正负等级切换时重建描述）
	private String descTemplate;
	private String desc;
	private int level;
	private boolean isPositive;
	private String effectType;
	private float effectValue;

	// 无参构造器（用于Bundle反序列化）
	public Trait() {
		this(0, "", "", 0, true, "MISC", 0f);
	}

	// 全参构造器
	public Trait(int id, String name, String desc, int level, boolean isPositive, String effectType, float effectValue) {
		this.id = id;
		this.name = name;
		this.descTemplate = desc;
		this.desc = desc;
		this.level = level;
		this.isPositive = isPositive;
		this.effectType = effectType;
		this.effectValue = effectValue;
		// 构造后立即刷新描述，替换%.0f%%为实际数值
		recalcEffectValue();
	}

	// ========== Bundle 序列化 ==========

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(TRAIT_ID, id);
		bundle.put(TRAIT_NAME, name);
		bundle.put(TRAIT_DESC, desc);
		bundle.put(TRAIT_LEVEL, level);
		bundle.put(TRAIT_POSITIVE, isPositive);
		bundle.put(TRAIT_EFFECT_TYPE, effectType);
		bundle.put(TRAIT_EFFECT_VALUE, effectValue);
		bundle.put(TRAIT_DESC + "_template", descTemplate);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		id          = bundle.getInt(TRAIT_ID);
		name        = bundle.getString(TRAIT_NAME);
		desc        = bundle.getString(TRAIT_DESC);
		level       = bundle.getInt(TRAIT_LEVEL);
		isPositive  = bundle.getBoolean(TRAIT_POSITIVE);
		effectType  = bundle.getString(TRAIT_EFFECT_TYPE);
		effectValue = bundle.getFloat(TRAIT_EFFECT_VALUE);
		descTemplate = bundle.getString(TRAIT_DESC + "_template");
		// 恢复后刷新描述，确保与当前等级一致
		recalcEffectValue();
	}

	// ========== Getter 方法 ==========

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public int getLevel() {
		return level;
	}

	public boolean isPositive() {
		return isPositive;
	}

	public String getEffectType() {
		return effectType;
	}

	public float getEffectValue() {
		return effectValue;
	}

	/**
	 * 获取实际的加成/减益百分比（正数=增益，负数=减益）。
	 * 正面词条效果公式：level * 2%
	 * 负面词条效果公式：level * -3%
	 */
	public float getActualEffect() {
		if (isPositive) {
			return level * 0.02f;
		} else {
			return level * -0.03f;
		}
	}

	/**
	 * 正面等级上限10，负面等级上限5。
	 * 降级最低不超过负向上限（正面-10，负面-5）。
	 */
	public int getMaxLevel() {
		return isPositive ? 10 : 5;
	}

	public int getMinLevel() {
		// 降级最低为负向等级上限
		return isPositive ? -10 : -5;
	}

	// ========== 等级升级方法 ==========

	/**
	 * 升1级（不超过上限）。
	 * @return true=升级成功, false=已达上限
	 */
	public boolean upgrade() {
		if (level >= getMaxLevel()) return false;
		level++;
		recalcEffectValue();
		return true;
	}

	/**
	 * 降1级（不低于下限）。
	 * @return true=降级成功, false=已达下限
	 */
	public boolean downgrade() {
		if (level <= getMinLevel()) return false;
		level--;
		recalcEffectValue();
		return true;
	}

	/**
	 * 设置指定等级（裁剪到合法范围）。
	 */
	public void setLevel(int level) {
		this.level = Math.max(getMinLevel(), Math.min(getMaxLevel(), level));
		recalcEffectValue();
	}

	/**
	 * 根据当前level和是正面/负面重新计算effectValue和描述。
	 */
	private void recalcEffectValue() {
		if (isPositive) {
			effectValue = level * 0.02f;
		} else {
			effectValue = level * -0.03f;
		}
		// 根据等级正负重建描述（每次升级/降级都会刷新）
		if (descTemplate == null) {
			// 兼容旧存档：descTemplate未保存时，从当前desc中提取模板
			if (desc != null && !desc.isEmpty()) {
				descTemplate = desc.replaceAll("\\d+%", "%.0f%%");
			}
		}
		if (descTemplate != null) {
			String baseDesc = descTemplate;
			int pct = Math.round(Math.abs(effectValue) * 100);
			String newDesc;
			if (isPositive && level <= 0) {
				// 正面词条但等级≤0 → 降级为负面效果, 反转描述关键词
				String reversed = baseDesc;
				String[][] flipPairs = {
					{"恢复", "扣除"}, {"治疗", "伤害"}, {"提升", "降低"},
					{"增加", "减少"}, {"增强", "削弱"}, {"强化", "弱化"},
					{"加成", "减免"}, {"赋予", "夺取"},
					{"回复", "扣除"}, {"吸血", "流血"}, {"反击", "自伤"},
					{"反伤", "自伤"}, {"护盾", "破盾"}, {"免疫", "易伤"},
					{"净化", "污染"},
				};
				for (String[] pair : flipPairs) {
					reversed = reversed.replace(pair[0], "【" + pair[1] + "】");
				}
				reversed = reversed.replace("【", "").replace("】", "");
				newDesc = reversed.replace("%.0f%%", pct + "% [降级]");
			} else if (!isPositive && level > getMaxLevel()) {
				// 负面词条等级异常高于上限 → 超限标记
				newDesc = baseDesc.replace("%.0f%%", pct + "% [超限]");
			} else {
				// 正常显示：正面有效等级 或 负面基础等级
				newDesc = baseDesc.replace("%.0f%%", pct + "%");
			}
			desc = newDesc;
		}
	}

	@Override
	public String toString() {
		return name + " Lv." + level + " (" + (isPositive ? "+" : "") + String.format("%.0f", getActualEffect() * 100) + "%)";
	}
}
