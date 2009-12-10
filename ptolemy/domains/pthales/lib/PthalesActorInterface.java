/* Interface for Pthales objects .

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.domains.pthales.lib;


/**
 Interface for Pthales objects, that contains useful items for PthalesScheduler.
 Implemented by PthalesGenericActor, PthalesCompositeActor, or any actor that will be
 used by a PthalesDirector.
 
@author Rémi Barrère
*/

public interface PthalesActorInterface {

    // Abstract methods used to get iteration from PthalesActor
    
    /** Return the repetitions with external and internal iterations
     * @return an array of repetitions 
     */
    abstract Integer[] getRepetitions();
    
    /** Return the repetitions with only external iterations
     * @return an array of repetitions 
     */
    abstract Integer[] getExternalRepetitions();
    /** Return the repetitions with only internal iterations
     * @return an array of repetitions 
     */
    abstract Integer[] getInternalRepetitions();
    
    /** Return the number of repetitions (including external and internal iterations)
     * @return the number of iterations for the actor 
     */
    abstract int getIterations();
       
    // Used by all Pthales actors
    /** The name of the repetitions parameter. */
    public static String REPETITIONS = "repetitions";

}
