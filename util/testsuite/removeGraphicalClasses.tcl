# Procs that remove graphical classes
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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

# 
# Create a new RemoveGraphicalClasses filter and add more classes
# to it.
# This method is called in ptolemy/configs/test/allConfigs.tcl
# ptolemy/vergil/test/VergilConfiguration.tcl
# ptolemy/moml/test/MoMLWriterTiming.tcl
#
proc removeGraphicalClasses {parser} {
    puts {    If you get a 'X connection to xxx:11.0 broken' message, then}
    puts {    see $PTII/ptolemy/moml/filter/RemoveGraphicalClasses.java}
    puts {    Or, under Solaris, run java -verbose -classpath }
    puts {           ${PTII}/lib/ptjacl.jar:${PTII}/lib/diva.jar:${PTII}}
    puts {           tcl.lang.Shell xxx.tcl}


    set filter [java::new ptolemy.moml.filter.RemoveGraphicalClasses]
    $filter put "ptolemy.caltrop.actors.CalInterpreter" [java::null]
    $filter put "ptolemy.actor.lib.gui.PlotterBase" [java::null]
    $filter put "ptolemy.actor.lib.gui.Plotter" [java::null]
    $filter put "ptolemy.actor.lib.gui.BarGraph" [java::null]
    $filter put "ptolemy.actor.lib.gui.Display" [java::null]
    $filter put "ptolemy.actor.lib.gui.HistogramPlotter" [java::null]
    $filter put "ptolemy.actor.lib.gui.RealTimePlotter" [java::null]
    $filter put "ptolemy.actor.lib.gui.SequencePlotter" [java::null]
    $filter put "ptolemy.actor.lib.gui.SequenceScope" [java::null]
    $filter put "ptolemy.actor.lib.gui.SketchedSource" [java::null]
    $filter put "ptolemy.actor.lib.gui.TimedPlotter" [java::null]
    $filter put "ptolemy.actor.lib.gui.TimedScope" [java::null]
    $filter put "ptolemy.actor.lib.gui.XYPlotter" [java::null]
    $filter put "ptolemy.actor.lib.gui.XYScope" [java::null]
    $filter put "ptolemy.actor.lib.gui.MatrixVisualizer" [java::null]
    $filter put "ptolemy.actor.lib.gui.MatrixViewer" [java::null]
    $filter put "ptolemy.actor.lib.joystick.Joystick" [java::null]
    #$filter put "ptolemy.domains.tm.kernel.TMDirector" [java::null]
    $filter put "ptolemy.domains.sr.lib.NonStrictDisplay" [java::null]
    $filter put "ptolemy.domains.tm.kernel.SchedulePlotter" [java::null]
    # We don't build in the demo directory, SoundSensor should not be 
    # in the .xml file
    $filter put "ptolemy.domains.wireless.demo.WirelessSoundDetection.SoundSensor" [java::null]
    $filter put "ptolemy.domains.wireless.lib.GraphicalLocator" [java::null]
    $filter put "ptolemy.vergil.icon.CopyCatIcon" [java::null]

    $parser addMoMLFilter $filter
}
