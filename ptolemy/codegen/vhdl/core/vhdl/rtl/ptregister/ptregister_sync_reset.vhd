--Ptolemy VHDL Code Generation Core Library
--ptregister_sync_reset: Flip flop based register block.
--Uses active low synchronous reset
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;

entity ptregister_sync_reset is
	generic
	(
		WIDTH 				: INTEGER := 32;      --width of the register in bits.
		RESET_ACTIVE_VALUE	: std_logic := '0'
	);
	port
	(
		clk 	: IN std_logic ;
		reset	: IN std_logic ;
		D		: IN std_logic_vector (WIDTH-1 DOWNTO 0);
		Q		: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	);
end ptregister_sync_reset;

architecture behaviour of ptregister_sync_reset is

begin
	reg : process(clk)
	begin
		if clk'event and clk = '1' then
			if reset=RESET_ACTIVE_VALUE then
				Q	<= (others => '0');
			else	
				Q <= D;
			end if;	
		end if;
	end process reg ;
end behaviour;
