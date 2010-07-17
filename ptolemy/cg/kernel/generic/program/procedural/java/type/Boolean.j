/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/

/***Boolean_new***/
// make a new integer token from the given value.
static Token Boolean_new(boolean b) {
    Token result = new Token();
    result.type = TYPE_Boolean;
    result.payload = Boolean.valueOf(b);
    return result;
}
/**/

/***Boolean_delete***/
/* Instead of Boolean_delete(), we call scalarDelete(). */
/**/

/***Boolean_equals***/
static Token Boolean_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];

    return Boolean_new(
            ( (Boolean)thisToken.payload && (Boolean)otherToken.payload ) ||
            ( !(Boolean)thisToken.payload && !(Boolean)otherToken.payload ));
}
/**/


/***Boolean_isCloseTo***/
// No need to use Boolean_isCloseTo(), we use Boolean_equals() instead.
/**/

/***Boolean_print***/
static Token Boolean_print(Token thisToken, Token... tokens) {
    System.out.println((Boolean)thisToken.payload);
    return null;
}
/**/

/***Boolean_toString***/
static Token Boolean_toString(Token thisToken, Token... ignored) {
    return String_new(BooleantoString((Boolean)thisToken.payload));
}
/**/

/***Boolean_add***/
static Token Boolean_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Boolean_new((Boolean)thisToken.payload || (Boolean)otherToken.payload);
}
/**/

/***Boolean_subtract***/
/** Boolean_subtract is not supported. */
/**/

/***Boolean_multiply***/
/** Boolean_multiply is not supported. */
/**/

/***Boolean_divide***/
/** Boolean_divide is not supported. */
/**/

/***Boolean_negate***/
static Token Boolean_negate(Token thisToken, Token... tokens) {
    return Boolean_new(!(Boolean)thisToken.payload);
}
/**/

/***Boolean_zero***/
static Token Boolean_zero(Token token, Token... tokens) {
    return Boolean_new(false);
}
/**/

/***Boolean_one***/
static Token Boolean_one(Token token, Token... tokens) {
    return Boolean_new(true);
}
/**/


/***Boolean_clone***/
static Token Boolean_clone(Token thisToken, Token... tokens) {
    return thisToken;
}
/**/


--------------------- static functions ------------------------------
/***Boolean_convert***/
static Token Boolean_convert(Token token, Token... tokens) {
    switch (token.type) {
    case TYPE_Boolean:
        return token;
    default:
        throw new RuntimeException("Boolean_convert(): Conversion from an unsupported type.: " + token.type);
    }
}
/**/

