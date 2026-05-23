package com.example.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.data.search.BOOK_ALIASES
import com.example.app.data.search.Reference
import com.example.app.data.search.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToVerse: (bookId: Int, chapter: Int, verse: Int?) -> Unit,
    onBack: () -> Unit
) {
    val query by viewModel.query.collectAsState()
    val referenceMatch by viewModel.referenceMatch.collectAsState()
    val textResults by viewModel.textResults.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("책명, 구절, 본문 검색") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding
        ) {
            referenceMatch?.let { ref ->
                item {
                    ReferenceHintCard(
                        reference = ref,
                        onClick = { onNavigateToVerse(ref.bookId, ref.chapter, ref.verse) }
                    )
                }
            }

            if (textResults.isNotEmpty()) {
                item {
                    Text(
                        text = "본문 검색 결과 (${textResults.size}개)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(textResults, key = { "${it.bookId}_${it.chapter}_${it.verse}" }) { result ->
                    SearchResultItem(
                        result = result,
                        query = query.trim(),
                        onClick = { onNavigateToVerse(result.bookId, result.chapter, result.verse) }
                    )
                }
            } else if (query.trim().length >= 2 && referenceMatch == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("검색 결과가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceHintCard(reference: Reference, onClick: () -> Unit) {
    val bookName = BOOK_ALIASES[reference.bookId]?.firstOrNull() ?: ""
    val refText = buildString {
        append("$bookName ${reference.chapter}장")
        if (reference.verse != null) append(" ${reference.verse}절")
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "→  $refText 로 이동",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    query: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "${result.bookNameKor} ${result.chapter}:${result.verse}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = highlightText(result.text, query),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
}

private fun highlightText(text: String, query: String): AnnotatedString = buildAnnotatedString {
    if (query.isEmpty()) { append(text); return@buildAnnotatedString }
    val lower = text.lowercase()
    val queryLower = query.lowercase()
    var pos = 0
    while (pos < text.length) {
        val idx = lower.indexOf(queryLower, pos)
        if (idx < 0) { append(text.substring(pos)); break }
        append(text.substring(pos, idx))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(idx, idx + query.length))
        }
        pos = idx + query.length
    }
}
