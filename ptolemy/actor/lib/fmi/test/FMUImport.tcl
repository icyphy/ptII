# Test FMUImport
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2012-2014 The Regents of the University of California.
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

#set VERBOSE 1
if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

######################################################################
####
#
test FMUImport-1.1 {Test out importFMU} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile {$CLASSPATH/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu} [java::null]]
    set fmuFileParameter [java::new ptolemy.data.expr.FileParameter $e1 fmuFileParmeter]
    $fmuFileParameter setExpression [$fmuFile getCanonicalPath]
    java::call ptolemy.actor.lib.fmi.FMUImport importFMU $e1 $fmuFileParameter $e1 100.0 100.0 false
    set bouncingBall [$e1 getEntity {bouncingBall}]
    set moml [$bouncingBall exportMoML]
    regsub {value=".*/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu"} $moml {value="$CLASSPATH/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu"} moml2
    # Deal with backslashes in MS-DOS-based systems.  Why we need to do this in this day and age is beyond me.
    regsub {value=".*\\org\\ptolemy\\fmi\\fmu\\cs\\bouncingBall.fmu"} $moml2 {value="$CLASSPATH/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu"} moml3
    list $moml3
} {{<entity name="bouncingBall" class="ptolemy.actor.lib.fmi.FMUImport">
    <property name="fmuFile" class="ptolemy.data.expr.FileParameter" value="$CLASSPATH/org/ptolemy/fmi/fmu/cs/bouncingBall.fmu">
    </property>
    <property name="_location" class="ptolemy.kernel.util.Location" value="100.0, 100.0">
    </property>
    <port name="h" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="double">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_hide" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
    </port>
    <port name="der_h_" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <display name="der(h)"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="double">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_hide" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
    </port>
    <port name="v" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="double">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_hide" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
    </port>
    <port name="der_v_" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <display name="der(v)"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="double">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
            <property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <property name="_hide" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
    </port>
</entity>
}}


proc importFMU {fmuFileName} {
    set e1 [sdfModel 5]
    set fmuFile [java::call ptolemy.util.FileUtilities nameToFile $fmuFileName [java::null]]
    set fmuFileParameter [java::new ptolemy.data.expr.FileParameter $e1 fmuFileParmeter]
    $fmuFileParameter setExpression [$fmuFile getCanonicalPath]

    # Look for FMI-1.0 ME fmus that are named *ME1.fmu
    set modelExchange false
    if [regexp {ME1.fmu$} $fmuFile] {
        set modelExchange true
    }

    java::call ptolemy.actor.lib.fmi.FMUImport importFMU $e1 $fmuFileParameter $e1 100.0 100.0 $modelExchange

    set fmuActorFileName [lindex [file split $fmuFileName] end]
    set fmuActorName [string range $fmuActorFileName 0 [expr {[string length $fmuActorFileName] -5}]]
    #puts "fmuActorFileName: $fmuActorFileName"
    #puts "fmuActorName: $fmuActorName"
    set entityList [$e1 entityList [java::call Class forName ptolemy.actor.lib.fmi.FMUImport]]
    set fmuActor [java::cast ptolemy.actor.lib.fmi.FMUImport [$entityList get 0]]
    puts [$fmuActor getFullName]
    #puts [$e1 exportMoML]
    #set moml [$fmuActor exportMoML]
    #return $moml
}

######################################################################
####
#

set i 0
set files [glob auto/*.fmu]
foreach file $files {
    incr i
    test FMUImport-2.1.$i "test $file" {
        importFMU $file
        # Success is not throwing an exception
        list {}
    } {{}}
}
