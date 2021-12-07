# PgnToGif
A mobile application to help convert chess pin files to animated gifs

See how PGNs work from this article
https://en.wikipedia.org/wiki/Portable_Game_Notation

## Libraries Used
https://github.com/bhlangonijr/chesslib For Pgn validation and parsing

https://github.com/videlalvaro/gifsockets/blob/master/src/java/AnimatedGifEncoder.java  For combining bitmaps into Gifs

Each bitmap frame is created by taking the current board position and representing it on and android View Canvas and converting the resulting canvas to a Bitmap object.


