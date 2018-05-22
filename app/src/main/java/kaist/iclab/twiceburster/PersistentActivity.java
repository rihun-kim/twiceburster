package kaist.iclab.twiceburster;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

public class PersistentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Log.e("Ria", ">>> PersistentActivity > onCreate : 켜졌다가 꺼집니다.");

        finish();
    }
}
