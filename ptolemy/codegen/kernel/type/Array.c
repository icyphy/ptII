/***declareBlock***/
struct array {
    int size;           // size of the array.
    Token* elements;    // array of Token elements.
    //unsigned char elementsType;  // type of all the elements.
};
typedef struct array* ArrayToken;
/**/

/***funcDeclareBlock***/
Token Array_new(int size, int given, ...);   
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
    Token result;
    
    result.type = TYPE_Array;
    result.payload.Array = (ArrayToken) malloc(sizeof(struct array));
    result.payload.Array->size = size;

	// Allocate an new array of Tokens.
    result.payload.Array->elements = (Token*) calloc(size, sizeof(Token));

    if (given > 0) {
        va_start(argp, given);

		for (i = 0; i < given; i++) {
			result.payload.Array->elements[i] = va_arg(argp, Token);
		}    
		
	    va_end(argp);
	}
    return result;
}    
/**/

/***deleteBlock***/
Token Array_delete(Token token, ...) { 
	int i;  
    // Delete each elements.
    for (i = 0; i < token.payload.Array->size; i++) {
        functionTable[Array_get(token, i).type][FUNC_delete](Array_get(token, i));
    }
    free(token.payload.Array->elements);
    free(token.payload.Array);
}
/**/

/***cloneBlock***/
Token Array_clone(Token thisToken, ...) {
	int i;
	Token result = Array_new(thisToken.payload.Array->size, 0);

	for (i = 0; i < thisToken.payload.Array->size; i++) {
		result.payload.Array->elements[i] = functionTable[Array_get(thisToken, i).type][FUNC_clone](Array_get(thisToken, i));
	}
	
	return result;
}
/**/


/***equalsBlock***/
Token Array_equals(Token thisToken, ...) {
	int i;
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);

	if (thisToken.payload.Array->size != otherToken.payload.Array->size) {
		return Boolean_new(false);
	}
	for (i = 0; i < thisToken.payload.Array->size; i++) {
	 	if (!functionTable[Array_get(thisToken, i).type][FUNC_equals](Array_get(thisToken, i), Array_get(otherToken, i)).payload.Boolean) {
			return Boolean_new(false);
	 	}
	}
	return Boolean_new(true);
}
/**/


/***convertBlock***/
Token Array_convert(Token token, ...) {
    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token = Array_new(1, 1, token);
                break;
        #endif
        
        #ifdef TYPE_Double
            case TYPE_Double:
                token = Array_new(1, 1, token);
                break;
        #endif
        
        #ifdef TYPE_String
            case TYPE_String:
                token = Array_new(1, 1, token);
                break;
        #endif
        
        default:
            // FIXME: not finished
            fprintf(stderr, "Array_convert: Conversion from an unsupported type. (%d)\n",
                    token.type);
            break;
    }
    return token;
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

/***addBlock***/
// Assume the given otherToken is array type.
// We will support other types in the future.
Token Array_add(Token thisToken, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);

	result = Array_new(thisToken.payload.Array->size, 0);
	
    for (i = 0; i < thisToken.payload.Array->size; i++) {
	  	result.payload.Array->elements[i] = functionTable[Array_get(thisToken, i).type][FUNC_add](Array_get(thisToken, i), Array_get(otherToken, i));
	}
	return result;
}
/**/
