package com.example.app.ui.verse

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.data.annotation.Attachment
import com.example.app.data.annotation.AttachmentType
import com.example.app.data.annotation.AnnotationWithAttachments
import com.example.app.data.annotation.PendingAttachment
import com.example.app.data.annotation.copyPdfToInternal
import com.example.app.data.crossref.CrossReference
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationBottomSheet(
    verseRef: String,
    annotationsWithAttachments: List<AnnotationWithAttachments>,
    pendingAttachments: List<PendingAttachment>,
    crossReferences: List<CrossReference>,
    onDismiss: () -> Unit,
    onSave: (content: String, existing: AnnotationWithAttachments?) -> Unit,
    onDeleteAnnotation: (id: Long) -> Unit,
    onDeleteAttachment: (Attachment) -> Unit,
    onDeletePending: (index: Int) -> Unit,
    onAddLink: (displayName: String, url: String) -> Unit,
    onAddPdf: (displayName: String, internalPath: String) -> Unit,
    onCrossRefClick: (bookId: Int, chapter: Int, verse: Int) -> Unit,
    getVerseText: suspend (bookId: Int, chapter: Int, verse: Int) -> String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingItem by remember { mutableStateOf<AnnotationWithAttachments?>(null) }
    var inputText by rememberSaveable { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }
    var pickedPdfUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(editingItem) {
        inputText = editingItem?.annotation?.content ?: ""
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pickedPdfUri = uri }

    if (showLinkDialog) {
        LinkDialog(
            onDismiss = { showLinkDialog = false },
            onAdd = { displayName, url ->
                onAddLink(displayName, url)
                showLinkDialog = false
            }
        )
    }

    pickedPdfUri?.let { uri ->
        PdfNameDialog(
            onDismiss = { pickedPdfUri = null },
            onAdd = { displayName ->
                scope.launch {
                    val path = copyPdfToInternal(context, uri, displayName)
                    onAddPdf(displayName, path)
                }
                pickedPdfUri = null
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = verseRef,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📝 주석") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📖 관련구절") }
                )
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> AnnotationTabContent(
                    annotationsWithAttachments = annotationsWithAttachments,
                    pendingAttachments = pendingAttachments,
                    editingItem = editingItem,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onEditingChange = { editingItem = it },
                    onSave = onSave,
                    onDeleteAnnotation = onDeleteAnnotation,
                    onDeleteAttachment = onDeleteAttachment,
                    onDeletePending = onDeletePending,
                    onShowLinkDialog = { showLinkDialog = true },
                    onPickPdf = { pdfLauncher.launch(arrayOf("application/pdf")) },
                    context = context
                )
                1 -> CrossRefTabContent(
                    crossReferences = crossReferences,
                    onCrossRefClick = onCrossRefClick,
                    getVerseText = getVerseText
                )
            }
        }
    }
}

