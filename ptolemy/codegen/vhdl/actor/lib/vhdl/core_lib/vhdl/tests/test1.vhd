--Ptolemy VHDL Code Generation Core Library
--pt_sfixed_add2_tb: Test bench for this component. 	
--2 input adder for signed fixed point operands. 
--This block uses the VHDL fixed point data
--type (proposed for VHDL-2006) support in backward compatible 
--mode for VHDL-1993)
--The block will truncate and wrap 
--on overflow when addition result cannot be
--represented in the output data width.
--The block allows using register retiming latency from 0 to 4 clock cycles.
--The I/O interface is done using std_logic_vector types.
--This block needs the fixed point extensions supplied
--by www.vhdl.org to be compiled in workspace ieee_proposed
--
--					
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;

entity test1 is
end test1;

ARCHITECTURE behave OF pt_sfixed_add2_tb IS
component pt_sfixed_const is
	generic
	(
		CONST_HIGH			:	integer		:= 15;
		CONST_LOW			:	integer		:= 0;
		CONST_VALUE			:	real		:= 0.125
	) ;
	port
	(
		const_out			: OUT std_logic_vector (CONST_HIGH-CONST_LOW DOWNTO 0) 
	) ;
end component pt_sfixed_const;
component ptconcat2 is
	generic
	(
		INPUTHIGH_WIDTH 		: integer := 32; --width in number of bits
		INPUTLOW_WIDTH 		: integer := 32; --width in number of bits
	) ;
	port
	(
		input_high	:	IN std_logic_vector (INPUTHIGH_WIDTH-1 DOWNTO 0) ;	
		input_low	:	IN std_logic_vector (INPUTLOW_WIDTH-1 DOWNTO 0) ;	
		output		:	OUT std_logic_vector (INPUTHIGH_WIDTH+INPUTLOW_WIDTH-1 DOWNTO 0) 	
	) ;
end component ptconcat2;


constant CONSTA_HIGH : integer := 0;
constant CONSTA_LOW : integer := -15;
constant CONSTB_HIGH : integer := 0;
constant CONSTB_LOW : integer := -15;

constant CONSTA_VAL	: real := -0.125;
constant CONSTB_VAL	: real := 0.325;

SIGNAL	input_high_sig	: std_logic_vector (CONSTA_HIGH-CONSTA_LOW DOWNTO 0) ;	 
SIGNAL	input_low_sig	: std_logic_vector (CONSTB_HIGH-CONSTB_LOW DOWNTO 0) ;	 

BEGIN

const1: pt_sfixed_const
	GENERIC MAP(
		CONST_HIGH		=> CONSTA_HIGH,	
		CONST_LOW		=> CONSTA_LOW,	
		CONSTA_VALUE		=> CONSTA_VAL	
		)
	PORT MAP(
		const_out	=> 	input_high_sig	
	);
const2: pt_sfixed_const
	GENERIC MAP(
		CONST_HIGH		=> CONSTB_HIGH,	
		CONST_LOW		=> CONSTB_LOW,	
		CONST_VALUE		=> CONSTB_VAL	
		)
	PORT MAP(
		const_out	=> 	input_low_sig	
	);
concat1: ptconcat
	GENERIC MAP(
		INPUTHIGH_WIDTH  => CONSTA_HIGH-CONSTA_LOW+1,	
		INPUTLOW_WIDTH 	 =>	CONSTB_HIGH-CONSTB_LOW+1
		)
	PORT MAP(
		input_high => input_high_sig,	
		input_low  => input_low_sig,	
		output	=> OPEN	
	);
END behave ;

