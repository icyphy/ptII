/***fireBlock***/
if ($hasToken(trueInput) && $hasToken(falseInput) && $hasToken(select)){
	if($get(select)){
		$get(falseInput);		//discard false token
		$put(output, $val(($cgType(output)) $get(trueInput)));
	}
	else{
		$get(trueInput);		//discard true token
		$put(output, $val(($cgType(output)) $get(falseInput)));
	}
}
/**/
