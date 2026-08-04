package com.qingyu.hermescompanion.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleFormattingTest {
    @Test
    fun limitsChineseTitleToFifteenCharacters() {
        assertEquals("帮我检查一下最新的知识库完成进", compactSessionTitle("帮我检查一下最新的知识库完成进度怎么样"))
    }

    @Test
    fun fallsBackToPreviewAndCleansUrlScheme() {
        assertEquals("github.com/xbma", compactSessionTitle("新会话", "https://github.com/xbmaxx/cogito"))
        assertEquals("整理本周工作", compactSessionTitle("Untitled session", "整理本周工作"))
    }

    @Test
    fun listTitleUsesEllipsisWithinFifteenCharacters() {
        assertEquals("昨天我已经把知识库进行了修订…", ellipsizeSessionTitle("昨天我已经把知识库进行了修订和整理"))
        assertEquals(15, ellipsizeSessionTitle("昨天我已经把知识库进行了修订和整理").length)
    }
}
