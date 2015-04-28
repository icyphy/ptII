/***preinitBlock***/
#ifdef __MBED__
#include "mbed.h"
#endif
#define nCOLs 10
int xPos;
int yPos;
int zPos;
int cubeSize;
/**/

/***initBlock***/
xPos = 0;
yPos = 0;
zPos = 0;
cubeSize = 2;
/**/

/***fireBlock***/
xPos += $getAndFree(deltaX);
yPos += $getAndFree(deltaY);
zPos += $getAndFree(deltaZ);
int red = $getAndFree(r);
int green = $getAndFree(g);
int blue = $getAndFree(b);
cubeSize += $getAndFree(deltaSize);
if (xPos < 0) {
    xPos = 0;
}
if (yPos < 0) {
    yPos = 0;
}
if (zPos < 0) {
    zPos = 0;
}
if (xPos >= nCOLs) {
    xPos = nCOLs - 1;
}
if (yPos >= nCOLs) {
    yPos = nCOLs - 1;
}
if (zPos >= nCOLs) {
    zPos = nCOLs - 1;
}
if (cubeSize >= nCOLs) {
    cubeSize = nCOLs;
}
if (cubeSize < 2) {
    cubeSize = 2;
}
if (!((xPos + cubeSize) <= nCOLs && (yPos + cubeSize) <= nCOLs && (zPos + cubeSize) <= nCOLs)) {
    if ((xPos + cubeSize) > nCOLs) {
        xPos += nCOLs - (xPos + cubeSize);
    }
    if ((yPos + cubeSize) > nCOLs) {
        yPos += nCOLs - (yPos + cubeSize);
    }
    if ((zPos + cubeSize) > nCOLs) {
        zPos += nCOLs - (zPos + cubeSize);
    }
}
Token * intTokenArray = IntArray_new(0, 0);
Token * colorTokenArray = IntArray_new(0, 0);
double bright;
bright = 1.0 / ((yPos + 1) * (yPos + 1));
//Panel 1
for(int i = xPos; i < xPos + cubeSize; i++) {
    for(int j = zPos; j < zPos + cubeSize; j++) {
        int led;
    if (j % 2 == 0) {
        led = nCOLs*2 * j + i;
    }
    else {
        led = nCOLs*2 * j + nCOLs + ((nCOLs - 1) - i);
    }
            IntArray_insert(intTokenArray, led);
            IntArray_insert(colorTokenArray, ((int)(red*bright) << 16) + ((int)(green*bright) << 8) + ((int)(blue*bright)));
    }
}
//Panel 2
bright = 1.0 / (((nCOLs-1) - xPos - (cubeSize-1) + 1) * ((nCOLs-1) - xPos - (cubeSize-1) + 1));
for(int i = yPos; i < yPos + cubeSize; i++) {
    for(int j = zPos; j < zPos + cubeSize; j++) {
        int led;
    if (j % 2 == 0) {
        led = nCOLs*2 * j + nCOLs + i;
    }
    else {
        led = nCOLs*2 * j + ((nCOLs - 1) - i);
    }
            IntArray_insert(intTokenArray, led);
            IntArray_insert(colorTokenArray, ((int)(red*bright) << 16) + ((int)(green*bright) << 8) + ((int)(blue*bright)));
    }
}
//Panel 3
bright = 1.0 / (((nCOLs-1) - zPos - (cubeSize-1) + 1) * ((nCOLs-1) - zPos - (cubeSize-1) + 1));   
for(int i = xPos; i < xPos + cubeSize; i++) {
    for(int j = yPos; j < yPos + cubeSize; j++) {
        int led;
    if (j % 2 == 0) {
        led = nCOLs * j + i + 200;
    }
    else {
        led = nCOLs * j + ((nCOLs - 1) - i) + 200;
    }
            IntArray_insert(intTokenArray, led);
            IntArray_insert(colorTokenArray, ((int)(red*bright) << 16) + ((int)(green*bright) << 8) + ((int)(blue*bright)));
    }
}
$put(indexes, intTokenArray);
$put(colors, colorTokenArray);
/**/

/***wrapupBlock***/
/**/

