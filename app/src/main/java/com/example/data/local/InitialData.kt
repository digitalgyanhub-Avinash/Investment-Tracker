package com.example.data.local

import com.example.data.model.Committee
import com.example.data.model.CommitteeMonth
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip

object InitialData {
    val HOLDINGS = listOf(
        Holding(
            id = "h-1",
            platform = "Zerodha",
            category = "Stocks",
            breakdownRaw = "17800, 6300, 9000, 9500, -2000, 15000",
            total = 67000.0,
            tillDate = "24-07-2026",
            remarks = "61600 invested, 5700 held in Demat"
        ),
        Holding(
            id = "h-2",
            platform = "Zerodha",
            category = "ETFs",
            breakdownRaw = "",
            total = 11000.0,
            tillDate = "28-07-2026",
            remarks = ""
        ),
        Holding(
            id = "h-3",
            platform = "IndMoney",
            category = "US Stocks & ETFs",
            breakdownRaw = "20000, 5000, 20000",
            total = 45000.0,
            tillDate = "28-07-2026",
            remarks = "38000 invested, 7000 held in Demat"
        ),
        Holding(
            id = "h-4",
            platform = "Wint Wealth",
            category = "Bonds",
            breakdownRaw = "5000, 10000, 10000",
            total = 25000.0,
            tillDate = "01-08-2026",
            remarks = ""
        ),
        Holding(
            id = "h-5",
            platform = "Groww",
            category = "Mutual Fund",
            breakdownRaw = "",
            total = 10000.0,
            tillDate = "24-07-2026",
            remarks = ""
        ),
        Holding(
            id = "h-6",
            platform = "Groww",
            category = "Liquid Fund",
            breakdownRaw = "5000, 65000",
            total = 65000.0,
            tillDate = "12-07-2026",
            remarks = ""
        ),
        Holding(
            id = "h-7",
            platform = "JAR",
            category = "Gold",
            breakdownRaw = "21, 120, 115, 1030, 515, 515, 1030",
            total = 3346.0,
            tillDate = "07-07-2026",
            remarks = ""
        ),
        Holding(
            id = "h-8",
            platform = "Paytm",
            category = "Gold",
            breakdownRaw = "500, 80, 17, 500",
            total = 1090.0,
            tillDate = "04-08-2026",
            remarks = ""
        ),
        Holding(
            id = "h-9",
            platform = "Paytm",
            category = "Silver",
            breakdownRaw = "",
            total = 3000.0,
            tillDate = "07-07-2026",
            remarks = ""
        )
    )

    val SIPS = listOf(
        Sip(
            id = "s-1",
            platform = "Groww",
            instrument = "SIP",
            amount = 6500.0,
            frequency = "Monthly",
            since = "05-01-2026",
            remarks = "Auto-debit from salary account"
        ),
        Sip(
            id = "s-2",
            platform = "JAR",
            instrument = "Daily Gold",
            amount = 20.0,
            frequency = "Daily",
            since = "12-02-2026",
            remarks = "Auto micro-investment"
        ),
        Sip(
            id = "s-3",
            platform = "PhonePe",
            instrument = "Daily Silver",
            amount = 30.0,
            frequency = "Daily",
            since = "25-04-2026",
            remarks = "Auto micro-investment"
        )
    )

    val IPOS = listOf(
        Ipo(
            id = "ipo-1",
            companyName = "Bajaj Housing Finance Ltd",
            category = "Mainboard",
            platform = "Zerodha",
            applicantName = "Self",
            sharesPerLot = 214,
            cutOffPrice = 70.0,
            lotsApplied = 1,
            totalAmountBlocked = 14980.0,
            applyDate = "08-08-2026",
            allotmentDate = "11-08-2026",
            listingDate = "14-08-2026",
            status = "Allotted",
            allottedLots = 1,
            listingPrice = 150.0,
            listingGainAmount = 17120.0,
            listingGainPercent = 114.28,
            remarks = "UPI Mandate approved on Zerodha. Allotted 1 lot."
        ),
        Ipo(
            id = "ipo-2",
            companyName = "Premier Energies Ltd",
            category = "Mainboard",
            platform = "Groww",
            applicantName = "Spouse",
            sharesPerLot = 33,
            cutOffPrice = 450.0,
            lotsApplied = 1,
            totalAmountBlocked = 14850.0,
            applyDate = "10-08-2026",
            allotmentDate = "13-08-2026",
            listingDate = "16-08-2026",
            status = "Applied",
            remarks = "Mandate approved on Paytm UPI."
        ),
        Ipo(
            id = "ipo-3",
            companyName = "Resourceful Automobile Ltd",
            category = "SME",
            platform = "AngelOne",
            applicantName = "Self",
            sharesPerLot = 1200,
            cutOffPrice = 117.0,
            lotsApplied = 1,
            totalAmountBlocked = 140400.0,
            applyDate = "01-08-2026",
            allotmentDate = "05-08-2026",
            refundDate = "06-08-2026",
            status = "Not Allotted",
            allottedLots = 0,
            remarks = "Not allotted. UPI block unblocked on 06-Aug."
        )
    )

