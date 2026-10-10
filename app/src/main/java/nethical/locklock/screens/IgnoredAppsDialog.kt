package nethical.locklock.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nethical.locklock.data.AppInfo
import nethical.locklock.services.AppLockerInfo
import nethical.locklock.utils.reloadApps

/**
 * Lista de apps que LockLock debe ignorar (paneles flotantes como
 * Volume Styles o Material Shade). Se guarda en SharedPreferences "ignored_apps".
 */
@Composable
fun IgnoredAppsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sp = remember { context.getSharedPreferences("ignored_apps", Context.MODE_PRIVATE) }
    var appList by remember { mutableStateOf(emptyList<AppInfo>()) }
    val ignoredApps = remember { mutableStateListOf<String>() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sp.getStringSet("ignored_apps", emptySet<String>())?.forEach { ignoredApps.add(it) }
        val result = reloadApps(context.packageManager)
        if (result.isSuccess) {
            appList = result.getOrDefault(emptyList())
        }
    }

    val filteredList = remember(appList, searchQuery) {
        if (searchQuery.isBlank()) {
            appList
        } else {
            appList.filter { app ->
                app.name.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ignored apps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Apps marked here won't trigger a re-lock. Use it for volume panels, notification shades and other floating overlays.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search apps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList.sortedBy { it.name }) { app ->
                        AppSelectionItem(
                            app = app,
                            selectedApps = ignoredApps,
                            onToggle = { packageName ->
                                if (ignoredApps.contains(packageName)) {
                                    ignoredApps.remove(packageName)
                                } else {
                                    ignoredApps.add(packageName)
                                }
                                AppLockerInfo.ignoredApps = ignoredApps.toHashSet()
                                sp.edit()
                                    .putStringSet("ignored_apps", ignoredApps.toSet())
                                    .commit()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
