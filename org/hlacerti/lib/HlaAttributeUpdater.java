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
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HlaAttributeUpdater

/**
 * This actor is a kind-of output port that sends the data on its input to the
 * HLA federation through the RTI (Run Time Infrastructure). In HLA, the
 * terminology is that this actor "updates" the specified attribute value of
 * the specified instance whenever there is a data on its input. This attribute
 * value will be "reflected" by all federates that have "subscribed" to this
 * attribute. The time stamp of this update depends on the time management
 * parameters of the {@link HlaManager}, as explained in the documentation for
 * that class. This actor assumes that there is exactly one HlaManager in the
 * model that contains this actor.
 * <p>
 * The attribute of an instance that this actor updates is specified using
 * three parameters: <i>attributeName</i>, <i>className</i>, and <i>instanceName</i>.
 * The updates will be reflected, for example, by an
 * {@link HlaAttributeReflector} whose three parameters match those of this actor.
 * The <i>className</i> and <i>attributeName</i> are required to match
 * a class and attribute specified in the FED file that is specified in the
 * HlaManager. The <i>instanceName</i> is an arbitrary name chosen by the
 * designer for the instance of the class. It specifies the instance of the
 * specified class whose attribute this actor updates.
 * <p>
 * If the specified class does not have an attribute with the specified
 * <i>attributeName</i>, as defined in the FED file, or there is no class
 * matching <i>className</i> in the FED file, then an exception will be thrown.
 * An exception is also thrown if the attribute name or the class name or
 * the instance name is empty.
 * <p>
 * If a federate intends to update <i>N</i> attributes of an instance, then it
 * must contain <i>N</i> HlaAttributeUpdater actors with the same
 * <i>instanceName</i> and same <i>className</i>. A federate can update
 * attributes of <i>M</i> different instances of a same class. In this case,
 * the federate must contain <i>M</i> HlaAttributeUpdater actors for each
 * attribute. During initialization, the HlaManager will notify the RTI of this
 * intention in two steps:
 * <p> - it "publishes" a list with all attributes of class by gathering
 * the parameter <i>attributeName</i> of all HlaAttributeReflectable actors
 * having the same <i>className</i>,
 * <p> it "registers" each different <i>instanceName</i> of a class <i>className</i>.
 * <p>
 * The data type of the <i>input</i> port of this actor must have the same type
 * of the attribute value this actor updates (as defined in the FOM). The data
 * type is used to define  the bytes that are transported via the HLA RTI.
 * Currently, only a small set of primitive data types are supported.
 *
 *  @author Gilles Lasnier, Contributors: Patricia Derler, David Come
 *  @version $Id: HlaAttributeUpdater.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Red (glasnier)
 */
public class HlaAttributeUpdater extends TypedAtomicActor implements HlaUpdatable {

    /** Construct the HlaAttributeUpdater actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public HlaAttributeUpdater(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // The single input port of the actor.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(false);

        // HLA attribute name.
        attributeName = new StringParameter(this, "attributeName");

        // HLA object class in FOM.
        className = new StringParameter(this, "className");

        // HLA class instance name given by the user.
        instanceName = new StringParameter(this, "instanceName");

        // Initialize default private values.
        _hlaManager = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the attribute this actor updates.
     *  This defaults to an empty string, but it must be non-empty to run the model.
     */
    public StringParameter attributeName;

    /** The name of the class whose attribute this actor updates.
     *  This defaults to an empty string, but it must be non-empty to run the model.
     */
    public StringParameter className;

    /** The name of the instance of the class to whose attribute this actor updates.
     *  This defaults to an empty string, but it must be non-empty to run the model.
     */
    public StringParameter instanceName;

    /** The input port providing the new value that will update the specified
     *  attribute of the specified instance.
     *  The type of the input port corresponds to the type of the attribute
     *  (that would be defined in the FOM, but is not described in the FED file).
     *  This type  must be the same as <i>attributeType</a> in {@link HlaAttributeReflector}.
     */
    public TypedIOPort input = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {

        // Manage public members.
        HlaAttributeUpdater newObject = (HlaAttributeUpdater) super.clone(workspace);

        newObject._hlaManager = _hlaManager;

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

    /** Ask the {@link HlaManager} for updating the event in the input port
     * (with time-stamp t) to the federation, by calling the UAV service (with
     * time-stamp t') related to the HLA attribute of an instance (mapped to
     * this actor). The value of t' depends on t and the time management
     * parameters used (NER or TAR, see {@link HlaManager} code).
     *
     */
    @Override
    public void fire() throws IllegalActionException {

        super.fire();

        for (int i = 0; i < input.getWidth(); ++i) {
            if (input.hasToken(i)) {
                Token in = input.get(i);
                _hlaManager.updateHlaAttribute(this, in);
                // FIXME: check if the log is correct
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

    /** Return the value of the <i>attributeName</i> parameter.
     *  @return The value of the <i>attributeName</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaAttributeName() throws IllegalActionException {
        String name = ((StringToken) attributeName.getToken()).stringValue();
        if (name.trim().equals("")) {
            throw new IllegalActionException(this,
                    "Cannot have an empty attributeName!");
        }
        return name;
    }

    /** Return the value of the <i>className</i> parameter.
     *  @return The value of the <i>className</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaClassName() throws IllegalActionException {
        String name = ((StringToken) className.getToken()).stringValue();
        if (name.trim().equals("")) {
            throw new IllegalActionException(this,
                    "Cannot have an empty className!");
        }
        return name;
    }

    /** Return the value of the <i>instanceName</i> parameter.
     *  @return The value of the <i>instanceName</i> parameter.
     *  @exception IllegalActionException If the class name is empty.
     */
    public String getHlaInstanceName() throws IllegalActionException {
        String name = ((StringToken) instanceName.getToken()).stringValue();
        if (name.trim().equals("")) {
            throw new IllegalActionException(this,
                    "Cannot have an empty instanceName!");
        }
        return name;
    }

    /** FIXME: This should probably not be here. See HlaManager. */
    public TypedIOPort getInputPort() {
        return input;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A reference to the associated {@link HlaManager}. */
    private HlaManager _hlaManager;
}
