# Lexicographic-Abyss

**Lexicographic-Abyss** 是基于 [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) 的二次创作修改版，由 **许玄** 制作。

一个融合了玄幻修仙诡异风格的闯关解密游戏，内置 10,000 道秘境机关/阵法谜题，38 种题型。

---

## 环境要求

| 工具 | 版本要求 |
|------|----------|
| Java | JDK 17+ |
| Android SDK | API 35+ (compileSdk 36) |
| Gradle | 8.12+ (推荐使用 wrapper) |
| Git | 任意版本 |

## 快速打包

### Android APK

```bash
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
cd Lexicographic-Abyss
./gradlew assembleDebug
```

APK 生成路径：
```
android/build/outputs/apk/debug/android-debug.apk
```

### 桌面版

```bash
./gradlew desktop:dist
```

JAR 生成路径：
```
desktop/build/libs/desktop-1.0.jar
```

### iOS (需 macOS + Xcode + RoboVM)

```bash
./gradlew ios:createIPA
```

---

## 项目结构

```
Lexicographic-Abyss/
├── android/          # Android 平台
├── core/             # 核心游戏代码
│   └── src/main/
│       ├── assets/
│       │   └── data/
│       │       └── question_bank.json   # 10,000 道预生成题库
│       └── java/.../
│           ├── items/quiz/              # 问答系统
│           │   ├── StaticQuestionBank   # 静态题库加载器
│           │   ├── QuizSystem           # 问答触发/奖励
│           │   ├── QuestionBank         # 兜底生成器
│           │   └── XianxiaThemer        # 玄幻词库
│           ├── windows/WndQuiz.java     # 问答UI
│           ├── items/Trait*.java        # 词条系统
│           ├── items/artifacts/         # 神器(含新增3件)
│           └── ads/AdManager.java       # 模拟广告
├── desktop/          # 桌面平台
├── ios/              # iOS 平台
├── services/         # 更新/新闻服务(已禁用)
├── SPD-classes/      # 引擎库
├── build.gradle      # 构建配置
└── LICENSE.txt       # GPLv3 许可证
```

---

## 功能特性

- **10,000 道秘境闯关谜题** — 38 种题型，场景化包装
- **词条系统** — 移动/战斗触发各种词条效果
- **11 装备槽** — 武器、护甲、神器、4 饰品、3 戒指、副武器
- **新增神器** — 太乙圣杯(TaiyiHolyGrail)、深渊号角(AbyssHorn)、盗贼封印(ThiefSeal)
- **模拟广告** — MockAdManagerImpl，无需真实SDK
- **问答预加载** — 游戏启动时异步加载题库，首次触发无卡顿
- **答案随机分布** — 正确答案均匀分布在 A/B/C/D

---

## GPLv3 合规声明

本程序是免费软件：您可以重新分发和/或修改它，遵循由自由软件基金会发布的 GNU 通用公共许可证（GPL）第三版，或（按您的选择）任何后续版本。

详细信息请参阅 [LICENSE.txt](LICENSE.txt)。

**原作者：** [00-Evan](https://github.com/00-Evan) (Evan Debenham) — [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon)

**二次创作：** [许玄 (qxjysd)](https://github.com/qxjysd) — [Lexicographic-Abyss](https://github.com/qxjysd/Lexicographic-Abyss-TY)
