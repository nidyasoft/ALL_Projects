package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ClipboardRepository
import com.example.data.PersonalDictionaryRepository
import com.example.nlp.KeyHitRect
import com.example.nlp.PredictionEngine
import com.example.nlp.SpatialTapDecoder
import com.example.settings.KeyboardLanguage
import com.example.ui.keyboard.KeyboardLayouts
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Epic Keyboard / کیبورد حماسی", appName)
    }

    @Test
    fun `typo correction suggests proper persian word`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = PredictionEngine(context)

        val suggestions = engine.getSuggestions("کارجانه", "", KeyboardLanguage.PERSIAN)
        assertTrue("Suggestions should contain 'کارخانه', got: $suggestions", suggestions.contains("کارخانه"))
    }

    @Test
    fun `long phrase context and instant mid word prediction work without space`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = PredictionEngine(context)

        // Multi-word sentence next-word prediction
        val nextWord = engine.getSuggestions("", "امیدوارم حال شما ", KeyboardLanguage.PERSIAN)
        assertTrue("Expected 'خوب' in next-word predictions, got: $nextWord", nextWord.contains("خوب"))

        // Instant mid-word completion without space using preceding sentence context
        val midWordFa = engine.getSuggestions("خ", "امیدوارم حال شما ", KeyboardLanguage.PERSIAN)
        assertEquals("خوب", midWordFa.firstOrNull())

        val midWordEn = engine.getSuggestions("m", "thank you very ", KeyboardLanguage.ENGLISH)
        assertEquals("much", midWordEn.firstOrNull())
    }

    @Test
    fun `gboard spatial decoder resolves gap taps and fat finger edge hits`() {
        val rects = listOf(
            KeyHitRect("r1_c6", "ت", left = 100f, top = 100f, right = 196f, bottom = 200f),
            KeyHitRect("r1_c5", "ا", left = 200f, top = 100f, right = 296f, bottom = 200f)
        )
        val nextProbs = mapOf('ا' to 0.75f, 'ت' to 0.01f)

        // 1. Tap lands strictly in the gap (x = 198f) between 'ت' and 'ا'
        val gapResult = SpatialTapDecoder.decodeTap(
            tapX = 198f,
            tapY = 150f,
            directKeyId = null,
            directKeyChar = null,
            keyRects = rects,
            nextCharProbs = nextProbs,
            hasContextOrWord = true,
            smartTouchEnabled = true
        )
        assertEquals("ا", gapResult)

        // 2. Fat-finger tap lands just inside 'ت' right edge (x = 190f), very close to 'ا'
        val edgeResult = SpatialTapDecoder.decodeTap(
            tapX = 190f,
            tapY = 150f,
            directKeyId = "r1_c6",
            directKeyChar = "ت",
            keyRects = rects,
            nextCharProbs = nextProbs,
            hasContextOrWord = true,
            smartTouchEnabled = true
        )
        assertEquals("ا", edgeResult)
    }

    @Test
    fun `symbol rows use latin numbers for latin languages and persian for rtl`() {
        val enSymbols = KeyboardLayouts.getSymbolRows(isRtl = false)
        assertEquals("1", enSymbols[0][0].label)

        val faSymbols = KeyboardLayouts.getSymbolRows(isRtl = true)
        assertEquals("۱", faSymbols[0][0].label)
    }

    @Test
    fun `clipboard retains 20 recent and 20 pinned items and personal dictionary imports sample csv`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val clipRepo = ClipboardRepository(db.clipboardDao())
        for (i in 1..25) {
            clipRepo.addClip("recent_$i", pinImmediately = false)
        }
        val recent = clipRepo.recentItems.first()
        assertEquals(20, recent.size)

        for (i in 1..22) {
            clipRepo.addClip("pinned_$i", pinImmediately = true)
        }
        val pinned = clipRepo.pinnedItems.first()
        assertEquals(20, pinned.size)

        // Personal dictionary + bundled sample CSV test
        val dictRepo = PersonalDictionaryRepository(db.personalDictionaryDao())
        val engine = PredictionEngine(context, dictRepo)

        val sampleCsv = context.assets.open("sample_dictionary.csv").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val importRes = dictRepo.importCsv(KeyboardLanguage.PERSIAN, sampleCsv)
        assertTrue("Expected imported words > 10, got ${importRes.addedCount}", importRes.addedCount >= 15)

        engine.reloadPersonalDictionarySync()
        assertTrue(engine.isKnownWord("هوشمندسازی", KeyboardLanguage.PERSIAN))
        assertFalse(engine.isKnownWord("واژه‌ناشناخته‌تست", KeyboardLanguage.PERSIAN))

        val customSuggestions = engine.getSuggestions("هوشم", "", KeyboardLanguage.PERSIAN)
        assertTrue(customSuggestions.contains("هوشمندسازی"))

        db.close()
    }
}
