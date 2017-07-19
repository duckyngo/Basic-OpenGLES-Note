package com.example.sev_user.a3dimageviewer;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    FrameLayout frameLayout;

    MyRenderer mMyRenderer;

    GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.glView);

//        mGlSurfaceView = new GLSurfaceView(this);
//
//        mGlSurfaceView.setEGLContextClientVersion(2);

        mMyRenderer = new MyRenderer(this);

//        mGlSurfaceView.setRenderer(mMyRenderer);

        frameLayout.addView(mMyRenderer);

    }

    public GLSurfaceView getGlSurfaceView() {
        return mGlSurfaceView;
    }
}
