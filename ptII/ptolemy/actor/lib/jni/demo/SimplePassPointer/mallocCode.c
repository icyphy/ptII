/***fileDependencies***/
/**/

/***preinitBlock***/
int * $actorSymbol(ptr);
/**/

/***initBlock***/
$actorSymbol(ptr) = (int *) malloc(sizeof(int));
*$actorSymbol(ptr) = 199;
/**/

/***fireBlock***/
$ref(ptr) = $actorSymbol(ptr);
$ref(value) = *$actorSymbol(ptr);
/**/

/***wrapupBlock***/
/**/

