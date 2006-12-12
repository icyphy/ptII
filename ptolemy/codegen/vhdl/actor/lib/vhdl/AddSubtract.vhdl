/*** sharedBlock ($operationType) ***/
component $operationType is
	generic
	(
		INPUTA_HIGH    		:    integer    := 15;
		INPUTA_LOW     		:    integer    := 0;
		INPUTB_HIGH    		:    integer    := 15;
		INPUTB_LOW     		:	 integer    := 0;
		OUTPUT_HIGH    		:    integer    := 15;
		OUTPUT_LOW     		:    integer    := 0;	
		RESET_ACTIVE_VALUE	:	 std_logic	:= '0';
		LATENCY        		:    integer    := 3
	) ;
	port
	(
		clk       : IN std_logic;
		reset	  : IN std_logic;
		A         : IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B         : IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		output    : OUT std_logic_vector (OUTPUT_HIGH - OUTPUT_LOW DOWNTO 0) 
	) ;
end component $operationType;
/**/


/*** sharedBlock_lat0($operationType) ***/
component $operationType is
	generic
	(
		INPUTA_HIGH         :    integer    := 15;
		INPUTA_LOW			:    integer    := 0;
		INPUTB_HIGH			:    integer    := 15;
		INPUTB_LOW			:    integer    := 0;
		OUTPUT_HIGH			:    integer    := 15;
		OUTPUT_LOW			:    integer    := 0		
	) ;
	port
	(
		A				: IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B				: IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		output			: OUT std_logic_vector (OUTPUT_HIGH - OUTPUT_LOW DOWNTO 0) 
	) ;
end component $operationType;
/**/



/*** fireBlock ($operationType, $highA, $lowA, $highB, $lowB, $highO, $lowO, $latency, $clk) ***/
$actorSymbol(instance): $operationType
	GENERIC MAP ( 
		INPUTA_HIGH => $highA, 
		INPUTA_LOW => $lowA,
		INPUTB_HIGH => $highB, 
		INPUTB_LOW => $lowB,
		OUTPUT_HIGH => $highO,
		OUTPUT_Low => $lowO
		$latency
	)
	PORT MAP ( 
		A => $ref(A),
		B => $ref(B),
		output => $ref(output)
		$clk
	);
/**/