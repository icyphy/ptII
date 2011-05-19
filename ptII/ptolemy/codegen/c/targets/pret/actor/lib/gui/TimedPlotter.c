/***preinitBlock***/
static int $actorSymbol(xvalue);
/**/

/***initBlock***/
$actorSymbol(xvalue) = 0;
/**/

/***plotBlock($channel)***/
//check with Ben to see how to do the file mapped i/o... I don't remember how it was done before
printf("%d, %d",$actorSymbol(xvalue),$ref(input#$channel));

/**/
/***plotBlock1($channel)***/
//check with Ben to see how to do the file mapped i/o... I don't remember how it was done before
printf("%d, %d",$actorSymbol(xvalue),$ref(input#$channel)); 
/**/

/***plotBlock2($channel)***/
//check with Ben to see how to do the file mapped i/o... I don't remember how it was done before
printf("%d, %d",$actorSymbol(xvalue),$ref(input#$channel));
/**/