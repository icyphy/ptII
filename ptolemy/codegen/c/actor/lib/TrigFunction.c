/***codeBlock1*/
$ref(output) = 	(!strcmp($val(function), "sin")) ? sin($ref(input)) : 
           		(!strcmp($val(function), "cos")) ? cos($ref(input)) : 
           		(!strcmp($val(function), "tan")) ? tan($ref(input)) : 
           		(!strcmp($val(function), "asin")) ? asin($ref(input)) : 
           		(!strcmp($val(function), "acos")) ? acos($ref(input)) : 
           		atan($ref(input));
/**/