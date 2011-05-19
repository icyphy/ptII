#include "$TypeArray.h"

/** NOTE: META SUBSTITUTION SYMBOLS
 * $Type: Int, Char, Array, etc.
 * $type_q: %d, %s, etc.
 * $type: int, char, etc.
 * $print_size: 12(int), 22(long), 22(double), 6(boolean)
 */

/***declareBlock***/
Token $TypeArray_new(int size, int given, ...);

struct $typearray {
    int size;                                   // size of the array.
    $type* elements;                            // array of Token elements.
    char elementType;                                 // type of the elements.
};
typedef struct $typearray* $TypeArrayToken;
/**/

/***funcDeclareBlock***/
// $TypeArray_get: get an element of an array.
#define $TypeArray_length(array) ((array).payload.$TypeArray->size)

$type $TypeArray_get(Token array, int i) {
        // Token result;
        // result.type = array.payload.$TypeArray->elementType;
        // result.payload.$Type = (array.payload.$TypeArray->elements)[i];
        // return result;
        return (array.payload.$TypeArray->elements)[i];
}

// $TypeArray_set: set an element of an array.
void $TypeArray_set(Token array, int i, $type element) {
    (array.payload.$TypeArray->elements)[i] = element;
}

// $TypeArray_resize: Change the size of an array,
// preserving those elements that fit.
void $TypeArray_resize(Token array, int size) {
    array.payload.$TypeArray->size = size;
    array.payload.$TypeArray->elements = ($type*) realloc(
                    array.payload.$TypeArray->elements, size * sizeof($type));
}

// $TypeArray_insert: Append the specified element to the end of an array.
void $TypeArray_insert(Token array, $type token) {
    int oldSize = array.payload.$TypeArray->size++;
    $TypeArray_resize(array, array.payload.$TypeArray->size);
    (array.payload.$TypeArray->elements)[oldSize] = token;
}
/**/


/***$TypeArray_new***/
// $TypeArray_new: Create a new array with the specified elements.
//  The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token *.
// If the "given" argument is greater than 0, then the last
// argument is expected to be the type that of the elements.
Token $TypeArray_new(int size, int given, ...) {
        va_list argp;
    int i;
    Token result;
    result.type = TYPE_$TypeArray;
    result.payload.$TypeArray = ($TypeArrayToken) malloc(sizeof(struct $typearray));
    result.payload.$TypeArray->size = size;
    result.payload.$TypeArray->elementType = TYPE_$Type;
    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
        // Allocate an new array of Tokens.
        result.payload.$TypeArray->elements =
        ($type *) calloc(size, sizeof($type));
        if (given > 0) {
            va_start(argp, given);
            for (i = 0; i < given; i++) {
                result.payload.$TypeArray->elements[i] = ($type) va_arg(argp, $type);
            }
            va_end(argp);
        }
    }
    return result;
}
/**/


/***$TypeArray_delete***/
// $TypeArray_delete: FIXME: What does this do?
Token $TypeArray_delete(Token token, ...) {
    Token emptyToken;
    //Token element;
    //int i;
    //char elementType;
    // Delete each elements.
    // for (i = 0; i < token.payload.$TypeArray->size; i++) {
    //     elementType = token.payload.$TypeArray->elementType;
    //     element = $TypeArray_get(token, i);
    //     functionTable[(int) elementType][FUNC_delete](element);
    // }
    free(token.payload.$TypeArray->elements);
    free(token.payload.$TypeArray);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken;
}
/**/


/***$TypeArray_equals***/
// $TypeArray_equals: Test an array for equality with a second array.
Token $TypeArray_equals(Token thisToken, ...) {
    int i;
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    if (thisToken.payload.$TypeArray->size != otherToken.payload.$TypeArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
            if (!$equals_$Type_$Type($TypeArray_get(thisToken, i), $TypeArray_get(otherToken, i))) {
                    return $new(Boolean(false));
            }
    }

    va_end(argp);
    return $new(Boolean(true));
}
/**/


