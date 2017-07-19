package example.ducky.com.cubetexture;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements View.OnClickListener {

    FrameLayout glLayout;
    GLSurfaceView glSurfaceView;
    MyRenderer myRenderer;
    private ToggleButton mXButton, mYButton, mZButton;

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

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:

                        float dx = x - mPreviousX;
                        float dy = y - mPreviousY;

                        // reverse direction of rotation above the mid-line
                        if (y > glSurfaceView.getHeight() / 2) {
                            dx = dx * -1;
                        }

                        // reverse direction of rotation to left of the mid-line
                        if (x < glSurfaceView.getWidth() / 2) {
                            dy = dy * -1;
                        }
                        myRenderer.setAngle(
                                myRenderer.getAngle() +
                                        ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320

                        glSurfaceView.requestRender();
                }

                mPreviousX = x;
                mPreviousY = y;
                return true;
            }
        });

        mXButton = (ToggleButton) findViewById(R.id.Xaxis);
        mYButton = (ToggleButton) findViewById(R.id.Yaxis);
        mZButton = (ToggleButton) findViewById(R.id.Zaxis);

    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Xaxis:
                if (mXButton.isChecked()) {
                    float temp[] =  {1.0f, 0.0f, 0.0f};
                    myRenderer.setAngle(0);
                    myRenderer.setmRotateCoordinate(temp);

                    if (mYButton.isChecked())
                        mYButton.setChecked(false);

                    if (mZButton.isChecked())
                        mZButton.setChecked(false);
                }
                break;

            case R.id.Yaxis:


                if (mYButton.isChecked()) {
                    float temp[] =  {0.0f, 1.0f, 0.0f};
                    myRenderer.setAngle(0);
                    myRenderer.setmRotateCoordinate(temp);

                    if (mXButton.isChecked())
                        mXButton.setChecked(false);

                    if (mZButton.isChecked())
                        mZButton.setChecked(false);
                }
                break;

            case R.id.Zaxis:


                if (mZButton.isChecked()) {
                    float temp[] =  {0.0f, 0.0f, 1.0f};
                    myRenderer.setAngle(0);
                    myRenderer.setmRotateCoordinate(temp);

                    if (mYButton.isChecked())
                        mYButton.setChecked(false);

                    if (mXButton.isChecked())
                        mXButton.setChecked(false);
                }
                break;
        }
    }
}
