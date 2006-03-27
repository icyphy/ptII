/***declareBlock***/
typedef char* StringToken;
/**/

/***funcDeclareBlock***/
Token String_convert(Token token, ...);
Token String_print(Token thisToken, ...);
Token String_toString(Token thisToken, ...);
Token String_toExpression(Token thisToken, ...);
Token String_equals(Token thisToken, ...);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token String_new(char* s) {
    Token result;
    result.type = TYPE_String;
    result.payload.String = s;
    return result;
}
/**/


/***deleteBlock***/
Token String_delete(Token token) {   
    free(token.payload.String);    
    free(&token);
}    
/**/

/***equalsBlock***/
// boolean equals(Token, Token);
Token String_equals(Token thisToken, ...) {
    va_list argp; 
    va_start(argp, thisToken);
	Token otherToken = va_arg(argp, Token);
	return Boolean_new(!strcmp(thisToken.payload.String, otherToken.payload.String);
}
/**/


/***convertBlock***/
Token String_convert(Token token, ...) {
    char* stringPointer;
    switch (token.type) {
        #ifdef TYPE_Int
            case TYPE_Int:
                stringPointer = (char*) malloc(sizeof(char) * 12);
                sprintf(stringPointer, "%d", token.payload.Int);
                token.payload.String = stringPointer;
                break;
        #endif

        #ifdef TYPE_Double
            case TYPE_Double:
                stringPointer = (char*) malloc(sizeof(char) * 12);
                sprintf(stringPointer, "%g", token.payload.Double);
                token.payload.String = stringPointer;
                break;
        #endif

        default:
            // FIXME: not finished
            fprintf(stderr, "String_convert(): Conversion from an supported type. (%d)", token.type);
            break;
    }
    return token;
}    
/**/

/***printBlock***/
Token String_print(Token thisToken, ...) {
    printf("\"%s\"", thisToken.payload.String);
}
/**/

/***toStringBlock***/
Token String_toString(Token thisToken, ...) {
	// Guarrantee to return a new string.
	char* result = (char*) malloc(sizeof(char) * (1 + strlen(thisToken.payload.String)));
	strcpy(result, thisToken.payload.String);
	return String_new(result);
}
/**/

/***toExpressionBlock***/
Token String_toExpression(Token thisToken, ...) {
	char* result = (char*) malloc((5 + strlen(thisToken.payload.String)) * sizeof(char));
	sprintf(result, "\"%s\"", thisToken.payload.String);
	return String_new(result);
}
/**/
