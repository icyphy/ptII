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

/***Complex_new***/
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


/***Complex_delete***/
Token Complex_delete(Token token, Token... tokens) {
    free(token.payload.Complex);
}
/**/

/***Complex_equals***/
Token Complex_equals(Token thisToken, Token... tokens) {
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

/***Complex_print***/
Token Complex_print(Token thisToken, Token... tokens) {
    printf("%g + %gi", thisToken.payload.Complex->real, thisToken.payload.Complex->imag);
}
/**/

/***Complex_toString***/
Token Complex_toString(Token thisToken, Token... tokens) {
    char* string = (char*) malloc(sizeof(char) * 32);
    sprintf(string, "%.14g + %.14gi", thisToken.payload.Complex->real, thisToken.payload.Complex->imag);

    return String_new(string);
}
/**/

/***Complex_add***/
Token Complex_add(Token thisToken, Token... tokens) {
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

/***Complex_subtract***/
Token Complex_subtract(Token thisToken, Token... tokens) {
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

/***Complex_multiply***/
Token Complex_multiply(Token thisToken, Token... tokens) {
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

#ifdef TYPE_Integer
    case TYPE_Integer:
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

/***Complex_divide***/
Token Complex_divide(Token thisToken, Token... tokens) {
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

/***Complex_negate***/
Token Complex_negate(Token thisToken, Token... tokens) {
    thisToken.payload.Complex->real = -thisToken.payload.Complex->real;
    thisToken.payload.Complex->imag = -thisToken.payload.Complex->imag;
    return thisToken;
}
/**/

/***Complex_zero***/
Token Complex_zero(Token token, Token... tokens) {
    return Complex_new(0.0, 0.0);
}
/**/

/***Complex_one***/
Token Complex_one(Token token, Token... tokens) {
    return Complex_new(1.0, 0.0);
}
/**/


/***Complex_clone***/
Token Complex_clone(Token thisToken, Token... tokens) {
    return Complex_new(thisToken.payload.Complex->real, thisToken.payload.Complex->imag);
}
/**/




--------------------- static functions --------------------------
/***Complex_convert***/
Token Complex_convert(Token token, Token... tokens) {
    switch (token.type) {
#ifdef TYPE_Integer
    case TYPE_Integer:
        token.type = TYPE_Complex;
        //token.payload.Complex = IntegertoComplex(token.payload.Int);
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

