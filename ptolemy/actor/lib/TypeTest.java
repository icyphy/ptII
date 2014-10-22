/* An actor for testing type resolution.

 Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// TypeTest

/**
 <p>An actor that can be used for regression test of the type resolution
 system.  During the initialize phase, after type resolution has been
 performed, this actor compares resolved types in the model with types
 stored in several parameters.  If the types are the same, then the
 actor does nothing.  However, if the types are different, then the
 actor throws an exception.  Hence, as with the Test actor, this actor
 can be easily used to build automatically executed tests of the type
 system from within Vergil.</p>

<p> The types expected by this actor are stored in two parameters,
 each parameter contains a record of record of strings.  The outer
 record contains labels corresponding to the names of actors in the
 same level of hierarchy as this actor.  The inner record contains
 labels corresponding to the names of typeable objects in the
 appropriate actor.  The strings in the record correspond to string
 representations of the types of the typeable objects.  For the
 <i>portTypes</i> parameter, the typeable objects are assumed to be
 ports, and for the <i>parameterTypes</i> parameter, the objects are
 assumed to be parameters.</p>

 <p> Note that this actor only tests type resolution at one level of
 opaque hierarchy.  Hierarchical models should include multiple
 instances of this actor.  Since filling in the types manually is
 difficult, this actor includes a training mode, similar to the
 NonStrictTest actor.  This mode automatically fills in the type
 parameters.  Also note that it is not necessary to specify the types
 of all typeable objects.  Any objects for which no type is specified
 are not checked.</p>

 <p> During runtime, this actor consumes and ignores any input tokens.
 This makes it very easy to add this actor to an existing model without
 changing the behavior of the model.</p>

 <p><b>Note:</b> For some reason, the way this actor reaches into
 other actors is not thread safe. This actor does not work with PN
 or Rendezvous, therefore.</p>

 @author Steve Neuendorffer
 @version $Id$
 @see ptolemy.actor.lib.Test
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (ssachs)
 */
