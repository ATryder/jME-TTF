/* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  (1) Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *  (2) Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  (3) The name of the copyright holder may not be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.atr.jme.font.util;

/**
 *
 * @author lou
 */
public class Vector2d {

    public float x;
    public float y;

    public Vector2d() {
    }

    public Vector2d(Vector2d p) {

        this.x = p.x;
        this.y = p.y;
    }

    public Vector2d(float x, float y) {

        this.x = x;
        this.y = y;
    }

    public final void set(Vector2d p) {

        this.x = p.x;
        this.y = p.y;
    }

    public void normalize() {

        float length = x * x + y * y;

        if (length != 1f && length != 0f) {

            length = 1.0f / (float)Math.sqrt(length);
            x = x * length;
            y = y * length;
        }
    }

    public void scale(float scalar) {

        x = x * scalar;
        y = y * scalar;
    }
}
