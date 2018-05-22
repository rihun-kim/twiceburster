package kaist.iclab.twiceburster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context _context, Intent _intent) {
        if (_intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("Ria", ">>> BootReceiver > onReceive : 꺼졌던 스마트폰이 켜져서 서비스를 다시 실행시킵니다.");

            _context.startService(new Intent(_context, NotificationService.class));
            _context.startService(new Intent(_context, AppService.class));
            _context.startService(new Intent(_context, HardwareService.class));
        }
    }
}

