package com.qingyu.hermescompanion.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qingyu.hermescompanion.ui.AppUiState
import com.qingyu.hermescompanion.ui.SkinMode
import com.qingyu.hermescompanion.ui.ThemeMode
import com.qingyu.hermescompanion.model.UserProfilePreferences
import com.qingyu.hermescompanion.storage.AvatarCropSpec
import com.qingyu.hermescompanion.storage.AvatarTarget
import com.qingyu.hermescompanion.ui.component.GlassPanel
import com.qingyu.hermescompanion.ui.component.HermesIconKind
import com.qingyu.hermescompanion.ui.component.HermesMulticolorIcon
import com.qingyu.hermescompanion.ui.component.HermesStatusIcon
import com.qingyu.hermescompanion.ui.component.HermesStatusKind
import com.qingyu.hermescompanion.ui.component.UserAvatar
import com.qingyu.hermescompanion.ui.theme.HermesSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: AppUiState,
    contentPadding: PaddingValues,
    onThemeChange: (ThemeMode) -> Unit,
    onSkinChange: (SkinMode) -> Unit,
    onConnectionSettings: () -> Unit,
    onNotificationSettings: () -> Unit,
    onVoiceSettings: () -> Unit,
    onSkillsTools: () -> Unit,
    onModelSettings: () -> Unit,
    onConversationStyle: () -> Unit,
    onApprovalSettings: () -> Unit,
    onMemoryContext: () -> Unit,
    onArchivedSessions: () -> Unit,
    onProfileSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    var showThemePicker by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(start = HermesSpacing.page, end = HermesSpacing.page, top = 6.dp, bottom = 10.dp),
        ) {
            ProfileHeroCard(state = state, onClick = onProfileSettings)
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = HermesSpacing.page),
        ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onConnectionSettings),
            shape = RoundedCornerShape(15.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconWell(HermesIconKind.CONNECTION)
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("远程网关", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        HermesStatusIcon(HermesStatusKind.CONNECTED)
                        Text("连接正常", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 5.dp))
                    }
                    Text(maskAddress(state.baseUrl), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }

        GlassPanel(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingRow(HermesIconKind.APPEARANCE, "外观与主题", "${skinModeLabel(state.skinMode)} · ${themeModeLabel(state.themeMode)}") { showThemePicker = true }
                SettingRow(HermesIconKind.NOTIFICATION, "通知设置", "消息弹窗、声音与桌面角标", onNotificationSettings)
                SettingRow(HermesIconKind.MICROPHONE, "语音输入", "语言与识别后发送方式", onVoiceSettings)
            }
        }
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(top = 9.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingRow(HermesIconKind.AI, "技能与工具", "技能、工具集与 MCP 服务", onSkillsTools)
                SettingRow(HermesIconKind.MODEL, "模型设置", "默认模型、辅助模型、MoA 与备用模型", onModelSettings)
                SettingRow(HermesIconKind.CHAT, "对话风格", "人格、时区与推理过程", onConversationStyle)
                SettingRow(HermesIconKind.VERIFIED, "审批模式", "危险操作审批方式与超时", onApprovalSettings)
                SettingRow(HermesIconKind.STORAGE, "记忆与上下文", "持久记忆、画像与自动压缩", onMemoryContext)
                SettingRow(HermesIconKind.ARCHIVE, "已归档对话", "查看、恢复或删除归档会话", onArchivedSessions)
            }
        }
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(top = 9.dp), shape = RoundedCornerShape(16.dp)) {
            SettingRow(HermesIconKind.INFORMATION, "关于 Hermes", "版本与能力说明", onAbout)
        }
        Spacer(Modifier.height(24.dp))
        }
    }

    if (showThemePicker) {
        ModalBottomSheet(
            onDismissRequest = { showThemePicker = false },
            shape = RoundedCornerShape(
                topStart = if (state.skinMode == SkinMode.GLASS) 24.dp else 16.dp,
                topEnd = if (state.skinMode == SkinMode.GLASS) 24.dp else 16.dp,
            ),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp)) {
                Text("外观与主题", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "切换后会立即应用到全部页面",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp, bottom = 12.dp),
                )
                Text("界面皮肤", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    SkinMode.entries.forEach { mode ->
                        SkinChoice(
                            mode = mode,
                            selected = state.skinMode == mode,
                            onClick = { onSkinChange(mode) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Text("颜色模式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp, bottom = 7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeChoice(
                            mode = mode,
                            selected = state.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun ProfileHeroCard(state: AppUiState, onClick: () -> Unit) {
    val displayName = state.userProfile.displayName.ifBlank { state.username.ifBlank { "Hermes 用户" } }
    GlassPanel(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 0.dp,
            ) {
                Box(modifier = Modifier.padding(3.dp)) {
                    UserAvatar(
                        uri = state.userProfile.avatarUri,
                        displayName = displayName,
                        size = 58.dp,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    state.userProfile.bio.ifBlank { "个人工作助理" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    "编辑头像与资料",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp,
            ) {
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    HermesMulticolorIcon(
                        HermesIconKind.CHEVRON_RIGHT,
                        contentDescription = "编辑个人资料",
                        iconSize = 15.dp,
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsScreen(
    state: AppUiState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onSave: (UserProfilePreferences) -> Unit,
    onUserAvatarSelected: (Uri, AvatarCropSpec) -> Unit,
    onHermesAvatarSelected: (Uri, AvatarCropSpec) -> Unit,
    onResetUserAvatar: () -> Unit,
    onResetHermesAvatar: () -> Unit,
) {
    var draftName by remember(state.userProfile.displayName, state.username) {
        mutableStateOf(state.userProfile.displayName.ifBlank { state.username })
    }
    var draftBio by remember(state.userProfile.bio) { mutableStateOf(state.userProfile.bio) }
    var draftHermesName by remember(state.userProfile.hermesDisplayName) {
        mutableStateOf(state.userProfile.hermesDisplayName.ifBlank { "Hermes" })
    }
    var pendingCrop by remember { mutableStateOf<PendingAvatarCrop?>(null) }
    val userAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { pendingCrop = PendingAvatarCrop(it, AvatarTarget.USER) }
    }
    val hermesAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { pendingCrop = PendingAvatarCrop(it, AvatarTarget.HERMES) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                HermesMulticolorIcon(HermesIconKind.BACK, contentDescription = "返回")
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text("个人资料", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("设置我与 Hermes 的聊天资料", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = HermesSpacing.page),
        ) {
        GlassPanel(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("我的聊天资料", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(state.userProfile.avatarUri, draftName.ifBlank { "我" }, 68.dp)
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(
                            if (state.userProfile.avatarUri.isBlank()) "内置用户头像" else "自定义用户头像",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "选择后会安全保存到本机应用目录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row {
                            TextButton(
                                onClick = { userAvatarPicker.launch(arrayOf("image/*")) },
                                enabled = !state.isAvatarUpdating,
                            ) { Text(if (state.userProfile.avatarUri.isBlank()) "选择图片" else "更换图片") }
                            if (state.userProfile.avatarUri.isNotBlank()) {
                                TextButton(onClick = onResetUserAvatar, enabled = !state.isAvatarUpdating) {
                                    Text("恢复默认")
                                }
                            }
                        }
                    }
                }
            }
        }

        GlassPanel(Modifier.fillMaxWidth().padding(top = 9.dp), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 4.dp)) {
                ProfileInputRow("昵称", draftName, "Hermes 用户") { draftName = it.take(24) }
                ProfileInputRow("个人签名", draftBio, "个人工作助理") { draftBio = it.take(50) }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("网关账号", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(state.username.ifBlank { "未登录" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        GlassPanel(Modifier.fillMaxWidth().padding(top = 9.dp), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Hermes 聊天资料", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        uri = state.userProfile.hermesAvatarUri,
                        displayName = draftHermesName.ifBlank { "Hermes" },
                        size = 68.dp,
                        hermesFallback = true,
                    )
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(
                            if (state.userProfile.hermesAvatarUri.isBlank()) "内置 Hermes 头像" else "自定义 Hermes 头像",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "会同步显示在会话列表和聊天界面",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row {
                            TextButton(
                                onClick = { hermesAvatarPicker.launch(arrayOf("image/*")) },
                                enabled = !state.isAvatarUpdating,
                            ) { Text(if (state.userProfile.hermesAvatarUri.isBlank()) "选择图片" else "更换图片") }
                            if (state.userProfile.hermesAvatarUri.isNotBlank()) {
                                TextButton(onClick = onResetHermesAvatar, enabled = !state.isAvatarUpdating) {
                                    Text("恢复默认")
                                }
                            }
                        }
                    }
                }
                ProfileInputRow("Hermes 昵称", draftHermesName, "Hermes") { draftHermesName = it.take(24) }
            }
        }
        Text(
            "头像会复制到应用私有目录，不依赖相册的临时授权；昵称和签名只用于这台手机，不会修改网关账号。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
        )
        Button(
            onClick = {
                onSave(
                    state.userProfile.copy(
                        displayName = draftName.trim(),
                        bio = draftBio.trim(),
                        hermesDisplayName = draftHermesName.trim().ifBlank { "Hermes" },
                    ),
                )
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("保存") }
        Spacer(Modifier.height(26.dp))
        }
    }

    pendingCrop?.let { request ->
        AvatarCropSheet(
            request = request,
            onDismiss = { pendingCrop = null },
            onConfirm = { uri, spec ->
                pendingCrop = null
                when (request.target) {
                    AvatarTarget.USER -> onUserAvatarSelected(uri, spec)
                    AvatarTarget.HERMES -> onHermesAvatarSelected(uri, spec)
                }
            },
        )
    }
}

private data class PendingAvatarCrop(val uri: Uri, val target: AvatarTarget)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarCropSheet(
    request: PendingAvatarCrop,
    onDismiss: () -> Unit,
    onConfirm: (Uri, AvatarCropSpec) -> Unit,
) {
    val context = LocalContext.current
    var zoom by remember(request.uri) { mutableStateOf(1f) }
    var horizontal by remember(request.uri) { mutableStateOf(0f) }
    var vertical by remember(request.uri) { mutableStateOf(0f) }
    val bitmap by produceState<Bitmap?>(initialValue = null, request.uri) {
        value = withContext(Dispatchers.IO) { loadAvatarCropPreview(context, request.uri) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (request.target == AvatarTarget.USER) "裁剪我的头像" else "裁剪 Hermes 头像",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
            )
            Text(
                "调整缩放和位置，方框内就是最终头像",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start).padding(top = 3.dp, bottom = 14.dp),
            )
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap == null) {
                    CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.4.dp)
                } else {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "头像裁剪预览",
                        contentScale = ContentScale.Crop,
                        alignment = BiasAlignment(horizontal, vertical),
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = avatarPreviewTranslation(horizontal, zoom, size.width)
                            translationY = avatarPreviewTranslation(vertical, zoom, size.height)
                        },
                    )
                    Box(Modifier.fillMaxSize().border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.82f), RoundedCornerShape(24.dp)))
                }
            }
            CropSlider("缩放", zoom, 1f..3f) { zoom = it }
            CropSlider("左右", horizontal, -1f..1f) { horizontal = it }
            CropSlider("上下", vertical, -1f..1f) { vertical = it }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
                Button(
                    onClick = { onConfirm(request.uri, AvatarCropSpec(zoom, horizontal, vertical)) },
                    enabled = bitmap != null,
                    modifier = Modifier.weight(1f),
                ) { Text("使用头像") }
            }
        }
    }
}

