package kaist.iclab.twiceburster;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

public class LogHandler extends IntentService {
    private static long $previousCallLogTimestamp = 0;
    private static long $previousSmsLogTimestamp = 0;

    public LogHandler() {
        super("LogHandler");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent _intent) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        }

        Cursor cursor = getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, "date ASC");

        if (cursor != null && cursor.moveToLast()) {
            long previousCallLogTimestamp = cursor.getLong(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
            long callLogTimestamp = previousCallLogTimestamp;
            int callLogCount = 1;

            while ($previousCallLogTimestamp != callLogTimestamp) {
                Log.e("Ria", "LogHandler(call) >>> onHandleIntent : 시간 " + simpleDateFormat.format(callLogTimestamp));
                Log.e("Ria", "LogHandler(call) >>> onHandleIntent : 번호 " + cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER)));
                Log.e("Ria", "LogHandler(call) >>> onHandleIntent : 타입 " + cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE)));
                Log.e("Ria", "LogHandler(call) >>> onHandleIntent : 지속 " + cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION)));

                try {
                    Intent intent = new Intent(this, SQLiteHandler.class);
                    intent.putExtra("CATEGORY", "LH(CALL)");
                    intent.putExtra("TIMESTAMP", simpleDateFormat.format(callLogTimestamp));
                    intent.putExtra("NUMBER", anonymizing(cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER))));
                    intent.putExtra("TYPE", cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE)));
                    intent.putExtra("DURATION", cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION)));

                    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

                if (!cursor.moveToPrevious() || callLogCount == 100) {
                    break;
                }

                callLogTimestamp = cursor.getLong(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
                callLogCount++;
            }

            $previousCallLogTimestamp = previousCallLogTimestamp;
            cursor.close();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        }

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Log.e("Ria", "LogHandler(contacts) >>> onHandleIntent : 번호 " + cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                Log.e("Ria", "LogHandler(contacts) >>> onHandleIntent : 연락 " + simpleDateFormat.format(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED))));

                try {
                    Intent intent = new Intent(this, SQLiteHandler.class);
                    intent.putExtra("CATEGORY", "LH(CONTACTS)");
                    intent.putExtra("LASTTIME", simpleDateFormat.format(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED))));
                    intent.putExtra("NUMBER", anonymizing(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));

                    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

            cursor.close();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
        }

        cursor = getContentResolver().query(Uri.parse("content://sms"), new String[]{"date", "type", "address", "body", "read"}, null, null, "date ASC");

        if (cursor != null && cursor.moveToLast()) {
            long previousSmsLogTimestamp = cursor.getLong(0);
            long smsLogTimestamp = previousSmsLogTimestamp;
            int smsLogCount = 1;

            while ($previousSmsLogTimestamp != smsLogTimestamp) {
                Log.e("Ria", "LogHandler(sms) >>> onHandleIntent : 시간 " + simpleDateFormat.format(smsLogTimestamp));
                Log.e("Ria", "LogHandler(sms) >>> onHandleIntent : 번호 " + cursor.getString(2));
                Log.e("Ria", "LogHandler(sms) >>> onHandleIntent : 타입 " + Long.toString(cursor.getLong(1)));
                Log.e("Ria", "LogHandler(sms) >>> onHandleIntent : 내용 " + cursor.getString(3));
                Log.e("Ria", "LogHandler(sms) >>> onHandleIntent : 읽음 " + Long.toString(cursor.getLong(4)));

                try {
                    Intent intent = new Intent(this, SQLiteHandler.class);
                    intent.putExtra("CATEGORY", "LH(SMS)");
                    intent.putExtra("TIMESTAMP", simpleDateFormat.format(smsLogTimestamp));
                    intent.putExtra("NUMBER", anonymizing(cursor.getString(2)));
                    intent.putExtra("TYPE", Long.toString(cursor.getLong(1)));
                    intent.putExtra("BODY", String.valueOf(cursor.getString(3).length()));
                    intent.putExtra("READ", Long.toString(cursor.getLong(4)));

                    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

                if (!cursor.moveToPrevious() || smsLogCount == 100) {
                    break;
                }

                smsLogTimestamp = cursor.getLong(0);
                smsLogCount++;
            }

            $previousSmsLogTimestamp = previousSmsLogTimestamp;
            cursor.close();
        }
    }

    private String anonymizing(String _number) {
        if(_number != null) {
            String number = _number.replaceAll("[^0-9]", "");
            StringBuilder stringBuilder = new StringBuilder();

            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(number.getBytes());
                byte byteData[] = messageDigest.digest();

                for (byte aByteData : byteData) {
                    stringBuilder.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if (number.length() > 5) {
                return number.substring(0, 4) + "_" + stringBuilder.toString();
            } else {
                return number + "_" + stringBuilder.toString();
            }
        } else {
            return "null";
        }
    }
}