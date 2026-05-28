package com.example.feature.hadith

data class Hadith(
    val id: Int,
    val bookNumber: Int,
    val bookName: String,
    val number: Int,
    val narrator: String,
    val textAr: String,
    val textEn: String,
    val chapterAr: String,
    val chapterEn: String
)

object HadithData {
    val rabiHadithsList = listOf(
        Hadith(
            id = 1,
            bookNumber = 1,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 1,
            narrator = "الربيع بن حبيب عن أبي عبيدة عن جابر بن زيد عن ابن عباس",
            textAr = "قَالَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: «إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى».",
            textEn = "The Messenger of Allah (PBUH) said: 'Actions are indeed judged by intentions, and every person shall have what he intended.'",
            chapterAr = "كتاب النيّة",
            chapterEn = "Book of Intention"
        ),
        Hadith(
            id = 2,
            bookNumber = 1,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 12,
            narrator = "الربيع عن أبي عبيدة عن جابر بن زيد عن أنس بن مالك",
            textAr = "عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «تَفَكَّرُوا فِي خَلْقِ اللَّهِ وَلاَ تَتَفَكَّرُوا فِي اللَّهِ فَتَهْلِكُوا».",
            textEn = "The Prophet (PBUH) said: 'Ponder on the creation of Allah, but do not ponder about [the essence of] Allah, lest you ruin yourselves.'",
            chapterAr = "كتاب الإيمان والتوحيد",
            chapterEn = "Book of Faith & Monotheism"
        ),
        Hadith(
            id = 3,
            bookNumber = 1,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 37,
            narrator = "الربيع عن أبي عبيدة عن جابر بن زيد عن أبي سعيد الخدري",
            textAr = "عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ».",
            textEn = "On the authority of the Prophet (PBUH) who said: 'Purity is half of faith, and Al-Hamdulillah (praise be to Allah) fills the Scale [of good deeds].'",
            chapterAr = "كتاب الطهارة والوضوء",
            chapterEn = "Book of Purity & Ablution"
        ),
        Hadith(
            id = 4,
            bookNumber = 1,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 98,
            narrator = "الربيع عن أبي عبيدة عن جابر بن زيد عن ابن عمر",
            textAr = "عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ إِذَا كَانَ فِي الصَّلاَةِ، فَإِذَا صَلَّيْتُمْ فَأَكْثِرُوا الدُّعَاءَ».",
            textEn = "The Prophet (PBUH) said: 'The closest a servant gets to his Lord is when he is in prayer. So, when you pray, make abundant supplications.'",
            chapterAr = "كتاب الصلاة والخشوع",
            chapterEn = "Book of Salat & Devotion"
        ),
        Hadith(
            id = 5,
            bookNumber = 2,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 180,
            narrator = "الربيع عن أبي عبيدة عن جابر بن زيد عن عائشة أم المؤمنين",
            textAr = "عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ».",
            textEn = "The Prophet (PBUH) said: 'The most beloved deeds to Allah are those that are most consistent, even if they are few.'",
            chapterAr = "كتاب العمل الصالح",
            chapterEn = "Book of Good Deeds"
        ),
        Hadith(
            id = 6,
            bookNumber = 2,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 245,
            narrator = "الربيع عن أبي عبيدة عن جابر بن زيد عن جابر بن عبد الله",
            textAr = "عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «مَنْ لَمْ يَرْحَمِ النَّاسَ لاَ يَرْحَمْهُ اللَّهُ».",
            textEn = "The Prophet (PBUH) said: 'He who does not show mercy to people, Allah will not show mercy to him.'",
            chapterAr = "كتاب الآداب والرحمة",
            chapterEn = "Book of Morals & Mercy"
        ),
        Hadith(
            id = 7,
            bookNumber = 3,
            bookName = "الجامع الصحيح للربيع بن حبيب",
            number = 301,
            narrator = "الربيع عن أبي عبيدة عن جابر بن زيد عن ابن عمر",
            textAr = "قَالَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ: «طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ».",
            textEn = "The Messenger of Allah (PBUH) said: 'Seeking knowledge is a mandatory duty upon every Muslim.'",
            chapterAr = "كتاب العلم وفضله",
            chapterEn = "Book of Knowledge & its Virtue"
        )
    )
}
