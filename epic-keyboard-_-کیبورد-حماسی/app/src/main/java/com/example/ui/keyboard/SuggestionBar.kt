package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuggestionBar(
    suggestions: List<String>,
    unknownWord: String? = null,
    onSuggestionClick: (String) -> Unit,
    onAddPersonalWord: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cleanUnknown = unknownWord?.trim()?.takeIf { it.length >= 2 }
    val hasAddChip = cleanUnknown != null && onAddPersonalWord != null

    val maxSuggestionSlots = if (hasAddChip) 3 else 4
    val candidates = rememberPaddedSuggestions(suggestions, maxSuggestionSlots)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasAddChip && cleanUnknown != null && onAddPersonalWord != null) {
            Row(
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onAddPersonalWord(cleanUnknown) }
                    .padding(horizontal = 6.dp)
                    .testTag("suggestion_add_personal"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "افزودن به دیکشنری شخصی",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = cleanUnknown,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }

        candidates.forEachIndexed { index, candidate ->
            val hasWord = candidate.isNotEmpty()
            val isTopChoice = index == 0 && hasWord && !hasAddChip

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (hasWord) {
                            Modifier
                                .background(
                                    if (isTopChoice) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )
                                .clickable { onSuggestionClick(candidate) }
                                .testTag("suggestion_$index")
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hasWord) {
                    Text(
                        text = candidate,
                        fontSize = 15.sp,
                        fontWeight = if (isTopChoice) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTopChoice) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (index < candidates.size - 1) {
                VerticalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun rememberPaddedSuggestions(raw: List<String>, count: Int): List<String> {
    val list = raw.take(count).toMutableList()
    while (list.size < count) {
        list.add("")
    }
    return list
}
