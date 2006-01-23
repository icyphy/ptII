/* A parameter for coordinated TOSSIM port numbering.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.kernel;

import java.util.Iterator;

import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.SharedParameter;

//////////////////////////////////////////////////////////////////////////
//// PtinyOSTOSSIMPort

/**
 This parameter is shared throughout a model for coordinated
 management of TOSSIM port numbers (command and event ports for
 connecting to TinyViz and other external tools). Changing the
 expression of any one instance of the parameter will result in all
 instances that are shared being changed. If the value is being set to
 zero, then all other values will be set to zero.  If it is being set
 to anything else, then all other values are set to unique numbers
 obtained by adding 2 to the specified value while iterating through
 the shared parameters.
   
 <p>
 An instance elsewhere in the model (within the same top level) is shared
 if it has the same type and its container is of the class specified in the
 constructor (or of the container class, if no class is specified
 in the constructor).
 <p>
   
 One exception is that if this parameter is (deeply) within an
 instance of EntityLibrary, then the parameter is not shared.
 Were this not the case, then opening a library containing this
 parameter would force expansion of all the sublibraries of
 EntityLibrary, which would defeat the lazy instantiation
 of EntityLibrary.
 <p>
   
 This parameter is always of type Int.

 <p> This parameter is based on ColtSeedParameter, except this
 parameter is of type Int instead of Long, and this parameter 
 increments by 2 instead of 1.
   
 @see ptolemy.actor.lib.colt.ColtSeedParameter
   
 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class PtinyOSTOSSIMPort extends SharedParameter {
    /** Construct a parameter with the given container and name.
     *  The container class will be used to determine which other
     *  instances of PtinyOSTOSSIMPort are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtinyOSTOSSIMPort(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Construct a parameter with the given container, name, and
     *  container class. The specified class will be used to determine
     *  which other instances of PtinyOSTOSSIMPort are shared with this one.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @param containerClass The class used to determine shared instances.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public PtinyOSTOSSIMPort(NamedObj container, String name,
            Class containerClass) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, containerClass, "0");
        setTypeEquals(BaseType.INT);
        
        SharedParameter sp = (SharedParameter) this;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
     /** Override the base class to also set the expression of shared
     *  parameters.
     */
    public void setExpression(String expression) {
        boolean previousSuppress = isSuppressingPropagation();

        try {
            setSuppressingPropagation(true);
            super.setExpression(expression);
        } finally {
            setSuppressingPropagation(previousSuppress);
        }

        // Have to evaluate the expression using getToken(),
        // rather than parsing the string. Unfortunately,
        // the Variable base class doesn't allow us to throw
        // IllegalActionException.  Too bad...
        try {
            IntToken token = (IntToken) getToken();
            int value = 0;

            if (token != null) {
                value = token.intValue();
            }

            if (value == 0) {
                // Call again without suppression of propagation.
                super.setExpression(expression);
            } else {
                // Need to assign unique values.
                if (!isSuppressingPropagation()) {
                    NamedObj toplevel = getRoot();

                    // Do not do sharing if this is within an EntityLibrary.
                    if (toplevel != null) {
                        Iterator sharedParameters = sharedParameterList(
                                toplevel).iterator();

                        while (sharedParameters.hasNext()) {
                            PtinyOSTOSSIMPort sharedParameter = (PtinyOSTOSSIMPort) sharedParameters
                                    .next();

                            if (sharedParameter != this) {
                                try {
                                    sharedParameter
                                            .setSuppressingPropagation(true);
                                    value += 2;

                                    String newExpression = String.valueOf(value);

                                    if (!sharedParameter.getExpression()
                                            .equals(newExpression)) {
                                        sharedParameter
                                                .setExpression(newExpression);

                                        // Make sure the new value is not persistent.
                                        sharedParameter.setPersistent(false);
                                    }
                                } finally {
                                    sharedParameter
                                            .setSuppressingPropagation(previousSuppress);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // FIXME: This is a lousy way to report a syntax error.
            throw new InternalErrorException(ex);
        }
    }
}
