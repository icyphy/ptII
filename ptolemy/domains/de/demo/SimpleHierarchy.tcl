# DE example using TclBlend
#
# @Author: Lukito Muliadi
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

#######################################################################


# Create the Composite Actor
set outer [java::new ptolemy.actor.TypedCompositeActor]
$outer setName outer

set inner [java::new ptolemy.actor.TypedCompositeActor $outer inner]

# Create directors and associate them with the composite actor.
set outerdir [java::new ptolemy.domains.de.kernel.DECQDirector OuterDirector]
$outer setDirector $outerdir
set innerdir [java::new ptolemy.domains.de.kernel.DECQDirector InnerDirector]
$inner setDirector $innerdir
set manager [java::new ptolemy.actor.Manager]
$outer setManager $manager

# Build the system
set poisson [java::new ptolemy.domains.de.lib.DEPoisson $inner Poisson]

set plot [java::new ptolemy.domains.de.lib.DEPlot $outer Plot]

# Identify the ports

set border [java::new ptolemy.actor.TypedIOPort $inner border 0 1]

set poissonOut [java::field $poisson output]

set plotIn [java::field $plot input]

# Connect the ports
set r1 [$inner connect $poissonOut $border R1]
set r2 [$outer connect $border $plotIn R2]

# Run it
$outerdir setStopTime 10.0
$manager run
