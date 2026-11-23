package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calendar.TripleCalendarHelper
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ساعت هشدار اپیک", appName)
  }

  @Test
  fun `triple calendar conversion is accurate`() {
    val date = TripleCalendarHelper.calculateTripleDate(2026, 10, 15, 5, AppLanguage.PERSIAN)
    assertEquals(1405, date.solarYear)
    assertEquals(7, date.solarMonth) // Mehr
    assertEquals(23, date.solarDay)
  }

  @Test
  fun `persian digit formatting`() {
    val formatted = Strings.formatNumber("12:34", AppLanguage.PERSIAN)
    assertEquals("۱۲:۳۴", formatted)
  }

  @Test
  fun `launch MainActivity without crash`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    assertNotNull(controller.get())
  }
}
