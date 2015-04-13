/* Huffman code base class.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HuffmanBasic

/**
 Given a probability distribution, generate the Huffman code book.
 The probability distribution is given by the <i>pmf</i> parameter.
 The corresponding alphabet is given by the <i>alphabet</i> parameter.
 The code book is in a format of an array of strings, each string
 consists of '0' and '1's. The code book is sent to the
 <i>huffmanCodeBook</i> output port.

 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class HuffmanBasic extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HuffmanBasic(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        pmf = new Parameter(this, "pmf");
        pmf.setExpression("{0.5, 0.5}");
        pmf.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        alphabet = new Parameter(this, "alphabet");
        alphabet.setExpression("{0, 1}");
        alphabet.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

        // Declare port types.
        huffmanCodeBook = new TypedIOPort(this, "huffmanCodeBook", false, true);
        huffmanCodeBook.setTypeEquals(new ArrayType(BaseType.STRING));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The probability mass function. This parameter is an array
     *  of doubles. Each element should be positive and the sum of
     *  all elements should be 1.0. The default value is {0.5, 0.5}.
     */
    public Parameter pmf;

    /** The alphabet of the input. This parameter is an ArrayToken.
     *  Its default value is {0, 1}.
     */
    public Parameter alphabet;

    /** A port that produces the Huffman code book generated
     *  based on the probability mass function. It is an array
     *  of strings.
     */
    public TypedIOPort huffmanCodeBook;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayElement.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HuffmanBasic newObject = (HuffmanBasic) super.clone(workspace);
        newObject.alphabet.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

        newObject._pmf = null;
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  public inner classes                      ////

    /** A class that defines the node in binary tree that is used
     *  to construct the codebook of Huffman code.
     */
    public static class Node {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct the node with the given probability value
         *  and its index in the <i>pmf</i> array.
         * @param prob The given probability value.
         * @param index The corresponding index in the pmf array.
         */
        public Node(double prob, int index) {
            probability = prob;
            indexInArray = index;
            leftChild = null;
            rightChild = null;
            huffmanCode = "";
        }

        /** Construct the parent node given the left child
         *  and the right child.
         * @param left The left child.
         * @param right The right child.
         */
        public Node(Node left, Node right) {
            probability = left.probability + right.probability;
            indexInArray = -1;
            leftChild = left;
            rightChild = right;
            huffmanCode = "";
        }

        /** The probability of the node.
         */
        public double probability;

        /** The corresponding index in the pmf array of this node.
         *  If the value is -1, then this node is constructed by
         *  combining at least two probabilities.
         */
        public int indexInArray;

        /** The left child of the node.
         */
        public Node leftChild;

        /** The right child of the node.
         */
        public Node rightChild;

        /** The huffman code of this node.
         */
        public String huffmanCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>pmf</i>, then verify
     *  all the elements are positive and their sum is 1.0.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If any element in <i>pmf</i>
     *  is non-positive or the sum is not 1.0.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        _parametersInvalid = true;

        if (attribute == pmf) {
            ArrayToken pmfValue = (ArrayToken) pmf.getToken();
            _pmf = new double[pmfValue.length()];

            double sum = 0.0;

            for (int i = 0; i < _pmf.length; i++) {
                _pmf[i] = ((DoubleToken) pmfValue.getElement(i)).doubleValue();

                if (_pmf[i] <= 0.0) {
                    throw new IllegalActionException(this,
                            "Probabilities must be positive!");
                }

                sum = sum + _pmf[i];
            }

            //if (!SignalProcessing.close(sum, 1.0))
            //  throw new IllegalActionException(this,
            //    "Parameter values is required to sum to one.");
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Generate the Huffman codebook for the given <i>pmf</i>, and
     *  encode the input into booleans and send them to the output port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        ArrayToken alphabetArrayToken = (ArrayToken) alphabet.getToken();

        if (_pmf.length != alphabetArrayToken.length()) {
            throw new IllegalActionException(this,
                    "uncoded alphabet and pmf are required to be arrays"
                            + "with same length.");
        }

        // Token[] alphabetTokens = new Token[_pmf.length];

        // for (int i = 0; i < _pmf.length; i++) {
        //     alphabetTokens[i] = alphabetArrayToken.getElement(i);
        // }

        if (_parametersInvalid) {
            _parametersInvalid = false;
            _codeBook = generateCodeBook(_pmf);

            // FIXME: only produce the code book if the parameters
            // have been updated.
            StringToken[] codeBookTokens = new StringToken[_pmf.length];

            for (int i = 0; i < _pmf.length; i++) {
                codeBookTokens[i] = new StringToken(_codeBook[i]);
            }

            huffmanCodeBook.send(0, new ArrayToken(BaseType.STRING,
                    codeBookTokens));
        }
    }

    /** Generate the Huffman code book given the probability
     *  mass function.
     * @param pmf The probability mass function.
     * @return The code book, where each codeword is a string
     *  of '0' and '1'.
     */
    public String[] generateCodeBook(double[] pmf) {
        String[] codeBook = new String[pmf.length];
        LinkedList list = new LinkedList();

        // Generate the huffman code book.
        for (int i = 0; i < _pmf.length; i++) {
            // Create a list of nodes;
            Node node = new Node(_pmf[i], i);
            list.add(node);
        }

        // Construct the binary tree.
        while (list.size() > 1) {
            Node node1 = _findMinNode(list);
            list.remove(node1);

            Node node2 = _findMinNode(list);
            list.remove(node2);

            // node2 has larger prob than node1.
            Node newNode = new Node(node2, node1);
            list.add(newNode);
        }

        // Now there is only one element in the list,
        // and its probability should be 1.
        Node root = (Node) list.get(0);
        root.huffmanCode = "";
        _setCode(root, codeBook);
        return codeBook;
    }

    /** Initialize the actor by resetting the _parametersInvalid to true.
     *  Creat a linked list to store the nodes for the binary tree.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _parametersInvalid = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The huffman code book.
     */
    protected String[] _codeBook;

    /** Flag that indicates if the parameters are invalid. If it is
     *  true, then a new code book needs to be generated.
     */
    protected boolean _parametersInvalid;

    /** The probability mass function.
     */
    protected double[] _pmf;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the node with the minimum probability value in the
     *  given linked list.
     *  @param list The given linked list.
     *  @return The node with the minimum probability value.
     */
    private Node _findMinNode(LinkedList list) {
        double minProb = ((Node) list.get(0)).probability;
        int index = 0;

        for (int i = 1; i < list.size(); i++) {
            if (((Node) list.get(i)).probability < minProb) {
                index = i;
                minProb = ((Node) list.get(i)).probability;
            }
        }

        return (Node) list.get(index);
    }

    /** Set the Huffman codeword for the given node and all its children.
     * @param node The given node.
     * @param codeBook The code book to be generated.
     */
    private void _setCode(Node node, String[] codeBook) {
        String parentCode = node.huffmanCode;
        Node left;
        Node right;

        if ((left = node.leftChild) != null) {
            String leftCode = parentCode + "0";
            left.huffmanCode = leftCode;

            if (left.indexInArray >= 0) {
                codeBook[left.indexInArray] = leftCode;
            } else {
                _setCode(left, codeBook);
            }
        }

        if ((right = node.rightChild) != null) {
            String rightCode = parentCode + "1";
            right.huffmanCode = rightCode;

            if (right.indexInArray >= 0) {
                codeBook[right.indexInArray] = rightCode;
            } else {
                _setCode(right, codeBook);
            }
        }
    }
}
