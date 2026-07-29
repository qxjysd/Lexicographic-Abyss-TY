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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;
	public Artifact artifact = null;
	public KindofMisc[] misc = new KindofMisc[4];
	public Ring[] ring = new Ring[3];

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return artifact;
		} else {
			return null;
		}
	}

	public KindofMisc[] misc(){
		if (!lostInventory()){
			return misc;
		} else {
			KindofMisc[] result = new KindofMisc[4];
			for (int i=0; i < 4; i++){
				if (misc[i] != null && misc[i].keptThroughLostInventory()){
					result[i] = misc[i];
				}
			}
			return result;
		}
	}

	public Ring[] ring(){
		if (!lostInventory()){
			return ring;
		} else {
			Ring[] result = new Ring[3];
			for (int i=0; i < 3; i++){
				if (ring[i] != null && ring[i].keptThroughLostInventory()){
					result[i] = ring[i];
				}
			}
			return result;
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( ARTIFACT, artifact );
		for (int i=0; i < misc.length; i++){
			bundle.put( MISC + i, misc[i] );
		}
		for (int i=0; i < ring.length; i++){
			bundle.put( RING + i, ring[i] );
		}
		bundle.put( SECOND_WEP, secondWep );
	}

	public static boolean bundleRestoring = false;
	
	public void restoreFromBundle( Bundle bundle ) {
		bundleRestoring = true;
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		artifact = (Artifact) bundle.get(ARTIFACT);
		if (artifact() != null)     artifact().activate(owner);

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);

		for (int i=0; i < misc.length; i++){
			misc[i] = (KindofMisc) bundle.get(MISC + i);
			if (misc[i] != null)    misc[i].activate(owner);
		}

		for (int i=0; i < ring.length; i++){
			ring[i] = (Ring) bundle.get(RING + i);
			if (ring[i] != null)    ring[i].activate(owner);
		}

		bundleRestoring = false;
	}

	public void clear(){
		backpack.clear();
		weapon = secondWep = null;
		armor = null;
		artifact = null;
		for (int i=0; i < misc.length; i++) misc[i] = null;
		for (int i=0; i < ring.length; i++) ring[i] = null;
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
		public ArrayList<Bag> getBags(){
			ArrayList<Bag> result = new ArrayList<>();

			result.add(backpack);

			for (Item i : this){
				if (i instanceof Bag){
					result.add((Bag)i);
				}
			}

			// 添加装备栏（背包中显示当前装备）
			result.add(new com.shatteredpixel.shatteredpixeldungeon.items.bags.EquipmentBag(this));

			return result;
		}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}
		if (artifact() != null) {
			//oblivion shard does not prevent artifact IDing
			artifact().identify();
			Badges.validateItemLevelAquired(artifact());
		}
		KindofMisc[] miscItems = misc();
		for (int i = 0; i < miscItems.length; i++) {
			if (miscItems[i] != null) {
				if (ShardOfOblivion.passiveIDDisabled() && miscItems[i] instanceof Ring){
					((Ring) miscItems[i]).setIDReady();
				} else {
					miscItems[i].identify();
					Badges.validateItemLevelAquired(miscItems[i]);
				}
			}
		}
		Ring[] ringItems = ring();
		for (int i = 0; i < ringItems.length; i++) {
			if (ringItems[i] != null) {
				if (ShardOfOblivion.passiveIDDisabled()){
					ringItems[i].setIDReady();
				} else {
					ringItems[i].identify();
					Badges.validateItemLevelAquired(ringItems[i]);
				}
			}
		}
		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		Item[] allEquipped = getAllEquipped();
		ScrollOfRemoveCurse.uncurse( owner, allEquipped );
	}

	public Item[] getAllEquipped(){
		ArrayList<Item> items = new ArrayList<>();
		items.add(armor());
		items.add(weapon());
		items.add(artifact());
		items.add(secondWep());
		for (KindofMisc m : misc()) if (m != null) items.add(m);
		for (Ring r : ring()) if (r != null) items.add(r);
		return items.toArray(new Item[0]);
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, artifact, misc[0], misc[1], misc[2], misc[3], ring[0], ring[1], ring[2], secondWep};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = artifact = null;
				break;
			case 3:
				equipped[3] = misc[0] = null;
				break;
			case 4:
				equipped[4] = misc[1] = null;
				break;
			case 5:
				equipped[5] = misc[2] = null;
				break;
			case 6:
				equipped[6] = misc[3] = null;
				break;
			case 7:
				equipped[7] = ring[0] = null;
				break;
			case 8:
				equipped[8] = ring[1] = null;
				break;
			case 9:
				equipped[9] = ring[2] = null;
				break;
			case 10:
				equipped[10] = secondWep = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
