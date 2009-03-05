#include "DoubleArray.h"

/** NOTE: META SUBSTITUTION SYMBOLS
 * $Type: Int, Char, Array, etc.
 * $TYPE_t: TYPE_Int, TYPE_Char, etc.
 * $type: int, char, etc.
 * $type_q: %d, %s, etc.
 * $print_size: 12(int), 22(long), 22(double), 6(boolean)
 */

/***declareBlock***/
struct doublearray {
    int size;                                   // size of the array.
    double* elements;                            // array of Token elements.
    char elementType;                          // type of the elements.
};
typedef struct doublearray* DoubleArrayToken;
/**/

/***funcDeclareBlock***/
// Array_get: get an element of an array.
#define DoubleArray_length(array) ((array).payload.DoubleArray->size)

double DoubleArray_get(Token array, int i) {
	// Token result;
	// result.type = array.payload.DoubleArray->elementType;
	// result.payload.Double = ((double *) array.payload.DoubleArray->elements)[i];
	// return result;
	return ((double *) array.payload.DoubleArray->elements)[i];
}

// Array_set: set an element of an array.
void DoubleArray_set(Token array, int i, double element) {
    ((double *) array.payload.DoubleArray->elements)[i] = element;
}

// Array_resize: Change the size of an array,
// preserving those elements that fit.
void DoubleArray_resize(Token array, int size) {
    array.payload.DoubleArray->size = size;
    array.payload.DoubleArray->elements = (double*) realloc(
    		array.payload.DoubleArray->elements, size * sizeof(double));
}

// DoubleArray_insert: Append the specified element to the end of an array.
void DoubleArray_insert(Token array, double token) {
    int oldSize = array.payload.DoubleArray->size++;
    DoubleArray_resize(array, array.payload.DoubleArray->size);
    ((double *) array.payload.DoubleArray->elements)[oldSize] = token;
}
/**/


/***DoubleArray_new***/
// Array_new: Create a new array with the specified elements.
//  The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token *.
// If the "given" argument is greater than 0, then the last
// argument is expected to be the type that of the elements.
Token DoubleArray_new(int size, int given, ...) { //Revised
    va_list argp;
    int i;
    Token result;
    result.type = TYPE_DoubleArray;
    result.payload.DoubleArray = (DoubleArrayToken) malloc(sizeof(struct doublearray));
    result.payload.DoubleArray->size = size;
    result.payload.DoubleArray->elementType = TYPE_Double;
    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
        // Allocate an new array of Tokens.
        result.payload.DoubleArray->elements =
        (double *) calloc(size, sizeof(double));
        if (given > 0) {
            va_start(argp, given);
            for (i = 0; i < given; i++) {
                Token temp = ((Token) va_arg(argp, Token));
                // if (elementType >= 0 && temp.type != elementType)
                // 	temp = functionTable[(int) elementType][FUNC_convert](temp);
				((double *) result.payload.DoubleArray->elements)[i] = temp.payload.Double;
            }
            va_end(argp);
        }
    }
    return result;
}
/**/


/***DoubleArray_delete***/
// Array_delete: FIXME: What does this do?
Token DoubleArray_delete(Token token, ...) { //Revised
    Token emptyToken;
    //Token element;
    //int i;
    //char elementType;
    // Delete each elements.
    // for (i = 0; i < token.payload.DoubleArray->size; i++) {
    //     elementType = token.payload.DoubleArray->elementType;
    //     element = Array_get(token, i);
    //     functionTable[(int) elementType][FUNC_delete](element);
    // }
    free((double *) token.payload.DoubleArray->elements);
    free(token.payload.DoubleArray);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken;
}
/**/


