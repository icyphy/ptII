/***constantsBlock***/
#define MISSING 0
#define boolean unsigned char
#define false 0
#define true 1
/**/

/***funcHeaderBlock ($function)***/
Token $function (Token thisToken, ...);
/**/

/***tokenDeclareBlock ($types)***/
struct token {                  // Base type for tokens.
    unsigned char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
$types                 
    } payload;
};
/**/


/***convertPrimitivesBlock***/
//int atoi (char* s);             // standard c function.
//double atof (char* s);          // standard c function.
//long atol (char* s);            // standard c function.
    
char* myItoa (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;       
}

char* myLtoa (long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf((char*) string, "%d", l);
    return string;       
}

char* myFtoa (double d) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%g", d);
    return string;       
}

char* myBtoa (char b) {
    char* string = (char*) malloc(sizeof(char) * 6);
    if (b) {
        strcpy(string, "true");
    } else {
        strcpy(string, "false");
    }
}

int myFtoi (double d) {
    return floor(d);
}

double myItof (int i) {
    return (double) i;
}
/**/
