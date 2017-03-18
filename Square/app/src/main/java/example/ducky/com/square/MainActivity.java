package example.ducky.com.square;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    FrameLayout glLayout;
    GLSurfaceView glSurfaceView;
    MyRenderer myRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glLayout = (FrameLayout) findViewById(R.id.gl_layout);
        myRenderer = new MyRenderer();
        glSurfaceView.setRenderer(myRenderer);
        glLayout.addView(glSurfaceView);

    }
}
