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
package com.atr.jme.font.asset;

import com.atr.jme.font.TrueTypeAWT;
import com.atr.jme.font.TrueTypeBMP;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads a {@link TrueTypeAWT} from a .ttf file stored on disk.
 * 
 * @see TrueTypeKey
 * @see com.atr.jme.font.TrueTypeFont
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeLoaderAWT implements AssetLoader {

    @SuppressWarnings("rawtypes")
    @Override
    public TrueTypeBMP load(AssetInfo assetInfo) throws IOException {
        TrueTypeKeyBMP key = (TrueTypeKeyBMP)assetInfo.getKey();
        
        java.awt.Font font = null;
        try {
            font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, assetInfo.openStream());
        } catch (java.awt.FontFormatException ffe) {
            font = null;
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unable to load "
                    + key.getName()
                    + " using system default Sans Serif font instead.", ffe);
            throw new IOException("Unable to load "
                    + key.getName()
                    + " using system default Sans Serif font instead.");
        } catch (IOException ioe) {
            throw ioe;
        }
        
        switch(key.getStyle()) {
            case Plain:
                font = font.deriveFont(java.awt.Font.PLAIN, key.getPointSize());
                break;
            case Bold:
                font = font.deriveFont(java.awt.Font.BOLD, key.getPointSize());
                break;
            case Italic:
                font = font.deriveFont(java.awt.Font.ITALIC, key.getPointSize());
                break;
            case BoldItalic:
                font = font.deriveFont(java.awt.Font.BOLD + java.awt.Font.ITALIC, key.getPointSize());
                break;
            default:

        }

        return new TrueTypeAWT(assetInfo.getManager(), font,
            key.getStyle(), key.getPointSize(), key.getOutline(), key.getScreenDensity(),
                key.getMaxAtlasRes(), key.getPreloadCharacters(), key.isFixedResolution());
    }
}
