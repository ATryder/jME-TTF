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
package com.atr.jme.font;

import com.atr.jme.font.glyph.Glyph;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.StringContainer.Align;
import com.atr.jme.font.util.StringContainer.VAlign;
import com.atr.jme.font.util.Style;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The <code>TrueTypeFont</code> class encapsulates variables and methods used to
 * create and display text created from a True Type Font(.ttf) file.</p>
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 * 
 * @see TrueTypeBMP
 * @see TrueTypeMesh
 */
public abstract class TrueTypeFont<T extends Glyph, S extends TrueTypeNode> {
    public static final int SPACE_CODEPOINT = ' ';
    
    protected int defaultCodePoint = '\u0000';
    protected final AssetManager assetManager;
    
    public final int dpi;
    protected float scale = 1;
    
    protected final Style style;
    
    protected final int pointSize;
    protected int lineHeight;
    protected int ascender;
    protected int descender;
    protected int lineGap;
    
    protected final Map<Integer, T> cache = new HashMap<Integer, T>();
    protected final List<Integer> invalidCharacters = new ArrayList<Integer>();
    
    protected final StringBuilder sb = new StringBuilder();
    
    protected boolean cacheLock = false;
    
    protected TrueTypeFont(AssetManager assetManager, Style style, int pointSize, int dpi) {
        this.assetManager = assetManager;
        this.style = style;
        this.pointSize = pointSize;
        this.dpi = dpi;
    }
    
    /**
     * Locking will prevent any new characters from being added
     * to the cache. Any requested characters not currently in the cache
     * will be replaced by the default character.
     * 
     * @param lock True will lock the cache, false will unlock it.
     * 
     * @see #setDefaultCharacter(java.lang.String) 
     */
    public void lock(boolean lock) {
        cacheLock = lock;
    }
    
    /**
     * Whether or not the cache is currently locked.
     * 
     * @return True if the cache is locked, false otherwise.
     * 
     * @see #lock(boolean) 
     */
    public boolean isLocked() {
        return cacheLock;
    }
    
    /**
     * Determines if this font has a glyph for the specified character.
     * 
     * @param codePoint The Unicode codepoint for the character to check.
     * @return True if the font contains a glyph for the specified character
     * otherwise false.
     */
    public abstract boolean canDisplay(int codePoint);
    
    /**
     * Sets the default character to be displayed when a glyph is not available
     * in the font or the character is in the list of invalid characters set
     * with {@link #setInvalidCharacters(java.lang.String)}. 
     * Only the first character in the String will be used. If no default character
     * is set the true type font file's default character will be used.
     * 
     * @param text The character to use as a default character.
     * @return True if the character was set as default, false if the supplied
     * string was empty or the font did not contain the character.
     * 
     * @see #setInvalidCharacters(java.lang.String) 
     */
    public boolean setDefaultCharacter(String text) {
        if (text.isEmpty())
            return false;
        
        sb.delete(0, sb.length());
        sb.append(text);
        int codePoint = sb.codePointAt(0);
        if (cacheLock && cache.get(codePoint) == null)
            return false;
        if (canDisplay(codePoint)) {
            defaultCodePoint = codePoint;
            return true;
        }
        
        return false;
    }
    
    /**
     * Sets a list of characters to be invalidated. When a character from
     * the list of invalids is encountered the default character will be
     * used instead.
     * 
     * @param text A <code>String</code> containing all the characters to be
     * invalidated.
     * 
     * @see #setDefaultCharacter(java.lang.String) 
     */
    public void setInvalidCharacters(String text) {
        sb.delete(0, sb.length());
        sb.append(text);
        for (int i = 0; i < sb.length(); i++) {
            int codePoint = sb.codePointAt(i);
            if (!invalidCharacters.contains(codePoint))
                invalidCharacters.add(codePoint);
        }
    }
    
