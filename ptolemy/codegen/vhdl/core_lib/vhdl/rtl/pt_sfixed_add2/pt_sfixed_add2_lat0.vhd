--Ptolemy VHDL Code Generation Core Library
--pt_sfixed_add2_lat0: 	
--2 input subtractor for signed fixed point operands. 
--Zero Latency.
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

entity pt_sfixed_add2_lat0 is
	generic
	(
		INPUTA_HIGH			:	integer		:= 15;
		INPUTA_LOW			:	integer		:= 0;
		INPUTB_HIGH			:	integer		:= 15;
		INPUTB_LOW			:	integer		:= 0;
		OUTPUT_HIGH			:	integer		:= 16;
		OUTPUT_LOW			:	integer		:= 0
	) ;
	port
	(
		A				: IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B				: IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		OUTPUT			: OUT std_logic_vector (OUTPUT_HIGH-OUTPUT_LOW DOWNTO 0) 
	) ;
end pt_sfixed_add2_lat0;


ARCHITECTURE behave OF pt_sfixed_add2_lat0 IS


--Signal Declarations

--Input A
signal As :	sfixed (INPUTA_HIGH DOWNTO INPUTA_LOW);
--Input B
signal Bs :	sfixed (INPUTB_HIGH DOWNTO INPUTB_LOW);
--Scaled Sum
signal SUMs_c: sfixed(OUTPUT_HIGH DOWNTO OUTPUT_LOW);


BEGIN
As <= to_sfixed(A,As'high,As'low);
Bs <= to_sfixed(B,Bs'high,Bs'low);

SUMs_c <= resize (	arg				=>	As+Bs,
					left_index		=>	OUTPUT_HIGH,
					right_index		=>	OUTPUT_LOW,
					round_style		=>	fixed_truncate,
					overflow_style	=>	fixed_wrap);

OUTPUT <= to_slv(SUMs_c);
END behave ;
