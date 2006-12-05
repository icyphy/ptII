--Ptolemy VHDL Code Generation Core Library
--pt_utility Package: 	
--Utility types and functions for Ptolemy code generation.
--Author: Vinayak Nagpal

package pt_utility is

--Types used by vhdl/tb/pttest actor

--Type to read correctvalues from ptolemy test actor.
type CORRECTVALS is array (integer range <>) of real;
--Type to represent interpretation of fixed point number
type FIXED_TYPE_SIGN is (SIGNED, UNSIGNED);

end package pt_utility;

package body pt_utility is



end package body pt_utility;
