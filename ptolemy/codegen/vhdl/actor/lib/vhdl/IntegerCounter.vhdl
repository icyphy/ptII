/*** sharedBlock ***/
component ptcounter is
	GENERIC (
		WIDTH 				: INTEGER := 32;     
		USE_ENABLE			: boolean := TRUE;
		RESET_ACTIVE_VALUE	: std_logic := '0';
		ENABLE_ACTIVE_VALUE	: std_logic := '0';
		WRAP				: boolean := TRUE
	);
	PORT (
		clk 	: IN std_logic ;
		reset	: IN std_logic ;
		enable	: IN std_logic ;
		output	: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	);
end component ptcounter;
/**/

/*** fireBlock ($width, $useEnable, $wrap, $enableSignal) ***/
$actorSymbol(instance): ptcounter 
	GENERIC MAP ( 
		WIDTH => $width, 
		USE_ENABLE => $useEnable; 
		RESET_ACTIVE_VALUE	=> ;
		ENABLE_ACTIVE_VALUE	=> ;
		WRAP => $wrap
	)

	PORT MAP ( 
		clk => clk,
		reset => reset,
		enable => $enableSignal,
		output => $ref(output)
	);
/**/