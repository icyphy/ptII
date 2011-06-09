/*** fireBlock ($type)***/
if ($get(select)) {
   $put(output, $convert_$cgType(trueInput)_$cgType(output)($get(trueInput)));
} else {
   $put(output, $convert_$cgType(falseInput)_$cgType(output)($get(falseInput)));
}
/**/
