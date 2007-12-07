/*** sharedBlock ***/
char* myStrtrim(char* result, char *str) {
        int length;
        if (!str) return 0;
        char *end = str + strlen(str);

        while(end-- > str)        {
                if(!strchr(" \t\n\r", *end))
                        break;
        }

        if (end - str + 1 != 0) {
                while(*str)        {
                        if(!strchr(" \t\n\r", *str))
                                break;
                        ++str;
                }
        }

        length = end - str + 1;
        result = (char*) realloc(result, sizeof(char) * (length + 1));
        strncpy(result, str, length);
        result[length] = '\0';
        return result;
}
/**/

/*** preinitBlock ***/
int $actorSymbol(i);
char* $actorSymbol(string) = NULL;
/**/

/*** toLowerCaseBlock ***/
$actorSymbol(string) = (char*) realloc ($actorSymbol(string), sizeof(char) * (1 + strlen($ref(input))));
for($actorSymbol(i) = 0; $ref(input)[ $actorSymbol(i) ]; $actorSymbol(i)++) {
        $actorSymbol(string)[$actorSymbol(i)] = tolower($ref(input)[ $actorSymbol(i) ]);
}
$actorSymbol(string)[$actorSymbol(i)] = '\0';
$ref(output) = $actorSymbol(string);
/**/

/*** toUpperCaseBlock ***/
$actorSymbol(string) = (char*) realloc ($actorSymbol(string), sizeof(char) * (1 + strlen($ref(input))));
for($actorSymbol(i) = 0; $ref(input)[ $actorSymbol(i) ]; $actorSymbol(i)++) {
        $actorSymbol(string)[$actorSymbol(i)] = toupper($ref(input)[ $actorSymbol(i) ]);
}
$actorSymbol(string)[$actorSymbol(i)] = '\0';
$ref(output) = $actorSymbol(string);
/**/

/*** trimBlock ***/
$ref(output) = myStrtrim($actorSymbol(string), $ref(input));
/**/

/*** wrapupBlock ***/
free($actorSymbol(string));
/**/
