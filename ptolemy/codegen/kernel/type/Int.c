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
Token Int_equals(Token this, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
	otherToken = va_arg(argp, Token);
	return Boolean_new(this.payload.Int == otherToken.payload.Int);
}
/**/

/***deleteBlock***/
Token Int_delete(Token token, ...) {}    
/**/

/***printBlock***/
Token Int_print(Token this, ...) {
    printf("%d", this.payload.Int);
}
/**/

/***toStringBlock***/
Token Int_toString(Token this, ...) {
	return String_new(InttoString(this.payload.Int));
}
/**/

/***toExpressionBlock***/
Token Int_toExpression(Token this, ...) {
	return Int_toString(this);
}
/**/


/***addBlock***/
Token Int_add(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
	Token otherToken = va_arg(argp, Token);
	
	return Int_new(this.payload.Int + otherToken.payload.Int);
}
/**/

/***substractBlock***/
Token Int_substract(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
	Token otherToken = va_arg(argp, Token);	
	return Int_new(this.payload.Int - otherToken.payload.Int);
}
/**/


/***negateBlock***/
Token Int_negate(Token this, ...) {
	this.payload.Int = -this.payload.Int;
	return this;
}
/**/


---------------- static functions -----------------------

/***convertBlock***/
Token Int_convert(Token token, ...) {
    switch (token.type) {

		#ifdef TYPE_Double
		    case TYPE_Double:
		        token.payload.Int = DoubletoInt(token.payload.Double);
		        break;
		#endif
	
	    // FIXME: not finished
	    default: 
	        fprintf(stderr, "Int_convert(): Conversion from an unsupported type. (%d)\n", token.type);
	        break;
    }    
    token.type = TYPE_Int;
    return token;
}    
/**/

/***zeroBlock***/
Token Int_zero(Token token, ...) {
	return Int_new(0);
}
/**/