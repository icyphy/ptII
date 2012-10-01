


/***mergeBlock($channel)***/
if ($hasToken(input#$channel)) {
        $put(output, $get(input#$channel));
}
/**/
