package com.example.ui.state

enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    HI("hi", "हिंदी")
}

object AppStrings {
    fun get(key: String, lang: AppLanguage): String {
        val entry = dictionary[key] ?: return key
        return if (lang == AppLanguage.HI) entry.second else entry.first
    }

    private val dictionary = mapOf(
        "app_name" to ("Bokaro Services" to "बोकारो सर्विसेज"),
        "tagline" to ("Fast & Trusted Home Repair in Bokaro Steel City" to "बोकारो स्टील सिटी में भरोसेमंद होम रिपेयर सेवा"),
        "switch_lang" to ("हिन्दी" to "English"),
        "role_customer" to ("Customer" to "ग्राहक"),
        "role_provider" to ("Mechanic / Provider" to "मैकेनिक / प्रदाता"),
        "role_admin" to ("Admin Manager" to "एडमिन मैनेजर"),
        "login" to ("Login" to "लॉग इन"),
        "register" to ("Register" to "रजिस्टर करें"),
        "quick_demo_login" to ("Quick Demo Login" to "क्विक डेमो लॉगिन"),
        "welcome" to ("Welcome" to "स्वागत है"),
        "book_service" to ("Book a Service" to "सर्विस बुक करें"),
        "our_services" to ("Our Core Services" to "हमारी प्रमुख सेवाएं"),
        "electrician" to ("Electrician" to "इलेक्ट्रीशियन"),
        "plumber" to ("Plumber" to "प्लम्बर"),
        "ac_appliance" to ("AC & Appliances" to "एसी व उपकरण"),
        "select_provider" to ("Choose a Specialist" to "विशेषज्ञ चुनें"),
        "auto_assign" to ("Auto Assign Best Nearby" to "नजदीकी सबसे अच्छा कारीगर"),
        "select_area" to ("Select Area in Bokaro" to "बोकारो में अपना क्षेत्र चुनें"),
        "your_address" to ("House / Flat / Quarter Address" to "घर / क्वार्टर का पूरा पता"),
        "describe_problem" to ("Describe Your Problem" to "समस्या का विवरण लिखें"),
        "select_slot" to ("Select Time Slot" to "सुविधाजनक समय चुनें"),
        "confirm_booking" to ("Confirm Booking" to "बुकिंग कन्फर्म करें"),
        "booking_success" to ("Booking Confirmed!" to "बुकिंग सफलतापूर्वक दर्ज हुई!"),
        "my_bookings" to ("My Bookings" to "मेरी बुकिंग्स"),
        "active_jobs" to ("Active Jobs" to "सक्रिय काम"),
        "history" to ("History" to "इतिहास"),
        "job_status" to ("Status" to "स्थिति"),
        "track_service" to ("Live Service Timeline" to "लाइव सर्विस ट्रैकिंग"),
        "call_provider" to ("Call Mechanic" to "मैकेनिक को कॉल करें"),
        "call_customer" to ("Call Customer" to "ग्राहक को कॉल करें"),
        "bill_details" to ("Bill Breakdown" to "बिल का विवरण"),
        "base_fee" to ("Inspection & Service Charge" to "विजिट व सर्विस चार्ज"),
        "extra_parts" to ("Extra Materials / Parts" to "अतिरिक्त सामान / पार्ट्स"),
        "total_amount" to ("Total Payable" to "कुल देय राशि"),
        "pay_now" to ("Pay Now" to "भुगतान करें"),
        "rate_service" to ("Rate Service" to "रेटिंग दें"),
        "submit_review" to ("Submit Review" to "रिव्यू सबमिट करें"),
        "pending" to ("Booking Placed" to "बुकिंग दर्ज"),
        "accepted" to ("Accepted" to "स्वीकृत"),
        "on_the_way" to ("On The Way" to "रास्ते में"),
        "work_started" to ("Work In Progress" to "काम चालू"),
        "work_completed" to ("Work Completed" to "काम पूरा हुआ"),
        "cancelled" to ("Cancelled" to "रद्द"),
        "paid" to ("Paid" to "भुगतान हो गया"),
        "unpaid" to ("Unpaid" to "भुगतान बाकी"),
        "provider_dashboard" to ("Provider Hub" to "कारीगर हब"),
        "availability" to ("Online for Jobs" to "काम के लिए उपलब्ध"),
        "accept_job" to ("Accept Booking" to "काम स्वीकार करें"),
        "update_status" to ("Update Work Status" to "स्थिति अपडेट करें"),
        "add_parts_cost" to ("Add Parts / Material Bill" to "पार्ट्स बिल जोड़ें"),
        "admin_dashboard" to ("Admin Control Center" to "एडमिन कंट्रोल सेंटर"),
        "total_bookings" to ("Total Bookings" to "कुल बुकिंग्स"),
        "revenue" to ("Platform Volume" to "कुल टर्नओवर"),
        "assign_provider" to ("Assign Provider" to "कारीगर असाइन करें"),
        "customers_list" to ("Customers" to "ग्राहक सूची"),
        "providers_list" to ("Service Providers" to "कारीगर सूची"),
        "payments_tracking" to ("Payment Tracker" to "पेमेंट ट्रैकर"),
        "bokaro_steel_city" to ("Bokaro Steel City, Jharkhand" to "बोकारो स्टील सिटी, झारखण्ड")
    )
}
