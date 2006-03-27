/***declareBlock***/
typedef int IntToken;
/**/

/***funcDeclareBlock***/
Token Int_convert(Token token, ...);
Token Int_print(Token thisToken, ...);
Token Int_toString(Token thisToken, ...);
Token Int_toExpression(Token thisToken, ...);
Token Int_equals(Token thisToken, ...);
/**/


/***newBlock***/
// make a new integer token from the given value.
Token Int_new(int i) {
    Token result;
    result.type = TYPE_Int;
    result.payload.Int = i;
    return result;
}
/**/

/***equalsBlock***/
Token Int_equals(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);
	return Boolean_new(thisToken.payload.Int == otherToken.payload.Int);
}
/**/

/***deleteBlock***/
Token Int_delete(Token token) {   
    free(&token);
}    
/**/


/***convertBlock***/
Token Int_convert(Token token, ...) {
    switch (token.type) {
        #ifdef TYPE_Double
            case TYPE_Double:
                token.payload.Int = floor(token.payload.Double);
                break;
        #endif
        
        // FIXME: not finished
        default:
            fprintf(stderr, "Int_convert(): Conversion from an supported type. (%d)", token.type);
            break;
    }    
    token.type = TYPE_Int;
    return token;
}    
/**/

/***printBlock***/
Token Int_print(Token thisToken, ...) {
    printf("%d", thisToken.payload.Int);
}
/**/

/***toStringBlock***/
Token Int_toString(Token thisToken, ...) {
	return String_new(itoa(thisToken.payload.Int));
}
/**/

/***toExpressionBlock***/
Token Int_toExpression(Token thisToken, ...) {
	return Int_toString(thisToken);
}
/**/




