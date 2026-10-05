package com.tik2wa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.tik2wa.feature.MainTab
import com.tik2wa.feature.Tik2WaViewModel
import coil.compose.AsyncImage
import com.tik2wa.domain.model.Sticker
import com.tik2wa.domain.model.SyncState
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF090B10)
private val Panel = Color(0xFF131722)
private val Panel2 = Color(0xFF191F2B)
private val Muted = Color(0xFF929AAA)
private val Cyan = Color(0xFF65E5E4)
private val Pink = Color(0xFFFF5B85)
private val Green = Color(0xFF56D6A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 11, 16)
        window.navigationBarColor = android.graphics.Color.rgb(9, 11, 16)
        setContent { Tik2WaApp() }
    }
}

@Composable
private fun Tik2WaApp() {
    val viewModel: Tik2WaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.uiState.collectAsState()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, secondary = Pink, background = Bg, surface = Panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF111622), Bg, Bg)))) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AnimatedVisibility(!state.settingsOpen, enter = fadeIn(tween(180)), exit = fadeOut(tween(120))) {
                        BottomNav(state.tab, onPage = viewModel::navigate)
                    }
                }
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    TopBar(onSettings = viewModel::toggleSettings, showSettings = !state.settingsOpen)
                    AnimatedContent(
                        targetState = if (state.settingsOpen) "settings" else state.tab.name,
                        transitionSpec = { (fadeIn(tween(220)) + slideInHorizontally { it / 14 }) togetherWith
                            (fadeOut(tween(120)) + slideOutHorizontally { -it / 18 }) },
                        label = "main_page"
                    ) { target ->
                        if (target == "settings") SettingsScreen(
                            state.autoSync, viewModel::setAutoSync,
                            state.notifications, viewModel::setNotifications,
                            state.darkAppearance, viewModel::setDarkAppearance,
                            onLogout = viewModel::logout
                        ) else when (MainTab.valueOf(target)) {
                            MainTab.Home -> HomeScreen(
                                onConnectTikTok = { viewModel.connectTikTok() },
                                onConnectWhatsApp = { viewModel.connectWhatsApp() },
                                onSettings = viewModel::openSettings
                            )
                            MainTab.Stickers -> StickersScreen(
                                stickers = state.stickers,
                                selectedIds = state.selectedStickerIds,
                                onToggle = viewModel::toggleSticker,
                                onSelectAll = viewModel::selectAllStickers,
                                onRefresh = { viewModel.refreshStickers() },
                                onAdd = { viewModel.addSticker() },
                                onAddAll = { viewModel.addAllStickers() }
                            )
                            MainTab.Sync -> SyncScreen(state.syncStep, state.syncProgress, onStart = { viewModel.playSyncPreview() })
                            MainTab.Profile -> ProfileScreen(
                                accountEmail = state.accountEmail,
                                authLoading = state.authLoading,
                                authMessage = state.authMessage,
                                onAuthenticate = { email, password, create -> viewModel.authenticate(email, password, create) },
                                onSignOut = { viewModel.signOutAppAccount() },
                                onSettings = viewModel::openSettings
                            )
                        }
                    }
                }
                AnimatedVisibility(state.feedback != null, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 94.dp), enter = fadeIn() + slideInHorizontally { it / 4 }, exit = fadeOut()) {
                    Card(
                        Modifier.padding(horizontal = 20.dp).clickable(onClick = viewModel::dismissFeedback),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262D3B)),
                        shape = RoundedCornerShape(18.dp)
                    ) { Text(state.feedback.orEmpty(), Modifier.padding(16.dp), color = Color.White, fontSize = 13.sp) }
                }
            }
            AmbientGlow(Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 8.dp))
        }
    }
}

