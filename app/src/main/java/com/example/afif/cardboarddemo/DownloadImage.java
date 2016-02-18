package com.example.afif.cardboarddemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by afif on 14/2/16.
 */
public class DownloadImage extends AsyncTask<String, String, Bitmap> {

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private DownloadListener mDownloadListener;

    public DownloadImage(Context context, DownloadListener downloadListener) {
        this.mContext = context;
        mDownloadListener = downloadListener;
    }

    public interface DownloadListener{
        void onDownloadComplete(Bitmap bitmap);
        void onDownloadFailed();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String url = params[0];
        Bitmap bitmap = null;
        try {
            URL urlConnection = new URL(url);
            bitmap = BitmapFactory.decodeStream(urlConnection.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (bitmap !=null){
            mDownloadListener.onDownloadComplete(bitmap);
        }else {
            mDownloadListener.onDownloadFailed();
        }
    }

}
