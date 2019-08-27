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

import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.glyph.Glyph;
import com.atr.jme.font.glyph.GlyphMesh;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.StringContainer.Align;
import static com.atr.jme.font.util.StringContainer.Align.Center;
import static com.atr.jme.font.util.StringContainer.Align.Right;
import static com.atr.jme.font.util.StringContainer.VAlign.Bottom;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

/**
 * An implementation of {@link TrueTypeContainer} used for
 * {@link com.atr.jme.font.TrueTypeMesh} fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeMeshContainer extends TrueTypeContainer {
    
    public TrueTypeMeshContainer(StringContainer stringContainer, Material material) {
        super(stringContainer, material);
        
        updateGeometry();
    }
    
    public void setAA(boolean aa) {
        if (material.getParam("useAA") == null)
            return;
        material.setBoolean("useAA", aa);
    }
    
    @Override
    public void updateGeometry() {
        detachAllChildren();
        TrueTypeMesh ttm = (TrueTypeMesh)stringContainer.getFont();
        
        Glyph[][] lines = stringContainer.getLines();
        
        int startIndex = 0;
        int startLine = 0;
        int numVerts = 0;
        int totalVerts = 0;
        int lineNum = 0;
        int index = 0;
        LinkedList<TextSnippet> snippets = new LinkedList<TextSnippet>();
        for (Glyph[] line : lines) {
            if (line.length == 0) {
                lineNum++;
                index = 0;
                continue;
            }
            index = 0;
            for (Glyph glyf : line) {
                GlyphMesh glyph = (GlyphMesh)glyf;
                if (glyph.codePoint != ' ') {
                    if (numVerts + glyph.getMesh().getVertexCount() < Short.MAX_VALUE) {
                        numVerts += glyph.getMesh().getVertexCount();
                    } else {
                        snippets.add(new TextSnippet(startLine, lineNum, startIndex, index, numVerts,
                                totalVerts));
                        
                        totalVerts += numVerts;
                        numVerts = glyph.getMesh().getVertexCount();
                        startLine = lineNum;
                        startIndex = index;
                    }
                }
                index++;
            }
            lineNum++;
        }
        snippets.add(new TextSnippet(startLine, lines.length - 1, startIndex, index, numVerts,
                                totalVerts));
        
        Vector2f xyOffset = new Vector2f(0, ttm.getScaledAscender());
        float yOffset = 0;
        switch(stringContainer.getVerticalAlignment()) {
            case Bottom:
                yOffset = stringContainer.getTextBox().height
                        - stringContainer.getTextHeight();
                break;
            case Center:
                float halfBox = stringContainer.getTextBox().height / 2;
                float halfHeight = stringContainer.getTextHeight() / 2;
                yOffset = halfBox - halfHeight;
                break;
        }
        
        for (TextSnippet snippet : snippets) {
            TextMesh mesh = new TextMesh();
            mesh.createMesh(lines, snippet, stringContainer.getLineWidths(),
                    stringContainer.getLineHeights(), stringContainer.getTextWidth(),
                    stringContainer.getTextHeight(), yOffset, xyOffset);
            Geometry g = new Geometry("TrueTypeMeshText", mesh);
            g.setMaterial(material);
            attachChild(g);
        }
    }
    
    /**
     * A helper class that tracks information necessary to determine where in the
     * text each {@link TextMesh} should start and end.
     * 
     * @author Adam T. Ryder
     */
    private class TextSnippet {
        public final int startLine;
        public final int endLine;
        public final int startIndex;
        public final int endIndex;
        public final int numVerts;
        
        private TextSnippet(final int startLine, final int endLine, final int startIndex,
                final int endIndex, final int numVerts, final int indexOffset) {
            this.startLine = startLine;
            this.endLine = endLine;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.numVerts = numVerts;
        }
    }
    
    /**
     * The mesh containing the assembled glyphs.
     * 
     * @author Adam T. Ryder
     */
    private class TextMesh extends Mesh {
        public Vector2f createMesh(final Glyph[][] lines, final TextSnippet snippet,
                final float[] lineWidths, final Vector2f[] lineHeights, final float width,
                final float height, final float yOffset, final Vector2f currentXY) {
            TrueTypeMesh ttm = (TrueTypeMesh)stringContainer.getFont();
            
            FloatBuffer verts = BufferUtils.createFloatBuffer(snippet.numVerts * 3);
            FloatBuffer tex1 = BufferUtils.createFloatBuffer(snippet.numVerts * 2);
            FloatBuffer tex2 = BufferUtils.createFloatBuffer(tex1.capacity());
            //Tex3 covers each character
            FloatBuffer tex3 = BufferUtils.createFloatBuffer(tex2.capacity());
            //Tex4 covers each line
            FloatBuffer tex4 = BufferUtils.createFloatBuffer(tex3.capacity());
            //Tex5 covers the whole text block
            FloatBuffer tex5 = BufferUtils.createFloatBuffer(tex4.capacity());
            ShortBuffer indices = BufferUtils.createShortBuffer(snippet.numVerts);
            
            float startX = currentXY.x;
            float currentY = currentXY.y;
            short currentIndex = 0;
            int startIndex = snippet.startIndex;
            lineLoop: for (int i = snippet.startLine; i <= snippet.endLine; i++) {
                float currentX = startX;
                startX = 0;
                float xOffset = 0;
                switch(stringContainer.getAlignment()) {
                    case Right:
                        xOffset = stringContainer.getTextBox().width
                                - lineWidths[i];
                        break;
                    case Center:
                        float halfBox = stringContainer.getTextBox().width / 2;
                        float halfWidth = lineWidths[i] / 2;
                        xOffset = halfBox - halfWidth;
                        break;
                }
                
                Vector2f lineY = lineHeights[i];
                float lineHeight = lineY == null ? 0 : lineY.x - lineY.y;
                for (int n = startIndex; n < lines[i].length; n++) {
                    startIndex = 0;
                    if (i == snippet.endLine && n == snippet.endIndex) {
                        if (snippet.endIndex == lines[i].length) {
                            currentXY.set(0, currentY + ttm.getScaledLineHeight());
                        } else 
                            currentXY.set(currentX, currentY);
                        break lineLoop;
                    }
                    
                    GlyphMesh glyph = (GlyphMesh)lines[i][n];
                    if (glyph.codePoint == ' ') {
                        currentX += glyph.getXAdvance() + stringContainer.getKerning();
                        continue;
                    }
                    
                    FloatBuffer gverts = glyph.getMesh().getFloatBuffer(VertexBuffer.Type.Position);
                    gverts.clear();
                    FloatBuffer gtex1 = glyph.getMesh().getFloatBuffer(VertexBuffer.Type.TexCoord);
                    gtex1.clear();
                    FloatBuffer gtex2 = glyph.getMesh().getFloatBuffer(VertexBuffer.Type.TexCoord2);
                    gtex2.clear();
                    ShortBuffer gindices = glyph.getMesh().getShortBuffer(VertexBuffer.Type.Index);
                    gindices.clear();
                    
                    int w = glyph.getWidth();
                    int h = glyph.getHeight();
                    while(gverts.hasRemaining()) {
                        float x = gverts.get();
                        float y = gverts.get();
                        float x2 = x + (currentX * ttm.getScale());
                        float y2 = y - currentY;
                        verts.put(x2 + xOffset);
                        verts.put(y2 - yOffset);
                        verts.put(gverts.get());
                        
                        tex1.put(gtex1.get());
                        tex1.put(gtex1.get());
                        tex2.put(gtex2.get());
                        tex2.put(gtex2.get());
                        tex3.put((x - (glyph.getLeft() * ttm.getScale())) / w);
                        tex3.put((y - (glyph.getDescender() * ttm.getScale())) / h);
                        tex4.put(x2 / lineWidths[i]);
                        tex4.put((y - lineY.y) / lineHeight);
                        if (stringContainer.getAlignment() == Align.Center) {
                            tex5.put((x2 + ((width / 2) - (lineWidths[i] / 2))) / width);
                        } else
                            tex5.put((x2 + xOffset) / width);
                        tex5.put(1f - (y2 / -height));
                        
                        indices.put((short)(gindices.get() + currentIndex));
                    }
                    
                    currentX += glyph.getXAdvance() + stringContainer.getKerning();
                    currentIndex += glyph.getMesh().getVertexCount();
                }
                
                if (i == snippet.endLine) {
                    currentXY.set(0, currentY + ttm.getScaledLineHeight());
                    break lineLoop;
                }
                
                currentY += ttm.getScaledLineHeight();
            }
            
            setBuffer(VertexBuffer.Type.Position, 3, verts);
            setBuffer(VertexBuffer.Type.TexCoord, 2, tex1);
            setBuffer(VertexBuffer.Type.TexCoord2, 2, tex2);
            setBuffer(VertexBuffer.Type.TexCoord3, 2, tex3);
            setBuffer(VertexBuffer.Type.TexCoord4, 2, tex4);
            setBuffer(VertexBuffer.Type.TexCoord5, 2, tex5);
            setBuffer(VertexBuffer.Type.Index, 3, indices);
            
            clearCollisionData();
            updateBound();
            
            return currentXY;
        }
    }
}
