package kaist.iclab.twiceburster;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import static kaist.iclab.twiceburster.MainActivity.INTENT_PERMIT;

public class PermitActivity extends AppCompatActivity {
    private Switch[] switchButton = new Switch[4];
    private boolean[] switchButtonPermit = new boolean[7];
    private int permitCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permit);

        switchButton[0] = (Switch) findViewById(R.id.BUTTON_SWITCH0);
        switchButton[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton _buttonView, boolean _isChecked) {
                if (_isChecked && !(switchButtonPermit[0] && switchButtonPermit[1])) {
                    new TedPermission(PermitActivity.this)
                            .setPermissionListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                }

                                @Override
                                public void onPermissionDenied(ArrayList<String> _deniedPermissions) {
                                }
                            })
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)
                            .check();
                }
            }
        });

        switchButton[1] = (Switch) findViewById(R.id.BUTTON_SWITCH1);
        switchButton[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton _buttonView, boolean _isChecked) {
                if (_isChecked && !(switchButtonPermit[2] && switchButtonPermit[3] && switchButtonPermit[4])) {
                    new TedPermission(PermitActivity.this)
                            .setPermissionListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                }

                                @Override
                                public void onPermissionDenied(ArrayList<String> _deniedPermissions) {
                                }
                            })
                            .setPermissions(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS)
                            .check();
                }
            }
        });

        switchButton[2] = (Switch) findViewById(R.id.BUTTON_SWITCH2);
        switchButton[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton _buttonView, boolean _isChecked) {
                if (_isChecked && !switchButtonPermit[5]) {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                }
            }
        });

        switchButton[3] = (Switch) findViewById(R.id.BUTTON_SWITCH3);
        switchButton[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton _buttonView, boolean _isChecked) {
                if (_isChecked && !switchButtonPermit[6]) {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            }
        });
    }

    private boolean permitCheck() {
        switchButtonPermit[0] = ContextCompat.checkSelfPermission(PermitActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0;
        switchButtonPermit[1] = ContextCompat.checkSelfPermission(PermitActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == 0;
        switchButtonPermit[2] = ContextCompat.checkSelfPermission(PermitActivity.this, Manifest.permission.READ_CALL_LOG) == 0;
        switchButtonPermit[3] = ContextCompat.checkSelfPermission(PermitActivity.this, Manifest.permission.READ_SMS) == 0;
        switchButtonPermit[4] = ContextCompat.checkSelfPermission(PermitActivity.this, Manifest.permission.READ_CONTACTS) == 0;
        switchButtonPermit[5] = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners") != null && Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName());
        switchButtonPermit[6] = accessibilityPermitCheck();

        Log.e("Ria", ">>> PermitActivity > permitCheck : 스토리지 허용 " + switchButtonPermit[0]);
        Log.e("Ria", ">>> PermitActivity > permitCheck : 위치정보 허용 " + switchButtonPermit[1]);
        Log.e("Ria", ">>> PermitActivity > permitCheck : 통화내역 허용 " + switchButtonPermit[2]);
        Log.e("Ria", ">>> PermitActivity > permitCheck : 문자내용 허용 " + switchButtonPermit[3]);
        Log.e("Ria", ">>> PermitActivity > permitCheck : 주소록 허용 " + switchButtonPermit[4]);
        Log.e("Ria", ">>> PermitActivity > permitCheck : 알림액세스 허용 " + switchButtonPermit[5]);
        Log.e("Ria", ">>> PermitActivity > permitCheck : 접근성 허용 " + switchButtonPermit[6]);

        if (switchButtonPermit[0] && switchButtonPermit[1]) {
            switchButton[0].setChecked(true);
            switchButton[0].setEnabled(false);
        } else {
            switchButton[0].setChecked(false);
        }

        if (switchButtonPermit[2] && switchButtonPermit[3] && switchButtonPermit[4]) {
            switchButton[1].setChecked(true);
            switchButton[1].setEnabled(false);
        } else {
            switchButton[1].setChecked(false);
        }

        if (switchButtonPermit[5]) {
            switchButton[2].setChecked(true);
            switchButton[2].setEnabled(false);
        } else {
            switchButton[2].setChecked(false);
        }

        if (switchButtonPermit[6]) {
            switchButton[3].setChecked(true);
            switchButton[3].setEnabled(false);
        } else {
            switchButton[3].setChecked(false);
        }

        return switchButtonPermit[0] && switchButtonPermit[1] && switchButtonPermit[2] && switchButtonPermit[3] && switchButtonPermit[4] && switchButtonPermit[5] && switchButtonPermit[6];
    }

    public boolean accessibilityPermitCheck() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (AccessibilityServiceInfo accessibilityServiceInfo : list) {
            if (accessibilityServiceInfo.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onResume() {
        if (permitCheck()) {
            Intent intent = new Intent();
            intent.putExtra("permitCount", permitCount);
            setResult(INTENT_PERMIT, intent);
            finish();
        } else {
            permitCount++;
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
