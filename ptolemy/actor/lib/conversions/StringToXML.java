/* An actor that outputs data read from a URL.

@Copyright (c) 1998-2002 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;



//////////////////////////////////////////////////////////////////////////
//// StringToXml
/**
This actor converts a string token to an xml token.

<p>FIXME: The type of the output ports is set to XmlTOken for now.
       It should ???.

@author  Yang Zhao
@version $Id$
@since Ptolemy II 3.1
*/
public class StringToXML extends Transformer{

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringToXML(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the input port.
        input.setMultiport(true);
        input.setTypeEquals(BaseType.STRING);
        // Set the type of the output port.
        output.setMultiport(true);
        output.setTypeEquals(BaseType.XMLTOKEN);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the XMLToken constucted from the input string.
     *  @exception IllegalActionException if the superclass throws it..
     */
    public void fire() throws IllegalActionException {
        //int k = 0;
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                StringToken in = (StringToken)input.get(i);
                try {
                _outToken = new XMLToken(in.stringValue());
                output.broadcast(_outToken);
                //k++;
                }
                catch (java.lang.Exception ex){
                    throw new IllegalActionException(this, ex,
                            "Can't construct an XML Token from '" +  in + "'");
                }
            }
        }

        /*//for test purpose, use the following code when fire.
        String in = "<?xml version='1.0' encoding='UTF-8'?> <Actors>" +  
         			"<Actor> <name>Const</name> <class>ptolemy.actor.lib.Const</class>"
         			+ " </Actor> </Actors>";
        try {
            //_outToken = new XmlToken[1];
            _outToken = new XmlToken(in);

            }
            catch (Exception e){
            e.printStackTrace();
            System.out.println("exception is " + e.getClass());
                System.out.println("### can't construct an XmlToken from: " + in + "\n");
                throw new IllegalActionException(this, e.getMessage());
        }
        output.broadcast(_outToken); */
    }
	
    /** Return true if there is token at the <i>input<i> input.
     *  Otherwise, return false.
     *  @exception IllegalActionException if the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                return true;
            }
        }
        return false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private XMLToken _outToken;
}
