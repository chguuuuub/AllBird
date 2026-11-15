# AllBird 鸟类全模态指南

全球鸟类指南 Android 项目。首版提供中英文界面规划和本地示例鸟种资料，并预留图像、鸟鸣识别模块入口。

## 功能状态

- Android 原生 Kotlin 项目
- 中英文界面
- 本地 25 个示例条目（含 1 个科级示例）与中英文名称搜索
- Wikimedia Commons 在线鸟类照片查询与缩略图展示（需联网）
- GitHub Actions Debug APK 自动构建
- 全球完整鸟种数据库：待接入可再分发数据源（当前 25 个示例条目不等于全球名录）
- 离线图像识别：待集成并验证 TFLite 模型
- 鸟类照片：在线从 Wikimedia Commons 检索，网络不可用或无匹配图片时显示图标占位
- 在线图像识别：待配置识别服务/API
- 鸟鸣音频识别：待集成音频模型与录音流程

> 尚未集成识别模型，因此当前版本不宣称具备实际 AI 图像或鸟鸣识别能力。

## Build

Requirements: JDK 17, Gradle 8.13, Android SDK 36.

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

Push to `main` or manually run **Android APK** under Actions. Download the `AllBird-debug-apk` artifact after a successful run.
