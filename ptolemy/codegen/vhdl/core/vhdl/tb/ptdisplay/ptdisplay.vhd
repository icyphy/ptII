--Ptolemy VHDL Code Generation Core Library
--ptdisplay: Test Block for displaying fixed point tokens
--
--THIS IS A TEST BENCH MODULE AND WILL NOT SYNTHESIZE TO HARDWARE.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use ieee_proposed.standard_textio_additions.all;
use work.pt_utility.all;


entity ptdisplay is
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
end ptdisplay;

ARCHITECTURE behave OF ptdisplay IS

SIGNAL In_signed	:	sfixed(INPUT_HIGH DOWNTO INPUT_LOW);	 
SIGNAL In_unsigned	:	ufixed(INPUT_HIGH DOWNTO INPUT_LOW);	 

BEGIN

In_signed 	<= to_sfixed(input,INPUT_HIGH,INPUT_LOW);
In_unsigned <= to_ufixed(input,INPUT_HIGH,INPUT_LOW);

compare : process(clk)
	variable In_real		: real := 0.0;
begin
if clk'event and clk = '1' then
	if FIXED_SIGN = SIGNED_TYPE then
		In_real := to_real(In_signed); 
		assert false
		report ACTORNAME & "VALUE = " & real'image(In_real)
		severity note;
	elsif FIXED_SIGN = SIGNED_TYPE then
		In_real := to_real(In_unsigned); 
		assert false
		report ACTORNAME & "VALUE = " & real'image(In_real)
		severity note;
	end if;
end if;
end process compare ;
END behave ;

