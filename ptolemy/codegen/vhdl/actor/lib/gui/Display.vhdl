
/*** sharedBlock ***/
component ptdisplay is
	generic
	(
		CONST_HIGH		:	integer		:= 15;
		CONST_LOW		:	integer		:= 0;
	) ;
	port
	(
		input			: IN std_logic_vector (CONST_HIGH-CONST_LOW DOWNTO 0) 
	) ;
end component ptdisplay;
/**/

/*** fireBlock ($high, $low, $fixValue) ***/
$actorSymbol(instance): ptdisplay
	GENERIC MAP ( 
		CONST_HIGH => $high, 
		CONST_LOW => $low,
	)
	PORT MAP ( 
		input => $ref(input)
	);
/**/