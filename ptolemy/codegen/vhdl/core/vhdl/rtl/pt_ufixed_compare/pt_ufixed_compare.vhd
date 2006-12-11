--Ptolemy VHDL Code Generation Core Library
--pt_ufixed_compare: 	
--2 input comparator for signed fixed point operands. 
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

--type COMPARETYPE is (GEQ,LEQ,EQ,G,L,NEQ);

entity pt_ufixed_compare is
	generic
	(
		INPUTA_HIGH			:	integer		:= 15;
		INPUTA_LOW			:	integer		:= 0;
		INPUTB_HIGH			:	integer		:= 15;
		INPUTB_LOW			:	integer		:= 0;
		COMPAREOP			:	COMPARETYPE	:= EQ;
		RESET_ACTIVE_VALUE	:	std_logic	:= '0';
		LATENCY				: 	integer		:= 3
	) ;
	port
	(
		clk				: IN std_logic;
		reset			: IN std_logic;
		A				: IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B				: IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		OUTPUT			: OUT std_logic 
	) ;
end pt_ufixed_compare;


ARCHITECTURE behave OF pt_ufixed_compare IS
--Constants
--Type Declarations
TYPE DELAYLINEA is ARRAY (1 to LATENCY) of ufixed (INPUTA_HIGH DOWNTO INPUTA_LOW);
TYPE DELAYLINEB is ARRAY (1 to LATENCY) of ufixed (INPUTB_HIGH DOWNTO INPUTB_LOW);

--Signal Declarations
SIGNAL delayA : DELAYLINEA;	 
SIGNAL delayB : DELAYLINEB;	 
--Input A
signal As 	 :	ufixed (INPUTA_HIGH DOWNTO INPUTA_LOW);
--Input B
signal Bs 	 :	ufixed (INPUTB_HIGH DOWNTO INPUTB_LOW);

BEGIN
As <= to_ufixed(A,As'high,As'low);
Bs <= to_ufixed(B,Bs'high,Bs'low);

compare : process(clk)
begin
	if clk'event and clk = '1' then
		if reset = RESET_ACTIVE_VALUE then
			delayA <= (others => (others => '0'));
			delayB <= (others => (others => '0'));
			output <= '0';
		else
			case COMPAREOP is
				when EQ =>
					if delayA(LATENCY) = delayB(LATENCY) then
						output <= '1' ;
					else
						output <= '0' ;
					end if;
				when NEQ =>
					if delayA(LATENCY) /= delayB(LATENCY) then
						output <= '1' ;
					else
						output <= '0';
					end if;	
				when GEQ =>
					if delayA(LATENCY) >= delayB(LATENCY) then
						output <= '1';
					else
						output <= '0';
					end if;	
				when LEQ =>
					if delayA(LATENCY) <= delayB(LATENCY) then
						output <= '1';
					else
						output <= '0';
					end if;	
				when G =>
					if delayA(LATENCY) > delayB(LATENCY) then
						output <= '1';
					else
						output <= '0';
					end if;	
				when L =>
					if delayA(LATENCY) < delayB(LATENCY) then
						output <= '1';
					else
						output <= '0';
					end if;	
				when others =>
					OUTPUT <= '0';
			end case;		
			delayA(1) <= As;
			delayB(1) <= Bs;
			if LATENCY > 1 then
				for i in 1 to LATENCY-1 loop
					delayA(i+1)<=delayA(i);
					delayB(i+1)<=delayB(i);
				end loop;	
			end if;	
		end if;
	end if;	
end process compare ;
END behave ;
