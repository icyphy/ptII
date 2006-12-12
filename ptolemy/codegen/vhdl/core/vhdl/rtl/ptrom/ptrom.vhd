--Ptolemy VHDL Code Generation Core Library
--ptrom_sfixed: Addressable read only memory.
--Uses active low synchronous reset
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use work.pt_utility.all;

entity ptrom is
	generic
	(
		FIXED_HIGH			: INTEGER := 16;
		FIXED_LOW			: INTEGER := -15;
		ADDR_BITS			: INTEGER := 4;
		DEPTH				: INTEGER := 16;
		VALUES				: CORRECTVALUES;
		LATENCY				: INTEGER := 1;
		RESET_ACTIVE_VALUE	: std_logic := '0'
	);
	port
	(
		clk 		: IN std_logic ;
		reset		: IN std_logic ;
		addr		: IN std_logic_vector (FIXED_HIGH-FIXED_LOW DOWNTO 0);
		output		: OUT std_logic_vector (FIXED_HIGH-FIXED_LOW DOWNTO 0) 
	);
end ptrom;

architecture behaviour of ptrom is
type ROMVALS is array (1 to DEPTH) of sfixed(FIXED_HIGH DOWNTO FIXED_LOW); 

signal rom : ROMVALS ;

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