@Composable
private fun CropSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(0.18f))
        Slider(value = value, onValueChange = onChange, valueRange = range, modifier = Modifier.weight(0.82f))
    }
}

internal fun avatarPreviewTranslation(bias: Float, zoom: Float, axisSize: Float): Float {
    val travel = (zoom.coerceIn(1f, 4f) - 1f) * axisSize.coerceAtLeast(0f) / 2f
    return if (travel == 0f) 0f else -bias.coerceIn(-1f, 1f) * travel
}

private fun loadAvatarCropPreview(context: Context, uri: Uri): Bitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri).use { input -> BitmapFactory.decodeStream(input, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
    context.contentResolver.openInputStream(uri).use { input ->
        BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}.getOrNull()

@Composable
private fun ProfileInputRow(label: String, value: String, placeholder: String, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.35f), maxLines = 1)
        Surface(
            modifier = Modifier.weight(0.65f).height(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            tonalElevation = 0.dp,
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        inner()
                    }
                },
            )
        }
    }
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "跟随系统"
    ThemeMode.LIGHT -> "浅色模式"
    ThemeMode.DARK -> "深色模式"
}

private fun skinModeLabel(mode: SkinMode): String = when (mode) {
    SkinMode.CLEAN -> "清爽办公"
    SkinMode.GLASS -> "圆润卡片"
}

