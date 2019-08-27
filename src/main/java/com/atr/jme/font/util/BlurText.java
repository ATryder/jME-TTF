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

import com.atr.jme.font.shape.TrueTypeContainer;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3.util.NativeObjectManager;
import java.nio.ByteBuffer;

/**
 * A class that allows to blur text created with jME-TTF in
 * real-time. Text is supplied to this class using a
 * {@link com.atr.jme.font.util.StringContainer} and calling the
 * {@link #render()} method will produce a <code>Texture2D</code>
 * that displays the supplied text with a gaussian blur applied.<br><br>
 * 
 * One will want to be sure to {@link #dispose()} this
 * <code>BlurText</code> when they are done with the rendered
 * <code>Texture2D</code> to ensure it is cleaned from memory
 * in a timely manner. Moreover one should use the
 * {@link #disposeLight()} method after calling {@link #render()}
 * to ensure that the secondary buffer/texture is removed from
 * memory when it is no longer needed, unless one plans to
 * continually call {@link #render()}.
 * 
 * Use this with {@link com.atr.jme.font.TrueTypeMesh}, for
 * {@link com.atr.jme.font.TrueTypeBMP} use {@link BlurTextBMP}.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 * 
 * @see #setPasses(int) 
 * @see #setOffset(float) 
 * @see #setIntensity(float) 
 * @see #render() 
 * @see #disposeLight() 
 * @see #dispose() 
 */
public class BlurText {
    
    private final RenderManager rm;
    private final Renderer renderer;
    
    private FrameBuffer vBuf;
    private FrameBuffer hBuf;
    private Texture2D vTex;
    private Texture2D hTex;
    private Material vBlur;
    private Material hBlur;
    private Material glow;
    
    protected StringContainer sc;
    
    private int passes;
    private float offset;
    private float intensity;
    
    private final Camera cam;
    private final Picture quad;
    protected TrueTypeContainer geom;
    
    private float containerW;
    private float containerH;
    
    protected ColorRGBA color;
    
    /**
     * Creates an instance of <code>BlurText</code> with a
     * {@link com.atr.jme.font.util.StringContainer} that will be blurred
     * in one pass with a radius multiplier of one and alpha multiplier
     * of intensity amount.<br><br>
     * 
     * Typically one will increase the radius of the gaussian blur by
     * running more passes, but to increase performance at the cost of
     * quality the radius can be increased by supplying an offset greater
     * than 1.
     * 
     * @param assetManager jMonkeyEngine AssetManager
     * @param renderManager jMonkeyEngine RenderManager
     * @param text A {@link com.atr.jme.font.util.StringContainer} with the desired
     * text to blur.
     * @param intensity A multiplier for the alpha value of the resulting
     * blur.
     * @param color The color of the blurred text.
     */
    public BlurText(AssetManager assetManager, RenderManager renderManager, StringContainer text,
            float intensity, ColorRGBA color) {
        this(assetManager, renderManager, text, 1, intensity, color);
    }
    
    /**
     * Creates an instance of <code>BlurText</code> with a
     * {@link com.atr.jme.font.util.StringContainer} that will be blurred
     * over the supplied number of passes with a radius multiplier of
     * 1 and alpha multiplier of intensity amount.<br>
     * 
     * Typically one will increase the radius of the gaussian blur by
     * running more passes, but to increase performance at the cost of
     * quality the radius can be increased by supplying an offset greater
     * than 1.
     * 
     * @param assetManager jMonkeyEngine AssetManager
     * @param renderManager jMonkeyEngine RenderManager
     * @param text A {@link com.atr.jme.font.util.StringContainer} with the desired
     * text to blur.
     * @param passes The number of gaussian blur passes to perform.
     * @param intensity A multiplier for the alpha value of the resulting
     * blur.
     * @param color The color of the blurred text.
     */
    public BlurText(AssetManager assetManager, RenderManager renderManager, StringContainer text,
            int passes, float intensity, ColorRGBA color) {
        this(assetManager, renderManager, text, passes, 1, intensity, color);
    }
    
