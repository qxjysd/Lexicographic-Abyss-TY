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
 */
public class WndQuiz extends Window {

	private static final int WIDTH = 150;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 2;

	private int correctIndex;
	private String[] options;
	private String explanation;
	private boolean answered = false;

	public WndQuiz(QuestionBank.Question q) {
		super();

		this.correctIndex = q.correctIndex;
		this.options = q.options;
		this.explanation = q.explanation;

		float pos = 0;

		// 标题
		RenderedTextBlock title = PixelScene.renderTextBlock("📖 机缘问答", 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2f, pos);
		add(title);
		pos = title.bottom() + 2;

		// 题目文本
		RenderedTextBlock txtQuestion = PixelScene.renderTextBlock(q.question, 7);
		txtQuestion.maxWidth(WIDTH - 8);
		txtQuestion.setPos(4, pos);
		add(txtQuestion);
		pos = txtQuestion.bottom() + GAP + 2;

		// 四个选项按钮
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
			btn.setRect(4, pos, WIDTH - 8, BTN_HEIGHT);
			add(btn);
			pos = btn.bottom() + 1;
		}

		resize(WIDTH, (int) pos + 4);
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
				Window result = new Window() {
					{
						RenderedTextBlock txtMsg = PixelScene.renderTextBlock(finalMsg, 7);
						txtMsg.maxWidth(130 - 8);
						txtMsg.setPos(4, 6);
						add(txtMsg);

						RedButton btnOk = new RedButton("确 定", 8) {
							@Override
							protected void onClick() {
								hide();
							}
						};
						btnOk.setRect(4, txtMsg.bottom() + 6, 130 - 8, 16);
						add(btnOk);

						resize(130, (int) btnOk.bottom() + 4);
					}
				};
				if (com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene() != null) {
					com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene().addToFront(result);
				}
			}
		});
	}
}
