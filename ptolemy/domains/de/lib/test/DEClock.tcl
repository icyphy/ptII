# Tests for the DEClock class.
#
# @Author: Lukito Muliadi
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

######################################################################
####
# 
test DEClock-2.1 {Test the constructors} {
    #set ws [java::new ptolemy.kernel.util.Workspace ".myworkspace" ]
    set actor [java::new ptolemy.actor.TypedCompositeActor ]
    set dir [java::new ptolemy.domains.de.kernel.DEDirector]
    $actor setDirector $dir
    set man [java::new ptolemy.actor.Manager]
    $actor setManager $man
    set clock [java::new ptolemy.domains.de.lib.DEClock $actor "MyClock" 1.0 1.0]
    $clock description
} {ptolemy.domains.de.lib.DEClock {..MyClock} attributes {
    {ptolemy.data.expr.Parameter {..MyClock.interval} ptolemy.data.DoubleToken(1.0)}
    {ptolemy.data.expr.Parameter {..MyClock.value} ptolemy.data.DoubleToken(1.0)}
} ports {
    {ptolemy.actor.TypedIOPort {..MyClock.output} attributes {
    } links {
    } insidelinks {
    } configuration {output opaque {width 0}} receivers {
    } remotereceivers {
    } type {declared ptolemy.data.Token resolved ptolemy.data.Token}}
}}






