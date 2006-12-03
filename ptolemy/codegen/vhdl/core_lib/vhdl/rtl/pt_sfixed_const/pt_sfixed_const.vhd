--Ptolemy VHDL Code Generation Core Library
--pt_sfixed_const: 	
--Signed Fixed point constant. 
--
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use work.pt_utility.all;

entity pt_sfixed_const is
	generic
	(
		CONST_HIGH			:	integer		:= 15;
		CONST_LOW			:	integer		:= 0;
		CONST_VALUE			:	real		:= 0.125
	) ;
	port
	(
		output			: OUT std_logic_vector (CONST_HIGH-CONST_LOW DOWNTO 0) 
	) ;
end pt_sfixed_const;


ARCHITECTURE behave OF pt_sfixed_const IS
--Constants
--Type Declarations

--Signal Declarations
SIGNAL const_r : sfixed (CONST_HIGH DOWNTO CONST_LOW);	 

BEGIN
const_r <= to_sfixed(CONST_VALUE,const_r);

output <= to_slv(const_r);
		
END behave ;
