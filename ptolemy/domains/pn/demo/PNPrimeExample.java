/* This creates a Universe for PNDomain

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.domains.pn.kernel;
import pt.kernel.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNUniverse
/** 
This is currently a Universe containing some PNStars. This might not support
hierarchy currently.
@author  Mudit Goel
@version $Id$
*/
public class PNPrimeUniverse extends CompositeEntity {
    /** Constructor
     */	
    public PNPrimeUniverse() {
        super();
    }

    /** Constructor
     * @param workspace is the container of this Universe
     */
    public PNPrimeUniverse(Workspace workspace) {
        super(workspace);
    }
 
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** This sets the mode of execution of the current Universe in the 
     *  following ways:
     * @param mode This indicates which of the three supported modes of PN 
     *  is to be used. The modes are :
     *  mode = 0 : Bounded memory, if possible by Parks' method
     *  mode = 1 : Unbounded memory. Increase queue capacity whenever needed
     *                  to accomodate a write.
     *  otherwise: Fixed queue capacities. Never increased.
     */ 
    public void setMode(int mode) {
        _mode = mode;
    }

    /** Set the maximum number of cycles for execution
     * @param count is the number of cycles
     */
    public void setNoCycles(int count) {
        _count = count;
    }

    /** This returns the executive responsible for execution of the current
     *  Universe.
     * @return the Executive
     */
    public PNExecutive executive() {
        return _myExecutive;
    }

    /** Creating a Universe
     * @exception IllegalActionException is thrown by methods being called
     * @exception NameDuplicationException is thrown my methods called
     */
    public void setup() throws IllegalStateException,
	    IllegalActionException, NameDuplicationException { 

        _myExecutive = new PNExecutive(this, "example");
        _myExecutive.setMode(_mode);
        _interleave = new PNInterleave(this, "interleave");
        _interleave.initialize();
        _interleave.setCycles(_count);
        _alternate = new PNAlternate(this, "alternate");
        _alternate.initialize();
        _redirect0 = new PNRedirect(this, "redirect0");
        _redirect0.initialize(0);
        _redirect1 = new PNRedirect(this, "redirect1");
        _redirect1.initialize(1);

        //FIXME: Find a neat way of specifying the queue length of input port!!
        //FIXME: Need a nice way of doing the following.
        //Maybe a nice method that set all star parameters and links all ports
        _queueX = new IORelation(this, "QX");
        PNPort port = (PNPort)_interleave.getPort("output");
        port.link(_queueX);
        port = (PNPort)_alternate.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueX);

        _queueY = new IORelation(this, "QY");
        port = (PNPort)_redirect0.getPort("output");
        port.link(_queueY);
        port = (PNPort)_interleave.getPort("input1");
        port.getQueue().setCapacity(1);
        port.link(_queueY);        
 
        _queueZ = new IORelation(this, "QZ");
        port = (PNPort)_redirect1.getPort("output");
        port.link(_queueZ);
        port = (PNPort)_interleave.getPort("input2");
        port.getQueue().setCapacity(1);
        port.link(_queueZ);

        _queueT1 = new IORelation(this, "QT1");
        port = (PNPort)_alternate.getPort("output1");
        port.link(_queueT1);
        port = (PNPort)_redirect0.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueT1);

        _queueT2 = new IORelation(this, "QT2");
        port = (PNPort)_alternate.getPort("output2");
        port.link(_queueT2);
        port = (PNPort)_redirect1.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueT2);

	//FIXME: Should I use connect() rather than all the above stuff??

        _myExecutive.execute();
        return;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
 
    private int _mode;
    private int _count;
    private PNExecutive _myExecutive;
    private PNAlternate _alternate;
    private PNInterleave _interleave;
    private PNRedirect _redirect0;
    private PNRedirect _redirect1;
    private IORelation _queueX;
    private IORelation _queueY;
    private IORelation _queueZ;
    private IORelation _queueT1;
    private IORelation _queueT2;
}
   





