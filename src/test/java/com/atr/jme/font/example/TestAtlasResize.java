package com.atr.jme.font.example;

import com.atr.jme.font.TrueTypeBMP;
import com.atr.jme.font.TrueTypeFont;
import com.atr.jme.font.asset.TrueTypeKey;
import com.atr.jme.font.asset.TrueTypeKeyBMP;
import com.atr.jme.font.asset.TrueTypeLoader;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.Style;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;

/**
 * @title TestAtlasResize
 * @author yanmaoyuan
 * @date 2019-08-27
 * @version 1.0
 */
public class TestAtlasResize extends SimpleApplication {

    private String font = "fonts/ubuntu-font-family-0.83/Ubuntu-M.ttf";

    private int fontSize = 18;

    public static void main(String[] args) {
        TestAtlasResize app = new TestAtlasResize();
        app.start();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        flyCam.setMoveSpeed(100f);

        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        
        TrueTypeKey key = new TrueTypeKeyBMP(font, Style.Plain, fontSize, 0, 72, false, "", 256, true);

        final TrueTypeFont font = (TrueTypeFont)assetManager.loadAsset(key);

        // Use a short String first.
        StringContainer sc = new StringContainer(font, "ABC", 2);
        TrueTypeContainer ttc = font.getFormattedText(sc, ColorRGBA.White);
        ttc.setLocalTranslation(0, font.getActualLineHeight(), 0);
        rootNode.attachChild(ttc);

        displayAtlas(font, -20, 0);

        inputManager.addMapping("ADD", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                // Use a longer String, force atlas resize.
                TrueTypeNode text = font.getText("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ColorRGBA.White);
                text.move(0, font.getActualLineHeight() * 2, 0);
                rootNode.attachChild(text);
                displayAtlas(font, -20, 21);

                // Use this listener only once
                inputManager.deleteMapping("ADD");
                inputManager.removeListener(this);
            }
        }, "ADD");
        

    }

    @SuppressWarnings("rawtypes")
    private void displayAtlas(TrueTypeFont font, float x, float y) {
        TrueTypeBMP bmp = (TrueTypeBMP)font;
        Texture2D texture = bmp.getAtlas();

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);

        Geometry geom = new Geometry("altas", new Quad(20, 20));
        geom.setMaterial(mat);
        geom.setLocalTranslation(x, y, 0);
        
        rootNode.attachChild(geom);
    }
}