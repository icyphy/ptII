/* Handle exceptions thrown in tests.

 Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.kernel.util;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// TestExceptionHandler

/**
 An attribute that handles exceptions thrown in a test. It has two
 working modes, trainging mode and not training mode. If in training mode, 
 this attribute handles an exception by recording the exception message. If
 not in training mode, this attribute first compares the previously stored 
 (assuming correct) message to the exception message and then throws the 
 exception if the two messages are not the same.  

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class TestExceptionHandler extends Attribute 
    implements ModelErrorHandler {
    
    /** Construct an attribute with the given container and name.
     *  If an attribute already exists with the same name as the one
     *  specified here, that is an instance of class
     *  SingletonAttribute (or a derived class), then that
     *  attribute is removed before this one is inserted in the container.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name, and the class of that container is not
     *   SingletonAttribute.
     */
    public TestExceptionHandler(NamedObj container, String name) 
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        correctExceptionMessage = new StringParameter(this, name);
        correctExceptionMessage.setExpression("");
        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setExpression("false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        // icon
        _attachText("_iconDescription", 
                "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:white\"/>\n"
                + "<polygon points=\"-20,-10 20,0 -20,10\" "
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** If true, then collect the exception message and set the 
     *  correctExceptionMessage parameter with the content of the 
     *  exception. This parameter is a boolean, and it defaults to false.
     */
    public Parameter trainingMode;

    /** The correct exception message to be compared against. */
    public StringParameter correctExceptionMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle an exception thrown in a test. If in training mode, simply
     *  record the exception message. If not in training mode, first compare
     *  the stored good message against the exception message. If they are 
     *  the same, do nothing. Otherwise, throw the exception again.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the exception message is the same as the saved message.
     *  @exception IllegalActionException If cannot get a valid token from
     *  the traningMode parameter or the exception message is not the same as
     *  the stored message.
     */
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {
        boolean training = ((BooleanToken) trainingMode.getToken()).booleanValue();
        if (training) {
            correctExceptionMessage.setExpression(exception.getMessage());
            correctExceptionMessage.setPersistent(true);
        } else {
            if (!exception.getMessage().equals(correctExceptionMessage.stringValue())) {
                throw exception;
            }
        }
        return true;
    }
}
