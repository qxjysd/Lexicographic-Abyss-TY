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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWard;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
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
 * 太一圣杯 — 每5回合恢复(1+等级)%最大生命值
 * 可通过"穿刺"献祭升级，等级越高穿刺伤害越大
 * 等级提升时回血效果同步提升
 */
public class TaiyiHolyGrail extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_CHALICE1;
        levelCap = 10;
    }

    public static final String AC_PRICK = "PRICK";

    @Override
    public String name() { return "太一圣杯"; }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero)
                && !cursed
                && !hero.isInvulnerable(getClass())
                && hero.buff(MagicImmune.class) == null)
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
                new WndOptions(new ItemSprite(this),
                        name(),
                        warnMsg,
                        "确定",
                        "取消") {
                    @Override
                    protected void onSelect(int index) {
                        if (index == 0) {
                            prick(Dungeon.hero);
                        }
                    }
                }
            );
        }
    }

    /**
     * 计算穿刺最小伤害，随等级平方增长
     */
    private int minPrickDmg() {
        return (int) Math.ceil(3 + 2.5f * (level() * level()));
    }

    /**
     * 计算穿刺最大伤害，随等级平方增长
     */
    private int maxPrickDmg() {
        return (int) Math.floor(7 + 3.5f * (level() * level()));
    }

    /**
     * 执行穿刺献祭：对英雄造成伤害，成功存活则升级
     */
    private void prick(Hero hero) {
        int damage = Random.NormalIntRange(minPrickDmg(), maxPrickDmg());

        // 处理伤害减免效果
        Earthroot.Armor armor = hero.buff(Earthroot.Armor.class);
        if (armor != null) {
            damage = armor.absorb(damage);
        }

        if (hero.buff(MagicImmune.class) != null && hero.buff(HolyWard.HolyArmBuff.class) != null) {
            damage -= hero.subClass == HeroSubClass.PALADIN ? 3 : 1;
        }

        WandOfLivingEarth.RockArmor rockArmor = hero.buff(WandOfLivingEarth.RockArmor.class);
        if (rockArmor != null) {
            damage = rockArmor.absorb(damage);
        }

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

    @Override
    public Item upgrade() {
        if (level() >= 6)
            image = ItemSpriteSheet.ARTIFACT_CHALICE3;
        else if (level() >= 2)
            image = ItemSpriteSheet.ARTIFACT_CHALICE2;
        return super.upgrade();
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (level() >= 7) image = ItemSpriteSheet.ARTIFACT_CHALICE3;
        else if (level() >= 3) image = ItemSpriteSheet.ARTIFACT_CHALICE2;
    }

    @Override
    public void charge(Hero target, float amount) {
        // charge is handled by passiveBuff
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new HolyGrailRegen();
    }

    @Override
    public String desc() {
        String desc = "蕴含太一之力的神圣圣杯，可穿刺献祭升级，能持续恢复生命。";
        if (isEquipped(Dungeon.hero)) {
            if (cursed) {
                desc += "\n\n圣杯已被诅咒，无法使用！";
            } else {
                desc += "\n\n当前等级：" + level()
                        + "\n回复效果：每5回合恢复 " + (1 + level()) + "% 生命值"
                        + "\n穿刺伤害：" + minPrickDmg() + " ~ " + maxPrickDmg();
            }
        }
        return desc;
    }

    /**
     * 圣杯被动回复 — 每5回合恢复(1+等级)%最大生命值
     */
    public class HolyGrailRegen extends ArtifactBuff {

        private float timer = 0f;

        @Override
        public boolean act() {
            if (cursed || target.buff(MagicImmune.class) != null) {
                spend(TICK);
                return true;
            }

            timer += TICK;
            if (timer >= 5f) { // 每5回合
                timer = 0f;
                if (target instanceof Hero) {
                    Hero hero = (Hero) target;
                    if (hero.HP < hero.HT && hero.isAlive()) {
                        int healPercent = 1 + level();
                        int healAmount = Math.max(1, hero.HT * healPercent / 100);
                        hero.HP = Math.min(hero.HT, hero.HP + healAmount);
                        hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healAmount), FloatingText.HEALING);
                    }
                }
            }

            spend(TICK);
            return true;
        }
    }
}
