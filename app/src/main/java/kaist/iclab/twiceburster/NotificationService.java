package kaist.iclab.twiceburster;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends NotificationListenerService {
    private static String $previousNotificationTimestamp = "GodLovesYou";
    private static boolean $notificationReceiverRegistered = false;

    public static TimerTask $trafficTimerTask;
    public static Timer $trafficTimer;

    @Override
    public void onCreate() {
        Log.e("Ria", ">>> NotificationService > onStartCommand : 노티피케이션 서비스를 시작합니다.");
    }

    @Override
    public int onStartCommand(Intent _intent, int _flags, int _startid) {
        if (!$notificationReceiverRegistered) {
            Log.e("Ria", ">>> NotificationService > onStartCommand : 노티피케이션 리시버를 등록합니다.");

            BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context _context, Intent _intent) {
                    String ringerType = "";

                    if (_intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        Log.e("Ria", ">>> notificationReceiver(ringer) > onReceive : 스크린이 켜졌습니다.");
                        ringerType = "screenOn";

                        $trafficTimer = new Timer(true);
                                $trafficTimerTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        Log.e("Ria", ">>> notificationReceiver(data) > run : 송신량 " + (TrafficStats.getTotalTxBytes()) / 1024 + " KB");
                                        Log.e("Ria", ">>> notificationReceiver(data) > run : 수신량 " + (TrafficStats.getTotalRxBytes()) / 1024 + " KB");

                                        try {
                                            Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                                            intent.putExtra("CATEGORY", "NR(DATA)");
                                            intent.putExtra("TIMESTAMP", new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
                                            intent.putExtra("TRANSMIT", "" + (TrafficStats.getTotalTxBytes()) / 1024 + " KB");
                                            intent.putExtra("RECEIVE", "" + (TrafficStats.getTotalRxBytes()) / 1024 + " KB");

                                            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                            pendingIntent.send();
                                        } catch (PendingIntent.CanceledException e) {
                                            e.printStackTrace();
                                        }
                            }
                        };

                        $trafficTimer.schedule($trafficTimerTask, 0, 10 * 1000);
                    } else if (_intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                        Log.e("Ria", ">>> notificationReceiver(ringer) > onReceive : 스크린이 꺼졌습니다.");
                        ringerType = "screenOff";

                        if ($trafficTimer != null) {
                            $trafficTimer.cancel();
                            $trafficTimer = null;
                        }
                    } else if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                        Log.e("Ria", ">>> notificationReceiver(ringer) > onReceive : 무음모드 입니다.");
                        ringerType = "silent";
                    } else if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                        Log.e("Ria", ">>> notificationReceiver(ringer) > onReceive : 진동모드 입니다.");
                        ringerType = "vibrate";
                    } else if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        Log.e("Ria", ">>> notificationReceiver(ringer) > onReceive : 소리모드 입니다.");
                        ringerType = "normal";
                    }

                    try {
                        Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                        intent.putExtra("CATEGORY", "NR(RINGER)");
                        intent.putExtra("TIMESTAMP", new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
                        intent.putExtra("TYPE", ringerType);

                        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);

            registerReceiver(notificationReceiver, intentFilter);
            $notificationReceiverRegistered = true;
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification _statusbarnotification) {
        String notificationTimestamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(_statusbarnotification.getPostTime()));

        if (!$previousNotificationTimestamp.equals(notificationTimestamp)) {
            Notification notificationPost = _statusbarnotification.getNotification();
            String notificationPackage = "" + _statusbarnotification.getPackageName();
            String notificationTitle = "" + notificationPost.extras.getString(Notification.EXTRA_TITLE);
            String notificationText = "" + notificationPost.extras.getString(Notification.EXTRA_TEXT);
            String notificationSubText = "" + notificationPost.extras.getString(Notification.EXTRA_SUB_TEXT);
            String notificationSound = "";
            String notificationVibrate = "" + Arrays.toString(notificationPost.vibrate);

            if (notificationPost.sound != null) {
                notificationSound = notificationPost.sound.toString();
            }

            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 시간 " + notificationTimestamp);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 앱명 " + notificationPackage);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 제목 " + notificationTitle);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 텍스트 " + notificationText);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 서브텍스트 " + notificationSubText);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 소리 값 " + notificationSound);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 진동 값 " + notificationVibrate);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : 기본 값  " + notificationPost.defaults);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : LED ON 기간 " + notificationPost.ledOnMS);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : LED OFF 기간 " + notificationPost.ledOffMS);
            Log.e("Ria", ">>> NotificationService(posted) > onNotificationPosted : LED RGB 값 " + notificationPost.ledARGB);

            $previousNotificationTimestamp = notificationTimestamp;

            try {
                Intent intent = new Intent(this, SQLiteHandler.class);
                intent.putExtra("CATEGORY", "NS(POSTED)");
                intent.putExtra("TIMESTAMP", notificationTimestamp);
                intent.putExtra("PACKAGE", notificationPackage);
                intent.putExtra("TITLE", notificationTitle);
                intent.putExtra("TEXT", notificationText);
                intent.putExtra("SUBTEXT", notificationSubText);
                intent.putExtra("SOUND", notificationSound);
                intent.putExtra("VIBRATE", notificationVibrate);
                intent.putExtra("DEFAULTS", notificationPost.defaults);
                intent.putExtra("LEDONMS", notificationPost.ledOnMS);
                intent.putExtra("LEDOFFNMS", notificationPost.ledOffMS);
                intent.putExtra("LEDARGB", notificationPost.ledARGB);

                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification _statusbarnotification) {
        String notificationTimestamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));
        Notification notificationPost = _statusbarnotification.getNotification();
        String notificationPackage = "" + _statusbarnotification.getPackageName();
        String notificationTitle = "" + notificationPost.extras.getString(Notification.EXTRA_TITLE);
        String notificationText = "" + notificationPost.extras.getString(Notification.EXTRA_TEXT);
        String notificationSubText = "" + notificationPost.extras.getString(Notification.EXTRA_SUB_TEXT);

        Log.e("Ria", ">>> NotificationService(removed) > onNotificationRemoved : 시간 " + notificationTimestamp);
        Log.e("Ria", ">>> NotificationService(removed) > onNotificationRemoved : 앱명 " + notificationPackage);
        Log.e("Ria", ">>> NotificationService(removed) > onNotificationRemoved : 제목 " + notificationTitle);
        Log.e("Ria", ">>> NotificationService(removed) > onNotificationRemoved : 텍스트 " + notificationText);
        Log.e("Ria", ">>> NotificationService(removed) > onNotificationRemoved : 서브텍스트 " + notificationSubText);

        try {
            Intent intent = new Intent(this, SQLiteHandler.class);
            intent.putExtra("CATEGORY", "NS(REMOVED)");
            intent.putExtra("TIMESTAMP", notificationTimestamp);
            intent.putExtra("PACKAGE", notificationPackage);
            intent.putExtra("TITLE", notificationTitle);
            intent.putExtra("TEXT", notificationText);
            intent.putExtra("SUBTEXT", notificationSubText);

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        throw new RuntimeException(">>> NotificationService > onDestroy : 노티피케이션 서비스가 죽었습니다.");
    }
}


