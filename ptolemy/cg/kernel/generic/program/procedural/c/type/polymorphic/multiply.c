/*** multiply_Array_Array() ***/
Token* multiply_Array_Array(Token *a1, Token *a2) {
    return $Array_multiply(a1, a2);
}
/**/

/*** multiply_Array_Double() ***/
Token* multiply_Array_Double(Token *a1, double a2) {
    return $multiply_Double_Array(a2, a1);
}
/**/

/*** multiply_Array_Int() ***/
Token* multiply_Array_Int(Token *a1, int a2) {
    return $multiply_Int_Array(a2, a1);
}
/**/

/*** multiply_Array_Long() ***/
Token* multiply_Array_Long(Token *a1, long long a2) {
    return $multiply_Long_Array(a2, a1);
}
/**/

/*** multiply_Boolean_Boolean() ***/
boolean multiply_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 & a2;
}
/**/

/*** multiply_DoubleArray_Double() ***/
Token* multiply_DoubleArray_Double(Token *a1, double a2) {
    int i;
    Token *result = $new(DoubleArray(a1->payload.DoubleArray->size, 0));

    for (i = 0; i < a1->payload.DoubleArray->size; i++) {
            DoubleArray_set(result, i, $multiply_Double_Double(DoubleArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** multiply_DoubleArray_DoubleArray() ***/
Token* multiply_DoubleArray_DoubleArray(Token *a1, Token *a2) {
    return $DoubleArray_multiply(a1, a2);
}
/**/

/*** multiply_Double_Array() ***/
Token* multiply_Double_Array(double a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $multiply_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Double_Double() ***/
double multiply_Double_Double(double a1, double a2) {
    return a1 * a2;
}
/**/

/*** multiply_Double_DoubleArray() ***/
#define multiply_Double_DoubleArray(a1, a2) $multiply_DoubleArray_Double(a2, a1)
/**/

/*** multiply_Double_Int() ***/
double multiply_Double_Int(double a1, int a2) {
    return a1 * a2;
}
/**/

/*** multiply_Double_IntArray() ***/
Token* multiply_Double_IntArray(double a1, Token *a2) {
        return $multiply_Double_DoubleArray(a1, $convert_IntArray_DoubleArray(a2));
}
/**/

/*** multiply_Double_Token() ***/
Token* multiply_Double_Token(double a1, Token *a2) {
    Token *token = $new(Double(a1));
    return $multiply_Token_Token(token, a2);
}
/**/

/*** multiply_IntArray_Int() ***/
Token* multiply_IntArray_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(IntArray(a1->payload.IntArray->size, 0));

    for (i = 0; i < a1->payload.IntArray->size; i++) {
            IntArray_set(result, i, $multiply_Int_Int(IntArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** multiply_IntArray_IntArray() ***/
Token* multiply_IntArray_IntArray(Token *a1, Token *a2) {
    return $IntArray_multiply(a1, a2);
}
/**/

/*** multiply_Int_Array() ***/
Token* multiply_Int_Array(int a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $multiply_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Int_Double() ***/
double multiply_Int_Double(int a1, double a2) {
    return a1 * a2;
}
/**/

/*** multiply_Int_DoubleArray() ***/
#define multiply_Int_DoubleArray(a1, a2) $multiply_Double_DoubleArray((double) a1, a2)
/**/

/*** multiply_Int_Int() ***/
int multiply_Int_Int(int a1, int a2) {
    return a1 * a2;
}
/**/

/*** multiply_Int_IntArray() ***/
#define multiply_Int_IntArray(a1, a2) $multiply_IntArray_Int(a2, a1)
/**/

/*** multiply_Int_Token() ***/
int multiply_Int_Token(int a1, Token *a2) {
    Token *token = $new(Int(a1));
    return $typeFunc(TYPE_Int::multiply(token, a2));
}
/**/

/*** multiply_Long_Array() ***/
Token* multiply_Long_Array(long long a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $multiply_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Long_Long() ***/
long long multiply_Long_Long(long long a1, long long a2) {
    return a1 * a2;
}
/**/

/*** multiply_Long_Token() ***/
Token* multiply_Long_Token(long long a1, Token *a2) {
    Token *token = $new(Long(a1));
    return $multiply_Token_Token(token, a2);
}
/**/

/*** multiply_Matrix_Matrix() ***/
Token* multiply_Matrix_Matrix(Token *a1, Token *a2) {
    return Matrix_multiply(a1, a2);
}
/**/

/*** multiply_Token_Double() ***/
Token* multiply_Token_Double(Token *a1, double a2) {
    return $multiply_Double_Token(a2, a1);
}
/**/

/*** multiply_Token_Int() ***/
int multiply_Token_Int(Token *a1, int a2) {
    return $multiply_Int_Token(a2, a1);
}
/**/

/*** multiply_Token_Token() ***/
Token* multiply_Token_Token(Token *a1, Token *a2) {
    return $tokenFunc(a1::multiply(a2));
}
/**/

