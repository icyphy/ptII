/***declareBlock***/
#include <stdarg.h>     // Needed Matrix_new va_* macros

struct matrix {
    unsigned int row;            // number of rows.
    unsigned int column;         // number of columns.
    Token *elements;            // matrix of pointers to the elements. 
    //unsigned char elementsType;  // type of all the elements.
};

typedef struct matrix* MatrixToken;
/**/


/***funcDeclareBlock***/
Token Matrix_new(int row, int column, int given, ...);
Token Matrix_get(Token token, int row, int column) {   
    return token.payload.Matrix->elements[column * token.payload.Matrix->row + row];
}
/**/


/***deleteBlock***/
Token Matrix_delete(Token token, ...) { 
	int i, j;
	  
    // Delete each elements.
    for (i = 0; i < token.payload.Matrix->column; i++) {
	    for (j = 0; j < token.payload.Matrix->row; j++) {
        	functionTable[Matrix_get(j, i).type][FUNC_delete](Matrix_get(j, i));
        }
    }
    free(token.payload.Matrix->elements);
    free(token.payload.Matrix);
}
/**/

/***convertBlock***/
Token Matrix_convert(Token token, ...) {
    fprintf(stderr, "Matrix_convert() not yet implemented.\n");
}
/**/


/***newBlock***/
// make a new matrix from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
// The rest of the arguments should be of type Token *.
Token Matrix_new(int row, int column, int given, ...) {
    va_list argp; 
    int i;
    char elementType;
    Token element;
    boolean doConvert = false;

    Token result;
    result.type = TYPE_Matrix;
    result.payload.Matrix = (MatrixToken) malloc(sizeof(struct matrix));
    result.payload.Matrix->row = row;
    result.payload.Matrix->column = column;

	// Allocate a new matrix of Tokens.
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
Token Matrix_equals(Token this, ...) {
	int i, j;
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

	if (( this.payload.Matrix->row != otherToken.payload.Matrix->row ) ||
		( this.payload.Matrix->column != otherToken.payload.Matrix->column )) {
			return Boolean_new(false);
	}
	for (i = 0; i < this.payload.Matrix->column; i++) { 
		for (j = 0; j < this.payload.Matrix->row; j++) { 
		 	if (!functionTable[Matrix_get(token, j, i).type][FUNC_equals](Matrix_get(this, j, i), Matrix_get(otherToken, j, i))).payload.Boolean) {
				return Boolean_new(false);
		 	}
		 }
	}
	return Boolean_new(true);
}
/**/


/***printBlock***/
Token Matrix_print(Token this, ...) {
	// Token string = Matrix_toString(this);
	// printf(string.payload.String);
	// free(string.payload.String);

    int i, j;
    printf("[");
    for (i = 0; i < this.payload.Matrix->column; i++) {
        if (i != 0) {
            printf("; ");
        }
	    for (j = 0; j < this.payload.Matrix->row; j++) {
	        if (j != 0) {
	            printf(", ");
	        }
	        functionTable[this.payload.Matrix->elements[i * this.payload.Matrix->row + j].type][FUNC_print](this.payload.Matrix->elements[i]);
	    }
	}
    printf("]");
}
/**/


/***toStringBlock***/
Token Matrix_toString(Token this, ...) {
    int i, j;
    int currentSize;
    int allocatedSize;
	char* string;
	Token elementString;

	allocatedSize = 512;
	string = (char*) malloc(allocatedSize);
	string[0] = '[';
	string[1] = '\0';
	currentSize = 2;
    for (i = 0; i < this.payload.Matrix->column; i++) {
        if (i != 0) {
			strcat(string, "; ");
        }
	    for (j = 0; j < this.payload.Matrix->row; j++) {
	        if (j != 0) {
				strcat(string, ", ");
	        }
	        elementString = functionTable[Matrix_get(this, j, i).type][FUNC_toString](Matrix_get(this, j, i));
			currentSize += strlen(elementString.payload.String);
	        if (currentSize > allocatedSize) {
	        	allocatedSize *= 2;
				string = (char*) realloc(string, allocatedSize);
	        }
	
	        strcat(string, elementString.payload.String);
	        free(elementString.payload.String);
	    }
    }
	strcat(string, "]");
	return String_new(string);
}
/**/

/***toExpressionBlock***/
Token Matrix_toExpression(Token this, ...) {
	return Matrix_toString(this);
}
/**/

