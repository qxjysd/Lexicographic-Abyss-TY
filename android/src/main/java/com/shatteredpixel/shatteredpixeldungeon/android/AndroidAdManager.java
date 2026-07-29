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

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.shatteredpixel.shatteredpixeldungeon.ads.AdManager;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdConfig;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapAdSdk;
import com.tapsdk.tapad.TapRewardVideoAd;

/**
 * Dirichlet Ad SDK Android 实现
 *
 * 参考官方Demo: https://ssp.dirichlet.cn/docs/resource-download/
 *   dirichlet_ad_demo.zip → reward/RewardHostActivity.java
 *
 * 接入参数:
 *   mediaId:   1105040
 *   mediaKey:  KpqvL72TWdDpaSSyhfFzVnZIpgy4bKbnXN0LtT1HveqlGwDlV5SMe0f1OZ8zAIcH
 *   mediaName: 破碎的像素地牢加强版
 *   spaceId:   1060113 (激励视频)
 *
 * 当前为模拟模式（SDK_DISABLED=true），如需启用真实SDK：
 *   1. SDK_DISABLED → false
 *   2. 确保 src/main/libs/dirichlet_ad_4.2.8.0.aar 存在
 *   3. build.gradle 已配 implementation files(...)
 */
public class AndroidAdManager implements AdManager.AdManagerImpl {

    private static final long MEDIA_ID = 1105040L;
    private static final String MEDIA_KEY = "KpqvL72TWdDpaSSyhfFzVnZIpgy4bKbnXN0LtT1HveqlGwDlV5SMe0f1OZ8zAIcH";
    private static final String MEDIA_NAME = "破碎的像素地牢加强版";
    private static final long SPACE_ID_REWARD = 1060113L;

    // ==== 模拟模式开关 ====
    private static final boolean SDK_DISABLED = true;

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean initialized = false;
    private boolean adLoading = false;
    private boolean adLoaded = false;
    private boolean adShowing = false;

    // 参考官方Demo：使用单例 TapAdNative
    private TapAdNative tapAdNative;
    private TapRewardVideoAd rewardVideoAd;
    private AdManager.RewardAdCallback currentCallback;

