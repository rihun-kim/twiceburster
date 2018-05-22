package kaist.iclab.twiceburster;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationResult;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static kaist.iclab.twiceburster.HardwareService.$locationRequestInterval;
import static kaist.iclab.twiceburster.HardwareService.$wifiManager;
import static kaist.iclab.twiceburster.HardwareService.$wifiManagerScan;
import static kaist.iclab.twiceburster.HardwareService.$previousWifiState;

public class HardwareHandler extends IntentService {
    private static int[][] $confidences = new int[4][7];
    private static int $previousMaxIndex;
    private static int $autoShortInterval1 = 0;
    private static int $autoShortInterval2 = 0;
    private static int $autoLongInterval = 0;

    public HardwareHandler() {
        super("HardwareHandler");
    }

    @Override
    protected void onHandleIntent(Intent _intent) {
        if (ActivityRecognitionResult.hasResult(_intent)) {
            ActivityRecognitionResult activityRecognitionResult = ActivityRecognitionResult.extractResult(_intent);
            handleDetectedActivities(activityRecognitionResult.getProbableActivities());

            try {
                Intent intent = new Intent(this, SQLiteHandler.class);
                intent.putExtra("CATEGORY", "HH(ACTIVITY)");
                intent.putExtra("TIMESTAMP", new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
                intent.putExtra("VEHICLE", $confidences[0][0]);
                intent.putExtra("BICYCLE", $confidences[0][1]);
                intent.putExtra("FOOT", $confidences[0][2]);
                intent.putExtra("RUNNING", $confidences[0][3]);
                intent.putExtra("STILL", $confidences[0][4]);
                intent.putExtra("WALKING", $confidences[0][5]);
                intent.putExtra("UNKNOWN", $confidences[0][6]);

                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            adaptiveSampling();


        } else if (LocationResult.hasResult(_intent)) {
            LocationResult locationResult = LocationResult.extractResult(_intent);

            Log.e("Ria", ">>> HardwareHandler(location) > onHandleIntent : GPS값 " + locationResult.getLastLocation().getLatitude() + ", " + locationResult.getLastLocation().getLongitude());

            try {
                Intent intent = new Intent(this, SQLiteHandler.class);
                intent.putExtra("CATEGORY", "HH(LOCATION)");
                intent.putExtra("TIMESTAMP", new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
                intent.putExtra("LATITUDE", locationResult.getLastLocation().getLatitude());
                intent.putExtra("LONGITUDE", locationResult.getLastLocation().getLongitude());

                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        if ($autoShortInterval1 > 999) {
            Log.e("Ria", ">>> HardwareHandler > onHandleIntent : 자동으로 지속 액티비티를 호출합니다.");
            Log.e("Ria", ">>> HardwareHandler > onHandleIntent : 자동으로 파일을 저장합니다.");

            try {
                Intent intent = new Intent(this, PersistentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);

                intent = new Intent(this, SQLiteHandler.class);
                intent.putExtra("CATEGORY", "AUTOSAVE");

                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            $autoShortInterval1 = 0;
        }

        if ($autoShortInterval2 > 1199) {
            try {
                Intent intent = new Intent(this, UploadHandler.class);

                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            $autoShortInterval2 = 0;
        }

        if ($autoLongInterval > 3999) {
            Log.e("Ria", ">>> HardwareHandler > onHandleIntent : 자동으로 노티피케이션 서비스를 켭니다.");
            Log.e("Ria", ">>> HardwareHandler > onHandleIntent : 자동으로 앱 서비스를 켭니다.");
            Log.e("Ria", ">>> HardwareHandler > onHandleIntent : 자동으로 로그 핸들러를 호출합니다.");

            try {
                PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotificationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();

                pendingIntent = PendingIntent.getService(this, 0, new Intent(this, AppService.class), PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();

                pendingIntent = PendingIntent.getService(this, 0, new Intent(this, LogHandler.class), PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

            $autoLongInterval = 0;
        }

        $autoShortInterval1++;
        $autoShortInterval2++;
        $autoLongInterval++;
    }

    private void adaptiveSampling() {
        Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : 어댑티브 샘플링을 시작합니다.");

        int maxValue, maxIndex;

        Arrays.fill($confidences[3], 0);
        System.arraycopy($confidences[1], 0, $confidences[2], 0, 7);
        System.arraycopy($confidences[0], 0, $confidences[1], 0, 7);

        for (int i = 1; i < 3; i++) {
            maxValue = $confidences[i][0];
            maxIndex = 0;
            for (int j = 1; j < 7; j++) {
                if ((j != 2) && (maxValue < $confidences[i][j])) {
                    maxValue = $confidences[i][j];
                    maxIndex = j;
                }
            }
            if (maxValue != 0) {
                $confidences[3][maxIndex] += 1;
            }
            Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : " + i + " 행의 인덱스값은 " + maxIndex + " 이고, 맥스값은 " + maxValue + " 입니다.");
        }

        maxValue = $confidences[3][0];
        maxIndex = 0;
        for (int j = 1; j <= 6; j++) {
            if (maxValue < $confidences[3][j]) {
                maxValue = $confidences[3][j];
                maxIndex = j;
            }
        }

        Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : 최종행의 인덱스값은 " + maxIndex + " 이고, 맥스값은 " + maxValue + " 입니다.");

        if ($confidences[3][maxIndex] < 2) {
            Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : 맥스값이 충족되지 않은 상황입니다.");
        } else if ($previousMaxIndex != maxIndex) {
            if (maxIndex == 0) {
                $locationRequestInterval = 1000 * 5;
            } else if (maxIndex == 1) {
                $locationRequestInterval = 1000 * 5;
            } else if (maxIndex == 2) {
                $locationRequestInterval = 1000 * 5;
            } else if (maxIndex == 3) {
                $locationRequestInterval = 1000 * 5;
            } else if (maxIndex == 4) {
                $locationRequestInterval = 1000 * 300;

                Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : 자동으로 하드웨어 리시버를 호출합니다. ");

                if (!$wifiManager.isWifiEnabled()) {
                    $wifiManager.setWifiEnabled(true);

                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            $previousWifiState = false;
                            $wifiManagerScan = true;
                            $wifiManager.startScan();
                        }
                    }, 10 * 1000);
                } else {
                    $previousWifiState = true;
                    $wifiManagerScan = true;
                    $wifiManager.startScan();
                }
            } else if (maxIndex == 5) {
                $locationRequestInterval = 1000 * 5;
            } else {
                $locationRequestInterval = 1000 * 300;
            }

            Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : 인덱스값 " + maxIndex + " 로 하드웨어 서비스로 인텐트를 보냅니다.");
            startService(new Intent(getApplicationContext(), HardwareService.class));

            $previousMaxIndex = maxIndex;
        } else {
            Log.e("Ria", ">>> HardwareHandler > adaptiveSampling : 변화없이 유지되고 있습니다.");
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> _probableActivities) {
        for (DetectedActivity detectedActivity : _probableActivities) {
            switch (detectedActivity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Vehicle " + detectedActivity.getConfidence());
                    $confidences[0][0] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Bicycle " + detectedActivity.getConfidence());
                    $confidences[0][1] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Foot " + detectedActivity.getConfidence());
                    $confidences[0][2] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Running " + detectedActivity.getConfidence());
                    $confidences[0][3] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Still " + detectedActivity.getConfidence());
                    $confidences[0][4] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Walking " + detectedActivity.getConfidence());
                    $confidences[0][5] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e("Ria", ">>> HardwareHandler(activity) > handleDetectedActivities : Unknown " + detectedActivity.getConfidence());
                    $confidences[0][6] = detectedActivity.getConfidence();
                    break;
                }
                case DetectedActivity.TILTING: {
                    break;
                }
            }
        }
    }
}
