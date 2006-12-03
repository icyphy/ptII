--Ptolemy VHDL Code Generation Core Library
--ptregister: Flip flop based register block.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;

entity ptregister is
	generic
	(
		WIDTH 				: INTEGER := 32      --width of the register in bits.
	);
	port
	(
		clk 	: IN std_logic ;
		D		: IN std_logic_vector (WIDTH-1 DOWNTO 0);
		Q		: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	);
end ptregister;

architecture behaviour of ptregister is

begin
	reg : process(clk)
	begin
		if clk'event and clk = '1' then
			Q <= D;
		end if;
	end process reg ;
end behaviour;
