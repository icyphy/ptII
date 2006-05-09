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
Token Double_equals(Token this, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
	otherToken = va_arg(argp, Token);
	
	if (otherToken.type != TYPE_Double) {
		otherToken = Double_convert(otherToken);
	}

    va_end(argp);
    
    // Give tolerance for testing.
	return Boolean_new(1.0E-6 > this.payload.Double - otherToken.payload.Double);
}
/**/

/***printBlock***/
Token Double_print(Token this, ...) {
    printf("%g", this.payload.Double);
}
/**/

/***toStringBlock***/
Token Double_toString(Token this, ...) {
	return String_new(DoubletoString(this.payload.Double));
}
/**/

/***addBlock***/
Token Double_add(Token this, ...) {
    va_list argp; 
    Token otherToken;    
    va_start(argp, this);
	otherToken = va_arg(argp, Token);	

    va_end(argp);
	return Double_new(this.payload.Double + otherToken.payload.Double);
}
/**/

/***substractBlock***/
Token Double_substract(Token this, ...) {
    va_list argp; 
    Token otherToken;
    
    va_start(argp, this);
	otherToken = va_arg(argp, Token);	

    va_end(argp);
	return Double_new(this.payload.Double - otherToken.payload.Double);
}
/**/

/***multiplyBlock***/
Token Double_multiply(Token this, ...) {
    va_list argp; 
    Token result;
    Token otherToken;
    
    va_start(argp, this);
	otherToken = va_arg(argp, Token);	

    switch (otherToken.type) {
    	case TYPE_Double:
    		result = Double_new(this.payload.Double * otherToken.payload.Double);
    		break;
        #ifdef TYPE_Int
            case TYPE_Int:
                result = Double_new(this.payload.Double * otherToken.payload.Int);
	    		break;
        #endif

        // FIXME: not finished
        default:
            fprintf(stderr, "Double_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
			exit(1);
    }

    va_end(argp);
	return result;
}
/**/

/***divideBlock***/
Token Double_divide(Token this, ...) {
    va_list argp; 
    Token otherToken;
    
    va_start(argp, this);
	otherToken = va_arg(argp, Token);	

    va_end(argp);
	return Double_new(this.payload.Double / otherToken.payload.Double);
}
/**/

/***negateBlock***/
Token Double_negate(Token this, ...) {
	this.payload.Double = -this.payload.Double;
	return this;
}
/**/

/***zeroBlock***/
Token Double_zero(Token token, ...) {
	return Double_new(0.0);
}
/**/

/***oneBlock***/
Token Double_one(Token token, ...) {
	return Double_new(1.0);
}
/**/


/***cloneBlock***/
Token Double_clone(Token this, ...) {
	return this;
}
/**/




--------------------- static functions --------------------------
/***convertBlock***/
Token Double_convert(Token token, ...) {
    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token.type = TYPE_Double;
                token.payload.Double = InttoDouble(token.payload.Int);
                break;
        #endif

        // FIXME: not finished
        default:
            fprintf(stderr, "Double_convert(): Conversion from an unsupported type. (%d)\n", token.type);
            break;
    }
    token.type = TYPE_Double;
    return token;
}
/**/

