/***declareBlock***/
struct intArray {
    int size;           // size of the array.
    int* elements;    // array of Token elements.
    //unsigned char elementsType;  // type of all the elements.
};
typedef struct intArray* IntArrayToken;
/**/

/***funcDeclareBlock***/
Token IntArray_new(char type, int size, int given, ...);   
int IntArray_get(Token token, int i) {   
    return token.payload.IntArray->elements[i];
}
/**/

/***newBlock***/
// make a new array from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
// The rest of the arguments should be of type Token *.
Token IntArray_new(int size, int given, ...) {   
    va_list argp; 
    int i;
    Token result;
    
    result.type = TYPE_Array;
    result.payload.IntArray = (IntArrayToken) malloc(sizeof(struct intArray));
    result.payload.IntArray->size = size;

	// Allocate an new array of Tokens.
    result.payload.IntArray->elements = (int*) calloc(size, sizeof(int));

    if (given > 0) {
        va_start(argp, given);

		for (i = 0; i < given; i++) {
			result.payload.IntArray->elements[i] = va_arg(argp, int);
		}    
		
	    va_end(argp);
	}
    return result;
}    
/**/

/***deleteBlock***/
Token IntArray_delete(Token token, ...) { 
    free(token.payload.IntArray->elements);
    free(token.payload.IntArray);
}
/**/


/***equalsBlock***/
Token IntArray_equals(Token thisToken, ...) {
	int i;
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);

	if (thisToken.payload.IntArray->size != otherToken.payload.IntArray->size) {
		return Boolean_new(false);
	}
	for (i = 0; i < thisToken.payload.IntArray->size; i++) {
	 	if (IntArray_get(thisToken, i) != IntArray_get(otherToken, i)) {
			return Boolean_new(false);
	 	}
	}
	return Boolean_new(true);
}
/**/


/***convertBlock***/
Token IntArray_convert(Token token, ...) {

    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token = IntArray_new(1, 1, token.payload.Int);
                break;
        #endif
        
        default:
            // FIXME: not finished
            fprintf(stderr, "IntArray_convert: Conversion from an unsupported type. (%d)\n", token.type);
            break;
    }
    return token;
}    
/**/

/***printBlock***/
Token IntArray_print(Token thisToken, ...) {
	// Token string = IntArray_toString(thisToken);
	// printf(string.payload.String);
	// free(string.payload.String);
	
    int i;
    printf("{");
    for (i = 0; i < thisToken.payload.IntArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        printf("%d", thisToken.payload.IntArray->elements[i]);
    }
    printf("}");
}
/**/

/***toStringBlock***/
Token IntArray_toString(Token thisToken, ...) {
    int i;
    int currentSize, allocatedSize;
    char* string;
	Token elementString;

	allocatedSize = 256;
	string = (char*) malloc(allocatedSize);
	string[0] = '{';
	string[1] = '\0';
	currentSize = 2;

    for (i = 0; i < thisToken.payload.IntArray->size; i++) {
        if (i != 0) {
			strcat(string, ", ");
        }
        elementString = myItoa(thisToken.payload.IntArray->elements[i]);
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
Token IntArray_toExpression(Token thisToken, ...) {
	return IntArray_toString(thisToken);
}
/**/

/***addBlock***/
// Assume the given otherToken is int array type.
// FIXME: We will support other types in the future.
Token IntArray_add(Token thisToken, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);

	result = IntArray_new(thisToken.payload.IntArray->size, 0);
	
    for (i = 0; i < thisToken.payload.IntArray->size; i++) {
	  	result.payload.IntArray->elements[i] = IntArray_get(thisToken, i) + IntArray_get(otherToken, i);
	}
	return result;
}
/**/
