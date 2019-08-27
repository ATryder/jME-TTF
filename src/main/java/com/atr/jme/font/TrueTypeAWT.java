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

import com.atr.jme.font.glyph.GlyphAWT;
import com.atr.jme.font.util.AtlasListener;
import com.atr.jme.font.util.Style;
import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.util.BufferUtils;
import com.jme3.util.NativeObjectManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link TrueTypeBMP} used primarily on desktop platforms
 * using <code>java.awt.Font</code> to load fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 * 
 * @see TrueTypeBMP
 * @see TrueTypeFont
 */
public class TrueTypeAWT extends TrueTypeBMP<GlyphAWT> {
    private final Font font;
    private final FontRenderContext frc;
    private final AffineTransform transform;
    
    public TrueTypeAWT(AssetManager assetManager, Font font, Style style, int pointSize,
            int outline, int dpi, int maxAtlasResolution, String preload, boolean fixedResolution) {
        super(assetManager, style, pointSize, outline, dpi, maxAtlasResolution, fixedResolution);
        
        if (font != null) {
            this.font = font; 
        } else {
            this.font = new Font(Font.SANS_SERIF, Font.PLAIN, pointSize);
        }
        
        double dpiScale =  dpi / 72f;
        transform = new AffineTransform();
        transform.setToScale(dpiScale, -dpiScale);
        Graphics2D g = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB).createGraphics();
        g.setFont(this.font);
        frc = g.getFontRenderContext();
        FontMetrics fm = g.getFontMetrics(this.font);
        g.dispose();
        
        ascender = (int)Math.ceil(fm.getMaxAscent() * dpiScale) + outline;
        descender = (int)Math.ceil(fm.getMaxDescent() * dpiScale) + Math.round(outline / 2f);
        lineGap = (int)Math.ceil(fm.getLeading() * dpiScale);
        lineHeight = ascender + descender + lineGap;
        
        charHeight = (int)Math.ceil(this.font.getMaxCharBounds(frc).getHeight() * dpiScale) + padding;
        resizeWidth = (int)Math.ceil(this.font.getMaxCharBounds(frc).getWidth() * dpiScale) + padding;
        
