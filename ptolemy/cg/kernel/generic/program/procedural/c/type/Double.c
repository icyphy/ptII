/*** Double_add() ***/
$include(<stdio.h>)
Token* add_Double_Array(double a1, Token a2);
Token* Double_add(Token* thisToken, ...) {
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    switch (otherToken->type) {
    case TYPE_Double:
            result = $new(Double(thisToken->payload.Double + otherToken->payload.Double));
            break;

#ifdef TYPE_Array
    case TYPE_Array:
        result = $add_Double_Array(thisToken->payload.Double, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Double_add(): Add with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Double_clone() ***/
Token* Double_clone(Token* thisToken, ...) {
    return thisToken;
}
/**/

/*** Double_convert() ***/
Token* Double_convert(Token* token, ...) {
    switch (token->type) {
#ifdef TYPE_String
    case TYPE_String:
        // FIXME: Is this safe?
        token->type = TYPE_Double;
        if (sscanf(token->payload.String, "%lg", &token->payload.Double) != 1) {
            fprintf(stderr, "Double_convert(): failed to convert \"%s\" to a Double\n", token->payload.String);
            exit(-1);
        }
        break;
#endif
#ifdef TYPE_Int
    case TYPE_Int:
        token->type = TYPE_Double;
        token->payload.Double = InttoDouble(token->payload.Int);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Double_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        exit(-1);
        break;
    }
    token->type = TYPE_Double;
    return token;
}
/**/

/*** Double_delete() ***/
/* Instead of Double_delete(), we call scalarDelete(). */
/**/

/*** Double_divide() ***/
Token* Double_divide(Token *thisToken, ...) {
    va_list argp;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken* = va_arg(argp, Token*);

    va_end(argp);
    return $new(Double(thisToken->payload.Double / otherToken->payload.Double));
}
/**/

/*** Double_equals() ***/
Token* Double_equals(Token *thisToken, ...) {
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (otherToken->type != TYPE_Double) {
        otherToken = Double_convert(otherToken);
    }

    va_end(argp);

    // Give tolerance for testing.
    return $new(Boolean(1.0E-6 > thisToken->payload.Double - otherToken->payload.Double));
}
/**/

/*** Double_isCloseTo() ***/
$include(<math.h>)
Token* Double_isCloseTo(Token *thisToken, ...) {
    va_list argp;
    Token* otherToken;
    Token* tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);
    tolerance = va_arg(argp, Token*);

    va_end(argp);
    return $new(Boolean(fabs(thisToken->payload.Double - otherToken->payload.Double) < tolerance->payload.Double));
}
/**/

/*** Double_multiply() ***/
Token* multiply_Double_Array(double a1, Token* a2);

Token* Double_multiply(Token *thisToken, ...) {
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    switch (otherToken->type) {
    case TYPE_Double:
        result = $new(Double(thisToken->payload.Double * otherToken->payload.Double));
        break;
#ifdef TYPE_Int
    case TYPE_Int:
        result = $new(Double(thisToken->payload.Double * otherToken->payload.Int));
        break;
#endif

#ifdef TYPE_Array
    case TYPE_Array:
        result = $multiply_Double_Array(thisToken->payload.Double, otherToken);
        break;
#endif

#ifdef TYPE_DoubleArray
    case TYPE_DoubleArray:
        result = $multiply_Double_DoubleArray(thisToken->payload.Double, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Double_multiply(): Multiply with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Double_negate() ***/
Token* Double_negate(Token *thisToken, ...) {
    thisToken->payload.Double = -thisToken->payload.Double;
    return thisToken;
}
/**/

/*** Double_new() ***/
// make a new integer token from the given value.
Token* Double_new(double d) {
    Token* result = calloc(1, sizeof(Token));
    result->type = TYPE_Double;
    result->payload.Double = d;
    return result;
}
/**/

/*** Double_one() ***/
Token* Double_one(Token *token, ...) {
    return $new(Double(1.0));
}
/**/

/*** Double_print() ***/
Token* Double_print(Token *thisToken, ...) {
    printf("%g", thisToken->payload.Double);
    return NULL;
}
/**/

/*** Double_subtract() ***/
Token* subtract_Double_Array(double a1, Token a2);

Token* Double_subtract(Token *thisToken, ...) {
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);


    switch (otherToken->type) {
    case TYPE_Double:
            result = $new(Double(thisToken->payload.Double - otherToken->payload.Double));
            break;

#ifdef TYPE_Array
    case TYPE_Array:
        result = $subtract_Double_Array(thisToken->payload.Double, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Double_subtract(): Subtract with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Double_toString() ***/
Token* Double_toString(Token *thisToken, ...) {
    return $new(String($toString_Double(thisToken->payload.Double)));
}
/**/

/*** Double_zero() ***/
Token* Double_zero(Token *token, ...) {
    return $new(Double(0.0));
}
/**/

/*** declareBlock() ***/
typedef double DoubleToken;
/**/

/*** funcDeclareBlock() ***/
Token* Double_new(double d);
/**/

