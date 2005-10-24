/*** sharedBlock ***/
void Display_print(Token* token) {
    int i, j;
    char type = token->type;
    switch (type) {
    case INT_TYPE:
        printf("%d", ((Int*) token)->value);
        break;
    case DOUBLE_TYPE:
        printf("%f", ((Double*) token)->value);
        break;
    case STRING_TYPE:
        printf("\"%s\"", ((String*) token)->value);
        break;
    case ARRAY_TYPE:            
        printf("{");
        //printf("length: %d\n", ((Array*) data)->length);
        for (i = 0; i < ((Array*) token)->size; i++) {
            if (i != 0) {
                printf(", ");
            }
            Display_print(((Array*) token)->elements[i]);
        }
        printf("}");
        break;
    case MATRIX_TYPE:
        printf("[");
        for (i = 0; i < ((Matrix*) token)->row; i++) {
            if (i != 0) {
                printf("; ");
            }            
            for (j = 0; j < ((Matrix*) token)->column; j++) {
                if (j != 0) {
                    printf(", ");
                }
                Display_print(((Matrix*) token)->elements[i * ((Matrix*) token)->column + j]);
            }
        }
        printf("]");
        break;
    }
}

/**/


/*** printInt(<channel>) ***/
    printf("Display: %d\n", $ref(input#<channel>).intPort);
/**/

/*** printDouble(<channel>) ***/
    printf("Display: %g\n", $ref(input#<channel>).doublePort);
/**/

/*** printString(<channel>) ***/
    printf("Display: %s\n", $ref(input#<channel>).stringPort);
/**/

/*** printToken(<channel>) ***/
    printf("Display: ");
    $typeFunc($ref(input)[<channel>].generalPort, print());
    printf("\n");
/**/
