/*** initBlock ***/
$put(output, $new(Matrix($size(input), 1, 0));
/**/

/*** fireBlock($channel) ***/
//Matrix_set(DOLLERref(output, DOLLERchannel, 0, DOLLERref(input#DOLLERchannel));
$put(output, $channel, $get(input#$channel));
/**/

/*** fireBlock($channel, $type) ***/
//Matrix_set(DOLLERref(output), DOLLERchannel, 0, DOLLERnew(DOLLERtype(DOLLERref(input#DOLLERchannel))));
$put(output, $channel, $new($type($get(input#$channel))));
/**/
