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

import android.graphics.Path;
import com.atr.jme.font.TrueTypeSfntly;
import com.atr.jme.font.util.Glyf;

/**
 * Represents a character glyph rendered using a bitmap created using
 * Google's Sfntly library. This is generally used on Android devices.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class GlyphSfntly extends GlyphBMP {
    public Path contours;
    
    public GlyphSfntly(TrueTypeSfntly ttb, int x, int y, int codePoint, Glyf glyf,
            float xAdvance, float yAdvance) {
        super(ttb, codePoint, x, y,
                (int)Math.ceil(glyf.maxX - glyf.minX) + ttb.padding + ttb.bold,
                (int)Math.ceil(glyf.getHeight()) + ttb.padding + ttb.bold);
        
        this.xAdvance = Math.round(xAdvance);
        this.yAdvance = Math.round(yAdvance);
        
        if (codePoint == ' ') {
            hMod = 0;
            xMod = 0;
            ascender = 1;
            descender = 0;
            left = 0;
            right = this.xAdvance;
            contours = null;
        } else {
            hMod = (int)Math.ceil(glyf.minY - (ttb.bold / 2f));
            xMod = (int)Math.ceil(glyf.minX - (ttb.bold / 2f));

            ascender = (int)Math.ceil(glyf.maxY + ttb.outline + (ttb.bold / 2f));
            descender = (int)Math.floor(glyf.minY - ((ttb.outline + ttb.bold) / 2f));
            left = (int)Math.floor(glyf.minX - ((ttb.outline + ttb.bold) / 2f));
            right = (int)Math.ceil(glyf.maxX + ttb.outline + (ttb.bold / 2f));

            contours = glyf.contours;
        }
    }
    
    /**
     * For internal use only. Should return null.
     * 
     * @return The glyph's outline.
     */
    public Path getOutline() {
        return contours;
    }
}
