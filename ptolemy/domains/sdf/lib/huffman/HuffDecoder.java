/*

 Copyright (c) 1998-1999 The Regents of the University of California.
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
/// HuffDecoder -- Huffman decoder that takes in a stream of boolean token,
//                 decode it and output the corresponding token in the
//                 code book. Huffman code book (huffman tree) will be input
//                 as a parameter.
/**

@author Michael Leung
@version $Id$
*/

public class HuffDecoder extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HuffDecoder(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        new Parameter(this, "huffmanTree", new ObjectToken());

        output = (TypedIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(ObjectToken.class);

        codeBookInput = (TypedIOPort) newPort("codeBookInput");
        codeBookInput.setInput(true);
        codeBookInput.setTypeEquals(ObjectToken.class);

        bitInput = (TypedIOPort) newPort("bitInput");
        bitInput.setInput(true);
        bitInput.setTypeEquals(BooleanToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type ObjectToken. */
    public TypedIOPort codeBookInput;

    /** The input port. This has type BooleanToken. */
    public TypedIOPort bitInput;

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
        HuffDecoder newobj = (HuffDecoder)(super.clone(ws));
        newobj.codeBookInput = (TypedIOPort)newobj.getPort("codeBookInput");
        newobj.bitInput = (TypedIOPort)newobj.getPort("bitInput");
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

    public final boolean prefire() throws IllegalActionException {
        /* if (no CodeBook yet and no codebook waiting)
         *    return notReady;
         * else
         *    return ready;
         */
        if (codeBookInput.hasToken(0))
            return true;
        return false;
    }

    /** Left branch represents False.
     *  Right branch represents True.
     *  @exception IllegalActionException If there is no director.
     */

    public void fire() throws IllegalActionException  {

        HuffTree tree = (HuffTree) huffmanTree.getRoot();

        while (!tree.isLeaf()) {
            BooleanToken bitToken = (BooleanToken) (bitInput.get(0));
            boolean bit = bitToken.booleanValue();
            if (bit)
                tree = (HuffTree) tree.getRight();
            else
                tree = (HuffTree) tree.getLeft();
        }

        // Output will be the stringValue of the input Token of HuffEncoder
        output.send(0, new StringToken(((HuffLeaf) tree).getData()));
    }

    private HuffTree huffmanTree;

}



