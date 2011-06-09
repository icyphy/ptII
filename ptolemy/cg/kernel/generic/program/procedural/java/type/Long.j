/***declareBlock***/
typedef long long LongToken;
/**/

/***funcDeclareBlock***/
static Token Long_new(long long i);
/**/


/***Long_new***/
// make a new long token from the given value.
static Token Long_new(long long i) {
    Token result;
    result.type = TYPE_Long;
    result.payload.Long = i;
    return result;
}
/**/

/***Long_equals***/
static Token Long_equals(Token thisToken, ...) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(thisToken.payload.Long == otherToken.payload.Long);
}
/**/

/***Long_delete***/
/* Instead of Long_delete(), we call scalarDelete(). */
/**/

/***Long_print***/
static Token Long_print(Token thisToken, ...) {
    printf("%d", thisToken.payload.Long);
}
/**/

/***Long_toString***/
static Token Long_toString(Token thisToken, ...) {
    return String_new(LongtoString(thisToken.payload.Long));
}
/**/

/***Long_add***/
static Token Long_add(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Long_new(thisToken.payload.Long + otherToken.payload.Long);
}
/**/

/***Long_subtract***/
static Token Long_subtract(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Long_new(thisToken.payload.Long - otherToken.payload.Long);
}
/**/

/***Long_multiply***/
static Token Long_multiply(Token thisToken, ...) {
    va_list argp;
    Token result;
    Token otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    switch (otherToken.type) {
#ifdef TYPE_Integer
    case TYPE_Integer:
        result = Long_new(thisToken.payload.Long * otherToken.payload.Int);
        break;
#endif
    case TYPE_Long:
        result = Long_new(thisToken.payload.Long * otherToken.payload.Long);
        break;

#ifdef TYPE_Double
    case TYPE_Double:
        result = Double_new(thisToken.payload.Long * otherToken.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Long_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***Long_divide***/
static Token Long_divide(Token thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Long_new(thisToken.payload.Long / otherToken.payload.Long);
}
/**/

/***Long_negate***/
static Token Long_negate(Token thisToken, ...) {
    thisToken.payload.Long = -thisToken.payload.Long;
    return thisToken;
}
/**/

/***Long_zero***/
static Token Long_zero(Token token, ...) {
    return Long_new(0);
}
/**/

/***Long_one***/
static Token Long_one(Token token, ...) {
    return Long_new(1);
}
/**/

/***Long_clone***/
static Token Long_clone(Token thisToken, ...) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***Long_convert***/
static Token Long_convert(Token token, ...) {
    switch (token.type) {

#ifdef TYPE_Double
    case TYPE_Double:
        token.payload.Long = DoubletoLong(token.payload.Double);
        break;
#endif
    case TYPE_Long:
        return token;

        // FIXME: not finished
    default:
        fprintf(stderr, "Long_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.type = TYPE_Long;
    return token;
}
/**/

