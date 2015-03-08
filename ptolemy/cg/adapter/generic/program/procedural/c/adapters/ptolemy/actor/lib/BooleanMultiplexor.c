/***fireBlock($type)***/
// This fire method assumes there is an input tokens at each one of the three
// input ports when this method is envoked.
if ($get(select)) {
        $put(output, ($targetType(output)) $get(trueInput));        //output true token
} else {
        $put(output, ($targetType(output)) $get(falseInput));        //output false token
}
/**/
