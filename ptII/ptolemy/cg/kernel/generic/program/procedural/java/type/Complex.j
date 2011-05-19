/***declareBlock***/
// ptolemy/cg/kernel/generic/program/procedural/java/type/Complex.j
// Named ComplexCG because we also have ptolemy.math.Complex
public class ComplexCG {
    public double real;
    public double imag;
};
/**/

/***funcDeclareBlock***/
/**/


/***Complex_new***/
static Token Complex_new() {
    return Complex_new(0.0, 0.0);
}

static Token Complex_new(double real) {
    return Complex_new(real, 0.0);
}

static Token Complex_new(Token thisToken) {
    switch (thisToken.type) {
#ifdef PTCG_TYPE_Complex
    case TYPE_Complex:
        return Complex_new(((ComplexCG)thisToken.payload).real,
	        ((ComplexCG)thisToken.payload).imag);
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        return Complex_new((Double)thisToken.payload, 0.0);
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Complex_new(Token): Conversion from an unsupported type. ("
		+ thisToken.type + ")");
    }
}

static Token Complex_new(double real, double imag) {
    Token result = new Token();
    result.payload = new ComplexCG();
    result.type = TYPE_Complex;
    ((ComplexCG)result.payload).real = real;
    ((ComplexCG)result.payload).imag = imag;
    return result;
}
/**/

/***Complex_equals***/
#ifdef PTCG_TYPE_Complex
static Token Complex_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];
    return Boolean_new(((ComplexCG)thisToken.payload).real == ((ComplexCG)otherToken.payload).real
    && ((ComplexCG)thisToken.payload).imag == ((ComplexCG)otherToken.payload).imag);
}
#endif
/**/

/***Complex_isCloseTo***/
static Token Complex_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken;
    Token tolerance;
    otherToken = tokens[0];
    tolerance = tokens[1];
    return Boolean_new(
        Math.abs(((ComplexCG)thisToken.payload).real - ((ComplexCG)otherToken.payload).real) < (Double)tolerance.payload
	&&
        Math.abs(((ComplexCG)thisToken.payload).imag - ((ComplexCG)otherToken.payload).imag) < (Double)tolerance.payload);
}
/**/

/***Complex_delete***/
/* Instead of Complex_delete(), we call scalarDelete(). */
/**/

/***Complex_print***/
static Token Complex_print(Token thisToken, Token... tokens) {
       return Complex_toString(thisToken);
}
/**/


/***ComplextoString***/
static String ComplextoString(Token thisToken) {
    if (((ComplexCG)thisToken.payload).imag >= 0) {
        return Double.toString(((ComplexCG)thisToken.payload).real)
	       + " + " + Double.toString(((ComplexCG)thisToken.payload).imag) + "i";
    } else {
        return Double.toString(((ComplexCG)thisToken.payload).real)
	       + " - " + Double.toString(-((ComplexCG)thisToken.payload).imag) + "i";
    }
}
/**/

/***Complex_toString***/
static Token Complex_toString(Token thisToken, Token... tokens) {
    if (((ComplexCG)thisToken.payload).imag >= 0) {
        return String_new(Double.toString(((ComplexCG)thisToken.payload).real)
	       + " + " + Double.toString(((ComplexCG)thisToken.payload).imag) + "i");
    } else {
        return String_new(Double.toString(((ComplexCG)thisToken.payload).real)
	       + " - " + Double.toString(-((ComplexCG)thisToken.payload).imag) + "i");

    }
}
/**/

/***Complex_add***/
static Token Complex_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Complex_new(((ComplexCG)thisToken.payload).real + ((ComplexCG)otherToken.payload).real,
        ((ComplexCG)thisToken.payload).imag + ((ComplexCG)otherToken.payload).imag);
}
/**/

/***Complex_subtract***/
static Token Complex_subtract(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Complex_new(((ComplexCG)thisToken.payload).real - ((ComplexCG)otherToken.payload).real,
        ((ComplexCG)thisToken.payload).imag - ((ComplexCG)otherToken.payload).imag);

}
/**/

/***Complex_multiply***/
static Token Complex_multiply(Token thisToken, Token... tokens) {
    Token result = null;
    Token otherToken = tokens[0];

    switch (otherToken.type) {
#ifdef PTCG_TYPE_Complex
    case TYPE_Complex:
        result =  Complex_new(
	       (((ComplexCG)otherToken.payload).real * ((ComplexCG)thisToken.payload).real)
	        - (((ComplexCG)otherToken.payload).real * ((ComplexCG)thisToken.payload).real),
	       (((ComplexCG)otherToken.payload).real * ((ComplexCG)thisToken.payload).imag)
	        + (((ComplexCG)otherToken.payload).imag * ((ComplexCG)thisToken.payload).real));
        break;
        // FIXME: not finished
#endif
    default:
        System.err.println("Complex_multiply(): Multiply with an unsupported type. ("
	  + otherToken.type + ")");
	System.exit(1);
    }

    return result;
}
/**/

/***Complex_divide***/
static Token Complex_divide(Token thisToken, Token... tokens) {
    Token divisor = tokens[0];
    double thisTokenReal = ((ComplexCG)thisToken.payload).real;
    double thisTokenImag = ((ComplexCG)thisToken.payload).imag;
    double divisorReal = ((ComplexCG)divisor.payload).real;
    double divisorImag = ((ComplexCG)divisor.payload).imag;
    double denominator = (divisorReal * divisorReal) + (divisorImag * divisorImag);
    return Complex_new(((thisTokenReal * divisorReal) + (thisTokenImag * divisorImag))
            / denominator,
            ((thisTokenImag * divisorReal) - (thisTokenReal * divisorImag))
            / denominator);
}

/**/

/***Complex_negate***/
static Token Complex_negate(Token thisToken, Token... tokens) {
    double r = 0.0;
    double i = 0.0;
    if (((ComplexCG)thisToken.payload).real != 0.0) {
        r = -((ComplexCG)thisToken.payload).real;
    }
    if (((ComplexCG)thisToken.payload).imag != 0.0) {
        i = -((ComplexCG)thisToken.payload).imag;
    }
    return Complex_new(r, i);
}
/**/

/***Complex_zero***/
static Token Complex_zero(Token token, Token... tokens) {
    return Complex_new(0.0, 0.0);
}
/**/

/***Complex_one***/
static Token Complex_one(Token token, Token... tokens) {
    return Complex_new(1.0, 0.0);
}
/**/

/***Complex_clone***/
static Token Complex_clone(Token thisToken, Token... tokens) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***Complex_convert***/
static Token Complex_convert(Token token, Token... tokens) {
    Token result = $Complex_new();
    switch (token.type) {

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        if (token.payload != null) {
	    ((ComplexCG)result.payload).real = ((Double)token.payload).doubleValue();
        }
        break;
#endif

        // FIXME: not finished
    default:
        System.err.println("Complex_convert(): Conversion from an unsupported type. ("
		+ token.type + ")");
        System.exit(1);
        break;
    }
    return result;
}
/**/

