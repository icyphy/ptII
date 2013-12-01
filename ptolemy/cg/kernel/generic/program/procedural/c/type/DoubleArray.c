/*** DoubleArray_add() ***/
// DoubleArray_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* DoubleArray_add(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.DoubleArray->size;
    size2 = otherToken->payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(DoubleArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                DoubleArray_set(result, i, $add_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
                DoubleArray_set(result, i, $add_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
                DoubleArray_set(result, i, $add_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** DoubleArray_clone() ***/
// DoubleArray_clone: Return a new array just like the
// specified array.
Token* DoubleArray_clone(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(DoubleArray(thisToken->payload.DoubleArray->size, 0));
    for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
        DoubleArray_set(result, i, $clone_Double(DoubleArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** DoubleArray_convert() ***/
// DoubleArray_convert: Convert the first argument array
// into the type specified by the second argument.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token* DoubleArray_convert(Token* token, ...) {
        switch (token->type) {
                case TYPE_DoubleArray:
                        break;
#ifdef TYPE_Int
                case TYPE_Int:
                        token = $convert_Double_DoubleArray(InttoDouble(token->payload.Int));
                        break;
#endif
#ifdef TYPE_Double
                case TYPE_Double:
                        token = $convert_Double_DoubleArray(token->payload.Double);
                        break;
#endif
#ifdef TYPE_IntArray
                case TYPE_IntArray:
                        token = $convert_IntArray_DoubleArray(token);
                        break;
#endif

                        // FIXME: not finished
                default:
                        fprintf(stderr, "DoubleArray_convert(): Conversion from an unsupported type. (%d)\n", token->type);
                        exit(-1);
                        break;
        }
        return token;
//    int i;
//    Token* result;
//    Token element;
//    va_list argp;
//    char targetType;
//
//    va_start(argp, token);
//    targetType = va_arg(argp, int);
//
//    // FIXME: HOW DO WE KNOW WHICH TYPE WE'RE CONVERTING TO?
//    result = $new(DoubleArray(token->payload.DoubleArray->size, 0));
//
//    for (i = 0; i < token->payload.DoubleArray->size; i++) {
//        element = DoubleArray_get(token, i);
//        if (targetType != token->payload.DoubleArray->elementType) {
//
//                DoubleArray_set(result, i, functionTable[(int)targetType][FUNC_convert](element));
//            // result->payload.DoubleArray->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
//        } else {
//                DoubleArray_set(result, i, element);
//        }
//    }
//
//    va_end(argp);
//    return result;
        return token;
}
/**/

/*** DoubleArray_delete() ***/
// DoubleArray_delete: FIXME: What does this do?
Token* DoubleArray_delete(Token* token, ...) {
    //Token element;
    //int i;
    //char elementType;
    // Delete each elements.
    // for (i = 0; i < token->payload.DoubleArray->size; i++) {
    //     elementType = token->payload.DoubleArray->elementType;
    //     element = DoubleArray_get(token, i);
    //     functionTable[(int) elementType][FUNC_delete](element);
    // }
    free((double *) token->payload.DoubleArray->elements);
    free(token->payload.DoubleArray);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return NULL;
}
/**/

/*** DoubleArray_divide() ***/
// DoubleArray_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* DoubleArray_divide(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.DoubleArray->size;
    size2 = otherToken->payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(DoubleArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                DoubleArray_set(result, i, $divide_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
                DoubleArray_set(result, i, $divide_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
                DoubleArray_set(result, i, $divide_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** DoubleArray_equals() ***/
#ifdef TYPE_DoubleArray
// DoubleArray_equals: Test an array for equality with a second array.
Token* DoubleArray_equals(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    switch (otherToken->type) {
    case TYPE_DoubleArray:
        if (thisToken->payload.DoubleArray->size != otherToken->payload.DoubleArray->size) {
            return $new(Boolean(false));
        }
        for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
            if (!$equals_Double_Double(DoubleArray_get(thisToken, i), DoubleArray_get(otherToken, i))) {
                return $new(Boolean(false));
            }
        }
        break;
#ifdef TYPE_Array
    case TYPE_Array:
        if (thisToken->payload.DoubleArray->size != otherToken->payload.Array->size) {
            return $new(Boolean(false));
        }
        for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
            if (!$equals_Double_Double(DoubleArray_get(thisToken, i), Array_get(otherToken, i)->payload.Double)) {
                return $new(Boolean(false));
            }
        }
        break;
    }
#endif
    va_end(argp);
    return $new(Boolean(true));
}
#endif
/**/

/*** DoubleArray_isCloseTo() ***/
#ifdef TYPE_DoubleArray
// DoubleArray_isCloseTo: Test an array to see whether it is close in value to another.
Token* DoubleArray_isCloseTo(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    Token* tolerance;
    va_start(argp, thisToken);


    otherToken = va_arg(argp, Token*);
    otherToken = DoubleArray_convert(otherToken);

    double value1, value2;
    tolerance = va_arg(argp, Token*);


    if (thisToken->payload.DoubleArray->size != otherToken->payload.DoubleArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
        value1 = DoubleArray_get(thisToken, i);
        value2 = DoubleArray_get(otherToken, i);

        if (fabs(value1 - value2) > tolerance->payload.Double) {
            return $new(Boolean(false));
        }
    }
    va_end(argp);
    return $new(Boolean(true));
}
#endif
/**/

/*** DoubleArray_multiply() ***/
// DoubleArray_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* DoubleArray_multiply(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.DoubleArray->size;
    size2 = otherToken->payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(DoubleArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                DoubleArray_set(result, i, $multiply_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
                DoubleArray_set(result, i, $multiply_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
                DoubleArray_set(result, i, $multiply_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** DoubleArray_negate() ***/
// DoubleArray_negate: Negate each element of an array.
// Return a new Array token.
Token* DoubleArray_negate(Token* thisToken, ...) {
    int i;
    Token* result;
    result = $new(DoubleArray(thisToken->payload.DoubleArray->size, 0));

    for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
        DoubleArray_set(result, i, $negate_Double(DoubleArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** DoubleArray_new() ***/
// DoubleArray_new: Create a new array with the specified elements.
//  The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token* .
// If the "given" argument is greater than 0, then the last
// argument is expected to be the type that of the elements.
Token* DoubleArray_new(int size, int given, ...) {
    va_list argp;
    int i;
    Token* result = malloc(sizeof(Token));
    result->type = TYPE_DoubleArray;
    result->payload.DoubleArray = (DoubleArrayToken) malloc(sizeof(struct doublearray));
    result->payload.DoubleArray->size = size;
    result->payload.DoubleArray->elementType = TYPE_Double;
    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
        // Allocate an new array of Tokens.
        result->payload.DoubleArray->elements =
        (double *) calloc(size, sizeof(double));
        if (given > 0) {
            va_start(argp, given);
            for (i = 0; i < given; i++) {
                result->payload.DoubleArray->elements[i] = (double) va_arg(argp, double);
            }
            va_end(argp);
        }
    }
    return result;
}
/**/

/*** DoubleArray_one() ***/
// DoubleArray_one: Return an array like the specified
// array but with ones of the same type.
Token* DoubleArray_one(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(DoubleArray(thisToken->payload.DoubleArray->size, 0));
    for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
        DoubleArray_set(result, i, 1.0);
    }
    return result;
}
/**/

/*** DoubleArray_print() ***/
// DoubleArray_print: Print the contents of an array to standard out.
Token* DoubleArray_print(Token* thisToken, ...) {
    int i;
    printf("{");
    for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        printf("%g", DoubleArray_get(thisToken, i));
        // functionTable[(int)thisToken->payload.DoubleArray->elementType][FUNC_print](DoubleArray_get(thisToken, i));
    }
    printf("}");
}
/**/

/*** DoubleArray_repeat() ***/
Token* DoubleArray_repeat(int number, double value) {
        Token* result;
        result = $new(DoubleArray(number, 0));
        int i;

        for (i = 0; i < number; i++) {
                DoubleArray_set(result, i, value);
    }
    return result;
}
/**/

/*** DoubleArray_subtract() ***/
// DoubleArray_subtract: Subtract the second argument array
// from the first argument array.
// Assume the given otherToken is an array.
// FIXME: Arrays can have scalars subtracted!
// This will cause a nasty seg fault.
// Return a new Array token.
Token* DoubleArray_subtract(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.DoubleArray->size;
    size2 = otherToken->payload.DoubleArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(DoubleArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                DoubleArray_set(result, i, $subtract_Double_Double(DoubleArray_get(thisToken, 0),DoubleArray_get(otherToken, i)));
        } else if (size2 == 1) {
                DoubleArray_set(result, i, $subtract_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, 0)));
        } else {
                DoubleArray_set(result, i, $subtract_Double_Double(DoubleArray_get(thisToken, i),DoubleArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** DoubleArray_sum() ***/
double DoubleArray_sum(Token* token) {
        double result;
        int i;

        if (token->payload.DoubleArray->size <= 0) {
                return 0.0;
        } else {
                result = DoubleArray_get(token, 0);
        }

    for (i = 1; i < token->payload.DoubleArray->size; i++) {
            result = $add_Double_Double(result, DoubleArray_get(token, i));
    }
    return result;
}
/**/

/*** DoubleArray_toString() ***/
// DoubleArray_toString: Return a string token with a string representation
// of the specified array.
Token* DoubleArray_toString(Token* thisToken, ...) {
        return $new(String($toString_DoubleArray(thisToken)));
}
/**/

/*** DoubleArray_zero() ***/
// DoubleArray_zero: Return an array like the specified
// array but with zeros of the same type.
Token* DoubleArray_zero(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(DoubleArray(thisToken->payload.DoubleArray->size, 0));
    for (i = 0; i < thisToken->payload.DoubleArray->size; i++) {
        DoubleArray_set(result, i, $zero_Double());
    }
    return result;
}
/**/

/*** declareBlock() ***/
Token* DoubleArray_new(int size, int given, ...);
struct doublearray {
    int size;                                   // size of the array.
    double* elements;                            // array of Token elements.
    char elementType;                                 // type of the elements.
};
typedef struct doublearray* DoubleArrayToken;
/**/

/*** funcDeclareBlock() ***/
// DoubleArray_get: get an element of an array.
#define DoubleArray_length(array) ((array)->payload.DoubleArray->size)

double DoubleArray_get(Token* array, int i);
void DoubleArray_set(Token* array, int i, double element);
void DoubleArray_resize(Token* array, int size);
void DoubleArray_insert(Token* array, double token);

/**/

/*** funcImplementationBlock() ***/
double DoubleArray_get(Token* array, int i) {
        // Token* result;
        // result->type = array->payload.DoubleArray->elementType;
        // result->payload.Double = ((double *) array->payload.DoubleArray->elements)[i];
        // return result;
        return ((double *) array->payload.DoubleArray->elements)[i];
}

// DoubleArray_set: set an element of an array.
void DoubleArray_set(Token* array, int i, double element) {
    ((double *) array->payload.DoubleArray->elements)[i] = element;
}

// DoubleArray_resize: Change the size of an array,
// preserving those elements that fit.
void DoubleArray_resize(Token* array, int size) {
    if (array->payload.DoubleArray->size == 0) {
        array->payload.DoubleArray->elements = (double *) malloc(size * sizeof(double));
    } else {
        array->payload.DoubleArray->elements = (double*) realloc(
                     array->payload.DoubleArray->elements, size * sizeof(double));
    }
    array->payload.DoubleArray->size = size;
}

// DoubleArray_insert: Append the specified element to the end of an array.
void DoubleArray_insert(Token* array, double token) {
    // FIXME: call this append(), not insert().
    int oldSize = array->payload.DoubleArray->size;
    DoubleArray_resize(array, oldSize + 1 );
    ((double *) array->payload.DoubleArray->elements)[oldSize] = token;
}
/**/
