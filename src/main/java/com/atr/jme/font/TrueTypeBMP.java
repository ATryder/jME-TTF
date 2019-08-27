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

import com.atr.jme.font.glyph.GlyphBMP;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.shape.TrueTypeBMPContainer;
import com.atr.jme.font.shape.TrueTypeText;
import com.atr.jme.font.util.AtlasListener;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.StringContainer.Align;
import com.atr.jme.font.util.StringContainer.VAlign;
import com.atr.jme.font.util.Style;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.NativeObjectManager;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>An implementation of {@link TrueTypeFont} that renders fonts with a <code>Texture2D</code>
 * texture atlas containing all the currently cached characters. This texture atlas is updated
 * in real-time as new characters are requested.</p>
 * 
 * <p>###It is imperative that you take care to provide {@link com.atr.jme.font.util.AtlasListener}s.
 * When the atlas is recreated to store a new character the previous atlas' underlying
 * ByteBuffers are removed from memory immediately. If you do not wish to work with
 * {@link com.atr.jme.font.util.AtlasListener}s then after loading the font pre-load all the
 * characters you desire to use by supplying a String with said characters to
 * {@link #getText(java.lang.String, int, com.jme3.math.ColorRGBA)} then supplying true to
 * {@link #lock(boolean)} which will prevent the atlas from loading any new characters.###</p>
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 * 
 * @see TrueTypeAWT
 * @see TrueTypeFont
 */
public abstract class TrueTypeBMP<T extends GlyphBMP> extends TrueTypeFont<T, TrueTypeText> {
    public final int outline;
    public final int padding;
    
    protected int charHeight;
    protected int resizeWidth;
    
    protected final List<AtlasLine> atlasLines = new ArrayList<AtlasLine>();
    protected Texture2D atlas;
    protected boolean atlasResized = false;
    protected int atlasWidth = 0;
    protected int atlasHeight = 0;
    protected int maxTexRes = 2048;
    protected final List<AtlasListener> onAtlas = new LinkedList<AtlasListener>();
    
    protected boolean fixedResolution = false;

    public TrueTypeBMP(AssetManager assetManager, Style style, int pointSize, int outline, int dpi,
            int maxAtlasResolution, boolean fixedResolution) {
        super(assetManager, style, pointSize, dpi);
        
        this.outline = outline;
        padding = 10 + (outline * 2);
        maxTexRes = maxAtlasResolution;
        this.fixedResolution = fixedResolution;
    }
    
    /**
     * Sets the maximum resolution of the texture atlas. Default
     * 2048px.
     * 
     * @param resolution The maximum resolution of the texture atlas.
     */
    public void setMaxAtlasResolution(int resolution) {
        maxTexRes = Math.max(resolution, 0);
    }
    
    /**
     * Retrieve the maximum resolution of the texture atlas.
     * 
     * @return The maximum resolution of the texture atlas.
     */
    public int getMaxAtlasResolution() {
        return maxTexRes;
    }
    
    public Texture2D getAtlas() {
        return atlas;
    }
    
    /**
     * Add a {@link AtlasListener} which will be called after the texture atlas
     * has been modified.
     * 
     * @param listener The {@link AtlasListener} you wish to add to the list
     * of listeners that will be called after the texture atlas has been resized.
     * 
     * @see AtlasListener
     */
    public void addAtlasListener(final AtlasListener listener) {
        onAtlas.add(listener);
    }
    
    /**
     * Removes a {@link AtlasListener} from this <code>TrueTypeFont</code>.
     * 
     * @param listener The <code>AtlasListener</code> you wish to remove.
     * @return True if the listener was removed, false if the listener was not found.
     */
    public boolean removeAtlasListener(final AtlasListener listener) {
        return onAtlas.remove(listener);
    }
    
    @Override
    public TrueTypeText getText(T[][] glyphs, int kerning, ColorRGBA color, Align hAlign,
            VAlign vAlign) {
        return getText(glyphs, kerning, color, color, hAlign, vAlign);
    }
    
    public TrueTypeText getText(String text, int kerning, ColorRGBA color, ColorRGBA outlineColor) {
        return getText(text, kerning, color, outlineColor, Align.Left, VAlign.Top);
    }
    
    public TrueTypeText getText(T[][] glyphs, int kerning, ColorRGBA color, ColorRGBA outlineColor) {
        return getText(glyphs, kerning, color, outlineColor, Align.Left, VAlign.Top);
    }
    
    public TrueTypeText getText(String text, int kerning, ColorRGBA color, ColorRGBA outlineColor,
            Align hAlign, VAlign vAlign) {
        return getText(getGlyphMatrix(text), kerning, color, outlineColor, hAlign, vAlign);
    }
    
    public TrueTypeText getText(T[][] glyphs, int kerning, ColorRGBA color, ColorRGBA outlineColor,
            Align hAlign, VAlign vAlign) {
        Material mat;
        if (outline > 0) {
            mat = new Material(assetManager, "Common/MatDefs/TTF/TTF_BitmapOutlined.j3md");
            mat.setColor("Color", color);
            mat.setColor("Outline", outlineColor);
        } else {
            mat = new Material(assetManager, "Common/MatDefs/TTF/TTF_Bitmap.j3md");
            mat.setColor("Color", color);
        }
        
        TrueTypeText ttt = new TrueTypeText(this, glyphs, kerning, hAlign, vAlign, mat);
        mat.setTexture("Texture", atlas);
        
        return ttt;
    }
    
    @Override
    public TrueTypeText getText(T[][] glyphs, int kerning, Material material, Align hAlign,
            VAlign vAlign) {
        return new TrueTypeText(this, glyphs, kerning, hAlign, vAlign, material);
    }
    
    @Override
    public TrueTypeContainer getFormattedText(StringContainer stringContainer,
            ColorRGBA color) {
        return getFormattedText(stringContainer, color, color);
    }
    
    public TrueTypeContainer getFormattedText(StringContainer stringContainer,
            ColorRGBA color, ColorRGBA outlineColor) {
        Material mat;
        if (outline > 0) {
            mat = new Material(assetManager, "Common/MatDefs/TTF/TTF_BitmapOutlined.j3md");
            mat.setColor("Color", color);
            mat.setColor("Outline", outlineColor);
        } else {
            mat = new Material(assetManager, "Common/MatDefs/TTF/TTF_Bitmap.j3md");
            mat.setColor("Color", color);
        }
        
        TrueTypeBMPContainer ttc = new TrueTypeBMPContainer(stringContainer, mat);
        mat.setTexture("Texture", atlas);
        ttc.setLocalTranslation(stringContainer.getTextBox().x,
                stringContainer.getTextBox().y, 0);
        
        return ttc;
    }
    
    @Override
    public TrueTypeContainer getFormattedText(StringContainer stringContainer,
            Material material) {
        TrueTypeBMPContainer ttc = new TrueTypeBMPContainer(stringContainer, material);
        ttc.setLocalTranslation(stringContainer.getTextBox().x,
                stringContainer.getTextBox().y, 0);
        
        return ttc;
    }
    
    @Override
    public T[] getGlyphs(StringBuilder text) {
        T[] glyphs = (T[])new GlyphBMP[text.length()];
        LinkedList<CharToCreate> unCached = new LinkedList<CharToCreate>();
        
        for (int i = 0; i < text.length(); i++) {
            int codePoint = text.codePointAt(i);
            if (!canDisplay(codePoint) || invalidCharacters.contains(codePoint)) {
                codePoint = defaultCodePoint;
            }
            glyphs[i] = cache.get(codePoint);
            if (glyphs[i] == null) {
                if (cacheLock) {
                    glyphs[i] = cache.get(defaultCodePoint);
                } else
                    unCached.add(new CharToCreate(i, codePoint));
            }
        }
        
        if (!unCached.isEmpty()) {
            createGlyphs((LinkedList<CharToCreate>)unCached.clone());
            for (CharToCreate ctc : unCached) {
                glyphs[ctc.index] = cache.get(ctc.codePoint);
            }
        }
        
        return glyphs;
    }
    
    @Override
    public T[][] getGlyphMatrix(String text) {
        String[] strings = text.split("\n");
        T[][] lines;
        if (text.startsWith("\n")) {
            if (text.endsWith("\n")) {
                lines = (T[][])new GlyphBMP[strings.length + 2][];
                lines[0] = (T[])new GlyphBMP[0];
                lines[lines.length - 1] = (T[])new GlyphBMP[0];
                for (int i = 1; i < lines.length - 1; i++)
                    lines[i] = getGlyphs(strings[i - 1]);
            } else {
                lines = (T[][])new GlyphBMP[strings.length + 1][];
                lines[0] = (T[])new GlyphBMP[0];
                for (int i = 1; i < lines.length; i++)
                    lines[i] = getGlyphs(strings[i - 1]);
            }
        } else {
            lines = (T[][])new GlyphBMP[strings.length][];
            for (int i = 0; i < lines.length; i++)
                lines[i] = getGlyphs(strings[i]);
        }
        return lines;
    }
    
    /**
     * 
     * @return The size of the outline around bitmap text.
     */
    public int getOutline() {
        return outline;
    }
    
    /**
     * For internal use only. Doesn't actually resize the texture atlas itself.
     * This method is used to recalculate what the texture atlas' size should be
     * when it is eventually resized.
     * 
     * @see TrueTypeAWT#createGlyphs(java.util.List) 
     * @see TrueTypeSfntly#createGlyphs(java.util.List)
     */
    protected void resizeAtlas() {
        if (fixedResolution) {
            atlasWidth = this.maxTexRes;
            atlasHeight = this.maxTexRes;
        } else {
            atlasWidth += (atlasWidth + resizeWidth > maxTexRes) ? 0 : resizeWidth;
            atlasHeight += (atlasHeight + charHeight > maxTexRes) ? 0 : charHeight;
        }
        
        int numNewLines = (int)FastMath.floor((float)atlasHeight / charHeight) - atlasLines.size();
        for (int i = 0; i < numNewLines; i++) {
            atlasLines.add(new AtlasLine());
        }
        
        atlasResized = true;
    }
    
    /**
     * For internal use only. This method is used to either create or re-create/re-size
     * the texture atlas.
     * 
     * @see TrueTypeAWT#createGlyphs(java.util.List) 
     * @see TrueTypeSfntly#createGlyphs(java.util.List)
     */
    protected abstract void createAtlas();
    
    /**
     * For internal use only. This method is used to either create or re-create/re-size
     * the texture atlas.
     * 
     * @see TrueTypeAWT#createGlyphs(java.util.List) 
     * @see TrueTypeSfntly#createGlyphs(java.util.List)
     */
    protected abstract void createAtlasOutlined();
    
    /**
     * Recreates the texture atlas.
     */
    public void reloadTexture() {
        int oldWidth = (atlas != null) ? atlas.getImage().getWidth() : 0;
        int oldHeight = (atlas != null) ? atlas.getImage().getHeight() : 0;

        if (outline > 0) {
            createAtlasOutlined();
        } else
            createAtlas();

        for (AtlasListener listener : onAtlas) {
            listener.mod(assetManager, oldWidth, oldHeight, atlasWidth,
                    atlasHeight, this);
        }
    }
    
    /**
     * A helper class used in determining a new characters position in the texture
     * atlas and if said character can fit on a particular line in that atlas.
     * 
     * @see TrueTypeAWT#createGlyphs(java.util.List) 
     * @see TrueTypeSfntly#createGlyphs(java.util.List) 
     * @see TrueTypeBMP#resizeAtlas() 
     * 
     * @author Adam T. Ryder http://1337atr.weebly.com
     */
    protected class AtlasLine {
        private int currentX = 0;
        
        public boolean canFit(int cWidth) {
            return (atlasWidth - currentX) - cWidth >= 0;
        }
        
        public void addChar(int cWidth) {
            currentX += cWidth;
        }
        
        public int getX() {
            return currentX;
        }
    }
    
    @Override
    public void finalize() throws Throwable {
        if (!(this instanceof TrueTypeSfntly) && atlas != null) {
            atlas.getImage().dispose();
            if (!NativeObjectManager.UNSAFE) {
                for (ByteBuffer buf : atlas.getImage().getData()) {
                    BufferUtils.destroyDirectBuffer(buf);
                }
            }
        }
        
        super.finalize();
    }
}
