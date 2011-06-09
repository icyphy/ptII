/*** sharedBlock ***/
int $actorClass(charRead);
/**/

/***preinitBlock***/
FILE * $actorSymbol(filePtr);
int $actorSymbol(index);
int $actorSymbol(length) = 128;
/**/

/***initBufferBlock***/
$ref(output) = (char*) malloc($actorSymbol(length) * sizeof(char));
/**/


/***openForStdin***/
$actorSymbol(filePtr) = stdin;
/**/

/***skipLine***/
// use fgetc() to readLine
//$actorSymbol(charReturned) = fscanf($actorSymbol(filePtr), "%s", $actorSymbol(line));
while ( ($actorClass(charRead) = fgetc($actorSymbol(filePtr))) != '\n' && $actorClass(charRead) != EOF );
$ref(endOfFile) = feof($actorSymbol(filePtr) );
/**/

/***openForRead($fileName)***/
if (!($actorSymbol(filePtr) = fopen ("$fileName","r"))) {
    fprintf(stderr,"ERROR: cannot open file \"$fileName\" for LineReader actor.\n");
    exit(1);
}
/**/

/***fireBlock***/
//$actorSymbol(charReturned) = fscanf($actorSymbol(filePtr), "%s", $ref(output));
$actorSymbol(index) = 0;
do {
    $actorClass(charRead) = fgetc($actorSymbol(filePtr));
    if ($actorSymbol(index) >= $actorSymbol(length)) {
        $actorSymbol(length) *= 2;
        $ref(output) = (char*) realloc ($ref(output), ($actorSymbol(length) + 1) * sizeof(char));
        /* Solaris: strncpy does not add a null if only */
        /* n chars are copied. */
        $ref(output)[$actorSymbol(length)] = '\0';
    }
    $ref(output)[$actorSymbol(index)++] = $actorClass(charRead);
} while ( $actorClass(charRead) != '\n' && $actorClass(charRead) != EOF );
$ref(endOfFile) = feof($actorSymbol(filePtr) );
$ref(output)[$actorSymbol(index)++] = '\0';
/**/

/***closeFile***/
fclose($actorSymbol(filePtr));
/**/

/***postfireBlock***/
if ($ref(endOfFile)) {
    return false;
}
/**/