    /**
     * Creates an instance of <code>BlurText</code> with a
     * {@link com.atr.jme.font.util.StringContainer} that will be blurred
     * over the supplied number of passes with a radius multiplier of
     * offset amount and alpha multiplier of intensity amount.<br>
     * 
     * Typically one will increase the radius of the gaussian blur by
     * running more passes, but to increase performance at the cost of
     * quality the radius can be increased by supplying an offset greater
     * than 1.
     * 
     * @param assetManager jMonkeyEngine AssetManager
     * @param renderManager jMonkeyEngine RenderManager
     * @param text A {@link com.atr.jme.font.util.StringContainer} with the desired
     * text to blur.
     * @param passes The number of gaussian blur passes to perform.
     * @param offset A multiplier for the radius of the gaussian blur.
     * @param intensity A multiplier for the alpha value of the resulting
     * blur.
     * @param color The color of the blurred text.
     */
    public BlurText(AssetManager assetManager, RenderManager renderManager, StringContainer text,
            int passes, float offset, float intensity, ColorRGBA color) {
        this.rm = renderManager;
        renderer = rm.getRenderer();
        
        sc = text;
        this.passes = passes < 1 ? 1 : passes;
        this.offset = offset < 1 ? 1 : offset;
        this.intensity = intensity;
        
        this.color = color;
        
        vBlur = new Material(assetManager, "Common/MatDefs/TTF/Blur/VGaussianBlur.j3md");
        
        hBlur = new Material(assetManager, "Common/MatDefs/TTF/Blur/HGaussianBlur.j3md");
        
        glow = new Material(assetManager, "Common/MatDefs/TTF/Blur/blurFinal.j3md");
        glow.setColor("Color",
                new ColorRGBA(color.r, color.g, color.b, color.a * intensity));
        
        cam = new Camera(1, 1);
        quad = new Picture("BlurText");
        
        init();
    }
    
    /**
     * Gets the offset of the {@link com.atr.jme.font.util.StringContainer} within
     * the rendered <code>Texture2D</code> bounds. Typically if you were
     * going to display that same {@link com.atr.jme.font.util.StringContainer}
     * over top of the resultant image rendered on a <code>Quad</code> you
     * would center your {@link com.atr.jme.font.shape.TrueTypeContainer} to
     * <code>Quad.getLocalTranslation().x + BlurText.getTextOffset().x</code>
     * and <code>Quad.getLocalTranslation().y + BlurText.getTextOffset().y
     * + StringContainer.getTextBox().height</code>.
     * 
     * @return 
     */
    public Vector3f getTextOffset() {
        return new Vector3f((vBuf.getWidth() / 2f) - (containerW / 2f),
                (vBuf.getHeight() / 2f) - (containerH / 2f), 0.02f);
    }
    
    /**
     * Sets the number of gaussian blur passes. When rendering this
     * <code>BlurText</code> a gaussian blur will be applied this
     * many times. Each pass will extend the blur radius so increase
     * this amount to obtain a blurrier result.
     * 
     * @param passes 
     * 
     * @see #setIntensity(float) 
     * @see #setOffset(float) 
     */
    public void setPasses(int passes) {
        this.passes = passes < 1 ? 1 : passes;
    }
    
    /**
     * Gets the number of gaussian blur passes.
     * 
     * @return 
     * 
     * @see #setPasses(int) 
     */
    public int getPasses() {
        return passes;
    }
    
    /**
     * Sets the offset of the blur radius. This will multiply the
     * radius of the gaussian blur so that each sample taken is offset
     * by this amount. Use this to artificially create larger blur
     * radii at the cost of quality.
     * 
     * @param offset 
     * 
     * @see #setPasses(int) 
     * @see #setIntensity(float) 
     */
    public void setOffset(float offset) {
        this.offset = offset < 1 ? 1 : offset;
    }
    
