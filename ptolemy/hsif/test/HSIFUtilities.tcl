# Tests for the HSIFUtilities class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003-2010 The Regents of the University of California.
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

test HSIFUtilities-1.1 {Convert the SwimmingPool example} {
    java::call ptolemy.hsif.HSIFUtilities HSIFToMoML \
	../demo/SwimmingPool/SwimmingPool.xml SwimmingPool_moml.xml
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parseFile SwimmingPool_moml.xml]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	
    list [$director toString]
} {{ptolemy.domains.continuous.kernel.ContinuousDirector {.new_swimmingpool.Continuous Director}}}

test HSIFUtilities-1.2 {Convert the Thermostat example using main to increase code coverage} {
    set args [java::new {String[]} 2 \
	[list "../demo/Thermostat/Thermostat.xml" "Thermostat_moml.xml"]]
    java::call ptolemy.hsif.HSIFUtilities main $args
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parseFile Thermostat_moml.xml]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	
    list [$director toString]
} {{ptolemy.domains.continuous.kernel.ContinuousDirector {.Thermostat.Continuous Director}}}

test HSIFUtilities-2.1 {Convert the SwimmingPool example, connect it up and run it} {
    java::call ptolemy.hsif.HSIFUtilities HSIFToMoML \
	../demo/SwimmingPool/SwimmingPool.xml SwimmingPool_moml.xml
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser purgeModelRecord SwimmingPool_moml.xml
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
		      [$parser parseFile SwimmingPool_moml.xml]]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	

    set relationT [$toplevel getRelation T]
    set relationTimer [$toplevel getRelation timer]

    set rec0 [java::new ptolemy.actor.lib.Recorder $toplevel rec0]
    set rec0Input [java::field [java::cast ptolemy.actor.lib.Sink $rec0] input]
    $rec0Input link $relationT

    set rec1 [java::new ptolemy.actor.lib.Recorder $toplevel rec1]
    set rec1Input [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]
    $rec1Input link $relationTimer

    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "myManager"]
    $toplevel setManager $manager
    [$toplevel getManager] execute

    #puts [enumToTokenValues [$rec0 getRecord 0]]
    #puts [enumToTokenValues [$rec1 getRecord 0]]
    list \
	[epsilonDiff [enumToTokenValues [$rec0 getRecord 0]] \
	     {27.0 27.0 26.9999987500004 26.9999987500004 27.0098250994764 27.0392256612873 27.0489924186935 27.0489924186935 27.0587534884395 27.087958732473 27.1170473917697 27.1460199317255 27.1700933586834 27.1700933586834 27.1796930721381 27.2084155361092 27.2179689597947 27.2179689597947 27.2275048832455 27.2560364867187 27.2844541945684 27.3127584614555 27.3409497402265 27.3690284819198 27.3969951357734 27.424850149232 27.4525939679542 27.4802270358193 27.5077497949351 27.5351626856442 27.5624661465316 27.5896606144313 27.6167465244336 27.6437243098921 27.6705944024303 27.6973572319487 27.7240132266319 27.7505628129548 27.7770064156904 27.8033444579155 27.8295773610185 27.8557055447053 27.8817294270064 27.9076494242839 27.9205458547977 27.9205458547977}] \
	[epsilonDiff [enumToTokenValues [$rec1 getRecord 0]] \
	     {0.0 0.0 1.25E-4 1.25E-4 0.100125 0.400125 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.0 0.1 0.4 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005}]
} {{} {}}

