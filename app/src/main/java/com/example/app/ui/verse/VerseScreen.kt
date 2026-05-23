package com.example.app.ui.verse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.data.annotation.Annotation
import com.example.app.data.model.Verse
import com.example.app.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseScreen(
    viewModel: VerseViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val annotatedVerses by viewModel.annotatedVerses.collectAsState()
    val selectedVerse by viewModel.selectedVerse.collectAsState()
    val selectedVerseAnnotations by viewModel.selectedVerseAnnotations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (state is UiState.Success) {
                        val (bookName, chapter) = (state as UiState.Success).data
                        "$bookName ${chapter.number}장"
                    } else "본문"
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(s.message) }

            is UiState.Success -> {
                val (bookName, chapter) = s.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding
                ) {
                    items(chapter.verses, key = { it.number }) { verse ->
                        VerseItem(
                            verse = verse,
                            hasAnnotation = verse.number in annotatedVerses,
                            onTap = { viewModel.selectVerse(verse.number) }
                        )
                    }
                }

                if (selectedVerse != null) {
                    val verseRef = "$bookName ${chapter.number}:$selectedVerse"
                    AnnotationBottomSheet(
                        verseRef = verseRef,
                        annotations = selectedVerseAnnotations,
                        onDismiss = { viewModel.dismissBottomSheet() },
                        onSave = { content, existing ->
                            viewModel.saveAnnotation(selectedVerse!!, content, existing)
                        },
                        onDelete = { viewModel.deleteAnnotation(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseItem(
    verse: Verse,
    hasAnnotation: Boolean,
    onTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 주석 마커 (파란 점)
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasAnnotation) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
            )
            Spacer(Modifier.width(8.dp))

            // 절번호 (superscript-like: 작고 위쪽 정렬)
            Text(
                text = "${verse.number}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 1.dp, end = 6.dp),
                lineHeight = 14.sp
            )

            // 본문
            Text(
                text = verse.text,
                fontSize = 17.sp,
                lineHeight = 28.sp,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 30.dp), thickness = 0.5.dp)
    }
}
