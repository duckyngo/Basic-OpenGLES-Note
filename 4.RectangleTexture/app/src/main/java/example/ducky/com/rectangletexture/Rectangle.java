package example.ducky.com.rectangletexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class Rectangle {

    String A_POSITON = "vPosition";
    String U_MATRIX = "uMVPMatrix";
    String U_COLOR = "vColor";
    String A_TEXTURECOOR = "aCoord";
    String U_TEXTURE = "uSampler";

//    private final String vertexShaderCode =
//            "uniform mat4 uMVPMatrix;" +
//                    "attribute vec4 vPosition;" +
//                    "attribute vec2 a_TexCoordinate;" +
//                    "varying vec2 v_TexCoordinate;" +
//                    "void main(){" +
//                    "   v_TexCoordinate = a_TexCoordinate;" +
//                    "   gl_Position = uMVPMatrix * vPosition;" +
//                    "}";
//
//    private final String fragmentShaderCode =
//            "precision mediump float;" +
//                    "uniform sampler2D u_Texture;" +
//                    "varying vec2 v_TexCoordinate;" +
//                    "void main(){" +
//                    "   gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
//                    "}";


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
//    private final FloatBuffer mIndiceBuffer;
    private final int mProgram;
    private int mPositionLocation;
    private int mColorLocation;
    private int mMVPMatrixLoaction;
    private int mTextureCoordLocation;
    private int mTextureUniformLocation;
    private int mTextureId;
    private Context mContext;

    static float squareCoord[] = {
            -0.5f, 0.8f,0.0f,
            -0.5f,-0.8f,0.0f,
             0.5f,-0.8f,0.0f,
             0.5f, 0.8f,0.0f
    };


    float color[] = {0.0f, 1.0f, 0.0f, 1.0f};

    static float textureCoord[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    public Rectangle(Context context) {
        mContext = context;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(squareCoord.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mSquareVertexBuffer = byteBuffer.asFloatBuffer();
        mSquareVertexBuffer.put(squareCoord);
        mSquareVertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(textureCoord.length * 4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        mCoordBuffer = byteBuffer2.asFloatBuffer();
        mCoordBuffer.put(textureCoord);
        mCoordBuffer.position(0);

        // load and compile shaders for OpenGL program

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        mTextureId = MyRenderer.loadTexture(mContext, R.drawable.tomato);
//        mTextureId = CreateTexture(mContext.getResources().openRawResource(R.drawable.tomato));
    }

    public void draw(float[] mvpMatrix){
        GLES20.glUseProgram(mProgram);

        mTextureUniformLocation = GLES20.glGetUniformLocation(mProgram, U_TEXTURE);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
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

    private int CreateTexture(InputStream ins){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        InputStream is1 = ins;
        Bitmap img1;
        try {
            img1 = BitmapFactory.decodeStream(is1);
        } finally {
            try {
                is1.close();
            } catch(IOException e) {
                //e.printStackTrace();
            }
        }
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img1, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textures[0];
    }
}