    /**
     * Gets the blur radius offset.
     * 
     * @return 
     * 
     * @see #setOffset(float) 
     */
    public float getOffset() {
        return offset;
    }
    
    /**
     * Sets the intensity of the blurred text. This affects the
     * alpha value of the resulting blur, use this to create
     * darker/lighter blurs.
     * 
     * @param intensity 
     */
    public void setIntensity(float intensity) {
        this.intensity = intensity;
        glow.setColor("Color",
                new ColorRGBA(color.r, color.g, color.b, color.a * intensity));
    }
    
    /**
     * Gets the intensity of the blurred text.
     * 
     * @return 
     * 
     * @see #setIntensity(float) 
     */
    public float getIntenisty() {
        return intensity;
    }
    
    /**
     * Sets the color of the blurred text.
     * 
     * @param color 
     */
    public void setColor(ColorRGBA color) {
        this.color.set(color);
        glow.setColor("Color", new ColorRGBA(color.r, color.g, color.b, color.a * intensity));
    }
    
    /**
     * Gets the color of the blurred text.
     * 
     * @return 
     * 
     * @see #setColor(com.jme3.math.ColorRGBA) 
     */
    public ColorRGBA getColor() {
        return color;
    }
    
    /**
     * Sets the underlying {@link com.atr.jme.font.util.StringContainer} used
     * to render the blurred text.
     * 
     * @param text 
     */
    public void setText(StringContainer text) {
        sc = text;
        dispose();
        init();
    }
    
    /**
     * Returns the underlying {@link com.atr.jme.font.util.StringContainer} used
     * to render the blurred text.
     * 
     * @return 
     */
    public StringContainer getText() {
        return sc;
    }
    
    /**
     * Returns true if this <code>BlurText</code> has been disposed of
     * using {@link #dispose()} otherwise false.
     * 
     * @return 
     * 
     * @see #dispose() 
     */
    public boolean isDisposed() {
        return hTex == null && vTex == null;
    }
    
    protected void init() {
        geom = sc.getFont().getFormattedText(sc, ColorRGBA.White);
        
        containerW = sc.getTextBox().width;
        containerH = sc.getTextBox().height;
        
        //The radius of a multiple pass gaussian blur is the square root of the sum of the
        //squares of the radius of each pass. sqrt(radius^2 * passes)
        int width = (int)Math.ceil((FastMath.sqrt(FastMath.sqr(offset * 4) * passes) * 2) + containerW);
        int height = (int)Math.ceil((FastMath.sqrt(FastMath.sqr(offset * 4) * passes) * 2) + containerH);
        
        vBuf = new FrameBuffer(width, height, 1);
        hBuf = new FrameBuffer(width, height, 1);
        
        vTex = new Texture2D(width, height, Image.Format.RGBA8);
        vTex.setWrap(Texture.WrapMode.EdgeClamp);
        vBuf.setColorTexture(vTex);
        hTex = new Texture2D(width, height, Image.Format.RGBA8);
        hTex.setWrap(Texture.WrapMode.EdgeClamp);
        hBuf.setColorTexture(hTex);
        
        vBlur.setTexture("Texture", hTex);
        vBlur.setFloat("Scale", offset);
        vBlur.setFloat("Size", height);
        
        hBlur.setTexture("Texture", vTex);
        hBlur.setFloat("Scale", offset);
        hBlur.setFloat("Size", width);
        
        cam.resize(width, height, true);
        quad.setWidth(width);
        quad.setHeight(height);
    }
    
