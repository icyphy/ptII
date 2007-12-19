/*** divide_Array_Array() ***/
inline Token divide_Array_Array(Token a1, Token a2) {
    return $Array_divide(a1, a2);
}
/**/

/*** divide_Array_Double() ***/
inline Token divide_Array_Double(Token a1, double a2) {
    int i;
    Token result = $new(Array(a1.payload.Array->size, 0));

    for (i = 0; i < a1.payload.Array->size; i++) {
        Array_set(result, i, $divide_Token_Double(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Array_Int() ***/
Token divide_Int_Array(Token a1, int a2) {
    int i;
    Token result = $new(Array(a1.payload.Array->size, 0));

    for (i = 0; i < a1.payload.Array->size; i++) {
        Array_set(result, i, $divide_Token_Int(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Array_Long() ***/
inline Token divide_Long_Array(Token a1, long long a2) {
    int i;
    Token result = $new(Array(a1.payload.Array->size, 0));

    for (i = 0; i < a1.payload.Array->size; i++) {
        Array_set(result, i, $divide_Token_Long(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Boolean_Boolean() ***/
inline boolean divide_Boolean_Boolean(boolean a1, boolean a2) {
    //if (!a2) {
    // FIXME: Illegal boolean divide.
    // throw exception("Illegal boolean division.");    
    //} 
    return a1;
}
/**/

/*** divide_Double_Array() ***/
Token divide_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $divide_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Double_Double() ***/
inline double divide_Double_Double(double a1, double a2) {
    return a1 / a2;
}
/**/

/*** divide_Double_Int() ***/
inline double divide_Double_Int(double a1, int a2) {
    return a1 / a2;
}
/**/

/*** divide_Double_Token() ***/
Token divide_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Int_Array() ***/
Token divide_Int_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $divide_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Int_Int() ***/
inline int divide_Int_Int(int a1, int a2) {
    return a1 / a2;
}
/**/

/*** divide_Int_Token() ***/
int divide_Int_Token(int a1, Token a2) {
    Token token = $new(Int, a1);
    return $typeFunc(TYPE_Int::divide(token, a2));
}
/**/

/*** divide_Long_Array() ***/
Token divide_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $divide_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Long_Long() ***/
inline long long divide_Long_Long(long long a1, long long a2) {
    return a1 / a2;
}
/**/

/*** divide_Long_Token() ***/
Token divide_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Token_Double() ***/
inline Token divide_Token_Double(Token a1, double a2) {
    Token token = $new(Double(a2));
    return $divide_Token_Token(a1, token);
}
/**/

/*** divide_Token_Int() ***/
inline int divide_Token_Int(Token a1, int a2) {
    Token token = $new(Int(a2));
    return $divide_Token_Token(a1, token);
}
/**/

/*** divide_Token_Token() ***/
inline Token divide_Token_Token(Token a1, Token a2) {
    return $tokenFunc(a1::divide(a2));
}
/**/

