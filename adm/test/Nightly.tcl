# Tests for the Nightly Build
#
# @Author: Christopher Brooks
#
# $Id: Release.tcl 63463 2012-05-02 02:47:37Z hudson $
#
# @Copyright (c) 2012 The Regents of the University of California.
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

# Get rid of any previous lists of .java files etc.
exec make clean

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# These variables match variables in the $PTII/adm/gen-$version/makefile
set major_version 9.0
set minor_version devel
set version $major_version.$minor_version
set windows_version 9_0_devel
set gendir $PTII/adm/gen-$major_version
set ptII_full $gendir/ptII$version.tar
set ptII_src_jar $gendir/ptII$version.src.jar
set ptsetup ptII${windows_version}_setup_windows

proc nightlyMake {target} {
    global PTII
    set ptIIhome $PTII
    set ptIIadm $PTII/adm
    set user hudson
    exec make USER=$user PTIIHOME=${ptIIhome} PTIIADM=${ptIIadm} JAR=/usr/bin/jar TAR=/usr/bin/tar $target
}

test nightly-1.1 {clean} {
    cd $gendir
    nightlyMake clean
    list [file exists $ptII_full]
} {0}

test nightly-1.2 {all} {
    cd $gendir
    nightlyMake all
    list [file exists $ptII_full]
} {1}

test nightly-1.3 {jnlp} {
    cd $gendir
    nightlyMake jnlp
    list [file exists $PTII/vergil.jnlp]
} {1}

test nightly-1.4 {src.jar} {
    cd $gendir
    nightlyMake jnlp
    list [file exists $gendir/ptII$version.src.jar]
} {1}

test nightly-1.5 {setup} {
    cd $gendir
    nightlyMake setup
    list [file exists $gendir/$ptsetup.exe]
} {1}

test nightly-1.6 {test_setup} {
    cd $gendir
    # FIXME: Need to check the output somehow
    nightlyMake test_setup
    list 1
} {1}

test nightly-1.7 {update_andrews} {
    cd $gendir
    nightlyMake update_andrews
    list 1
} {1}

test nightly-1.8 {updateDOPCenterImage} {
    cd $gendir
    nightlyMake updateDOPCenterImage
    list [file exists $PTII/ptolemy/domains/space/demo/DOPCenter/DOPCenter.png]
} {1}
