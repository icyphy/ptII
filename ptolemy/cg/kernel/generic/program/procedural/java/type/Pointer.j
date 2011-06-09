/***declareBlock***/
typedef void* PointerToken;
/**/

/***funcDeclareBlock***/
static Token Pointer_new(void *i);
/**/


/***Pointer_new***/
static Token Pointer_new(void *i) {
    Token result;
    result.type = TYPE_Pointer;
                result.payload.Pointer = (PointerToken) malloc(sizeof(void *));
    result.payload.Pointer = i;
    return result;
}
/**/

/***Pointer_delete***/
static Token Pointer_delete(Token thisToken, Token... tokens) {
    free(thisToken.payload.Pointer);
    return emptyToken;
}
/**/

/***Pointer_equals***/
static Token Pointer_equals(Token thisToken, Token... tokens) {
    va_list argp;
    Token otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token);

    if (otherToken.type != TYPE_Pointer) {
        otherToken = Pointer_convert(otherToken);
    }

    va_end(argp);

    return Boolean_new(
        (int) thisToken.payload.Pointer == (int) otherToken.payload.Pointer);
}
/**/

/***Pointer_isCloseTo***/
static Token Pointer_isCloseTo(Token thisToken, Token... tokens) {
    /** Pointers are never close to each other. */
    return Boolean_new(false);
}
/**/

/***Pointer_print***/
static Token Pointer_print(Token thisToken, Token... tokens) {
    printf("Pointer at %o", (int) thisToken.payload.Pointer);
}
/**/

/***Pointer_toString***/
static Token Pointer_toString(Token thisToken, Token... tokens) {
    char* string = (char*) malloc(sizeof(char) * 32);
    sprintf(string, "Object at %.22o", (int) thisToken.payload.Pointer);

    return String_new(string);
}
/**/

/***Pointer_add***/
/** Pointer_add is not supported. */
/**/

/***Pointer_subtract***/
/** Pointer_subtract is not supported. */
/**/

/***Pointer_multiply***/
/** Pointer_multiply is not supported. */
/**/

/***Pointer_divide***/
/** Pointer_divide is not supported. */
/**/

/***Pointer_negate***/
/** Pointer_negate is not supported. */
/**/

/***Pointer_zero***/
/** Pointer_zero is not supported. */
/**/

/***Pointer_one***/
/** Pointer_one is not supported. */
/**/

/***Pointer_clone***/
static Token Pointer_clone(Token thisToken, Token... tokens) {
    return Pointer_new(thisToken.payload.Pointer);
}
/**/

---------------- static functions -----------------------

/***Pointer_convert***/
static Token Pointer_convert(Token token, Token... tokens) {
    switch (token.type) {

#ifdef TYPE_Object
    case TYPE_Object:
        token.payload.Pointer = token.payload.Object;
        break;
#endif

        // FIXME: not finished
    default:
        fprintf(stderr, "Pointer_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.type = TYPE_Pointer;
    return token;
}
/**/

