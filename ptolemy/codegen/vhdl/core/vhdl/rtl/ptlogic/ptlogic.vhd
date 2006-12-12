--Ptolemy VHDL Code Generation Core Library
--ptlogic: 	
--2 input logic function. 
--Latency programmable.
--Parametrizeable to any size fixed point operation.
--Posedge clock.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use work.pt_utility.all;

--type LOGICTYPE is (PT_OR,PT_AND,PT_XOR,PT_NAND,PT_NOR,PT_XNOR);

entity ptlogic is
	generic
	(
		WIDTH				:	integer		:= 15;
		LOGICOP				:	LOGICTYPE	:= PT_AND;
		RESET_ACTIVE_VALUE	:	std_logic	:= '0';
		LATENCY				: 	integer		:= 3
	) ;
	port
	(
		clk				: IN std_logic;
		reset			: IN std_logic;
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		OUTPUT			: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	) ;
end ptlogic;


ARCHITECTURE behave OF ptlogic IS
--Constants
--Type Declarations
TYPE DELAYLINE is ARRAY (1 to LATENCY) of std_logic_vector (WIDTH-1 DOWNTO 0);

--Signal Declarations
SIGNAL delay : DELAYLINE;	 
 

BEGIN
output <= delay(LATENCY);

compare : process(clk)
begin
	if clk'event and clk = '1' then
		if reset = RESET_ACTIVE_VALUE then
			delay <= (others=>(others=>'0'));
		else
			case LOGICOP is
				when PT_AND =>
					delay(1) <= A and B;	
				when PT_OR =>
					delay(1) <= A or B;	
				when PT_NAND =>
					delay(1) <= A nand B;	
				when PT_NOR =>
					delay(1) <= A nor B;	
				when PT_XOR =>
					delay(1) <= A xor B;	
				when PT_XNOR =>
					delay(1) <= A xnor B;	
				when others =>
					delay(1) <= A and B;
			end case;		
			if LATENCY > 1 then
				for i in 1 to LATENCY-1 loop
					delay(i+1)<=delay(i);
				end loop;	
			end if;	
		end if;
	end if;	
end process compare ;
END behave ;
