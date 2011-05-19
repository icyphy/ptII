# Tests for the SDFScheduler class
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2009 The Regents of the University of California.
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

test composition-1.0 {DE model contained within SDF} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [$director getScheduler]

    set a1 [java::new ptolemy.actor.lib.Ramp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.actor.TypedIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.actor.TypedIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.de.kernel.DEDirector $c1 d5]
    $c1 setDirector $d5
    set a3 [java::new ptolemy.actor.lib.Recorder $toplevel Recorder]

    $toplevel connect \
	    [java::field [java::cast ptolemy.actor.lib.Source $a1] output]\
	    $p1 R1
    $c1 connect $p1 $p2 R2
    $toplevel connect \
	    $p2 \
	    [java::field [java::cast ptolemy.actor.lib.Sink $a3] input] R4
    set iterations [$director getAttribute iterations]
    _testSetToken $iterations [java::new {ptolemy.data.IntToken int} 6]

    $manager run
    list [enumToTokenValues [$a3 getRecord 0]]  [enumToObjects [$a3 getTimeRecord]]
} {{0 1 2 3 4 5} {0.0 0.0 0.0 0.0 0.0 0.0}}

####
test composition-2.0 {Liberal links should work if you have a single SDF director at the top level. (no opaque composite actor with other domains) https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=26} { 

    # Create a Ramp -> Scale -> Recorder model, where the Scale is contained
    # in a separate Composite.

    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [$director getScheduler]

    set ramp2 [java::new ptolemy.actor.lib.Ramp $toplevel Ramp2]

    # Put the scale inside a TypedCompositeActor 
    set composite2 [java::new ptolemy.actor.TypedCompositeActor \
			$toplevel Composite2]
    set innerScale2 [java::new ptolemy.actor.lib.Scale $composite2 InnerScale2]

    # Set the Scale factor to 2
    set factor [$innerScale2 getAttribute factor]
    _testSetToken $factor [java::new {ptolemy.data.IntToken int} 2]

    set scaleInput [java::field \
			[java::cast ptolemy.actor.lib.Transformer \
			     $innerScale2] input]

    set r1 [java::new ptolemy.actor.TypedIORelation $toplevel r1]
    
    # This used to throw a level crossing link exception, but doesn't
    # anymore, as of 4/26/09.
    catch {$scaleInput link $r1} errMsg

    # This works, but is now redundant.
    # $scaleInput liberalLink $r1


    set rampOutput [java::field [java::cast ptolemy.actor.lib.Source $ramp2] \
			output]
    $rampOutput link $r1


    set scaleOutput [java::field \
			[java::cast ptolemy.actor.lib.Transformer \
			     $innerScale2] output]
    set r2 [java::new ptolemy.actor.TypedIORelation $toplevel r2]
    $scaleOutput liberalLink $r2


    set recorder2 [java::new ptolemy.actor.lib.Recorder $toplevel Recorder2]
    set recorderInput [java::field [java::cast ptolemy.actor.lib.Sink \
					$recorder2] input]
    $recorderInput link $r2

    set iterations [$director getAttribute iterations]
    _testSetToken $iterations [java::new {ptolemy.data.IntToken int} 6]

    $manager run
    list [enumToTokenValues [$recorder2 getRecord 0]]
} {{0 2 4 6 8 10}}

