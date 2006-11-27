/*** preinitBlock($ports, $signals, $components, $instantiation) ***/
entity $actorSymbol() is
$ports
end entity

architecture composite of $actorSymbol() is

$signals
$components

begin

$instantiation

end architecture composite
/**/



/*** fireBlock ($genericMap, $portMap) ***/
	component $actorSymbol() is
	end component
	
	$actorSymbol(instance): $actorSymbol()
		GENERIC MAP ( $genericMap )
		PORT MAP ( $portMap );		
/**/
