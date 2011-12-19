/*** modulo_Array_Array() ***/
static Token modulo_Array_Array(Token a1, Token a2) {
    return $Array_modulo(a1, a2);
}
/**/

/*** modulo_Array_Double() ***/
static Token modulo_Array_Double(Token a1, double a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(a1.payload)).size; i++) {
        Array_set(result, i, $modulo_Token_Double(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** modulo_Array_Integer() ***/
static Token modulo_Array_Integer(Token a1, int a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(a1.payload)).size; i++) {
        Array_set(result, i, $modulo_Token_Integer(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** modulo_Array_Long() ***/
static Token modulo_Array_Long(Token a1, long long a2) {
    int i;
    Token result = $new(Array(((Array)(a1.payload)).size, 0));

    for (i = 0; i < ((Array)(a1.payload)).size; i++) {
        Array_set(result, i, $modulo_Token_Long(Array_get(a1, i), a2));
    }
    return result;
}
/**/

/*** modulo_Boolean_Boolean() ***/
static boolean modulo_Boolean_Boolean(boolean a1, boolean a2) {
    //if (!a2) {
    // FIXME: Illegal boolean modulo.
    // throw exception("Illegal boolean division.");
    //}
    return a1;
}
/**/

/*** modulo_Complex_Complex() ***/
static Token modulo_Complex_Complex(Token a1, Token a2) {
    return $Complex_modulo(a1, a2);
}
/**/

/*** modulo_Double_Array() ***/
static Token modulo_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
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

/*** modulo_Double_Integer() ***/
double modulo_Double_Integer(double a1, int a2) {
    return a1 % a2;
}
/**/

/*** modulo_Double_Token() ***/
static Token modulo_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $modulo_Token_Token(token, a2);
}
/**/

/*** modulo_Integer_Array() ***/
static Token modulo_Integer_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $modulo_Integer_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** modulo_Integer_Double() ***/
double modulo_Integer_Double(int a1, double a2) {
    return (a1 % a2);
}
/**/

/*** modulo_Integer_Integer() ***/
int modulo_Integer_Integer(int a1, int a2) {
    return a1 % a2;
}
/**/

/*** modulo_Integer_Token() ***/
static Token modulo_Integer_Token(int a1, Token a2) {
    Token token = $new(Integer(a1));
    //return $typeFunc(TYPE_Int::modulo(token, a2));
    return $modulo_Token_Token(token, a2);
}
/**/

/*** modulo_Long_Array() ***/
static Token modulo_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $modulo_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** modulo_Long_Long() ***/
static Token modulo_Long_Long(long long a1, long long a2) {
    return a1 % a2;
}
/**/

/*** modulo_Long_Token() ***/
static Token modulo_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $modulo_Token_Token(token, a2);
}
/**/

/*** modulo_Matrix_Double() ***/
static Token modulo_Matrix_Double(Token a1, double a2) {
    int i, j;
    Token result = $new(Matrix(((Matrix)(a1.payload)).row,
                    ((Matrix)(a1.payload)).column, 0));

    for (i = 0; i < ((Matrix)(a1.payload)).row; i++) {
        for (j = 0; j < ((Matrix)(a1.payload)).column; j++) {
            Matrix_set(result, i, j,
                    $modulo_Token_Double(Matrix_get(a1, i, j), a2));
        }
    }
    return result;
}
/**/

/*** modulo_Token_Double() ***/
static Token modulo_Token_Double(Token a1, double a2) {
    Token token = $new(Double(a2));
    return $modulo_Token_Token(a1, token);
}
/**/

/*** modulo_Token_Integer() ***/
static Token modulo_Token_Integer(Token a1, int a2) {
    Token token = $new(Integer(a2));
    return $modulo_Token_Token(a1, token);
}
/**/

/*** modulo_Token_Token() ***/
static Token modulo_Token_Token(Token a1, Token a2) {
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
                    result = Double_new((Double)a1.payload % (Double)a2.payload);
                break;
#ifdef PTCG_TYPE_Array
            case TYPE_Array:
                    result = $modulo_Double_Array((Double)a1.payload, a2);
                break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = Double_new((Double)a1.payload % (Integer)a2.payload);
                break;
#endif
            default:
                System.out.println("modulo_Token_Token(): a1 is a Double, "
                        + "a2 is a " + a2.type);
                result = null;

        }
        break;
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        switch (a2.type) {
            case TYPE_Integer:
                    result = Integer_new((Integer)a1.payload % (Integer)a2.payload);
                break;
#ifdef PTCG_TYPE_Array
            case TYPE_Array:
                    result = $modulo_Integer_Array((Integer)a1.payload, a2);
                break;
#endif
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    result = Double_new((Integer)a1.payload % (Double)a2.payload);
                break;
#endif
            default:
                System.out.println("modulo_Token_Token(): a1 is a Integer, "
                        + "a2 is a " + a2.type);

                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        switch (a2.type) {
            case TYPE_Array:
                    result = $Array_modulo(a1, a2);
                break;
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    result = $modulo_Array_Double(a1, (Double)a2.payload);
                break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = $modulo_Array_Integer(a1, (Integer)a2.payload);
                break;
#endif
            default:
                result = null;

        }
        break;
#endif
    default:
        System.out.println("modulo_Token_Token(): a1 is a " + a1.type
                        + "a2 is a " + a2.type);

        result = null;
    }

    if (result == null) {
        throw new InternalError("modulo_Token_Token_(): modulo with an unsupported type. "
            + a1.type + " or " + a2.type);

    }
    return result;
}
/**/

/***modulo_one_Array***/
static Token modulo_one_Array(Token a1, Token... tokens) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $Array_modulo(oneToken, a1);
}
/**/

/*** modulo_one_Boolean ***/
double modulo_one_Boolean(boolean b, Token... tokens) {
    // FIXME: is this right?
    return b;
}
/**/

/*** modulo_one_Complex ***/
Token modulo_one_Complex(Token a1, Token... tokens) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $modulo_Complex_Complex(oneToken, a1);
}
/**/

/*** modulo_one_Double ***/
double modulo_one_Double(double d, Token... tokens) {
    return 1.0 % d;
}
/**/

/*** modulo_one_Int ***/
int modulo_one_Integer(int i, Token... tokens) {
    return 1 % i;
}
/**/

/*** modulo_one_Long ***/
long modulo_one_Long(long l, Token... tokens) {
    return 1L % l;
}
/**/

/*** modulo_one_Token ***/
long modulo_one_Token(Token a1, Token... tokens) {
    Token oneToken = $tokenFunc(a1::one(a1));
    return $modulo_Token_Token(a1, token);
}
/**/

