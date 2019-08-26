#ifdef USEAA
    #ifdef GL_ES
        #extension GL_OES_standard_derivatives:enable
        #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
        #else
            precision mediump float;
        #endif
    #endif
#else
    #ifdef GL_ES
        precision mediump float;
    #endif
#endif

uniform vec4 m_Color;

varying vec2 texCoord;
varying vec2 texCoord2;

#ifdef USEAA
    float AA(in float x, in float add) {
        vec2 px = vec2(dFdx(texCoord.x), dFdx(texCoord.y));
        vec2 py = vec2(dFdy(texCoord.x), dFdy(texCoord.y));
        float fx = (2.0*texCoord.x)*px.x - px.y;
        float fy = (2.0*texCoord.y)*py.x - py.y;
        float sd = x/sqrt(fx*fx + fy*fy);

        return clamp(add + sd, 0.0, 1.0);
    }
#endif

void main() {
    float s = step(texCoord2.x, 1.5);
    float c = ((texCoord.x * texCoord.x) - texCoord.y) * texCoord2.y;
    #ifdef USEAA
        float alpha = (AA(c, 0.25) * s) + (AA(texCoord.x, 0.25) * (1.0 - s));
    #else
        float alpha = (step(0.0, c) * s) + (1.0 * (1.0 - s));
    #endif
    
    if (alpha <= 0.001) {
        discard;
    } else {
        gl_FragColor = vec4(m_Color.rgb, alpha * m_Color.a); 
    }
}

