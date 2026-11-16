package com.hypergpt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HyperGPTApp(
            onStartAgent = { ContextCompat.startForegroundService(this, Intent(this, AgentService::class.java)) },
            onStopAgent = { stopService(Intent(this, AgentService::class.java)) },
            onOpenTermux = { launchPackage("com.termux") },
            onOpenX11 = { launchPackage("com.termux.x11") },
            onStoreKey = { name, value -> SecureVault(this).put(name, value) }
        ) }
    }

    private fun launchPackage(packageName: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) startActivity(launch)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperGPTApp(
    onStartAgent: () -> Unit,
    onStopAgent: () -> Unit,
    onOpenTermux: () -> Unit,
    onOpenX11: () -> Unit,
    onStoreKey: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf("Overview") }
    var keyName by remember { mutableStateOf("") }
    var keyValue by remember { mutableStateOf("") }
    var keyMessage by remember { mutableStateOf("") }
    var folderUri by remember { mutableStateOf<Uri?>(null) }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        folderUri = it
    }
    val pages = listOf("Overview", "Workspace", "Terminal", "X11 Desktop", "API Keys", "Abilities", "Integrations", "MCP & API", "Approvals", "Settings")
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF8B9CFF),
        secondary = Color(0xFF65D6C4),
        background = Color(0xFF0B1020),
        surface = Color(0xFF151D30)
    )) {
        Scaffold(
            containerColor = Color(0xFF0B1020),
            topBar = {
                TopAppBar(
                    title = { Column {
                        Text("HyperGPT", fontWeight = FontWeight.Bold)
                        Text("PERSONAL AI WORKSPACE", fontSize = 10.sp, color = Color.LightGray, letterSpacing = 2.sp)
                    } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1020), titleContentColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF11192A)) {
                    listOf("Overview", "Workspace", "Terminal", "Abilities", "Settings").forEach { page ->
                        NavigationBarItem(
                            selected = selected == page,
                            onClick = { selected = page },
                            icon = { Icon(
                                when(page) {
                                    "Overview" -> Icons.Default.Dashboard
                                    "Workspace" -> Icons.Default.Folder
                                    "Terminal" -> Icons.Default.Terminal
                                    "Abilities" -> Icons.Default.Extension
                                    else -> Icons.Default.Settings
                                }, contentDescription = page
                            ) },
                            label = { Text(page, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                if (selected !in listOf("Overview", "Workspace", "Terminal", "Abilities", "Settings")) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        pages.drop(5).forEach { p ->
                            FilterChip(selected == p, { selected = p }, label = { Text(p, fontSize = 10.sp) })
                        }
                    }
                }
                when(selected) {
                    "Overview" -> Overview(onStartAgent, onStopAgent, onOpenTermux, onOpenX11)
                    "Workspace" -> Workspace(folderUri, { folderPicker.launch(null) })
                    "Terminal" -> TerminalPage(onOpenTermux)
                    "X11 Desktop" -> X11Page(onOpenX11)
                    "API Keys" -> ApiKeysPage(keyName, { keyName = it }, keyValue, { keyValue = it }, keyMessage, {
                        if (keyName.isNotBlank() && keyValue.isNotBlank()) {
                            onStoreKey(keyName.trim(), keyValue)
                            keyValue = ""; keyMessage = "Saved in Android Keystore-backed encrypted storage."
                        } else keyMessage = "Enter a provider name and key."
                    })
                    "Abilities" -> AbilitiesPage()
                    "Integrations" -> IntegrationsPage()
                    "MCP & API" -> McpPage()
                    "Approvals" -> ApprovalsPage()
                    else -> SettingsPage()
                }
            }
        }
    }
}

@Composable
fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}
@Composable
fun Panel(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit = {}) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D30)), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            if (subtitle != null) Text(subtitle, color = Color(0xFFADB9D0), fontSize = 13.sp)
            content()
        }
    }
}
@Composable
fun Overview(start: () -> Unit, stop: () -> Unit, termux: () -> Unit, x11: () -> Unit) {
    PageColumn {
        Text("Your agent command center", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Local-first workspace • Android 14+", color = Color(0xFFADB9D0))
        Panel("Runtime status", "Local bridge: Not paired • Cloud agent: Not configured") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = start) { Text("Start service") }
                OutlinedButton(onClick = stop) { Text("Stop") }
            }
        }
        Panel("Your tablet workspace", "Choose a folder to grant HyperGPT access.") {
            Text("Storage permission is folder-scoped, not unrestricted.", color = Color(0xFFADB9D0))
        }
        Panel("Quick launch") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = termux) { Text("Open Termux") }
                OutlinedButton(onClick = x11) { Text("Open X11") }
            }
        }
        Panel("Security", "Sensitive actions require approval. No root access or hidden background activity.")
    }
}
@Composable
fun Workspace(uri: Uri?, choose: () -> Unit) {
    PageColumn {
        Text("Workspace storage", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Tablet storage", uri?.toString() ?: "No folder selected") {
            Button(onClick = choose) { Text("Choose workspace folder") }
            Text("Android's Storage Access Framework grants access only to the folder you select.", color = Color(0xFFADB9D0))
        }
        Panel("Cloud workspace", "Not connected • backend required")
        Panel("Files", "File listing and upload/download operations will be enabled after the local bridge and API are connected.")
    }
}
@Composable
fun TerminalPage(open: () -> Unit) {
    PageColumn {
        Text("Terminal", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Termux connection", "Uses your separately installed Termux app. Configure Termux to allow approved external RUN_COMMAND calls before enabling command execution.") {
            Button(onClick = open) { Text("Open installed Termux") }
        }
        Panel("Command session", "Not connected • commands are not executed by this prototype.")
    }
}
@Composable
fun X11Page(open: () -> Unit) {
    PageColumn {
        Text("X11 Desktop", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Display bridge", "Not connected • a live display requires a separately configured Termux:X11 session and a supported screen transport.") {
            Button(onClick = open) { Text("Open installed Termux:X11") }
        }
        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF050812), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Text("Desktop stream will appear here\nNo live stream connected", color = Color(0xFFADB9D0))
        }
    }
}
@Composable
fun ApiKeysPage(name: String, setName: (String)->Unit, value: String, setValue: (String)->Unit, message: String, save: ()->Unit) {
    PageColumn {
        Text("Credential vault", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Add API key", "Keys are encrypted with an Android Keystore-managed key on this device.") {
            OutlinedTextField(name, setName, label = { Text("Provider / label") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value, setValue, label = { Text("API key") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = save) { Text("Encrypt and save") }
            if (message.isNotBlank()) Text(message, color = Color(0xFF65D6C4))
        }
        Panel("Security note", "Device-side storage does not automatically make a key available to a cloud backend. Use short-lived tokens or a server-side encrypted vault when syncing.")
    }
}
@Composable
fun AbilitiesPage() {
    PageColumn {
        Text("Abilities", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Ability builder", "Create a tool with a name, description, input schema, permission scopes and approval policy. Runtime execution is not enabled in this prototype.")
        Panel("Starter abilities", "• Inspect project files\n• Run approved test suite\n• Search connected documentation\n• Generate a code patch\n• Request a browser task")
    }
}
@Composable
fun IntegrationsPage() {
    PageColumn {
        Text("Integrations", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        listOf("GitHub", "Lovable", "Coding environments", "Browser / web tools", "Custom MCP servers", "REST / OpenAPI").forEach {
            Panel(it, "Not connected • add OAuth or API credentials in a future connector setup.")
        }
    }
}
@Composable
fun McpPage() {
    PageColumn {
        Text("MCP & API", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("MCP endpoint", "Not deployed • configure the backend URL and authentication before connecting ChatGPT.")
        Panel("Tool registry", "Tools will be exposed with JSON input schemas and explicit permission scopes.")
        Panel("GPT Actions", "An OpenAPI contract is included in the project under integrations/openapi.yaml.")
    }
}
@Composable
fun ApprovalsPage() {
    PageColumn {
        Text("Approvals & audit", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Approval queue", "No pending actions.")
        Panel("Policy", "Require confirmation for shell commands, file deletion, external publishing, credential use and network actions.")
    }
}
@Composable
fun SettingsPage() {
    PageColumn {
        Text("Settings", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Panel("Device pairing", "Pair a remote MCP/API backend using a one-time code. Device identity and scoped permissions will be added in the next milestone.")
        Panel("Background service", "Android foreground service with persistent notification. Android may still restrict startup and background activity based on OS and battery settings.")
        Panel("Permissions", "Request permissions only when a feature needs them. HyperGPT does not request unrelated permissions.")
    }
}
