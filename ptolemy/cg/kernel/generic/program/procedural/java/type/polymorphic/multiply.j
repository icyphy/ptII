/*** multiply_Array_Array() ***/
static Token multiply_Array_Array(Token a1, Token a2) {
    return $Array_multiply(a1, a2);
}
/**/

/*** multiply_Array_Double() ***/
static Token multiply_Array_Double(Token a1, double a2) {
    return $multiply_Double_Array(a2, a1);
}
/**/

/*** multiply_Array_Integer() ***/
static Token multiply_Integer_Array(Token a1, int a2) {
    return $multiply_Array_Integer(a2, a1);
}
/**/

/*** multiply_Array_Long() ***/
static Token multiply_Long_Array(Token a1, long a2) {
    return $multiply_Array_Long(a2, a1);
}
/**/

/*** multiply_Boolean_Boolean() ***/
static boolean multiply_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 & a2;
}
/**/

/*** multiply_Complex_Complex() ***/
static Token multiply_Complex_Complex(Token a1, Token a2) {
       return $Complex_multiply(a1, a2);
}
/**/

/*** multiply_Double_Array() ***/
static Token multiply_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
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

/*** multiply_Double_Integer() ***/
double multiply_Double_Integer(double a1, int a2) {
    return a1 * a2;
}
/**/

/*** multiply_Double_Token() ***/
static Token multiply_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $multiply_Token_Token(token, a2);
}
/**/

/*** multiply_Integer_Array() ***/
static Token multiply_Integer_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)a2.payload).size, 0));

    for (i = 0; i < ((Array)a2.payload).size; i++) {
        Array_set(result, i, $multiply_Integer_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Integer_Double() ***/
double multiply_Integer_Double(int a1, double a2) {
    return a1 * a2;
}
/**/


/*** multiply_Integer_Integer() ***/
int multiply_Integer_Integer(int a1, int a2) {
    return a1 * a2;
}
/**/

/*** multiply_Integer_Token() ***/
int multiply_Integer_Token(int a1, Token a2) {
    Token token = $new(Int, a1);
    return $typeFunc(TYPE_Int::multiply(token, a2));
}
/**/

/*** multiply_Long_Array() ***/
static Token multiply_Long_Array(long a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(otherToken.payload)).size; i++) {
        Array_set(result, i, $multiply_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** multiply_Long_Long() ***/
long multiply_Long_Long(long a1, long a2) {
    return a1 * a2;
}
/**/

/*** multiply_Long_Token() ***/
static Token multiply_Long_Token(long a1, Token a2) {
    Token token = $new(Long(a1));
    return $multiply_Token_Token(token, a2);
}
/**/

/*** multiply_Matrix_Matrix() ***/
static Token multiply_Matrix_Matrix(Token a1, Token a2) {
    return $Matrix_multiply(a1, a2);
}
/**/

/*** multiply_Token_Double() ***/
static Token multiply_Token_Double(Token a1, double a2) {
    return $multiply_Double_Token(a2, a1);
}
/**/

/*** multiply_Token_Integer() ***/
int multiply_Token_Integer(Token a1, int a2) {
    return $multiply_Integer_Token(a2, a1);
}
/**/

/*** multiply_Token_Token() ***/
static Token multiply_Token_Token(Token a1, Token a2) {
    Token result = null;
    switch (a1.type) {
#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        switch (a2.type) {
            case TYPE_Double:
                    result = Double_new((Double)a1.payload * (Double)a2.payload);
                break;
#endif
// FIXME: this is wrong because if Double is not defined, but Integer is, we are hosed.
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = Double_new((Double)a1.payload * (Integer)a2.payload);
                break;
#endif
#ifdef PTCG_TYPE_Double
            default:
                System.out.println("multiply_Token_Token(): a1 is a Double, "
                        + "a2 is a " + a2.type);
                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        switch (a2.type) {
            case TYPE_Integer:
                    result = Integer_new((Integer)a1.payload * (Integer)a2.payload);
                break;
            default:
                System.out.println("multiply_Token_Token(): a1 is a Integer, "
                        + "a2 is a " + a2.type);

                result = null;

        }
        break;
#endif
    case TYPE_Array:
        switch (a2.type) {
            case TYPE_Array:
                    result = $Array_multiply(a1, a2);
                System.out.println("multiply_Token_Token: " + a1.type + " " + a2.type + " " + result);
                break;
            default:
                result = null;

        }
        break;
    default:
        System.out.println("multiply_Token_Token(): a1 is a " + a1.type
                        + "a2 is a " + a2.type);

        result = null;
    }

    if (result == null) {
        throw new InternalError("multiply_Token_Token_(): multiply with an unsupported type. "
            + a1.type + " or " + a2.type);

    }
    return result;
}
/**/

