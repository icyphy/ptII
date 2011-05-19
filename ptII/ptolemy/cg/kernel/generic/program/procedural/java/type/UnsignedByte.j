/***declareBlock***/
typedef unsigned char UnsignedByteToken;
/**/

/***funcDeclareBlock***/
static Token UnsignedByte_new(int i);
/**/


/***UnsignedByte_new***/
// make a new unsigned byte token from the given value.
static Token UnsignedByte_new(int b) {
    Token result;
    result.type = TYPE_UnsignedByte;
    result.payload.UnsignedByte = b;
    return result;
}
/**/

/***UnsignedByte_equals***/
static Token UnsignedByte_equals(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Boolean_new((UnsignedByte)thisToken.payload == (UnsignedByte)otherToken.payload);
}
/**/

/***UnsignedByte_isCloseTo***/
static Token UnsignedByte_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    Token tolerance = tokens[1];
    return Boolean_new(Math.abs((UnsignedByte)thisToken.payload - (UnsignedByte)otherToken.payload) < (Double)tolerance.payload);
}
/**/

/***UnsignedByte_delete***/
/* Instead of UnsignedByte_delete(), we call scalarDelete(). */
/**/

/***UnsignedByte_print***/
static Token UnsignedByte_print(Token thisToken, Token... tokens) {
    printf("%dub", (UnsignedByte)thisToken.payload);
}
/**/

/***UnsignedByte_toString***/
static Token UnsignedByte_toString(Token thisToken, Token... tokens) {
    return String_new(UnsignedBytetoString((UnsignedByte)thisToken.payload));
}
/**/

/***UnsignedByte_add***/
static Token UnsignedByte_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return UnsignedByte_new((UnsignedByte)thisToken.payload + (UnsignedByte)otherToken.payload);
}
/**/

/***UnsignedByte_subtract***/
static Token UnsignedByte_subtract(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return UnsignedByte_new((UnsignedByte)thisToken.payload - (UnsignedByte)otherToken.payload);
}
/**/

/***UnsignedByte_multiply***/
static Token UnsignedByte_multiply(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    switch (otherToken.type) {
    case TYPE_UnsignedByte:
        result = UnsignedByte_new((UnsignedByte)thisToken.payload * (UnsignedByte)otherToken.payload);
        break;

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        result = Double_new((UnsignedByte)thisToken.payload * (Double)otherToken.payload);
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
static Token UnsignedByte_divide(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return UnsignedByte_new((UnsignedByte)thisToken.payload / (UnsignedByte)otherToken.payload);
}
/**/

/***UnsignedByte_negate***/
static Token UnsignedByte_negate(Token thisToken, Token... tokens) {
    (UnsignedByte)thisToken.payload = -(UnsignedByte)thisToken.payload;
    return thisToken;
}
/**/

/***UnsignedByte_zero***/
static Token UnsignedByte_zero(Token token, Token... tokens) {
    return UnsignedByte_new(0);
}
/**/

/***UnsignedByte_one***/
static Token UnsignedByte_one(Token token, Token... tokens) {
    return UnsignedByte_new(1);
}
/**/

/***UnsignedByte_clone***/
static Token UnsignedByte_clone(Token thisToken, Token... tokens) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***UnsignedByte_convert***/
static Token UnsignedByte_convert(Token token, Token... tokens) {
    switch (token.type) {

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        token.payload.UnsignedByte = DoubletoInteger((Double)token.payload);
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

