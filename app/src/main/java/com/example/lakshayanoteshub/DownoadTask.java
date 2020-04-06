package com.example.lakshayanoteshub;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownoadTask {
    private Context context;
    String downloadUrl;
    String fileName;
    ProgressDialog progressDialog;
    Handler handler;

    public DownoadTask(Context context, String downloadUrl, String fileName, String subject, Handler handler) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName + subject + ".pdf";
        this.handler = handler;

        startDownload();
    }


    public void startDownload() {
        File pdfFile = new File(context.getExternalFilesDir(null) + "/LakshayaNotesHuB/" + fileName);
        if (pdfFile.exists()) {
            Uri path = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                context.startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (new Checker().isInternetAvailable()) {
                DownloadingTask task = new DownloadingTask();
                task.execute(downloadUrl);
            } else {
                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_LONG).show();
            }
        }

    }

    public class DownloadingTask extends AsyncTask<String, String, String> {

        File apkStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Download Progress");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setMessage("Downloading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected String doInBackground(String... args) {
            try {

                URL url = new URL(downloadUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                int length = httpURLConnection.getContentLength();

                if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DownloadTask", "Server returned HTTP " + httpURLConnection.getResponseCode()
                            + " " + httpURLConnection.getResponseMessage());

                }

                if (new checkForSDCard().isSdCardPresent()) {
                    apkStorage = new File(context.getExternalFilesDir(null) + "/" + "LakshayaNotesHuB");
                } else {
                    Toast.makeText(context, "Oops! There is no  SD Card", Toast.LENGTH_SHORT).show();
                }

                if (!apkStorage.exists()) {
                    apkStorage.mkdir();
                    Log.e("DownloadTask", "Directory Created.");
                }

                outputFile = new File(apkStorage, fileName);

                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e("DownnloadTask", "File Created");
                }

                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = httpURLConnection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    total += len1;
                    publishProgress((int) ((total * 100) / length) + "");
                    fos.write(buffer, 0, len1);
                }

                fos.close();
                is.close();


            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            try {
                if (outputFile != null) {
                    progressDialog.dismiss();
//                    ContextThemeWrapper ctw = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Dialog_Alert);
//                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
//                    alertDialogBuilder.setTitle("Document  ");
//                    alertDialogBuilder.setMessage("Document Downloaded Successfully ");
//                    alertDialogBuilder.setCancelable(false);
//                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    alertDialogBuilder.setNegativeButton("Open", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            File pdfFile = new File(Environment.getExternalStorageDirectory() + "/NotesPlay/" + fileName);
//                            Uri path = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
//                            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
//                            pdfIntent.setDataAndType(path ,"application/pdf");
//                            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                            try {
//                                context.startActivity(pdfIntent);
//                            } catch (ActivityNotFoundException e) {
//                                Toast.makeText(context, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                    alertDialogBuilder.show();

                    File pdfFile = new File(context.getExternalFilesDir(null) + "/LakshayaNotesHuB/" + fileName);
                    Uri path = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", pdfFile);
                    Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                    pdfIntent.setDataAndType(path, "application/pdf");
                    pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        context.startActivity(pdfIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(context, "Note Downloaded Successfully", Toast.LENGTH_SHORT).show();

                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    handler.sendMessage(msg);

                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 3000);

                    Log.e("DownloadTask", "Download Failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 3000);
                Log.e("DownloadTask", "Download Failed with Exception - " + e.getLocalizedMessage());
            }

            super.onPostExecute(aVoid);
        }
    }

    class checkForSDCard {
        public boolean isSdCardPresent() {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return true;
            } else {
                return false;
            }
        }
    }


}
