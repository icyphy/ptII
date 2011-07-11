/*** divide_Array_Array() ***/
static Token divide_Array_Array(Token a1, Token a2) {
    return $Array_divide(a1, a2);
}
/**/

/*** divide_Array_Double() ***/
static Token divide_Array_Double(Token a1, double a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(a1.payload)).size; i++) {
        Array_set(result, i, $divide_Token_Double(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Array_Integer() ***/
static Token divide_Array_Integer(Token a1, int a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(a1.payload)).size; i++) {
        Array_set(result, i, $divide_Token_Integer(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Array_Long() ***/
static Token divide_Array_Long(Token a1, long long a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(a1.payload)).size; i++) {
        Array_set(result, i, $divide_Token_Long(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** divide_Boolean_Boolean() ***/
static boolean divide_Boolean_Boolean(boolean a1, boolean a2) {
    //if (!a2) {
    // FIXME: Illegal boolean divide.
    // throw exception("Illegal boolean division.");
    //}
    return a1;
}
/**/

/*** divide_Complex_Complex() ***/
static Token divide_Complex_Complex(Token a1, Token a2) {
    return $Complex_divide(a1, a2);
}
/**/

/*** divide_Double_Array() ***/
static Token divide_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
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

/*** divide_Double_Integer() ***/
double divide_Double_Integer(double a1, int a2) {
    return a1 / a2;
}
/**/

/*** divide_Double_Token() ***/
static Token divide_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Integer_Array() ***/
static Token divide_Integer_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $divide_Integer_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Integer_Double() ***/
double divide_Integer_Double(int a1, double a2) {
    return (a1 / a2);
}
/**/

/*** divide_Integer_Integer() ***/
int divide_Integer_Integer(int a1, int a2) {
    return a1 / a2;
}
/**/

/*** divide_Integer_Token() ***/
static Token divide_Integer_Token(int a1, Token a2) {
    Token token = $new(Integer(a1));
    //return $typeFunc(TYPE_Int::divide(token, a2));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Long_Array() ***/
static Token divide_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $divide_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** divide_Long_Long() ***/
static Token divide_Long_Long(long long a1, long long a2) {
    return a1 / a2;
}
/**/

/*** divide_Long_Token() ***/
static Token divide_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $divide_Token_Token(token, a2);
}
/**/

/*** divide_Matrix_Double() ***/
static Token divide_Matrix_Double(Token a1, double a2) {
    int i, j;
    Token result = $new(Matrix(((Matrix)(a1.payload)).row,
                    ((Matrix)(a1.payload)).column, 0));

    for (i = 0; i < ((Matrix)(a1.payload)).row; i++) {
        for (j = 0; j < ((Matrix)(a1.payload)).column; j++) {
            Matrix_set(result, i, j,
                    $divide_Token_Double(Matrix_get(a1, i, j), a2));
        }
    }
    return result;
}
/**/

/*** divide_Token_Double() ***/
static Token divide_Token_Double(Token a1, double a2) {
    Token token = $new(Double(a2));
    return $divide_Token_Token(a1, token);
}
/**/

/*** divide_Token_Integer() ***/
static Token divide_Token_Integer(Token a1, int a2) {
    Token token = $new(Integer(a2));
    return $divide_Token_Token(a1, token);
}
/**/

/*** divide_Token_Token() ***/
static Token divide_Token_Token(Token a1, Token a2) {
    Token result = null;
    switch (a1.type) {
#ifdef PTCG_TYPE_Complex
    case TYPE_Complex:
        switch (a2.type) {
            case TYPE_Complex:
                    result = Complex_add(a1, a2);
                break;
            default:
                System.out.println("add_Token_Token(): a1 is a Complex, "
                        + "a2 is a " + a2.type);
                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        switch (a2.type) {
            case TYPE_Double:
                    result = Double_new((Double)a1.payload / (Double)a2.payload);
                break;
#ifdef PTCG_TYPE_Array
            case TYPE_Array:
                    result = $divide_Double_Array((Double)a1.payload, a2);
                break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = Double_new((Double)a1.payload / (Integer)a2.payload);
                break;
#endif
            default:
                System.out.println("divide_Token_Token(): a1 is a Double, "
                        + "a2 is a " + a2.type);
                result = null;

        }
        break;
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        switch (a2.type) {
            case TYPE_Integer:
                    result = Integer_new((Integer)a1.payload / (Integer)a2.payload);
                break;
#ifdef PTCG_TYPE_Array
            case TYPE_Array:
                    result = $divide_Integer_Array((Integer)a1.payload, a2);
                break;
#endif
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    result = Double_new((Integer)a1.payload/(Double)a2.payload);
                break;
#endif
            default:
                System.out.println("divide_Token_Token(): a1 is a Integer, "
                        + "a2 is a " + a2.type);

                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        switch (a2.type) {
            case TYPE_Array:
                    result = $Array_divide(a1, a2);
                break;
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    result = $divide_Array_Double(a1, (Double)a2.payload);
                break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = $divide_Array_Integer(a1, (Integer)a2.payload);
                break;
#endif
            default:
                result = null;

        }
        break;
#endif
    default:
        System.out.println("divide_Token_Token(): a1 is a " + a1.type
                        + "a2 is a " + a2.type);

        result = null;
    }

    if (result == null) {
        throw new InternalError("divide_Token_Token_(): divide with an unsupported type. "
            + a1.type + " or " + a2.type);

    }
    return result;
}
/**/

/***divide_one_Array***/
static Token divide_one_Array(Token a1, Token... tokens) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $Array_divide(oneToken, a1);
}
/**/

/*** divide_one_Boolean ***/
double divide_one_Boolean(boolean b, Token... tokens) {
    // FIXME: is this right?
    return b;
}
/**/

/*** divide_one_Complex ***/
Token divide_one_Complex(Token a1, Token... tokens) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $divide_Complex_Complex(oneToken, a1);
}
/**/

/*** divide_one_Double ***/
double divide_one_Double(double d, Token... tokens) {
    return 1.0/d;
}
/**/

/*** divide_one_Int ***/
int divide_one_Integer(int i, Token... tokens) {
    return 1/i;
}
/**/

/*** divide_one_Long ***/
long divide_one_Long(long l, Token... tokens) {
    return 1L/l;
}
/**/

/*** divide_one_Token ***/
long divide_one_Token(Token a1, Token... tokens) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $divide_Token_Token(a1, token);
}
/**/

