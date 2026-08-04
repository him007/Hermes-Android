package com.qingyu.hermescompanion.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatLinksTest {
    @Test
    fun normalizesHermesMediaLinksWithoutDamagingRegularUrls() {
        assertEquals(
            "/root/workspace/久鸿珠宝/翠合财合作方案讨论纪要.md",
            normalizeChatLinkTarget("MEDIA:/root/workspace/久鸿珠宝/翠合财合作方案讨论纪要.md"),
        )
        assertEquals(
            "https://example.com/file.md",
            normalizeChatLinkTarget("https://example.com/file.md"),
        )
        assertEquals(
            "/root/workspace/日报.md",
            normalizeChatLinkTarget("`media:/root/workspace/日报.md`"),
        )
    }

    @Test
    fun extractsStandaloneMarkdownDocumentsAsLargeTapTargets() {
        assertEquals(
            ChatFileLink(
                label = "翠合财合作方案讨论纪要.md",
                target = "/root/workspace/久鸿珠宝/翠合财合作方案讨论纪要.md",
            ),
            parseChatFileLinkLine("MEDIA:/root/workspace/久鸿珠宝/翠合财合作方案讨论纪要.md"),
        )
        assertEquals(
            ChatFileLink(label = "打开会议纪要", target = "/root/workspace/会议纪要.md"),
            parseChatFileLinkLine("[打开会议纪要](MEDIA:/root/workspace/会议纪要.md)"),
        )
        assertEquals(
            ChatFileLink(label = "日报.md", target = "/root/workspace/日报.md"),
            parseChatFileLinkLine("文件写好了：/root/workspace/日报.md，请查看"),
        )
    }

    @Test
    fun extractsHermesImageReferencesForInlinePreview() {
        assertEquals(
            ChatImageLink(
                label = "upload202608021419516.png",
                target = "/root/.hermes/images/upload202608021419516.png",
            ),
            parseChatImageLinkLine("@image:/root/.hermes/images/upload202608021419516.png"),
        )
        assertEquals(
            ChatImageLink(
                label = "设置截图",
                target = "/root/.hermes/images/settings.jpg",
            ),
            parseChatImageLinkLine("![设置截图](/root/.hermes/images/settings.jpg)"),
        )
        assertEquals(
            listOf(
                "/root/.hermes/images/first.png",
                "/root/.hermes/images/second.webp",
            ),
            findChatImageTargets(
                """
                请查看：
                @image:/root/.hermes/images/first.png
                @image:/root/.hermes/images/second.webp
                """.trimIndent(),
            ),
        )
    }
}