    val COMMITTEES = listOf(
        Committee(
            id = "comm-khatabook-4",
            name = "Committee-4 (Jul-2027)",
            totalAmount = 90000.0,
            totalMembers = 12,
            totalMonths = 12,
            startDate = "14-07-2026",
            baseMonthlyInstallment = 7500.0,
            currentMonth = 2,
            hasTakenPot = false,
            remarks = "Maintained by Rahul MT 90000rs. Started Jul 2026.",
            months = listOf(
                CommitteeMonth(1, "Jul 2026", 12504.0, 77496.0, 6458.0, true),
                CommitteeMonth(2, "Aug 2026", 0.0, 90000.0, 7500.0, true)
            )
        ),
        Committee(
            id = "comm-khatabook-3",
            name = "Committee-3 (Dec-2026)",
            totalAmount = 90000.0,
            totalMembers = 12,
            totalMonths = 12,
            startDate = "19-01-2026",
            baseMonthlyInstallment = 7500.0,
            currentMonth = 8,
            hasTakenPot = false,
            remarks = "Maintained by Rahul MT 90000rs. Started Jan 2026.",
            months = listOf(
                CommitteeMonth(1, "Jan 2026", 21492.0, 68508.0, 5709.0, true),
                CommitteeMonth(2, "Feb 2026", 0.0, 90000.0, 7500.0, true),
                CommitteeMonth(3, "Mar 2026", 19992.0, 70008.0, 5834.0, true),
                CommitteeMonth(4, "Apr 2026", 12000.0, 78000.0, 6500.0, true),
                CommitteeMonth(5, "May 2026", 9492.0, 80508.0, 6709.0, true),
                CommitteeMonth(6, "Jun 2026", 7200.0, 82800.0, 6900.0, true),
                CommitteeMonth(7, "Jul 2026", 5604.0, 84396.0, 7033.0, true),
                CommitteeMonth(8, "Aug 2026", 4800.0, 85200.0, 7100.0, true)
            )
        ),
        Committee(
            id = "comm-khatabook-2",
            name = "Committee-2 (Jun-2026)",
            totalAmount = 60000.0,
            totalMembers = 12,
            totalMonths = 12,
            startDate = "10-07-2025",
            baseMonthlyInstallment = 5000.0,
            currentMonth = 12,
            hasTakenPot = true,
            potTakenMonth = 9,
            potTakenAmount = 56000.0,
            remarks = "Maintained by Rahul MT 60000rs. Pot taken on 11 Mar 2026 (₹56,000). Adjustment profit: ₹1,081.",
            months = listOf(
                CommitteeMonth(1, "Jul 2025", 15996.0, 44004.0, 3667.0, true),
                CommitteeMonth(2, "Aug 2025", 0.0, 60000.0, 5000.0, true),
                CommitteeMonth(3, "Sep 2025", 9000.0, 51000.0, 4250.0, true),
                CommitteeMonth(4, "Oct 2025", 6000.0, 54000.0, 4500.0, true),
                CommitteeMonth(5, "Nov 2025", 6600.0, 53400.0, 4450.0, true),
                CommitteeMonth(6, "Dec 2025", 6096.0, 53904.0, 4492.0, true),
                CommitteeMonth(7, "Jan 2026", 5700.0, 54300.0, 4525.0, true),
                CommitteeMonth(8, "Feb 2026", 4992.0, 55008.0, 4584.0, true),
                CommitteeMonth(9, "Mar 2026", 4000.0, 56000.0, 4667.0, true, "Self (Taken ₹56,000)"),
                CommitteeMonth(10, "Apr 2026", 2100.0, 57900.0, 4825.0, true),
                CommitteeMonth(11, "May 2026", 492.0, 59508.0, 4959.0, true),
                CommitteeMonth(12, "Jun 2026", 0.0, 60000.0, 5000.0, true)
            )
        ),
        Committee(
            id = "comm-khatabook-1",
            name = "Committee-1 (Dec-2025)",
            totalAmount = 60000.0,
            totalMembers = 12,
            totalMonths = 12,
            startDate = "15-01-2025",
            baseMonthlyInstallment = 5000.0,
            currentMonth = 12,
            hasTakenPot = true,
            potTakenMonth = 10,
            potTakenAmount = 59250.0,
            remarks = "Maintained by Rahul 60000. Pot taken on 18 Oct 2025 (₹59,250). Final adjustment profit: ₹5,990.",
            months = listOf(
                CommitteeMonth(1, "Jan 2025", 9480.0, 50520.0, 4210.0, true),
                CommitteeMonth(2, "Feb 2025", 0.0, 60000.0, 5000.0, true),
                CommitteeMonth(3, "Mar 2025", 11496.0, 48504.0, 4042.0, true),
                CommitteeMonth(4, "Apr 2025", 14592.0, 45408.0, 3784.0, true),
                CommitteeMonth(5, "May 2025", 11196.0, 48804.0, 4067.0, true),
                CommitteeMonth(6, "Jun 2025", 12696.0, 47304.0, 3942.0, true),
                CommitteeMonth(7, "Jul 2025", 8496.0, 51504.0, 4292.0, true),
                CommitteeMonth(8, "Aug 2025", 4992.0, 55008.0, 4584.0, true),
                CommitteeMonth(9, "Sep 2025", 6696.0, 53304.0, 4442.0, true),
                CommitteeMonth(10, "Oct 2025", 744.0, 59256.0, 4938.0, true, "Self (Taken ₹59,250)"),
                CommitteeMonth(11, "Nov 2025", 492.0, 59508.0, 4959.0, true),
                CommitteeMonth(12, "Dec 2025", 0.0, 60000.0, 5000.0, true)
            )
        )
    )

    val PLATFORMS = listOf(
        "Zerodha", "Groww", "IndMoney", "Wint Wealth", "JAR", "Paytm", "PhonePe", "AngelOne", "Upstox", "ICICI Direct", "HDFC Securities"
    )

    val CATEGORIES = listOf(
        "Stocks", "ETFs", "US Stocks & ETFs", "Bonds", "Mutual Fund", "Liquid Fund", "Gold", "Silver", "Crypto", "Real Estate", "Fixed Deposit"
    )
}
