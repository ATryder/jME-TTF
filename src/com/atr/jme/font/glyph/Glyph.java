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

/**
 * Represents a character glyph.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class Glyph {
    public final int codePoint;
    
    protected int ascender;
    protected int descender;
    protected int left;
    protected int right;
    protected int xAdvance;
    protected int yAdvance;
    
    public Glyph(int codePoint) {
        this.codePoint = codePoint;
    }
    
    /**
     * Gets the amount the glyph extends above the baseline.
     * 
     * @return Amount the glyph extends above the baseline.
     */
    public int getAscender() {
        return ascender;
    }
    
    /**
     * Gets the amount the glyph extends below the baseline.
     * This should be negative if the glyph extends below the baseline.
     * 
     * @return Amount the glyph extends below the baseline.
     */
    public int getDescender() {
        return descender;
    }
    
    /**
     * Gets the amount beyond the glyph's origin that the contour begins.
     * Will be negative if the glyph extends left of the origin.
     * 
     * @return Amount beyond the glyph's origin that the contour begins.
     */
    public int getLeft() {
        return left;
    }
    
    /**
     * Gets the amount beyond the origin that the glyph's contour ends.
     * 
     * @return Amount beyond the origin that the glyph's contour ends.
     */
    public int getRight() {
        return right;
    }
    
    /**
     * Gets the total height of the glyph.
     * 
     * @return The height of the glyph.
     */
    public int getHeight() {
        return ascender - descender;
    }
    
    /**
     * Gets the total width of the glyph.
     * 
     * @return The width of the glyph.
     */
    public int getWidth() {
        return right - left;
    }
    
    /**
     * The distance from the origin to the beginning of the next character on
     * the x-axis.
     * 
     * @return The distance, in pixels, from this characters origin to the
     * beginning of the next character.
     */
    public int getXAdvance() {
        return xAdvance;
    }
    
    /**
     * The distance from the origin to the beginning of the next character on
     * the y-axis. Not currently supported.
     * 
     * @return The distance, in pixels, from this character's origin to the
     * beginning of the next character on the y-axis for horizontal text.
     */
    public int getYAdvance() {
        return yAdvance;
    }
}
