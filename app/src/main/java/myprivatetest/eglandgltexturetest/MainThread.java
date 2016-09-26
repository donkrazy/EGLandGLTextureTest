package myprivatetest.eglandgltexturetest;

import android.opengl.GLDebugHelper;
import android.os.Handler;

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
public class MainThread extends Thread {
    EGL10 mEGL;
    EGLDisplay mGLDisplay;
    EGLConfig mGLConfig;
    EGLSurface mGLSurface;
    EGLContext mGLContext;
    GL10 mGL;
    MainView mMainView;
    int mWidth;
    int mHeight;
    boolean mState = false;
    boolean mIsDraw = false;
    FloatBuffer vertices;
    final Handler mHandler = new Handler();

    MainThread(MainView view, int width, int height) {
        mMainView = view;
        mWidth = width;
        mHeight = height;
        mState = true;
    }

    public void run() {
        this.init();
        while (mState) {
            mMainView.draw();
        }
    }

    public void init( ) {
        mEGL = (EGL10) GLDebugHelper.wrap(
                EGLContext.getEGL(),
                GLDebugHelper.CONFIG_CHECK_GL_ERROR |
                        GLDebugHelper.CONFIG_CHECK_THREAD,
                null);

        mGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] curGLVersion = new int[2];
        mEGL.eglInitialize(mGLDisplay, curGLVersion);

        int[] mConfigSpec = { EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_NONE };
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        mEGL.eglChooseConfig(mGLDisplay, mConfigSpec,  configs, 1, num_config);
        mGLConfig = configs[0];

        mGLSurface = mEGL.eglCreateWindowSurface(
                mGLDisplay, mGLConfig, mMainView.getHolder(), null);

        mGLContext =  mEGL.eglCreateContext(mGLDisplay, mGLConfig, EGL10.EGL_NO_CONTEXT, null);

        mEGL.eglMakeCurrent(mGLDisplay, mGLSurface, mGLSurface, mGLContext);

        mGL= (GL10) GLDebugHelper.wrap(mGLContext.getGL(),
                GLDebugHelper.CONFIG_CHECK_GL_ERROR | GLDebugHelper.CONFIG_CHECK_THREAD,
                null);

        mMainView.glInit(mEGL, mGLDisplay, mGLConfig, mGLSurface, mGLContext, mGL);
        mIsDraw = true;
    }

    public void requestStop() {
        mState = false;
        try { join(); } catch (InterruptedException e) { }
        mEGL.eglMakeCurrent(mGLDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mEGL.eglDestroySurface(mGLDisplay, mGLSurface);
        mEGL.eglDestroyContext(mGLDisplay, mGLContext);
        mEGL.eglTerminate(mGLDisplay);
    }
}
