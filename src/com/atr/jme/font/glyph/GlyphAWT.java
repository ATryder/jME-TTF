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

import com.atr.jme.font.TrueTypeBMP;
import java.awt.Shape;
import java.awt.font.GlyphVector;

/**
 * Represents a character glyph rendered using a bitmap created using
 * java.awt.Font. This is generally used for desktop platforms.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class GlyphAWT extends GlyphBMP {
    private final Shape shape;
    
    public GlyphAWT(TrueTypeBMP ttb, int x, int y, int codePoint, GlyphVector gv) {
        super(ttb, codePoint, x, y,
                (int)Math.ceil(gv.getVisualBounds().getWidth()) + ttb.padding,
                (int)Math.ceil(gv.getVisualBounds().getHeight()) + ttb.padding);
        
        xAdvance = (int)gv.getGlyphMetrics(0).getAdvanceX() + ttb.outline + Math.round(ttb.outline / 2f);
        yAdvance = (int)gv.getGlyphMetrics(0).getAdvanceY() + ttb.outline + Math.round(ttb.outline / 2f);
        
        if (codePoint == ' ') {
            hMod = 0;
            xMod = 0;
            ascender = 1;
            descender = 0;
            left = 0;
            right = xAdvance;
            shape = null;
        } else {
            hMod = (int)gv.getVisualBounds().getMinY();
            xMod = (int)gv.getVisualBounds().getMinX();

            ascender = (int)Math.ceil(gv.getVisualBounds().getMaxY() + ttb.outline);
            descender = (int)Math.floor(gv.getVisualBounds().getMinY() - (ttb.outline / 2f));
            left = (int)Math.floor(gv.getVisualBounds().getMinX() - (ttb.outline / 2f));
            right = (int)Math.ceil(gv.getVisualBounds().getMaxX() + ttb.outline);

            shape = gv.getOutline();
        }
    }
    
    /**
     * 
     * @return The glyph's outline.
     */
    public Shape getOutline() {
        return shape;
    }
}
