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

import com.atr.jme.font.TrueTypeFont;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;

/**
 * Loads a {@link TrueTypeFont} from a .ttf file stored on disk. If this was
 * accessed with a {@link TrueTypeKeyBMP} a {@link com.atr.jme.font.TrueTypeSfntly} will be
 * returned if running on Android otherwise {@link com.atr.jme.font.TrueTypeAWT} will be
 * returned. If this was accessed with {@link TrueTypeKeyMesh} then
 * {@link com.atr.jme.font.TrueTypeMesh} will be returned.
 * 
 * @see TrueTypeKey
 * @see TrueTypeFont
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TrueTypeLoader implements AssetLoader {
    @Override
    public TrueTypeFont load(AssetInfo assetInfo) throws IOException {
        if (assetInfo.getKey() instanceof TrueTypeKeyMesh)
            return new TrueTypeLoaderMesh().load(assetInfo);
        
        String vendor = System.getProperty("java.vendor.url");
        if (vendor == null || !vendor.toLowerCase().contains("android"))
            return new TrueTypeLoaderAWT().load(assetInfo);
        
        return new TrueTypeLoaderSfntly().load(assetInfo);
    }
}
