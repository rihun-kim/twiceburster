package kaist.iclab.twiceburster;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HardwareService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient $googleApiClient;
    private static int $previousBatteryLevel = 0;

    public static WifiManager $wifiManager;
    public static boolean $wifiManagerScan = false;
    public static boolean $previousWifiState = false;
    public static boolean $hardwareReceiverRegistered = false;
    public static long $locationRequestInterval = 30000;

    @Override
    public void onCreate() {
        Log.e("Ria", ">>> HardwareService > onCreate : 하드웨어 서비스를 시작합니다.");

        $googleApiClient = new GoogleApiClient.Builder(this).addApi(ActivityRecognition.API).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        $googleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent _intent, int _flags, int _startId) {
        if (!$hardwareReceiverRegistered) {
            Log.e("Ria", ">>> HardwareService > onStartCommand : 하드웨어 리시버를 등록합니다.");

            $wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            BroadcastReceiver hardwareReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context _context, Intent _intent) {
                    String hardwareReceiverTimestamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));

                    if (_intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                        int batteryLevel = _intent.getIntExtra("level", 0) * 100 / _intent.getIntExtra("scale", 100);

                        if ($previousBatteryLevel != batteryLevel) {
                            Log.e("Ria", ">>> hardwareReceiver(battery) > onReceive : 시간 " + hardwareReceiverTimestamp);
                            Log.e("Ria", ">>> hardwareReceiver(battery) > onReceive : 전압 " + _intent.getIntExtra("voltage", 0) + "mV");
                            Log.e("Ria", ">>> hardwareReceiver(battery) > onReceive : 잔존 " + _intent.getIntExtra("level", 0) * 100 / _intent.getIntExtra("scale", 100) + "%");
                            Log.e("Ria", ">>> hardwareReceiver(battery) > onReceive : 연결 " + _intent.getIntExtra("plugged", 0));
                            Log.e("Ria", ">>> hardwareReceiver(battery) > onReceive : 상태 " + _intent.getIntExtra("status", 1));

                            try {
                                Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                                intent.putExtra("CATEGORY", "HR(BATTERY)");
                                intent.putExtra("TIMESTAMP", hardwareReceiverTimestamp);
                                intent.putExtra("VOLTAGE", "" + _intent.getIntExtra("voltage", 0) + "mV");
                                intent.putExtra("LEVEL", "" + _intent.getIntExtra("level", 0) * 100 / _intent.getIntExtra("scale", 100) + "%");
                                intent.putExtra("PLUGGED", _intent.getIntExtra("plugged", 0));
                                intent.putExtra("STATUS", _intent.getIntExtra("status", 1));

                                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }

                            $previousBatteryLevel = batteryLevel;
                        }
                    } else if ($wifiManagerScan) {
                        Log.e("Ria", ">>> hardwareReceiver(wifi1) > onReceive : SSID " + $wifiManager.getConnectionInfo().getSSID());
                        Log.e("Ria", ">>> hardwareReceiver(wifi1) > onReceive : BSSID " + $wifiManager.getConnectionInfo().getBSSID());
                        Log.e("Ria", ">>> hardwareReceiver(wifi1) > onReceive : RSSI " + $wifiManager.getConnectionInfo().getRssi());
                        Log.e("Ria", ">>> hardwareReceiver(wifi1) > onReceive : IP " + $wifiManager.getDhcpInfo().gateway);

                        try {
                            Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                            intent.putExtra("CATEGORY", "HR(WIFI1)");
                            intent.putExtra("TIMESTAMP", hardwareReceiverTimestamp);
                            intent.putExtra("SSID", $wifiManager.getConnectionInfo().getSSID());
                            intent.putExtra("BSSID", $wifiManager.getConnectionInfo().getBSSID());
                            intent.putExtra("RSSI", $wifiManager.getConnectionInfo().getRssi());
                            intent.putExtra("IP", $wifiManager.getDhcpInfo().gateway);

                            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }

                        for (ScanResult scanResult : $wifiManager.getScanResults()) {
                            Log.e("Ria", ">>> hardwareReceiver(wifi2) > onReceive : SSID " + scanResult.SSID);
                            Log.e("Ria", ">>> hardwareReceiver(wifi2) > onReceive : BSSID " + scanResult.BSSID);
                            Log.e("Ria", ">>> hardwareReceiver(wifi2) > onReceive : RSSI " + scanResult.level);

                            try {
                                Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                                intent.putExtra("CATEGORY", "HR(WIFI2)");
                                intent.putExtra("TIMESTAMP", hardwareReceiverTimestamp);
                                intent.putExtra("SSID", scanResult.SSID);
                                intent.putExtra("BSSID", scanResult.BSSID);
                                intent.putExtra("RSSI", scanResult.level);

                                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }

                        CellLocation cellLocation = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getCellLocation();

                        if (cellLocation instanceof CdmaCellLocation) {
                            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;

                            Log.e("Ria", ">>> hardwareReceiver(cell1) > onReceive : NID " + cdmaCellLocation.getNetworkId());
                            Log.e("Ria", ">>> hardwareReceiver(cell1) > onReceive : LAT " + cdmaCellLocation.getBaseStationLatitude());
                            Log.e("Ria", ">>> hardwareReceiver(cell1) > onReceive : LON " + cdmaCellLocation.getBaseStationLongitude());

                            try {
                                Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                                intent.putExtra("CATEGORY", "HR(CELL1)");
                                intent.putExtra("TIMESTAMP", hardwareReceiverTimestamp);
                                intent.putExtra("NID", "" + cdmaCellLocation.getNetworkId());
                                intent.putExtra("LAT", cdmaCellLocation.getBaseStationLatitude());
                                intent.putExtra("LON", cdmaCellLocation.getBaseStationLongitude());

                                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        } else if (cellLocation instanceof GsmCellLocation) {
                            GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;

                            Log.e("Ria", ">>> hardwareReceiver(cell2) > onReceive : CID " + gsmCellLocation.getCid());
                            Log.e("Ria", ">>> hardwareReceiver(cell2) > onReceive : LAC " + gsmCellLocation.getLac());

                            try {
                                Intent intent = new Intent(getApplicationContext(), SQLiteHandler.class);
                                intent.putExtra("CATEGORY", "HR(CELL2)");
                                intent.putExtra("TIMESTAMP", hardwareReceiverTimestamp);
                                intent.putExtra("CID", gsmCellLocation.getCid());
                                intent.putExtra("LAC", gsmCellLocation.getLac());

                                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }

                        if(!$previousWifiState) {
                            $wifiManager.setWifiEnabled(false);
                        }

                        $wifiManagerScan = false;
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(hardwareReceiver, intentFilter);

            $hardwareReceiverRegistered = true;
        }

        Log.e("Ria", ">>> HardwareService > onStartCommand : 구글API 서비스에 끊었다가 연결합니다.");

        $googleApiClient.disconnect();
        $googleApiClient.connect();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("Ria", ">>> HardwareService > onConnected : 구글API 서비스에 연결되었습니다.");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, HardwareHandler.class), PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates($googleApiClient, 15000, pendingIntent);
        Log.e("Ria", ">>> HardwareService > onConnected : 하드웨어 핸들러에 16000 인터벌로 액티비티 인텐트를 보냈습니다.");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval($locationRequestInterval);
        LocationServices.FusedLocationApi.requestLocationUpdates($googleApiClient, locationRequest, pendingIntent);
        Log.e("Ria", ">>> HardwareService > onConnected : 하드웨어 핸들러에 " + $locationRequestInterval + " 인터벌로 위치 인텐트를 보냈습니다.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {
        $googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        $googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        throw new RuntimeException(">>> HardwareService > onDestroy : 하드웨어 서비스가 죽었습니다.");
    }
}
