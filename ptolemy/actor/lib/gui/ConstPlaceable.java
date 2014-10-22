/* A constant source that is placeable.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.gui;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.lib.Const;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ConstPlaceable

/**
 Produce a constant output from an actor that is placeable in a GUI.

 <p>The value of the
 output is that of the token contained by the <i>value</i> parameter,
 which by default is an IntToken with value 1. The type of the output
 is that of <i>value</i> parameter.
 <p>
 If the trigger port is connected, then this actor fires only if a token is
 provided on any channel of the trigger port. The value of that token
 does not matter. Specifically, if there is no such token, the prefire()
 method returns false.</p>

 @author Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bilung)
 */
public class ConstPlaceable extends Const implements PortablePlaceable {
    /** Construct a constant source with the given container and name.
     *  Create the <i>value</i> parameter, initialize its value to
     *  the default value of an IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ConstPlaceable(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the token in the <i>value</i> parameter to the output.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _getImplementation().setValue(value.getToken());
    }

    /** Place the visual representation of the actor into the specified container.
     *  @param container The container in which to place the object, or
     *   null to specify that there is no current container.
     */
    @Override
    public void place(PortableContainer container) {
        _getImplementation().place(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the right instance of the implementation depending upon the
     *  of the dependency specified through dependency injection.
     *  If the instance has not been created, then it is created.
     *  If the instance already exists then return the same.
     *
     *        <p>This code is used as part of the dependency injection needed for the
     *  HandSimDroid project, see $PTII/ptserver.  This code uses dependency
     *  inject to determine what implementation to use at runtime.
     *  This method eventually reads ptolemy/actor/ActorModule.properties.
     *  {@link ptolemy.actor.injection.ActorModuleInitializer#initializeInjector()}
     *  should be called before this method is called.  If it is not
     *  called, then a message is printed and initializeInjector() is called.</p>
     *  @return the implementation.
     */
    private TextFieldContainerInterface _getImplementation() {
        if (_implementation == null) {
            if (PtolemyInjector.getInjector() == null) {
                System.err.println("Warning: main() did not call "
                        + "ActorModuleInitializer.initializeInjector(), "
                        + "so Const is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            _implementation = PtolemyInjector.getInjector().getInstance(
                    TextFieldContainerInterface.class);
        }
        return _implementation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Implementation of the ConstInterface.  This code is used as part
     *  of the dependency injection needed for the HandSimDroid project, see
     *  $PTII/ptserver.  Note that if you get a NullPointerException here,
     *  then the solution is to have your main() method call
     *  ActorModuleInitializer.initializeInjector().
     */
    private TextFieldContainerInterface _implementation;
}
