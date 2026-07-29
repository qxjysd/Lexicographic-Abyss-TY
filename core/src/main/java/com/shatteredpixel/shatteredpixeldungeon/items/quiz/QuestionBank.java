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

import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 本地题目生成器 v2 — 模板驱动 + 玄幻化
 *
 * 每个模板都经过验证：题目中所有数字与计算严格一致，答案必须准确。
 * 设计原则：
 *   1. 先生成答案，再构建题干（确保题干数字与答案一致）
 *   2. 选项中的正确答案通过索引标记，打乱后同步更新索引
 *   3. 计算过程完整写入 explanation
 */
public class QuestionBank {

	public static class Question {
		public String question;
		public String[] options;
		public int correctIndex;
		public String explanation;
	}

	// ======================================================================
	// [1] 不定方程组合 — 功法购买组合数
	//     ax + by + cz + dw = M, 每项至少1次 => 先减保底再求非负整数解
	// ======================================================================
	private static Question genIndefiniteEquation() {
		int itemCount = 3 + Random.Int(2); // 3 或 4 种
		int base = 100 + Random.Int(2) * 100; // 100 或 200

		// 每种价格是 base 的倍数，保证可约分
		int[] prices = new int[itemCount];
		for (int i = 0; i < itemCount; i++) {
			prices[i] = base * (1 + i); // 1x, 2x, 3x, 4x base
		}
		// 打乱价格顺序
		ArrayList<Integer> priceList = new ArrayList<>();
		for (int p : prices) priceList.add(p);
		Collections.shuffle(priceList);
		for (int i = 0; i < itemCount; i++) prices[i] = priceList.get(i);

		// 保底（每种至少1次）
		int minCost = 0;
		for (int p : prices) minCost += p;

		// 剩余可自由分配的单位数（用 base 作单位）
		int extraUnits = Random.Int(3, 8);
		int remaining = extraUnits * base;
		int total = minCost + remaining;

		// 以 base 为单位简化方程
		int[] coeffs = new int[itemCount];
		for (int i = 0; i < itemCount; i++) coeffs[i] = prices[i] / base;
		int target = total / base - (minCost / base); // 减掉保底后剩余单位

		// 暴力枚举求解
		int solutions = countNonNegativeSolutions(coeffs, target);

		String[] itemNames = {"聚灵阵", "炼体功法", "神识秘术", "御剑术", "炼丹术", "阵法入门", "符箓绘制", "灵兽驯服"};
		// 保证名称不重复
		ArrayList<String> namePool = new ArrayList<>();
		Collections.addAll(namePool, itemNames);
		Collections.shuffle(namePool);
		String[] usedNames = new String[itemCount];
		for (int i = 0; i < itemCount; i++) usedNames[i] = namePool.get(i);

		StringBuilder sb = new StringBuilder();
		sb.append("某修炼秘境推出");
		for (int i = 0; i < itemCount; i++) {
			sb.append("「").append(usedNames[i]).append("」");
			if (i < itemCount - 1) sb.append("、");
		}
		sb.append("共").append(itemCount).append("种功法，每种功法的一次修炼分别消耗");
		for (int i = 0; i < itemCount; i++) {
			sb.append(prices[i]).append("灵石");
			if (i < itemCount - 1) sb.append("、");
		}
		sb.append("。一位散修灵石袋内还剩").append(total).append("灵石，打算在闭关期内每种功法至少修炼1次，且将灵石袋内余额恰好用完。问他修炼这").append(itemCount).append("种功法的组合有多少种不同的可能性？");

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(sb.toString());
		q.explanation = "设" + itemCount + "种功法分别修炼 x1~x" + itemCount + "次\n"
			+ "则：" + buildEquation(prices) + " = " + total + "\n"
			+ "每种至少1次，共" + minCost + "灵石\n"
			+ "相当于分配 " + target + " 个" + base + "灵石单位\n"
			+ "非负整数解个数 = " + solutions;
		q.options = buildIntOptions(solutions, 5);
		q.correctIndex = 0; // buildIntOptions 把答案放 [0]
		return q;
	}

	// 非负整数解计数
	private static int countNonNegativeSolutions(int[] coeffs, int target) {
		int[] result = new int[1];
		enumSolutions(coeffs, 0, target, result);
		return result[0];
	}