/***$TypeArray_isCloseTo***/
// $TypeArray_isCloseTo: Test an array to see whether it is close in value to another.
Token $TypeArray_isCloseTo(Token thisToken, ...) {
    int i;
    va_list argp;
    Token otherToken;
    Token tolerance;
    va_start(argp, thisToken);


    otherToken = va_arg(argp, Token);
    otherToken = $TypeArray_convert(otherToken);

    $type value1, value2;
    tolerance = va_arg(argp, Token);


    if (thisToken.payload.$TypeArray->size != otherToken.payload.$TypeArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
        value1 = $TypeArray_get(thisToken, i);
        value2 = $TypeArray_get(otherToken, i);

        if (fabs(value1 - value2) > tolerance.payload.Double) {
            return $new(Boolean(false));
        }
    }
    va_end(argp);
    return $new(Boolean(true));
}
/**/


/***$TypeArray_print***/
// $TypeArray_print: Print the contents of an array to standard out.
Token $TypeArray_print(Token thisToken, ...) {
    int i;
    printf("{");
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        $print_$Type($TypeArray_get(thisToken, i));
        // functionTable[(int)thisToken.payload.$TypeArray->elementType][FUNC_print]($TypeArray_get(thisToken, i));
    }
    printf("}");
}
/**/


/***$TypeArray_toString***/
// $TypeArray_toString: Return a string token with a string representation
// of the specified array.
Token $TypeArray_toString(Token thisToken, ...) {
        int i;
    int currentSize, allocatedSize;
    char* string;
    char* elementString;
    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    string[1] = '\0';

    // Space for '{', '}', and '\0' characters.
    currentSize = 3;

    //printf("%d\n", thisToken.payload.$TypeArray->size);
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
                // Calculate the require storage size.

            // $type temp = $TypeArray_get(thisToken, i);
        elementString = $toString_$Type($TypeArray_get(thisToken, i));
        currentSize += strlen(elementString);
                if (i != 0) {
                        currentSize += 2;
                }

                // Re-allocate storage.
                if (currentSize > allocatedSize) {
            allocatedSize *= 2;
            string = (char*) realloc(string, allocatedSize);
        }

                // Concat the element strings and separators.
                if (i != 0) {
            strcat(string, ", ");
        }
        strcat(string, elementString);
    }

    strcat(string, "}");
    return $new(String(string));
}
/**/


