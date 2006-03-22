/***declareBlock***/
#include <stdarg.h>     // Needed Matrix_new va_* macros

struct matrix {
    unsigned int row;            // number of rows.
    unsigned int column;         // number of columns.
    Token **elements;            // matrix of pointers to the elements. 
    //unsigned char elementsType;  // type of all the elements.
};

typedef struct matrix* MatrixToken;
/**/


/***funcDeclareBlock***/
//Token Matrix_convert(Token token);
Token Matrix_print(Token thisToken, ...);
Token Matrix_toString(Token thisToken, ...);
Token Matrix_toExpression(Token thisToken, ...);
Token Matrix_equals(Token thisToken, ...);

Token Matrix_get(Token token, int column, int row) {   
    return token.payload.Matrix->elements[column * token.payload.Matrix->row + row];
}
/**/

/***newBlock***/
// make a new matrix from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
// The rest of the arguments should be of type Token *.
Token Matrix_new(int column, int row, int given, ...) {
    va_list argp; 
    int i;
    char elementType;
    Token element;
    boolean doConvert = false;

    Token result;
    result.type = TYPE_Matrix;
    result.payload.Matrix = (MatrixToken) malloc(sizeof(struct array));
    result.payload.Matrix->size = size;

	// Allocate an new 2-dimenional array (matrix) of Tokens.
    result.payload.Matrix->elements = (Token*) calloc(row * column, sizeof(Token));

    if (given > 0) {
		// Set the first element.
        va_start(argp, given);
		for (i = 0; i < given; i++) {
			element = va_arg(argp, Token);
			result.payload.Matrix->elements[i] = element;
		}    
	    va_end(argp);
	}
    return result;
}    
/**/

/***equalsBlock***/
Token Matrix_equals(Token thisToken, ...) {
	int i, j;
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);

	if (( thisToken.payload.Matrix->row != otherToken.payload.Array->row ) ||
		( thisToken.payload.Matrix->column != otherToken.payload.Array->column )) {
			return Boolean_new(false);
	}
	for (i = 0; i < thisToken.payload.Matrix->column; i++) { 
		for (j = 0; j < thisToken.payload.Matrix->row; j++) { 
		 	if (!$typeFunc(Array_get(thisToken, i, j), equals(Array_get(otherToken, i, j)))) {
				return Boolean_new(false);
		 	}
		 }
	}
	return Boolean_new(true);
}
/**/


/***printBlock***/
Token Matrix_print(Token thisToken, ...) {
	// Token string = Matrix_toString(thisToken);
	// printf(string.payload.String);
	// free(string.payload.String);

    int i, j;
    printf("{");
    for (i = 0; i < thisToken.payload.Matrix->column; i++) {
        if (i != 0) {
            printf("; ");
        }
	    for (j = 0; j < thisToken.payload.Matrix->row; j++) {
	        if (j != 0) {
	            printf(", ");
	        }
	        functionTable[thisToken.payload.Matrix->elements[i * thisToken.payload.Matrix->row + j].type][FUNC_print](thisToken.payload.Matrix->elements[i]);
	    }
	}
    printf("}");
}
/**/


/***toStringBlock***/
Token Matrix_toString(Token thisToken, ...) {
    int i, j;
    int currentSize;
    int allocatedSize;
	char* string;
	Token elementString;

	allocatedSize = 512;
	string = (char*) malloc(allocatedSize);
	string[0] = '}';
	string[1] = '\0';
	currentSize = 2;
    for (i = 0; i < thisToken.payload.Matrix->column; i++) {
        if (i != 0) {
			strcat(string, "; ");
        }
	    for (j = 0; j < thisToken.payload.Matrix->row; j++) {
	        if (j != 0) {
				strcat(string, ", ");
	        }
	        elementString = functionTable[Matrix_get(thisToken, i, j).type][FUNC_toString](Matrix_get(thisToken, i, j));
			currentSize += strlen(elementString.payload.String);
	        if (currentSize > allocatedSize) {
	        	allocatedSize *= 2;
				string = (char*) realloc(string, allocatedSize);
	        }
	
	        strcat(string, elementString.payload.String);
	        free(elementString.payload.String);
	    }
    }
	strcat(string, "}");
	return String_new(string);
}
/**/

/***toExpressionBlock***/
Token Matrix_toExpression(Token thisToken, ...) {
	return Matrix_toString(thisToken);
}
/**/

