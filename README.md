
# PgnToGif
A mobile application to help convert chess PGN files to animated gifs

See how PGNs work from this article
https://en.wikipedia.org/wiki/Portable_Game_Notation

## Libraries Used
https://github.com/bhlangonijr/chesslib For Pgn validation and parsing

https://github.com/videlalvaro/gifsockets/blob/master/src/java/AnimatedGifEncoder.java  For combining bitmaps into Gifs

## How it works
The PGN is parsed using the [library](https://github.com/bhlangonijr/chesslib) mentioned above and the a game object that contains the board position for all the moves is created.
Each bitmap frame is then created by taking the current board position and representing it on an android View Canvas and converting the resulting canvas to a Bitmap object.

## Possible improvements
1. Improve the speed of generating the Animated Gifs without compromising the quality of the generated gifs and colour of the cells.
2. Improve the quality of the codebase, the application has just two screens for now so it does not neccessarily require any particular architecture but the structure of the code could be improved. For example, the dimension of the board could board and the other size calculations could be placed in a separate class in case it has to change. 
3. Implement the Gif generation algorithm using Kotlin Multi platform. The two major reason why this cannot be done now is
 I. The chess parsing library is written in Java and has some implementations that cannot be converted to KMP directly. 
 II. The images are generated using [Bitmap](https://developer.android.com/reference/android/graphics/Bitmap) and [Canvas](https://developer.android.com/reference/android/graphics/Canvas) classes which are android framework classes. 

These two problems can be worked around.

4. Improve the UI, I ran out of ideas for how the UI should look, especially the first screen.


## How to contribute

You could pick up any of the issues or possible improvements listed above. If you're picking up an issue, you could comment on the issue, also if your picking up any of the possible improvements above you should create an issue for it too. 

## Playstore link

https://play.google.com/store/apps/details?id=com.chunkymonkey.imagetogifconverter
