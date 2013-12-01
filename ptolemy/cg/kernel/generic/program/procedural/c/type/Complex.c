/*** Complex_add() ***/
Token* Complex_add(Token *thisToken, ...) {
    va_list argp;
    Token *otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    va_end(argp);

    return $new(Complex(
    thisToken->payload.Complex->real + otherToken->payload.Complex->real,
    thisToken->payload.Complex->imag + otherToken->payload.Complex->imag));
}
/**/

/*** Complex_clone() ***/
Token* Complex_clone(Token *thisToken, ...) {
    return $new(Complex(thisToken->payload.Complex->real, thisToken->payload.Complex->imag));
}
/**/

/*** Complex_convert() ***/
Token* Complex_convert(Token token, ...) {
    switch (token->type) {
#ifdef TYPE_Int
    case TYPE_Int:
        token->type = TYPE_Complex;
        //token->payload.Complex = InttoComplex(token->payload.Int);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Complex_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        break;
    }
    token->type = TYPE_Complex;
    return token;
}
/**/

/*** Complex_delete() ***/
Token* Complex_delete(Token token, ...) {
    free(token->payload.Complex);
}
/**/

/*** Complex_divide() ***/
Token* Complex_divide(Token *thisToken, ...) {
    va_list argp;
    Token *otherToken;
    Token *result;
    double r1, i1, r2, i2, temp;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    r1 = thisToken->payload.Complex->real;
    i1 = thisToken->payload.Complex->imag;
    r2 = otherToken->payload.Complex->real;
    i2 = otherToken->payload.Complex->imag;

    //(a+ib)/(c+id)=(ac+bd+i(bc-ad))/(c^2+d^2)
    temp = (r2 * r2) + (i2 * i2);

    result = $new(Complex(((r1 * r2) + (i1 * i2)) / temp, ((i1 * r2) - (r1 * i2)) / temp));

    va_end(argp);
    return result;
}
/**/

/*** Complex_equals() ***/
Token* Complex_equals(Token *thisToken, ...) {
    va_list argp;
    Token *otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (otherToken->type != TYPE_Complex) {
        otherToken = Complex_convert(otherToken);
    }

    va_end(argp);

    // Give tolerance for testing.
    return $new(Boolean(
    (1.0E-6 > thisToken->payload.Complex->real - otherToken->payload.Complex->real) &&
    (1.0E-6 > thisToken->payload.Complex->imag - otherToken->payload.Complex->imag)));
}
/**/

/*** Complex_multiply() ***/
Token* Complex_multiply(Token *thisToken, ...) {
    va_list argp;
    Token *result;
    Token *otherToken;
    double r1, i1, r2, i2;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    r1 = thisToken->payload.Complex->real;
    i1 = thisToken->payload.Complex->imag;

    switch (otherToken->type) {
    case TYPE_Complex:
        r2 = otherToken->payload.Complex->real;
        i2 = otherToken->payload.Complex->imag;

        result = $new(Complex((r1 * r2) - (i1 * i2), (r1 * i2) + (r2 * i1)));
        break;

#ifdef TYPE_Double
    case TYPE_Double:
        r2 = otherToken->payload.Double;
        result = $new(Complex(r1 * r2, i1 * r2));
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
        result = $new(Complex(r1 * otherToken->payload.Int, i1 * otherToken->payload.Int));
        break;
#endif


        // FIXME: not finished
    default:
        fprintf(stderr, "Complex_multiply(): Multiply with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Complex_negate() ***/
Token* Complex_negate(Token *thisToken, ...) {
    thisToken->payload.Complex->real = -thisToken->payload.Complex->real;
    thisToken->payload.Complex->imag = -thisToken->payload.Complex->imag;
    return thisToken;
}
/**/

/*** Complex_new() ***/
// make a new integer token from the given value.
Token* Complex_new(double real, double imag) {
    Token *result;
    result->type = TYPE_Complex;
    result->payload.Complex = (ComplexToken) malloc(sizeof(struct complex));
    result->payload.Complex->real = real;
    result->payload.Complex->imag = imag;
    return result;
}
/**/

/*** Complex_one() ***/
Token* Complex_one(Token token, ...) {
    return $new(Complex(1.0, 0.0));
}
/**/

/*** Complex_print() ***/
Token* Complex_print(Token *thisToken, ...) {
    printf("%g + %gi", thisToken->payload.Complex->real, thisToken->payload.Complex->imag);
}
/**/

/*** Complex_subtract() ***/
Token* Complex_subtract(Token *thisToken, ...) {
    va_list argp;
    Token *otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    va_end(argp);

    return $new(Complex(
    thisToken->payload.Complex->real - otherToken->payload.Complex->real,
    thisToken->payload.Complex->imag - otherToken->payload.Complex->imag));
}
/**/

/*** Complex_toString() ***/
Token* Complex_toString(Token *thisToken, ...) {
    char* string = (char*) malloc(sizeof(char) * 32);
    sprintf(string, "%.14g + %.14gi", thisToken->payload.Complex->real, thisToken->payload.Complex->imag);

    return $new(String(string));
}
/**/

/*** Complex_zero() ***/
Token* Complex_zero(Token token, ...) {
    return $new(Complex(0.0, 0.0));
}
/**/

/*** declareBlock() ***/
struct complex {
    double real;    // size of the array.
    double imag;    // array of Token elements.
};
typedef struct complex* ComplexToken;
/**/

/*** funcDeclareBlock() ***/
Token* Complex_new(double real, double imag);
/**/

