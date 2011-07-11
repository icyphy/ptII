/***PrimitiveFireBlock***/
$put(output, $convert_$cgType(input)_Double($get(input))  < 0.0 ? -$get(input) : $get(input));
/**/

/***ComplexFireBlock***/
$put(output, Math.sqrt(((ComplexCG)($get(input).payload)).real * ((ComplexCG)($get(input).payload)).real + ((ComplexCG)($get(input).payload)).imag * ((ComplexCG)($get(input).payload)).imag));
/**/
