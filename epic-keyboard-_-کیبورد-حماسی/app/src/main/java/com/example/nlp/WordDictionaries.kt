package com.example.nlp

object WordDictionaries {

    // Rich Persian phrase & sentence corpus used to automatically extract 2-gram, 3-gram, and 4-gram models
    val PERSIAN_PHRASES = listOf(
        "سلام وقت بخیر امیدوارم حال شما خوب باشد",
        "سلام صبح بخیر روز خوبی داشته باشید",
        "سلام شب بخیر خواب خوبی ببینید",
        "امیدوارم حال شما بسیار خوب و عالی باشد",
        "خیلی ممنون از لطف و محبت شما",
        "بسیار سپاسگزارم از زحمت و همکاری شما",
        "دست شما درد نکند خیلی عالی بود",
        "خواهش می‌کنم وظیفه من بود در خدمتم",
        "لطفاً فایل مورد نظر را برای من ارسال کنید",
        "لطفاً بررسی بفرمایید و نتیجه را اطلاع دهید",
        "لطفاً شماره تماس خود را برای من بفرستید",
        "در صورت امکان همین امروز تماس بگیرید",
        "در حال حاضر مشغول کار روی پروژه جدید هستم",
        "فردا ساعت ده صبح جلسه مهمی داریم",
        "امروز هوا خیلی خوب و آفتابی است",
        "می‌خواهم درباره موضوع جلسه با شما صحبت کنم",
        "نمی‌توانم در جلسه امروز شرکت کنم",
        "می‌توانم فردا صبح به دفتر شما مراجعه کنم",
        "به امید دیدار شما در آینده نزدیک",
        "هر زمان که آماده بودید به من خبر بدهید",
        "با عرض سلام و احترام خدمت شما دوست عزیز",
        "با تشکر فراوان از صبر و شکیبایی شما",
        "آیا این برنامه روی گوشی اندروید نصب می‌شود",
        "کیبورد حماسی با امکانات هوشمند و پیشرفته آماده است",
        "کیفیت کار شما واقعاً بی‌نظیر و فوق‌العاده است",
        "خوشحال می‌شوم نظر خودتان را با من در میان بگذارید",
        "چطور می‌توانم به شما کمک کنم بفرمایید",
        "اگر سوالی داشتید حتماً با من مطرح کنید",
        "همه چیز به خوبی پیش می‌رود نگران نباشید",
        "این پیام جهت یادآوری قرار ملاقات فردا ارسال شده است"
    )

    // Common Persian vocabulary with standard spelling and half-space (ZWNBSP)
    val PERSIAN_WORDS = (listOf(
        "سلام", "درود", "خوبی", "شما", "من", "ما", "آنها", "این", "آن", "که", "را", "به", "از", "در", "با", "برای",
        "کارخانه", "دانشگاه", "کتابخانه", "خوشحال", "بیمارستان", "دانش‌آموز", "دانشجو", "همکاری",
        "می‌خواهم", "نمی‌خواهم", "می‌توانم", "نمی‌توانم", "می‌شود", "نمی‌شود", "می‌کنم", "نمی‌کنم",
        "می‌شوم", "نمی‌شوم", "می‌دهم", "می‌گیرم", "می‌روم", "می‌آیم", "می‌بینم", "می‌دانم",
        "روزبخیر", "شب‌بخیر", "صبح‌بخیر", "وقت", "بخیر", "ممنون", "متشکرم", "فراوان",
        "سپاسگزارم", "لطفاً", "بفرمایید", "خواهش", "امروز", "فردا", "دیروز", "دیشب", "همین",
        "چطور", "چگونه", "کجا", "چه", "چرا", "کی", "کدام", "خانواده", "دوست", "زندگی",
        "ایران", "تهران", "اصفهان", "شیراز", "تبریز", "مشهد", "زبان", "کشور", "جهان",
        "کتاب", "دفتر", "قلم", "خانه", "ماشین", "موبایل", "گوشی", "کامپیوتر", "اینترنت", "برنامه",
        "پروژه", "جلسه", "تماس", "پیام", "فایل", "ساعت", "زمان", "دقیقه", "هفته", "ماه", "سال",
        "عالی", "زیبا", "خوب", "بهتر", "بهترین", "بزرگ", "کوچک", "سریع", "درست", "غلط",
        "آسان", "سخت", "مهم", "جدید", "قدیم", "روشن", "تاریک", "آرام", "موفق", "امیدوارم",
        "سلامتی", "محبت", "عزیز", "همکار", "مدیر", "کارمند", "استاد", "معلم", "پزشک",
        "مهندس", "طراح", "نویسنده", "توسعه‌دهنده", "سیستم", "اطلاعات", "امنیت", "سرعت",
        "کیفیت", "نتیجه", "پیشرفت", "راهنما", "خدمات", "محصول", "فروش", "خرید", "تخفیف",
        "قیمت", "هزینه", "بانک", "کارت", "حساب", "واریز", "برداشت", "سرمایه", "بازار",
        "کار", "شغل", "تلاش", "قرارداد", "موفقیت", "تمرین", "آموزش", "یادگیری",
        "آزمون", "نمره", "مدرک", "تحصیل", "تجربه", "رزومه", "مصاحبه", "قبول", "رد",
        "همیشه", "هرگز", "معمولاً", "شاید", "احتمالاً", "قطعاً", "حتماً", "دقیقاً", "واقعاً",
        "ارسال", "دریافت", "بررسی", "انتخاب", "تنظیمات", "دیکشنری", "کیبورد", "حماسی", "هوشمند",
        "اندروید", "سوال", "جواب", "پاسخ", "موضوع", "امکان", "احوال", "حاضر", "نگران",
        "باشد", "باشید", "هستم", "هستید", "هست", "دارم", "دارد", "داریم", "دارید", "بود", "شد",
        "کتاب‌ها", "خانه‌ها", "دوستان", "کارها", "روزها", "شب‌ها", "سال‌ها", "دست‌ها",
        "چشم‌ها", "کلمات", "جملات", "صفحات", "دستورات", "امکانات", "قابلیت‌ها",
        "بسیار", "خیلی", "فوق‌العاده", "بی‌نظیر", "درد", "نکند", "لطف", "زحمت", "احترام", "تشکر",
        "آینده", "نزدیک", "دیدار", "امید", "خبر", "آماده", "شماره", "قرار", "ملاقات", "مشغول"
    ) + PERSIAN_PHRASES.flatMap { it.split(" ") }).distinct()

