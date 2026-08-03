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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 太一圣杯 — 穿刺献祭升级 + 攻击吸血
 * 每5回合恢复(1+等级)%最大生命值
 * 攻击时回复(15+5×等级)%伤害值的生命
 * 可通过穿刺献祭或造成伤害积累经验升级
 */
public class TaiyiHolyGrail extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_CHALICE1;
        levelCap = Integer.MAX_VALUE;
    }

    public static final String AC_PRICK = "PRICK";

    @Override
    public String name() { return "太一圣杯"; }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero) && !cursed && !hero.isInvulnerable(getClass()) && hero.buff(MagicImmune.class) == null)
            actions.add(AC_PRICK);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_PRICK)) {
            int minDmg = minPrickDmg();
            int maxDmg = maxPrickDmg();
            int totalHeroHP = hero.HP + hero.shielding();
            float deathChance = 0;
            if (totalHeroHP < maxDmg) {
                deathChance = (maxDmg - totalHeroHP) / (float) (maxDmg - minDmg);
                if (deathChance < 0.5f) {
                    deathChance = (float) Math.pow(2 * deathChance, 2) / 2f;
                } else if (deathChance < 1f) {
                    deathChance = 1f - deathChance;
                    deathChance = (float) Math.pow(2 * deathChance, 2) / 2f;
                    deathChance = 1f - deathChance;
                } else {
                    deathChance = 1;
                }
            }

            String warnMsg = "穿刺圣杯将对您造成 " + minDmg + " ~ " + maxDmg + " 点伤害，"
                    + "死亡率：" + String.format("%.0f", deathChance * 100) + "%"
                    + "。\n\n确定要进行穿刺献祭吗？";

            GameScene.show(
                new WndOptions(new ItemSprite(this), name(), warnMsg, "确定", "取消") {
                    @Override
                    protected void onSelect(int index) {
                        if (index == 0) prick(Dungeon.hero);
                    }
                }
            );
        }
    }

    private int minPrickDmg() {
        return (int) Math.ceil(3 + 2.5f * (level() * level()));
    }

    private int maxPrickDmg() {
        return (int) Math.floor(7 + 3.5f * (level() * level()));
    }

    private void prick(Hero hero) {
        int damage = Random.NormalIntRange(minPrickDmg(), maxPrickDmg());
        Earthroot.Armor armor = hero.buff(Earthroot.Armor.class);
        if (armor != null) damage = armor.absorb(damage);
        if (hero.buff(MagicImmune.class) != null && hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard.HolyArmBuff.class) != null) {
            damage -= hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.PALADIN ? 3 : 1;
        }
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor rockArmor = hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.RockArmor.class);
        if (rockArmor != null) damage = rockArmor.absorb(damage);
        damage -= hero.drRoll();

        hero.sprite.operate(hero.pos);
        hero.busy();
        hero.spend(Actor.TICK);
        GLog.w("圣杯之刺深深扎入你的身体！");
        if (damage <= 0) {
            damage = 1;
        } else {
            Sample.INSTANCE.play(Assets.Sounds.CURSED);
            hero.sprite.emitter().burst(ShadowParticle.CURSE, 4 + (damage / 10));
        }
        hero.damage(damage, this);
        if (!hero.isAlive()) {
            Badges.validateDeathFromFriendlyMagic();
            Dungeon.fail(this);
            GLog.n("你死在了圣杯的穿刺献祭之下……");
        } else {
            upgrade();
            Catalog.countUse(getClass());
        }
    }

    /**
     * 攻击触发：吸血 + 经验积累升级
     */
    public int onHeroAttack(Hero hero, Char enemy, int damage) {
        if (cursed || hero.buff(MagicImmune.class) != null || damage <= 0) return damage;

        // 基础攻击力加成（每级 +1.5 伤害）
        damage += Math.round(level() * 1.5f);

        float lifestealPct = 0.15f + 0.05f * level();
        int healAmount = Math.round(damage * lifestealPct);
        if (healAmount > 0 && hero.HP < hero.HT) {
            hero.HP = Math.min(hero.HT, hero.HP + healAmount);
            hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healAmount), FloatingText.HEALING);
        }

        // 造成伤害积累经验升级
        if (enemy.buff(MagicImmune.class) == null) {
            exp += Math.max(1, damage / 3);
            while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
                exp -= 10 + Math.round(3.33f * level());
                Catalog.countUse(getClass());
                GLog.p("太一圣杯吸收血气，等级提升！");
                upgrade();
            }
        }
        return damage;
    }

    // 击杀敌人时获得经验
    public void onKillEnemy(Char enemy) {
        if (cursed) return;
        exp += 5 + Math.round(level() / 2f);
        while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
            exp -= 10 + Math.round(3.33f * level());
            Catalog.countUse(getClass());
            GLog.p("太一圣杯吸收血气，等级提升！");
            upgrade();
        }
    }

    @Override
    public Item upgrade() {
        if (level() >= 6) image = ItemSpriteSheet.ARTIFACT_CHALICE3;
        else if (level() >= 2) image = ItemSpriteSheet.ARTIFACT_CHALICE2;
        return super.upgrade();
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (level() >= 7) image = ItemSpriteSheet.ARTIFACT_CHALICE3;
        else if (level() >= 3) image = ItemSpriteSheet.ARTIFACT_CHALICE2;
    }

    @Override
    public void charge(Hero target, float amount) {}

    @Override
    protected ArtifactBuff passiveBuff() { return new HolyGrailRegen(); }

    @Override
    public String desc() {
        String desc = "蕴含太一之力的神圣圣杯，可穿刺献祭升级，攻击时汲取敌人生命。";
        if (isEquipped(Dungeon.hero)) {
            if (cursed) {
                desc += "\n\n圣杯已被诅咒，无法使用！";
            } else {
                float lifestealPct = 0.15f + 0.05f * level();
                desc += "\n\n当前等级：" + level()
                        + "\n吸血效果：" + (int)(lifestealPct * 100) + "%"
                        + "\n回复效果：每5回合+" + (1 + level()) + "%生命"
                        + "\n穿刺伤害：" + minPrickDmg() + "~" + maxPrickDmg();
            }
        }
        return desc;
    }

    public class HolyGrailRegen extends ArtifactBuff {
        private float timer = 0f;
        @Override
        public boolean act() {
            if (cursed || target.buff(MagicImmune.class) != null) { spend(TICK); return true; }
            timer += TICK;
            if (timer >= 5f) {
                timer = 0f;
                if (target instanceof Hero) {
                    Hero hero = (Hero) target;
                    if (hero.HP < hero.HT && hero.isAlive()) {
                        int healAmount = Math.max(1, hero.HT * (1 + level()) / 100);
                        hero.HP = Math.min(hero.HT, hero.HP + healAmount);
                        hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healAmount), FloatingText.HEALING);
                    }
                }
            }
            spend(TICK);
            return true;
        }
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (action.equals(AC_PRICK)) return "穿刺";
        return super.actionName(action, hero);
    }
}
