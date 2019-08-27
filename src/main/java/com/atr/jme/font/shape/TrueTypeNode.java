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
package com.atr.jme.font.shape;

import com.atr.jme.font.glyph.Glyph;
import com.atr.jme.font.util.StringContainer;
import com.jme3.material.Material;
import com.jme3.scene.Node;

/**
 * A <code>Node</code> that builds a <code>Mesh</code> to display text.
 * 
 * For {@link com.atr.jme.font.TrueTypeBMP} fonts this will have one child
 * <code>Geometry</code>. For {@link com.atr.jme.font.TrueTypeMesh} fonts this will
 * have one or more children which will be the supplied text
 * split into multiple geometries should the vertex count exceed
 * <code>Short.MAX_VALUE</code>.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class TrueTypeNode<T extends Glyph> extends Node {
    protected T[][] glyphs;
    
    protected int kerning;
    
    protected StringContainer.Align hAlign;
    protected StringContainer.VAlign vAlign;
    
    protected Material mat;
    
    protected float width;
    protected float height;
    
    /**
     * Constructs a new instance of <code>TrueTypeNode</code> it is recommended
     * that you use
     * {@link com.atr.jme.font.TrueTypeFont#getText(java.lang.String, int, com.jme3.math.ColorRGBA, com.atr.jme.font.util.StringContainer.Align, com.atr.jme.font.util.StringContainer.VAlign)}
     * or associated methods to obtain an instance of this class.
     * 
     * @param glyphs
     * @param kerning
     * @param hAlign
     * @param vAlign
     * @param material 
     * 
     * @see com.atr.jme.font.TrueTypeFont#getText(java.lang.String, int, com.jme3.math.ColorRGBA) 
     * @see com.atr.jme.font.TrueTypeFont#getText(java.lang.String, int, com.jme3.material.Material) 
     * @see com.atr.jme.font.TrueTypeFont#getText(java.lang.String, int, com.jme3.math.ColorRGBA, com.atr.jme.font.util.StringContainer.Align, com.atr.jme.font.util.StringContainer.VAlign) 
     * @see com.atr.jme.font.TrueTypeFont#getText(java.lang.String, int, com.jme3.material.Material, com.atr.jme.font.util.StringContainer.Align, com.atr.jme.font.util.StringContainer.VAlign)
     */
    public TrueTypeNode(T[][] glyphs, int kerning,
            StringContainer.Align hAlign, StringContainer.VAlign vAlign,
            Material material) {
        super("TrueTypeNode");
        this.glyphs = glyphs;
        this.kerning = kerning;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.mat = material;
    }
    
    /**
     * Gets the width of the displayed text.
     * 
     * @return The width of the text.
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the displayed text.
     * 
     * @return The height of the text.
     */
    public float getHeight() {
        return height;
    }
    
    @Override
    public void setMaterial(Material material) {
        super.setMaterial(material);
        mat = material;
    }
    
    /**
     * Gets the material used to render this text.
     * 
     * @return The material used to render this text.
     */
    public Material getMaterial() {
        return mat;
    }
    
    /**
     * Sets the horizontal alignment.
     * 
     * @param hAlign 
     * 
     * @see #updateGeometry() 
     */
    public void setHorizontalAlignment(StringContainer.Align hAlign) {
        this.hAlign = hAlign;
    }
    
    /**
     * Gets the horizontal alignment.
     * 
     * @return 
     */
    public StringContainer.Align getHorizontalAlignment() {
        return hAlign;
    }
    
    /**
     * Sets the vertical alignment.
     * 
     * @return 
     */
    public StringContainer.VAlign getVerticalAlignment() {
        return vAlign;
    }
    
    /**
     * Gets the vertical alignment.
     * 
     * @param vAlign 
     * 
     * @see #updateGeometry() 
     */
    public void setVerticalAlignment(StringContainer.VAlign vAlign) {
        this.vAlign = vAlign;
    }
    
    /**
     * Sets the amount of additional spacing between characters.
     * 
     * @param kerning 
     * 
     * @see #updateGeometry() 
     */
    public void setKerning(int kerning) {
        this.kerning = kerning;
    }
    
    /**
     * Gets the amount of additional spacing between characters.
     * 
     * @return 
     */
    public int getKerning() {
        return kerning;
    }
    
    /**
     * Sets the glyphs to be displayed. Each element in the root array
     * represents one line of text.
     * 
     * @param glyphs 
     * 
     * @see #updateGeometry() 
     */
    public void setGlyphs(T[][] glyphs) {
        this.glyphs = glyphs;
    }
    
    /**
     * Gets the glyphs currently displayed by this <code>TrueTypeNode</code>.
     * 
     * @return 
     */
    public T[][] getGlyphs() {
        return glyphs;
    }
    
    /**
     * Sets the text to display.
     * 
     * @param Text 
     * 
     * @see #updateGeometry() 
     */
    public abstract void setText(String Text);
    
    /**
     * Gets the text displayed by this <code>TrueTypeNode</code>
     * 
     * @return 
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < glyphs.length; i++) {
            if (glyphs[i].length == 0) {
                sb.append("\n");
                continue;
            }
            
            for (Glyph glyph : glyphs[i])
                sb.appendCodePoint(glyph.codePoint);
            if (i < glyphs.length - 1)
                sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Used to update the underlying <code>Mesh</code> after any parameters
     * such as {@link #setText(java.lang.String)} have been changed.
     */
    public abstract void updateGeometry();
}
