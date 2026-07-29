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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Bleeding extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	protected float level;

	//used in specific cases where the source of the bleed is important for death logic
	private Class source;

	private int turnsLeft = 0;

	public float level(){
		return level;
	}
	
	private static final String LEVEL	= "level";
	private static final String SOURCE	= "source";
	private static final String TURNS	= "turns";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
		bundle.put( SOURCE, source );
		bundle.put( TURNS, turnsLeft );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getFloat( LEVEL );
		source = bundle.getClass( SOURCE );
		turnsLeft = bundle.getInt( TURNS );
	}
	
	public void set( float level ) {
		set( level, null );
	}

	public void set( float level, Class source ){
		set( level, source, 0 );
	}

	public void set( float level, Class source, int turns ){
		if (this.level < level) {
			this.level = Math.max(this.level, level);
			this.source = source;
		}
		this.turnsLeft = Math.max(this.turnsLeft, turns);
	}

	public void setDuration( int turns ) {
		this.turnsLeft = Math.max(this.turnsLeft, turns);
	}

	public void extend( float amount ) {
		level += amount;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BLEEDING;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.round(level));
	}
	
	@Override
	public boolean act() {
		if (target.isAlive()) {

			level = Random.NormalFloat(level / 2f, level);
			int dmg = Math.round(level);

			if (dmg > 0) {

				target.damage( dmg, this );
				if (target.sprite.visible) {
					Splash.at( target.sprite.center(), -PointF.PI / 2, PointF.PI / 6,
							target.sprite.blood(), Math.min( 10 * dmg / target.HT, 10 ) );
				}

				if (target == Dungeon.hero && !target.isAlive()) {
					if (source == Chasm.class){
						Badges.validateDeathFromFalling();
					} else if (source == Sacrificial.class){
						Badges.validateDeathFromFriendlyMagic();
					}
					Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
				}

				if (source == Sickle.HarvestBleedTracker.class && !target.isAlive()){
					MeleeWeapon.onAbilityKill(Dungeon.hero, target);
				}

				// 如果设置了回合限制，递减并到期后自动移除
				if (turnsLeft > 0) {
					turnsLeft--;
					if (turnsLeft <= 0) {
						detach();
						return true;
					}
				}

				spend( TICK );
			} else {
				detach();
			}

		} else {

			detach();

		}

		return true;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round(level));
	}
}
