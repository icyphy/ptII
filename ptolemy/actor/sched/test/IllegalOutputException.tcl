# Tests for the IllegalOutputException class
#
# @Author: Christopher Hylands, Based on SDFReceiver by Brian K. Vogel
#
# @Version: $Id$
#
# @Copyright (c) 1999-2006 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

test IllegalOutputException-1.1 {empty detail} {
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException ""]
    list [$e getMessage] [$e getLocalizedMessage]
} {{} {}}

test IllegalOutputException-1.2 {detail} {
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException "detail message"]
    list [$e getMessage] [$e getLocalizedMessage]
} {{detail message} {detail message}}

test IllegalOutputException-2.1 {unnamed NamedObj} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException $n1]
    list [$e getMessage]
} {{  in .<Unnamed Object>}}

test IllegalOutputException-2.2 {named NamedObj} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj1"]
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException $n1]
    list [$e getMessage]
} {{  in .NamedObj1}}

test IllegalOutputException-3.1 {unnamed NamedObj and empty detail} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException $n1 ""]
    list [$e getMessage]
} {{  in .<Unnamed Object>}}

test IllegalOutputException-3.2 {named NamedObj and empty detail} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj1"]
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException $n1 ""]
    list [$e getMessage]
} {{  in .NamedObj1}}

test IllegalOutputException-4.1 {unnamed NamedObj and detail} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException $n1 "detail"]
    list [$e getMessage]
} {{detail
  in .<Unnamed Object>}}

test IllegalOutputException-4.2 {named NamedObj and detail} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj1"]
    set e [java::new \
	    ptolemy.actor.sched.IllegalOutputException $n1 "detail"]
    list [$e getMessage] "\n" [$e toString]
} {{detail
  in .NamedObj1} {
} {ptolemy.actor.sched.IllegalOutputException: detail
  in .NamedObj1}}

