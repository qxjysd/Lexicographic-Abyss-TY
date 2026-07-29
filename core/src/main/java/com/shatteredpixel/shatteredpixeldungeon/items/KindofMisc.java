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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class KindofMisc extends EquipableItem {

	@Override
	public boolean doEquip(final Hero hero) {

		//determine which slots to try for this item
		if (this instanceof Artifact
				&& hero.belongings.artifact != null
				&& !hasEmptyMiscSlot(hero)){

			//see if we can re-arrange items first
			if (hasEmptyRingSlot(hero)){
				for (int i = 0; i < hero.belongings.misc.length; i++){
					if (hero.belongings.misc[i] instanceof Ring){
						int emptyRing = firstEmptyRingSlot(hero);
						hero.belongings.ring[emptyRing] = (Ring) hero.belongings.misc[i];
						hero.belongings.misc[i] = null;
						break;
					}
				}
			}
			
			//check again after rearrangement
			if (hero.belongings.artifact != null && !hasEmptyMiscSlot(hero)){
				showEquipSwapDialog(hero);
				return false;
			}

		} else if (this instanceof Ring
				&& !hasEmptyRingSlot(hero)
				&& !hasEmptyMiscSlot(hero)){

			//see if we can re-arrange items first
			if (hero.belongings.artifact == null){
				for (int i = 0; i < hero.belongings.misc.length; i++){
					if (hero.belongings.misc[i] instanceof Artifact){
						hero.belongings.artifact = (Artifact) hero.belongings.misc[i];
						hero.belongings.misc[i] = null;
						break;
					}
				}
			}

			//check again after rearrangement
			if (!hasEmptyRingSlot(hero) && !hasEmptyMiscSlot(hero)){
				showEquipSwapDialog(hero);
				return false;
			}
		}

		// 15/25% chance
		if (hero.heroClass != HeroClass.CLERIC && hero.hasTalent(Talent.HOLY_INTUITION)
				&& cursed && !cursedKnown
				&& Random.Int(20) < 1 + 2*hero.pointsInTalent(Talent.HOLY_INTUITION)){
			cursedKnown = true;
			GLog.p(Messages.get(this, "curse_detected"));
			return false;
		}

		if (this instanceof Artifact){
			if (hero.belongings.artifact == null)   hero.belongings.artifact = (Artifact) this;
			else                                    placeInMiscSlot(hero, (Artifact) this);
		} else if (this instanceof Ring){
			int emptyRing = firstEmptyRingSlot(hero);
			if (emptyRing >= 0)                     hero.belongings.ring[emptyRing] = (Ring) this;
			else                                    placeInMiscSlot(hero, (Ring) this);
		}

		detach( hero.belongings.backpack );

		Talent.onItemEquipped(hero, this);
		activate( hero );

		cursedKnown = true;
		if (cursed) {
			equipCursed( hero );
			GLog.n( Messages.get(this, "equip_cursed", this) );
		}

		hero.spendAndNext( timeToEquip(hero) );
		return true;

	}

	private boolean hasEmptyMiscSlot(Hero hero){
		for (int i = 0; i < hero.belongings.misc.length; i++){
			if (hero.belongings.misc[i] == null) return true;
		}
		return false;
	}

	private boolean hasEmptyRingSlot(Hero hero){
		for (int i = 0; i < hero.belongings.ring.length; i++){
			if (hero.belongings.ring[i] == null) return true;
		}
		return false;
	}

	private int firstEmptyRingSlot(Hero hero){
		for (int i = 0; i < hero.belongings.ring.length; i++){
			if (hero.belongings.ring[i] == null) return i;
		}
		return -1;
	}

	private void placeInMiscSlot(Hero hero, KindofMisc item){
		for (int i = 0; i < hero.belongings.misc.length; i++){
			if (hero.belongings.misc[i] == null){
				hero.belongings.misc[i] = item;
				return;
			}
		}
	}

	private void showEquipSwapDialog(final Hero hero){

		//collect all occupied relevant slots
		ArrayList<KindofMisc> occupied = new ArrayList<>();
		ArrayList<Integer> occupiedTypes = new ArrayList<>(); //0=artifact, 1=misc, 2=ring
		ArrayList<Integer> occupiedIndices = new ArrayList<>();

		if (hero.belongings.artifact != null){
			occupied.add(hero.belongings.artifact);
			occupiedTypes.add(0);
			occupiedIndices.add(-1);
		}
		for (int i = 0; i < hero.belongings.misc.length; i++){
			if (hero.belongings.misc[i] != null){
				occupied.add(hero.belongings.misc[i]);
				occupiedTypes.add(1);
				occupiedIndices.add(i);
			}
		}
		for (int i = 0; i < hero.belongings.ring.length; i++){
			if (hero.belongings.ring[i] != null){
				occupied.add(hero.belongings.ring[i]);
				occupiedTypes.add(2);
				occupiedIndices.add(i);
			}
		}

		final ArrayList<KindofMisc> finalOccupied = occupied;
		final ArrayList<Integer> finalTypes = occupiedTypes;
		final ArrayList<Integer> finalIndices = occupiedIndices;
		final boolean[] enabled = new boolean[occupied.size()];
		for (int i = 0; i < enabled.length; i++) enabled[i] = true;

		String[] labels = new String[occupied.size()];
		for (int i = 0; i < occupied.size(); i++){
			labels[i] = occupied.get(i) == null ? "---" : Messages.titleCase(occupied.get(i).title());
		}

		GameScene.show(
				new WndOptions(new ItemSprite(this),
						Messages.get(KindofMisc.class, "unequip_title"),
						Messages.get(KindofMisc.class, "unequip_message"),
						labels) {

					@Override
					protected void onSelect(int index) {

						KindofMisc equipped = finalOccupied.get(index);
						int type = finalTypes.get(index);
						int slotIndex = finalIndices.get(index);

						int thisSlot = Dungeon.quickslot.getSlot(KindofMisc.this);
						slotOfUnequipped = -1;
						Dungeon.hero.belongings.backpack.items.remove(KindofMisc.this);
						if (equipped.doUnequip(hero, true, false)) {

							//place item in the vacated slot
							if (type == 0){
								//artifact slot
								if (KindofMisc.this instanceof Artifact){
									hero.belongings.artifact = (Artifact) KindofMisc.this;
								} else {
									hero.belongings.misc[0] = KindofMisc.this;
								}
							} else if (type == 1){
								//misc slot
								hero.belongings.misc[slotIndex] = KindofMisc.this;
							} else if (type == 2){
								//ring slot
								if (KindofMisc.this instanceof Ring){
									hero.belongings.ring[slotIndex] = (Ring) KindofMisc.this;
								} else {
									hero.belongings.misc[0] = KindofMisc.this;
								}
							}

							Dungeon.hero.belongings.backpack.items.add(KindofMisc.this);
							doEquip(hero);
						} else {
							Dungeon.hero.belongings.backpack.items.add(KindofMisc.this);
						}
						if (thisSlot != -1) {
							Dungeon.quickslot.setSlot(thisSlot, KindofMisc.this);
						} else if (slotOfUnequipped != -1 && defaultAction() != null){
							Dungeon.quickslot.setSlot(slotOfUnequipped, KindofMisc.this);
						}
						updateQuickslot();
					}

					@Override
					protected boolean enabled(int index) {
						return enabled[index];
					}
				});

	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){

			if (hero.belongings.artifact == this) {
				hero.belongings.artifact = null;
			} else {
				for (int i = 0; i < hero.belongings.misc.length; i++){
					if (hero.belongings.misc[i] == this){
						hero.belongings.misc[i] = null;
						break;
					}
				}
				for (int i = 0; i < hero.belongings.ring.length; i++){
					if (hero.belongings.ring[i] == this){
						hero.belongings.ring[i] = null;
						break;
					}
				}
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		if (hero == null) return false;
		if (hero.belongings.artifact() == this) return true;
		for (KindofMisc m : hero.belongings.misc()) {
			if (m == this) return true;
		}
		for (Ring r : hero.belongings.ring()) {
			if (r == this) return true;
		}
		return false;
	}

}
