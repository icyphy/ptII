/***constantsBlock***/
// ConstantsBlock from SharedCode.j
/**/

/***funcHeaderBlock ($function)***/
// Token $function (Token thisToken, Token... tokens);  funcHeaderBlock from SharedCode.j
/**/

/***tokenDeclareBlock***/

public class Token {
    public Short type;
    public Object payload;
        public Token() {};
        public Short getType() {
                return type;
        }
        public Object getPayload() {
                return payload;
        }
}
/**/


/***convertPrimitivesBlock***/
static Integer StringtoInteger(String string) {
     return Integer.valueOf(string);
}

static Long StringtoLong(String string) {
     return Long.valueOf(string);
}

// static String ComplextoString(Token thisToken) {
//     if (((ComplexCG)thisToken.payload).imag >= 0) {
//         return Double.toString(((ComplexCG)thisToken.payload).real)
//                + " + " + Double.toString(((ComplexCG)thisToken.payload).imag) + "i";
//     } else {
//         return Double.toString(((ComplexCG)thisToken.payload).real)
//                + " - " + Double.toString(-((ComplexCG)thisToken.payload).imag) + "i";
//     }
// }

//static String ComplextoString(Token complex) {
//       return (String)(Complex_toString(complex).payload);
//}

static Integer DoubletoInteger(Double d) {
       return Integer.valueOf((int)Math.floor(d.doubleValue()));
}

static Double IntegertoDouble(Integer i) {
       return Double.valueOf(i.doubleValue());
}

static Long IntegertoLong(int i) {
     return Long.valueOf(i);
}


static String IntegertoString(int i) {
    return Integer.toString(i);
}

static String LongtoString(long l) {
    return Long.toString(l);
}

static String DoubletoString(double d) {
    return Double.toString(d);
}

static String BooleantoString(boolean b) {
    return Boolean.toString(b);
}

static String UnsignedBytetoString(byte b) {
    return Byte.toString(b);
}

/**/

/*** unsupportedTypeFunction ***/
/* We share one method between all types so as to reduce code size. */
static Token unsupportedTypeFunction(Token token, Token... tokens) {
    System.err.println("Attempted to call unsupported method on a type");
    System.exit(1);
    return emptyToken;
}
/**/

/*** scalarDeleteFunction ***/
/* We share one method between all scalar types so as to reduce code size. */
static Token scalarDelete(Token token, Token... tokens) {
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken;
}
/**/
