package com.example.sev_user.a3dimageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by sev_user on 4/18/2017.
 */

public class MyRenderer extends GLSurfaceView implements GLSurfaceView.Renderer, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    private static final String TAG = "ky.nd";
    Context mContext;

    public static final String ATTR_POSITION = "a_Position";
    public static final String ATTR_TEXTURE_COORDINATE = "a_TextureCoordinate";
    public static final String UNI_SAMPLER = "u_Sampler";
    public static final String UNI_MATRIX = "u_Matrix";

    private int mProgramId;

    public static final int NUM_EDGES_PER_CIRCLE = 100;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mElementBuffer;

    private int mTextureId = -1, mElementBufferId = -1, mVertexBufferId = -1;
    private  int width, height;

    private float mRotationX = 0;
    private float mRotationY = 270;
    private float mRotationZ = 0;

    public static final float VIEW_ANGLE_Y = 100;
    private float mViewAngle = VIEW_ANGLE_Y;
    private float MAX_VIEW_ANGLE = 100;
    private float MIN_VIEW_ANGLE = 80;
    private static final float PADING_ANGLE = 0;
    private static float MAX_X = 90;
    private static float MIN_X = -MAX_X;
    private int mNumIndices;
    private float[] mMatrix = new float[16];

    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private static final float MIN_Z = 0.0f;
    private static final float MAX_Z = 2.0f;
    private float z = MAX_Z;
    private float zFactor = 20 + (z - MIN_Z)*80/(MAX_Z - MIN_Z);
    private float[] mIdentityMatrix = new float[16];
    private float mScreenRadius;
    private boolean mFreeze = false;
    private float VIEW_ANGLE_RATIO = 1.0f;

    private boolean mIsRotated = false;
    private float mVelocityY, mVelocityX;


    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private final float DECCELERATION = 0.025f / 20;

    private long mPreviousUpdate;




    private int mPosLocation, mTexLocation, mMatLocation, mSamplerLocation;

    private float mTransX = 0, mTransY = 0, mScale = 1f;

    public MyRenderer(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        init();
    }


    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(this);
