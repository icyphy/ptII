/*** Matrix_add() ***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token* Matrix_add(Token* thisToken, ...) {
    int i, j;
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    result = $new(Matrix(thisToken->payload.Matrix->row, thisToken->payload.Matrix->column, 0));

    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
            Matrix_set(result, j, i, functionTable[(int)Matrix_get(thisToken, i, j)->type][FUNC_add](Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** Matrix_convert() ***/
Token* Matrix_convert(Token* token, ...) {
    /* token->payload.Matrix = (MatrixToken) malloc(sizeof(struct matrix));
       token->payload.Matrix->row = 1;
       token->payload.Matrix->column = 1;
       result->payload.Matrix->elements = (Token*) calloc(1, sizeof(Token));
       token->type = TYPE_Matrix;
       Matrix_set(token, 0, 0, token);
       return token;
    */
    return $new(Matrix(1, 1, 1, token, token->type));
}
/**/

/*** Matrix_delete() ***/
Token* Matrix_delete(Token* token, ...) {
    int i, j;
    Token* element;

    // Delete each elements.
    for (i = 0; i < token->payload.Matrix->column; i++) {
        for (j = 0; j < token->payload.Matrix->row; j++) {
            element = Matrix_get(token, j, i);
            functionTable[(int) element->type][FUNC_delete](element);
        }
    }
    free(token->payload.Matrix->elements);
    free(token->payload.Matrix);

    return NULL;
}
/**/

/*** Matrix_divide() ***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token* Matrix_divide(Token* thisToken, ...) {
    int i, j, index;
    va_list argp;
    Token* result;
    Token* element;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    switch (otherToken->type) {
        case TYPE_Matrix:
        for (i = 0; i < thisToken->payload.Matrix->column; i++) {
            for (j = 0; j < thisToken->payload.Matrix->row; j++) {
                element = Matrix_get(thisToken, j, i);
                // FIXME: Need to program this.
            }
        }
        break;
        #ifdef TYPE_Array
        case TYPE_Array:
        // Divide reverse.
        result = $new(Array(otherToken->payload.Array->size, 0));
        for (i = 0; i < otherToken->payload.Array->size; i++) {
            element = Array_get(thisToken, i);
            result->payload.Array->elements[i] = functionTable[TYPE_Matrix][FUNC_divide](thisToken, element);
        }

        break;
        #endif
        default:
        result = $new(Matrix(thisToken->payload.Matrix->row, thisToken->payload.Matrix->column, 0));

        for (i = 0, index = 0; i < thisToken->payload.Matrix->column; i++) {
            for (j = 0; j < thisToken->payload.Matrix->row; j++, index++) {
                element = Matrix_get(thisToken, j, i);
                result->payload.Matrix->elements[index] = functionTable[(int)element->type][FUNC_divide](element, otherToken);
            }
        }
    }
    va_end(argp);
    return result;
}
/**/

/*** Matrix_equals() ***/
#ifdef TYPE_Matrix
Token* Matrix_equals(Token* thisToken, ...) {
    int i, j;
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);
    va_end(argp);

    if (( thisToken->payload.Matrix->row != otherToken->payload.Matrix->row ) ||
            ( thisToken->payload.Matrix->column != otherToken->payload.Matrix->column )) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
            if (!functionTable[(int) Matrix_get(thisToken, j, i)->type][FUNC_equals](Matrix_get(thisToken, j, i), Matrix_get(otherToken, j, i))->payload.Boolean) {
                return $new(Boolean(false));
            }
        }
    }
    return $new(Boolean(true));
}
#endif
/**/

/*** Matrix_isCloseTo() ***/
#ifdef TYPE_Matrix
Token* Matrix_isCloseTo(Token* thisToken, ...) {
    int i, j;
    va_list argp;
    Token* otherToken;
    Token* tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);
    tolerance = va_arg(argp, Token*);

    if (( thisToken->payload.Matrix->row != otherToken->payload.Matrix->row ) ||
            ( thisToken->payload.Matrix->column != otherToken->payload.Matrix->column )) {
        return $new(Boolean(false));
    }
    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
            if (!functionTable[(int) Matrix_get(thisToken, j, i)->type][FUNC_isCloseTo](Matrix_get(thisToken, j, i), Matrix_get(otherToken, j, i), tolerance)->payload.Boolean) {
                return $new(Boolean(false));
            }
        }
    }
    va_end(argp);
    return $new(Boolean(true));
}
#endif
/**/