    /**
     * Renders the blurred text.
     * 
     * @return A <code>Texture2D</code> which displays the blurred text.
     */
    public Texture2D render() {
        if (containerW != sc.getTextBox().width
                || containerH != sc.getTextBox().height || isDisposed()) {
            dispose();
            init();
        } else if (hTex == null) {
            int width = (int)Math.ceil((FastMath.sqrt(FastMath.sqr(offset * 4) * passes) * 2) + containerW);
            int height = (int)Math.ceil((FastMath.sqrt(FastMath.sqr(offset * 4) * passes) * 2) + containerH);
            vBuf = new FrameBuffer(width, height, 1);
            hBuf = new FrameBuffer(width, height, 1);
            
            vBuf.setColorTexture(vTex);
            hTex = new Texture2D(width, height, Image.Format.RGBA8);
            hTex.setWrap(Texture.WrapMode.EdgeClamp);
            hBuf.setColorTexture(hTex);
            
            vBlur.setTexture("Texture", hTex);
        }
        
        if (sc.getNumNonSpaceCharacters() == 0) {
            rm.setCamera(cam, true);
            renderer.setBackgroundColor(new ColorRGBA(color.r, color.g, color.b, 0));
            renderer.setFrameBuffer(hBuf);
            renderer.clearBuffers(true, false, false);
            
            return hTex;
        }
        
        geom.setLocalTranslation((vBuf.getWidth() / 2f) - (containerW / 2f),
                (vBuf.getHeight() / 2f) + (containerH / 2f), 0);
        geom.updateGeometricState();
        
        rm.setCamera(cam, true);
        renderer.setBackgroundColor(ColorRGBA.Black);
        renderer.setFrameBuffer(hBuf);
        renderer.clearBuffers(true, false, false);
        for (Spatial s : geom.getChildren()) {
            if (s instanceof Geometry)
                rm.renderGeometry((Geometry)s);
        }
        
        for (int i = 0; i < passes; i++) {
            renderer.setFrameBuffer(vBuf);
            quad.setMaterial(vBlur);
            rm.renderGeometry(quad);
            
            renderer.setFrameBuffer(hBuf);
            quad.setMaterial(hBlur);
            rm.renderGeometry(quad);
        }
        
        renderer.setBackgroundColor(new ColorRGBA(color.r, color.g, color.b, 0));
        geom.getMaterial().setColor("Color", color);
        
        renderer.setFrameBuffer(vBuf);
        renderer.clearBuffers(true, false, false);
        
        glow.setTexture("Texture", hTex);
        quad.setMaterial(glow);
        rm.renderGeometry(quad);
        
        return vTex;
    }
    
    /**
     * Deletes just the horizontal buffer/texture used for rendering
     * the blurred text, the vertical buffer/texture remain untouched.
     * Use this method after running {@link #render()} to delete the
     * unused portion of the rendering pipeline unless you plan to
     * call {@link #render()} repeatedly.
     */
    public void disposeLight() {
        if (hBuf == null)
            return;
        
        String vendor = System.getProperty("java.vendor.url");
        if (vendor != null && vendor.toLowerCase().contains("android")) {
            hBuf = null;
            hTex = null;
            return;
        }
        
        hBuf.deleteObject(renderer);
        hTex.getImage().dispose();
        if (!NativeObjectManager.UNSAFE) {
            for (ByteBuffer buf : hTex.getImage().getData()) {
                BufferUtils.destroyDirectBuffer(buf);
            }
        }
        
        hBuf = null;
        hTex = null;
    }
    
    /**
     * Deletes all resources used to render the blurred text.
     * Use this method when you no longer need the blurred
     * text texture.
     */
    public void dispose() {
        if (isDisposed())
            return;
        
        String vendor = System.getProperty("java.vendor.url");
        if (vendor != null && vendor.toLowerCase().contains("android")) {
            hBuf = null;
            hTex = null;
            vBuf = null;
            vTex = null;
            return;
        }
        
        disposeLight();
        vBuf.deleteObject(renderer);
        vTex.getImage().dispose();
        if (!NativeObjectManager.UNSAFE) {
            for (ByteBuffer buf : vTex.getImage().getData()) {
                BufferUtils.destroyDirectBuffer(buf);
            }
        }

        vBuf = null;
        vTex = null;
    }
}
