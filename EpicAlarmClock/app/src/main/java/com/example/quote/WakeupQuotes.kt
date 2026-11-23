package com.example.quote

data class WakeupQuote(
    val english: String,
    val persian: String,
    val author: String
)

object WakeupQuotes {
    val quotes = listOf(
        WakeupQuote(
            english = "The unexamined life is not worth living.",
            persian = "زندگیِ نیازموده، ارزش زیستن ندارد.",
            author = "سقراط"
        ),
        WakeupQuote(
            english = "To know thyself is the beginning of wisdom.",
            persian = "شناختنِ خویشتن، آغازِ خردمندی است.",
            author = "سقراط"
        ),
        WakeupQuote(
            english = "The waking have one common world, but the sleeping turn aside each into a world of his own.",
            persian = "بیداران جهانی مشترک دارند، اما خوابیدگان هر یک به جهانی از آنِ خود روی می‌آورند.",
            author = "هراکلیتوس"
        ),
        WakeupQuote(
            english = "Some things are within our power, while others are not.",
            persian = "برخی چیزها در اختیار ما هستند و برخی دیگر نیستند.",
            author = "اپیکتتوس"
        ),
        WakeupQuote(
            english = "It is not things themselves that disturb people, but their judgments about them.",
            persian = "این خودِ رویدادها نیستند که انسان‌ها را آشفته می‌کنند، بلکه داوری آنان دربارهٔ رویدادهاست.",
            author = "اپیکتتوس"
        ),
        WakeupQuote(
            english = "First say to yourself what you would be; and then do what you have to do.",
            persian = "نخست به خود بگو که می‌خواهی چه کسی باشی؛ سپس آنچه باید انجام دهی، انجام بده.",
            author = "اپیکتتوس"
        ),
        WakeupQuote(
            english = "The happiness of your life depends upon the quality of your thoughts.",
            persian = "خوشبختی زندگی‌ات به کیفیت اندیشه‌هایت بستگی دارد.",
            author = "مارکوس اورلیوس"
        ),
        WakeupQuote(
            english = "The soul becomes dyed with the color of its thoughts.",
            persian = "روح انسان به رنگ اندیشه‌هایش درمی‌آید.",
            author = "مارکوس اورلیوس"
        ),
        WakeupQuote(
            english = "Confine yourself to the present.",
            persian = "خود را به لحظهٔ اکنون محدود کن.",
            author = "مارکوس اورلیوس"
        ),
        WakeupQuote(
            english = "We suffer more often in imagination than in reality.",
            persian = "ما بیشتر در خیال رنج می‌کشیم تا در واقعیت.",
            author = "سنکا"
        ),
        WakeupQuote(
            english = "While we are postponing, life speeds by.",
            persian = "در حالی که ما کارها را به تأخیر می‌اندازیم، زندگی با شتاب می‌گذرد.",
            author = "سنکا"
        ),
        WakeupQuote(
            english = "Life is long if you know how to use it.",
            persian = "اگر بدانی چگونه از زندگی استفاده کنی، زندگی طولانی است.",
            author = "سنکا"
        ),
        WakeupQuote(
            english = "To know what you know and what you do not know, that is true knowledge.",
            persian = "این‌که بدانی چه می‌دانی و چه نمی‌دانی، همان دانشِ حقیقی است.",
            author = "کنفوسیوس"
        ),
        WakeupQuote(
            english = "He who learns but does not think is lost; he who thinks but does not learn is in danger.",
            persian = "آن‌که می‌آموزد اما نمی‌اندیشد، گمراه می‌شود؛ و آن‌که می‌اندیشد اما نمی‌آموزد، در خطر است.",
            author = "کنفوسیوس"
        ),
        WakeupQuote(
            english = "Knowing others is intelligence; knowing yourself is true wisdom.",
            persian = "شناختن دیگران هوشمندی است؛ اما شناختن خویشتن، خردِ راستین است.",
            author = "لائوتسه"
        ),
        WakeupQuote(
            english = "He who knows, and desires to know, shall raise himself to the heights of felicity.",
            persian = "آنکس که بداند و بخواهد که بداند * خود را به بلندای سعادت برساند",
            author = "نراقی"
        ),
        WakeupQuote(
            english = "He who knows, and is aware that he knows, shall vault his steed of honor beyond the celestial sphere.",
            persian = "آنکس که بداند و بداند که بداند * اسب شرف از گنبد گردون بجهاند",
            author = "نراقی"
        ),
        WakeupQuote(
            english = "He who knows, yet is unaware that he knows, is like one holding a jug of water who nonetheless remains thirsty.",
            persian = "آنکس که بداند و نداند که بداند * با کوزه ی آب است ولی تشنه بماند",
            author = "نراقی"
        ),
        WakeupQuote(
            english = "He who knows not, yet is aware that he knows not, shall—though limping—eventually bring his mount to the destination.",
            persian = "آنکس که نداند و بداند که نداند * لنگان خرک خویش به مقصد برساند",
            author = "نراقی"
        ),
        WakeupQuote(
            english = "He who knows not, but desires to know, shall liberate his body and soul from the shackles of ignorance.",
            persian = "آنکس که نداند و بخواهد که بداند * جان و تن خود را ز جهالت برهاند",
            author = "نراقی"
        ),
        WakeupQuote(
            english = "He who knows not, and is unaware that he knows not, shall remain forever trapped in profound, compounded ignorance.",
            persian = "آنکس که نداند و نداند که نداند * در جهل مرکب ابدالدهر بماند",
            author = "نراقی"
        ),
        WakeupQuote(
            english = "He who knows not, and has no desire to know—it is a pity that such a creature should remain alive.",
            persian = "آنکس که نداند و نخواهد که بداند * حیف است چنین جانوری زنده بماند",
            author = "نراقی"
        )
    )

    fun getRandomQuote(): WakeupQuote {
        return quotes.random()
    }
}