@Composable
private fun IconWell(icon: HermesIconKind) {
    Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            HermesMulticolorIcon(icon, contentDescription = null, iconSize = 21.dp)
        }
    }
}

@Composable
private fun SkinChoice(mode: SkinMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(15.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val previewBackground = if (mode == SkinMode.CLEAN) {
            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF8FAFE), Color(0xFFF1F5FB)))
        } else {
            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE7F0FF), Color(0xFFF1ECFF), Color(0xFFE6F8F3)))
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(67.dp).clip(RoundedCornerShape(11.dp)).background(previewBackground),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(7.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(5.dp)).background(Color.White))
                repeat(2) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(if (it == 0) Color(0xFFBDEBDD) else Color(0xFFDCE8FF)))
                        Box(Modifier.weight(1f).padding(start = 5.dp).height(5.dp).clip(CircleShape).background(Color(0xFFB7C0CF)))
                    }
                }
                Box(Modifier.fillMaxWidth().height(11.dp).clip(RoundedCornerShape(5.dp)).background(Color.White))
            }
            if (selected) {
                HermesMulticolorIcon(HermesIconKind.CHECK_CIRCLE, contentDescription = "已选择", modifier = Modifier.align(Alignment.TopEnd).padding(5.dp), iconSize = 16.dp)
            }
        }
        Text(
            skinModeLabel(mode),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 7.dp, bottom = 2.dp),
        )
    }
}