/***DoubleArray_equals***/
// Array_equals: Test an array for equality with a second array.
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
    	if (!equals_Double_Double(DoubleArray_get(thisToken, i), DoubleArray_get(otherToken, i))) {
    		return Boolean_new(false);
    	}
        // if (!functionTable[(int)Array_get(thisToken, i).type][FUNC_equals]
        //                 (Array_get(thisToken, i), Array_get(otherToken, i)).payload.Boolean) {
        //     return Boolean_new(false);
        // }
    }

    va_end(argp);
    return Boolean_new(true);
}
/**/


/***DoubleArray_isCloseTo***/
// Array_isCloseTo: Test an array to see whether it is close in value to another.
Token DoubleArray_isCloseTo(Token thisToken, ...) {
    int i;
    va_list argp;
    Token otherToken;
    Token tolerance;
    va_start(argp, thisToken);


    otherToken = va_arg(argp, Token);
    otherToken = DoubleArray_convert(otherToken);

    double value1, value2;
    tolerance = va_arg(argp, Token);


    if (thisToken.payload.DoubleArray->size != otherToken.payload.DoubleArray->size) {
        return Boolean_new(false);
    }
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        value1 = DoubleArray_get(thisToken, i);
        value2 = DoubleArray_get(otherToken, i);

        if (fabs(value1 - value2) > tolerance.payload.Double) {
            return Boolean_new(false);
        }
    }
    va_end(argp);
    return Boolean_new(true);
}
/**/


/***DoubleArray_print***/
// DoubleArray_print: Print the contents of an array to standard out.
Token DoubleArray_print(Token thisToken, ...) {
    int i;
    printf("{");
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        printf("%g", DoubleArray_get(thisToken, i));
        // functionTable[(int)thisToken.payload.DoubleArray->elementType][FUNC_print](Array_get(thisToken, i));
    }
    printf("}");
}
/**/


/***DoubleArray_toString***/
// Array_toString: Return a string token with a string representation
// of the specified array.
Token DoubleArray_toString(Token thisToken, ...) {
	int i;
    int currentSize, allocatedSize;
    char* string;
    char elementString[22];
    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    string[1] = '\0';
    currentSize = 2;

    //printf("%d\n", thisToken.payload.DoubleArray->size);
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
    	if (i != 0) {
            strcat(string, ", ");
        }
    	// double temp = DoubleArray_get(thisToken, i);
        sprintf(elementString, "%g", DoubleArray_get(thisToken, i));
        currentSize += strlen(elementString);
        if (currentSize > allocatedSize) {
            allocatedSize *= 2;
            string = (char*) realloc(string, allocatedSize);
        }
        strcat(string, elementString);
    }

    strcat(string, "}");
    return String_new(string);
}
/**/


