package com.dangxuanthong.projectcreator.ui.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dangxuanthong.projectcreator.ui.theme.ProjectCreatorTheme

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(start = 8.dp))
    }
}

@Preview
@Composable
private fun SectionHeaderPreview() {
    ProjectCreatorTheme {
        SectionHeader("Preview header")
    }
}
