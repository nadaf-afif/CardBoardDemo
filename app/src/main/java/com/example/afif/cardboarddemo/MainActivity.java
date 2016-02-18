package com.example.afif.cardboarddemo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;

import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.microedition.khronos.egl.EGLConfig;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;


public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer, DownloadImage.DownloadListener{


    private static final String TAG = "MainActivity";

    private Sphere mSphere;
    private final float[] mCamera = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewProjectionMatrix = new float[16];

    private float CAMERA_Z = 0.5f;
    private float[] mView = new float[16];
    private int[] mResourceId = {R.drawable.photo_sphere_4, R.drawable.photo_sphere_1, R.drawable.photo_sphere_2, R.drawable.photo_sphere_4};
    private int mCurrentPhotoPos = 0;
    private boolean mIsCardboardTriggered;
    private String mUrl = "http://54.169.74.32:9000/public/images/sample_vr_min.jpg";
    private Bitmap mImageBitmap;
    private DownloadImage mDownloadImage;
    CardboardView mCardboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        // Associate a CardboardView.StereoRenderer with cardboardView.
        mCardboardView.setRenderer(this);
        // Associate the cardboardView with this activity.
        setCardboardView(mCardboardView);
        //initImageDownloader();

    }

    private void initImageDownloader() {
        DownloadImage downloadImage = new DownloadImage(MainActivity.this, this);
        downloadImage.execute(mUrl);
    }


    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

        /** Setting the camera in the center **/
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        checkGLError("onReadyToDraw");
    }

    @Override
    public void onDrawEye(Eye eye) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        /** Camera should move based on the user movement **/
        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

        /** setting the view projection matrix **/
        multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mView, 0);
        //  multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mCamera, 0);
        //  multiplyMM(scratch, 0, mViewProjectionMatrix, 0, mRotationMatrix, 0);

        /** Drawing the sphere  and apply the projection to it**/
        mSphere.draw(mViewProjectionMatrix);

        checkGLError("onDrawEye");

        if (mIsCardboardTriggered) {
            mIsCardboardTriggered = false;
            resetTexture();
        }

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        /**Setting the view port to the width and height of the device **/
        glViewport(0,0,width,height);
        /** Setting the projection Matrix for the view **/
        MatrixHelper.perspectiveM(mProjectionMatrix,90,(float)width/ (float)height, 1f,10f);
        Log.i(TAG, "onSurfaceChanged");

    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     * <p/>
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param eglConfig The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(1f, 1f, 0f, 1f);// Dark background so text shows up well.

        /** Creating the Sphere for Rendering images inside the sphere **/
        mSphere = new Sphere(this, 50, 5f);
        //mSphere.loadTexture(this, getPhotoIndex());
        mSphere.loadTexture(this, getImageBitmap(mUrl));
        checkGLError("onSurfaceCreated");

    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bitmap = null;
        URL urlConnection = null;
        try {
            urlConnection = new URL(url);
            bitmap = BitmapFactory.decodeStream(urlConnection.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }


    /**
     * Reload the texture
     */
    private void resetTexture() {
        mSphere.deleteCurrentTexture();
        checkGLError("after deleting texture");
        mSphere.loadTexture(this, getPhotoIndex());
        checkGLError("loading texture");
    }

    private int getPhotoIndex() {
        return mResourceId[mCurrentPhotoPos++ % mResourceId.length];
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onDownloadComplete(Bitmap bitmap) {

        // Associate a CardboardView.StereoRenderer with cardboardView.
        mCardboardView.setRenderer(this);
        // Associate the cardboardView with this activity.
        setCardboardView(mCardboardView);
    }

    @Override
    public void onDownloadFailed() {
        Log.d("MAINACTIVITY", "DOWNLOAD FAILED");
    }
}
