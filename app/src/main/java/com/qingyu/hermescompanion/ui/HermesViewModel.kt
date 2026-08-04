package com.qingyu.hermescompanion.ui

import android.app.Application
import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qingyu.hermescompanion.data.ApiException
import com.qingyu.hermescompanion.data.AttachmentReader
import com.qingyu.hermescompanion.data.ChatInsightParser
import com.qingyu.hermescompanion.data.HermesApiClient
import com.qingyu.hermescompanion.data.StreamController
import com.qingyu.hermescompanion.diagnostics.CrashDiagnostics
import com.qingyu.hermescompanion.model.ChatMessage
import com.qingyu.hermescompanion.model.ChatImage
import com.qingyu.hermescompanion.model.ChatArtifact
import com.qingyu.hermescompanion.model.ChatTodo
import com.qingyu.hermescompanion.model.ConnectionConfig
import com.qingyu.hermescompanion.model.CronJob
import com.qingyu.hermescompanion.model.HermesSession
import com.qingyu.hermescompanion.model.HermesProject
import com.qingyu.hermescompanion.model.HermesProfile
import com.qingyu.hermescompanion.model.scopedId
import com.qingyu.hermescompanion.model.MessageRole
import com.qingyu.hermescompanion.model.ImagePreview
import com.qingyu.hermescompanion.model.ModelCatalog
import com.qingyu.hermescompanion.model.PendingAttachment
import com.qingyu.hermescompanion.model.FailedSend
import com.qingyu.hermescompanion.model.NotificationPreferences
import com.qingyu.hermescompanion.model.StreamEvent
import com.qingyu.hermescompanion.model.ToolActivity
import com.qingyu.hermescompanion.model.ToolStatus
import com.qingyu.hermescompanion.model.VoicePreferences
import com.qingyu.hermescompanion.model.UserProfilePreferences
import com.qingyu.hermescompanion.model.ServerSettings
import com.qingyu.hermescompanion.model.SlashCommand
import com.qingyu.hermescompanion.model.ServerModelSettings
import com.qingyu.hermescompanion.model.ConversationStyleSettings
import com.qingyu.hermescompanion.model.ApprovalSettings
import com.qingyu.hermescompanion.model.MemoryContextSettings
import com.qingyu.hermescompanion.model.ServerSkill
import com.qingyu.hermescompanion.model.ToolsetInfo
import com.qingyu.hermescompanion.model.McpServerInfo
import com.qingyu.hermescompanion.model.WorkspaceDocument
import com.qingyu.hermescompanion.model.WorkspaceListing
import com.qingyu.hermescompanion.storage.SecureConfigStore
import com.qingyu.hermescompanion.storage.SecureCookieJar
import com.qingyu.hermescompanion.storage.AvatarStorage
import com.qingyu.hermescompanion.storage.AvatarTarget
import com.qingyu.hermescompanion.storage.AvatarCropSpec
import com.qingyu.hermescompanion.ui.format.compactSessionTitle
import com.qingyu.hermescompanion.ui.format.isPlaceholderSessionTitle
import com.qingyu.hermescompanion.notification.HermesNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URI

enum class AppRoute {
    SETUP,
    SESSIONS,
    SEARCH,
    CHAT,
    WORKSPACE,
    TASKS,
    CRON_DETAIL,
    PROFILE,
    PROFILE_SETTINGS,
    SKILLS_TOOLS,
    MODEL_SETTINGS,
    CONVERSATION_STYLE,
    APPROVAL_SETTINGS,
    MEMORY_CONTEXT,
    ARCHIVED_SESSIONS,
    NOTIFICATIONS,
    VOICE_SETTINGS,
    ABOUT,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class SkinMode {
    CLEAN,
    GLASS,
}

data class AppUiState(
    val route: AppRoute = AppRoute.SETUP,
    val baseUrl: String = "",
    val username: String = "",
    val hasSavedConnection: Boolean = false,
    val sessions: List<HermesSession> = emptyList(),
    val sessionTotalCount: Int = 0,
    val projects: List<HermesProject> = emptyList(),
    val profiles: List<HermesProfile> = emptyList(),
    val activeProfile: String = "default",
    val isProfilesLoading: Boolean = false,
    val isProfileSwitching: Boolean = false,
    val selectedSession: HermesSession? = null,
    val messages: List<ChatMessage> = emptyList(),
    val toolActivities: List<ToolActivity> = emptyList(),
    val chatArtifacts: List<ChatArtifact> = emptyList(),
    val chatTodos: List<ChatTodo> = emptyList(),
    val attachments: List<PendingAttachment> = emptyList(),
    val draft: String = "",
    val failedSend: FailedSend? = null,
    val slashCommands: List<SlashCommand> = emptyList(),
    val isSlashCommandsLoading: Boolean = false,
    val isBusy: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingSessionId: String? = null,
    val unreadSessionIds: Set<String> = emptySet(),
    val isRecoveringConnection: Boolean = false,
    val modelCatalog: ModelCatalog = ModelCatalog(),
    val isModelsLoading: Boolean = false,
    val isModelSwitching: Boolean = false,
    val isProjectsLoading: Boolean = false,
    val sessionActionId: String? = null,
    val isBatchRenaming: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val skinMode: SkinMode = SkinMode.CLEAN,
    val workspaceListing: WorkspaceListing? = null,
    val projectPickerListing: WorkspaceListing? = null,
    val isProjectPickerLoading: Boolean = false,
    val workspaceRootPath: String? = null,
    val workspaceDocument: WorkspaceDocument? = null,
    val workspaceDocumentOrigin: AppRoute? = null,
    val imagePreview: ImagePreview? = null,
    val isImageLoading: Boolean = false,
    val inlineImagePreviews: Map<String, ImagePreview> = emptyMap(),
    val inlineImageLoading: Set<String> = emptySet(),
    val inlineImageFailures: Set<String> = emptySet(),
    val workspaceDraft: String = "",
    val isWorkspaceLoading: Boolean = false,
    val isWorkspaceEditing: Boolean = false,
    val isWorkspaceSaving: Boolean = false,
    val cronJobs: List<CronJob> = emptyList(),
    val selectedCronJob: CronJob? = null,
    val isCronLoading: Boolean = false,
    val cronActionId: String? = null,
    val serverSettings: ServerSettings = ServerSettings(),
    val serverSkills: List<ServerSkill> = emptyList(),
    val toolsets: List<ToolsetInfo> = emptyList(),
    val mcpServers: List<McpServerInfo> = emptyList(),
    val selectedSkill: ServerSkill? = null,
    val selectedSkillContent: String = "",
    val archivedSessions: List<HermesSession> = emptyList(),
    val isAdvancedSettingsLoading: Boolean = false,
    val settingsActionKey: String? = null,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val voicePreferences: VoicePreferences = VoicePreferences(),
    val userProfile: UserProfilePreferences = UserProfilePreferences(),
    val isAvatarUpdating: Boolean = false,
    val crashReport: String? = null,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
)

class HermesViewModel(application: Application) : AndroidViewModel(application) {
    private val configStore = SecureConfigStore(application)
    private val avatarStorage = AvatarStorage(application)
    private val cookieJar = SecureCookieJar(configStore)
    private var apiClient: HermesApiClient? = null
    private var streamController: StreamController? = null
    private var streamJob: Job? = null
    private var streamRecoveryJob: Job? = null
    private var streamRecoveryPrompt: String = ""
    private var streamBaselineAssistantSignature: String = ""
    private var activeSubmittedPrompt: String = ""
    private var activeSubmittedAttachments: List<PendingAttachment> = emptyList()
    private var activeUserMessageId: String? = null
    private var titleRefreshJob: Job? = null
    private var slashCommandJob: Job? = null
    private var slashCommandQuery: String = ""
    private val messageCache = LinkedHashMap<String, List<ChatMessage>>()
    private var activeStreamSession: HermesSession? = null
    private var activeStreamToolActivities: List<ToolActivity> = emptyList()
    private var activeStreamArtifacts: List<ChatArtifact> = emptyList()
    private var activeStreamTodos: List<ChatTodo> = emptyList()
    private val pendingTitleSessionIds = mutableSetOf<String>()
    private var settingsReturnRoute: AppRoute = AppRoute.PROFILE

    var uiState by androidx.compose.runtime.mutableStateOf(AppUiState())
        private set

    init {
        val crashReport = CrashDiagnostics.read(application)
        val savedActiveProfile = configStore.readActiveHermesProfile()
        val unreadSessionIds = configStore.readUnreadSessionIds().mapTo(mutableSetOf()) { id ->
            if ("::" in id) id else "$savedActiveProfile::$id"
        }
        configStore.saveUnreadSessionIds(unreadSessionIds)
        val savedTheme = configStore.readThemeMode()
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
        val savedSkin = configStore.readSkinMode()
            ?.let { runCatching { SkinMode.valueOf(it) }.getOrNull() }
            ?: SkinMode.CLEAN
        val storedProfile = configStore.readUserProfile()
        val safeProfile = avatarStorage.sanitize(storedProfile)
        if (safeProfile != storedProfile) configStore.saveUserProfile(safeProfile)
        uiState = uiState.copy(themeMode = savedTheme, skinMode = savedSkin)
        uiState = uiState.copy(
            notificationPreferences = configStore.readNotificationPreferences(),
            voicePreferences = configStore.readVoicePreferences(),
            userProfile = safeProfile,
            activeProfile = savedActiveProfile,
            unreadSessionIds = unreadSessionIds,
            crashReport = crashReport,
        )
        val saved = configStore.read()
        if (saved != null) {
            val client = HermesApiClient(saved, cookieJar)
            apiClient = client
            uiState = uiState.copy(
                route = AppRoute.SETUP,
                baseUrl = saved.baseUrl,
                username = saved.username,
                hasSavedConnection = true,
                isBusy = client.hasSavedSession(),
            )
            // If the previous process crashed, stay on the safe setup route so the report
            // can be copied instead of immediately repeating the same route transition.
            if (client.hasSavedSession() && crashReport == null) resumeSavedConnection(client)
        }
    }

