/*** sharedBlock ***/
component ptregister is
	generic
	(
		WIDTH 				: INTEGER := 32      --width of the register in bits.
	);
	port
	(
		clk 	: IN std_logic ;
		D		: IN std_logic_vector (WIDTH-1 DOWNTO 0);
		Q		: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	);
end component ptregister;
/**/

/*** fireBlock ($width) ***/
$actorSymbol(instance): ptregister
	GENERIC MAP ( 
		WIDTH => $width, 
	)
	PORT MAP ( 
		clk => clk,
		D	=> $ref(input),
		Q	=> $ref(output)
	);
/**/