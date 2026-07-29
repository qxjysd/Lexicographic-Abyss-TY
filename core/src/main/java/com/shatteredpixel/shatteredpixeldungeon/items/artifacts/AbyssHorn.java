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
 * This is a modified version of Shattered Pixel Dungeon. Original copyrights apply.
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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MeatPie;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 洞渊神角 — 攻击时概率造成额外伤害
 * 可储存食物充能，食用充能恢复饱食度
 * 储存食物可提升神器等级
 * 等级提升时触发概率和额外伤害同步提升
 */
public class AbyssHorn extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_HORN1;
        levelCap = 10;

        charge = 0;
        partialCharge = 0;
        chargeCap = 5 + level() / 2;

        defaultAction = AC_SNACK;
    }

    private int storedFoodEnergy = 0;

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
        if (isEquipped(hero) && level() < levelCap && !cursed) {
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
            else if (charge == 0) GLog.i("洞渊神角中没有储存的食物！");
            else {
                // 消耗充能恢复饱食度，最少消耗1点
                int satietyPerCharge = (int) (Hunger.STARVING / 5f);
                if (Dungeon.isChallenged(Challenges.NO_FOOD)) {
                    satietyPerCharge /= 3;
                }

                Hunger hunger = Buff.affect(Dungeon.hero, Hunger.class);
                int chargesToUse = Math.max(1, hunger.hunger() / satietyPerCharge);
                if (chargesToUse > charge) chargesToUse = charge;

                // 零食模式只消耗1点充能
                if (action.equals(AC_SNACK)) {
                    chargesToUse = 1;
                }

                doEatEffect(hero, chargesToUse);
            }

        } else if (action.equals(AC_STORE)) {
            GameScene.selectItem(itemSelector);
        }
    }

    /**
     * 执行进食效果
     */
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
        GLog.i("洞渊神角释放了储存的食物能量！");

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

    /**
     * 根据充能更新图标
     */
    private void updateChargeImage() {
        if (charge >= 8)        image = ItemSpriteSheet.ARTIFACT_HORN4;
        else if (charge >= 5)   image = ItemSpriteSheet.ARTIFACT_HORN3;
        else if (charge >= 2)   image = ItemSpriteSheet.ARTIFACT_HORN2;
        else                    image = ItemSpriteSheet.ARTIFACT_HORN1;
    }

    /**
     * 攻击触发额外伤害 — (10+2*等级)%概率造成(50+5*等级)%额外伤害
     */
    public int onHeroAttack(Hero hero, Char enemy, int damage) {
        if (cursed || hero.buff(MagicImmune.class) != null || damage <= 0) {
            return damage;
        }

        float chance = 0.1f + 0.02f * level();
        if (Random.Float() < chance) {
            float extraDmgMultiplier = 0.5f + 0.05f * level();
            int extraDamage = Math.round(damage * extraDmgMultiplier);
            GLog.p("洞渊神角触发！造成额外 " + extraDamage + " 点伤害！");
            return damage + extraDamage;
        }

        return damage;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new AbyssHornRecharge();
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
        String desc = "来自深渊的神秘号角，可储存食物充能恢复饱食度。攻击时有概率触发额外伤害。";
        if (isEquipped(Dungeon.hero)) {
            float chance = 0.1f + 0.02f * level();
            float extraDmg = 0.5f + 0.05f * level();
            desc += "\n\n当前等级：" + level()
                    + "\n触发概率：" + (int) (chance * 100) + "%"
                    + "\n额外伤害：" + (int) (extraDmg * 100) + "%"
                    + "\n充能：" + charge + "/" + chargeCap;
            if (cursed) {
                desc += "\n\n神角已被诅咒，无法使用！";
            } else if (level() < levelCap) {
                desc += "\n\n将食物存入神角可提升其等级。";
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

    /**
     * 储存食物能量用于升级
     */
    public void gainFoodValue(Food food) {
        if (level() >= 10) return;

        storedFoodEnergy += food.energy;
        // 馅饼和幻影肉值2次升级，肉馅饼值4次
        if (food instanceof Pasty || food instanceof PhantomMeat) {
            storedFoodEnergy += Hunger.HUNGRY / 2;
        } else if (food instanceof MeatPie) {
            storedFoodEnergy += Hunger.HUNGRY;
        }
        if (storedFoodEnergy >= Hunger.HUNGRY) {
            int upgrades = storedFoodEnergy / (int) Hunger.HUNGRY;
            upgrades = Math.min(upgrades, 10 - level());
            upgrade(upgrades);
            Catalog.countUse(AbyssHorn.class);
            storedFoodEnergy -= upgrades * Hunger.HUNGRY;
            if (level() == 10) {
                storedFoodEnergy = 0;
                GLog.p("洞渊神角已达到最大等级！");
            } else {
                GLog.p("洞渊神角吸收了食物能量，等级提升！");
            }
        } else {
            GLog.i("洞渊神角吸收了食物能量。");
        }
    }

    private static final String STORED = "stored";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STORED, storedFoodEnergy);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        storedFoodEnergy = bundle.getInt(STORED);

        updateChargeImage();
    }

    /**
     * 神角被动充能 — 随英雄等级自动恢复充能
     */
    public class AbyssHornRecharge extends ArtifactBuff {

        public void gainCharge(float levelPortion) {
            if (cursed || target.buff(MagicImmune.class) != null) return;

            if (charge < chargeCap) {
                // 每级产生0.25x最大饥饿值充能，每级神角额外+0.125x，最多1.5x
                float chargeGain = Hunger.STARVING * levelPortion * (0.25f + (0.125f * level()));
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);

                // 每点充能 = 1/5 最大饥饿值
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

    /**
     * 选择食物的物品选择器
     */
    protected static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return "选择要存入洞渊神角的食物";
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof Food;
        }

        @Override
        public void onSelect(Item item) {
            if (item != null && item instanceof Food) {
                if (item instanceof Blandfruit && ((Blandfruit) item).potionAttrib == null) {
                    GLog.w("洞渊神角不接受未调制的盲果！");
                } else {
                    Hero hero = Dungeon.hero;
                    hero.sprite.operate(hero.pos);
                    hero.busy();
                    hero.spend(Food.TIME_TO_EAT);

                    ((AbyssHorn) curItem).gainFoodValue(((Food) item));
                    item.detach(hero.belongings.backpack);
                }
            }
        }
    };
}
