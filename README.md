jME-TTF
==================

jME-TTF is a True Type Font(.ttf) rendering system for jMonkeyEngine3.1 and newer. With this library you can render text styles loaded directly from a true type font file at run-time. Characters from the ttf file are written to a dynamically sized texture atlas and then rendered on screen using a series of quads that display each character from the atlas. When requesting a text string to display jME-TTF looks through the string to see if any characters are missing from the atlas and if so adds them, expanding the size of the atlas texture as necessary.

[<img 
src="https://dl.dropboxusercontent.com/s/ldrg036040dx3g4/c5855071bc96733bcb4db72b98356deed0003d2f.png?dl=0"
 alt="TEdit Screen" width="567" height="294">](https://dl.dropboxusercontent.com/s/ldrg036040dx3g4/c5855071bc96733bcb4db72b98356deed0003d2f.png?dl=0)

In addition to that you can opt to render scaleable text styled from a true type font file in your 3D or 2D scenes. jME-TTF can triangulate a mesh from the glyph outline of each requested character, caching new glyph meshes, and apply a material that interpolates curved sections of the glyph's contours using quadratic bezier formulas. The result is a text that can scale without pixelaltion and is fully compatible with modern GPU anti-aliasing methods.

[<img 
src="https://dl.dropboxusercontent.com/s/wmgamqzxx5ky4s6/Screenshot_2017_11_17_17.png?dl=0"
 alt="TEdit Screen" width="567" height="249">](https://dl.dropboxusercontent.com/s/wmgamqzxx5ky4s6/Screenshot_2017_11_17_17.png?dl=0)

jME-TTF provides a variety of conveniences such as getting the width of a line of text in pixels or world units, the line height, visual heights, scaling and kerning, you can even get a texture displaying blurred text. Formatting options such as vertical/horizontal alignment and text wrapping are also available.

Finally jME-TTF assigns several UV layers that can be taken advantage of in your own custom shaders to create a wide variety of different effects.

[<img 
src="https://dl.dropboxusercontent.com/s/oav8rjtu4aebxer/707ad177d0efb01352a00e8a81be9cf6b3b876ef.png?dl=0"
 alt="TEdit Screen" width="474" height="400">](https://dl.dropboxusercontent.com/s/oav8rjtu4aebxer/707ad177d0efb01352a00e8a81be9cf6b3b876ef.png?dl=0)

[<img 
src="https://dl.dropboxusercontent.com/s/puy6ebbxc2p5p0i/6983fb0efd926ecd68d1ad2220f557549e37a2c2.png?dl=0"
 alt="TEdit Screen" width="696" height="398">](https://dl.dropboxusercontent.com/s/puy6ebbxc2p5p0i/6983fb0efd926ecd68d1ad2220f557549e37a2c2.png?dl=0)

[<img 
src="https://dl.dropboxusercontent.com/s/fydn0y11xxckxp2/1693b179e6b26b076e9748f69eaa010188ae23b9.png?dl=0"
 alt="TEdit Screen" width="695" height="415">](https://dl.dropboxusercontent.com/s/fydn0y11xxckxp2/1693b179e6b26b076e9748f69eaa010188ae23b9.png?dl=0)

jME-TTF depends upon Google's Sfntly library available at https://github.com/rillig/sfntly

You can find more about jME-TTF including usage documentation at http://1337atr.weebly.com/jttf.html
