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

import com.atr.jme.font.util.StringContainer;
import com.jme3.material.Material;
import com.jme3.scene.Node;

/**
 * A <code>Node</code> that builds a <code>Mesh</code> to display
 * text constrained by parameters set via a {@link StringContainer}
 * 
 * For {@link com.atr.jme.font.TrueTypeBMP} fonts this will have one child
 * <code>Geometry</code>. For {@link com.atr.jme.font.TrueTypeMesh} fonts this will
 * have one or more children which will be the supplied text
 * split into multiple geometries should the vertex count exceed
 * <code>Short.MAX_VALUE</code>.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 * 
 * @see StringContainer
 */
public abstract class TrueTypeContainer extends Node {
    protected StringContainer stringContainer;
    protected Material material;
    
    /**
     * Constructs a new instance of <code>TrueTypecontainer</code> it is
     * recommended that you use {@link com.atr.jme.font.TrueTypeFont#getFormattedText(com.atr.jme.font.util.StringContainer, com.jme3.math.ColorRGBA)}
     * or {@link com.atr.jme.font.TrueTypeFont#getFormattedText(com.atr.jme.font.util.StringContainer, com.jme3.material.Material)}
     * to create an instance of this class.
     * 
     * @param stringContainer
     * @param material 
     * 
     * @see com.atr.jme.font.TrueTypeFont#getFormattedText(com.atr.jme.font.util.StringContainer, com.jme3.math.ColorRGBA) 
     * @see com.atr.jme.font.TrueTypeFont#getFormattedText(com.atr.jme.font.util.StringContainer, com.jme3.material.Material) 
     */
    public TrueTypeContainer(StringContainer stringContainer, Material material) {
        super("TrueTypeText");
        this.stringContainer = stringContainer;
        this.material = material;
    }
    
    /**
     * The actual width of the text from the origin in the upper left
     * corner to the right side of the last character in the longest line.
     * This will equal zero until the first call to {@link StringContainer#getLines()}.
     * 
     * @return The width in pixels.
     */
    public float getTextWidth() {
        return stringContainer.getTextWidth();
    }
    
    /**
     * The actual height of the text from the origin in the upper left
     * corner to the lowest point of the lowest character on the last line.
     * This will equal zero until the first call to {@link StringContainer#getLines()}.
     * 
     * @return The height in pixels.
     */
    public float getTextHeight() {
        return stringContainer.getTextHeight();
    }
    
    /**
     * The width of the <code>Rectangle</code> used to constrain this text.
     * 
     * @return The width of the <code>Rectangle</code> that constrains this text.
     */
    public float getWidth() {
        return stringContainer.getTextBox().width;
    }
    
    /**
     * The height of the <code>Rectangle</code> used to constrain this text.
     * 
     * @return The height of the <code>Rectangle</code> that constrains this text.
     */
    public float getHeight() {
        return stringContainer.getTextBox().height;
    }
    
    @Override
    public void setMaterial(Material material) {
        super.setMaterial(material);
        this.material = material;
    }
    
    /**
     * The <code>Material</code> used to render this text.
     * 
     * @return The <code>Material</code> used to render this text.
     */
    public Material getMaterial() {
        return material;
    }
    
    /**
     * Get the {@link StringContainer} associated with this
     * <code>TrueTypeContainer</code>.
     * 
     * @return The {@link StringContainer}
     * 
     * @see StringContainer
     */
    public StringContainer getStringContainer() {
        return stringContainer;
    }
    
    /**
     * Sets the {@link StringContainer} for this <code>TrueTypeContainer</code>
     * and calls {@link #updateGeometry()}.
     * 
     * @param container The new {@link StringContainer}
     * 
     * @see StringContainer
     * @see #updateGeometry() 
     */
    public void setStringContainer(StringContainer container) {
        stringContainer = container;
        updateGeometry();
    }
    
    /**
     * Sets the text to display. Note that this does not automatically update
     * the geometry. To actually display the set text use {@link #updateGeometry()}
     * after using this method.
     * 
     * @param text 
     */
    public void setText(String text) {
        stringContainer.setText(text);
    }
    
    /**
     * Gets the text this <code>TrueTypeContainer</code> displays.
     * 
     * @return The text displayed by this <code>TrueTypeContainer</code>
     */
    public String getText() {
        return stringContainer.getText();
    }
    
    /**
     * Used to update the underlying <code>Mesh</code> after any parameters
     * such as {@link #setText(java.lang.String)} or any of the settings
     * for the <code>StringContainer</code> used by this
     * <code>TrueTypeContainer</code> have been changed.
     */
    public abstract void updateGeometry();
}
