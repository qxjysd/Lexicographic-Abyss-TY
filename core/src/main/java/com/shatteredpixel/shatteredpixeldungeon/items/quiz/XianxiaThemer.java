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

import java.util.HashMap;
import java.util.Map;

/**
 * 玄幻化文字替换引擎
 * 将现实场景题干 → 修仙/玄幻风格
 */
public class XianxiaThemer {

	// 主题词映射库
	private static final Map<String, String[]> THEME_MAP = new HashMap<>();

	static {
		// === 人物/职业 ===
		THEME_MAP.put("会员|顾客|用户|学生|客户|游客|买家",
			new String[]{"修士", "武者", "灵徒", "散修", "道童", "剑修", "丹修"});
		THEME_MAP.put("工人|员工|职员|师傅|工匠|厨师|服务员",
			new String[]{"炼器师", "炼丹师", "符师", "阵法师", "灵厨", "铸剑师"});
		THEME_MAP.put("经理|老板|店主|商家|卖家",
			new String[]{"宗主", "阁主", "掌柜", "洞主", "坊主"});
		THEME_MAP.put("教练|教师|导师|培训师",
			new String[]{"师尊", "长老", "护法", "执事"});

		// === 地点 ===
		THEME_MAP.put("健身房|运动馆|体育馆|操场|训练场",
			new String[]{"修炼场", "演武殿", "灵武阁", "锻体台", "试炼窟"});
		THEME_MAP.put("学校|学院|大学|培训班|教室",
			new String[]{"宗门", "仙门", "道院", "灵府", "学宫"});
		THEME_MAP.put("工厂|车间|工地|工坊",
			new String[]{"炼器坊", "丹房", "锻造殿", "灵器阁"});
		THEME_MAP.put("商店|超市|商场|市场|集市|店铺",
			new String[]{"灵市", "坊市", "灵宝阁", "万宝楼", "丹阁"});
		THEME_MAP.put("医院|诊所|药店",
			new String[]{"药王谷", "回春堂", "灵医阁"});
		THEME_MAP.put("公司|企业|集团",
			new String[]{"仙盟", "灵界商会", "万宝宗"});

		// === 物品/货币 ===
		THEME_MAP.put("元|块|钱|金额|花费|价格|费用|收费|售价|单价",
			new String[]{"灵石", "灵玉", "灵力值", "仙石"});
		THEME_MAP.put("充值卡|储值卡|会员卡|银行卡|信用卡",
			new String[]{"灵石袋", "玉简", "灵符", "纳物戒"});
		THEME_MAP.put("套餐|方案|计划|课程|项目|服务",
			new String[]{"功法", "丹方", "阵法", "灵技", "秘术"});
		THEME_MAP.put("商品|产品|物品|货物|货品",
			new String[]{"灵药", "法器", "灵材", "天材地宝"});
		THEME_MAP.put("优惠券|折扣券|代金券|抵用券",
			new String[]{"灵符", "机缘令", "福缘帖"});

		// === 动作 ===
		THEME_MAP.put("消费|购买|买入|订购|下单|选购|采购",
			new String[]{"兑换", "换取", "求购", "请购"});
		THEME_MAP.put("销售|卖出|出售|售卖|促销",
			new String[]{"奉送", "赐予", "出让", "易物"});
		THEME_MAP.put("运输|运送|搬运|配送|快递|物流",
			new String[]{"灵鹤传书", "飞剑传送", "传送阵", "灵舟运送"});
		THEME_MAP.put("生产|制造|制作|加工|建造|施工",
			new String[]{"炼制", "锻造", "凝练", "筑造", "祭炼"});

		// === 时间 ===
		THEME_MAP.put("天|日|小时|分钟|秒|周|月|年",
			new String[]{"修炼日", "时辰", "柱香", "弹指", "须臾"});
		THEME_MAP.put("速度|效率|速率|进度",
			new String[]{"修炼速度", "灵根资质", "悟性", "灵力运转"});
		THEME_MAP.put("时间|时期|期间|时段|期限",
			new String[]{"时限", "修炼期", "闭关期", "劫数"});

		// === 数量/属性 ===
		THEME_MAP.put("数量|个数|人数|次数|份数|件数",
			new String[]{"数量", "数目", "件数"});
		THEME_MAP.put("成本|进价|进价成本|投入",
			new String[]{"灵力消耗", "灵材成本", "祭炼损耗"});
		THEME_MAP.put("利润|收益|盈利|收入|营收",
			new String[]{"灵力收益", "修为增长", "丹气收获"});
		THEME_MAP.put("折扣|打折|降价|优惠",
			new String[]{"机缘折扣", "天道馈赠", "福缘加持"});
		THEME_MAP.put("合格|达标|通过|过关",
			new String[]{"渡劫成功", "淬体圆满", "突破瓶颈"});
		THEME_MAP.put("不合格|不达标|未通过|失败",
			new String[]{"渡劫失败", "走火入魔", "瓶颈卡住"});

		// === 交通工具 ===
		THEME_MAP.put("汽车|公交车|地铁|火车|飞机|轮船|自行车|电动车|摩托车",
			new String[]{"灵剑", "飞舟", "仙鹤", "传送阵", "灵兽", "祥云"});
		THEME_MAP.put("速度|时速|车速|航速",
			new String[]{"御剑速度", "灵舟时速", "遁速"});
		THEME_MAP.put("路程|距离|里程|行程|路程",
			new String[]{"道途", "灵程", "仙途距离"});
		THEME_MAP.put("相遇|碰面|会合|汇合",
			new String[]{"论道相遇", "灵山会合", "仙缘相逢"});
		THEME_MAP.put("追及|追上|赶超|超越",
			new String[]{"追及", "赶超"});

		// === 工程/工作 ===
		THEME_MAP.put("工程|项目|任务|工作|作业",
			new String[]{"炼器", "炼丹", "阵法布置", "灵脉开采"});
		THEME_MAP.put("完成|做完|结束|竣工",
			new String[]{"炼成", "丹成", "完工", "功成"});
		THEME_MAP.put("合作|一起|共同|协同",
			new String[]{"联手", "合力", "齐心", "协同"});

		// === 逻辑/推理 ===
		THEME_MAP.put("真话|实话|真言|真陈述",
			new String[]{"真言", "天机", "道言"});
		THEME_MAP.put("假话|谎言|假陈述|骗人",
			new String[]{"妄语", "心魔", "魔障"});
		THEME_MAP.put("说真话|说假话|说实话|说假话",
			new String[]{"道真言", "吐妄语", "言天机", "惑心魔"});
		THEME_MAP.put("判断|推理|推测|断定",
			new String[]{"推演", "卜算", "天机推衍"});

		// === 自然/环境 ===
		THEME_MAP.put("水池|水箱|水缸|游泳池|蓄水池",
			new String[]{"灵泉", "灵池", "灵潭"});
		THEME_MAP.put("水管|管道|水龙头|水泵|水闸",
			new String[]{"灵脉", "灵力通道", "灵气阀门"});
		THEME_MAP.put("水|液体|溶液",
			new String[]{"灵液", "灵泉", "灵水"});
		THEME_MAP.put("草|草地|草坪|牧场",
			new String[]{"灵草园", "药田", "灵田"});
		THEME_MAP.put("牛|羊|马|猪|鸡|鸭|鱼",
			new String[]{"灵兽", "灵禽", "瑞兽", "仙兽"});
		THEME_MAP.put("食物|粮食|饲料|草料",
			new String[]{"灵谷", "丹药", "灵草", "灵果"});

		// === 年龄/身份 ===
		THEME_MAP.put("年龄|岁数|年纪",
			new String[]{"骨龄", "修炼年岁", "道龄"});
		THEME_MAP.put("父亲|母亲|爸爸|妈妈|父母|家长",
			new String[]{"师尊", "师娘", "师父", "师门长辈"});
		THEME_MAP.put("兄弟|姐妹|兄妹|姐弟|兄弟姐妹",
			new String[]{"师兄", "师弟", "师姐", "师妹", "同门"});
		THEME_MAP.put("同学|同事|同伴|伙伴",
			new String[]{"同门", "道友", "仙侣", "同伴"});
	}

