package myprivatetest.eglandgltexturetest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangjael on 7/18/2016.
 */
public class MainView extends SurfaceView implements SurfaceHolder.Callback{
    MainThread mMainThread;
    Context mMainContext;
    SurfaceHolder mHolder;
    FloatBuffer vertexBuffer;
    FloatBuffer vertexBufferTex;
    int mFrameCount;
    Bitmap bitmap1;
    int tex1;
    EGL10 mEGL;
    EGLDisplay mGLDisplay;
    EGLConfig mGLConfig;
    EGLSurface mGLSurface;
    EGLContext mGLContext;
    GL10 mGL;
    boolean mIsDraw = false;
    int mWidth = 0;
    int mHeight = 0;
    public MainView(Context r, AttributeSet a) {
        super (r,a);
        getHolder().addCallback(this);
        setFocusable(true);
        mMainContext = r;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }
    public void init(int screenWidth, int screenHeight) {
        mWidth = screenWidth;
        mHeight = screenHeight;
    }
    public void glInit (EGL10 eGL,
                        EGLDisplay gLDisplay,
                        EGLConfig gLConfig,
                        EGLSurface gLSurface,
                        EGLContext gLContext,
                        GL10 gL) {
        mEGL = eGL;
        mGLDisplay = gLDisplay;
        mGLConfig = gLConfig;
        mGLSurface = gLSurface;
        mGLContext = gLContext;
        mGL = gL;
        mFrameCount = 0;
        mGL.glOrthof(0, mWidth,0, mHeight, 1, -1);
        mGL.glViewport(0, 0, mWidth, mHeight);
        mGL.glClear(GL10.GL_COLOR_BUFFER_BIT);
        mGL.glMatrixMode(GL10.GL_PROJECTION);
        mGL.glLoadIdentity();

        float[] vertexArray ={
                0.0f, 0.0f,
                0.0f, mHeight,
                mWidth, mHeight,
                mWidth, 0.0f,
        };

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexArray.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertexArray);
        vertexBuffer.flip();

        float[] vertexArrayTex = {
                0, 0,
                0, 1,
                1, 1,
                1, 0
        };
        ByteBuffer byteBufferTex = ByteBuffer.allocateDirect(vertexArrayTex.length * 4);
        byteBufferTex.order(ByteOrder.nativeOrder());
        vertexBufferTex = byteBufferTex.asFloatBuffer();
        vertexBufferTex.put(vertexArrayTex);
        vertexBufferTex.flip();

        bitmap1  = decodeSampledBitmapFromResource(this.getResources(), R.drawable.pen, 512, 512);
        int[] textures = new int[1];
        mGL.glGenTextures(1, textures, 0);
        tex1 = textures[0];
        mGL.glBindTexture(GL10.GL_TEXTURE_2D, tex1);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D,0, bitmap1, 0);
        mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);

        mGL.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        bitmap1.recycle();
        mIsDraw = true;
    }

    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height){
    }

    public void surfaceCreated(SurfaceHolder holder){
        mWidth = this.getWidth();
        mHeight = this.getHeight();
        mMainThread = new MainThread(this, mWidth, mHeight);
        mMainThread.start();
        mIsDraw = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        if (mMainThread != null) {
            mMainThread.requestStop();
        }
    }

    public void draw()
    {
        if(mIsDraw == false) {
            return;
        }

        mGL.glClear(GL10.GL_COLOR_BUFFER_BIT);
        mGL.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        vertexBuffer.position(0);
        mGL.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);

        mGL.glEnable(GL10.GL_TEXTURE_2D);
        mGL.glBindTexture(GL10.GL_TEXTURE_2D, tex1);

        mGL.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        mGL.glTexCoordPointer(2, GL10.GL_FLOAT, 0 , vertexBufferTex);

        mGL.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
        mEGL.eglSwapBuffers(mGLDisplay,  mGLSurface);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}
