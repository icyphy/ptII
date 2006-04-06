/*** assignFireBlock($channel, $type) ***/
    $ref(output#$channel) = $ref(input, $channel);
/**/

/*** upgradeFireBlock($channel, $type) ***/
    $ref(output#$channel) = $new($type, $ref(input));
/**/

