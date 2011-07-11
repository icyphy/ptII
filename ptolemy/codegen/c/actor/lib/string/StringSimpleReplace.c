/*** sharedBlock ***/

/** For every instance of pattern in stringToEdit, replace pattern with
 *  replacement.
 *  A new string is returned, it is up to the caller to free() it.
 */
char *replaceString(char *stringToEdit, char *pattern, char *replacement) {
    size_t size = 10;
    char * output = NULL;

    char *p;
    char *start = stringToEdit;

    if (! (p = strstr(start, pattern))) {
        return stringToEdit;
    }

    output = (char *) malloc(size);
    output[0] = '\0';

    while (p != NULL) {
        if (strlen(output) + p - start + 1 >= size) {
            size = (strlen(output) + p - start + 1) * 2;
            output = (char *) realloc(output, size);
        }
        strncat(output, start, p - start);

        if (strlen(output) + strlen(replacement) + 1 >= size) {
            size = (strlen(output) + strlen(replacement) + 1) * 2;
            output = (char *) realloc(output, size);
        }
        strncat(output, replacement, size - strlen(output) - 1);

        start = p + strlen(pattern);
        p = strstr(start, pattern);
    }
    if (strlen(output) + strlen(start) + 1 >= size) {
        size = strlen(output) + strlen(start) + 1;
        output = (char *) realloc(output, size);
    }
    strncat(output, start, size - strlen(output) - 1);

  return output;
}

//int main(int argc, char ** argv) {
//    printf("%s\n", replaceString("This is a test", "t", "pt"));
//
//}

/**/

/*** fireBlock ***/
$ref(output) = replaceString($ref(stringToEdit), $ref(pattern), $ref(replacement));
/**/

