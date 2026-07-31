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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.ads.AdManager;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Trait;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AbyssHorn;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TaiyiHolyGrail;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ThiefSeal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Locale;

public class WndHero extends WndTabbed {

	private static final int WIDTH		= 120;
	private static final int HEIGHT		= 120;

	private StatsTab stats;
	private TalentsTab talents;
	private BuffsTab buffs;
	private TraitsTab traits;
	private AdRewardTab adReward;

	public static int lastIdx = 0;

	public WndHero() {

		super();

		resize( WIDTH, HEIGHT );

		stats = new StatsTab();
		add( stats );

		talents = new TalentsTab();
		add(talents);
		talents.setRect(0, 0, WIDTH, HEIGHT);

		buffs = new BuffsTab();
		add( buffs );
		buffs.setRect(0, 0, WIDTH, HEIGHT);
		buffs.setupList();

		traits = new TraitsTab();
		add( traits );
		traits.setRect(0, 0, WIDTH, HEIGHT);
		traits.setupList();


		adReward = new AdRewardTab();
		add( adReward );
		adReward.setRect(0, 0, WIDTH, HEIGHT);

		add( new IconTab( Icons.get(Icons.RANKINGS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) {
					lastIdx = 0;
					if (!stats.visible) {
						stats.initialize();
					}
				}
				stats.visible = stats.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.TALENT) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 1;
				if (selected) StatusPane.talentBlink = 0;
				talents.visible = talents.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.BUFFS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 2;
				buffs.visible = buffs.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.CATALOG) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 3;
				traits.visible = traits.active = selected;
				if (selected) traits.setupList();
			}
		} );
		add( new IconTab( Icons.get(Icons.GOLD) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 4;
				adReward.visible = adReward.active = selected;
			}
		} );

		layoutTabs();

		talents.setRect(0, 0, WIDTH, HEIGHT);
		talents.pane.scrollTo(0, talents.pane.content().height() - talents.pane.height());
		talents.layout();

		select( lastIdx );
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey( event ) == SPDAction.HERO_INFO) {
			onBackPressed();
			return true;
		} else {
			return super.onSignal(event);
		}
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		talents.layout();
		buffs.layout();
		traits.layout();
	}

	private class StatsTab extends Group {

		private static final int GAP = 6;

		private float pos;

		public StatsTab() {
			initialize();
		}

		public void initialize(){

			for (Gizmo g : members){
				if (g != null) g.destroy();
			}
			clear();

			Hero hero = Dungeon.hero;

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar(hero) );
			if (hero.name().equals(hero.className()))
				title.label( Messages.get(this, "title", hero.lvl, hero.className() ).toUpperCase( Locale.ENGLISH ) );
			else
				title.label((hero.name() + "\n" + Messages.get(this, "title", hero.lvl, hero.className())).toUpperCase(Locale.ENGLISH));
			title.color(Window.TITLE_COLOR);
			title.setRect( 0, 0, WIDTH-16, 0 );
			add(title);

			IconButton infoButton = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					if (ShatteredPixelDungeon.scene() instanceof GameScene){
						GameScene.show(new WndHeroInfo(hero.heroClass));
					} else {
						ShatteredPixelDungeon.scene().addToFront(new WndHeroInfo(hero.heroClass));
					}
				}

				@Override
				protected String hoverText() {
					return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
				}

			};
			infoButton.setRect(title.right(), 0, 16, 16);
			add(infoButton);

			pos = title.bottom() + 2*GAP;

			int strBonus = hero.STR() - hero.STR;
			if (strBonus > 0)           statSlot( Messages.get(this, "str"), hero.STR + " + " + strBonus );
			else if (strBonus < 0)      statSlot( Messages.get(this, "str"), hero.STR + " - " + -strBonus );
			else                        statSlot( Messages.get(this, "str"), hero.STR() );
			if (hero.shielding() > 0)   statSlot( Messages.get(this, "health"), hero.HP + "+" + hero.shielding() + "/" + hero.HT );
			else                        statSlot( Messages.get(this, "health"), (hero.HP) + "/" + hero.HT );
			statSlot( Messages.get(this, "exp"), hero.exp + "/" + hero.maxExp() );

			pos += GAP;

			statSlot( Messages.get(this, "gold"), Statistics.goldCollected );
			statSlot( Messages.get(this, "depth"), Statistics.deepestFloor );
			if (Dungeon.daily){
				if (!Dungeon.dailyReplay) {
					statSlot(Messages.get(this, "daily_for"), "_" + Dungeon.customSeedText + "_");
				} else {
					statSlot(Messages.get(this, "replay_for"), "_" + Dungeon.customSeedText + "_");
				}
			} else if (!Dungeon.customSeedText.isEmpty()){
				statSlot( Messages.get(this, "custom_seed"), "_" + Dungeon.customSeedText + "_" );
			} else {
				statSlot( Messages.get(this, "dungeon_seed"), DungeonSeed.convertToCode(Dungeon.seed) );
			}

			pos += GAP;
		}

		private void statSlot( String label, String value ) {

			int size = 8;
			RenderedTextBlock txt;
			do {
				txt = PixelScene.renderTextBlock( label, size );
				size--;
			} while (txt.width() >= WIDTH * 0.55f);
			txt.setPos(0, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );

			size = 8;
			do {
				txt = PixelScene.renderTextBlock( value, size );
				size--;
			} while (txt.width() >= WIDTH * 0.45f);
			txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );

			pos += GAP + txt.height();
		}

		private void statSlot( String label, int value ) {
			statSlot( label, Integer.toString( value ) );
		}

		public float height() {
			return pos;
		}
	}

	public class TalentsTab extends Component {

		TalentsPane pane;

		@Override
		protected void createChildren() {
			super.createChildren();
			pane = new TalentsPane(TalentButton.Mode.UPGRADE);
			add(pane);
		}

		@Override
		protected void layout() {
			super.layout();
			pane.setRect(x, y, width, height);
		}

	}

	private class BuffsTab extends Component {

		private static final int GAP = 2;

		private float pos;
		private ScrollPane buffList;
		private ArrayList<BuffSlot> slots = new ArrayList<>();

		@Override
		protected void createChildren() {

			super.createChildren();

			buffList = new ScrollPane( new Component() ){
				@Override
				public void onClick( float x, float y ) {
					int size = slots.size();
					for (int i=0; i < size; i++) {
						if (slots.get( i ).onClick( x, y )) {
							break;
						}
					}
				}
			};
			add(buffList);
		}

		@Override
		protected void layout() {
			super.layout();
			buffList.setRect(0, 0, width, height);
		}

		private void setupList() {
			Component content = buffList.content();
			for (Buff buff : Dungeon.hero.buffs()) {
				if (buff.icon() != BuffIndicator.NONE) {
					BuffSlot slot = new BuffSlot(buff);
					slot.setRect(0, pos, WIDTH, slot.icon.height());
					content.add(slot);
					slots.add(slot);
					pos += GAP + slot.height();
				}
			}
			content.setSize(buffList.width(), pos);
			buffList.setSize(buffList.width(), buffList.height());
		}

		private class BuffSlot extends Component {

			private Buff buff;

			Image icon;
			RenderedTextBlock txt;

			public BuffSlot( Buff buff ){
				super();
				this.buff = buff;

				icon = new BuffIcon(buff, true);
				icon.y = this.y;
				add( icon );

				txt = PixelScene.renderTextBlock( Messages.titleCase(buff.name()), 8 );
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
				add( txt );

			}

			@Override
			protected void layout() {
				super.layout();
				icon.y = this.y;
				txt.maxWidth((int)(width - icon.width()));
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
			}

			protected boolean onClick ( float x, float y ) {
				if (inside( x, y )) {
					GameScene.show(new WndInfoBuff(buff));
					return true;
				} else {
					return false;
				}
			}
		}
	}

	// ========== 词条Tab ==========

	private class TraitsTab extends Component {

		private static final int SLOT_HEIGHT = 18;

		private RenderedTextBlock pointsText;
		private int quantity = 1; // 默认单次调整数量
		private ScrollPane traitList;
		private ArrayList<TraitSlot> slots = new ArrayList<>();
		private com.watabou.noosa.PointerArea globalClick; // 词条列表全局点击区(重建前需destroy,避免监听者泄漏)
		private int traitPoints = 0;

		@Override
		protected void createChildren() {
			super.createChildren();

			// 可分配点数文本
			pointsText = PixelScene.renderTextBlock("可分配点数: " + traitPoints, 8);
			add(pointsText);

			// 数量选择器：[×1] [×5] [×10]
			StyledButton btnQ1 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "×1", 6) {
				@Override
				protected void onClick() {
					super.onClick();
					quantity = 1;
					refreshQuantityButtons();
				}
			};
			btnQ1.setRect(0, 14, 24, 14);
			add(btnQ1);

			StyledButton btnQ5 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "×5", 6) {
				@Override
				protected void onClick() {
					super.onClick();
					quantity = 5;
					refreshQuantityButtons();
				}
			};
			btnQ5.setRect(26, 14, 24, 14);
			add(btnQ5);

			StyledButton btnQ10 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "×10", 6) {
				@Override
				protected void onClick() {
					super.onClick();
					quantity = 10;
					refreshQuantityButtons();
				}
			};
			btnQ10.setRect(52, 14, 24, 14);
			add(btnQ10);

			// 词条列表（滚动）
			traitList = new ScrollPane(new Component());
			add(traitList);
		}

		@Override
		protected void layout() {
			super.layout();
			pointsText.setPos(0, 0);
			traitList.setRect(0, 30, width, height - 30);
		}

		private void refreshQuantityButtons() {
			// 切换选中样式（通过文本颜色模拟）
			setupList();
		}

		private void setupList() {
			Component content = traitList.content();
			// 双重安全保护：content 和其父级必须有效
			if (!isActive() || content == null || content.parent == null) return;
			// 显式销毁旧全局点击区(Group.clear() 不调用 destroy(),否则 PointerEvent 静态监听列表泄漏)
			if (globalClick != null) {
				globalClick.destroy();
				globalClick = null;
			}
			content.clear();
			slots.clear();

			float pos = 0;
			ArrayList<Trait> collected = Dungeon.traits.getCollectedTraits();
			for (Trait t : collected) {
				if (t == null) continue;
				TraitSlot slot = new TraitSlot(t);
				slot.setRect(0, pos, WIDTH, SLOT_HEIGHT);
				content.add(slot);
				slots.add(slot);
				pos += SLOT_HEIGHT;
			}
			content.setSize(traitList.width(), pos);
			traitList.setSize(traitList.width(), traitList.height());

			// 添加全局点击处理器（content.clear() 已移除旧的）
			// 命中区域只覆盖名称/图标区(x=16..WIDTH-16),避开左右 [+]/[-] 按钮
			final float totalHeight = pos;
			globalClick = new com.watabou.noosa.PointerArea(16, 0, WIDTH - 32, totalHeight) {
				@Override
				protected void onPointerUp(com.watabou.input.PointerEvent event) {
					// 拖动距离超过阈值视为滚动操作,取消点击,避免滚动结束误弹详情
					if (com.watabou.utils.PointF.distance(event.current, event.start) > PixelScene.defaultZoom * 8) {
						curEvent = null;
					}
				}
				@Override
				public void onClick(com.watabou.input.PointerEvent event) {
					if (!isActive()) return;
					// 将屏幕坐标转换为词条列表内容坐标（抵消窗口偏移、缩放与滚动偏移）
					com.watabou.utils.PointF p = camera().screenToCamera((int) event.current.x, (int) event.current.y);
					int slotIdx = (int)(p.y / SLOT_HEIGHT);
					if (slotIdx >= 0 && slotIdx < slots.size()) {
						Trait clicked = slots.get(slotIdx).t;
						if (clicked != null) {
							com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new WndInfoTrait(clicked));
						}
					}
				}
			};
			globalClick.blockLevel = com.watabou.noosa.PointerArea.NEVER_BLOCK;
			content.add(globalClick);

			// 刷新点数显示
			traitPoints = calculatePoints();
			pointsText.text("可分配点数: " + traitPoints);
		}

		private int calculatePoints() {
			// 从已收集词条中计算可分配点数（每层1点基础 + 广告奖励额外点数）
			int base = Dungeon.depth;
			int spent = 0;
			for (Trait t : Dungeon.traits.getCollectedTraits()) {
				if (t == null) continue;
				int lvl = t.getLevel();
				if (lvl > 0) spent += lvl;
			}
			int bonus = Dungeon.traits.getBonusPoints();
			return Math.max(0, base + bonus - spent);
		}

		private void doUpgrade(Trait t, int qty) {
			int available = calculatePoints();
			int canAfford = Math.min(qty, available);
			if (canAfford <= 0) return;

			int upgraded = 0;
			for (int i = 0; i < canAfford; i++) {
				if (t.upgrade()) {
					upgraded++;
				} else {
					break;
				}
			}
			if (upgraded > 0) {
				GLog.p("词条 " + t.getName() + " 已升级 " + upgraded + " 级，当前 Lv." + t.getLevel());
			}
			setupList();
		}

		private void doDowngrade(Trait t, int qty) {
			int downgraded = 0;
			for (int i = 0; i < qty; i++) {
				// 降级前检查：已经是最低等级则停止
				if (t.getLevel() <= t.getMinLevel()) break;
				// 如果是正面词条从正等级降到0或以下，应返还点数
				if (t.isPositive() && t.getLevel() > 0) {
					// 返还点数将在calculatePoints中自动计算
				}
				if (t.downgrade()) {
					downgraded++;
				} else {
					break;
				}
			}
			if (downgraded > 0) {
				GLog.p("词条 " + t.getName() + " 已降级 " + downgraded + " 级，当前 Lv." + t.getLevel());
			}
			setupList();
		}

		// ===== 词条槽位 =====

		private class TraitSlot extends Component {

			private Trait t;
			private StyledButton btnMinus;
			private StyledButton btnPlus;
			private Image colorIcon;
			private RenderedTextBlock nameText;

			public TraitSlot(Trait t) {
				super();
				this.t = t;
			}

			@Override
			protected void createChildren() {
				super.createChildren();

				// [-] 按钮
				btnMinus = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "-", 6) {
					@Override
					protected void onClick() {
						super.onClick();
						doDowngrade(t, quantity);
					}
				};
				add(btnMinus);

				// [+] 按钮
				btnPlus = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "+", 6) {
					@Override
					protected void onClick() {
						super.onClick();
						doUpgrade(t, quantity);
					}
				};
				add(btnPlus);

				// 颜色图标（占位，layout中刷新）
				colorIcon = new Image(TextureCache.createSolid(0x888888));
				colorIcon.width = 10;
				colorIcon.height = 10;
				add(colorIcon);

				// 名称（占位文字，layout中刷新）
				nameText = PixelScene.renderTextBlock("", 7);
				nameText.maxWidth(90);
				nameText.hardlight(Window.TITLE_COLOR);
				add(nameText);
			}

			@Override
			protected void layout() {
				super.layout();
				if (t == null) return;

				// 刷新颜色图标和名称
				boolean isDegraded = t.isPositive() && t.getLevel() <= 0;
				boolean isEffectivelyPositive = t.isPositive() && !isDegraded;
				colorIcon.hardlight(isEffectivelyPositive ? 0x00FF00 : 0xFF0000);
				String levelStr;
				if (isDegraded) {
					levelStr = String.valueOf(t.getLevel());
				} else {
					levelStr = t.getLevel() > 0 ? "+" + t.getLevel() : String.valueOf(t.getLevel());
				}
				nameText.text(t.getName() + " Lv." + levelStr);

				// [-] 按钮 14px 宽，左对齐
				btnMinus.setRect(x, y, 14, height);
				// 颜色图标
				colorIcon.x = x + 16;
				colorIcon.y = y + (height - 10) / 2;
				PixelScene.align(colorIcon);
				// 文本
				nameText.setPos(
						colorIcon.x + 12,
						y + (height - nameText.height()) / 2
				);
				PixelScene.align(nameText);
				// [+] 按钮 14px 宽，右对齐
				btnPlus.setRect(x + width - 14, y, 14, height);
			}
		}
	}

	// ========== 装备Tab ==========

	private class AdRewardTab extends Component {

		private RenderedTextBlock infoText;
		private RenderedTextBlock statusText;
		private RenderedTextBlock resultText;
		private RenderedTextBlock oaidText;
		private StyledButton btnWatchAd;

		private int dailyCount = 0;
		private boolean adLoading = false;
		private boolean adReady = false;

		// 从持久化存储读取今日广告次数（跨日期自动重置）
		private void loadDailyCount() {
			int today = (int)(System.currentTimeMillis() / 86400000L);
			if (com.shatteredpixel.shatteredpixeldungeon.SPDSettings.adDay() != today) {
				com.shatteredpixel.shatteredpixeldungeon.SPDSettings.adDay(today);
				com.shatteredpixel.shatteredpixeldungeon.SPDSettings.adCount(0);
				dailyCount = 0;
			} else {
				dailyCount = com.shatteredpixel.shatteredpixeldungeon.SPDSettings.adCount();
			}
		}

		private void saveDailyCount() {
			com.shatteredpixel.shatteredpixeldungeon.SPDSettings.adCount(dailyCount);
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			loadDailyCount();

			// 信息文本
			infoText = PixelScene.renderTextBlock("观看激励视频可获得以下奖励之一：\n" +
					"• 随机神器（太一圣杯 / 洞渊神角 / 盗圣之证）", 7);
			infoText.maxWidth(WIDTH - 10);
			add(infoText);

			// 状态文本
			statusText = PixelScene.renderTextBlock("准备就绪", 7);
			statusText.maxWidth(WIDTH - 10);
			add(statusText);

			// 结果文本
			resultText = PixelScene.renderTextBlock("", 7);
			resultText.maxWidth(WIDTH - 10);
			add(resultText);



			// 观看广告按钮
			btnWatchAd = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "观看广告获取奖励", 7) {
				@Override
				protected void onClick() {
					super.onClick();
					watchAd();
				}
			};
			add(btnWatchAd);
		}

		@Override
		protected void layout() {
			super.layout();
			infoText.setPos(5, 5);
			statusText.setPos(5, infoText.bottom() + 4);
			resultText.setPos(5, statusText.bottom() + 4);
			// 广告按钮固定在底部
			btnWatchAd.setRect(
					(width - 100) / 2,
					this.height - 30,
					100,
					22
			);

			updateDisplay();
		}

		private void setupContent() {
			updateDisplay();
		}

		private void updateDisplay() {
			if (adLoading) {
				statusText.text("正在加载广告...");
				btnWatchAd.enable(false);
				btnWatchAd.text("加载中...");
			} else if (adReady) {
				statusText.text("广告已就绪，点击观看");
				btnWatchAd.enable(true);
				btnWatchAd.text("观看广告获取奖励");
			} else if (dailyCount >= 11) {
				statusText.text("今日次数已用完（11/11）");
				btnWatchAd.enable(false);
				btnWatchAd.text("已用完");
			} else {
				statusText.text("准备就绪（今日 " + dailyCount + "/11）");
				btnWatchAd.enable(true);
				btnWatchAd.text("观看广告获取奖励");
			}
		}

		private void watchAd() {
			if (dailyCount >= 11) {
				resultText.text("今日次数已用完！");
				return;
			}
			if (AdManager.isAdShowing()) {
				resultText.text("广告正在播放中...");
				return;
			}

			adLoading = true;
			updateDisplay();

			// 加载并展示广告
			AdManager.loadRewardAd();
			AdManager.showRewardAd(new AdManager.RewardAdCallback() {
				@Override
				public void onReward() {
					// 广告播放完成，发放奖励
					dailyCount++;
					saveDailyCount();
					adLoading = false;
					adReady = false;
					onAdReward();
					updateDisplay();
					// 自动加载下一个广告（如果次数未用完）
					if (dailyCount < 11) {
						AdManager.loadRewardAd();
					}
				}

				@Override
				public void onAdFailed(String errorMsg) {
					adLoading = false;
					adReady = false;
					resultText.text("广告加载失败: " + errorMsg);
					updateDisplay();
				}
			});

			if (dailyCount < 11) {
				adReady = true;
			}
			updateDisplay();
		}

		private void onAdReward() {
			// 随机选择奖励
			String rewardMsg;
			// 收集玩家已有神器(防止重复)
			java.util.ArrayList<String> ownedArtifacts = new java.util.ArrayList<>();
			for (com.shatteredpixel.shatteredpixeldungeon.items.Item item : Dungeon.hero.belongings.backpack.items) {
				ownedArtifacts.add(item.getClass().getName());
			}
			// 检查已装备的主神器槽
			if (Dungeon.hero.belongings.artifact != null)
				ownedArtifacts.add(Dungeon.hero.belongings.artifact.getClass().getName());
			// 检查所有 misc 装备槽（配件/戒指等也可能有神器）
			for (com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc m : Dungeon.hero.belongings.misc) {
				if (m != null) ownedArtifacts.add(m.getClass().getName());
			}

			// 构建可用的神器奖励列表
			java.util.ArrayList<String> availableArtifacts = new java.util.ArrayList<>();
			if (!ownedArtifacts.contains(TaiyiHolyGrail.class.getName())) availableArtifacts.add("TaiyiHolyGrail");
			if (!ownedArtifacts.contains(AbyssHorn.class.getName())) availableArtifacts.add("AbyssHorn");
			if (!ownedArtifacts.contains(ThiefSeal.class.getName())) availableArtifacts.add("ThiefSeal");

			int totalOptions = availableArtifacts.size() + 1; // +1 for trait points
			int roll = Random.Int(totalOptions);
			if (roll < availableArtifacts.size()) {
				// 给予未持有的神器
				String chosen = availableArtifacts.get(roll);
				com.shatteredpixel.shatteredpixeldungeon.items.Item artifactItem;
				String name;
				switch (chosen) {
					case "TaiyiHolyGrail": artifactItem = new TaiyiHolyGrail(); name = "TaiyiHolyGrail"; break;
					case "AbyssHorn": artifactItem = new AbyssHorn(); name = "AbyssHorn"; break;
					default: artifactItem = new ThiefSeal(); name = "ThiefSeal"; break;
				}
				if (artifactItem.collect(Dungeon.hero.belongings.backpack)) {
					rewardMsg = "获得 " + name + "！";
				} else {
					Dungeon.level.drop(artifactItem, Dungeon.hero.pos);
					rewardMsg = "背包已满，" + name + " 掉落在脚下！";
				}
			} else {
				// 三个神器都已获得 → 获得可分配词条点数
				int pts = 3 + Dungeon.depth / 5;
				Dungeon.traits.addBonusPoints(pts);
				rewardMsg = "三个神器已集齐！获得 " + pts + " 点可分配词条点数！";
			}
			resultText.text(rewardMsg);
			GLog.p(rewardMsg);
		}
	}
}
