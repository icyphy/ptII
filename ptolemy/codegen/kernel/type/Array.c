/***declareBlock***/
struct array {
    int size;           			// size of the array.
    Token* elements;    			// array of Token elements.
    //char elementType;  				// type of the elements.
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
    char elementType;
    
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
		
		// elementType is given as the last argument.
		elementType = va_arg(argp, int);			
		//result.payload.Array->elementType = elementType;

		// convert the elements if needed.
		for (i = 0; i < given; i++) {
			if (Array_get(result, i).type != elementType) {
				result.payload.Array->elements[i] = functionTable[elementType][FUNC_convert](Array_get(result, i));
			}
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


/***equalsBlock***/
Token Array_equals(Token this, ...) {
	int i;
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

	if (this.payload.Array->size != otherToken.payload.Array->size) {
		return Boolean_new(false);
	}
	for (i = 0; i < this.payload.Array->size; i++) {
	 	if (!functionTable[Array_get(this, i).type][FUNC_equals](Array_get(this, i), Array_get(otherToken, i)).payload.Boolean) {
			return Boolean_new(false);
	 	}
	}

    va_end(argp);
	return Boolean_new(true);
}
/**/

/***printBlock***/
Token Array_print(Token this, ...) {
	// Token string = Array_toString(this);
	// printf(string.payload.String);
	// free(string.payload.String);
	
    int i;
    printf("{");
    for (i = 0; i < this.payload.Array->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        functionTable[this.payload.Array->elements[i].type][FUNC_print](this.payload.Array->elements[i]);
    }
    printf("}");
}
/**/

/***toStringBlock***/
Token Array_toString(Token this, ...) {
    int i;
    int currentSize, allocatedSize;
    char* string;
	Token elementString;

	allocatedSize = 256;
	string = (char*) malloc(allocatedSize);
	string[0] = '{';
	string[1] = '\0';
	currentSize = 2;

    for (i = 0; i < this.payload.Array->size; i++) {
        if (i != 0) {
			strcat(string, ", ");
        }
        elementString = functionTable[this.payload.Array->elements[i].type][FUNC_toString](this.payload.Array->elements[i]);
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
Token Array_toExpression(Token this, ...) {
	return Array_toString(this);
}
/**/

/***addBlock***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_add(Token this, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

	result = Array_new(this.payload.Array->size, 0);
	
    for (i = 0; i < this.payload.Array->size; i++) {
	  	result.payload.Array->elements[i] = functionTable[Array_get(this, i).type][FUNC_add](Array_get(this, i), Array_get(otherToken, i));
	}

    va_end(argp);
	return result;
}
/**/


/***substractBlock***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_substract(Token this, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

	result = Array_new(this.payload.Array->size, 0);
	
    for (i = 0; i < this.payload.Array->size; i++) {
	  	result.payload.Array->elements[i] = functionTable[Array_get(this, i).type][FUNC_substract](Array_get(this, i), Array_get(otherToken, i));
	}

    va_end(argp);
	return result;
}
/**/


/***multiplyBlock***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_multiply(Token this, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

	result = Array_new(this.payload.Array->size, 0);
	
    for (i = 0; i < this.payload.Array->size; i++) {
	  	result.payload.Array->elements[i] = functionTable[Array_get(this, i).type][FUNC_multiply](Array_get(this, i), Array_get(otherToken, i));
	}

    va_end(argp);
	return result;
}
/**/

/***divideBlock***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_divide(Token this, ...) {
	int i;
    va_list argp; 
	Token result; 
	Token otherToken;
	Token element;
	
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

	result = Array_new(this.payload.Array->size, 0);
	
    for (i = 0; i < this.payload.Array->size; i++) {
    	element = Array_get(this, i);
	  	result.payload.Array->elements[i] = functionTable[element.type][FUNC_divide](element, Array_get(otherToken, i));
	}

    va_end(argp);
	return result;
}
/**/

/***negateBlock***/
// Return a new Array token.
Token Array_negate(Token this, ...) {
	int i;
	Token result; 

	result = Array_new(this.payload.Array->size, 0);

    for (i = 0; i < this.payload.Array->size; i++) {
	  	result.payload.Array->elements[i] = functionTable[Array_get(this, i).type][FUNC_negate](Array_get(this, i));
	}
	return result;
}
/**/

/***zeroBlock***/
Token Array_zero(Token token, ...) {
	Token result;
	Token element;
	int i;
	
	result = Array_new(token.payload.Array->size, 0);
	for (i = 0; i < token.payload.Array->size; i++) {
		element = Array_get(token, i);
		result.payload.Array->elements[i] = functionTable[element.type][FUNC_zero](element);
	}
	return result;
}
/**/

/***oneBlock***/
Token Array_one(Token token, ...) {
	Token result;
	Token element;
	int i;
	
	result = Array_new(token.payload.Array->size, 0);
	for (i = 0; i < token.payload.Array->size; i++) {
		element = Array_get(token, i);
		result.payload.Array->elements[i] = functionTable[element.type][FUNC_one](element);
	}
	return result;
}
/**/







------------ static function -----------------------------------------------

/***convertBlock***/
// Convert between different types of Array.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token Array_convert(Token token, ...) {
	int i;
	Token result; 
	Token element;
    va_list argp; 
	char targetType;

    va_start(argp, token);
	targetType = va_arg(argp, int);

	if (targetType != token.type) {		
		result = Array_new(token.payload.Array->size, 0);
	
		for (i = 0; i < token.payload.Array->size; i++) {
			element = Array_get(token, i);
			if (targetType != element.type) {
				result.payload.Array->elements[i] = functionTable[targetType][FUNC_convert](element);
			}
		}
	}

    va_end(argp);
    return result;
}    
/**/

