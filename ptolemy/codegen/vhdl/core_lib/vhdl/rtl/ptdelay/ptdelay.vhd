--Ptolemy VHDL Code Generation Core Library
--ptdelay: Flip flop based latency block.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;

entity ptdelay is
	generic
	(
		WIDTH 				: integer := 32; --width in number of bits
		LATENCY 			: integer :=10   --non strict delay latency in number of clock ticks
	) ;
	port
	(
		clk 	: 	IN std_logic;
		D		:	IN std_logic_vector (WIDTH-1 DOWNTO 0) ;	
		Q		:	OUT std_logic_vector (WIDTH-1 DOWNTO 0) 	
	) ;
end ptdelay;

ARCHITECTURE behave OF ptdelay IS
--Uses component pt register
component ptregister is
	generic
	(
		WIDTH : INTEGER := 32     
	);
	port
	(
		clk : IN std_logic ;
		D	: IN std_logic_vector (WIDTH DOWNTO 0);
		Q	: OUT std_logic_vector (WIDTH DOWNTO 0) 
	);
end component ptregister;

--Type definition for wires to connect the instances of ptregsiter.
TYPE PTBUS is ARRAY (1 to LATENCY) of std_logic_vector (WIDTH-1 DOWNTO 0) ;

--Wires to connect ptregisters
signal dbus	: PTBUS;

BEGIN
dbus(1) <= D;
Q <= dbus(LATENCY);
G1: for i in 1 TO LATENCY GENERATE
		ptreg: ptregister
		GENERIC MAP(
		WIDTH => WIDTH
		)
		PORT MAP(
		clk 	=> clk,
		D		=> dbus(i),
		Q		=> dbus(i+1)
		);
end GENERATE G1;
END behave ;
