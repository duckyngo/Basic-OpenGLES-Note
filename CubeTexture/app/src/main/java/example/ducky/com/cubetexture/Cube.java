package example.ducky.com.cubetexture;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


public class Cube {

    String A_POSITON = "vPosition";
    String U_MATRIX = "uMVPMatrix";
    String U_COLOR = "vColor";
    String A_TEXTURECOOR = "aCoord";
    String U_SAMPLER = "uSampler";

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aCoord;" +
                    "varying vec2 vCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "vCoord = aCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "varying vec2 vCoord;" +
                    "uniform sampler2D uSampler;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uSampler, vCoord);" +
                    "}";

    private final FloatBuffer mSquareVertexBuffer;
    private final FloatBuffer mCoordBuffer;
    private final ShortBuffer mIndiceBuffer;
    private final int mProgram;
    private int mPositionLocation;
    private int mColorLocation;
    private int mMVPMatrixLoaction;
    private int mTextureCoordLocation;
    private int mTextureUniformLocation;
    private int mTextureId[] = new int[6];
    private Context mContext;

    static float cubeCoord[] = {
            -1.0f,   1.0f,   1.0f,
            -1.0f,  -1.0f,   1.0f,
             1.0f,  -1.0f,   1.0f,
             1.0f,   1.0f,   1.0f,

            -1.0f,   1.0f,  -1.0f,
            -1.0f,  -1.0f,  -1.0f,
             1.0f,  -1.0f,  -1.0f,
             1.0f,   1.0f,  -1.0f
    };

    private final short indices[] = {0, 4, 7, 3, 0, 3, 2, 1, 0, 1, 5, 4, 6, 2, 1, 5, 6, 5, 4, 7, 6, 2, 3, 7};

    float color[] = {0.0f, 1.0f, 0.0f, 1.0f};

    static float textureCoord[] = {
            0, 1,
            1, 1,
            1, 0,
            0, 0,
    };

    public Cube(Context context) {
        mContext = context;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cubeCoord.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mSquareVertexBuffer = byteBuffer.asFloatBuffer();
        mSquareVertexBuffer.put(cubeCoord);
        mSquareVertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(textureCoord.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mCoordBuffer = byteBuffer.asFloatBuffer();
        mCoordBuffer.put(textureCoord);
        mCoordBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(indices.length * 2);
        byteBuffer.order(ByteOrder.nativeOrder());
        mIndiceBuffer= byteBuffer.asShortBuffer();
        mIndiceBuffer.put(indices);
        mIndiceBuffer.position(0);

        // load and compile shaders for OpenGL program

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        mTextureId[0] = MyRenderer.loadTexture(mContext, R.drawable.texture1);
        mTextureId[1] = MyRenderer.loadTexture(mContext, R.drawable.texture2);
        mTextureId[2] = MyRenderer.loadTexture(mContext, R.drawable.texture3);
        mTextureId[3] = MyRenderer.loadTexture(mContext, R.drawable.texture4);
        mTextureId[4] = MyRenderer.loadTexture(mContext, R.drawable.texture5);
        mTextureId[5] = MyRenderer.loadTexture(mContext, R.drawable.texture6);

    }

    public void draw(float[] mvpMatrix){
        GLES20.glUseProgram(mProgram);

        mTextureUniformLocation = GLES20.glGetUniformLocation(mProgram, U_SAMPLER);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLES20.glUniform1i(mTextureUniformLocation, 0);


        mPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITON);
        GLES20.glEnableVertexAttribArray(mPositionLocation);
        mSquareVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionLocation, 3, GLES20.GL_FLOAT, false,  3 * 4, mSquareVertexBuffer);

        mTextureCoordLocation = GLES20.glGetAttribLocation(mProgram, A_TEXTURECOOR);
        GLES20.glEnableVertexAttribArray(mTextureCoordLocation);

        mCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordLocation, 2, GLES20.GL_FLOAT, false,  2 * 4, mCoordBuffer);

        mColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        GLES20.glUniform4fv(mColorLocation, 1, color, 0);

        mMVPMatrixLoaction = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixLoaction, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        for (int i = 0; i <= 5; i++){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[1]);
            mIndiceBuffer.position(i * 4);
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4,
                    GLES20.GL_UNSIGNED_SHORT, mIndiceBuffer);
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
