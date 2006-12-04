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

entity pt_sfixed_add2_tb is
end pt_sfixed_add2_tb;

ARCHITECTURE behave OF pt_sfixed_add2_tb IS

function intmax2 (A	: integer; B : integer) return integer is
	variable ret : integer;
	begin
		if A>B then
			ret := A;
		elsif B>A then
			ret := B;
		else
			ret := A;
		end if;
		return ret;
	end intmax2;
function intmin2 (A	: integer; B : integer) return integer is
	variable ret : integer;
	begin
		if A>B then
			ret := B;
		elsif B>A then
			ret := A;
		else
			ret := A;
		end if;
		return ret;
	end;

component pt_sfixed_add2 is
	generic
	(
		CLOCK_EDGE			:	std_logic 	:= '1' ;
		RESET_ACTIVE_VALUE	:	std_logic 	:= '0' ;
		INPUTA_HIGH			:	integer		:= 0;
		INPUTA_LOW			:	integer		:= -16;
		INPUTB_HIGH			:	integer		:= 0;
		INPUTB_LOW			:	integer		:= -16;
		OUTPUT_HIGH			:	integer		:= 1;
		OUTPUT_LOW			:	integer		:= -16;
		LATENCY				:	integer		:= 1 --Pipeline Latency must be between 0 and 4
	) ;
	port
	(
		clk				: IN std_logic;
		reset			: IN std_logic;
		A				: IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B				: IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		SUM				: OUT std_logic_vector (OUTPUT_HIGH-OUTPUT_LOW DOWNTO 0) 
	) ;
end component pt_sfixed_add2;

constant RESET_ACTIVE_VALUE : std_logic := '0';
constant INPUTA_HIGH		: integer := 0;
constant INPUTA_LOW			: integer := -16;
constant INPUTB_HIGH		: integer := 0;
constant INPUTB_LOW			: integer := -16;
constant OUTPUT_HIGH		: integer := intmax2(INPUTA_HIGH,INPUTB_HIGH)+1;
constant OUTPUT_LOW			: integer := intmin2(INPUTA_LOW,INPUTB_LOW);
signal A_val				: real	  := 0.0;
signal B_val				: real 	  := 0.0;
signal clk_sig		: std_logic := '0';
SIGNAL reset_sig 	: std_logic := RESET_ACTIVE_VALUE;
signal A_sig		: std_logic_vector(INPUTA_HIGH-INPUTA_LOW DOWNTO 0);
signal B_sig		: std_logic_vector(INPUTB_HIGH-INPUTB_LOW DOWNTO 0);
signal SUM_sig		: std_logic_vector(OUTPUT_HIGH-OUTPUT_LOW DOWNTO 0);
SIGNAL As			: sfixed(INPUTA_HIGH DOWNTO INPUTA_LOW);
SIGNAL Bs			: sfixed(INPUTB_HIGH DOWNTO INPUTB_LOW);
SIGNAL SUMs			: sfixed(OUTPUT_HIGH DOWNTO OUTPUT_LOW);
SIGNAL sum_val		: real;
SIGNAL As_val		: real;
SIGNAL Bs_val		: real;

BEGIN

clk_sig    	<= not clk_sig after 10 ns;
reset_sig 	<= not RESET_ACTIVE_VALUE after 24 ns;

As 		<= to_sfixed(A_val,INPUTA_HIGH,INPUTA_LOW);
Bs 		<= to_sfixed(B_val,INPUTB_HIGH,INPUTB_LOW);
A_sig 	<= to_slv(As);
B_sig	<= to_slv(Bs);
SUMs 	<= to_sfixed(SUM_sig,SUMs);
sum_val <= to_real(SUMs);
As_val  <= to_real(As);
Bs_val 	<= to_real(Bs);

add1: pt_sfixed_add2
	GENERIC MAP(
		CLOCK_EDGE			=>	'1',
		RESET_ACTIVE_VALUE	=>	RESET_ACTIVE_VALUE,
		INPUTA_HIGH			=>	INPUTA_HIGH,
		INPUTA_LOW			=>	INPUTA_LOW,
		INPUTB_HIGH			=>	INPUTB_HIGH,
		INPUTB_LOW			=>	INPUTB_LOW,
		OUTPUT_HIGH			=>	OUTPUT_HIGH,
		OUTPUT_LOW			=>	OUTPUT_LOW,
		LATENCY				=>	2
	)
	PORT MAP(
		clk			=>	clk_sig,	
		reset		=>	reset_sig,	
		A			=>	A_sig,		
		B			=>	B_sig,	
		SUM			=>	SUM_sig	
	);

	adder : process(clk_sig,reset_sig)
	begin
	if reset_sig = '0' then
		A_val <= 0.0;
		B_val <= 0.0;
	elsif clk_sig'event and clk_sig = '1' then
		A_val <= A_val + 0.003;
		B_val <= B_val + 0.001;
	end if;
	end process adder ;
END behave ;