/*** Matrix_multiply() ***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token* Matrix_multiply(Token* thisToken, ...) {
    int i, j;
    va_list argp;
    Token* result;
    Token* element;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);
    if (otherToken->type == TYPE_Matrix
            && otherToken->payload.Matrix->row == 1
            && otherToken->payload.Matrix->column == 1) {
        // Handle simple scaling by a 1x1 matrix
        result = $new(Matrix(thisToken->payload.Matrix->row, thisToken->payload.Matrix->column, 0));
    } else {
        result = $new(Matrix(thisToken->payload.Matrix->row, thisToken->payload.Matrix->row, 0));
    }
    switch (otherToken->type) {
        case TYPE_Matrix:
        for (i = 0; i < thisToken->payload.Matrix->column; i++) {
            for (j = 0; j < thisToken->payload.Matrix->row; j++) {
                element = Matrix_get(thisToken, j, i);
                if (otherToken->payload.Matrix->row == 1
                        && otherToken->payload.Matrix->column == 1) {
                    Matrix_set(result, j, i, functionTable[(int)element->type][FUNC_multiply](element, Matrix_get(otherToken, 0, 0)));
                }
            }
        }
        break;
        #ifdef TYPE_Array
        case TYPE_Array:
        element = $new(Array(thisToken->payload.Matrix->column *
        thisToken->payload.Matrix->row, 0));
        for (i = 0; i < thisToken->payload.Matrix->column; i++) {
            for (j = 0; j < thisToken->payload.Matrix->row; j++) {
                Array_set(element,
                i + thisToken->payload.Matrix->row * j,
                Matrix_get(thisToken, j, i));
            }
        }
        break;
        #endif
        default:
        for (i = 0; i < thisToken->payload.Matrix->column; i++) {
            for (j = 0; j < thisToken->payload.Matrix->row; j++) {
                element = Matrix_get(thisToken, j, i);
                result->payload.Matrix->elements[i] = functionTable[(int)element->type][FUNC_multiply](element, otherToken);
            }
        }
    }
    va_end(argp);
    return result;
}
/**/

/*** Matrix_new() ***/
// make a new matrix from the given values
// assume that number of the rest of the arguments == length,
// and they are in the form of (element, element, ...).
// The rest of the arguments should be of type Token* .
Token* Matrix_new(int row, int column, int given, ...) {
    va_list argp;
    int i;
    Token* result = malloc(sizeof(Token));
    char elementType;

    result->type = TYPE_Matrix;
    result->payload.Matrix = (MatrixToken) malloc(sizeof(struct matrix));
    result->payload.Matrix->row = row;
    result->payload.Matrix->column = column;

    // Allocate a new matrix of Tokens.
    if (row > 0 && column > 0) {
        // Allocate an new 2-D array of Tokens.
        result->payload.Matrix->elements = (Token**) calloc(row * column, sizeof(Token));

        if (given > 0) {
            // Set the first element.
            va_start(argp, given);

            for (i = 0; i < given; i++) {
                result->payload.Matrix->elements[i] = va_arg(argp, Token*);
            }

            // elementType is given as the last argument.
            elementType = va_arg(argp, int);

            if (elementType >= 0) {
                // convert the elements if needed.
                for (i = 0; i < given; i++) {
                    if (Matrix_get(result, i, 0)->type != elementType) {
                        result->payload.Matrix->elements[i] = functionTable[(int)elementType][FUNC_convert](Matrix_get(result, i, 0));
                    }
                }
            }

            va_end(argp);
        }
    }
    return result;
}
/**/

/*** Matrix_print() ***/
Token* Matrix_print(Token* thisToken, ...) {
    // Token string = Matrix_toString(thisToken);
    // printf(string->payload.String);
    // free(string->payload.String);

    int i, j;
    printf("[");
    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
        if (i != 0) {
            printf(", ");
        }
        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
            if (j != 0) {
                printf("; ");
            }
            functionTable[thisToken->payload.Matrix->elements[i * thisToken->payload.Matrix->row + j]->type][FUNC_print](thisToken->payload.Matrix->elements[i]);
        }
    }
    printf("]");
}
/**/

/*** Matrix_subtract() ***/
// Assume the given otherToken is array type.
// Return a new Array token.
Token* Matrix_subtract(Token* thisToken, ...) {
    int i, j;
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    result = $new(Matrix(thisToken->payload.Matrix->row, thisToken->payload.Matrix->column, 0));

    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
            Matrix_set(result, j, i, functionTable[(int)Matrix_get(thisToken, i, j)->type][FUNC_subtract](Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j)));
        }
    }

    va_end(argp);
    return result;
}
/**/

