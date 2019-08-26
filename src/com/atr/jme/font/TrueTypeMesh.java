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

import com.atr.jme.font.glyph.GlyphMesh;
import com.atr.jme.font.sfntly.AnchorTable;
import com.atr.jme.font.sfntly.NullAnchorTable;
import com.atr.jme.font.shape.MeshGlyf;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.shape.TrueTypeMeshContainer;
import com.atr.jme.font.shape.TrueTypeMeshText;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.StringContainer.Align;
import com.atr.jme.font.util.StringContainer.VAlign;
import com.atr.jme.font.util.Style;
import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.Table;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.sfntly.table.core.FontHeaderTable;
import com.google.typography.font.sfntly.table.core.HorizontalHeaderTable;
import com.google.typography.font.sfntly.table.core.HorizontalMetricsTable;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.GlyphTable;
import com.google.typography.font.sfntly.table.truetype.LocaTable;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of {@link TrueTypeFont} that renders fonts by triangulating a
 * <code>Mesh</code> from the contours of each glyph.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 * 
 * @see TrueTypeFont
 */
public class TrueTypeMesh extends TrueTypeFont<GlyphMesh, TrueTypeMeshText> {
    private final Font font;
    
    private final CMap characterMap;
    private final LocaTable loca;
    private final GlyphTable glyphs;
    private final HorizontalMetricsTable hmtx;
    private final AnchorTable ankr;
    
    private final float pointScale;
    
    private final float italic;
    private float italicRef;
    
    private final float maxCharX;
    private final float maxCharY;
    private final float minCharX;
    private final float minCharY;
    
    private boolean aa = true;
    
    public TrueTypeMesh(AssetManager assetManager, Font font, Style style, int pointSize,
            int screenDensity, String preload) {
        super(assetManager, style, pointSize, screenDensity);
        this.font = font;
        
        CMapTable cmapTable = font.getTable(Tag.cmap);
        CMap cmap = cmapTable.cmap(Font.PlatformId.Windows.value(),
                Font.WindowsEncodingId.UnicodeUCS4.value());
        if (cmap == null) {
            characterMap = cmapTable.cmap(Font.PlatformId.Windows.value(),
                    Font.WindowsEncodingId.UnicodeUCS2.value());
        } else {
            characterMap = cmap;
        }
        
        loca = font.getTable(Tag.loca);
        glyphs = font.getTable(Tag.glyf);
        hmtx = font.getTable(Tag.hmtx);
        
        FontHeaderTable head = font.getTable(Tag.head);
        int maxX = head.xMax();
        int minX = head.xMin();
        int maxY = head.yMax();
        int minY = head.yMin();
        
        pointScale = (pointSize * dpi) / (72f * head.unitsPerEm());
        italicRef = maxY * pointScale;
        
        switch(style) {
            case Italic:
                italic = -(float)Math.sin(-0.25f) * italicRef;
                italicRef = (float)Math.cos(-0.25f) * italicRef;
                break;
            case Bold:
                italic = 0;
                break;
            case BoldItalic:
                italic = -(float)Math.sin(-0.25f) * italicRef;
                italicRef = (float)Math.cos(-0.25f) * italicRef;
                break;
            default:
                italic = 0;
        }
        
        int w = maxX - minX;
        int h = maxY - minY;
        maxCharX = (int)Math.ceil((maxX + (w * 0.1f)) * pointScale);
        maxCharY = (int)Math.ceil((maxY + (h * 0.1f)) * pointScale) + 10f;
        minCharX = (int)Math.floor((minX - (w * 0.1f)) * pointScale) - 10f;
        minCharY = (int)Math.floor((minY - (h * 0.1f)) * pointScale) - 10f;
        
        HorizontalHeaderTable hhea = font.getTable(Tag.hhea);
        ascender = Math.round(hhea.ascender() * pointScale);
        descender = Math.round(-hhea.descender() * pointScale);
        lineGap = Math.round(hhea.lineGap() * pointScale);
        
        lineHeight = ascender + descender + lineGap;
        
        Table t = font.getTable(Tag.intValue(new byte[]{'a', 'n', 'k', 'r'}));
        if (t != null) {
            ankr = new AnchorTable(t);
        } else
            ankr = new NullAnchorTable();
        
        getGlyphs(new StringBuilder().appendCodePoint(defaultCodePoint).append(" ").append(preload));
    }
    
    /**
     * Set whether or not text created with this <code>TrueTypeMesh</code>
     * will use shader based anti-aliasing. Note this only effects newly
     * created text, text that has already been created with this instance
     * will not be effected.
     * 
     * @param aa 
     */
    public void setAA(boolean aa) {
        this.aa = aa;
    }
    
