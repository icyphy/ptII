--Ptolemy VHDL Code Generation Core Library
--pt_mux2_lat0: 	
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



entity pt_mux2_lat0 is
	generic
	(
		WIDTH		:	integer		:= 15
	) ;
	port
	(
		A				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		B				: IN std_logic_vector (WIDTH-1 DOWNTO 0) ;
		sel				: IN std_logic;
		output			: OUT std_logic_vector (WIDTH-1 DOWNTO 0)
	) ;
end pt_mux2_lat0;


ARCHITECTURE behave OF pt_mux2_lat0 IS
--Constants
--Type Declarations


--Signal Declarations


BEGIN
OUTPUT <= A when sel='0' else B;
END behave ;

