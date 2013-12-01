/*** String_add() ***/
Token* String_add(Token* thisToken, ...) {
    va_list argp;
    va_start(argp, thisToken);
    Token* otherToken = va_arg(argp, Token*);

    char* result = (char*) malloc(sizeof(char) * (1 + strlen(thisToken->payload.String) + strlen(otherToken->payload.String)));
    strcpy(result, thisToken->payload.String);
    strcat(result, otherToken->payload.String);

    va_end(argp);
    return $new(String(result));
}
/**/

/*** String_clone() ***/
Token* String_clone(Token* thisToken, ...) {
    return $new(String(thisToken->payload.String));
}
/**/

/*** String_convert() ***/
Token* String_convert(Token* token, ...) {
    char* stringPointer;

    switch (token->type) {
#ifdef TYPE_Boolean
    case TYPE_Boolean:
        stringPointer = BooleantoString(token->payload.Boolean);
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
        stringPointer = InttoString(token->payload.Int);
        break;
#endif

#ifdef TYPE_Double
    case TYPE_Double:
        stringPointer = DoubletoString(token->payload.Double);
        break;
#endif

    default:
        // FIXME: not finished
        fprintf(stderr, "String_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        break;
    }
    token->payload.String = stringPointer;
    token->type = TYPE_String;
    return token;
}
/**/

/*** String_delete() ***/
Token* String_delete(Token* token, ...) {
    free(token->payload.String);
    /* We need to return something here because all the methods are declared
     * as returning a Token so we can use them in a table of functions.
     */
    return NULL;
}
/**/

/*** String_divide() ***/
/** String_divide is not supported. */
/**/

/*** String_equals() ***/
Token* String_equals(Token* thisToken, ...) {
    va_list argp;
    Token* otherToken;
    va_start(argp, thisToken);
    otherToken = va_arg(argp, Token*);

    va_end(argp);
    return $new(Boolean(!strcmp(thisToken->payload.String, otherToken->payload.String)));
}
/**/

/*** String_isCloseTo() ***/
/* No need to use String_isCloseTo(), we use String_equals() instead. */
/**/

/*** String_multiply() ***/
/** String_multiply is not supported. */
/**/

/*** String_negate() ***/
Token* String_negate(Token* thisToken, ...) {
    return emptyToken;
}
/**/

/*** String_new() ***/
/* Make a new integer token from the given value. */
Token* String_new(char* s) {
    Token* result = malloc(sizeof(Token));
    result->type = TYPE_String;
    if (!s) {
        result->payload.String = "";
    } else {
        result->payload.String = strdup(s);
    }
    return result;
}
/**/

/*** String_one() ***/
/** String_one is not supported. */
/**/

/*** String_print() ***/
Token* String_print(Token* thisToken, ...) {
    printf("\"%s\"", thisToken->payload.String);
    return emptyToken;
}
/**/

/*** String_subtract() ***/
/** String_subtract is not supported. */
/**/

/*** String_toString() ***/
Token* String_toString(Token* thisToken, ...) {
        return thisToken;
//    // Guarrantee to return a new string.
//    char* result = (char*) malloc(sizeof(char) * (3 + strlen(thisToken->payload.String)));
//    sprintf(result, "\"%s\"", thisToken->payload.String);
//    return $new(String(result));
}
/**/

/*** String_zero() ***/
Token* String_zero(Token* token, ...) {
    return $new(String(""));
}
/**/

/*** declareBlock() ***/
typedef char* StringToken;
/**/

/*** funcDeclareBlock() ***/
Token* String_new(char* s);
/**/

