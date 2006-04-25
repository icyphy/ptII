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

    va_end(argp);
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

/***addBlock***/
Token Int_add(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
	Token otherToken = va_arg(argp, Token);

    va_end(argp);
	return Int_new(this.payload.Int + otherToken.payload.Int);
}
/**/

/***substractBlock***/
Token Int_substract(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
	Token otherToken = va_arg(argp, Token);	

    va_end(argp);
	return Int_new(this.payload.Int - otherToken.payload.Int);
}
/**/

/***multiplyBlock***/
Token Int_multiply(Token this, ...) {
    va_list argp; 
	Token result;
	Token otherToken;

    va_start(argp, this);
	otherToken = va_arg(argp, Token);	

    switch (otherToken.type) {
    	case TYPE_Int:
    		result = Int_new(this.payload.Int * otherToken.payload.Int);
    		break;
    		
        #ifdef TYPE_Double
            case TYPE_Double:
                result = Double_new(this.payload.Int * otherToken.payload.Double);
                break;
        #endif

        // FIXME: not finished
        default:
            fprintf(stderr, "Int_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
            exit(1);
    }

    va_end(argp);
	return result;
}
/**/

/***divideBlock***/
Token Int_divide(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
	Token otherToken = va_arg(argp, Token);	

    va_end(argp);
	return Int_new(this.payload.Int / otherToken.payload.Int);
}
/**/

/***negateBlock***/
Token Int_negate(Token this, ...) {
	this.payload.Int = -this.payload.Int;
	return this;
}
/**/

/***zeroBlock***/
Token Int_zero(Token token, ...) {
	return Int_new(0);
}
/**/

/***oneBlock***/
Token Int_one(Token token, ...) {
	return Int_new(1);
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

