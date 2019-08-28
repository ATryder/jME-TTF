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

/**
 * Used to load {@link com.atr.jme.font.TrueTypeMesh} fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeKeyMesh extends TrueTypeKey {
    
    /**
     * Instantiates a new <code>TrueTypeKeyMesh</code> with a default
     * style of Plain, default point size of 12, screen density of
     * 72, and strong cache.
     * 
     * @param name The path to the true type font asset.
     */
    public TrueTypeKeyMesh(String name) {
        this(name, 12);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyMesh</code> with a default
     * style of Plain, screen density of 72, and strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param pointSize The desired point size.
     */
    public TrueTypeKeyMesh(String name, int pointSize) {
        this(name, Style.Plain, pointSize);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyMesh</code> with a default 
     * screen density of 72, and strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     */
    public TrueTypeKeyMesh(String name, Style style, int pointSize) {
        this(name, style, pointSize, 72, false);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyMesh</code> with a default 
     * screen density of 72, and strong cache.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param preload A set of characters to initialize the font with.
     */
    public TrueTypeKeyMesh(String name, Style style, int pointSize, String preload) {
        this(name, style, pointSize, 72, false, preload);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyMesh</code>.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param dpi The density of the screen in dots per inch.
     * @param useWeakCache Set to true to use weak references in
     * the cache, this will cause the loaded font to be cleaned up
     * by the garbage collector if memory becomes scarce, the font
     * will be re-loaded when needed in this case. Set to false
     * to use strong references in the cache preventing the
     * garbage collector from reclaiming this font.
     */
    public TrueTypeKeyMesh(String name, Style style, int pointSize, int dpi,
            boolean useWeakCache) {
        super(name, style, pointSize, dpi, useWeakCache);
    }
    
    /**
     * Instantiates a new <code>TrueTypeKeyMesh</code>.
     * 
     * @param name The path to the true type font asset.
     * @param style The {@link Style} of the font.
     * @param pointSize The desired point size.
     * @param dpi The density of the screen in dots per inch.
     * @param useWeakCache Set to true to use weak references in
     * the cache, this will cause the loaded font to be cleaned up
     * by the garbage collector if memory becomes scarce, the font
     * will be re-loaded when needed in this case. Set to false
     * to use strong references in the cache preventing the
     * garbage collector from reclaiming this font.
     * @param preload A set of characters to initialize the font with.
     */
    public TrueTypeKeyMesh(String name, Style style, int pointSize, int dpi,
            boolean useWeakCache, String preload) {
        super(name, style, pointSize, dpi, useWeakCache, preload);
    }
}
