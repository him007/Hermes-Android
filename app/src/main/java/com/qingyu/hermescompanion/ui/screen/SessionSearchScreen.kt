package com.qingyu.hermescompanion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qingyu.hermescompanion.model.HermesSession
import com.qingyu.hermescompanion.ui.component.GlassPanel
import com.qingyu.hermescompanion.ui.component.HermesIconKind
import com.qingyu.hermescompanion.ui.component.HermesMulticolorIcon
import com.qingyu.hermescompanion.ui.format.sessionTimeLabel
import com.qingyu.hermescompanion.ui.theme.HermesSpacing
import com.qingyu.hermescompanion.ui.format.ellipsizeSessionTitle

@Composable
fun SessionSearchScreen(
    sessions: List<HermesSession>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onOpenSession: (HermesSession) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val results = remember(sessions, query) {
        val keyword = query.trim()
        if (keyword.isBlank()) emptyList() else sessions.filter {
            it.title.contains(keyword, ignoreCase = true) || it.source.contains(keyword, ignoreCase = true)
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).statusBarsPadding()
            .padding(horizontal = HermesSpacing.page, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                HermesMulticolorIcon(HermesIconKind.BACK, contentDescription = "返回")
            }
            Text(
                "搜索对话",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 5.dp),
            )
        }
        GlassPanel(
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
            shape = RoundedCornerShape(15.dp),
            contentPadding = PaddingValues(horizontal = 11.dp, vertical = 9.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HermesMulticolorIcon(
                    HermesIconKind.SEARCH,
                    contentDescription = null,
                    iconSize = 19.dp,
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp).focusRequester(focusRequester),
                    decorationBox = { inner ->
                        if (query.isBlank()) {
                            Text("输入标题或来源", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        inner()
                    },
                )
                if (query.isNotBlank()) {
                    IconButton(onClick = { query = "" }, modifier = Modifier.size(40.dp)) {
                        HermesMulticolorIcon(HermesIconKind.CLOSE, contentDescription = "清空", iconSize = 17.dp)
                    }
                }
            }
        }

        when {
            query.isBlank() -> SearchHint("输入关键词查找服务器上的会话")
            results.isEmpty() -> SearchHint("没有匹配的对话")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
            ) {
                items(results, key = { it.id }) { session ->
                    SearchResultRow(session, onOpenSession)
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(session: HermesSession, onClick: (HermesSession) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
            .clickable { onClick(session) }.padding(horizontal = 9.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            HermesMulticolorIcon(HermesIconKind.CHAT, contentDescription = null, iconSize = 18.dp)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ellipsizeSessionTitle(session.title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Clip, modifier = Modifier.weight(1f))
                Text(sessionTimeLabel(session.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                session.preview.ifBlank { if (session.source.equals("cron", true)) "定时任务运行记录" else "暂无内容摘要" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SearchHint(text: String) {
    Box(Modifier.fillMaxWidth().padding(top = 70.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
