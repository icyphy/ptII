/***fireBlock***/
if ($hasToken(trueInput) && $hasToken(falseInput) && $hasToken(select)){
	if($get(select))
		$put(output, ($cgType(output)) $get(trueInput));	//output true token
	else
		$put(output, ($cgType(output)) $get(falseInput));	//output false token
}
/**/
