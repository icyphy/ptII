/***declareBlock***/
// ptolemy/cg/kernel/generic/program/procedural/java/type/Array.j
public class Array {
    public int size;
    public Token [] elements;
}
// Definition of the array struct.
//struct array {
//    int size;                                   // size of the array.
//    Token* elements;                            // array of Token elements.
//    //char elementType;                          // type of the elements.
//};
//typedef struct array* ArrayToken;
/**/

/***funcDeclareBlock***/

// Array_get: get an element of an array.
static Token Array_get(Token array, int i) {
    //return array.payload.Array->elements[i];
    return ((Array)(array.payload)).elements[i];
}

// Array_set: set an element of an array.
static void Array_set(Token array, int i, Token element) {
    //array.payload.Array->elements[i] = element;
    ((Array)(array.payload)).elements[i] = element;
}

// Array_resize: Change the size of an array,
// preserving those elements that fit.
static void Array_resize(Token array, int size) {
        //array.payload.Array->size = size;
        // FIXME: Does realloc() initialize memory? If not, then we need to do that.
        //array.payload.Array->elements = (Token*) realloc(array.payload.Array->elements, size * sizeof(Token));
}

// Array_insert: Append the specified element to the end of an array.
static void Array_insert(Token array, Token token) {
    //int oldSize = array.payload.Array->size++;
    //Array_resize(array, array.payload.Array->size);
    //array.payload.Array->elements[oldSize] = token;
}

int Array_length(Token array) {
    return ((Array)(array.payload)).size;
}

/**/

/***Array_new***/

// Array_new: Create a new array with the specified elements.
// The "size" argument specifies the size of the array, and
// the "given" argument specifies the number of provided elements
// (which will typically be <= size).
// The rest of the arguments are the provided elements (there
// should be "given" of them). The given elements
// should be of type Token.
// The last element is the type, which is why this takes Object...
// and not Token...
static Token Array_new(int size, int given, Object... elements) {
    int i;
    Token result = new Token();
    int elementType;

    Array array = new Array();
    array.size = size;
    array.elements = new Token[size];

    result.type = TYPE_Array;
    result.payload = array;

    if (size > 0 && given > 0) {

        for (i = 0; i < given; i++) {
            array.elements[i] = (Token)elements[i];
        }
        // elementType is given as the last argument.
        elementType = (Short)elements[i];

        if (elementType >= 0) {
           // convert the elements if needed.
           for (int j = 0; j < given; j++) {
              if (Array_get(result, j).type != elementType) {
                //Array_set(result, j, functionTable[(int)elementType][FUNC_convert](Array_get(result, j)));
                switch(elementType) {
                    case TYPE_Array:
                        System.out.println("Array_new on an array of arrays, possible problem");
                        break;
                    case TYPE_Token:
                        Array_set(result, j, Array_get(result,j));
                        break;
#ifdef PTCG_TYPE_Double
                    case TYPE_Double:
                        Array_set(result, j, Double_convert(Array_get(result,j)));
                        break;
#endif
#ifdef PTCG_TYPE_Integer
                    case TYPE_Integer:
                        Array_set(result, j, Integer_convert(Array_get(result,j)));
                        break;
#endif
                    default:
                        throw new RuntimeException("Array_new(): Conversion from an unsupported type: "
                                                   +  elementType);
                   }
                }
            }
        }
    }
    return result;
}
/**/


/***Array_delete***/

// Array_delete: FIXME: What does this do?
static Token Array_delete(Token token, Object... elements) {
    int i;
    Token element;

    // Delete each elements.
    for (i = 0; i < ((Array)(token.payload)).size; i++) {
            element = Array_get(token, i);
                System.out.println("Array_delete: convert needs work");
        //functionTable[(int)element.type][FUNC_delete](element);
    }
    //free(token.payload.Array->elements);
    //free(token.payload.Array);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return null;
}
/**/


/***Array_equals***/

// Array_equals: Test an array for equality with a second array.
static boolean Array_equals(Token thisToken, Token... tokens) {
    int i;
    Token otherToken = tokens[0];
    if (((Array)(thisToken.payload)).size != ((Array)(otherToken.payload)).size) {
        return false;
    }
    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        if (!equals_Token_Token(Array_get(thisToken, i), Array_get(otherToken, i))) {
            return false;
        }
    }

    return true;
}
/**/

/***Array_isCloseTo***/

