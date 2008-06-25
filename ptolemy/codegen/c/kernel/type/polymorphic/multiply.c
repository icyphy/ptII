/*** convert_Matrix_Matrix() ***/
inline Token convert_Matrix_Matrix(Token a1) {
    return a1;
}
/**/

/*** multiply_Array_Array() ***/
inline Token multiply_Array_Array(Token a1, Token a2) {
    return $Array_multiply(a1, a2);
}
/**/

/*** multiply_Array_Double() ***/
inline Token multiply_Array_Double(Token a1, double a2) {
    return $multiply_Double_Array(a2, a1);
}
/**/

/*** multiply_Array_Int() ***/
inline Token multiply_Int_Array(Token a1, int a2) {
    return $multiply_Array_Int(a2, a1);
}
/**/

/*** multiply_Array_Long() ***/
inline Token multiply_Long_Array(Token a1, long long a2) {
    return $multiply_Array_Long(a2, a1);
}
/**/

/*** multiply_Boolean_Boolean() ***/
inline boolean multiply_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 & a2;
}
/**/

/*** multiply_Double_Array() ***/
Token multiply_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $multiply_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Double_Double() ***/
inline double multiply_Double_Double(double a1, double a2) {
    return a1 * a2;
}
/**/

/*** multiply_Double_Int() ***/
inline double multiply_Double_Int(double a1, int a2) {
    return a1 * a2;
}
/**/

/*** multiply_Double_Token() ***/
Token multiply_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $multiply_Token_Token(token, a2);
}
/**/

/*** multiply_Int_Array() ***/
Token multiply_Int_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $multiply_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Int_Double() ***/
inline double multiply_Int_Double(int a1, double a2) {
    return a1 * a2;
}
/**/


/*** multiply_Int_Int() ***/
inline int multiply_Int_Int(int a1, int a2) {
    return a1 * a2;
}
/**/

/*** multiply_Int_Token() ***/
int multiply_Int_Token(int a1, Token a2) {
    Token token = $new(Int, a1);
    return $typeFunc(TYPE_Int::multiply(token, a2));
}
/**/

/*** multiply_Long_Array() ***/
Token multiply_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $multiply_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Long_Long() ***/
inline long long multiply_Long_Long(long long a1, long long a2) {
    return a1 * a2;
}
/**/

/*** multiply_Long_Token() ***/
Token multiply_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $multiply_Token_Token(token, a2);
}
/**/

/*** multiply_Matrix_Matrix() ***/
inline Token multiply_Matrix_Matrix(Token a1, Token a2) {
    return Matrix_multiply(a1, a2);
}
/**/

/*** multiply_Token_Double() ***/
inline Token multiply_Token_Double(Token a1, double a2) {
    return $multiply_Double_Token(a2, a1);
}
/**/

/*** multiply_Token_Int() ***/
inline int multiply_Token_Int(Token a1, int a2) {
    return $multiply_Int_Token(a2, a1);
}
/**/

/*** multiply_Token_Token() ***/
inline Token multiply_Token_Token(Token a1, Token a2) {
    return $tokenFunc(a1::multiply(a2));
}
/**/

