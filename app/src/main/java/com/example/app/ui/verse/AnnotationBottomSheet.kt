package com.example.app.ui.verse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.data.annotation.Annotation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationBottomSheet(
    verseRef: String,
    annotations: List<Annotation>,
    onDismiss: () -> Unit,
    onSave: (content: String, existing: Annotation?) -> Unit,
    onDelete: (id: Long) -> Unit
) {
    var editingAnnotation by remember { mutableStateOf<Annotation?>(null) }
    var inputText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(editingAnnotation) {
        inputText = editingAnnotation?.content ?: ""
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = verseRef,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (annotations.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(annotations, key = { it.id }) { ann ->
                        AnnotationItem(
                            annotation = ann,
                            isEditing = editingAnnotation?.id == ann.id,
                            onEdit = { editingAnnotation = ann },
                            onDelete = { onDelete(ann.id) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("주석을 입력하세요") },
                minLines = 3,
                maxLines = 6,
                label = { Text(if (editingAnnotation != null) "주석 수정" else "새 주석") }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editingAnnotation != null) {
                    TextButton(onClick = {
                        editingAnnotation = null
                        inputText = ""
                    }) { Text("취소") }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSave(inputText.trim(), editingAnnotation)
                            inputText = ""
                            editingAnnotation = null
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Text(if (editingAnnotation != null) "수정 저장" else "저장")
                }
            }
        }
    }
}

@Composable
private fun AnnotationItem(
    annotation: Annotation,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = annotation.content,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "수정", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = buildString {
                    append(formatter.format(Date(annotation.createdAt)))
                    if (annotation.updatedAt != annotation.createdAt) {
                        append("  수정: ${formatter.format(Date(annotation.updatedAt))}")
                    }
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
