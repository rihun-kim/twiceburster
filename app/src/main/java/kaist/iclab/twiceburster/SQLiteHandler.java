package kaist.iclab.twiceburster;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import static kaist.iclab.twiceburster.SQLite.$helper;

public class SQLiteHandler extends IntentService {
    public SQLiteHandler() {
        super("SQLiteHandler");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent _intent) {
        if (_intent != null) {
            // 8
            if (_intent.getStringExtra("CATEGORY").equals("HH(ACTIVITY)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        _intent.getIntExtra("VEHICLE", 0),
                        _intent.getIntExtra("BICYCLE", 0),
                        _intent.getIntExtra("FOOT", 0),
                        _intent.getIntExtra("RUNNING", 0),
                        _intent.getIntExtra("STILL", 0),
                        _intent.getIntExtra("WALKING", 0),
                        _intent.getIntExtra("UNKNOWN", 0));
                $helper.close();
            }

            // 3
            else if (_intent.getStringExtra("CATEGORY").equals("HH(LOCATION)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        _intent.getDoubleExtra("LATITUDE", 0),
                        _intent.getDoubleExtra("LONGITUDE", 0));
                $helper.close();
            }

            // 12
            else if (_intent.getStringExtra("CATEGORY").equals("NS(POSTED)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "POSTED",
                        _intent.getStringExtra("PACKAGE"),
                        _intent.getStringExtra("TITLE"),
                        _intent.getStringExtra("TEXT"),
                        _intent.getStringExtra("SUBTEXT"),
                        _intent.getStringExtra("SOUND"),
                        _intent.getStringExtra("VIBRATE"),
                        _intent.getIntExtra("DEFAULTS", 0),
                        _intent.getIntExtra("LEDONMS", 0),
                        _intent.getIntExtra("LEDOFFNMS", 0),
                        _intent.getIntExtra("LEDARGB", 0));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("NS(REMOVED)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "REMOVED",
                        _intent.getStringExtra("PACKAGE"),
                        _intent.getStringExtra("TITLE"),
                        _intent.getStringExtra("TEXT"),
                        _intent.getStringExtra("SUBTEXT"),
                        "", "", 0, 0, 0, 0);
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("NR(RINGER)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "RINGER",
                        _intent.getStringExtra("TYPE"),
                        "", "", "", "", "", 0, 0, 0, 0);
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("NR(DATA)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "DATA",
                        _intent.getStringExtra("TRANSMIT"),
                        _intent.getStringExtra("RECEIVE"),
                        "", "", "", "", 0, 0, 0, 0);
                $helper.close();
            }

            // 6
            else if (_intent.getStringExtra("CATEGORY").equals("HR(BATTERY)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "BATTERY",
                        _intent.getStringExtra("VOLTAGE"),
                        _intent.getStringExtra("LEVEL"),
                        _intent.getIntExtra("PLUGGED", 0),
                        _intent.getIntExtra("STATUS", 0));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("HR(WIFI1)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "WIFI1",
                        _intent.getStringExtra("SSID"),
                        _intent.getStringExtra("BSSID"),
                        _intent.getIntExtra("RSSI", 0),
                        _intent.getIntExtra("IP", 0));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("HR(WIFI2)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "WIFI2",
                        _intent.getStringExtra("SSID"),
                        _intent.getStringExtra("BSSID"),
                        _intent.getIntExtra("RSSI", 0),
                        0);
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("HR(CELL1)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "CELL1",
                        "",
                        _intent.getStringExtra("NID"),
                        _intent.getIntExtra("LAT", 0),
                        _intent.getIntExtra("LON", 0));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("HR(CELL2)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "CELL2",
                        "",
                        "",
                        _intent.getIntExtra("CID", 0),
                        _intent.getIntExtra("LAC", 0));
                $helper.close();
            }

            // 3
            else if (_intent.getStringExtra("CATEGORY").equals("AS(ANDROID)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "ANDROID",
                        _intent.getStringExtra("PACKAGE"));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("AS(INSTALLED)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TYPE"),
                        "INSTALLED",
                        _intent.getStringExtra("PACKAGE"));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("AS(RUNNING)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "RUNNING",
                        _intent.getStringExtra("PACKAGE"));
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("AS(KEYBOARD)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "KEYBOARD",
                        "");
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("AS(SHORTTOUCH)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "SHORTTOUCH",
                        "");
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("AS(LONGTOUCH)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "LONGTOUCH",
                        "");
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("AS(SCROLLTOUCH)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "SCROLLTOUCH",
                        "");
                $helper.close();
            }

            // 6
            else if (_intent.getStringExtra("CATEGORY").equals("LH(CALL)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "CALL",
                        _intent.getStringExtra("NUMBER"),
                        _intent.getStringExtra("TYPE"),
                        _intent.getStringExtra("DURATION"),
                        "");
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("LH(CONTACTS)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("LASTTIME"),
                        "CONTACTS",
                        _intent.getStringExtra("NUMBER"),
                        "",
                        "",
                        "");
                $helper.close();
            } else if (_intent.getStringExtra("CATEGORY").equals("LH(SMS)")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteInsert(
                        _intent.getStringExtra("TIMESTAMP"),
                        "SMS",
                        _intent.getStringExtra("NUMBER"),
                        _intent.getStringExtra("TYPE"),
                        _intent.getStringExtra("BODY"),
                        _intent.getStringExtra("READ"));
                $helper.close();
            }

            // 1
            else if (_intent.getStringExtra("CATEGORY").equals("AUTOSAVE")) {
                $helper = new SQLite(getApplicationContext());
                $helper.SQLiteExport();
                $helper.close();
            }
        }

    }
}