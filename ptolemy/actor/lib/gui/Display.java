/* An actor that displays input data in a text area on the screen.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */

package ptolemy.actor.lib.gui;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Display

/**
 <p>
 Display the values of the tokens arriving on the input channels in a
 text area on the screen.  Each input token is written on a
 separate line.  The input type can be of any type.
 If the input happens to be a StringToken,
 then the surrounding quotation marks are stripped before printing
 the value of the token.  Thus, string-valued tokens can be used to
 generate arbitrary textual output, at one token per line.
 Tokens are read from the input only in
 the postfire() method, to allow them to settle in domains where they
 converge to a fixed point.
 </p><p>
  This actor accepts any type of data on its input port, therefore it
 doesn't declare a type, but lets the type resolution algorithm find
 the least fixed point. If backward type inference is enabled, and
 no input type has been declared, the input is constrained to be
 equal to <code>BaseType.GENERAL</code>. This will result in upstream
 ports resolving to the most general type rather than the most specific.
 </p><p>
 This actor has a <i>suppressBlankLines</i> parameter, whose default value
 is false. If this parameter is configured to be true, this actor does not
 put a blank line in the display.
 </p><p>
 Note that because of complexities in Swing, if you resize the display
 window, then, unlike the plotters, the new size will not be persistent.
 That is, if you save the model and then re-open it, the new size is
 forgotten.  To control the size, you should set the <i>rowsDisplayed</i>
 and <i>columnsDisplayed</i> parameters.
 </p><p>
 Note that this actor internally uses JTextArea, a Java Swing object
 that is known to consume large amounts of memory. It is not advisable
 to use this actor to log large output streams.</p>

 @author  Yuhong Xiong, Edward A. Lee Contributors: Ishwinder Singh
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (vogel)
 */
public class Display extends TypedAtomicActor implements PortablePlaceable {
    /** Construct an actor with an input multiport of type GENERAL.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */

