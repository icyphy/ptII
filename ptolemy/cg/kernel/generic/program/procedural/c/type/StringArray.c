/*** StringArray_add() ***/
// StringArray_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token* StringArray_add(Token* thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;

    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    size1 = thisToken->payload.StringArray->size;
    size2 = otherToken->payload.StringArray->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = $new(StringArray(resultSize, 0));

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
                StringArray_set(result, i, $add_String_String(StringArray_get(thisToken, 0),StringArray_get(otherToken, i)));
        } else if (size2 == 1) {
                StringArray_set(result, i, $add_String_String(StringArray_get(thisToken, i),StringArray_get(otherToken, 0)));
        } else {
                StringArray_set(result, i, $add_String_String(StringArray_get(thisToken, i),StringArray_get(otherToken, i)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** StringArray_clone() ***/
// StringArray_clone: Return a new array just like the
// specified array.
Token* StringArray_clone(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(StringArray(thisToken->payload.StringArray->size, 0));
    for (i = 0; i < thisToken->payload.StringArray->size; i++) {
        StringArray_set(result, i, $clone_String(StringArray_get(thisToken, i)));
    }
    return result;
}
/**/

/*** StringArray_convert() ***/
// StringArray_convert: Convert the first argument array
// into the type specified by the second argument.
// @param token The token to be converted.
// @param targetType The type to convert the elements of the given token to.
Token* StringArray_convert(Token* token, ...) {
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
//    result = StringArray_new(token->payload.StringArray->size, 0);
//
//    for (i = 0; i < token->payload.StringArray->size; i++) {
//        element = StringArray_get(token, i);
//        if (targetType != token->payload.StringArray->elementType) {
//
//                StringArray_set(result, i, functionTable[(int)targetType][FUNC_convert](element));
//            // result->payload.StringArray->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
//        } else {
//                StringArray_set(result, i, element);
//        }
//    }
//
//    va_end(argp);
//    return result;
        return token;
}
/**/

/*** StringArray_delete() ***/
// StringArray_delete: FIXME: What does this do?
Token* StringArray_delete(Token* token, ...) {
    //Token element;
    //int i;
    //char elementType;
    // Delete each elements.
    // for (i = 0; i < token->payload.StringArray->size; i++) {
    //     elementType = token->payload.StringArray->elementType;
    //     element = StringArray_get(token, i);
    //     functionTable[(int) elementType][FUNC_delete](element);
    // }
    free((string *) token->payload.StringArray->elements);
    free(token->payload.StringArray);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return NULL;
}
/**/

/*** StringArray_equals() ***/
#ifdef TYPE_StringArray
// StringArray_equals: Test an array for equality with a second array.
Token* StringArray_equals(Token* thisToken, ...) {
    int i;
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (thisToken->payload.StringArray->size != otherToken->payload.StringArray->size) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.StringArray->size; i++) {
            if (!$equals_String_String(StringArray_get(thisToken, i), StringArray_get(otherToken, i))) {
                    return $new(Boolean(false));
            }
    }

    va_end(argp);
    return $new(Boolean(true));
}
#endif
/**/

/*** StringArray_isCloseTo() ***/
#ifdef TYPE_StringArray
// StringArray_isCloseTo: Test an array to see whether it is close in value to another.
Token* StringArray_isCloseTo(Token* thisToken, ...) {
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);
    va_end(argp);
    return $StringArray_equals(thisToken, otherToken);
}
#endif
/**/

/*** StringArray_new() ***/
// StringArray_new: Create a new array with the specified elements.
//  The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token* .
// If the "given" argument is greater than 0, then the last
// argument is expected to be the type that of the elements.
Token* StringArray_new(int size, int given, ...) {
    va_list argp;
    int i;
    Token* result = malloc(sizeof(Token));
    result->type = TYPE_StringArray;
    result->payload.StringArray = (StringArrayToken) malloc(sizeof(struct stringarray));
    result->payload.StringArray->size = size;
    result->payload.StringArray->elementType = TYPE_String;
    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
        // Allocate an new array of Tokens.
        result->payload.StringArray->elements =
        (string *) calloc(size, sizeof(string));
        if (given > 0) {
            va_start(argp, given);
            for (i = 0; i < given; i++) {
                result->payload.StringArray->elements[i] = (string) va_arg(argp, string);
            }
            va_end(argp);
        }
    }
    return result;
}
/**/

