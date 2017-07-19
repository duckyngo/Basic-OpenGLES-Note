package example.ducky.com.cube;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Ducky-PC on 3/18/2017.
 */

public class Cube {

    String A_POSITON = "vPosition";
    String U_MATRIX = "uMVPMatrix";
    String U_COLOR = "vColor";

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main(){" +
                    "   gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main(){" +
                    "   gl_FragColor = vColor;" +
                    "}";


    private final FloatBuffer mCubeVertexBuffer;
    private final ShortBuffer mIndiceBuffer;
    private final int mProgram;
    private int mPositionLocation;
    private int mColorLocation;
    private int mMVPMatrixLoaction;

    static float cubeCoords[] = {
            -1.0f, 1.0f, 1.0f,
            -1.0f,-1.0f, 1.0f,
             1.0f,-1.0f, 1.0f,
             1.0f, 1.0f, 1.0f,

            -1.0f, 1.0f, -1.0f,
            -1.0f,-1.0f, -1.0f,
             1.0f,-1.0f, -1.0f,
             1.0f, 1.0f, -1.0f,

            0.0f, 0.0f, 0.0f,
            100.0f, 0.0f, 0.0f,

//            0.0f, 0.0f, 0.0f,
            0.0f, 100.0f, 0.0f,

//            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 100.0f

    };

    static short indice[] = {0, 1, 2, 3,   0, 3, 7, 4,   0, 4,  5, 1    , 6, 7, 3, 2,    6, 2, 1, 5,    6, 5, 4, 7,   8, 9, 8, 10, 8, 11 };

    float color[][] = {{1.0f, 0.0f, 0.0f, 1.0f},
            {0.0f, 1.0f, 0.0f, 1.0f},
            {0.0f, 0.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 0.0f, 1.0f},
            {1.0f, 0.0f, 1.0f, 1.0f},
            {0.0f, 1.0f, 1.0f, 1.0f}};

    public Cube() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mCubeVertexBuffer = byteBuffer.asFloatBuffer();
        mCubeVertexBuffer.put(cubeCoords);
        mCubeVertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(indice.length * 2);
        byteBuffer2.order(ByteOrder.nativeOrder());
        mIndiceBuffer = byteBuffer2.asShortBuffer();
        mIndiceBuffer.put(indice);
        mIndiceBuffer.position(0);

        // load and compile shaders for OpenGL program

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] OrigmvpMatrix, float[] mvpMatrix){
        GLES20.glUseProgram(mProgram);

        mPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITON);
        GLES20.glEnableVertexAttribArray(mPositionLocation);
        GLES20.glVertexAttribPointer(mPositionLocation, 3, GLES20.GL_FLOAT, false,  3 * 4, mCubeVertexBuffer);

        mColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);

        mMVPMatrixLoaction = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixLoaction, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");


        for (int i = 0; i < 6; i++){
            GLES20.glUniform4fv(mColorLocation, 1, color[i], 0);

            mIndiceBuffer.position(i * 4);
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, mIndiceBuffer);

        }

        GLES20.glUniformMatrix4fv(mMVPMatrixLoaction, 1, false, OrigmvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        for (int i = 0; i < 3; i++){
            GLES20.glUniform4fv(mColorLocation, 1, color[i], 0);

            mIndiceBuffer.position(24 + i * 2);
            GLES20.glDrawElements(GLES20.GL_LINES, 2, GLES20.GL_UNSIGNED_SHORT, mIndiceBuffer);
        }



        GLES20.glDisableVertexAttribArray(mPositionLocation);
    }

    private int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("Renderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