@Composable
private fun AnnotationTabContent(
    annotationsWithAttachments: List<AnnotationWithAttachments>,
    pendingAttachments: List<PendingAttachment>,
    editingItem: AnnotationWithAttachments?,
    inputText: String,
    onInputChange: (String) -> Unit,
    onEditingChange: (AnnotationWithAttachments?) -> Unit,
    onSave: (content: String, existing: AnnotationWithAttachments?) -> Unit,
    onDeleteAnnotation: (id: Long) -> Unit,
    onDeleteAttachment: (Attachment) -> Unit,
    onDeletePending: (index: Int) -> Unit,
    onShowLinkDialog: () -> Unit,
    onPickPdf: () -> Unit,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (annotationsWithAttachments.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(annotationsWithAttachments, key = { it.annotation.id }) { item ->
                    AnnotationItemCard(
                        item = item,
                        isEditing = editingItem?.annotation?.id == item.annotation.id,
                        onEdit = { onEditingChange(item) },
                        onDeleteAnnotation = { onDeleteAnnotation(item.annotation.id) },
                        onDeleteAttachment = onDeleteAttachment,
                        onOpenLink = { openLink(context, it) },
                        onOpenPdf = { openPdf(context, it) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("주석을 입력하세요") },
            label = { Text(if (editingItem != null) "주석 수정" else "새 주석") },
            minLines = 3,
            maxLines = 6
        )

        if (pendingAttachments.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            pendingAttachments.forEachIndexed { index, pending ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${if (pending.type == AttachmentType.LINK) "🔗" else "📄"} ${pending.displayName}",
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onDeletePending(index) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "삭제", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row {
            TextButton(onClick = onShowLinkDialog) { Text("🔗 링크 추가") }
            Spacer(Modifier.width(4.dp))
            TextButton(onClick = onPickPdf) { Text("📄 PDF 첨부") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (editingItem != null) {
                TextButton(onClick = { onEditingChange(null); onInputChange("") }) { Text("취소") }
                Spacer(Modifier.width(8.dp))
            }
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSave(inputText.trim(), editingItem)
                        onInputChange("")
                        onEditingChange(null)
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Text(if (editingItem != null) "수정 저장" else "저장")
            }
        }
    }
}

@Composable
private fun CrossRefTabContent(
    crossReferences: List<CrossReference>,
    onCrossRefClick: (bookId: Int, chapter: Int, verse: Int) -> Unit,
    getVerseText: suspend (bookId: Int, chapter: Int, verse: Int) -> String?
) {
    if (crossReferences.isEmpty()) {
        Text(
            text = "이 절에 대한 관련구절이 없습니다.",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(crossReferences, key = { "${it.toBookId}.${it.toChapter}.${it.toVerseStart}" }) { cr ->
            CrossRefItem(
                crossReference = cr,
                onClick = { onCrossRefClick(cr.toBookId, cr.toChapter, cr.toVerseStart) },
                getVerseText = getVerseText
            )
        }
    }
}

@Composable
private fun CrossRefItem(
    crossReference: CrossReference,
    onClick: () -> Unit,
    getVerseText: suspend (bookId: Int, chapter: Int, verse: Int) -> String?
) {
    val verseText by produceState<String?>(
        initialValue = null,
        crossReference.toBookId, crossReference.toChapter, crossReference.toVerseStart
    ) {
        value = getVerseText(crossReference.toBookId, crossReference.toChapter, crossReference.toVerseStart)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = crossReference.toDisplayRef(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        if (verseText != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = verseText!!,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}

@Composable
private fun AnnotationItemCard(
    item: AnnotationWithAttachments,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onDeleteAnnotation: () -> Unit,
    onDeleteAttachment: (Attachment) -> Unit,
    onOpenLink: (String) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    val formatter = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
    }
    val ann = item.annotation

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
                    text = ann.content,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "수정", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeleteAnnotation, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(18.dp))
                    }
                }
            }

            item.attachments.forEach { att ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "${if (att.type == AttachmentType.LINK) "🔗" else "📄"} ${att.displayName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (att.type == AttachmentType.LINK) onOpenLink(att.data)
                                else onOpenPdf(att.data)
                            }
                    )
                    IconButton(
                        onClick = { onDeleteAttachment(att) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "첨부 삭제", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = buildString {
                    append(formatter.format(Date(ann.createdAt)))
                    if (ann.updatedAt != ann.createdAt) {
                        append("  수정: ${formatter.format(Date(ann.updatedAt))}")
                    }
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LinkDialog(onDismiss: () -> Unit, onAdd: (displayName: String, url: String) -> Unit) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("링크 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("표시 이름") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    placeholder = { Text("https://") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(displayName.ifBlank { url }, url) },
                enabled = url.isNotBlank()
            ) { Text("추가") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun PdfNameDialog(onDismiss: () -> Unit, onAdd: (displayName: String) -> Unit) {
    var displayName by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF 표시 이름") },
        text = {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("표시 이름") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(displayName.ifBlank { "첨부 PDF" }) },
            ) { Text("첨부") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
