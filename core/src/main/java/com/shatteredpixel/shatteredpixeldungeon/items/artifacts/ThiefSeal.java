/*
 * Lexicographic-Abyss by 许玄
 * Copyright (C) 2024-2026 许玄
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.effects.Surprise;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 盗圣之证 — 攻击使敌人残废 + 偷窃升级
 * 可偷窃敌人物品，成功偷窃提升等级
 * 攻击时概率使敌人残废减速
 */
public class ThiefSeal extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_ARMBAND;
        levelCap = Integer.MAX_VALUE;

        charge = 0;
        partialCharge = 0;
        chargeCap = 5 + level() / 2;

        defaultAction = AC_STEAL;
    }

    public static final String AC_STEAL = "STEAL";

    @Override
    public String name() { return "盗圣之证"; }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero)
                && charge > 0
                && hero.buff(MagicImmune.class) == null
                && !cursed) {
            actions.add(AC_STEAL);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_STEAL)) {

            curUser = hero;

            if (!isEquipped(hero)) {
                GLog.i("需要先装备盗圣之证！");
                usesTargeting = false;

            } else if (charge < 1) {
                GLog.i("盗圣之证充能不足！");
                usesTargeting = false;

            } else if (cursed) {
                GLog.w("盗圣之证已被诅咒！");
                usesTargeting = false;

            } else {
                usesTargeting = true;
                GameScene.selectCell(targeter);
            }
        }
    }

    /**
     * 攻击触发：概率残废 + 经验积累升级
     * 残废概率：(15 + 5×等级)%
     * 持续：(2 + 等级/2) 回合
     */
    public void onHeroAttack(Hero hero, Char enemy, int damage) {
        if (cursed || hero.buff(MagicImmune.class) != null || damage <= 0) {
            return;
        }

        // 基础攻击力加成（每级 +1.0 伤害，盗贼偏技巧）
        damage += Math.round(level());

        // 概率使敌人残废
        float crippleChance = 0.15f + 0.05f * level();
        if (Random.Float() < crippleChance && enemy.buff(MagicImmune.class) == null) {
            int crippleDuration = 2 + level() / 2;
            Buff.prolong(enemy, Cripple.class, crippleDuration);
            GLog.p("盗圣之证使敌人残废！");
        }

        // 造成伤害积累经验升级
        if (enemy.buff(MagicImmune.class) == null) {
            exp += Math.max(1, damage / 4);
            while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
                exp -= 10 + Math.round(3.33f * level());
                Catalog.countUse(getClass());
                GLog.p("盗圣之证经验提升，等级提高！");
                upgrade();
            }
        }
    }

    // 击杀敌人时获得经验
    public void onKillEnemy(Char enemy) {
        if (cursed) return;
        exp += 5 + Math.round(level() / 2f);
        while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
            exp -= 10 + Math.round(3.33f * level());
            Catalog.countUse(getClass());
            GLog.p("盗圣之证经验提升，等级提高！");
            upgrade();
        }
    }

    public CellSelector.Listener targeter = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer target) {

            if (target == null) {
                return;
            } else if (!Dungeon.level.adjacent(curUser.pos, target) || Actor.findChar(target) == null) {
                GLog.w("没有可偷窃的目标");
            } else {
                Char ch = Actor.findChar(target);
                if (ch instanceof Shopkeeper) {
                    GLog.w("你不敢偷窃商店老板！");
                } else if (ch.alignment != Char.Alignment.ENEMY
                        && !(ch instanceof Mimic && ch.alignment == Char.Alignment.NEUTRAL)) {
                    GLog.w("没有可偷窃的目标");
                } else if (ch instanceof Mob) {
                    curUser.busy();
                    curUser.sprite.attack(target, new Callback() {
                        @Override
                        public void call() {
                            Sample.INSTANCE.play(Assets.Sounds.HIT);

                            boolean surprised = ((Mob) ch).surprisedBy(curUser, false);
                            float lootMultiplier = 1f + 0.1f * level();
                            int debuffDuration = 3 + level() / 2;

                            Invisibility.dispel(curUser);

                            if (surprised) {
                                lootMultiplier += 0.5f;
                                Surprise.hit(ch);
                                Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                                debuffDuration += 2;
                                exp += 2;
                            }

                            float lootChance = ((Mob) ch).lootChance() * lootMultiplier;

                            if (Dungeon.hero.lvl > ((Mob) ch).maxLvl + 2) {
                                lootChance = 0;
                            } else if (ch.buff(StolenTracker.class) != null) {
                                lootChance = 0;
                            }

                            if (lootChance == 0) {
                                GLog.w("这个敌人身上没有值得偷的东西");
                            } else if (Random.Float() <= lootChance) {
                                Item loot = ((Mob) ch).createLoot();
                                if (Challenges.isItemBlocked(loot)) {
                                    GLog.i("偷窃失败，什么都没拿到");
                                    Buff.affect(ch, StolenTracker.class).setItemStolen(false);
                                } else {
                                    if (loot.doPickUp(curUser)) {
                                        curUser.spend(-loot.pickupDelay());
                                    } else {
                                        Dungeon.level.drop(loot, curUser.pos).sprite.drop();
                                    }
                                    GLog.i("偷窃成功！获得了 " + loot.name() + "！");
                                    Buff.affect(ch, StolenTracker.class).setItemStolen(true);
                                }
                            } else {
                                GLog.i("偷窃失败，什么都没拿到");
                                Buff.affect(ch, StolenTracker.class).setItemStolen(false);
                            }

                            Buff.prolong(ch, Blindness.class, debuffDuration);
                            Buff.prolong(ch, Cripple.class, debuffDuration);

                            artifactProc(ch, visiblyUpgraded(), 1);

                            charge--;
                            exp += 3;
                            Talent.onArtifactUsed(Dungeon.hero);
                            while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
                                exp -= 10 + Math.round(3.33f * level());
                                Catalog.countUse(ThiefSeal.class);
                                GLog.p("盗圣之证经验提升，等级提高！");
                                upgrade();
                            }
                            Item.updateQuickslot();
                            curUser.next();
                        }
                    });
                }
            }
        }

        @Override
        public String prompt() {
            return "选择偷窃目标";
        }
    };

    public static class StolenTracker extends CounterBuff {
        { revivePersists = true; }

        public void setItemStolen(boolean stolen) { if (stolen) countUp(1); }

        public boolean itemWasStolen() { return count() > 0; }
    }

    public float onKillGoldMultiplier() {
        if (cursed) return 0f;
        return 0.5f + 0.1f * level();
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new ThiefSealThievery();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;
        if (charge < chargeCap) {
            partialCharge += 0.1f * amount;
            while (partialCharge >= 1f) {
                charge++;
                partialCharge--;
            }
            if (charge >= chargeCap) {
                GLog.p("盗圣之证充能已满！");
                partialCharge = 0;
                charge = chargeCap;
            }
            updateQuickslot();
        }
    }

    @Override
    public Item upgrade() {
        chargeCap = 5 + (level() + 1) / 2;
        return super.upgrade();
    }

    @Override
    public String desc() {
        String desc = "盗圣留下的信物，可偷窃敌人物品。攻击时概率使敌人残废。";
        if (isEquipped(Dungeon.hero)) {
            float crippleChance = 0.15f + 0.05f * level();
            desc += "\n\n当前等级：" + level()
                    + "\n残废概率：" + (int)(crippleChance * 100) + "%"
                    + "\n金币加成：击杀额外获得 " + (50 + 10 * level()) + "%"
                    + "\n充能：" + charge + "/" + chargeCap;
            if (cursed) {
                desc += "\n\n盗圣之证已被诅咒，会偷走你的金币！";
            } else {
                desc += "\n\n对敌人使用以尝试偷窃其携带的物品。";
            }
        }
        return desc;
    }

    public class ThiefSealThievery extends ArtifactBuff {

        @Override
        public boolean act() {
            if (cursed && Dungeon.gold > 0 && Random.Int(5) == 0) {
                Dungeon.gold--;
                updateQuickslot();
            }
            spend(TICK);
            return true;
        }

        public void gainCharge(float levelPortion) {
            if (cursed || target.buff(MagicImmune.class) != null) return;

            if (charge < chargeCap) {
                float chargeGain = 3f * levelPortion;
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);

                partialCharge += chargeGain;
                while (partialCharge > 1f) {
                    partialCharge--;
                    charge++;
                    updateQuickslot();

                    if (charge == chargeCap) {
                        GLog.p("盗圣之证充能已满！");
                        partialCharge = 0;
                    }
                }
            } else {
                partialCharge = 0f;
            }
        }
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (action.equals(AC_STEAL)) return "偷窃";
        return super.actionName(action, hero);
    }
}
