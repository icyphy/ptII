/*** allocNewArray($i, $length) ***/
int $actorSymbol(j);
Token $actorSymbol(valueArray);
// FIXME: int only supported here, need to support other types
int $actorSymbol(initialOutputsArray)[$length] = $val(initialOutputs, $i);

$actorSymbol(valueArray) = $new(Array($length, 0));
for ($actorSymbol(j) = 0; $actorSymbol(j) < $length; $actorSymbol(j)++) {
    Array_set($actorSymbol(valueArray),$actorSymbol(j), 
            Int_new($actorSymbol(initialOutputsArray)[$actorSymbol(j)]));
}        
$ref(output, j) = $actorSymbol(valueArray);
/**/

/***initProductionBlock($offset)***/
$ref(output, $offset) = $val(initialOutputs, $offset);
/**/

/***fireBlock***/
$ref(output) = $ref(input);
/**/