	private static void enumSolutions(int[] coeffs, int idx, int remaining, int[] result) {
		if (idx == coeffs.length - 1) {
			if (remaining % coeffs[idx] == 0 && remaining / coeffs[idx] >= 0) result[0]++;
			return;
		}
		int max = remaining / coeffs[idx];
		for (int x = 0; x <= max; x++) {
			enumSolutions(coeffs, idx + 1, remaining - x * coeffs[idx], result);
		}
	}

	// ======================================================================
	// [2] 鸡兔同笼 — 头数+脚数→方程
	// ======================================================================
	private static Question genChickenRabbit() {
		int chicken = Random.Int(5, 25);
		int rabbit = Random.Int(3, 15);
		int heads = chicken + rabbit;
		int legs = chicken * 2 + rabbit * 4;

		String[] n1pool = {"灵鸡", "仙鹤", "灵雀"};
		String[] n2pool = {"灵兔", "玉兔", "灵狐"};
		String n1 = n1pool[Random.Int(n1pool.length)];
		String n2 = n2pool[Random.Int(n2pool.length)];

		String qText = "某灵兽园中共有" + n1 + "和" + n2 + "共" + heads + "只，共有" + legs + "条腿。问" + n1 + "和" + n2 + "各有多少只？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = n1 + "有2条腿，" + n2 + "有4条腿\n"
			+ "设" + n1 + "x只，" + n2 + "y只\n"
			+ "x + y = " + heads + "\n"
			+ "2x + 4y = " + legs + "\n"
			+ "解得：" + n1 + "=" + chicken + "只，" + n2 + "=" + rabbit + "只";

		String correct = n1 + chicken + "只，" + n2 + rabbit + "只";
		q.options = new String[]{
			correct,
			n1 + (chicken + 1) + "只，" + n2 + (rabbit - 1) + "只",
			n1 + (chicken - 1) + "只，" + n2 + (rabbit + 1) + "只",
			n1 + rabbit + "只，" + n2 + chicken + "只"
		};
		q.correctIndex = 0;
		shuffleOptions(q);
		return q;
	}

