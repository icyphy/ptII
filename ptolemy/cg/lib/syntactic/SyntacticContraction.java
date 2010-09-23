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

public class SyntacticContraction implements SyntacticTerm {
    
    public SyntacticContraction(int degree) {
        _degree = degree > 0 ? degree : 0;
        _kernel = null;
        _rank = null;
    }
    
    public List<SyntacticPort> getInputs() {
        if (_kernel == null) return null;
        List<SyntacticPort> ports = _kernel.getInputs();
        return ports.subList(_degree, ports.size());
    }
    
    public List<SyntacticPort> getOutputs() {
        if (_kernel == null) return null;
        List<SyntacticPort> ports = _kernel.getOutputs();
        return ports.subList(_degree, ports.size());
    }
    
    public int sizeInputs() {
        return _kernel == null ? 0 : (_kernel.sizeInputs() - _degree);
    }
    
    public int sizeOutputs() {
        return _kernel == null ? 0 : (_kernel.sizeOutputs() - _degree);
    }
    
    public SyntacticRank rank() {
        return _rank;
    }
    
    public Integer inputIndex(SyntacticPort port) {
        if (_kernel == null) return null;
        Integer index = _kernel.inputIndex(port);
        return index == null || index < _degree ? null : (index - _degree);
    }
    
    public Integer outputIndex(SyntacticPort port) {
        return null;
    }
    
    public boolean setKernel(SyntacticTerm term) {
        SyntacticRank rank  = term.rank();
        SyntacticRank crank = SyntacticRank.contract(rank, _degree);
        
        if (crank == null) return false;
        _rank = crank;
        _kernel = term;
        
        System.out.print("\nSetting Kernel with degree "
            + _degree + " and boundary " + crank.generateCode() + ".\n");
        
        return true;
    }
    
    public String generateCode() {
        if (_kernel == null) return "{}";
        else return "{" + _degree + ": " + _kernel.generateCode() + "}";
    }
    
    public int getOrder() {
        return 0;
    }
    
    public boolean hasCode() {
        return true;
    }
    
    public SyntacticTerm kernel() {
        return _kernel;
    }
    
    public int degree() {
        return _degree;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private SyntacticTerm _kernel;
    private int _degree;
    private SyntacticRank _rank;
    
}