	/**
	 * 将现实题材题干转化为玄幻风格
	 */
	public static String xianxiaTransform(String input) {
		String result = input;

		// 按关键词匹配替换（优先长词匹配）
		for (Map.Entry<String, String[]> entry : THEME_MAP.entrySet()) {
			String[] patterns = entry.getKey().split("\\|");
			String[] replacements = entry.getValue();

			// 按长度降序匹配（长词优先）
			java.util.Arrays.sort(patterns, (a, b) -> Integer.compare(b.length(), a.length()));

			for (String pattern : patterns) {
				if (result.contains(pattern)) {
					String replacement = replacements[Random.Int(replacements.length)];
					// 50% 概率替换，50% 概率保持原样（增加多样性）
					if (Random.Float() < 0.7f) {
						result = result.replace(pattern, replacement);
					}
					break; // 匹配到一个就不再尝试该组
				}
			}
		}

		// === 额外修饰 ===
		String[] prefixes = {"", "🌙 ", "✨ ", "⚡ ", "🔥 ", "💫 "};
		String[] suffixes = {"", "（灵界版）", "（修仙界）", "（秘境版）"};

		if (Random.Float() < 0.15f && !result.startsWith("（")) {
			result = prefixes[Random.Int(prefixes.length)] + result;
		}

		return result;
	}

	/**
	 * 将现实名词转化为玄幻版本（用于选项等）
	 */
	public static String xianxiaWord(String word) {
		for (Map.Entry<String, String[]> entry : THEME_MAP.entrySet()) {
			String[] patterns = entry.getKey().split("\\|");
			for (String pattern : patterns) {
				if (word.contains(pattern) || pattern.contains(word)) {
					String[] replacements = entry.getValue();
					return replacements[Random.Int(replacements.length)];
				}
			}
		}
		return word;
	}
}
