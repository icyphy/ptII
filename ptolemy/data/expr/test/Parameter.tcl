# Tests for the Parameter class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#


######################################################################
####
# 
test Param-2.1 {Check constructors} {
    set e [java::new {pt.kernel.Entity String} parent]
    set tok [java::new  {pt.data.DoubleToken double} 4.5]

    set param1 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String pt.data.Token} $e id1 $tok]
    
    set param2 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String String} $e id2 1.5]
    
    set name1 [$param1 getFullName]
    set name2 [$param2 getFullName]
    set value1 [[$param1 getToken] getValue]
    set value2 [[$param2 getToken] getValue]
    list $name1 $value1 $name2 $value2
} {.parent.id1 4.5 .parent.id2 1.5}

#################################
####
# This needs to extended to test type checking
test Param-3.0 {Check setting the contained Token with another Token} {
    set e [java::new {pt.kernel.Entity String} parent]
    set tok1 [java::new  {pt.data.DoubleToken double} 4.5]

    set param1 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String pt.data.Token} $e id1 $tok1]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] getValue]

    # Now put a new token into the Param
    set tok2 [java::new  {pt.data.DoubleToken double} 7.3]
    $param1 {setToken pt.data.Token} $tok2
    
    set name2 [$param1 getFullName]
    set value2 [[$param1 getToken] getValue]

    list $name1 $value1 $name2 $value2
} {.parent.id1 4.5 .parent.id1 7.3}

#################################
####
#
test Param-4.0 {Check setting the contained Token from a String or aother Token} {
    set e [java::new {pt.kernel.Entity String} parent]
    set param1 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String String} $e id1 "1.6 + 8.3"]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] getValue]

    # Now put a new token into the Param
    set tok1 [java::new  {pt.data.DoubleToken double} 7.7]
    $param1 {setToken pt.data.Token} $tok1    
    set value2 [[$param1 getToken] getValue]

    # Now set the Token contained from a String
    $param1 {setTokenFromExpr String} "-((true) ? 5.5 : \"crap\")" 
    set value3 [[$param1 getToken] getValue]

    # Now put a new token into the Param
    set tok2 [java::new  {pt.data.DoubleToken double} 3.3]
    $param1 {setToken pt.data.Token} $tok2    
    set value4 [[$param1 getToken] getValue]

    list $name1 $value1 $value2 $value3 $value4
} {.parent.id1 9.9 7.7 -5.5 3.3}

#################################
####
#
test Param-5.0 {Check reseting the Param to its original String} {
    set e [java::new {pt.kernel.Entity String} parent]
    set param1 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String String} $e id1 "1.6 + 8.3"]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] getValue]

    # Now put a new token into the Param
    set tok1 [java::new  {pt.data.DoubleToken double} 7.7]
    $param1 {setToken pt.data.Token} $tok1    
    set value2 [[$param1 getToken] getValue]

    # Now reset the Token 
    $param1 reset
    set value3 [[$param1 getToken] getValue]

    # Put a new Token in the Param from a String
    $param1 {setTokenFromExpr String} "((true) ? 5.5 : \"crap\")" 
    set value4 [[$param1 getToken] getValue]
    
    # Reset the Token 
    $param1 reset
    set value5 [[$param1 getToken] getValue]

    list $name1 $value1 $value2 $value3 $value4 $value5 
} {.parent.id1 9.9 7.7 9.9 5.5 9.9}

#################################
####
#
test Param-5.1 {Check reseting the Param to its original Token} {
    set e [java::new {pt.kernel.Entity String} parent]
    set tok1 [java::new  {pt.data.DoubleToken double} 9.9]
    set param1 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String pt.data.Token} $e id1 $tok1]
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] getValue]

    # Put a new token into the Param
    set tok1 [java::new  {pt.data.DoubleToken double} 7.7]
    $param1 {setToken pt.data.Token} $tok1    
    set value2 [[$param1 getToken] getValue]

    # Reset the Token 
    $param1 reset
    set value3 [[$param1 getToken] getValue]

    # Put a new Token in the Param from a String
    $param1 {setTokenFromExpr String} "((true) ? 5.5 : \"crap\")" 
    set value4 [[$param1 getToken] getValue]
    
    # Reset the Token 
    $param1 reset
    set value5 [[$param1 getToken] getValue]

    list $name1 $value1 $value2 $value3 $value4 $value5 
} {.parent.id1 9.9 7.7 9.9 5.5 9.9}

#################################
####
#
test Param-6.0 {Check updating of Params that refer to other Params} {
    set e [java::new {pt.kernel.Entity String} parent]
    set param1 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String String} $e id1 1.1]
    $param1 setContainer $e

    set tok1 [java::new  {pt.data.DoubleToken double} 9.9]
    set param2 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String String} $e id2 9.9]
    $param2 setContainer $e

    set param3 [java::new {pt.data.expr.Parameter pt.kernel.util.NamedObj String String} $e id3 "id1 + id2"]
    $param3 setContainer $e
 
    set name1 [$param1 getFullName]
    set value1 [[$param1 getToken] getValue]
    set name2 [$param2 getFullName]
    set value2 [[$param2 getToken] getValue]
    set name3 [$param3 getFullName]
    set value3 [[$param3 getToken] getValue]
    
    $param1 {setTokenFromExpr String} "((true) ? 5.5 : \"crap\")" 
    set name4 [$param1 getFullName]
    set value4 [[$param1 getToken] getValue]

    set name5 [$param3 getFullName]
    set value5 [[$param3 getToken] getValue]

    list $name1 $value1 $name2 $value2 $name3 $value3 $name4 $value4 $name5 $value5 
} {.parent.id1 1.1 .parent.id2 9.9 .parent.id3 11.0 .parent.id1 5.5 .parent.id3 15.4}

#################################
####
# check that depency cycles get broken/aren't allowed
