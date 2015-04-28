/***preinitBlock***/
#ifdef __MBED__
#include "mbed.h"
#include "WS2812.h"
#endif
#define WS2812_BUF 300
int pixels[WS2812_BUF];
#ifdef __MBED__
WS2812 ws(D9, WS2812_BUF, 0, 5, 5, 0);
#endif
/**/

/***initBlock***/
for (int i = 0; i < WS2812_BUF; i++) {
pixels[i] = 0x000000;
}
#ifdef __MBED__
ws.write(pixels);
#endif
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
if ($getAndFree(clear)) {
for (int i = 0; i < WS2812_BUF; i++) {
pixels[i] = 0x000000;
}
}
Token *  indexTokenArray = $get(ledIndex);
Token *  colorsTokenArray = $get(color);
int arraySize = indexTokenArray->payload.IntArray->size;
for (int i = 0; i < arraySize; i++) {
    pixels[IntArray_get(indexTokenArray, i)] = IntArray_get(colorsTokenArray,i);
}
IntArray_delete(indexTokenArray);
IntArray_delete(colorsTokenArray);
free(indexTokenArray);
free(colorsTokenArray);
#ifdef __MBED__
ws.write(pixels);
//wait(1);
#endif
/**/

/***wrapupBlock***/
/**/



