package kaist.iclab.twiceburster;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class UploadHandler extends IntentService {
    final String clientUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TWICEBURSTER";
    final String serverUrl = "http://143.248.92.61/Hatchery/SpawningPool/zergring.php";

    public UploadHandler() {
        super("UploadHandler");
    }

    @Override
    protected void onHandleIntent(Intent _intent) {
        Log.e("Ria", ">>> UploadHandler > onHandleIntent : 클라이언트 UID " + getUID());
        String boundary = "******", line = "\r\n", delimiter = line + "--" + boundary + line;

        File[] files = new File(clientUrl).listFiles();
        for (File file : files) {
            if (file.getName().contains("Log_")) {
                Log.e("Ria", ">>> UploadHandler > onHandleIntent : 클라이언트 파일 " + file.getName());

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(delimiter);
                stringBuilder.append(setValue("UID", getUID()));
                stringBuilder.append(delimiter);
                stringBuilder.append(setValue("FILE", file.getName()));
                stringBuilder.append(line);

                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(serverUrl).openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(httpURLConnection.getOutputStream()));
                    dataOutputStream.writeUTF(stringBuilder.toString());

                    FileInputStream fileInputStream = new FileInputStream(clientUrl + "/" + file.getName());
                    int bufferSize = Math.min(fileInputStream.available(), 1024);
                    byte[] buffer = new byte[bufferSize];
                    int byteRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (byteRead > 0) {
                        dataOutputStream.write(buffer);
                        bufferSize = Math.min(fileInputStream.available(), 1024);
                        byteRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dataOutputStream.writeBytes(delimiter);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    fileInputStream.close();

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.e("Ria", ">>> UploadHandler > onHandleIntent : 클라이언트 파일 전송완료");
                        file.delete();
                    } else {
                        Log.e("Ria", ">>> UploadHandler > onHandleIntent : 클라이언트 파일 전송실패");
                    }

                    httpURLConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String setValue(String _key, String _value) {
        if (_key.equals("UID")) {
            return "Content-Disposition: form-data; name=\"" + _key + "\"r\n\r\n" + _value;
        } else {
            return "Content-Disposition: form-data; name=\"" + _key + "\"; filename=\"" + _value + "\"\r\n";
        }
    }

    public static String getUID() {
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
