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

package com.shatteredpixel.shatteredpixeldungeon.items.quiz;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Trait;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TaiyiHolyGrail;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AbyssHorn;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ThiefSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 问答系统 — 纯本地，无 AI / 无网络依赖
 * 玩家每走一定步数触发一次问答
 * 答对奖励，答错惩罚
 */
public class QuizSystem implements Bundlable {

	// 每 N 步触发一次问答
	private static final int STEPS_PER_QUIZ = 80;
	private static final int STEPS_VARIANCE = 20;

	private int stepsSinceLastQuiz = 0;
	private int nextTriggerSteps; // 达到此步数触发

	public QuizSystem() {
		nextTriggerSteps = STEPS_PER_QUIZ + Random.Int(-STEPS_VARIANCE, STEPS_VARIANCE);
		if (nextTriggerSteps < 30) nextTriggerSteps = 30;

		// 预加载题库（在游戏初始化时异步执行，避免首次触发时卡顿）
		com.watabou.noosa.Game.runOnRenderThread(() -> StaticQuestionBank.load());
	}

	/**
	 * 每走一步调用。当达到触发步数时返回 true
	 */
	public boolean onStep() {
		stepsSinceLastQuiz++;
		if (stepsSinceLastQuiz >= nextTriggerSteps) {
			stepsSinceLastQuiz = 0;
			nextTriggerSteps = STEPS_PER_QUIZ + Random.Int(-STEPS_VARIANCE, STEPS_VARIANCE);
			if (nextTriggerSteps < 30) nextTriggerSteps = 30;
			return true;
		}
		return false;
	}

	/**
	 * 获取下一道题目（从预生成题库加载，无 AI / 无网络）
	 */
	public QuestionBank.Question getNextQuestion() {
		return StaticQuestionBank.getRandomQuestion();
	}

	/**
	 * 答对奖励 — 包含词条等级奖励
	 */
	public static String applyReward(Hero hero) {
		// 必给：词条可加点数 +1
		Dungeon.traits.addBonusPoints(1);
		StringBuilder msg = new StringBuilder("答对了！词条点数 +1");

		// 随机奖励分类
		float r = Random.Float();
		if (r < 0.10f) {
			// 10% 给一个随机词条
			Dungeon.traits.grantTrait(Dungeon.depth);
			msg.append("，获得一个随机词条！");

		} else if (r < 0.25f) {
			// 15% 回复满血
			hero.HP = hero.HT;
			msg.append("，完全恢复生命值！");

		} else if (r < 0.35f) {
			// 10% 药水类
			if (Random.Float() < 0.5f) {
				Item item = new PotionOfHealing();
				msg.append("，获得一瓶治疗药水！");
				if (!item.collect()) Dungeon.level.drop(item, hero.pos);
			} else {
				Item item = new PotionOfStrength();
				msg.append("，获得一瓶力量药水！");
				if (!item.collect()) Dungeon.level.drop(item, hero.pos);
			}

		} else if (r < 0.45f) {
			// 10% 治疗药水
			Item item = new PotionOfHealing();
			msg.append("，获得一瓶治疗药水！");
			if (!item.collect()) Dungeon.level.drop(item, hero.pos);

		} else if (r < 0.55f) {
			// 10% 卷轴类
			float sr = Random.Float();
			Item item;
			if (sr < 0.20f) item = new ScrollOfIdentify();
			else if (sr < 0.40f) item = new ScrollOfUpgrade();
			else if (sr < 0.55f) item = new ScrollOfRemoveCurse();
			else if (sr < 0.70f) item = new ScrollOfMagicMapping();
			else if (sr < 0.80f) item = new ScrollOfTeleportation();
			else if (sr < 0.90f) item = new ScrollOfRage();
			else item = new ScrollOfMirrorImage();
			String name = item.name();
			msg.append("，获得" + name + "！");
			if (!item.collect()) Dungeon.level.drop(item, hero.pos);

		} else if (r < 0.60f) {
			// 5% 随机神器（不重复）
			java.util.ArrayList<String> owned = new java.util.ArrayList<>();
			for (com.shatteredpixel.shatteredpixeldungeon.items.Item i : Dungeon.hero.belongings.backpack.items) {
				owned.add(i.getClass().getName());
			}
			if (Dungeon.hero.belongings.artifact != null) owned.add(Dungeon.hero.belongings.artifact.getClass().getName());
			for (com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc m : Dungeon.hero.belongings.misc) {
				if (m != null) owned.add(m.getClass().getName());
			}
			java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.items.Item> candidates = new java.util.ArrayList<>();
			for (Class cls : new Class[]{TaiyiHolyGrail.class, AbyssHorn.class, ThiefSeal.class,
					ChaliceOfBlood.class, HornOfPlenty.class, MasterThievesArmband.class, AlchemistsToolkit.class}) {
				if (!owned.contains(cls.getName())) {
					try { candidates.add((com.shatteredpixel.shatteredpixeldungeon.items.Item) cls.newInstance()); }
					catch (Exception e) {}
				}
			}
			if (!candidates.isEmpty()) {
				Item item = Random.element(candidates);
				msg.append("，获得神器：" + item.name() + "！");
				if (!item.collect()) Dungeon.level.drop(item, hero.pos);
			} else {
				msg.append("（神器已集齐）");
			}

		} else if (r < 0.70f) {
			// 10% 鉴定卷轴
			Item item = new ScrollOfIdentify();
			msg.append("，获得一张鉴定卷轴！");
			if (!item.collect()) Dungeon.level.drop(item, hero.pos);

		} else if (r < 0.80f) {
			// 10% 地图卷轴
			Item item = new ScrollOfMagicMapping();
			msg.append("，获得一张地图卷轴！");
			if (!item.collect()) Dungeon.level.drop(item, hero.pos);

		} else if (r < 0.90f) {
			// 10% 移除诅咒卷轴
			Item item = new ScrollOfRemoveCurse();
			msg.append("，获得一张移除诅咒卷轴！");
			if (!item.collect()) Dungeon.level.drop(item, hero.pos);

		} else {
			// 10% 金币
			int gold = Random.Int(20, 100) * (Dungeon.depth + 1);
			Dungeon.gold += gold;
			msg.append("，获得 " + gold + " 金币！");
		}
		return msg.toString();
	}

