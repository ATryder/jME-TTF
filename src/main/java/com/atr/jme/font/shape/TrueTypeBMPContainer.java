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

import com.atr.jme.font.TrueTypeBMP;
import com.atr.jme.font.glyph.Glyph;
import com.atr.jme.font.glyph.GlyphBMP;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.StringContainer.Align;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * An implementation of {@link TrueTypeContainer} used for
 * {@link com.atr.jme.font.TrueTypeBMP} fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeBMPContainer extends TrueTypeContainer {
    public TrueTypeBMPContainer(StringContainer stringContainer, Material material) {
        super(stringContainer, material);
        attachChild(new Geometry("TrueTypeContainerBMP", new BMPMesh()));
        setMaterial(material);
    }
    
    @Override
    public void updateGeometry() {
        ((BMPMesh)((Geometry)getChild(0)).getMesh()).updateMesh();
    }
    
    /**
     * The actual mesh that the text is rendered with.
     * 
     * @author Adam T. Ryder
     */
    private class BMPMesh extends Mesh {
        private BMPMesh() {
            updateMesh();
        }
        
        @SuppressWarnings("rawtypes")
        public void updateMesh() {
            Glyph[][] lines = stringContainer.getLines();
            TrueTypeBMP ttb = (TrueTypeBMP)stringContainer.getFont();

            float heightOffset = 0;
            switch(stringContainer.getVerticalAlignment()) {
                case Bottom:
                    heightOffset = stringContainer.getTextBox().height
                            - stringContainer.getTextHeight();
                    break;
                case Center:
                    float halfBox = stringContainer.getTextBox().height / 2;
                    float halfHeight = stringContainer.getTextHeight() / 2;
                    heightOffset = halfBox - halfHeight;
                    break;
            }

            FloatBuffer verts = BufferUtils.createFloatBuffer(stringContainer.getNumNonSpaceCharacters() * 12);
            FloatBuffer tex1 = BufferUtils.createFloatBuffer(stringContainer.getNumNonSpaceCharacters() * 8);
            FloatBuffer tex2 = BufferUtils.createFloatBuffer(tex1.capacity());
            FloatBuffer tex3 = BufferUtils.createFloatBuffer(tex2.capacity());
            FloatBuffer tex4 = BufferUtils.createFloatBuffer(tex3.capacity());
            ShortBuffer indices = BufferUtils.createShortBuffer(stringContainer.getNumNonSpaceCharacters() * 6);

            float[] widths = stringContainer.getLineWidths();
            int padding = ttb.padding / 2;
            float currentLineHeight = stringContainer.getFont().getActualAscender();
            short currentIndex = 0;
            int lineNum = 0;
            for (Glyph[] line : lines) {
                if (line.length == 0)
                    continue;

                int currentX = 0;
                Vector2f lineY = stringContainer.getLineHeights()[lineNum];
                float lineHeight = lineY.x - lineY.y;
                for (Glyph glyf : line) {
                    GlyphBMP glyph = (GlyphBMP)glyf;
                    if (glyph.codePoint == ' ') {
                        currentX += glyph.getXAdvance() + stringContainer.getKerning();
                        continue;
                    }

                    float widthOffset = 0;
                    switch(stringContainer.getAlignment()) {
                        case Right:
                            widthOffset = stringContainer.getTextBox().width
                                    - widths[lineNum];
                            break;
                        case Center:
                            float halfBox = stringContainer.getTextBox().width / 2;
                            float halfWidth = widths[lineNum] / 2;
                            widthOffset = halfBox - halfWidth;
                            break;
                    }

                    //Lower left
                    float x = currentX + glyph.getXOffset();
                    float y = glyph.getYOffset();
                    verts.put(((x - padding) * ttb.getScale()) + widthOffset);
                    verts.put((((y - padding) - currentLineHeight) * ttb.getScale()) - heightOffset);
                    verts.put(0);

                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getLeftU());
                    tex1.put(glyph.getBottomV());
                    tex2.put(0);
                    tex2.put(0);
                    tex3.put(x / widths[lineNum]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (stringContainer.getAlignment() == Align.Center) {
                        tex4.put((x + ((getWidth() / 2) - (widths[lineNum] / 2))) / getWidth());
                    } else
                        tex4.put((x + (getWidth() - widths[lineNum])) / getWidth());
                    tex4.put(1f - ((y - currentLineHeight) / -getHeight()));

                    //Lower right
                    x = currentX + glyph.getXOffset() + glyph.atlasWidth;
                    y = glyph.getYOffset();
                    verts.put((x * ttb.getScale()) + widthOffset);
                    verts.put(((y - padding - currentLineHeight) * ttb.getScale()) - heightOffset);
                    verts.put(0);

                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getRightU());
                    tex1.put(glyph.getBottomV());
                    tex2.put(1);
                    tex2.put(0);
                    tex3.put(x / widths[lineNum]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (stringContainer.getAlignment() == Align.Center) {
                        tex4.put((x + ((getWidth() / 2) - (widths[lineNum] / 2))) / getWidth());
                    } else
                        tex4.put((x + (getWidth() - widths[lineNum])) / getWidth());
                    tex4.put(1f - ((y - currentLineHeight) / -getHeight()));

                    //Upper left
                    x = currentX + glyph.getXOffset();
                    y = glyph.getYOffset() + glyph.atlasHeight;
                    verts.put(((x - padding) * ttb.getScale()) + widthOffset);
                    verts.put(((y - currentLineHeight) * ttb.getScale()) - heightOffset);
                    verts.put(0);

                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getLeftU());
                    tex1.put(glyph.getTopV());
                    tex2.put(0);
                    tex2.put(1);
                    tex3.put(x / widths[lineNum]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (stringContainer.getAlignment() == Align.Center) {
                        tex4.put((x + ((getWidth() / 2) - (widths[lineNum] / 2))) / getWidth());
                    } else
                        tex4.put((x + (getWidth() - widths[lineNum])) / getWidth());
                    tex4.put(1f - ((y - currentLineHeight) / -getHeight()));

                    //Upper right
                    x = currentX + glyph.getXOffset() + glyph.atlasWidth;
                    y = glyph.getYOffset() + glyph.atlasHeight;
                    verts.put((x * ttb.getScale()) + widthOffset);
                    verts.put(((y - currentLineHeight) * ttb.getScale()) - heightOffset);
                    verts.put(0);

                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getRightU());
                    tex1.put(glyph.getTopV());
                    tex2.put(1);
                    tex2.put(1);
                    tex3.put(x / widths[lineNum]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (stringContainer.getAlignment() == Align.Center) {
                        tex4.put((x + ((getWidth() / 2) - (widths[lineNum] / 2))) / getWidth());
                    } else
                        tex4.put((x + (getWidth() - widths[lineNum])) / getWidth());
                    tex4.put(1f - ((y - currentLineHeight) / -getHeight()));

                    indices.put(currentIndex);
                    indices.put((short)(currentIndex + 1));
                    indices.put((short)(currentIndex + 2));
                    indices.put((short)(currentIndex + 2));
                    indices.put((short)(currentIndex + 1));
                    indices.put((short)(currentIndex + 3));

                    currentX += glyph.getXAdvance() + stringContainer.getKerning();
                    currentIndex += 4;
                }

                currentLineHeight += stringContainer.getFont().getActualLineHeight();
                lineNum++;
            }

            setBuffer(VertexBuffer.Type.Position, 3, verts);
            setBuffer(VertexBuffer.Type.TexCoord, 2, tex1);
            setBuffer(VertexBuffer.Type.TexCoord2, 2, tex2);
            setBuffer(VertexBuffer.Type.TexCoord3, 2, tex3);
            setBuffer(VertexBuffer.Type.TexCoord4, 2, tex4);
            setBuffer(VertexBuffer.Type.Index, 3, indices);

            clearCollisionData();
            updateBound();
        }
    }
}
