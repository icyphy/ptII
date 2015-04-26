/***preinitBlock***/
#include <math.h>
/**/

/***initBlock***/
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
double hue_in = $getAndFree(hue);
double saturation_in = $getAndFree(saturation);
double brightness_in = $getAndFree(brightness);
    int red = 0, green = 0, blue = 0;
    if (saturation == 0) {
        red = green = blue = (int) (brightness_in * 255.0 + 0.5);
    } else {
        double h = (hue_in - (double)floor(hue_in)) * 6.0;
        double f = h - (double)floor(h);
        double p = brightness_in * (1.0 - saturation_in);
        double q = brightness_in * (1.0 - saturation_in * f);
        double t = brightness_in * (1.0 - (saturation_in * (1.0 - f)));
        switch ((int) h) {
            case 0:
                red = (int) (brightness_in * 255.0 + 0.5);
                green = (int) (t * 255.0 + 0.5);
                blue = (int) (p * 255.0 + 0.5);
                break;
            case 1:
                red = (int) (q * 255.0 + 0.5);
                green = (int) (brightness_in * 255.0 + 0.5);
                blue = (int) (p * 255.0 + 0.5);
                break;
            case 2:
                red = (int) (p * 255.0 + 0.5);
                green = (int) (brightness_in * 255.0 + 0.5);
                blue = (int) (t * 255.0 + 0.5);
                break;
            case 3:
                red = (int) (p * 255.0 + 0.5);
                green = (int) (q * 255.0 + 0.5);
                blue = (int) (brightness_in * 255.0 + 0.5);
                break;
            case 4:
                red = (int) (t * 255.0 + 0.5);
                green = (int) (p * 255.0 + 0.5);
                blue = (int) (brightness_in * 255.0 + 0.5);
                break;
            case 5:
                red = (int) (brightness_in * 255.0 + 0.5);
                green = (int) (p * 255.0 + 0.5);
                blue = (int) (q * 255.0 + 0.5);
                break;
        }
    }
    $put(r, red);
    $put(g, green);
    $put(b, blue);
/**/

/***wrapupBlock***/
/**/