public class TypeTest extends Discard {
    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TypeTest(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        parameterTypes = new Parameter(this, "parameterTypes");
        parameterTypes.setExpression("");

        //  parameterTypes.setTypeEquals(
        //                 new RecordType(new String[0], new Type[0]));
        portTypes = new Parameter(this, "portTypes");
        portTypes.setExpression("");

        //  portTypes.setTypeEquals(
        //                 new RecordType(new String[0], new Type[0]));
        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setExpression("false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A record of record of strings representing the types of
     *  parameters of actors in the model.
     */
    public Parameter parameterTypes;

    /** A record of record of strings representing the types of ports
     *  of actors in the model.
     */
    public Parameter portTypes;

    /** If true, then do not check inputs, but rather collect them
     *  into the <i>portTypes</i> and <i>parameterTypes</i> arrays.
     *  This parameter is a boolean, and it defaults to false.
     */
    public Parameter trainingMode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize this actor.  If the types stored in the type
     *  parameters do not correspond to the types of corresponding
     *  typeable objects in the model, then throw an exception
     *  indicating a failed regression test.
     *  @exception IllegalActionException If the test fails.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Accumulate the types of ports and Parameters
        ArrayList portActorNameList = new ArrayList();
        ArrayList parameterActorNameList = new ArrayList();
        ArrayList portAssignments = new ArrayList();
        ArrayList parameterAssignments = new ArrayList();
        List entityList = ((CompositeEntity) getContainer()).entityList();

        for (Iterator i = entityList.iterator(); i.hasNext();) {
            ComponentEntity entity = (ComponentEntity) i.next();

            // Skip the type test actor itself.
            if (entity.equals(this)) {
                continue;
            }

            ArrayList portNames = new ArrayList();
            ArrayList portTypes = new ArrayList();

            for (Iterator ports = entity.portList().iterator(); ports.hasNext();) {
                TypedIOPort port = (TypedIOPort) ports.next();
                portNames.add(port.getName());
                portTypes.add(new StringToken(port.getType().toString()));
            }

            if (portNames.size() > 0) {
                portActorNameList.add(entity.getName());
                portAssignments
                        .add(new RecordToken((String[]) portNames
                                .toArray(new String[portNames.size()]),
                                (Token[]) portTypes.toArray(new Token[portTypes
                                        .size()])));
            }

            ArrayList paramNames = new ArrayList();
            ArrayList paramTypes = new ArrayList();

            for (Object element : entity.attributeList(Parameter.class)) {
                Parameter param = (Parameter) element;
                paramNames.add(param.getName());
                paramTypes.add(new StringToken(param.getType().toString()));
            }

            if (paramNames.size() > 0) {
                parameterActorNameList.add(entity.getName());
                parameterAssignments.add(new RecordToken((String[]) paramNames
                        .toArray(new String[paramNames.size()]),
                        (Token[]) paramTypes.toArray(new Token[paramTypes
                                .size()])));
            }
        }

        RecordToken actualPortTypes = new RecordToken(
                (String[]) portActorNameList.toArray(new String[portActorNameList
                        .size()]),
                (Token[]) portAssignments.toArray(new Token[portAssignments
                        .size()]));
        RecordToken actualParameterTypes = new RecordToken(
                (String[]) parameterActorNameList.toArray(new String[parameterActorNameList
                        .size()]),
                (Token[]) parameterAssignments
                        .toArray(new Token[parameterAssignments.size()]));

        if (((BooleanToken) trainingMode.getToken()).booleanValue()) {
            if (MessageHandler.isRunningNightlyBuild()) {
                throw new IllegalActionException(this,
                        NonStrictTest.TRAINING_MODE_ERROR_MESSAGE);
            } else {
                System.err.println("Warning: '" + this.getFullName()
                        + "' is in training mode, set the trainingMode "
                        + "parameter to false before checking in");
            }

            if (actualPortTypes.length() > 0) {
                portTypes.setToken(actualPortTypes);
            } else {
                portTypes.setToken((Token) null);
            }

            if (actualParameterTypes.length() > 0) {
                parameterTypes.setToken(actualParameterTypes);
            } else {
                parameterTypes.setToken((Token) null);
            }
        } else {
            RecordToken correctPortTypes = (RecordToken) portTypes.getToken();
            RecordToken correctParameterTypes = (RecordToken) parameterTypes
                    .getToken();

            if ((correctPortTypes == null || correctPortTypes.labelSet().size() == 0)
                    && (correctParameterTypes == null || correctParameterTypes
                            .labelSet().size() == 0)) {
                throw new IllegalActionException(this,
                        "TypeTest actor has no training data.");
            }

            if (correctPortTypes != null) {
                for (Object element : correctPortTypes.labelSet()) {
                    String actorName = (String) element;
                    RecordToken assignment = (RecordToken) correctPortTypes
                            .get(actorName);

                    for (Object element2 : assignment.labelSet()) {
                        String name = (String) element2;
                        StringToken value = (StringToken) assignment.get(name);

                        if (actualPortTypes.get(actorName) == null) {
                            throw new IllegalActionException(
                                    this,
                                    "actualPortTypes.get("
                                            + actorName
                                            + ") returned null.  Perhaps there is no "
                                            + "actor by that name?");
                        }

                        StringToken actualValue = (StringToken) ((RecordToken) actualPortTypes
                                .get(actorName)).get(name);

                        if (!value.equals(actualValue)) {
                            throw new IllegalActionException(
                                    this,
                                    "Type of port "
                                            + ((CompositeEntity) getContainer())
                                                    .getEntity(actorName)
                                                    .getFullName() + "." + name
                                            + " should have been " + value
                                            + " but was " + actualValue + ".");
                        }
                    }
                }
            }

            if (correctParameterTypes != null) {
                for (Object element : correctParameterTypes.labelSet()) {
                    String actorName = (String) element;
                    RecordToken assignment = (RecordToken) correctParameterTypes
                            .get(actorName);

                    for (Object element2 : assignment.labelSet()) {
                        String name = (String) element2;
                        StringToken value = (StringToken) assignment.get(name);
                        StringToken actualValue = (StringToken) ((RecordToken) actualParameterTypes
                                .get(actorName)).get(name);

                        if (!value.equals(actualValue)) {
                            throw new IllegalActionException(
                                    this,
                                    "Type of parameter "
                                            + ((CompositeEntity) getContainer())
                                                    .getEntity(actorName)
                                                    .getFullName() + "." + name
                                            + " should have been " + value
                                            + " but was " + actualValue + ".");
                        }
                    }
                }
            }
        }
    }
}
