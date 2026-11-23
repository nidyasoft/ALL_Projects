package com.example.data.repository

import com.example.data.local.AlarmDao
import com.example.data.local.TimerDao
import com.example.data.local.WorldCityDao
import com.example.data.model.AlarmEntity
import com.example.data.model.TimerEntity
import com.example.data.model.WorldCityEntity
import kotlinx.coroutines.flow.Flow
import java.util.TimeZone

data class WorldCityPreset(
    val cityNameFa: String,
    val cityNameEn: String,
    val cityNameAr: String,
    val cityNameAz: String,
    val countryName: String,
    val timezoneId: String
)

object WorldCityPresets {
    val allPresets = listOf(
        // --- ایران (Iran) ---
        WorldCityPreset("تهران", "Tehran", "طهران", "Tehran", "Iran", "Asia/Tehran"),
        WorldCityPreset("تبریز", "Tabriz", "تبريز", "Təbriz", "Iran", "Asia/Tehran"),
        WorldCityPreset("مشهد", "Mashhad", "مشهد", "Məşhəd", "Iran", "Asia/Tehran"),
        WorldCityPreset("اصفهان", "Isfahan", "أصفهان", "İsfahan", "Iran", "Asia/Tehran"),
        WorldCityPreset("شیراز", "Shiraz", "شيراز", "Şiraz", "Iran", "Asia/Tehran"),
        WorldCityPreset("اهواز", "Ahvaz", "الأهواز", "Əhvaz", "Iran", "Asia/Tehran"),
        WorldCityPreset("قم", "Qom", "قم", "Qum", "Iran", "Asia/Tehran"),
        WorldCityPreset("کرمانشاه", "Kermanshah", "كرمانشاه", "Kermanşah", "Iran", "Asia/Tehran"),
        WorldCityPreset("رشت", "Rasht", "رشت", "Rəşt", "Iran", "Asia/Tehran"),
        WorldCityPreset("زاهدان", "Zahedan", "زاهدان", "Zahidan", "Iran", "Asia/Tehran"),
        WorldCityPreset("کرمان", "Kerman", "كرمان", "Kirman", "Iran", "Asia/Tehran"),
        WorldCityPreset("ارومیه", "Urmia", "أورمية", "Urmiya", "Iran", "Asia/Tehran"),
        WorldCityPreset("یزد", "Yazd", "يزد", "Yəzd", "Iran", "Asia/Tehran"),
        WorldCityPreset("بندرعباس", "Bandar Abbas", "بندر عباس", "Bəndər Abbas", "Iran", "Asia/Tehran"),
        WorldCityPreset("کیش", "Kish", "كيش", "Kiş", "Iran", "Asia/Tehran"),
        WorldCityPreset("همدان", "Hamadan", "همدان", "Həmədan", "Iran", "Asia/Tehran"),
        WorldCityPreset("اردبیل", "Ardabil", "أردبيل", "Ərdəbil", "Iran", "Asia/Tehran"),
        WorldCityPreset("قزوین", "Qazvin", "قزوين", "Qəzvin", "Iran", "Asia/Tehran"),
        WorldCityPreset("زنجان", "Zanjan", "زنجان", "Zəncan", "Iran", "Asia/Tehran"),

        // --- خاورمیانه و قفقاز (Middle East & Caucasus) ---
        WorldCityPreset("دبی", "Dubai", "دبي", "Dubay", "UAE", "Asia/Dubai"),
        WorldCityPreset("ابوظبی", "Abu Dhabi", "أبو ظبي", "Əbu-Dabi", "UAE", "Asia/Dubai"),
        WorldCityPreset("دوحه", "Doha", "الدوحة", "Doha", "Qatar", "Asia/Qatar"),
        WorldCityPreset("ریاض", "Riyadh", "الرياض", "Riyad", "Saudi Arabia", "Asia/Riyadh"),
        WorldCityPreset("جده", "Jeddah", "جدة", "Ciddə", "Saudi Arabia", "Asia/Riyadh"),
        WorldCityPreset("مکه", "Mecca", "مكة المكرمة", "Məkkə", "Saudi Arabia", "Asia/Riyadh"),
        WorldCityPreset("مدینه", "Medina", "المدينة المنورة", "Mədinə", "Saudi Arabia", "Asia/Riyadh"),
        WorldCityPreset("بغداد", "Baghdad", "بغداد", "Bağdad", "Iraq", "Asia/Baghdad"),
        WorldCityPreset("بصره", "Basra", "البصرة", "Bəsrə", "Iraq", "Asia/Baghdad"),
        WorldCityPreset("اربیل", "Erbil", "أربيل", "Ərbil", "Iraq", "Asia/Baghdad"),
        WorldCityPreset("کویت", "Kuwait City", "مدينة الكويت", "Küveyt", "Kuwait", "Asia/Kuwait"),
        WorldCityPreset("منامه", "Manama", "المنامة", "Manama", "Bahrain", "Asia/Bahrain"),
        WorldCityPreset("مسقط", "Muscat", "مسقط", "Məsqət", "Oman", "Asia/Muscat"),
        WorldCityPreset("امان", "Amman", "عمان", "Əmman", "Jordan", "Asia/Amman"),
        WorldCityPreset("بیروت", "Beirut", "بيروت", "Beyrut", "Lebanon", "Asia/Beirut"),
        WorldCityPreset("دمشق", "Damascus", "دمشق", "Dəməşq", "Syria", "Asia/Damascus"),
        WorldCityPreset("قدس", "Jerusalem", "القدس", "Qüds", "Palestine", "Asia/Jerusalem"),
        WorldCityPreset("استانبول", "Istanbul", "إسطنبول", "İstanbul", "Turkey", "Europe/Istanbul"),
        WorldCityPreset("آنکارا", "Ankara", "أنقرة", "Ankara", "Turkey", "Europe/Istanbul"),
        WorldCityPreset("ازمیر", "Izmir", "إزمير", "İzmir", "Turkey", "Europe/Istanbul"),
        WorldCityPreset("باکو", "Baku", "باكو", "Bakı", "Azerbaijan", "Asia/Baku"),
        WorldCityPreset("ایروان", "Yerevan", "يريفان", "İrəvan", "Armenia", "Asia/Yerevan"),
        WorldCityPreset("تفلیس", "Tbilisi", "تبليسي", "Tbilisi", "Georgia", "Asia/Tbilisi"),
        WorldCityPreset("کابل", "Kabul", "كابول", "Kabil", "Afghanistan", "Asia/Kabul"),

        // --- اروپا (Europe) ---
        WorldCityPreset("لندن", "London", "لندن", "London", "United Kingdom", "Europe/London"),
        WorldCityPreset("پاریس", "Paris", "باريس", "Paris", "France", "Europe/Paris"),
        WorldCityPreset("برلین", "Berlin", "برلين", "Berlin", "Germany", "Europe/Berlin"),
        WorldCityPreset("فرانکفورت", "Frankfurt", "فرانكفورت", "Frankfurt", "Germany", "Europe/Berlin"),
        WorldCityPreset("رم", "Rome", "روما", "Roma", "Italy", "Europe/Rome"),
        WorldCityPreset("میلان", "Milan", "ميلانو", "Milan", "Italy", "Europe/Rome"),
        WorldCityPreset("مادرید", "Madrid", "مدريد", "Madrid", "Spain", "Europe/Madrid"),
        WorldCityPreset("بارسلونا", "Barcelona", "برشلونة", "Barselona", "Spain", "Europe/Madrid"),
        WorldCityPreset("آمستردام", "Amsterdam", "أمستردام", "Amsterdam", "Netherlands", "Europe/Amsterdam"),
        WorldCityPreset("بروکسل", "Brussels", "بروكسل", "Brüssel", "Belgium", "Europe/Brussels"),
        WorldCityPreset("وین", "Vienna", "فيينا", "Vyana", "Austria", "Europe/Vienna"),
        WorldCityPreset("زوریخ", "Zurich", "زيورخ", "Sürix", "Switzerland", "Europe/Zurich"),
        WorldCityPreset("ژنو", "Geneva", "جنيف", "Cenevrə", "Switzerland", "Europe/Zurich"),
        WorldCityPreset("استکهلم", "Stockholm", "ستوكهولم", "Stokholm", "Sweden", "Europe/Stockholm"),
        WorldCityPreset("اسلو", "Oslo", "أوسلو", "Oslo", "Norway", "Europe/Oslo"),
        WorldCityPreset("کپنهاگ", "Copenhagen", "كوبنهاغن", "Kopenhagen", "Denmark", "Europe/Copenhagen"),
        WorldCityPreset("هلسینکی", "Helsinki", "هلسنكي", "Helsinki", "Finland", "Europe/Helsinki"),
        WorldCityPreset("دوبلین", "Dublin", "دبلن", "Dublin", "Ireland", "Europe/Dublin"),
        WorldCityPreset("لیسبون", "Lisbon", "لشبونة", "Lissabon", "Portugal", "Europe/Lisbon"),
        WorldCityPreset("آتن", "Athens", "أثينا", "Afina", "Greece", "Europe/Athens"),
        WorldCityPreset("ورشو", "Warsaw", "وارسو", "Varşava", "Poland", "Europe/Warsaw"),
        WorldCityPreset("پراگ", "Prague", "براغ", "Praqa", "Czech Republic", "Europe/Prague"),
        WorldCityPreset("بوداپست", "Budapest", "بودابست", "Budapeşt", "Hungary", "Europe/Budapest"),
        WorldCityPreset("بخارست", "Bucharest", "بوخارست", "Buxarest", "Romania", "Europe/Bucharest"),
        WorldCityPreset("بلگراد", "Belgrade", "بلغراد", "Belqrad", "Serbia", "Europe/Belgrade"),
        WorldCityPreset("کی‌یف", "Kyiv", "كييف", "Kiyev", "Ukraine", "Europe/Kyiv"),
        WorldCityPreset("مسکو", "Moscow", "موسكو", "Moskva", "Russia", "Europe/Moscow"),
        WorldCityPreset("سن‌پترزبورگ", "Saint Petersburg", "سانت بطرسبرغ", "Sankt-Peterburq", "Russia", "Europe/Moscow"),
        WorldCityPreset("ریکیاویک", "Reykjavik", "ريكيافيك", "Reykyavik", "Iceland", "Atlantic/Reykjavik"),

        // --- آمریکای شمالی (North America) ---
        WorldCityPreset("نیویورک", "New York", "نيويورك", "Nyu York", "United States", "America/New_York"),
        WorldCityPreset("واشنگتن", "Washington DC", "واشنطن العاصمة", "Vaşinqton", "United States", "America/New_York"),
        WorldCityPreset("لس آنجلس", "Los Angeles", "لوس أنجلوس", "Los-Anceles", "United States", "America/Los_Angeles"),
        WorldCityPreset("سانفرانسیسکو", "San Francisco", "سان فرانسيسكو", "San-Fransisko", "United States", "America/Los_Angeles"),
        WorldCityPreset("شیکاگو", "Chicago", "شيكاغو", "Çikaqo", "United States", "America/Chicago"),
        WorldCityPreset("میامی", "Miami", "ميامي", "Mayami", "United States", "America/New_York"),
        WorldCityPreset("بوستون", "Boston", "بوسطن", "Boston", "United States", "America/New_York"),
        WorldCityPreset("سیاتل", "Seattle", "سياتل", "Sietl", "United States", "America/Los_Angeles"),
        WorldCityPreset("دنور", "Denver", "دنفر", "Denver", "United States", "America/Denver"),
        WorldCityPreset("هیوستون", "Houston", "هيوستن", "Hyuston", "United States", "America/Chicago"),
        WorldCityPreset("آتلانتا", "Atlanta", "أتلانتا", "Atlanta", "United States", "America/New_York"),
        WorldCityPreset("لاس وگاس", "Las Vegas", "لاس فيغاس", "Las-Veqas", "United States", "America/Los_Angeles"),
        WorldCityPreset("فونیکس", "Phoenix", "فينيكس", "Feniks", "United States", "America/Phoenix"),
        WorldCityPreset("هونولولو (هاوایی)", "Honolulu", "هونولولو", "Honolulu", "United States", "Pacific/Honolulu"),
        WorldCityPreset("انکوریج (آلاسکا)", "Anchorage", "أنكوريج", "Ankoric", "United States", "America/Anchorage"),
        WorldCityPreset("تورنتو", "Toronto", "تورونتو", "Toronto", "Canada", "America/Toronto"),
        WorldCityPreset("ونکوور", "Vancouver", "فانكوفر", "Vankuver", "Canada", "America/Vancouver"),
        WorldCityPreset("مونترال", "Montreal", "مونتريال", "Monreal", "Canada", "America/Toronto"),
        WorldCityPreset("مکزیکوسیتی", "Mexico City", "مدينة مكسيكو", "Mexiko", "Mexico", "America/Mexico_City"),
        WorldCityPreset("هاوانا", "Havana", "هافانا", "Havana", "Cuba", "America/Havana"),
        WorldCityPreset("پاناما", "Panama City", "بنما", "Panama", "Panama", "America/Panama"),

        // --- آمریکای جنوبی (South America) ---
        WorldCityPreset("سائوپائولو", "Sao Paulo", "ساو باولو", "San-Paulu", "Brazil", "America/Sao_Paulo"),
        WorldCityPreset("ریودوژانیرو", "Rio de Janeiro", "ريو دي جانيرو", "Rio-de-Janeyro", "Brazil", "America/Sao_Paulo"),
        WorldCityPreset("بوئنوس آیرس", "Buenos Aires", "بوينس آيرس", "Buenos-Ayres", "Argentina", "America/Argentina/Buenos_Aires"),
        WorldCityPreset("بوگوتا", "Bogota", "بوغوتا", "Boqota", "Colombia", "America/Bogota"),
        WorldCityPreset("لیما", "Lima", "ليما", "Lima", "Peru", "America/Lima"),
        WorldCityPreset("سانتیاگو", "Santiago", "سانتياغو", "Santyaqo", "Chile", "America/Santiago"),
        WorldCityPreset("کاراکاس", "Caracas", "كاراكاس", "Karakas", "Venezuela", "America/Caracas"),
        WorldCityPreset("مونته‌ویدئو", "Montevideo", "مونتفيدو", "Montevideo", "Uruguay", "America/Montevideo"),

        // --- آسیا و اقیانوسیه (Asia & Pacific) ---
        WorldCityPreset("توکیو", "Tokyo", "طوكيو", "Tokyo", "Japan", "Asia/Tokyo"),
        WorldCityPreset("اوزاکا", "Osaka", "أوساكا", "Osaka", "Japan", "Asia/Tokyo"),
        WorldCityPreset("سئول", "Seoul", "سيول", "Seul", "South Korea", "Asia/Seoul"),
        WorldCityPreset("پکن", "Beijing", "بكين", "Pekin", "China", "Asia/Shanghai"),
        WorldCityPreset("شانگهای", "Shanghai", "شانغهاي", "Şanxay", "China", "Asia/Shanghai"),
        WorldCityPreset("گوانگژو", "Guangzhou", "قوانغتشو", "Quanqjou", "China", "Asia/Shanghai"),
        WorldCityPreset("هنگ کنگ", "Hong Kong", "هونغ كونغ", "Honq Konq", "Hong Kong", "Asia/Hong_Kong"),
        WorldCityPreset("تایپه", "Taipei", "تايبيه", "Taybey", "Taiwan", "Asia/Taipei"),
        WorldCityPreset("سنگاپور", "Singapore", "سنغافورة", "Sinqapur", "Singapore", "Asia/Singapore"),
        WorldCityPreset("کوالالامپور", "Kuala Lumpur", "كوالالمبور", "Kuala-Lumpur", "Malaysia", "Asia/Kuala_Lumpur"),
        WorldCityPreset("بانکوک", "Bangkok", "بانكوك", "Banqkok", "Thailand", "Asia/Bangkok"),
        WorldCityPreset("جاکارتا", "Jakarta", "جاكرتا", "Cakarta", "Indonesia", "Asia/Jakarta"),
        WorldCityPreset("مانیل", "Manila", "مانيلا", "Manila", "Philippines", "Asia/Manila"),
        WorldCityPreset("هانوی", "Hanoi", "هانوي", "Hanoy", "Vietnam", "Asia/Ho_Chi_Minh"),
        WorldCityPreset("دهلی نو", "New Delhi", "نيودلهي", "Yeni Dehli", "India", "Asia/Kolkata"),
        WorldCityPreset("بمبئی", "Mumbai", "مومباي", "Mumbay", "India", "Asia/Kolkata"),
        WorldCityPreset("بنگلور", "Bengaluru", "بنغالور", "Benqaluru", "India", "Asia/Kolkata"),
        WorldCityPreset("کراچی", "Karachi", "كراتشي", "Kəraçi", "Pakistan", "Asia/Karachi"),
        WorldCityPreset("لاهور", "Lahore", "لاهور", "Lahor", "Pakistan", "Asia/Karachi"),
        WorldCityPreset("اسلام آباد", "Islamabad", "إسلام آباد", "İslamabad", "Pakistan", "Asia/Karachi"),
        WorldCityPreset("داکا", "Dhaka", "دكا", "Dəkkə", "Bangladesh", "Asia/Dhaka"),
        WorldCityPreset("کلمبو", "Colombo", "كولومبو", "Kolombo", "Sri Lanka", "Asia/Colombo"),
        WorldCityPreset("کاتماندو", "Kathmandu", "كاتماندو", "Katmandu", "Nepal", "Asia/Kathmandu"),
        WorldCityPreset("تاشکند", "Tashkent", "طشقند", "Daşkənd", "Uzbekistan", "Asia/Tashkent"),
        WorldCityPreset("آلماتی", "Almaty", "ألماتي", "Almatı", "Kazakhstan", "Asia/Almaty"),
        WorldCityPreset("آستانه", "Astana", "أستانا", "Astana", "Kazakhstan", "Asia/Almaty"),

        // --- اقیانوسیه (Oceania) ---
        WorldCityPreset("سیدنی", "Sydney", "سيدني", "Sidney", "Australia", "Australia/Sydney"),
        WorldCityPreset("ملبورن", "Melbourne", "ملبورن", "Melburn", "Australia", "Australia/Melbourne"),
        WorldCityPreset("بریزبن", "Brisbane", "بريزبان", "Brisben", "Australia", "Australia/Brisbane"),
        WorldCityPreset("پرت", "Perth", "بيرث", "Pert", "Australia", "Australia/Perth"),
        WorldCityPreset("آدلاید", "Adelaide", "أديلايد", "Adelaida", "Australia", "Australia/Adelaide"),
        WorldCityPreset("اوکلند", "Auckland", "أوكلاند", "Oklend", "New Zealand", "Pacific/Auckland"),
        WorldCityPreset("ولینگتون", "Wellington", "ويلينغتون", "Vellinqton", "New Zealand", "Pacific/Auckland"),

        // --- آفریقا (Africa) ---
        WorldCityPreset("قاهره", "Cairo", "القاهرة", "Qahirə", "Egypt", "Africa/Cairo"),
        WorldCityPreset("اسکندریه", "Alexandria", "الإسكندرية", "İsgəndəriyyə", "Egypt", "Africa/Cairo"),
        WorldCityPreset("ژوهانسبورگ", "Johannesburg", "جوهانسبرغ", "Yohannesburq", "South Africa", "Africa/Johannesburg"),
        WorldCityPreset("کیپ تاون", "Cape Town", "كيب تاون", "Keyptaun", "South Africa", "Africa/Johannesburg"),
        WorldCityPreset("نایروبی", "Nairobi", "نيروبي", "Nayrobi", "Kenya", "Africa/Nairobi"),
        WorldCityPreset("لاگوس", "Lagos", "لاغوس", "Laqos", "Nigeria", "Africa/Lagos"),
        WorldCityPreset("کازابلانکا", "Casablanca", "الدار البيضاء", "Kasablanka", "Morocco", "Africa/Casablanca"),
        WorldCityPreset("رباط", "Rabat", "الرباط", "Rabat", "Morocco", "Africa/Casablanca"),
        WorldCityPreset("تونس", "Tunis", "تونس", "Tunis", "Tunisia", "Africa/Tunis"),
        WorldCityPreset("الجزیره", "Algiers", "الجزائر", "Əlcəzair", "Algeria", "Africa/Algiers"),
        WorldCityPreset("آدیس آبابا", "Addis Ababa", "أديس أبابا", "Əddis-Əbəbə", "Ethiopia", "Africa/Addis_Ababa"),
        WorldCityPreset("داکار", "Dakar", "داكار", "Dakar", "Senegal", "Africa/Dakar")
    )
}

