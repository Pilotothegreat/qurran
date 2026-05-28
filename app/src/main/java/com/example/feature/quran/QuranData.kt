package com.example.feature.quran

data class Surah(
    val number: Int,
    val nameEn: String,
    val nameAr: String,
    val versesCount: Int,
    val type: String, // "Meccan" or "Medinan"
    val englishMeaning: String
)

data class QuranVerse(
    val number: Int,
    val textAr: String,
    val textEn: String,
    val textSw: String
)

object QuranData {
    val fullSurahHeadersList = listOf(
        Surah(1, "Al-Fatihah", "الفاتحة", 7, "Meccan", "The Opening"),
        Surah(2, "Al-Baqarah", "البقرة", 286, "Medinan", "The Cow"),
        Surah(3, "Ali 'Imran", "آل عمران", 200, "Medinan", "Family of Imran"),
        Surah(4, "An-Nisa", "النساء", 176, "Medinan", "The Women"),
        Surah(5, "Al-Ma'idah", "المائدة", 120, "Medinan", "The Table Spread"),
        Surah(6, "Al-An'am", "الأنعام", 165, "Meccan", "The Cattle"),
        Surah(7, "Al-A'raf", "الأعراف", 206, "Meccan", "The Heights"),
        Surah(8, "Al-Anfal", "الأنفال", 75, "Medinan", "The Spoils of War"),
        Surah(9, "At-Tawbah", "التوبة", 129, "Medinan", "The Repentance"),
        Surah(10, "Yunus", "يونس", 109, "Meccan", "Jonah"),
        Surah(11, "Hud", "هود", 123, "Meccan", "Hud"),
        Surah(12, "Yusuf", "يوسف", 111, "Meccan", "Joseph"),
        Surah(13, "Ar-Ra'd", "الرعد", 43, "Medinan", "The Thunder"),
        Surah(14, "Ibrahim", "إبراهيم", 52, "Meccan", "Abraham"),
        Surah(15, "Al-Hijr", "الحجر", 99, "Meccan", "The Rocky Tract"),
        Surah(16, "An-Nahl", "النحل", 128, "Meccan", "The Bee"),
        Surah(17, "Al-Isra", "الإسراء", 111, "Meccan", "The Night Journey"),
        Surah(18, "Al-Kahf", "الكهف", 110, "Meccan", "The Cave"),
        Surah(19, "Maryam", "مريم", 98, "Meccan", "Mary"),
        Surah(20, "Taha", "طه", 135, "Meccan", "Ta-Ha"),
        Surah(21, "Al-Anbya", "الأنبياء", 112, "Meccan", "The Prophets"),
        Surah(22, "Al-Hajj", "الحج", 78, "Medinan", "The Pilgrimage"),
        Surah(23, "Al-Mu'minun", "المؤمنون", 118, "Meccan", "The Believers"),
        Surah(24, "An-Nur", "النور", 64, "Medinan", "The Light"),
        Surah(25, "Al-Furqan", "الفرقان", 77, "Meccan", "The Criterion"),
        Surah(26, "Ash-Shu'ara", "الشعراء", 227, "Meccan", "The Poets"),
        Surah(27, "An-Naml", "النمل", 93, "Meccan", "The Ant"),
        Surah(28, "Al-Qasas", "القصص", 88, "Meccan", "The Stories"),
        Surah(29, "Al-'Ankabut", "العنكبوت", 69, "Meccan", "The Spider"),
        Surah(30, "Ar-Rum", "الروم", 60, "Meccan", "The Romans"),
        Surah(31, "Luqman", "لقمان", 34, "Meccan", "Luqman"),
        Surah(32, "As-Sajdah", "السجدة", 30, "Meccan", "The Prostration"),
        Surah(33, "Al-Ahzab", "الأحزاب", 73, "Medinan", "The Combined Forces"),
        Surah(34, "Saba", "سبأ", 54, "Meccan", "Sheba"),
        Surah(35, "Fatir", "فاطر", 45, "Meccan", "Originator"),
        Surah(36, "Yaseen", "يس", 83, "Meccan", "Ya Seen"),
        Surah(37, "As-Saffat", "الصافات", 182, "Meccan", "Those who set the Ranks"),
        Surah(38, "Sad", "ص", 88, "Meccan", "The Letter Sad"),
        Surah(39, "Az-Zumar", "الزمر", 75, "Meccan", "The Troops"),
        Surah(40, "Ghafir", "غافر", 85, "Meccan", "The Forgiver"),
        Surah(41, "Fussilat", "فصلت", 54, "Meccan", "Explained in Detail"),
        Surah(42, "Ash-Shura", "الشورى", 53, "Meccan", "The Consultation"),
        Surah(43, "Az-Zukhruf", "الزخرف", 89, "Meccan", "The Ornaments of Gold"),
        Surah(44, "Ad-Dukhan", "الدخان", 59, "Meccan", "The Smoke"),
        Surah(45, "Al-Jathiyah", "الجاثية", 37, "Meccan", "The Crouching"),
        Surah(46, "Al-Ahqaf", "الأحقاف", 35, "Meccan", "The Wind-Curved Sandhills"),
        Surah(47, "Muhammad", "محمد", 38, "Medinan", "Muhammad"),
        Surah(48, "Al-Fath", "الفتح", 29, "Medinan", "The Victory"),
        Surah(49, "Al-Hujurat", "الحجرات", 18, "Medinan", "The Dwellings"),
        Surah(50, "Qaf", "ق", 45, "Meccan", "The Letter Qaf"),
        Surah(51, "Adh-Dhariyat", "الذاريات", 60, "Meccan", "The Winnowing Winds"),
        Surah(52, "At-Tur", "الطور", 49, "Meccan", "The Mount"),
        Surah(53, "An-Najm", "النجم", 62, "Meccan", "The Star"),
        Surah(54, "Al-Qamar", "القمر", 55, "Meccan", "The Moon"),
        Surah(55, "Ar-Rahman", "الرحمن", 78, "Medinan", "The Beneficent"),
        Surah(56, "Al-Waqi'ah", "الواقعة", 96, "Meccan", "The Inevitable"),
        Surah(57, "Al-Hadid", "الحديد", 29, "Medinan", "The Iron"),
        Surah(58, "Al-Mujadilah", "المجادلة", 22, "Medinan", "The Pleading Woman"),
        Surah(59, "Al-Hashr", "الحشر", 24, "Medinan", "The Exile"),
        Surah(60, "Al-Mumtahanah", "الممتحنة", 13, "Medinan", "She that is to be examined"),
        Surah(61, "As-Saff", "الصف", 14, "Medinan", "The Ranks"),
        Surah(62, "Al-Jumu'ah", "الجمعة", 11, "Medinan", "The Congregation"),
        Surah(63, "Al-Munafiqun", "المنافقون", 11, "Medinan", "The Hypocrites"),
        Surah(64, "At-Taghabun", "التغابن", 18, "Medinan", "The Mutual Disillusion"),
        Surah(65, "At-Talaq", "الطلاق", 12, "Medinan", "The Divorce"),
        Surah(66, "At-Tahrim", "التحريم", 12, "Medinan", "The Prohibition"),
        Surah(67, "Al-Mulk", "الملك", 30, "Meccan", "The Sovereignty"),
        Surah(68, "Al-Qalam", "القلم", 52, "Meccan", "The Pen"),
        Surah(69, "Al-Haqqah", "الحاقة", 52, "Meccan", "The Inevitable Reality"),
        Surah(70, "Al-Ma'arij", "المعارج", 44, "Meccan", "The Ascending Stairways"),
        Surah(71, "Nuh", "نوح", 28, "Meccan", "Noah"),
        Surah(72, "Al-Jinn", "الجن", 28, "Meccan", "The Jinn"),
        Surah(73, "Al-Muzzammil", "المزمل", 20, "Meccan", "The Enshrouded One"),
        Surah(74, "Al-Muddaththir", "المدثر", 56, "Meccan", "The Cloaked One"),
        Surah(75, "Al-Qiyamah", "القيامة", 40, "Meccan", "The Resurrection"),
        Surah(76, "Al-Insan", "الإنسان", 31, "Medinan", "Man"),
        Surah(77, "Al-Mursalat", "المرسلات", 50, "Meccan", "The Emissaries"),
        Surah(78, "An-Naba", "النبأ", 40, "Meccan", "The Tidings"),
        Surah(79, "An-Nazi'at", "النازعات", 46, "Meccan", "Those who drag forth"),
        Surah(80, "Abasa", "عبس", 42, "Meccan", "He Frowned"),
        Surah(81, "At-Takwir", "التكوير", 29, "Meccan", "The Overthrowing"),
        Surah(82, "Al-Infitar", "الانفطار", 19, "Meccan", "The Cleaving"),
        Surah(83, "Al-Mutaffifin", "المطففين", 36, "Meccan", "The Defrauding"),
        Surah(84, "Al-Inshiqaq", "الانشقاق", 25, "Meccan", "The Sundering"),
        Surah(85, "Al-Buruj", "البروج", 22, "Meccan", "The Mansions of the Stars"),
        Surah(86, "At-Tariq", "الطارق", 17, "Meccan", "The Nightcomer"),
        Surah(87, "Al-A'la", "الأعلى", 19, "Meccan", "The Most High"),
        Surah(88, "Al-Ghashiyah", "الغاشية", 26, "Meccan", "The Overwhelming"),
        Surah(89, "Al-Fajr", "الفجر", 30, "Meccan", "The Dawn"),
        Surah(90, "Al-Balad", "البلد", 20, "Meccan", "The City"),
        Surah(91, "Ash-Shams", "الشems", 15, "Meccan", "The Sun"),
        Surah(92, "Al-Layl", "الليل", 21, "Meccan", "The Night"),
        Surah(93, "Ad-Duha", "الضحى", 11, "Meccan", "The Morning Hours"),
        Surah(94, "Ash-Sharh", "الشرح", 8, "Meccan", "The Relief"),
        Surah(95, "At-Tin", "التين", 8, "Meccan", "The Fig"),
        Surah(96, "Al-'Alaq", "العلق", 19, "Meccan", "The Clot"),
        Surah(97, "Al-Qadr", "القدر", 5, "Meccan", "The Power"),
        Surah(98, "Al-Bayyinah", "البينة", 8, "Medinan", "The Clear Proof"),
        Surah(99, "Az-Zalzalah", "الزلزلة", 8, "Medinan", "The Earthquake"),
        Surah(100, "Al-'Adiyat", "العاديات", 11, "Meccan", "The Courser"),
        Surah(101, "Al-Qari'ah", "القارعة", 11, "Meccan", "The Calamity"),
        Surah(102, "At-Takathur", "التكاثر", 8, "Meccan", "The Rivalry"),
        Surah(103, "Al-'Asr", "العصر", 3, "Meccan", "The Declining Day"),
        Surah(104, "Al-Humazah", "الهمزة", 9, "Meccan", "The Slanderer"),
        Surah(105, "Al-Fil", "الفيل", 5, "Meccan", "The Elephant"),
        Surah(106, "Quraysh", "قريش", 4, "Meccan", "Quraysh"),
        Surah(107, "Al-Ma'un", "الماعون", 7, "Meccan", "The Small Kindnesses"),
        Surah(108, "Al-Kawthar", "الكوثر", 3, "Meccan", "The Abundance"),
        Surah(109, "Al-Kafirun", "الكافرون", 6, "Meccan", "The Disbelievers"),
        Surah(110, "An-Nasr", "النصر", 3, "Medinan", "The Divine Support"),
        Surah(111, "Al-Masad", "المسد", 5, "Meccan", "The Palm Fiber"),
        Surah(112, "Al-Ikhlas", "الإخلاص", 4, "Meccan", "The Sincerity"),
        Surah(113, "Al-Falaq", "الفلق", 5, "Meccan", "The Daybreak"),
        Surah(114, "An-Nas", "الناس", 6, "Meccan", "Mankind")
    )

