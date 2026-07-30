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

import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理约200个唯一词条定义（150-200条），每个effectType有3-5条唯一效果。
 * 每条词条有唯一的名称、描述，不重复effectType+描述。
 * 覆盖39种effectType。
 */
public class TraitLibrary {

	private static final List<TraitTemplate> positiveTemplates = new ArrayList<>();
	private static final List<TraitTemplate> negativeTemplates = new ArrayList<>();

	// 39种效果类型
	private static final String[] EFFECT_TYPES = {
		"ATK", "DEF", "HP", "SPD", "DEX", "SAT", "GOLD", "EXP", "MISC",
		"LIFESTEAL", "THORNS", "CRIT", "REGEN", "HASTE", "VISION", "MAGIC", "LOOT",
		"BLEED", "POISON", "STUN", "SLOW", "WEAK", "BURN", "FROST", "SHIELD",
		"DODGE", "RAGE", "BLESS", "CURSE",
		"TERROR", "SLEEP", "CHARM", "AMOK", "INVISIBLE", "LEVITATE", "CLEANSE",
		"INSTAKILL", "CRIPPLE", "CORROSION"
	};

	private static final int[] EFFECT_WEIGHTS = {
		10, 6, 5, 4, 3, 3, 3, 3, 3,
		3, 3, 3, 3, 3, 2, 3, 2,
		2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2,
		2, 2, 2
	};

	static {
		initPositiveTemplates();
		initNegativeTemplates();
	}

	private static String randomEffectType() {
		int total = 0;
		for (int w : EFFECT_WEIGHTS) total += w;
		int roll = Random.Int(total);
		int cum = 0;
		for (int i = 0; i < EFFECT_TYPES.length; i++) {
			cum += EFFECT_WEIGHTS[i];
			if (roll < cum) return EFFECT_TYPES[i];
		}
		return "MISC";
	}

