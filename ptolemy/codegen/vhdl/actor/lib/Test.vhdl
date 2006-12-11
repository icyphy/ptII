/*** sharedBlock ***/
component pttest is
	generic
	(
		LENGTH		:	integer := 3;
		INPUT_HIGH	:	integer	:= 0;
		INPUT_LOW	:	integer := -15;
		LIST		:	CORRECTVALS;
		FIXED_SIGN	:	FIXED_TYPE_SIGN := SIGNED_TYPE	
	) ;
	port
	(
		clk			:	in 	std_logic ;
		data_in		:	in	std_logic_vector (INPUT_HIGH-INPUT_LOW DOWNTO 0)
	) ;
end component pttest;
/**/


/*** fireBlock ($high, $low, $values) ***/
$actorSymbol(instance): pttest
	GENERIC MAP ( 
		LENGTH		=> $size(correctValues),
		INPUT_HIGH	=> $high,
		INPUT_LOW	=> $low,
		LIST		=> ($values),
		FIXED_SIGN	=> UNSIGNED_TYPE
	)
	PORT MAP ( 
		clk	=> clk,
		data_in => $ref(input)
	);
/**/