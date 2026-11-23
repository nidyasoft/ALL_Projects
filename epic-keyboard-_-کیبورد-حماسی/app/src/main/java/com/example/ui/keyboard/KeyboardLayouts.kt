package com.example.ui.keyboard

import com.example.settings.KeyboardLanguage

enum class KeyType {
    CHAR,
    HARAKAT,
    BACKSPACE,
    ENTER,
    SPACE,
    SHIFT,
    LANGUAGE_TOGGLE,
    SYMBOLS_TOGGLE,
    NUMPAD_TOGGLE,
    EMOJI_TOGGLE,
    HALF_SPACE,
    DEL,
    HOME,
    END,
    HOME_ALL,
    END_ALL,
    COPY,
    COPY_PIN,
    PASTE,
    CUT,
    SELECT_ALL,
    CLIPBOARD_DIALOG,
    UNDO
}

data class KeyModel(
    val label: String,
    val type: KeyType = KeyType.CHAR,
    val output: String = label,
    val secondary: String? = null,
    val variants: List<String> = emptyList(),
    val weight: Float = 1.0f
)

object KeyboardLayouts {

    // 2 Rows of 8 Harakat / Diacritics keys with Tatweel base (ـ) for perfect anchor placement and zero clipping
    val HARAKAT_TOP_ROW = listOf(
        KeyModel(label = "ـٓ", output = "\u0653", type = KeyType.HARAKAT), // Maddah
        KeyModel(label = "ـٔ", output = "\u0654", type = KeyType.HARAKAT), // Hamza above
        KeyModel(label = "ـ", output = "\u0640"),                           // Tatweel
        KeyModel(label = "ـٕ", output = "\u0655", type = KeyType.HARAKAT), // Hamza below
        KeyModel(label = "ـٰ", output = "\u0670", type = KeyType.HARAKAT), // Dagger alif
        KeyModel(label = "«", output = "«"),                                // Quote open
        KeyModel(label = "»", output = "»"),                                // Quote close
        KeyModel(label = "٫", output = "\u066B")                            // Persian decimal separator
    )

    val HARAKAT_BOTTOM_ROW = listOf(
        KeyModel(label = "ـٍ", output = "\u064D", type = KeyType.HARAKAT), // Tanwin Kasr
        KeyModel(label = "ـٌ", output = "\u064C", type = KeyType.HARAKAT), // Tanwin Damm
        KeyModel(label = "ـً", output = "\u064B", type = KeyType.HARAKAT), // Tanwin Fath
        KeyModel(label = "ـْ", output = "\u0652", type = KeyType.HARAKAT), // Sukun
        KeyModel(label = "ـّ", output = "\u0651", type = KeyType.HARAKAT), // Tashdid
        KeyModel(label = "ـِ", output = "\u0650", type = KeyType.HARAKAT), // Kasra
        KeyModel(label = "ـُ", output = "\u064F", type = KeyType.HARAKAT), // Damma
        KeyModel(label = "ـَ", output = "\u064E", type = KeyType.HARAKAT)  // Fatha
    )

    // Number rows
    val PERSIAN_NUMBER_ROW = listOf(
        KeyModel("۱", variants = listOf("1", "!", "١")),
        KeyModel("۲", variants = listOf("2", "@", "٢")),
        KeyModel("۳", variants = listOf("3", "#", "٣")),
        KeyModel("۴", variants = listOf("4", "$", "٤")),
        KeyModel("۵", variants = listOf("5", "%", "٥")),
        KeyModel("۶", variants = listOf("6", "^", "٦")),
        KeyModel("۷", variants = listOf("7", "&", "٧")),
        KeyModel("۸", variants = listOf("8", "*", "٨")),
        KeyModel("۹", variants = listOf("9", "(", "٩")),
        KeyModel("۰", variants = listOf("0", ")", "٠"))
    )

    val LATIN_NUMBER_ROW = listOf(
        KeyModel("1", variants = listOf("۱", "!", "¹", "½")),
        KeyModel("2", variants = listOf("۲", "@", "²", "⅓")),
        KeyModel("3", variants = listOf("۳", "#", "³", "¼")),
        KeyModel("4", variants = listOf("۴", "$", "⁴")),
        KeyModel("5", variants = listOf("۵", "%", "⅝")),
        KeyModel("6", variants = listOf("۶", "^")),
        KeyModel("7", variants = listOf("۷", "&", "⅞")),
        KeyModel("8", variants = listOf("۸", "*")),
        KeyModel("9", variants = listOf("۹", "(")),
        KeyModel("0", variants = listOf("۰", ")", "°", "∅"))
    )

