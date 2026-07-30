/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Lexicographic-Abyss (modified version) by 许玄
 * Copyright (C) 2024-2026 许玄
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 洞渊神角 — 攻击使敌人流血 + 击杀升级
 * 可食用储存的能量恢复饱食度
 * 攻击时概率使敌人陷入流血状态
 */
public class AbyssHorn extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_HORN1;
        levelCap = Integer.MAX_VALUE;

        charge = 0;
        partialCharge = 0;
        chargeCap = 5 + level() / 2;

        defaultAction = AC_SNACK;
    }

    public static final String AC_SNACK = "SNACK";
    public static final String AC_EAT = "EAT";
    public static final String AC_STORE = "STORE";

    @Override
    public String name() {
        return "洞渊神角";
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (hero.buff(MagicImmune.class) != null) return actions;
        if (isEquipped(hero) && charge > 0) {
            actions.add(AC_SNACK);
            actions.add(AC_EAT);
        }
        if (isEquipped(hero) && !cursed) {
            actions.add(AC_STORE);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_EAT) || action.equals(AC_SNACK)) {

            if (!isEquipped(hero)) GLog.i("需要先装备洞渊神角！");
            else if (charge == 0) GLog.i("洞渊神角中没有储存的能量！");
            else {
                int satietyPerCharge = (int) (Hunger.STARVING / 5f);
                if (Dungeon.isChallenged(Challenges.NO_FOOD)) {
                    satietyPerCharge /= 3;
                }

                Hunger hunger = Buff.affect(Dungeon.hero, Hunger.class);
                int chargesToUse = Math.max(1, hunger.hunger() / satietyPerCharge);
                if (chargesToUse > charge) chargesToUse = charge;

                if (action.equals(AC_SNACK)) {
                    chargesToUse = 1;
                }

                doEatEffect(hero, chargesToUse);
            }

        } else if (action.equals(AC_STORE)) {
            GameScene.selectItem(itemSelector);
        }
    }

    public void doEatEffect(Hero hero, int chargesToUse) {
        int satietyPerCharge = (int) (Hunger.STARVING / 5f);
        if (Dungeon.isChallenged(Challenges.NO_FOOD)) {
            satietyPerCharge /= 3;
        }

        Buff.affect(hero, Hunger.class).satisfy(satietyPerCharge * chargesToUse);
        Statistics.foodEaten++;
        charge -= chargesToUse;
        Talent.onArtifactUsed(hero);

        hero.sprite.operate(hero.pos);
        hero.busy();
        SpellSprite.show(hero, SpellSprite.FOOD);
        Sample.INSTANCE.play(Assets.Sounds.EAT);
        GLog.i("洞渊神角释放了储存的能量！");

        if (Dungeon.hero.hasTalent(Talent.IRON_STOMACH)
                || Dungeon.hero.hasTalent(Talent.ENERGIZING_MEAL)
                || Dungeon.hero.hasTalent(Talent.MYSTICAL_MEAL)
                || Dungeon.hero.hasTalent(Talent.INVIGORATING_MEAL)
                || Dungeon.hero.hasTalent(Talent.FOCUSED_MEAL)
                || Dungeon.hero.hasTalent(Talent.ENLIGHTENING_MEAL)) {
            hero.spend(Food.TIME_TO_EAT - 2);
        } else {
            hero.spend(Food.TIME_TO_EAT);
        }

        Talent.onFoodEaten(hero, satietyPerCharge * chargesToUse, this);
        Badges.validateFoodEaten();
        updateChargeImage();
        updateQuickslot();
    }

    private void updateChargeImage() {
        if (charge >= 8)        image = ItemSpriteSheet.ARTIFACT_HORN4;
        else if (charge >= 5)   image = ItemSpriteSheet.ARTIFACT_HORN3;
        else if (charge >= 2)   image = ItemSpriteSheet.ARTIFACT_HORN2;
        else                    image = ItemSpriteSheet.ARTIFACT_HORN1;
    }

    /**
     * 攻击触发：概率流血 + 经验积累升级
     * 流血概率：(10 + 5×等级)%
     * 流血持续：(2 + 等级/2) 回合
     */
    public int onHeroAttack(Hero hero, Char enemy, int damage) {
        if (cursed || hero.buff(MagicImmune.class) != null || damage <= 0) {
            return damage;
        }

        // 概率使敌人流血
        float bleedChance = 0.1f + 0.05f * level();
        if (Random.Float() < bleedChance && enemy.buff(MagicImmune.class) == null) {
            int bleedDuration = 2 + level() / 2;
            Buff.affect(enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class).set(bleedDuration);
            GLog.p("洞渊神角使敌人陷入流血状态！");
        }

        // 造成伤害积累经验升级
        if (enemy.buff(MagicImmune.class) == null) {
            exp += Math.max(1, damage / 3);
            while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
                exp -= 10 + Math.round(3.33f * level());
                Catalog.countUse(getClass());
                GLog.p("洞渊神角吞噬血气，等级提升！");
                upgrade();
            }
        }

        return damage;
    }

    @Override
    public void charge(Hero target, float amount) {
        if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
            partialCharge += 0.25f * amount;
            while (partialCharge >= 1) {
                partialCharge--;
                charge++;
                if (charge == chargeCap) {
                    GLog.p("洞渊神角充能已满！");
                    partialCharge = 0;
                }
                updateChargeImage();
                updateQuickslot();
            }
        }
    }

    @Override
    public String desc() {
        String desc = "来自深渊的神秘号角，可储存能量恢复饱食度。攻击时有概率使敌人流血。";
        if (isEquipped(Dungeon.hero)) {
            float bleedChance = 0.1f + 0.05f * level();
            int bleedDuration = 2 + level() / 2;
            desc += "\n\n当前等级：" + level()
                    + "\n流血概率：" + (int)(bleedChance * 100) + "%"
                    + "\n流血持续：" + bleedDuration + " 回合"
                    + "\n充能：" + charge + "/" + chargeCap;
            if (cursed) {
                desc += "\n\n神角已被诅咒，无法使用！";
            }
        }
        return desc;
    }

    @Override
    public void level(int value) {
        super.level(value);
        chargeCap = 5 + level() / 2;
    }

    @Override
    public Item upgrade() {
        super.upgrade();
        chargeCap = 5 + level() / 2;
        return this;
    }

    public void gainFoodValue(com.shatteredpixel.shatteredpixeldungeon.items.food.Food food) {
        if (level() >= levelCap) return;
        int foodEnergy = (int) food.energy;
        if (food instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty
                || food instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat) {
            foodEnergy += com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.HUNGRY / 2;
        } else if (food instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.MeatPie) {
            foodEnergy += com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.HUNGRY;
        }
        if (foodEnergy >= com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.HUNGRY) {
            int upgrades = foodEnergy / (int) com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.HUNGRY;
            upgrade(upgrades);
            Catalog.countUse(AbyssHorn.class);
            GLog.p("洞渊神角吸收了食物能量，等级提升！");
        } else {
            GLog.i("洞渊神角吸收了食物能量。");
        }
    }

    protected static com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector itemSelector =
            new com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector() {
        @Override
        public String textPrompt() { return "选择要存入洞渊神角的食物"; }
        @Override
        public Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag> preferredBag() {
            return com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings.Backpack.class;
        }
        @Override
        public boolean itemSelectable(com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
            return item instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
        }
        @Override
        public void onSelect(com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
            if (item != null && item instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food) {
                if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit
                        && ((com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit) item).potionAttrib == null) {
                    GLog.w("洞渊神角不接受未调制的盲果！");
                } else {
                    Hero hero = Dungeon.hero;
                    hero.sprite.operate(hero.pos);
                    hero.busy();
                    hero.spend(com.shatteredpixel.shatteredpixeldungeon.items.food.Food.TIME_TO_EAT);
                    ((AbyssHorn) com.shatteredpixel.shatteredpixeldungeon.items.Item.curItem).gainFoodValue(((com.shatteredpixel.shatteredpixeldungeon.items.food.Food) item));
                    item.detach(hero.belongings.backpack);
                }
            }
        }
    };

    @Override
    protected ArtifactBuff passiveBuff() {
        return new AbyssHornRecharge();
    }

    public class AbyssHornRecharge extends ArtifactBuff {
        public void gainCharge(float levelPortion) {
            if (cursed || target.buff(MagicImmune.class) != null) return;
            if (charge < chargeCap) {
                float chargeGain = Hunger.STARVING * levelPortion * (0.25f + (0.125f * level()));
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);
                chargeGain /= Hunger.STARVING / 5;
                partialCharge += chargeGain;
                while (partialCharge >= 1) {
                    charge++;
                    partialCharge -= 1;
                    updateChargeImage();
                    updateQuickslot();
                    if (charge == chargeCap) {
                        GLog.p("洞渊神角充能已满！");
                        partialCharge = 0;
                    }
                }
            } else {
                partialCharge = 0;
            }
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        updateChargeImage();
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (action.equals(AC_SNACK)) return "零食";
        if (action.equals(AC_EAT)) return "进食";
        if (action.equals(AC_STORE)) return "存入";
        return super.actionName(action, hero);
    }
}
