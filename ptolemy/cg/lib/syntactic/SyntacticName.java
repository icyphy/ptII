/* Syntactic Graph for syntactic representations.

Copyright (c) 2010 The Regents of the University of California.
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

public class SyntacticName implements SyntacticTerm {
    public SyntacticName() {
        _name = "";
        _term = null;
    }
    
    public List<SyntacticPort> getInputs() {
        return _term == null ? null : _term.getInputs();
    }
    
    public List<SyntacticPort> getOutputs() {
        return _term == null ? null : _term.getOutputs();
    }
    
    public int sizeInputs() {
        return _term == null ? 0 : _term.sizeInputs();
    }
    
    public int sizeOutputs() {
        return _term == null ? 0 : _term.sizeOutputs();
    }
    
    public SyntacticRank rank() {
        if (_term == null) return null;
        
        SyntacticRank rank = _term.rank();
        return rank == null ? null : rank.copy();
    }
    
    public Integer inputIndex(SyntacticPort port) {
        return _term == null ? null : _term.inputIndex(port);
    }
    
    public Integer outputIndex(SyntacticPort port) {
        return _term == null ? null : _term.outputIndex(port);
    }
    
    public String generateCode() {
        String boundary = SyntacticRank.noCode();
        if (_term != null) {
            SyntacticRank rank = _term.rank(); 
            if (rank != null) boundary = rank.generateCode();
        }
        return _name + boundary;
    }
    
    public String generateDefinitionCode() {
        return generateCode() + " = " + 
            (_term == null ? "null" : _term.generateCode());
    }
    
    public int getOrder() {
        return 0;
    }
    
    public boolean hasCode() {
        return true;
    }
    
    public void setName(String name) {
        _name = name;
    }
    
    public void bind(SyntacticTerm term) {
        _term = term;
    }
    
    public String getName() {
        return _name;
    }
    
    public SyntacticTerm getBound() {
        return _term;
    }
    
    private String _name;
    private SyntacticTerm _term;
    
}