    fun dismissCrashReport() {
        CrashDiagnostics.clear(getApplication())
        uiState = uiState.copy(crashReport = null)
    }

    fun connect(baseUrl: String, username: String, password: String, allowInsecureHttp: Boolean) {
        val normalizedUrl = normalizeBaseUrl(baseUrl)
        if (normalizedUrl == null) {
            showError("请输入有效的远程网关地址，例如 http://服务器IP:9119")
            return
        }
        if (normalizedUrl.startsWith("http://") && !allowInsecureHttp) {
            showError("这是未加密的 HTTP 连接，请勾选风险确认后再连接")
            return
        }
        if (username.isBlank() || password.isBlank()) {
            showError("请输入 Hermes 用户名和密码")
            return
        }

        uiState = uiState.copy(isBusy = true, errorMessage = null, noticeMessage = null)
        viewModelScope.launch {
            runCatching {
                val config = ConnectionConfig(normalizedUrl, username.trim())
                val client = HermesApiClient(config, cookieJar)
                val signedInAs = withContext(Dispatchers.IO) { client.login(config.username, password) }
                configStore.save(config)
                apiClient?.takeIf { it !== client }?.close()
                apiClient = client
                Triple(config, signedInAs, client)
            }.onSuccess { (config, signedInAs, client) ->
                uiState = uiState.copy(
                    route = AppRoute.SESSIONS,
                    baseUrl = config.baseUrl,
                    username = config.username,
                    hasSavedConnection = true,
                    isBusy = false,
                    noticeMessage = "已登录：$signedInAs",
                )
                loadProfilesAndSessions(client)
            }.onFailure(::handleFailure)
        }
    }

