/***declareBlock***/
/**/

/***funcDeclareBlock***/
/**/


/***Integer_new***/
// make a new integer token from the given value.
static Token Integer_new(int i) {
    Token result = new Token();
    result.type = TYPE_Integer;
    result.payload = Integer.valueOf(i);
    return result;
}
/**/

/***Integer_equals***/
static Token Integer_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];

    return Boolean_new((Integer)(thisToken.payload) == (Integer)(otherToken.payload));
}
/**/

/***Integer_isCloseTo***/
static Token Integer_isCloseTo(Token thisToken, Token... tokens) {
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
static Token Integer_print(Token thisToken, Token... tokens) {
    System.out.println((Integer)thisToken.payload);
    return null;
}
/**/

/***Integer_toString***/
static Token Integer_toString(Token thisToken, Token... tokens) {
    return String_new(((Integer)thisToken.payload).toString());
}
/**/

/***Integer_add***/
static Token Integer_add(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Integer_new((Integer)(thisToken.payload) + (Integer)(otherToken.payload));
}
/**/

/***Integer_subtract***/
static Token Integer_subtract(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Integer_new((Integer)(thisToken.payload) - (Integer)(otherToken.payload));
}
/**/

/***Integer_multiply***/
static Token Integer_multiply(Token thisToken, Token... tokens) {
    Token result = new Token();
    Token otherToken;

    otherToken = tokens[0];

    switch (otherToken.type) {
    case TYPE_Integer:
        result = Integer_new((Integer)(thisToken.payload) * (Integer)(otherToken.payload));
        break;

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        result = Double_new((Integer)(thisToken.payload) * (Double)(otherToken.payload));
        break;
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Integer_multiply(): Multiply with an unsupported type.: " + otherToken.type);
    }

    return result;
}
/**/

/***Integer_divide***/
static Token Integer_divide(Token thisToken, Token... tokens) {
    Token otherToken = tokens[0];
    return Integer_new((Integer)(thisToken.payload) / (Integer)(otherToken.payload));
}
/**/

/***Integer_negate***/
static Token Integer_negate(Token thisToken, Token... tokens) {
    return Integer_new(-(Integer)(thisToken.payload));
}
/**/

/***Integer_zero***/
static Token Integer_zero(Token token, Token... tokens) {
    return Integer_new(0);
}
/**/

/***Integer_one***/
static Token Integer_one(Token token, Token... tokens) {
    return Integer_new(1);
}
/**/

/***Integer_clone***/
static Token Integer_clone(Token thisToken, Token... tokens) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***Integer_convert***/
static Token Integer_convert(Token token, Token... elements) {
    token.type = TYPE_Integer;
    switch (token.type) {

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        token.payload = DoubletoInteger((Double)token.payload);
        return token;

#endif
    case TYPE_Integer:
        return token;
        // FIXME: not finished
    default:
        throw new RuntimeException("Integer_convert(): Conversion from an unsupported type: "
         + token.type);
    }
}
/**/

