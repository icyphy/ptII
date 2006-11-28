--Ptolemy VHDL Code Generation Core Library
--ptconcat2: 2 input concat.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;

entity ptconcat2 is
	generic
	(
		INPUTHIGH_WIDTH 		: integer := 32; --width in number of bits
		INPUTLOW_WIDTH 		: integer := 32; --width in number of bits
	) ;
	port
	(
		input_high	:	IN std_logic_vector (INPUTHIGH_WIDTH-1 DOWNTO 0) ;	
		input_low	:	IN std_logic_vector (INPUTLOW_WIDTH-1 DOWNTO 0) ;	
		output		:	OUT std_logic_vector (INPUTHIGH_WIDTH+INPUTLOW_WIDTH-1 DOWNTO 0) 	
	) ;
end ptconcat2;

ARCHITECTURE behave OF ptconcat2 IS
BEGIN

output(INPUTLOW_WIDTH-1 DOWNTO 0) <= input_low;
output(INPUTHIGH_WIDTH-1 DOWNTO INPUTLOW_WIDTH) <= input_high;

END behave ;
