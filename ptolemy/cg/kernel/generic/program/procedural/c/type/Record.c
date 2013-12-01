/*** Record_add() ***/
Token* Record_add(Token* thisToken, ...) {
//    int i, j;
//    va_list argp;
//    Token* result;
//    Token* otherToken;
//
//    va_start(argp, thisToken);
//    otherToken = va_arg(argp, Token*);
//
//    result = $new(Record());
//
//    for (i = 0; i < thisToken->payload.Matrix->column; i++) {
//        for (j = 0; j < thisToken->payload.Matrix->row; j++) {
//            Matrix_set(result, j, i, functionTable[(int)Matrix_get(thisToken, i, j)->type][FUNC_add](Matrix_get(thisToken, i, j), Matrix_get(otherToken, i, j)));
//        }
//    }
//
//    va_end(argp);
//    return result;
        return NULL;
}
/**/

/***Record_convert()***/
Token* Record_convert(Token* token, ...) {
        return token;
//        switch (token->type) {
//                // FIXME: not finished
//            default:
//                fprintf(stderr, "Record_convert(): Conversion from an unsupported type. (%d)\n", token->type);
//                exit(-1);
//                break;
//        }
//        token->type = TYPE_Record;
//        return token;
}
/**/
/*** Record_new() ***/
Token* Record_new(Time time, int microstep, Token* t, ...) {
        Token* result = malloc(sizeof(Token));

        result->type = TYPE_Record;
        result->payload.Record = (RecordToken) malloc(sizeof(struct record));
        result->payload.Record->timestamp = time;
        result->payload.Record->microstep = microstep;

        result->payload.Record->payload = malloc(sizeof(Token));
        // FIXME: should this be a copy?
        result->payload.Record->payload = t;

        return result;
}
/**/

/*** declareBlock() ***/
struct record {
    Time timestamp;               // timestamp
    int microstep;                 // microstep
    Token* payload;         // the actual payload
};

typedef struct record* RecordToken;
/**/

/*** funcDeclareBlock() ***/
Token* Record_new(double timestamp, int microstep, Token* t, ...);
/**/

/*** funcImplementationBlock() ***/
/**/
