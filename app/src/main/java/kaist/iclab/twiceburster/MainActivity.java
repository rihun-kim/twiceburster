package kaist.iclab.twiceburster;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static kaist.iclab.twiceburster.SQLite.$helper;

public class MainActivity extends AppCompatActivity {
    static final int INTENT_PERMIT = 625;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, PermitActivity.class);
        startActivityForResult(intent, INTENT_PERMIT);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.activitybar_main);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        TextView titleText = (TextView) findViewById(R.id.TEXT_TITLE);
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, UploadHandler.getUID(), Toast.LENGTH_LONG).show();
            }
        });
        titleText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                $helper = new SQLite(MainActivity.this);
                $helper.SQLiteExport();
                $helper.close();

                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
        if (_requestCode == INTENT_PERMIT) {
            if (_data.getExtras().getInt("permitCount") < 1) {
                Toast.makeText(MainActivity.this, "센싱을 유지 중입니다.", Toast.LENGTH_SHORT).show();
            } else if (_data.getExtras().getInt("permitCount") < 3) {
                Toast.makeText(MainActivity.this, "껐던 센싱을 시작합니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "처음으로 센싱을 시작합니다.", Toast.LENGTH_SHORT).show();
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        Log.e("Ria", ">>> MainActivity > onCreate : NotificationService 켭니다.");
                        startService(new Intent(MainActivity.this, NotificationService.class));

                        Log.e("Ria", ">>> MainActivity > onCreate : AppService 켭니다.");
                        startService(new Intent(MainActivity.this, AppService.class));

                        Log.e("Ria", ">>> MainActivity > onCreate : HardwareService 켭니다.");
                        startService(new Intent(MainActivity.this, HardwareService.class));

                        Log.e("Ria", ">>> MainActivity > onCreate : LogHandler 호출합니다.");
                        try {
                            PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, new Intent(MainActivity.this, LogHandler.class), PendingIntent.FLAG_UPDATE_CURRENT);
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }, 5 * 1000);

                new Timer().schedule(new TimerTask() {
                    public void run() {
                        $helper = new SQLite(MainActivity.this);
                        $helper.SQLiteExport();
                        $helper.close();
                    }
                }, 30 * 1000);
            }
        }
    }

    private boolean isDebuggable(Context _context) {
        boolean debuggable = false;

        PackageManager packageManager = _context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(_context.getPackageName(), 0);
            debuggable = (0 != (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {

        }

        return debuggable;
    }
}
