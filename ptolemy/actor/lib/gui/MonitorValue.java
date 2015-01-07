/* Monitor input values.

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

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.lib.Sink;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// MonitorValue

/**
 Monitor inputs by setting the <i>value</i> parameter equal
 to each arriving token.  This actor can be used with
 an icon that displays the value of a parameter to get
 on-screen display of values in a diagram. The value is
 updated only in postfire.

 <p>Note that the icon for this actor is defined in
 <code>ptolemy/actor/lib/genericsinks.xml</code>, which looks something
 like
 <pre>
&lt;entity name="MonitorValue" class="ptolemy.actor.lib.MonitorValue"&gt;
&lt;doc&gt;Monitor and display values&lt;/doc&gt;
   &lt;property name="displayWidth" class="ptolemy.data.expr.Parameter" value="20"/&gt;
   &lt;property name="_icon" class="ptolemy.vergil.icon.UpdatedValueIcon"&gt;
      &lt;property name="attributeName" value="value"/&gt;
      &lt;property name="displayWidth" value="displayWidth"/&gt;
   &lt;/property&gt;
&lt;/entity&gt;
 </pre>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 */
public class MonitorValue extends Sink implements PortablePlaceable {
    /** Construct an actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MonitorValue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        initial = new Parameter(this, "initial");

        value = new Parameter(this, "value");

        // The value parameter should not be persistent so that when
        // we save and load
        // doc/books/systems/types/test/auto/ObjectType.xml we avoid
        // an error because ObjectTokens are not parseable.
        value.setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The most recently seen input value.  This parameter has the same
     *  type as the input.
     */
    public Parameter value;

    /** The initial value to be displayed.
     */
    public Parameter initial;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the actor by clearing the display. */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        value.setExpression(initial.getExpression());
        value.validate();
    }

    /** Place the visual representation of the actor into the specified container.
     *  @param container The container in which to place the object, or
     *   null to specify that there is no current container.
     */
    @Override
    public void place(PortableContainer container) {
        _getImplementation().place(container);
    }

    /** Read at most one token from the input and record its value.
     *  @exception IllegalActionException If there is no director.
     *  @return True.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token oldToken = value.getToken();
            Token newToken = _getInputToken(0);
            if (oldToken == null || !oldToken.equals(newToken)) {
                value.setToken(newToken);
                value.validate();
                _getImplementation().setValue(newToken);
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the input port greater than or equal to
     *  <code>BaseType.GENERAL</code> in case backward type inference is
     *  enabled and the input port has no type declared.
     *
     *  @return A set of inequalities.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        HashSet<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()
                && input.getTypeTerm().isSettable()) {
            result.add(new Inequality(new TypeConstant(BaseType.GENERAL), input
                    .getTypeTerm()));
        }
        return result;
    }

    /** Return a token from the named input channel.
     *  This is a protected method to allow subclasses to override
     *  how inputs are observed.
     *  @param i The channel
     *  @return A token from the input channel or null if there is
     *   nothing to display.
     *  @exception IllegalActionException If reading the input fails.
     */
    protected Token _getInputToken(int i) throws IllegalActionException {
        if (input.hasToken(i)) {
            Token token = input.get(i);
            return token;
        } 
        return null;
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
                        + "so Monitor is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            _implementation = PtolemyInjector.getInjector().getInstance(
                    TextFieldContainerInterface.class);
        }

        try {
            _implementation.init(this);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(this, e,
                    "Failed to initialize implementation");
        } catch (IllegalActionException e) {
            throw new InternalErrorException(this, e,
                    "Failed to initialize implementation");
        }

        return _implementation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Implementation of the MonitorValueInterface. */
    private TextFieldContainerInterface _implementation;
}
