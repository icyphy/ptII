/***declareBlock***/
typedef char BooleanToken;
Token* Boolean_convert(Token* token);
void Boolean_print(Token* thisToken);
/**/


/***newBlock***/
// make a new integer token from the given value.
Token* Boolean_new(char b) {
    Token* result = (Token*) malloc(sizeof(Token));
    result->type = TYPE_Boolean;
    result->payload.Boolean = b;
    return result;
}
/**/


/***deleteBlock***/
void Boolean_delete(Token* token) {   
    free(token);
}    
/**/


/***convertBlock***/
Token* Boolean_convert(Token* token) {
    switch (token->type) {
        // FIXME: not finished
        default:
            fprintf(stderr, "Convertion from a not supported type.");
            break;
    }    
    token->type = TYPE_Boolean;
    return token;
}    
/**/

/***printBlock***/
void Boolean_print(Token* thisToken) {
    printf((thisToken->payload.Boolean) ? "true" : "false");
}
/**/