    public AndroidAdManager(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
        if (initialized) return;
        initialized = true;
        System.out.println("[AdManager] SDK_DISABLED=" + SDK_DISABLED);

        if (SDK_DISABLED) return;

        // 在主线程初始化
        mainHandler.post(() -> {
            try {
                TapAdConfig config = new TapAdConfig.Builder()
                        .withMediaId(MEDIA_ID)
                        .withMediaKey(MEDIA_KEY)
                        .withMediaName(MEDIA_NAME)
                        .enableDebug(true)
                        .shakeEnabled(true)
                        .build();
                TapAdSdk.init(context, config);
                System.out.println("[AdManager] SDK初始化成功");

                // 预创建 TapAdNative（参考官方Demo）
                tapAdNative = TapAdManager.get().createAdNative((Activity) context);
            } catch (Throwable e) {
                System.err.println("[AdManager] SDK初始化失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void loadRewardAd() {
        if (SDK_DISABLED) return;
        if (!initialized) { init(); return; }
        if (adLoading) return;

        mainHandler.post(() -> {
            try {
                adLoading = true;
                adLoaded = false;

                // 参考官方Demo：每次加载前重置 TapAdNative
                if (tapAdNative != null) {
                    tapAdNative.dispose();
                }
                tapAdNative = TapAdManager.get().createAdNative((Activity) context);

                AdRequest adRequest = new AdRequest.Builder()
                        .withSpaceId(SPACE_ID_REWARD)
                        .withRewardName("reward")
                        .withRewardAmount(1)
                        .build();

                tapAdNative.loadRewardVideoAd(adRequest,
                        new TapAdNative.RewardVideoAdListener() {
                    @Override
                    public void onError(int code, String message) {
                        adLoading = false;
                        System.err.println("[AdManager] 加载失败: code=" + code + " msg=" + message);
                    }

                    @Override
                    public void onRewardVideoAdLoad(TapRewardVideoAd ad) {
                        System.out.println("[AdManager] 广告加载成功");
                    }

                    @Override
                    public void onRewardVideoCached(TapRewardVideoAd ad) {
                        adLoading = false;
                        adLoaded = true;
                        System.out.println("[AdManager] 广告缓存完成");

                        // 参考官方Demo：在onRewardVideoCached中展示广告
                        if (currentCallback != null && !adShowing) {
                            showWithAd(ad);
                        }
                    }
                });
            } catch (Throwable e) {
                adLoading = false;
                System.err.println("[AdManager] 加载异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void showRewardAd(AdManager.RewardAdCallback callback) {
        if (SDK_DISABLED) {
            // 模拟模式：直接发奖励
            if (callback != null) callback.onReward();
            return;
        }

        if (!initialized) { init(); }
        if (adShowing) {
            if (callback != null) callback.onAdFailed("广告正在展示");
            return;
        }

        currentCallback = callback;

        if (rewardVideoAd != null && rewardVideoAd.isValid()) {
            showWithAd(rewardVideoAd);
        } else {
            // 广告未就绪，先加载（加载完成后自动展示）
            loadRewardAd();
        }
    }

    // 参考官方Demo：展示广告，先注册监听再show
    private void showWithAd(TapRewardVideoAd ad) {
        if (adShowing || ad == null) return;

        try {
            adShowing = true;
            rewardVideoAd = ad;

            ad.setRewardAdInteractionListener(
                    new TapRewardVideoAd.RewardAdInteractionListener() {

                @Override
                public void onAdShow(TapRewardVideoAd ad) {
                    System.out.println("[AdManager] 广告已显示");
                }

                @Override
                public void onAdClose(TapRewardVideoAd ad) {
                    adShowing = false;
                    System.out.println("[AdManager] 广告已关闭");
                    // 参考官方Demo：关闭后释放
                    releaseAd();
                }

                @Override
                public void onVideoComplete(TapRewardVideoAd ad) {
                    System.out.println("[AdManager] 视频播放完成");
                }

                @Override
                public void onVideoError(TapRewardVideoAd ad) {
                    adShowing = false;
                    System.err.println("[AdManager] 视频播放出错");
                    if (currentCallback != null) {
                        currentCallback.onAdFailed("视频播放出错");
                        currentCallback = null;
                    }
                }

                @Override
                public void onRewardVerify(TapRewardVideoAd ad,
                        boolean verified, int amount,
                        String rewardName, int code, String msg) {
                    System.out.println("[AdManager] 激励验证: " + verified);
                    if (verified && currentCallback != null) {
                        currentCallback.onReward();
                        currentCallback = null;
                    }
                }

                @Override
                public void onSkippedVideo(TapRewardVideoAd ad) {
                    System.out.println("[AdManager] 用户跳过视频");
                }

                @Override
                public void onAdClick(TapRewardVideoAd ad) {
                    System.out.println("[AdManager] 广告被点击");
                }

                @Override
                public void onAdValidShow(TapRewardVideoAd ad) {
                    System.out.println("[AdManager] 广告有效曝光");
                }
            });

            ad.showRewardVideoAd((Activity) context);
        } catch (Throwable e) {
            adShowing = false;
            System.err.println("[AdManager] 展示异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 参考官方Demo：释放广告资源
    private void releaseAd() {
        if (rewardVideoAd != null) {
            rewardVideoAd.setRewardAdInteractionListener(null);
            rewardVideoAd.dispose();
            rewardVideoAd = null;
        }
    }

    @Override
    public boolean isAdLoaded() { return adLoaded; }

    @Override
    public boolean isAdShowing() { return adShowing; }

    @Override
    public boolean isInitialized() { return initialized; }

    @Override
    public String getOaid() {
        return "tapad-oaid";
    }
}
