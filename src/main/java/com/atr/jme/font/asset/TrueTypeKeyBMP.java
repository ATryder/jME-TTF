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
package com.atr.jme.font.asset;

import com.atr.jme.font.util.Style;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import java.io.IOException;

/**
 * Used to load {@link com.atr.jme.font.TrueTypeBMP} fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeKeyBMP extends TrueTypeKey {
    private int outline;
    private int maxAtlasResolution = 2048;
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with a default style
     * of Plain, no outline, a default screen density of 72 and default
     * strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param pointSize The desired point size.
     */
    public TrueTypeKeyBMP(String name, int pointSize) {
        this(name, Style.Plain, pointSize);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with no outline,
     * a default screen density of 72 and default strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize) {
        this(name, style, pointSize, 0);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with no outline,
     * a default screen density of 72 and default strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param preload A set of characters to initialize the font with.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, String preload) {
        this(name, style, pointSize, 0, preload);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with no outline,
     * a default screen density of 72 and default strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param preload A set of characters to initialize the font with.
     * @param maxAtlasResolution The maximum resolution of the texture atlas.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, String preload,
            int maxAtlasResolution) {
        this(name, style, pointSize, 0, preload, maxAtlasResolution);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with a default
     * screen density of 72 and default strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, int outline) {
        this(name, style, pointSize, outline, 72, false);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with a default
     * screen density of 72 and default strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @param preload A set of characters to initialize the font with.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, int outline, String preload) {
        this(name, style, pointSize, outline, 72, false, preload);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code> with a default
     * screen density of 72 and default strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @param preload A set of characters to initialize the font with.
     * @param maxAtlasResolution The maximum resolution of the texture atlas.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, int outline, String preload,
            int maxAtlasResolution) {
        this(name, style, pointSize, outline, 72, false, preload, maxAtlasResolution);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code>.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @param dpi The density of the screen in dots per inch.
     * @param useWeakCache Set to true to use weak references in
     * the cache, this will cause the loaded font to be cleaned up
     * by the garbage collector if memory becomes scarce, the font
     * will be re-loaded when needed in this case. Set to false
     * to use strong references in the cache preventing the
     * garbage collector from reclaiming this font.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, int outline, int dpi,
            boolean useWeakCache) {
        super(name, style, pointSize, dpi, useWeakCache);
        this.outline = outline;
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code>.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @param dpi The density of the screen in dots per inch.
     * @param useWeakCache Set to true to use weak references in
     * the cache, this will cause the loaded font to be cleaned up
     * by the garbage collector if memory becomes scarce, the font
     * will be re-loaded when needed in this case. Set to false
     * to use strong references in the cache preventing the
     * garbage collector from reclaiming this font.
     * @param preload A set of characters to initialize the font with.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, int outline, int dpi,
            boolean useWeakCache, String preload) {
        super(name, style, pointSize, dpi, useWeakCache, preload);
        this.outline = outline;
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyBMP</code>.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @param dpi The density of the screen in dots per inch.
     * @param useWeakCache Set to true to use weak references in
     * the cache, this will cause the loaded font to be cleaned up
     * by the garbage collector if memory becomes scarce, the font
     * will be re-loaded when needed in this case. Set to false
     * to use strong references in the cache preventing the
     * garbage collector from reclaiming this font.
     * @param preload A set of characters to initialize the font with.
     * @param maxAtlasResolution The maximum resolution of the texture atlas.
     */
    public TrueTypeKeyBMP(String name, Style style, int pointSize, int outline, int dpi,
            boolean useWeakCache, String preload, int maxAtlasResolution) {
        super(name, style, pointSize, dpi, useWeakCache, preload);
        this.outline = outline;
        this.maxAtlasResolution = maxAtlasResolution;
    }
    
    /**
     * 
     * @return The size of the outline around bitmap text.
     */
    public int getOutline() {
        return outline;
    }
    
    /**
     * 
     * @return The maximum resolution of the texture atlas.
     */
    public int getMaxAtlasRes() {
        return maxAtlasResolution;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TrueTypeKeyBMP) ||
                !super.equals(other))
            return false;
        
        TrueTypeKeyBMP key = (TrueTypeKeyBMP)other;
        return outline == key.getOutline();
    }
    
    @Override
    public String toString() {
        return super.toString() + "_Outline:" + Integer.toString(outline)
                + "_MaxRes:" + Integer.toString(maxAtlasResolution);
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(outline, "outline", 0);
        oc.write(maxAtlasResolution, "maxres", 2048);
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        outline = ic.readInt("outline", 0);
        maxAtlasResolution = ic.readInt("maxres", 2048);
    }
}
