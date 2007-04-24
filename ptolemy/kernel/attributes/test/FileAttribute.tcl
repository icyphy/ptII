# Tests for the FileAttribute class
#
# @Author: Christopher Brooks
#
# @Version: $Id$ 
#
# @Copyright (c) 200y The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.htm for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test FileAttribute-1.2 {two arg Constructor} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s2 [java::new ptolemy.kernel.attributes.FileAttribute $n "my FileAttribute"]
    set output [java::new java.io.StringWriter]
    $s2 exportMoML $output 1
    list [$s2 toString] \
	[$output toString] \
	[$s2 getDefaultExpression] \
	[$s2 getDisplayName]
} {{ptolemy.kernel.attributes.FileAttribute {.my NamedObj.my FileAttribute}} {    <property name="my FileAttribute" class="ptolemy.kernel.attributes.FileAttribute">
    </property>
} {} {my FileAttribute}}

######################################################################
####
#
test FileAttribute-2.1 {asFile} {
    set n2 [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set f2 [java::new ptolemy.kernel.attributes.FileAttribute $n2 "my FileAttribute"]
    set r1 [$f2 asFile]
    $f2 setExpression makefile
    set file [$f2 asFile]
    set r2 [$file toString]
    list [java::isnull $r1] $r2
} {1 makefile}

######################################################################
####
#
test FileAttribute-3.1 {asURL} {
    set n3 [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set f3 [java::new ptolemy.kernel.attributes.FileAttribute $n3 "my FileAttribute"]
    set r1 [$f3 asURL]
    $f3 setExpression ""
    set r2 [$f3 asURL]
    $f3 setExpression {file:///.doesNotExist}
    set url [$f3 asURL]
    set r3 [$url toString]
    list [java::isnull $r1] [java::isnull $r2] $r3
} {1 1 file:/.doesNotExist}


######################################################################
####
#
test FileAttribute-3.2 {asURL: $CLASSPATH} {
    set n3 [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set f3 [java::new ptolemy.kernel.attributes.FileAttribute $n3 "my FileAttribute"]
    set r1 [$f3 asFile]
    $f3 setExpression {$CLASSPATH/makefile}
    set url [$f3 asURL]
    set r2 [[$url -noconvert toString] endsWith makefile]
    $f3 setExpression {$CLASSPATH/DoesNotExist}
    catch {$f3 asURL} r3
    list [java::isnull $r1] $r2 "\n" $r3
} {1 1 {
} {ptolemy.kernel.util.IllegalActionException: Cannot find file in classpath: $CLASSPATH/DoesNotExist
  in .my NamedObj.my FileAttribute}}

######################################################################
####
#
test FileAttribute-4.1 {openForWriting} {
    set n4 [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set f4 [java::new ptolemy.kernel.attributes.FileAttribute $n4 "my FileAttribute"]
    # Don't use a URL here
    $f4 setExpression {./test.txt}
    set writer [$f4 openForWriting]
    # Use \n to make comparison easier
    $writer write "This is a test file\n"
    $writer write "that has two lines.\n"
    $f4 close
} {}

######################################################################
####
#
test FileAttribute-4.2 {openForReading} {

    # Reads test.txt that was created in 4.1

    set n4 [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set f4 [java::new ptolemy.kernel.attributes.FileAttribute $n4 "my FileAttribute"]
    $f4 setExpression {file:./test.txt}
    set reader [$f4 openForReading]
    set line1 [$reader readLine]
    set line2 [$reader readLine]
    set line3 [$reader readLine]
    $f4 close
    list $line1 $line2 $line3
} {{This is a test file} {that has two lines.} {}}

######################################################################
####
#
test FileAttribute-5.1 {setBaseDirectory, getBaseDirectory} {
    set n5 [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set f5 [java::new ptolemy.kernel.attributes.FileAttribute $n5 "my FileAttribute"]
    # Don't use a URL here
    $f5 setExpression {./test.txt}
    set r1 [$f5 getBaseDirectory]
    set uri [java::new java.net.URI [$f5 getExpression]]
    $f5 setBaseDirectory $uri
    set f5clone [java::cast ptolemy.kernel.attributes.FileAttribute [$f5 clone]]
    set r3 [$f5clone getBaseDirectory]
    list [java::isnull $r1] [$uri equals [$f5 getBaseDirectory]] \
	[java::isnull $r3]
} {1 1 1}