    /**
     * Bitmap texts tend not to look wonderful in smaller point sizes. To
     * remedy this use a larger point size, 26pt for example, and then use
     * this method to scale that back down to a smaller point size, such as
     * 14pt. For example <code>setScale(14f / 26f)</code>.
     * 
     * This will effect not only the scale of geometries returned by methods
     * of this <code>TrueTypeFont</code>, but also results returned by methods
     * such as <code>getLineWidth(String text)</code>.
     * 
     * @param scale The scale to modify geometries and calculations by.
     */
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    /**
     * The scale used by this <code>TrueTypeFont</code> to modify
     * Geometry sizes and calculations.
     * 
     * @return 
     */
    public float getScale() {
        return scale;
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied text.
     * Default horizontal alignment of left and vertical alignment
     * of top.
     * 
     * @param text The text to display.
     * @param kerning Amount of additional space between characters.
     * @param color The color of the text.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public S getText(String text, int kerning, ColorRGBA color) {
        return getText(text, kerning, color, Align.Left, VAlign.Top);
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied text.
     * Default horizontal alignment of left and vertical alignment
     * of top.
     * 
     * @param text The text to display.
     * @param kerning Amount of additional space between characters.
     * @param material The material to render the text with.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public S getText(String text, int kerning, Material material) {
        return getText(text, kerning, material, Align.Left, VAlign.Top);
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied text.
     * 
     * @param text The text to display.
     * @param kerning Amount of additional space between characters.
     * @param color The color of the text.
     * @param hAlign The horizontal alignment of the text.
     * @param vAlign The vertical alignment of the text.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public S getText(String text, int kerning, ColorRGBA color,
            Align hAlign, VAlign vAlign) {
        return getText(getGlyphMatrix(text), kerning, color, hAlign, vAlign);
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied text.
     * 
     * @param text The text to display.
     * @param kerning Amount of additional space between characters.
     * @param material The material to render the text with.
     * @param hAlign The horizontal alignment of the text.
     * @param vAlign The vertical alignment of the text.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public S getText(String text, int kerning, Material material,
            Align hAlign, VAlign vAlign) {
        return getText(getGlyphMatrix(text), kerning, material, hAlign, vAlign);
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied glyphs.
     * Default horizontal alignment of left and vertical alignment
     * of top.
     * 
     * @param glyphs The glyphs to display, each element in the root array
     * representing one line of text.
     * @param kerning Amount of additional space between characters.
     * @param color The color of the text.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public S getText(T[][] glyphs, int kerning, ColorRGBA color) {
        return getText(glyphs, kerning, color, Align.Left, VAlign.Top);
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied glyphs.
     * Default horizontal alignment of left and vertical alignment
     * of top.
     * 
     * @param glyphs The glyphs to display, each element in the root array
     * representing one line of text.
     * @param kerning Amount of additional space between characters.
     * @param material The material to render the text with.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public S getText(T[][] glyphs, int kerning, Material material) {
        return getText(glyphs, kerning, material, Align.Left, VAlign.Top);
    }
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied glyphs.
     * 
     * @param glyphs The glyphs to display, each element in the root array
     * representing one line of text.
     * @param kerning Amount of additional space between characters.
     * @param color The color of the text.
     * @param hAlign The horizontal alignment of the text.
     * @param vAlign The vertical alignment of the text.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public abstract S getText(T[][] glyphs, int kerning, ColorRGBA color, Align hAlign,
            VAlign vAlign);
    
    /**
     * Creates a {@link TrueTypeNode} to display the supplied glyphs.
     * 
     * @param glyphs The glyphs to display, each element in the root array
     * representing one line of text.
     * @param kerning Amount of additional space between characters.
     * @param material The material to render the text with.
     * @param hAlign The horizontal alignment of the text.
     * @param vAlign The vertical alignment of the text.
     * 
     * @return A {@link TrueTypeNode} to display.
     * 
     * @see TrueTypeNode
     */
    public abstract S getText(T[][] glyphs, int kerning, Material material, Align hAlign,
            VAlign vAlign);
    
    /**
     * Creates a {@link TrueTypeContainer} to display formatted text. The
     * resulting text will be displayed with the supplied color.
     * 
     * @param stringContainer An {@link StringContainer} containing the text
     * to be rendered along with associated formatting.
     * @param color The desired color of the text.
     * @return A {@link TrueTypeContainer} with the formatted text ready for
     * rendering. The <code>TrueTypeContainer</code> will be located at the
     * <code>StringContainer</code>s <code>textBox</code> x and y values.
     * 
     * @see TrueTypeContainer
     * @see StringContainer
     * @see #getFormattedText(com.atr.jme.font.util.StringContainer, com.jme3.material.Material)  
     */
    public abstract TrueTypeContainer getFormattedText(StringContainer stringContainer,
            ColorRGBA color);
    
    /**
     * Creates a {@link TrueTypeContainer} to display formatted text.
     * 
     * @param stringContainer An {@link StringContainer} containing the text
     * to be rendered along with associated formatting.
     * @param material A <code>Material</code> to render the text with.
     * @return A {@link TrueTypeContainer} with the formatted text ready for
     * rendering. The <code>TrueTypeContainer</code> will be located at the
     * <code>StringContainer</code>s <code>textBox</code> x and y values.
     * 
     * @see TrueTypeContainer
     * @see StringContainer
     * @see #getFormattedText(com.atr.jme.font.util.StringContainer, com.jme3.math.ColorRGBA) 
     */
    public abstract TrueTypeContainer getFormattedText(StringContainer stringContainer,
            Material material);
    
    /**
     * Gets an array of {@link Glyph}s representing the characters in
     * the supplied <code>String</code>. Characters not already in the cache
     * will be added. NO LINE BREAKS!!
     * 
     * @param text A <code>String</code> containing the characters you wish to
     * retrieve.
     * @return An array of <code>Glyph</code>s representing the characters
     * in the supplied <code>String</code>
     * 
     * @see Glyph
     * @see #getGlyphs(java.lang.StringBuilder) 
     */
    public T[] getGlyphs(String text) {
        sb.delete(0, sb.length());
        sb.append(text);
        return getGlyphs(sb);
    }
    
    /**
     * Gets an array of {@link Glyph}s representing the characters in
     * the supplied <code>String</code>. Characters not already in the cache
     * will be added. NO LINE BREAKS!!
     * 
     * @param text A <code>StringBuilder</code> containing the characters you wish to
     * retrieve.
     * @return An array of <code>Glyph</code>s representing the characters
     * in the supplied <code>StringBuilder</code>
     * 
     * @see Glyph
     * @see #getGlyphs(java.lang.String) 
     */
    public abstract T[] getGlyphs(StringBuilder text);
    
    /**
     * Gets a matrix of {@link Glyph}s. The text is split into separate lines
     * where '\n' is found. Each element in the root array of the matrix represents
     * one line of glyphs.
     * 
     * @param text The text to convert to glyphs.
     * @return 
     */
    public abstract T[][] getGlyphMatrix(String text);
    
    /**
     * For internal use only. Creates {@link Glyph}s that are not already cached
     * and adds them to the cache.
     * 
     * @param characters A <code>List</code> of {@link TrueTypeFont.CharToCreate}
     * containing the characters to be created and added to the cache. This
     * list may contain doubles.
     * 
     * @see #getGlyphs(java.lang.StringBuilder)
     */
    protected abstract void createGlyphs(List<CharToCreate> characters);
    
    /**
     * Get the width of one line of text in pixels. Characters supplied to
     * this method will be added to the texture atlas if not already present.
     * Use this method to obtain the line width of text created with one of the
     * bitmap text methods. NO LINE BREAKS!!
     * 
     * @param text A <code>String</code> containing the text to calculate.
     * @param kerning Additional spacing between characters in pixels.
     * @return The width of the supplied <code>String</code> in pixels.
     * 
     * @see #getLineWidth(java.lang.StringBuilder, int) 
     */
    public float getLineWidth(String text, int kerning) {
        return getLineWidth(getGlyphs(text), kerning);
    }
    
    /**
     * Get the width of one line of text in pixels. Characters supplied to
     * this method will be added to the texture atlas if not already present.
     * Use this method to obtain the line width of text created with one of the
     * bitmap text methods. NO LINE BREAKS!!
     * 
     * @param text A <code>StringBuilder</code> containing the text to calculate.
     * @param kerning Additional spacing between characters in pixels.
     * @return The width of the supplied <code>StringBuilder</code>s in pixels.
     * 
     * @see #getLineWidth(java.lang.String, int) 
     */
    public float getLineWidth(StringBuilder text, int kerning) {
        return getLineWidth(getGlyphs(text), kerning);
    }
    
    /**
     * Get the width of one line of text in pixels.Use this method to obtain
     * the line width of text created with one of the bitmap text methods.
     * 
     * @param glyphs An array of <code>TrueTypeBitmapGlyph</code>s.
     * @param kerning Additional spacing between characters in pixels.
     * @return The width of the supplied <code>TrueTypeBitmapGlyph</code>s in pixels.
     * 
     * @see #getLineWidth(java.lang.String, int) 
     * @see #getLineWidth(java.lang.StringBuilder, int) 
     */
    public float getLineWidth(T[] glyphs, int kerning) {
        if (glyphs.length == 0)
            return 0;
        int lineWidth = 0;
        for (int i = 0; i < glyphs.length - 1; i++) {
            lineWidth += glyphs[i].getXAdvance() + kerning;
        }
        lineWidth += glyphs[glyphs.length - 1].getRight();
        
        return lineWidth * scale;
    }
    
    /**
     * This is the actual line height from baseline to baseline not modified by
     * {@link #setScale(float)}.
     * 
     * @return The line height, in pixels, from baseline to baseline.
     */
    public int getActualLineHeight() {
        return lineHeight;
    }
    
    /**
     * The line height from baseline to baseline scaled by
     * {@link #setScale(float)}.
     * 
     * @return The line height,from baseline to baseline.
     */
    public float getScaledLineHeight() {
        return scale * lineHeight;
    }
    
    /**
     * The line height from baseline to baseline scaled by
     * {@link TrueTypeFont#setScale(float)}.
     * 
     * @return The line height, in pixels, from baseline to baseline.
     */
    public int getScaledLineHeightInt() {
        return Math.round(scale * lineHeight);
    }
    
    /**
     * Additional space between lines.
     * 
     * @return The amount of additional space between lines.
     */
    public int getActualLineGap() {
        return lineGap;
    }
    
    /**
     * Gets the additional spacing between lines.
     * 
     * @return The amount of additional space between lines scaled by
     * {@link #setScale(float)}.
     */
    public float getScaledLineGap() {
        return lineGap * scale;
    }
    
    /**
     * Gets the additional spacing between lines rounded to the
     * nearest integer.
     * 
     * @return 
     */
    public int getScaledLineGapInt() {
        return Math.round(scale * lineGap);
    }
    
    /**
     * The actual point size of the font.
     * 
     * @return 
     */
    public float getActualPointSize() {
        return pointSize;
    }
    
    /**
     * The point size of the font scaled by {@link #setScale(float)}.
     * 
     * @return 
     */
    public float getScaledPointSize() {
        return pointSize * scale;
    }
    
    /**
     * The point size of the font scaled by {@link #setScale(float)}
     * and rounded to the nearest integer.
     * 
     * @return 
     */
    public int getScaledPointSizeInt() {
        return Math.round(scale * pointSize);
    }
    
    /**
     * The number of pixels above the baseline that a character
     * can extend. Note some characters may extend greater than this amount.
     * 
     * @return 
     */
    public int getActualAscender() {
        return ascender;
    }
    
    /**
     * The number of pixels above the baseline that a character
     * can extend scaled by the <code>TrueTypeFont</code>s scale value.
     * Note some characters may extend greater than this amount.
     * 
     * @return 
     */
    public float getScaledAscender() {
        return scale * ascender;
    }
    
    /**
     * The number of pixels below the baseline that a character
     * can extend. Note some characters may extend greater than this amount.
     * 
     * @return 
     */
    public int getActualDescender() {
        return descender;
    }
    
    /**
     * The number of pixels below the baseline that a character
     * can extend scaled by the <code>TrueTypeFont</code>s scale value.
     * Note some characters may extend greater than this amount.
     * 
     * @return 
     */
    public float getScaledDescender() {
        return scale * descender;
    }
    
    /**
     * Get the height of the text from the bottom of the character that extends the
     * deepest below the baseline to the top of the character that extends the highest
     * above the baseline. NO LINE BREAKS!!
     * 
     * @param text The text to calculate the hight of.
     * @return The height.
     */
    public float getVisualLineHeight(String text) {
        return getVisualLineHeight(getGlyphs(text));
    }
    
    /**
     * Get the height of the text from the bottom of the character that extends the
     * deepest below the baseline to the top of the character that extends the highest
     * above the baseline. NO LINE BREAKS!!
     * 
     * @param text The text to calculate the hight of.
     * @return The height.
     */
    public float getVisualLineHeight(StringBuilder text) {
        return getVisualLineHeight(getGlyphs(text));
    }
    
    /**
     * Get the height of the text from the bottom of the character that extends the
     * deepest below the baseline to the top of the character that extends the highest
     * above the baseline.
     * 
     * @param glyphs The text to calculate the hight of.
     * @return The height.
     */
    public float getVisualLineHeight(T[] glyphs) {
        if (glyphs.length == 0)
            return 0;
        int maxY = Integer.MIN_VALUE;
        for (T glyph : glyphs) {
            maxY = (glyph.getHeight() > maxY) ? glyph.getHeight() : maxY;
        }
        
        return maxY * scale;
    }
    
    /**
     * Gets the amount above the baseline the tallest character in the
     * array extends.
     * 
     * @param text The text to calculate.
     * @return The hight above the baseline of the tallest character.
     */
    public float getVisualAscent(String text) {
        return getVisualAscent(getGlyphs(text));
    }
    
    /**
     * Gets the amount above the baseline the tallest character in the
     * array extends.
     * 
     * @param text The text to calculate.
     * @return The hight above the baseline of the tallest character.
     */
    public float getVisualAscent(StringBuilder text) {
        return getVisualAscent(getGlyphs(text));
    }
    
    /**
     * Gets the amount above the baseline the tallest character in the
     * array extends.
     * 
     * @param glyphs The text to calculate.
     * @return The hight above the baseline of the tallest character.
     */
    public float getVisualAscent(T[] glyphs) {
        if (glyphs.length == 0)
            return 0;
        int maxY = Integer.MIN_VALUE;
        for (T glyph : glyphs) {
            maxY = (glyph.getAscender() > maxY) ? glyph.getAscender() : maxY;
        }
        
        return maxY * scale;
    }
    
    /**
     * Gets the amount below the baseline the character that extends
     * the lowest beyond the baseline extends.
     * 
     * @param text The text to calculate.
     * @return The depth below the baseline.
     */
    public float getVisualDescent(String text) {
        return getVisualDescent(getGlyphs(text));
    }
    
    /**
     * Gets the amount below the baseline the character that extends
     * the lowest beyond the baseline extends.
     * 
     * @param text The text to calculate.
     * @return The depth below the baseline.
     */
    public float getVisualDescent(StringBuilder text) {
        return getVisualDescent(getGlyphs(text));
    }
    
    /**
     * Gets the amount below the baseline the character that extends
     * the lowest beyond the baseline extends.
     * 
     * @param glyphs The text to calculate.
     * @return The depth below the baseline.
     */
    public float getVisualDescent(T[] glyphs) {
        if (glyphs.length == 0)
            return 0;
        int minY = Integer.MAX_VALUE;
        for (T glyph : glyphs) {
            minY = (glyph.getDescender() < minY) ? glyph.getDescender() : minY;
        }
        
        return minY * scale;
    }
    
    /**
     * A helper class used when adding new characters to the texture atlas and cache.
     * 
     * @see TrueTypeFont#createGlyphs(java.util.List) 
     * 
     * @author Adam T. Ryder http://1337atr.weebly.com
     */
    protected class CharToCreate {
        public final int index;
        public int codePoint;
        
        protected CharToCreate(int index, int codePoint) {
            this.index = index;
            this.codePoint = codePoint;
        }
    }
}