/***$TypeArray_add***/
// $TypeArray_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token $TypeArray_add(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.$TypeArray->size;
    size2 = otherToken.payload.$TypeArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new($TypeArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                $TypeArray_set(result, i, $add_$Type_$Type($TypeArray_get(thisToken, 0),$TypeArray_get(otherToken, i)));
        } else if (size2 == 1) {
                $TypeArray_set(result, i, $add_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, 0)));
        } else {
                $TypeArray_set(result, i, $add_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***$TypeArray_subtract***/
// $TypeArray_subtract: Subtract the second argument array
// from the first argument array.
// Assume the given otherToken is an array.
// FIXME: Arrays can have scalars subtracted!
// This will cause a nasty seg fault.
// Return a new Array token.
Token $TypeArray_subtract(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.$TypeArray->size;
    size2 = otherToken.payload.$TypeArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new($TypeArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                $TypeArray_set(result, i, $subtract_$Type_$Type($TypeArray_get(thisToken, 0),$TypeArray_get(otherToken, i)));
        } else if (size2 == 1) {
                $TypeArray_set(result, i, $subtract_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, 0)));
        } else {
                $TypeArray_set(result, i, $subtract_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***$TypeArray_multiply***/
// $TypeArray_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
Token $TypeArray_multiply(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.$TypeArray->size;
    size2 = otherToken.payload.$TypeArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new($TypeArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                $TypeArray_set(result, i, $multiply_$Type_$Type($TypeArray_get(thisToken, 0),$TypeArray_get(otherToken, i)));
        } else if (size2 == 1) {
                $TypeArray_set(result, i, $multiply_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, 0)));
        } else {
                $TypeArray_set(result, i, $multiply_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***$TypeArray_divide***/
// $TypeArray_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token $TypeArray_divide(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.$TypeArray->size;
    size2 = otherToken.payload.$TypeArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new($TypeArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                $TypeArray_set(result, i, $divide_$Type_$Type($TypeArray_get(thisToken, 0),$TypeArray_get(otherToken, i)));
        } else if (size2 == 1) {
                $TypeArray_set(result, i, $divide_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, 0)));
        } else {
                $TypeArray_set(result, i, $divide_$Type_$Type($TypeArray_get(thisToken, i),$TypeArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***$TypeArray_negate***/
// $TypeArray_negate: Negate each element of an array.
// Return a new Array token.
Token $TypeArray_negate(Token thisToken, ...) {
    int i;
    Token result;
    result = $new($TypeArray(thisToken.payload.$TypeArray->size, 0));

    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
        $TypeArray_set(result, i, $negate_$Type($TypeArray_get(thisToken, i)));
    }
    return result;
}
/**/


/***$TypeArray_zero***/
// $TypeArray_zero: Return an array like the specified
// array but with zeros of the same type.
Token $TypeArray_zero(Token thisToken, ...) {
    Token result;
    int i;

    result = $new($TypeArray(thisToken.payload.$TypeArray->size, 0));
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
        $TypeArray_set(result, i, $zero_$Type());
    }
    return result;
}
/**/


/***$TypeArray_one***/
// $TypeArray_one: Return an array like the specified
// array but with ones of the same type.
Token $TypeArray_one(Token thisToken, ...) {
    Token result;
    int i;

    result = $new($TypeArray(thisToken.payload.$TypeArray->size, 0));
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
        $TypeArray_set(result, i, $one_$Type());
    }
    return result;
}
/**/


/***$TypeArray_clone***/
// $TypeArray_clone: Return a new array just like the
// specified array.
Token $TypeArray_clone(Token thisToken, ...) {
    Token result;
    int i;

    result = $new($TypeArray(thisToken.payload.$TypeArray->size, 0));
    for (i = 0; i < thisToken.payload.$TypeArray->size; i++) {
        $TypeArray_set(result, i, $clone_$Type($TypeArray_get(thisToken, i)));
    }
    return result;
}
/**/


/***$TypeArray_sum***/
// FIXME: WHAT DOES THIS FUNCTION DO?
$type $TypeArray_sum(Token token) {
        $type result;
        int i;

        if (token.payload.$TypeArray->size <= 0) {
                return $zero_$Type();
        } else {
                result = $TypeArray_get(token, 0);
        }

    for (i = 1; i < token.payload.$TypeArray->size; i++) {
            result = $add_$Type_$Type(result, $TypeArray_get(token, i));
    }
    return result;
}
/**/

/***$TypeArray_repeat***/
Token $TypeArray_repeat(int number, $type value) {
        Token result;
        result = $new($TypeArray(number, 0));
        int i;

        for (i = 0; i < number; i++) {
                $TypeArray_set(result, i, value);
    }
    return result;
}
/**/


/***$TypeArray_convert***/
// $TypeArray_convert: Convert the first argument array
// into the type specified by the second argument.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token $TypeArray_convert(Token thisToken, ...) {
//    int i;
//    Token result;
//    Token element;
//    va_list argp;
//    char targetType;
//
//    va_start(argp, token);
//    targetType = va_arg(argp, int);
//
//    // FIXME: HOW DO WE KNOW WHICH TYPE WE'RE CONVERTING TO?
//    result = $TypeArray_new(token.payload.$TypeArray->size, 0);
//
//    for (i = 0; i < token.payload.$TypeArray->size; i++) {
//        element = $TypeArray_get(token, i);
//        if (targetType != token.payload.$TypeArray->elementType) {
//
//                $TypeArray_set(result, i, functionTable[(int)targetType][FUNC_convert](element));
//            // result.payload.$TypeArray->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
//        } else {
//                $TypeArray_set(result, i, element);
//        }
//    }
//
//    va_end(argp);
//    return result;
        return token;
}
/**/

