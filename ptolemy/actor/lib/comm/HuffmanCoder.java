/* Huffman Coder.

Copyright (c) 2003-2004 The Regents of the University of California.
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

package ptolemy.actor.lib.comm;

import java.util.LinkedList;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.SignalProcessing;

//////////////////////////////////////////////////////////////////////////
//// HuffmanCoder
/** Given a probability distribution, produce the code.
    FIXME: The output shouldn't be a string, but a sequence of booleans.
    Part of the code is not optimized for efficiency.
   
   @author Rachel Zhou
   @version $Id$
   @since Ptolemy II 3.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class HuffmanCoder extends Transformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HuffmanCoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        pmf = new Parameter(this, "pmf");
        pmf.setExpression("{0.5, 0.5}");
        pmf.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        
        alphabets = new Parameter(this, "alphabets");
        alphabets.setExpression("{0, 1}");
        alphabets.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        
        // Create input port and declare data types.
        //input = new TypedIOPort(this, "input", true, false);
        //input.setTypeEquals(BaseType.BOOLEAN);
        //ArrayType alphabetsArrayType = (ArrayType)alphabets.getType();
        //InequalityTerm elementTerm = alphabetsArrayType.getElementTypeTerm();
        //output.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeEquals(BaseType.STRING);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** 
     */
    public Parameter pmf;

    /** 
     */
    public Parameter alphabets;
    
    ////////////////////////////////////////////////////////////////////
    ////                  public inner classes                      ////
    public class Node {
        // The constructor. Construct the child node with prob and 
        // its index in the alphabet array.
        public Node(double prob, int index) {
            probability = prob;
            indexInArray = index;
            //isChild = true;
            leftChild = null;
            rightChild = null;
            huffmanCode = "";
        }
        
        // construct a node with left and right children.
        public Node(Node left, Node right) {
            probability = left.probability + right.probability;
            indexInArray = -1;
            //isChild = false;
            leftChild = left;
            rightChild  = right;
            huffmanCode = "";
        }
        
        public void setHuffmanCode(String code) {
            huffmanCode = code;
        }
        
        public double probability;
        //public boolean isChild;
        public int indexInArray;
        public Node leftChild;
        public Node rightChild;
        public String huffmanCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>polynomial</i>, then
     *  verify that is a positive integer and the lower-order bit is 1.
     *  @exception IllegalActionException If <i>polynomial</i> is
     *  non-positive or the lower-order bit is not 1.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pmf) {
            _parametersInvalid = true;
            ArrayToken pmfValue = (ArrayToken)pmf.getToken();
            _pmf = new double[pmfValue.length()];
            double sum = 0.0;
            for (int i = 0; i < _pmf.length; i++) {
                _pmf[i] = ((DoubleToken)pmfValue.getElement(i))
                    .doubleValue();
                if (_pmf[i] < 0.0)
                    throw new IllegalActionException(this,
                        "Probabilities must be non-negative!");
                sum = sum + _pmf[i];
            }
            if (!SignalProcessing.close(sum, 1.0))
                throw new IllegalActionException(this,
                    "Parameter values is required to sum to one.");
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read a bit from the input port and shift it into the shift register
     *  to scramble. Compute the parity and send <i>true</i> to the output
     *  port if it is 1; otherwise send <i>false</i> to the output port.
     *  The parity is shifted into the delay line for the next iteration.
     */
    public void fire() throws IllegalActionException {
        ArrayToken alphabetsArrayToken = (ArrayToken)alphabets.getToken();
        if (_pmf.length != alphabetsArrayToken.length()) {
            throw new IllegalActionException(this,
                "uncoded alphabets and pmf are required to be arrays" +
                "with same length.");
        }
        Token[] alphabetsTokens = new Token[_pmf.length];
        for (int i = 0; i < _pmf.length; i ++) {
            alphabetsTokens[i] = alphabetsArrayToken.getElement(i);
        }
        if (_parametersInvalid) {
            _parametersInvalid = false;
            _codeBook = new String[_pmf.length];
            // Generate the huffman code book.
            for (int i = 0; i < _pmf.length; i ++) {
                // Create a list of objects;
                Node node = new Node(_pmf[i], i);
                _list.add(node);   
            }
            // Construct the binary tree.
            while (_list.size() > 1) {
                Node node1 = findMinNode(_list);
                _list.remove(node1);
                Node node2 = findMinNode(_list);
                _list.remove(node2);
                // node2 has larger prob than node1.
                Node newNode = new Node(node2, node1);
                _list.add(newNode);
            }
            // Now there is only one element in the list,
            // and its probability should be 1.
            Node root = (Node)_list.get(0);
            root.setHuffmanCode("");
            setCode(root);
        }
        //ready for output.
        Token inputToken = (Token)input.get(0);
        // Find the token in the alphabet;
        boolean validInput = false;
        for (int i = 0; i < _pmf.length; i++) {
            if (inputToken.equals(alphabetsTokens[i])) {
                validInput = true;
                output.send(0, new StringToken(_codeBook[i]));
                break;
            }
        }
        if (!validInput) {
            throw new IllegalActionException(this,
                "Input is not matched to the alphabets");
        }
    }
    
    // set the code for a node and all its child.
    // If this child is at the bottom, then set the codeBook.
    public void setCode(Node node) {
        String parentCode = node.huffmanCode;
        Node left, right;
        if ((left = node.leftChild) != null) {
            String leftCode = parentCode + "0";
            left.setHuffmanCode(leftCode);
            if (left.indexInArray >= 0) {
                _codeBook[left.indexInArray] = leftCode; 
            } else {
                setCode(left);
            }
        }
        if ((right = node.rightChild) != null) {
            String rightCode = parentCode + "1";
            right.setHuffmanCode(rightCode);
            if (right.indexInArray >= 0) {
                _codeBook[right.indexInArray] = rightCode;
            } else {
                setCode(right);
            }
        }
    }

    // Find the min node in the list.
    public Node findMinNode(LinkedList list) {
        double minProb = ((Node)list.get(0)).probability;
        int index = 0;
        for (int i = 1; i < list.size(); i ++) {
            if (((Node)list.get(i)).probability < minProb) {
                index = i;
                minProb = ((Node)list.get(i)).probability;
            }
        }
        return (Node)list.get(index);
    }

    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initialState</i>
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _list = new LinkedList();
        _parametersInvalid = true;
    }

    //////////////////////////////////////////////////////////////
    ////                     private variables                ////

    private boolean _parametersInvalid;
    private double[] _pmf;
    private LinkedList _list;
    private String[] _codeBook;

}
