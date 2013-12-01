/*** modulo_Array_Array() ***/
Token* modulo_Array_Array(Token *a1, Token *a2) {
    return $Array_modulo(a1, a2);
}
/**/

/*** modulo_Array_Double() ***/
Token* modulo_Array_Double(Token *a1, double a2) {
    return $modulo_Double_Array(a2, a1);
}
/**/

/*** modulo_Array_Int() ***/
Token* modulo_Array_Int(Token *a1, int a2) {
    return $modulo_Int_Array(a2, a1);
}
/**/

/*** modulo_Array_Long() ***/
Token* modulo_Array_Long(Token *a1, long long a2) {
    return $modulo_Long_Array(a2, a1);
}
/**/

/*** modulo_BooleanArray_BooleanArray() ***/
Token* modulo_BooleanArray_BooleanArray(Token *a1, Token *a2) {
    return $BooleanArray_modulo(a1, a2);
}
/**/

/*** modulo_Boolean_Boolean() ***/
boolean modulo_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 | a2;
}
/**/

/*** modulo_Boolean_Int() ***/
int modulo_Boolean_Int(boolean a1, int a2) {
    return $modulo_Int_Boolean(a2, a1);
}
/**/

/*** modulo_DoubleArray_Double() ***/
Token* modulo_DoubleArray_Double(Token *a1, double a2) {
    return $modulo_Double_DoubleArray(a2, a1);
}
/**/

/*** modulo_DoubleArray_DoubleArray() ***/
Token* modulo_DoubleArray_DoubleArray(Token *a1, Token *a2) {
    return $DoubleArray_modulo(a1, a2);
}
/**/

/*** modulo_Double_Array() ***/
Token* modulo_Double_Array(double a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $modulo_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** modulo_Double_Double() ***/
double modulo_Double_Double(double a1, double a2) {
    return a1 % a2;
}
/**/

/*** modulo_Double_DoubleArray() ***/
Token* modulo_Double_DoubleArray(double a1, Token *a2) {
    int i;
    Token *result = $new(DoubleArray(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.DoubleArray->size; i++) {
            DoubleArray_set(result, i, $modulo_Double_Double(a1, DoubleArray_get(a2, i)));
    }
    return result;
}
/**/

/*** modulo_Double_Int() ***/
double modulo_Double_Int(double a1, int a2) {
    return a1 % a2;
}
/**/

/*** modulo_Double_Token() ***/
Token* modulo_Double_Token(double a1, Token *a2) {
    Token *token = $new(Double(a1));
    return $modulo_Token_Token(token, a2);
}
/**/

/*** modulo_IntArray_IntArray() ***/
Token* modulo_IntArray_IntArray(Token *a1, Token *a2) {
    return $IntArray_modulo(a1, a2);
}
/**/

/*** modulo_Int_IntArray() ***/
#define modulo_Int_IntArray(a1, a2) $modulo_IntArray_Int(a2, a1)
/**/

/*** modulo_IntArray_Int() ***/
Token* modulo_IntArray_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(IntArray(a1->payload.IntArray->size, 0));

    for (i = 0; i < a1->payload.IntArray->size; i++) {
            IntArray_set(result, i, $modulo_Int_Int(IntArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** modulo_Int_Array() ***/
Token* modulo_Int_Array(int a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $modulo_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** modulo_Int_Boolean() ***/
int modulo_Int_Boolean(int a1, boolean a2) {
    return a1 % (a2 ? 1 : 0);
}
/**/

/*** modulo_Int_Int() ***/
int modulo_Int_Int(int a1, int a2) {
    return a1 % a2;
}
/**/


/*** modulo_Int_Token() ***/
int modulo_Int_Token(int a1, Token *a2) {
    Token *token = $new(Int(a1));
    return $typeFunc(TYPE_Int::modulo(token, a2));
}
/**/

/*** modulo_Long_Array() ***/
Token* modulo_Long_Array(long long a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $modulo_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** modulo_Long_Long() ***/
long long modulo_Long_Long(long long a1, long long a2) {
    return a1 % a2;
}
/**/

/*** modulo_Long_Token() ***/
Token* modulo_Long_Token(long long a1, Token *a2) {
    Token *token = $new(Long(a1));
    return $modulo_Token_Token(token, a2);
}
/**/

/*** modulo_Token_Double() ***/
Token* modulo_Token_Double(Token *a1, double a2) {
    return $modulo_Double_Token(a2, a1);
}
/**/

/*** modulo_Token_Int() ***/
int modulo_Token_Int(Token *a1, int a2) {
    return $modulo_Int_Token(a2, a1);
}
/**/

/*** modulo_Token_Token() ***/
Token* modulo_Token_Token(Token *a1, Token *a2) {
    return $tokenFunc(a1::modulo(a2));
}
/**/