    fun refreshSessions() {
        val client = apiClient ?: return
        val expectedProfile = client.currentProfile()
        uiState = uiState.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listSessions() } }
                .onSuccess { page ->
                    if (client.currentProfile() != expectedProfile) return@onSuccess
                    uiState = uiState.copy(
                        // Compose LazyColumn requires globally unique item keys. Some Hermes
                        // gateway builds can repeat a stored session in the first page.
                        sessions = page.sessions.distinctBy(HermesSession::id),
                        sessionTotalCount = page.totalCount,
                        isBusy = false,
                        isProfileSwitching = false,
                    )
                    refreshProjects()
                }
                .onFailure { error ->
                    if (client.currentProfile() == expectedProfile) handleFailure(error)
                }
        }
    }

    fun refreshProfiles() {
        val client = apiClient ?: return
        uiState = uiState.copy(isProfilesLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listProfiles() } }
                .onSuccess { profiles ->
                    val available = profiles.ifEmpty { listOf(HermesProfile(name = "default", isDefault = true)) }
                    val activeStillExists = available.any { it.name == client.currentProfile() }
                    if (activeStillExists) {
                        uiState = uiState.copy(profiles = available, isProfilesLoading = false)
                    } else {
                        val fallback = available.firstOrNull(HermesProfile::isDefault) ?: available.first()
                        client.setProfile(fallback.name)
                        configStore.saveActiveHermesProfile(fallback.name)
                        messageCache.clear()
                        uiState = uiState.copy(
                            profiles = available,
                            activeProfile = fallback.name,
                            isProfilesLoading = false,
                            isProfileSwitching = true,
                            sessions = emptyList(),
                            projects = emptyList(),
                            noticeMessage = "原 Profile 已不存在，已切换到 ${fallback.name}",
                        )
                        refreshSessions()
                    }
                }
                .onFailure {
                    uiState = uiState.copy(isProfilesLoading = false)
                    handleFailure(it)
                }
        }
    }

    fun selectProfile(profile: HermesProfile) {
        val client = apiClient ?: return
        if (uiState.isStreaming) {
            showNotice("当前回复仍在生成，请等待完成后再切换 Profile")
            return
        }
        if (profile.name == client.currentProfile()) return
        titleRefreshJob?.cancel()
        slashCommandJob?.cancel()
        client.setProfile(profile.name)
        configStore.saveActiveHermesProfile(profile.name)
        messageCache.clear()
        pendingTitleSessionIds.clear()
        uiState = uiState.copy(
            route = AppRoute.SESSIONS,
            activeProfile = profile.name,
            isProfileSwitching = true,
            isProfilesLoading = false,
            isProjectsLoading = false,
            sessions = emptyList(),
            sessionTotalCount = 0,
            projects = emptyList(),
            selectedSession = null,
            messages = emptyList(),
            toolActivities = emptyList(),
            chatArtifacts = emptyList(),
            chatTodos = emptyList(),
            attachments = emptyList(),
            draft = "",
            failedSend = null,
            workspaceListing = null,
            workspaceDocument = null,
            cronJobs = emptyList(),
            selectedCronJob = null,
            serverSkills = emptyList(),
            toolsets = emptyList(),
            mcpServers = emptyList(),
            archivedSessions = emptyList(),
            noticeMessage = "已切换到 Profile：${profile.name}",
            errorMessage = null,
        )
        refreshSessions()
    }

    fun createSession() {
        if (uiState.isStreaming) {
            showNotice("当前回复仍在后台生成，请等待完成后再新建对话")
            return
        }
        val client = apiClient ?: return
        uiState = uiState.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.createSession() } }
                .onSuccess { session ->
                    uiState = uiState.copy(
                        route = AppRoute.CHAT,
                        selectedSession = session,
                        messages = emptyList(),
                        toolActivities = emptyList(),
                        chatArtifacts = emptyList(),
                        chatTodos = emptyList(),
                        inlineImagePreviews = emptyMap(),
                        inlineImageLoading = emptySet(),
                        inlineImageFailures = emptySet(),
                        draft = configStore.readDraft(session.profile, session.id),
                        failedSend = null,
                        isBusy = false,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun openSession(session: HermesSession) {
        val client = apiClient ?: return
        markSessionRead(session.scopedId)
        val cached = messageCache[session.scopedId]
        val cachedInsights = cached?.let(ChatInsightParser::fromMessages)
        val isActiveStream = uiState.isStreaming && uiState.streamingSessionId == session.id
        uiState = uiState.copy(
            route = AppRoute.CHAT,
            selectedSession = session,
            messages = cached.visibleConversationMessages(),
            toolActivities = if (isActiveStream) activeStreamToolActivities else emptyList(),
            chatArtifacts = if (isActiveStream) activeStreamArtifacts else cachedInsights?.artifacts.orEmpty(),
            chatTodos = if (isActiveStream) activeStreamTodos else cachedInsights?.todos.orEmpty(),
            attachments = emptyList(),
            draft = configStore.readDraft(session.profile, session.id),
            failedSend = null,
            inlineImagePreviews = emptyMap(),
            inlineImageLoading = emptySet(),
            inlineImageFailures = emptySet(),
            isBusy = cached == null && !isActiveStream,
            errorMessage = null,
        )
        if (isActiveStream) return
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.loadMessages(session) } }
                .onSuccess { messages ->
                    if (uiState.route != AppRoute.CHAT || uiState.selectedSession?.id != session.id) {
                        return@onSuccess
                    }
                    messageCache[session.scopedId] = messages
                    while (messageCache.size > 8) messageCache.remove(messageCache.keys.first())
                    val insights = ChatInsightParser.fromMessages(messages)
                    uiState = uiState.copy(
                        messages = messages.visibleConversationMessages(),
                        chatArtifacts = insights.artifacts,
                        chatTodos = insights.todos,
                        isBusy = false,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun deleteSession(session: HermesSession) {
        val client = apiClient ?: return
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.deleteSession(session.id) } }
                .onSuccess {
                    val unreadSessionIds = uiState.unreadSessionIds - session.scopedId
                    configStore.saveUnreadSessionIds(unreadSessionIds)
                    configStore.clearDraft(session.profile, session.id)
                    uiState = uiState.copy(
                        sessions = uiState.sessions.filterNot { it.id == session.id },
                        sessionTotalCount = (uiState.sessionTotalCount - 1).coerceAtLeast(0),
                        unreadSessionIds = unreadSessionIds,
                        noticeMessage = "会话已删除",
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun refreshProjects() {
        val client = apiClient ?: return
        if (uiState.isProjectsLoading) return
        val expectedProfile = client.currentProfile()
        uiState = uiState.copy(isProjectsLoading = true)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.projectCatalog() } }
                .onSuccess { projects ->
                    if (client.currentProfile() != expectedProfile) return@onSuccess
                    uiState = uiState.copy(projects = projects, isProjectsLoading = false)
                }
                .onFailure {
                    if (client.currentProfile() == expectedProfile) {
                        uiState = uiState.copy(isProjectsLoading = false)
                    }
                }
        }
    }

    fun createProject(name: String, primaryPath: String) {
        val client = apiClient ?: return
        val cleanName = name.trim()
        val cleanPath = primaryPath.trim()
        when {
            cleanName.isBlank() -> showError("请输入项目名称")
            cleanPath.isBlank() -> showError("请输入服务器上的项目目录")
            !cleanPath.startsWith('/') -> showError("项目目录需要使用绝对路径，例如 /root/workspace/my-project")
            uiState.isProjectsLoading -> return
            else -> {
                uiState = uiState.copy(isProjectsLoading = true, errorMessage = null)
                viewModelScope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { client.createProject(cleanName, cleanPath) }
                    }.onSuccess { project ->
                        uiState = uiState.copy(
                            projects = (uiState.projects + project).distinctBy(HermesProject::id),
                            isProjectsLoading = false,
                            noticeMessage = "项目“${project.name}”已创建",
                        )
                    }.onFailure(::handleFailure)
                }
            }
        }
    }

    fun loadProjectDirectoryPicker(path: String? = null) {
        val client = apiClient ?: return
        if (uiState.isProjectPickerLoading) return
        uiState = uiState.copy(isProjectPickerLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    if (path.isNullOrBlank()) client.initialWorkspace() else client.listWorkspace(path)
                }
            }.onSuccess { listing ->
                uiState = uiState.copy(projectPickerListing = listing, isProjectPickerLoading = false)
            }.onFailure(::handleFailure)
        }
    }

    fun closeProjectDirectoryPicker() {
        uiState = uiState.copy(projectPickerListing = null, isProjectPickerLoading = false)
    }

    fun aiRenameSession(session: HermesSession) {
        val client = apiClient ?: return
        if (uiState.sessionActionId != null || uiState.isBatchRenaming) return
        uiState = uiState.copy(sessionActionId = session.id, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val generated = client.generateSessionTitles(listOf(session))[session.id]
                        ?: throw ApiException(500, "Hermes 没有生成新的会话标题")
                    client.renameSession(session.id, generated)
                }
            }.onSuccess { title ->
                updateSession(session.id) { it.copy(title = title) }
                uiState = uiState.copy(
                    sessionActionId = null,
                    noticeMessage = "已重命名为“$title”",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun batchAiRenameSessions() {
        val client = apiClient ?: return
        if (uiState.isBatchRenaming || uiState.sessionActionId != null) return
        val targets = uiState.sessions.filter { !it.source.equals("cron", true) && it.messageCount > 0 }
        if (targets.isEmpty()) {
            showNotice("当前没有可重命名的对话")
            return
        }
        uiState = uiState.copy(isBatchRenaming = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val generated = client.generateSessionTitles(targets)
                    buildMap {
                        targets.forEach { session ->
                            val title = generated[session.id] ?: return@forEach
                            put(session.id, client.renameSession(session.id, title))
                        }
                    }
                }
            }.onSuccess { titles ->
                uiState = uiState.copy(
                    sessions = uiState.sessions.map { session ->
                        titles[session.id]?.let { session.copy(title = it) } ?: session
                    },
                    isBatchRenaming = false,
                    noticeMessage = "已完成 ${titles.size} 个对话改名",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun toggleSessionPinned(session: HermesSession) {
        val client = apiClient ?: return
        if (uiState.sessionActionId != null) return
        val pinned = !session.isPinned
        uiState = uiState.copy(sessionActionId = session.id, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.setSessionPinned(session.id, pinned) } }
                .onSuccess {
                    updateSession(session.id) { it.copy(isPinned = pinned) }
                    uiState = uiState.copy(
                        sessionActionId = null,
                        noticeMessage = if (pinned) "会话已置顶" else "已取消置顶",
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun archiveSession(session: HermesSession) {
        val client = apiClient ?: return
        if (uiState.sessionActionId != null) return
        uiState = uiState.copy(sessionActionId = session.id, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.archiveSession(session.id) } }
                .onSuccess {
                    val unreadSessionIds = uiState.unreadSessionIds - session.scopedId
                    configStore.saveUnreadSessionIds(unreadSessionIds)
                    uiState = uiState.copy(
                        sessions = uiState.sessions.filterNot { it.id == session.id },
                        sessionTotalCount = (uiState.sessionTotalCount - 1).coerceAtLeast(0),
                        unreadSessionIds = unreadSessionIds,
                        sessionActionId = null,
                        noticeMessage = "会话已归档，可在 Hermes 电脑端恢复",
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun moveSessionToProject(session: HermesSession, project: HermesProject) {
        val client = apiClient ?: return
        if (uiState.sessionActionId != null) return
        uiState = uiState.copy(sessionActionId = session.id, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { client.moveSessionToProject(session, project) }
            }.onSuccess { updated ->
                updateSession(session.id) { current ->
                    current.copy(runtimeId = updated.runtimeId, workspacePath = updated.workspacePath)
                }
                uiState = uiState.copy(
                    sessionActionId = null,
                    noticeMessage = "已移至项目“${project.name}”",
                )
                refreshProjects()
            }.onFailure(::handleFailure)
        }
    }

    fun updateDraft(value: String) {
        uiState.selectedSession?.let { session ->
            configStore.saveDraft(session.profile, session.id, value)
        }
        val token = value.trimStart()
        val slashQuery = token.takeIf { it.startsWith('/') && !it.contains(Regex("\\s")) }
        uiState = uiState.copy(
            draft = value,
            slashCommands = if (slashQuery == null) emptyList() else uiState.slashCommands,
            isSlashCommandsLoading = if (slashQuery == null) false else uiState.isSlashCommandsLoading,
        )
        if (slashQuery == null) {
            slashCommandJob?.cancel()
            slashCommandQuery = ""
        } else {
            loadSlashCommands(slashQuery)
        }
    }

    private fun loadSlashCommands(query: String) {
        val client = apiClient ?: return
        if (query == slashCommandQuery && (uiState.slashCommands.isNotEmpty() || uiState.isSlashCommandsLoading)) return
        slashCommandQuery = query
        slashCommandJob?.cancel()
        uiState = uiState.copy(isSlashCommandsLoading = true)
        slashCommandJob = viewModelScope.launch {
            delay(120)
            val commands = runCatching { withContext(Dispatchers.IO) { client.slashCommands(query) } }
                .getOrElse { DEFAULT_SLASH_COMMANDS.filter { it.command.startsWith(query, ignoreCase = true) } }
            if (slashCommandQuery == query) {
                uiState = uiState.copy(
                    slashCommands = commands.ifEmpty { DEFAULT_SLASH_COMMANDS.filter { it.command.startsWith(query, ignoreCase = true) } },
                    isSlashCommandsLoading = false,
                )
            }
        }
    }

    fun addAttachments(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val resolver = getApplication<Application>().contentResolver
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { uris.map { AttachmentReader.read(resolver, it) } }
            }.onSuccess { attachments ->
                uiState = uiState.copy(
                    attachments = (uiState.attachments + attachments).take(5),
                    noticeMessage = if (uiState.attachments.size + attachments.size > 5) {
                        "单次最多添加 5 个附件"
                    } else {
                        null
                    },
                )
            }.onFailure(::handleFailure)
        }
    }

    fun removeAttachment(id: String) {
        uiState = uiState.copy(attachments = uiState.attachments.filterNot { it.id == id })
    }

    fun loadModelCatalog() {
        val client = apiClient ?: return
        if (uiState.isModelsLoading || uiState.modelCatalog.providers.isNotEmpty()) return
        uiState = uiState.copy(isModelsLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.modelCatalog() } }
                .onSuccess { catalog ->
                    uiState = uiState.copy(modelCatalog = catalog, isModelsLoading = false)
                }
                .onFailure(::handleFailure)
        }
    }

    fun switchModel(provider: String, model: String) {
        val client = apiClient ?: return
        val session = uiState.selectedSession ?: return
        if (uiState.isStreaming || uiState.isModelSwitching) return
        uiState = uiState.copy(isModelSwitching = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { client.switchSessionModel(session, provider, model) }
            }.onSuccess { updated ->
                uiState = uiState.copy(
                    selectedSession = updated,
                    isModelSwitching = false,
                    noticeMessage = "当前会话已切换到 ${model.substringAfterLast('/')}",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun openChatArtifact(artifact: ChatArtifact) {
        val client = apiClient ?: return
        if (artifact.path.substringAfterLast('.', "").lowercase() in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp")) {
            openImage(artifact.path, artifact.name)
            return
        }
        if (!artifact.path.substringAfterLast('.', "").equals("md", true) &&
            !artifact.path.substringAfterLast('.', "").equals("markdown", true)
        ) {
            showNotice("${artifact.name} 已列入聊天产物；当前版本可在空间页浏览，Markdown 可直接预览编辑")
            return
        }
        uiState = uiState.copy(isWorkspaceLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.readWorkspaceDocument(artifact.path) } }
                .onSuccess { document ->
                    uiState = uiState.copy(
                        route = AppRoute.WORKSPACE,
                        workspaceDocument = document,
                        workspaceDocumentOrigin = AppRoute.CHAT,
                        workspaceDraft = document.content,
                        isWorkspaceEditing = false,
                        isWorkspaceLoading = false,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun openChatLink(rawTarget: String) {
        val target = normalizeChatLinkTarget(rawTarget)
        if (target.isBlank()) {
            showNotice("文件链接为空，无法打开")
            return
        }
        if (target.startsWith("http://", ignoreCase = true) || target.startsWith("https://", ignoreCase = true)) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { getApplication<Application>().startActivity(intent) }
                .onFailure { showNotice("没有找到可以打开这个链接的应用") }
            return
        }
        openChatArtifact(
            ChatArtifact(
                path = target,
                name = target.substringAfterLast('/').ifBlank { "聊天文件" },
                kind = "文件",
            ),
        )
    }

    fun sendMessage() {
        val client = apiClient ?: return
        val session = uiState.selectedSession ?: return
        val prompt = uiState.draft.trim()
        val attachments = uiState.attachments
        if (prompt.isBlank() && attachments.isEmpty()) return
        if (uiState.isStreaming) return

        val submittedPrompt = prompt.ifBlank { "请查看我发送的附件。" }

        val displayText = buildString {
            if (prompt.isNotBlank()) append(prompt)
            attachments.forEach { attachment ->
                if (isNotEmpty()) append('\n')
                append("📎 ").append(attachment.name)
            }
        }
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = displayText,
            images = attachments.mapNotNull { attachment ->
                attachment.dataUrl?.let { ChatImage(attachment.name, it, attachment.mimeType) }
            },
        )
        val assistantMessage = ChatMessage(
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true,
        )
        if (session.messageCount == 0 || session.title == "新会话") {
            pendingTitleSessionIds += session.id
        }
        streamRecoveryJob?.cancel()
        streamRecoveryPrompt = submittedPrompt
        activeSubmittedPrompt = prompt
        activeSubmittedAttachments = attachments
        activeUserMessageId = userMessage.id
        streamBaselineAssistantSignature = uiState.messages
            .lastOrNull { it.role == MessageRole.ASSISTANT }
            ?.recoverySignature()
            .orEmpty()
        activeStreamSession = session
        activeStreamToolActivities = emptyList()
        activeStreamArtifacts = emptyList()
        activeStreamTodos = emptyList()
        val unreadSessionIds = uiState.unreadSessionIds - session.scopedId
        configStore.saveUnreadSessionIds(unreadSessionIds)
        configStore.clearDraft(session.profile, session.id)
        uiState = uiState.copy(
            draft = "",
            attachments = emptyList(),
            failedSend = null,
            messages = uiState.messages + userMessage + assistantMessage,
            toolActivities = emptyList(),
            isStreaming = true,
            streamingSessionId = session.id,
            unreadSessionIds = unreadSessionIds,
            isRecoveringConnection = false,
            errorMessage = null,
            noticeMessage = null,
        )
        cacheMessages(session.id, uiState.messages)

        val controller = StreamController()
        streamController = controller
        streamJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                client.streamMessage(
                    controller = controller,
                    session = session,
                    prompt = submittedPrompt,
                    attachments = attachments,
                    onEvent = { event ->
                        viewModelScope.launch { handleStreamEvent(event) }
                    },
                )
            }.onFailure { throwable ->
                if (!controller.isStopped() && !controller.wasDisconnected()) {
                    withContext(Dispatchers.Main) { handleStreamFailure(throwable) }
                }
            }
        }
    }

    fun retryFailedMessage() {
        if (uiState.isStreaming || uiState.failedSend == null) return
        sendMessage()
    }

    fun stopGeneration() {
        val controller = streamController
        val runtimeSessionId = controller?.runtimeSessionId
        val sessionId = uiState.streamingSessionId
        controller?.stop()
        streamJob?.cancel()
        streamRecoveryJob?.cancel()
        streamRecoveryJob = null
        streamRecoveryPrompt = ""
        streamBaselineAssistantSignature = ""
        val stoppedMessages = activeStreamMessages().mapNotNull { message ->
            if (!message.isStreaming) return@mapNotNull message
            message.takeIf { it.content.isNotBlank() || it.images.isNotEmpty() }
                ?.copy(isStreaming = false)
        }
        if (sessionId != null) cacheMessages(sessionId, stoppedMessages)
        uiState = uiState.copy(
            isStreaming = false,
            streamingSessionId = null,
            isRecoveringConnection = false,
            messages = if (uiState.selectedSession?.id == sessionId) stoppedMessages else uiState.messages,
            noticeMessage = "已请求停止",
        )
        clearActiveStreamState()
        if (!runtimeSessionId.isNullOrBlank()) {
            val client = apiClient ?: return
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { client.stopRun(runtimeSessionId) }
            }
        }
    }

    fun backToSessions() {
        uiState.selectedSession?.id?.let { sessionId ->
            if (uiState.messages.isNotEmpty()) cacheMessages(sessionId, uiState.messages)
        }
        uiState = uiState.copy(
            route = AppRoute.SESSIONS,
            selectedSession = null,
            messages = emptyList(),
            toolActivities = emptyList(),
            attachments = emptyList(),
            draft = "",
            failedSend = null,
            inlineImagePreviews = emptyMap(),
            inlineImageLoading = emptySet(),
            inlineImageFailures = emptySet(),
        )
        refreshSessions()
    }

    fun showSessions() {
        if (uiState.route == AppRoute.SESSIONS) return
        uiState = uiState.copy(route = AppRoute.SESSIONS, errorMessage = null, noticeMessage = null)
        refreshSessions()
    }

    fun showSessionSearch() {
        uiState = uiState.copy(route = AppRoute.SEARCH, errorMessage = null, noticeMessage = null)
    }

    fun closeSessionSearch() {
        uiState = uiState.copy(route = AppRoute.SESSIONS, errorMessage = null, noticeMessage = null)
    }

    fun showWorkspace() {
        uiState = uiState.copy(
            route = AppRoute.WORKSPACE,
            workspaceDocumentOrigin = null,
            errorMessage = null,
            noticeMessage = null,
        )
        if (uiState.workspaceListing == null) refreshWorkspace(resetToRoot = true)
    }

    fun refreshWorkspace(resetToRoot: Boolean = false) {
        val client = apiClient ?: return
        val existing = uiState.workspaceListing
        uiState = uiState.copy(isWorkspaceLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    if (resetToRoot || existing == null) client.initialWorkspace()
                    else client.listWorkspace(existing.path).copy(projectName = existing.projectName)
                }
            }.onSuccess { listing ->
                uiState = uiState.copy(
                    workspaceListing = listing,
                    workspaceRootPath = if (resetToRoot || uiState.workspaceRootPath == null) listing.path else uiState.workspaceRootPath,
                    workspaceDocument = null,
                    workspaceDocumentOrigin = null,
                    workspaceDraft = "",
                    isWorkspaceEditing = false,
                    isWorkspaceLoading = false,
                )
            }.onFailure(::handleFailure)
        }
    }

    fun openWorkspaceDirectory(path: String) {
        val client = apiClient ?: return
        val current = uiState.workspaceListing ?: return
        val root = uiState.workspaceRootPath ?: current.path
        if (!pathIsWithin(root, path)) {
            showError("不能离开当前 Hermes 项目目录")
            return
        }
        uiState = uiState.copy(isWorkspaceLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listWorkspace(path) } }
                .onSuccess { listing ->
                    uiState = uiState.copy(
                        workspaceListing = listing.copy(projectName = current.projectName),
                        isWorkspaceLoading = false,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun openWorkspaceDocument(path: String) {
        val client = apiClient ?: return
        uiState = uiState.copy(isWorkspaceLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.readWorkspaceDocument(path) } }
                .onSuccess { document ->
                    uiState = uiState.copy(
                        workspaceDocument = document,
                        workspaceDocumentOrigin = null,
                        workspaceDraft = document.content,
                        isWorkspaceEditing = false,
                        isWorkspaceLoading = false,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun openImage(source: String, name: String = "图片") {
        val client = apiClient ?: return
        uiState = uiState.copy(isImageLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.readImage(source) } }
                .onSuccess { image ->
                    uiState = uiState.copy(
                        imagePreview = image.copy(name = image.name.takeUnless { it == "图片" }.orEmpty().ifBlank { name }),
                        isImageLoading = false,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun loadInlineChatImages(sources: List<String>) {
        val client = apiClient ?: return
        val sessionId = uiState.selectedSession?.id ?: return
        val pending = sources.asSequence()
            .filter(String::isNotBlank)
            .distinct()
            .filterNot { source ->
                source in uiState.inlineImagePreviews ||
                    source in uiState.inlineImageLoading ||
                    source in uiState.inlineImageFailures ||
                    source.startsWith("data:image/", ignoreCase = true)
            }
            .take(12)
            .toList()
        if (pending.isEmpty()) return

        uiState = uiState.copy(inlineImageLoading = uiState.inlineImageLoading + pending)
        viewModelScope.launch {
            pending.forEach { source ->
                val result = runCatching { withContext(Dispatchers.IO) { client.readImage(source) } }
                if (uiState.selectedSession?.id != sessionId) return@launch
                result.onSuccess { image ->
                    uiState = uiState.copy(
                        inlineImagePreviews = uiState.inlineImagePreviews + (source to image),
                        inlineImageLoading = uiState.inlineImageLoading - source,
                    )
                }.onFailure {
                    uiState = uiState.copy(
                        inlineImageLoading = uiState.inlineImageLoading - source,
                        inlineImageFailures = uiState.inlineImageFailures + source,
                    )
                }
            }
        }
    }

    fun closeImagePreview() {
        uiState = uiState.copy(imagePreview = null, isImageLoading = false)
    }

    fun closeWorkspaceDocument() {
        val returnRoute = uiState.workspaceDocumentOrigin
        uiState = uiState.copy(
            route = returnRoute ?: uiState.route,
            workspaceDocument = null,
            workspaceDocumentOrigin = null,
            workspaceDraft = "",
            isWorkspaceEditing = false,
            errorMessage = null,
        )
    }

    fun setWorkspaceEditing(editing: Boolean) {
        uiState = uiState.copy(
            isWorkspaceEditing = editing,
            workspaceDraft = if (!editing) uiState.workspaceDocument?.content.orEmpty() else uiState.workspaceDraft,
        )
    }

    fun updateWorkspaceDraft(value: String) {
        uiState = uiState.copy(workspaceDraft = value)
    }

    fun saveWorkspaceDocument() {
        val client = apiClient ?: return
        val document = uiState.workspaceDocument ?: return
        if (uiState.isWorkspaceSaving) return
        uiState = uiState.copy(isWorkspaceSaving = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    client.saveWorkspaceDocument(document.path, uiState.workspaceDraft)
                }
            }.onSuccess { saved ->
                uiState = uiState.copy(
                    workspaceDocument = saved,
                    workspaceDraft = saved.content,
                    isWorkspaceEditing = false,
                    isWorkspaceSaving = false,
                    noticeMessage = "文档已保存到 Hermes 工作区",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun showTasks() {
        uiState = uiState.copy(route = AppRoute.TASKS, errorMessage = null, noticeMessage = null)
        refreshCronJobs()
    }

    fun openCronJob(job: CronJob) {
        uiState = uiState.copy(
            route = AppRoute.CRON_DETAIL,
            selectedCronJob = job,
            errorMessage = null,
            noticeMessage = null,
        )
    }

    fun closeCronJob() {
        uiState = uiState.copy(route = AppRoute.TASKS, selectedCronJob = null, errorMessage = null)
    }

    fun refreshCronJobs() {
        val client = apiClient ?: return
        if (uiState.isCronLoading) return
        uiState = uiState.copy(isCronLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listCronJobs() } }
                .onSuccess { jobs ->
                    uiState = uiState.copy(cronJobs = jobs, isCronLoading = false)
                    configStore.saveCronSnapshot(jobs.associate { it.id to "${it.lastRunAt}|${it.lastStatus}" })
                }
                .onFailure(::handleFailure)
        }
    }

    fun createCronJob(name: String, prompt: String, schedule: String) {
        val client = apiClient ?: return
        if (name.isBlank() || prompt.isBlank() || schedule.isBlank()) {
            showError("请填写任务名称、执行内容和时间计划")
            return
        }
        uiState = uiState.copy(isCronLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.createCronJob(name, prompt, schedule) } }
                .onSuccess { job ->
                    uiState = uiState.copy(
                        cronJobs = (uiState.cronJobs + job).distinctBy(CronJob::id),
                        isCronLoading = false,
                        noticeMessage = "定时任务已创建",
                    )
                    HermesNotifications.scheduleCronPolling(
                        getApplication(),
                        uiState.notificationPreferences.enabled && uiState.notificationPreferences.taskAlerts,
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun updateCronJob(job: CronJob, name: String, prompt: String, schedule: String) {
        val client = apiClient ?: return
        if (name.isBlank() || prompt.isBlank() || schedule.isBlank()) {
            showError("请填写任务名称、执行内容和时间计划")
            return
        }
        if (uiState.cronActionId != null) return
        uiState = uiState.copy(cronActionId = job.id, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    client.updateCronJob(job.id, name, prompt, schedule)
                }
            }.onSuccess { updated ->
                uiState = uiState.copy(
                    cronJobs = uiState.cronJobs.map { if (it.id == job.id) updated else it },
                    selectedCronJob = uiState.selectedCronJob?.let { if (it.id == job.id) updated else it },
                    cronActionId = null,
                    noticeMessage = "定时任务已更新",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun toggleCronJob(job: CronJob) {
        val client = apiClient ?: return
        if (uiState.cronActionId != null) return
        uiState = uiState.copy(cronActionId = job.id, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    if (job.enabled) client.pauseCronJob(job.id) else client.resumeCronJob(job.id)
                }
            }.onSuccess {
                uiState = uiState.copy(
                    cronJobs = uiState.cronJobs.map { if (it.id == job.id) it.copy(enabled = !job.enabled) else it },
                    selectedCronJob = uiState.selectedCronJob?.let {
                        if (it.id == job.id) it.copy(enabled = !job.enabled) else it
                    },
                    cronActionId = null,
                    noticeMessage = if (job.enabled) "定时任务已暂停" else "定时任务已恢复",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun triggerCronJob(job: CronJob) {
        val client = apiClient ?: return
        if (uiState.cronActionId != null) return
        uiState = uiState.copy(cronActionId = job.id, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.triggerCronJob(job.id) } }
                .onSuccess {
                    uiState = uiState.copy(cronActionId = null, noticeMessage = "已开始执行“${job.name}”")
                    delay(1_000)
                    refreshCronJobs()
                }
                .onFailure(::handleFailure)
        }
    }

    fun deleteCronJob(job: CronJob) {
        val client = apiClient ?: return
        if (uiState.cronActionId != null) return
        uiState = uiState.copy(cronActionId = job.id, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.deleteCronJob(job.id) } }
                .onSuccess {
                    uiState = uiState.copy(
                        cronJobs = uiState.cronJobs.filterNot { it.id == job.id },
                        route = if (uiState.route == AppRoute.CRON_DETAIL) AppRoute.TASKS else uiState.route,
                        selectedCronJob = null,
                        cronActionId = null,
                        noticeMessage = "定时任务已删除",
                    )
                }
                .onFailure(::handleFailure)
        }
    }

    fun showProfile() {
        uiState = uiState.copy(route = AppRoute.PROFILE, errorMessage = null, noticeMessage = null)
    }

    fun updateUserProfile(value: UserProfilePreferences) {
        val safeProfile = avatarStorage.sanitize(value)
        configStore.saveUserProfile(safeProfile)
        uiState = uiState.copy(userProfile = safeProfile, noticeMessage = "个人资料已保存")
    }

    fun updateUserAvatar(source: Uri, crop: AvatarCropSpec) {
        replaceAvatar(source, AvatarTarget.USER, crop)
    }

    fun updateHermesAvatar(source: Uri, crop: AvatarCropSpec) {
        replaceAvatar(source, AvatarTarget.HERMES, crop)
    }

    fun resetUserAvatar() {
        resetAvatar(AvatarTarget.USER)
    }

    fun resetHermesAvatar() {
        resetAvatar(AvatarTarget.HERMES)
    }

    fun showProfileSettings() {
        uiState = uiState.copy(route = AppRoute.PROFILE_SETTINGS, errorMessage = null, noticeMessage = null)
    }

    private fun replaceAvatar(source: Uri, target: AvatarTarget, crop: AvatarCropSpec) {
        if (uiState.isAvatarUpdating) return
        uiState = uiState.copy(isAvatarUpdating = true, errorMessage = null, noticeMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { avatarStorage.save(source, target, crop) } }
                .onSuccess { privateUri ->
                    val profile = when (target) {
                        AvatarTarget.USER -> uiState.userProfile.copy(avatarUri = privateUri)
                        AvatarTarget.HERMES -> uiState.userProfile.copy(hermesAvatarUri = privateUri)
                    }
                    configStore.saveUserProfile(profile)
                    uiState = uiState.copy(
                        userProfile = profile,
                        isAvatarUpdating = false,
                        noticeMessage = if (target == AvatarTarget.USER) "我的头像已更新" else "Hermes 头像已更新",
                    )
                }
                .onFailure { throwable ->
                    val message = when (unwrapFailure(throwable)) {
                        is SecurityException -> "照片读取授权已失效，请重新选择图片"
                        is OutOfMemoryError -> "图片尺寸过大，请选择较小的图片"
                        else -> throwable.message?.takeIf(String::isNotBlank) ?: "头像保存失败，请重新选择"
                    }
                    uiState = uiState.copy(isAvatarUpdating = false, errorMessage = message)
                }
        }
    }

    private fun resetAvatar(target: AvatarTarget) {
        if (uiState.isAvatarUpdating) return
        uiState = uiState.copy(isAvatarUpdating = true, errorMessage = null, noticeMessage = null)
        viewModelScope.launch {
            withContext(Dispatchers.IO) { avatarStorage.delete(target) }
            val profile = when (target) {
                AvatarTarget.USER -> uiState.userProfile.copy(avatarUri = "")
                AvatarTarget.HERMES -> uiState.userProfile.copy(hermesAvatarUri = "")
            }
            configStore.saveUserProfile(profile)
            uiState = uiState.copy(
                userProfile = profile,
                isAvatarUpdating = false,
                noticeMessage = if (target == AvatarTarget.USER) "已恢复默认用户头像" else "已恢复默认 Hermes 头像",
            )
        }
    }

    fun showSkillsAndTools() {
        val client = apiClient ?: return
        uiState = uiState.copy(
            route = AppRoute.SKILLS_TOOLS,
            isAdvancedSettingsLoading = true,
            errorMessage = null,
            noticeMessage = null,
        )
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    Triple(client.listSkills(), client.listToolsets(), client.listMcpServers())
                }
            }.onSuccess { (skills, tools, mcp) ->
                uiState = uiState.copy(
                    serverSkills = skills,
                    toolsets = tools,
                    mcpServers = mcp,
                    isAdvancedSettingsLoading = false,
                )
            }.onFailure(::handleFailure)
        }
    }

    fun toggleSkill(skill: ServerSkill) {
        val client = apiClient ?: return
        if (uiState.settingsActionKey != null) return
        uiState = uiState.copy(settingsActionKey = "skill:${skill.name}", errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.setSkillEnabled(skill.name, !skill.enabled) } }
                .onSuccess {
                    uiState = uiState.copy(
                        serverSkills = uiState.serverSkills.map {
                            if (it.name == skill.name) it.copy(enabled = !skill.enabled) else it
                        },
                        selectedSkill = uiState.selectedSkill?.let {
                            if (it.name == skill.name) it.copy(enabled = !skill.enabled) else it
                        },
                        settingsActionKey = null,
                        noticeMessage = "技能设置已保存，下次会话生效",
                    )
                }.onFailure(::handleFailure)
        }
    }

    fun openSkill(skill: ServerSkill) {
        val client = apiClient ?: return
        uiState = uiState.copy(selectedSkill = skill, selectedSkillContent = "", settingsActionKey = "skill-content:${skill.name}")
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.skillContent(skill.name) } }
                .onSuccess { content ->
                    uiState = uiState.copy(selectedSkillContent = content, settingsActionKey = null)
                }.onFailure(::handleFailure)
        }
    }

    fun closeSkill() {
        uiState = uiState.copy(selectedSkill = null, selectedSkillContent = "", settingsActionKey = null)
    }

    fun toggleToolset(toolset: ToolsetInfo) {
        val client = apiClient ?: return
        if (uiState.settingsActionKey != null) return
        uiState = uiState.copy(settingsActionKey = "toolset:${toolset.name}", errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.setToolsetEnabled(toolset.name, !toolset.enabled) } }
                .onSuccess {
                    uiState = uiState.copy(
                        toolsets = uiState.toolsets.map {
                            if (it.name == toolset.name) it.copy(enabled = !toolset.enabled) else it
                        },
                        serverSettings = withContext(Dispatchers.IO) { client.serverSettings() },
                        settingsActionKey = null,
                        noticeMessage = "工具集设置已保存，下次会话生效",
                    )
                }.onFailure(::handleFailure)
        }
    }

    fun toggleMcpServer(server: McpServerInfo) {
        val client = apiClient ?: return
        if (uiState.settingsActionKey != null) return
        uiState = uiState.copy(settingsActionKey = "mcp:${server.name}", errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.setMcpServerEnabled(server.name, !server.enabled) } }
                .onSuccess {
                    uiState = uiState.copy(
                        mcpServers = uiState.mcpServers.map {
                            if (it.name == server.name) it.copy(enabled = !server.enabled) else it
                        },
                        settingsActionKey = null,
                        noticeMessage = "MCP 设置已保存",
                    )
                }.onFailure(::handleFailure)
        }
    }

    fun showModelSettings() = loadServerSettings(AppRoute.MODEL_SETTINGS, loadModels = true)

    fun saveModelSettings(value: ServerModelSettings) {
        saveServerSettings("模型设置已保存，新会话将使用新的模型配置") { it.saveModelSettings(value) }
    }

    fun addCustomProvider(id: String, name: String, baseUrl: String, model: String, apiKey: String) {
        val client = apiClient ?: return
        if (id.isBlank() || baseUrl.isBlank() || model.isBlank()) {
            showError("请填写提供商标识、接口地址和默认模型")
            return
        }
        uiState = uiState.copy(isAdvancedSettingsLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val settings = client.addCustomProvider(id, name, baseUrl, model, apiKey)
                    settings to client.modelCatalog()
                }
            }.onSuccess { (settings, catalog) ->
                uiState = uiState.copy(
                    serverSettings = settings,
                    modelCatalog = catalog,
                    isAdvancedSettingsLoading = false,
                    noticeMessage = "模型提供商已添加",
                )
            }.onFailure(::handleFailure)
        }
    }

    fun showConversationStyleSettings() = loadServerSettings(AppRoute.CONVERSATION_STYLE)

    fun saveConversationStyle(value: ConversationStyleSettings) {
        saveServerSettings("对话风格已保存") { it.saveConversationStyle(value) }
    }

    fun showApprovalSettings() = loadServerSettings(AppRoute.APPROVAL_SETTINGS)

    fun saveApprovalSettings(value: ApprovalSettings) {
        saveServerSettings("审批模式已保存") { it.saveApprovalSettings(value) }
    }

    fun showMemoryContextSettings() = loadServerSettings(AppRoute.MEMORY_CONTEXT)

    fun saveMemorySettings(value: MemoryContextSettings) {
        saveServerSettings("记忆与上下文设置已保存") { it.saveMemorySettings(value) }
    }

    fun showArchivedSessions() {
        val client = apiClient ?: return
        uiState = uiState.copy(
            route = AppRoute.ARCHIVED_SESSIONS,
            isAdvancedSettingsLoading = true,
            errorMessage = null,
            noticeMessage = null,
        )
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listArchivedSessions() } }
                .onSuccess { sessions ->
                    uiState = uiState.copy(archivedSessions = sessions, isAdvancedSettingsLoading = false)
                }.onFailure(::handleFailure)
        }
    }

    fun restoreArchivedSession(session: HermesSession) {
        val client = apiClient ?: return
        if (uiState.settingsActionKey != null) return
        uiState = uiState.copy(settingsActionKey = "restore:${session.id}", errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.restoreSession(session.id) } }
                .onSuccess {
                    uiState = uiState.copy(
                        archivedSessions = uiState.archivedSessions.filterNot { it.id == session.id },
                        settingsActionKey = null,
                        noticeMessage = "会话已恢复",
                    )
                    refreshSessions()
                }.onFailure(::handleFailure)
        }
    }

    fun deleteArchivedSession(session: HermesSession) {
        val client = apiClient ?: return
        if (uiState.settingsActionKey != null) return
        uiState = uiState.copy(settingsActionKey = "delete:${session.id}", errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.deleteSession(session.id) } }
                .onSuccess {
                    uiState = uiState.copy(
                        archivedSessions = uiState.archivedSessions.filterNot { it.id == session.id },
                        settingsActionKey = null,
                        noticeMessage = "归档会话已删除",
                    )
                }.onFailure(::handleFailure)
        }
    }

    private fun loadServerSettings(route: AppRoute, loadModels: Boolean = false) {
        val client = apiClient ?: return
        uiState = uiState.copy(
            route = route,
            isAdvancedSettingsLoading = true,
            errorMessage = null,
            noticeMessage = null,
        )
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    client.serverSettings() to if (loadModels) client.modelCatalog() else uiState.modelCatalog
                }
            }.onSuccess { (settings, catalog) ->
                uiState = uiState.copy(
                    serverSettings = settings,
                    modelCatalog = catalog,
                    isAdvancedSettingsLoading = false,
                )
            }.onFailure(::handleFailure)
        }
    }

    private fun saveServerSettings(
        notice: String,
        save: (HermesApiClient) -> ServerSettings,
    ) {
        val client = apiClient ?: return
        if (uiState.isAdvancedSettingsLoading) return
        uiState = uiState.copy(isAdvancedSettingsLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { save(client) } }
                .onSuccess { settings ->
                    uiState = uiState.copy(
                        serverSettings = settings,
                        isAdvancedSettingsLoading = false,
                        noticeMessage = notice,
                    )
                }.onFailure(::handleFailure)
        }
    }

    fun showNotificationSettings() {
        uiState = uiState.copy(route = AppRoute.NOTIFICATIONS, errorMessage = null, noticeMessage = null)
    }

    fun updateNotificationPreferences(value: NotificationPreferences) {
        configStore.saveNotificationPreferences(value)
        HermesNotifications.applyPreferences(getApplication(), value)
        HermesNotifications.scheduleCronPolling(getApplication(), value.enabled && value.taskAlerts)
        uiState = uiState.copy(notificationPreferences = value)
    }

    fun sendTestNotification() {
        HermesNotifications.showMessage(getApplication(), "Hermes 通知测试", "系统通知、提示音与角标已经可以正常工作。")
        showNotice("测试通知已发送；如果没有出现，请检查系统通知权限")
    }

    fun showVoiceSettings() {
        uiState = uiState.copy(route = AppRoute.VOICE_SETTINGS, errorMessage = null, noticeMessage = null)
    }

    fun updateVoicePreferences(value: VoicePreferences) {
        configStore.saveVoicePreferences(value)
        uiState = uiState.copy(voicePreferences = value)
    }

    fun acceptVoiceResult(text: String) {
        if (text.isBlank()) return
        updateDraft(listOf(uiState.draft, text).filter(String::isNotBlank).joinToString(" "))
        if (uiState.voicePreferences.autoSend) sendMessage()
    }

    fun showAbout() {
        uiState = uiState.copy(route = AppRoute.ABOUT, errorMessage = null, noticeMessage = null)
    }

    fun closeSettingsPage() {
        uiState = uiState.copy(route = AppRoute.PROFILE, errorMessage = null, noticeMessage = null)
    }

    fun openConnectionSettings() {
        if (uiState.isStreaming) stopGeneration()
        settingsReturnRoute = uiState.route.takeIf { it in setOf(AppRoute.SESSIONS, AppRoute.WORKSPACE, AppRoute.TASKS, AppRoute.PROFILE) }
            ?: AppRoute.PROFILE
        uiState = uiState.copy(route = AppRoute.SETUP, errorMessage = null, noticeMessage = null)
    }

    fun closeConnectionSettings() {
        uiState = uiState.copy(route = settingsReturnRoute, errorMessage = null, noticeMessage = null)
    }

    fun disconnect() {
        if (uiState.isStreaming) stopGeneration()
        apiClient?.close()
        cookieJar.clear()
        configStore.clear()
        apiClient = null
        uiState = AppUiState(
            route = AppRoute.SETUP,
            themeMode = uiState.themeMode,
            skinMode = uiState.skinMode,
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        configStore.saveThemeMode(mode.name)
        uiState = uiState.copy(themeMode = mode)
    }

    fun setSkinMode(mode: SkinMode) {
        configStore.saveSkinMode(mode.name)
        uiState = uiState.copy(skinMode = mode)
    }

    fun clearTransientMessage() {
        uiState = uiState.copy(errorMessage = null, noticeMessage = null)
    }

    fun showVoiceRecognitionUnavailable() {
        showError("此手机没有可用的系统语音识别服务，请安装或启用语音助手后再试")
    }

    fun showNotice(message: String) {
        uiState = uiState.copy(noticeMessage = message, errorMessage = null)
    }

    private fun handleStreamEvent(event: StreamEvent) {
        when (event) {
            is StreamEvent.RunStarted -> Unit
            is StreamEvent.AssistantDelta -> updateStreamingMessage { current -> current + event.text }
            is StreamEvent.AssistantCompleted -> {
                if (event.content.isNotBlank()) updateStreamingMessage { event.content }
            }
            is StreamEvent.ToolStarted -> updateTool(event.name, event.preview, ToolStatus.RUNNING, event.todos)
            is StreamEvent.ToolCompleted -> updateTool(event.name, event.preview, ToolStatus.COMPLETED, event.todos)
            is StreamEvent.ToolFailed -> updateTool(event.name, event.preview, ToolStatus.FAILED)
            is StreamEvent.ConnectionInterrupted -> recoverInterruptedStream(event.message)
            is StreamEvent.Error -> {
                handleStreamFailure(IllegalStateException(event.message))
            }
            StreamEvent.Completed -> finishStreaming()
        }
    }

    private fun updateStreamingMessage(transform: (String) -> String) {
        val updated = activeStreamMessages().map { message ->
            if (message.isStreaming) message.copy(content = transform(message.content)) else message
        }
        setActiveStreamMessages(updated)
    }

    private fun updateTool(
        name: String,
        preview: String,
        status: ToolStatus,
        todos: List<ChatTodo> = emptyList(),
    ) {
        val existing = activeStreamToolActivities.indexOfLast { it.name == name && it.status == ToolStatus.RUNNING }
        val updated = activeStreamToolActivities.toMutableList()
        if (existing >= 0) {
            updated[existing] = updated[existing].copy(
                preview = preview.ifBlank { updated[existing].preview },
                status = status,
            )
        } else {
            updated += ToolActivity(name = name, preview = preview, status = status)
        }
        val newArtifacts = ChatInsightParser.artifactsFromText(preview)
        activeStreamToolActivities = updated
        if (todos.isNotEmpty()) activeStreamTodos = todos
        activeStreamArtifacts = (activeStreamArtifacts + newArtifacts).distinctBy(ChatArtifact::path)
        if (uiState.selectedSession?.id == uiState.streamingSessionId) {
            uiState = uiState.copy(
                toolActivities = activeStreamToolActivities,
                chatTodos = activeStreamTodos,
                chatArtifacts = activeStreamArtifacts,
            )
        }
    }

    private fun finishStreaming() {
        val sessionId = uiState.streamingSessionId ?: activeStreamSession?.id
        val session = activeStreamSession ?: uiState.sessions.firstOrNull { it.id == sessionId }
        val completedMessages = activeStreamMessages().mapNotNull { message ->
            if (!message.isStreaming) return@mapNotNull message
            message.takeIf { it.content.isNotBlank() || it.images.isNotEmpty() }
                ?.copy(isStreaming = false)
        }
        if (sessionId != null) cacheMessages(sessionId, completedMessages)
        val completedMessage = completedMessages.lastOrNull { it.role == MessageRole.ASSISTANT }
        val completedText = completedMessage?.content.orEmpty().replace(Regex("\\s+"), " ").trim()
        val hasReply = completedText.isNotBlank() || completedMessage?.images?.isNotEmpty() == true
        val needsAttention = sessionId != null && hasReply && replyNeedsAttention(
            route = uiState.route,
            selectedSessionId = uiState.selectedSession?.id,
            completedSessionId = sessionId,
            appInForeground = isAppInForeground(),
        )
        val unreadKey = session?.scopedId ?: "${uiState.activeProfile}::$sessionId"
        val unreadSessionIds = if (needsAttention) uiState.unreadSessionIds + unreadKey else uiState.unreadSessionIds
        if (needsAttention) configStore.saveUnreadSessionIds(unreadSessionIds)
        uiState = uiState.copy(
            isStreaming = false,
            streamingSessionId = null,
            isRecoveringConnection = false,
            messages = if (uiState.selectedSession?.id == sessionId) completedMessages else uiState.messages,
            toolActivities = if (uiState.selectedSession?.id == sessionId) activeStreamToolActivities else uiState.toolActivities,
            chatArtifacts = if (uiState.selectedSession?.id == sessionId) activeStreamArtifacts else uiState.chatArtifacts,
            chatTodos = if (uiState.selectedSession?.id == sessionId) activeStreamTodos else uiState.chatTodos,
            unreadSessionIds = unreadSessionIds,
            sessions = uiState.sessions.map { item ->
                if (item.id == sessionId && hasReply) {
                    item.copy(preview = completedText.ifBlank { "Hermes 已发送图片" })
                } else item
            },
        )
        if (needsAttention) {
            val hermesName = uiState.userProfile.hermesDisplayName.ifBlank { "Hermes" }
            HermesNotifications.showMessage(
                getApplication(),
                "$hermesName 已回复",
                listOfNotNull(
                    session?.title?.takeIf(String::isNotBlank),
                    completedText.take(120).ifBlank { "回复中包含图片" },
                ).joinToString(" · "),
            )
        }
        if (sessionId != null) scheduleTitleRefresh(sessionId)
        clearActiveStreamState()
    }

    private fun recoverInterruptedStream(message: String) {
        if (streamRecoveryJob?.isActive == true) return
        val client = apiClient ?: return finishInterruptedRecovery()
        val session = activeStreamSession
            ?: uiState.sessions.firstOrNull { it.id == uiState.streamingSessionId }
            ?: return finishInterruptedRecovery()
        val prompt = streamRecoveryPrompt
        val baselineSignature = streamBaselineAssistantSignature

        streamController = null
        streamJob = null
        uiState = uiState.copy(
            isStreaming = true,
            isRecoveringConnection = true,
            errorMessage = null,
            noticeMessage = message,
            toolActivities = activeStreamToolActivities.map { activity ->
                if (activity.status == ToolStatus.RUNNING) activity.copy(status = ToolStatus.FAILED) else activity
            },
        )
        activeStreamToolActivities = activeStreamToolActivities.map { activity ->
            if (activity.status == ToolStatus.RUNNING) activity.copy(status = ToolStatus.FAILED) else activity
        }

        streamRecoveryJob = viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.reconnectGateway() } }

            val retryDelays = listOf(0L, 1_000L, 2_000L, 4_000L, 7_000L, 10_000L, 15_000L, 20_000L)
            for (waitMillis in retryDelays) {
                if (waitMillis > 0) delay(waitMillis)
                if (!uiState.isStreaming || uiState.streamingSessionId != session.id) return@launch

                val result = runCatching { withContext(Dispatchers.IO) { client.loadLatestMessages(session) } }
                val failure = result.exceptionOrNull()?.let(::unwrapFailure)
                if (failure is ApiException && failure.statusCode in setOf(401, 403)) {
                    handleStreamFailure(failure)
                    return@launch
                }

                val messages = result.getOrNull() ?: continue
                val recoveredAssistant = findRecoveredAssistant(messages, prompt, baselineSignature)
                if (recoveredAssistant != null) {
                    val visibleMessages = messages.visibleConversationMessages()
                    cacheMessages(session.id, visibleMessages)
                    val insights = ChatInsightParser.fromMessages(messages)
                    activeStreamArtifacts = insights.artifacts
                    activeStreamTodos = insights.todos
                    uiState = uiState.copy(
                        messages = if (uiState.selectedSession?.id == session.id) visibleMessages else uiState.messages,
                        chatArtifacts = if (uiState.selectedSession?.id == session.id) insights.artifacts else uiState.chatArtifacts,
                        chatTodos = if (uiState.selectedSession?.id == session.id) insights.todos else uiState.chatTodos,
                        noticeMessage = "连接已恢复，回复已同步",
                        errorMessage = null,
                    )
                    finishStreaming()
                    return@launch
                }
            }
            finishInterruptedRecovery()
        }
    }

    private fun finishInterruptedRecovery() {
        val sessionId = uiState.streamingSessionId
        val stoppedMessages = preserveFailedSend(sessionId)
        streamRecoveryJob = null
        streamController = null
        streamJob = null
        streamRecoveryPrompt = ""
        streamBaselineAssistantSignature = ""
        uiState = uiState.copy(
            isStreaming = false,
            streamingSessionId = null,
            isRecoveringConnection = false,
            messages = if (uiState.selectedSession?.id == sessionId) stoppedMessages else uiState.messages,
            toolActivities = uiState.toolActivities.map { activity ->
                if (activity.status == ToolStatus.RUNNING) activity.copy(status = ToolStatus.FAILED) else activity
            },
            noticeMessage = null,
            errorMessage = "网络仍不稳定，未能自动取回完整回复。请稍后重新打开当前对话确认结果。",
        )
        clearActiveStreamState()
    }

    private fun scheduleTitleRefresh(sessionId: String) {
        val session = activeStreamSession?.takeIf { it.id == sessionId }
            ?: uiState.selectedSession?.takeIf { it.id == sessionId }
            ?: uiState.sessions.firstOrNull { it.id == sessionId }
            ?: return
        if (sessionId !in pendingTitleSessionIds) return
        val client = apiClient ?: return
        titleRefreshJob?.cancel()
        titleRefreshJob = viewModelScope.launch {
            val waits = listOf(700L, 1_400L, 2_800L)
            for (wait in waits) {
                delay(wait)
                val raw = runCatching { withContext(Dispatchers.IO) { client.sessionTitle(session.id) } }
                    .getOrNull()
                    .orEmpty()
                if (!isPlaceholderSessionTitle(raw)) {
                    val compact = compactSessionTitle(raw)
                    val saved = if (compact != raw.trim()) {
                        runCatching { withContext(Dispatchers.IO) { client.renameSession(session.id, compact) } }
                            .getOrDefault(compact)
                    } else compact
                    applySessionTitle(session.id, saved)
                    pendingTitleSessionIds -= session.id
                    return@launch
                }
            }
            val fallback = compactSessionTitle(
                messageCache[cacheKey(sessionId)].orEmpty().firstOrNull { it.role == MessageRole.USER }?.content.orEmpty(),
            )
            if (fallback != "新会话") {
                val saved = runCatching { withContext(Dispatchers.IO) { client.renameSession(session.id, fallback) } }
                    .getOrDefault(fallback)
                applySessionTitle(session.id, saved)
            }
            pendingTitleSessionIds -= session.id
        }
    }

    private fun applySessionTitle(sessionId: String, title: String) {
        uiState = uiState.copy(
            selectedSession = uiState.selectedSession?.takeIf { it.id == sessionId }?.copy(title = title)
                ?: uiState.selectedSession,
            sessions = uiState.sessions.map { if (it.id == sessionId) it.copy(title = title) else it },
        )
    }

    private fun updateSession(sessionId: String, transform: (HermesSession) -> HermesSession) {
        uiState = uiState.copy(
            sessions = uiState.sessions.map { if (it.id == sessionId) transform(it) else it },
            selectedSession = uiState.selectedSession?.let { if (it.id == sessionId) transform(it) else it },
        )
    }

    private fun activeStreamMessages(): List<ChatMessage> {
        val sessionId = uiState.streamingSessionId ?: activeStreamSession?.id ?: return emptyList()
        return if (uiState.selectedSession?.id == sessionId && uiState.messages.isNotEmpty()) {
            uiState.messages
        } else {
            messageCache[cacheKey(sessionId)].orEmpty()
        }
    }

    private fun setActiveStreamMessages(messages: List<ChatMessage>) {
        val sessionId = uiState.streamingSessionId ?: activeStreamSession?.id ?: return
        cacheMessages(sessionId, messages)
        if (uiState.selectedSession?.id == sessionId) {
            uiState = uiState.copy(messages = messages)
        }
    }

    private fun cacheMessages(sessionId: String, messages: List<ChatMessage>) {
        messageCache[cacheKey(sessionId)] = messages
        while (messageCache.size > 8) messageCache.remove(messageCache.keys.first())
    }

    private fun cacheKey(sessionId: String): String =
        "${activeStreamSession?.profile ?: uiState.activeProfile}::$sessionId"

    private fun markSessionRead(sessionId: String) {
        if (sessionId !in uiState.unreadSessionIds) return
        val unreadSessionIds = uiState.unreadSessionIds - sessionId
        configStore.saveUnreadSessionIds(unreadSessionIds)
        uiState = uiState.copy(unreadSessionIds = unreadSessionIds)
    }

    private fun clearActiveStreamState() {
        streamController = null
        streamJob = null
        streamRecoveryJob = null
        streamRecoveryPrompt = ""
        streamBaselineAssistantSignature = ""
        activeSubmittedPrompt = ""
        activeSubmittedAttachments = emptyList()
        activeUserMessageId = null
        activeStreamSession = null
        activeStreamToolActivities = emptyList()
        activeStreamArtifacts = emptyList()
        activeStreamTodos = emptyList()
    }

    private fun handleStreamFailure(throwable: Throwable) {
        val sessionId = uiState.streamingSessionId ?: activeStreamSession?.id
        val stoppedMessages = preserveFailedSend(sessionId)
        uiState = uiState.copy(
            isStreaming = false,
            streamingSessionId = null,
            isRecoveringConnection = false,
            messages = if (uiState.selectedSession?.id == sessionId) stoppedMessages else uiState.messages,
        )
        clearActiveStreamState()
        handleFailure(throwable)
    }

    private fun preserveFailedSend(sessionId: String?): List<ChatMessage> {
        val currentMessages = activeStreamMessages()
        val partialReply = currentMessages.lastOrNull { it.isStreaming }
            ?.let { it.content.isNotBlank() || it.images.isNotEmpty() }
            ?: false
        val stopped = currentMessages.mapNotNull { item ->
            if (!item.isStreaming) return@mapNotNull item
            item.takeIf { it.content.isNotBlank() || it.images.isNotEmpty() }
                ?.copy(isStreaming = false)
        }
        val cleaned = if (!partialReply && activeUserMessageId != null) {
            stopped.filterNot { it.id == activeUserMessageId }
        } else stopped
        if (sessionId != null) cacheMessages(sessionId, cleaned)

        val failure = FailedSend(activeSubmittedPrompt, activeSubmittedAttachments)
        val session = activeStreamSession ?: uiState.selectedSession?.takeIf { it.id == sessionId }
        if (session != null) configStore.saveDraft(session.profile, session.id, activeSubmittedPrompt)
        uiState = uiState.copy(
            draft = if (uiState.selectedSession?.id == sessionId) activeSubmittedPrompt else uiState.draft,
            attachments = if (uiState.selectedSession?.id == sessionId) activeSubmittedAttachments else uiState.attachments,
            failedSend = if (uiState.selectedSession?.id == sessionId) failure else uiState.failedSend,
        )
        return cleaned
    }

    private fun handleFailure(throwable: Throwable) {
        val root = unwrapFailure(throwable)
        val message = when (root) {
            is ApiException -> when (root.statusCode) {
                401, 403 -> "登录已失效，或 Hermes 用户名/密码不正确"
                404 -> "当前 Hermes 版本不支持所需接口，请先升级 Hermes Agent"
                429 -> "Hermes 正在处理过多任务，请稍后再试"
                else -> root.message.ifBlank { "服务器请求失败" }
            }
            is IOException -> "网络连接不稳定，请稍后重试"
            else -> root.message?.takeIf { it.isNotBlank() } ?: "连接失败，请检查远程网关地址和网络"
        }
        uiState = uiState.copy(
            isBusy = false,
            isWorkspaceLoading = false,
            isWorkspaceSaving = false,
            isImageLoading = false,
            isCronLoading = false,
            cronActionId = null,
            isModelsLoading = false,
            isModelSwitching = false,
            isProfileSwitching = false,
            isProjectsLoading = false,
            isProjectPickerLoading = false,
            isAdvancedSettingsLoading = false,
            settingsActionKey = null,
            sessionActionId = null,
            isBatchRenaming = false,
            errorMessage = message,
            route = if (root is ApiException && root.statusCode in setOf(401, 403)) AppRoute.SETUP else uiState.route,
            hasSavedConnection = if (root is ApiException && root.statusCode in setOf(401, 403)) {
                false
            } else {
                uiState.hasSavedConnection
            },
        )
    }

    private fun showError(message: String) {
        uiState = uiState.copy(errorMessage = message, noticeMessage = null, isBusy = false)
    }

    private fun normalizeBaseUrl(raw: String): String? {
        val value = raw.trim().trimEnd('/')
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) return null
        if (uri.userInfo != null || uri.query != null || uri.fragment != null) return null
        return value
    }

    private fun pathIsWithin(root: String, target: String): Boolean {
        val cleanRoot = root.trimEnd('/', '\\')
        if (target == cleanRoot || target == root) return true
        return target.startsWith("$cleanRoot/") || target.startsWith("$cleanRoot\\")
    }

    private fun isAppInForeground(): Boolean {
        val info = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(info)
        return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
            info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
    }

    private fun resumeSavedConnection(client: HermesApiClient) {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.checkSavedSession() } }
                .onSuccess { signedInAs ->
                    uiState = uiState.copy(
                        route = AppRoute.SESSIONS,
                        isBusy = false,
                        noticeMessage = "已恢复登录：$signedInAs",
                    )
                    loadProfilesAndSessions(client)
                }
                .onFailure {
                    cookieJar.clear()
                    uiState = uiState.copy(
                        route = AppRoute.SETUP,
                        isBusy = false,
                        hasSavedConnection = false,
                        noticeMessage = "登录已过期，请重新输入密码",
                    )
                }
        }
    }

    private fun loadProfilesAndSessions(client: HermesApiClient) {
        uiState = uiState.copy(isProfilesLoading = true, isBusy = true)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listProfiles() } }
                .onSuccess { profiles ->
                    val available = profiles.ifEmpty { listOf(HermesProfile(name = "default", isDefault = true)) }
                    val saved = configStore.readActiveHermesProfile()
                    val selected = available.firstOrNull { it.name == saved }
                        ?: available.firstOrNull { it.isDefault }
                        ?: available.first()
                    client.setProfile(selected.name)
                    configStore.saveActiveHermesProfile(selected.name)
                    uiState = uiState.copy(
                        profiles = available,
                        activeProfile = selected.name,
                        isProfilesLoading = false,
                    )
                    refreshSessions()
                }
                .onFailure {
                    uiState = uiState.copy(isProfilesLoading = false)
                    handleFailure(it)
                }
        }
    }

    private fun unwrapFailure(throwable: Throwable): Throwable {
        var current = throwable
        while (current.cause != null && current.cause !== current) current = current.cause!!
        return current
    }

    override fun onCleared() {
        streamRecoveryJob?.cancel()
        apiClient?.close()
        super.onCleared()
    }
}