    // English Phrases & Sentences
    val ENGLISH_PHRASES = listOf(
        "hello how are you doing today",
        "i hope you are having a great day",
        "good morning everyone have a nice day",
        "thank you very much for your help",
        "please let me know if you need anything",
        "i am working on a new android project",
        "could you please send me the file",
        "we have a meeting tomorrow morning at ten",
        "nice to meet you see you soon",
        "thank you for your time and support",
        "i will get back to you as soon as possible",
        "please check the attachment and let me know",
        "this smart keyboard supports multiple languages",
        "everything is going well do not worry",
        "happy birthday wishing you all the best"
    )

    val ENGLISH_WORDS = (listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "factory", "keyboard", "smart", "language", "mobile", "android", "application",
        "hello", "welcome", "please", "thank", "thanks", "friend", "happy", "great",
        "morning", "evening", "afternoon", "night", "today", "tomorrow", "yesterday",
        "meeting", "project", "message", "number", "email", "address", "company", "team",
        "support", "update", "version", "feature", "custom", "dictionary", "clipboard",
        "possible", "already", "important", "working", "sending", "received", "checking"
    ) + ENGLISH_PHRASES.flatMap { it.split(" ") }).distinct()

    // Turkish Phrases & Sentences
    val TURKISH_PHRASES = listOf(
        "merhaba nasılsın bugün her şey yolunda mı",
        "günaydın iyi günler dilerim herkese",
        "çok teşekkür ederim yardımınız için sağ olun",
        "lütfen bana haber verin bekliyorum",
        "yarın sabah saat onda toplantımız var",
        "bu akıllı klavye çok hızlı ve kullanışlı",
        "en kısa zamanda size geri dönüş yapacağım",
        "dosyayı kontrol edip bana gönderebilir misiniz",
        "iyi akşamlar kendine iyi bak görüşürüz"
    )

    val TURKISH_WORDS = (listOf(
        "merhaba", "nasılsın", "teşekkür", "ederim", "evet", "hayır", "lütfen", "günaydın",
        "iyi", "akşamlar", "geceler", "günler", "bugün", "yarın", "dün", "güzel",
        "insan", "dünya", "çocuk", "zaman", "hayat", "iş", "fabrika", "arkadaş",
        "kitap", "ev", "araba", "telefon", "bilgisayar", "klavye", "akıllı", "dil",
        "türkiye", "istanbul", "ankara", "izmir", "büyük", "küçük", "hızlı", "yeni",
        "nasıl", "neden", "nerede", "kim", "ne", "hangi", "her", "hiç", "çok", "için",
        "dosya", "mesaj", "toplantı", "çalışma", "proje", "yardım", "haber", "sabah"
    ) + TURKISH_PHRASES.flatMap { it.split(" ") }).distinct()

    // Azerbaijani Latin Phrases & Sentences
    val AZERBAIJANI_LATIN_PHRASES = listOf(
        "salam necəsən işlərin necə gedir",
        "sabahınız xeyir gününüz uğurlu olsun",
        "çox təşəkkür edirəm köməyinizə görə",
        "zəhmət olmasa mənə xəbər verin",
        "sabah səhər saat onda görüşümüz var",
        "bu ağıllı klaviatura çox rahat və sürətlidir",
        "axşamınız xeyir yaxşı istirahət edin"
    )

    val AZERBAIJANI_LATIN_WORDS = (listOf(
        "salam", "necəsən", "təşəkkür", "edirəm", "bəli", "xeyr", "zəhmət", "olmasa",
        "sabahınız", "xeyir", "axşamınız", "gecəniz", "sabah", "dünən", "gözəl",
        "insan", "dünya", "uşaq", "vaxt", "həyat", "iş", "zavod", "dost",
        "kitab", "ev", "maşın", "telefon", "kompüter", "klaviatura", "ağıllı", "dil",
        "azərbaycan", "bakı", "gəncə", "böyük", "kiçik", "sürətli", "yeni", "necə",
        "çox", "mənə", "xəbər", "görüş", "layihə", "fayl", "mesaj", "uğurlu", "olsun"
    ) + AZERBAIJANI_LATIN_PHRASES.flatMap { it.split(" ") }).distinct()

    // Azerbaijani Arabic Script (تورکجه) Phrases & Sentences
    val AZERBAIJANI_ARABIC_PHRASES = listOf(
        "سلام نئجه‌سن ایشلرین یاخشی گئدیرمی",
        "گون‌آیدین گونونوز خئیرلی و اوغورلو اولسون",
        "چوخ تشکور ائدیرم کؤمگینیزه گؤره",
        "لطفاً منه خبر وئرین گؤزله‌ییرم",
        "صاباح سحر ساعات اوندا گؤروشوموز وار",
        "بو عاغیللی کیبورد چوخ راحات و یئییندیر",
        "گئجه‌نیز خئیر یاخشی دئنجه‌لین"
    )

    val AZERBAIJANI_ARABIC_WORDS = (listOf(
        "سلام", "نئجه‌سن", "تشکور", "ائدیریرم", "ائدیرم", "بلی", "خیر", "گون‌آیدین", "گئجه‌نیز",
        "خئیر", "صاباح", "دونن", "گؤزل", "اینسان", "دونیا", "اوشاق", "واخت",
        "حیات", "ایش", "کارخانا", "یولداش", "کیتاب", "ائو", "ماشین", "تلفون", "دیل",
        "آذربایجان", "تبریز", "اردبیل", "اورمیه", "بؤیوک", "کیچیک", "یئنی", "نئجه",
        "چوخ", "منه", "خبر", "سحر", "گؤروش", "راحات", "اوغورلو", "اولسون", "یاخشی"
    ) + AZERBAIJANI_ARABIC_PHRASES.flatMap { it.split(" ") }).distinct()

    // Arabic Phrases & Sentences
    val ARABIC_PHRASES = listOf(
        "مرحبا كيف حالك أتمنى أن تكون بخير",
        "صباح الخير أتمنى لك يوما سعيدا",
        "مساء الخير ليلة سعيدة للجميع",
        "شكرا جزيلا على مساعدتك وتعاونك معنا",
        "من فضلك أرسل لي الملف في أقرب وقت",
        "لدينا اجتماع مهم غدا في الصباح",
        "لوحة المفاتيح الذكية تدعم عدة لغات بسهولة"
    )

    val ARABIC_WORDS = (listOf(
        "مرحبا", "أهلا", "شكرا", "جزيلا", "كيف", "حالك", "نعم", "لا", "من", "فضلك",
        "صباح", "الخير", "مساء", "تصبح", "على", "خير", "اليوم", "غدا", "أمس",
        "جميل", "إنسان", "عالم", "طفل", "وقت", "حياة", "عمل", "مصنع", "صديق",
        "كتاب", "بيت", "سيارة", "هاتف", "حاسوب", "لوحة", "مفاتيح", "ذكي", "لغة",
        "كبير", "صغير", "سريع", "جديد", "أين", "لماذا", "متى", "ماذا", "أتمنى", "سعيد",
        "الملف", "اجتماع", "رسالة", "تعاونك", "مساعدتك", "بسهولة"
    ) + ARABIC_PHRASES.flatMap { it.split(" ") }).distinct()
}