/***DoubleArray_add***/
// Array_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token DoubleArray_add(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.DoubleArray->size;
    size2 = otherToken.payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = DoubleArray_new(resultSize, 0, TYPE_Double);

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
        	DoubleArray_set(result, i, add_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
        	DoubleArray_set(result, i, add_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
        	DoubleArray_set(result, i, add_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***DoubleArray_subtract***/
// Array_subtract: Subtract the second argument array
// from the first argument array.
// Assume the given otherToken is an array.
// FIXME: Arrays can have scalars subtracted!
// This will cause a nasty seg fault.
// Return a new Array token.
Token DoubleArray_subtract(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.DoubleArray->size;
    size2 = otherToken.payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = DoubleArray_new(resultSize, 0, TYPE_Double);

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
        	DoubleArray_set(result, i, substract_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
        	DoubleArray_set(result, i, substract_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
        	DoubleArray_set(result, i, substract_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***DoubleArray_multiply***/
// Array_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
Token DoubleArray_multiply(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.DoubleArray->size;
    size2 = otherToken.payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = DoubleArray_new(resultSize, 0, TYPE_Double);

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
        	DoubleArray_set(result, i, multiply_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
        	DoubleArray_set(result, i, multiply_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
        	DoubleArray_set(result, i, multiply_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***DoubleArray_divide***/
// Array_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token DoubleArray_divide(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.DoubleArray->size;
    size2 = otherToken.payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = DoubleArray_new(resultSize, 0, TYPE_Double);

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
        	DoubleArray_set(result, i, divide_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
        	DoubleArray_set(result, i, divide_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
        	DoubleArray_set(result, i, divide_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***DoubleArray_negate***/
// Array_negate: Negate each element of an array.
// Return a new Array token.
Token DoubleArray_negate(Token thisToken, ...) {
    int i;
    Token result;
    result = DoubleArray_new(thisToken.payload.DoubleArray->size, 0, TYPE_Double);

    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        DoubleArray_set(thisToken, i, negate_Double(DoubleArray_get(thisToken, i)));
    }
    return result;
}
/**/


/***DoubleArray_zero***/
// Array_zero: Return an array like the specified
// array but with zeros of the same type.
Token DoubleArray_zero(Token thisToken, ...) {
    Token result;
    Token element;
    int i;

    result = DoubleArray_new(thisToken.payload.DoubleArray->size, 0, TYPE_Double);
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        DoubleArray_set(thisToken, i, zero_Double(DoubleArray_get(thisToken, i)));
    }
    return result;
}
/**/


/***DoubleArray_one***/
// Array_one: Return an array like the specified
// array but with ones of the same type.
Token DoubleArray_one(Token thisToken, ...) {
    Token result;
    Token element;
    int i;

    result = DoubleArray_new(thisToken.payload.DoubleArray->size, 0, TYPE_Double);
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        DoubleArray_set(thisToken, i, one_Double(DoubleArray_get(thisToken, i)));
    }
    return result;
}
/**/


/***DoubleArray_clone***/
// Array_clone: Return a new array just like the
// specified array.
Token DoubleArray_clone(Token thisToken, ...) {
    Token result;
    Token element;
    int i;

    result = DoubleArray_new(thisToken.payload.DoubleArray->size, 0, TYPE_Double);
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
        DoubleArray_set(thisToken, i, clone_Double(DoubleArray_get(thisToken, i)));
    }
    return result;
}
/**/


/***DoubleArray_sum***/
// FIXME: WHAT DOES THIS FUNCTION DO?
Token DoubleArray_sum(Token token) {
	double result;
	int i;

	if (token.payload.DoubleArray->size <= 0) {
		return token;
	} else {
		result = DoubleArray_get(token, 0);
	}

    for (i = 1; i < token.payload.DoubleArray->size; i++) {
    	result = add_Double_Double(result, DoubleArray_get(token, i));
    }
    return Double_new(result);
}
/**/

/***DoubleArray_repeat***/
Token DoubleArray_repeat(int number, double value) {
	Token result;
	result = DoubleArray_new(number, 0);
	int i;

	for (i = 0; i < number; i++) {
        Array_set(result, i, value);
    }
    return result;
}
/**/


/***DoubleArray_convert***/
// DoubleArray_convert: Convert the first argument array
// into the type specified by the second argument.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token DoubleArray_convert(Token token, ...) {
//    int i;
//    Token result;
//    Token element;
//    va_list argp;
//    char targetType;

    // FIXME: do nothing for now, because we haven't figure out
    // the proper design for this.

//    va_start(argp, token);
//    targetType = va_arg(argp, int);

    // FIXME: HOW DO WE KNOW WHICH TYPE WE'RE CONVERTING TO?
//    result = DoubleArray_new(token.payload.DoubleArray->size, 0);
//
//    for (i = 0; i < token.payload.DoubleArray->size; i++) {
//        element = Array_get(token, i);
//        if (targetType != token.payload.DoubleArray->elementType) {
//        	Array_set(result, i, functionTable[(int)targetType][FUNC_convert](element));
//            // result.payload.DoubleArray->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
//        } else {
//            Array_Set(result, i, element);
//        }
//    }

//    va_end(argp);
//    return result;
    return token;
}
/**/

