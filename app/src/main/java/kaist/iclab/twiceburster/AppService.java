package kaist.iclab.twiceburster;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AppService extends AccessibilityService {
    private static String $previousPackageName = "GodLovesYou";

    @Override
    public void onCreate() {
        Log.e("Ria", ">>> AppService > onCreate : 앱 서비스를 시작합니다.");
    }

    @Override
    public int onStartCommand(Intent _intent, int _flags, int _startId) {
        Log.e("Ria", ">>> AppService(android) > onStartCommand : 모델 " + Build.MODEL);
        Log.e("Ria", ">>> AppService(android) > onStartCommand : 브랜드 " + Build.BRAND);
        Log.e("Ria", ">>> AppService(android) > onStartCommand : 디바이스 " + Build.DEVICE);
        Log.e("Ria", ">>> AppService(android) > onStartCommand : 제조사 " + Build.MANUFACTURER);
        Log.e("Ria", ">>> AppService(android) > onStartCommand : 버전 " + Build.VERSION.RELEASE);

        try {
            Intent intent = new Intent(this, SQLiteHandler.class);
            intent.putExtra("CATEGORY", "AS(ANDROID)");
            intent.putExtra("TIMESTAMP", new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
            intent.putExtra("PACKAGE", Build.MODEL + ", " + Build.BRAND + ", " + Build.DEVICE + ", " + Build.MANUFACTURER + ", " + Build.VERSION.RELEASE);

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        List<ApplicationInfo> applicationInfoList = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        String applicationType = "";

        for (ApplicationInfo applicationInfo : applicationInfoList) {
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    Log.e("Ria", ">>> AppService(installed) > onStartCommand : 시스템마켓 패키지명 : " + applicationInfo.packageName);
                    applicationType = "systemMarket";
                } else {
                    Log.e("Ria", ">>> AppService(installed) > onStartCommand : 시스템 패키지명 : " + applicationInfo.packageName);
                    applicationType = "system";
                }
            } else {
                Log.e("Ria", ">>> AppService(installed) > onStartCommand : 써드파티 패키지명 : " + applicationInfo.packageName);
                applicationType = "market";
            }

            try {
                Intent intent = new Intent(this, SQLiteHandler.class);
                intent.putExtra("CATEGORY", "AS(INSTALLED)");
                intent.putExtra("TYPE", applicationType);
                intent.putExtra("PACKAGE", applicationInfo.packageName);

                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onServiceConnected() {
        Log.e("Ria", ">>> AppService > onServiceConnected : 앱 서비스에 연결되었습니다.");

        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.DEFAULT;
        accessibilityServiceInfo.notificationTimeout = 150;

        setServiceInfo(accessibilityServiceInfo);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent _event) {
        if (_event != null) {
            String accessibilityTimestamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss.SSS").format(new Date(System.currentTimeMillis()));

            if (_event.isFullScreen() && _event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                String getPackageName = _event.getPackageName().toString();

                if (!$previousPackageName.equals(getPackageName) && !getPackageName.contains("inputmethod")) {
                    Log.e("Ria", ">>> AppService(running) > onAccessibilityEvent : 패키지명 " + getPackageName);

                    $previousPackageName = getPackageName;

                    try {
                        Intent intent = new Intent(this, SQLiteHandler.class);
                        intent.putExtra("CATEGORY", "AS(RUNNING)");
                        intent.putExtra("TIMESTAMP", accessibilityTimestamp);
                        intent.putExtra("PACKAGE", getPackageName);

                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (_event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || _event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                    Log.e("Ria", ">>> AppService(keyboard) > onAccessibilityEvent : 키보드 " + accessibilityTimestamp);

                    try {
                        Intent intent = new Intent(this, SQLiteHandler.class);
                        intent.putExtra("CATEGORY", "AS(KEYBOARD)");
                        intent.putExtra("TIMESTAMP", accessibilityTimestamp);

                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                } else if (_event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    Log.e("Ria", ">>> AppService(shorttouch) > onAccessibilityEvent : 짧은터치 " + accessibilityTimestamp);

                    try {
                        Intent intent = new Intent(this, SQLiteHandler.class);
                        intent.putExtra("CATEGORY", "AS(SHORTTOUCH)");
                        intent.putExtra("TIMESTAMP", accessibilityTimestamp);

                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                } else if (_event.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
                    Log.e("Ria", ">>> AppService(longtouch) > onAccessibilityEvent : 롱터치 " + accessibilityTimestamp);

                    try {
                        Intent intent = new Intent(this, SQLiteHandler.class);
                        intent.putExtra("CATEGORY", "AS(LONGTOUCH)");
                        intent.putExtra("TIMESTAMP", accessibilityTimestamp);

                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                } else if (_event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                    Log.e("Ria", ">>> AppService(scrolltouch) > onAccessibilityEvent : 스크롤터치 " + accessibilityTimestamp);

                    try {
                        Intent intent = new Intent(this, SQLiteHandler.class);
                        intent.putExtra("CATEGORY", "AS(SCROLLTOUCH)");
                        intent.putExtra("TIMESTAMP", accessibilityTimestamp);

                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        throw new RuntimeException(">>> AppService > onDestroy : 앱 서비스가 죽었습니다.");
    }
}