	// =======================================================================
	//  正面词条 — 约160条，每种effectType 4条，均为唯一效果
	// =======================================================================
	private static void initPositiveTemplates() {

		// === ATK (5条) ===
		String[][] atk = {
			{"破极兵刃", "攻击力提升%.0f%%，兵刃锋芒破尽万物"},
			{"无极剑道", "每回合攻击力递增%.0f%%，无极无尽"},
			{"破军七杀", "攻击力+%.0f%%，暴击率同步提升"},
			{"嗜血狂攻", "攻击力提升%.0f%%，但防御力略微下降"},
			{"会心一击", "暴击率大幅提升%.0f%%，一击致命"}
		};
		for (int i = 0; i < atk.length; i++) registerPos(i, "ATK", atk[i][0], atk[i][1]);

		// === DEF (4条) ===
		String[][] def = {
			{"金刚不坏", "防御力提升%.0f%%，金刚之躯万法不侵"},
			{"玄武真甲", "防御力提升%.0f%%，玄武之甲坚不可摧"},
			{"斗转星移", "防御力提升%.0f%%，借力打力转移部分伤害"},
			{"铁壁铜墙", "防御力提升%.0f%%，铁壁横阻挡千军"}
		};
		for (int i = 0; i < def.length; i++) registerPos(10 + i, "DEF", def[i][0], def[i][1]);

		// === HP (4条) ===
		String[][] hp = {
			{"凤凰涅槃", "生命上限提升%.0f%%，涅槃重生不死不灭"},
			{"长生诀", "生命上限提升%.0f%%，长生久视寿与天齐"},
			{"不灭之体", "生命上限提升%.0f%%，不灭之体永世长存"},
			{"龙血之体", "生命上限提升%.0f%%，龙血淬炼肉身强横"}
		};
		for (int i = 0; i < hp.length; i++) registerPos(20 + i, "HP", hp[i][0], hp[i][1]);

		// === SPD (4条) ===
		String[][] spd = {
			{"凌波微步", "移动速度提升%.0f%%，凌波微步罗袜生尘"},
			{"缩地成寸", "移动速度提升%.0f%%，缩地成寸咫尺天涯"},
			{"神行百变", "移动速度提升%.0f%%，神行无影百变莫测"},
			{"雷动九天", "移动速度提升%.0f%%，雷动九天瞬息千里"}
		};
		for (int i = 0; i < spd.length; i++) registerPos(30 + i, "SPD", spd[i][0], spd[i][1]);

		// === DEX (4条) ===
		String[][] dex = {
			{"灵猴身法", "灵巧度提升%.0f%%，身如灵猴腾挪自如"},
			{"百步穿杨", "灵巧度提升%.0f%%，百步之外穿杨入叶"},
			{"左右互搏", "灵巧度提升%.0f%%，双手并用威力倍增"},
			{"庖丁解牛", "灵巧度提升%.0f%%，游刃有余精准无误"}
		};
		for (int i = 0; i < dex.length; i++) registerPos(40 + i, "DEX", dex[i][0], dex[i][1]);

		// === SAT (4条) ===
		String[][] sat = {
			{"太乙真火", "法术强度提升%.0f%%，太乙真火焚尽虚空"},
			{"九天玄女诀", "法术强度提升%.0f%%，玄女降世法力无边"},
			{"鸿蒙紫气", "法术强度提升%.0f%%，鸿蒙紫气先天之炁"},
			{"混沌大道", "法术强度提升%.0f%%，混沌大道万法之源"}
		};
		for (int i = 0; i < sat.length; i++) registerPos(50 + i, "SAT", sat[i][0], sat[i][1]);

		// === GOLD (4条) ===
		String[][] gold = {
			{"点石成金", "金钱收益提升%.0f%%，点石成金财富自来"},
			{"聚宝盆", "金钱收益提升%.0f%%，聚宝之盆财源滚滚"},
			{"财运亨通", "金钱收益提升%.0f%%，财运亨通富贵自来"},
			{"金蟾献宝", "金钱收益提升%.0f%%，金蟾吐宝财富无尽"}
		};
		for (int i = 0; i < gold.length; i++) registerPos(60 + i, "GOLD", gold[i][0], gold[i][1]);

		// === EXP (4条) ===
		String[][] exp = {
			{"醍醐灌顶", "经验获取提升%.0f%%，醍醐灌顶顿悟大道"},
			{"顿悟", "经验获取提升%.0f%%，一朝顿悟境界飞升"},
			{"天道酬勤", "经验获取提升%.0f%%，天道酬勤努力有报"},
			{"过目不忘", "经验获取提升%.0f%%，过目不忘学习神速"}
		};
		for (int i = 0; i < exp.length; i++) registerPos(70 + i, "EXP", exp[i][0], exp[i][1]);

		// === MISC (4条) ===
		String[][] misc = {
			{"一炁化三清", "所有正面效果强度提升%.0f%%，三清道炁加身"},
			{"因果律武器", "%.0f%%概率追加一次相同伤害，因果循环"},
			{"否极泰来", "生命低于20%%时所有属性临时提升%.0f%%"},
			{"金蝉脱壳", "生命归零时%.0f%%概率以1血存活并隐身"}
		};
		for (int i = 0; i < misc.length; i++) registerPos(80 + i, "MISC", misc[i][0], misc[i][1]);

		// === LIFESTEAL (4条) ===
		String[][] lifesteal = {
			{"嗜血魔功", "造成伤害时回复%.0f%%伤害值的生命，嗜血成魔"},
			{"饮血剑意", "剑锋饮血，将%.0f%%攻击伤害转为己用"},
			{"生命汲取", "每回合恢复%.0f%%已损失生命值，绵绵不绝"},
			{"血煞大法", "击杀敌人时回复%.0f%%最大生命值，血煞滔天"}
		};
		for (int i = 0; i < lifesteal.length; i++) registerPos(90 + i, "LIFESTEAL", lifesteal[i][0], lifesteal[i][1]);

		// === THORNS (4条) ===
		String[][] thorns = {
			{"荆棘光环", "反弹%.0f%%受到的近战伤害给攻击者"},
			{"以牙还牙", "%.0f%%概率将本次受到的伤害全额反弹"},
			{"镜花水月", "受到远程攻击时%.0f%%概率完全反弹该次伤害"},
			{"毒荆棘", "反弹%.0f%%伤害并使攻击者中毒3回合"}
		};
		for (int i = 0; i < thorns.length; i++) registerPos(100 + i, "THORNS", thorns[i][0], thorns[i][1]);

		// === CRIT (4条) ===
		String[][] crit = {
			{"致命一击", "暴击率提升%.0f%%，一击必杀鬼神皆惊"},
			{"弱点洞察", "暴击率提升%.0f%%，精准命中敌人要害"},
			{"破绽追击", "连续攻击同一目标暴击率递增%.0f%%"},
			{"狂暴之心", "生命低于30%%时暴击率额外提升%.0f%%"}
		};
		for (int i = 0; i < crit.length; i++) registerPos(110 + i, "CRIT", crit[i][0], crit[i][1]);

		// === REGEN (4条) ===
		String[][] regen = {
			{"青木逢春", "非战斗状态每回合额外回复%.0f%%最大生命"},
			{"生命源泉", "每走5步恢复%.0f%%已损失生命值"},
			{"快速自愈", "脱离战斗后每秒额外回复%.0f%%生命"},
			{"自然恩赐", "站在草丛或水边时每回合回复%.0f%%血量"}
		};
		for (int i = 0; i < regen.length; i++) registerPos(120 + i, "REGEN", regen[i][0], regen[i][1]);

		// === HASTE (4条) ===
		String[][] haste = {
			{"疾风连击", "攻击速度提升%.0f%%，出手如风连击不绝"},
			{"快手如电", "攻速提升%.0f%%，出手如电后发先至"},
			{"残影连斩", "击杀后攻速大幅提升%.0f%%，残影重重"},
			{"音速斩击", "攻速突破极限提升%.0f%%，音速之刃"}
		};
		for (int i = 0; i < haste.length; i++) registerPos(130 + i, "HASTE", haste[i][0], haste[i][1]);

		// === VISION (4条) ===
		String[][] vision = {
			{"天眼通", "视野范围扩大%.0f%%，洞悉一切"},
			{"破妄之眼", "视野扩大%.0f%%，可看穿隐形单位和陷阱"},
			{"神识外放", "视野扩大%.0f%%，远程攻击距离+1"},
			{"千里眼", "视野范围扩大%.0f%%，远距离探知"}
		};
		for (int i = 0; i < vision.length; i++) registerPos(140 + i, "VISION", vision[i][0], vision[i][1]);

		// === MAGIC (4条) ===
		String[][] magic = {
			{"玄冰诀", "冰系法术伤害提升%.0f%%，寒气逼人"},
			{"紫霄神雷", "雷系法术伤害提升%.0f%%，雷霆万钧"},
			{"五行混元功", "全属性法术伤害提升%.0f%%，五行轮转"},
			{"法力洪流", "法力值越高法术伤害越高，最多提升%.0f%%"}
		};
		for (int i = 0; i < magic.length; i++) registerPos(150 + i, "MAGIC", magic[i][0], magic[i][1]);

		// === LOOT (4条) ===
		String[][] loot = {
			{"鸿运当头", "稀有物品掉率提升%.0f%%，好运加持"},
			{"宝藏嗅觉", "宝物位置可见，稀有掉率提升%.0f%%"},
			{"秘宝感应", "击杀精英怪时%.0f%%概率额外掉落宝物"},
			{"天降横财", "每层首次开箱获得%.0f%%额外稀有物品"}
		};
		for (int i = 0; i < loot.length; i++) registerPos(160 + i, "LOOT", loot[i][0], loot[i][1]);

		// === BLEED (4条) ===
		String[][] bleed = {
			{"割裂伤口", "攻击使敌人流血，每秒额外造成%.0f%%伤害"},
			{"血刃风暴", "暴击时施加流血，每秒造成%.0f%%武器伤害"},
			{"放血疗法", "流血状态下敌人受到治疗减半，每秒流血%.0f%%"},
			{"持续失血", "流血效果叠加时伤害递增%.0f%%每层"}
		};
		for (int i = 0; i < bleed.length; i++) registerPos(170 + i, "BLEED", bleed[i][0], bleed[i][1]);

		// === POISON (4条) ===
		String[][] poison = {
			{"五毒神掌", "攻击附带毒素，每秒造成%.0f%%毒性伤害"},
			{"蛇蝎美人", "毒伤叠加时效果递增%.0f%%每层"},
			{"剧毒新星", "击败毒伤中敌人时爆发毒雾，造成%.0f%%范围毒伤"},
			{"鹤顶红", "毒伤无视%.0f%%毒抗性，剧毒无比"}
		};
		for (int i = 0; i < poison.length; i++) registerPos(180 + i, "POISON", poison[i][0], poison[i][1]);

		// === STUN (4条) ===
		String[][] stun = {
			{"雷霆震击", "攻击时%.0f%%几率眩晕敌人1回合，雷电轰鸣"},
			{"震荡冲击波", "%.0f%%概率使周围敌人眩晕1回合"},
			{"昏厥打击", "暴击时%.0f%%概率附加眩晕效果"},
			{"震慑怒吼", "造成伤害时%.0f%%概率震慑周围敌人"}
		};
		for (int i = 0; i < stun.length; i++) registerPos(190 + i, "STUN", stun[i][0], stun[i][1]);

		// === SLOW (4条) ===
		String[][] slow = {
			{"冰霜新星", "冰霜之力使敌人速度降低%.0f%%，步履蹒跚"},
			{"迟缓诅咒", "敌人移动速度降低%.0f%%，行动如陷泥沼"},
			{"减速力场", "周围敌人速度降低%.0f%%，力场压制"},
			{"泥沼陷阱", "敌人移动后触发减速，移动速度-%.0f%%"}
		};
		for (int i = 0; i < slow.length; i++) registerPos(200 + i, "SLOW", slow[i][0], slow[i][1]);

		// === WEAK (4条) ===
		String[][] weak = {
			{"虚弱诅咒", "目标攻击力和防御力降低%.0f%%，诅咒缠身"},
			{"破甲重击", "攻击使敌人防御力降低%.0f%%，护甲崩裂"},
			{"能量吸取", "每次攻击降低目标%.0f%%攻击力，持续2回合"},
			{"瓦解光环", "周围敌人全属性降低%.0f%%，气场压制"}
		};
		for (int i = 0; i < weak.length; i++) registerPos(210 + i, "WEAK", weak[i][0], weak[i][1]);

		// === BURN (4条) ===
		String[][] burn = {
			{"烈焰焚身", "灼烧敌人每秒造成%.0f%%火系伤害，焚尽一切"},
			{"灼热烙印", "灼烧效果叠加时伤害递增%.0f%%每层"},
			{"炎爆连击", "对灼烧敌人暴击率提升%.0f%%"},
			{"熔岩灼烧", "灼烧无视%.0f%%火抗，熔岩之力"}
		};
		for (int i = 0; i < burn.length; i++) registerPos(220 + i, "BURN", burn[i][0], burn[i][1]);

		// === FROST (4条) ===
		String[][] frost = {
			{"永冻冰棺", "%.0f%%概率完全冻结敌人2回合，冰封禁锢"},
			{"极寒领域", "周围敌人每回合累计冰冻值，达到100%%时冰冻%.0f%%概率"},
			{"霜甲术", "被冰冻的敌人解冻后减速%.0f%%，持续3回合"},
			{"冰晶穿刺", "对冰冻敌人暴击伤害提升%.0f%%"}
		};
		for (int i = 0; i < frost.length; i++) registerPos(230 + i, "FROST", frost[i][0], frost[i][1]);

		// === SHIELD (4条) ===
		String[][] shield = {
			{"圣光壁垒", "每进入新楼层获得护盾，数值为最大生命值的%.0f%%"},
			{"玄天护盾", "受到首次攻击时获得护盾，吸收伤害提升%.0f%%"},
			{"法力护盾", "消耗法力值补充护盾，每点法力转化%.0f%%护盾"},
			{"神圣壁垒", "生命满时获得护盾加成提升%.0f%%"}
		};
		for (int i = 0; i < shield.length; i++) registerPos(240 + i, "SHIELD", shield[i][0], shield[i][1]);

		// === DODGE (4条) ===
		String[][] dodge = {
			{"幻影迷踪步", "闪避率提升%.0f%%，幻影重重真身难寻"},
			{"无相身法", "被击中后下回合闪避率提升%.0f%%，无迹可寻"},
			{"凌空虚渡", "闪避率提升%.0f%%，虚空漫步不沾凡尘"},
			{"燕返", "闪避后回复%.0f%%已损失生命值，燕返归巢"}
		};
		for (int i = 0; i < dodge.length; i++) registerPos(250 + i, "DODGE", dodge[i][0], dodge[i][1]);

		// === RAGE (4条) ===
		String[][] rage = {
			{"破军战意", "每损失10%%生命提升%.0f%%攻击力，破军之志"},
			{"狂暴血怒", "生命低于50%%时攻击力提升%.0f%%，嗜血狂暴"},
			{"绝地反击", "生命低于30%%时伤害提高%.0f%%，濒死爆发"},
			{"嗜血狂战", "每击杀一个敌人增加%.0f%%攻击力，可叠加"}
		};
		for (int i = 0; i < rage.length; i++) registerPos(260 + i, "RAGE", rage[i][0], rage[i][1]);

		// === BLESS (4条) ===
		String[][] bless = {
			{"神恩浩瀚", "攻击力、防御力、速度同时提升%.0f%%，神恩如海"},
			{"神圣净化", "每次击杀清除一个负面状态，全属性提升%.0f%%持续3回合"},
			{"神佑之体", "受到致命伤时%.0f%%概率触发无敌1回合"},
			{"光明礼赞", "治疗法术效果额外提升%.0f%%，圣光普照"}
		};
		for (int i = 0; i < bless.length; i++) registerPos(270 + i, "BLESS", bless[i][0], bless[i][1]);

		// === CURSE (4条) ===
		String[][] curse = {
			{"暗影诅咒", "诅咒目标每秒受到%.0f%%暗影伤害，如坠深渊"},
			{"恶魔契约", "目标每秒损失%.0f%%当前生命，恶魔低语"},
			{"诅咒之力", "目标受到的所有伤害增加%.0f%%，厄运缠身"},
			{"死亡标记", "对标记目标伤害加深%.0f%%，死神凝视"}
		};
		for (int i = 0; i < curse.length; i++) registerPos(280 + i, "CURSE", curse[i][0], curse[i][1]);

		// === TERROR (4条) ===
		String[][] terror = {
			{"恐惧咆哮", "攻击时%.0f%%概率使敌人陷入恐惧，逃窜2回合"},
			{"摄魂术", "攻击时%.0f%%概率摄魂，恐惧敌人"},
			{"龙威", "龙族威压，%.0f%%概率使敌人恐惧"},
			{"幽冥鬼啸", "鬼哭神嚎，%.0f%%概率恐惧敌人"}
		};
		for (int i = 0; i < terror.length; i++) registerPos(290 + i, "TERROR", terror[i][0], terror[i][1]);

		// === SLEEP (4条) ===
		String[][] sleep = {
			{"催眠术", "攻击时%.0f%%概率使敌人陷入沉睡"},
			{"梦魇诅咒", "%.0f%%概率令敌人陷入噩梦无法行动"},
			{"宁神咒", "%.0f%%概率使敌人安眠"},
			{"迷魂香", "%.0f%%概率释放迷香使敌人沉睡"}
		};
		for (int i = 0; i < sleep.length; i++) registerPos(300 + i, "SLEEP", sleep[i][0], sleep[i][1]);

		// === CHARM (4条) ===
		String[][] charm = {
			{"魅惑之瞳", "攻击时%.0f%%概率魅惑敌人，使其为你而战"},
			{"倾城之姿", "%.0f%%概率使敌人被美色迷惑"},
			{"迷魂大法", "%.0f%%概率迷惑敌人神智"},
			{"摄心术", "%.0f%%概率操控敌人心智"}
		};
		for (int i = 0; i < charm.length; i++) registerPos(310 + i, "CHARM", charm[i][0], charm[i][1]);

		// === AMOK (4条) ===
		String[][] amok = {
			{"混乱咒", "攻击时%.0f%%概率使敌人陷入混乱，不分敌我"},
			{"狂乱术", "%.0f%%概率激发敌人狂乱"},
			{"混沌之力", "%.0f%%概率释放混沌之力使敌人疯狂"},
			{"疯魔咒", "%.0f%%概率使敌人疯魔"}
		};
		for (int i = 0; i < amok.length; i++) registerPos(320 + i, "AMOK", amok[i][0], amok[i][1]);

		// === INVISIBLE (4条) ===
		String[][] invisible = {
			{"隐匿术", "进入战斗时%.0f%%概率隐身3回合"},
			{"暗影潜行", "受击时%.0f%%概率进入隐身状态"},
			{"鬼影步", "移动后%.0f%%概率隐身1回合"},
			{"虚空藏身", "%.0f%%概率遁入虚空隐身"}
		};
		for (int i = 0; i < invisible.length; i++) registerPos(330 + i, "INVISIBLE", invisible[i][0], invisible[i][1]);

		// === LEVITATE (4条) ===
		String[][] levitate = {
			{"御风术", "%.0f%%概率获得御风飞行能力，无视地面陷阱"},
			{"腾云驾雾", "%.0f%%概率腾云飞行"},
			{"浮空术", "%.0f%%概率悬浮于空中"},
			{"御剑飞行", "%.0f%%概率御剑飞空"}
		};
		for (int i = 0; i < levitate.length; i++) registerPos(340 + i, "LEVITATE", levitate[i][0], levitate[i][1]);

		// === CLEANSE (4条) ===
		String[][] cleanse = {
			{"净化术", "每%.0f回合自动净化一个负面状态"},
			{"清心诀", "受到负面状态时%.0f%%概率立即净化"},
			{"圣光洗礼", "每%.0f回合清除所有负面状态"},
			{"圣洁之体", "%.0f%%概率负面状态持续时间减半"}
		};
		for (int i = 0; i < cleanse.length; i++) registerPos(350 + i, "CLEANSE", cleanse[i][0], cleanse[i][1]);

		// === INSTAKILL (4条) ===
		String[][] instakill = {
			{"死神镰刀", "攻击时%.0f%%概率直接斩杀敌人（对Boss无效）"},
			{"因果律斩", "%.0f%%概率触发因果律，即死效果"},
			{"断魂", "%.0f%%概率一击断魂"},
			{"斩仙", "%.0f%%概率一击斩仙"}
		};
		for (int i = 0; i < instakill.length; i++) registerPos(360 + i, "INSTAKILL", instakill[i][0], instakill[i][1]);

		// === CRIPPLE (4条) ===
		String[][] cripple = {
			{"断筋", "攻击时%.0f%%概率使敌人残废，速度大降"},
			{"碎骨", "%.0f%%概率击碎敌人骨骼使其残废"},
			{"截脉", "%.0f%%概率截断敌人经脉使其残废"},
			{"破体", "%.0f%%概率重创敌人身体致残"}
		};
		for (int i = 0; i < cripple.length; i++) registerPos(370 + i, "CRIPPLE", cripple[i][0], cripple[i][1]);

		// === CORROSION (4条) ===
		String[][] corrosion = {
			{"强酸喷雾", "攻击时%.0f%%概率腐蚀敌人护甲"},
			{"锈蚀术", "%.0f%%概率锈蚀敌人装备"},
			{"化骨水", "%.0f%%概率使用化骨水腐蚀敌人"},
			{"熔蚀", "%.0f%%概率熔蚀敌人防御"}
		};
		for (int i = 0; i < corrosion.length; i++) registerPos(380 + i, "CORROSION", corrosion[i][0], corrosion[i][1]);

		// === 额外混合/特殊词条 — 保证总数在150-200之间（当前39*4=156，再加10条=166） ===
		String[][] hybrid = {
			// 混合型词条，赋予独特混合效果
			{"八荒六合唯我独尊功", "孤身作战时全属性提升%.0f%%，唯我独尊"},
			{"天地借法", "%.0f%%概率触发天地共鸣，技能效果翻倍"},
			{"不灭战意", "战斗状态下每回合提升%.0f%%攻击力，上限5层"},
			{"天人合一", "站立不动时每回合恢复%.0f%%已损失生命"},
			{"破而后立", "受到暴击后下回合伤害提升%.0f%%"},
			{"一气呵成", "连续使用相同技能时伤害递增%.0f%%"},
			{"以战养战", "击杀回复%.0f%%生命并临时提升5%%攻击力"},
			{"移花接木", "受到伤害时有%.0f%%概率转移50%%给召唤物"},
			{"斗转星移", "受到远程攻击时%.0f%%概率反弹给最近敌人"},
			{"回光返照", "濒死状态时所有属性提升%.0f%%，持续3回合"}
		};
		for (int i = 0; i < hybrid.length; i++) {
			String type = "MISC";
			String desc = hybrid[i][1];
			if (desc.contains("攻击")) type = "ATK";
			else if (desc.contains("生命") || desc.contains("回复")) type = "REGEN";
			else if (desc.contains("反弹")) type = "THORNS";
			registerPos(390 + i, type, hybrid[i][0], hybrid[i][1]);
		}
	}

