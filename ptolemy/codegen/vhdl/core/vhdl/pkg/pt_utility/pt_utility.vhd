--Ptolemy VHDL Code Generation Core Library
--pt_utility Package: 	
--Utility types and functions for Ptolemy code generation.
--Author: Vinayak Nagpal

package pt_utility is

--Types used by vhdl/tb/pttest actor

--Type to read correctvalues from ptolemy test actor.
type CORRECTVALS is array (integer range <>) of real;
--Type to represent interpretation of fixed point number
type FIXED_TYPE_SIGN is (SIGNED_TYPE, UNSIGNED_TYPE);
--Type to represent comparator operation
type COMPARETYPE is (GEQ,LEQ,EQ,G,L,NEQ);

type LOGICTYPE is (PT_OR,PT_AND,PT_XOR,PT_NAND,PT_NOR,PT_XNOR);

end package pt_utility;

package body pt_utility is



end package body pt_utility;
