/***declareBlock***/
typedef double DoubleToken;
/**/

/***funcDeclareBlock***/
Token Double_new(double d);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token Double_new(double d) {
    Token result;
    result.type = TYPE_Double;
    result.payload.Double = d;
    return result;
}
/**/


/***deleteBlock***/
Token Double_delete(Token token, ...) {}    
/**/

/***equalsBlock***/
Token Double_equals(Token thisToken, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);
	return Boolean_new(thisToken.payload.Double == otherToken.payload.Double);
}
/**/

/***convertBlock***/
Token Double_convert(Token token, ...) {
    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token.type = TYPE_Double;
                token.payload.Double = (double) token.payload.Int;
                break;
        #endif

        // FIXME: not finished
        default:
            fprintf(stderr, "Double_convert(): Conversion from an unsupported type. (%d)",
                    token.type);
            break;
    }
    token.type = TYPE_Double;
    return token;
}
/**/

/***printBlock***/
Token Double_print(Token thisToken, ...) {
    printf("%g", thisToken.payload.Double);
}
/**/

/***toStringBlock***/
Token Double_toString(Token thisToken, ...) {
	return String_new(ftoa(thisToken.payload.Double));
}
/**/

/***toExpressionBlock***/
Token Double_toExpression(Token thisToken, ...) {
	return Double_toString(thisToken);
}
/**/

/***addBlock***/
Token Double_add(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);	
	return Double_new(thisToken.payload.Int + otherToken.payload.Int);
}
/**/




