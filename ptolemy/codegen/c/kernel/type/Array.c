/***declareBlock***/

// Definition of the array struct.
struct array {
    int size;                                   // size of the array.
    Token* elements;                            // array of Token elements.
    //char elementType;                          // type of the elements.
};
typedef struct array* ArrayToken;
/**/

/***funcDeclareBlock***/

Token Array_new(int size, int given, ...); 

// Array_get: get an element of an array.  
Token Array_get(Token array, int i) {   
    return array.payload.Array->elements[i];
}

// Array_set: set an element of an array.  
void Array_set(Token array, int i, Token element) {
        array.payload.Array->elements[i] = element;
}

// Array_resize: Change the size of an array,
// preserving those elements that fit.  
void Array_resize(Token array, int size) {
        array.payload.Array->size = size;
        // FIXME: Does realloc() initialize memory? If not, then we need to do that.
        array.payload.Array->elements = (Token*) realloc(array.payload.Array->elements, size * sizeof(Token));
}

// Array_insert: Append the specified element to the end of an array.
void Array_insert(Token array, Token token) {
    int oldSize = array.payload.Array->size++;
    Array_resize(array, array.payload.Array->size);
    array.payload.Array->elements[oldSize] = token;
}

/**/

/***Array_new***/

// Array_new: Create a new array with the specified elements.
// The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token *.
Token Array_new(int size, int given, ...) {   
    va_list argp; 
    int i;
    Token result;
    char elementType;
    
    result.type = TYPE_Array;
    result.payload.Array = (ArrayToken) malloc(sizeof(struct array));
    result.payload.Array->size = size;

    // Only call calloc if size > 0.  Otherwise Electric Fence reports
    // an error.
    if (size > 0) {
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

            if (elementType >= 0) {
                // convert the elements if needed.
                for (i = 0; i < given; i++) {
                    if (Array_get(result, i).type != elementType) {
                        Array_set(result, i, functionTable[(int)elementType][FUNC_convert](Array_get(result, i)));
                    }
                }    
            }

            va_end(argp);
        }
    }
    return result;
}
/**/


/***Array_delete***/

// Array_delete: FIXME: What does this do?
Token Array_delete(Token token, ...) { 
    int i;  
    Token element, emptyToken;
    
    // Delete each elements.
    for (i = 0; i < token.payload.Array->size; i++) {
            element = Array_get(token, i);
        functionTable[(int)element.type][FUNC_delete](element);
    }
    free(token.payload.Array->elements);
    free(token.payload.Array);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken; 
}
/**/


/***Array_equals***/

// Array_equals: Test an array for equality with a second array.
Token Array_equals(Token thisToken, ...) {
    int i;
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    if (thisToken.payload.Array->size != otherToken.payload.Array->size) {
        return Boolean_new(false);
    }
    for (i = 0; i < thisToken.payload.Array->size; i++) {
        if (!functionTable[(int)Array_get(thisToken, i).type][FUNC_equals]
                        (Array_get(thisToken, i), Array_get(otherToken, i)).payload.Boolean) {
            return Boolean_new(false);
        }
    }

    va_end(argp);
    return Boolean_new(true);
}
/**/

/***Array_isCloseTo***/

// Array_isCloseTo: Test an array to see whether it is close in value to another.
Token Array_isCloseTo(Token thisToken, ...) {
    int i;
    va_list argp; 
    Token otherToken; 
    Token tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    tolerance = va_arg(argp, Token);

    if (thisToken.payload.Array->size != otherToken.payload.Array->size) {
        return Boolean_new(false);
    }
    for (i = 0; i < thisToken.payload.Array->size; i++) {
        if (!functionTable[(int)Array_get(thisToken, i).type][FUNC_isCloseTo](Array_get(thisToken, i), Array_get(otherToken, i), tolerance).payload.Boolean) {
            return Boolean_new(false);
        }
    }

    va_end(argp);
    return Boolean_new(true);
}
/**/

/***Array_print***/

// Array_print: Print the contents of an array to standard out.
Token Array_print(Token thisToken, ...) {
    // Token string = Array_toString(thisToken);
    // printf(string.payload.String);
    // free(string.payload.String);
        
    int i;
    printf("{");
    for (i = 0; i < thisToken.payload.Array->size; i++) {
        if (i != 0) {
            printf(", ");
        }
        functionTable[(int)thisToken.payload.Array->elements[i].type][FUNC_print](thisToken.payload.Array->elements[i]);
    }
    printf("}");
}
/**/

/***Array_toString***/

// Array_toString: Return a string token with a string representation
// of the specified array.
Token Array_toString(Token thisToken, ...) {
    int i;
    int currentSize, allocatedSize;
    char* string;
    Token elementString;

    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    string[1] = '\0';
    currentSize = 2;

    for (i = 0; i < thisToken.payload.Array->size; i++) {
        if (i != 0) {
            strcat(string, ", ");
        }
        elementString = functionTable[(int)thisToken.payload.Array->elements[i].type][FUNC_toString](thisToken.payload.Array->elements[i]);
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

/***Array_add***/

// Array_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_add(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;
    
    va_list argp; 
    Token result; 
    Token otherToken;
        
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.Array->size;
    size2 = otherToken.payload.Array->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = Array_new(resultSize, 0);
        
    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, 0)::add(Array_get(otherToken, i))));
        } else if (size2 == 1) {
            //result.payload.Array->elements[i] = functionTable[(int)Array_get(otherToken, 0).type][FUNC_add](Array_get(thisToken, i), Array_get(otherToken, 0));        
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::add(Array_get(otherToken, 0))));
        } else {
            //result.payload.Array->elements[i] = functionTable[(int)Array_get(thisToken, i).type][FUNC_add](Array_get(thisToken, i), Array_get(otherToken, i));
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::add(Array_get(otherToken, i))));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***Array_subtract***/

