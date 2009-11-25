/* An actor that specifies the expected behavior in the event of an error in the model's execution.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.domains.giotto.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.ModelErrorHandler;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
This error actor enables the user to specify how an error is handled in 
C code generated from a Giotto model. In theory it should implement the 
Model Error Handler interface, however I'm not sure if it is correct to 
incorporate error handling in the specification and simulation of a Giotto 
Model since a giotto model only specifies logical execution time.


 @author Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */

public class GiottoError extends TypedAtomicActor implements ModelErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public GiottoError(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Parameters
        errorAction = new StringParameter(this, "errorAction");
        errorAction.setExpression("Warn");
        errorAction.addChoice("Warn");
        errorAction.addChoice("Reset");
        errorAction.addChoice("TimedUtilityFunction");

        // Icon is a stop sign named error handler
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-8,-19 8,-19 19,-8 19,8 8,19 "
                + "-8,19 -19,8 -19,-8\" " + "style=\"fill:orange\"/>\n"
                + "<text x=\"-15\" y=\"4\""
                + "style=\"font-size:10; fill:red; font-family:SansSerif\">"
                + "Error Actor</text>\n" + "</svg>\n");
    }

    public void preinitialize() {
        CompositeActor container = (CompositeActor) getContainer();
        if (_debugging) {
            _debug("inside Giotto error preinitialize about to register the container as an error handler");
        }
        if (container != null) {
            if (_debugging) {
                _debug("the model error handler is set to " + getDisplayName()
                        + " for container " + container.getDisplayName());
            }
            container.setModelErrorHandler((ModelErrorHandler) this);
        }

    }

    /** Override the base class to determine which comparison is being
     *  specified.  Read the value of the comparison attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the comparison is not recognized.
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        String errorActionName = "";
        if (attribute == errorAction) {
            errorActionName = errorAction.getExpression().trim();

            if (errorActionName.equals("Warn")) {
                _errorAction = ErrorAction.warn;
            } else if (errorActionName.equals("Reset")) {
                _errorAction = ErrorAction.reset;
            } else if (errorActionName.equals("TimedUtilityFunction")) {
                _errorAction = ErrorAction.timedutilityfunction;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized action on error: " + errorActionName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    public void fire() throws IllegalActionException {
        // System.out.print("fire method called"); 
    }

    public void initialize() {
        //System.out.println("Initialize method called");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false if the
     *   error is not handled.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.///
     */
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {

        System.out
        .println("handleModelError called for the GiottoError Actor with name "
                + this.getDisplayName());
        System.out.println("error action has value " + _errorAction);
        if (_errorAction == ErrorAction.warn) {
            System.out.println("an error was detected in "
                    + context.getFullName());
            return true;
        } else if (_errorAction == ErrorAction.timedutilityfunction) {
            System.out
            .println("I should check to see if I'm within the acceptable range for the timed utility function");
            String temp = exception.toString();
            int i, j, k, l = 0;
            i = temp.indexOf("(");
            j = temp.indexOf(")");
            k = temp.lastIndexOf("(");
            l = temp.lastIndexOf(")");
            double wcet = Double.parseDouble(temp.substring(i + 1, j));
            double periodvalue = Double.parseDouble(temp.substring(k + 1, l));
            System.out.println("wcet value is " + wcet
                    + " and period value is " + periodvalue);
            return true;
        } else if (_errorAction == ErrorAction.reset) {
            System.out.println("I should request a reset to the model");
            throw exception;

        }

        return false;
    }

    /** The errorAction operator.  This is a string-valued attribute
     *  that defaults to "warn".
     */
    public StringParameter errorAction;

    /// Enumeration of the different ways to handle errors
    private enum ErrorAction {
        warn, reset, timedutilityfunction
    }

    ///////////////////////////////////////////////////////////////////
    ////private variables                 ////
    //  An indicator for the error action to take.
    private ErrorAction _errorAction;

}
