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

/**
 * Represents a character glyph rendered using a bitmap.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class GlyphBMP extends Glyph {
    public final TrueTypeBMP ttb;
    
    public final int x;
    public final int y;
    public final int atlasWidth;
    public final int atlasHeight;
    
    protected int hMod;
    protected int xMod;
    
    public GlyphBMP(TrueTypeBMP ttb,
            int codePoint, int x, int y, int atlasWidth, int atlasHeight) {
        super(codePoint);
        
        this.ttb = ttb;
        this.x = x;
        this.y = y;
        if (codePoint == ' ') {
            this.atlasWidth = ttb.padding;
            this.atlasHeight = ttb.padding;
        } else {
            this.atlasWidth = atlasWidth;
            this.atlasHeight = atlasHeight;
        }
    }
    
    /**
     * 
     * @return The Y offset of the character in the atlas from its intended
     * Y location relative to the characters origin. When displaying the character
     * you'll want to subtract this value from the intended locations y-axis value.
     */
    public int getYOffset() {
        return hMod;
    }
    
    /**
     * 
     * @return The X offset of the character in the atlas from its intended
     * X location relative to the characters origin. When displaying the character
     * you'll want to add this value from the intended locations x-axis value.
     */
    public int getXOffset() {
        return xMod;
    }
    
    /**
     * 
     * @return The left x UV coordinate of this character in the texture atlas.
     */
    public float getLeftU() {
        return (float)x / ttb.getAtlas().getImage().getWidth();
    }
    
    /**
     * 
     * @return The right x UV coordinate of this character in the texture atlas.
     */
    public float getRightU() {
        return (float)(x + atlasWidth) / ttb.getAtlas().getImage().getWidth();
    }
    
    /**
     * 
     * @return The bottom y UV coordinate of this character in the texture atlas.
     */
    public float getBottomV() {
        return (float)y / ttb.getAtlas().getImage().getHeight();
    }
    
    /**
     * 
     * @return The top y UV coordinate of this character in the texture atlas.
     */
    public float getTopV() {
        return (float)(y + atlasHeight) / ttb.getAtlas().getImage().getHeight();
    }
}
