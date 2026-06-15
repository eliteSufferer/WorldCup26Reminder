package com.worldcup26.reminder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.worldcup26.reminder.domain.CountryFlags

/** A small flag for [teamName], or an empty slot for knockout placeholders. */
@Composable
fun FlagImage(teamName: String, height: Dp = 16.dp, modifier: Modifier = Modifier) {
    val url = CountryFlags.flagUrl(teamName)
    val flagModifier = modifier
        .height(height)
        .width(height * 1.5f)
        .clip(RoundedCornerShape(2.dp))
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = flagModifier,
        )
    } else {
        Box(flagModifier) // keep alignment stable when no flag is available
    }
}

/** Flag + team name on one line. Used in both the group list and the bracket. */
@Composable
fun RowScope.TeamLabel(
    teamName: String,
    bold: Boolean = false,
    trailingScore: Int? = null,
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FlagImage(teamName)
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
    if (trailingScore != null) {
        Text(
            text = trailingScore.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (bold) MaterialTheme.colorScheme.primary else Color.Unspecified,
        )
    }
}
