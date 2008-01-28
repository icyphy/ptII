/*** preinitBlock ***/
int $actorSymbol(i);
int $actorSymbol(length);
boolean $actorSymbol(doDelete) = false;
Token $actorSymbol(unsignedByteArray);
/**/

/*** fireBlock***/
if ($actorSymbol(doDelete)) {
    Array_delete($actorSymbol(unsignedByteArray));
} else {
    $actorSymbol(doDelete) = true;
}

$actorSymbol(length) = strlen($ref(input));
$actorSymbol(unsignedByteArray) = $new(Array($actorSymbol(length), 0));
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
    $actorSymbol(unsignedByteArray).payload.Array->elements[$actorSymbol(i)] = $new(UnsignedByte((unsigned char) $ref(input)[$actorSymbol(i)]));
}
$ref(output) = $actorSymbol(unsignedByteArray);
/**/