/*** StringArray_one() ***/
// StringArray_one: Return an array like the specified
// array but with ones of the same type.
Token* StringArray_one(Token* thisToken, ...) {
    Token* result;
    Token element;
    int i;

    result = $new(StringArray(thisToken->payload.StringArray->size, 0));
    for (i = 0; i < thisToken->payload.StringArray->size; i++) {
        StringArray_set(result, i, $one_String());
    }
    return result;
}
/**/

/*** StringArray_print() ***/
// StringArray_print: Print the contents of an array to standard out.
Token* StringArray_print(Token* thisToken, ...) {
    int i;
    printf("{");
    for (i = 0; i < thisToken->payload.StringArray->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        printf("%s", StringArray_get(thisToken, i));
        // functionTable[(int)thisToken->payload.StringArray->elementType][FUNC_print](StringArray_get(thisToken, i));
    }
    printf("}");
}
/**/

/*** StringArray_repeat() ***/
Token* StringArray_repeat(int number, string value) {
        Token* result;
        result = $new(StringArray(number, 0));
        int i;

        for (i = 0; i < number; i++) {
                StringArray_set(result, i, value);
    }
    return result;
}
/**/

/*** StringArray_sum() ***/
// FIXME: WHAT DOES THIS FUNCTION DO?
string StringArray_sum(Token token) {
        string result;
        int i;

        if (token->payload.StringArray->size <= 0) {
                return $zero_String(token);
        } else {
                result = StringArray_get(token, 0);
        }

    for (i = 1; i < token->payload.StringArray->size; i++) {
            result = $add_String_String(result, StringArray_get(token, i));
    }
    return result;
}
/**/

/*** StringArray_toString() ***/
// StringArray_toString: Return a string token with a string representation
// of the specified array.
Token* StringArray_toString(Token* thisToken, ...) {
        return $new(String($toString_StringArray(thisToken)));
}
/**/

/*** StringArray_zero() ***/
// StringArray_zero: Return an array like the specified
// array but with zeros of the same type.
Token* StringArray_zero(Token* thisToken, ...) {
    Token* result;
    int i;

    result = $new(StringArray(thisToken->payload.StringArray->size, 0));
    for (i = 0; i < thisToken->payload.StringArray->size; i++) {
        StringArray_set(result, i, $zero_String());
    }
    return result;
}
/**/

/*** declareBlock() ***/
Token* StringArray_new(int size, int given, ...);
struct stringarray {
    int size;                                   // size of the array.
    string* elements;                            // array of Token elements.
    char elementType;                                 // type of the elements.
};
typedef struct stringarray* StringArrayToken;
/**/

/*** funcDeclareBlock() ***/
// StringArray_get: get an element of an array.
#define StringArray_length(array) ((array)->payload.StringArray->size)

string StringArray_get(Token* array, int i);
void StringArray_set(Token* array, int i, string element);
void StringArray_resize(Token* array, int size);
void StringArray_insert(Token* array, string token);

/**/

/*** funcImplementationBlock() ***/
string StringArray_get(Token* array, int i) {
        // Token* result;
        // result->type = array->payload.StringArray->elementType;
        // result->payload.String = ((string *) array->payload.StringArray->elements)[i];
        // return result;
        return ((string *) array->payload.StringArray->elements)[i];
}

// StringArray_set: set an element of an array.
void StringArray_set(Token* array, int i, string element) {
    ((string *) array->payload.StringArray->elements)[i] = element;
}

// StringArray_resize: Change the size of an array,
// preserving those elements that fit.
void StringArray_resize(Token* array, int size) {
    if (array->payload.StringArray->size == 0) {
        array->payload.StringArray->elements = (string *) malloc(size * sizeof(string));
    } else {
        array->payload.StringArray->elements = (string*) realloc(
                     array->payload.StringArray->elements, size * sizeof(string));
    }
    array->payload.StringArray->size = size;
}

// StringArray_insert: Append the specified element to the end of an array.
void StringArray_insert(Token* array, string token) {
    // FIXME: call this append(), not insert().
    int oldSize = array->payload.StringArray->size;
    StringArray_resize(array, oldSize + 1 );
    ((string *) array->payload.StringArray->elements)[oldSize] = token;
}
/**/
