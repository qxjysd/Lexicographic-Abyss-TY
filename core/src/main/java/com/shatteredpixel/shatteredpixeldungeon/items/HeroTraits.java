/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Lexicographic-Abyss (modified version) by 许玄
 * Copyright (C) 2024-2026 许玄
 *
 * This is a modified version of Shattered Pixel Dungeon. Original copyrights apply.
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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 角色词条管理器 —— 管理玩家在整个游戏过程中获得的所有词条。
 *
 * 通过 Dungeon.traits 访问单例实例。
 * 词条在首次到达新楼层时由 Dungeon.newLevel() 自动授予。
 */
public class HeroTraits implements Bundlable {

	private static final String COLLECTED_TRAITS = "collected_traits";
	private static final String TRAIT_COUNTS     = "trait_counts";

	private ArrayList<Trait> collectedTraits = new ArrayList<>();
	private HashMap<Integer, Integer> traitCounts = new HashMap<>();

	/**
	 * 根据当前楼层深度生成并授予一个词条。
	 * 调用 TraitLibrary.generateTrait() 创建词条后加入收集列表。
	 */
	public void grantTrait(int floorDepth) {
		Trait trait = TraitLibrary.generateTrait(floorDepth);
		collectedTraits.add(trait);
		traitCounts.put(floorDepth, traitCounts.getOrDefault(floorDepth, 0) + 1);
	}

	/**
	 * 返回所有已收集的词条列表。
	 */
	public ArrayList<Trait> getCollectedTraits() {
		return collectedTraits;
	}

	/**
	 * 返回词条统计映射（楼层 → 该层获得的词条数量）。
	 */
	public HashMap<Integer, Integer> getTraitCounts() {
		return traitCounts;
	}

	// ========== Bundle 序列化 / 反序列化 ==========

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(COLLECTED_TRAITS, collectedTraits);

		Bundle countsBundle = new Bundle();
		for (int key : traitCounts.keySet()) {
			countsBundle.put(String.valueOf(key), traitCounts.get(key));
		}
		bundle.put(TRAIT_COUNTS, countsBundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		// 恢复词条列表
				if (bundle.contains(COLLECTED_TRAITS)) {
					collectedTraits = new ArrayList<>();
					for (Bundlable b : bundle.getCollection(COLLECTED_TRAITS)) {
						if (b != null) {
							collectedTraits.add((Trait) b);
						}
					}
				} else {
			collectedTraits = new ArrayList<>();
		}

		// 恢复词条统计
		if (bundle.contains(TRAIT_COUNTS)) {
			traitCounts = new HashMap<>();
			Bundle countsBundle = bundle.getBundle(TRAIT_COUNTS);
			for (String key : countsBundle.getKeys()) {
				traitCounts.put(Integer.parseInt(key), countsBundle.getInt(key));
			}
		} else {
			traitCounts = new HashMap<>();
		}
	}
}
