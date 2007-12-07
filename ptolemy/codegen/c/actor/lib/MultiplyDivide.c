/*** preinitBlock ***/
//int $actorSymbol(i);
$targetType(output) $actorSymbol(numerator);
$targetType(output) $actorSymbol(denominator);
/**/

/***SetNumeratorBlock***/
$actorSymbol(numerator) = $ref(multiply#0);
/**/

/***SetNumeratorBlock ($type) ***/
$actorSymbol(numerator) = $new($type($ref(multiply#0)));
/**/


/***SetNumeratorOneBlock***/
$actorSymbol(numerator) = 1;
/**/

/***TokenSetNumeratorOneBlock***/
$actorSymbol(numerator) = $tokenFunc($ref(divide#0)::one());
/**/

/***SetDenominatorBlock***/
$actorSymbol(denominator) = $ref(divide#0);
/**/

/***SetDenominatorBlock ($type) ***/
$actorSymbol(denominator) = $new($type($ref(divide#0)));
/**/





/***MultiplyBlock($channel)***/
$actorSymbol(numerator) *= $ref(multiply#$channel);
/**/

/***TokenMultiplyBlock($channel)***/
$actorSymbol(numerator) = $tokenFunc($actorSymbol(numerator)::multiply($ref(multiply#$channel)));
/**/

/***TokenMultiplyBlock($channel, $type)***/
$actorSymbol(numerator) = $tokenFunc($actorSymbol(numerator)::multiply($new($type($ref(multiply#$channel)))));
/**/

/***DivideBlock($channel)***/
$actorSymbol(denominator) *= $ref(divide#$channel);
/**/

/***TokenDivideBlock($channel)***/
$actorSymbol(denominator) = $tokenFunc($actorSymbol(denominator)::divide($ref(divide#$channel)));
/**/

/***TokenDivideBlock($channel, $type)***/
$actorSymbol(denominator) = $tokenFunc($actorSymbol(denominator)::divide($new($type($ref(divide#$channel)))));
/**/




/***OutputBlock***/
$ref(output) = $actorSymbol(numerator) / $actorSymbol(denominator);
/**/

/***NumeratorOutputBlock***/
$ref(output) = $actorSymbol(numerator);
/**/

/***TokenOutputBlock***/
$ref(output) = $tokenFunc($actorSymbol(numerator)::divide($actorSymbol(denominator)));
/**/











/***NumericFireBlock***/
$ref(output) = $ref(multiply#0);
for ($actorSymbol(i) = 1; $actorSymbol(i) < $size(multiply); $actorSymbol(i)++) {
    $ref(output) *= $ref(multiply#$actorSymbol(i));
}
for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(divide); $actorSymbol(i)++) {
    $ref(output) /= $ref(divide#$actorSymbol(i));
}
/**/

/***TokenFireBlock($width)***/
$ref(output) = $ref(multiply#0);
        
for ($actorSymbol(i) = 1; $actorSymbol(i) < $size(multiply); $actorSymbol(i)++) {
    $tokenFunc($ref(output)::multiply($ref(multiply#$actorSymbol(i))));
}
for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(divide); $actorSymbol(i)++) {
    $ref(output) = $tokenFunc($ref(output)::divide($ref(divide#$actorSymbol(i))));
}
/**/
