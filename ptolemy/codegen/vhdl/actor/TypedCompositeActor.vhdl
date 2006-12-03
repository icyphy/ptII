/*** preinitBlock($ports, $signals, $components, $instantiation) ***/
entity $actorSymbol(block) is
	port
	(
$ports
		clk	: in std_logic
	) ;
end entity;

architecture composite of $actorSymbol(block) is

$signals
$components

begin

$instantiation

end architecture composite;
/**/


/*** shareBlock ***/
	component $actorSymbol(block) is
	end component;
/**/



/*** fireBlock ($genericMap, $portMap) ***/
	
	$actorSymbol(instance): $actorSymbol()
		GENERIC MAP ( $genericMap )
		PORT MAP ( $portMap );		
/**/