        getGlyphs(new StringBuilder().appendCodePoint(defaultCodePoint).append(" ").append(preload));
    }
    
    @Override
    public boolean canDisplay(int codePoint) {
        return font.canDisplay(codePoint);
    }
    
    /**
     * 
     * @return The <code>java.awt.Font</code> used by this <code>TrueTypeFont</code>.
     */
    public Font getFont() {
        return font;
    }
    
    @Override
    protected void createGlyphs(List<CharToCreate> characters) {
        if (atlas == null) {
            resizeAtlas();
        }
        
        boolean added = false;
        Map<Integer, GlyphVector> backLog = new HashMap<Integer, GlyphVector>();
        do {
            int line = 0;
            for (AtlasLine al : atlasLines) {
                for (Iterator<CharToCreate> it = characters.iterator(); it.hasNext();) {
                    CharToCreate ctc = it.next();
                    if (cache.containsKey(ctc.codePoint)) {
                        it.remove();
                        continue;
                    }
                    
                    sb.delete(0, sb.length());
                    sb.appendCodePoint(ctc.codePoint);
                    GlyphVector gv = backLog.get(ctc.codePoint);
                    if (gv == null)
                        gv = font.createGlyphVector(frc, sb.toString());
                    gv.setGlyphTransform(0, transform);
                    Rectangle2D bounds = gv.getVisualBounds();
                    int w = (int)bounds.getWidth() + padding;
                    if (al.canFit(w)) {
                        GlyphAWT gawt = new GlyphAWT(this, al.getX(), line * charHeight,
                                ctc.codePoint, gv);
                        cache.put(ctc.codePoint, gawt);
                        
                        added = true;
                        al.addChar(w);
                        it.remove();
                    } else
                        backLog.put(ctc.codePoint, gv);
                }
                line++;
            }
            
            if (!characters.isEmpty()) {
                if (atlasWidth + resizeWidth > maxTexRes
                        && atlasHeight + charHeight > maxTexRes) {
                    for (Iterator<CharToCreate> it = characters.iterator(); it.hasNext();) {
                        it.next().codePoint = defaultCodePoint;
                        it.remove();
                    }
                    break;
                } else
                    resizeAtlas();
            }
        } while (!characters.isEmpty());
        
        if (atlasResized || added) {
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
    }
    
    @Override
    protected void createAtlas() {
        BufferedImage tmpImg = new BufferedImage(atlasWidth, atlasHeight,
                BufferedImage.TYPE_INT_BGR);
        Graphics2D g = tmpImg.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, atlasWidth, atlasHeight);
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.white);
        g.setFont(font);
        
        //AffineTransform af = new AffineTransform();
        //af.setToScale(transform.getScaleX(), -transform.getScaleY());
        for (GlyphAWT glyph : cache.values()) {
            if (glyph.getOutline() == null)
                continue;
            int x = (glyph.x + (padding / 2)) - glyph.getXOffset();
            int y = (glyph.y + (padding /2 )) - glyph.getYOffset();
            g.translate(x, y);
            
            //GlyphVector gv = font.createGlyphVector(frc, glyph.getCharacter());
            //gv.setGlyphTransform(0, af);
            //Shape s = gv.getOutline();
            g.fill(glyph.getOutline());
            
            g.translate(-x, -y);
        }
        if (atlas != null) {
            atlas.getImage().dispose();
            if (!NativeObjectManager.UNSAFE) {
                for (ByteBuffer buf : atlas.getImage().getData()) {
                    BufferUtils.destroyDirectBuffer(buf);
                }
            }
            atlas.setImage(new AWTLoader().load(tmpImg, false));
        } else {
            atlas = new Texture2D(new AWTLoader().load(tmpImg, false));
        }

        g.dispose();
        
        atlasResized = false;
    }
    
    @Override
    protected void createAtlasOutlined() {
        BufferedImage tmpImg = new BufferedImage(atlasWidth, atlasHeight,
                BufferedImage.TYPE_INT_BGR);
        Graphics2D g = tmpImg.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, atlasWidth, atlasHeight);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        //AffineTransform af = new AffineTransform();
        //af.setToScale(transform.getScaleX(), -transform.getScaleY());
        Color fill = new Color(255, 0, 255);
        BasicStroke stroke = new BasicStroke(outline, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        BasicStroke noStroke = new BasicStroke(0);
        for (GlyphAWT glyph : cache.values()) {
            if (glyph.getOutline() == null)
                continue;
            g.setStroke(stroke);
            int x = (glyph.x + (padding / 2)) - glyph.getXOffset();
            int y = (glyph.y + (padding / 2)) - glyph.getYOffset();
            g.translate(x, y);
            
            //GlyphVector gv = font.createGlyphVector(frc, glyph.getCharacter());
            //gv.setGlyphTransform(0, af);
            //Shape s = gv.getOutline();
            g.setPaint(new Color(255, 0, 0));
            g.draw(glyph.getOutline());
            g.setStroke(noStroke);
            g.setPaint(fill);
            g.fill(glyph.getOutline());
            g.translate(-x, -y);
            
        }

        if (atlas != null) {
            atlas.getImage().dispose();
            if (!NativeObjectManager.UNSAFE) {
                for (ByteBuffer buf : atlas.getImage().getData()) {
                    BufferUtils.destroyDirectBuffer(buf);
                }
            }
            atlas.setImage(new AWTLoader().load(tmpImg, false));
        } else {
            atlas = new Texture2D(new AWTLoader().load(tmpImg, false));
        }

        g.dispose();
        
        atlasResized = false;
    }
}
