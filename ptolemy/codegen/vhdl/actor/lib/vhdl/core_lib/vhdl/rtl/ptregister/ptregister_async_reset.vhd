--Ptolemy VHDL Code Generation Core Library
--ptregister: Flip flop based register block.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;

entity ptregister_async_reset is
	generic
	(
		RESET_ACTIVE_VALUE	: std_logic := '0' ;
		WIDTH 				: INTEGER := 32      --width of the register in bits.
	);
	port
	(
		clk 	: IN std_logic ;
		reset	: IN std_logic ;
		D		: IN std_logic_vector (WIDTH-1 DOWNTO 0);
		Q		: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	);
end ptregister_async_reset;

architecture behaviour of ptregister_async_reset is

begin
	reg : process(clk,reset)
	begin
		if reset=RESET_ACTIVE_VALUE then
			Q <= (OTHERS => '0');
		elsif clk'event and clk = '1' then
			Q <= D;
		end if;
	end process reg ;
end behaviour;
