/***globalBlock (<types>)***/
#define MISSING 0

typedef struct token Token;

struct token {                  // Base type for tokens.
    unsigned char type;         // TYPE field has to be the first field.
    union typeMembers {
        <types>                 // type member declarations [i.e. Type1Token Type1;]
    } payload;
};

int atoi (char* s);             // standard c function.

double atof (char* s);          // standard c function.

long atol (char* s);            // standard c function.
    
char* itoa (int i) {
    char* string = (Token*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;       
}

char* ftoa (double d) {
    char* string = (Token*) malloc(sizeof(char) * 12);
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
