/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/

/***Double_new***/
// make a new integer token from the given value.
static Token Double_new(double d) {
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
static Token Double_equals(Token thisToken, Token... tokens) {
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
static Token Double_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken;
    Token tolerance;
    otherToken = tokens[0];
    tolerance = tokens[1];

    return Boolean_new(Math.abs((Double)thisToken.payload - (Double)otherToken.payload) < (Double)tolerance.payload);
}
/**/

/***Double_print***/
static Token Double_print(Token thisToken, Token... tokens) {
    System.out.println((Double)thisToken.payload);
    return null;
}
/**/

/***Double_toString***/
static Token Double_toString(Token thisToken, Token... tokens) {
    return String_new(((Double)thisToken.payload).toString());
}
/**/

/***Double_add***/
static Token Double_add(Token thisToken, Token... tokens) {
    Token result;
    Token otherToken = tokens[0];

    switch (otherToken.type) {
    case TYPE_Double:
            result = Double_new((Double)thisToken.payload + (Double)otherToken.payload);
            break;

#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        result = $add_Double_Array((Double)thisToken.payload, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Double_multiply(): Multiply with an unsupported type.: " + otherToken.type);
    }

    return result;
}
/**/

/***Double_subtract***/
static Token Double_subtract(Token thisToken, Token... tokens) {
    Token result;
    Token otherToken = tokens[0];

    switch (otherToken.type) {
    case TYPE_Double:
            result = Double_new((Double)thisToken.payload - (Double)otherToken.payload);
            break;

#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        result = $subtract_Double_Array((Double)thisToken.payload, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Double_subtract(): Multiply with an unsupported type.: " + otherToken.type);
    }
    return result;
}
/**/

/***Double_multiply***/
static Token Double_multiply(Token thisToken, Token... tokens) {
    Token result;
    Token otherToken = tokens[0];

    switch (otherToken.type) {
    case TYPE_Double:
        result = Double_new((Double)(thisToken.payload) * (Double)(otherToken.payload));
        break;
#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        result = Double_new((Double)(thisToken.payload) * (Integer)otherToken.payload);
        break;
#endif

#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        result = $multiply_Double_Array((Double)thisToken.payload, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Double_multiply(): Multiply with an unsupported type.: " + otherToken.type);
    }

    return result;
}
/**/

/***Double_divide***/
static Token Double_divide(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Double_new((Double)thisToken.payload / (Double)otherToken.payload);
}
/**/

/***Double_negate***/
static Token Double_negate(Token thisToken, Token... tokens) {
    return Double_new(-(Double)(thisToken.payload));
}
/**/

/***Double_zero***/
static Token Double_zero(Token token, Token... tokens) {
    return Double_new(0.0);
}
/**/

/***Double_one***/
static Token Double_one(Token token, Token... tokens) {
    return Double_new(1.0);
}
/**/


/***Double_clone***/
static Token Double_clone(Token thisToken, Token... tokens) {
    return thisToken;
}
/**/




--------------------- static functions --------------------------
/***Double_convert***/
static Token Double_convert(Token token, Token... elements) {
    switch (token.type) {
#ifdef PTCG_TYPE_String
    case TYPE_String:
        // FIXME: Is this safe?
        token.type = TYPE_Double;
        token.payload = ((Double)(token.payload)).toString();
        return token;
#endif
#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        token.type = TYPE_Double;
        token.payload = IntegertoDouble((Integer)(token.payload));
        return token;
#endif

    case TYPE_Double:
        return token;
        // FIXME: not finished
    default:
        throw new RuntimeException("Double_convert(): Conversion from an unsupported type.: " + token.type);
    }
}
/**/

