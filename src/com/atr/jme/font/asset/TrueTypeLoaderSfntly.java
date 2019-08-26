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

import com.atr.jme.font.TrueTypeSfntly;
import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;

/**
 * Loads a {@link TrueTypeSfntly} from a .ttf file stored on disk.
 * 
 * @see TrueTypeKey
 * @see com.atr.jme.font.TrueTypeFont
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeLoaderSfntly implements AssetLoader {
    @Override
    public TrueTypeSfntly load(AssetInfo assetInfo) throws IOException {
        TrueTypeKeyBMP key = (TrueTypeKeyBMP)assetInfo.getKey();
        
        FontFactory fontFactory = FontFactory.getInstance();
        Font[] fonts = fontFactory.loadFonts(assetInfo.openStream());
        
        if (fonts.length > 0) {
            return new TrueTypeSfntly(assetInfo.getManager(), fonts[0], key.getStyle(),
                    key.getPointSize(), key.getOutline(), key.getScreenDensity(),
                    key.getMaxAtlasRes(), key.getPreloadCharacters());
        } else
            throw new IOException("No fonts found in: " + assetInfo.getKey().getName());
    }
}
