# Tests for the MoMLParser class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#
test ModalModel-1.1 {Create a ModalModel} {
    set moml_1 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel"/>
</entity>
}
    set moml "$header $moml_1"
    set parser [java::new ptolemy.moml.MoMLParser]

    # Filter out graphical classes while inside MoMLParser so that
    # these test will run at night.
    $parser setMoMLFilters [java::null] 
    $parser addMoMLFilter \
	    [java::new ptolemy.moml.filter.RemoveGraphicalClasses]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
    <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
        <property name="directorClass" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.fsm.kernel.HSDirector">
            <property name="style" class="ptolemy.actor.gui.style.ChoiceStyle">
                <property name="style0" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.fsm.kernel.HSDirector">
                </property>
                <property name="style1" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.fsm.kernel.FSMDirector">
                </property>
                <property name="style2" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.hdf.kernel.HDFFSMDirector">
                </property>
            </property>
        </property>
        <property name="_Director" class="ptolemy.domains.fsm.kernel.HSDirector">
            <property name="controllerName" class="ptolemy.kernel.util.StringAttribute" value="_Controller">
            </property>
        </property>
        <entity name="_Controller" class="ptolemy.domains.fsm.modal.ModalController">
            <property name="initialStateName" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="finalStateNames" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="_nonStrictMarker" class="ptolemy.kernel.util.Attribute">
            </property>
        </entity>
    </entity>
</entity>
}

######################################################################
####
#
test ModalModel-1.2 {Create a port in the modal model and check controller} {
    set moml_2 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <port name="foo"/>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    set controller [$modal getEntity "_Controller"]
    _testEntityGetPorts $controller
} {foo}

######################################################################
####
#
test ModalModel-1.2.1 {Rename a port in the modal model and check controller} {
    set moml_2 {
<entity name="test2">
  <entity name="modal model">
    <port name="foo">
      <rename name="bar"/>
    </port>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    set controller [$modal getEntity "_Controller"]
    _testEntityGetPorts $controller
} {bar}

######################################################################
####
#
test ModalModel-1.3 {Remove a port in the modal model and check controller} {
    set moml_3 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <deletePort name="bar"/>
  </entity>
</entity>
}
    set moml "$header $moml_3"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    set controller [$modal getEntity "_Controller"]
    _testEntityGetPorts $controller
} {{}}

######################################################################
####
#
test ModalModel-1.4 {Create a port in the controller and check modal model} {
    set moml_4 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <entity name="_Controller">
      <port name="foo"/>
    </entity>
  </entity>
</entity>
}
    set moml "$header $moml_4"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    _testEntityGetPorts $modal
} {foo}

######################################################################
####
#
test ModalModel-1.4.1 {Rename a port in the controller and check modal model} {
    set moml_2 {
<entity name="test2">
  <entity name="modal model">
    <entity name="_Controller">
      <port name="foo">
        <rename name="bar"/>
      </port>
    </entity>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    _testEntityGetPorts $modal
} {bar}

######################################################################
####
#
test ModalModel-1.5 {Remove a port in the controller and check modal model} {
    set moml_5 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <entity name="_Controller">
      <deletePort name="bar"/>
    </entity>
  </entity>
</entity>
}
    set moml "$header $moml_5"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    _testEntityGetPorts $modal
} {{}}

######################################################################
####
#
test ModalModel-1.6 {Create a refinement in the modal model} {
    set moml_2 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <entity name="refinement" class="ptolemy.domains.fsm.modal.Refinement"/>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
    <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
        <property name="directorClass" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.fsm.kernel.HSDirector">
            <property name="style" class="ptolemy.actor.gui.style.ChoiceStyle">
                <property name="style0" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.fsm.kernel.HSDirector">
                </property>
                <property name="style1" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.fsm.kernel.FSMDirector">
                </property>
                <property name="style2" class="ptolemy.kernel.util.StringAttribute" value="ptolemy.domains.hdf.kernel.HDFFSMDirector">
                </property>
            </property>
        </property>
        <property name="_Director" class="ptolemy.domains.fsm.kernel.HSDirector">
            <property name="controllerName" class="ptolemy.kernel.util.StringAttribute" value="_Controller">
            </property>
        </property>
        <entity name="_Controller" class="ptolemy.domains.fsm.modal.ModalController">
            <property name="initialStateName" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="finalStateNames" class="ptolemy.kernel.util.StringAttribute">
            </property>
            <property name="_nonStrictMarker" class="ptolemy.kernel.util.Attribute">
            </property>
        </entity>
        <entity name="refinement" class="ptolemy.domains.fsm.modal.Refinement">
        </entity>
    </entity>
</entity>
}

######################################################################
####
#
test ModalModel-1.7 {Create a port in the modal model and check refinement} {
    set moml_2 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <port name="foo"/>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    set refinement [$modal getEntity "refinement"]
    _testEntityGetPorts $refinement
} {foo}

######################################################################
####
#
test ModalModel-1.8 {Rename a port in the modal model and check refinement} {
    set moml_2 {
<entity name="test2">
  <entity name="modal model">
    <port name="foo">
      <rename name="bar"/>
    </port>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    set refinement [$modal getEntity "refinement"]
    _testEntityGetPorts $refinement
} {bar}

######################################################################
####
#
test ModalModel-1.9 {Remove a port in the modal model and check refinement} {
    set moml_3 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <deletePort name="bar"/>
  </entity>
</entity>
}
    set moml "$header $moml_3"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    set refinement [$modal getEntity "refinement"]
    _testEntityGetPorts $refinement
} {{}}

######################################################################
####
#
test ModalModel-1.10 {Create a port in the refinement and check modal model} {
    set moml_4 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <entity name="refinement">
      <port name="foo"/>
    </entity>
  </entity>
</entity>
}
    set moml "$header $moml_4"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    _testEntityGetPorts $modal
} {foo}

######################################################################
####
#
test ModalModel-1.11 {Rename a port in the refinement and check modal model} {
    set moml_2 {
<entity name="test2">
  <entity name="modal model">
    <entity name="refinement">
      <port name="foo">
        <rename name="bar"/>
      </port>
    </entity>
  </entity>
</entity>
}
    set moml "$header $moml_2"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    _testEntityGetPorts $modal
} {bar}

######################################################################
####
#
test ModalModel-1.12 {Remove a port in the refinement and check modal model} {
    set moml_5 {
<entity name="test2" class="ptolemy.actor.TypedCompositeActor">
  <entity name="modal model" class="ptolemy.domains.fsm.modal.ModalModel">
    <entity name="refinement">
      <deletePort name="bar"/>
    </entity>
  </entity>
</entity>
}
    set moml "$header $moml_5"
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set modal [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity "modal model"]]
    _testEntityGetPorts $modal
} {{}}
