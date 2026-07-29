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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class TraitHandler {

    // ===== 概率效果上限 =====
    // 概率型效果（CRIT/TERROR/SLEEP等）最大不超过15%，防止过于OP
    private static final float MAX_PROBABILITY = 0.15f;

    // ===== 英雄属性加成（正负都生效：正面增益，负面减益） =====
    public static float getAttackBonus(Hero hero) {
        return sumEffect(hero, "ATK", false);
    }
    public static float getDefenseBonus(Hero hero) {
        return sumEffect(hero, "DEF", false);
    }
    public static float getMaxHPBonus(Hero hero) {
        return sumEffect(hero, "HP", false);
    }
    public static float getSpeedBonus(Hero hero) {
        return sumEffect(hero, "SPD", false);
    }
    public static float getDodgeBonus(Hero hero) {
        return sumEffect(hero, "DODGE", false) + sumEffect(hero, "DEX", false);
    }
    public static float getHasteBonus(Hero hero) {
        return sumEffect(hero, "HASTE", false);
    }
    public static float getVisionBonus(Hero hero) {
        return sumEffect(hero, "VISION", false);
    }
    public static float getMagicBonus(Hero hero) {
        return sumEffect(hero, "MAGIC", false) + sumEffect(hero, "SAT", false);
    }
    public static float getRegenBonus(Hero hero) {
        return sumEffect(hero, "REGEN", false);
    }

    // ===== 对敌效果（仅正面词条生效，带概率上限） =====
    public static float getLifestealPercent(Hero hero) {
        return Math.min(MAX_PROBABILITY, sumEffect(hero, "LIFESTEAL", true));
    }
    public static float getThornsPercent(Hero hero) {
        // 荆棘上限略高，因为是被动反伤
        return Math.min(0.25f, sumEffect(hero, "THORNS", true));
    }
    public static float getCritChance(Hero hero) {
        return Math.min(MAX_PROBABILITY, sumEffect(hero, "CRIT", true));
    }
    public static float getLootBonus(Hero hero) {
        return Math.min(MAX_PROBABILITY * 2, sumEffect(hero, "LOOT", true) + sumEffect(hero, "GOLD", true));
    }
    public static float getExpBonus(Hero hero) {
        return Math.min(MAX_PROBABILITY * 2, sumEffect(hero, "EXP", true));
    }

    // ===== 攻击触发（概率效果统一封顶15%） =====
    public static int onAttackProc(Hero hero, Char enemy, int damage) {
    	int extraDmg = 0;

    	// 正面: 流血/毒素/灼烧/冰冻 → 对敌额外伤害（伤害型，不封顶）
    	extraDmg += (int)(damage * sumEffect(hero, "BLEED", true));
    	extraDmg += (int)(damage * sumEffect(hero, "POISON", true));
    	extraDmg += (int)(damage * sumEffect(hero, "BURN", true));
    	extraDmg += (int)(damage * sumEffect(hero, "FROST", true));

    	// 正面: 恐惧/睡眠/魅惑/混乱 → 对敌施加控制（概率上限15%）
    	float terrorChance = cappedProb(hero, "TERROR");
    	float sleepChance = cappedProb(hero, "SLEEP");
    	float charmChance = cappedProb(hero, "CHARM");
    	float amokChance = cappedProb(hero, "AMOK");

    	if (terrorChance > 0 && enemy.isAlive() && Random.Float() < terrorChance) {
    		Buff.affect(enemy, Terror.class, 2f);
    	}
    	if (sleepChance > 0 && enemy.isAlive() && Random.Float() < sleepChance) {
    		Buff.affect(enemy, Sleep.class);
    	}
    	if (charmChance > 0 && enemy.isAlive() && Random.Float() < charmChance) {
    		Buff.affect(enemy, Charm.class, 3f);
    	}
    	if (amokChance > 0 && enemy.isAlive() && Random.Float() < amokChance) {
    		Buff.affect(enemy, Vertigo.class, 2f);
    	}

    	// 正面: 即死(对Boss无效，概率上限15%)
    	float instakillChance = cappedProb(hero, "INSTAKILL");
    	if (instakillChance > 0 && enemy.isAlive() && Random.Float() < instakillChance) {
    		if (enemy.properties().contains(Char.Property.BOSS)) {
    			extraDmg += (int)(damage * 0.5f);
    		} else {
    			extraDmg += enemy.HP;
    		}
    	}

    	// 正面: 残废敌人（概率上限15%）
    	float crippleChance = cappedProb(hero, "CRIPPLE");
    	if (crippleChance > 0 && enemy.isAlive() && Random.Float() < crippleChance) {
    		Buff.affect(enemy, Cripple.class, 3f);
    		Buff.affect(enemy, Slow.class, 3f);
    	}

    	// 正面: 腐蚀敌人（概率上限15%）
    	float corrosionChance = cappedProb(hero, "CORROSION");
    	if (corrosionChance > 0 && enemy.isAlive() && Random.Float() < corrosionChance) {
    		Buff.affect(enemy, Corrosion.class).set(3f, (int)(damage * 0.25f));
    		Buff.affect(enemy, Ooze.class).set(3f);
    	}

    	// 隐身触发（攻击时概率隐身）
    	float invisibleChance = getInvisibleChance(hero);
    	if (invisibleChance > 0 && hero.isAlive() && Random.Float() < invisibleChance) {
    		Buff.affect(hero, Invisibility.class, 3f);
    	}

    	// 净化触发（攻击后净化自身一个负面状态）
    	float cleanseChance = getCleanseChance(hero);
    	if (cleanseChance > 0 && Random.Float() < cleanseChance) {
    		cleanseOneDebuff(hero);
    	}

    	// ===== 负面: 反噬效果（仅计算负面的净效果） =====
    	applyNegativeSelfEffects(hero, enemy, damage);

    	return extraDmg;
    }

    // ===== 负面反噬效果（独立方法，简化逻辑） =====
    private static void applyNegativeSelfEffects(Hero hero, Char enemy, int damage) {
    	// 伤害型反噬：流血/毒素/灼烧
    	float selfBleed = sumEffect(hero, "BLEED", false);
    	float selfPoison = sumEffect(hero, "POISON", false);
    	float selfBurn = sumEffect(hero, "BURN", false);

    	if (selfBleed < 0 && hero.isAlive()) {
    		Buff.affect(hero, Bleeding.class).set(damage * Math.abs(selfBleed));
    	}
    	if (selfPoison < 0 && hero.isAlive()) {
    		Buff.affect(hero, Poison.class).set(3f);
    	}
    	if (selfBurn < 0 && hero.isAlive()) {
    		hero.damage((int)(damage * Math.abs(selfBurn)), hero);
    	}

    	// 控制型反噬
    	float selfStun = sumEffect(hero, "STUN", false);
    	float selfSlow = sumEffect(hero, "SLOW", false);
    	float selfLifesteal = sumEffect(hero, "LIFESTEAL", false);
    	float selfThorns = sumEffect(hero, "THORNS", false);

    	if (selfStun < 0 && hero.isAlive()) {
    		Buff.affect(hero, Paralysis.class, 2f);
    	}
    	if (selfSlow < 0 && hero.isAlive()) {
    		Buff.affect(hero, Slow.class, 3f);
    	}
    	if (selfLifesteal < 0 && hero.isAlive()) {
    		hero.damage((int)(damage * Math.abs(selfLifesteal)), hero);
    	}
    	if (selfThorns < 0 && hero.isAlive()) {
    		hero.damage((int)(damage * Math.abs(selfThorns)), hero);
    	}

    	// 概率型反噬（仅负面净效果，封顶15%）
    	float selfTerror = negativeOnly(hero, "TERROR");
    	float selfSleep = negativeOnly(hero, "SLEEP");
    	float selfCharm = negativeOnly(hero, "CHARM");
    	float selfAmok = negativeOnly(hero, "AMOK");

    	if (selfTerror < 0 && hero.isAlive() && Random.Float() < Math.min(MAX_PROBABILITY, Math.abs(selfTerror))) {
    		Buff.affect(hero, Terror.class, 2f);
    	}
    	if (selfSleep < 0 && hero.isAlive() && Random.Float() < Math.min(MAX_PROBABILITY, Math.abs(selfSleep))) {
    		Buff.affect(hero, Sleep.class, 2f);
    	}
    	if (selfCharm < 0 && hero.isAlive() && Random.Float() < Math.min(MAX_PROBABILITY, Math.abs(selfCharm))) {
    		Buff.affect(hero, Charm.class, 2f);
    	}
    	if (selfAmok < 0 && hero.isAlive() && Random.Float() < Math.min(MAX_PROBABILITY, Math.abs(selfAmok))) {
    		Buff.affect(hero, Vertigo.class, 2f);
    	}
    }

    // ===== 受击触发 =====
    public static int onDefenseProc(Hero hero, Char enemy, int damage) {
        int thornsDmg = (int)(damage * getThornsPercent(hero));
        
        // 隐身触发（受击时概率隐身）
        float invisibleChance = getInvisibleChance(hero);
        if (invisibleChance > 0 && hero.isAlive() && Random.Float() < invisibleChance && thornsDmg == 0) {
            Buff.affect(hero, Invisibility.class, 2f);
        }
        
        return thornsDmg;
    }

    // ===== 移动/地型触发 =====
    // 在有陷阱/深坑的地形移动时触发漂浮
    public static void onMoveProc(Hero hero) {
        // 漂浮触发
        float levitateChance = getLevitateChance(hero);
        if (levitateChance > 0 && Random.Float() < levitateChance) {
            Buff.affect(hero, Levitation.class, 3f);
        }
    }

    // ===== 特殊被动效果查询 =====
    public static float getInvisibleChance(Hero hero) {
        return cappedProb(hero, "INVISIBLE");
    }
    public static float getLevitateChance(Hero hero) {
        return cappedProb(hero, "LEVITATE");
    }
    public static float getCleanseChance(Hero hero) {
        return Math.min(MAX_PROBABILITY, sumEffect(hero, "CLEANSE", true));
    }

    // ===== 概率上限辅助 =====
    // 概率型效果统一封顶15%，负数（负面反噬）取绝对值后再封顶

    // 计算仅负面净效果（all - positive），用于反噬判定
    private static float negativeOnly(Hero hero, String type) {
        return sumEffect(hero, type, false) - sumEffect(hero, type, true);
    }

    private static float cappedProb(Hero hero, String type) {
        float raw = sumEffect(hero, type, true);
        if (raw >= 0) return Math.min(MAX_PROBABILITY, raw);
        // 负面：取和的绝对值（负面值原本就是负数）
        float neg = sumEffect(hero, type, false);
        if (neg >= 0) return 0; // 没有负面净效果
        return Math.min(MAX_PROBABILITY, Math.abs(neg));
    }

    // ===== 内部辅助 =====
    // 净化一个负面状态
    private static void cleanseOneDebuff(Hero hero) {
        if (hero == null) return;
        // 按优先级净化：眩晕 > 恐惧 > 睡眠 > 魅惑 > 混乱 > 残废 > 减速 > 虚弱 > 腐蚀 > 中毒
        Class<? extends Buff>[] debuffPriority = new Class[] {
            Paralysis.class, Terror.class, Sleep.class, Charm.class,
            Vertigo.class, Cripple.class, Slow.class, Weakness.class,
            Corrosion.class, Poison.class, Bleeding.class, Vulnerable.class
        };
        for (Class<? extends Buff> cls : debuffPriority) {
            Buff b = hero.buff(cls);
            if (b != null) {
                b.detach();
                break;
            }
        }
    }

    private static float sumEffect(Hero hero, String type, boolean positiveOnly) {
        if (hero == null || Dungeon.traits == null) return 0;
        ArrayList<Trait> traits = Dungeon.traits.getCollectedTraits();
        float total = 0;
        for (Trait t : traits) {
            if (t.getEffectType().equals(type)) {
                if (positiveOnly && !t.isPositive()) continue;
                total += t.getActualEffect();
            }
            // 负面MISC词条（"全属性降低"）作为减益叠加到所有属性类型上
            if (!type.equals("MISC")
                && t.getEffectType().equals("MISC")
                && !t.isPositive()
                && !positiveOnly) {
                total += t.getActualEffect();
            }
        }
        return total;
    }
}
