/* Syntactic Contraction operator in Syntactic expression language.

Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.cg.lib.syntactic;

import java.util.List;

///////////////////////////////////////////////////////////////////
//// SyntacticContraction

/**
 This class represents a contraction operator in the SyntacticTerm
 language. It represents the operation of contracting a SyntacticTerm
 with <b>m</b> inputs and <b>n</b> outputs along the first <b>k</b>
 of each, where <b>k</b> is a parameter of the operator called
 <i>degree</i> in the class. The result is a term (as an interface)
 with <b>m-k</b> inputs and <b>n-k</b> outputs. The operand term being
 contracted is the <i>kernel</i> of the operator and is referred to
 by the class as such.

 The initial object, without a kernel, can be understood to be an
 operator. Once the kernel is set, the object can be reasonably
 treated as a syntactic term representing the operator acting on
 the kernel.

 @author Chris Shaver
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (shaver)
 @Pt.AcceptedRating Red
 */
public class SyntacticContraction implements SyntacticTerm {

    /** Makes a new contraction operator with a given constant degree.
     *  The degree is the number of outputs of the kernel that will
     *  be connected (enumerated ascending) to corresponding inputs.
     *
     *  @param degree Degree of the operator.
     */
    public SyntacticContraction(int degree) {
        _degree = degree > 0 ? degree : 0;
        _kernel = null;
        _rank = null;
    }

    /** Get a list of the input ports to the operator.
     *  If there is no kernel null is returned.
     *
     *  @return list of input ports.
     */
    @Override
    public List<SyntacticPort> getInputs() {
        if (_kernel == null) {
            return null;
        }
        List<SyntacticPort> ports = _kernel.getInputs();
        return ports.subList(_degree, ports.size());
    }

    /** Get a list of output ports to the operator.
     *  If there is no kernel null is returned.
     *
     *  @return list of output ports.
     */
    @Override
    public List<SyntacticPort> getOutputs() {
        if (_kernel == null) {
            return null;
        }
        List<SyntacticPort> ports = _kernel.getOutputs();
        return ports.subList(_degree, ports.size());
    }

    /** Get the number of inputs to the term.
     *
     *  @return number of inputs or zero if no kernel.
     */
    @Override
    public int sizeInputs() {
        return _kernel == null ? 0 : _kernel.sizeInputs() - _degree;
    }

    /** Get the number of outputs from the term.
     *
     *  @return number of outputs or zero if no kernel.
     */
    @Override
    public int sizeOutputs() {
        return _kernel == null ? 0 : _kernel.sizeOutputs() - _degree;
    }

    /** Get the rank of the term.
     *
     *  @return rank of term.
     */
    @Override
    public SyntacticRank rank() {
        return _rank;
    }

    /** Get the index of an input port given a reference to it.
     *  null is returned if the port does not exist in the term
     *  or the kernel is absent.
     *
     *  @param port Port to find the index of.
     *  @return the index of the port or null if none.
     */
    @Override
    public Integer inputIndex(SyntacticPort port) {
        if (_kernel == null) {
            return null;
        }
        Integer index = _kernel.inputIndex(port);
        return index == null || index < _degree ? null : index - _degree;
    }

    // FIXME: This is not fully implemented.
    /** Get the index of an output port given a reference to it.
     *  null is returned if the port does not exist in the term
     *  or the kernel is absent.
     *
     *  @param port Port to find the index of.
     *  @return the index of the port or null if none.
     */
    @Override
    public Integer outputIndex(SyntacticPort port) {
        return null;
    }

    /** Set the kernel of the operator, effectively making it a term.
     *  Once the kernel is set, the class can be understood via the
     *  SyntacticTerm interface to be the term constituted of the
     *  kernel operated on by the contraction.
     *
     *  If the rank is larger than the number of contractions that can
     *  happen on the kernel, it is not set and false is returned.
     *
     *  @param term Term to contract with operator.
     *  @return whether kernel was set.
     */
    public boolean setKernel(SyntacticTerm term) {
        SyntacticRank rank = term.rank();
        SyntacticRank crank = SyntacticRank.contract(rank, _degree);

        if (crank == null) {
            return false;
        }
        _rank = crank;
        _kernel = term;

        System.out.print("\nSetting Kernel with degree " + _degree
                + " and boundary " + crank.generateCode() + ".\n");

        return true;
    }

    /** Generate the lexical representation of the contraction.
     *  The generation of code recursively descends to generate
     *  the code corresponding to the kernel. The operator is
     *  notated:
     *
     *      { <i>degree</i> : <i>kernel code</i> }
     *
     *  @return lexical representation of term.
     */
    @Override
    public String generateCode() {
        if (_kernel == null) {
            return "{}";
        } else {
            return "{" + _degree + ": " + _kernel.generateCode() + "}";
        }
    }

    /** Get the sort order of the term.
     *  @return sort order of the term.
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /** Decide whether code can be generated from this term.
     *  @return whether code can be generated.
     */
    @Override
    public boolean hasCode() {
        return true;
    }

    /** Get the reference to the kernel of the operator.
     *  @return kernel of the operator.
     */
    public SyntacticTerm kernel() {
        return _kernel;
    }

    /** Get the degree of the term.
     *  @return degree of term.
     */
    public int degree() {
        return _degree;
    }

    ///////////////////////////////////////////////////////////////////

    /** Kernel of the operator. */
    private SyntacticTerm _kernel;

    /** Degree of the operator. */
    private int _degree;

    /** Rank of the term. */
    private SyntacticRank _rank;

}
