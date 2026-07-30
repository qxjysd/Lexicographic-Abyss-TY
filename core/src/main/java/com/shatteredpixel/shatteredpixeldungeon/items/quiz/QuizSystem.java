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
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
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
		float r = Random.Float();

		if (r < 0.10f) {
			// 10% 给一个随机词条
			Dungeon.traits.grantTrait(Dungeon.depth);
			return "答对了！获得一个随机词条！";

		} else if (r < 0.25f) {
			// 15% 回复满血
			hero.HP = hero.HT;
			return "答对了！奖励：完全恢复生命值！";

		} else if (r < 0.45f) {
			// 20% 给一瓶治疗药水
			Item item = new PotionOfHealing();
			if (item.collect()) {
				return "答对了！获得一瓶治疗药水！";
			} else {
				Dungeon.level.drop(item, hero.pos);
				return "答对了！掉落了一瓶治疗药水！";
			}

		} else if (r < 0.60f) {
			// 15% 给鉴定卷轴
			Item item = new ScrollOfIdentify();
			if (item.collect()) {
				return "答对了！获得一张鉴定卷轴！";
			} else {
				Dungeon.level.drop(item, hero.pos);
				return "答对了！掉落了一张鉴定卷轴！";
			}

		} else if (r < 0.75f) {
			// 15% 给地图卷轴
			Item item = new ScrollOfMagicMapping();
			if (item.collect()) {
				return "答对了！获得一张地图卷轴！";
			} else {
				Dungeon.level.drop(item, hero.pos);
				return "答对了！掉落了一张地图卷轴！";
			}

		} else if (r < 0.90f) {
			// 15% 给移除诅咒卷轴
			Item item = new ScrollOfRemoveCurse();
			if (item.collect()) {
				return "答对了！获得一张移除诅咒卷轴！";
			} else {
				Dungeon.level.drop(item, hero.pos);
				return "答对了！掉落了一张移除诅咒卷轴！";
			}

		} else {
			// 10% 给金币
			int gold = Random.Int(20, 100) * (Dungeon.depth + 1);
			Dungeon.gold += gold;
			return "答对了！获得 " + gold + " 金币！";
		}
	}

	/**
	 * 答错惩罚 — 包含词条等级惩罚
	 */
	public static String applyPunishment(Hero hero) {
		int dmg = Math.round(hero.HP * 0.15f);
		if (dmg < 1) dmg = 1;
		hero.damage(dmg, new QuizSystem());

		// 20%概率随机降级一个词条
		String traitMsg = "";
		if (Random.Float() < 0.20f) {
			ArrayList<Trait> traits = Dungeon.traits.getCollectedTraits();
			if (traits != null && !traits.isEmpty()) {
				// 找可以降级的词条
				ArrayList<Trait> downgradable = new ArrayList<>();
				for (Trait t : traits) {
					if (t != null && t.getLevel() > t.getMinLevel()) {
						downgradable.add(t);
					}
				}
				if (!downgradable.isEmpty()) {
					Trait target = Random.element(downgradable);
					target.downgrade();
					traitMsg = " 词条「" + target.getName() + "」降级了！";
				}
			}
		}

		return "答错了！受到 " + dmg + " 点伤害。" + traitMsg;
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
