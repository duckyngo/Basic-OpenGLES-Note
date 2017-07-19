attribute vec3 a_Position;
attribute vec2 a_TextureCoordinate;
uniform mat4 u_Matrix;
varying vec2 v_TextureCoordinate;
void main() {
 gl_PointSize = 5.0;
 gl_Position = u_Matrix*vec4(a_Position,1);
 v_TextureCoordinate = a_TextureCoordinate;
}