@Composable
private fun TopBar(onSettings: () -> Unit, showSettings: Boolean) {
    Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 16.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("TIK", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
        Text("2", color = Cyan, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Text("WA", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        if (showSettings) Surface(onClick = onSettings, shape = RoundedCornerShape(14.dp), color = Panel2, modifier = Modifier.clip(RoundedCornerShape(14.dp))) {
            Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Settings, null, tint = Cyan, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Settings", color = Color.White, fontSize = 12.sp)
            }
        } else TextButton(onClick = onSettings) { Text("← Volver", color = Cyan) }
    }
}

@Composable
private fun HomeScreen(onConnectTikTok: () -> Unit, onConnectWhatsApp: () -> Unit, onSettings: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(18.dp))
        LogoTransfer(Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(16.dp))
        Text("Tus stickers,\nsin complicaciones.", fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.White)
        Spacer(Modifier.height(10.dp))
        Text("Tus stickers de TikTok en WhatsApp, sin complicaciones.", color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        AnimatedVisibility(visible, enter = fadeIn(tween(500, 160)) + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 3 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ConnectCard("TikTok", "Iniciar sesión en TikTok", "Conecta tu cuenta de TikTok", "♪", Pink, onConnectTikTok)
                ConnectCard("WhatsApp", "Vincular WhatsApp", "Prepara tus stickers para WhatsApp", "◉", Green, onConnectWhatsApp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("▣", color = Green, fontSize = 14.sp); Spacer(Modifier.width(8.dp))
            Text("Tus cuentas están seguras.", color = Color(0xFFDAE0EA), fontSize = 12.sp)
        }
        Text("Esta versión no solicita contraseñas ni tokens.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.weight(1f))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131923)), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0x203CE4D2)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CloudSync, null, tint = Cyan, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("Sincronización segura", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text("Tú eliges qué agregar. Nada se mueve.", color = Muted, fontSize = 11.sp) }
                Icon(Icons.Rounded.ArrowForward, null, tint = Muted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun LogoTransfer(modifier: Modifier = Modifier) {
    var pulse by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { pulse = !pulse; delay(1100) } }
    val scale by animateFloatAsState(if (pulse) 1.04f else 0.97f, animationSpec = tween(850, easing = FastOutSlowInEasing), label = "logo_pulse")
    Row(modifier.scale(scale).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        BrandMark("♪", Pink)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 15.dp)) {
            Text("•••", color = Cyan, letterSpacing = 4.sp, fontSize = 15.sp)
            Text("⇢", color = Cyan, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
        BrandMark("◉", Green)
    }
}

@Composable
private fun BrandMark(mark: String, color: Color) {
    Box(Modifier.size(58.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = .10f)).border(1.dp, color.copy(alpha = .4f), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
        Text(mark, color = color, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConnectCard(title: String, heading: String, detail: String, icon: String, accent: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .985f else 1f, animationSpec = spring(dampingRatio = .5f), label = "card_press")
    Card(
        modifier = Modifier.fillMaxWidth().scale(scale).clickable {
            pressed = true; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick(); pressed = false
        }.border(1.dp, accent.copy(alpha = .22f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xCC151A25))
    ) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(17.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Text(icon, color = accent, fontWeight = FontWeight.Bold, fontSize = 28.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), color = accent, letterSpacing = 1.5.sp, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp)); Text(heading, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(detail, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Icon(Icons.Rounded.ArrowForward, null, tint = accent, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun BottomNav(page: MainTab, onPage: (MainTab) -> Unit) {
    val activeIndex = MainTab.entries.indexOf(page)
    Surface(color = Color(0xF20D1119), tonalElevation = 2.dp) {
        Column {
            BoxWithConstraints(Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 14.dp)) {
                val itemWidth = maxWidth / MainTab.entries.size
                val indicatorOffset by animateDpAsState(
                    itemWidth * activeIndex.toFloat() + (itemWidth - 39.dp) / 2,
                    animationSpec = spring(dampingRatio = .78f, stiffness = 420f),
                    label = "nav_indicator"
                )
                Box(Modifier.offset(x = indicatorOffset).width(39.dp).height(2.dp).clip(CircleShape).background(Cyan))
            }
            NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, modifier = Modifier.height(69.dp)) {
                MainTab.entries.forEach { item ->
                    val selected = page == item
                    val icon = when (item) {
                        MainTab.Home -> Icons.Rounded.Home
                        MainTab.Stickers -> Icons.Rounded.StickyNote2
                        MainTab.Sync -> Icons.Rounded.Sync
                        MainTab.Profile -> Icons.Rounded.Person
                    }
                    NavigationBarItem(selected = selected, onClick = { onPage(item) }, icon = { Icon(icon, item.label, modifier = Modifier.size(20.dp)) }, label = { Text(item.label, fontSize = 10.sp) }, alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Cyan, selectedTextColor = Color.White, unselectedIconColor = Muted, unselectedTextColor = Muted, indicatorColor = Color(0x203CE4D2)))
                }
            }
        }
    }
}

@Composable
private fun StickersScreen(
    stickers: List<Sticker>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onRefresh: () -> Unit,
    onAdd: () -> Unit,
    onAddAll: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        ScreenHeading("Tus stickers", "Tu colección, lista para elegir.")
        Spacer(Modifier.height(18.dp))
        ProviderStatusCard("TikTok", "No conectado", Pink, "La lectura de Favoritos requiere una API oficial disponible.")
        Spacer(Modifier.height(16.dp))
        if (stickers.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 18.dp)) {
                    Box(Modifier.size(82.dp).clip(RoundedCornerShape(27.dp)).background(Color(0xFF171E2A)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.EmojiEmotions, null, tint = Cyan, modifier = Modifier.size(38.dp)) }
                    Spacer(Modifier.height(18.dp)); Text("Todavía no hay stickers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp)); Text("Cuando una integración oficial permita consultar tus Favoritos, aparecerán aquí para seleccionar visualmente.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 19.sp)
                    Spacer(Modifier.height(20.dp)); OutlinedButton(onClick = onRefresh, shape = RoundedCornerShape(15.dp)) { Text("Comprobar conexión") }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${selectedIds.size} seleccionados", color = Color.White, fontSize = 13.sp)
                TextButton(onClick = onSelectAll) { Text("Seleccionar todos", color = Cyan, fontSize = 12.sp) }
            }
            StickerGrid(stickers, selectedIds, onToggle, Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onAddAll, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Agregar todos", fontSize = 12.sp) }
                Button(onClick = onAdd, enabled = selectedIds.isNotEmpty(), modifier = Modifier.weight(1.35f).height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Bg)) {
                    Text("Agregar · ${selectedIds.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun StickerGrid(stickers: List<Sticker>, selectedIds: Set<String>, onTap: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(modifier = modifier, columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(stickers, key = { it.id }) { sticker ->
            val selected = sticker.id in selectedIds || sticker.syncState == SyncState.Synced
            StickerTile(sticker, selected) { if (sticker.syncState != SyncState.Synced) onTap(sticker.id) }
        }
    }
}

@Composable
private fun StickerTile(sticker: Sticker, selected: Boolean, onTap: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.03f else 1f, animationSpec = spring(dampingRatio = .5f), label = "sticker_scale")
    Card(Modifier.scale(scale).clickable(onClick = onTap), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Panel2)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(76.dp), contentAlignment = Alignment.Center) {
                if (sticker.previewUrl != null) {
                    AsyncImage(model = sticker.previewUrl, contentDescription = sticker.title, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                } else Text("✨", fontSize = 34.sp)
            }
            Text(if (sticker.syncState == SyncState.Synced) "✓" else if (selected) "✓" else "+", color = if (selected) Green else Cyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SyncScreen(step: Int, progress: Float, onStart: () -> Unit) {
    val states = listOf("Preparando…", "Convirtiendo…", "Optimizando…", "Agregando a WhatsApp…", "Completado ✓")
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        ScreenHeading("Sincronizar", "Una copia segura. Tu original se queda.")
        Spacer(Modifier.height(22.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { BrandMark("♪", Pink); Text("TikTok", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp)) }
                    Spacer(Modifier.weight(1f))
                    Text("⟶", color = Cyan, fontSize = 24.sp)
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { BrandMark("◉", Green); Text("WhatsApp", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp)) }
                }
                Spacer(Modifier.height(26.dp))
                listOf("😭", "😂", "🗿").forEachIndexed { index, emoji ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 27.sp)
                        Spacer(Modifier.weight(1f))
                        val traveling = step in 0..3 && index == step % 3
                        if (traveling) CircularProgressIndicator(Modifier.size(17.dp), color = Cyan, strokeWidth = 2.dp)
                        else Text("············", color = Color(0xFF384252), fontSize = 13.sp)
                        Spacer(Modifier.weight(1f)); Text(emoji, fontSize = 27.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                if (step >= 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(states[step.coerceIn(0, 4)], color = Color.White, fontSize = 13.sp)
                        Text(if (step == 4) "0 / 0" else "${(progress * 4).toInt()} / 4", color = Cyan, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Cyan, trackColor = Color(0xFF26303E))
                } else Text("Tus stickers sincronizados aparecerán aquí.", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171B25)), shape = RoundedCornerShape(18.dp)) {
            Text("La animación es una vista previa. La transferencia real requiere la integración oficial de origen y el flujo de packs de WhatsApp.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(15.dp))
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Bg)) {
            Text(if (step == 4) "Ver animación otra vez" else "Vista previa de sincronización", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsScreen(autoSync: Boolean, onAutoSync: (Boolean) -> Unit, notifications: Boolean, onNotifications: (Boolean) -> Unit, appearance: Boolean, onAppearance: (Boolean) -> Unit, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        ScreenHeading("Settings", "Preferencias y privacidad.")
        Spacer(Modifier.height(16.dp))
        SectionLabel("CUENTAS")
        SettingRow("TikTok", "No conectado · API oficial no disponible", "♪", Pink)
        SettingRow("WhatsApp", "Sin pack conectado", "◉", Green)
        Spacer(Modifier.height(14.dp)); SectionLabel("PREFERENCIAS")
        ToggleRow("Sincronización automática", "Solo al estar disponible", autoSync, onAutoSync)
        ToggleRow("Notificaciones", "Nuevos stickers compatibles", notifications, onNotifications)
        ToggleRow("Apariencia oscura", "Interfaz nocturna", appearance, onAppearance)
        Spacer(Modifier.height(14.dp)); SectionLabel("PRIVACIDAD")
        SettingRow("Datos almacenados", "Solo preferencias locales", "◈", Cyan)
        SettingRow("Privacidad", "Sin credenciales en esta versión", "⌑", Cyan)
        SettingRow("Información", "Tik2WA · versión 0.1.0", "ⓘ", Muted)
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("Cerrar sesión", color = Pink) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileScreen(
    accountEmail: String?,
    authLoading: Boolean,
    authMessage: String?,
    onAuthenticate: (String, String, Boolean) -> Unit,
    onSignOut: () -> Unit,
    onSettings: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        ScreenHeading("Tu perfil", "Tu cuenta de Tik2WA y conexiones.")
        Spacer(Modifier.height(18.dp))
        FirebaseAccountCard(accountEmail, authLoading, authMessage, onAuthenticate, onSignOut)
        Spacer(Modifier.height(16.dp))
        ProviderStatusCard("TikTok", "No conectado", Pink, "Acceso a Favoritos no disponible mediante API pública oficial.")
        Spacer(Modifier.height(12.dp))
        ProviderStatusCard("WhatsApp", "No conectado", Green, "La importación debe pasar por el flujo de pack oficial.")
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) { Icon(Icons.Rounded.Settings, null); Spacer(Modifier.width(8.dp)); Text("Abrir Settings") }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun FirebaseAccountCard(
    accountEmail: String?,
    loading: Boolean,
    message: String?,
    onAuthenticate: (String, String, Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var creatingAccount by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(Color(0x203CE4D2)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = Cyan, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Cuenta Tik2WA", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(if (accountEmail == null) "Firebase Authentication · correo" else "Sesión iniciada", color = Muted, fontSize = 11.sp)
                }
            }
            if (accountEmail != null) {
                Text(accountEmail, color = Cyan, fontSize = 13.sp)
                OutlinedButton(onClick = onSignOut, enabled = !loading, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Cerrar sesión") }
            } else {
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan, cursorColor = Cyan)
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !loading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan, cursorColor = Cyan)
                )
                if (message != null) Text(message, color = if (message.startsWith("No se pudo")) Pink else Cyan, fontSize = 12.sp)
                Button(
                    onClick = { onAuthenticate(email, password, creatingAccount) }, enabled = !loading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Bg)
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(19.dp), color = Bg, strokeWidth = 2.dp)
                    else Text(if (creatingAccount) "Crear cuenta" else "Iniciar sesión", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { creatingAccount = !creatingAccount }, enabled = !loading, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(if (creatingAccount) "Ya tengo cuenta · Iniciar sesión" else "¿Primera vez? Crear cuenta", color = Muted, fontSize = 12.sp)
                }
            }
            if (message != null && accountEmail != null) Text(message, color = Cyan, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProviderStatusCard(title: String, state: String, accent: Color, detail: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(detail, color = Muted, fontSize = 10.sp, lineHeight = 14.sp) }
            Text(state, color = accent, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, icon: String, accent: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Text(icon, color = accent, fontSize = 18.sp) }
        Spacer(Modifier.width(12.dp)); Column { Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(subtitle, color = Muted, fontSize = 10.sp) }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(subtitle, color = Muted, fontSize = 10.sp) }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SectionLabel(label: String) { Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, modifier = Modifier.padding(bottom = 5.dp)) }

@Composable
private fun ScreenHeading(title: String, subtitle: String) {
    Column(Modifier.padding(top = 16.dp)) { Text(title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
}

@Composable
private fun AmbientGlow(modifier: Modifier = Modifier) {
    Box(modifier.size(170.dp).alpha(.08f).background(Brush.radialGradient(listOf(Cyan, Color.Transparent)), CircleShape))
}

@Preview(showBackground = true, backgroundColor = 0xFF090B10)
@Composable
private fun Tik2WaHomePreview() {
    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, secondary = Pink, background = Bg, surface = Panel)) {
        HomeScreen(onConnectTikTok = {}, onConnectWhatsApp = {}, onSettings = {})
    }
}
