/***declareBlock***/
typedef double DoubleToken;
/**/

/***funcDeclareBlock***/
Token Double_convert(Token token);
Token Double_print(Token thisToken);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token Double_new(double d) {
    Token *result = (Token *) malloc(sizeof(Token));
    result->type = TYPE_Double;
    result->payload.Double = d;
    return *result;
}

/* Token Double_new(double d) { */
/*     Token result; */
/*     result.type = TYPE_Double; */
/*     result.payload.Double = d; */
/*     return result; */
/* } */
/**/


/***deleteBlock***/
Token Double_delete(Token token) {
    free(token);
}    
/**/


/***convertBlock***/
Token Double_convert(Token token) {
    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                token.type = TYPE_Double;
                token.payload.Double = (double) token.payload.Int;
                break;
        #endif

        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }
    token.type = TYPE_Double;
    return token;
}
/**/

/***printBlock***/
Token Double_print(Token thisToken) {
    printf("%g", thisToken.payload.Double);
}
/**/
