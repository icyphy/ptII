/***declareBlock***/
typedef int IntToken;
/**/

/***funcDeclareBlock***/
Token Int_new(int i);
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
    Token otherToken; 
    va_start(argp, thisToken);
	otherToken = va_arg(argp, Token);
	return Boolean_new(thisToken.payload.Int == otherToken.payload.Int);
}
/**/

/***deleteBlock***/
Token Int_delete(Token token, ...) {}    
/**/

/***cloneBlock***/
Token Int_clone(Token thisToken, ...) {
	return thisToken;
}
/**/

/***convertBlock***/
Token Int_convert(Token token, ...) {
    switch (token.type) {
#ifdef TYPE_String
    case TYPE_String:
        fprintf(stderr,
                "Int_convert(): Converting an String (%s) to an Int is not supported.\n",
                token.payload.String);
        break;
#endif
#ifdef TYPE_Double
    case TYPE_Double:
        token.payload.Int = floor(token.payload.Double);
        break;
#endif
        
    case TYPE_Int:
        fprintf(stderr,
                "Int_convert(): Converting an Int (%d) to an Int?\n",
                token.payload.Int);
        break;

        // FIXME: not finished
    default: 
        {
            char * typeNames[] = {"String", "Double", "Array", "Boolean",
                                  "Int"};
            fprintf(stderr, "Int_convert(): Conversion from an unsupported type. (%d: %s)\n", token.type, (token.type <= 5 ? typeNames[token.type] : "Unknown?") );
        }
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
	return String_new(myItoa(thisToken.payload.Int));
}
/**/

/***toExpressionBlock***/
Token Int_toExpression(Token thisToken, ...) {
	return Int_toString(thisToken);
}
/**/


/***addBlock***/
Token Int_add(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);
	
	return Int_new(thisToken.payload.Int + otherToken.payload.Int);
}
/**/


