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

/**
 * A callback used to listen for changes in the font's texture atlas. Whenever the
 * texture atlas is modified the <code>mod</code> method will be called on all
 * <code>TTF_AtlasListener</code>s attached to the <code>TrueTypeFont</code>.
 * 
 * @see TrueTypeBMP#addAtlasListener(com.atr.jme.font.util.AtlasListener) 
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public interface AtlasListener {
    @SuppressWarnings("rawtypes")
    public void mod(AssetManager assetManager, int oldWidth,
            int oldHeight, int newWidth, int newHeight, TrueTypeBMP font);
}
