
/*** sharedBlock ***/
component pt_ufixed_const is
	generic
	(
		CONST_HIGH		:	integer		:= 15;
		CONST_LOW		:	integer		:= 0;
		CONST_VALUE		:	real			:= 0.125
	) ;
	port
	(
		output			: OUT std_logic_vector (CONST_HIGH-CONST_LOW DOWNTO 0) 
	) ;
end component pt_ufixed_const;
/**/

/*** fireBlock ($high, $low, $fixValue) ***/

$actorSymbol(instance): pt_ufixed_const
	GENERIC MAP ( 
		CONST_HIGH => $high, 
		CONST_LOW => $low,
		CONST_VALUE => $fixValue
	)
	PORT MAP ( 
		output => $ref(output)
	);
/**/