    fun getAlphabetRows(
        language: KeyboardLanguage,
        isShifted: Boolean,
        isCapsLock: Boolean
    ): List<List<KeyModel>> {
        return when (language) {
            KeyboardLanguage.PERSIAN -> getPersianRows()
            KeyboardLanguage.ENGLISH -> getEnglishRows(isShifted || isCapsLock)
            KeyboardLanguage.TURKISH -> getTurkishRows(isShifted || isCapsLock)
            KeyboardLanguage.AZERBAIJANI_LATIN -> getAzerbaijaniLatinRows(isShifted || isCapsLock)
            KeyboardLanguage.AZERBAIJANI_ARABIC -> getAzerbaijaniArabicRows()
            KeyboardLanguage.ARABIC -> getArabicRows()
        }
    }

    private fun getPersianRows(): List<List<KeyModel>> {
        return listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ").map {
                when (it) {
                    "ض" -> KeyModel(it, variants = listOf("ص", "ظ", "ضـ"))
                    "ص" -> KeyModel(it, variants = listOf("ض", "س", "صـ"))
                    "ث" -> KeyModel(it, variants = listOf("ت", "س", "ثـ"))
                    "ق" -> KeyModel(it, variants = listOf("ف", "غ", "قـ"))
                    "ف" -> KeyModel(it, variants = listOf("ڤ", "ق", "فـ"))
                    "غ" -> KeyModel(it, variants = listOf("ع", "غـ", "ـغـ"))
                    "ع" -> KeyModel(it, variants = listOf("غ", "ء", "عـ", "ـعـ"))
                    "ه" -> KeyModel(it, variants = listOf("ة", "هٔ", "ۀ", "هـ", "ـهـ"))
                    "خ" -> KeyModel(it, variants = listOf("ح", "ج", "خـ"))
                    "ح" -> KeyModel(it, variants = listOf("خ", "ج", "حـ"))
                    "ج" -> KeyModel(it, variants = listOf("چ", "ح", "خ", "جـ"))
                    "چ" -> KeyModel(it, variants = listOf("ج", "چـ"))
                    else -> KeyModel(it)
                }
            },
            listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ").map {
                when (it) {
                    "ش" -> KeyModel(it, variants = listOf("س", "شـ"))
                    "س" -> KeyModel(it, variants = listOf("ش", "ص", "سـ"))
                    "ی" -> KeyModel(it, variants = listOf("ي", "ئ", "ى", "یـ", "ـیـ"))
                    "ب" -> KeyModel(it, variants = listOf("پ", "بـ", "ـبـ"))
                    "ل" -> KeyModel(it, variants = listOf("لا", "لـ", "ـلـ"))
                    "ا" -> KeyModel(it, variants = listOf("آ", "أ", "إ", "ٱ", "1", "۱"))
                    "ت" -> KeyModel(it, variants = listOf("ط", "ث", "ة", "تـ"))
                    "ن" -> KeyModel(it, variants = listOf("نـ", "ـن"))
                    "م" -> KeyModel(it, variants = listOf("مـ", "ـمـ"))
                    "ک" -> KeyModel(it, variants = listOf("گ", "ك", "کـ"))
                    "گ" -> KeyModel(it, variants = listOf("ک", "گـ"))
                    else -> KeyModel(it)
                }
            },
            listOf(
                KeyModel("ظ", variants = listOf("ط", "ض", "ظـ")),
                KeyModel("ط", variants = listOf("ظ", "ت", "طـ")),
                KeyModel("ز", variants = listOf("ژ", "ذ", "زـ")),
                KeyModel("ر", variants = listOf("ز", "رـ")),
                KeyModel("ذ", variants = listOf("د", "ز", "ذـ")),
                KeyModel("د", variants = listOf("ذ", "دـ")),
                KeyModel("پ", variants = listOf("ب", "پـ")),
                KeyModel("و", variants = listOf("ؤ", "وُ", "وْ", "وِ", "وَ")),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.35f)
            )
        )
    }

    private fun getEnglishRows(isUpper: Boolean): List<List<KeyModel>> {
        fun transform(char: String): String = if (isUpper) char.uppercase() else char.lowercase()

        val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map {
            val label = transform(it)
            when (it) {
                "e" -> KeyModel(label, variants = listOf("é", "è", "ê", "ë", "ē", "ę").map(::transform))
                "u" -> KeyModel(label, variants = listOf("ú", "ù", "û", "ü", "ū").map(::transform))
                "i" -> KeyModel(label, variants = listOf("í", "ì", "î", "ï", "ī", "ı").map(::transform))
                "o" -> KeyModel(label, variants = listOf("ó", "ò", "ô", "ö", "õ", "ø", "œ").map(::transform))
                "w" -> KeyModel(label, variants = listOf("ŵ").map(::transform))
                "r" -> KeyModel(label, variants = listOf("ř").map(::transform))
                "t" -> KeyModel(label, variants = listOf("ţ", "ť").map(::transform))
                "y" -> KeyModel(label, variants = listOf("ý", "ÿ").map(::transform))
                else -> KeyModel(label)
            }
        }

        val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map {
            val label = transform(it)
            when (it) {
                "a" -> KeyModel(label, variants = listOf("á", "à", "â", "ä", "ã", "å", "æ", "ā").map(::transform))
                "s" -> KeyModel(label, variants = listOf("ß", "š", "ş", "$").map(::transform))
                "d" -> KeyModel(label, variants = listOf("ð", "ď").map(::transform))
                "g" -> KeyModel(label, variants = listOf("ğ").map(::transform))
                "l" -> KeyModel(label, variants = listOf("ł").map(::transform))
                "k" -> KeyModel(label, variants = listOf("ķ").map(::transform))
                else -> KeyModel(label)
            }
        }

        val r3 = listOf(
            KeyModel("⇧", type = KeyType.SHIFT, weight = 1.25f)
        ) + listOf("z", "x", "c", "v", "b", "n", "m").map {
            val label = transform(it)
            when (it) {
                "c" -> KeyModel(label, variants = listOf("ç", "ć", "č").map(::transform))
                "z" -> KeyModel(label, variants = listOf("ž", "ź", "ż").map(::transform))
                "n" -> KeyModel(label, variants = listOf("ñ", "ń").map(::transform))
                else -> KeyModel(label)
            }
        } + listOf(
            KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.25f)
        )

        return listOf(r1, r2, r3)
    }

    private fun getTurkishRows(isUpper: Boolean): List<List<KeyModel>> {
        fun transform(char: String): String = if (isUpper) char.uppercase() else char.lowercase()

        val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "ı", "o", "p", "ğ", "ü").map {
            val label = transform(it)
            when (it) {
                "e" -> KeyModel(label, variants = listOf("é", "ë").map(::transform))
                "u" -> KeyModel(label, variants = listOf("ü", "û").map(::transform))
                "ı" -> KeyModel(label, variants = listOf("i", "î").map(::transform))
                "o" -> KeyModel(label, variants = listOf("ö", "ô").map(::transform))
                "ğ" -> KeyModel(label, variants = listOf("g").map(::transform))
                "ü" -> KeyModel(label, variants = listOf("u").map(::transform))
                else -> KeyModel(label)
            }
        }
        val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ş", "i").map {
            val label = transform(it)
            when (it) {
                "a" -> KeyModel(label, variants = listOf("â", "ä").map(::transform))
                "s" -> KeyModel(label, variants = listOf("ş", "ß").map(::transform))
                "ş" -> KeyModel(label, variants = listOf("s").map(::transform))
                "i" -> KeyModel(label, variants = listOf("ı", "î", "ï").map(::transform))
                "g" -> KeyModel(label, variants = listOf("ğ").map(::transform))
                else -> KeyModel(label)
            }
        }
        val r3 = listOf(
            KeyModel("⇧", type = KeyType.SHIFT, weight = 1.2f)
        ) + listOf("z", "x", "c", "v", "b", "n", "m", "ö", "ç").map {
            val label = transform(it)
            when (it) {
                "c" -> KeyModel(label, variants = listOf("ç").map(::transform))
                "ç" -> KeyModel(label, variants = listOf("c").map(::transform))
                "ö" -> KeyModel(label, variants = listOf("o").map(::transform))
                else -> KeyModel(label)
            }
        } + listOf(
            KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.2f)
        )

        return listOf(r1, r2, r3)
    }

    private fun getAzerbaijaniLatinRows(isUpper: Boolean): List<List<KeyModel>> {
        fun transform(char: String): String = if (isUpper) char.uppercase() else char.lowercase()

        val r1 = listOf("q", "ü", "e", "r", "t", "y", "u", "i", "o", "p", "ö", "ğ").map {
            val label = transform(it)
            when (it) {
                "e" -> KeyModel(label, variants = listOf("ə", "é").map(::transform))
                "ü" -> KeyModel(label, variants = listOf("u").map(::transform))
                "u" -> KeyModel(label, variants = listOf("ü").map(::transform))
                "i" -> KeyModel(label, variants = listOf("ı", "î").map(::transform))
                "o" -> KeyModel(label, variants = listOf("ö").map(::transform))
                "ö" -> KeyModel(label, variants = listOf("o").map(::transform))
                "ğ" -> KeyModel(label, variants = listOf("g").map(::transform))
                else -> KeyModel(label)
            }
        }
        val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ı", "ə").map {
            val label = transform(it)
            when (it) {
                "a" -> KeyModel(label, variants = listOf("â", "ä").map(::transform))
                "s" -> KeyModel(label, variants = listOf("ş").map(::transform))
                "ı" -> KeyModel(label, variants = listOf("i").map(::transform))
                "ə" -> KeyModel(label, variants = listOf("e", "ë").map(::transform))
                "g" -> KeyModel(label, variants = listOf("ğ").map(::transform))
                else -> KeyModel(label)
            }
        }
        val r3 = listOf(
            KeyModel("⇧", type = KeyType.SHIFT, weight = 1.2f)
        ) + listOf("z", "x", "c", "v", "b", "n", "m", "ç", "ş").map {
            val label = transform(it)
            when (it) {
                "c" -> KeyModel(label, variants = listOf("ç").map(::transform))
                "ç" -> KeyModel(label, variants = listOf("c").map(::transform))
                "ş" -> KeyModel(label, variants = listOf("s").map(::transform))
                else -> KeyModel(label)
            }
        } + listOf(
            KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.2f)
        )

        return listOf(r1, r2, r3)
    }

    private fun getAzerbaijaniArabicRows(): List<List<KeyModel>> {
        // Includes Azerbaijani Perso-Arabic specials (اؤ، اوْ، او، اۆ، گ، پ، چ، ژ)
        return listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ").map {
                when (it) {
                    "ج" -> KeyModel(it, variants = listOf("چ", "ح", "خ", "جـ"))
                    "چ" -> KeyModel(it, variants = listOf("ج", "چـ"))
                    "ه" -> KeyModel(it, variants = listOf("ة", "هٔ", "هـ"))
                    "خ" -> KeyModel(it, variants = listOf("ح", "خـ"))
                    "غ" -> KeyModel(it, variants = listOf("ع", "غـ"))
                    "ف" -> KeyModel(it, variants = listOf("ڤ", "ق", "فـ"))
                    else -> KeyModel(it)
                }
            },
            listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ").map {
                when (it) {
                    "ا" -> KeyModel(it, variants = listOf("آ", "اؤ", "اوْ", "اۆ", "أ", "إ", "1", "۱"))
                    "ک" -> KeyModel(it, variants = listOf("گ", "ك", "کـ"))
                    "گ" -> KeyModel(it, variants = listOf("ک", "گـ"))
                    "ی" -> KeyModel(it, variants = listOf("ي", "ئ", "یـ"))
                    "ب" -> KeyModel(it, variants = listOf("پ", "بـ"))
                    else -> KeyModel(it)
                }
            },
            listOf(
                KeyModel("ظ", variants = listOf("ط")),
                KeyModel("ط", variants = listOf("ظ")),
                KeyModel("ز", variants = listOf("ژ", "ذ")),
                KeyModel("ر", variants = listOf("ز")),
                KeyModel("ذ", variants = listOf("د")),
                KeyModel("د", variants = listOf("ذ")),
                KeyModel("پ", variants = listOf("ب", "پـ")),
                KeyModel("و", variants = listOf("اؤ", "اوْ", "اۆ", "ؤ", "وُ")),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.35f)
            )
        )
    }

    private fun getArabicRows(): List<List<KeyModel>> {
        return listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د").map {
                when (it) {
                    "ض" -> KeyModel(it, variants = listOf("ص", "ظ", "ضـ"))
                    "ص" -> KeyModel(it, variants = listOf("ض", "س", "صـ"))
                    "ث" -> KeyModel(it, variants = listOf("ت", "ثـ"))
                    "ق" -> KeyModel(it, variants = listOf("ف", "قـ"))
                    "ف" -> KeyModel(it, variants = listOf("ق", "فـ"))
                    "غ" -> KeyModel(it, variants = listOf("ع", "غـ"))
                    "ع" -> KeyModel(it, variants = listOf("غ", "ء", "عـ"))
                    "ه" -> KeyModel(it, variants = listOf("ة", "هـ"))
                    "خ" -> KeyModel(it, variants = listOf("ح", "خـ"))
                    "ح" -> KeyModel(it, variants = listOf("خ", "ج", "حـ"))
                    "ج" -> KeyModel(it, variants = listOf("ح", "جـ"))
                    "د" -> KeyModel(it, variants = listOf("ذ", "دـ"))
                    else -> KeyModel(it)
                }
            },
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط").map {
                when (it) {
                    "ا" -> KeyModel(it, variants = listOf("أ", "إ", "آ", "ٱ", "ء"))
                    "ي" -> KeyModel(it, variants = listOf("ى", "ئ", "يـ"))
                    "ت" -> KeyModel(it, variants = listOf("ة", "ث", "تـ"))
                    "ك" -> KeyModel(it, variants = listOf("كـ"))
                    "ب" -> KeyModel(it, variants = listOf("بـ"))
                    "ل" -> KeyModel(it, variants = listOf("لا", "لـ"))
                    "ن" -> KeyModel(it, variants = listOf("نـ"))
                    "م" -> KeyModel(it, variants = listOf("مـ"))
                    else -> KeyModel(it)
                }
            },
            listOf(
                KeyModel("ئ", variants = listOf("ء", "ؤ")),
                KeyModel("ء"),
                KeyModel("ؤ", variants = listOf("و")),
                KeyModel("ر", variants = listOf("ز")),
                KeyModel("لا", variants = listOf("لأ", "لإ", "لآ")),
                KeyModel("ى", variants = listOf("ي")),
                KeyModel("ة", variants = listOf("ت", "ه")),
                KeyModel("و", variants = listOf("ؤ")),
                KeyModel("ز", variants = listOf("ر")),
                KeyModel("ظ", variants = listOf("ط")),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
            )
        )
    }

    fun getSymbolRows(isRtl: Boolean): List<List<KeyModel>> {
        val row1 = if (isRtl) {
            listOf("۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "۰").mapIndexed { idx, fa ->
                val lat = ((idx + 1) % 10).toString()
                KeyModel(fa, variants = listOf(lat))
            }
        } else {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").mapIndexed { idx, lat ->
                val fa = listOf("۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "۰")[idx]
                KeyModel(lat, variants = listOf(fa))
            }
        }

        val row2 = if (isRtl) {
            listOf("ريال", "،", "؛", ",", "_", ":", "|", "@", "#", "$", "%", "&").map {
                KeyModel(it, variants = if (it == "&") listOf("§") else emptyList())
            }
        } else {
            listOf("$", "€", "£", ",", ";", "_", ":", "|", "@", "#", "%", "&").map {
                KeyModel(it, variants = if (it == "&") listOf("§") else if (it == "$") listOf("ريال", "¥") else emptyList())
            }
        }

        val row3 = if (isRtl) {
            listOf(
                KeyModel("!"), KeyModel("؟", variants = listOf("?", "¿")),
                KeyModel("?"),
                KeyModel("("), KeyModel(")"), KeyModel("{"), KeyModel("}"),
                KeyModel("["), KeyModel("]"), KeyModel("«", variants = listOf("\"")),
                KeyModel("»", variants = listOf("'")),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
            )
        } else {
            listOf(
                KeyModel("!"), KeyModel("?", variants = listOf("؟", "¿")),
                KeyModel("؟"),
                KeyModel("("), KeyModel(")"), KeyModel("{"), KeyModel("}"),
                KeyModel("["), KeyModel("]"), KeyModel("\"", variants = listOf("«", "»")),
                KeyModel("'", variants = listOf("`")),
                KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
            )
        }

        val row4 = listOf(
            KeyModel("+"), KeyModel("-"), KeyModel("×", variants = listOf("*")),
            KeyModel("÷", variants = listOf("/")),
            KeyModel("="), KeyModel("<"), KeyModel(">"),
            KeyModel("^", variants = listOf("°")),
            KeyModel("&", variants = listOf("§")),
            KeyModel("/"), KeyModel("\\"), KeyModel("~")
        )

        return listOf(row1, row2, row3, row4)
    }

    val SYMBOL_ROWS = getSymbolRows(isRtl = true)

    val NUMPAD_GRID = listOf(
        listOf(KeyModel("7"), KeyModel("8"), KeyModel("9"), KeyModel("⌫", type = KeyType.BACKSPACE)),
        listOf(KeyModel("4"), KeyModel("5"), KeyModel("6"), KeyModel("Del", type = KeyType.DEL)),
        listOf(KeyModel("1"), KeyModel("2"), KeyModel("3"), KeyModel("↵", type = KeyType.ENTER)),
        listOf(KeyModel("."), KeyModel("0"), KeyModel(",", variants = listOf("00")), KeyModel("ABC", type = KeyType.SYMBOLS_TOGGLE))
    )
}
