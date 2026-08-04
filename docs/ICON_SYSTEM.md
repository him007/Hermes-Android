# Hermes Light 图标系统

## 设计原则

- 统一使用 24 × 24 设计网格，常规界面以 18–24 dp 显示。
- 使用轻量圆角几何、适度留白与少量面线结合，避免上一版图标过厚、过满。
- 一个图标通常使用一至两个语义色；白色只承担信息镂空，不添加无功能圆点或角标。
- 造型优先采用圆角矩形、圆弧与简化几何面，控制各图标的光学占比和重心。
- 图标造型在清爽办公和圆润卡片两套皮肤中保持一致，皮肤只改变容器形状与层次。
- 选中状态通过浅色底板、文字颜色和字重表达，不把整枚图标改成单色。
- 返回、关闭、发送、展开等平台操作同样使用 Hermes Light 资源，避免页面风格断裂。
- 删除无功能含义的独立圆点、随机色块和装饰角标；通知铃舌、图片太阳等语义细节不受此限制。

## 语义色

| 颜色 | 浅色模式 | 主要语义 |
| --- | --- | --- |
| 蓝 | `#3370FF` | 对话、文件、主要操作 |
| 青 | `#20C7C9` | 轻量辅助结构、同步 |
| 绿 | `#24C48E` | 空间、连接、完成状态 |
| 黄 | `#FFB323` | 置顶、项目、提示 |
| 红 | `#F45B69` | 删除、错误、停止 |
| 紫 | `#8B5CF6` | AI、模型、外观 |
| 中性 | `#596780` | 返回、关闭、列表与低优先级操作 |

深色模式使用单独的高明度色值，定义在 `values-night/icon_colors.xml`。

## 工程结构

- 矢量资源：`app/src/main/res/drawable/hermes_light_*.xml`
- 品牌标志：`app/src/main/res/drawable/hermes_logo_mark.xml`
- 浅色语义色：`app/src/main/res/values/icon_colors.xml`
- 深色语义色：`app/src/main/res/values-night/icon_colors.xml`
- Compose 统一入口：`ui/component/HermesIcons.kt`
- 生成脚本：`tools/generate_android_light_icons.py`

业务页面只能通过 `HermesIconKind` 和 `HermesMulticolorIcon` 调用语义图标，不在页面中重新绘制同类图标。

完整设计源位于交付包 `Hermes-light-icon-system-v1`；Android 工程内只保留编译后的 VectorDrawable 与可重复生成脚本。
