/* Test the evolution of a Petri net.
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.domains.petrinet.lib;

import ptolemy.actor.lib.NonStrictTest;
import ptolemy.data.StringToken;
import ptolemy.domains.petrinet.kernel.PetriNetDisplayer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
   Compare the evolution of a Petri net after execution with a known good result.

   <p>Place this actor on a canvas along with a Petri Net (containing
   Places and Transitions) and the actor will display the evolution of the Net after
   execution. Specifically, the token count of each place will be printed
   for each iteration.</p>

   <p>It should be noted that there are alignment issues within the display 
   on certain operating systems where the font used is not monospaced.</p>

   @author  Zach Ezzell
   @version $Id$ 
   @since Ptolemy II 8.1
   @Pt.ProposedRating Red (yukewang)
   @Pt.AcceptedRating Red (reviewmoderator)
*/
public class PetriNetTest extends NonStrictTest implements PetriNetDisplayer {

    /**
     * Construct a new PetriNetDisplay.
     * 
     * @param container
     *           The CompositeEntity.
     * @param name
     *            The name of the PetriNetDisplay.
     *            
     * @exception IllegalActionException
     *                If the name has a period in it.
     *                
     * @exception NameDuplicationException
     *                If the container already contains an entity with 
     *                the specified name.
     */

    public PetriNetTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////   

    /** Ensure that the text is placed in the textArea.
     * 
     * @exception IllegalActionException If _openWindow() in the base 
     * class throws it.
     */
    public void openDisplay() throws IllegalActionException {

    }
    
    /** Set the text for the display.  This method is called by the 
     * PetriNetDirector.
     * 
     * @param text The text to be compared with a known good result.
     * @exception IllegalActionException If there is a problem
     * reading or writing a Ptolemy object.
     */
    public void setText(String text) throws IllegalActionException {
        
        input.send(0, new StringToken(text));
        fire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////   
    

    /** This string represents the evolution of the Petri Net and should
     * be set by the PetriNetDirector.
     */
    private String text;

}
