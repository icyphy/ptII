/* An actor that takes in arbitray numbers of tokens and output
   two identaical huffman tree that is built according to the input tokens
   distribution.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (mikele@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib.huffman;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;
import java.util.HashMap;
import java.util.Vector;


///////////////////////////////////////////////////////////////
/// CodeBook  -- Create a code book to be use for huffman encoder and
//               decoder according to the huffman encoding scheme.
//               Code book will be built according to the distribution
//               of input tokens. This actor will take in arbitrary numbers of
//               Tokens as the training sequence and output a huffman tree.
//
/**

   @author Michael Leung
   @version $Id$
*/

public class CodeBook extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CodeBook(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        new Parameter(this, "trainingSequenceSize", new IntToken("30"));


        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(Token.class);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(ObjectToken.class);

        /*
          output = (TypedIOPort) newPort("output");
          output.setOutput(true);
          output.setTypeEquals(ObjectToken.class);

          input = (TypedIOPort) newPort("input");
          input.setInput(true);
          input.setTypeEquals(Token.class);
        */
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type ObjectToken. */
    public TypedIOPort input;

    /** The output port. This has type ObjectToken. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        CodeBook newobj = (CodeBook)(super.clone(ws));
        newobj.input = (TypedIOPort)newobj.getPort("input");
        newobj.output = (TypedIOPort)newobj.getPort("output");
        return newobj;
    }

    /**
     * Initialize this actor
     * @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	Parameter p;
	p = (Parameter) getAttribute("trainingSequenceSize");
        size = ((IntToken)p.getToken()).intValue();
        _debug("The training Sequence Size is " + size);
    }

    /**
     *  @exception IllegalActionException If there is no director.
     */

    public void fire() throws IllegalActionException  {
        _debug("fired");

        HashMap map = new HashMap ();
        //codeVector stores strings which are
        //the stringValues of the value of input tokens.
        Vector codeVector = new Vector();
        int counter = 0;

        /* Put new objects into the HashMap or if the HashMap contains the key
         * already, increment the count for that object.
         *
         * Assumptions : All the input tokens are with the same type and
         *               all the stringValues of tokens are unique.
         */
        for (int i = 0; i < size; i++) {
            Token object = (Token) (input.get(0));
            String key = object.stringValue();
            //            _debug("Token " + i + " = " + key);
            if (map.containsKey(key)) {
                int temp = ((Integer) map.get(key)).intValue();
                temp = temp + 1;
                map.put (key, new Integer(temp));
            } else {
                map.put(key, new Integer(1));
                codeVector.add(key);
                counter = counter + 1;
            }
        }

        /* Initialized the huffTree array.
         * Use private variable currentSize to keep track of how many huffman
         * nodes are in the huffTree array currently.
         */

        _debug("Number of unique tokens = " + counter);
        HuffTree[] huffArray = new HuffTree[counter];
        int currentSize = counter ;

        /* Fill the huffTree array with huffLeaf that is constructed by giving
         * the HuffLeaf constructor object and the probability related to the
         * object.
         */

        for (int j = 0; j < counter ; j++) {
            Object leafObject = codeVector.elementAt(j);
            //StringToken leafToken = new StringToken(leafObject.toString());
            //_debug(leafObject.toString());
            //String key = ((Token) leafToken).stringValue();
            String key = codeVector.elementAt(j).toString();
            int count = ((Integer) map.get(key)).intValue();
            huffArray[j] = new HuffLeaf(key , (double) count / size);
            _debug("Things in the huffArray with index " + j);
            _debug(huffArray[j].toString());
        }
        while (currentSize > 1) {
            _debug("currentSize = " +  currentSize);
            for (int c = 0; c < currentSize; c++) {
                _debug(huffArray[c].toString());
            }
            double minimum1 = 1.0;
            int first = 0;
            double minimum2 = 1.0;
            int second = 0;

            for (int i = 0; i < currentSize; i++) {
                double currentProb = huffArray[i].getProb();
                if (currentProb < minimum1) {
                    minimum1 = currentProb;
                    first = i;
                    _debug("current first is " + first);
                }

            }
            for (int j = 0; j < currentSize; j++) {
                double currentProb = huffArray[j].getProb();
                if ((currentProb < minimum2) && (j != first)) {
                    minimum2 = currentProb;
                    _debug("current second is " + second);
                    second = j;
                }
            }

            _debug("first = " + first);
            _debug("second = " + second);

            HuffTree tree = new HuffTree();
            tree.addLeft(huffArray[first]);
            tree.addRight(huffArray[second]);

            huffArray[first] = tree;
            huffArray[second] = huffArray[currentSize - 1];
            currentSize --;
        }


        //debugging line to print out the huffman tree in order
        System.out.println(huffArray[0].printPreOrder());

        ObjectToken token = new ObjectToken(huffArray[0]);
        output.send(0, token);
        //output.broadcast(token);
    }

    /* Postfire method make sure that the fire method only
     * get executed once.
     */
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    private int size;

}




