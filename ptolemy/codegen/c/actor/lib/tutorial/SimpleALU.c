/***fireBlock***/
///////////////////////////SimpleALU fireBlock//////////////////////////
switch($ref(operation)) {
    case 0:
        $ref(output) = 0;
        break;
    case 1:
        $ref(output) = $ref(A) + $ref(B);
        break;
    case 2:
        $ref(output) = $ref(A) - $ref(B);
        break;
    case 3:
        $ref(output) = $ref(A) * $ref(B);
        break;
    case 4:
        $ref(output) = $ref(A) / $ref(B);
        break;
}
////////////////////////////////////////////////////////////////////////
/**/
