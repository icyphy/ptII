/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/


/***Integer_new***/
// make a new integer token from the given value.
Token Integer_new(int i) {
    Token result = new Token();
    result.type = TYPE_Integer;
    result.payload = Integer.valueOf(i);
    return result;
}
/**/

/***Integer_equals***/
Token Integer_equals(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new((Integer)(thisToken.payload) == (Integer)(otherToken.payload);
}
/**/

/***Integer_isCloseTo***/
Token Integer_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken;
    Token tolerance;
    otherToken = tokens[0];
    tolerance = tokens[1];

    return Boolean_new(Math.abs((Integer)thisToken.payload - (Integer)otherToken.payload) < (Double)tolerance.payload);
}
/**/

/***Integer_delete***/
/* Instead of Integer_delete(), we call scalarDelete(). */
/**/

/***Integer_print***/
Token Integer_print(Token thisToken, ...) {
    printf("%d", thisToken.payload.Int);
}
/**/

/***Integer_toString***/
Token Integer_toString(Token thisToken, ...) {
    return String_new(InttoString(thisToken.payload.Int));
}
/**/

/***Integer_add***/
Token Integer_add(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Integer_new(thisToken.payload.Int + otherToken.payload.Int);
}
/**/

/***Integer_subtract***/
Token Integer_subtract(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Integer_new(thisToken.payload.Int - otherToken.payload.Int);
}
/**/

/***Integer_multiply***/
Token Integer_multiply(Token thisToken, Token... tokens) {
    Token result = new Token();
    Token otherToken;

    otherToken = tokens[0];

    switch (otherToken.type) {
    case TYPE_Integer:
        result = Integer_new((Integer)thisToken.payload * (Integer)otherToken.payload);
        break;

//#ifdef TYPE_Double
    case TYPE_Double:
        result = Double_new((Integer)thisToken.payload * (Double)otherToken.payload);
        break;
//#endif

        // FIXME: not finished
    default:
        System.err.printf( "Integer_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        System.exit(1);
    }

    return result;
}
/**/

/***Integer_divide***/
Token Integer_divide(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Integer_new(thisToken.payload.Int / otherToken.payload.Int);
}
/**/

/***Integer_negate***/
Token Integer_negate(Token thisToken, ...) {
    thisToken.payload.Int = -thisToken.payload.Int;
    return thisToken;
}
/**/

/***Integer_zero***/
Token Integer_zero(Token token, ...) {
    return Integer_new(0);
}
/**/

/***Integer_one***/
Token Integer_one(Token token, ...) {
    return Integer_new(1);
}
/**/

/***Integer_clone***/
Token Integer_clone(Token thisToken, ...) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***Integer_convert***/
Token Integer_convert(Token token, Token... elements) {
    switch (token.type) {

//#ifdef TYPE_Double
    case TYPE_Double:
        token.payload = DoubletoInteger((Double)token.payload);
        break;
//#endif

        // FIXME: not finished
    default:
        System.err.printf("Integer_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.type = TYPE_Integer;
    return token;
}
/**/