// Array_subtract: Subtract the second argument array
// from the first argument array.
// Assume the given otherToken is an array.
// FIXME: Arrays can have scalars subtracted!
// This will cause a nasty seg fault.
// Return a new Array token.
Token Array_subtract(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;
    
    va_list argp; 
    Token result; 
    Token otherToken;
    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.Array->size;
    size2 = otherToken.payload.Array->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = Array_new(resultSize, 0);
    
    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, 0)::subtract(Array_get(otherToken, i))));
        } else if (size2 == 1) {
            //result.payload.Array->elements[i] = functionTable[(int)Array_get(otherToken, 0).type][FUNC_add](Array_get(thisToken, i), Array_get(otherToken, 0));        
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::subtract(Array_get(otherToken, 0))));
        } else {
            //result.payload.Array->elements[i] = functionTable[(int)Array_get(thisToken, i).type][FUNC_add](Array_get(thisToken, i), Array_get(otherToken, i));
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::subtract(Array_get(otherToken, i))));
        }
    }

    va_end(argp);
    return result;
}
/**/


/***Array_multiply***/

// Array_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_multiply(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;
    
    va_list argp; 
    Token result; 
    Token otherToken;
    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.Array->size;
    size2 = otherToken.payload.Array->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = Array_new(resultSize, 0);
    
    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
            result.payload.Array->elements[i] = functionTable[(int)Array_get(thisToken, 0).type][FUNC_multiply](Array_get(thisToken, 0), Array_get(otherToken, i));
        } else if (size2 == 1) {
            result.payload.Array->elements[i] = functionTable[(int)Array_get(otherToken, 0).type][FUNC_multiply](Array_get(thisToken, i), Array_get(otherToken, 0));        
        } else {
            result.payload.Array->elements[i] = functionTable[(int)Array_get(thisToken, i).type][FUNC_multiply](Array_get(thisToken, i), Array_get(otherToken, i));
        }
    }

    va_end(argp);
    return result;
}
/**/

/***Array_divide***/

// Array_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
Token Array_divide(Token thisToken, ...) {
    int i;
    int size1;
    int size2;
    int resultSize;
    
    va_list argp; 
    Token result; 
    Token otherToken;
    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    size1 = thisToken.payload.Array->size;
    size2 = otherToken.payload.Array->size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = Array_new(resultSize, 0);
    
    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
            result.payload.Array->elements[i] = functionTable[(int)Array_get(thisToken, 0).type][FUNC_divide](Array_get(thisToken, 0), Array_get(otherToken, i));
        } else if (size2 == 1) {
            result.payload.Array->elements[i] = functionTable[(int)Array_get(otherToken, 0).type][FUNC_divide](Array_get(thisToken, i), Array_get(otherToken, 0));        
        } else {
            result.payload.Array->elements[i] = functionTable[(int)Array_get(thisToken, i).type][FUNC_divide](Array_get(thisToken, i), Array_get(otherToken, i));
        }
    }

    va_end(argp);
    return result;
}
/**/

/***Array_negate***/

// Array_negate: Negate each element of an array.
// Return a new Array token.
Token Array_negate(Token thisToken, ...) {
    int i;
    Token result; 

    result = Array_new(thisToken.payload.Array->size, 0);

    for (i = 0; i < thisToken.payload.Array->size; i++) {
        Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::negate()));
    }
    return result;
}
/**/

/***Array_zero***/

// Array_zero: Return an array like the specified
// array but with zeros of the same type.
Token Array_zero(Token token, ...) {
    Token result;
    Token element;
    int i;
        
    result = Array_new(token.payload.Array->size, 0);
    for (i = 0; i < token.payload.Array->size; i++) {
        element = Array_get(token, i);
        result.payload.Array->elements[i]
                        = functionTable[(int)element.type][FUNC_zero](element);
    }
    return result;
}
/**/

/***Array_one***/

// Array_one: Return an array like the specified
// array but with ones of the same type.
Token Array_one(Token token, ...) {
    Token result;
    Token element;
    int i;
        
    result = Array_new(token.payload.Array->size, 0);
    for (i = 0; i < token.payload.Array->size; i++) {
        element = Array_get(token, i);
        result.payload.Array->elements[i]
                        = functionTable[(int)element.type][FUNC_one](element);
    }
    return result;
}
/**/

/***Array_clone***/

// Array_clone: Return a new array just like the
// specified array.
Token Array_clone(Token token, ...) {
    Token result;
    Token element;
    int i;
        
    result = Array_new(token.payload.Array->size, 0);
    for (i = 0; i < token.payload.Array->size; i++) {
        element = Array_get(token, i);
        result.payload.Array->elements[i] = functionTable[(int)element.type][FUNC_clone](element);
    }
    return result;        
}
/**/




------------ static function -----------------------------------------------

/***Array_convert***/

// Array_convert: Convert the first argument array
// into the type specified by the second argument.
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
    
            
    result = Array_new(token.payload.Array->size, 0);
        
    for (i = 0; i < token.payload.Array->size; i++) {
        element = Array_get(token, i);
        if (targetType != element.type) {
            result.payload.Array->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
        } else {
            result.payload.Array->elements[i] = element;
        }    
    }

    va_end(argp);
    return result;
}    
/**/

