--Ptolemy VHDL Code Generation Core Library
--ptslice: Bit slice block.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;

entity ptslice is
	generic
	(
		INPUT_WIDTH 		: integer := 32; --width in number of bits
		OUTPUT_START 		: integer :=10;
		OUTPUT_END			: integer :=31;
	) ;
	port
	(
		input	:	IN std_logic_vector (INPUT_WIDTH-1 DOWNTO 0) ;	
		output	:	OUT std_logic_vector (OUTPUT_END-OUTPUT_START DOWNTO 0) 	
	) ;
end ptslice;

ARCHITECTURE behave OF ptslice IS
--Type definition for wires to connect the instances of ptregsiter.
--Wires to connect ptregisters
BEGIN

output <= input(OUTPUT_END DOWNTO OUTPUT_START);

END behave ;
