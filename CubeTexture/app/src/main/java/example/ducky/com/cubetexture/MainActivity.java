package example.ducky.com.cubetexture;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
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
        myRenderer = new MyRenderer(this);
        glSurfaceView.setRenderer(myRenderer);
        glLayout.addView(glSurfaceView);

    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > glSurfaceView.getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < glSurfaceView.getWidth() / 2) {
                    dy = dy * -1 ;
                }

                myRenderer.setAngleZ(
                        myRenderer.getAngleZ() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                glSurfaceView.requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
