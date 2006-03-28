/***declareBlock***/
typedef char BooleanToken;
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
Token Boolean_delete(Token token, ...) {}    
/**/

/***equalsBlock***/
Token Boolean_equals(Token thisToken, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);
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
            fprintf(stderr, "Boolean_convert(): Conversion from an unsupported type. (%d)", token.type);
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

/***addBlock***/
Token Boolean_add(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);
	return Boolean_new(thisToken.payload.Boolean || otherToken.payload.Boolean);
}
/**/

