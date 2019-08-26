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
package com.atr.jme.font.util;

import com.atr.jme.font.TrueTypeBMP;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.texture.Texture2D;

/**
 * An implementation of {@link BlurText} used to blur {@link com.atr.jme.font.TrueTypeBMP} fonts.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class BlurTextBMP extends BlurText {
    private int atlasWidth;
    private int atlasHeight;
    
    public BlurTextBMP(AssetManager assetManager, RenderManager renderManager, StringContainer text,
            float intensity, ColorRGBA color) {
        this(assetManager, renderManager, text, 1, intensity, color);
    }
    
    public BlurTextBMP(AssetManager assetManager, RenderManager renderManager, StringContainer text,
            int passes, float intensity, ColorRGBA color) {
        this(assetManager, renderManager, text, passes, 1, intensity, color);
    }
    
    public BlurTextBMP(AssetManager assetManager, RenderManager renderManager, StringContainer text,
            int passes, float offset, float intensity, ColorRGBA color) {
        super(assetManager, renderManager, text, passes, intensity, intensity, color);
    }
    
    @Override
    protected void init() {
        super.init();
        atlasWidth = ((TrueTypeBMP)sc.getFont()).getAtlas().getImage().getWidth();
        atlasHeight = ((TrueTypeBMP)sc.getFont()).getAtlas().getImage().getHeight();
    }
    
    @Override
    public Texture2D render() {
        TrueTypeBMP ttb = (TrueTypeBMP)sc.getFont();
        if (atlasWidth != ttb.getAtlas().getImage().getWidth()
                || atlasHeight != ttb.getAtlas().getImage().getHeight()) {
            atlasWidth = ttb.getAtlas().getImage().getWidth();
            atlasHeight = ttb.getAtlas().getImage().getHeight();
            geom.updateGeometry();
        }
        geom.getMaterial().setTexture("Texture", ttb.getAtlas());
        if (ttb.getOutline() > 0)
            geom.getMaterial().setColor("Outline", color);
        
        return super.render();
    }
}