test HSIFUtilities-2.2 {Convert the Thermostat example, connect it up and run it} {
    java::call ptolemy.hsif.HSIFUtilities HSIFToMoML \
	../demo/Thermostat/Thermostat.xml Thermostat_moml.xml
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser purgeModelRecord Thermostat_moml.xml
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
		      [$parser parseFile Thermostat_moml.xml]]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	

    set relationT [$toplevel getRelation T]
    set relationt [$toplevel getRelation t]

    set rec0 [java::new ptolemy.actor.lib.Recorder $toplevel rec0]
    set rec0Input [java::field [java::cast ptolemy.actor.lib.Sink $rec0] input]
    $rec0Input link $relationT

    set rec1 [java::new ptolemy.actor.lib.Recorder $toplevel rec1]
    set rec1Input [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]
    $rec1Input link $relationt

    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "myManager"]
    $toplevel setManager $manager
    [$toplevel getManager] execute

    #puts [enumToTokenValues [$rec0 getRecord 0]]
    #puts [enumToTokenValues [$rec1 getRecord 0]]
    list \
	[epsilonDiff [enumToTokenValues [$rec0 getRecord 0]] \
	     {0.0 0.2 0.8 1.4 2.0 2.6 3.2 3.8 4.0001 4.0001 3.8050117895833 3.5300699611313 3.274994827768 3.1151927183569 3.1151927183569 3.3151927183569 3.9151927183569 4.5151927183569 5.1151927183569 5.7151927183569 6.3151927183569 6.9151927183569 7.1152927183569 7.1152927183569 6.7682739630721 6.2792132920998 5.8254910753917 5.5412384853388 5.5412384853388 5.7412384853388 6.3412384853388 6.9412384853388 7.5412384853388 8.1412384853388 8.7412384853388 9.00005 9.00005 8.5611100614583 7.9425029758456 7.3685950850363 6.8361565229933 6.3421908067342 5.9999475672648 5.9999475672648 6.1999475672648 6.7999475672648 7.3999475672648 7.9999475672648 8.5999475672648 9.00005 9.00005 8.5611100614583 7.9425029758456 7.3685950850363 6.8361565229933 6.3421908067342 5.9999475672648 5.9999475672648 6.1999475672648 6.7999475672648 7.3999475672648 7.9187411438648 7.9187411438648}] \
	[epsilonDiff [enumToTokenValues [$rec1 getRecord 0]] \
	     {0.0 0.1 0.4 0.7 1.0 1.3 1.6 1.9 2.00005 0.0 0.1 0.25 0.4 0.50005 0.0 0.1 0.4 0.7 1.0 1.3 1.6 1.9 2.00005 0.0 0.1 0.25 0.4 0.50005 0.0 0.1 0.4 0.7 1.0 1.3 1.6 1.7294057573306 1.7294057573306 1.7794057573306 1.8544057573306 1.9294057573306 2.0044057573306 2.0794057573306 2.134878876315 0.0 0.1 0.4 0.7 1.0 1.3 1.5000512163676 1.5000512163676 1.5500512163676 1.6250512163676 1.7000512163676 1.7750512163676 1.8500512163676 1.9055243353519 0.0 0.1 0.4 0.7 0.9593967883 0.9593967883}]
} {{} {}}

test HSIFUtilities-2.3 {Convert the HybridAutomatonNot example, connect it up and run it, see https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=62} {
    java::call ptolemy.hsif.HSIFUtilities HSIFToMoML \
	HybridAutomatonNot.xml HybridAutomatonNot_moml.xml
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser purgeModelRecord HybridAutomatonNot_moml.xml
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
		      [$parser parseFile HybridAutomatonNot_moml.xml]]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	

    set relationX [$toplevel getRelation x]
    set relationY [$toplevel getRelation y]

    $parser setContext $toplevel
    set moml "<entity name=\"clock\" class=\"ptolemy.domains.continuous.lib.ContinuousClock\"/>"

    set topl [java::cast ptolemy.actor.TypedCompositeActor \
    		   [$parser parse $moml]]
    set clock [$topl getEntity clock]
    #set clock [java::new ptolemy.domains.ct.lib.ContinuousClock $toplevel clock]

    #set clockOutput [java::field [java::cast ptolemy.actor.lib.Source $clock] output]
    set clockOutput [$clock  getPort output]
    #set p [java::cast ptolemy.data.expr.Parameter [$clock getAttribute ContinuousClock.values]]
    set p [getParameter $clock values]
    $p setExpression {{true, false}}
    $clockOutput link $relationX

    set rec0 [java::new ptolemy.actor.lib.Recorder $toplevel rec0]
    set rec0Input [java::field [java::cast ptolemy.actor.lib.Sink $rec0] input]
    $rec0Input link $relationY


    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "myManager"]
    $toplevel setManager $manager
    catch {[$toplevel getManager] execute} errMsg

    #list [epsilonDiff [enumToTokenValues [$rec0 getRecord 0]] {}]
    list $errMsg
} {{java.lang.RuntimeException: ptolemy.kernel.util.IllegalActionException: Cannot find port or variable with the name: State.y.initialState
  in .NOT_BLOCK.NOT._Controller and .NOT_BLOCK.NOT._Controller.idt1id18id18.setActions}}
