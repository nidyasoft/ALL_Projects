package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun harakatRows_haveEightKeysEach_andExpectedOutputs() {
    val topRow = com.example.ui.keyboard.KeyboardLayouts.HARAKAT_TOP_ROW
    val bottomRow = com.example.ui.keyboard.KeyboardLayouts.HARAKAT_BOTTOM_ROW

    assertEquals(8, topRow.size)
    assertEquals(8, bottomRow.size)

    // Verify bottom row reversed order matches user request
    val expectedBottomOutputs = listOf("\u064D", "\u064C", "\u064B", "\u0652", "\u0651", "\u0650", "\u064F", "\u064E")
    assertEquals(expectedBottomOutputs, bottomRow.map { it.output })
  }
}
