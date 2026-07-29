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

package com.shatteredpixel.shatteredpixeldungeon.ads;

/**
 * 广告管理器接口（纯Java，无Android依赖）
 * 各平台实现通过 AdManagerImpl 注入
 */
public class AdManager {

    private static AdManagerImpl impl = new MockAdManagerImpl();

    /**
     * 设置平台特定的广告实现
     */
    public static void setImpl(AdManagerImpl implementation) {
        impl = implementation;
    }

    /**
     * 初始化广告SDK
     */
    public static void init() {
        impl.init();
    }

    /**
     * 加载激励视频广告
     */
    public static void loadRewardAd() {
        impl.loadRewardAd();
    }

    /**
     * 展示激励视频广告
     */
    public static void showRewardAd(RewardAdCallback callback) {
        impl.showRewardAd(callback);
    }

    public static boolean isAdLoaded() { return impl.isAdLoaded(); }
    public static boolean isAdShowing() { return impl.isAdShowing(); }
    public static boolean isInitialized() { return impl.isInitialized(); }

    /**
     * 获取设备OAID（广告标识符）
     * Android平台由AndroidAdManager提供，桌面/iOS返回空字符串
     */
    public static String getOaid() {
        return impl != null ? impl.getOaid() : "";
    }

    /**
     * 广告管理器接口
     */
    public interface AdManagerImpl {
        void init();
        void loadRewardAd();
        void showRewardAd(RewardAdCallback callback);
        boolean isAdLoaded();
        boolean isAdShowing();
        boolean isInitialized();
        String getOaid();
    }

    /**
     * 激励广告回调接口
     */
    public interface RewardAdCallback {
        void onReward();
        void onAdFailed(String errorMsg);
    }

    /**
     * 模拟实现（无真实SDK时使用）
     */
    private static class MockAdManagerImpl implements AdManagerImpl {
        private boolean initialized = false;
        private boolean adLoaded = false;
        private boolean adShowing = false;

        @Override
        public void init() {
            if (initialized) return;
            initialized = true;
            System.out.println("[AdManager Mock] SDK initialized");
        }

        @Override
        public void loadRewardAd() {
            if (!initialized) { init(); }
            adLoaded = true;
            System.out.println("[AdManager Mock] Ad loaded");
        }

        @Override
        public void showRewardAd(RewardAdCallback callback) {
            if (!adLoaded) loadRewardAd();
            adShowing = true;
            System.out.println("[AdManager Mock] Showing ad...");
            try { Thread.sleep(500); } catch (InterruptedException e) { }
            adShowing = false;
            if (callback != null) callback.onReward();
        }

        @Override
        public boolean isAdLoaded() { return adLoaded; }
        @Override
        public boolean isAdShowing() { return adShowing; }
        @Override
        public boolean isInitialized() { return initialized; }
        @Override
        public String getOaid() { return "mock-oaid"; }
    }
}