    val surahList = fullSurahHeadersList

    private val versesMap = mapOf(
        1 to listOf(
            QuranVerse(1, "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Kwa jina la Mwenyezi Mungu, Mwingi wa rehema, Mwenye kurehemu."),
            QuranVerse(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "All praise is [due] to Allah, Lord of the worlds -", "Sifa njema zote ni za Mwenyezi Mungu, Mola wa walimwengu wote,"),
            QuranVerse(3, "الرَّحْمَنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "Mwingi wa rehema, Mwenye kurehemu,"),
            QuranVerse(4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "Mfalme wa Siku ya Malipo (Kiama)."),
            QuranVerse(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "Wewe tu tunakuabudu, na Wewe tu tunakuomba msaada."),
            QuranVerse(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "Utuongoze njia iliyonyooka,"),
            QuranVerse(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have earned [Your] anger or of those who are astray.", "Njia ya wale uliowaneemesha, si ya wale waliokasirikiwa, wala ya wale waliopotea.")
        ),
        112 to listOf(
            QuranVerse(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, 'He is Allah, [who is] One,", "Sema: Yeye Mwenyezi Mungu ni wa pekee,"),
            QuranVerse(2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "Mwenyezi Mungu Mkusudiwa (Hajibu maombi ya wote na hazai wala hazaliwi),"),
            QuranVerse(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "Hakuzaa wala hakuzaliwa,"),
            QuranVerse(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "And there is none co-equal or comparable unto Him.'", "Wala hana anayefanana naye hata mmoja.")
        ),
        113 to listOf(
            QuranVerse(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, 'I seek refuge in the Lord of daybreak", "Sema: Najilinda kwa Mola Mlezi wa mapambazuko,"),
            QuranVerse(2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", "Na shari ya alivyoviumba,"),
            QuranVerse(3, "مِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "Na shari ya giza linapoingia,"),
            QuranVerse(4, "مِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "Na shari ya wanaopulizia kwenye fundo,"),
            QuranVerse(5, "مِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.'", "Na shari ya mhasidi anapohusudu.")
        ),
        114 to listOf(
            QuranVerse(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, 'I seek refuge in the Lord of mankind,", "Sema: Najilinda kwa Mola Mlezi wa wanaadamu,"),
            QuranVerse(2, "مَلِكِ النَّاسِ", "The Sovereign of mankind,", "Mfalme wa wanaadamu,"),
            QuranVerse(3, "إِلَهِ النَّاسِ", "The God of mankind,", "Mungu wa wanaadamu,"),
            QuranVerse(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "Na shari ya wasiwasi wa msiri anayerudi nyuma,"),
            QuranVerse(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "Ambaye hutia wasiwasi katika vifua vya watu,"),
            QuranVerse(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.'", "Kutokana na majini na wanaadamu.")
        ),
        67 to listOf(
            QuranVerse(1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "Blessed is He in whose hand is dominion, and He is over all things competent -", "Ametukuka yule ambaye mkononi mwake mna ufalme wote; naye ana uwezo juu ya kila kitu,"),
            QuranVerse(2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا وَهُوَ الْعَزِيزُ الْغَفُورُ", "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -", "Ambaye ameumba mauti na uzima ili kukujaribuni ni nani miongoni mwenu mwenye vitendo vizuri zaidi. Naye ni Mwenye nguvu na Mwingi wa maghfira,"),
            QuranVerse(3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا مَّا تَرَى فِي خَلْقِ الرَّحْمَنِ مِن تَفَاوُتٍ فَارْجِعِ الْبَصَرَ هَلْ تَرَى مِن فُطُورٍ", "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency. So return [your] vision [to the heaven]; do you see any breaks?", "Aliyeumba mbingu saba kwa matabaka. Huoni katika uumbaji wa Mwingi wa Rehema kasoro yoyote; basi rudisha macho yako: Je, unaona ufa wowote?"),
            QuranVerse(4, "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", "Then return [your] vision twice again. [Your] vision will return to you humbled while it is fatigued.", "Kisha rudisha macho yako mara mbili tena; macho yako yatarudi kwako yakiwa yamedhalilika na kuchoka."),
            QuranVerse(5, "وَلَقَدْ زَيَّنَّا السَّمَاءَ الدُّنْيَا بِمَصَابِيحَ وَجَعَلْنَاهَا رُجُومًا لِّلشَّيَاطِينِ وَأَعْتَدْنَا لَهُمْ عَذَابَ السَّعِيرِ", "And We have certainly beautified the nearest heaven with stars and have made them [what is thrown as] stones for the devils and have prepared for them the punishment of the Blaze.", "Na hakika Tumeipamba mbingu ya karibu kwa taa (nyota) na Tukazifanya kuwa za kuwapiga nazo mashetani; na Tukawaandalia adhabu ya Moto mkali."),
            QuranVerse(6, "وَلِلَّذِينَ كَفَرُوا بِرَبِّهِمْ عَذَابُ جَهَنَّمَ وَبِئْسَ الْمَصِيرُ", "And for those who disbelieved in their Lord is the punishment of Hell, and wretched is the destination.", "Na kwa walio mkufuru Mola wao Mlezi ipo adhabu ya Jahannamu. Na ni mapitio mabaya yaliyoje hayo!"),
            QuranVerse(7, "إِذَا أُلْقُوا فِيهَا سَمِعُوا لَهَا شَهِيقًا وَهِيَ تَفُورُ", "When they are thrown into it, they hear from it an inhaling while it boils up.", "Watakapo tupwa humo watasikia msonzo wake na huku inafura,"),
            QuranVerse(8, "تَكَادُ تَمَيَّزُ مِنَ الْغَيْظِ كُلَّمَا أُلْقِيَ فِيهَا فَوْجٌ سَأَلَهُمْ خَزَنَتُهَا أَلَمْ يَأْتِكُمْ نَذِيرٌ", "It almost bursts with rage. Every time a company is thrown into it, its keepers ask them, 'Did there not come to you a warner?'", "Inakaribia kupasuka kwa hasira. Kila kundi linapotupwa humo walinzi wake huwauliza: Je! Hakukujieni mwonyaji?"),
            QuranVerse(9, "قَالُوا بَلَى قَدْ جَاءَنَا نَذِيرٌ فَكَذَّبْنَا وَقُلْنَا مَا نَزَّلَ اللَّهُ مِن شَيْءٍ إِنْ أَنتُمْ إِلَّا فِي ضَلَالٍ كَبِيرٍ", "They will say, 'Yes, a warner had come to us, but we denied and said, \"Allah has not sent down anything. You are not but in great error.\"'", "Watasema: Kwani! Alitujia mwonyaji, lakini tulikataa, na tukasema: Mwenyezi Mungu hakuteremsha kitu; nyinyi hammo ila katika upotovu mkubwa!"),
            QuranVerse(10, "وَقَالُوا لَوْ كُنَّا نَسْمَعُ أَوْ نَعْقِلُ مَا كُنَّا فِي أَصْحَابِ السَّعِيرِ", "And they will say, 'If only we had been listening or reasoning, we would not be among the companions of the Blaze.'", "Na watasema: Lau kama tungeli sikia, au tungeli kuwa na akili, tusingeli kuwa katika watu wa Moto wenye kuvuma kwa nguvu!"),
            QuranVerse(11, "فَاعْتَرَفُوا بِذَنبِهِمْ فَسُحْقًا لِّأَصْحَابِ السَّعِيرِ", "And they will admit their sin, so [it is] ruin for the companions of the Blaze.", "Wakiriri dhambi zao. Basi kuangamia ni kwa watu wa Moto wenye kuvuma kwa nguvu!"),
            QuranVerse(12, "إِنَّ الَّذِينَ يَخْشَوْنَ رَبَّهُم بِالْغَيْبِ لَهُم مَّغْفِرَةٌ وَأَجْرٌ كَبِيرٌ", "Indeed, those who fear their Lord unseen will have forgiveness and great reward.", "Hakika wale wanaomwogopa Mola wao Mlezi kwa siri, watapata maghfira na ujira mkubwa."),
            QuranVerse(13, "وَأَسِرُّوا قَوْلَكُمْ أَوِ اجْهَرُوا بِهِ إِنَّهُ عَلِيمٌ بِذَاتِ الصُّدُورِ", "And conceal your speech or proclaim it; indeed, He is Knowing of that within the breasts.", "Na ficheni kauli yenu, au idhihirisheni; hakika Yeye ni Mjuzi wa yaliyomo vifuani."),
            QuranVerse(14, "أَلَا يَعْلَمُ مَنْ خَلَقَ وَهو اللَّطِيفُ الْخَبِيرُ", "Does He who created not know, while He is the Subtle, the Acquainted?", "Je, asijue aliye umba, naye ndiye Mjuzi wa mambo yote ya siri, Mwenye khabar za kila kitu?"),
            QuranVerse(15, "هُوَ الَّذِي جَعَلَ لَكُمُ الْأَرْضَ ذَلُولًا فَامْشُوا فِي مَنَاكِبِهَا وَكُلُوا مِن رِّزْقِهِ وَإِلَيْهِ النُّشُورُ", "He is the One who made the earth subservient to you - so walk among its slopes and eat of His provision - and to Him is the resurrection.", "Yeye ndiye aliye idhalilisha ardhi kwa ajili yenu, basi nendeni katika pande zake, na kuleni katika riziki zake. Na kwake Yeye ndio kufufuliwa."),
            QuranVerse(16, "أَأَمِنتُم مَّن فِي السَّمَاءِ أَن يَخْسِفَ بِكُمُ الْأَرْضَ فَإِذَا هِيَ تَمُورُ", "Do you feel secure that He who is in heaven would not cause the earth to swallow you and suddenly it would sway?", "Je! Mnaamini kweli kwamba aliyeko mbinguni hatakudidimizeni katika ardhi, na tahamaki inatikisika?"),
            QuranVerse(17, "أَمْ أَمِنتُم مَّن فِي السَّمَاءِ أَن يُرْسِلَ عَلَيْكُمْ حَاصِبًا فَسَتَعْلَمُونَ كَيْفَ نَذِيرِ", "Or do you feel secure that He who is in heaven would not send against you a storm of stones? Then you would know how [severe] was My warning.", "Au mnaamini kweli kwamba aliyeko mbinguni hatakuleteeni kimbunga cha kokoto? Basi mtajua jinsi ulivyo onyo langu!"),
            QuranVerse(18, "وَلَقَدْ كَذَّبَ الَّذِينَ مِن قَبْلِهِمْ فَكَيْفَ كَانَ نَكِيرِ", "And already had those before them denied, and how [terrible] was My rejection.", "Na hakika walikataa walio kuwa kabla yao; basi kulikuwaje kugeuza kwangu mambo!"),
            QuranVerse(19, "أَوَلَمْ يَرَوْا إِلَى الطَّيْرِ فَوْقَهُمْ صَافَّاتٍ وَيَقْبِضْنَ مَا يُمْسِكُهُنَّ إِلَّا الرَّحْمَنُ إِنَّهُ بِكُلِّ شَيْءٍ بَصِيرٌ", "Do they not see the birds above them with wings outspread and [sometimes] folded in? None holds them [aloft] except the Most Merciful. Indeed, He is, of all things, Seeing.", "Je, hawaoni ndege walioko juu yao wakikunjua mabawa yao na kuyafunika? Hakuna anaye washikilia ila Mwingi wa Rehema. Hakika Yeye ni Mwenye kuona kila kitu."),
            QuranVerse(20, "أَمَّنْ هَذَا الَّذِي هُوَ جُندٌ لَّكُمْ يَنصُرُكُم مِّن دُونِ الرَّحْمَنِ إِنِ الْكَافِرُونَ إِلَّا فِي غُرُورٍ", "Or who is it that could be an army for you to aid you other than the Most Merciful? The disbelievers are not but in delusion.", "Au ni lipi hili jeshi lenu la kukunusuruni badala ya Mwingi wa Rehema? Makafiri hawamo ila katika mghafala."),
            QuranVerse(21, "أَمَّنْ هَذَا الَّذِي يَرْزُقُكُمْ إِنْ أَمْسَكَ رِزْقَهُ بَل لَّجُّوا فِي عُتُوٍّ وَنُفُورٍ", "Or who is it that can provide for you if He should withhold His provision? But they have persisted in insolence and aversion.", "Au ni nani huyu ambaye atakupeni riziki kama Yeye akizuia riziki yake? Bali wao wamekomaa katika uasi na chuki."),
            QuranVerse(22, "أَفَمَن يَمْشِي مُكِبًّا عَلَى وَجْهِهِ أَهْدَى أَمَّن يَمْشِي سَوِيًّا عَلَى صِرَاطٍ مُّسْتَقِيمٍ", "Then is one who walks fallen on his face more guided or one who walks [upright] on a straight path?", "Je! Anaye tembea akisunukia juu ya uso wake ni mwongofu zaidi, au yule anaye tembea wima juu ya Njia Iliyo Nyooka?"),
            QuranVerse(23, "قُلْ هُوَ الَّذِي أَنشَأَكُمْ وَجَعَلَ لَكُمُ السَّمْعَ وَالْأَبْصَارَ وَالْأَفْئِدَةَ قَلِيلًا مَّا تَشْكُرُونَ", "Say, 'It is He who has produced you and made for you hearing and vision and hearts; little are you grateful.'", "Sema: Yeye ndiye aliye kuumbeni, na akakujaalieni kusikia, na kuona, na nyoyo. Ni kidogo sana shukrani zenu!"),
            QuranVerse(24, "قُلْ هُوَ الَّذِي ذَرَأَكُمْ فِي الْأَرْضِ وَإِلَيْهِ تُحْشَرُونَ", "Say, 'It is He who has multiplied you throughout the earth, and to Him you will be gathered.'", "Sema: Yeye ndiye aliye kutandazeni katika ardhi, na kwake Yeye mtakusanywa."),
            QuranVerse(25, "وَيَقُولُونَ مَتَى هَذَا الْوَعْدُ إِن كُنتُمْ صَادِقِينَ", "And they say, 'When is this promise, if you should be truthful?'", "Na wanasema: Lini ahadi hii ikiwa mnasema kweli?"),
            QuranVerse(26, "قُلْ إِنَّمَا الْعِلْمُ عِندَ اللَّهِ وَإِنَّمَا أَنَا نَذِيرٌ مُّبِينٌ", "Say, 'The knowledge is only with Allah, and I am only a clear warner.'", "Sema: Hakika ujuzi wake uko kwa Mwenyezi Mungu; na hakika mimi ni mwonyaji dhaahiri."),
            QuranVerse(27, "فَلَمَّا رَأَوْهُ زُلْفَةً سِيئَتْ وُجُوهُ الَّذِينَ كَفَرُوا وَقِيلَ هَذَا الَّذِي كُنتُم بِهِ تَدَّعُونَ", "But when they see it approaching, the faces of those who disbelieved will be distressed, and it will be said, 'This is that which you used to call for.'", "Watakapo uona umekaribia, nyuso za wale walio kufuru zitaharibika, na itasemwa: Hili ndilo lile mlilo kuwa mkilidai!"),
            QuranVerse(28, "قُلْ أَرَأَيْتُمْ إِنْ أَهْلَكَنِيَ اللَّهُ وَمَن مَّعِيَ أَوْ رَحِمَنَا فَمَن يُجِيرُ الْكَافِرِينَ مِنْ عَذَابٍ أَلِيمٍ", "Say, 'Have you considered: whether Allah should destroy me and those with me or have mercy upon us, who can protect the disbelievers from a painful punishment?'", "Sema: Mwaonaje, akiniangamiza Mwenyezi Mungu mimi na walio pamoja nami, au akitufanyia rehema, ni nani atakaye walinda makafiri na adhabu chungu?"),
            QuranVerse(29, "قُلْ هُوَ الرَّحْمَنُ آمَنَّا بِهِ وَعَلَيْهِ تَوَكَّلْنَا فَسَتَعْلَمُونَ مَنْ هُوَ فِي ضَلَالٍ مُّبِينٍ", "Say, 'He is the Most Merciful; we have believed in Him, and upon Him we have relied. And you will [come to] know who it is that is in clear error.'", "Sema: Yeye ndiye Mwingi wa Rehema; tunamuamini Yeye tu, na juu yake tunategemea. Basi mtajua ni nani aliye katika upotovu wa dhaahiri."),
            QuranVerse(30, "قُلْ أَرَأَيْتُمْ إِنْ أَصْبَحَ مَاؤُكُمْ غَوْرًا فَمَن يَأْتِيكُم بِمَاءٍ مَّعِينٍ", "Say, 'Have you considered: if your water was to become sunken [into the earth], then who could bring you flowing water?'", "Sema: Mwaonaje, yakiasiri maji yenu yakadidimia chini, ni nani atakaye kuleteeni maji yanayo tiririka matatasi?")
        ),
        103 to listOf(
            QuranVerse(1, "وَالْعَصْرِ", "By time,", "Kwa nyakati zote!"),
            QuranVerse(2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "Indeed, mankind is in loss,", "Hakika mwanadamu yumo katika khasara!"),
            QuranVerse(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "Ila wale walio amini, na wakatenda mema, na wakausiana kwa Haki, na wakausiana kwa kusubiri.")
        ),
        108 to listOf(
            QuranVerse(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "Hakika Sisi tumekupa kheri nyingi (Kauthar)."),
            QuranVerse(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", "Basi mswalie Mola wako Mlezi na uchinje."),
            QuranVerse(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Indeed, your enemy is the one cut off.", "Hakika anaye kuchukia ndiye aliyetengwa na kila kheri.")
        ),
        110 to listOf(
            QuranVerse(1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", "When the victory of Allah has come and the conquest,", "Utakapo fika msaada wa Mwenyezi Mungu na ushindi,"),
            QuranVerse(2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", "And you see the people entering into the religion of Allah in multitudes,", "Na ukaona watu wanaingia katika Dini ya Mwenyezi Mungu makundi makundi,"),
            QuranVerse(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ إِنَّهُ كَانَ تَوَّابًا", "Then exalt [Him] with praise of your Lord and ask forgiveness of Him. Indeed, He is ever Accepting of repentance.", "Basi mtakase Mola wako Mlezi kwa kumsifu, na umuombe maghfira; hakika Yeye ndiye Mwenye kupokea toba.")
        ),
        97 to listOf(
            QuranVerse(1, "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "Indeed, We sent the Qur'an down during the Night of Decree.", "Hakika Sisi tumemteremsha yeye (Qur'ani) katika Usiku wa Cheo."),
            QuranVerse(2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "And what can make you know what is the Night of Decree?", "Na nini kitakacho kujuulisha nini Usiku wa Cheo?"),
            QuranVerse(3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", "The Night of Decree is better than a thousand months.", "Usiku wa Cheo ni bora kuliko miezi elfu."),
            QuranVerse(4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ", "The angels and the Spirit descend therein by permission of their Lord for every matter.", "Huteremka Malaika na Roho (Jibrili) katika usiku huo kwa idhini ya Mola wao Mlezi kwa kila jambo."),
            QuranVerse(5, "سَلَامٌ هِيَ حَتَّى مَطْلَعِ الْفَجْرِ", "Peace it is until the emergence of dawn.", "Amani usiku huo mpaka kupambazuka kwa alfajiri.")
        )
    )

    fun getVersesForSurah(surahNumber: Int): List<QuranVerse> {
        return versesMap[surahNumber] ?: List(fullSurahHeadersList.firstOrNull { it.number == surahNumber }?.versesCount ?: 5) { i ->
            val num = i + 1
            QuranVerse(
                number = num,
                textAr = "آية كريمة $num من سورة ${fullSurahHeadersList.firstOrNull { it.number == surahNumber }?.nameAr ?: ""}",
                textEn = "Verse $num of Surah ${fullSurahHeadersList.firstOrNull { it.number == surahNumber }?.nameEn ?: ""}.",
                textSw = "Aya ya $num ya Surah ${fullSurahHeadersList.firstOrNull { it.number == surahNumber }?.nameEn ?: ""}."
            )
        }
    }
}
