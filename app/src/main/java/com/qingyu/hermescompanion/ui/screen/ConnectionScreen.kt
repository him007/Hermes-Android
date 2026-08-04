package com.qingyu.hermescompanion.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.qingyu.hermescompanion.ui.AppUiState
import com.qingyu.hermescompanion.ui.component.GlassPanel
import com.qingyu.hermescompanion.ui.component.HermesMark
import com.qingyu.hermescompanion.ui.component.HermesIconKind
import com.qingyu.hermescompanion.ui.component.HermesMulticolorIcon
import com.qingyu.hermescompanion.ui.component.HermesStatusIcon
import com.qingyu.hermescompanion.ui.component.HermesStatusKind
import com.qingyu.hermescompanion.ui.theme.HermesSpacing

@Composable
fun ConnectionScreen(
    state: AppUiState,
    contentPadding: PaddingValues,
    onConnect: (String, String, String, Boolean) -> Unit,
    onBack: (() -> Unit)?,
    onDisconnect: (() -> Unit)?,
) {
    var baseUrl by remember { mutableStateOf(state.baseUrl) }
    var username by remember { mutableStateOf(state.username) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var allowInsecureHttp by remember { mutableStateOf(false) }

    LaunchedEffect(state.baseUrl) { if (baseUrl.isBlank()) baseUrl = state.baseUrl }
    LaunchedEffect(state.username) { if (username.isBlank()) username = state.username }

    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(contentPadding),
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { HermesMulticolorIcon(HermesIconKind.BACK, "返回") }
                Column(Modifier.padding(start = 4.dp)) {
                    Text("远程网关", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("连接服务器上的 Hermes Agent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 22.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HermesMark()
                Text("Hermes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                Text("连接你的远程个人助理", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = HermesSpacing.page),
        ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.46f),
            tonalElevation = 0.dp,
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                HermesStatusIcon(if (state.hasSavedConnection) HermesStatusKind.CONNECTED else HermesStatusKind.BUSY)
                Text(
                    if (state.hasSavedConnection) "已保存远程网关，可重新验证或更新" else "使用与 Hermes Desktop 相同的账号连接",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        GlassPanel(Modifier.fillMaxWidth().padding(top = 9.dp), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CompactConnectionField(
                    label = "远程网关地址",
                    value = baseUrl,
                    placeholder = "http://服务器IP:9119",
                    keyboardType = KeyboardType.Uri,
                    onValueChange = {
                        baseUrl = it
                        if (!it.trim().startsWith("http://")) allowInsecureHttp = false
                    },
                )
                CompactConnectionField("Hermes 用户名", username, "与电脑端相同的用户名", onValueChange = { username = it })
                CompactConnectionField(
                    label = "Hermes 密码",
                    value = password,
                    placeholder = "仅用于本次登录验证",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(40.dp)) {
                            HermesMulticolorIcon(
                                if (showPassword) HermesIconKind.EYE_OFF else HermesIconKind.EYE,
                                if (showPassword) "隐藏密码" else "显示密码",
                                iconSize = 19.dp,
                            )
                        }
                    },
                    onValueChange = { password = it },
                )

                if (baseUrl.trim().startsWith("http://")) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.62f),
                        tonalElevation = 0.dp,
                    ) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(allowInsecureHttp, { allowInsecureHttp = it }, modifier = Modifier.size(36.dp))
                            Column(Modifier.padding(start = 5.dp)) {
                                Text("允许未加密 HTTP 连接", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("公网使用建议改为 HTTPS 或可信 VPN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                Button(
                    onClick = { onConnect(baseUrl, username, password, allowInsecureHttp) },
                    enabled = !state.isBusy,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    if (state.isBusy) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else {
                        HermesMulticolorIcon(HermesIconKind.CHECK_CIRCLE, null, iconSize = 19.dp)
                        Text(if (state.hasSavedConnection) "验证并更新连接" else "验证并连接", modifier = Modifier.padding(start = 7.dp))
                    }
                }
            }
        }

        GatewayNote(HermesIconKind.LOCK, "本机凭据保护", "密码不会保存；登录 Cookie 使用 Android Keystore 加密。")
        GatewayNote(HermesIconKind.WARNING, "地址填写说明", "填写电脑端“远程 URL”的完整内容，不要额外添加 /api 或 /v1。")

        if (onDisconnect != null) {
            TextButton(onClick = onDisconnect, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("清除连接并退出", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CompactConnectionField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 2.dp, bottom = 4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(9.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            tonalElevation = 0.dp,
        ) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 10.dp),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                            if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            inner()
                        }
                    },
                )
                trailing?.invoke()
            }
        }
    }
}

@Composable
private fun GatewayNote(icon: HermesIconKind, title: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
        HermesMulticolorIcon(icon, null, iconSize = 18.dp)
        Column(Modifier.padding(start = 9.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