// Array_isCloseTo: Test an array to see whether it is close in value to another.
static Token Array_isCloseTo(Token thisToken, Token... elements) {
    int i;
    Token otherToken = elements[0];
    Token tolerance = elements[1];

    if ( ((Array)(thisToken.payload)).size != ((Array)(otherToken.payload)).size) {
        //System.out.println("Array_isCloseTo sizes different:" + ((Array)(thisToken.payload)).size + " "
        //                   + ((Array)(otherToken.payload)).size);
        //print_Token3(thisToken);
        //print_Token3(otherToken);
        return Boolean_new(false);
    }
    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        //System.out.println("Array_isCloseTo " + i);
        //print_Token3(Array_get(thisToken, i));
        //print_Token3(Array_get(otherToken, i));
        if (!$isCloseTo_Token_Token(Array_get(thisToken, i), Array_get(otherToken, i), tolerance)) {
            return Boolean_new(false);
        }
    }

    return Boolean_new(true);
}
/**/

/***Array_print***/

// Array_print: Print the contents of an array to standard out.
static Token Array_print(Token thisToken, Token... tokens) {
    // Token string = Array_toString(thisToken);
    // System.out.printf(string.payload.String);
    // free(string.payload.String);

    StringBuffer results = new StringBuffer("{");
    for (int i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        if (i != 0) {
            results.append(", ");
        }
        // Arrays elements could have different types?
        if (((Array)(thisToken.payload)).elements == null) {
            results.append("elements == null");
        } else if (((Array)(thisToken.payload)).elements[i] == null) {
            results.append("elements[" + i + "] == null");
        } else {
            short elementType = ((Array)(thisToken.payload)).elements[i].type;
               switch(elementType) {
                case TYPE_Array:
                      results.append($Array_toString(((Array)(thisToken.payload)).elements[i]).payload);
                    break;
                  default:
                    results.append(((Array)(thisToken.payload)).elements[i].payload.toString());
                    break;
            }
        }
    }
    results.append("}");

    System.out.println(results.toString());
    return null;
}
/**/

/***Array_toString***/

// Array_toString: Return a string token with a string representation
// of the specified array.
static Token Array_toString(Token thisToken, Token... ignored) {
    StringBuffer result = new StringBuffer("{");
    if (thisToken == null) {
        result.append("null");
    } else {
        for (int i = 0; i < ((Array)(thisToken.payload)).size; i++) {
            if (i != 0) {
                result.append(", ");
            }
            // Arrays elements could have different types?
            if (thisToken.payload instanceof Array
                && ((Array)(thisToken.payload)).elements == null) {
                    result.append("elements == null");
            } else if (thisToken.payload instanceof Array
                && ((Array)(thisToken.payload)).elements[i] == null) {
                    result.append("elements[" + i + "] == null");
                    throw new RuntimeException("elements[] is null");
            } else {
                short elementType = ((Array)(thisToken.payload)).elements[i].type;
                   switch(elementType) {
                    case TYPE_Array:
                          result.append(Array_toString(((Array)(thisToken.payload)).elements[i]).payload);
                        break;
#ifdef PTCG_TYPE_Complex
                    case TYPE_Complex:
                          result.append(Complex_toString(((Array)(thisToken.payload)).elements[i]));
                            break;
#endif
                    case TYPE_String:
                        result.append("\"" + ((Array)(thisToken.payload)).elements[i].payload.toString() + "\"");
                        break;
                    default:
                        result.append(((Array)(thisToken.payload)).elements[i].payload.toString());
                        break;
                  }
            }
        }
    }
    result.append("}");
    return String_new(result.toString());
}
/**/

/***Array_add***/

// Array_add: Add an array to another array.
// Assume the given otherToken is array type.
// Return a new Array token.
static Token Array_add(Token thisToken, Token... tokens) {
    int i;
    int size1;
    int size2;
    int resultSize;

    Token result = new Token();
    Token otherToken;

    otherToken = tokens[0];

    size1 = ((Array)(thisToken.payload)).size;
    size2 = ((Array)(otherToken.payload)).size;
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
static Token Array_subtract(Token thisToken, Token... tokens) {
    int i;
    int size1;
    int size2;
    int resultSize;

    Token result;
    Token otherToken;

    otherToken = tokens[0];

    size1 = ((Array)(thisToken.payload)).size;
    size2 = ((Array)(otherToken.payload)).size;
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
    return result;
}
/**/


/***Array_multiply***/

// Array_multiply: Multiply an array by another array.
// Multiplication is element-wise.
// Assume the given otherToken is array type.
// Return a new Array token.
static Token Array_multiply(Token thisToken, Token... elements) {
    int i;
    int size1;
    int size2;
    int resultSize;

    Token result;
    Token otherToken;

    otherToken = elements[0];

    size1 = ((Array)(thisToken.payload)).size;
    size2 = ((Array)(otherToken.payload)).size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = Array_new(resultSize, 0);

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, 0)::multiply(Array_get(otherToken, i))));
        } else if (size2 == 1) {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::multiply(Array_get(otherToken, 0))));
        } else {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::multiply(Array_get(otherToken, i))));
        }
    }

    return result;
}
/**/

