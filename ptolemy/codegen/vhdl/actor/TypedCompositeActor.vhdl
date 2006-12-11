/*** preinitBlock($ports, $signals, $components, $instantiation) ***/
// preinit
entity $actorSymbol() is
	port
	(
		$ports
		clk	: in std_logic
	) ;
end entity;


// main entry
architecture composite of $actorSymbol() is
	// init
	$signals
	$components

begin

	// fire
	$instantiation

// exit
end architecture composite;


/**/





/*** shareBlock ***/
	component $actorSymbol() is
	end component;
/**/






/*** fireBlock ($genericMap, $portMap) ***/
	
	$actorSymbol(instance): $actorSymbol()
		GENERIC MAP ( $genericMap )
		PORT MAP ( $portMap );		
/**/
