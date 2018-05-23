# Definition of the nightlyMake Tcl proc, used by the tests.
#
# @Author: Christopher Brooks
#
# $Id$
#
# @Copyright (c) 2012-2015 The Regents of the University of California.
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

proc nightlyMake {target {pattern {.*\*\*\*.*}}} {
    global PTII gendir
    set ptIIhome $PTII
    set ptIIadm $PTII/adm

    # Use StreamExec so that we echo the results to stdout as the
    # results are produced.
    set streamExec [java::new ptolemy.util.StreamExec]
    set commands [java::new java.util.LinkedList]
    cd $PTII
    $commands add "make -C $gendir PTIIHOME=${ptIIhome} PTIIADM=${ptIIadm} JAR=/usr/bin/jar TAR=/usr/local/bin/tar $target"
    $streamExec setCommands $commands
    $streamExec setPattern $pattern
    puts "adm/test/nightlyMake.tcl: about to execute [$commands toString]"
    $streamExec start
    set returnCode [$streamExec getLastSubprocessReturnCode]
    if { $returnCode != 0 } {
	return [list "Last subprocess returned non-zero: $returnCode" \
		    [$streamExec getPatternLog]]
    }
    return [$streamExec getPatternLog]
}
