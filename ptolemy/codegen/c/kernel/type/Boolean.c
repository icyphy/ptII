/***declareBlock***/
typedef boolean BooleanToken;
/**/

/***funcDeclareBlock***/
Token Boolean_new(boolean b);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token Boolean_new(boolean b) {
    Token result;
    result.type = TYPE_Boolean;
    result.payload.Boolean = b;
    return result;
}
/**/

/***deleteBlock***/
/* Instead of Boolean_delete(), we call scalarDelete(). */
/**/

/***equalsBlock***/
Token Boolean_equals(Token thisToken, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(
            ( thisToken.payload.Boolean && otherToken.payload.Boolean ) || 
            ( !thisToken.payload.Boolean && !otherToken.payload.Boolean ));
}
/**/


/***isCloseToBlock***/
// No need to use Boolean_isCloseTo(), we use Boolean_equals() instead.
/**/

/***printBlock***/
Token Boolean_print(Token thisToken, ...) {
    printf((thisToken.payload.Boolean) ? "true" : "false");
}
/**/

/***toStringBlock***/
Token Boolean_toString(Token thisToken, ...) {
    return String_new(BooleantoString(thisToken.payload.Boolean));
}
/**/

/***addBlock***/
Token Boolean_add(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
    Token otherToken = va_arg(argp, Token);
    va_end(argp);
    return Boolean_new(thisToken.payload.Boolean || otherToken.payload.Boolean);
}
/**/

/***subtractBlock***/
/** Boolean_subtract is not supported. */
/**/

/***multiplyBlock***/
/** Boolean_multiply is not supported. */
/**/

/***divideBlock***/
/** Boolean_divide is not supported. */
/**/

/***negateBlock***/
Token Boolean_negate(Token thisToken, ...) {
    thisToken.payload.Boolean = !thisToken.payload.Boolean;
    return thisToken;
}
/**/

/***zeroBlock***/
Token Boolean_zero(Token token, ...) {
    return Boolean_new(false);
}
/**/

/***oneBlock***/
Token Boolean_one(Token token, ...) {
    return Boolean_new(true);
}
/**/


/***cloneBlock***/
Token Boolean_clone(Token thisToken, ...) {
    return thisToken;
}
/**/


--------------------- static functions ------------------------------
/***convertBlock***/
Token Boolean_convert(Token token, ...) {
    switch (token.type) {
        // FIXME: not finished
    default:
        fprintf(stderr, "Boolean_convert(): Conversion from an unsupported type. (%d)", token.type);
        break;
    }
    token.type = TYPE_Boolean;
    return token;
}    
/**/

