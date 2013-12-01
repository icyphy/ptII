/*** Scalar_add() ***/
$include(<stdio.h>)
Token* add_Scalar_Array(Scalar a1, Token a2);
Token* Scalar_add(Token* thisToken, ...) {
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);


    switch (otherToken->type) {

    case TYPE_Scalar:
            result = $new(Scalar(thisToken->payload.Scalar + otherToken->payload.Scalar));
            break;

#ifdef TYPE_Array
    case TYPE_Array:
        result = $add_Scalar_Array(thisToken->payload.Scalar, otherToken);
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
                        result = $new(Scalar(thisToken->payload.Scalar + Scalar_convert(otherToken->payload.Int)));
                        break;
#endif

#ifdef TYPE_Double
    case TYPE_Int:
                        result = $new(Scalar(thisToken->payload.Scalar + Scalar_convert(otherToken->payload.Double)));
                        break;
#endif

#ifdef TYPE_Long
    case TYPE_Int:
                        result = $new(Scalar(thisToken->payload.Scalar + Scalar_convert(otherToken->payload.Long)));
                        break;
#endif
        // FIXME: not finished
    default:
        fprintf(stderr, "Scalar_add(): Add with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Scalar_clone() ***/
Token* Scalar_clone(Token* thisToken, ...) {
    return thisToken;
}
/**/

/*** Scalar_convert() ***/
Token* Scalar_convert(Token* token, ...) {
    switch (token->type) {
#ifdef TYPE_Boolean
    case TYPE_Boolean:
            token->type = TYPE_Scalar;
            token->payload.Scalar = (token->payload.Boolean == true ? 1 : 0);
            break;
#endif

#ifdef TYPE_String
    case TYPE_String:
        // FIXME: Is this safe?
        token->type = TYPE_Scalar;
        if (sscanf(token->payload.String, "%lg", &token->payload.Scalar) != 1) {
            fprintf(stderr, "Scalar_convert(): failed to convert \"%s\" to a Scalar\n", token->payload.String);
            exit(-1);
        }
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
        token->type = TYPE_Scalar;
        token->payload.Scalar = InttoDouble(token->payload.Int);
        break;
#endif

#ifdef TYPE_Double
    case TYPE_Double:
        token->type = TYPE_Scalar;
        token->payload.Scalar = token->payload.Double;
        break;
#endif

#ifdef TYPE_Long
    case TYPE_Long:
        token->type = TYPE_Scalar;
        token->payload.Scalar = LongtoDouble(token->payload.Long);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Scalar_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        exit(-1);
        break;
    }
    token->type = TYPE_Scalar;
    return token;
}
/**/

/*** Scalar_divide() ***/
Token* Scalar_divide(Token* thisToken, ...) {
    va_list argp;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Scalar(thisToken->payload.Scalar / otherToken->payload.Scalar));
}
/**/

/***Scalar_equals() ***/
Token* Scalar_equals(Token* thisToken, ...) {
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (otherToken->type != TYPE_Scalar) {
        otherToken = Scalar_convert(otherToken);
    }

    va_end(argp);

    // Give tolerance for testing.
    return $new(Boolean(1.0E-6 > thisToken->payload.Scalar - otherToken->payload.Scalar));
}
/**/

/*** Scalar_isCloseTo() ***/
$include(<math.h>)
Token* Scalar_isCloseTo(Token* thisToken, ...) {
    va_list argp;
    Token* otherToken;
    Token* tolerance;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);
    tolerance = va_arg(argp, Token*);

    va_end(argp);
    return $new(Boolean(fabs(thisToken->payload.Scalar - otherToken->payload.Scalar) < tolerance->payload.Scalar));
}
/**/

/*** Scalar_multiply() ***/
Token* multiply_Scalar_Array(Scalar a1, Token a2);

Token* Scalar_multiply(Token* thisToken, ...) {
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    switch (otherToken->type) {
    case TYPE_Scalar:
        result = $new(Scalar(thisToken->payload.Scalar * otherToken->payload.Scalar));
        break;
#ifdef TYPE_Int
    case TYPE_Int:
        result = $new(Scalar(thisToken->payload.Scalar * otherToken->payload.Int));
        break;
#endif

#ifdef TYPE_Long
    case TYPE_Long:
        result = $new(Scalar(thisToken->payload.Scalar * otherToken->payload.Long));
        break;
#endif

#ifdef TYPE_Double
    case TYPE_Double:
        result = $new(Scalar(thisToken->payload.Scalar * otherToken->payload.Double));
        break;
#endif

#ifdef TYPE_Array
    case TYPE_Array:
        result = $multiply_Scalar_Array(thisToken->payload.Scalar, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Scalar_multiply(): Multiply with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Scalar_negate() ***/
Token* Scalar_negate(Token* thisToken, ...) {
    thisToken->payload.Scalar = -thisToken->payload.Scalar;
    return thisToken;
}
/**/

/*** Scalar_new() ***/
// make a new Scalar token from the given value.
Token* Scalar_new(Scalar s) {
    Token* result;
    result->type = TYPE_Scalar;
    result->payload.Scalar = s;
    return result;
}
/**/

/*** Scalar_one() ***/
Token* Scalar_one(Token* token, ...) {
    return $new(Scalar(1.0));
}
/**/

/*** Scalar_print() ***/
Token* Scalar_print(Token* thisToken, ...) {
    printf("%g", thisToken->payload.Scalar);
}
/**/

/*** Scalar_subtract() ***/
Token* subtract_Scalar_Array(Scalar a1, Token* a2);

Token* Scalar_subtract(Token* thisToken, ...) {
    va_list argp;
    Token* result;
    Token* otherToken;

    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);


    switch (otherToken->type) {
    case TYPE_Scalar:
            result = $new(Scalar(thisToken->payload.Scalar - otherToken->payload.Scalar));
            break;

#ifdef TYPE_Array
    case TYPE_Array:
        result = $subtract_Scalar_Array(thisToken->payload.Scalar, otherToken);
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Scalar_subtract(): Subtract with an unsupported type. (%d)\n", otherToken->type);
        exit(1);
    }

    va_end(argp);
    return result;
}
/**/

/*** Scalar_toString() ***/
Token* Scalar_toString(Token* thisToken, ...) {
    return $new(String($toString_Scalar(thisToken->payload.Scalar)));
}
/**/

/*** Scalar_zero() ***/
Token* Scalar_zero(Token* token, ...) {
    return $new(Scalar(0.0));
}
/**/

/*** declareBlock() ***/
typedef double Scalar;
typedef Scalar ScalarToken;
/**/

/*** funcDeclareBlock() ***/
Token* Scalar_new(Scalar s);
/**/

