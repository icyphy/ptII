/*** sharedBlock ***/
component pttest is
	generic
	(
		LENGTH		:	integer := 3;
		INPUT_HIGH	:	integer	:= 0;
		INPUT_LOW	:	integer := -15;
		LIST		:	CORRECTVALS;
		RESET_ACTIVE_VALUE : std_logic := '0';
		FIXED_SIGN	:	FIXED_TYPE_SIGN := SIGNED_TYPE	
	) ;
	port
	(
		clk			:	in 	std_logic ;
		reset		:  	in  std_logic ;
		data_in		:	in	std_logic_vector (INPUT_HIGH-INPUT_LOW DOWNTO 0)
	) ;
end component pttest;
/**/


/*** fireBlock ($high, $low, $values, $signed) ***/
$actorSymbol(instance): pttest
	GENERIC MAP ( 
		LENGTH		=> $size(correctValues),
		INPUT_HIGH	=> $high,
		INPUT_LOW	=> $low,
		LIST		=> ($values),
		RESET_ACTIVE_VALUE => '0',
		FIXED_SIGN	=> $signed
	)
	PORT MAP ( 
		clk	=> clk,
		reset => reset,
		data_in => $ref(input)
	);
/**/