@Composable
private fun ThemeChoice(mode: ThemeMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> HermesIconKind.SYSTEM_MODE
        ThemeMode.LIGHT -> HermesIconKind.LIGHT_MODE
        ThemeMode.DARK -> HermesIconKind.DARK_MODE
    }
    val label = when (mode) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
    }
    val previewColors = when (mode) {
        ThemeMode.SYSTEM -> listOf(Color(0xFF5D7CF3), Color(0xFF70D3BD))
        ThemeMode.LIGHT -> listOf(Color(0xFFF7FAFF), Color(0xFFFFE8C7))
        ThemeMode.DARK -> listOf(Color(0xFF1A2240), Color(0xFF604A91))
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(12.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(previewColors)),
            contentAlignment = Alignment.Center,
        ) {
            HermesMulticolorIcon(icon, contentDescription = null, iconSize = 22.dp)
            if (selected) {
                HermesMulticolorIcon(
                    HermesIconKind.CHECK_CIRCLE,
                    contentDescription = "已选择",
                    modifier = Modifier.align(Alignment.TopEnd).padding(5.dp),
                    iconSize = 16.dp,
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(top = 7.dp, bottom = 2.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun SettingRow(icon: HermesIconKind, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconWell(icon)
        Column(modifier = Modifier.weight(1f).padding(start = 11.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        HermesMulticolorIcon(HermesIconKind.CHEVRON_RIGHT, contentDescription = null, iconSize = 15.dp)
    }
}

private fun maskAddress(url: String): String {
    if (url.isBlank()) return "尚未配置"
    return url.replace(Regex(":\\d+(?=/|$)"), ":••••")
}
