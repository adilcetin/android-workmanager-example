package tr.com.adil.wmexample;

import android.Manifest;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    private WorkManager workManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        workManager = WorkManager.getInstance(this);
        Spinner spinner = findViewById(R.id.spinner);

        // Permission for saving image
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        findViewById(R.id.button1).setOnClickListener(v -> {
            String [] urls = getResources().getStringArray(R.array.image_urls);
            int position = spinner.getSelectedItemPosition();
            startDownloadWorker(urls [position]);
        });
    }

    public void startDownloadWorker(String url) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                .addTag(DownloadWorker.class.getCanonicalName())
                .setInputData(new Data.Builder()
                        .putString("url", url)
                        .build())
                .build();

        workManager.enqueueUniqueWork(DownloadWorker.class.getCanonicalName(), ExistingWorkPolicy.REPLACE, request);

        workManager.getWorkInfoByIdLiveData(request.getId()).observe(this, workInfo -> {
            if (workInfo != null) {
                TextView textView = findViewById(R.id.textView);
                textView.setText("Work Manager State: " + workInfo.getState().toString());

                switch (workInfo.getState()) {
                    case FAILED:
                        textView.setTextColor(getColor(R.color.red));
                        break;
                    case SUCCEEDED:
                        textView.setTextColor(getColor(R.color.green));
                        try {
                            String resultFilePath = workInfo.getOutputData().getString("result");

                            ImageView imageView = findViewById(R.id.image);
                            imageView.setImageBitmap(BitmapFactory.decodeFile(resultFilePath));

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case RUNNING:
                        textView.setTextColor(getColor(R.color.orange));
                        break;
                    default:
                        textView.setTextColor(getColor(R.color.black));
                        break;
                }
            }
        });
    }
}