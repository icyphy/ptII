# Tests for the nil tokens
#
# @Author: Christopher Brooks
#
# @Version $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
# All rights reserved.
# 
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
# 
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
# 
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
# 

set types [list DoubleToken IntToken LongToken]
foreach type $types {
    puts -nonewline "$type"
    # Perform binary operations on nil types
    # bitwiseAnd bitwiseOr bitwiseXor are excluded, they only work on scalars?
    set binaryOperations \
	[list add \
	     divide modulo multiply subtract]
    foreach binaryOperation $binaryOperations {
	test "$type-$binaryOperation" "Test $binaryOperation binary op on $type" {

	    puts -nonewline " $binaryOperation"
	    #set nil [java::new ptolemy.data.$type [java::null]] 
	    set nil [java::field ptolemy.data.Token NIL]
	    set typeToken [java::new ptolemy.data.$type] 
	    set one [java::cast ptolemy.data.$type [$typeToken one]]

	    # Try the binaryop with both args nil
	    set results [java::cast ptolemy.data.Token \
			     [$nil $binaryOperation $nil]]
	    set resultsClassName [[$results getClass] getName] 

	    # Try the binaryop with one arg nil
	    set results1 [java::cast ptolemy.data.Token \
			     [$nil $binaryOperation $one]]
	    set results1ClassName [[$results1 getClass] getName] 

	    # Try the binaryop with the other arg nil
	    set results2 [java::cast ptolemy.data.Token \
			     [$one $binaryOperation $nil]]
	    set results2ClassName [[$results2 getClass] getName] 


	    list [list \
		      [$results toString] \
		      [$results isNil] \
		      [expr {"$resultsClassName" == "ptolemy.data.Token"}]] \
		[list \
		     [$results1 toString] \
		     [$results1 isNil] \
		     [expr {"$results1ClassName" == "ptolemy.data.Token"}]] \
		[list \
		     [$results2 toString] \
		     [$results2 isNil] \
		     [expr {"$results1ClassName" == "ptolemy.data.Token"}]] \

	} {{nil 1 1} {nil 1 1} {nil 1 1}}
    }


#    # Perform unary operations on nil types
#    set unaryOperations \
#	[list absolute]
#    foreach unaryOperation $unaryOperations {
#	test "$type-$unaryOperation" "Test $unaryOperation unary op on $type" {
#	    puts -nonewline " $unaryOperation"
#	    #set nil [java::new ptolemy.data.$type [java::null]] 
#	    set nil [java::field ptolemy.data.Token NIL]
#	    set results [$nil $unaryOperation]
#	    set resultsClassName [[$results getClass] getName] 
#	    list [$results toString] \
#		[$results isNil] \
#		[expr {"$resultsClassName" == "ptolemy.data.$type"}]
#	} {nil 1 1}
#    }

    # Perform isEqualTo on nil types.  isEqualTo always returns false
    set relationalOperations [list isEqualTo]
    foreach relationalOperation $relationalOperations {
	test "$type-$relationalOperation" "Test $relationalOperation on $type" {
	    puts -nonewline " $relationalOperation"
	    #set nil [java::new ptolemy.data.$type [java::null]] 
	    set nil [java::field ptolemy.data.Token NIL]
	    set typeToken [java::new ptolemy.data.$type] 
	    set one [java::cast ptolemy.data.$type [$typeToken one]]
	    set result [$nil $relationalOperation $nil]
	    set result2 [$one $relationalOperation $nil]
	    set result3 [$nil $relationalOperation $one]
	    set result4 [$one $relationalOperation $one]
	    list [$result toString] [$result2 toString] \
		[$result3 toString] [$result4 toString]
	} {false false false true}
    }

#    # Perform isLessThan on nil types. isLessThan throws an exception
#    set relationalOperations [list isLessThan]
#    foreach relationalOperation $relationalOperations {
#	test "$type-$relationalOperation" "Test $relationalOperation on $type" {
#
#	    puts -nonewline " $relationalOperation"
#	    #set nil [java::new ptolemy.data.$type [java::null]] 
#	    set nil [java::field ptolemy.data.Token NIL]
#	    set typeToken [java::new ptolemy.data.$type] 
#	    set one [java::cast ptolemy.data.$type [$typeToken one]]
#	    catch {$one $relationalOperation $nil} msg
#	    regsub -all $type $msg "XXXToken" result
#	    list $result
#	} {{ptolemy.kernel.util.IllegalActionException: isLessThan operation not supported between ptolemy.data.XXXToken 'nil' and ptolemy.data.XXXToken 'nil' because one or the other is nil}}
#    }
#
    puts "."
}
