package tr.com.adil.wmexample;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class DownloadWorker extends Worker {

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
       String res = download();
        if (res != null) {
            Data output = new Data.Builder().putString("result", res).build();
            return Result.success(output);
        }
        return Result.failure();
    }

    private String download() {
        try {
            String fileURL = getInputData().getString("url");
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream is = connection.getInputStream();

            File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            String filePath = mediaStorageDir.getPath() + "/" + UUID.randomUUID() + ".png";

            try (OutputStream output = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = is.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }

                output.flush();
            }

            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
