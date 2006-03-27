/***declareBlock***/
#include <stdarg.h>     // Needed Array_new va_* macros

struct array {
    int size;           // size of the array.
    Token* elements;    // array of Token elements.
    //unsigned char elementsType;  // type of all the elements.
};
typedef struct array* ArrayToken;
/**/

/***funcDeclareBlock***/
Token Array_convert(Token token, ...);
Token Array_print(Token thisToken, ...);
Token  Array_toString(Token thisToken, ...);
Token Array_toExpression(Token thisToken, ...);
Token Array_equals(Token thisToken, ...);

Token Array_get(Token token, int i) {   
    return token.payload.Array->elements[i];
}
/**/

/***newBlock***/
// make a new array from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
// The rest of the arguments should be of type Token *.
Token Array_new(int size, int given, ...) {   
    va_list argp; 
    int i;
    char elementType;
    Token element;
    boolean doConvert = false;

    Token result;
    result.type = TYPE_Array;
    result.payload.Array = (ArrayToken) malloc(sizeof(struct array));
    result.payload.Array->size = size;

	// Allocate an new array of Tokens.
    result.payload.Array->elements = (Token*) calloc(size, sizeof(Token));

    if (given > 0) {
		// Set the first element.
        va_start(argp, given);
		element = va_arg(argp, Token);
		elementType = element.type;
		result.payload.Array->elements[0] = element;

		for (i = 1; i < given; i++) {
			element = va_arg(argp, Token);
			if (element.type != elementType) {
				doConvert = true;

				// Get the max type.
				if (element.type > elementType) {
					elementType = element.type;
				}
			}
			result.payload.Array->elements[i] = element;
		}
    
		// If elements are not of the same type, 
		// convert all the elements to the max type.
		if (doConvert) {
			for (i = 0; i < given; i++) {
				// Don't cast to a Token here, the MS VisualC compiler fails
				result.payload.Array->elements[i] = functionTable[elementType][FUNC_convert](result.payload.Array->elements[i]);
			}
		}
	    va_end(argp);
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


/***equalsBlock***/
Token Array_equals(Token thisToken, ...) {
	int i;
	if (thisToken.payload.Array->size != otherToken.payload.Array->size) {
		return Boolean_new(false);
	}
	for (i = 0; i < thisToken.size; i++) {
	 	if (!$typeFunc(Array_get(thisToken, i), equals(Array_get(otherToken, i)))) {
			return Boolean_new(false);
	 	}
	}
	return Boolean_new(true);
}
/**/


/***convertBlock***/
Token Array_convert(Token token, ...) {
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
            fprintf(stderr, "Array_convert: Conversion from an unsupported type. (%d)\n",
                    token.type);
            break;
    }
    return result;
}    
/**/

/***printBlock***/
Token Array_print(Token thisToken, ...) {
	// Token string = Array_toString(thisToken);
	// printf(string.payload.String);
	// free(string.payload.String);
	
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

/***toStringBlock***/
Token Array_toString(Token thisToken, ...) {
    int i;
    int currentSize, allocatedSize;
    char* string;
	Token elementString;

	allocatedSize = 256;
	string = (char*) malloc(allocatedSize);
	string[0] = '{';
	string[1] = '\0';
	currentSize = 2;

    for (i = 0; i < thisToken.payload.Array->size; i++) {
        if (i != 0) {
			strcat(string, ", ");
        }
        elementString = functionTable[thisToken.payload.Array->elements[i].type][FUNC_toString](thisToken.payload.Array->elements[i]);
		currentSize += strlen(elementString.payload.String);
        if (currentSize > allocatedSize) {
        	allocatedSize *= 2;
			string = (char*) realloc(string, allocatedSize);
        }

        strcat(string, elementString.payload.String);
        free(elementString.payload.String);
    }
	strcat(string, "}");
	return String_new(string);
}
/**/

/***toExpressionBlock***/
Token Array_toExpression(Token thisToken, ...) {
	return Array_toString(thisToken);
}
/**/
