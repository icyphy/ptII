--Ptolemy VHDL Code Generation Core Library
--pt_ufixed_compare: 	
--2 input comparator for signed fixed point operands. 
--Latency 0.
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

--type COMPARETYPE is (GEQ,LEQ,EQ,G,L,NEQ);

entity pt_ufixed_compare_lat0 is
	generic
	(
		INPUTA_HIGH			:	integer		:= 15;
		INPUTA_LOW			:	integer		:= 0;
		INPUTB_HIGH			:	integer		:= 15;
		INPUTB_LOW			:	integer		:= 0;
		COMPAREOP			:	COMPARETYPE	:= EQ
	) ;
	port
	(
		A				: IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B				: IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		OUTPUT			: OUT std_logic 
	) ;
end pt_ufixed_compare_lat0;


ARCHITECTURE behave OF pt_ufixed_compare_lat0 IS
--Constants
--Type Declarations
--Signal Declarations
--Input A
signal As 	 :	ufixed (INPUTA_HIGH DOWNTO INPUTA_LOW);
--Input B
signal Bs 	 :	ufixed (INPUTB_HIGH DOWNTO INPUTB_LOW);

BEGIN
As <= to_ufixed(A,As'high,As'low);
Bs <= to_ufixed(B,Bs'high,Bs'low);

output <= '1' when (((As > Bs) and COMPAREOP = G) or
				   ((As >= Bs) and COMPAREOP = GEQ) or
				   ((As < Bs) and COMPAREOP = L) or
				   ((As = Bs) and COMPAREOP = EQ) or
				   ((As <= Bs) and COMPAREOP = LEQ) or
				   ((As /= Bs) and COMPAREOP = NEQ)) else '0'; 
END behave ;
