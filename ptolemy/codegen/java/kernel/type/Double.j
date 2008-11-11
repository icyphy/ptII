/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/

/***Double_new***/
// make a new integer token from the given value.
Token Double_new(double d) {
    Token result = new Token();
    result.type = TYPE_Double;
    result.payload = Double.valueOf(d);
    return result;
}
/**/

/***Double_delete***/
/* Instead of Double_delete(), we call scalarDelete(). */
/**/

/***Double_equals***/
Token Double_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];

    if (otherToken.type != TYPE_Double) {
        otherToken = Double_convert(otherToken);
    }
    // Give tolerance for testing.
    return Boolean_new(1.0E-6 > (Double)(thisToken.payload) - (Double)(otherToken.payload));
}
/**/

/***Double_isCloseTo***/
$include(<math.h>)
Token Double_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken;
    Token tolerance;
    otherToken = tokens[0];
    tolerance = tokens[0];

    return Boolean_new(Math.abs((Double)thisToken.payload - (Double)otherToken.payload) < (Double)tolerance.payload);
}
/**/

/***Double_print***/
Token Double_print(Token thisToken, ...) {
    System.out.printf("%g", thisToken.payload.Double);
}
/**/

/***Double_toString***/
Token Double_toString(Token thisToken, ...) {
    return String_new(DoubletoString(thisToken.payload.Double));
}
/**/

/***Double_add***/
Token add_Double_Array(double a1, Token a2);
Token Double_add(Token thisToken, ...) {
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    
    switch (otherToken.type) {
    case TYPE_Double:
    	result = Double_new(thisToken.payload.Double + otherToken.payload.Double);
    	break;
    	
//#ifdef TYPE_Array
    case TYPE_Array:
        result = $add_Double_Array(thisToken.payload.Double, otherToken);
        break;
//#endif
    
	// FIXME: not finished
    default:
        System.err.printf("Double_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        System.exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***Double_subtract***/
Token subtract_Double_Array(double a1, Token a2);
Token Double_subtract(Token thisToken, ...) {
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    
    switch (otherToken.type) {
    case TYPE_Double:
    	result = Double_new(thisToken.payload.Double - otherToken.payload.Double);
    	break;
    	
//#ifdef TYPE_Array
    case TYPE_Array:
        result = $subtract_Double_Array(thisToken.payload.Double, otherToken);
        break;
//#endif
    
	// FIXME: not finished
    default:
        System.err.printf("Double_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        System.exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***Double_multiply***/
Token Double_multiply(Token thisToken, Token... tokens) {
    Token result = new Token();
    Token otherToken;

    otherToken = tokens[0];

    switch (otherToken.type) {
    case TYPE_Double:
        result = Double_new((Double)(thisToken.payload) * (Double)(otherToken.payload));
        break;
//#ifdef TYPE_Integer
    case TYPE_Integer:
        result = Double_new((Double)(thisToken.payload) * (Integer)otherToken.payload);
        break;
//#endif

//#ifdef TYPE_Array
    case TYPE_Array:
        result = $multiply_Double_Array((Double)thisToken.payload, otherToken);
        break;
//#endif

        // FIXME: not finished
    default:
        System.err.printf( "Double_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        System.exit(1);
    }

    return result;
}
/**/

/***Double_divide***/
Token Double_divide(Token thisToken, ...) {
    va_list argp;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Double_new(thisToken.payload.Double / otherToken.payload.Double);
}
/**/

/***Double_negate***/
Token Double_negate(Token thisToken, ...) {
    thisToken.payload.Double = -thisToken.payload.Double;
    return thisToken;
}
/**/

/***Double_zero***/
Token Double_zero(Token token, ...) {
    return Double_new(0.0);
}
/**/

/***Double_one***/
Token Double_one(Token token, ...) {
    return Double_new(1.0);
}
/**/


/***Double_clone***/
Token Double_clone(Token thisToken, ...) {
    return thisToken;
}
/**/




--------------------- static functions --------------------------
/***Double_convert***/
Token Double_convert(Token token, Token... elements) {
    switch (token.type) {
#ifdef TYPE_String
    case TYPE_String:
        // FIXME: Is this safe?
        token.type = TYPE_Double;
	token.payload = (Double)(token.payload).toString();
	return token;
        break;
#endif
#ifdef TYPE_Integer
    case TYPE_Integer:
        token.type = TYPE_Double;
        token.payload = IntegertoDouble((Integer)(token.payload));
	return token;
        break;
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Double_convert(): Conversion from an unsupported type.: " + token.type);
    }
}
/**/

