package com.example.ui.settings

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.SmartKeyboardApp
import com.example.data.PersonalWordEntity
import com.example.settings.KeyboardLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDictionaryScreen(
    initialLanguage: KeyboardLanguage,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val repo = SmartKeyboardApp.instance.personalDictionaryRepository
    val engine = SmartKeyboardApp.instance.predictionEngine
    val coroutineScope = rememberCoroutineScope()

    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var searchQuery by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var editingEntity by remember { mutableStateOf<PersonalWordEntity?>(null) }
    var isClearConfirmOpen by remember { mutableStateOf(false) }

    val allWords by repo.allWordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val languageCounts = remember(allWords) {
        allWords.groupingBy { it.languageId }.eachCount()
    }
    val filteredWords = remember(allWords, selectedLanguage, searchQuery) {
        allWords.filter { entity ->
            entity.languageId == selectedLanguage.id &&
                (searchQuery.isBlank() || entity.word.contains(searchQuery.trim(), ignoreCase = true))
        }
    }

    val csvFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        it.readText()
                    } ?: ""
                    if (content.isNotBlank()) {
                        val res = repo.importCsv(selectedLanguage, content)
                        engine.reloadPersonalDictionarySync()
                        res
                    } else null
                }
                statusMessage = if (result != null) {
                    "فایل CSV وارد شد: ${result.addedCount} کلمه جدید، ${result.updatedCount} به‌روزرسانی (${result.skippedCount} مورد نادیده گرفته شد)"
                } else {
                    "فایل انتخاب‌شده خالی یا غیرقابل خواندن است."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("دیکشنری شخصی هر زبان", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "افزودن، ویرایش، حذف و درون‌ریزی لیست CSV",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (filteredWords.isNotEmpty()) {
                        IconButton(onClick = { isClearConfirmOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "حذف همه کلمات زبان فعلی",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                modifier = Modifier.testTag("add_personal_word_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن کلمه جدید")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Language selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeyboardLanguage.entries.forEach { lang ->
                    val count = languageCounts[lang.id] ?: 0
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = {
                            Text(
                                text = "${lang.nativeName} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // CSV Import + Sample CSV Testing Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "افزودن گروهی با فایل CSV",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "فرمت پشتیبانی‌شده: یک ستون (کلمه) یا چند ستون (word,language,frequency مثل: هوشمندسازی,fa,15)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                csvFilePickerLauncher.launch(
                                    arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*")
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_csv_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ورود فایل CSV", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        val sample = context.assets.open("sample_dictionary.csv")
                                            .bufferedReader(Charsets.UTF_8)
                                            .use { it.readText() }
                                        val res = repo.importCsv(selectedLanguage, sample)
                                        engine.reloadPersonalDictionarySync()
                                        res
                                    }
                                    statusMessage = "فایل نمونه CSV آزمایشی با موفقیت وارد شد: ${result.addedCount} کلمه جدید و ${result.updatedCount} به‌روزرسانی در همه زبان‌ها."
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_sample_csv_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تست نمونه CSV", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val file = withContext(Dispatchers.IO) {
                                    val outFile = File(context.cacheDir, "sample_dictionary.csv")
                                    context.assets.open("sample_dictionary.csv").use { input ->
                                        outFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    outFile
                                }
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "نمونه فایل CSV دیکشنری کیبورد حماسی")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "ذخیره یا ارسال فایل نمونه CSV")
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دریافت / مشاهده فایل نمونه CSV پایه (sample_dictionary.csv)", fontSize = 11.sp)
                    }
                }
            }

            if (statusMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMessage!!,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { statusMessage = null }) {
                            Text("بستن", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Search box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("جستجو در کلمات ${selectedLanguage.nativeName}...", fontSize = 13.sp) },
                shape = RoundedCornerShape(10.dp)
            )

            // Words list
            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "هنوز کلمه‌ای برای ${selectedLanguage.nativeName} ثبت نشده است.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "می‌توانید حین تایپ روی نوار پیشنهاد، با دکمه + و یا دکمه افزودن بالا اضافه کنید.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredWords, key = { it.id }) { entity ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entity.word,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "اولویت کاربرد: ${entity.frequency}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { editingEntity = entity },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "اصلاح کلمه",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            repo.deleteWord(entity.id)
                                            engine.reloadPersonalDictionarySync()
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف کلمه",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add word dialog
    if (isAddDialogOpen) {
        var newWordText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isAddDialogOpen = false },
            title = { Text("افزودن کلمه جدید (${selectedLanguage.nativeName})", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newWordText,
                    onValueChange = { newWordText = it },
                    singleLine = true,
                    label = { Text("کلمه یا عبارت جدید") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newWordText.trim()
                        if (clean.length >= 2) {
                            coroutineScope.launch {
                                repo.addOrBoostWord(selectedLanguage, clean, 12)
                                engine.reloadPersonalDictionarySync()
                            }
                            isAddDialogOpen = false
                        }
                    }
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddDialogOpen = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Edit word dialog
    val entityToEdit = editingEntity
    if (entityToEdit != null) {
        var editedWord by remember(entityToEdit.id) { mutableStateOf(entityToEdit.word) }
        var editedLang by remember(entityToEdit.id) {
            mutableStateOf(KeyboardLanguage.fromId(entityToEdit.languageId))
        }
        AlertDialog(
            onDismissRequest = { editingEntity = null },
            title = { Text("اصلاح کلمه شخصی", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedWord,
                        onValueChange = { editedWord = it },
                        singleLine = true,
                        label = { Text("متن اصلاح‌شده") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("زبان:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KeyboardLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = editedLang == lang,
                                onClick = { editedLang = lang },
                                label = { Text(lang.nativeName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = editedWord.trim()
                        if (clean.length >= 2) {
                            coroutineScope.launch {
                                repo.updateWord(entityToEdit.id, clean, editedLang)
                                engine.reloadPersonalDictionarySync()
                            }
                            editingEntity = null
                        }
                    }
                ) {
                    Text("ثبت اصلاح")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntity = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Clear all confirmation
    if (isClearConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isClearConfirmOpen = false },
            title = { Text("حذف همه کلمات ${selectedLanguage.nativeName}", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text("آیا مطمئن هستید که می‌خواهید تمام کلمات ثبت‌شده در دیکشنری شخصی این زبان را حذف کنید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repo.clearLanguage(selectedLanguage)
                            engine.reloadPersonalDictionarySync()
                        }
                        isClearConfirmOpen = false
                    }
                ) {
                    Text("حذف همه")
                }
            },
            dismissButton = {
                TextButton(onClick = { isClearConfirmOpen = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
