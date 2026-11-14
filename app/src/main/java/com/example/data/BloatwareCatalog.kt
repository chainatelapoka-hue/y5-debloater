package com.example.data

data class BloatwareAdvice(
    val level: SafetyLevel,
    val noteBn: String
)

object BloatwareCatalog {

    private val exactMatches: Map<String, BloatwareAdvice> = mapOf(
        // Huawei / EMUI Safe Bloatware (Y5 Prime 2018 / EMUI 8)
        "com.huawei.appmarket" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei AppGallery — Play Store ব্যবহার করলে এটি নিরাপদে রিমুভ করা যায়।"
        ),
        "com.huawei.gameassistant" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Game Suite — গেম বুস্টার ব্লোটওয়্যার, রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.himovie" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Video — বিকল্প ভিডিও প্লেয়ার থাকলে নিরাপদে রিমুভ করা যায়।"
        ),
        "com.huawei.hwvplayer" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Video Player — রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.music" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Music — অন্য মিউজিক প্লেয়ার থাকলে রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.phoneservice" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "HiCare / Support অ্যাপ — রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.hicloud" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Cloud — ব্যাকআপ না ব্যবহার করলে নিরাপদে রিমুভ করা যায়।"
        ),
        "com.huawei.wallet" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Wallet — রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.health" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Health — ফিটনেস ট্র্যাকিং না করলে রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.compass" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Compass — প্রয়োজন না হলে রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.mirror" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Mirror — রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.browser" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Browser — অন্য ব্রাউজার ব্যবহার করলে রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.tips" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Tips — রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.hitouch" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "HiTouch Shopping Assistant — রিমুভ করলে RAM বাঁচে।"
        ),
        "com.huawei.skytone" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "SkyTone Roaming — সাধারণ ব্যবহারে প্রয়োজন নেই।"
        ),
        "com.huawei.vassistant" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "HiVoice Assistant — ব্যবহার না করলে রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.hifolder" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "AppAdvisor / Suggested Apps — নিরাপদে রিমুভ করুন।"
        ),
        "com.huawei.imedia.sws" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Histen / Audio effect bloat — সাধারণ অডিও চলবে, রিমুভ করা যায়।"
        ),
        "com.huawei.parentcontrol" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Parental Control — ব্যবহার না করলে রিমুভ করা নিরাপদ।"
        ),
        "com.huawei.android.karaoke" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Huawei Karaoke — নিরাপদে রিমুভ করা যায়।"
        ),

        // Pre-installed Third-party / Social / Carrier Bloat
        "com.facebook.katana" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Pre-installed Facebook — রিমুভ করে লাইট বা প্লে-স্টোর ভার্সন ব্যবহার করা যায়।"
        ),
        "com.facebook.system" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Facebook App Installer — ব্যাকগ্রাউন্ডে চলে, রিমুভ করা নিরাপদ।"
        ),
        "com.facebook.appmanager" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Facebook App Manager — ব্যাকগ্রাউন্ডে RAM খরচ করে, রিমুভ করা নিরাপদ।"
        ),
        "com.facebook.services" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Facebook Services — রিমুভ করা নিরাপদ।"
        ),
        "com.booking" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Booking.com pre-install — সম্পূর্ণ নিরাপদে রিমুভ করা যায়।"
        ),
        "com.netflix.mediaclient" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Pre-installed Netflix — ব্যবহার না করলে রিমুভ করা নিরাপদ।"
        ),
        "com.netflix.partner.activation" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Netflix Partner Activation — রিমুভ করা নিরাপদ।"
        ),

        // Google Optional Apps
        "com.google.android.videos" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Play Movies / TV — ব্যবহার না করলে রিমুভ করা নিরাপদ।"
        ),
        "com.google.android.music" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Play Music — পুরোনো অ্যাপ, নিরাপদে রিমুভ করা যায়।"
        ),
        "com.google.android.apps.tachyon" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Duo / Meet — প্রয়োজন না হলে রিমুভ করা নিরাপদ।"
        ),
        "com.google.android.apps.docs" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Drive — ব্যবহার না করলে রিমুভ করা যায়।"
        ),
        "com.google.android.apps.books" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Play Books — নিরাপদে রিমুভ করা যায়।"
        ),
        "com.google.android.apps.magazines" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Newsstand — নিরাপদে রিমুভ করা যায়।"
        ),
        "com.google.android.play.games" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Play Games — প্রয়োজন না হলে রিমুভ করা নিরাপদ।"
        ),
        "com.google.android.talk" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "Google Hangouts — বন্ধ হয়ে যাওয়া সার্ভিস, নিরাপদে রিমুভ করুন।"
        ),
        "com.google.android.apps.photos" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Google Photos — গ্যালারি হিসেবে ব্যবহার করলে রেখে দিন।"
        ),
        "com.google.android.youtube" to BloatwareAdvice(
            SafetyLevel.SAFE_BLOAT,
            "YouTube — চাইলে রিমুভ বা ডিজেবল করা যায়।"
        ),

        // Caution Apps
        "com.huawei.systemmanager" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Huawei Phone Manager — এটি সরালে ব্যাটারি/পারমিশন সেটিংস প্রভাবিত হতে পারে।"
        ),
        "com.huawei.android.launcher" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Huawei Home Launcher — বিকল্প লঞ্চার ইনস্টল না থাকলে কখনোই রিমুভ করবেন না!"
        ),
        "com.huawei.hwid" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "HMS Core / Huawei ID — Huawei থিম বা ক্লাউড ব্যবহার করলে প্রয়োজন হয়।"
        ),
        "com.google.android.gms" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Google Play Services — সরালে Play Store ও বেশিরভাগ অ্যাপ কাজ করবে না!"
        ),
        "com.google.android.gsf" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Google Services Framework — Play Services-এর জন্য জরুরি।"
        ),
        "com.android.vending" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Google Play Store — এটি সরালে নতুন অ্যাপ ইনস্টল করতে পারবেন না।"
        ),

        // Critical Core System Apps
        "android" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Android OS Core — এটি রিমুভ করলে ফোন বুট হবে না (Bootloop)!"
        ),
        "com.android.systemui" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "System UI — স্ট্যাটাস বার ও লকস্ক্রিনের মূল অংশ, রিমুভ করবেন না!"
        ),
        "com.android.settings" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "System Settings — সেটিংস অ্যাপ, রিমুভ করলে ফোন নিয়ন্ত্রণ হারাবেন!"
        ),
        "com.android.phone" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Phone Services — কল ও সিম নেটওয়ার্কের জন্য অপরিহার্য।"
        ),
        "com.android.server.telecom" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Telecom Framework — কলিং সার্ভিসের মূল অংশ।"
        ),
        "com.android.packageinstaller" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Package Installer — APK ইনস্টল/আনইনস্টলের জন্য জরুরি।"
        ),
        "com.google.android.packageinstaller" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Package Installer — অ্যাপ ইনস্টল ও পারমিশন ডায়ালগের জন্য জরুরি।"
        ),
        "com.android.permissioncontroller" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Permission Controller — অ্যাপ পারমিশন ম্যানেজমেন্টের জন্য অপরিহার্য।"
        ),
        "com.google.android.permissioncontroller" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Permission Controller — অ্যাপ পারমিশনের জন্য অপরিহার্য।"
        ),
        "com.android.keychain" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "KeyChain — সিস্টেম সিকিউরিটি ও সার্টিফিকেটের জন্য অপরিহার্য।"
        ),
        "com.android.shell" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Android Shell — ADB ও Shizuku কাজের জন্য অপরিহার্য!"
        ),
        "com.huawei.android.hwouc" to BloatwareAdvice(
            SafetyLevel.CAUTION,
            "Huawei System Update — OTA আপডেট না চাইলে ডিজেবল করা যায়, তবে সতর্ক থাকুন।"
        ),
        "com.huawei.systemserver" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Huawei System Server — EMUI সিস্টেমের মূল সার্ভিস, রিমুভ করবেন না!"
        ),
        "com.android.webview" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Android System WebView — অনেক অ্যাপ ক্র্যাশ করবে এটি না থাকলে।"
        ),
        "com.google.android.webview" to BloatwareAdvice(
            SafetyLevel.CRITICAL,
            "Android System WebView — অ্যাপের ওয়েব কন্টেন্টের জন্য অপরিহার্য।"
        )
    )

    fun analyze(packageName: String): BloatwareAdvice {
        exactMatches[packageName]?.let { return it }

        return when {
            packageName.startsWith("com.android.providers.") -> BloatwareAdvice(
                SafetyLevel.CRITICAL,
                "সিস্টেম ডেটা প্রোভাইডার (Contacts/Media/Settings) — রিমুভ করা ঝুঁকিপূর্ণ।"
            )
            packageName.startsWith("com.mediatek.") || packageName.startsWith("com.mtk.") -> BloatwareAdvice(
                SafetyLevel.CRITICAL,
                "MediaTek হার্ডওয়্যার/মডেম সার্ভিস (Y5 Prime MT6739) — সাধারণত রিমুভ না করাই ভালো।"
            )
            packageName.startsWith("com.swiftkey.") || packageName.startsWith("com.touchtype") -> BloatwareAdvice(
                SafetyLevel.SAFE_BLOAT,
                "প্রি-ইনস্টলড কিবোর্ড — অন্য কিবোর্ড (যেমন Gboard) সেট করা থাকলে রিমুভ করা যায়।"
            )
            packageName.startsWith("com.gameloft.") -> BloatwareAdvice(
                SafetyLevel.SAFE_BLOAT,
                "প্রি-ইনস্টলড গেম ডেমো — নিরাপদে রিমুভ করুন।"
            )
            packageName.startsWith("com.huawei.android.thememanager") -> BloatwareAdvice(
                SafetyLevel.CAUTION,
                "Huawei Themes — থিম ও ওয়ালপেপার পরিবর্তনের জন্য ব্যবহৃত হয়।"
            )
            packageName.contains("telephony", ignoreCase = true) ||
                packageName.contains("ims", ignoreCase = true) ||
                packageName.contains("bluetooth", ignoreCase = true) ||
                packageName.contains("wifi", ignoreCase = true) -> BloatwareAdvice(
                SafetyLevel.CRITICAL,
                "কানেক্টিভিটি বা নেটওয়ার্ক কম্পোনেন্ট — রিমুভ করবেন না।"
            )
            else -> BloatwareAdvice(
                SafetyLevel.UNKNOWN,
                "সতর্কতার সাথে যাচাই করে Uninstall বা Disable করুন (চাইলে পরে Restore করা যাবে)।"
            )
        }
    }
}
