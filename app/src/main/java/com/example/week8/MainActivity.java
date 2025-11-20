package com.example.week8;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    EditText txtUrl;
    Button btnDownload;
    ImageView imgView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtUrl = findViewById(R.id.txtURL);
        btnDownload = findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imgView);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
//
//                String filename = "temp_image.jpg";
//                String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
//                        + "/" + filename;
//                downloadFile(txtUrl.getText().toString(), filepath);
//                preview(filepath);

//                DownloadTask downloadTask = new DownloadTask();
//                String[] urls = new String[1];
//                urls[0] = txtUrl.getText().toString();
//                downloadTask.execute(urls);
                  Thread backgroundThread = new Thread(new DownloadRunnable(txtUrl.getText().toString()));
                  backgroundThread.start();
            }
        });
    }

    private void downloadFile(String fileUrl, String filepath) {
        try {
            URL strUrl = new URL(fileUrl);
            URLConnection urlConnection = strUrl.openConnection();
            urlConnection.connect();
            InputStream input = new BufferedInputStream(strUrl.openStream(), 8192);
            OutputStream output = new FileOutputStream(filepath);
            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.close();
            input.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void preview(String filepath) {
        Bitmap image = BitmapFactory.decodeFile(filepath);
        float width = image.getWidth();
        float height = image.getHeight();
        int W = 400;
        int H = (int) (height * W / width);
        Bitmap.createScaledBitmap(image, W, H, false);
        imgView.setImageBitmap(image);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                String filename = "temp_image.jpg";
//                String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
//                        + "/" + filename;
//                downloadFile(txtUrl.getText().toString(), filepath);
//                preview(filepath);
//                DownloadTask downloadTask = new DownloadTask();
//                String[] urls = new String[1];
//                urls[0] = txtUrl.getText().toString();
//                downloadTask.execute(urls);

            Thread backgroundThread = new Thread(new DownloadRunnable(txtUrl.getText().toString()));
            backgroundThread.start();



            }
        }
        else {
            Toast.makeText(this, "External Storage permission not granted ", Toast.LENGTH_SHORT).show();
        }
    }
    public Bitmap scaleBitmap(String filepath) {
        Bitmap image = BitmapFactory.decodeFile(filepath);
        float width = image.getWidth();
        float height = image.getHeight();
        int W = 400;
        int H = (int) (height * W / width);
        return Bitmap.createScaledBitmap(image, W, H, false);
    }

    class DownloadTask extends AsyncTask<String, Integer, Bitmap> {
        ProgressDialog PD;
        @Override
        protected Bitmap doInBackground(String... urls) {
            String fileName = "temp.jpg";
            String imgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            downloadFile(urls[0], imgPath + "/" + fileName);

            return scaleBitmap(imgPath + "/" + fileName);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imgView.setImageBitmap(bitmap);
            PD.dismiss();
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PD = new ProgressDialog(MainActivity.this);
            PD.setMax(100);
            PD.setIndeterminate(false);
            PD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            PD.setTitle("Downloading");
            PD.setMessage("Please wait...");
            PD.show();



        }
    }
    class DownloadRunnable implements Runnable {
        String url;

        public DownloadRunnable(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            String fileName = "temp.jpg";
            String imgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            downloadFile(url, imgPath + "/" + fileName);
            Bitmap bitmap = scaleBitmap(imgPath + "/" + fileName);
            runOnUiThread(new updateBitmap(bitmap));


        }

        class updateBitmap implements Runnable {
            Bitmap bitmap;
            public updateBitmap(Bitmap bitmap){
                this.bitmap = bitmap;
            }
            @Override
            public void run() {
                imgView.setImageBitmap(bitmap);
            }
        }
    }
}