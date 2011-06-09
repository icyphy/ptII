/*** fireBlock($channel) ***/
if ($hasToken(input#$channel)) {
        $put(output#$channel, $get(input#$channel));
}
/**/

