/***declareBlock***/
// ptolemy/cg/kernel/generic/program/procedural/java/type/Complex.j
public class Complex {
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
        return Complex_new(((Complex)thisToken.payload).real,
	        ((Complex)thisToken.payload).imag);
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
    result.payload = new Complex();
    result.type = TYPE_Complex;
    ((Complex)result.payload).real = real;
    ((Complex)result.payload).imag = imag;
    return result;
}
/**/

/***Complex_equals***/
static Token Complex_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];
    return Boolean_new(((Complex)thisToken.payload).real == ((Complex)otherToken.payload).real
    && ((Complex)thisToken.payload).imag == ((Complex)otherToken.payload).imag);
}
/**/

/***Complex_isCloseTo***/
static Token Complex_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken;
    Token tolerance;
    otherToken = tokens[0];
    tolerance = tokens[1];
    return Boolean_new(
        Math.abs(((Complex)thisToken.payload).real - ((Complex)otherToken.payload).real) < (Double)tolerance.payload
	&&
        Math.abs(((Complex)thisToken.payload).imag - ((Complex)otherToken.payload).imag) < (Double)tolerance.payload);
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

/***Complex_toString***/
static Token Complex_toString(Token thisToken, Token... tokens) {
    if (((Complex)thisToken.payload).imag >= 0) {
        return String_new(Double.toString(((Complex)thisToken.payload).real)
	       + " + " + Double.toString(((Complex)thisToken.payload).imag) + "i");
    } else {
        return String_new(Double.toString(((Complex)thisToken.payload).real)
	       + " - " + Double.toString(-((Complex)thisToken.payload).imag) + "i");

    }
}
/**/

/***Complex_add***/
static Token Complex_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Complex_new(((Complex)thisToken.payload).real + ((Complex)otherToken.payload).real,
        ((Complex)thisToken.payload).imag + ((Complex)otherToken.payload).imag);
}
/**/

/***Complex_subtract***/
static Token Complex_subtract(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Complex_new(((Complex)thisToken.payload).real - ((Complex)otherToken.payload).real,
        ((Complex)thisToken.payload).imag - ((Complex)otherToken.payload).imag);

}
/**/

/***Complex_multiply***/
static Token Complex_multiply(Token thisToken, Token... tokens) {
    Token result;
    Token otherToken = tokens[0];

    switch (otherToken.type) {
#ifdef PTCG_TYPE_Complex
    case TYPE_Complex:
        result =  Complex_new(
	       (((Complex)otherToken.payload).real * ((Complex)thisToken.payload).real)
	        - (((Complex)otherToken.payload).real * ((Complex)thisToken.payload).real),
	       (((Complex)otherToken.payload).real * ((Complex)thisToken.payload).imag)
	        + (((Complex)otherToken.payload).imag * ((Complex)thisToken.payload).real));
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
    Token otherToken = tokens[0];
    return Complex_new(((Complex)thisToken.payload) / ((Complex)otherToken.payload));
}
/**/

/***Complex_negate***/
static Token Complex_negate(Token thisToken, Token... tokens) {
    double r = 0.0;
    double i = 0.0;
    if (((Complex)thisToken.payload).real != 0.0) {
        r = -((Complex)thisToken.payload).real;
    }
    if (((Complex)thisToken.payload).imag != 0.0) {
        i = -((Complex)thisToken.payload).imag;
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
    switch (token.type) {

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        ((Complex)token.payload).real = (Double)token.payload;
        break;
#endif

        // FIXME: not finished
    default:
        System.err.println("Complex_convert(): Conversion from an unsupported type. ("
		+ token.type + ")");
        System.exit(1);
        break;
    }
    token.type = TYPE_Complex;
    return token;
}
/**/

