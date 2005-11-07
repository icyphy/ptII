/*** printInt(<channel>) ***/
    printf("Display: %d\n", $ref(input#<channel>));
/**/

/*** printDouble(<channel>) ***/
    printf("Display: %g\n", $ref(input#<channel>));
/**/

/*** printString(<channel>) ***/
    printf("Display: %s\n", $ref(input#<channel>));
/**/

/*** printToken(<channel>) ***/
    printf("Display: ");
    $typeFunc($ref(input)[<channel>], print());
    printf("\n");
/**/