class AppRepository(
    private val alarmDao: AlarmDao,
    private val timerDao: TimerDao,
    private val worldCityDao: WorldCityDao
) {
    val alarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    val timers: Flow<List<TimerEntity>> = timerDao.getAllTimers()
    val worldCities: Flow<List<WorldCityEntity>> = worldCityDao.getAllCities()

    suspend fun saveAlarm(alarm: AlarmEntity): Long = alarmDao.insert(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.update(alarm)
    suspend fun updateAllAlarms(alarms: List<AlarmEntity>) = alarmDao.updateAll(alarms)
    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.delete(alarm)
    suspend fun deleteAlarms(ids: List<Long>) = alarmDao.deleteByIds(ids)
    suspend fun deleteAllAlarms() = alarmDao.deleteAllAlarms()
    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    suspend fun saveTimer(timer: TimerEntity): Long = timerDao.insert(timer)
    suspend fun updateTimer(timer: TimerEntity) = timerDao.update(timer)
    suspend fun updateAllTimers(timers: List<TimerEntity>) = timerDao.updateAll(timers)
    suspend fun deleteTimer(timer: TimerEntity) = timerDao.delete(timer)
    suspend fun deleteTimers(ids: List<Long>) = timerDao.deleteByIds(ids)
    suspend fun deleteAllTimers() = timerDao.deleteAllTimers()
    suspend fun getTimerById(id: Long): TimerEntity? = timerDao.getTimerById(id)

    suspend fun addWorldCity(city: WorldCityEntity): Long = worldCityDao.insert(city)
    suspend fun updateAllCities(cities: List<WorldCityEntity>) = worldCityDao.updateAll(cities)
    suspend fun deleteWorldCity(city: WorldCityEntity) = worldCityDao.delete(city)
    suspend fun deleteWorldCities(ids: List<Long>) = worldCityDao.deleteByIds(ids)

    suspend fun seedInitialCitiesIfEmpty() {
        try {
            if (worldCityDao.getCount() == 0) {
                val initial = listOf(
                    WorldCityPresets.allPresets[0], // Tehran
                    WorldCityPresets.allPresets[1], // London
                    WorldCityPresets.allPresets[2], // New York
                    WorldCityPresets.allPresets[3], // Tokyo
                    WorldCityPresets.allPresets[4], // Dubai
                    WorldCityPresets.allPresets[6]  // Baku
                ).mapIndexed { index, preset ->
                    WorldCityEntity(
                        cityNameFa = preset.cityNameFa,
                        cityNameEn = preset.cityNameEn,
                        cityNameAr = preset.cityNameAr,
                        cityNameAz = preset.cityNameAz,
                        countryName = preset.countryName,
                        timezoneId = preset.timezoneId,
                        orderIndex = index
                    )
                }
                worldCityDao.insertAll(initial)
            }
        } catch (_: Throwable) {}
    }
}
