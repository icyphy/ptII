/*** sharedBlock ***/
component ptlogic is
	generic
	(
		WIDTH				:	integer		:= 15;
		LOGICOP				:	LOGICTYPE	:= PT_AND;
		RESET_ACTIVE_VALUE	:	std_logic	:= '0';
		LATENCY				: 	integer		:= 3
	) ;
	port
	(
		clk				: IN std_logic;
		reset			: IN std_logic;
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		OUTPUT			: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	) ;
end component ptlogic;
/**/


/*** sharedBlock_lat0 ***/
component ptlogic_lat0 is
	generic
	(
		WIDTH				:	integer		:= 15;
		LOGICOP				:	LOGICTYPE	:= PT_AND
	) ;
	port
	(
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		OUTPUT			: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	) ;
end component ptlogic_lat0;
/**/



/*** fireBlock ($operationType, $width, $logicop, $latency, $clk) ***/
$actorSymbol(instance): $operationType
	GENERIC MAP ( 
		WIDTH => $width, 
		LOGICOP => $logicop,
		$latency
	)
	PORT MAP ( 
		A => $ref(A),
		B => $ref(B),
		output => $ref(output)$clk
	);
/**/