/*

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
import ptolemy.domains.sdf.lib.huffman.*;

///////////////////////////////////////////////////////////////
/// HuffEncoder -- This is a huffman encoder actor that use the huffman code
//                 book to encode the input tokens into a sequence of boolean
//                 tokens. If tokens are not found in
//                 the code book, illegal exception will be throw. The huffman
//                 code book (huffman tree) will be input as a parameter.
/**

@author Michael Leung
@version $Id$
*/

public class HuffEncoder extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HuffEncoder(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        new Parameter(this, "huffmanTree", new ObjectToken());

        output = (TypedIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(BooleanToken.class);

        codeBookInput = (TypedIOPort) newPort("codeBookInput");
        codeBookInput.setInput(true);
        codeBookInput.setTypeEquals(ObjectToken.class);

        codeInput = (TypedIOPort) newPort("codeInput");
        codeInput.setInput(true);
        codeInput.setTypeEquals(Token.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type ObjectToken. */
    public TypedIOPort codeBookInput;

    /** The input port. This has type ObjectToken. */
    public TypedIOPort codeInput;

    /** The output port. This has type BooleanToken. */
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
        HuffEncoder newobj = (HuffEncoder)(super.clone(ws));
        newobj.codeBookInput = (TypedIOPort)newobj.getPort("codeBookInput");
        newobj.codeBookInput = (TypedIOPort)newobj.getPort("codeInput");
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
	p = (Parameter) getAttribute("huffmanTree");
        huffmanTree = (HuffTree) ((ObjectToken)p.getToken()).getValue();
    }

    public boolean prefire() throws IllegalActionException {
        /* if (no CodeBook yet and no codebook waiting)
         *    return notReady;
         * else
         *    return ready;
         */
        if (codeBookInput.hasToken(0)) {
            _debug("ready to fire");
            return true;
        }
        return false;
    }

    /** Left branch represents False.
     *  Right branch represents True.
     *  @exception IllegalActionException If there is no director.
     */

    public void fire() throws IllegalActionException  {
        _debug("Encoder fired");
        Token dataToken = (Token) (codeInput.get(0));
        String data = dataToken.stringValue();
        _debug(data);

        //put all the huffleaf contains data and the boolean representation
        //of that data into a HashMap --  map;
        HashMap map = new HashMap();
        ((HuffTree) huffmanTree.getRoot()).fill(map);

        if (map.containsKey(data)) {
            HuffTree leaf = (HuffTree) map.get(data);
            while (leaf.getParent() != null) {
                if (leaf == leaf.getParent().getLeft())
                    output.send(0, new BooleanToken(false));
                else
                    output.send(0, new BooleanToken(true));
                leaf = (HuffTree) leaf.getParent();
            }

        }

    }

    private HuffTree huffmanTree;

}



