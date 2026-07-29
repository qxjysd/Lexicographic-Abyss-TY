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
 * 盗圣之证 — 击杀怪物额外获得(50+10×等级)%金币
 * 可偷窃敌人获取物品并提升等级
 * 等级提升时金币加成和偷窃成功率同步提升
 */
public class ThiefSeal extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_ARMBAND;
        levelCap = 10;

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
     * 偷窃目标选择器
     */
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

    /**
     * 偷窃记录 — 防止重复偷窃同一目标
     */
    public static class StolenTracker extends CounterBuff {
        { revivePersists = true; }

        public void setItemStolen(boolean stolen) { if (stolen) countUp(1); }

        public boolean itemWasStolen() { return count() > 0; }
    }

    /**
     * 击杀敌人时额外金币倍率
     * 如: 0.5 = 额外50%, 1.0 = 额外100%
     */
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
        String desc = "盗圣留下的信物，可偷窃敌人获取物品并借此升级。击杀怪物额外获得金币。";
        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n当前等级：" + level()
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

    /**
     * 盗证被动 — 自动充能 + 偷窃逻辑
     */
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

        /**
         * 随英雄等级自动获得充能
         */
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

        /**
         * 偷窃物品（返回是否成功）
         */
        public boolean steal(Item item) {
            int chargesUsed = chargesToUse(item);
            float stealChance = stealChance(item);
            if (Random.Float() > stealChance) {
                return false;
            } else {
                charge -= chargesUsed;
                exp += 4 * chargesUsed;
                GLog.i("偷窃成功！获得了 " + item.name() + "！");

                Talent.onArtifactUsed(Dungeon.hero);
                while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
                    exp -= 10 + Math.round(3.33f * level());
                    Catalog.countUse(ThiefSeal.class);
                    GLog.p("盗圣之证经验提升，等级提高！");
                    upgrade();
                }
                updateQuickslot();
                return true;
            }
        }

        /**
         * 计算偷窃成功率
         */
        public float stealChance(Item item) {
            int chargesUsed = chargesToUse(item);
            float val = chargesUsed * (10 + level() / 2f);
            return Math.min(1f, val / item.value());
        }

        /**
         * 计算偷窃物品所需充能数
         */
        public int chargesToUse(Item item) {
            int value = item.value();
            float valUsing = 0;
            int chargesUsed = 0;
            while (valUsing < value && chargesUsed < charge) {
                valUsing += 10 + level() / 2f;
                chargesUsed++;
            }
            return chargesUsed;
        }
    }
}