	// =======================================================================
	//  负面词条 — 约40条，覆盖主要属性类型
	// =======================================================================
	private static void initNegativeTemplates() {

		String[][] neg = {
			// ATK负面 (4条)
			{"心魔缠绕", "攻击力降低%.0f%%，心魔侵蚀心神"},
			{"法力反噬", "攻击力降低%.0f%%，法力失控反噬丹田"},
			{"修为倒退", "攻击力降低%.0f%%，功力不进反退"},
			{"六神无主", "攻击力降低%.0f%%，心神不宁战意全消"},
			// DEF负面 (4条)
			{"阴气入体", "防御力降低%.0f%%，阴寒之气侵入骨髓"},
			{"七情六欲乱", "防御力降低%.0f%%，七情六欲扰乱心神"},
			{"煞气冲霄", "防御力降低%.0f%%，煞气入体扰乱气息"},
			{"祸不单行", "防御力降低%.0f%%，屋漏偏逢连夜雨"},
			// HP负面 (4条)
			{"气血亏损", "生命上限降低%.0f%%，气血亏空面色苍白"},
			{"业火焚心", "生命上限降低%.0f%%，业力化为烈火"},
			{"诅咒缠身", "全属性降低%.0f%%，古老诅咒附体厄运连连"},
			{"病入膏肓", "生命上限降低%.0f%%，重病缠身药石难医"},
			// SPD负面 (3条)
			{"心猿意马", "速度降低%.0f%%，心思不定如猿猴跳跃"},
			{"寒毒入体", "移动速度降低%.0f%%，寒毒冻结经脉"},
			{"寸步难行", "速度降低%.0f%%，每走一步艰难万分"},
			// DEX负面 (2条)
			{"经络滞涩", "灵巧度降低%.0f%%，经脉不通百病生"},
			{"阴差阳错", "灵巧度降低%.0f%%，阴阳失调运势错乱"},
			// SAT负面 (3条)
			{"魂魄不稳", "法术强度降低%.0f%%，三魂七魄动荡"},
			{"道心破碎", "法术强度降低%.0f%%，修道之心碎裂"},
			{"反应迟钝", "法术强度降低%.0f%%，思维混沌反应变慢"},
			// GOLD负面 (3条)
			{"业力深重", "金钱收益降低%.0f%%，业力缠身财运不济"},
			{"晦气缠身", "金钱收益降低%.0f%%，晦气随身财运不通"},
			{"破财之兆", "金钱收益降低%.0f%%，财运流失"},
			// EXP负面 (2条)
			{"灵力匮乏", "经验获取降低%.0f%%，天地灵气难以吸纳"},
			{"霉运当头", "经验获取降低%.0f%%，霉运笼罩诸事不顺"},
			// MISC负面 (2条)
			{"衰神附体", "全属性降低%.0f%%，衰神临门"},
			{"命途多舛", "战斗时%.0f%%概率使敌人陷入随机负面状态，命途坎坷多灾多难"},
			// 特殊效果负面 (5条)
			{"血咒反噬", "每秒损失%.0f%%血量，血咒之力反噬自身"},
			{"易伤", "受到的所有伤害增加%.0f%%"},
			{"麻痹", "有%.0f%%概率无法行动，身体僵直"},
			{"沉重", "移动速度降低%.0f%%，身体沉重如灌铅"},
			{"穷神附体", "掉落率降低%.0f%%，穷神缠身"},
			// 10种高级类型负面 (10条)
			{"战栗", "攻击时%.0f%%概率陷入恐惧，逃窜1回合"},
			{"嗜睡", "%.0f%%概率因嗜睡而错过行动回合"},
			{"心神不宁", "%.0f%%概率被美色所惑而分心"},
			{"神智错乱", "%.0f%%概率陷入混乱不分敌我"},
			{"现形", "%.0f%%概率在战斗中暴露身形"},
			{"坠落", "%.0f%%概率从空中坠落受伤"},
			{"污秽缠身", "%.0f%%概率净化能力失效"},
			{"死里逃生", "%.0f%%概率即死效果被抵抗"},
			{"腿脚不便", "%.0f%%概率自身行动残废减速"},
			{"装备腐蚀", "%.0f%%概率自身装备被酸液腐蚀"}
		};
		for (int i = 0; i < neg.length; i++) {
			String type = "MISC";
			String desc = neg[i][1];
			String name = neg[i][0];
			if (desc.contains("攻击")) type = "ATK";
			else if (desc.contains("防御")) type = "DEF";
			else if (desc.contains("生命")) type = "HP";
			else if (desc.contains("速度") || desc.contains("移动")) type = "SPD";
			else if (desc.contains("灵巧")) type = "DEX";
			else if (desc.contains("法术")) type = "SAT";
			else if (desc.contains("金钱")) type = "GOLD";
			else if (desc.contains("经验")) type = "EXP";
			else if (desc.contains("全属性")) type = "MISC";
			else if (desc.contains("流血") || desc.contains("掉血") || desc.contains("损失") || desc.contains("流失")) type = "BLEED";
			else if (desc.contains("伤害增加")) type = "WEAK";
			else if (desc.contains("无法行动") || desc.contains("僵直") || desc.contains("麻痹")) type = "STUN";
			else if (desc.contains("掉落") || desc.contains("穷神")) type = "LOOT";
			else if (desc.contains("恐惧") || name.contains("战栗")) type = "TERROR";
			else if (desc.contains("嗜睡") || desc.contains("错过行动")) type = "SLEEP";
			else if (desc.contains("美色") || desc.contains("分心")) type = "CHARM";
			else if (desc.contains("混乱")) type = "AMOK";
			else if (desc.contains("隐身") || desc.contains("暴露") || name.contains("现形")) type = "INVISIBLE";
			else if (desc.contains("坠落") || desc.contains("飞行")) type = "LEVITATE";
			else if (desc.contains("净化") || desc.contains("驱散")) type = "CLEANSE";
			else if (desc.contains("即死") || desc.contains("抵抗")) type = "INSTAKILL";
			else if (desc.contains("残废") || desc.contains("行动不便")) type = "CRIPPLE";
			else if (desc.contains("腐蚀") || desc.contains("酸液")) type = "CORROSION";
			registerNeg(500 + i, type, neg[i][0], neg[i][1]);
		}
	}

