--Ptolemy VHDL Code Generation Core Library
--pttest: Test Block for comparing generated VHDL for bit and cycle
--accuracy w.r.t. Ptolemy model.
--THIS IS A TEST BENCH MODULE AND WILL NOT SYNTHESIZE TO HARDWARE.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use ieee_proposed.standard_textio_additions.all;
use work.pt_utility.all;

--type CORRECTVALS is array (integer range <>) of real;


entity pttest is
GENERIC(
	LENGTH		:	integer := 3;
	INPUT_HIGH	:	integer	:= 0;
	INPUT_LOW	:	integer := -15;
	LIST		:	CORRECTVALS;
	FIXED_SIGN	:	FIXED_TYPE_SIGN := SIGNED	
);
PORT (
	clk			:	in 	std_logic ;
	data_in		:	in	std_logic_vector (INPUT_HIGH-INPUT_LOW DOWNTO 0)
--	list		:	in  CORRECTVALS(1 to LENGTH) := (0.23,0.345,-1.0)
);
end pttest;

ARCHITECTURE behave OF pttest IS

SIGNAL In_signed	:	sfixed(INPUT_HIGH DOWNTO INPUT_LOW);	 
SIGNAL In_unsigned	:	ufixed(INPUT_HIGH DOWNTO INPUT_LOW);	 
SIGNAL count		:	integer :=0;	 

BEGIN

In_signed 	<= to_sfixed(data_in,INPUT_HIGH,INPUT_LOW);
In_unsigned <= to_ufixed(data_in,INPUT_HIGH,INPUT_LOW);

compare : process(clk)
	variable In_real		: real := 0.0;
	variable expected_real	: real := -1.0;
begin
		if clk'event and clk = '1' then
			if count = LENGTH then
				count <= count;
			else
				count <= count + 1;
				expected_real:=LIST(count);
				if FIXED_SIGN = SIGNED then
					In_real := to_real(In_signed); 
					assert expected_real=In_real
					report real'image(expected_real) & "/=" & real'image(In_real)
					severity error;
				elsif FIXED_SIGN = UNSIGNED then
					In_real := to_real(In_unsigned); 
					assert expected_real=In_real
					report real'image(expected_real) & "/=" & real'image(In_real)
					severity error;
				end if;	
			end if;
		end if;
end process compare ;
END behave ;

