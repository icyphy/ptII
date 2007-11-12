/***declareBlock***/
struct complex {
    double real;    // size of the array.
    double imag;    // array of Token elements.
};
typedef struct complex* ComplexToken;
/**/

/***funcDeclareBlock***/
Token Complex_new(double real, double imag);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token Complex_new(double real, double imag) {
    Token result;
    result.type = TYPE_Complex;
    result.payload.Complex = (ComplexToken) malloc(sizeof(struct complex));
    result.payload.Complex->real = real;
    result.payload.Complex->imag = imag;
    return result;
}
/**/


/***deleteBlock***/
Token Complex_delete(Token token, ...) {
    free(token.payload.Complex);
}    
/**/

/***equalsBlock***/
Token Complex_equals(Token thisToken, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);
    
    if (otherToken.type != TYPE_Complex) {
        otherToken = Complex_convert(otherToken);
    }

    va_end(argp);
    
    // Give tolerance for testing.
    return Boolean_new(
    (1.0E-6 > thisToken.payload.Complex->real - otherToken.payload.Complex->real) &&
    (1.0E-6 > thisToken.payload.Complex->imag - otherToken.payload.Complex->imag));
}
/**/

/***printBlock***/
Token Complex_print(Token thisToken, ...) {
    printf("%g + %gi", thisToken.payload.Complex->real, thisToken.payload.Complex->imag);
}
/**/

/***toStringBlock***/
Token Complex_toString(Token thisToken, ...) {
    char* string = (char*) malloc(sizeof(char) * 32);
    sprintf(string, "%.14g + %.14gi", thisToken.payload.Complex->real, thisToken.payload.Complex->imag);
    
    return String_new(string);
}
/**/

/***addBlock***/
Token Complex_add(Token thisToken, ...) {
    va_list argp; 
    Token otherToken;    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);   

    va_end(argp);
    
    return Complex_new(
    thisToken.payload.Complex->real + otherToken.payload.Complex->real, 
    thisToken.payload.Complex->imag + otherToken.payload.Complex->imag);
}
/**/

/***subtractBlock***/
Token Complex_subtract(Token thisToken, ...) {
    va_list argp; 
    Token otherToken;
    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);   

    va_end(argp);

    return Complex_new(
    thisToken.payload.Complex->real - otherToken.payload.Complex->real, 
    thisToken.payload.Complex->imag - otherToken.payload.Complex->imag);
}
/**/

/***multiplyBlock***/
Token Complex_multiply(Token thisToken, ...) {
    va_list argp; 
    Token result;
    Token otherToken;
    double r1, i1, r2, i2;
    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);   

    r1 = thisToken.payload.Complex->real;
    i1 = thisToken.payload.Complex->imag;
    
    switch (otherToken.type) {
    case TYPE_Complex:
        r2 = otherToken.payload.Complex->real;
        i2 = otherToken.payload.Complex->imag;

        result = Complex_new((r1 * r2) - (i1 * i2), (r1 * i2) + (r2 * i1));
        break;
        
#ifdef TYPE_Double
    case TYPE_Double:
        r2 = otherToken.payload.Double;
        result = Complex_new(r1 * r2, i1 * r2);
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
        result = Complex_new(r1 * otherToken.payload.Intl, i1 * otherToken.payload.Int);
        break;
#endif


        // FIXME: not finished
    default:
        fprintf(stderr, "Complex_multiply(): Multiply with an unsupported type. (%d)\n", otherToken.type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/***divideBlock***/
Token Complex_divide(Token thisToken, ...) {
    va_list argp; 
    Token otherToken;
    Token result;
    double r1, i1, r2, i2, temp;
    
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);   

    r1 = thisToken.payload.Complex->real;
    i1 = thisToken.payload.Complex->imag;
    r2 = otherToken.payload.Complex->real;
    i2 = otherToken.payload.Complex->imag;
    
    //(a+ib)/(c+id)=(ac+bd+i(bc-ad))/(c^2+d^2) 
    temp = (r2 * r2) + (i2 * i2);
    
    result = Complex_new(((r1 * r2) + (i1 * i2)) / temp, ((i1 * r2) - (r1 * i2)) / temp);
    
    va_end(argp);
    return result;
}
/**/

/***negateBlock***/
Token Complex_negate(Token thisToken, ...) {
    thisToken.payload.Complex->real = -thisToken.payload.Complex->real;
    thisToken.payload.Complex->imag = -thisToken.payload.Complex->imag;
    return thisToken;
}
/**/

/***zeroBlock***/
Token Complex_zero(Token token, ...) {
    return Complex_new(0.0, 0.0);
}
/**/

/***oneBlock***/
Token Complex_one(Token token, ...) {
    return Complex_new(1.0, 0.0);
}
/**/


/***cloneBlock***/
Token Complex_clone(Token thisToken, ...) {
    return Complex_new(thisToken.payload.Complex->real, thisToken.payload.Complex->imag);
}
/**/




--------------------- static functions --------------------------
/***convertBlock***/
Token Complex_convert(Token token, ...) {
    switch (token.type) {
#ifdef TYPE_Int
    case TYPE_Int:
        token.type = TYPE_Complex;
        //token.payload.Complex = InttoComplex(token.payload.Int);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Complex_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.type = TYPE_Complex;
    return token;
}
/**/

