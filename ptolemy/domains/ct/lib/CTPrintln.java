/* Printout the input token and the current time in stdout.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CTPrintln
/**
Print the time and data of the input tokens at stdout. Single input sink.
(Input type: double) This actor has no parameters.
@author Jie Liu
@version $Id$
*/
public class CTPrintln extends TypedAtomicActor {
    /** Construct the printer. Single input sink.
     * @param container The CTSubSystem this star belongs to
     * @param name The name
     * @exception NameDuplicationException another star already had this name
     * @exception IllegalActionException illustrates internal problems
     */
    public CTPrintln(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(true);
        input.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**Print out the input token and the current time each in a line.
     * The format of printout is :
     * time data1 data2... 
     * If one of the receivers has no data then skip this time point.
     * But all the other input data are consumed.
     *  @exception IllegalActionException If there's no director or
     *        no input token when needed.
     */

    public boolean postfire() throws  IllegalActionException{
       CTDirector dir = (CTDirector) getDirector();
       if (dir == null) {
           throw new IllegalActionException(this,
                   "No director avaliable");
       }
       String dataString = "";
       _hasToken = true;
       for (int i = 0; i < _width; i++ ) {
           if(!input.hasToken(i)) {
               _hasToken = false;
           } else {
               _data[i] = ((DoubleToken)input.get(i)).doubleValue();
               dataString += _data[i]+" ";
           }
       }
       if(_hasToken) {
           //System.out.println("CTTime:"+
           //        ((CTDirector)getDirector()).getCurrentTime());
           //System.out.println("CTData:"+in);
           System.out.println(((CTDirector)getDirector()).getCurrentTime()
                   +"  "+ dataString);
       }
       return true;
    }

    /** set up the input data buffer.
     */
    public void initialize() {
        _data = new double[input.getWidth()];
    }

    /** Prefire, adjust input data buffer if necessary.
     */
    public boolean prefire() {
        _width = input.getWidth();
        if(_data.length != _width) {
            _data = new double[_width];
        }
        return true;
    }

    /** The input port.
     */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    private double[] _data;
    private int _width;
    private boolean _hasToken;
}
