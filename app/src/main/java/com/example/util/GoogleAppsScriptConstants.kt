package com.example.util

object GoogleAppsScriptConstants {
    val SCRIPT_CODE: String = """
/**
 * Google Apps Script for Investment Tracker
 * Paste this script in Google Sheets -> Extensions -> Apps Script
 * Click "Deploy" -> "New deployment" -> Select "Web app"
 * Set "Execute as: Me" and "Who has access: Anyone"
 * Copy the Web App URL into your Investment Tracker app settings!
 *
 * Ensures all dates are strictly stored, parsed, and outputted in DD-MM-YYYY format.
 */

function doGet(e) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  // Ensure Indian locale and IST timezone so Google Sheets never interprets DD-MM as MM-DD
  try {
    ss.setSpreadsheetLocale("en_IN");
    ss.setSpreadsheetTimeZone("Asia/Kolkata");
  } catch(e) {}

  var holdingsSheet = getOrCreateSheet(ss, "Holdings", getHoldingsHeaders(), [6]);
  var sipsSheet = getOrCreateSheet(ss, "SIPs", getSipsHeaders(), [6]);
  var committeesSheet = getOrCreateSheet(ss, "Committees", getCommitteesHeaders(), [6]);
  var iposSheet = getOrCreateSheet(ss, "IPOs", getIposHeaders(), [11, 12, 13, 14]);

  var holdings = readSheetData(holdingsSheet, parseHoldingRow);
  var sips = readSheetData(sipsSheet, parseSipRow);
  var committees = readSheetData(committeesSheet, parseCommitteeRow);
  var ipos = readSheetData(iposSheet, parseIpoRow);

  return responseJSON({
    status: "success",
    holdings: holdings,
    sips: sips,
    committees: committees,
    ipos: ipos,
    lastSynced: new Date().toISOString()
  });
}

function doPost(e) {
  try {
    var contents = e.postData ? JSON.parse(e.postData.contents) : {};
    var action = contents.action;
    var data = contents.data;
    var id = contents.id;

    var ss = SpreadsheetApp.getActiveSpreadsheet();
    try {
      ss.setSpreadsheetLocale("en_IN");
      ss.setSpreadsheetTimeZone("Asia/Kolkata");
    } catch(e) {}

    var holdingsSheet = getOrCreateSheet(ss, "Holdings", getHoldingsHeaders(), [6]);
    var sipsSheet = getOrCreateSheet(ss, "SIPs", getSipsHeaders(), [6]);
    var committeesSheet = getOrCreateSheet(ss, "Committees", getCommitteesHeaders(), [6]);
    var iposSheet = getOrCreateSheet(ss, "IPOs", getIposHeaders(), [11, 12, 13, 14]);

    if (action === "addHolding") {
      addRow(holdingsSheet, [
        data.id || "h-" + Date.now(),
        data.platform || "",
        data.category || "",
        Array.isArray(data.breakdown) ? data.breakdown.join(", ") : (data.breakdownRaw || ""),
        data.total || 0,
        formatDateToDdMmYyyy(data.tillDate),
        data.remarks || "",
        new Date().toISOString()
      ], [6]);
    } else if (action === "updateHolding") {
      updateRowById(holdingsSheet, id, [
        id,
        data.platform || "",
        data.category || "",
        Array.isArray(data.breakdown) ? data.breakdown.join(", ") : (data.breakdownRaw || ""),
        data.total || 0,
        formatDateToDdMmYyyy(data.tillDate),
        data.remarks || "",
        new Date().toISOString()
      ], [6]);
    } else if (action === "deleteHolding") {
      deleteRowById(holdingsSheet, id);
    } else if (action === "addSip") {
      addRow(sipsSheet, [
        data.id || "s-" + Date.now(),
        data.platform || "",
        data.instrument || "",
        data.amount || 0,
        data.frequency || "Monthly",
        formatDateToDdMmYyyy(data.since),
        data.remarks || "",
        data.isPaused ? "Paused" : "Active",
        new Date().toISOString()
      ], [6]);
    } else if (action === "updateSip") {
      updateRowById(sipsSheet, id, [
        id,
        data.platform || "",
        data.instrument || "",
        data.amount || 0,
        data.frequency || "Monthly",
        formatDateToDdMmYyyy(data.since),
        data.remarks || "",
        data.isPaused ? "Paused" : "Active",
        new Date().toISOString()
      ], [6]);
    } else if (action === "deleteSip") {
      deleteRowById(sipsSheet, id);
    } else if (action === "addCommittee") {
      addRow(committeesSheet, formatCommitteeRow(data), [6]);
    } else if (action === "updateCommittee") {
      updateRowById(committeesSheet, id, formatCommitteeRow(data), [6]);
    } else if (action === "deleteCommittee") {
      deleteRowById(committeesSheet, id);
    } else if (action === "addIpo") {
      addRow(iposSheet, formatIpoRow(data), [11, 12, 13, 14]);
    } else if (action === "updateIpo") {
      updateRowById(iposSheet, id, formatIpoRow(data), [11, 12, 13, 14]);
    } else if (action === "deleteIpo") {
      deleteRowById(iposSheet, id);
    } else if (action === "syncAll") {
      if (contents.holdings) {
        clearAndRewrite(holdingsSheet, getHoldingsHeaders(), contents.holdings.map(function(h) {
          return [
            h.id,
            h.platform || "",
            h.category || "",
            Array.isArray(h.breakdown) ? h.breakdown.join(", ") : (h.breakdownRaw || ""),
            h.total || 0,
            formatDateToDdMmYyyy(h.tillDate),
            h.remarks || "",
            new Date().toISOString()
          ];
        }), [6]);
      }
      if (contents.sips) {
        clearAndRewrite(sipsSheet, getSipsHeaders(), contents.sips.map(function(s) {
          return [
            s.id,
            s.platform || "",
            s.instrument || "",
            s.amount || 0,
            s.frequency || "Monthly",
            formatDateToDdMmYyyy(s.since),
            s.remarks || "",
            s.isPaused ? "Paused" : "Active",
            new Date().toISOString()
          ];
        }), [6]);
      }
      if (contents.committees) {
        clearAndRewrite(committeesSheet, getCommitteesHeaders(), contents.committees.map(function(c) {
          return formatCommitteeRow(c);
        }), [6]);
      }
      if (contents.ipos) {
        clearAndRewrite(iposSheet, getIposHeaders(), contents.ipos.map(function(i) {
          return formatIpoRow(i);
        }), [11, 12, 13, 14]);
      }
    }

    var updatedHoldings = readSheetData(holdingsSheet, parseHoldingRow);
    var updatedSips = readSheetData(sipsSheet, parseSipRow);
    var updatedCommittees = readSheetData(committeesSheet, parseCommitteeRow);
    var updatedIpos = readSheetData(iposSheet, parseIpoRow);

    return responseJSON({
      status: "success",
      message: "Operation completed successfully",
      action: action,
      holdings: updatedHoldings,
      sips: updatedSips,
      committees: updatedCommittees,
      ipos: updatedIpos
    });

  } catch (err) {
    return responseJSON({
      status: "error",
      message: err.toString()
    });
  }
}

// Helper to create case-insensitive alphanumeric lookup for header columns
function getHeaderMap(headers) {
  var map = {};
  if (!headers || !headers.length) return map;
  for (var i = 0; i < headers.length; i++) {
    var raw = String(headers[i] || "").trim().toLowerCase().replace(/[^a-z0-9]/g, "");
    if (raw) {
      map[raw] = i;
    }
  }
  return map;
}

function getColIdx(headerMap, possibleKeys, defaultIdx) {
  if (!headerMap) return defaultIdx;
  for (var i = 0; i < possibleKeys.length; i++) {
    var k = possibleKeys[i].toLowerCase().replace(/[^a-z0-9]/g, "");
    if (headerMap[k] !== undefined) {
      return headerMap[k];
    }
  }
  for (var key in headerMap) {
    for (var j = 0; j < possibleKeys.length; j++) {
      var target = possibleKeys[j].toLowerCase().replace(/[^a-z0-9]/g, "");
      if (key.indexOf(target) !== -1 || target.indexOf(key) !== -1) {
        return headerMap[key];
      }
    }
  }
  return defaultIdx;
}

// Format any date value into strict DD-MM-YYYY format
function formatDateToDdMmYyyy(val, displayVal) {
  // 1. Check formatted display value first if available (preserves exact user entry on sheet)
  if (displayVal !== undefined && displayVal !== null) {
    var strDisplay = String(displayVal).trim();
    if (strDisplay && strDisplay !== "-" && strDisplay !== "—" && strDisplay !== "N/A" && strDisplay.toLowerCase() !== "null" && strDisplay.toLowerCase() !== "undefined") {
      // Check DD-MM-YYYY or DD/MM/YYYY or DD.MM.YYYY (support 2 or 4 digit years)
      var dmy = strDisplay.match(/^(\d{1,2})[-/. ](\d{1,2})[-/. ](\d{2,4})(?:[ T].*)?$/);
      if (dmy) {
        var p1 = Number(dmy[1]);
        var p2 = Number(dmy[2]);
        var yr = Number(dmy[3]);
        if (yr < 100) yr += 2000;
        var dVal = p1;
        var mVal = p2;
        // In Indian context (Investment Tracker), DD-MM-YYYY is standard
        // Only if second part > 12 can it be US MM/DD/YYYY format
        if (p2 > 12) {
          dVal = p2;
          mVal = p1;
        }
        return (dVal < 10 ? "0" + dVal : "" + dVal) + "-" + (mVal < 10 ? "0" + mVal : "" + mVal) + "-" + yr;
      }

      // Check YYYY-MM-DD or YYYY/MM/DD
      var ymd = strDisplay.match(/^(\d{4})[-/. ](\d{1,2})[-/. ](\d{1,2})(?:[ T].*)?$/);
      if (ymd) {
        var y4 = ymd[1];
        var m2 = Number(ymd[2]);
        var d2 = Number(ymd[3]);
        return (d2 < 10 ? "0" + d2 : "" + d2) + "-" + (m2 < 10 ? "0" + m2 : "" + m2) + "-" + y4;
      }
    }
  }

  if (val === undefined || val === null || val === "") return "";

  // 2. If val is a Date object (NEVER use val.getDate() as it uses script engine's US/UTC timezone and causes 1-day shifts!)
  if (Object.prototype.toString.call(val) === "[object Date]" || val instanceof Date) {
    if (isNaN(val.getTime())) return "";
    var tz = "Asia/Kolkata";
    try {
      var activeSs = SpreadsheetApp.getActiveSpreadsheet();
      if (activeSs) tz = activeSs.getSpreadsheetTimeZone() || "Asia/Kolkata";
    } catch(e) {}
    return Utilities.formatDate(val, tz, "dd-MM-yyyy");
  }

  // 3. If val is a number (e.g. Excel/Google Sheets serial date)
  if (typeof val === "number" && val >= 20000 && val <= 80000) {
    var epochMillis = Math.round((val - 25569) * 86400 * 1000);
    var dateObj = new Date(epochMillis);
    if (!isNaN(dateObj.getTime())) {
      var tzNum = "Asia/Kolkata";
      try {
        var activeSs2 = SpreadsheetApp.getActiveSpreadsheet();
        if (activeSs2) tzNum = activeSs2.getSpreadsheetTimeZone() || "Asia/Kolkata";
      } catch(e) {}
      return Utilities.formatDate(dateObj, tzNum, "dd-MM-yyyy");
    }
  }

  // 4. String format processing
  var str = String(val).trim();
  if (!str || str === "-" || str === "—" || str === "N/A" || str.toLowerCase() === "undefined" || str.toLowerCase() === "null") {
    return "";
  }

  // Strip wrapping quotes
  str = str.replace(/^["']|["']$/g, "").trim();

  // Textual month names (e.g. "19-Aug-2026", "19 August 2026", "Aug 19, 2026", "19-Aug-26")
  var monthsMap = {
    jan: 1, january: 1, feb: 2, february: 2, mar: 3, march: 3, apr: 4, april: 4,
    may: 5, jun: 6, june: 6, jul: 7, july: 7, aug: 8, august: 8, sep: 9, september: 9,
    oct: 10, october: 10, nov: 11, november: 11, dec: 12, december: 12
  };
  var textMonthMatch1 = str.match(/^(\d{1,2})[-/ ]([A-Za-z]+)[-/ ](\d{2,4})(?:[ T].*)?$/);
  if (textMonthMatch1) {
    var d = Number(textMonthMatch1[1]);
    var mName = textMonthMatch1[2].toLowerCase();
    var y = Number(textMonthMatch1[3]);
    if (y < 100) y += 2000;
    if (monthsMap[mName]) {
      var m = monthsMap[mName];
      return (d < 10 ? "0" + d : "" + d) + "-" + (m < 10 ? "0" + m : "" + m) + "-" + y;
    }
  }
  var textMonthMatch2 = str.match(/^([A-Za-z]+)\s+(\d{1,2})[-, \/]+(\d{2,4})(?:[ T].*)?$/);
  if (textMonthMatch2) {
    var mName2 = textMonthMatch2[1].toLowerCase();
    var d2 = Number(textMonthMatch2[2]);
    var y2 = Number(textMonthMatch2[3]);
    if (y2 < 100) y2 += 2000;
    if (monthsMap[mName2]) {
      var m2 = monthsMap[mName2];
      return (d2 < 10 ? "0" + d2 : "" + d2) + "-" + (m2 < 10 ? "0" + m2 : "" + m2) + "-" + y2;
    }
  }

  // Check DD-MM-YYYY or DD/MM/YYYY or DD.MM.YYYY
  var dmyMatch = str.match(/^(\d{1,2})[-/. ](\d{1,2})[-/. ](\d{2,4})(?:[ T].*)?$/);
  if (dmyMatch) {
    var part1 = Number(dmyMatch[1]);
    var part2 = Number(dmyMatch[2]);
    var year = Number(dmyMatch[3]);
    if (year < 100) year += 2000;
    var day = part1;
    var month = part2;
    if (part2 > 12) { day = part2; month = part1; }
    return (day < 10 ? "0" + day : "" + day) + "-" + (month < 10 ? "0" + month : "" + month) + "-" + year;
  }

  // Check YYYY-MM-DD or YYYY/MM/DD
  var ymdMatch = str.match(/^(\d{4})[-/. ](\d{1,2})[-/. ](\d{1,2})(?:[ T].*)?$/);
  if (ymdMatch) {
    var yYear = ymdMatch[1];
    var mMonth = Number(ymdMatch[2]);
    var dDay = Number(ymdMatch[3]);
    return (dDay < 10 ? "0" + dDay : "" + dDay) + "-" + (mMonth < 10 ? "0" + mMonth : "" + mMonth) + "-" + yYear;
  }

  // Check ISO string
  if (str.indexOf("T") !== -1) {
    var parsedIso = new Date(str);
    if (!isNaN(parsedIso.getTime())) {
      var tzIso = "Asia/Kolkata";
      try {
        var activeSs3 = SpreadsheetApp.getActiveSpreadsheet();
        if (activeSs3) tzIso = activeSs3.getSpreadsheetTimeZone() || "Asia/Kolkata";
      } catch(e) {}
      return Utilities.formatDate(parsedIso, tzIso, "dd-MM-yyyy");
    }
  }

  // Check textual date fallback
  var parsedText = new Date(str);
  if (!isNaN(parsedText.getTime())) {
    var tzText = "Asia/Kolkata";
    try {
      var activeSs4 = SpreadsheetApp.getActiveSpreadsheet();
      if (activeSs4) tzText = activeSs4.getSpreadsheetTimeZone() || "Asia/Kolkata";
    } catch(e) {}
    return Utilities.formatDate(parsedText, tzText, "dd-MM-yyyy");
  }

  return str;
}

function getHoldingsHeaders() {
  return ["ID", "Platform", "Category", "Breakdown", "Total", "Till Date", "Remarks", "Last Updated"];
}

function getSipsHeaders() {
  return ["ID", "Platform", "Instrument", "Amount", "Frequency", "Since", "Remarks", "Status", "Last Updated"];
}

function getCommitteesHeaders() {
  return ["ID", "Name", "Total Amount", "Total Members", "Total Months", "Start Date", "Base Installment", "Current Month", "Has Taken Pot", "Pot Taken Month", "Pot Taken Amount", "Remarks", "Months Details", "Last Updated"];
}

function getIposHeaders() {
  return ["ID", "Company Name", "Segment", "Platform", "Applicant", "App No", "Cut-off Price", "Shares/Lot", "Lots Applied", "Amount Blocked", "Apply Date", "Allotment Date", "Refund Date", "Listing Date", "Status", "Allotted Lots", "Listing Price", "Listing Gain Amount", "Listing Gain %", "Remarks", "Last Updated"];
}

function getOrCreateSheet(ss, name, headers, dateCols) {
  var sheet = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
    sheet.appendRow(headers);
    sheet.getRange("1:1").setFontWeight("bold").setBackground("#f3f4f6");
  }
  if (dateCols && dateCols.length > 0) {
    var maxR = Math.max(sheet.getMaxRows(), 500);
    for (var k = 0; k < dateCols.length; k++) {
      sheet.getRange(2, dateCols[k], maxR - 1, 1).setNumberFormat("@");
    }
  }
  return sheet;
}

function readSheetData(sheet, parserFn) {
  var dataRange = sheet.getDataRange();
  var data = dataRange.getValues();
  var displayData = dataRange.getDisplayValues();
  if (data.length <= 1) return [];

  var headerRow = displayData[0] || data[0] || [];
  var headerMap = getHeaderMap(headerRow);

  var result = [];
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    var displayRow = displayData[i] || [];

    var hasContent = false;
    for (var c = 0; c < row.length; c++) {
      if (row[c] !== "" && row[c] !== null && row[c] !== undefined) {
        hasContent = true;
        break;
      }
    }

    if (hasContent) {
      result.push(parserFn(row, displayRow, headerMap, i));
    }
  }
  return result;
}

function parseHoldingRow(row, displayRow, headerMap, rowIndex) {
  var idIdx = getColIdx(headerMap, ["id"], -1);
  var platformIdx = getColIdx(headerMap, ["platform", "broker"], idIdx === -1 ? 0 : 1);
  var categoryIdx = getColIdx(headerMap, ["category", "type", "asset"], idIdx === -1 ? 1 : 2);
  var breakdownIdx = getColIdx(headerMap, ["breakdown", "items", "details"], idIdx === -1 ? 2 : 3);
  var totalIdx = getColIdx(headerMap, ["total", "amount", "value", "currentvalue"], idIdx === -1 ? 3 : 4);
  var tillDateIdx = getColIdx(headerMap, ["tilldate", "till", "date", "asofdate"], idIdx === -1 ? 4 : 5);
  var remarksIdx = getColIdx(headerMap, ["remarks", "remark", "notes", "note"], idIdx === -1 ? 5 : 6);

  var id = idIdx !== -1 && row[idIdx] ? String(row[idIdx]) : ("holding-" + rowIndex + "-" + Date.now());
  var platform = platformIdx !== -1 && row[platformIdx] ? String(row[platformIdx]) : "Portfolio";
  var category = categoryIdx !== -1 && row[categoryIdx] ? String(row[categoryIdx]) : "General";
  var breakdownRaw = breakdownIdx !== -1 && row[breakdownIdx] ? String(row[breakdownIdx]) : "";
  var breakdownArr = breakdownRaw ? breakdownRaw.split(",").map(function(s) { return Number(s.trim()); }).filter(function(n) { return !isNaN(n); }) : [];
  var total = totalIdx !== -1 && row[totalIdx] ? Number(row[totalIdx]) || 0 : 0;
  var tillDateVal = tillDateIdx !== -1 ? row[tillDateIdx] : null;
  var tillDateDisplay = tillDateIdx !== -1 && displayRow ? displayRow[tillDateIdx] : null;
  var tillDate = formatDateToDdMmYyyy(tillDateVal, tillDateDisplay);
  var remarks = remarksIdx !== -1 && row[remarksIdx] ? String(row[remarksIdx]) : "";

  return {
    id: id,
    platform: platform,
    category: category,
    breakdown: breakdownArr,
    breakdownRaw: breakdownRaw,
    total: total,
    tillDate: tillDate,
    remarks: remarks,
    updatedAt: new Date().toISOString()
  };
}

function parseSipRow(row, displayRow, headerMap, rowIndex) {
  var idIdx = getColIdx(headerMap, ["id"], -1);
  var platformIdx = getColIdx(headerMap, ["platform", "broker", "source"], idIdx === -1 ? 0 : 1);
  var instrumentIdx = getColIdx(headerMap, ["instrument", "fund", "name", "scheme"], idIdx === -1 ? 1 : 2);
  var amountIdx = getColIdx(headerMap, ["amount", "monthlyamount", "value", "investment"], idIdx === -1 ? 2 : 3);
  var frequencyIdx = getColIdx(headerMap, ["frequency", "freq", "period"], idIdx === -1 ? 3 : 4);
  var sinceIdx = getColIdx(headerMap, ["since", "sincedate", "startdate", "date", "till"], idIdx === -1 ? 4 : 5);
  var remarksIdx = getColIdx(headerMap, ["remarks", "remark", "notes", "note", "desc"], idIdx === -1 ? 5 : 6);

  var id = idIdx !== -1 && row[idIdx] ? String(row[idIdx]) : ("sip-" + rowIndex + "-" + Date.now());
  var platform = platformIdx !== -1 && row[platformIdx] ? String(row[platformIdx]) : "";
  var instrument = instrumentIdx !== -1 && row[instrumentIdx] ? String(row[instrumentIdx]) : "";
  var amount = amountIdx !== -1 && row[amountIdx] ? Number(row[amountIdx]) || 0 : 0;
  var frequency = frequencyIdx !== -1 && row[frequencyIdx] ? String(row[frequencyIdx]) : "Monthly";
  var sinceVal = sinceIdx !== -1 ? row[sinceIdx] : null;
  var sinceDisplay = sinceIdx !== -1 && displayRow ? displayRow[sinceIdx] : null;
  var since = formatDateToDdMmYyyy(sinceVal, sinceDisplay);
  var remarks = remarksIdx !== -1 && row[remarksIdx] ? String(row[remarksIdx]) : "";
  var statusIdx = getColIdx(headerMap, ["status", "state", "ispaused", "paused"], -1);
  var statusStr = statusIdx !== -1 && row[statusIdx] ? String(row[statusIdx]).trim().toLowerCase() : "";
  var isPaused = statusStr === "paused" || statusStr === "true";

  return {
    id: id,
    platform: platform,
    instrument: instrument,
    amount: amount,
    frequency: frequency,
    since: since,
    remarks: remarks,
    status: isPaused ? "Paused" : "Active",
    isPaused: isPaused,
    updatedAt: new Date().toISOString()
  };
}

function parseCommitteeRow(row, displayRow, headerMap, rowIndex) {
  var idIdx = getColIdx(headerMap, ["id"], -1);
  var nameIdx = getColIdx(headerMap, ["name", "committee"], idIdx === -1 ? 0 : 1);
  var totalAmountIdx = getColIdx(headerMap, ["totalamount", "amount", "total"], idIdx === -1 ? 1 : 2);
  var totalMembersIdx = getColIdx(headerMap, ["totalmembers", "members"], idIdx === -1 ? 2 : 3);
  var totalMonthsIdx = getColIdx(headerMap, ["totalmonths", "months"], idIdx === -1 ? 3 : 4);
  var startDateIdx = getColIdx(headerMap, ["startdate", "start", "date"], idIdx === -1 ? 4 : 5);
  var baseInstallmentIdx = getColIdx(headerMap, ["baseinstallment", "installment", "monthly"], idIdx === -1 ? 5 : 6);
  var currentMonthIdx = getColIdx(headerMap, ["currentmonth", "currmonth"], idIdx === -1 ? 6 : 7);
  var hasTakenPotIdx = getColIdx(headerMap, ["hastakenpot", "takenpot", "pottaken"], idIdx === -1 ? 7 : 8);
  var potTakenMonthIdx = getColIdx(headerMap, ["pottakenmonth", "potmonth"], idIdx === -1 ? 8 : 9);
  var potTakenAmountIdx = getColIdx(headerMap, ["pottakenamount", "potamount"], idIdx === -1 ? 9 : 10);
  var remarksIdx = getColIdx(headerMap, ["remarks", "remark", "notes", "note"], idIdx === -1 ? 10 : 11);
  var monthsIdx = getColIdx(headerMap, ["monthsdetails", "months"], idIdx === -1 ? 11 : 12);

  var monthsParsed = [];
  if (monthsIdx !== -1 && row[monthsIdx]) {
    try {
      monthsParsed = JSON.parse(String(row[monthsIdx]));
    } catch(e) {
      monthsParsed = [];
    }
  }

  var id = idIdx !== -1 && row[idIdx] ? String(row[idIdx]) : ("comm-" + rowIndex + "-" + Date.now());
  var name = nameIdx !== -1 && row[nameIdx] ? String(row[nameIdx]) : "Committee";
  var totalAmount = totalAmountIdx !== -1 && row[totalAmountIdx] ? Number(row[totalAmountIdx]) || 0 : 0;
  var totalMembers = totalMembersIdx !== -1 && row[totalMembersIdx] ? Number(row[totalMembersIdx]) || 12 : 12;
  var totalMonths = totalMonthsIdx !== -1 && row[totalMonthsIdx] ? Number(row[totalMonthsIdx]) || 12 : 12;
  var startDateVal = startDateIdx !== -1 ? row[startDateIdx] : null;
  var startDateDisplay = startDateIdx !== -1 && displayRow ? displayRow[startDateIdx] : null;
  var startDate = formatDateToDdMmYyyy(startDateVal, startDateDisplay);
  var baseInstallment = baseInstallmentIdx !== -1 && row[baseInstallmentIdx] ? Number(row[baseInstallmentIdx]) || 0 : 0;
  var currentMonth = currentMonthIdx !== -1 && row[currentMonthIdx] ? Number(row[currentMonthIdx]) || 1 : 1;
  var hasTakenPot = hasTakenPotIdx !== -1 ? Boolean(row[hasTakenPotIdx] === true || String(row[hasTakenPotIdx]).toLowerCase() === "true") : false;
  var potTakenMonth = potTakenMonthIdx !== -1 && row[potTakenMonthIdx] !== "" ? Number(row[potTakenMonthIdx]) : undefined;
  var potTakenAmount = potTakenAmountIdx !== -1 && row[potTakenAmountIdx] !== "" ? Number(row[potTakenAmountIdx]) : undefined;
  var remarks = remarksIdx !== -1 && row[remarksIdx] ? String(row[remarksIdx]) : "";

  return {
    id: id,
    name: name,
    totalAmount: totalAmount,
    totalMembers: totalMembers,
    totalMonths: totalMonths,
    startDate: startDate,
    baseMonthlyInstallment: baseInstallment,
    currentMonth: currentMonth,
    hasTakenPot: hasTakenPot,
    potTakenMonth: potTakenMonth,
    potTakenAmount: potTakenAmount,
    remarks: remarks,
    months: monthsParsed,
    updatedAt: new Date().toISOString()
  };
}

function formatCommitteeRow(data) {
  return [
    data.id || "comm-" + Date.now(),
    data.name || "",
    data.totalAmount || 0,
    data.totalMembers || 12,
    data.totalMonths || 12,
    formatDateToDdMmYyyy(data.startDate),
    data.baseMonthlyInstallment || 0,
    data.currentMonth || 1,
    data.hasTakenPot ? "TRUE" : "FALSE",
    data.potTakenMonth || "",
    data.potTakenAmount || "",
    data.remarks || "",
    JSON.stringify(data.months || []),
    new Date().toISOString()
  ];
}

function parseIpoRow(row, displayRow, headerMap, rowIndex) {
  var idIdx = getColIdx(headerMap, ["id"], -1);
  var companyIdx = getColIdx(headerMap, ["companyname", "company", "name"], idIdx === -1 ? 0 : 1);
  var categoryIdx = getColIdx(headerMap, ["segment", "category"], idIdx === -1 ? 1 : 2);
  var platformIdx = getColIdx(headerMap, ["platform", "broker"], idIdx === -1 ? 2 : 3);
  var applicantIdx = getColIdx(headerMap, ["applicant", "applicantname"], idIdx === -1 ? 3 : 4);
  var appNoIdx = getColIdx(headerMap, ["appno", "applicationno"], idIdx === -1 ? 4 : 5);
  var cutOffPriceIdx = getColIdx(headerMap, ["cutoffprice", "price"], idIdx === -1 ? 5 : 6);
  var sharesPerLotIdx = getColIdx(headerMap, ["shareslot", "sharesperlot", "shares"], idIdx === -1 ? 6 : 7);
  var lotsAppliedIdx = getColIdx(headerMap, ["lotsapplied", "lots"], idIdx === -1 ? 7 : 8);
  var totalBlockedIdx = getColIdx(headerMap, ["amountblocked", "totalamountblocked", "blocked"], idIdx === -1 ? 8 : 9);
  var applyDateIdx = getColIdx(headerMap, ["applydate", "apply"], idIdx === -1 ? 9 : 10);
  var allotmentDateIdx = getColIdx(headerMap, ["allotmentdate", "allotment"], idIdx === -1 ? 10 : 11);
  var refundDateIdx = getColIdx(headerMap, ["refunddate", "refund"], idIdx === -1 ? 11 : 12);
  var listingDateIdx = getColIdx(headerMap, ["listingdate", "listing"], idIdx === -1 ? 12 : 13);
  var statusIdx = getColIdx(headerMap, ["status"], idIdx === -1 ? 13 : 14);
  var allottedLotsIdx = getColIdx(headerMap, ["allottedlots", "allotted"], idIdx === -1 ? 14 : 15);
  var listingPriceIdx = getColIdx(headerMap, ["listingprice"], idIdx === -1 ? 15 : 16);
  var gainAmountIdx = getColIdx(headerMap, ["listinggainamount", "gainamount"], idIdx === -1 ? 16 : 17);
  var gainPercentIdx = getColIdx(headerMap, ["listinggainpercent", "gainpercent", "gain"], idIdx === -1 ? 17 : 18);
  var remarksIdx = getColIdx(headerMap, ["remarks", "remark", "notes", "note"], idIdx === -1 ? 18 : 19);

  var id = idIdx !== -1 && row[idIdx] ? String(row[idIdx]) : ("ipo-" + rowIndex + "-" + Date.now());
  var companyName = companyIdx !== -1 && row[companyIdx] ? String(row[companyIdx]) : "";
  var category = categoryIdx !== -1 && row[categoryIdx] ? String(row[categoryIdx]) : "Mainboard";
  var platform = platformIdx !== -1 && row[platformIdx] ? String(row[platformIdx]) : "Zerodha";
  var applicantName = applicantIdx !== -1 && row[applicantIdx] ? String(row[applicantIdx]) : "Self";
  var applicationNo = appNoIdx !== -1 && row[appNoIdx] ? String(row[appNoIdx]) : "";
  var cutOffPrice = cutOffPriceIdx !== -1 && row[cutOffPriceIdx] ? Number(row[cutOffPriceIdx]) || 0 : 0;
  var sharesPerLot = sharesPerLotIdx !== -1 && row[sharesPerLotIdx] ? Number(row[sharesPerLotIdx]) || 1 : 1;
  var lotsApplied = lotsAppliedIdx !== -1 && row[lotsAppliedIdx] ? Number(row[lotsAppliedIdx]) || 1 : 1;
  var totalAmountBlocked = totalBlockedIdx !== -1 && row[totalBlockedIdx] ? Number(row[totalBlockedIdx]) || 0 : 0;

  var applyDate = applyDateIdx !== -1 ? formatDateToDdMmYyyy(row[applyDateIdx], displayRow ? displayRow[applyDateIdx] : null) : "";
  var allotmentDate = allotmentDateIdx !== -1 ? formatDateToDdMmYyyy(row[allotmentDateIdx], displayRow ? displayRow[allotmentDateIdx] : null) : "";
  var refundDate = refundDateIdx !== -1 ? formatDateToDdMmYyyy(row[refundDateIdx], displayRow ? displayRow[refundDateIdx] : null) : "";
  var listingDate = listingDateIdx !== -1 ? formatDateToDdMmYyyy(row[listingDateIdx], displayRow ? displayRow[listingDateIdx] : null) : "";

  var status = statusIdx !== -1 && row[statusIdx] ? String(row[statusIdx]) : "Applied";
  var allottedLots = allottedLotsIdx !== -1 && row[allottedLotsIdx] !== "" && row[allottedLotsIdx] !== undefined ? Number(row[allottedLotsIdx]) : undefined;
  var listingPrice = listingPriceIdx !== -1 && row[listingPriceIdx] !== "" && row[listingPriceIdx] !== undefined ? Number(row[listingPriceIdx]) : undefined;
  var listingGainAmount = gainAmountIdx !== -1 && row[gainAmountIdx] !== "" && row[gainAmountIdx] !== undefined ? Number(row[gainAmountIdx]) : undefined;
  var listingGainPercent = gainPercentIdx !== -1 && row[gainPercentIdx] !== "" && row[gainPercentIdx] !== undefined ? Number(row[gainPercentIdx]) : undefined;
  var remarks = remarksIdx !== -1 && row[remarksIdx] ? String(row[remarksIdx]) : "";

  return {
    id: id,
    companyName: companyName,
    category: category,
    platform: platform,
    applicantName: applicantName,
    applicationNo: applicationNo,
    cutOffPrice: cutOffPrice,
    sharesPerLot: sharesPerLot,
    lotsApplied: lotsApplied,
    totalAmountBlocked: totalAmountBlocked,
    applyDate: applyDate,
    allotmentDate: allotmentDate,
    refundDate: refundDate,
    listingDate: listingDate,
    status: status,
    allottedLots: allottedLots,
    listingPrice: listingPrice,
    listingGainAmount: listingGainAmount,
    listingGainPercent: listingGainPercent,
    remarks: remarks,
    updatedAt: new Date().toISOString()
  };
}

function formatIpoRow(data) {
  return [
    data.id || "ipo-" + Date.now(),
    data.companyName || "",
    data.category || "Mainboard",
    data.platform || "Zerodha",
    data.applicantName || "Self",
    data.applicationNo || "",
    data.cutOffPrice || 0,
    data.sharesPerLot || 1,
    data.lotsApplied || 1,
    data.totalAmountBlocked || 0,
    formatDateToDdMmYyyy(data.applyDate),
    formatDateToDdMmYyyy(data.allotmentDate),
    formatDateToDdMmYyyy(data.refundDate),
    formatDateToDdMmYyyy(data.listingDate),
    data.status || "Applied",
    data.allottedLots !== undefined ? data.allottedLots : "",
    data.listingPrice !== undefined ? data.listingPrice : "",
    data.listingGainAmount !== undefined ? data.listingGainAmount : "",
    data.listingGainPercent !== undefined ? data.listingGainPercent : "",
    data.remarks || "",
    new Date().toISOString()
  ];
}

function addRow(sheet, rowValues, dateCols) {
  sheet.appendRow(rowValues);
  if (dateCols && dateCols.length > 0) {
    var lastRow = sheet.getLastRow();
    for (var k = 0; k < dateCols.length; k++) {
      sheet.getRange(lastRow, dateCols[k]).setNumberFormat("@");
    }
  }
}

function updateRowById(sheet, id, newRowValues, dateCols) {
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]) === String(id)) {
      sheet.getRange(i + 1, 1, 1, newRowValues.length).setValues([newRowValues]);
      if (dateCols && dateCols.length > 0) {
        for (var k = 0; k < dateCols.length; k++) {
          sheet.getRange(i + 1, dateCols[k]).setNumberFormat("@");
        }
      }
      return true;
    }
  }
  sheet.appendRow(newRowValues);
  if (dateCols && dateCols.length > 0) {
    var lastRow = sheet.getLastRow();
    for (var k = 0; k < dateCols.length; k++) {
      sheet.getRange(lastRow, dateCols[k]).setNumberFormat("@");
    }
  }
  return false;
}

function deleteRowById(sheet, id) {
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) {
    if (String(data[i][0]) === String(id)) {
      sheet.deleteRow(i + 1);
      return true;
    }
  }
  return false;
}

function clearAndRewrite(sheet, headers, rowsData, dateCols) {
  sheet.clearContents();
  sheet.appendRow(headers);
  sheet.getRange("1:1").setFontWeight("bold").setBackground("#f3f4f6");
  if (dateCols && dateCols.length > 0) {
    var maxR = Math.max(rowsData ? rowsData.length + 50 : 200, 200);
    for (var k = 0; k < dateCols.length; k++) {
      sheet.getRange(2, dateCols[k], maxR, 1).setNumberFormat("@");
    }
  }
  if (rowsData && rowsData.length > 0) {
    sheet.getRange(2, 1, rowsData.length, headers.length).setValues(rowsData);
  }
}

function responseJSON(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
""".trimIndent()
}
