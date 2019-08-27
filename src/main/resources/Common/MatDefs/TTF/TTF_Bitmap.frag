#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef GL_ES
    precision mediump float;
#endif

uniform sampler2D m_Texture;
uniform vec4 m_Color;

varying vec2 texCoord;

void main() {
    vec4 col = texture2D(m_Texture, texCoord);
    if (col.r <= 0.01) {
        discard;
    } else {
        col.a = m_Color.a * col.r;
        col.rgb = m_Color.rgb;
        
        gl_FragColor = col;
    }
}

