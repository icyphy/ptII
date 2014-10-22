/* Syntactic Graph for syntactic representations.

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
////SyntacticName

/**
 This class represents a named SyntacticTerm. It could be either a
 a bound name referring to another term, possibly an expression,
 or a SyntacticNode. These terms are the leaves of the expression
 tree created using SyntacticTerm expression constructs. This mechanism
 could also be used for recursion, although this idea has not been
 completely worked out.

 @author Chris Shaver
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (shaver)
 @Pt.AcceptedRating Red
 */
public class SyntacticName implements SyntacticTerm {

    // TODO: make alternative constructor with name parameter.
    /** Make named term with no reference or name. */
    public SyntacticName() {
        _name = "";
        _term = null;
    }

    /** Get a list of the input ports to the operator.
     *  If there is no kernel null is returned.
     *
     *  @return list of input ports.
     */
    @Override
    public List<SyntacticPort> getInputs() {
        return _term == null ? null : _term.getInputs();
    }

    /** Get a list of output ports to the operator.
     *  If there is no kernel null is returned.
     *
     *  @return list of output ports.
     */
    @Override
    public List<SyntacticPort> getOutputs() {
        return _term == null ? null : _term.getOutputs();
    }

    /** Get the number of inputs to the term.
     *
     *  @return number of inputs or zero if no kernel.
     */
    @Override
    public int sizeInputs() {
        return _term == null ? 0 : _term.sizeInputs();
    }

    /** Get the number of outputs from the term.
     *
     *  @return number of outputs or zero if no kernel.
     */
    @Override
    public int sizeOutputs() {
        return _term == null ? 0 : _term.sizeOutputs();
    }

    /** Get the rank of the term.
     *
     *  @return rank of term.
     */
    @Override
    public SyntacticRank rank() {
        if (_term == null) {
            return null;
        }

        SyntacticRank rank = _term.rank();
        return rank == null ? null : rank.copy();
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
        return _term == null ? null : _term.inputIndex(port);
    }

    /** Get the index of an output port given a reference to it.
     *  null is returned if the port does not exist in the term
     *  or the kernel is absent.
     *
     *  @param port Port to find the index of.
     *  @return the index of the port or null if none.
     */
    @Override
    public Integer outputIndex(SyntacticPort port) {
        return _term == null ? null : _term.outputIndex(port);
    }

    /** Generate the lexical representation of the term.
     *  Since this is a leaf of the expression tree, the
     *  term is simply the name annotated with the rank:
     *
     *      <i>name</i>&lt; <i>rank</i> &gt;
     *
     *  @return lexical representation of term.
     */
    @Override
    public String generateCode() {
        String boundary = SyntacticRank.noCode();
        if (_term != null) {
            SyntacticRank rank = _term.rank();
            if (rank != null) {
                boundary = rank.generateCode();
            }
        }
        return _name + boundary;
    }

    /** Generate definition code for name in terms of the expression
     *  it is bound to. This code is of the form:
     *
     *      <i>node</i> = <i>term expression</i>
     *
     *  @return definition statement for the named term.
     */
    public String generateDefinitionCode() {
        return generateCode() + " = "
                + (_term == null ? "null" : _term.generateCode());
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

    /** Set the name of the term.
     *  @param name Name for the term.
     *  @see #getName
     */
    public void setName(String name) {
        _name = name;
    }

    /** Bind a given term to this named term.
     *
     *  @param term Term to bind to name.
     */
    public void bind(SyntacticTerm term) {
        _term = term;
    }

    /** Get the name of the term.
     *  @return name of term.
     *  @see #setName
     */
    public String getName() {
        return _name;
    }

    /** Get the term bound to this name.
     *  @return term bound.
     */
    public SyntacticTerm getBound() {
        return _term;
    }

    ///////////////////////////////////////////////////////////////////
    //// private variables

    /** Name of term. */
    private String _name;

    /** term bound to this named term. */
    private SyntacticTerm _term;

}
