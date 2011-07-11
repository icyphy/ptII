/*** preinitializeFiringCountLimitBlock() ***/
$super()
/**/

/***fireBlock***/
{
    $targetType(output) $actorSymbol(temp);
    $actorSymbol(temp) = $val(($cgType(output)) value);
    $put(output, $actorSymbol(temp));
}
/**/

/*** postfireFiringCountLimitBlock() ***/
$super()
/**/
