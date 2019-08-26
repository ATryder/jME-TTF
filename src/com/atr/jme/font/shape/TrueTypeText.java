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
import com.atr.jme.font.glyph.GlyphBMP;
import com.atr.jme.font.util.StringContainer.Align;
import com.atr.jme.font.util.StringContainer.VAlign;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * An implementation of {@link TrueTypeNode} used for
 * {@link com.atr.jme.font.TrueTypeBMP} fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeText extends TrueTypeNode<GlyphBMP> {
    TrueTypeBMP ttb;
    
    public TrueTypeText(TrueTypeBMP ttb, GlyphBMP[][] glyphs, int kerning,
            Align hAlign, VAlign vAlign, Material material) {
        super(glyphs, kerning, hAlign, vAlign, material);
        
        this.ttb = ttb;
        
        attachChild(new Geometry("TrueTypeText", new BMPMesh()));
        setMaterial(material);
    }
    
    @Override
    public void setText(String text) {
        setGlyphs(ttb.getGlyphMatrix(text));
    }
    
    @Override
    public void updateGeometry() {
        ((BMPMesh)((Geometry)getChild(0)).getMesh()).updateMesh();
    }
    
    /**
     * The actual mesh the text is rendered with.
     * 
     * @author Adam T. Ryder
     */
    private class BMPMesh extends Mesh {
        private BMPMesh() {
            updateMesh();
        }
        
        public void updateMesh() {
            int numChars = 0;
            float[] widths = new float[glyphs.length];
            Vector2f[] lineHeights = new Vector2f[glyphs.length];
            height = ttb.getScaledAscender() + ttb.getScaledDescender();
            if (glyphs.length > 1)
                height += (glyphs.length - 1) * ttb.getScaledLineHeight();
            width = 0;

            int count = 0;
            for (GlyphBMP[] line : glyphs) {
                if (line.length == 0) {
                    widths[count++] = 0;
                    continue;
                }
                float w = 0;
                Vector2f lineHeight = new Vector2f(Float.MIN_VALUE, Float.MAX_VALUE);
                for (GlyphBMP glyph : line) {
                    w += (glyph.getXAdvance() + kerning) * ttb.getScale();
                    if (glyph.getAscender() > lineHeight.x)
                        lineHeight.x = glyph.getAscender();
                    if (glyph.getDescender() < lineHeight.y)
                        lineHeight.y = glyph.getDescender();
                    if (glyph.codePoint != ' ') {
                        numChars++;
                    }
                }
                w -= (line[line.length - 1].getXAdvance() + kerning) * ttb.getScale();
                w += line[line.length - 1].getRight() * ttb.getScale();
                lineHeights[count] = lineHeight.multLocal(ttb.getScale());
                widths[count++] = w;
                if (w > width)
                    width = w;
            }

            FloatBuffer verts = BufferUtils.createFloatBuffer(numChars * 12);
            FloatBuffer tex1 = BufferUtils.createFloatBuffer(numChars * 8);
            FloatBuffer tex2 = BufferUtils.createFloatBuffer(tex1.capacity());
            FloatBuffer tex3 = BufferUtils.createFloatBuffer(tex2.capacity());
            FloatBuffer tex4 = BufferUtils.createFloatBuffer(tex3.capacity());
            ShortBuffer indices = BufferUtils.createShortBuffer(numChars * 6);

            float currentY = ttb.getScaledAscender();
            float yOffset = 0;
            switch (vAlign) {
                case Center:
                    yOffset = height / 2;
                    break;
                case Bottom:
                    yOffset = height;
            }
            int padding = ttb.padding / 2;
            short currentIndex = 0;
            count = 0;
            for (GlyphBMP[] line : glyphs) {
                int currentX = 0;
                float xOffset = 0;
                switch (hAlign) {
                    case Center:
                        xOffset = -widths[count] / 2;
                        break;
                    case Right:
                        xOffset = width - widths[count];
                }
                
                Vector2f lineY = lineHeights[count];
                float lineHeight = lineY == null ? 0 : lineY.x - lineY.y;
                for (GlyphBMP glyph : line) {
                    if (glyph.codePoint == ' ') {
                        currentX += glyph.getXAdvance() + kerning;
                        continue;
                    }
                    float w = glyph.getWidth();
                    float h = glyph.getHeight();

                    //lower left
                    float x = currentX + glyph.getXOffset();
                    float y = glyph.getYOffset();
                    verts.put(((x - padding) * ttb.getScale()) + xOffset);
                    verts.put(((y - padding) * ttb.getScale()) + yOffset - currentY);
                    verts.put(0);
                    
                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getLeftU());
                    tex1.put(glyph.getBottomV());
                    tex2.put(0);
                    tex2.put(0);
                    tex3.put(x / widths[count]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (hAlign == Align.Center) {
                        tex4.put((x + ((width / 2) - (widths[count] / 2))) / width);
                    } else
                        tex4.put((x + xOffset) / width);
                    tex4.put(1f - ((y - currentY) / -height));

                    //lower right
                    x = currentX + glyph.getXOffset() + glyph.atlasWidth;
                    y = glyph.getYOffset();
                    verts.put((x * ttb.getScale()) + xOffset);
                    verts.put(((y - padding) * ttb.getScale()) + yOffset - currentY);
                    verts.put(0);
                    
                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getRightU());
                    tex1.put(glyph.getBottomV());
                    tex2.put(1);
                    tex2.put(0);
                    tex3.put(x / widths[count]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (hAlign == Align.Center) {
                        tex4.put((x + ((width / 2) - (widths[count] / 2))) / width);
                    } else
                        tex4.put((x + xOffset) / width);
                    tex4.put(1f - ((y - currentY) / -height));

                    //Upper left
                    x = currentX + glyph.getXOffset();
                    y = glyph.getYOffset() + glyph.atlasHeight;
                    verts.put(((x - padding) * ttb.getScale()) + xOffset);
                    verts.put((y * ttb.getScale()) + yOffset - currentY);
                    verts.put(0);
                    
                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getLeftU());
                    tex1.put(glyph.getTopV());
                    tex2.put(0);
                    tex2.put(1);
                    tex3.put(x / widths[count]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (hAlign == Align.Center) {
                        tex4.put((x + ((width / 2) - (widths[count] / 2))) / width);
                    } else
                        tex4.put((x + xOffset) / width);
                    tex4.put(1f - ((y - currentY) / -height));

                    //Upper right
                    x = currentX + glyph.getXOffset() + glyph.atlasWidth;
                    y = glyph.getYOffset() + glyph.atlasHeight;
                    verts.put((x * ttb.getScale()) + xOffset);
                    verts.put((y * ttb.getScale()) + yOffset - currentY);
                    verts.put(0);
                    
                    x *= ttb.getScale();
                    y *= ttb.getScale();
                    tex1.put(glyph.getRightU());
                    tex1.put(glyph.getTopV());
                    tex2.put(1);
                    tex2.put(1);
                    tex3.put(x / widths[count]);
                    tex3.put((y - lineY.y) / lineHeight);
                    if (hAlign == Align.Center) {
                        tex4.put((x + ((width / 2) - (widths[count] / 2))) / width);
                    } else
                        tex4.put((x + xOffset) / width);
                    tex4.put(1f - ((y - currentY) / -height));

                    indices.put(currentIndex);
                    indices.put((short)(currentIndex + 1));
                    indices.put((short)(currentIndex + 2));
                    indices.put((short)(currentIndex + 2));
                    indices.put((short)(currentIndex + 1));
                    indices.put((short)(currentIndex + 3));

                    currentX += glyph.getXAdvance() + kerning;
                    currentIndex += 4;
                }

                count++;
                currentY += ttb.getScaledLineHeight();
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
