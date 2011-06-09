/***declareBlock***/
typedef unsigned char UnsignedByteToken;
/**/

/***funcDeclareBlock***/
Token UnsignedByte_new(int i);
/**/


/***UnsignedByte_new***/
// make a new unsigned byte token from the given value.
Token UnsignedByte_new(int b) {
    Token result;
    result.type = TYPE_UnsignedByte;
    result.payload.UnsignedByte = b;
    return result;
}
/**/

/***UnsignedByte_equals***/
Token UnsignedByte_equals(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(thisToken.payload.UnsignedByte == otherToken.payload.UnsignedByte);
}
/**/

/***UnsignedByte_isCloseTo***/
Token UnsignedByte_isCloseTo(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    Token tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    tolerance = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(fabs(thisToken.payload.UnsignedByte - otherToken.payload.UnsignedByte) < tolerance.payload.Double);
}
/**/

/***UnsignedByte_delete***/
/* Instead of UnsignedByte_delete(), we call scalarDelete(). */
/**/

/***UnsignedByte_print***/
Token UnsignedByte_print(Token thisToken, ...) {
    printf("%dub", thisToken.payload.UnsignedByte);
}
/**/

/***UnsignedByte_toString***/
Token UnsignedByte_toString(Token thisToken, ...) {
    return String_new(UnsignedBytetoString(thisToken.payload.UnsignedByte));
}
/**/

/***UnsignedByte_add***/
Token UnsignedByte_add(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return UnsignedByte_new(thisToken.payload.UnsignedByte + otherToken.payload.UnsignedByte);
}
/**/

/***UnsignedByte_subtract***/
Token UnsignedByte_subtract(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return UnsignedByte_new(thisToken.payload.UnsignedByte - otherToken.payload.UnsignedByte);
}
/**/

/***UnsignedByte_multiply***/
Token UnsignedByte_multiply(Token thisToken, ...) {
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    switch (otherToken.type) {
    case TYPE_UnsignedByte:
        result = UnsignedByte_new(thisToken.payload.UnsignedByte * otherToken.payload.UnsignedByte);
        break;

#ifdef TYPE_Double
    case TYPE_Double:
        result = Double_new(thisToken.payload.UnsignedByte * otherToken.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "UnsignedByte_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***UnsignedByte_divide***/
Token UnsignedByte_divide(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return UnsignedByte_new(thisToken.payload.UnsignedByte / otherToken.payload.UnsignedByte);
}
/**/

/***UnsignedByte_negate***/
Token UnsignedByte_negate(Token thisToken, ...) {
    thisToken.payload.UnsignedByte = -thisToken.payload.UnsignedByte;
    return thisToken;
}
/**/

/***UnsignedByte_zero***/
Token UnsignedByte_zero(Token token, ...) {
    return UnsignedByte_new(0);
}
/**/

/***UnsignedByte_one***/
Token UnsignedByte_one(Token token, ...) {
    return UnsignedByte_new(1);
}
/**/

/***UnsignedByte_clone***/
Token UnsignedByte_clone(Token thisToken, ...) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***UnsignedByte_convert***/
Token UnsignedByte_convert(Token token, ...) {
    switch (token.type) {

#ifdef TYPE_Double
    case TYPE_Double:
        token.payload.UnsignedByte = DoubletoIntegertoken.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "UnsignedByte_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.type = TYPE_UnsignedByte;
    return token;
}
/**/

