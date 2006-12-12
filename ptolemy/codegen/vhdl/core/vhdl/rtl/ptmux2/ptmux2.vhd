--Ptolemy VHDL Code Generation Core Library
--pt_mux2: 	
--2 input multiplexor
--Latency programmable.
--
--Posedge clock.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use work.pt_utility.all;

--type COMPARETYPE is (GEQ,LEQ,EQ,G,L,NEQ);

entity pt_mux2 is
	generic
	(
		WIDTH		:	integer		:= 15;
		RESET_ACTIVE_VALUE	:	std_logic	:= '0';
		LATENCY				: 	integer		:= 3
	) ;
	port
	(
		clk				: IN std_logic;
		reset			: IN std_logic;
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		sel				: IN std_logic;
		output			: OUT std_logic_vector (WIDTH-1 DOWNTO 0)
	) ;
end pt_mux2;


ARCHITECTURE behave OF pt_mux2 IS
--Constants
--Type Declarations
TYPE DELAYLINE is ARRAY (1 to LATENCY) of std_logic_vector (WIDTH-1 DOWNTO 0);

--Signal Declarations
SIGNAL delay : DELAYLINE;	 

BEGIN
OUTPUT <= delay(LATENCY);
mux : process(clk)
begin
	if clk'event and clk = '1' then
		if reset = RESET_ACTIVE_VALUE then
			delay <= (others=>(others=>'0'));
		else
			if sel = '0' then
				delay(1) <= A;
			elsif sel = '1' then
				delay(1) <= B;
			else
				delay(1) <= (others=>'U');
			end if;			
			if LATENCY > 1 then
				for i in 1 to LATENCY-1 loop
					delay(i+1)<=delay(i);
				end loop;	
			end if;	
		end if;
	end if;	
end process mux ;
END behave ;

