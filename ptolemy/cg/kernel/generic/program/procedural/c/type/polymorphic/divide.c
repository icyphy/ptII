/*** divide_Array_Array() ***/
 Token divide_Array_Array(Token *a1, Token *a2) {
    return $Array_divide(a1, a2);
}
/**/

/*** divide_Array_Double() ***/
Token* divide_Array_Double(Token *a1, double a2) {
    int i;
    Token *result = $new(Array(a1->payload.Array->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        Array_set(result, i, $divide_Token_Double(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Array_Int() ***/
Token* divide_Array_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(Array(a1->payload.Array->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        Array_set(result, i, $divide_Token_Int(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Array_Long() ***/
Token* divide_Array_Long(Token *a1, long long a2) {
    int i;
    Token *result = $new(Array(a1->payload.Array->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        Array_set(result, i, $divide_Token_Long(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Boolean_Boolean() ***/
boolean divide_Boolean_Boolean(boolean a1, boolean a2) {
    //if (!a2) {
    // FIXME: Illegal boolean divide.
    // throw exception("Illegal boolean division.");
    //}
    return a1;
}
/**/

/*** divide_DoubleArray_Double() ***/
Token* divide_DoubleArray_Double(Token *a1, double a2) {
    int i;
    Token *result = $new(DoubleArray(a1->payload.DoubleArray->size, 0));

    for (i = 0; i < a1->payload.DoubleArray->size; i++) {
            DoubleArray_set(result, i, $divide_Double_Double(DoubleArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_DoubleArray_Int() ***/
Token* divide_DoubleArray_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(DoubleArray(a1->payload.DoubleArray->size, 0));

    for (i = 0; i < a1->payload.DoubleArray->size; i++) {
            DoubleArray_set(result, i, $divide_Double_Int(DoubleArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Double_Array() ***/
Token* divide_Double_Array(double a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $divide_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Double_Double() ***/
double divide_Double_Double(double a1, double a2) {
    return a1 / a2;
}
/**/

/*** divide_Double_Int() ***/
double divide_Double_Int(double a1, int a2) {
    return a1 / a2;
}
/**/

/*** divide_Double_Token() ***/
Token* divide_Double_Token(double a1, Token *a2) {
    Token *token = $new(Double(a1));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Int_IntArray() ***/
Token* divide_Int_IntArray(int a1, Token *a2) {
    int i;
    Token *result = $new(IntArray(a2->payload.IntArray->size, 0));

    for (i = 0; i < a2->payload.IntArray->size; i++) {
        IntArray_set(result, i, $divide_Int_Int(a1, IntArray_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_IntArray_Int() ***/
Token* divide_IntArray_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(IntArray(a1->payload.IntArray->size, 0));

    for (i = 0; i < a1->payload.IntArray->size; i++) {
            IntArray_set(result, i, $divide_Int_Int(IntArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Int_DoubleArray() ***/
#define divide_Int_DoubleArray(a1, a2) $divide_Double_DoubleArray((double) a1, a2)
/**/


/*** divide_IntArray_IntArray() ***/
Token* divide_IntArray_IntArray(Token *a1, Token *a2) {
    return $IntArray_divide(a1, a2);
}
/**/

/*** divide_Int_Array() ***/
Token* divide_Int_Array(int a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $divide_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Int_Double() ***/
int divide_Int_Double(int a1, double a2) {
    return a1 / a2;
}
/**/

/*** divide_Int_Int() ***/
int divide_Int_Int(int a1, int a2) {
    return a1 / a2;
}
/**/

/*** divide_Int_Token() ***/
int divide_Int_Token(int a1, Token *a2) {
    Token *token = $new(Int(a1));
    return $typeFunc(TYPE_Int::divide(token, a2));
}
/**/

/*** divide_Long_Array() ***/
Token* divide_Long_Array(long long a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $divide_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Long_Long() ***/
long long divide_Long_Long(long long a1, long long a2) {
    return a1 / a2;
}
/**/

/*** divide_Long_Token() ***/
Token* divide_Long_Token(long long a1, Token *a2) {
    Token *token = $new(Long(a1));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Matrix_Double() ***/
Token* divide_Matrix_Double(Token *a1, double a2) {
    int i, j;
    Token *result = $new(Matrix(a1->payload.Matrix->row,
                    a1->payload.Matrix->column, 0));

    for (i = 0; i < a1->payload.Matrix->row; i++) {
        for (j = 0; j < a1->payload.Matrix->column; j++) {
            Matrix_set(result, i, j,
                    $divide_Token_Double(Matrix_get(a1, i, j), a2));
        }
    }
    return result;
}
/**/

/*** divide_Token_Double() ***/
Token* divide_Token_Double(Token *a1, double a2) {
    Token *token = $new(Double(a2));
    return $divide_Token_Token(a1, token);
}
/**/

/*** divide_Token_Int() ***/
int divide_Token_Int(Token *a1, int a2) {
    Token *token = $new(Int(a2));
    return $divide_Token_Token(a1, token);
}
/**/

/*** divide_Token_Token() ***/
Token* divide_Token_Token(Token *a1, Token *a2) {
    return $tokenFunc(a1::divide(a2));
}
/**/

/*** divide_one_Array() ***/
Token* divide_one_Array(Token *a1, ...) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $Array_divide(oneToken, a1);
}
/**/

/*** divide_one_Boolean() ***/
double divide_one_Boolean(boolean b, ...) {
    // FIXME: is this right?
    return b;
}
/**/

/*** divide_one_Double() ***/
double divide_one_Double(double d, ...) {
    return 1.0/d;
}
/**/

/*** divide_one_Int() ***/
int divide_one_Int(int i, ...) {
    return 1/i;
}
/**/

/*** divide_one_Long() ***/
long divide_one_Long(long l, ...) {
    return 1L/l;
}
/**/

/*** divide_one_Token() ***/
long divide_one_Token(Token *a1, ...) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $divide_Token_Token(a1, token);
}
/**/

