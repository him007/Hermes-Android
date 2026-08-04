package com.qingyu.hermescompanion.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qingyu.hermescompanion.R

/** Hermes Light semantic icon set: 24 x 24 grid, light geometry and restrained color. */
enum class HermesIconKind(@param:DrawableRes val drawableRes: Int) {
    CHAT(R.drawable.hermes_bitmap_chat),
    SPACE(R.drawable.hermes_bitmap_space),
    TASK(R.drawable.hermes_bitmap_task),
    PROFILE(R.drawable.hermes_bitmap_profile),
    AI(R.drawable.hermes_bitmap_ai),
    PROJECT(R.drawable.hermes_bitmap_project),
    NEW_CHAT(R.drawable.hermes_bitmap_new_chat),
    SEARCH(R.drawable.hermes_bitmap_search),
    RECENT(R.drawable.hermes_bitmap_recent),
    ARCHIVE(R.drawable.hermes_bitmap_archive),
    PINNED(R.drawable.hermes_bitmap_pinned),
    RENAME(R.drawable.hermes_bitmap_rename),
    MOVE(R.drawable.hermes_bitmap_move),
    DELETE(R.drawable.hermes_bitmap_delete),
    FOLDER(R.drawable.hermes_bitmap_folder),
    FILE(R.drawable.hermes_bitmap_file),
    ARTIFACT(R.drawable.hermes_bitmap_markdown),
    PHOTO(R.drawable.hermes_bitmap_photo),
    LINK(R.drawable.hermes_bitmap_link),
    ATTACHMENT(R.drawable.hermes_bitmap_attachment),
    MICROPHONE(R.drawable.hermes_bitmap_microphone),
    SEND(R.drawable.hermes_bitmap_send),
    STOP(R.drawable.hermes_bitmap_stop),
    CLOSE(R.drawable.hermes_bitmap_close),
    MORE(R.drawable.hermes_bitmap_more),
    REFRESH(R.drawable.hermes_bitmap_refresh),
    ADD(R.drawable.hermes_bitmap_add),
    PLAY(R.drawable.hermes_bitmap_play),
    PAUSE(R.drawable.hermes_bitmap_pause),
    EDIT(R.drawable.hermes_bitmap_edit),
    CHECK(R.drawable.hermes_bitmap_check),
    CHECK_CIRCLE(R.drawable.hermes_bitmap_check_circle),
    PENDING(R.drawable.hermes_bitmap_pending),
    UNCHECKED(R.drawable.hermes_bitmap_unchecked),
    ERROR(R.drawable.hermes_bitmap_error),
    SYNC(R.drawable.hermes_bitmap_sync),
    WAVEFORM(R.drawable.hermes_bitmap_waveform),
    IDEA(R.drawable.hermes_bitmap_idea),
    SUMMARIZE(R.drawable.hermes_bitmap_summarize),
    PLAN(R.drawable.hermes_bitmap_plan),
    APPEARANCE(R.drawable.hermes_bitmap_appearance),
    NOTIFICATION(R.drawable.hermes_bitmap_notification),
    CONNECTION(R.drawable.hermes_bitmap_connection),
    INFORMATION(R.drawable.hermes_bitmap_information),
    STORAGE(R.drawable.hermes_bitmap_storage),
    VERIFIED(R.drawable.hermes_bitmap_verified),
    MODEL(R.drawable.hermes_bitmap_model),
    TODO(R.drawable.hermes_bitmap_todo),
    BACK(R.drawable.hermes_bitmap_back),
    CHEVRON_RIGHT(R.drawable.hermes_bitmap_chevron_right),
    EXPAND_UP(R.drawable.hermes_bitmap_expand_up),
    EXPAND_DOWN(R.drawable.hermes_bitmap_expand_down),
    OPEN_EXTERNAL(R.drawable.hermes_bitmap_open_external),
    EYE(R.drawable.hermes_bitmap_eye),
    EYE_OFF(R.drawable.hermes_bitmap_eye_off),
    LOCK(R.drawable.hermes_bitmap_lock),
    WARNING(R.drawable.hermes_bitmap_warning),
    LIGHT_MODE(R.drawable.hermes_bitmap_light_mode),
    DARK_MODE(R.drawable.hermes_bitmap_dark_mode),
    SYSTEM_MODE(R.drawable.hermes_bitmap_system_mode),
    COPY(R.drawable.hermes_bitmap_copy),
    CHECKBOX_CHECKED(R.drawable.hermes_bitmap_checkbox_checked),
    CHECKBOX_EMPTY(R.drawable.hermes_bitmap_checkbox_empty),
    BOLD(R.drawable.hermes_bitmap_bold),
    ITALIC(R.drawable.hermes_bitmap_italic),
    BULLET_LIST(R.drawable.hermes_bitmap_bullet_list),
    NUMBERED_LIST(R.drawable.hermes_bitmap_numbered_list),
    QUOTE(R.drawable.hermes_bitmap_quote),
    HORIZONTAL_RULE(R.drawable.hermes_bitmap_horizontal_rule),
    FOLDER_UP(R.drawable.hermes_bitmap_folder_up),
    HISTORY(R.drawable.hermes_bitmap_history),
    LOADING(R.drawable.hermes_bitmap_loading),
    STATUS_CONNECTED(R.drawable.hermes_bitmap_status_connected),
    STATUS_BUSY(R.drawable.hermes_bitmap_status_busy),
    STATUS_ERROR(R.drawable.hermes_bitmap_status_error),
    DRAG_HANDLE(R.drawable.hermes_bitmap_drag_handle),
    SWITCH_ON(R.drawable.hermes_bitmap_switch_on),
    SWITCH_OFF(R.drawable.hermes_bitmap_switch_off),
    RADIO_SELECTED(R.drawable.hermes_bitmap_radio_selected),
}

enum class HermesStatusKind(internal val icon: HermesIconKind) {
    CONNECTED(HermesIconKind.STATUS_CONNECTED),
    BUSY(HermesIconKind.STATUS_BUSY),
    ERROR(HermesIconKind.STATUS_ERROR),
}

@Composable
fun HermesMulticolorIcon(
    kind: HermesIconKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    tint: Color? = null,
    grayscale: Boolean = false,
) {
    val effectiveTint = tint ?: if (kind == HermesIconKind.BACK) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        null
    }
    Image(
        painter = painterResource(kind.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.size(iconSize),
        colorFilter = when {
            effectiveTint != null -> ColorFilter.tint(effectiveTint)
            grayscale -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            else -> null
        },
    )
}

@Composable
fun HermesPinnedMarker(
    modifier: Modifier = Modifier,
    contentDescription: String? = "已置顶",
) {
    Image(
        painter = painterResource(HermesIconKind.PINNED.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
fun HermesStatusIcon(
    status: HermesStatusKind,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    HermesMulticolorIcon(
        kind = status.icon,
        contentDescription = contentDescription,
        modifier = modifier,
        iconSize = 12.dp,
    )
}
