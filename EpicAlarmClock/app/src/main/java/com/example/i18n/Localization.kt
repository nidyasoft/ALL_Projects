package com.example.i18n

enum class AppLanguage(val code: String, val title: String, val nativeName: String) {
    PERSIAN("fa", "فارسی", "فارسی"),
    ENGLISH("en", "English", "English"),
    ARABIC("ar", "العربية", "العربية"),
    AZERBAIJANI("az", "Azərbaycanca", "تورکجه")
}

object Strings {
    fun get(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.PERSIAN) ?: key
    }

    fun formatNumber(number: Int, lang: AppLanguage): String {
        return formatNumber(number.toString(), lang)
    }

    fun formatNumber(text: String, lang: AppLanguage): String {
        if (lang == AppLanguage.ENGLISH) return text
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val targetDigits = if (lang == AppLanguage.ARABIC) arabicDigits else persianDigits
        
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(targetDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private val translations = mapOf(
        "app_name" to mapOf(
            AppLanguage.PERSIAN to "ساعت هشدار اپیک",
            AppLanguage.ENGLISH to "Epic Alarm Clock",
            AppLanguage.ARABIC to "ساعة المنبه إبيك",
            AppLanguage.AZERBAIJANI to "Epik Zəngli Saat"
        ),
        "tab_alarm" to mapOf(
            AppLanguage.PERSIAN to "آلارم",
            AppLanguage.ENGLISH to "Alarm",
            AppLanguage.ARABIC to "المنبه",
            AppLanguage.AZERBAIJANI to "Zəng"
        ),
        "tab_world_clock" to mapOf(
            AppLanguage.PERSIAN to "ساعت جهانی",
            AppLanguage.ENGLISH to "World Clock",
            AppLanguage.ARABIC to "ساعة العالم",
            AppLanguage.AZERBAIJANI to "Dünya Saatı"
        ),
        "tab_stopwatch" to mapOf(
            AppLanguage.PERSIAN to "کرنومتر",
            AppLanguage.ENGLISH to "Stopwatch",
            AppLanguage.ARABIC to "ساعة إيقاف",
            AppLanguage.AZERBAIJANI to "Saniyəölçən"
        ),
        "tab_timer" to mapOf(
            AppLanguage.PERSIAN to "تایمر",
            AppLanguage.ENGLISH to "Timer",
            AppLanguage.ARABIC to "المؤقت",
            AppLanguage.AZERBAIJANI to "Taymer"
        ),
        "tab_settings" to mapOf(
            AppLanguage.PERSIAN to "تنظیمات",
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.ARABIC to "الإعدادات",
            AppLanguage.AZERBAIJANI to "Tənzimləmələr"
        ),
        "next_alarm_in" to mapOf(
            AppLanguage.PERSIAN to "هشدار تا %s",
            AppLanguage.ENGLISH to "Alarm in %s",
            AppLanguage.ARABIC to "المنبه بعد %s",
            AppLanguage.AZERBAIJANI to "Zəngə qalan vaxt: %s"
        ),
        "next_timer_in" to mapOf(
            AppLanguage.PERSIAN to "تایمر بعدی تا %s",
            AppLanguage.ENGLISH to "Next timer in %s",
            AppLanguage.ARABIC to "المؤقت التالي بعد %s",
            AppLanguage.AZERBAIJANI to "Növbəti taymer: %s"
        ),
        "no_active_alarms" to mapOf(
            AppLanguage.PERSIAN to "هیچ آلارم فعالی وجود ندارد",
            AppLanguage.ENGLISH to "No active alarms",
            AppLanguage.ARABIC to "لا توجد منبهات نشطة",
            AppLanguage.AZERBAIJANI to "Aktiv zəng yoxdur"
        ),
        "add_alarm" to mapOf(
            AppLanguage.PERSIAN to "افزودن آلارم",
            AppLanguage.ENGLISH to "Add Alarm",
            AppLanguage.ARABIC to "إضافة منبه",
            AppLanguage.AZERBAIJANI to "Zəng əlavə et"
        ),
        "edit_alarm" to mapOf(
            AppLanguage.PERSIAN to "ویرایش آلارم",
            AppLanguage.ENGLISH to "Edit Alarm",
            AppLanguage.ARABIC to "تعديل المنبه",
            AppLanguage.AZERBAIJANI to "Zəngi redaktə et"
        ),
        "add_timer" to mapOf(
            AppLanguage.PERSIAN to "افزودن تایمر جدید",
            AppLanguage.ENGLISH to "Add New Timer",
            AppLanguage.ARABIC to "إضافة مؤقت جديد",
            AppLanguage.AZERBAIJANI to "Yeni taymer əlavə et"
        ),
        "label" to mapOf(
            AppLanguage.PERSIAN to "برچسب",
            AppLanguage.ENGLISH to "Label",
            AppLanguage.ARABIC to "تسمية",
            AppLanguage.AZERBAIJANI to "Etiket"
        ),
        "ringtone" to mapOf(
            AppLanguage.PERSIAN to "آهنگ هشدار",
            AppLanguage.ENGLISH to "Ringtone",
            AppLanguage.ARABIC to "نغمة الرنين",
            AppLanguage.AZERBAIJANI to "Zəng səsi"
        ),
        "vibration" to mapOf(
            AppLanguage.PERSIAN to "لرزش دستگاه",
            AppLanguage.ENGLISH to "Vibration",
            AppLanguage.ARABIC to "الاهتزاز",
            AppLanguage.AZERBAIJANI to "Titrəmə"
        ),
        "auto_delete" to mapOf(
            AppLanguage.PERSIAN to "حذف پس از یکبار استفاده",
            AppLanguage.ENGLISH to "Delete after ringing once",
            AppLanguage.ARABIC to "حذف بعد الاستخدام مرة واحدة",
            AppLanguage.AZERBAIJANI to "Bir dəfə istifadədən sonra sil"
        ),
        "repeat" to mapOf(
            AppLanguage.PERSIAN to "تکرار",
            AppLanguage.ENGLISH to "Repeat",
            AppLanguage.ARABIC to "التكرار",
            AppLanguage.AZERBAIJANI to "Təkrar"
        ),
        "repeat_once" to mapOf(
            AppLanguage.PERSIAN to "یکبار",
            AppLanguage.ENGLISH to "Once",
            AppLanguage.ARABIC to "مرة واحدة",
            AppLanguage.AZERBAIJANI to "Bir dəfə"
        ),
        "repeat_daily" to mapOf(
            AppLanguage.PERSIAN to "روزانه",
            AppLanguage.ENGLISH to "Daily",
            AppLanguage.ARABIC to "يومياً",
            AppLanguage.AZERBAIJANI to "Hər gün"
        ),
        "repeat_sat_thu" to mapOf(
            AppLanguage.PERSIAN to "شنبه تا پنجشنبه",
            AppLanguage.ENGLISH to "Saturday to Thursday",
            AppLanguage.ARABIC to "من السبت إلى الخميس",
            AppLanguage.AZERBAIJANI to "Şənbədən Cümə axşamına"
        ),
        "repeat_mon_fri" to mapOf(
            AppLanguage.PERSIAN to "دوشنبه تا جمعه",
            AppLanguage.ENGLISH to "Monday to Friday",
            AppLanguage.ARABIC to "من الاثنين إلى الجمعة",
            AppLanguage.AZERBAIJANI to "Bazar ertəsindən Cüməyə"
        ),
        "repeat_custom" to mapOf(
            AppLanguage.PERSIAN to "روزهای دلخواه",
            AppLanguage.ENGLISH to "Custom Days",
            AppLanguage.ARABIC to "أيام مخصصة",
            AppLanguage.AZERBAIJANI to "Seçilmiş günlər"
        ),
        "repeat_shift" to mapOf(
            AppLanguage.PERSIAN to "شیفت دوره‌ای (Shift Alarm)",
            AppLanguage.ENGLISH to "Shift Cycle Alarm",
            AppLanguage.ARABIC to "منبه ورديات العمل",
            AppLanguage.AZERBAIJANI to "Növbəli zəng (Shift)"
        ),
        "cycle_days" to mapOf(
            AppLanguage.PERSIAN to "دوره شیفت (چند روز یکبار):",
            AppLanguage.ENGLISH to "Cycle length (days):",
            AppLanguage.ARABIC to "طول الدورة (بالأيام):",
            AppLanguage.AZERBAIJANI to "Dövr uzunluğu (gün):"
        ),
        "start_date" to mapOf(
            AppLanguage.PERSIAN to "تاریخ شروع دوره",
            AppLanguage.ENGLISH to "Cycle Start Date",
            AppLanguage.ARABIC to "تاريخ بدء الدورة",
            AppLanguage.AZERBAIJANI to "Dövrün başlama tarixi"
        ),
        "calendar_solar" to mapOf(
            AppLanguage.PERSIAN to "شمسی",
            AppLanguage.ENGLISH to "Solar",
            AppLanguage.ARABIC to "الشمسي",
            AppLanguage.AZERBAIJANI to "Şəmsi"
        ),
        "calendar_gregorian" to mapOf(
            AppLanguage.PERSIAN to "میلادی",
            AppLanguage.ENGLISH to "Gregorian",
            AppLanguage.ARABIC to "الميلادي",
            AppLanguage.AZERBAIJANI to "Miladi"
        ),
        "calendar_lunar" to mapOf(
            AppLanguage.PERSIAN to "قمری",
            AppLanguage.ENGLISH to "Lunar",
            AppLanguage.ARABIC to "القمري",
            AppLanguage.AZERBAIJANI to "Qəməri"
        ),
        "shift_day_n" to mapOf(
            AppLanguage.PERSIAN to "روز %d دوره",
            AppLanguage.ENGLISH to "Day %d of Cycle",
            AppLanguage.ARABIC to "اليوم %d من الدورة",
            AppLanguage.AZERBAIJANI to "Dövrün %d-ci günü"
        ),
        "rest_day" to mapOf(
            AppLanguage.PERSIAN to "استراحت / بدون آلارم",
            AppLanguage.ENGLISH to "Off / Rest day",
            AppLanguage.ARABIC to "راحة / بدون منبه",
            AppLanguage.AZERBAIJANI to "İstirahət / zəngsiz"
        ),
        "cancel" to mapOf(
            AppLanguage.PERSIAN to "انصراف",
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.ARABIC to "إلغاء",
            AppLanguage.AZERBAIJANI to "Ləğv et"
        ),
        "save" to mapOf(
            AppLanguage.PERSIAN to "ذخیره",
            AppLanguage.ENGLISH to "Save",
            AppLanguage.ARABIC to "حفظ",
            AppLanguage.AZERBAIJANI to "Saxla"
        ),
        "delete" to mapOf(
            AppLanguage.PERSIAN to "حذف",
            AppLanguage.ENGLISH to "Delete",
            AppLanguage.ARABIC to "حذف",
            AppLanguage.AZERBAIJANI to "Sil"
        ),
        "select_all" to mapOf(
            AppLanguage.PERSIAN to "انتخاب همه",
            AppLanguage.ENGLISH to "Select All",
            AppLanguage.ARABIC to "تحديد الكل",
            AppLanguage.AZERBAIJANI to "Hamısını seç"
        ),
        "deselect_all" to mapOf(
            AppLanguage.PERSIAN to "لغو انتخاب",
            AppLanguage.ENGLISH to "Deselect All",
            AppLanguage.ARABIC to "إلغاء التحديد",
            AppLanguage.AZERBAIJANI to "Seçimi ləğv et"
        ),
        "selected_count" to mapOf(
            AppLanguage.PERSIAN to "%d مورد انتخاب شده",
            AppLanguage.ENGLISH to "%d selected",
            AppLanguage.ARABIC to "%d محدد",
            AppLanguage.AZERBAIJANI to "%d seçildi"
        ),
        "move_up" to mapOf(
            AppLanguage.PERSIAN to "بالا",
            AppLanguage.ENGLISH to "Up",
            AppLanguage.ARABIC to "أعلى",
            AppLanguage.AZERBAIJANI to "Yuxarı"
        ),
        "move_down" to mapOf(
            AppLanguage.PERSIAN to "پایین",
            AppLanguage.ENGLISH to "Down",
            AppLanguage.ARABIC to "أسفل",
            AppLanguage.AZERBAIJANI to "Aşağı"
        ),
        "start" to mapOf(
            AppLanguage.PERSIAN to "شروع",
            AppLanguage.ENGLISH to "Start",
            AppLanguage.ARABIC to "بدء",
            AppLanguage.AZERBAIJANI to "Başla"
        ),
        "pause" to mapOf(
            AppLanguage.PERSIAN to "توقف",
            AppLanguage.ENGLISH to "Pause",
            AppLanguage.ARABIC to "إيقاف مؤقت",
            AppLanguage.AZERBAIJANI to "Fasilə"
        ),
        "resume" to mapOf(
            AppLanguage.PERSIAN to "ادامه",
            AppLanguage.ENGLISH to "Resume",
            AppLanguage.ARABIC to "استئناف",
            AppLanguage.AZERBAIJANI to "Davam et"
        ),
        "reset" to mapOf(
            AppLanguage.PERSIAN to "شروع مجدد",
            AppLanguage.ENGLISH to "Reset",
            AppLanguage.ARABIC to "إعادة ضبط",
            AppLanguage.AZERBAIJANI to "Sıfırla"
        ),
        "lap" to mapOf(
            AppLanguage.PERSIAN to "ثبت دور",
            AppLanguage.ENGLISH to "Lap",
            AppLanguage.ARABIC to "دورة",
            AppLanguage.AZERBAIJANI to "Dövrə"
        ),
        "lap_num" to mapOf(
            AppLanguage.PERSIAN to "دور",
            AppLanguage.ENGLISH to "Lap",
            AppLanguage.ARABIC to "رقم",
            AppLanguage.AZERBAIJANI to "Dövrə"
        ),
        "lap_time" to mapOf(
            AppLanguage.PERSIAN to "زمان دور",
            AppLanguage.ENGLISH to "Lap Time",
            AppLanguage.ARABIC to "وقت الدورة",
            AppLanguage.AZERBAIJANI to "Dövrə vaxtı"
        ),
        "split_diff" to mapOf(
            AppLanguage.PERSIAN to "اختلاف از آخرین رکورد",
            AppLanguage.ENGLISH to "Split Difference",
            AppLanguage.ARABIC to "الفارق عن السابق",
            AppLanguage.AZERBAIJANI to "Əvvəlki fərqi"
        ),
        "overall_time" to mapOf(
            AppLanguage.PERSIAN to "زمان کل",
            AppLanguage.ENGLISH to "Overall Time",
            AppLanguage.ARABIC to "الوقت الإجمالي",
            AppLanguage.AZERBAIJANI to "Ümumi vaxt"
        ),
        "add_city" to mapOf(
            AppLanguage.PERSIAN to "افزودن ساعت جهانی",
            AppLanguage.ENGLISH to "Add World Clock",
            AppLanguage.ARABIC to "إضافة ساعة مدينة",
            AppLanguage.AZERBAIJANI to "Dünya saatı əlavə et"
        ),
        "search_cities" to mapOf(
            AppLanguage.PERSIAN to "جستجوی شهر یا کشور...",
            AppLanguage.ENGLISH to "Search city or country...",
            AppLanguage.ARABIC to "البحث عن مدينة أو بلد...",
            AppLanguage.AZERBAIJANI to "Şəhər və ya ölkə axtar..."
        ),
        "dismiss" to mapOf(
            AppLanguage.PERSIAN to "قطع هشدار",
            AppLanguage.ENGLISH to "Dismiss",
            AppLanguage.ARABIC to "إيقاف",
            AppLanguage.AZERBAIJANI to "Dayandır"
        ),
        "snooze" to mapOf(
            AppLanguage.PERSIAN to "چرت ۵ دقیقه",
            AppLanguage.ENGLISH to "Snooze 5m",
            AppLanguage.ARABIC to "غفوة ٥ دقائق",
            AppLanguage.AZERBAIJANI to "5 dəqiqə təxirə sal"
        ),
        "days" to mapOf(
            AppLanguage.PERSIAN to "روز",
            AppLanguage.ENGLISH to "Days",
            AppLanguage.ARABIC to "أيام",
            AppLanguage.AZERBAIJANI to "Gün"
        ),
        "hours" to mapOf(
            AppLanguage.PERSIAN to "ساعت",
            AppLanguage.ENGLISH to "Hours",
            AppLanguage.ARABIC to "ساعات",
            AppLanguage.AZERBAIJANI to "Saat"
        ),
        "minutes" to mapOf(
            AppLanguage.PERSIAN to "دقیقه",
            AppLanguage.ENGLISH to "Minutes",
            AppLanguage.ARABIC to "دقائق",
            AppLanguage.AZERBAIJANI to "Dəqiqə"
        ),
        "seconds" to mapOf(
            AppLanguage.PERSIAN to "ثانیه",
            AppLanguage.ENGLISH to "Seconds",
            AppLanguage.ARABIC to "ثواني",
            AppLanguage.AZERBAIJANI to "Saniyə"
        ),
        "vibe_continuous" to mapOf(
            AppLanguage.PERSIAN to "ممتد",
            AppLanguage.ENGLISH to "Continuous",
            AppLanguage.ARABIC to "مستمر",
            AppLanguage.AZERBAIJANI to "Davamlı"
        ),
        "vibe_intermittent" to mapOf(
            AppLanguage.PERSIAN to "تکرار شونده با وقفه",
            AppLanguage.ENGLISH to "Intermittent Pulse",
            AppLanguage.ARABIC to "متقطع مع فترات",
            AppLanguage.AZERBAIJANI to "Fasiləli titrəmə"
        ),
        "vibe_heartbeat" to mapOf(
            AppLanguage.PERSIAN to "ضربان قلب",
            AppLanguage.ENGLISH to "Heartbeat",
            AppLanguage.ARABIC to "نبضات قلب",
            AppLanguage.AZERBAIJANI to "Ürək döyüntüsü"
        ),
        "vibe_strobe" to mapOf(
            AppLanguage.PERSIAN to "لرزش سریع و پی‌درپی",
            AppLanguage.ENGLISH to "Rapid Strobe",
            AppLanguage.ARABIC to "اهتزاز سريع متتابع",
            AppLanguage.AZERBAIJANI to "Sürətli titrəmə"
        ),
        "vibe_off" to mapOf(
            AppLanguage.PERSIAN to "بدون لرزش",
            AppLanguage.ENGLISH to "Off",
            AppLanguage.ARABIC to "معطل",
            AppLanguage.AZERBAIJANI to "Titrəməsiz"
        ),
        "theme" to mapOf(
            AppLanguage.PERSIAN to "تم و پوسته",
            AppLanguage.ENGLISH to "Theme",
            AppLanguage.ARABIC to "المظهر",
            AppLanguage.AZERBAIJANI to "Mövzu"
        ),
        "theme_dialog_title" to mapOf(
            AppLanguage.PERSIAN to "انتخاب تم و پوسته",
            AppLanguage.ENGLISH to "Choose Theme",
            AppLanguage.ARABIC to "اختيار المظهر",
            AppLanguage.AZERBAIJANI to "Mövzu seçimi"
        ),
        "theme_dark" to mapOf(
            AppLanguage.PERSIAN to "تاریک",
            AppLanguage.ENGLISH to "Dark",
            AppLanguage.ARABIC to "داكن",
            AppLanguage.AZERBAIJANI to "Qaranlıq"
        ),
        "theme_dark_desc" to mapOf(
            AppLanguage.PERSIAN to "حالت مشکی OLED با نورپردازی نئونی",
            AppLanguage.ENGLISH to "OLED dark mode with ambient neon glow",
            AppLanguage.ARABIC to "الوضع الداكن بتقنية OLED مع توهج نيون",
            AppLanguage.AZERBAIJANI to "Neon işıqları ilə OLED qaranlıq rejimi"
        ),
        "theme_light" to mapOf(
            AppLanguage.PERSIAN to "روشن",
            AppLanguage.ENGLISH to "Light",
            AppLanguage.ARABIC to "فاتح",
            AppLanguage.AZERBAIJANI to "Açıq"
        ),
        "theme_light_desc" to mapOf(
            AppLanguage.PERSIAN to "حالت روشن و شفاف با کنتراست بالا",
            AppLanguage.ENGLISH to "Clean high-contrast light mode",
            AppLanguage.ARABIC to "الوضع الفاتح الشفاف عالي التباين",
            AppLanguage.AZERBAIJANI to "Yüksək kontrastlı təmiz açıq rejim"
        ),
        "theme_system" to mapOf(
            AppLanguage.PERSIAN to "مطابق تنظیمات دستگاه اندرویدی",
            AppLanguage.ENGLISH to "Follow Android System",
            AppLanguage.ARABIC to "مطابق لإعدادات جهاز أندرويد",
            AppLanguage.AZERBAIJANI to "Android sistem tənzimləmələrinə uyğun"
        ),
        "theme_system_desc" to mapOf(
            AppLanguage.PERSIAN to "هماهنگی خودکار با حالت تاریک/روشن گوشی",
            AppLanguage.ENGLISH to "Automatically matches device settings",
            AppLanguage.ARABIC to "المطابقة التلقائية مع إعدادات هاتفك",
            AppLanguage.AZERBAIJANI to "Cihazın rejiminə uyğun avtomatik tənzimlənir"
        ),
        "theme_system_short" to mapOf(
            AppLanguage.PERSIAN to "مطابق دستگاه",
            AppLanguage.ENGLISH to "System",
            AppLanguage.ARABIC to "تلقائي",
            AppLanguage.AZERBAIJANI to "Sistem"
        ),
        "theme_close" to mapOf(
            AppLanguage.PERSIAN to "بستن",
            AppLanguage.ENGLISH to "Close",
            AppLanguage.ARABIC to "إغلاق",
            AppLanguage.AZERBAIJANI to "Bağla"
        ),
        "font_scale_title" to mapOf(
            AppLanguage.PERSIAN to "تناسب اندازه فونت",
            AppLanguage.ENGLISH to "Font Size Scale",
            AppLanguage.ARABIC to "مقياس حجم الخط",
            AppLanguage.AZERBAIJANI to "Şrift ölçüsü nisbəti"
        ),
        "font_scale_desc" to mapOf(
            AppLanguage.PERSIAN to "تنظیم اندازه نوشته‌ها در تمام بخش‌های برنامه",
            AppLanguage.ENGLISH to "Adjust text size across the entire app",
            AppLanguage.ARABIC to "ضبط حجم النصوص في جميع أقسام التطبيق",
            AppLanguage.AZERBAIJANI to "Bütün tətbiqdə mətn ölçüsünü tənzimləyin"
        ),
        "font_scale_small" to mapOf(
            AppLanguage.PERSIAN to "کوچک",
            AppLanguage.ENGLISH to "Small",
            AppLanguage.ARABIC to "صغير",
            AppLanguage.AZERBAIJANI to "Kiçik"
        ),
        "font_scale_normal" to mapOf(
            AppLanguage.PERSIAN to "عادی",
            AppLanguage.ENGLISH to "Normal",
            AppLanguage.ARABIC to "عادي",
            AppLanguage.AZERBAIJANI to "Normal"
        ),
        "font_scale_large" to mapOf(
            AppLanguage.PERSIAN to "بزرگ",
            AppLanguage.ENGLISH to "Large",
            AppLanguage.ARABIC to "كبير",
            AppLanguage.AZERBAIJANI to "Böyük"
        ),
        "font_scale_larger" to mapOf(
            AppLanguage.PERSIAN to "بزرگتر",
            AppLanguage.ENGLISH to "Larger",
            AppLanguage.ARABIC to "أكبر",
            AppLanguage.AZERBAIJANI to "Daha böyük"
        ),
        "pick_from_device" to mapOf(
            AppLanguage.PERSIAN to "انتخاب آهنگ از حافظه دستگاه",
            AppLanguage.ENGLISH to "Choose from Device Storage",
            AppLanguage.ARABIC to "اختيار نغمة من ذاكرة الجهاز",
            AppLanguage.AZERBAIJANI to "Cihaz yaddaşından musiqi seçin"
        ),
        "custom_device_sound" to mapOf(
            AppLanguage.PERSIAN to "فایل صوتی انتخابی شما",
            AppLanguage.ENGLISH to "Selected Audio File",
            AppLanguage.ARABIC to "الملف الصوتي المختار",
            AppLanguage.AZERBAIJANI to "Seçilmiş audio fayl"
        ),
        "change_audio_file" to mapOf(
            AppLanguage.PERSIAN to "تغییر آهنگ",
            AppLanguage.ENGLISH to "Change Song",
            AppLanguage.ARABIC to "تغيير النغمة",
            AppLanguage.AZERBAIJANI to "Musiqini dəyiş"
        ),
        "builtin_sounds" to mapOf(
            AppLanguage.PERSIAN to "آهنگ‌های پیش‌فرض",
            AppLanguage.ENGLISH to "Built-in Sounds",
            AppLanguage.ARABIC to "النغمات الافتراضية",
            AppLanguage.AZERBAIJANI to "Daxili səslər"
        )
    )
}
