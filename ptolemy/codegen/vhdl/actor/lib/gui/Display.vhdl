
/*** sharedBlock ***/
component ptdisplay is
GENERIC(
	INPUT_HIGH	:	integer	:= 0;
	INPUT_LOW	:	integer := -15;
	ACTORNAME	:	string;
	FIXED_SIGN	:	FIXED_TYPE_SIGN := SIGNED_TYPE
	);
PORT (
	clk			:	in std_logic ;
	input		:	in	std_logic_vector (INPUT_HIGH-INPUT_LOW DOWNTO 0)
);
end component ptdisplay;
/**/

/*** fireBlock ($high, $low, $name, $sign ) ***/
$actorSymbol(instance): ptdisplay
GENERIC MAP (
	INPUT_HIGH	=> $high,
	INPUT_LOW	=> $low,
	ACTORNAME	=> "$name",
	FIXED_SIGN	=> $sign
	)
PORT MAP(
	clk			=> clk,
	input		=> $ref(input)
);
/**/