/*** dontIgnoreCaseContainsBlock ***/
$put(output, $get(firstString).contains($get(secondString)));
/**/

/*** ignoreCaseContainsBlock ***/
$put(output, $get(firstString).toLowerCase().contains($get(secondString).toLowerCase()));
/**/


/*** dontIgnoreCaseEndsWithBlock ***/
$put(output, $get(firstString).endsWith($get(secondString)));
/**/

/*** ignoreCaseEndsWithBlock ***/
$put(output, $get(firstString).toLowerCase().endsWith($get(secondString).toLowerCase()));
/**/

/*** dontIgnoreCaseEqualsBlock ***/
$put(output, $get(firstString).equals($get(secondString)));
/**/

/*** ignoreCaseEqualsBlock ***/
$put(output, $get(firstString).equalsIgnoreCase($get(secondString)));
/**/

/*** dontIgnoreCaseStartsWithBlock ***/
$put(output, $get(firstString).startsWith($get(secondString)));
/**/

/*** ignoreCaseStartsWithBlock ***/
$put(output, $get(firstString).toLowerCase().startsWith($get(secondString).toLowerCase()));
/**/