    public Display(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setAutomaticTypeConversion(false);

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setExpression("10");
        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setExpression("40");

        suppressBlankLines = new Parameter(this, "suppressBlankLines");
        suppressBlankLines.setTypeEquals(BaseType.BOOLEAN);
        suppressBlankLines.setToken(BooleanToken.FALSE);

        title = new StringParameter(this, "title");
        title.setExpression("");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-15\" " + "width=\"40\" height=\"30\" "
                + "style=\"fill:lightGrey\"/>\n" + "<rect x=\"-15\" y=\"-10\" "
                + "width=\"30\" height=\"20\" " + "style=\"fill:white\"/>\n"
                + "<line x1=\"-13\" y1=\"-6\" x2=\"-4\" y2=\"-6\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"-2\" x2=\"0\" y2=\"-2\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"2\" x2=\"-8\" y2=\"2\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"6\" x2=\"4\" y2=\"6\" "
                + "style=\"stroke:grey\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    /** The input port, which is a multiport.
     */
    public TypedIOPort input;

    /** The vertical size of the display, in rows. This contains an
     *  integer, and defaults to 10.
     */
    public Parameter rowsDisplayed;

    /** The flag indicating whether this display actor suppress
     *  blank lines. The default value is false.
     */
    public Parameter suppressBlankLines;

    /** The title to put on top. Note that the value of the title
     *  overrides the value of the name of the actor or the display
     *  name of the actor.
     */
    public StringParameter title;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>rowsDisplayed</i>, then set
     *  the desired number of rows of the textArea, if there is one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rowsDisplayed) {
            int numRows = ((IntToken) rowsDisplayed.getToken()).intValue();

            if (numRows <= 0) {
                throw new IllegalActionException(this,
                        "rowsDisplayed: requires a positive value.");
            }

            if (numRows != _previousNumRows) {
                _previousNumRows = numRows;

                _getImplementation().setRows(numRows);

            }
        } else if (attribute == columnsDisplayed) {
            int numColumns = ((IntToken) columnsDisplayed.getToken())
                    .intValue();

            if (numColumns <= 0) {
                throw new IllegalActionException(this,
                        "columnsDisplayed: requires a positive value.");
            }

            if (numColumns != _previousNumColumns) {
                _previousNumColumns = numColumns;

                _getImplementation().setColumns(numColumns);

            }
        } else if (attribute == suppressBlankLines) {
            _isSuppressBlankLines = ((BooleanToken) suppressBlankLines
                    .getToken()).booleanValue();
        } else if (attribute == title) {
            _getImplementation().setTitle(title.stringValue());
        }

    }

    /** Free up memory when closing. */
    public void cleanUp() {
        _implementation.cleanUp();
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the textArea public variable to null.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Display newObject = (Display) super.clone(workspace);
        newObject._implementation = null;
        return newObject;
    }

    /** Initialize this display.  If place() has not been called
     *  with a container into which to place the display, then create a
     *  new frame into which to put it.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the numRows or numColumns parameters are incorrect, or
     *   if there is no effigy for the top level container, or if a problem
     *   occurs creating the effigy and tableau.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _initialized = false;
    }

    /** Specify the container into which this object should be placed.
     *  Obviously, this method needs to be called before the object
     *  is actually placed in a container.  Otherwise, the object will be
     *  expected to create its own frame into which to place itself.
     *  For actors, this method should be called before initialize().
     *  @param container The container in which to place the object, or
     *   null to specify that there is no current container.
     */
    @Override
    public void place(PortableContainer container) {
        _getImplementation().place(container);
    }

    /** Read at most one token from each input channel and display its
     *  string value on the screen.  Each value is terminated
     *  with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {

        int width = input.getWidth();

        for (int i = 0; i < width; i++) {
            String value = _getInputString(i);
            if (value != null) {
                // Do not open the display until there is a token.
                if (!_initialized) {
                    _initialized = true;
                    _openWindow();
                }
                _implementation.display(value);
            }

            else if (!_isSuppressBlankLines) {
                // There is no input token on this channel, so we
                // output a blank line.
                _implementation.display("");
            }

        }
        // If we have a Const -> Display SDF model with iterations set
        // to 0, then stopping the model by hitting the stop button
        // was taking between 2 and 17 seconds (average over 11 runs, 7.2 seconds)
        // If we have a Thread.yield() here, then the time is between
        // 1.3 and 3.5 seconds ( average over 10 runs, 2.5 seconds).
        // Unfortunately, this doesn't actually work... The textArea object
        // may clutter the event thread with unprocessed events that delay
        // response to the stop button.
        Thread.yield();

        return super.postfire();
    }

    /** Override the base class to remove the display from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        Nameable previousContainer = getContainer();
        super.setContainer(container);

        if (container != previousContainer && previousContainer != null) {
            _remove();
        }
    }

    /** Set a name to present to the user.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of the
     *  Display window will be updated to the value of the name parameter.</p>
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    @Override
    public void setDisplayName(String name) {
        super.setDisplayName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        _setTitle(name);
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of the
     *  Display window will be updated to the value of the name parameter.</p>
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period
     *   or if the object is a derived object and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #getName()
     *  @see #getName(NamedObj)
     *  @see #title
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        super.setName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        _setTitle(name);
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
     *
     *  @return the instance of the implementation.
     */
    protected DisplayInterface _getImplementation() {
        if (_implementation == null) {
            if (PtolemyInjector.getInjector() == null) {
                System.err.println("Warning: main() did not call "
                        + "ActorModuleInitializer.initializeInjector(), "
                        + "so Display is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            _implementation = PtolemyInjector.getInjector().getInstance(
                    DisplayInterface.class);
            try {
                _implementation.init(this);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(this, e,
                        "Failed to initialize implementation");
            } catch (IllegalActionException e) {
                throw new InternalErrorException(this, e,
                        "Failed to initialize implementation");
            }
        }
        return _implementation;
    }

    /** Return a string describing the input on channel i.
     *  This is a protected method to allow subclasses to override
     *  how inputs are observed.
     *  @param i The channel
     *  @return A string representation of the input, or null
     *   if there is nothing to display.
     *  @exception IllegalActionException If reading the input fails.
     */
    protected String _getInputString(int i) throws IllegalActionException {
        if (input.hasToken(i)) {
            Token token = input.get(i);
            String value = token.toString();
            if (token instanceof StringToken) {
                value = ((StringToken) token).stringValue();
            }
            return value;
        }
        return null;
    }

    /** Open the display window if it has not been opened.
     *  @exception IllegalActionException If there is a problem creating
     *  the effigy.
     */
    protected void _openWindow() throws IllegalActionException {
        _getImplementation().openWindow();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Indicator that the display window has been opened. */
    protected boolean _initialized = false;

    /** The flag indicating whether the blank lines will be suppressed. */
    protected boolean _isSuppressBlankLines = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        _implementation.remove();
    }

    /** Set the title of this window.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of the
     *  Display window will be updated to the value of the name parameter.</p>
     */
    private void _setTitle(String name) {

        try {
            _getImplementation().setTitle(name);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to get the value of the title parameter.");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Implementation of the DisplayInterface
    private DisplayInterface _implementation;

    // Record of previous columns.
    private int _previousNumColumns = 0;

    // Record of previous rows.
    private int _previousNumRows = 0;

}
