/*** Boolean_add() ***/
Token* Boolean_add(Token* thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token* otherToken = va_arg(argp, Token*);
    va_end(argp);
    return $new(Boolean(thisToken->payload.Boolean || otherToken->payload.Boolean));
}
/**/

/*** Boolean_clone() ***/
Token* Boolean_clone(Token* thisToken, ...) {
    return thisToken;
}
/**/

/*** Boolean_convert() ***/
Token* Boolean_convert(Token* token, ...) {
    switch (token->type) {
        // FIXME: not finished
    default:
        fprintf(stderr, "Boolean_convert(): Conversion from an unsupported type. (%d)", token->type);
        break;
    }
    token->type = TYPE_Boolean;
    return token;
}
/**/

/*** Boolean_delete() ***/
/* Instead of Boolean_delete(), we call scalarDelete(). */
/**/

/*** Boolean_divide() ***/
/** Boolean_divide is not supported. */
/**/

/*** Boolean_equals() ***/
Token* Boolean_equals(Token* thisToken, ...) {
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Boolean(
                    ( thisToken->payload.Boolean && otherToken->payload.Boolean ) ||
                    ( !thisToken->payload.Boolean && !otherToken->payload.Boolean )));
}
/**/

/*** Boolean_isCloseTo() ***/
// No need to use Boolean_isCloseTo(), we use Boolean_equals() instead.
/**/

/*** Boolean_multiply() ***/
/** Boolean_multiply is not supported. */
/**/

/*** Boolean_negate() ***/
Token* Boolean_negate(Token* thisToken, ...) {
    thisToken->payload.Boolean = !thisToken->payload.Boolean;
    return thisToken;
}
/**/

/*** Boolean_new() ***/
// make a new integer token from the given value.
Token* Boolean_new(boolean b) {
    Token* result = malloc(sizeof(Token));
    result->type = TYPE_Boolean;
    result->payload.Boolean = b;
    return result;
}
/**/

/*** Boolean_one() ***/
Token* Boolean_one(Token* token, ...) {
    return $new(Boolean(true));
}
/**/

/*** Boolean_print() ***/
Token* Boolean_print(Token* thisToken, ...) {
    printf((thisToken->payload.Boolean) ? "true" : "false");
    return emptyToken;
}
/**/

/*** Boolean_subtract() ***/
/** Boolean_subtract is not supported. */
/**/

/*** Boolean_toString() ***/
Token* Boolean_toString(Token* thisToken, ...) {
    return $new(String($toString_Boolean(thisToken->payload.Boolean)));
}
/**/

/*** Boolean_zero() ***/
Token* Boolean_zero(Token* token, ...) {
    return $new(Boolean(false));
}
/**/

/*** declareBlock() ***/
typedef boolean BooleanToken;
/**/

/*** funcDeclareBlock() ***/
Token* Boolean_new(boolean b);
/**/

