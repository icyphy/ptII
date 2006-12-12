--Ptolemy VHDL Code Generation Core Library
--ptcounter: Unsigned integer UP counter.
--Provides active high synchronous reset and active high synchronous enable.
--The counter allows wrap or saturate on overflow.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_arith.all;

entity ptcounter is
	generic
	(
		WIDTH 				: INTEGER := 32;     
		USE_ENABLE			: boolean := TRUE;
		RESET_ACTIVE_VALUE	: std_logic := '0';
		ENABLE_ACTIVE_VALUE	: std_logic := '0';
		WRAP				: boolean := TRUE
	);
	port
	(
		clk 	: IN std_logic ;
		reset	: IN std_logic ;
		enable	: IN std_logic ;
		output	: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	);
end ptcounter;

architecture behaviour of ptcounter is

signal count 	: std_logic_vector (WIDTH-1 DOWNTO 0);
signal countup 	: std_logic_vector (WIDTH-1 DOWNTO 0);
signal max	: std_logic_vector (WIDTH-1 DOWNTO 0);

begin
	max <= (others => '1');
	countup <= count + 1;
	
	reg : process(clk)
	begin
		if clk'event and clk = '1' then
			if reset=RESET_ACTIVE_VALUE then
				count	<= (others => '0');
			else	
				if USE_ENABLE then
					if enable = ENABLE_ACTIVE_VALUE then
						if WRAP=false then
							if count = max then 
								count <= count;
							else
								count <= countup;
							end if;
						else
							count <= countup;
						end if;
					else
						count <= count;
					end if;	
				else
					if WRAP=false then
						if count = max then
							count <= count;
						else
							count <= countup;
						end if;
					else
						count <= countup;
					end if;
				end if;	
			end if;	
		end if;
	end process reg ;
end behaviour;