	// ========== 词条注册辅助方法 ==========

	private static void registerPos(int id, String effectType, String name, String descTemplate) {
		positiveTemplates.add(new TraitTemplate(id, effectType, name, "对英雄: " + descTemplate, true));
	}

	private static void registerNeg(int id, String effectType, String name, String descTemplate) {
		negativeTemplates.add(new TraitTemplate(id, effectType, name, "对敌人: " + descTemplate, false));
	}

	// ========== 词条模板内部类 ==========

	private static class TraitTemplate {
		int id;
		String effectType;
		String name;
		String descTemplate;
		boolean isPositive;

		TraitTemplate(int id, String effectType, String name, String descTemplate, boolean isPositive) {
			this.id = id;
			this.effectType = effectType;
			this.name = name;
			this.descTemplate = descTemplate;
			this.isPositive = isPositive;
		}
	}

	// ========== 公共接口 ==========

	/**
	 * 根据楼层深度生成一个合适的词条。
	 */
	public static Trait generateTrait(int floorDepth) {
		if (floorDepth < 1) floorDepth = 1;
		if (floorDepth > 1000) floorDepth = 1000;

		boolean isPositive;
		if (floorDepth <= 100) {
			isPositive = Random.Float() < 0.80f;
		} else if (floorDepth <= 500) {
			isPositive = Random.Float() < 0.55f;
		} else {
			// 深层也不过分惩罚玩家，保持55%以上正面率
			isPositive = Random.Float() < 0.55f;
		}

		TraitTemplate template;
		if (isPositive) {
			template = Random.element(positiveTemplates);
		} else {
			template = Random.element(negativeTemplates);
		}

		int level = 1;

		if (isPositive) {
			int posLevel = floorDepth / 50 + 1;
			if (posLevel > 10) posLevel = 10;
			level = posLevel;
		} else {
			int negLevel = floorDepth / 100 + 1;
			if (negLevel > 5) negLevel = 5;
			level = negLevel;
		}

		float effectValue;
		if (isPositive) {
			effectValue = level * 0.02f;
		} else {
			effectValue = level * -0.03f;
		}

		return new Trait(
				template.id,
				template.name,
				template.descTemplate,  // 使用原始模板(含%.0f%%)，recalcEffectValue会自动替换
				level,
				isPositive,
				template.effectType,
				effectValue
		);
	}

	/**
	 * 根据ID获取词条模板，返回一个新的初始等级(1级)的Trait实例。
	 */
	public static Trait getTraitById(int id) {
		if (id < 0 || id >= 2000) return null;

		if (id < 500) {
			for (TraitTemplate t : positiveTemplates) {
				if (t.id == id) {
					float effectPct = 1 * 2;
					String desc = String.format(t.descTemplate, effectPct);
					return new Trait(t.id, t.name, desc, 1, true, t.effectType, 0.02f);
				}
			}
		} else {
			for (TraitTemplate t : negativeTemplates) {
				if (t.id == id) {
					float effectPct = 1 * 3;
					String desc = String.format(t.descTemplate, effectPct);
					return new Trait(t.id, t.name, desc, 1, false, t.effectType, -0.03f);
				}
			}
		}
		return null;
	}

	public static int getTotalTraitCount() {
		return positiveTemplates.size() + negativeTemplates.size();
	}

	public static int getPositiveTraitCount() {
		return positiveTemplates.size();
	}

	public static int getNegativeTraitCount() {
		return negativeTemplates.size();
	}
}
