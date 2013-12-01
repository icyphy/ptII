/*** BooleanArray_add() ***/
// BooleanArray_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* BooleanArray_add(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token *result;
    Token *otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.BooleanArray->size;
    size2 = otherToken->payload.BooleanArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(BooleanArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                BooleanArray_set(result, i, $add_Boolean_Boolean(BooleanArray_get(thisToken, 0),BooleanArray_get(otherToken, i)));
        } else if (size2 == 1) {
                BooleanArray_set(result, i, $add_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, 0)));
        } else {
                BooleanArray_set(result, i, $add_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** BooleanArray_clone() ***/
// BooleanArray_clone: Return a new array just like the
// specified array.
Token* BooleanArray_clone(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(BooleanArray(thisToken->payload.BooleanArray->size, 0));
    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
        BooleanArray_set(result, i, $clone_Boolean(BooleanArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** BooleanArray_convert() ***/
// BooleanArray_convert: Convert the first argument array
// into the type specified by the second argument.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token* BooleanArray_convert(Token* token, ...) {
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
//    result = BooleanArray_new(token->payload.BooleanArray->size, 0);
//
//    for (i = 0; i < token->payload.BooleanArray->size; i++) {
//        element = BooleanArray_get(token, i);
//        if (targetType != token->payload.BooleanArray->elementType) {
//
//                BooleanArray_set(result, i, functionTable[(int)targetType][FUNC_convert](element));
//            // result->payload.BooleanArray->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
//        } else {
//                BooleanArray_set(result, i, element);
//        }
//    }
//
//    va_end(argp);
//    return result;
        return token;
}
/**/

/*** BooleanArray_delete() ***/
// BooleanArray_delete: FIXME: What does this do?
Token* BooleanArray_delete(Token* token, ...) {
        //Revised
    //Token element;
    //int i;
    //char elementType;
    // Delete each elements.
    // for (i = 0; i < token->payload.BooleanArray->size; i++) {
    //     elementType = token->payload.BooleanArray->elementType;
    //     element = BooleanArray_get(token, i);
    //     functionTable[(int) elementType][FUNC_delete](element);
    // }
    free((boolean *) token->payload.BooleanArray->elements);
    free(token->payload.BooleanArray);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return NULL;
}
/**/

/*** BooleanArray_divide() ***/
// BooleanArray_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* BooleanArray_divide(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.BooleanArray->size;
    size2 = otherToken->payload.BooleanArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(BooleanArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                BooleanArray_set(result, i, $divide_Boolean_Boolean(BooleanArray_get(thisToken, 0),BooleanArray_get(otherToken, i)));
        } else if (size2 == 1) {
                BooleanArray_set(result, i, $divide_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, 0)));
        } else {
                BooleanArray_set(result, i, $divide_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/*** BooleanArray_equals() ***/
#ifdef TYPE_BooleanArray
// BooleanArray_equals: Test an array for equality with a second array.
Token* BooleanArray_equals(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (thisToken->payload.BooleanArray->size != otherToken->payload.BooleanArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
            if (!$equals_Boolean_Boolean(BooleanArray_get(thisToken, i), BooleanArray_get(otherToken, i))) {
                    return $new(Boolean(false));
            }
        // if (!functionTable[(int)BooleanArray_get(thisToken, i)->type][FUNC_equals]
        //                 (BooleanArray_get(thisToken, i), BooleanArray_get(otherToken, i))->payload.Boolean) {
        //     return $new(Boolean(false));
        // }
    }

    va_end(argp);
    return $new(Boolean(true));
}
#endif
/**/

/*** BooleanArray_isCloseTo() ***/
#ifdef TYPE_BooleanArray
// BooleanArray_isCloseTo: Test an array to see whether it is close in value to another.
Token* BooleanArray_isCloseTo(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    Token* tolerance;
    va_start(argp, thisToken);


    otherToken = va_arg(argp, Token*);
    otherToken = $BooleanArray_convert(otherToken);

    boolean value1, value2;
    tolerance = va_arg(argp, Token*);


    if (thisToken->payload.BooleanArray->size != otherToken->payload.BooleanArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
        value1 = BooleanArray_get(thisToken, i);
        value2 = BooleanArray_get(otherToken, i);

        if (fabs(value1 - value2) > tolerance->payload.Double) {
            return $new(Boolean(false));
        }
    }
    va_end(argp);
    return $new(Boolean(true));
}
#endif
/**/

/*** BooleanArray_multiply() ***/
// BooleanArray_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* BooleanArray_multiply(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.BooleanArray->size;
    size2 = otherToken->payload.BooleanArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(BooleanArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                BooleanArray_set(result, i, $multiply_Boolean_Boolean(BooleanArray_get(thisToken, 0),BooleanArray_get(otherToken, i)));
        } else if (size2 == 1) {
                BooleanArray_set(result, i, $multiply_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, 0)));
        } else {
                BooleanArray_set(result, i, $multiply_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** BooleanArray_negate() ***/
// BooleanArray_negate: Negate each element of an array.
// Return a new Array token.
Token* BooleanArray_negate(Token* thisToken, ...) {
    int i;
    Token* result;
    result = $new(BooleanArray(thisToken->payload.BooleanArray->size, 0));

    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
        BooleanArray_set(result, i, $negate_Boolean(BooleanArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** BooleanArray_new() ***/
// BooleanArray_new: Create a new array with the specified elements.
//  The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token* .
// If the "given" argument is greater than 0, then the last
// argument is expected to be the type that of the elements.
Token* BooleanArray_new(int size, int given, ...) {
    va_list argp;
    int i;
    Token* result = malloc(sizeof(Token));
    result->type = TYPE_BooleanArray;
    result->payload.BooleanArray = (BooleanArrayToken) malloc(sizeof(struct booleanarray));
    result->payload.BooleanArray->size = size;
    result->payload.BooleanArray->elementType = TYPE_Boolean;
    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
        // Allocate an new array of Tokens.
        result->payload.BooleanArray->elements =
        (boolean *) calloc(size, sizeof(boolean));
        if (given > 0) {
            va_start(argp, given);
            for (i = 0; i < given; i++) {

                    // va_arg requires the second argument to be not be unsigned char.
                    // so we'll promote it to int instead.
                result->payload.BooleanArray->elements[i] = (boolean) va_arg(argp, int);
            }
            va_end(argp);
        }
    }
    return result;
}
/**/

/*** BooleanArray_one() ***/
// BooleanArray_one: Return an array like the specified
// array but with ones of the same type.
Token* BooleanArray_one(Token* thisToken, ...) {
    Token* result;
    Token element;
    int i;

    result = $new(BooleanArray(thisToken->payload.BooleanArray->size, 0));
    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
        BooleanArray_set(result, i, $one_Boolean());
    }
    return result;
}
/**/

/*** BooleanArray_print() ***/
// BooleanArray_print: Print the contents of an array to standard out.
Token* BooleanArray_print(Token* thisToken, ...) {
    int i;
    printf("{");
    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        print_Boolean(BooleanArray_get(thisToken, i));
        // functionTable[(int)thisToken->payload.BooleanArray->elementType][FUNC_print](BooleanArray_get(thisToken, i));
    }
    printf("}");
}
/**/

/*** BooleanArray_repeat() ***/
Token* BooleanArray_repeat(int number, boolean value) {
        Token* result;
        result = $new(BooleanArray(number, 0));
        int i;

        for (i = 0; i < number; i++) {
                BooleanArray_set(result, i, value);
    }
    return result;
}
/**/

/*** BooleanArray_subtract() ***/
// BooleanArray_subtract: Subtract the second argument array
// from the first argument array.
// Assume the given otherToken is an array.
// FIXME: Arrays can have scalars subtracted!
// This will cause a nasty seg fault.
// Return a new Array token.
Token* BooleanArray_subtract(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.BooleanArray->size;
    size2 = otherToken->payload.BooleanArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(BooleanArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                BooleanArray_set(result, i, $subtract_Boolean_Boolean(BooleanArray_get(thisToken, 0),BooleanArray_get(otherToken, i)));
        } else if (size2 == 1) {
                BooleanArray_set(result, i, $subtract_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, 0)));
        } else {
                BooleanArray_set(result, i, $subtract_Boolean_Boolean(BooleanArray_get(thisToken, i),BooleanArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** BooleanArray_sum() ***/
// FIXME: WHAT DOES THIS FUNCTION DO?
boolean BooleanArray_sum(Token* token) {
        boolean result;
        int i;

        if (token->payload.BooleanArray->size <= 0) {
                return false;
        } else {
                result = BooleanArray_get(token, 0);
        }

    for (i = 1; i < token->payload.BooleanArray->size; i++) {
            result = $add_Boolean_Boolean(result, BooleanArray_get(token, i));
    }
    return result;
}
/**/

/*** BooleanArray_toString() ***/
// BooleanArray_toString: Return a string token with a string representation
// of the specified array.
Token* BooleanArray_toString(Token* thisToken, ...) {
        return $new(String($toString_BooleanArray(thisToken)));
}
/**/

/*** BooleanArray_zero() ***/
// BooleanArray_zero: Return an array like the specified
// array but with zeros of the same type.
Token* BooleanArray_zero(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(BooleanArray(thisToken->payload.BooleanArray->size, 0));
    for (i = 0; i < thisToken->payload.BooleanArray->size; i++) {
        BooleanArray_set(result, i, $zero_Boolean());
    }
    return result;
}
/**/

/*** declareBlock() ***/
Token* BooleanArray_new(int size, int given, ...);
struct booleanarray {
    int size;                                   // size of the array.
    boolean* elements;                            // array of Token elements.
    char elementType;                                 // type of the elements.
};
typedef struct booleanarray* BooleanArrayToken;
/**/

/*** funcDeclareBlock() ***/
// BooleanArray_get: get an element of an array.
#define BooleanArray_length(array) ((array)->payload.BooleanArray->size)

boolean BooleanArray_get(Token* array, int i);
void BooleanArray_set(Token* array, int i, boolean element);
void BooleanArray_resize(Token* array, int size);
void BooleanArray_insert(Token* array, boolean token);

/**/

/*** funcImplementationBlock() ***/
boolean BooleanArray_get(Token* array, int i) {
        // Token* result;
        // result->type = array->payload.BooleanArray->elementType;
        // result->payload.Boolean = ((boolean *) array->payload.BooleanArray->elements)[i];
        // return result;
        return ((boolean *) array->payload.BooleanArray->elements)[i];
}

// BooleanArray_set: set an element of an array.
void BooleanArray_set(Token* array, int i, boolean element) {
    ((boolean *) array->payload.BooleanArray->elements)[i] = element;
}

// BooleanArray_resize: Change the size of an array,
// preserving those elements that fit.
void BooleanArray_resize(Token* array, int size) {
    if (array->payload.BooleanArray->size == 0) {
        array->payload.BooleanArray->elements = (boolean *) malloc(size * sizeof(boolean));
    } else {
        array->payload.BooleanArray->elements = (boolean*) realloc(
                     array->payload.BooleanArray->elements, size * sizeof(boolean));
    }
}

// BooleanArray_insert: Append the specified element to the end of an array.
void BooleanArray_insert(Token* array, boolean token) {
    // FIXME: call this append(), not insert().
    int oldSize = array->payload.BooleanArray->size;
    BooleanArray_resize(array, oldSize + 1 );
    ((boolean *) array->payload.BooleanArray->elements)[oldSize] = token;

}
/**/
