/* This actor provides information to publish, register and update values in
 * the Ptolemy-HLA/CERTI framework.

@Copyright (c) 2013-2019 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package org.hlacerti.lib;

import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HlaPublisher

/**
 * This actor provides updates to shared objects in an HLA (High Level Architecture)
 * federation. The shared objects are are instances of classes defined in a
 * FED file (Federation Execution Data). Each class has attributes, and this
 * actor sends updates to an attribute of an instance of a class when it
 * receives data on its input.  If there are instances of {@link HlaSubscriber}
 * that refer to the same attribute of the same instance, then those will be
 * notified of the update. The HlaSubscriber will produce an output event with
 * the same data and a time stamp that depends on the time management parameters
 * of the {@link HlaManager}.
 *
 * This actor requires that there be an instance of {@link HlaManager} in the
 * same model. That instance references the FED file that is required to
 * define the class referred to in this HlaPublisher, and that class is
 * required to have an attribute with a name matching the name given here.
 *
 * The name of the instance is arbitrary. If an instance with the specified
 * name does not already exist, it will be created during initialization of
 * the model. If two instances of this HlaPublisher actor refer to the
 * same instance name, then they will send updates to the same HLA instance,
 * but they are required to update distinct attributes of that instance.
 *
 * During initialization, the HlaManager will notify the RTI (Run Time
 * Infrastructure) of the intent to update this particular attribute
 * of this particular instance of the class.
 *
 * This actor throws an exception if the attribute name or the class name or
 * the instance name is empty. An exception is also thrown if the class name
 * or the attribute name is not defined in the FED file.
 * </p><p>
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler, David Come
 *  @version $Id: HlaPublisher.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaPublisher extends TypedAtomicActor implements HlaUpdatable {

    /** Construct the HlaPublisher actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public HlaPublisher(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // The single input port of the actor.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(false);

        // HLA attribute name as defined in the FOM.
        attributeName = new Parameter(this, "attributeName");
        attributeName.setDisplayName("Name of the attribute to publish");
        attributeName.setTypeEquals(BaseType.STRING);
        attributeName.setExpression("\"HLAattributName\"");
        attributeChanged(attributeName);

        // HLA object class as defined in the FOM.
        classObjectName = new Parameter(this, "classObjectName");
        classObjectName.setDisplayName("Object class in FOM");
        classObjectName.setTypeEquals(BaseType.STRING);
        classObjectName.setExpression("\"HLAobjectClass\"");
        attributeChanged(classObjectName);

        // HLA class instance name given by the user.
        classInstanceName = new Parameter(this, "classInstanceName");
        classInstanceName.setDisplayName("Name of the HLA class instance");
        classInstanceName.setTypeEquals(BaseType.STRING);
        classInstanceName.setExpression("\"HLAclassInstanceName\"");
        attributeChanged(classInstanceName);

        // CERTI message buffer encapsulation
        useCertiMessageBuffer = new Parameter(this, "useCertiMessageBuffer");
        useCertiMessageBuffer.setTypeEquals(BaseType.BOOLEAN);
        useCertiMessageBuffer.setExpression("false");
        useCertiMessageBuffer.setDisplayName("use CERTI message buffer");
        attributeChanged(useCertiMessageBuffer);

        // Initialize default private values.
        _hlaManager = null;
        _useCertiMessageBuffer = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The HLA attribute name the HLASubscriber is mapped to. */
    public Parameter attributeName;

    /** The object class of the HLA attribute to publish. */
    public Parameter classObjectName;

    /** The name of the HLA class instance for this HlaSubscriber. */
    public Parameter classInstanceName;

    /** The input port. */
    public TypedIOPort input = null;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    public Parameter useCertiMessageBuffer;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the attributeChanged method of the parent. Check if the
     *  user has set all information related to HLA to publish.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If one of the parameters is
     *  empty.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == attributeName) {
            String value = ((StringToken) attributeName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        } else if (attribute == classObjectName) {
            String value = ((StringToken) classObjectName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        }
        if (attribute == classInstanceName) {
            String value = ((StringToken) classInstanceName.getToken())
                    .stringValue();
            if (value.compareTo("") == 0) {
                throw new IllegalActionException(this,
                        "Cannot have empty name !");
            }
        } else if (attribute == useCertiMessageBuffer) {
            _useCertiMessageBuffer = ((BooleanToken) useCertiMessageBuffer
                    .getToken()).booleanValue();
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HlaPublisher newObject = (HlaPublisher) super.clone(workspace);
        newObject._hlaManager = _hlaManager;
        newObject._useCertiMessageBuffer = _useCertiMessageBuffer;
        return newObject;
    }

    /** Retrieve and check if there is one and only one {@link HlaManager}
     *  deployed in the Ptolemy model. The {@link HlaManager} provides the
     *  method to publish the attributes of a class, register an instance of a
     *  class and update the value of a HLA attribute of a class instance to
     *  the HLA/CERTI Federation.
     *  @exception IllegalActionException If there is zero or more than one
     *  {@link HlaManager} per Ptolemy model.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Find the HlaManager by looking into container
        // (recursively if needed).
        // FIXMEjc: it really looks recursively? The HlaManager must be in a DE
        // top level model.
        CompositeActor ca = (CompositeActor) this.getContainer();

        List<HlaManager> hlaManagers = ca.attributeList(HlaManager.class);
        if (hlaManagers.size() > 1) {
            throw new IllegalActionException(this,
                    "Only one HlaManager attribute is allowed per model");
        } else if (hlaManagers.size() < 1) {
            throw new IllegalActionException(this,
                    "A HlaManager attribute is required to use this actor");
        }

        // Here, we are sure that there is one and only one instance of the
        // HlaManager in the Ptolemy model.
        _hlaManager = hlaManagers.get(0);
    }

    /** All tokens, received in the input port, are transmitted to the
     *  {@link HlaManager} for a publication to the HLA/CERTI Federation.
     */
    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        for (int i = 0; i < input.getWidth(); ++i) {
            if (input.hasToken(i)) {
                Token in = input.get(i);
                _hlaManager.updateHlaAttribute(this, in);
                if (_debugging) {
                    _debug(this.getDisplayName()
                            + " Called fire() - the update value \""
                            + in.toString() + "\" of the HLA Attribute \""
                            + this.getName() + "\" has been sent to \""
                            + _hlaManager.getDisplayName() + "\"");
                }
            }
        }
    }

    /** Return the HLA attribute name indicated in the HlaPublisher actor
     * that will be used by HLA services (publish and update). The attribute
     * must be defined in the FED file. It belongs to the class classObjectName.
     *  @return the HLA Attribute name.
     *  @exception IllegalActionException if a bad token string value is provided
     */
    public String getHlaAttributeName() throws IllegalActionException {
        String parameter = "";
        try {
            parameter = ((StringToken) attributeName.getToken()).stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad attributeName token string value");
        }
        return parameter;
    }

    /** Return the name of the HLA class instance indicated in the HlaPublisher
     *  actor that will be used by HLA services (register and update). It is
     *  chosen by the user.
     *  @return the HLA class instance name.
     *  @exception IllegalActionException if a bad token string value is provided.
     */
    public String getHlaInstanceName() throws IllegalActionException {
        String parameter = "";
        try {
            parameter = ((StringToken) classInstanceName.getToken())
                    .stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad classInstanceName token string value");
        }
        return parameter;
    }

    /** Return the HLA class object name indicated in the HlaPublisher actor.
     * The class must be defined in the FED file and has the attribute
     * attributeName.
     *  @return the HLA object class name.
     *  @exception IllegalActionException if a bad token string value is provided.
     */
    public String getHlaClassName() throws IllegalActionException {
        String parameter = "";
        try {
            parameter = ((StringToken) classObjectName.getToken())
                    .stringValue();
        } catch (IllegalActionException illegalActionException) {
            throw new IllegalActionException(this,
                    "Bad classObjectName token string value");
        }
        return parameter;
    }

    /** FIXME: This should probably not be here. See HlaManager. */
    public TypedIOPort getInputPort() {
        return input;
    }

    /** Indicate if the HLA publisher actor uses the CERTI message
     *  buffer API.
     *  @return true if the HLA publisher actor uses the CERTI message and
     *  false if it doesn't.
     */
    public boolean useCertiMessageBuffer() {
        // XXX: FIXME: where is the exception management ?
        return _useCertiMessageBuffer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A reference to the associated {@link HlaManager}. */
    private HlaManager _hlaManager;

    /** Indicate if the event is wrapped in a CERTI message buffer. */
    private boolean _useCertiMessageBuffer;

}
