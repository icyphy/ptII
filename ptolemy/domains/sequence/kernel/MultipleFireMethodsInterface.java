/* The interface for actors with multiple fire methods that can be separately scheduled
 * in the Sequence and Process Directors.

Copyright (c) 1999-2014 The Regents of the University of California.
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

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultipleFireMethodsInterface

/** The interface for actors with multiple fire methods that can be separately scheduled
 *  in the Sequence and Process Directors.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @see SequencedModelDirector
 *  @see SequenceScheduler
 *  @see SequenceSchedule
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public interface MultipleFireMethodsInterface {

    /** Return the name of the default fire method for this actor.
     *
     *  @return The string name of the default fire method for the actor.
     *   If the actor does not have multiple fire methods, return null.
     */
    public String getDefaultFireMethodName();

    /** Return the list of strings that represent the names of
     *  all the fire methods the actor has.
     *
     *  @return The list of fire method names strings.
     */
    public List<String> getFireMethodNames();

    /** Return the list of input ports associated with the given method name.
     *  If the method has no input ports, return an empty list. If the actor
     *  does not have multiple methods, return null.
     *
     *  @param methodName The specified method name.
     *  @return The list of input ports associated with the method name.
     */
    public List<IOPort> getMethodInputPortList(String methodName);

    /** Return the output port associated with the given method name, if there is one.
     *  If the method does not have any outputs, or the actor does not have multiple
     *  fire methods, return null
     *
     *  @param methodName The specified name of the method.
     *  @return The output port associated with this method, or null is there is none.
     */
    public IOPort getMethodOutputPort(String methodName);

    /** Return the number of fire methods the actor has.
     *  @return the number of fire methods the actor has, which should be
     *   at least one.
     */
    public int numFireMethods();

    /** Set the fire method to the method that matches the specified
     *  string name.
     *  @param methodName The name of the method to be used.
     *  @exception IllegalActionException If the specified fire method cannot be found
     *   in the actor.
     */
    public void setFireMethod(String methodName) throws IllegalActionException;
}
