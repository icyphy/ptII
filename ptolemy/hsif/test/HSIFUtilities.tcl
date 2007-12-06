# Tests for the HSIFUtilities class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003-2007 The Regents of the University of California.
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
} {{ptolemy.domains.ct.kernel.CTMixedSignalDirector {.new_swimmingpool.CT Director}}}

test HSIFUtilities-1.2 {Convert the Thermostat example using main to increase code coverage} {
    set args [java::new {String[]} 2 \
	[list "../demo/Thermostat/Thermostat.xml" "Thermostat_moml.xml"]]
    java::call ptolemy.hsif.HSIFUtilities main $args
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parseFile Thermostat_moml.xml]
    set composite [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$composite getDirector]	
    list [$director toString]
} {{ptolemy.domains.ct.kernel.CTMixedSignalDirector {.Thermostat.CT Director}}}

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

    list \
	[epsilonDiff [enumToTokenValues [$rec0 getRecord 0]] \
	     {27.0 27.0 27.0 27.0 26.9999987500004 26.9999987500004 26.9999987500004 26.9999987500004 27.0000602053798 27.0003674745949 27.001903628647 27.0095796003794 27.0389811422009 27.048992418693 27.048992418693 27.048992418693 27.048992418693 27.0782366280489 27.1073640971218 27.1363752919286 27.1652706766259 27.1700933587218 27.1700933587218 27.1700933587218 27.1700933587218 27.1941584221073 27.2179689598283 27.2179689598283 27.2179689598283 27.2179689598283 27.2465386298597 27.2749942523092 27.3033362824446 27.3315651737162 27.3596813777644 27.3876853444265 27.4155775217442 27.4433583559706 27.4710282915775 27.4985877712622 27.526037235955 27.553377124826 27.580607875292 27.6077299230239 27.6347437019533 27.6616496442797 27.6884481804772 27.7151397393014 27.7417247477967 27.7682036313023 27.7945768134599 27.8208447162198 27.847007759848 27.8730663629328 27.8990209423915 27.9205458548559 27.9205458548559 27.9205458548559}] \
	[epsilonDiff [enumToTokenValues [$rec1 getRecord 0]] \
	     {0.0 0.0 0.0 0.0 1.25E-4 1.25E-4 1.25E-4 1.25E-4 7.5E-4 0.003875 0.0195 0.097625 0.397625 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.0 0.0 0.2509375 0.50005 0.0 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005 0.50005}]
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

    puts [enumToTokenValues [$rec0 getRecord 0]]

    list \
	[epsilonDiff [enumToTokenValues [$rec0 getRecord 0]] \
	     {0.0 0.2 0.8 1.4 2.0 2.6 3.2 3.8 4.0001 4.0001 4.0001 4.0001 3.7110615242187 3.4009854118313 3.1151824373718 0.0 3.1151824373718 3.1151824373718 3.4852901367283 4.0852901367283 4.6852901367283 5.2852901367283 5.8852901367283 6.4852901367283 7.0852901367283 7.1152824373718 0.0 7.1152824373718 7.1152824373718 6.8534657016512 6.3833385805403 5.9350633763282 5.5412299979421 0.0 5.5412299979421 5.5412299979421 5.8465246671443 6.4465246671443 7.0465246671443 7.6465246671443 8.2465246671443 8.8465246671443 9.00005 9.00005 9.00005 9.00005 8.3497260746094 7.8118601243344 7.2978337177104 6.8069727613011 6.3387373529107 5.9999478160614 0.0 5.9999478160614 5.9999478160614 6.1485566326196 6.7485566326196 7.3485566326196 7.9485566326196 8.5485566326196 9.00005 0.0 9.00005 9.00005 8.3497260746094 7.8118601243344 7.2978337177104 6.8069727613011 6.3387373529107 5.9999478160614 0.0 5.9999478160614 5.9999478160614 6.1485566326196 6.7485566326196 7.3485566326196 7.9187312984196 7.9187312984196 7.9187312984196}] \
	[epsilonDiff [enumToTokenValues [$rec1 getRecord 0]] \
	     {0.0 0.1 0.4 0.7 1.0 1.3 1.6 1.9 2.00005 2.00005 0.0 0.0 0.15 0.3245003094091 0.50005 0.0 0.0 0.0 0.1850538496783 0.4850538496783 0.7850538496783 1.0850538496783 1.3850538496783 1.6850538496783 1.9850538496783 2.00005 0.0 0.0 0.0 0.0749807516087 0.2171049364913 0.362729538684 0.50005 0.0 0.0 0.0 0.1526473346011 0.4526473346011 0.7526473346011 1.0526473346011 1.3526473346011 1.6526473346011 1.7294100010289 1.7294100010289 1.7294100010289 1.7294100010289 1.8044100010289 1.8709947624257 1.939059372547 2.0086883972304 2.0799551592338 2.1348835839393 0.0 0.0 0.0 0.0743044082791 0.3743044082791 0.6743044082791 0.9743044082791 1.2743044082791 1.5000510919693 0.0 1.5000510919693 1.5000510919693 1.5750510919693 1.6416358533661 1.7097004634873 1.7793294881708 1.8505962501741 1.9055246748796 0.0 0.0 0.0 0.0743044082791 0.3743044082791 0.6743044082791 0.9593917411791 0.9593917411791 0.9593917411791}]
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

    set clock [java::new ptolemy.domains.ct.lib.ContinuousClock $toplevel clock]

    set clockOutput [java::field [java::cast ptolemy.actor.lib.Source $clock] output]
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
