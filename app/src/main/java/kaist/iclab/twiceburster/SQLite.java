package kaist.iclab.twiceburster;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLite extends SQLiteOpenHelper {
    private Context $context;

    public static SQLite $helper;

    public SQLite(Context _context) {
        super(_context, "DATABASE.db", null, 1);
        $context = _context;
    }

    @Override
    public void onCreate(SQLiteDatabase _database) {
        _database.execSQL("CREATE TABLE ACTIVITYTABLE (" +
                "TIMESTAMP TEXT, " +
                "VEHICLE INTEGER, " +
                "BICYCLE INTEGER, " +
                "FOOT INTEGER, " +
                "RUNNING INTEGER, " +
                "STILL INTEGER, " +
                "WALKING INTEGER, " +
                "UNKNOWN INTEGER)");

        _database.execSQL("CREATE TABLE GPSTABLE (" +
                "TIMESTAMP TEXT, " +
                "LATITUDE DOUBLE, " +
                "LONGITUDE DOUBLE)");

        _database.execSQL("CREATE TABLE NOTIFICATIONTABLE (" +
                "TIMESTAMP TEXT, " +
                "POSTREMOVALRINGER TEXT, " +
                "PACKAGE TEXT, " +
                "TITLE TEXT, " +
                "TEXT TEXT, " +
                "SUBTEXT TEXT, " +
                "SOUND TEXT, " +
                "VIBRATE TEXT, " +
                "DEFAULTS INT, " +
                "LEDON INT, " +
                "LEDOFF INT, " +
                "LEDRGB INT)");

        _database.execSQL("CREATE TABLE HARDWARETABLE (" +
                "TIMESTAMP TEXT, " +
                "BATTERYWIFICELL TEXT, " +
                "SSID TEXT, " +
                "BSSID TEXT, " +
                "RSSI INT, " +
                "IP INT)");

        _database.execSQL("CREATE TABLE APPTABLE (" +
                "TIMESTAMPTYPE TEXT, " +
                "ANDROIDINSTALLEDRUNNING TEXT, " +
                "PACKAGE TEXT)");

        _database.execSQL("CREATE TABLE LOGTABLE (" +
                "TIMESTAMPLASTTIME TEXT, " +
                "CALLCONTACTSSMS TEXT, " +
                "NUMBER TEXT, " +
                "TYPE TEXT, " +
                "DURATIONBODY TEXT, " +
                "READ TEXT)");
    }

    public void SQLiteInsert(String _timestamp,
                             int _vehicle,
                             int _bicycle,
                             int _Foot,
                             int _running,
                             int _still,
                             int _walking,
                             int _unknown) {
        SQLiteDatabase database = $helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIMESTAMP", _timestamp);
        values.put("VEHICLE", _vehicle);
        values.put("BICYCLE", _bicycle);
        values.put("FOOT", _Foot);
        values.put("RUNNING", _running);
        values.put("STILL", _still);
        values.put("WALKING", _walking);
        values.put("UNKNOWN", _unknown);
        database.insert("ACTIVITYTABLE", null, values);
    }

    public void SQLiteInsert(String _timestamp,
                             double _latitude,
                             double _longitude) {
        SQLiteDatabase database = $helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIMESTAMP", _timestamp);
        values.put("LATITUDE", _latitude);
        values.put("LONGITUDE", _longitude);
        database.insert("GPSTABLE", null, values);
    }

    public void SQLiteInsert(String _timestamp,
                             String _postremovalringer,
                             String _package,
                             String _title,
                             String _text,
                             String _subtext,
                             String _sound,
                             String _vibrate,
                             int _defaults,
                             int _ledon,
                             int _ledoff,
                             int _ledrgb) {
        SQLiteDatabase database = $helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIMESTAMP", _timestamp);
        values.put("POSTREMOVALRINGER", _postremovalringer);
        values.put("PACKAGE", _package);
        values.put("TITLE", _title);
        values.put("TEXT", _text);
        values.put("SUBTEXT", _subtext);
        values.put("SOUND", _sound);
        values.put("VIBRATE", _vibrate);
        values.put("DEFAULTS", _defaults);
        values.put("LEDON", _ledon);
        values.put("LEDOFF", _ledoff);
        values.put("LEDRGB", _ledrgb);
        database.insert("NOTIFICATIONTABLE", null, values);
    }

    public void SQLiteInsert(String _timestamptype,
                             String _androidinstalledrunning,
                             String _package) {
        SQLiteDatabase database = $helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIMESTAMPTYPE", _timestamptype);
        values.put("ANDROIDINSTALLEDRUNNING", _androidinstalledrunning);
        values.put("PACKAGE", _package);
        database.insert("APPTABLE", null, values);
    }

    public void SQLiteInsert(String _timestamplasttime,
                             String _callcontactssms,
                             String _number,
                             String _type,
                             String _durationbody,
                             String _read) {
        SQLiteDatabase database = $helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIMESTAMPLASTTIME", _timestamplasttime);
        values.put("CALLCONTACTSSMS", _callcontactssms);
        values.put("NUMBER", _number);
        values.put("TYPE", _type);
        values.put("DURATIONBODY", _durationbody);
        values.put("READ", _read);
        database.insert("LOGTABLE", null, values);
    }

    public void SQLiteInsert(String _timestamp,
                             String _batterywificell,
                             String _ssid,
                             String _bssid,
                             int _rssi,
                             int _ip) {
        SQLiteDatabase database = $helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIMESTAMP", _timestamp);
        values.put("BATTERYWIFICELL", _batterywificell);
        values.put("SSID", _ssid);
        values.put("BSSID", _bssid);
        values.put("RSSI", _rssi);
        values.put("IP", _ip);
        database.insert("HARDWARETABLE", null, values);
    }

    public void SQLiteExport() {
        try {
            File internal = Environment.getDataDirectory();
            File external = Environment.getExternalStorageDirectory();

            File directory = new File(external.getAbsolutePath() + "/TWICEBURSTER");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (external.canWrite()) {
                File currentDB = new File(internal, "/data/kaist.iclab.twiceburster/databases/DATABASE.db");
                File exportDB = new File(external, "/TWICEBURSTER/Log_" + new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())) + ".db");

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(exportDB).getChannel();

                    dst.transferFrom(src, 0, src.size());

                    src.close();
                    dst.close();

                    Log.e("Ria", ">>> SQLite > SQLiteExport : 자동으로 파일을 출력하였습니다.");
                    Toast.makeText($context, "자동으로 파일을 출력하였습니다.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {

        }

        SQLiteDelete();
    }

    public void SQLiteDelete() {
        SQLiteDatabase database = $helper.getWritableDatabase();

        database.delete("ACTIVITYTABLE", null, null);
        database.delete("APPTABLE", null, null);
        database.delete("GPSTABLE", null, null);
        database.delete("HARDWARETABLE", null, null);
        database.delete("LOGTABLE", null, null);
        database.delete("NOTIFICATIONTABLE", null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

