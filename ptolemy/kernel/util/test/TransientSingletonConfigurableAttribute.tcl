# Tests for the TransientSingletonConfigurableAttribute class
#
# @Author: Steve Neuendorffer and Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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

######################################################################
####
#

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#
test TransientSingletonConfigurableAttribute-1.1 {test export moml.} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.TransientSingletonConfigurableAttribute $n0 P1]
    $p1 configure [java::null] [java::null] {My Test String}
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
</entity>
}

test TransientSingletonConfigurableAttribute-1.2 {test replacement of previous via constructor.} {
    set p2 [java::new ptolemy.kernel.util.TransientSingletonConfigurableAttribute $n0 P1]
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
</entity>
}

test TransientSingletonConfigurableAttribute-1.2 {test replacement of setContainer()} {
    set p3 [java::new ptolemy.kernel.util.TransientSingletonConfigurableAttribute]
    $p3 setName P1
    $p3 configure [java::null] [java::null] {yyy}
    $p3 setContainer $n0
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
</entity>
}

test TransientSingletonConfigurableAttribute-2.1 {test ctor that takes a workspace.} {
    set n3 [java::new ptolemy.kernel.util.NamedObj]
    $n3 setName N3
    set p3 [java::new ptolemy.kernel.util.TransientSingletonConfigurableAttribute [$n3 workspace]]
    $p3 configure [java::null] [java::null] {My Test String}
    $n3 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N3" class="ptolemy.kernel.util.NamedObj">
</entity>
} 
