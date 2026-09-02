package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class ServiceType(val id: String, val titleEn: String, val titleHi: String) {
    ELECTRICIAN("ELECTRICIAN", "Electrician Services", "इलेक्ट्रीशियन सेवाएं"),
    PLUMBER("PLUMBER", "Plumbing Services", "नलसाजी / प्लम्बर सेवाएं"),
    AC_APPLIANCE("AC_APPLIANCE", "AC & Appliance Repair", "एसी और उपकरण मरम्मत")
}

data class SubService(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val basePrice: Double,
    val estimatedDuration: String,
    val descriptionEn: String,
    val descriptionHi: String
)

data class ServiceCategoryItem(
    val type: ServiceType,
    val titleEn: String,
    val titleHi: String,
    val shortDescEn: String,
    val shortDescHi: String,
    val startingPrice: Double,
    @DrawableRes val imageRes: Int,
    val subServices: List<SubService>
)

object ServiceCatalog {
    val categories = listOf(
        ServiceCategoryItem(
            type = ServiceType.ELECTRICIAN,
            titleEn = "Electrician",
            titleHi = "इलेक्ट्रीशियन",
            shortDescEn = "Wiring, switchboard, fuse, fan & inverter repair",
            shortDescHi = "वायरिंग, स्विचबोर्ड, पंखा और इन्वर्टर मरम्मत",
            startingPrice = 149.0,
            imageRes = R.drawable.service_electrician,
            subServices = listOf(
                SubService(
                    "elec_1", "Switchboard & Socket Repair", "स्विच व सॉकेट रिपेयर",
                    149.0, "30-45 mins",
                    "Fix burnt sockets, switch replacements, loose connections",
                    "खराब स्विच, सॉकेट बदलाव व ढीले कनेक्शन ठीक करें"
                ),
                SubService(
                    "elec_2", "Ceiling Fan / Exhaust Installation", "सीलिंग फैन / पंखा फिटिंग",
                    199.0, "40-60 mins",
                    "Mounting, regulator check, new wiring setup",
                    "पंखा इंस्टॉलेशन, रेगुलेटर जांच, नई वायरिंग"
                ),
                SubService(
                    "elec_3", "Inverter & Battery Wiring", "इन्वर्टर और बैटरी वायरिंग",
                    349.0, "1-2 hours",
                    "Troubleshooting backup failure, new inverter installation",
                    "बैकअप समस्या समाधान, नया इन्वर्टर कनेक्शन"
                ),
                SubService(
                    "elec_4", "Complete House Wiring Inspection", "पूरे घर की वायरिंग जांच",
                    499.0, "2 hours",
                    "Short circuit check, MCB tripping fix, load balancing",
                    "शॉर्ट सर्किट जांच, एमसीबी ट्रिपिंग सुधार"
                )
            )
        ),
        ServiceCategoryItem(
            type = ServiceType.PLUMBER,
            titleEn = "Plumber",
            titleHi = "प्लम्बर",
            shortDescEn = "Tap leakage, pipeline block, basin & water tank repair",
            shortDescHi = "नल रिसाव, पाइपलाइन ब्लॉक, बेसिन व टंकी रिपेयर",
            startingPrice = 179.0,
            imageRes = R.drawable.service_plumber,
            subServices = listOf(
                SubService(
                    "plumb_1", "Tap / Faucet Leakage Repair", "नल की लीकेज मरम्मत",
                    179.0, "30 mins",
                    "Gasket replacement, washer fixing, new tap installation",
                    "वाशर बदलना, नया नल लगाना, टपकता पानी रोकना"
                ),
                SubService(
                    "plumb_2", "Drain & Pipe Blockage Removal", "नाली और पाइप ब्लॉकेज हटाना",
                    299.0, "45-60 mins",
                    "Sink, bathroom drain, and sewage pipe clearing",
                    "सिंक, बाथरूम नाली और पाइपलाइन की गहरी सफाई"
                ),
                SubService(
                    "plumb_3", "Overhead Water Tank Repair & Fitting", "पानी की टंकी रिपेयर व फिटिंग",
                    399.0, "1-2 hours",
                    "Float valve replacement, inlet/outlet pipe plumbing",
                    "फ्लोट वाल्व बदलाव, इनलेट आउटलेट पाइप फिटिंग"
                ),
                SubService(
                    "plumb_4", "Bathroom Sanitaryware Installation", "बाथरूम सेनेटरी फिटिंग",
                    449.0, "1-2 hours",
                    "Commode, washbasin, shower & mixer tap fittings",
                    "कमोड, वाशबेसिन, शावर और मिक्सर टैप फिटिंग"
                )
            )
        ),
        ServiceCategoryItem(
            type = ServiceType.AC_APPLIANCE,
            titleEn = "AC & Appliance Repair",
            titleHi = "एसी व उपकरण रिपेयर",
            shortDescEn = "Split/Window AC, Refrigerator, Washing Machine, Geyser",
            shortDescHi = "एसी, फ्रिज, वाशिंग मशीन व गीजर मरम्मत",
            startingPrice = 299.0,
            imageRes = R.drawable.service_ac_repair,
            subServices = listOf(
                SubService(
                    "ac_1", "AC Deep Jet Cleaning & Service", "एसी डीप जेट सर्विसिंग",
                    499.0, "60 mins",
                    "Indoor/outdoor coil wash, filter cleaning, cooling check",
                    "इनडोर व आउटडोर कॉइल जेट वॉश, फिल्टर सफाई, कूलिंग चेक"
                ),
                SubService(
                    "ac_2", "AC Gas Refill & Leak Fix", "एसी गैस रिफिल व लीकेज समाधान",
                    1499.0, "1.5 hours",
                    "Nitrogen pressure test, copper braising, 100% genuine gas",
                    "प्रेशर टेस्टिंग, लीकेज सोल्डरिंग, शुद्ध कूलेंट गैस चार्जिंग"
                ),
                SubService(
                    "ac_3", "Refrigerator Not Cooling / Noise Fix", "फ्रिज कूलिंग व मोटर रिपेयर",
                    349.0, "45-60 mins",
                    "Thermostat check, compressor relay, defrost timer fix",
                    "थर्मोस्टेट जांच, रिले बदलाव, कंप्रेसर निरीक्षण"
                ),
                SubService(
                    "ac_4", "Washing Machine Repair (Semi/Auto)", "वाशिंग मशीन मरम्मत",
                    399.0, "1 hour",
                    "Drain motor, spin tub vibrate fix, drum belt replacement",
                    "ड्रेन मोटर, स्पिन टब वाइब्रेशन और बेल्ट रिप्लेसमेंट"
                ),
                SubService(
                    "ac_5", "Geyser Installation & Element Replacement", "गीजर इंस्टॉलेशन व कॉइल रिपेयर",
                    299.0, "45 mins",
                    "Heating rod replacement, thermostat safety test",
                    "हीटिंग रॉड बदलाव, थर्मोस्टेट सुरक्षा टेस्ट"
                )
            )
        )
    )
}
