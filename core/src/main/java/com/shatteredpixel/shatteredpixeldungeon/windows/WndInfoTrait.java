/* Pixel Dungeon
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

import com.shatteredpixel.shatteredpixeldungeon.items.Trait;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ColorBlock;
import com.watabou.gltextures.TextureCache;

public class WndInfoTrait extends Window {

    private static final float GAP = 2;
    private static final int WIDTH = 120;

    public WndInfoTrait(Trait trait) {
        super();

        IconTitle titlebar = new IconTitle();

        // 判断是否降级（正面词条但等级≤0 → 实际负效果）
        boolean isDegraded = trait.isPositive() && trait.getLevel() <= 0;
        boolean isEffectivelyPositive = trait.isPositive() && !isDegraded;
        int color = isEffectivelyPositive ? 0xFF44CC44 : 0xFFCC4444;

        // 颜色方块表示正负面
        Image icon = new Image(TextureCache.createSolid(color));
        icon.hardlight(color);
        icon.width = 16;
        icon.height = 16;

        String levelStr;
        if (isDegraded) {
            levelStr = String.valueOf(trait.getLevel());
        } else {
            levelStr = trait.getLevel() > 0 ? "+" + trait.getLevel() : String.valueOf(trait.getLevel());
        }
        String title = trait.getName() + " Lv." + levelStr;

        titlebar.icon(icon);
        titlebar.label(title, isEffectivelyPositive ? Window.TITLE_COLOR : 0xFFCC4444);
        titlebar.setRect(0, 0, WIDTH, 0);
        add(titlebar);

        // 效果描述
        RenderedTextBlock txtInfo = PixelScene.renderTextBlock(trait.getDesc(), 6);
        txtInfo.maxWidth(WIDTH);
        txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2 * GAP);
        add(txtInfo);

        // 等级信息（降级词条显示特殊标注）
        RenderedTextBlock txtLevel;
        if (isDegraded) {
            txtLevel = PixelScene.renderTextBlock(
                    "降级词条 · 等级 " + trait.getLevel() + "（效果反转）", 6);
        } else if (trait.isPositive()) {
            txtLevel = PixelScene.renderTextBlock(
                    "正面词条 · 等级 " + trait.getLevel(), 6);
        } else {
            txtLevel = PixelScene.renderTextBlock(
                    "负面词条 · 等级 " + Math.abs(trait.getLevel()), 6);
        }
        txtLevel.maxWidth(WIDTH);
        txtLevel.setPos(titlebar.left(), txtInfo.bottom() + GAP);
        txtLevel.hardlight(color);
        add(txtLevel);

        resize(WIDTH, (int) txtLevel.bottom() + 2);
    }
}
