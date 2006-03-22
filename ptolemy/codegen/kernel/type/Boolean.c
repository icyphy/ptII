/***declareBlock***/
typedef char BooleanToken;
/**/

/***funcDeclareBlock***/
Token Boolean_convert(Token token, ...);
Token Boolean_print(Token thisToken, ...);
Token Boolean_toString(Token thisToken, ...);
Token Boolean_toExpression(Token thisToken, ...);
Token Boolean_equals(Token thisToken, ...);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token Boolean_new(char b) {
    Token result;
    result.type = TYPE_Boolean;
    result.payload.Boolean = b;
    return result;
}
/**/


/***deleteBlock***/
Token Boolean_delete(Token token) {   
    free(&token);
}    
/**/

/***equalsBlock***/
Token Boolean_equals(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);
	return Boolean_new(
	( thisToken.payload.Boolean && otherToken.payload.Boolean ) || 
	( !thisToken.payload.Boolean && !otherToken.payload.Boolean ));
}
/**/


/***convertBlock***/
Token Boolean_convert(Token token, ...) {
    switch (token.type) {
        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }    
    token.type = TYPE_Boolean;
    return token;
}    
/**/

/***printBlock***/
Token Boolean_print(Token thisToken, ...) {
    printf((thisToken.payload.Boolean) ? "true" : "false");
}
/**/

/***toStringBlock***/
Token Boolean_toString(Token thisToken, ...) {
	return String_new(btoa(thisToken.payload.Boolean));
}
/**/

/***toExpressionBlock***/
Token Boolean_toExpression(Token thisToken, ...) {
	return Boolean_toString(thisToken);
}
/**/