private val DEFAULT_SLASH_COMMANDS = listOf(
    SlashCommand("/new", "开始一个新对话", "会话"),
    SlashCommand("/retry", "重新执行上一条消息", "会话"),
    SlashCommand("/undo", "移除上一轮用户与助手消息", "会话"),
    SlashCommand("/title", "设置当前对话标题", "会话", "[标题]"),
    SlashCommand("/compress", "压缩当前对话上下文", "会话"),
    SlashCommand("/model", "查看或切换当前模型", "模型", "[provider:model]"),
    SlashCommand("/reasoning", "调整推理强度或显示方式", "模型", "[级别]"),
    SlashCommand("/skills", "搜索、查看或管理技能", "技能"),
    SlashCommand("/status", "查看当前会话状态", "信息"),
    SlashCommand("/usage", "查看本会话用量", "信息"),
    SlashCommand("/help", "查看可用命令", "信息"),
    SlashCommand("/stop", "停止当前正在执行的任务", "会话"),
)

private fun List<ChatMessage>?.visibleConversationMessages(): List<ChatMessage> =
    this.orEmpty().filter { it.role == MessageRole.USER || it.role == MessageRole.ASSISTANT }

internal fun replyNeedsAttention(
    route: AppRoute,
    selectedSessionId: String?,
    completedSessionId: String,
    appInForeground: Boolean,
): Boolean = !appInForeground || route != AppRoute.CHAT || selectedSessionId != completedSessionId

internal fun ChatMessage.recoverySignature(): String = buildString {
    append(createdAt)
    append('|')
    append(content.trim())
    images.forEach { append('|').append(it.source) }
}

internal fun findRecoveredAssistant(
    messages: List<ChatMessage>,
    submittedPrompt: String,
    baselineSignature: String,
): ChatMessage? {
    val visible = messages.visibleConversationMessages()
    val normalizedPrompt = submittedPrompt.trim()
    val submittedUserIndex = visible.indexOfLast { message ->
        message.role == MessageRole.USER &&
            normalizedPrompt.isNotBlank() &&
            message.content.trim().startsWith(normalizedPrompt)
    }
    val candidate = if (submittedUserIndex >= 0) {
        visible.drop(submittedUserIndex + 1).lastOrNull { it.role == MessageRole.ASSISTANT }
    } else {
        visible.lastOrNull { it.role == MessageRole.ASSISTANT }
    }
    return candidate?.takeIf {
        (it.content.isNotBlank() || it.images.isNotEmpty()) && it.recoverySignature() != baselineSignature
    }
}
