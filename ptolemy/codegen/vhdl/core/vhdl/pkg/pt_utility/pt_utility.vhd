--Ptolemy VHDL Code Generation Core Library
--pt_utility Package: 	
--Utility types and functions for Ptolemy code generation.
--Author: Vinayak Nagpal

package pt_utility is

function sfixed_add_to_sfixed_high (
	A_high	: integer; 
	B_high : integer) 
return integer; 

function sfixed_add_to_sfixed_low (
	A_low	: integer; 
	B_low	: integer) 
return integer;

function sfixed_add_to_slv_high (
	A_high	: integer;
	A_low	: integer;
	B_high	: integer;
	B_low	: integer)
return integer;


end package pt_utility;

package body pt_utility is

function sfixed_add_to_sfixed_high (
	A_high	: integer; 
	B_high : integer) 
return integer is
variable ret : integer;
begin
		if A_high>B_high then
			ret := A_high+1;
		elsif B_high>A_high then
			ret := B_high+1;
		else
			ret := A_high+1;
		end if;
		return ret;
end function sfixed_add_to_sfixed_high;

function sfixed_add_to_sfixed_low (
	A_low	: integer; 
	B_low : integer) 
return integer is
variable ret : integer;
begin
		if A_low>B_low then
			ret := B_low;
		elsif B_low>A_low then
			ret := A_low;
		else
			ret := A_low;
		end if;
		return ret;
end function sfixed_add_to_sfixed_low;

function sfixed_add_to_slv_high (
	A_high	: integer;
	A_low	: integer;
	B_high	: integer;
	B_low	: integer)
return integer is
variable ret : integer;
begin
	ret := sfixed_add_to_sfixed_high(A_high,B_high)-sfixed_add_to_sfixed_low(A_low,B_low);
	return ret;
end function sfixed_add_to_slv_high;

end package body pt_utility;
