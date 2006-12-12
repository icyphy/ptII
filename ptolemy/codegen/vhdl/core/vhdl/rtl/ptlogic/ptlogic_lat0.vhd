--Ptolemy VHDL Code Generation Core Library
--ptlogic_lat0: 	
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
		LOGICOP				:	LOGICTYPE	:= PT_AND
	) ;
	port
	(
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		OUTPUT			: OUT std_logic_vector (WIDTH-1 DOWNTO 0) 
	) ;
end ptlogic;


ARCHITECTURE behave OF ptlogic IS
--Constants
--Type Declarations
--TYPE DELAYLINE is ARRAY (1 to LATENCY) of std_logic_vector (WIDTH-1 DOWNTO 0);

--Signal Declarations
--SIGNAL delay : DELAYLINE;	 
 

BEGIN
--output <= delay(LATENCY);

compare : process(A,B)
begin
	case LOGICOP is
		when PT_AND =>
			output <= A and B;	
		when PT_OR =>
			output <= A or B;	
		when PT_NAND =>
			output <= A nand B;	
		when PT_NOR =>
			output <= A nor B;	
		when PT_XOR =>
			output <= A xor B;	
		when PT_XNOR =>
			output <= A xnor B;	
		when others =>
			output <= A and B;
	end case;		
end process compare ;
END behave ;
