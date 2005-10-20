/***globalBlock***/
#define true 1
#define false 0

#define BOOLEAN_TYPE    0
#define INT_TYPE        1
#define DOUBLE_TYPE     2
#define STRING_TYPE     3
#define ARRAY_TYPE      4
#define MATRIX_TYPE     5

typedef struct token Token;
typedef struct integer Int;
typedef struct floating Double;
typedef struct string String;
typedef struct array Array;
typedef struct matrix Matrix;

typedef char* string;
typedef unsigned char boolean;

typedef union multiport Multiport;
union multiport {
    int intPort;
    char booleanPort;
    double doublePort;
    char* stringPort;
    Token* generalPort;           // for token types
};

Token* _convertType(int type2, Token* token);

struct token {                   // Base type for tokens
    unsigned char type;          // TYPE field has to be the first field
    void* payload;
};

struct integer {
    unsigned char type;          // INT_TYPE
    int value;                   // value.
};

struct floating {
    unsigned char type;          // DOUBLE_TYPE
    double value;                // value.
};

struct string {
    unsigned char type;          // STRING_TYPE
    char *value;                 // string value.
    unsigned int length;         // length of the string.    
};

struct array {
    unsigned char type;          // ARRAY_TYPE
    unsigned char elementsType;  // type of all the elements.
    unsigned int size;           // size of the array.
    Token **elements;            // array of pointers to the elements. 
};

struct matrix {
    unsigned char type;          // MATRIX_TYPE
    unsigned char elementsType;  // type of all the elements.
    unsigned int row;            // number of rows.
    unsigned int column;         // number of columns.
    Token **elements;            // matrix of pointers to the elements. 
};


Int* newInt(int i) {
    Int* result = (Int*) malloc(sizeof(Int));
    result->value = i;
    result->type = INT_TYPE;
    return result;
}

Double* newDouble(double d) {
    Double* result = (Double*) malloc(sizeof(Double));
    result->value = d;
    result->type = DOUBLE_TYPE;
    return result;
}

String* newString(char* s) {
    String* result = (String*) malloc(sizeof(String));
    result->value = s;
    result->type = STRING_TYPE;
    result->length = strlen(s);
    return result;
}


// make a new array from the given values (Int or Double type)
// assume that number of the rest of the arguments == 2 * length,
// and they are pairs in the form of (element, type, element, type, ...).
Array* newArray(int size, int elementsType, ...) {   
    int i;
    int* element = (&size) + 2;
    int type;

    Array* result = (Array*) malloc(sizeof(Array));
    result->elements = (Token**) calloc(size, sizeof(Token*));
    result->type = ARRAY_TYPE;
    result->elementsType = elementsType;
    result->size = size;

    for (i = 0; i < size; i++) {
        type = *(element + 1);
        result->elements[i] = (Token*) *element;
        element += 2;
    }

    // convert all the elements to the max type.
    for (i = 0; i < size; i++) {
        //result->elements[i] = _convertType(result->elementTypes[i], maxType, result->elements[i]);
        result->elements[i] = _convertType(elementsType, result->elements[i]);
    }
    return result;
}

Matrix* newMatrix(int row, int column, ...) {   
    int i, j;
    int* element = (&column) + 1;
    int type;
    int maxType = -1;

    Matrix* result = (Matrix*) malloc(sizeof(Matrix));
    result->elements = (Token**) calloc(row * column, sizeof(Token*));
    result->type = MATRIX_TYPE;
    result->row = row;
    result->column = column;

    for (i = 0; i < row; i++) {
        for (j = 0; j < column; j++) {
            type = *(element + 1);
            result->elements[i * column + j] = (Token*) *element;
            if (type > maxType) {
                maxType = type;
            }
            element += 2;
        }
    }
    
    for (i = 0; i < row; i++) {
        for (j = 0; j < column; j++) {
            result->elements[i * column + j] = _convertType(maxType, result->elements[i * column + j]);
        }
    }
    // FIXME: matrix construct
    return result;
}

// assume type2 > element->type, convert token to an token of type2.
// given a element pointer, convert to TYPE2, and return the new pointer.
// This function will free the old pointer location as well.
Token* _convertType(int type2, Token* token) {
    Token* oldToken = token;
    int type1 = token->type;
    Token* result = token;    // return the old pointer by default.

    switch (type1) {
        case INT_TYPE:
            switch (type2) {
                case DOUBLE_TYPE:
                    result = (Token*) newDouble(floor((double) ((Int*) token)->value));
                    free(oldToken);
                    break;
                case STRING_TYPE:
                    token = (Token*) malloc(sizeof(char) * 12);
                    sprintf((char*) token, "%d", ((Int*) oldToken)->value);
                    result = (Token*) newString((char*) token);
                    free(oldToken);
                    break;
                case ARRAY_TYPE:
                    result = (Token*) newArray(1, INT_TYPE, token, INT_TYPE);
                    break;
                case MATRIX_TYPE:
                    result = (Token*) newMatrix(1, 1, token, INT_TYPE);
                    break;
            }        
            break;
        case DOUBLE_TYPE:
            switch (type2) {
                case STRING_TYPE:
                    token = (Token*) malloc(sizeof(char) * 12);
                    sprintf((char*) token, "%g", ((Double*) oldToken)->value);
                    result = (Token*) newString((char*) token);
                    free(oldToken);
                    break;
                case ARRAY_TYPE:
                    result = (Token*) newArray(1, DOUBLE_TYPE, token, DOUBLE_TYPE);
                    break;
                case MATRIX_TYPE:
                    result = (Token*) newMatrix(1, 1, token, DOUBLE_TYPE);
                    break;
            }
            break;
           
        case STRING_TYPE:
            switch (type2) {
                case ARRAY_TYPE:
                    result = (Token*) newArray(1, STRING_TYPE, token, STRING_TYPE);
                    break;
                case MATRIX_TYPE:
                    result = (Token*) newMatrix(1, 1, token, STRING_TYPE);
                    break;
            }
            break;

        case ARRAY_TYPE:
            // FIXME: not finished
            break;
    }
    return result;
}
/**/
