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
import com.jme3.asset.AssetKey;
import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.SimpleAssetCache;
import com.jme3.asset.cache.WeakRefAssetCache;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import java.io.IOException;

/**
 * Used by jMonkeyEngine's <code>AssetManager</code> to load a
 * {@link com.atr.jme.font.TrueTypeFont}.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
@SuppressWarnings("rawtypes")
public abstract class TrueTypeKey extends AssetKey {
    private Style style;
    private int pointSize;
    private int dpi;
    private boolean weakCache = false;
    private String preload;
    
    protected TrueTypeKey(String name, Style style, int pointSize, int dpi,
            boolean useWeakCache) {
        this(name, style, pointSize, dpi, useWeakCache, "");
    }
    
    protected TrueTypeKey(String name, Style style, int pointSize, int dpi,
            boolean useWeakCache, String preloadCharacters) {
        super(name);
        
        this.style = style;
        this.pointSize = pointSize;
        this.dpi = dpi;
        weakCache = useWeakCache;
        preload = preloadCharacters;
    }
    
    @Override
    public Class<? extends AssetCache> getCacheType(){
        return (!weakCache) ? SimpleAssetCache.class : WeakRefAssetCache.class;
    }
    
    /**
     * Gets the style used by the font associated with this key.
     * 
     * @return The style used by the font associated with this key.
     * 
     * @see Style
     */
    public Style getStyle() {
        return style;
    }
    
    /**
     * Gets the point size used by the font associated with this key.
     * 
     * @return The point size used by the font associated with this key.
     */
    public int getPointSize() {
        return pointSize;
    }
    
    /**
     * Gets the screen density setting for the font loaded by this key.
     * 
     * @return 
     */
    public int getScreenDensity() {
        return dpi;
    }
    
    /**
     * The jMonkeyEngine cache system can be set to use weak or strong references for
     * caching. If this is set to true this font will be stored as a weak reference
     * meaning it may be cleaned up by the garbage collector if there are no other
     * strong references to the font and memory is running low.
     * 
     * @return True if using weak references, false for strong references.
     */
    public boolean isWeakCache() {
        return weakCache;
    }
    
    public String getPreloadCharacters() {
        return new StringBuilder(preload).toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TrueTypeKey))
            return false;
        
        TrueTypeKey key = (TrueTypeKey)other;
        return name.equals(key.getName())
                && style == key.getStyle()
                && pointSize == key.getPointSize()
                && dpi == key.getScreenDensity()
                && weakCache == key.isWeakCache()
                && preload.equals(key.preload);
    }
    
    @Override
    public int hashCode() {
        return (this.toString()).hashCode();
    }
    
    @Override
    public String toString() {
        return name + "_Style:" + style.toString() +
                "_PointSize:" + Integer.toString(pointSize) +
                "_ScreenDensity:" + Integer.toString(dpi) +
                "_Cache:" + (weakCache ? "Weak" : "Strong") +
                "_Preload:" + preload;
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name, "name", null);
        oc.write(style.toString(), "style", "Plain");
        oc.write(pointSize, "pointSize", 5);
        oc.write(dpi, "density", 72);
        oc.write(weakCache, "weakCache", false);
        oc.write(preload, "preload", "");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        name = reducePath(ic.readString("name", null));
        extension = getExtension(name);
        String styl = ic.readString("style", "Plain");
        switch (styl) {
            case "Plain":
                style = Style.Plain;
                break;
            case "Bold":
                style = Style.Bold;
                break;
            case "Italic":
                style = Style.Italic;
                break;
            case "BoldItalic":
                style = Style.BoldItalic;
                break;
            default:
                style = Style.Plain;
        }
        pointSize = ic.readInt("pointSize", 5);
        dpi = ic. readInt("density", 72);
        weakCache = ic.readBoolean("weakCache", false);
        preload = ic.readString("preload", "");
    }
}