/*** Matrix_toExpression() ***/
Token* Matrix_toExpression(Token* thisToken, ...) {
    return Matrix_toString(thisToken);
}
/**/

/*** Matrix_toString() ***/
Token* Matrix_toString(Token* thisToken, ...) {
    int i, j;
    int currentSize, allocatedSize;
    char* string;
    Token* elementString;

    allocatedSize = 512;
    string = (char*) malloc(allocatedSize);
    string[0] = '[';
    string[1] = '\0';
    currentSize = 2;
    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
        if (i != 0) {
            strcat(string, "; ");
        }
        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
            if (j != 0) {
                strcat(string, ", ");
            }
            elementString = functionTable[(int) Matrix_get(thisToken, j, i)->type][FUNC_toString](Matrix_get(thisToken, j, i));
            currentSize += strlen(elementString->payload.String);
            if (currentSize > allocatedSize) {
                allocatedSize *= 2;
                string = (char*) realloc(string, allocatedSize);
            }

            strcat(string, elementString->payload.String);
            free(elementString->payload.String);
        }
    }
    strcat(string, "]");
    return $new(String(string));
}
/**/

/*** declareBlock() ***/
#include <stdarg.h>     // Needed Matrix_new va_* macros

struct matrix {
    unsigned int row;            // number of rows.
    unsigned int column;         // number of columns.
    Token** elements;            // matrix of pointers to the elements.
    //unsigned char elementsType;  // type of all the elements.
};

typedef struct matrix* MatrixToken;
/**/

/*** funcDeclareBlock() ***/
Token* Matrix_new(int row, int column, int given, ...);
Token* Matrix_get(Token* token, int row, int column);
void Matrix_set(Token* matrix, int row, int column, Token* element);
/**/

/*** funcImplementationBlock() ***/
Token* Matrix_get(Token* token, int row, int column) {
    return token->payload.Matrix->elements[column * token->payload.Matrix->row + row];
}

void Matrix_set(Token* matrix, int row, int column, Token* element) {
    matrix->payload.Matrix->elements[column * matrix->payload.Matrix->row + row] = element;
}
/**/

/*** matrixToArray() ***/
Token* matrixToArray(Token* thisToken) {
    int i, j, index;
    Token* result;
    Token* element;

    // Instantiate the result.
    switch (Matrix_get(thisToken, 0, 0)->type) {
      // This seems really wrong, dealing with DoubleArray and IntArray adds complexity
#ifdef TYPE_DoubleArray
    case TYPE_Double:
      result = DoubleArray_new(thisToken->payload.Matrix->column*thisToken->payload.Matrix->row, 0);
      break;
#endif
#ifdef TYPE_IntArray
    case TYPE_Int:
      result = IntArray_new(thisToken->payload.Matrix->column*thisToken->payload.Matrix->row, 0);
      break;
#endif
    default:
      result = Array_new(thisToken->payload.Matrix->column*thisToken->payload.Matrix->row, 0);
      break;
    }

    for (i = 0, index = 0; i < thisToken->payload.Matrix->column; i++) {
        for (j = 0; j < thisToken->payload.Matrix->row; j++, index++) {
            element = Matrix_get(thisToken, j, i);
            switch (element->type) {
                // This seems really wrong, dealing with DoubleArray and IntArray adds complexity
#ifdef TYPE_DoubleArray
            case TYPE_Double:
                result->payload.DoubleArray->elements[index] = element->payload.Double;
                break;
#endif
#ifdef TYPE_IntArray
            case TYPE_Int:
                result->payload.IntArray->elements[index] = element->payload.Int;
                break;
#endif
            default:
                result->payload.Array->elements[index] = element;
                break;
            }
        }
    }
    return result;
}
/**/

/*** Matrix_zero() ***/
// Matrix_zero: Return a matrix like the specified
// but with zeros of the same type.
Token* Matrix_zero(Token* token, ...) {
        Token *result;
        Token *element;
        int i;

        result = $new(Matrix(thisToken->payload.Matrix->row, thisToken->payload.Matrix->column, 0));
        for (i = 0; i < token->payload.Matrix->column; i++) {
            for (j = 0; j < token->payload.Matrix->row; j++) {
                element = Matrix_get(token, i, j);
                Matrix_set(result, j, i, functionTable[(int)Matrix_get(thisToken, i, j)->type][FUNC_zero](element));
        }
        return result;
}
/**/
