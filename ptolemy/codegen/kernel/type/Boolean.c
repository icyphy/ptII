/***declareBlock***/
typedef boolean BooleanToken;
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
Token Boolean_equals(Token this, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
	otherToken = va_arg(argp, Token);

    va_end(argp);
	return Boolean_new(
	( this.payload.Boolean && otherToken.payload.Boolean ) || 
	( !this.payload.Boolean && !otherToken.payload.Boolean ));
}
/**/


/***printBlock***/
Token Boolean_print(Token this, ...) {
    printf((this.payload.Boolean) ? "true" : "false");
}
/**/

/***toStringBlock***/
Token Boolean_toString(Token this, ...) {
	return String_new(BooleantoString(this.payload.Boolean));
}
/**/

/***addBlock***/
Token Boolean_add(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
	Token otherToken = va_arg(argp, Token);
    va_end(argp);
	return Boolean_new(this.payload.Boolean || otherToken.payload.Boolean);
}
/**/

/***substractBlock***/
Token Boolean_substract(Token this, ...) {
	fprintf(stderr, "Boolean_substract not supported");
	exit(1);
}
/**/

/***multiplyBlock***/
Token Boolean_multiply(Token this, ...) {
	fprintf(stderr, "Boolean_multiply not supported");
	exit(1);
}
/**/

/***divideBlock***/
Token Boolean_divide(Token this, ...) {
	fprintf(stderr, "Boolean_divide not supported");
	exit(1);
}
/**/
/***negateBlock***/
Token Boolean_negate(Token this, ...) {
	this.payload.Boolean = !this.payload.Boolean;
	return this;
}
/**/

/***zeroBlock***/
Token Boolean_zero(Token token, ...) {
	return Boolean_new(false);
}
/**/

/***oneBlock***/
Token Boolean_one(Token token, ...) {
	return Boolean_new(true);
}
/**/




--------------------- static functions ------------------------------
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

