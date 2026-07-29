/*
 * Lexicographic-Abyss by 许玄
 * Copyright (C) 2024-2026 许玄
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.quiz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 静态题库加载器 — 从预生成的 question_bank.json 加载 10000 道题
 * 38 种题型，场景化包装，无需 AI / 网络
 */
public class StaticQuestionBank {

	private static ArrayList<JsonValue> questions = null;
	private static boolean loaded = false;

	/**
	 * 从 assets/data/question_bank.json 加载题库
	 */
	public static void load() {
		if (loaded) return;
		questions = new ArrayList<>();

		try {
			JsonReader reader = new JsonReader();
			JsonValue root = reader.parse(Gdx.files.internal("data/question_bank.json"));

			JsonValue questionArray = root.get("questions");
			for (JsonValue q : questionArray) {
				questions.add(q);
			}

			loaded = true;

		} catch (Exception e) {
			Game.reportException(new RuntimeException("StaticQuestionBank: failed to load question bank", e));
			loaded = false;
		}
	}

	/**
	 * 获取随机题目
	 */
	public static QuestionBank.Question getRandomQuestion() {
		if (!loaded) {
			load();
		}
		if (!loaded || questions == null || questions.isEmpty()) {
			// 兜底：使用实时生成（仅当题库加载失败时）
			return QuestionBank.getRandomQuestion();
		}

		JsonValue entry = Random.element(questions);

		QuestionBank.Question q = new QuestionBank.Question();
		q.question = entry.getString("question");
		q.explanation = entry.getString("explanation", "");
		q.correctIndex = entry.getInt("correctIndex", 0);

		JsonValue optsArr = entry.get("options");
		q.options = new String[optsArr.size];
		for (int i = 0; i < optsArr.size; i++) {
			q.options[i] = optsArr.getString(i);
		}

		return q;
	}

	public static boolean isLoaded() { return loaded; }
	public static int size() {
		if (questions == null) return 0;
		return questions.size();
	}
}
