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

package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 装备栏背包页 - 显示当前装备的所有物品
 * 包括：神器(artifact)、神器位(misc)、戒指(ring)、武器、防具
 */
public class EquipmentBag extends Bag {

    private transient Belongings belongings;

    public EquipmentBag() {
        super();
        image = ItemSpriteSheet.ARMOR_HOLDER;
    }

    public EquipmentBag(Belongings belongings) {
        this();
        this.belongings = belongings;
    }

    @Override
    public boolean contains(Item item) {
        return false;
    }

    @Override
    public boolean canHold(Item item) {
        return false;
    }

    @Override
    public String toString() {
        return "装备栏";
    }

    @Override
    public int capacity() {
        if (belongings == null) return 0;
        int count = 0;
        if (belongings.weapon != null) count++;
        if (belongings.armor != null) count++;
        if (belongings.artifact() != null) count++;
        if (belongings.misc != null) {
            for (KindofMisc m : belongings.misc) { if (m != null) count++; }
        }
        if (belongings.ring != null) {
            for (Ring r : belongings.ring) { if (r != null) count++; }
        }
        return Math.max(count, 5);
    }

    @Override
    public void clear() {}
}
