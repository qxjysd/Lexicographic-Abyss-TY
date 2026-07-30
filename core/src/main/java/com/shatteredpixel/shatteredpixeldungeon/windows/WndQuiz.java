/*
 * Lexicographic-Abyss by 许玄
 * Copyright (C) 2024-2026 许玄
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.quiz.QuestionBank;
import com.shatteredpixel.shatteredpixeldungeon.items.quiz.QuizSystem;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

/**
 * 问答弹窗 — 纯本地题目，无 AI 依赖
 * 自适屏宽布局：标题→间隙→题目→间隙→四个选项按钮
 */
public class WndQuiz extends Window {

	private static final int BTN_HEIGHT = 22;
	private static final int PAD = 8;

	private int correctIndex;
	private String[] options;
	private String explanation;
	private boolean answered = false;

	public WndQuiz(QuestionBank.Question q) {
		super();

		this.correctIndex = q.correctIndex;
		this.options = q.options;
		this.explanation = q.explanation;

		// 自适应宽度：竖屏取屏幕宽85%，横屏取60%
		int W;
		if (PixelScene.landscape()) {
			W = Math.round(PixelScene.uiCamera.width * 0.60f);
		} else {
			W = Math.round(PixelScene.uiCamera.width * 0.85f);
		}
		if (W < 120) W = 120;
		if (W > 220) W = 220;

		float pos = PAD;

		// ===== 标题区 =====
		RenderedTextBlock title = PixelScene.renderTextBlock("-- 机缘问答 --", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((W - title.width()) / 2f, pos);
		add(title);
		pos = title.bottom() + 10;  // 标题后大间隙

		// ===== 题目区（可滚动区域包裹，适应长题目）=====
		RenderedTextBlock txtQuestion = PixelScene.renderTextBlock(q.question, 7);
		txtQuestion.maxWidth(W - PAD * 2);
		txtQuestion.setPos(PAD, pos);
		add(txtQuestion);
		pos = txtQuestion.bottom() + 8;  // 题目与选项间大间隙

		// 分隔装饰线
		RenderedTextBlock divider = PixelScene.renderTextBlock("-- -- -- -- -- -- -- --", 6);
		divider.hardlight(0x888888);
		divider.setPos((W - divider.width()) / 2f, pos);
		add(divider);
		pos = divider.bottom() + 6;

		// ===== 选项按钮区（每个按钮固定高度，均匀排列）=====
		for (int i = 0; i < 4; i++) {
			final int choice = i;
			RedButton btn = new RedButton((char)('A' + i) + ". " + q.options[i], 7) {
				@Override
				protected void onClick() {
					if (!answered) {
						answered = true;
						onAnswer(choice);
					}
				}
			};
			btn.setRect(PAD, pos, W - PAD * 2, BTN_HEIGHT);
			add(btn);
			pos = btn.bottom() + 3;
		}

		resize(W, (int) pos + PAD);
	}

	private void onAnswer(int choice) {
		Hero hero = Dungeon.hero;
		if (hero == null) return;

		String msg;
		if (choice == correctIndex) {
			msg = QuizSystem.applyReward(hero);
		} else {
			msg = QuizSystem.applyPunishment(hero);
			if (explanation != null && !explanation.isEmpty()) {
				msg += "\n\n正确答案：" + (char)('A' + correctIndex) + ". "
					+ options[correctIndex] + "\n" + explanation;
			}
		}

		final String finalMsg = msg;
		hide();
		Game.runOnRenderThread(() -> {
			if (PixelScene.uiCamera != null) {
				int rW = Math.round(PixelScene.uiCamera.width * 0.65f);
				if (rW < 120) rW = 120;
				if (rW > 200) rW = 200;

				final int rW_final = rW;
				Window result = new Window() {
					{
						RenderedTextBlock txtMsg = PixelScene.renderTextBlock(finalMsg, 7);
						txtMsg.maxWidth(rW_final - PAD * 2);
						txtMsg.setPos(PAD, PAD);
						add(txtMsg);

						RedButton btnOk = new RedButton("确 定", 8) {
							@Override
							protected void onClick() {
								hide();
							}
						};
						btnOk.setRect(PAD, txtMsg.bottom() + 8, rW_final - PAD * 2, 20);
						add(btnOk);

						resize(rW_final, (int) btnOk.bottom() + PAD);
					}
				};
				if (com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene() != null) {
					com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene().addToFront(result);
				}
			}
		});
	}
}
