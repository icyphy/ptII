/***constantsBlock***/

// Constants.
#define MISSING 0

// boolean for AVR.  A mess.  I'm sorry...
#ifndef __AVR__
typedef unsigned char boolean;
#else

#ifndef  __cplusplus
typedef unsigned char boolean;
#else
typedef uint8_t boolean;
#endif

#endif

#define DO_NOT_CALL_EXIT
#ifdef DO_NOT_CALL_EXIT

#ifdef PTJNI
extern void throwInternalErrorException();
#define ptExit(x) throwInternalErrorException()
#else /* PTJNI */
#define ptExit(x) {}
#endif /* PTJNI */

#else /* DO_NOT_CALL_EXIT */
#define ptExit(x) exit(x)
#endif /* DO_NOT_CALL_EXIT */

typedef char* string;

/* Infinity is a valid Ptolemy identifier. */
#ifdef __AVR__
#define Infinity INFINITY
#else
#define Infinity HUGE_VAL
#endif

#ifdef __linux__
/* Linux tends to have NAN. */
#define NaN (__builtin_nanf (""))
#else /*linux*/
#define NaN nanf(0)
#endif /*linux*/

#define false 0
#define true 1

/**/

/***funcHeaderBlock ($function)***/

Token* $function (Token *thisToken, ...);
/**/

/***tokenDeclareBlock ($types)***/

// Token structure containing the specified types.
struct token {         // Base type for tokens.
    char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
        $types
    } payload;
};
/**/


/***convertPrimitivesBlockDeclaration***/
#define StringtoInt atoi
#define StringtoDouble atof
#define StringtoLong atol
#define DoubletoInt (int)
#define InttoDouble (double)
#define InttoLong (long)

char* InttoString (int i);
char* LongtoString (long long l);
char* DoubletoString (double d);
char* BooleantoString (boolean b);
char* UnsignedBytetoString (unsigned char b);

/**/

/***convertPrimitivesBlockImplementation***/

char* InttoString (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;
}

char* LongtoString (long long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf(string, "%lld", l);
    return string;
}

char* DoubletoString (double d) {
    int index;
    char* string = (char*) malloc(sizeof(char) * 20);
    sprintf(string, "%.14g", d);

        // Make sure that there is a decimal point.
    if (strrchr(string, '.') == NULL) {
        index = strlen(string);
        if (index == 20) {
            string = (char*) realloc(string, sizeof(char) * 22);
        }
        string[index] = '.';
        string[index + 1] = '0';
        string[index + 2] = '\0';
    }
    return string;
}

char* BooleantoString (boolean b) {
    char *results;
    if (b) {
        // AVR does not have strdup
        results = (char*) malloc(sizeof(char) * 5);
        strcpy(results, "true");
    } else {
        results = (char*) malloc(sizeof(char) * 6);
        strcpy(results, "false");
    }
    return results;
}

char* UnsignedBytetoString (unsigned char b) {
    char* string = (char*) malloc(sizeof(char) * 3);
    sprintf(string, "%d", (int) b);
    return string;
}

/**/

/*** unsupportedTypeFunctionDeclaration ***/
Token* unsupportedTypeFunction(Token* token, ...);
/**/

/*** unsupportedTypeFunction ***/
/* We share one method between all types so as to reduce code size. */
Token* unsupportedTypeFunction(Token* token, ...) {
    fprintf(stderr, "Attempted to call unsupported method on a type.\n");
    exit(1);
    return NULL;
}
/**/

/*** scalarDeleteFunctionDeclaration ***/
Token* scalarDelete(Token *token, ...);
/**/

/*** scalarDeleteFunction ***/
/* We share one method between all scalar types so as to reduce code size. */
Token* scalarDelete(Token *token, ...) {
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return NULL;
}
/**/
