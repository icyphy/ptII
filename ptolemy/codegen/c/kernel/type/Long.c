/***declareBlock***/
typedef long LongToken;
/**/

/***funcDeclareBlock***/
Token Long_new(long i);
/**/


/***newBlock***/
// make a new long token from the given value.
Token Long_new(long i) {
    Token result;
    result.type = TYPE_Long;
    result.payload.Long = i;
    return result;
}
/**/

/***equalsBlock***/
Token Long_equals(Token this, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(this.payload.Long == otherToken.payload.Long);
}
/**/

/***deleteBlock***/
Token Long_delete(Token token, ...) {
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return emptyToken; 
}    
/**/

/***printBlock***/
Token Long_print(Token this, ...) {
    printf("%d", this.payload.Long);
}
/**/

/***toStringBlock***/
Token Long_toString(Token this, ...) {
    return String_new(LongtoString(this.payload.Long));
}
/**/

/***addBlock***/
Token Long_add(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
    Token otherToken = va_arg(argp, Token);

    va_end(argp);
    return Long_new(this.payload.Long + otherToken.payload.Long);
}
/**/

/***substractBlock***/
Token Long_substract(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
    Token otherToken = va_arg(argp, Token);	

    va_end(argp);
    return Long_new(this.payload.Long - otherToken.payload.Long);
}
/**/

/***multiplyBlock***/
Token Long_multiply(Token this, ...) {
    va_list argp; 
    Token result;
    Token otherToken;

    va_start(argp, this);
    otherToken = va_arg(argp, Token);	

    switch (otherToken.type) {
#ifdef TYPE_Int
    case TYPE_Int:
        result = Long_new(this.payload.Long * otherToken.payload.Int);
        break;
#endif
    case TYPE_Long:
        result = Long_new(this.payload.Long * otherToken.payload.Long);
        break;
    		
#ifdef TYPE_Double
    case TYPE_Double:
        result = Double_new(this.payload.Long * otherToken.payload.Double);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Long_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***divideBlock***/
Token Long_divide(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
    Token otherToken = va_arg(argp, Token);	

    va_end(argp);
    return Long_new(this.payload.Long / otherToken.payload.Long);
}
/**/

/***negateBlock***/
Token Long_negate(Token this, ...) {
    this.payload.Long = -this.payload.Long;
    return this;
}
/**/

/***zeroBlock***/
Token Long_zero(Token token, ...) {
    return Long_new(0);
}
/**/

/***oneBlock***/
Token Long_one(Token token, ...) {
    return Long_new(1);
}
/**/

/***cloneBlock***/
Token Long_clone(Token this, ...) {
    return this;
}
/**/

---------------- static functions -----------------------

/***convertBlock***/
Token Long_convert(Token token, ...) {
    switch (token.type) {

#ifdef TYPE_Double
    case TYPE_Double:
        token.payload.Long = DoubletoLong(token.payload.Double);
        break;
#endif
	
        // FIXME: not finished
    default: 
        fprintf(stderr, "Long_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }    
    token.type = TYPE_Long;
    return token;
}    
/**/

