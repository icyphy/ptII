/*** sharedBlock ***/
component pt_mux2 is
	generic
	(
		WIDTH				:	integer		:= 15;
		RESET_ACTIVE_VALUE	:	std_logic	:= '0';
		LATENCY				: 	integer		:= 3
	) ;
	port
	(
		clk				: IN std_logic;
		reset			: IN std_logic;
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		sel				: IN std_logic;
		output			: OUT std_logic_vector (WIDTH-1 DOWNTO 0)
	) ;
end component pt_mux2;
/**/


/*** sharedBlock_lat0 ***/
component pt_mux2_lat0 is
	generic
	(
		WIDTH				:	integer		:= 15;
	) ;
	port
	(
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		sel				: IN std_logic;
		output			: OUT std_logic_vector (WIDTH-1 DOWNTO 0)
	) ;
end component pt_mux2;
/**/



/*** fireBlock ($component, $width, $latency) ***/
$actorSymbol(instance): $component
	generic map
	(
		WIDTH				=> $width,
		RESET_ACTIVE_VALUE	=> '0',
		LATENCY				=> $latency
	) 
	port
	(
		A				=> $ref(A),
		B				=> $ref(B),
		sel				=> $ref(select),
		output			=> $ref(output),
		clk			    => clk,
		reset			=> reset
	) ;
/**/

/*** fireBlock_lat0 ($component, $width) ***/
$actorSymbol(instance): $component
	generic map
	(
		WIDTH				=> $width
	) 
	port
	(
		A				=> $ref(A),
		B				=> $ref(B),
		sel				=> $ref(select),
		output			=> $ref(output)
	) ;
/**/