/***Array_divide***/

// Array_divide: Divide the elements of the first array
// by the elements of the second array.
// Assume the given otherToken is array type.
// Return a new Array token.
static Token Array_divide(Token thisToken, Token... elements) {
    int i;
    int size1;
    int size2;
    int resultSize;

    Token result;
    Token otherToken;

    otherToken = elements[0];
    size1 = ((Array)(thisToken.payload)).size;
    size2 = ((Array)(otherToken.payload)).size;
    resultSize = (size1 > size2) ? size1 : size2;

    result = Array_new(resultSize, 0);

    for (i = 0; i < resultSize; i++) {
        if (size1 == 1) {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, 0)::divide(Array_get(otherToken, i))));
        } else if (size2 == 1) {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::divide(Array_get(otherToken, 0))));
        } else {
            Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::divide(Array_get(otherToken, i))));
        }
    }

    return result;
}
/**/

/***Array_negate***/

// Array_negate: Negate each element of an array.
// Return a new Array token.
static Token Array_negate(Token thisToken, Token... tokens) {
    int i;
    Token result;

    result = Array_new(((Array)(thisToken.payload)).size, 0);

    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        Array_set(result, i, $tokenFunc(Array_get(thisToken, i)::negate()));
    }
    return result;
}
/**/

/***Array_zero***/

// Array_zero: Return an array like the specified
// array but with zeros of the same type.
static Token Array_zero(Token thisToken, Token... tokens) {
    Token result;
    Token element;
    int i;

    result = Array_new(((Array)(thisToken.payload)).size, 0);

    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        element = Array_get(thisToken, i);
        Array_set(result, i, $tokenFunc(element::zero()));
    }
    return result;
}
/**/

/***Array_one***/

// Array_one: Return an array like the specified
// array but with ones of the same type.
static Token Array_one(Token thisToken, Token... tokens) {
    Token result;
    Token element;
    int i;

    result = Array_new(((Array)(thisToken.payload)).size, 0);
    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        System.out.println("Array_one(): handle func table");
        //element = Array_get(token, i);
        //result.payload.Array->elements[i]
        //                = functionTable[(int)element.type][FUNC_one](element);
    }
    return result;
}
/**/

/***Array_clone***/

// Array_clone: Return a new array just like the
// specified array.
static Token Array_clone(Token token, Token... tokens) {
    Token result;
    Token element;
    int i;

    result = Array_new(((Array)(thisToken.payload)).size, 0);
    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        System.out.println("Array_clone(): handle func table");
        //element = Array_get(token, i);
        //result.payload.Array->elements[i] = functionTable[(int)element.type][FUNC_clone](element);
    }
    return result;
}
/**/


/***arraySum***/
static Token arraySum(Token token) {
        Token result;
        int i;
        if (((Array)(thisToken.payload)).size <= 0) {
                return token;
        } else {
                result = Array_get(token, 0);
        }

    for (i = 0; i < ((Array)(thisToken.payload)).size; i++) {
        result = $add_Token_Token(result, Array_get(token, i));
    }
    return result;
}
/**/

/***arrayRepeat***/
static Token arrayRepeat(int number, Token value) {
        Token result = $new(Array(number, 0));
        int i;

        for (i = 0; i < number; i++) {
        Array_set(result, i, value);
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
static Token Array_convert(Token token, Short... targetTypes) {
    int i;
    Token result;
    Token element;
    Short targetType;

    targetType = targetTypes[0];
    result = Array_new(((Array)token.payload).size, 0);

    for (i = 0; i < ((Array)token.payload).size; i++) {
        element = Array_get(token, i);
        if (targetType != element.type) {
            //result.payload.Array->elements[i] = functionTable[(int)targetType][FUNC_convert](element);
            switch (targetType) {
#ifdef PTCG_TYPE_String
            case TYPE_String:
                    element = String_convert(element);
            break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    element = Integer_convert(element);
            break;
#endif
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    element = Double_convert(element);
            break;
#endif
            case TYPE_Array:
                element = Array_convert(element, targetType);
            break;

           default:
               throw new RuntimeException("Array_convert(): Conversion from an unsupported type: " + targetType);
            }
            ((Array)(result.payload)).elements[i] = element;
        } else {
            ((Array)(result.payload)).elements[i] = element;
        }
    }

    return result;
}
/**/

