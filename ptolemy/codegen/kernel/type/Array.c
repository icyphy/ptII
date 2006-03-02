/***declareBlock***/
#include <stdarg.h>     // Needed Array_new va_* macros

struct array {
    int size;           // size of the array.
    Token* elements;    // array of Token elements.
};
typedef struct array* ArrayToken;
/**/

/***funcDeclareBlock***/
Token Array_convert(Token token);
Token Array_print(Token thisToken);

Token Array_get(Token token, int i) {   
    return token.payload.Array->elements[i];
}
/**/

/***newBlock***/
// make a new array from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
Token Array_new(int size, int given, ...) {   
    va_list argp; 
    int i;
    char elementType;
    Token* element;
    boolean doConvert = false;

    Token result;
    result.type = TYPE_Array;
    result.payload.Array = (ArrayToken) malloc(sizeof(struct array));
    result.payload.Array->size = size;
    if (given > 0) {
        va_start(argp, given);
        element = va_arg(argp, Token*);
        elementType = element->type;
    }

    // Allocate an new array of Tokens.
    result.payload.Array->elements = (Token*) calloc(size, sizeof(Token));
    for (i = 0; i < given; i++) {
        if (element->type != elementType) {
            doConvert = true;

            // Get the max type.
            if (element->type > elementType) {
                elementType = element->type;
            }
        }
        result.payload.Array->elements[i] = *element;
        element = va_arg(argp, Token*);
    }
    
    // If elements are not of the same type, 
    // convert all the elements to the max type.
    if (doConvert) {
        for (i = 0; i < given; i++) {
            // Don't cast to a Token here, the MS VisualC compiler fails
            result.payload.Array->elements[i] = functionTable[elementType][FUNC_convert](result.payload.Array->elements[i]);
        }
    }
    return result;
}    
/**/

/***deleteBlock***/
Token Array_delete(Token token) {   
    // Delete each elements.
    for (i = 0; i < token.payload.Array->size; i++) {
        functionTable[token->elements[i]->type][FUNC_delete](token->elements[i]);
    }
    free(token.payload.Array->elements);
    free(token.payload.Array);
}
/**/

/***convertBlock***/
Token Array_convert(Token token) {
    Token oldToken = token;
    Token result = token;    // return the old pointer by default.

    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                result = Array_new(1, TYPE_Int, token);
                break;
        #endif
        
        #ifdef TYPE_Double
            case TYPE_Double:
                result = Array_new(1, TYPE_Double, token);
                break;
        #endif
        
        #ifdef TYPE_String
            case TYPE_String:
                result = Array_new(1, TYPE_String, token);
                break;
        #endif
        
        default:
            // FIXME: not finished
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }
    return result;
}    
/**/

/***printBlock***/
Token Array_print(Token thisToken) {
    int i;
    printf("{");
    for (i = 0; i < thisToken.payload.Array->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        functionTable[thisToken.payload.Array->elements[i].type][FUNC_print](thisToken.payload.Array->elements[i]);
    }
    printf("}");
}
/**/