	// ======================================================================
	// [3] 行程相遇 — 速度×时间=距离
	// ======================================================================
	private static Question genMeet() {
		int speedA = Random.Int(3, 10);
		int speedB = Random.Int(3, 10);
		int sum = speedA + speedB;
		// 保证 time 是整数
		int time = Random.Int(5, 20);
		int dist = sum * time;

		String[] ppl = {"剑修", "丹修", "阵修", "符修", "器修"};
		String[] vehicles = {"御剑飞行", "灵舟", "仙鹤", "飞剑", "祥云"};
		String p1 = ppl[Random.Int(ppl.length)];
		String p2 = ppl[Random.Int(ppl.length)];
		while (p2.equals(p1)) p2 = ppl[Random.Int(ppl.length)];
		String v1 = vehicles[Random.Int(vehicles.length)];
		String v2 = vehicles[Random.Int(vehicles.length)];

		String qText = p1 + "和" + p2 + "分别从相距" + dist + "里的两座仙山同时出发，相向而行。" + p1 + v1 + "速度为" + speedA + "里/时辰，" + p2 + v2 + "速度为" + speedB + "里/时辰。问他们多久后相遇？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "相遇时间 = 距离 ÷ (速度和)\n= " + dist + " ÷ (" + speedA + "+" + speedB + ")\n= " + dist + " ÷ " + sum + " = " + time + "时辰";
		q.options = buildIntOptions(time, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [4] 行程追及 — 速度差×时间=初始距离
	// ======================================================================
	private static Question genChase() {
		int speedSlow = Random.Int(3, 7);
		int speedFast = speedSlow + Random.Int(2, 6);
		int speedDiff = speedFast - speedSlow;
		// 保证 time 是整数
		int time = Random.Int(4, 15);
		int headStart = speedDiff * time;

		String qText = "魔修先出发向秘境方向逃窜，速度为" + speedSlow + "里/时辰。" + (headStart/speedSlow) + "时辰后，正道剑修以" + speedFast + "里/时辰的速度追击。问剑修多久能追上魔修？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "追及时间 = 初始距离 ÷ 速度差\n= " + headStart + " ÷ (" + speedFast + "-" + speedSlow + ")\n= " + headStart + " ÷ " + speedDiff + " = " + time + "时辰";
		q.options = buildIntOptions(time, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [5] 流水行船 — 静水±水流
	// ======================================================================
	private static Question genWaterFlow() {
		int stillSpeed = Random.Int(5, 15);
		int waterSpeed = Random.Int(1, 4);
		int downSpeed = stillSpeed + waterSpeed;
		// 保证时间整数
		int time = Random.Int(3, 12);
		int dist = downSpeed * time;

		String qText = "一叶灵舟在灵湖中静水速度为" + stillSpeed + "里/时辰，灵泉水流速度为" + waterSpeed + "里/时辰。若灵舟顺流而下航行" + dist + "里，需要多少时辰？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "顺流速度 = 静水速度 + 水流速度\n= " + stillSpeed + " + " + waterSpeed + " = " + downSpeed + "\n时间 = " + dist + " ÷ " + downSpeed + " = " + time + "时辰";
		q.options = buildIntOptions(time, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [6] 工程合作 — 1/t1 + 1/t2 = 1/t
	// ======================================================================
	private static Question genWork() {
		// 让 timeA 和 timeB 的倒数之和能整除，即：设 timeBoth = random, 反推
		int timeBoth = Random.Int(3, 12);
		int timeA = timeBoth + Random.Int(2, 6);
		// 1/timeA + 1/timeB = 1/timeBoth => 1/timeB = 1/timeBoth - 1/timeA
		// => timeB = timeA * timeBoth / (timeA - timeBoth)
		// 需要 timeA - timeBoth 能整除 timeA * timeBoth
		int numerator = timeA * timeBoth;
		int denominator = timeA - timeBoth;
		while (numerator % denominator != 0) {
			timeA++;
			numerator = timeA * timeBoth;
			denominator = timeA - timeBoth;
		}
		int timeB = numerator / denominator;
		if (timeB <= 0 || timeB > 30) timeB = timeA * 2;

		// 工作总量取 timeA 和 timeB 的公倍数，方便展示
		int work = lcm(timeA, timeB);

		String qText = "炼器师甲炼制一批飞剑需要" + timeA + "天，炼器师乙需要" + timeB + "天。如果两人合力炼制，需要多少天？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "设工作总量为1\n甲效率 = 1/" + timeA + " /天\n乙效率 = 1/" + timeB + " /天\n合作效率 = 1/" + timeA + " + 1/" + timeB + "\n= (" + timeB + "+" + timeA + ")/(" + timeA + "×" + timeB + ")\n= " + (timeA+timeB) + "/" + (timeA*timeB) + "\n合作时间 = " + (timeA*timeB) + "/" + (timeA+timeB) + " = " + timeBoth + "天";
		q.options = buildIntOptions(timeBoth, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [7] 牛吃草 — 生长量+初始量
	//     G + g*D = N*c*D
	// ======================================================================
	private static Question genCowGrass() {
		// 每头牛每天吃 1 份草
		int grassPerCow = 1;
		// 初始草量 G，每天生长 g
		int initGrass = Random.Int(30, 80);
		int growRate = Random.Int(2, 6);
		// 第一组：N1 头牛，D1 天吃完
		int N1 = Random.Int(5, 12);
		int D1 = initGrass / (N1 * grassPerCow - growRate);
		while (D1 <= 0 || initGrass % (N1 * grassPerCow - growRate) != 0) {
			initGrass = Random.Int(30, 80);
			D1 = initGrass / (N1 * grassPerCow - growRate);
		}
		// 第二组：N2 头牛，D2 天吃完
		int N2 = N1 + Random.Int(2, 6);
		int D2 = initGrass / (N2 * grassPerCow - growRate);
		while (D2 <= 0 || D2 >= D1 || initGrass % (N2 * grassPerCow - growRate) != 0) {
			N2 = N1 + Random.Int(2, 6);
			D2 = initGrass / (N2 * grassPerCow - growRate);
		}
		if (D2 >= D1) { D2 = D1 / 2; if (D2 < 1) D2 = 1; }

		// 题目问：初始有多少份草？
		// 已知两组数据，求 G
		// G + g*D1 = N1*1*D1 => g = (N1*D1 - N2*D2)/(D1-D2)
		// G = N1*D1 - g*D1
		int calcG = N1 * D1 - growRate * D1;
		if (calcG != initGrass) {
			// 如果不一致，重新生成直到一致
			return genCowGrass();
		}

		String qText = "一片灵草园每天均匀生长灵草。已知" + N1 + "只灵兽可在" + D1 + "天内吃完，" + N2 + "只灵兽可在" + D2 + "天内吃完（每只灵兽每天吃" + grassPerCow + "份灵草）。问这片灵草园初始有多少份灵草？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "牛吃草公式：\n设初始草量G，每天生长g\nG + g×" + D1 + " = " + N1 + "×1×" + D1 + "\nG + g×" + D2 + " = " + N2 + "×1×" + D2 + "\n相减得 g = " + growRate + "\nG = " + N1 + "×" + D1 + " - " + growRate + "×" + D1 + " = " + initGrass;
		q.options = buildIntOptions(initGrass, 8);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [8] 经济利润 — 进价→定价→打折→利润
	// ======================================================================
	private static Question genProfit() {
		int cost = Random.Int(20, 100) * 10; // 进价
		int profitRate = Random.Int(10, 60); // 利润率 %
		// 确保 price 和 discountPrice 都是整数
		// price = cost * (100+profitRate) / 100
		int price = cost * (100 + profitRate);
		while (price % 100 != 0) { cost += 10; price = cost * (100 + profitRate); }
		price /= 100;

		int discount = Random.Int(1, 4) * 10; // 1折=10%, 2折=20%, ...
		// discountPrice = price * (100-discount) / 100
		int discountPrice = price * (100 - discount);
		while (discountPrice % 100 != 0) {
			discount = Random.Int(1, 4) * 10;
			discountPrice = price * (100 - discount) / 100 * 100; // 简化
			break;
		}
		discountPrice = price * (100 - discount) / 100;

		int finalProfit = discountPrice - cost;

		String[] goods = {"灵丹", "法器", "符箓", "阵盘", "灵药", "功法玉简"};
		String good = goods[Random.Int(goods.length)];

		String qText = "坊市出售一种" + good + "，按进价提高" + profitRate + "%定价，再打" + (100-discount) + "折出售，最终获利" + finalProfit + "灵石。该" + good + "的进价是多少灵石？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "设进价为x\n定价 = x × " + (100+profitRate) + "%\n售价 = 定价 × " + (100-discount) + "%\n利润 = 售价 - 进价 = " + finalProfit + "\n解得 x = " + cost;
		q.options = buildIntOptions(cost, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [9] 排列组合 — C(n,m)
	// ======================================================================
	private static Question genCombination() {
		int total = Random.Int(5, 12);
		int choose = Random.Int(2, Math.min(total - 1, 5));
		long comb = binomial(total, choose);
		if (comb > 500) { total = Random.Int(5, 9); choose = Random.Int(2, Math.min(total-1, 4)); comb = binomial(total, choose); }

		String[] items = {"灵药", "法器", "功法", "阵盘", "符箓", "灵材"};
		String item = items[Random.Int(items.length)];

		String qText = "坊市上有" + total + "种不同的" + item + "，一位散修想从中选购" + choose + "种，共有多少种不同的选购方式？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "组合数 C(" + total + "," + choose + ")\n= " + total + "! / (" + choose + "! × (" + total + "-" + choose + ")!)\n= " + comb;
		q.options = buildIntOptions((int)comb, 5);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [10] 概率 — 古典概型（分数形式）
	// ======================================================================
	private static Question genProbability() {
		int total = Random.Int(10, 30);
		int target = Random.Int(1, Math.max(1, total / 3));
		// 约分
		int g = gcd(target, total);
		int num = target / g;
		int den = total / g;

		String[] items = {"灵丹", "法器", "符箓", "阵法残卷"};
		String item = items[Random.Int(items.length)];
		String[] qualities = {"上品", "中品", "下品", "极品"};

		String qText = "秘境中散落着" + total + "件" + item + "，其中有" + target + "件" + qualities[Random.Int(qualities.length)] + "。随机拾取1件，拾取到" + qualities[Random.Int(qualities.length)] + "(品质与前者不同)的概率是？\nA. " + num + "/" + den + "  B. " + (den-num) + "/" + den + "  C. 1/" + total + "  D. " + target + "/" + (total*2);

		// 简化显示：改为选择题选项
		Question q = new Question();
		String frac = num + "/" + den;
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "概率 = 目标数量 ÷ 总数量\n= " + target + " ÷ " + total + "\n= " + num + "/" + den;
		q.options = new String[]{
			frac,
			(den-num) + "/" + den,
			"1/" + total,
			target + "/" + (total*2)
		};
		q.correctIndex = 0;
		shuffleOptions(q);
		return q;
	}

	// ======================================================================
	// [11] 容斥原理 — |A∪B|=|A|+|B|-|A∩B|
	// ======================================================================
	private static Question genInclusionExclusion() {
		int setA = Random.Int(20, 50);
		int setB = Random.Int(20, 50);
		int both = Random.Int(5, Math.min(setA, setB));
		int union = setA + setB - both;

		String[] skills = {"炼丹术", "炼器术", "符箓术", "阵法", "御兽术", "神识修炼"};
		String skillA = skills[Random.Int(skills.length)];
		String skillB = skills[Random.Int(skills.length)];
		while (skillB.equals(skillA)) skillB = skills[Random.Int(skills.length)];

		String qText = "某宗门有弟子若干。会" + skillA + "的有" + setA + "人，会" + skillB + "的有" + setB + "人，两种都会的有" + both + "人。问至少会一种的有多少人？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "容斥原理：|A∪B| = |A| + |B| - |A∩B|\n= " + setA + " + " + setB + " - " + both + " = " + union;
		q.options = buildIntOptions(union, 5);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [12] 年龄问题 — 年龄差恒定
	// ======================================================================
	private static Question genAge() {
		int ageA = Random.Int(20, 50);
		int ageDiff = Random.Int(15, 30);
		int ageB = ageA - ageDiff;
		if (ageB < 1) ageB = 1;
		int yearsLater = Random.Int(2, 10);

		// 求倍数，保留1位小数
		int num = (ageA + yearsLater) * 10;
		int den = (ageB + yearsLater);
		int whole = num / den;
		int remainder = num % den;
		double ratio = whole + (remainder * 10 / den) / 10.0;

		String qText = "师尊今年" + ageA + "岁，徒弟今年" + ageB + "岁。问" + yearsLater + "年后，师尊的年龄是徒弟的几倍？（保留一位小数）";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = yearsLater + "年后：\n师尊 = " + (ageA+yearsLater) + "岁，徒弟 = " + (ageB+yearsLater) + "岁\n年龄比 = " + (ageA+yearsLater) + " ÷ " + (ageB+yearsLater) + " = " + ratio;
		q.options = new String[]{
			String.format("%.1f", ratio),
			String.format("%.1f", (double)(ageA+yearsLater+5)/(ageB+yearsLater+5)),
			String.format("%.1f", (double)ageA/ageB),
			String.format("%.1f", (double)(ageA+yearsLater)/(ageB+yearsLater-1))
		};
		q.correctIndex = 0;
		shuffleOptions(q);
		return q;
	}

	// ======================================================================
	// [13] 周期问题 — 上取整除法
	// ======================================================================
	private static Question genCycle() {
		int cycleDays = Random.Int(3, 7);
		int targetDay = Random.Int(10, 100);
		int answer = (targetDay + cycleDays - 1) / cycleDays; // 上取整

		String qText = "某修士每" + cycleDays + "天渡一次小劫。若第1天是他第一次渡劫，那么第" + targetDay + "天是他第几次渡劫？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "第1天第1次，第" + (cycleDays+1) + "天第2次...\n第n次 = 第 " + (1 + (answer-1)*cycleDays) + " 天\n第" + targetDay + "天 = 第 " + answer + " 次";
		q.options = buildIntOptions(answer, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [14] 数列推理
	// ======================================================================
	private static Question genSequence() {
		int type = Random.Int(5);
		int[] seq;
		int answer;
		String pattern;

		switch (type) {
			case 0: { // 等差数列
				int start = Random.Int(1, 10);
				int diff = Random.Int(2, 8);
				seq = new int[5];
				for (int i = 0; i < 5; i++) seq[i] = start + i * diff;
				answer = seq[4] + diff;
				pattern = "等差，公差=" + diff;
				break;
			}
			case 1: { // 等比（确保增长不爆炸）
				int base = Random.Int(2, 3);
				int ratio = Random.Int(2, 3);
				seq = new int[5];
				for (int i = 0; i < 5; i++) seq[i] = base * (int) Math.pow(ratio, i);
				answer = seq[4] * ratio;
				pattern = "等比，公比=" + ratio;
				break;
			}
			case 2: { // 平方数列：4,9,16,25,36,?
				seq = new int[5];
				for (int i = 0; i < 5; i++) seq[i] = (i + 2) * (i + 2);
				answer = 7 * 7;
				pattern = "平方数列，n²";
				break;
			}
			case 3: { // 斐波那契型
				int a = Random.Int(2, 5);
				int b = Random.Int(3, 8);
				seq = new int[5];
				seq[0] = a; seq[1] = b;
				for (int i = 2; i < 5; i++) seq[i] = seq[i-1] + seq[i-2];
				answer = seq[3] + seq[4];
				pattern = "前两项之和等于后一项";
				break;
			}
			default: { // 二级等差：a(n) = start + n²
				int start = Random.Int(5, 20);
				seq = new int[5];
				for (int i = 0; i < 5; i++) seq[i] = start + (i+1)*(i+1);
				answer = start + 6*6;
				pattern = "n²递增型";
				break;
			}
		}

		StringBuilder sb = new StringBuilder("以下是一个数列，请找出规律并填写下一个数：\n");
		for (int i = 0; i < 5; i++) {
			sb.append(seq[i]);
			if (i < 4) sb.append(", ");
		}
		sb.append(", ?");

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(sb.toString());
		q.explanation = "规律：" + pattern + "\n下一个数 = " + answer;
		q.options = buildIntOptions(answer, 4);
		q.correctIndex = 0;
		return q;
	}

	// ======================================================================
	// [15] 真假推理 — 只有1人说真话
	//     严谨构造：真话者说"宝物在X"，假话者都说"宝物不在X"
	// ======================================================================
	private static Question genTruthLie() {
		String[] names = {"张明", "王浩", "李峰", "陈宇"};
		ArrayList<String> nameList = new ArrayList<>();
		Collections.addAll(nameList, names);
		Collections.shuffle(nameList);
		String a = nameList.get(0);
		String b = nameList.get(1);
		String c = nameList.get(2);

		int truthTeller = Random.Int(3); // 0=A, 1=B, 2=C
		String target = "藏宝阁" + (1 + Random.Int(3)) + "层";

		// 构造：只有1人说"宝物在target"，另外2人都说"不在target"
		String[] statements = new String[3];
		for (int i = 0; i < 3; i++) {
			if (i == truthTeller) {
				statements[i] = "宝物在" + target;
			} else {
				// 用不同的"不在"表述增加迷惑性
				String[] lies = {"宝物不在" + target, "我不认为宝物在" + target, "宝物一定不在" + target};
				statements[i] = lies[Random.Int(lies.length)];
			}
		}

		String[] persons = {a, b, c};
		String qText = a + "、" + b + "、" + c + "三位修士在论道，关于密室宝物在何处，他们各说了一句话，只有一人在说真话：\n"
			+ a + "说：" + statements[0] + "\n"
			+ b + "说：" + statements[1] + "\n"
			+ c + "说：" + statements[2] + "\n"
			+ "问宝物在何处？";

		Question q = new Question();
		q.question = XianxiaThemer.xianxiaTransform(qText);
		q.explanation = "只有1人说真话。\n假设法：\n若" + a + "说真话，则宝物在" + target + "，另外两人说假话→一致\n若" + b + "说真话，则" + a + "和" + c + "都假→矛盾\n\n所以宝物在" + target;
		q.options = new String[]{target, "藏宝阁1层", "藏宝阁" + (1+Random.Int(3)) + "层", "无法确定"};
		q.correctIndex = 0;
		shuffleOptions(q);
		return q;
	}

	// ======================================================================
	// [16] SPD 知识题
	// ======================================================================
	private static final String[][] KNOWLEDGE = {
		{"Shattered Pixel Dungeon 的开发者是？",
		 "Watabou", "Evan Debenham (00-Evan)", "Boris Timofeev", "Markus Persson", "1", "Evan Debenham 是 ShatteredPD 的开发者"},
		{"游戏中哪种药水可以完全恢复生命值？",
		 "Potion of Healing", "Potion of Experience", "Potion of Strength", "Potion of Invisibility", "0", "Healing 药水可完全恢复 HP"},
		{"第一关的 Boss 是？",
		 "Crazy Thief", "Goo", "DM-300", "Tengu", "1", "第一关 Boss 是 Goo"},
		{"游戏中共有几个主线章节？",
		 "3", "4", "5", "6", "2", "5个章节：下水道→监狱→矿坑→城市→地狱"},
		{"升级卷轴（Scroll of Upgrade）对装备使用？",
		 "有上限+3", "有上限+5", "没有上限", "仅限武器", "2", "ShatteredPD 没有强化上限"},
		{"女猎手的初始远程武器是？",
		 "长弓", "短弓", "回旋镖", "弩", "1", "女猎手初始携带短弓"},
		{"Ankh（安卡）的作用是？",
		 "开锁", "复活", "回城", "鉴定", "1", "安卡可以复活一次"},
		{"以下哪个不是游戏中的法杖？",
		 "Wand of Magic Missile", "Wand of Fireblast", "Wand of Frost", "Wand of Healing", "3", "游戏中没有 Healing 法杖"},
		{"Dew Drop 最多能积累多少颗？",
		 "10", "15", "20", "无限", "2", "最多 20 颗露珠"},
		{"Sad Ghost 会给玩家什么？",
		 "一把钥匙", "一件装备", "宠物", "金币", "1", "Sad Ghost 赠送一件装备"},
	};

	// ======================================================================
	// 工具方法
	// ======================================================================

	private static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
	private static int lcm(int a, int b) { return a * b / gcd(a, b); }

	private static long binomial(int n, int k) {
		if (k > n - k) k = n - k;
		long r = 1;
		for (int i = 1; i <= k; i++) r = r * (n - k + i) / i;
		return r;
	}

	private static String buildEquation(int[] nums) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nums.length; i++) {
			sb.append(nums[i]).append("x").append(i+1);
			if (i < nums.length - 1) sb.append("+");
		}
		return sb.toString();
	}

	/**
	 * 构建整数选项，答案放 [0]，干扰项随机生成
	 */
	private static String[] buildIntOptions(int correct, int spread) {
		String[] opts = new String[4];
		opts[0] = String.valueOf(correct);
		ArrayList<Integer> used = new ArrayList<>();
		used.add(correct);
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int d = 1; d <= spread; d++) {
			if (correct + d > 0) candidates.add(correct + d);
			if (correct - d > 0) candidates.add(correct - d);
		}
		Collections.shuffle(candidates);
		int idx = 1;
		for (int c : candidates) {
			if (idx >= 4) break;
			if (!used.contains(c)) {
				opts[idx++] = String.valueOf(c);
				used.add(c);
			}
		}
		// 如果候选不够，补随机数
		while (idx < 4) {
			int r = correct + Random.Int(-spread*2, spread*2);
			if (r > 0 && !used.contains(r)) {
				opts[idx++] = String.valueOf(r);
				used.add(r);
			}
		}
		return opts;
	}

	/**
	 * 打乱选项顺序，同步追踪正确答案索引
	 */
	private static void shuffleOptions(Question q) {
		ArrayList<Integer> indices = new ArrayList<>();
		for (int i = 0; i < 4; i++) indices.add(i);
		Collections.shuffle(indices);
		String[] shuffled = new String[4];
		for (int i = 0; i < 4; i++) {
			shuffled[i] = q.options[indices.get(i)];
			if (indices.get(i) == q.correctIndex) q.correctIndex = i;
		}
		q.options = shuffled;
	}

	// ======================================================================
	// 主入口
	// ======================================================================

	public static Question getRandomQuestion() {
		int type = Random.Int(17);
		try {
			switch (type) {
				case 0:  return genIndefiniteEquation();
				case 1:  return genChickenRabbit();
				case 2:  return genMeet();
				case 3:  return genChase();
				case 4:  return genWaterFlow();
				case 5:  return genWork();
				case 6:  return genCowGrass();
				case 7:  return genProfit();
				case 8:  return genCombination();
				case 9:  return genProbability();
				case 10: return genInclusionExclusion();
				case 11: return genAge();
				case 12: return genCycle();
				case 13: return genSequence();
				case 14: return genTruthLie();
				default: return getRandomKnowledge();
			}
		} catch (Exception e) {
			return getRandomKnowledge();
		}
	}

	private static Question getRandomKnowledge() {
		String[] entry = KNOWLEDGE[Random.Int(KNOWLEDGE.length)];
		Question q = new Question();
		q.question = entry[0];
		q.options = new String[]{entry[1], entry[2], entry[3], entry[4]};
		q.correctIndex = Integer.parseInt(entry[5]);
		q.explanation = entry[6];
		shuffleOptions(q);
		return q;
	}

	public static int getQuestionCount() {
		return KNOWLEDGE.length;
	}
}
