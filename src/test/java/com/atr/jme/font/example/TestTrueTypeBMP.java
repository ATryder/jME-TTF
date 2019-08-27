package com.atr.jme.font.example;

import com.atr.jme.font.TrueTypeFont;
import com.atr.jme.font.asset.TrueTypeKey;
import com.atr.jme.font.asset.TrueTypeKeyBMP;
import com.atr.jme.font.asset.TrueTypeLoader;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.Style;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;

/**
 * @title TestTrueTypeBMP
 * @author yanmaoyuan
 * @date 2019-08-27
 * @version 1.0
 */
public class TestTrueTypeBMP extends SimpleApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestTrueTypeBMP app = new TestTrueTypeBMP();
        app.start();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(100f);

        assetManager.registerLoader(TrueTypeLoader.class, "ttf");

        TrueTypeKey key = new TrueTypeKeyBMP(Constants.FONT, Style.Plain, 48);
        //TrueTypeKey key = new TrueTypeKeyBMP(FONT, Style.Plain, 48, 1, CONTENT, 128);

        TrueTypeFont font = (TrueTypeFont)assetManager.loadAsset(key);

        StringContainer sc = new StringContainer(font, Constants.HELLO_WORLD);

        // test getFormattedText
        TrueTypeContainer ttc = font.getFormattedText(sc, ColorRGBA.White);
        rootNode.attachChild(ttc);

        // test getText
        TrueTypeNode text = font.getText(Constants.HELLO_WORLD, 1, ColorRGBA.White);
        text.move(0, text.getHeight(), 0);// move up
        rootNode.attachChild(text);
 
    }

}
