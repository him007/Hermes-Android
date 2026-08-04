# Hermes v0.6.3 图标系统说明

本版本将应用内图标统一为 **Solid Geo** 风格，以用户选定的底栏「对话」C 方案为母版。

## 设计规则

- 所有语义图标使用 24 × 24 网格，统一视觉占比和圆润转角。
- 以清晰的实心几何轮廓为主，优先保证 18–24 dp 小尺寸识别力。
- 每个图标最多使用一个主色和一个有语义的辅助色。
- 白色仅用于信息镂空；删除独立圆点、随机色块、装饰角标等无功能元素。
- 深色模式使用 `values-night/icon_colors.xml` 中的高亮色，避免图标与背景混在一起。
- 返回、关闭、刷新、编辑、删除等基础操作统一使用 Material Rounded 轮廓和主题色，不额外增加复杂装饰。

## 已更新范围

- 底栏：对话、空间、任务、我的。
- 对话：AI、模型、新对话、搜索、附件、麦克风、项目、最近、待办、产物。
- 空间与文件：空间、文件夹、文件、图片、链接、重命名、移动、归档、删除、置顶、验证。
- 设置与状态：外观、通知、连接、信息、存储。
- 品牌入口：应用启动图标、应用内 Hermes 标志、系统通知小图标。
- 形状代替图标：语音语言弹窗中的文字勾选符已替换为正式圆角矢量图标。

## 资源位置

- 语义图标：`app/src/main/res/drawable/hermes_geo_*.xml`
- 品牌标志：`app/src/main/res/drawable/hermes_logo_mark.xml`
- 应用内品牌底板：`app/src/main/res/drawable/hermes_logo_tile.xml`
- 亮色图标色板：`app/src/main/res/values/icon_colors.xml`
- 深色图标色板：`app/src/main/res/values-night/icon_colors.xml`

## 验证结果

- Android 资源编译通过。
- Debug APK 完整构建通过。
- 13 项单元测试全部通过。
- APK 版本：`0.6.3-debug`，versionCode `24`。
