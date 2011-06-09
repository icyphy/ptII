/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/

/***String_new***/
/* Make a new integer token from the given value. */
static Token String_new(String s) {
    Token result = new Token();;
    result.type = TYPE_String;
    result.payload = new String(s);
    return result;
}
/**/

/***String_delete***/
static Token String_delete(Token token, Token... ignored) {
    //free(token.payload.String);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken;
}
/**/

/***String_equals***/
static Token String_equals(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Boolean_new(((String)(thisToken.payload)).equals((String)(otherToken.payload)));
}
/**/

/***String_isCloseTo***/
/* No need to use String_isCloseTo(), we use String_equals() instead. */
}
/**/

/***String_print***/
static Token String_print(Token thisToken, Token... tokens) {
    System.out.println((String)(thisToken.payload));
    return emptyToken;
}
/**/

/***String_toString***/
static Token String_toString(Token thisToken, Token... ignored) {
    return String_new("\"" + (String)(thisToken.payload) + "\"");
}
/**/

/***String_add***/
static Token String_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];

    return String_new((String)(String_convert(thisToken).payload)
               + (String)(String_convert(otherToken).payload));
}
/**/

/***String_subtract***/
/** String_subtract is not supported. */
/**/

/***String_multiply***/
/** String_multiply is not supported. */
/**/

/***String_divide***/
/** String_divide is not supported. */
/**/

/***String_negate***/
static Token String_negate(Token thisToken, Token... tokens) {
    return emptyToken;
}
/**/

/***String_zero***/
static Token String_zero(Token token, Token... tokens) {
    return String_new("");
}
/**/

/***String_one***/
/** String_one is not supported. */
/**/

/***String_clone***/
static Token String_clone(Token thisToken, Token... tokens) {
    return String_new((String)(thisToken.payload));
}
/**/


------------------ static functions --------------------------------------

/***String_convert***/
static Token String_convert(Token token, Token... ignored) {
    switch (token.type) {
#ifdef PTCG_TYPE_Boolean
    case TYPE_Boolean:
        token.payload = BooleantoString((Boolean)(token.payload));
        token.type = TYPE_String;
        return token;
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        token.payload = IntegertoString((Integer)(token.payload));
        token.type = TYPE_String;
        return token;
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        token.payload = DoubletoString((Double)(token.payload));
        token.type = TYPE_String;
        return token;
#endif
    case TYPE_String:
        return token;
        // FIXME: not finished
    default:
        throw new RuntimeException("String_convert(): Conversion from an unsupported type: "
         + token.type);
    }

}
/**/