	/**
	 * 答错惩罚 — 包含词条等级惩罚
	 */
	public static String applyPunishment(Hero hero) {
		StringBuilder msg = new StringBuilder("答错了！");

		// 1. 15%血量伤害
		int dmg = Math.round(hero.HP * 0.15f);
		if (dmg < 1) dmg = 1;
		hero.damage(dmg, new QuizSystem());
		msg.append(" 受到 " + dmg + " 点伤害。");

		// 2. 随机附加一个负面状态（各8.89%）
		randomDebuff(hero, msg);

		// 3. 必扣：从已有词条中随机抽取一个扣2级
		ArrayList<Trait> traits = Dungeon.traits.getCollectedTraits();
		if (traits != null && !traits.isEmpty()) {
			ArrayList<Trait> downgradable = new ArrayList<>();
			for (Trait t : traits) {
				if (t != null && t.getLevel() > t.getMinLevel()) {
					downgradable.add(t);
				}
			}
			if (!downgradable.isEmpty()) {
				Trait target = Random.element(downgradable);
				int deducted = 0;
				for (int i = 0; i < 2; i++) {
					if (target.downgrade()) deducted++;
				}
				msg.append(" 词条「" + target.getName() + "」降低了 " + deducted + " 级！");
			} else {
				msg.append("（所有词条已达最低等级）");
			}
		} else {
			msg.append("（没有可降级的词条）");
		}
		return msg.toString();
	}

	private static void randomDebuff(Hero hero, StringBuilder msg) {
		float r = Random.Float();
		// 8.89% each × 8 = 71.12%, remaining 28.88% = no debuff
		if (r < 0.0889f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison.class).set(6 + Dungeon.depth);
			msg.append(" ☠️中毒6回合");
		} else if (r < 0.1778f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness.class, 6f);
			msg.append(" 👁失明6回合");
		} else if (r < 0.2667f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze.class).set(6f);
			msg.append(" 💧淤泥污秽6回合");
		} else if (r < 0.3556f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class).set(6f);
			msg.append(" 🩸流血6回合");
		} else if (r < 0.4445f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class, 6f);
			msg.append(" 🦽残废6回合");
		} else if (r < 0.5334f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness.class, 6f);
			msg.append(" 😵虚弱6回合");
		} else if (r < 0.6223f) {
			// 饥饿 - 直接减少饱食度
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger hunger = com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.class);
			hunger.satisfy(-com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.STARVING * 0.5f);
			msg.append(" 🍽陷入饥饿");
		} else if (r < 0.7112f) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis.class, 6f);
			msg.append(" 💫眩晕6回合");
		}
		// else: 20%概率无附加负面状态
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put("stepsSinceLastQuiz", stepsSinceLastQuiz);
		bundle.put("nextTriggerSteps", nextTriggerSteps);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		stepsSinceLastQuiz = bundle.getInt("stepsSinceLastQuiz");
		nextTriggerSteps = bundle.getInt("nextTriggerSteps");
	}

	public boolean bundlesLikeATypedMap() {
		return false;
	}
}
