# Print out info about Crypto
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


proc providers {} {
   puts "PROVIDERS"
   set providers [java::call java.security.Security getProviders]
   for {set i 0} {$i < [$providers length]} {incr i} {
       set provider [$providers get $i]
       puts [$provider toString]
   }
    puts "";
}

proc algorithms {algorithm} {
    puts "$algorithm: "
    set algorithms [java::call java.security.Security getAlgorithms $algorithm]
    set iterator [$algorithms iterator]
    for {set i 0} {[$iterator hasNext]} {incr i} {
	puts [$iterator next]
    }
    puts "";		  
}

proc cryptoInfo {} {
    providers	   
    algorithms CIPHER
    algorithms KEYGENERATOR
    algorithms KEYPAIRGENERATOR
    algorithms MESSAGEDIGEST
    algorithms SIGNATURE
}
