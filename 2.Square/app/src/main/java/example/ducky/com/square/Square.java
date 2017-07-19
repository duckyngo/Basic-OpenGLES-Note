package example.ducky.com.square;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class Square {

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


    private final FloatBuffer mSquareVertexBuffer;
//    private final FloatBuffer mIndiceBuffer;
    private final int mProgram;
    private int mPositionLocation;
    private int mColorLocation;
    private int mMVPMatrixLoaction;

    static float squareCoord[] = {
            -0.5f, 0.5f,0.0f,
            -0.5f,-0.5f,0.0f,
             0.5f,-0.5f,0.0f,
             0.5f, 0.5f,0.0f
    };


    float color[] = {0.0f, 1.0f, 0.0f, 1.0f};

    public Square() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(squareCoord.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mSquareVertexBuffer = byteBuffer.asFloatBuffer();
        mSquareVertexBuffer.put(squareCoord);
        mSquareVertexBuffer.position(0);

        // load and compile shaders for OpenGL program

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix){
        GLES20.glUseProgram(mProgram);

        mPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITON);
        GLES20.glEnableVertexAttribArray(mPositionLocation);
        GLES20.glVertexAttribPointer(mPositionLocation, 3, GLES20.GL_FLOAT, false,  3 * 4, mSquareVertexBuffer);

        mColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        GLES20.glUniform4fv(mColorLocation, 1, color, 0);

        mMVPMatrixLoaction = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixLoaction, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

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
