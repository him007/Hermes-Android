# Hermes v0.6.4 图标系统说明

本版本将应用内图标统一为用户确认的 **Hermes Light** 风格，并将独立设计的 Hermes 女生形象接入 Android 自适应启动图标。

## 图标规则

- 79 枚功能图标统一使用 24 × 24 网格、轻量圆角几何和克制的双色结构。
- Move 使用 A、Attachment 使用 C、Microphone 使用 A、Hide 使用 A、Bold 使用 B。
- Pinned 使用全黄色；Rename 只保留铅笔；Refresh 与 Sync 使用上蓝下绿；Hide 使用单一蓝色主体。
- 删除无功能圆点、装饰下划线和不自然的交叉拼色。
- 中性线条通过 `values-night` 自动反色，深色模式不再使用浅色模式的深灰线条。

## 接入范围

- 四枚底栏图标及选中、未选中状态。
- 会话列表、会话操作、项目、附件、聊天输入、助理面板与全部弹层。
- 空间文件、Markdown 预览与编辑工具栏。
- 定时任务、运行状态、刷新、新建、播放、暂停、编辑与删除。
- 设置、模型、连接、外观、返回、展开、选择、警告与状态图标。
- 应用启动图标和 Android 自适应蒙版。

## 工程位置

- VectorDrawable：`app/src/main/res/drawable/hermes_light_*.xml`
- Compose 入口：`app/src/main/java/com/qingyu/hermescompanion/ui/component/HermesIcons.kt`
- 日/夜色板：`app/src/main/res/values*/icon_colors.xml`
- 生成脚本：`tools/generate_android_light_icons.py`
- 启动图：`app/src/main/res/drawable-nodpi/hermes_icon_art.webp`

## 验证结果

- 79 枚 VectorDrawable 全部通过 Android 资源编译。
- 工程内 Material 默认图标调用为 0。
- 13 项单元测试全部通过。
- `0.6.4-debug` APK 完整构建、v2 签名与 16 KB 对齐检查通过。
