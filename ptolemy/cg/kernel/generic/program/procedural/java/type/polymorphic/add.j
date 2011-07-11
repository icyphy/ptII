/*** add_Array_Array() ***/
static Token add_Array_Array(Token a1, Token a2) {
    return $Array_add(a1, a2);
}
/**/

/*** add_Array_Double() ***/
static Token add_Array_Double(Token a1, double a2) {
    return $add_Double_Array(a2, a1);
}
/**/

/*** add_Array_Integer() ***/
static Token add_Array_Integer(Token a1, int a2) {
    return $add_Integer_Array(a2, a1);
}
/**/

/*** add_Array_Long() ***/
static Token add_Long_Array(Token a1, long long a2) {
    return $add_Array_Long(a2, a1);
}
/**/

/*** add_Boolean_Boolean() ***/
static boolean add_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 | a2;
}
/**/

/*** add_Boolean_Integer() ***/
int add_Boolean_Integer(boolean a1, int a2) {
    return $add_Integer_Boolean(a2, a1);
}
/**/

/*** add_Boolean_String() ***/
String add_Boolean_String(boolean a1, String a2) {
    return $add_String_Boolean(a2, a1);
}
/**/

/*** add_Complex_Complex() ***/
static Token add_Complex_Complex(Token a1, Token a2) {
    return Complex_new(((ComplexCG)a1.payload).real + ((ComplexCG)a2.payload).real,
        ((ComplexCG)a1.payload).imag + ((ComplexCG)a2.payload).imag);
}
/**/

/*** add_Double_Array() ***/
static Token add_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(((Array)(a2.payload)).size, 0));

    for (i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $add_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Double_Double() ***/
double add_Double_Double(double a1, double a2) {
    return a1 + a2;
}
/**/

/*** add_Double_Integer() ***/
double add_Double_Integer(double a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_Double_String() ***/
double add_Double_String(double a1, String a2) {
    return a1 + Double.valueOf(a2);
}
/**/

/*** add_Double_Token() ***/
static Token add_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_Integer_Array() ***/
static Token add_Integer_Array(int a1, Token a2) {
    Token result = $new(Array(((Array)(a2.payload)).size, 0));
    for (int i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $add_Integer_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Integer_Boolean() ***/
int add_Integer_Boolean(int a1, boolean a2) {
    return a1 + (a2 ? 1 : 0);
}
/**/

/*** add_Integer_Integer() ***/
int add_Integer_Integer(int a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_Integer_String() ***/
int add_Integer_String(int a1, String a2) {
    return a1 + Integer.valueOf(a2);
}
/**/

/*** add_Integer_Token() ***/
static Token add_Integer_Token(int a1, Token a2) {
    Token token = $new(Integer(a1));
    //   return $typeFunc(TYPE_Integer::add(token, a2));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_Long_Array() ***/
static Token add_Long_Array(long long a1, Token a2) {
    Token result = $new(Array(((Array)(a2.payload)).size, 0));
    for (int i = 0; i < ((Array)(a2.payload)).size; i++) {
        Array_set(result, i, $add_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Long_Long() ***/
long long add_Long_Long(long long a1, long long a2) {
    return a1 + a2;
}
/**/

/*** add_Long_Token() ***/
static Token add_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_String_Boolean() ***/
String add_String_Boolean(String a1, boolean a2) {
    return a1 + a2;
}
/**/

/*** add_String_Double() ***/
String add_String_Double(String a1, double a2) {
    return a1 + a2;
}
/**/

/*** add_String_Integer() ***/
String add_String_Integer(String a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_String_String() ***/
String add_String_String(String a1, String a2) {
    return a1 + a2;
}
/**/

/*** add_Token_Double() ***/
static Token add_Token_Double(Token a1, double a2) {
    return $add_Double_Token(a2, a1);
}
/**/

/*** add_Token_Integer() ***/
static Token add_Token_Integer(Token a1, int a2) {
    return $add_Integer_Token(a2, a1);
}
/**/

/*** add_Token_Token() ***/
static Token add_Token_Token(Token a1, Token a2) {
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
                    result = Double_new((Double)a1.payload + (Double)a2.payload);
                break;
#ifdef PTCG_TYPE_Array
            case TYPE_Array:
                    result = $add_Double_Array((Double)a1.payload, a2);
                break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = Double_new((Double)a1.payload + (Integer)a2.payload);
                break;
#endif
            default:
                System.out.println("add_Token_Token(): a1 is a Double, "
                        + "a2 is a " + a2.type);
                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        switch (a2.type) {
            case TYPE_Integer:
                    result = Integer_new((Integer)a1.payload + (Integer)a2.payload);
                break;
#ifdef PTCG_TYPE_Array
            case TYPE_Array:
                    result = $add_Integer_Array((Integer)a1.payload, a2);
                break;
#endif
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    result = Double_new((Integer)a1.payload + (Double)a2.payload);
                break;
#endif
            default:
                System.out.println("add_Token_Token(): a1 is a Integer, "
                        + "a2 is a " + a2.type);

                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        switch (a2.type) {
            case TYPE_Array:
                    result = $Array_add(a1, a2);
                break;
#ifdef PTCG_TYPE_Double
            case TYPE_Double:
                    result = $add_Array_Double(a1, (Double)a2.payload);
                break;
#endif
#ifdef PTCG_TYPE_Integer
            case TYPE_Integer:
                    result = $add_Array_Integer(a1, (Integer)a2.payload);
                break;
#endif
            default:
                result = null;

        }
        break;
#endif
#ifdef PTCG_TYPE_Matrix
    case TYPE_Matrix:
        switch (a2.type) {
            case TYPE_Matrix:
                    result = $Matrix_add(a1, a2);
                break;
            default:
                result = null;

        }
        break;
#endif
    default:
        System.out.println("add_Token_Token(): a1 is a " + a1.type
                        + "a2 is a " + a2.type);

        result = null;
    }

    if (result == null) {
        throw new InternalError("add_Token_Token_(): Add with an unsupported type. "
            + a1.type + " or " + a2.type);

    }
   return result;
}

static void print_Token2(Token token) {
    if (token == null) {
        System.out.println("Token is null");
        return;
    }

    switch (token.type) {
#ifdef PTCG_TYPE_Integer
        case TYPE_Integer:
            System.out.println((Integer) token.payload);
            break;
#endif
        case TYPE_Array:
            $Array_print(token);
            break;
        default:
            System.out.println(token);
            break;
    }
}
/**/


/*** add_UnsignedByte_Double() ***/
int add_UnsignedByte_Integer(int a1, double a2) {
    return a1 + a2;
}
/**/

/*** add_UnsignedByte_Integer() ***/
int add_UnsignedByte_Integer(int a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_UnsignedByte_String() ***/
int add_UnsignedByte_String(int a1, String a2) {
    // FIXME: Integer.valueOf() is probably not correct.
    return a1 + Integer.valueOf(a2);
}
/**/

/*** add_UnsignedByte_Token() ***/
static Token add_UnsignedByte_Token(int a1, Token a2) {
    Token token = $new(UnsignedByte(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_UnsignedByte_UnsignedByte() ***/
int add_UnsignedByte_UnsignedByte(int a1, int a2) {
    return a1 + a2;
}
/**/


