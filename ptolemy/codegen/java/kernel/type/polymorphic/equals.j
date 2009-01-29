/*** equals_Array_Array() ***/
inline boolean equals_Array_Array(Token a1, Token a2) {
    return $Array_equals(a1, a2);
}
/**/

/*** equals_Array_Double() ***/
inline boolean equals_Array_Double(Token a1, double a2) {
    return $equals_Double_Array(a2, a1);
}
/**/

/*** equals_Array_Int() ***/
inline boolean equals_Int_Array(Token a1, int a2) {
    return $equals_Array_Int(a2, a1);
}
/**/

/*** equals_Array_Long() ***/
inline boolean equals_Long_Array(Token a1, long long a2) {
    return $equals_Array_Long(a2, a1);
}
/**/

/*** equals_Boolean_Boolean() ***/
inline boolean equals_Boolean_Boolean(boolean a1, boolean a2) {
	// logical comparison.
    return (!a1 == !a2);
}
/**/

/*** equals_Boolean_Int() ***/
inline boolean equals_Boolean_Int(boolean a1, int a2) {
    return $equals_Int_Boolean(a2, a1);
}
/**/

/*** equals_Boolean_String() ***/
inline boolean equals_Boolean_String(boolean a1, char* a2) {
    return $equals_String_Boolean(a2, a1);
}
/**/

/*** equals_Double_Array() ***/
boolean equals_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(((array)(a1.payload)).size, 0));

    for (i = 0; i < ((array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $equals_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Double_Double() ***/
inline boolean equals_Double_Double(double a1, double a2) {
    return a1 == a2;
}
/**/

/*** equals_Double_Int() ***/
inline boolean equals_Double_Int(double a1, int a2) {
    return a1 == (double) a2;
}
/**/

/*** equals_Double_String() ***/
inline boolean equals_Double_String(double a1, char* a2) {
    return $equals_String_Double(a2, a1);
}
/**/

/*** equals_Double_Token() ***/
boolean equals_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $equals_Token_Token(token, a2);
}
/**/

/*** equals_Int_Array() ***/
boolean equals_Int_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(((array)(a1.payload)).size, 0));

    for (i = 0; i < ((array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $equals_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Int_Boolean() ***/
inline boolean equals_Int_Boolean(int a1, boolean a2) {
    return $equals_String_Int(a2, a1);
}
/**/

/*** equals_Int_Int() ***/
inline boolean equals_Int_Int(int a1, int a2) {
    return a1 == a2;
}
/**/

/*** equals_Int_String() ***/
boolean equals_Int_String(int a1, char* a2) {
    return a1 == atoi(a2);
}
/**/

/*** equals_Int_Token() ***/
boolean equals_Int_Token(int a1, Token a2) {
    Token token = $new(Int, a1);
    return $typeFunc(TYPE_Int::equals(token, a2));
}
/**/

/*** equals_Long_Array() ***/
boolean equals_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(((array)(a1.payload)).size, 0));

    for (i = 0; i < ((array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $equals_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Long_Long() ***/
inline boolean equals_Long_Long(long long a1, long long a2) {
    return a1 == a2;
}
/**/

/*** equals_Long_Token() ***/
boolean equals_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $equals_Token_Token(token, a2);
}
/**/

/*** equals_String_Boolean() ***/
boolean equals_String_Boolean(char* a1, boolean a2) {
    char* a2String = a2 ? "true" : "false";
    return strcmp(a1, a2String) == 0 ? true : false;
}
/**/

/*** equals_String_Double() ***/
boolean equals_String_Double(char* a1, double a2) {
	return atof(a1) == a2;
}
/**/

/*** equals_String_Int() ***/
boolean char* equals_String_Int(char* a1, int a2) {
	return atoi(a1) == a2;
}
/**/

/*** equals_String_String() ***/
boolean equals_String_String(char* a1, char* a2) {
    return strcmp(a1, a2) == 0 ? true : false;
}
/**/

/*** equals_Token_Double() ***/
inline boolean equals_Token_Double(Token a1, double a2) {
    return $equals_Double_Token(a2, a1);
}
/**/

/*** equals_Token_Int() ***/
inline boolean equals_Token_Int(Token a1, int a2) {
    return $equals_Int_Token(a2, a1);
}
/**/

/*** equals_Token_Token() ***/
boolean equals_Token_Token(Token a1, Token a2) {
    boolean result = false;
    if (a1.payload instanceof Number && a2.payload instanceof Number) {
        result = ((Number)(a1.payload)).equals((Number)(a2.payload));
    } else if (a1.payload instanceof array && a2.payload instanceof array) {
        result = $Array_equals(a1, a2);
    } else {
        throw new InternalError("equals_Token_Token_(): equalswith an unsupported type. " + a1.type + " " + a2.type);
    }
    return result;
}
/**/

/*** isCloseTo_Token_Token() ***/
boolean isCloseTo_Token_Token(Token thisToken, Token otherToken, Token tolerance) {
    boolean result = false;
    if (thisToken.payload instanceof Number 
            && thisToken.payload instanceof Number
            && tolerance.payload instanceof Number) {
        result = 
	Math.abs((((Double)(thisToken.payload)) - ((Double)(otherToken.payload)))) < (Double)(tolerance.payload);
    } else { 
        throw new InternalError("equals_Token_Token_(): iscloseTo with an unsupported type. (%d" + thisToken.type + " " + otherToken.type + " " + tolerance.type);
    }
    return result;
}
/**/

