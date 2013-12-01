/*** Pointer_add() ***/
/** Pointer_add is not supported. */
/**/

/*** Pointer_clone() ***/
Token* Pointer_clone(Token *thisToken, ...) {
    return Pointer_new(thisToken->payload.Pointer);
}
/**/

/*** Pointer_convert() ***/
Token* Pointer_convert(Token token, ...) {
    switch (token->type) {

#ifdef TYPE_Object
    case TYPE_Object:
        token->payload.Pointer = token->payload.Object;
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Pointer_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        break;
    }
    token->type = TYPE_Pointer;
    return token;
}
/**/

/*** Pointer_delete() ***/
Token* Pointer_delete(Token *thisToken, ...) {
    free(thisToken->payload.Pointer);
    return emptyToken;
}
/**/

/*** Pointer_divide() ***/
/** Pointer_divide is not supported. */
/**/

/*** Pointer_equals() ***/
Token* Pointer_equals(Token *thisToken, ...) {
    va_list argp;
    Token *otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    if (otherToken->type != TYPE_Pointer) {
        otherToken = Pointer_convert(otherToken);
    }

    va_end(argp);

    return Boolean_new(
        (int) thisToken->payload.Pointer == (int) otherToken->payload.Pointer);
}
/**/

/*** Pointer_isCloseTo() ***/
Token* Pointer_isCloseTo(Token *thisToken, ...) {
    /** Pointers are never close to each other. */
    return Boolean_new(false);
}
/**/

/*** Pointer_multiply() ***/
/** Pointer_multiply is not supported. */
/**/

/*** Pointer_negate() ***/
/** Pointer_negate is not supported. */
/**/

/*** Pointer_new() ***/
Token* Pointer_new(void *i) {
    Token *result;
    result->type = TYPE_Pointer;
                result->payload.Pointer = (PointerToken) malloc(sizeof(void *));
    result->payload.Pointer = i;
    return result;
}
/**/

/*** Pointer_one() ***/
/** Pointer_one is not supported. */
/**/

/*** Pointer_print() ***/
Token* Pointer_print(Token *thisToken, ...) {
    printf("Pointer at %o", (int) thisToken->payload.Pointer);
}
/**/

/*** Pointer_subtract() ***/
/** Pointer_subtract is not supported. */
/**/

/*** Pointer_toString() ***/
Token* Pointer_toString(Token *thisToken, ...) {
    return $new(String($toString_Pointer(thisToken->payload.Pointer)));
}
/**/

/*** Pointer_zero() ***/
/** Pointer_zero is not supported. */
/**/

/*** declareBlock() ***/
typedef void* PointerToken;
/**/

/*** funcDeclareBlock() ***/
Token* Pointer_new(void *i);
/**/

