/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;

import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.ConfigurableEntity;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Plot

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Plot extends Event implements ConfigurableEntity {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public Plot(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _initializePlotter();
        parameters.setExpression("(value : double)");
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <code>parameters</code> attribute, update the parser scope for the
     *  actions so that the parameters' names can be referred to in those
     *  actions. If it is <code>monitoredVariables</code>, register this event
     *  as a value listener of all the monitored variables. If the changed
     *  attribute is <code>isInitialState</code>, do nothing. This is because
     *  the Ptera controller need not be updated with this attribute is set. If
     *  the changed attribute is among the other attributes, then the superclass
     *  is called.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the parser scope cannot be updated.
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == parameters) {
            int index = parameters.getParameterNames().indexOf("value");
            if (index >= 0) {
                if (BaseType.DOUBLE.isCompatible(parameters.getParameterTypes()[index])) {
                    return;
                }
            }
            throw new IllegalActionException(this, "The event must have a " +
                    "parameter with name \"value\" and type \"double\".");
        }
    }

    /** Clone the state into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Plot newObject = (Plot) super.clone(workspace);
        try {
            newObject._initializePlotter();
        } catch (KernelException e) {
            throw new CloneNotSupportedException("Unable to clone object.");
        }
        return newObject;
    }

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The object should interpret the
     *  source first, if it is specified, followed by the literal text,
     *  if that is specified.  The new configuration should usually
     *  override any old configuration wherever possible, in order to
     *  ensure that the current state can be successfully retrieved.
     *  <p>
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        _plotter.configure(base, source, text);
    }

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #refire(ArrayToken, RefiringData)
     */
    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        int index = parameters.getParameterNames().indexOf("value");
        if (index < 0) {
            throw new IllegalActionException(this,
                    "No parameter with name \"value\" is found.");
        }

        _value = DoubleToken.convert(arguments.getElement(index));
        _plotter.iterate(1);

        return data;
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    public String getConfigureSource() {
        return _plotter.getConfigureSource();
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    public String getConfigureText() {
        return _plotter.getConfigureText();
    }

    /** Get the {@link Configurer} object for this entity.
     *  @return the Configurer object for this entity.
     */
    public Configurer getConfigurer() {
        return _configurer;
    }

    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);

        if (_plotter.plot == null) {
            return;
        }

        String sourceSpec = "";

        String configureSource = getConfigureSource();
        if ((configureSource != null) && !configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + configureSource + "\"";
        }

        String header =
            "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n" +
            "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";

        output.write(_getIndentPrefix(depth) + "<configure" + sourceSpec +
                ">\n");
        output.write("<?plotml " + header + "\n<plot>\n");

        PrintWriter print = new PrintWriter(output);

        // The second (null) argument indicates that PlotML PUBLIC DTD
        // should be referenced.
        _plotter.plot.writeFormat(print);
        output.write("</plot>?>\n");
        output.write(_getIndentPrefix(depth) + "</configure>\n");
    }

    private void _initializePlotter() throws IllegalActionException,
            NameDuplicationException {
        _configurer = new Configurer(workspace());
        _configurer.setName("Configurer");
        _configurer.setConfiguredObject(this);
        addInitializable(_configurer);
        new DEDirector(_configurer, "DEDirector");
        _plotter = new TimedPlotter(_configurer, "Plotter") {
            public boolean postfire() throws IllegalActionException {
                DoubleToken currentToken = _value;
                double currentValue = currentToken.doubleValue();
                Time modelTime = getController().getDirector().getModelTime();
                ((ptolemy.plot.Plot) plot).addPoint(0,
                        modelTime.getDoubleValue(), currentValue, true);

                return super.postfire();
            }

            public NamedObj toplevel() {
                return Plot.this.toplevel();
            }
        };
    }

    private Configurer _configurer;

    private TimedPlotter _plotter;

    private DoubleToken _value;

}
