package example.ducky.com.cubetexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Ducky-PC on 3/18/2017.
 */

public class MyRenderer implements GLSurfaceView.Renderer {

    private Cube mCube;
    private Context mContext;
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRoationMatrix = new float[16];

    private float mAngleZ, mAngleX, mAngleY;

    public MyRenderer(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mCube = new Cube(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width/ height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, 1, -1, 3, 7);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        float[] scratch = new float[16];

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -7, 0f, 0f, 0f, 0f, 1.0f, 0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        Matrix.setRotateM(mRoationMatrix, 0, mAngleZ, 1.0f, 0, 0.0f);


        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRoationMatrix, 0);

        mCube.draw(scratch);
    }

    public static int loadTexture(final Context context, final  int resID){
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;	// No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resID, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public float getAngleZ() {
        return mAngleZ;
    }

    public void setAngleZ(float mAngleZ) {
        this.mAngleZ = mAngleZ;
    }

    public float getAngleX() {
        return mAngleX;
    }

    public void setAngleX(float mAngleX) {
        this.mAngleX = mAngleX;
    }

    public float getAngleY() {
        return mAngleY;
    }

    public void setAngleY(float mAngleY) {
        this.mAngleY = mAngleY;
    }
}
