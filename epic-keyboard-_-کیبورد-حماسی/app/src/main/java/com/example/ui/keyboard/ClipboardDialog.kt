package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.SmartKeyboardApp
import com.example.data.ClipboardItem
import kotlinx.coroutines.launch

@Composable
fun ClipboardDialog(
    onPasteText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = SmartKeyboardApp.instance.clipboardRepository
    val pinnedList by repo.pinnedItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentList by repo.recentItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        SmartKeyboardApp.instance.syncSystemClipboard()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Pinned, 1: Recent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "کلیپ‌بورد هوشمند (Clipboard)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedTabIndex == 1 && recentList.isNotEmpty()) {
                    TextButton(onClick = { coroutineScope.launch { repo.clearRecent() } }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "پاکسازی اخیر",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پاکسازی", fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Tabs: Pinned vs Recent
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("سنجاق‌شده (${pinnedList.size}/20)", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("اخیر (${recentList.size}/20)", fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val currentList = if (selectedTabIndex == 0) pinnedList else recentList

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTabIndex == 0) "مورد سنجاق‌شده‌ای وجود ندارد (حداکثر ۲۰ مورد)"
                    else "مورد اخیری وجود ندارد (حداکثر ۲۰ مورد؛ با کپی یا برش در هر برنامه‌ای اضافه می‌شود)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentList, key = { it.id }) { item ->
                    ClipboardItemRow(
                        item = item,
                        onPaste = { onPasteText(item.text) },
                        onTogglePin = { coroutineScope.launch { repo.togglePin(item) } },
                        onDelete = { coroutineScope.launch { repo.delete(item.id) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClipboardItemRow(
    item: ClipboardItem,
    onPaste: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onPaste),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.text,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = if (item.isPinned) "حذف سنجاق" else "سنجاق کردن",
                    tint = if (item.isPinned) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