//        setRenderMode(RENDERMODE_WHEN_DIRTY);
        createBuffers();

        mGestureDetector = new GestureDetector(mContext, this);
        mScaleGestureDetector = new ScaleGestureDetector(mContext, this);

        Matrix.setIdentityM(mIdentityMatrix, 0);
    }

    private void createBuffers(){
        float[] mVertices = new float[5 * NUM_EDGES_PER_CIRCLE * NUM_EDGES_PER_CIRCLE];
        int k = 0;
        double padding = PADING_ANGLE * Math.PI/180;
        for(int i = 0; i < NUM_EDGES_PER_CIRCLE; i++) {
            double phi = padding + (Math.PI - 2*padding)*i/(NUM_EDGES_PER_CIRCLE - 1);
            for(int j = 0; j < NUM_EDGES_PER_CIRCLE; j++) {
                double theta = j*2*Math.PI/(NUM_EDGES_PER_CIRCLE - 1);
                mVertices[k++] = (float) (Math.sin(phi)*Math.cos(theta));
                mVertices[k++] = (float) Math.cos(phi);
                mVertices[k++] = (float) -(Math.sin(phi)*Math.sin(theta));
                mVertices[k++] =  1 - ((float)j)/(NUM_EDGES_PER_CIRCLE - 1);
                mVertices[k++] = ((float)i)/(NUM_EDGES_PER_CIRCLE - 1);
                //Log.i(TAG, "u,v , x,y,z     "+mVertices[k - 2]+","+mVertices[k - 1]+" :: "+mVertices[k - 5]+","+mVertices[k - 4]+ ","+mVertices[k - 3]);
            }
        }
        mVertexBuffer = ByteBuffer.allocateDirect(mVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mVertices);
        mVertexBuffer.position(0);
        mNumIndices = (NUM_EDGES_PER_CIRCLE - 1)*(2*NUM_EDGES_PER_CIRCLE);
        short[] indices = new short[mNumIndices];
        k = 0;
        for(int i = 1; i < NUM_EDGES_PER_CIRCLE; i++) {
            for(int j = 0; j <= NUM_EDGES_PER_CIRCLE - 1; j++) {
                indices[k++] = (short) ((i - 1)*NUM_EDGES_PER_CIRCLE + j);
                indices[k++] = (short) (i*NUM_EDGES_PER_CIRCLE + j);

            }
        }
        mElementBuffer = ByteBuffer.allocateDirect(indices.length*2).order(ByteOrder.nativeOrder()).asShortBuffer().put(indices);
        mElementBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

//        GLES20.glClearColor(0.925f, 0.925f, 0.925f, 1);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        mProgramId = getProgram(Utils.readRawTextFile(mContext, R.raw.ver), Utils.readRawTextFile(mContext, R.raw.frag));

        mPosLocation = GLES20.glGetAttribLocation(mProgramId, ATTR_POSITION);
        mMatLocation = GLES20.glGetUniformLocation(mProgramId, UNI_MATRIX);
        mTexLocation = GLES20.glGetAttribLocation(mProgramId, ATTR_TEXTURE_COORDINATE);
        mSamplerLocation = GLES20.glGetUniformLocation(mProgramId, UNI_SAMPLER);

//        if (mTextureId != -1){
//
//        }
        mTextureId = createTexture(mContext, R.drawable.test_image);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;

        calcViewAngle();

        calcScreenRadius();


        loadBuffers();
    }

    private void loadBuffers(){
        int[] tmp = new int[2];
        GLES20.glGenBuffers(2, tmp, 0);
        mElementBufferId = tmp[0];
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mElementBufferId);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mElementBuffer.capacity() * 2, mElementBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        mVertexBufferId = tmp[1];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertexBuffer.capacity() * 4, mVertexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void calcScreenRadius() {
        mViewAngle = z > 1 ? MAX_VIEW_ANGLE : MIN_VIEW_ANGLE + (MAX_VIEW_ANGLE - MIN_VIEW_ANGLE) * (z - MIN_Z) / (1 - MIN_Z);
        if(z > 1) {
            float yy = (float) Math.sqrt((1 - 1 / (z * z)));
            float zz = 1 / z;
            float viewPointZ = z;
            float zPlane = z - n;
            float t = (zPlane - zz) / (viewPointZ - zz);
            float screenY = height * (1 - ((float) ((yy - t * yy) / (n * Math.tan(getViewAngleY() * Math.PI / 360))))) / 2;
            mScreenRadius = Math.abs((screenY - height / 2));
            //Log.i(TAG,"z is "+z);
        }
        else {
            mScreenRadius = Integer.MAX_VALUE;
        }
    }

    private float getViewAngleY() {
        return (height > width) ? mViewAngle : mViewAngle*VIEW_ANGLE_RATIO;

    }

    private void calcViewAngle(){
        float portWidth = Math.min(width, height);
        float portHeight = Math.max(width, height);
        float landWidth = portWidth;
        float landHeight = portHeight;
        float yy = (float) Math.sqrt((1 - 1 / (MAX_Z * MAX_Z)));
        float zz = 1 / MAX_Z;

        float viewPointZ = MAX_Z;
        float zPlane = MAX_Z - n;
        float t = (zPlane - zz) / (viewPointZ - zz);
        float screenY = (portHeight - portWidth)/2;
        MAX_VIEW_ANGLE = (float) (2*Math.atan(1/n*(  ((float) ((yy - t * yy) /(1 - (screenY * 2)/portHeight)))))* 180/Math.PI);
        screenY = 0;
        float maxViewAngleLand = (float) (2*Math.atan(1/n*(  ((float) ((yy - t * yy) /(1 - (screenY * 2)/landHeight)))))* 180/Math.PI);
        VIEW_ANGLE_RATIO = maxViewAngleLand / MAX_VIEW_ANGLE;
        mViewAngle = z > 1 ? MAX_VIEW_ANGLE : ( z>=MIN_Z? MIN_VIEW_ANGLE + (MAX_VIEW_ANGLE - MIN_VIEW_ANGLE) * (z - MIN_Z) / (1 - MIN_Z): MIN_VIEW_ANGLE - (MIN_Z - z)* MIN_VIEW_ANGLE*0.5f);
    }



    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        updateMatrix();

        GLES20.glUseProgram(mProgramId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferId);

        GLES20.glVertexAttribPointer(mPosLocation, 3, GLES20.GL_FLOAT, true, 20, 0);

        GLES20.glVertexAttribPointer(mTexLocation, 2, GLES20.GL_FLOAT, true, 20, 12);

        GLES20.glUniformMatrix4fv(mMatLocation, 1, false, mMatrix, 0);

        GLES20.glEnableVertexAttribArray(mPosLocation);

        GLES20.glEnableVertexAttribArray(mTexLocation);

        GLES20.glUniform1i(mSamplerLocation, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mElementBufferId);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mNumIndices, GLES20.GL_UNSIGNED_SHORT, 0);

        update();

    }


    private static final float f = -50.0f;
    private static final float n = 0.5f;
    private void updateMatrix() {
        //Log.i(TAG, "z: "+z);
        Matrix.setIdentityM(mModelMatrix, 0);
        //Matrix.rotateM(mModelMatrix, 0, mRotationZ, 0, 0, 1);
        Matrix.translateM(mModelMatrix, 0, 0, 0, -z);
        Matrix.rotateM(mModelMatrix, 0, mRotationZ, 0, 0, 1);
        Matrix.rotateM(mModelMatrix, 0, mRotationX, 1, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, mRotationY, 0, 1, 0);
        Matrix.perspectiveM(mProjectionMatrix, 0, getViewAngleY(), (float) width / height, n, f);

        float[] mat1 = new float[16];
        float[] mat2 = new float[16];

        Matrix.setIdentityM(mat1, 0);
        Matrix.scaleM(mat1, 0, mScale, mScale, 1);
        Matrix.translateM(mat1, 0, mTransX * 2 / width, -mTransY * 2 / height, 0);
        Matrix.multiplyMM(mat2, 0, mProjectionMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mat1, 0, mat2, 0);



    }

    private static int getProgram(String vertexShaderSource, String fragmentShaderSource) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        int[] status = new int[1];
        glShaderSource(vertexShader, vertexShaderSource);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(vertexShader);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "vertex shader compilation failed....");
            Log.e(TAG, glGetShaderInfoLog(vertexShader));
            return 0;
        } else {
            Log.i(TAG, "vertex shader compilation successful");
        }
        glCompileShader(fragmentShader);
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "fragment shader compilation failed....");
            Log.e(TAG, glGetShaderInfoLog(fragmentShader));
            return 0;
        } else {
            Log.i(TAG, "fragment shader compilation successful");
        }
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        glGetProgramiv(program, GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "program link error...");
            Log.e(TAG, glGetProgramInfoLog(program));
            return 0;
        } else {
            Log.i(TAG, "program link successful");
        }
        return program;
    }

