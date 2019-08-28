#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef GL_ES
    precision mediump float;
#endif

uniform sampler2D m_Texture;
uniform vec4 m_Color;
uniform vec4 m_Outline;

varying vec2 texCoord;

void main() {
    vec4 col = texture2D(m_Texture, texCoord);
    if (col.r <= 0.01) {
        discard;
    } else {
        float a = col.r;
        col = (m_Outline * (1.0 - col.b)) + (m_Color * col.b);
        col.a *= a;
        
        gl_FragColor = col;
    }  
}

