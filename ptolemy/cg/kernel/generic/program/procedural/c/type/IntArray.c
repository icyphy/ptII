/*** IntArray_add() ***/
// IntArray_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* IntArray_add(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token *result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.IntArray->size;
    size2 = otherToken->payload.IntArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(IntArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                IntArray_set(result, i, $add_Int_Int(IntArray_get(thisToken, 0),IntArray_get(otherToken, i)));
        } else if (size2 == 1) {
                IntArray_set(result, i, $add_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, 0)));
        } else {
                IntArray_set(result, i, $add_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** IntArray_clone() ***/
// IntArray_clone: Return a new array just like the
// specified array.
Token* IntArray_clone(Token* thisToken, ...) {
    Token *result;
    int i;

    result = $new(IntArray(thisToken->payload.IntArray->size, 0));
    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
        IntArray_set(result, i, $clone_Int(IntArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** IntArray_convert() ***/
// IntArray_convert: Convert the first argument array
// into the type specified by the second argument.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token* IntArray_convert(Token* token, ...) {

        switch (token->type) {
#ifdef TYPE_IntArray
                case TYPE_IntArray:
                        break;
#endif
#ifdef TYPE_Int
            case TYPE_Int:
                token = $convert_Int_IntArray(token->payload.Int);
                break;
#endif

                // FIXME: not finished
            default:
                fprintf(stderr, "IntArray_convert(): Conversion from an unsupported type. (%d)\n", token->type);
                exit(-1);
                break;
        }
        return token;

//    int i;
//    Token *result;
//    Token element;
//    va_list argp;
//    char targetType;
//
//    va_start(argp, token);
//    targetType = va_arg(argp, int);
//
//    // FIXME: HOW DO WE KNOW WHICH TYPE WE'RE CONVERTING TO?
//    result = IntArray_new(token->payload.IntArray->size, 0);
//
//    for (i = 0; i < token->payload.IntArray->size; i++) {
//        element = IntArray_get(token, i);
//        if (targetType != token->payload.IntArray->elementType) {
//
//                IntArray_set(result, i, functionTable[(int)targetType][FUNC_convert](element));
//            // result->payload.IntArray->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
//        } else {
//                IntArray_set(result, i, element);
//        }
//    }
//
//    va_end(argp);
//    return result;
}
/**/

/*** IntArray_delete() ***/
// IntArray_delete: FIXME: What does this do?
Token* IntArray_delete(Token* token, ...) {
    //Token element;
    //int i;
    //char elementType;
    // Delete each elements.
    // for (i = 0; i < token->payload.IntArray->size; i++) {
    //     elementType = token->payload.IntArray->elementType;
    //     element = IntArray_get(token, i);
    //     functionTable[(int) elementType][FUNC_delete](element);
    // }
    free((int *) token->payload.IntArray->elements);
    free(token->payload.IntArray);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return NULL;
}
/**/

/*** IntArray_divide() ***/
// IntArray_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* IntArray_divide(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token *result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.IntArray->size;
    size2 = otherToken->payload.IntArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(IntArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                IntArray_set(result, i, $divide_Int_Int(IntArray_get(thisToken, 0),IntArray_get(otherToken, i)));
        } else if (size2 == 1) {
                IntArray_set(result, i, $divide_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, 0)));
        } else {
                IntArray_set(result, i, $divide_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** IntArray_equals() ***/
// IntArray_equals: Test an array for equality with a second array.
Token* IntArray_equals(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (thisToken->payload.IntArray->size != otherToken->payload.IntArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
            if (IntArray_get(thisToken, i) != IntArray_get(otherToken, i)) {
                    return $new(Boolean(false));
            }
    }

    va_end(argp);
    return $new(Boolean(true));
}
/**/

/*** IntArray_isCloseTo() ***/
// IntArray_isCloseTo: Test an array to see whether it is close in value to another.
Token* IntArray_isCloseTo(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    Token *tolerance;
    va_start(argp, thisToken);


    otherToken = va_arg(argp, Token*);
    otherToken = IntArray_convert(otherToken);

    int value1, value2;
    tolerance = va_arg(argp, Token*);


    if (thisToken->payload.IntArray->size != otherToken->payload.IntArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
        value1 = IntArray_get(thisToken, i);
        value2 = IntArray_get(otherToken, i);

        if (fabs(value1 - value2) > tolerance->payload.Double) {
            return $new(Boolean(false));
        }
    }
    va_end(argp);
    return $new(Boolean(true));
}
/**/

/*** IntArray_multiply() ***/
// IntArray_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* IntArray_multiply(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token *result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.IntArray->size;
    size2 = otherToken->payload.IntArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(IntArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                IntArray_set(result, i, $multiply_Int_Int(IntArray_get(thisToken, 0),IntArray_get(otherToken, i)));
        } else if (size2 == 1) {
                IntArray_set(result, i, $multiply_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, 0)));
        } else {
                IntArray_set(result, i, $multiply_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** IntArray_negate() ***/
// IntArray_negate: Negate each element of an array.
// Return a new Array token.
Token* IntArray_negate(Token* thisToken, ...) {
    int i;
    Token *result;
    result = $new(IntArray(thisToken->payload.IntArray->size, 0));

    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
        IntArray_set(result, i, $negate_Int(IntArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** IntArray_new() ***/
// IntArray_new: Create a new array with the specified elements.
//  The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token *.
// If the "given" argument is greater than 0, then the last
// argument is expected to be the type that of the elements.
Token* IntArray_new(int size, int given, ...) {
    va_list argp;
    int i;
    Token * result = malloc(sizeof(Token));
    result->type = TYPE_IntArray;
    result->payload.IntArray = (IntArrayToken) malloc(sizeof(struct intarray));
    result->payload.IntArray->size = size;
    result->payload.IntArray->elementType = TYPE_Int;
    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
        // Allocate an new array of Tokens.
        result->payload.IntArray->elements =
        (int *) calloc(size, sizeof(int));
        if (given > 0) {
            va_start(argp, given);
            for (i = 0; i < given; i++) {
                result->payload.IntArray->elements[i] = (int) va_arg(argp, int);
            }
            va_end(argp);
        }
    }
    return result;
}
/**/

/*** IntArray_one() ***/
// IntArray_one: Return an array like the specified
// array but with ones of the same type.
Token* IntArray_one(Token* thisToken, ...) {
    Token *result;
    Token element;
    int i;

    result = $new(IntArray(thisToken->payload.IntArray->size, 0));
    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
        IntArray_set(result, i, $one_Int());
    }
    return result;
}
/**/

/*** IntArray_print() ***/
// IntArray_print: Print the contents of an array to standard out.
Token* IntArray_print(Token* thisToken, ...) {
    int i;
    printf("{");
    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        printf("%d", IntArray_get(thisToken, i));
        // functionTable[(int)thisToken->payload.IntArray->elementType][FUNC_print](IntArray_get(thisToken, i));
    }
    printf("}");
    return emptyToken;
}
/**/

/*** IntArray_repeat() ***/
Token* IntArray_repeat(int number, int value) {
        Token *result;
        result = $new(IntArray(number, 0));
        int i;

        for (i = 0; i < number; i++) {
                IntArray_set(result, i, value);
    }
    return result;
}
/**/

/*** IntArray_subtract() ***/
// IntArray_subtract: Subtract the second argument array
// from the first argument array.
// Assume the given otherToken is an array.
// FIXME: Arrays can have scalars subtracted!
// This will cause a nasty seg fault.
// Return a new Array token.
Token* IntArray_subtract(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.IntArray->size;
    size2 = otherToken->payload.IntArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(IntArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                IntArray_set(result, i, $subtract_Int_Int(IntArray_get(thisToken, 0),IntArray_get(otherToken, i)));
        } else if (size2 == 1) {
                IntArray_set(result, i, $subtract_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, 0)));
        } else {
                IntArray_set(result, i, $subtract_Int_Int(IntArray_get(thisToken, i),IntArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** IntArray_sum() ***/
// FIXME: WHAT DOES THIS FUNCTION DO?
int IntArray_sum(Token* token) {
        int result;
        int i;

        if (token->payload.IntArray->size <= 0) {
                return 0;
        } else {
                result = IntArray_get(token, 0);
        }

    for (i = 1; i < token->payload.IntArray->size; i++) {
            result = $add_Int_Int(result, IntArray_get(token, i));
    }
    return result;
}
/**/

/*** IntArray_toString() ***/
// IntArray_toString: Return a string token with a string representation
// of the specified array.
Token* IntArray_toString(Token* thisToken, ...) {
        return $new(String($toString_IntArray(thisToken)));
}
/**/

/*** IntArray_zero() ***/
// IntArray_zero: Return an array like the specified
// array but with zeros of the same type.
Token* IntArray_zero(Token* thisToken, ...) {
    Token *result;
    int i;

    result = $new(IntArray(thisToken->payload.IntArray->size, 0));
    for (i = 0; i < thisToken->payload.IntArray->size; i++) {
        IntArray_set(result, i, 0);
    }
    return result;
}
/**/

/*** declareBlock() ***/
Token* IntArray_new(int size, int given, ...);
struct intarray {
    int size;                                   // size of the array.
    int* elements;                            // array of Token elements.
    char elementType;                                 // type of the elements.
};
typedef struct intarray* IntArrayToken;
/**/

/*** funcDeclareBlock() ***/
// IntArray_get: get an element of an array.
#define IntArray_length(array) ((array)->payload.IntArray->size)

int IntArray_get(Token* array, int i);
void IntArray_set(Token* array, int i, int element);
void IntArray_resize(Token* array, int size);
void IntArray_insert(Token* array, int token);

/**/

/*** funcImplementationBlock() ***/
int IntArray_get(Token* array, int i) {
        // Token *result;
        // result->type = array->payload.IntArray->elementType;
        // result->payload.Int = ((int *) array->payload.IntArray->elements)[i];
        // return result;
        return ((int *) array->payload.IntArray->elements)[i];
}

// IntArray_set: set an element of an array.
void IntArray_set(Token* array, int i, int element) {
    ((int *) array->payload.IntArray->elements)[i] = element;
}

// IntArray_resize: Change the size of an array,
// preserving those elements that fit.
void IntArray_resize(Token* array, int size) {
    if (array->payload.IntArray->size == 0) {
        array->payload.IntArray->elements = (int *) malloc(size * sizeof(int));
    } else {
        array->payload.IntArray->elements = (int*) realloc(
                     array->payload.IntArray->elements, size * sizeof(int));
    }
    array->payload.IntArray->size = size;
}

// IntArray_insert: Append the specified element to the end of an array.
void IntArray_insert(Token* array, int token) {
     // FIXME: call this append(), not insert().
    int oldSize = array->payload.IntArray->size;
    IntArray_resize(array, oldSize + 1 );
    ((int *) array->payload.IntArray->elements)[oldSize] = token;
}
/**/
