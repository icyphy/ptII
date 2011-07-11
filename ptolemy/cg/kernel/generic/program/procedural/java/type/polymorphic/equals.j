/*** equals_Array_Array() ***/
static boolean equals_Array_Array(Token a1, Token a2) {
    return $Array_equals(a1, a2);
}
/**/

/*** equals_Array_Double() ***/
static boolean equals_Array_Double(Token a1, double a2) {
    return $equals_Double_Array(a2, a1);
}
/**/

/*** equals_Array_Integer() ***/
static boolean equals_Int_Array(Token a1, int a2) {
    return $equals_Array_Integer(a2, a1);
}
/**/

/*** equals_Array_Long() ***/
static boolean equals_Long_Array(Token a1, long long a2) {
    return $equals_Array_Long(a2, a1);
}
/**/

/*** equals_Boolean_Boolean() ***/
static boolean equals_Boolean_Boolean(boolean a1, boolean a2) {
        // logical comparison.
    return (!a1 == !a2);
}
/**/

/*** equals_Boolean_Integer() ***/
static boolean equals_Boolean_Integer(boolean a1, int a2) {
    return $equals_Int_Boolean(a2, a1);
}
/**/

/*** equals_Boolean_String() ***/
static boolean equals_Boolean_String(boolean a1, String a2) {
    return $equals_String_Boolean(a2, a1);
}
/**/

/*** equals_Double_Array() ***/
static boolean equals_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $equals_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Double_Double() ***/
static boolean equals_Double_Double(double a1, double a2) {
    return a1 == a2;
}
/**/

/*** equals_Double_Integer() ***/
static boolean equals_Double_Integer(double a1, int a2) {
    return a1 == (double) a2;
}
/**/

/*** equals_Double_String() ***/
static boolean equals_Double_String(double a1, String a2) {
    return $equals_String_Double(a2, a1);
}
/**/

/*** equals_Double_Token() ***/
static boolean equals_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $equals_Token_Token(token, a2);
}
/**/

/*** equals_Int_Array() ***/
static boolean equals_Int_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $equals_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Int_Boolean() ***/
static boolean equals_Int_Boolean(int a1, boolean a2) {
    return $equals_String_Integer(a2, a1);
}
/**/

/*** equals_Int_Integer() ***/
static boolean equals_Int_Integer(int a1, int a2) {
    return a1 == a2;
}
/**/

/*** equals_Int_String() ***/
static boolean equals_Int_String(int a1, String a2) {
    return a1 == atoi(a2);
}
/**/

/*** equals_Int_Token() ***/
static boolean equals_Int_Token(int a1, Token a2) {
    Token token = $new(Int, a1);
    return $typeFunc(TYPE_Int::equals(token, a2));
}
/**/

/*** equals_Long_Array() ***/
static boolean equals_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $equals_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** equals_Long_Long() ***/
static boolean equals_Long_Long(long long a1, long long a2) {
    return a1 == a2;
}
/**/

/*** equals_Long_Token() ***/
static boolean equals_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $equals_Token_Token(token, a2);
}
/**/

/*** equals_String_Boolean() ***/
static boolean equals_String_Boolean(String a1, boolean a2) {
    if (a2 && a1.equals("true")
            || !a2 && a2.equals("false")) {
        return true;
    }
    return false;
}
/**/

/*** equals_String_Double() ***/
static boolean equals_String_Double(String a1, double a2) {
    // FIXME: What about epsilon?
    return Double.valueOf(a1) == a2;
}
/**/

/*** equals_String_Integer() ***/
static boolean equals_String_Integer(String a1, int a2) {
    return Integer.valueOf(a1) == a1;
}
/**/

/*** equals_String_String() ***/
static boolean equals_String_String(String a1, String a2) {
    return a1.equals(a2);
}
/**/

/*** equals_Token_Double() ***/
static boolean equals_Token_Double(Token a1, double a2) {
    return $equals_Double_Token(a2, a1);
}
/**/

/*** equals_Token_Integer() ***/
static boolean equals_Token_Integer(Token a1, int a2) {
    return $equals_Integer_Token(a2, a1);
}
/**/

/*** equals_Token_Token() ***/
static boolean equals_Token_Token(Token a1, Token a2) {
    boolean result = false;
    if (a1.payload instanceof Number && a2.payload instanceof Number) {
        result = (((Number)(a1.payload)).doubleValue() == ((Number)(a2.payload)).doubleValue());
    } else if (a1.payload instanceof Array && a2.payload instanceof Array) {
        result = $Array_equals(a1, a2);
#ifdef PTCG_TYPE_Matrix
    } else if (a1.payload instanceof Matrix && a2.payload instanceof Matrix) {
        result = $Matrix_equals(a1, a2);
#endif
#ifdef PTCG_TYPE_Complex
    } else if (a1.payload instanceof Complex && a2.payload instanceof Complex) {
        result = ((Boolean)$Complex_equals(a1, a2).payload).booleanValue();
#endif
    } else {
        throw new InternalError("equals_Token_Token_(): equals with an unsupported type. " + a1.type + " " + a2.type);
    }
    return result;
}
/**/

/*** isCloseTo_Token_Token() ***/
static boolean isCloseTo_Token_Token(Token thisToken, Token otherToken, Token tolerance) {
    boolean result = false;
    if (thisToken.payload instanceof Number
            && thisToken.payload instanceof Number
            && tolerance.payload instanceof Number) {
        result =
            Math.abs((((Number)(thisToken.payload)).doubleValue() - ((Number)(otherToken.payload)).doubleValue())) < ((Number)(tolerance.payload)).doubleValue();
    } else if (thisToken.type == TYPE_Array
               && otherToken.type == TYPE_Array) {
        return ((Boolean)(Array_isCloseTo(thisToken, otherToken, tolerance).payload)).booleanValue();
#ifdef PTCG_TYPE_Complex
    } else if (thisToken.type == TYPE_Complex
               && otherToken.type == TYPE_Complex) {
        return ((Boolean)(Complex_isCloseTo(thisToken, otherToken, tolerance).payload)).booleanValue();
#endif
#ifdef PTCG_TYPE_Matrix
    } else if (thisToken.type == TYPE_Matrix
               && otherToken.type == TYPE_Matrix) {
        return ((Boolean)(Matrix_isCloseTo(thisToken, otherToken, tolerance).payload)).booleanValue();
#endif
    } else {

        throw new InternalError("equals_Token_Token_(): iscloseTo with an unsupported type. " + thisToken.type + " " + otherToken.type + " " + tolerance.type);
    }
    return result;
}
/**/

