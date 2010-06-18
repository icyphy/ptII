/* ProcessAttribute is a subclass of Parameter with support for strings.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

////ProcessAttribute

/**
ProcessAttribute is a subclass of Parameter with support for strings

   A ProcessAttribute is a tuple (string processName, int sequenceNumber, (optionally) string methodName)

   The ProcessDirector collects the ProcessAttributes to determine the order
   in which order the actors in the model are fired. 
   
 @author Elizabeth Latronico (Bosch)
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
 */
public class ProcessAttribute extends SequenceAttribute {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public ProcessAttribute() {
        super();

    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public ProcessAttribute(Workspace workspace) {
        super(workspace);

    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ProcessAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setTypeEquals(new ArrayType(BaseType.STRING));

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-30\" y=\"-2\" "
                + "width=\"60\" height=\"4\" " + "style=\"fill:blue\"/>\n"
                + "<rect x=\"15\" y=\"-10\" " + "width=\"4\" height=\"20\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    // FIXME:  Do we want the sequence director to ignore the process name alltogether
    //   when executing a model?  If so, can't use this compareTo method.  Need to use
    // getSequenceNumber
    /** Implement compareTo method to compare sequence numbers 
     *  
     *  Updated:  This method now compares both the process name and the 
     *     sequence number.
     *     - If the process names are different, then "less than" is determined
     *     alphabetically
     *     - If the process names are the same, then "less than" is determined by
     *     the sequence number
     *     - If a ProcessAttribute and a SequenceAttribute are compared, then only the 
     *     sequence number is considered
     *     
     *     This method assumes that the expression is well-formed (checked in validate()) 
     *     
     *   @param obj The SequenceAttribute or ProcessAttribute object.
     *   @return 0 if the sequence numbers are the same.
     */
    public int compareTo(Object obj) {

        int seq1 = 0;
        int seq2 = 0;

        try {
            // Check for either a SequenceAttribute or ProcessAttribute (which is a SequenceAttribute)
            if (obj instanceof SequenceAttribute) {

                // If the second object is a ProcessAttribute, compare the process names
                if (obj instanceof ProcessAttribute) {
                    String proc1 = this.getProcessName();
                    String proc2 = ((ProcessAttribute) obj).getProcessName();

                    int procCompare = proc1.compareTo(proc2);

                    // If not equal, return result
                    if (procCompare != 0) {
                        return procCompare;
                    }

                    // If process names are the same, compare the sequence numbers
                    seq1 = this.getSequenceNumber();
                    seq2 = ((ProcessAttribute) obj).getSequenceNumber();

                    if (seq1 < seq2) {
                        return -1;
                    } else if (seq1 > seq2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

                else {
                    // For process name and sequence number, just compare sequence numbers
                    // Need to call getSequenceNumber from SequenceAttribute
                    seq1 = this.getSequenceNumber();
                    seq2 = ((SequenceAttribute) obj).getSequenceNumber();

                    if (seq1 < seq2) {
                        return -1;
                    } else if (seq1 > seq2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        } catch (IllegalActionException e) {
            throw new IllegalArgumentException(
                    "Invalid ProcessAttribute passed to compareTo method.", e);
        }

        // FIXME:  Throw exception?  Otherwise we can not compare them, if the second object
        // is not a SequenceAttribute or ProcessAttribute
        return 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the method name to be called on the actor, or an empty
     *  string if there is none.
     * 
     *  @return String method name.
     *  @throws IllegalActionException If there is a problem getting the token
     *   from the ProcessAttribute.
     */
    public String getMethodName() throws IllegalActionException {
        String methodName = "";
        
        ArrayToken processArrayToken = (ArrayToken) getToken();
        if (processArrayToken.length() > 2) {
            methodName = ((StringToken) processArrayToken.getElement(2)).stringValue();
        }        
        
        return methodName;
    }    
    
    /** Returns the process name, or throws an exception if there is none.
     * 
     * @return String process name
     * @throws IllegalActionException If there is a problem getting the token
     *   from the ProcessAttribute.
     */
    public String getProcessName() throws IllegalActionException {
        String processName = "";

        ArrayToken processArrayToken = (ArrayToken) getToken();
        
        if (processArrayToken.length() > 0) {
            StringToken processNameToken = (StringToken) (processArrayToken).getElement(0);
            processName = processNameToken.stringValue();
        } else {
            throw new IllegalActionException(this, "ProcessAttribute " +
                    getName() + " has no process name.");
        }

        return processName;
    }

    /** Returns the sequence number as an int, or throws an exception if there is none.
     * 
     * @return int sequence number
     * @throws IllegalActionException If there is a problem getting the token
     *   from the ProcessAttribute.
     */
    public int getSequenceNumber() throws IllegalActionException {
        // FIXME:  0 is actually a valid sequence number - want different default return?
        int seqNumber = 0;
        
        ArrayToken processArrayToken = (ArrayToken) getToken();
        
        if (processArrayToken.length() > 1) {
            StringToken sequenceNumToken = (StringToken) (processArrayToken).getElement(1);            
            try {
                seqNumber = Integer.parseInt(sequenceNumToken.stringValue());
                
                // Check to make sure sequence number is positive or zero.
                if (seqNumber < 0) {
                    throw new IllegalActionException(this, "In ProcessAttribute " +
                            getName() + " the sequence number must be a positive integer. " +
                            "It cannot be zero or negative.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalActionException(this, e, "ProcessAttribute " +
                        getName() + " has an incorrectly formatted sequence number: " +
                        sequenceNumToken.stringValue() + " - The sequence number must " +
                        " be a positive integer.");
            }
        } else {
            throw new IllegalActionException(this, "ProcessAttribute " +
                    getName() + " has no sequence number.");
        }

        return seqNumber;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
