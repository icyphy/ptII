/***declareBlock***/
struct doubleArray {
    int size;           // size of the array.
    double* elements;    // array of Token elements.
    //unsigned char elementsType;  // type of all the elements.
};
typedef struct doubleArray* DoubleArrayToken;
/**/

/***funcDeclareBlock***/
Token DoubleArray_new(char type, int size, int given, ...);   
double DoubleArray_get(Token token, int i) {   
    return token.payload.DoubleArray->elements[i];
}
/**/

/***newBlock***/
// make a new array from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
// The rest of the arguments should be of type Token *.
Token DoubleArray_new(int size, int given, ...) {   
    va_list argp; 
    int i;
    Token result;
    
    result.type = TYPE_Array;
    result.payload.DoubleArray = (DoubleArrayToken) malloc(sizeof(struct doubleArray));
    result.payload.DoubleArray->size = size;

	// Allocate an new array of Tokens.
    result.payload.DoubleArray->elements = (int*) calloc(size, sizeof(double));

    if (given > 0) {
        va_start(argp, given);

		for (i = 0; i < given; i++) {
			result.payload.DoubleArray->elements[i] = va_arg(argp, double);
		}    
		
	    va_end(argp);
	}
    return result;
}    
/**/

/***deleteBlock***/
Token DoubleArray_delete(Token token, ...) { 
    free(token.payload.DoubleArray->elements);
    free(token.payload.DoubleArray);
}
/**/


/***equalsBlock***/
Token DoubleArray_equals(Token thisToken, ...) {
	int i;
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);

	if (thisToken.payload.DoubleArray->size != otherToken.payload.DoubleArray->size) {
		return Boolean_new(false);
	}
	for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
	 	if (DoubleArray_get(thisToken, i) != DoubleArray_get(otherToken, i)) {
			return Boolean_new(false);
	 	}
	}
	return Boolean_new(true);
}
/**/


/***convertBlock***/
Token DoubleArray_convert(Token token, ...) {

    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token = DoubleArray_new(1, 1, token.payload.Double);
                break;
        #endif

        #ifdef TYPE_Double
            case TYPE_Double:
                token = DoubleArray_new(1, 1, token.payload.Double);
                break;
        #endif
        
        default:
            // FIXME: not finished
            fprintf(stderr, "DoubleArray_convert: Conversion from an unsupported type. (%d)\n", token.type);
            break;
    }
    return token;
}    
/**/

/***printBlock***/
Token DoubleArray_print(Token thisToken, ...) {
	// Token string = DoubleArray_toString(thisToken);
	// printf(string.payload.String);
	// free(string.payload.String);
	
    int i;
    printf("{");
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        printf("%g", thisToken.payload.DoubleArray->elements[i]);
    }
    printf("}");
}
/**/

/***toStringBlock***/
Token DoubleArray_toString(Token thisToken, ...) {
    int i;
    int currentSize, allocatedSize;
    char* string;
	Token elementString;

	allocatedSize = 256;
	string = (char*) malloc(allocatedSize);
	string[0] = '{';
	string[1] = '\0';
	currentSize = 2;

    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        if (i != 0) {
			strcat(string, ", ");
        }
        elementString = ftoa(thisToken.payload.DoubleArray->elements[i]);
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
Token DoubleArray_toExpression(Token thisToken, ...) {
	return DoubleArray_toString(thisToken);
}
/**/

/***addBlock***/
// Assume the given otherToken is int array type.
// FIXME: We will support other types in the future.
Token DoubleArray_add(Token thisToken, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);

	result = DoubleArray_new(thisToken.payload.DoubleArray->size, 0);
	
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
	  	result.payload.DoubleArray->elements[i] = DoubleArray_get(thisToken, i) + DoubleArray_get(otherToken, i);
	}
	return result;
}
/**/
