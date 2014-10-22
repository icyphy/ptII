/*
@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib.luminary;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A class for GPIO pins on the Luminary board.
 * <p>This actor will have no effect in model simulations, but
 * allows for code generators to generate the actors.
 *
 * @author Jia Zou, Jeff C. Jensen
 * @version $ld$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating
 *
 */
public class GPInputHandler extends LuminarySensorHandler {

    /**
     * Construct a GPInputHandler object.
     *
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException if the super constructor throws it
     * or if setting the pin and pad expressions fails.
     * @exception NameDuplicationException if the super constructor throws it.
     */
    public GPInputHandler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        pin = new Parameter(this, "pin");
        //FIXME: GPIO A7 is an easy-to-use output, but should it be default?
        pin.setExpression("0");
        pad = new StringParameter(this, "pad");
        pad.setExpression("G");
        _initSupportedConfigurations();
        startingConfiguration = "0";
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** Which pin (0-7) of GPIO to use.
     *  FIXME: Verify user has set value between 0 and 7.
     */
    public Parameter pin;

    /** Which pad (A-G) of GPIO to use.
     *  FIXME: Verify user has set value between A and H.
     */
    public StringParameter pad;

    /** A GPInputHandler's configuration is its pad name.
     *  @exception IllegalActionException
     */
    @Override
    public String configuration() throws IllegalActionException {
        return pad.stringValue();
    }

    /** Returns the starting configuration, which is an integer.
     */
    @Override
    public String startingConfiguration() {
        return startingConfiguration;
    }

    /** Returns the list of supported configurations.
     */
    @Override
    public List<String> supportedConfigurations() {
        return _supportedConfigurations;
    }

    /** The default configuration.
     */
    private String startingConfiguration;

    /** The set of supported configurations.
     */
    private List<String> _supportedConfigurations;

    /** Initialize the list of supported configurations.
     */
    private void _initSupportedConfigurations() {
        _supportedConfigurations = new LinkedList<String>();
        _supportedConfigurations.add("A");
        _supportedConfigurations.add("B");
        _supportedConfigurations.add("C");
        _supportedConfigurations.add("D");
        _supportedConfigurations.add("E");
        _supportedConfigurations.add("F");
        _supportedConfigurations.add("G");
        _supportedConfigurations.add("H");
    }
}
