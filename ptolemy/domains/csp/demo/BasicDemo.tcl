# CSP Basic example showing two actors rendezvousing with each other.
#
# @Author: Neil Smyth
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
#
#  A two actor system performing a simple send and receive communication.

set univ [java::new ptolemy.actor.CompositeActor]
$univ setName BasicDemo
set manager [java::new ptolemy.actor.Manager Manager]
set dir [java::new ptolemy.domains.csp.kernel.CSPDirector CSPDirector]
$univ setDirector $dir
$univ setManager $manager

set source [java::new ptolemy.domains.csp.lib.CSPSource $univ Source]
set sink [java::new ptolemy.domains.csp.lib.CSPSink $univ Sink]

set input [java::field $sink input]
set output [java::field $source output]

set relation [$univ connect $output $input R1]
#puts [ $univ description 1023]

# For now this just returns, a bug
puts [$manager {go int} 1]






