/*** subtract_Array_Array() ***/
Token* subtract_Array_Array(Token *a1, Token *a2) {
    return $Array_subtract(a1, a2);
}
/**/

/*** subtract_Array_Double() ***/
Token* subtract_Array_Double(Token *a1, double a2) {
    int i;
    Token *result = $new(Array(a1->payload.Array->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        Array_set(result, i, $subtract_Token_Double(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_Array_Int() ***/
inline Token subtract_Array_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(Array(a1->payload.Array->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        Array_set(result, i, $subtract_Token_Int(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_Array_Long() ***/
inline Token subtract_Array_Long(Token *a1, long long a2) {
    int i;
    Token *result = $new(Array(a1->payload.Array->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        Array_set(result, i, $subtract_Token_Long(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_BooleanArray_BooleanArray() ***/
Token* subtract_BooleanArray_BooleanArray(Token *a1, Token *a2) {
    return $BooleanArray_subtract(a1, a2);
}
/**/

/*** subtract_Boolean_Boolean() ***/
inline boolean subtract_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 | a2;
}
/**/

/*** subtract_Boolean_Int() ***/
inline int subtract_Boolean_Int(boolean a1, int a2) {
    //return $subtract_Int_Boolean(a2, a1);
}
/**/

/*** subtract_DoubleArray_Double() ***/
Token* subtract_DoubleArray_Double(Token *a1, double a2) {
    int i;
    Token *result = $new(DoubleArray(a1->payload.DoubleArray->size, 0));

    for (i = 0; i < a1->payload.Array->size; i++) {
        DoubleArray_set(result, i, DoubleArray_get(a1, i) - a2);
    }
    return result;
}
/**/

/*** subtract_DoubleArray_DoubleArray() ***/
Token* subtract_DoubleArray_DoubleArray(Token *a1, Token *a2) {
    return $DoubleArray_subtract(a1, a2);
}
/**/

/*** subtract_Double_Array() ***/
Token* subtract_Double_Array(double a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $subtract_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** subtract_Double_Double() ***/
#define subtract_Double_Double(a1, a2) ((a1) - (a2))
/**/

/*** subtract_Double_Int() ***/
inline double subtract_Double_Int(double a1, int a2) {
    return a1 - a2;
}
/**/

/*** subtract_Double_Token() ***/
Token* subtract_Double_Token(double a1, Token *a2) {
    Token *token = $new(Double(a1));
    return $subtract_Token_Token(token, a2);
}
/**/

/*** subtract_Int_IntArray() ***/
#define subtract_Int_IntArray(a1, a2) $subtract_IntArray_Int(a2, a1)
/**/

/*** subtract_IntArray_IntArray() ***/
Token* subtract_IntArray_IntArray(Token *a1, Token *a2) {
    return $IntArray_subtract(a1, a2);
}
/**/

/*** subtract_IntArray_Int() ***/
Token* subtract_IntArray_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(IntArray(a1->payload.IntArray->size, 0));

    for (i = 0; i < a1->payload.IntArray->size; i++) {
            IntArray_set(result, i, $subtract_Int_Int(IntArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** subtract_Int_Array() ***/
Token* subtract_Int_Array(int a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $subtract_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** subtract_Int_Boolean() ***/
int subtract_Int_Boolean(int a1, boolean a2) {
    return a1 - (a2 ? 1 : 0);
}
/**/

/*** subtract_Int_Int() ***/
int subtract_Int_Int(int a1, int a2) {
    return a1 - a2;
}
/**/

/*** subtract_Int_Token() ***/
int subtract_Int_Token(int a1, Token *a2) {
    Token *token = $new(Int(a1));
    return $typeFunc(TYPE_Int::subtract(token, a2));
}
/**/

/*** subtract_Long_Array() ***/
Token* subtract_Long_Array(long long a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $subtract_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** subtract_Long_Long() ***/
long long subtract_Long_Long(long long a1, long long a2) {
    return a1 - a2;
}
/**/

/*** subtract_Long_Token() ***/
Token* subtract_Long_Token(long long a1, Token *a2) {
    Token *token = $new(Long(a1));
    return $subtract_Token_Token(token, a2);
}
/**/

/*** subtract_StringArray_StringArray() ***/
Token* subtract_StringArray_StringArray(Token *a1, Token *a2) {
    return $StringArray_subtract(a1, a2);
}
/**/

/*** subtract_Token_Double() ***/
inline Token subtract_Token_Double(Token *a1, double a2) {
    Token *token = $new(Double(a2));
    return $subtract_Token_Token(a1, token);
}
/**/

/*** subtract_Token_Int() ***/
inline int subtract_Token_Int(Token *a1, int a2) {
    Token *token = $new(Int(a2));
    return $typeFunc(TYPE_Int::subtract(a1, token));
}
/**/

/*** subtract_Token_Token() ***/
inline Token subtract_Token_Token(Token *a1, Token *a2) {
    return $tokenFunc(a1::subtract(a2));
}
/**/

