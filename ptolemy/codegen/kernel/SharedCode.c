/***constantsBlock***/
#define MISSING 0
#define false 0
#define true 1
/**/


/***tokenDeclareBlock (<types>)***/
struct token {                  // Base type for tokens.
    unsigned char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
<types>                 
    } payload;
};
typedef struct token Token;
/**/


/***convertPrimitivesBlock***/
//int atoi (char* s);             // standard c function.
//double atof (char* s);          // standard c function.
//long atol (char* s);            // standard c function.
    
char* itoa (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;       
}

char* ltoa (long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf((char*) string, "%d", l);
    return string;       
}

char* ftoa (double d) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%g", d);
    return string;       
}

char* btoa (char b) {
    if (b) {
        return "true";
    } else {
        return "false";
    }
}

int ftoi (double d) {
    return floor(d);
}

double itof (int i) {
    return (double) i;
}
/**/
