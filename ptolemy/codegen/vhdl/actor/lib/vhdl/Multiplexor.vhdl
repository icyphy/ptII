/*** preinitBlock ***/

/**/

/*** sharedBlock ***/
/**/

/*** fireBlock ***/
process($refList(input), $actorSymbol(S))
begin
	case $ref(select) is
	  	for i in 0 to $size(input) loop
	  		when i then
		    	$ref(output) <= $ref(input#i);
		end loop;
	end case;
end process;
/**/
