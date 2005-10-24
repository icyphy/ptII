/***declareBlock***/
struct array {
    char elementsType;  // type of all the elements.
    int size;           // size of the array.
    Token **elements;   // array of pointers to the elements. 
};
typedef struct array* ArrayToken;

Token* Array_convert(Token* token);
/**/


/***newBlock***/
// make a new array from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
Token* Array_new(int size, ...) {   
    int i;
    Token* element = (Token*) (&size + 1);
    char doConvert = 0; // false

    Token result = (Token*) malloc(sizeof(Token*));
    result->payload.Array = (ArrayToken*) malloc(sizeof(ArrayToken));
    result->elements = (Token**) calloc(size, sizeof(Token*));
    result->type = TYPE_Array;
    result->size = size;
    result->elementsType = element->type;

    for (i = 0; i < size; i++, element++;) {
        if (element->type != result->elementsType) {
            doConvert = 1;  // true

            // Get the max type.
            if (element->type > result->elementsType) {
                result->elementsType = element->type;
            }
        }
        result->elements[i] = *element;
    }

    // If elements are not of the same type, 
    // convert all the elements to the max type.
    if (doConvert) {
        for (i = 0; i < size; i++) {
            result->elements[i] = (Token*) functionTable[result->elementsType][FUNC_convert](result->elements[i]);
        }
    }
    return result;
}    
/**/

/***deleteBlock***/
void Array_delete(Token* token) {   
    // Delete each elements.
    for (i = 0; i < token->payload.Array->size; i++) {
        functionTable[token->elements[i]->type][FUNC_delete](token->elements[i]);
    }
    free(token->payload.Array);
    free(token);
}
/**/

/***convertBlock***/
Token* Array_convert(Token* token) {
    Token* oldToken = token;
    Token* result = token;    // return the old pointer by default.

    switch (token->type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                result = Array_new(1, TYPE_Int, token, TYPE_Int);
                break;
        #endif
        
        #ifdef TYPE_Double
            case TYPE_Double:
                result = Array_new(1, TYPE_Double, token, TYPE_Double);
                break;
        #endif
        
        #ifdef TYPE_String
            case TYPE_String:
                result = Array_new(1, TYPE_String, token, TYPE_String);
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
void Array_print(Token* thisToken) {
    int i;
    printf("{");
    for (i = 0; i < thisToken->payload.Array->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        functionTable[((Array*) thisToken)->elementsType][FUNC_print](thisToken->payload.Array->elements[i]);
    }
    printf("}");
}
/**/
