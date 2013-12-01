/*** Long_add() ***/
Token* Long_add(Token *thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token* otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Long(thisToken->payload.Long + otherToken->payload.Long));
}
/**/

/*** Long_clone() ***/
Token* Long_clone(Token *thisToken, ...) {
    return thisToken;
}
/**/

/*** Long_convert() ***/
Token* Long_convert(Token token, ...) {
    switch (token->type) {

#ifdef TYPE_Double
    case TYPE_Double:
        token->payload.Long = DoubletoLong(token->payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Long_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        break;
    }
    token->type = TYPE_Long;
    return token;
}
/**/

/*** Long_delete() ***/
/* Instead of Long_delete(), we call scalarDelete(). */
/**/

/*** Long_divide() ***/
Token* Long_divide(Token *thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token* otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Long(thisToken->payload.Long / otherToken->payload.Long));
}
/**/

/*** Long_equals() ***/
Token* Long_equals(Token *thisToken, ...) {
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Boolean(thisToken->payload.Long == otherToken->payload.Long));
}
/**/

/*** Long_multiply() ***/
Token* Long_multiply(Token *thisToken, ...) {
    va_list argp;
    Token *result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    switch (otherToken->type) {
#ifdef TYPE_Int
    case TYPE_Int:
        result = $new(Long(thisToken->payload.Long * otherToken->payload.Int));
        break;
#endif
    case TYPE_Long:
        result = $new(Long(thisToken->payload.Long * otherToken->payload.Long));
        break;

#ifdef TYPE_Double
    case TYPE_Double:
        result = $new(Double(thisToken->payload.Long * otherToken->payload.Double));
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Long_multiply(): Multiply with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Long_negate() ***/
Token* Long_negate(Token *thisToken, ...) {
    thisToken->payload.Long = -thisToken->payload.Long;
    return thisToken;
}
/**/

/*** Long_new() ***/
// make a new long token from the given value.
Token* Long_new(long long i) {
    Token *result;
    result->type = TYPE_Long;
    result->payload.Long = i;
    return result;
}
/**/

/*** Long_one() ***/
Token* Long_one(Token token, ...) {
    return $new(Long(1));
}
/**/

/*** Long_print() ***/
Token* Long_print(Token *thisToken, ...) {
    printf("%d", thisToken->payload.Long);
}
/**/

/*** Long_subtract() ***/
Token* Long_subtract(Token *thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token* otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Long(thisToken->payload.Long - otherToken->payload.Long));
}
/**/

/*** Long_toString() ***/
Token* Long_toString(Token *thisToken, ...) {
    return $new(String($toString_Long(thisToken->payload.Long)));
}
/**/

/*** Long_zero() ***/
Token* Long_zero(Token token, ...) {
    return $new(Long(0));
}
/**/

/*** declareBlock() ***/
typedef long long LongToken;
/**/

/*** funcDeclareBlock() ***/
Token* Long_new(long long i);
/**/