    /**
     * Gets if this <code>TrueTypeMesh</code> activates shader based
     * anti-aliasing on text.
     * 
     * @return 
     */
    public boolean isAA() {
        return aa;
    }
    
    public Font getFont() {
        return font;
    }
    
    public float getMaxCharX() {
        return maxCharX;
    }
    
    public float getMaxCharY() {
        return maxCharY;
    }
    
    public float getMinCharX() {
        return minCharX;
    }
    
    public float getMinCharY() {
        return minCharY;
    }
    
    public float getPointScale() {
        return pointScale;
    }
    
    public float getItalicRef() {
        return italicRef;
    }
    
    public float getItalicAngle() {
        return italic;
    }
    
    @Override
    public boolean canDisplay(int codePoint) {
        return characterMap.glyphId(codePoint) != 0;
    }
    
    /**
     * Gets the GlyphID of a glyph associated with a particular character.
     * 
     * @param codePoint The Unicode code point of the requested character.
     * @return The ID which can be used to lookup a glyph in the truetype
     * font file.
     * 
     * @see #getGlyph(int)
     */
    public int getGlyphID(int codePoint) {
        return characterMap.glyphId(codePoint);
    }
    
    /**
     * Gets a <code>com.google.typography.font.sfntly.table.truetype.Glyph</code> from
     * the truetype font file.
     * 
     * @param glyphID The ID of the requested glyph.
     * @return The requested glyph.
     * 
     * @see #getGlyphID(int)
     */
    public Glyph getGlyph(int glyphID) {
        return glyphs.glyph(loca.glyphOffset(glyphID), loca.glyphLength(glyphID));
    }
    
    @Override
    public TrueTypeMeshText getText(GlyphMesh[][] glyphs, int kerning, ColorRGBA color, Align hAlign,
            VAlign vAlign) {
        Material mat = new Material(assetManager, "Common/MatDefs/TTF/TTF.j3md");
        mat.setColor("Color", color);
        TrueTypeMeshText ttmt = getText(glyphs, kerning, mat, hAlign, vAlign);
        ttmt.setAA(aa);
        return ttmt;
    }
    
    @Override
    public TrueTypeMeshText getText(GlyphMesh[][] glyphs, int kerning, Material material, Align hAlign,
            VAlign vAlign) {
        TrueTypeMeshText ttmt = new TrueTypeMeshText(this, glyphs, kerning, hAlign, vAlign, material);
        return ttmt;
    }
    
    @Override
    public TrueTypeContainer getFormattedText(StringContainer stringContainer,
            ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/TTF/TTF.j3md");
        mat.setColor("Color", color);
        TrueTypeMeshContainer ttmc = (TrueTypeMeshContainer)getFormattedText(stringContainer, mat);
        ttmc.setAA(aa);
        return ttmc;
    }
    
    @Override
    public TrueTypeContainer getFormattedText(StringContainer stringContainer,
            Material material) {
        TrueTypeMeshContainer ttmc = new TrueTypeMeshContainer(stringContainer, material);
        ttmc.setLocalTranslation(stringContainer.getTextBox().x,
                stringContainer.getTextBox().y, 0);
        return ttmc;
    }
    
    @Override
    public GlyphMesh[] getGlyphs(StringBuilder text) {
        GlyphMesh[] glyphs = new GlyphMesh[text.length()];
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
    public GlyphMesh[][] getGlyphMatrix(String text) {
        String[] strings = text.split("\n");
        GlyphMesh[][] lines;
        if (text.startsWith("\n")) {
            if (text.endsWith("\n")) {
                lines = new GlyphMesh[strings.length + 2][];
                lines[0] = new GlyphMesh[0];
                lines[lines.length - 1] = new GlyphMesh[0];
                for (int i = 1; i < lines.length - 1; i++)
                    lines[i] = getGlyphs(strings[i - 1]);
            } else {
                lines = new GlyphMesh[strings.length + 1][];
                lines[0] = new GlyphMesh[0];
                for (int i = 1; i < lines.length; i++)
                    lines[i] = getGlyphs(strings[i - 1]);
            }
        } else {
            lines = new GlyphMesh[strings.length][];
            for (int i = 0; i < lines.length; i++)
                lines[i] = getGlyphs(strings[i]);
        }
        return lines;
    }
    
    @Override
    protected void createGlyphs(List<CharToCreate> characters) {
        for (CharToCreate ctc : characters) {
            if (cache.containsKey(ctc.codePoint))
                continue;
            int gid = getGlyphID(ctc.codePoint);
            MeshGlyf mg = new MeshGlyf(this, ankr, getGlyph(gid));
            GlyphMesh gm = new GlyphMesh(ctc.codePoint, mg, hmtx.advanceWidth(gid) * pointScale,
                    0);
            cache.put(ctc.codePoint, gm);
        }
    }
}
