/*** sharedBlock ***/
    // Shared block contains code that is shared by multiple instances
    // of the helper from the same type, so it should not contain any
    // actor specific marcos (e.g. actorSymbol, val, ref).
    // Any method or type declarations should be prefixed with the actor
    // type name followed by an underscore (e.g. ActorName_method).
/**/

/*** preinitBlock ***/
    int $actorSymbol(position) = 0;
/**/

/*** initBlock ***/
/**/

/*** fireBlock(<portWidth>) ***/
    $ref(output) = $ref(input)[$actorSymbol(position)++];
    if (position >= <portWidth>) {
        position = 0;
    }
/**/

/*** wrapupBlock ***/
/**/