//    private synchronized void loadImage() {
//        if (mTextureId == -1) {
//            int[] tmp = new int[1];
//            glGenTextures(1, tmp, 0);
//            mTextureId = tmp[0];
//            glActiveTexture(GL_TEXTURE0);
//        }
//        if(mImageData != null) {
//		 /*  Bitmap bmp;
//		   synchronized (this) {
//			   bmp = Bitmap.createBitmap(mImageData, mImageWidth, mImageHeight, Config.RGB_565);
//		   }*/
//            glBindTexture(GL_TEXTURE_2D, mTextureId);
//            ByteBuffer bb = ByteBuffer.allocateDirect(mImageData.length * 4).order(ByteOrder.nativeOrder());
//            IntBuffer ib = bb.asIntBuffer();
//            synchronized (this) {
//                ib.put(mImageData);
//            }
//            ib.position(0);
//            bb.position(0);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//
//            //GLUtils.texImage2D(GL_TEXTURE_2D, 0, bmp, 0);
//            // bmp.recycle();
//            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, mImageWidth, mImageHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, ib);
//            glGenerateMipmap(GL_TEXTURE_2D);
//            glBindTexture(GL_TEXTURE_2D, 0);
//        }
//        mImageLoaded = true;
//    }

    public static int createTexture(Context context, int resource) {
        int[] textures = new int[1];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap tmpImageBitmap = BitmapFactory.decodeResource(context.getResources(), resource, options);

        GLES20.glGenTextures(1, textures, 0);

        if (textures[0] == 0){
            return 0;
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, tmpImageBitmap, 0);

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        tmpImageBitmap.recycle();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return textures[0];
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i(TAG, "MyRenderer: onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i(TAG, "MyRenderer: onFligh");

        if(!mFreeze){
            mVelocityY = mIsRotated?-velocityX: velocityX;
            mVelocityX = mIsRotated?-velocityY: velocityY;
        }
        mPreviousUpdate = System.currentTimeMillis();
        requestRender();
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.i(TAG, "MyRenderer: onScale");
        float factor = detector.getScaleFactor();
        zFactor /= factor;
        if (zFactor < 20){
            zFactor = 20;
        }
        else if (zFactor > 100){
            zFactor = 100;
        }

        z = MIN_Z + (zFactor - 20) * (MAX_Z - MIN_Z) / 80;

        calcScreenRadius();
        requestRender();

        return true;
    }

    private boolean scaling = false;
    private float tmpZ = 0;


    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.i(TAG, "MyRenderer: onScaleBegin");
        scaling = true;
        tmpZ = zFactor;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.i(TAG, "MyRenderer: onScaleEnd");
        scaling = false;
    }


    private float prevX, prevY;
    private boolean mTouchDown = false;
    private long CLICK_DURATION = 150;
    private int CLICK_MAX_DISTANCE = 100;
    private long mTouchDownTime;
    private float mTouchDownX, mTouchDownY;
    private Handler mLongClickHandler = new Handler();
    private boolean mIsLongClicked = false;
    private float mLongTouchX = -1, mLongTouchY = -1;
    private static final int LONG_CLICK_DURATION = 500;        // #54064
    private static final int LONG_TOUCH_RANGE = 30;
    private float mClickX, mClickY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        Log.i(TAG, "MyRenderer::onTouchEvent");

        float xEvent = event.getX();
        float yEvent = event.getY();

        if (!mFreeze){
            float x = mIsRotated ? width - event.getX() : event.getX();
            float y = mIsRotated ? height - event.getY() : event.getY();

            mGestureDetector.onTouchEvent(event);
            mScaleGestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_DOWN){
                prevX = x;
                prevY = y;
                mTouchDown = true;
                mVelocityY = 0;
                mVelocityX = 0;

                mTouchDownTime = System.currentTimeMillis();
                mTouchDownX = x;
                mTouchDownY = y;

            }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float diffY = x - prevX;
                float diffX = y - prevY;
                if (!scaling && Math.abs(diffX) < 250 && Math.abs(diffY) < 250) {
                    //mRotationX -= mViewAngle*diffX/height;

                    mRotationX -= diffX * 0.04f;

                    if (mRotationX > MAX_X) {
                        mRotationX = MAX_X;
                    } else if (mRotationX < MIN_X) {
                        mRotationX = MIN_X;
                    }
                    //mRotationY -= diffY*mViewAngle/(height*Math.cos((mRotationX + (1 - 2*event.getY()/height)*(mViewAngle/height))*Math.PI/180));
                    mRotationY -= diffY * 0.04f;
                    if (mRotationY < 0) {
                        mRotationY += 360;
                    } else if (mRotationY > 360) {
                        mRotationY -= 360;
                    }
                }
                prevX = x;
                prevY = y;

            }else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mTouchDown = false;
            }


            requestRender();
        }

        return  true;

    }



    public void update() {
        if (mVelocityY != 0 || mVelocityX != 0) {

            //mVelocityY *= DECCELERATION;
            //mVelocityX *= DECCELERATION;
            mVelocityX = mVelocityX - (System.currentTimeMillis() - mPreviousUpdate) * DECCELERATION * mVelocityX;
            mVelocityY = mVelocityY - (System.currentTimeMillis() - mPreviousUpdate) * DECCELERATION * mVelocityY;

            mPreviousUpdate = System.currentTimeMillis();
            mRotationX -= mVelocityX * 0.0006;
            mRotationY -= mVelocityY * 0.0006;
            if (Math.abs(mVelocityY) < 500) {
                mVelocityY = 0;
            }

            if (Math.abs(mVelocityX) < 500) {
                mVelocityX = 0;
            }
            if (mRotationX > MAX_X) {
                mRotationX = MAX_X;
            } else if (mRotationX < MIN_X) {
                mRotationX = MIN_X;
            }
            if (mRotationY < 0) {
                mRotationY += 360;
            } else if (mRotationY > 360) {
                mRotationY -= 360;
            }
            requestRender();
        }
    }



}
