/*
 * Free Public License 1.0.0
 * Permission to use, copy, modify, and/or distribute this software
 * for any purpose with or without fee is hereby granted.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.atr.jme.font.glyph;

import com.atr.jme.font.shape.MeshGlyf;

/**
 * Represents a character glyph rendered using a triangulated mesh.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class GlyphMesh extends Glyph {
    private final MeshGlyf mesh;
    
    public GlyphMesh(int codePoint, MeshGlyf mesh, float xAdvance,
            float yAdvance) {
        super(codePoint);
        
        this.mesh = mesh;
        ascender = (int)Math.ceil(mesh.getMaxY());
        descender = (int)Math.floor(mesh.getMinY());
        left = (int)Math.floor(mesh.getMinX());
        right = (int)Math.ceil(mesh.getMaxX());
        this.xAdvance = (int)Math.ceil(xAdvance);
        this.yAdvance = (int)Math.ceil(yAdvance);
    }
    
    public MeshGlyf getMesh() {
        return mesh;
    }
}
