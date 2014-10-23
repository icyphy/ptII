// $ANTLR 2.7.7 (2006-11-01): "populator.g" -> "PtalonPopulator.java"$
/* ANTLR TreeParser that populates a PtalonActor using a PtalonEvaluator.

 Copyright (c) 2006-2008 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import antlr.ASTPair;
import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;

/**
  PtalonPopulator.java generated from populator.g by ANTLR.

  @author Adam Cataldo, Elaine Cheong, Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 7.0
  @Pt.ProposedRating Red (celaine)
  @Pt.AcceptedRating Red (celaine)
 */
public class PtalonPopulator extends antlr.TreeParser implements
        PtalonPopulatorTokenTypes {

    private PtalonEvaluator info;

    public PtalonEvaluator getCodeManager() {
        return info;
    }

    //private String scopeName;

    public PtalonPopulator() {
        tokenNames = _tokenNames;
    }

    public final void port_declaration(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST port_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST g = null;
        PtalonAST g_AST = null;
        PtalonAST h = null;
        PtalonAST h_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST i = null;
        PtalonAST i_AST = null;
        PtalonAST j = null;
        PtalonAST j_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST k = null;
        PtalonAST k_AST = null;
        PtalonAST l = null;
        PtalonAST l_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;
        PtalonAST m = null;
        PtalonAST m_AST = null;
        PtalonAST n = null;
        PtalonAST n_AST = null;
        PtalonAST e = null;
        PtalonAST e_AST = null;
        PtalonAST o = null;
        PtalonAST o_AST = null;
        PtalonAST p = null;
        PtalonAST p_AST = null;
        PtalonAST f = null;
        PtalonAST f_AST = null;
        PtalonAST q = null;
        PtalonAST q_AST = null;
        PtalonAST r = null;
        PtalonAST r_AST = null;

        try { // for error handling
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case PORT: {
                AST __t2 = _t;
                PtalonAST tmp1_AST = null;
                tmp1_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp1_AST);
                ASTPair __currentAST2 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, PORT);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        a = (PtalonAST) _t;
                        a_AST = (PtalonAST) astFactory.create(a);
                        astFactory.addASTChild(currentAST, a_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(a.getText())) {
                            info.addPort(a.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t4 = _t;
                        PtalonAST tmp2_AST = null;
                        tmp2_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp2_AST);
                        ASTPair __currentAST4 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        g = (PtalonAST) _t;
                        g_AST = (PtalonAST) astFactory.create(g);
                        astFactory.addASTChild(currentAST, g_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        h = (PtalonAST) _t;
                        h_AST = (PtalonAST) astFactory.create(h);
                        astFactory.addASTChild(currentAST, h_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST4;
                        _t = __t4;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(h.getText());
                            if (value != null) {
                                String name = g.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "port");
                                }
                                if (!info.isCreated(name)) {
                                    info.addPort(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST2;
                _t = __t2;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case INPORT: {
                AST __t5 = _t;
                PtalonAST tmp3_AST = null;
                tmp3_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp3_AST);
                ASTPair __currentAST5 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, INPORT);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        b = (PtalonAST) _t;
                        b_AST = (PtalonAST) astFactory.create(b);
                        astFactory.addASTChild(currentAST, b_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(b.getText())) {
                            info.addInPort(b.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t7 = _t;
                        PtalonAST tmp4_AST = null;
                        tmp4_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp4_AST);
                        ASTPair __currentAST7 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        i = (PtalonAST) _t;
                        i_AST = (PtalonAST) astFactory.create(i);
                        astFactory.addASTChild(currentAST, i_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        j = (PtalonAST) _t;
                        j_AST = (PtalonAST) astFactory.create(j);
                        astFactory.addASTChild(currentAST, j_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST7;
                        _t = __t7;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(j.getText());
                            if (value != null) {
                                String name = i.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "inport");
                                }
                                if (!info.isCreated(name)) {
                                    info.addInPort(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST5;
                _t = __t5;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case OUTPORT: {
                AST __t8 = _t;
                PtalonAST tmp5_AST = null;
                tmp5_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp5_AST);
                ASTPair __currentAST8 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, OUTPORT);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        c = (PtalonAST) _t;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(c.getText())) {
                            info.addOutPort(c.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t10 = _t;
                        PtalonAST tmp6_AST = null;
                        tmp6_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp6_AST);
                        ASTPair __currentAST10 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        k = (PtalonAST) _t;
                        k_AST = (PtalonAST) astFactory.create(k);
                        astFactory.addASTChild(currentAST, k_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        l = (PtalonAST) _t;
                        l_AST = (PtalonAST) astFactory.create(l);
                        astFactory.addASTChild(currentAST, l_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST10;
                        _t = __t10;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(l.getText());
                            if (value != null) {
                                String name = k.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "outport");
                                }
                                if (!info.isCreated(name)) {
                                    info.addOutPort(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST8;
                _t = __t8;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case MULTIPORT: {
                AST __t11 = _t;
                PtalonAST tmp7_AST = null;
                tmp7_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp7_AST);
                ASTPair __currentAST11 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, MULTIPORT);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        d = (PtalonAST) _t;
                        d_AST = (PtalonAST) astFactory.create(d);
                        astFactory.addASTChild(currentAST, d_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(d.getText())) {
                            info.addPort(d.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t13 = _t;
                        PtalonAST tmp8_AST = null;
                        tmp8_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp8_AST);
                        ASTPair __currentAST13 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        m = (PtalonAST) _t;
                        m_AST = (PtalonAST) astFactory.create(m);
                        astFactory.addASTChild(currentAST, m_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        n = (PtalonAST) _t;
                        n_AST = (PtalonAST) astFactory.create(n);
                        astFactory.addASTChild(currentAST, n_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST13;
                        _t = __t13;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(n.getText());
                            if (value != null) {
                                String name = m.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "multiport");
                                }
                                if (!info.isCreated(name)) {
                                    info.addPort(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST11;
                _t = __t11;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case MULTIINPORT: {
                AST __t14 = _t;
                PtalonAST tmp9_AST = null;
                tmp9_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp9_AST);
                ASTPair __currentAST14 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, MULTIINPORT);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        e = (PtalonAST) _t;
                        e_AST = (PtalonAST) astFactory.create(e);
                        astFactory.addASTChild(currentAST, e_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(e.getText())) {
                            info.addInPort(e.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t16 = _t;
                        PtalonAST tmp10_AST = null;
                        tmp10_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp10_AST);
                        ASTPair __currentAST16 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        o = (PtalonAST) _t;
                        o_AST = (PtalonAST) astFactory.create(o);
                        astFactory.addASTChild(currentAST, o_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        p = (PtalonAST) _t;
                        p_AST = (PtalonAST) astFactory.create(p);
                        astFactory.addASTChild(currentAST, p_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST16;
                        _t = __t16;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(p.getText());
                            if (value != null) {
                                String name = o.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "multiinport");
                                }
                                if (!info.isCreated(name)) {
                                    info.addInPort(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST14;
                _t = __t14;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case MULTIOUTPORT: {
                AST __t17 = _t;
                PtalonAST tmp11_AST = null;
                tmp11_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp11_AST);
                ASTPair __currentAST17 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, MULTIOUTPORT);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        f = (PtalonAST) _t;
                        f_AST = (PtalonAST) astFactory.create(f);
                        astFactory.addASTChild(currentAST, f_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(f.getText())) {
                            info.addOutPort(f.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t19 = _t;
                        PtalonAST tmp12_AST = null;
                        tmp12_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp12_AST);
                        ASTPair __currentAST19 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        q = (PtalonAST) _t;
                        q_AST = (PtalonAST) astFactory.create(q);
                        astFactory.addASTChild(currentAST, q_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        r = (PtalonAST) _t;
                        r_AST = (PtalonAST) astFactory.create(r);
                        astFactory.addASTChild(currentAST, r_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST19;
                        _t = __t19;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(r.getText());
                            if (value != null) {
                                String name = q.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "multioutport");
                                }
                                if (!info.isCreated(name)) {
                                    info.addOutPort(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST17;
                _t = __t17;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        } catch (PtalonScopeException excep) {

            throw new PtalonRuntimeException("", excep);

        }
        returnAST = port_declaration_AST;
        _retTree = _t;
    }

    public final void parameter_declaration(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST parameter_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;

        try { // for error handling
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case PARAMETER: {
                AST __t21 = _t;
                PtalonAST tmp13_AST = null;
                tmp13_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp13_AST);
                ASTPair __currentAST21 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, PARAMETER);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        a = (PtalonAST) _t;
                        a_AST = (PtalonAST) astFactory.create(a);
                        astFactory.addASTChild(currentAST, a_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();

                        if (info.isReady() && !info.isCreated(a.getText())) {
                            info.addParameter(a.getText());
                        }

                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t23 = _t;
                        PtalonAST tmp14_AST = null;
                        tmp14_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp14_AST);
                        ASTPair __currentAST23 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        c = (PtalonAST) _t;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        d = (PtalonAST) _t;
                        d_AST = (PtalonAST) astFactory.create(d);
                        astFactory.addASTChild(currentAST, d_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST23;
                        _t = __t23;
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(d.getText());
                            if (value != null) {
                                String name = c.getText() + value;
                                if (!info.inScope(name)) {
                                    info.addSymbol(name, "parameter");
                                }
                                if (!info.isCreated(name)) {
                                    info.addParameter(name);
                                }
                            }
                        }

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST21;
                _t = __t21;
                _t = _t.getNextSibling();
                parameter_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case ACTOR: {
                AST __t24 = _t;
                PtalonAST tmp15_AST = null;
                tmp15_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp15_AST);
                ASTPair __currentAST24 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR);
                _t = _t.getFirstChild();
                b = (PtalonAST) _t;
                b_AST = (PtalonAST) astFactory.create(b);
                astFactory.addASTChild(currentAST, b_AST);
                match(_t, ID);
                _t = _t.getNextSibling();

                if (info.isReady() && !info.isCreated(b.getText())) {
                    info.addActorParameter(b.getText());
                }

                currentAST = __currentAST24;
                _t = __t24;
                _t = _t.getNextSibling();
                parameter_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        } catch (PtalonScopeException excep) {

            throw new PtalonRuntimeException("", excep);

        }
        returnAST = parameter_declaration_AST;
        _retTree = _t;
    }

    public final void assigned_parameter_declaration(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST assigned_parameter_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;
        PtalonAST e = null;
        PtalonAST e_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST q = null;

        boolean dynamic_name = false;

        try { // for error handling
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case PARAM_EQUALS: {
                AST __t26 = _t;
                PtalonAST tmp16_AST = null;
                tmp16_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp16_AST);
                ASTPair __currentAST26 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, PARAM_EQUALS);
                _t = _t.getFirstChild();
                AST __t27 = _t;
                PtalonAST tmp17_AST = null;
                tmp17_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp17_AST);
                ASTPair __currentAST27 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, PARAMETER);
                _t = _t.getFirstChild();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        a = (PtalonAST) _t;
                        a_AST = (PtalonAST) astFactory.create(a);
                        astFactory.addASTChild(currentAST, a_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t29 = _t;
                        PtalonAST tmp18_AST = null;
                        tmp18_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp18_AST);
                        ASTPair __currentAST29 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        c = (PtalonAST) _t;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        d = (PtalonAST) _t;
                        d_AST = (PtalonAST) astFactory.create(d);
                        astFactory.addASTChild(currentAST, d_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST29;
                        _t = __t29;
                        _t = _t.getNextSibling();

                        dynamic_name = true;

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST27;
                _t = __t27;
                _t = _t.getNextSibling();
                e = (PtalonAST) _t;
                e_AST = (PtalonAST) astFactory.create(e);
                astFactory.addASTChild(currentAST, e_AST);
                match(_t, EXPRESSION);
                _t = _t.getNextSibling();

                if (dynamic_name) {
                    if (info.isReady()) {
                        String value = info.evaluateString(d.getText());
                        if (value != null) {
                            String name = c.getText() + value;
                            if (!info.inScope(name)) {
                                info.addSymbol(name, "parameter");
                            }
                            if (!info.isCreated(name)) {
                                info.addParameter(name, e.getText());
                            }
                        }
                    }
                } else {
                    if (info.isReady() && !info.isCreated(a.getText())) {
                        info.addParameter(a.getText(), e.getText());
                    }
                }

                currentAST = __currentAST26;
                _t = __t26;
                _t = _t.getNextSibling();
                assigned_parameter_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case ACTOR_EQUALS: {
                AST __t30 = _t;
                PtalonAST tmp19_AST = null;
                tmp19_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp19_AST);
                ASTPair __currentAST30 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR_EQUALS);
                _t = _t.getFirstChild();
                AST __t31 = _t;
                PtalonAST tmp20_AST = null;
                tmp20_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp20_AST);
                ASTPair __currentAST31 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR);
                _t = _t.getFirstChild();
                b = (PtalonAST) _t;
                b_AST = (PtalonAST) astFactory.create(b);
                astFactory.addASTChild(currentAST, b_AST);
                match(_t, ID);
                _t = _t.getNextSibling();
                currentAST = __currentAST31;
                _t = __t31;
                _t = _t.getNextSibling();
                q = _t == ASTNULL ? null : (PtalonAST) _t;
                qualified_identifier(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);

                if (info.isReady() && !info.isCreated(b.getText())) {
                    info.addActorParameter(b.getText(), q.getText());
                }

                currentAST = __currentAST30;
                _t = __t30;
                _t = _t.getNextSibling();
                assigned_parameter_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        } catch (PtalonScopeException excep) {

            throw new PtalonRuntimeException("", excep);

        }
        returnAST = assigned_parameter_declaration_AST;
        _retTree = _t;
    }

    public final void qualified_identifier(AST _t) throws RecognitionException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST qualified_identifier_AST = null;

        PtalonAST tmp21_AST = null;
        tmp21_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp21_AST);
        match(_t, QUALID);
        _t = _t.getNextSibling();
        qualified_identifier_AST = (PtalonAST) currentAST.root;
        returnAST = qualified_identifier_AST;
        _retTree = _t;
    }

    public final void relation_declaration(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST relation_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;

        try { // for error handling
            AST __t33 = _t;
            PtalonAST tmp22_AST = null;
            tmp22_AST = (PtalonAST) astFactory.create(_t);
            astFactory.addASTChild(currentAST, tmp22_AST);
            ASTPair __currentAST33 = currentAST.copy();
            currentAST.root = currentAST.child;
            currentAST.child = null;
            match(_t, RELATION);
            _t = _t.getFirstChild();
            {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case ID: {
                    a = (PtalonAST) _t;
                    a_AST = (PtalonAST) astFactory.create(a);
                    astFactory.addASTChild(currentAST, a_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();

                    if (info.isReady() && !info.isCreated(a.getText())) {
                        info.addRelation(a.getText());
                    }

                    break;
                }
                case DYNAMIC_NAME: {
                    AST __t35 = _t;
                    PtalonAST tmp23_AST = null;
                    tmp23_AST = (PtalonAST) astFactory.create(_t);
                    astFactory.addASTChild(currentAST, tmp23_AST);
                    ASTPair __currentAST35 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    c = (PtalonAST) _t;
                    c_AST = (PtalonAST) astFactory.create(c);
                    astFactory.addASTChild(currentAST, c_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    d = (PtalonAST) _t;
                    d_AST = (PtalonAST) astFactory.create(d);
                    astFactory.addASTChild(currentAST, d_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();
                    currentAST = __currentAST35;
                    _t = __t35;
                    _t = _t.getNextSibling();

                    if (info.isReady()) {
                        String value = info.evaluateString(d.getText());
                        if (value != null) {
                            String name = c.getText() + value;
                            if (!info.inScope(name)) {
                                info.addSymbol(name, "relation");
                            }
                            if (!info.isCreated(name)) {
                                info.addRelation(name);
                            }
                        }
                    }

                    break;
                }
                default: {
                    throw new NoViableAltException(_t);
                }
                }
            }
            currentAST = __currentAST33;
            _t = __t33;
            _t = _t.getNextSibling();
            relation_declaration_AST = (PtalonAST) currentAST.root;
        } catch (PtalonScopeException excep) {

            throw new PtalonRuntimeException("", excep);

        }
        returnAST = relation_declaration_AST;
        _retTree = _t;
    }

    public final void transparent_relation_declaration(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST transparent_relation_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;

        try { // for error handling
            AST __t37 = _t;
            PtalonAST tmp24_AST = null;
            tmp24_AST = (PtalonAST) astFactory.create(_t);
            astFactory.addASTChild(currentAST, tmp24_AST);
            ASTPair __currentAST37 = currentAST.copy();
            currentAST.root = currentAST.child;
            currentAST.child = null;
            match(_t, TRANSPARENT);
            _t = _t.getFirstChild();
            {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case ID: {
                    a = (PtalonAST) _t;
                    a_AST = (PtalonAST) astFactory.create(a);
                    astFactory.addASTChild(currentAST, a_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();

                    if (info.isReady() && !info.isCreated(a.getText())) {
                        info.addTransparentRelation(a.getText());
                    }

                    break;
                }
                case DYNAMIC_NAME: {
                    AST __t39 = _t;
                    PtalonAST tmp25_AST = null;
                    tmp25_AST = (PtalonAST) astFactory.create(_t);
                    astFactory.addASTChild(currentAST, tmp25_AST);
                    ASTPair __currentAST39 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    c = (PtalonAST) _t;
                    c_AST = (PtalonAST) astFactory.create(c);
                    astFactory.addASTChild(currentAST, c_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    d = (PtalonAST) _t;
                    d_AST = (PtalonAST) astFactory.create(d);
                    astFactory.addASTChild(currentAST, d_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();
                    currentAST = __currentAST39;
                    _t = __t39;
                    _t = _t.getNextSibling();

                    if (info.isReady()) {
                        String value = info.evaluateString(d.getText());
                        if (value != null) {
                            String name = c.getText() + value;
                            if (!info.inScope(name)) {
                                info.addSymbol(name, "transparent");
                            }
                            if (!info.isCreated(name)) {
                                info.addTransparentRelation(name);
                            }
                        }
                    }

                    break;
                }
                default: {
                    throw new NoViableAltException(_t);
                }
                }
            }
            currentAST = __currentAST37;
            _t = __t37;
            _t = _t.getNextSibling();
            transparent_relation_declaration_AST = (PtalonAST) currentAST.root;
        } catch (PtalonScopeException excep) {

            throw new PtalonRuntimeException("", excep);

        }
        returnAST = transparent_relation_declaration_AST;
        _retTree = _t;
    }

    public final void assignment(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST assignment_AST = null;
        PtalonAST left = null;
        PtalonAST left_AST = null;
        PtalonAST leftExp = null;
        PtalonAST leftExp_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;
        PtalonAST i = null;
        PtalonAST i_AST = null;
        PtalonAST e = null;
        PtalonAST e_AST = null;
        PtalonAST p = null;
        PtalonAST p_AST = null;

        boolean addAssignment = false;
        String name = "";

        try { // for error handling
            AST __t42 = _t;
            PtalonAST tmp26_AST = null;
            tmp26_AST = (PtalonAST) astFactory.create(_t);
            astFactory.addASTChild(currentAST, tmp26_AST);
            ASTPair __currentAST42 = currentAST.copy();
            currentAST.root = currentAST.child;
            currentAST.child = null;
            match(_t, ASSIGN);
            _t = _t.getFirstChild();
            {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case ID: {
                    PtalonAST tmp27_AST = null;
                    tmp27_AST = (PtalonAST) astFactory.create(_t);
                    astFactory.addASTChild(currentAST, tmp27_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    break;
                }
                case DYNAMIC_NAME: {
                    AST __t44 = _t;
                    PtalonAST tmp28_AST = null;
                    tmp28_AST = (PtalonAST) astFactory.create(_t);
                    astFactory.addASTChild(currentAST, tmp28_AST);
                    ASTPair __currentAST44 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    left = (PtalonAST) _t;
                    left_AST = (PtalonAST) astFactory.create(left);
                    astFactory.addASTChild(currentAST, left_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    leftExp = (PtalonAST) _t;
                    leftExp_AST = (PtalonAST) astFactory.create(leftExp);
                    astFactory.addASTChild(currentAST, leftExp_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();

                    if (info.isReady()) {
                        String value = info.evaluateString(leftExp.getText());
                        if (value != null) {
                            name = left.getText() + value;
                            addAssignment = true;
                        }
                    }

                    currentAST = __currentAST44;
                    _t = __t44;
                    _t = _t.getNextSibling();
                    break;
                }
                default: {
                    throw new NoViableAltException(_t);
                }
                }
            }
            {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case ID: {
                    b = (PtalonAST) _t;
                    b_AST = (PtalonAST) astFactory.create(b);
                    astFactory.addASTChild(currentAST, b_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();

                    if (addAssignment) {
                        info.addPortAssign(name, b.getText());
                    }

                    break;
                }
                case DYNAMIC_NAME: {
                    AST __t46 = _t;
                    d = _t == ASTNULL ? null : (PtalonAST) _t;
                    d_AST = (PtalonAST) astFactory.create(d);
                    astFactory.addASTChild(currentAST, d_AST);
                    ASTPair __currentAST46 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    i = (PtalonAST) _t;
                    i_AST = (PtalonAST) astFactory.create(i);
                    astFactory.addASTChild(currentAST, i_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    e = (PtalonAST) _t;
                    e_AST = (PtalonAST) astFactory.create(e);
                    astFactory.addASTChild(currentAST, e_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();

                    if (addAssignment) {
                        info.addPortAssign(name, i.getText(), e.getText());
                    }

                    currentAST = __currentAST46;
                    _t = __t46;
                    _t = _t.getNextSibling();
                    break;
                }
                case ACTOR_DECLARATION: {
                    nested_actor_declaration(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case EXPRESSION: {
                    p = (PtalonAST) _t;
                    p_AST = (PtalonAST) astFactory.create(p);
                    astFactory.addASTChild(currentAST, p_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();

                    if (addAssignment) {
                        info.addParameterAssign(name, p.getText());
                    }

                    break;
                }
                default: {
                    throw new NoViableAltException(_t);
                }
                }
            }
            currentAST = __currentAST42;
            _t = __t42;
            _t = _t.getNextSibling();
            assignment_AST = (PtalonAST) currentAST.root;
        } catch (PtalonScopeException excep) {

            throw new PtalonRuntimeException("", excep);

        }
        returnAST = assignment_AST;
        _retTree = _t;
    }

    /**
     * In this case we do not add any actors, but rather
     * defer this decision to any generated actors.
     */
    public final void nested_actor_declaration(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST nested_actor_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        AST __t55 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST55 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, ACTOR_DECLARATION);
        _t = _t.getFirstChild();

        info.enterActorDeclaration(a.getText());

        {
            _loop57: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                if (_t.getType() == ASSIGN) {
                    assignment(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                } else {
                    break _loop57;
                }

            } while (true);
        }

        info.exitActorDeclaration();

        currentAST = __currentAST55;
        _t = __t55;
        _t = _t.getNextSibling();
        nested_actor_declaration_AST = (PtalonAST) currentAST.root;
        returnAST = nested_actor_declaration_AST;
        _retTree = _t;
    }

    /**
     * This is for a top level actor declaration, which
     * requires separate treatment from a nested actor
     * declaration.
     */
    public final void actor_declaration(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST actor_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;

        String name = null;

        AST __t48 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST48 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, ACTOR_DECLARATION);
        _t = _t.getFirstChild();

        info.enterActorDeclaration(a.getText());

        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case ACTOR_ID: {
                AST __t50 = _t;
                b = _t == ASTNULL ? null : (PtalonAST) _t;
                b_AST = (PtalonAST) astFactory.create(b);
                astFactory.addASTChild(currentAST, b_AST);
                ASTPair __currentAST50 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR_ID);
                _t = _t.getFirstChild();
                name = b.getText();
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case EXPRESSION: {
                        c = (PtalonAST) _t;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();

                        if (info.isReady()) {
                            String value = info.evaluateString(c.getText());
                            if (value != null) {
                                name = name + value;
                            }
                        } else {
                            name = null;
                        }

                        break;
                    }
                    case 3: {
                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST50;
                _t = __t50;
                _t = _t.getNextSibling();
                break;
            }
            case 3:
            case ASSIGN: {
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }
        {
            _loop53: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                if (_t.getType() == ASSIGN) {
                    assignment(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                } else {
                    break _loop53;
                }

            } while (true);
        }

        if (info.isActorReady()) {
            info.addActor(name);
        }
        info.exitActorDeclaration();

        currentAST = __currentAST48;
        _t = __t48;
        _t = _t.getNextSibling();
        actor_declaration_AST = (PtalonAST) currentAST.root;
        returnAST = actor_declaration_AST;
        _retTree = _t;
    }

    public final void atomic_statement(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST atomic_statement_AST = null;

        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case PORT:
            case INPORT:
            case OUTPORT:
            case MULTIPORT:
            case MULTIINPORT:
            case MULTIOUTPORT: {
                port_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case PARAMETER:
            case ACTOR: {
                parameter_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case PARAM_EQUALS:
            case ACTOR_EQUALS: {
                assigned_parameter_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case RELATION: {
                relation_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case TRANSPARENT: {
                transparent_relation_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case ACTOR_DECLARATION: {
                actor_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case NEGATE:
            case OPTIONAL:
            case REMOVE:
            case PRESERVE: {
                transformation_declaration(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }
        atomic_statement_AST = (PtalonAST) currentAST.root;
        returnAST = atomic_statement_AST;
        _retTree = _t;
    }

    public final void transformation_declaration(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST transformation_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST d = null;
        PtalonAST d_AST = null;
        PtalonAST e = null;
        PtalonAST e_AST = null;
        PtalonAST f = null;
        PtalonAST f_AST = null;
        PtalonAST g = null;
        PtalonAST g_AST = null;
        PtalonAST h = null;
        PtalonAST h_AST = null;
        PtalonAST i = null;
        PtalonAST i_AST = null;
        PtalonAST l = null;
        PtalonAST l_AST = null;
        PtalonAST m = null;
        PtalonAST m_AST = null;
        PtalonAST n = null;
        PtalonAST n_AST = null;

        String id;
        String expr = null;
        int type = -1;

        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case NEGATE: {
                AST __t62 = _t;
                PtalonAST tmp29_AST = null;
                tmp29_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp29_AST);
                ASTPair __currentAST62 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, NEGATE);
                _t = _t.getFirstChild();
                type = 0;
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        a = (PtalonAST) _t;
                        a_AST = (PtalonAST) astFactory.create(a);
                        astFactory.addASTChild(currentAST, a_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        id = a.getText();
                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t64 = _t;
                        PtalonAST tmp30_AST = null;
                        tmp30_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp30_AST);
                        ASTPair __currentAST64 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        b = (PtalonAST) _t;
                        b_AST = (PtalonAST) astFactory.create(b);
                        astFactory.addASTChild(currentAST, b_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        c = (PtalonAST) _t;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST64;
                        _t = __t64;
                        _t = _t.getNextSibling();
                        id = b.getText();
                        expr = c.getText();
                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST62;
                _t = __t62;
                _t = _t.getNextSibling();
                break;
            }
            case OPTIONAL: {
                AST __t65 = _t;
                PtalonAST tmp31_AST = null;
                tmp31_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp31_AST);
                ASTPair __currentAST65 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, OPTIONAL);
                _t = _t.getFirstChild();
                type = 1;
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        d = (PtalonAST) _t;
                        d_AST = (PtalonAST) astFactory.create(d);
                        astFactory.addASTChild(currentAST, d_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        id = d.getText();
                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t67 = _t;
                        PtalonAST tmp32_AST = null;
                        tmp32_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp32_AST);
                        ASTPair __currentAST67 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        e = (PtalonAST) _t;
                        e_AST = (PtalonAST) astFactory.create(e);
                        astFactory.addASTChild(currentAST, e_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        f = (PtalonAST) _t;
                        f_AST = (PtalonAST) astFactory.create(f);
                        astFactory.addASTChild(currentAST, f_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST67;
                        _t = __t67;
                        _t = _t.getNextSibling();
                        id = e.getText();
                        expr = f.getText();
                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST65;
                _t = __t65;
                _t = _t.getNextSibling();
                break;
            }
            case REMOVE: {
                AST __t68 = _t;
                PtalonAST tmp33_AST = null;
                tmp33_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp33_AST);
                ASTPair __currentAST68 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, REMOVE);
                _t = _t.getFirstChild();
                type = 2;
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        g = (PtalonAST) _t;
                        g_AST = (PtalonAST) astFactory.create(g);
                        astFactory.addASTChild(currentAST, g_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        id = g.getText();
                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t70 = _t;
                        PtalonAST tmp34_AST = null;
                        tmp34_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp34_AST);
                        ASTPair __currentAST70 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        h = (PtalonAST) _t;
                        h_AST = (PtalonAST) astFactory.create(h);
                        astFactory.addASTChild(currentAST, h_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        i = (PtalonAST) _t;
                        i_AST = (PtalonAST) astFactory.create(i);
                        astFactory.addASTChild(currentAST, i_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST70;
                        _t = __t70;
                        _t = _t.getNextSibling();
                        id = h.getText();
                        expr = i.getText();
                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST68;
                _t = __t68;
                _t = _t.getNextSibling();
                break;
            }
            case PRESERVE: {
                AST __t71 = _t;
                PtalonAST tmp35_AST = null;
                tmp35_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp35_AST);
                ASTPair __currentAST71 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, PRESERVE);
                _t = _t.getFirstChild();
                type = 3;
                {
                    if (_t == null) {
                        _t = ASTNULL;
                    }
                    switch (_t.getType()) {
                    case ID: {
                        l = (PtalonAST) _t;
                        l_AST = (PtalonAST) astFactory.create(l);
                        astFactory.addASTChild(currentAST, l_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        id = l.getText();
                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t73 = _t;
                        PtalonAST tmp36_AST = null;
                        tmp36_AST = (PtalonAST) astFactory.create(_t);
                        astFactory.addASTChild(currentAST, tmp36_AST);
                        ASTPair __currentAST73 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        m = (PtalonAST) _t;
                        m_AST = (PtalonAST) astFactory.create(m);
                        astFactory.addASTChild(currentAST, m_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        n = (PtalonAST) _t;
                        n_AST = (PtalonAST) astFactory.create(n);
                        astFactory.addASTChild(currentAST, n_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST73;
                        _t = __t73;
                        _t = _t.getNextSibling();
                        id = m.getText();
                        expr = n.getText();
                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST71;
                _t = __t71;
                _t = _t.getNextSibling();
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }

        if (expr != null) {
            String value = info.evaluateString(expr);
            if (value != null) {
                id = id + value;
            } else {
                id = null;
            }
        }
        if (id != null && info.isReady()) {
            if (type == 0) {
                info.negateObject(id);
            } else if (type == 1) {
                info.optionalObject(id);
            } else if (type == 2) {
                info.removeObject(id);
            } else if (type == 3) {
                info.preserveObject(id);
            }
        }

        transformation_declaration_AST = (PtalonAST) currentAST.root;
        returnAST = transformation_declaration_AST;
        _retTree = _t;
    }

    public final void conditional_statement(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST conditional_statement_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST e = null;
        PtalonAST e_AST = null;

        boolean ready;

        AST __t75 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST75 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, IF);
        _t = _t.getFirstChild();

        info.enterIfScope(a.getText());
        ready = info.isIfReady();

        e = (PtalonAST) _t;
        e_AST = (PtalonAST) astFactory.create(e);
        astFactory.addASTChild(currentAST, e_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();

        if (ready) {
            info.setActiveBranch(info.evaluateBoolean(e.getText()));
        }

        AST __t76 = _t;
        PtalonAST tmp37_AST = null;
        tmp37_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp37_AST);
        ASTPair __currentAST76 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, TRUEBRANCH);
        _t = _t.getFirstChild();

        if (ready) {
            info.setCurrentBranch(true);
        }

        {
            _loop78: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case PORT:
                case INPORT:
                case OUTPORT:
                case PARAMETER:
                case ACTOR:
                case RELATION:
                case TRANSPARENT:
                case NEGATE:
                case OPTIONAL:
                case REMOVE:
                case PRESERVE:
                case ACTOR_DECLARATION:
                case MULTIPORT:
                case MULTIINPORT:
                case MULTIOUTPORT:
                case PARAM_EQUALS:
                case ACTOR_EQUALS: {
                    atomic_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case IF: {
                    conditional_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case FOR: {
                    iterative_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                default: {
                    break _loop78;
                }
                }
            } while (true);
        }
        currentAST = __currentAST76;
        _t = __t76;
        _t = _t.getNextSibling();
        AST __t79 = _t;
        PtalonAST tmp38_AST = null;
        tmp38_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp38_AST);
        ASTPair __currentAST79 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, FALSEBRANCH);
        _t = _t.getFirstChild();

        if (ready) {
            info.setCurrentBranch(false);
        }

        {
            _loop81: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case PORT:
                case INPORT:
                case OUTPORT:
                case PARAMETER:
                case ACTOR:
                case RELATION:
                case TRANSPARENT:
                case NEGATE:
                case OPTIONAL:
                case REMOVE:
                case PRESERVE:
                case ACTOR_DECLARATION:
                case MULTIPORT:
                case MULTIINPORT:
                case MULTIOUTPORT:
                case PARAM_EQUALS:
                case ACTOR_EQUALS: {
                    atomic_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case IF: {
                    conditional_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case FOR: {
                    iterative_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                default: {
                    break _loop81;
                }
                }
            } while (true);
        }
        currentAST = __currentAST79;
        _t = __t79;
        _t = _t.getNextSibling();
        currentAST = __currentAST75;
        _t = __t75;
        _t = _t.getNextSibling();

        info.exitIfScope();

        conditional_statement_AST = (PtalonAST) currentAST.root;
        returnAST = conditional_statement_AST;
        _retTree = _t;
    }

    public final void iterative_statement(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST iterative_statement_AST = null;
        PtalonAST f = null;
        PtalonAST f_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST n = null;
        PtalonAST n_AST = null;

        boolean ready;
        PtalonAST inputAST = (PtalonAST) _t;

        AST __t83 = _t;
        f = _t == ASTNULL ? null : (PtalonAST) _t;
        f_AST = (PtalonAST) astFactory.create(f);
        astFactory.addASTChild(currentAST, f_AST);
        ASTPair __currentAST83 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, FOR);
        _t = _t.getFirstChild();
        AST __t84 = _t;
        PtalonAST tmp39_AST = null;
        tmp39_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp39_AST);
        ASTPair __currentAST84 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, VARIABLE);
        _t = _t.getFirstChild();
        a = (PtalonAST) _t;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        match(_t, ID);
        _t = _t.getNextSibling();
        currentAST = __currentAST84;
        _t = __t84;
        _t = _t.getNextSibling();
        AST __t85 = _t;
        PtalonAST tmp40_AST = null;
        tmp40_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp40_AST);
        ASTPair __currentAST85 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, INITIALLY);
        _t = _t.getFirstChild();
        b = (PtalonAST) _t;
        b_AST = (PtalonAST) astFactory.create(b);
        astFactory.addASTChild(currentAST, b_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST85;
        _t = __t85;
        _t = _t.getNextSibling();
        AST __t86 = _t;
        PtalonAST tmp41_AST = null;
        tmp41_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp41_AST);
        ASTPair __currentAST86 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, SATISFIES);
        _t = _t.getFirstChild();
        c = (PtalonAST) _t;
        c_AST = (PtalonAST) astFactory.create(c);
        astFactory.addASTChild(currentAST, c_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST86;
        _t = __t86;
        _t = _t.getNextSibling();

        info.enterForScope(f.getText(), inputAST, this);
        ready = info.isForReady();
        if (ready) {
            info.setActiveBranch(true);
            info.setCurrentBranch(false);
        }

        {
            _loop88: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case PORT:
                case INPORT:
                case OUTPORT:
                case PARAMETER:
                case ACTOR:
                case RELATION:
                case TRANSPARENT:
                case NEGATE:
                case OPTIONAL:
                case REMOVE:
                case PRESERVE:
                case ACTOR_DECLARATION:
                case MULTIPORT:
                case MULTIINPORT:
                case MULTIOUTPORT:
                case PARAM_EQUALS:
                case ACTOR_EQUALS: {
                    atomic_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case IF: {
                    conditional_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case FOR: {
                    iterative_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                default: {
                    break _loop88;
                }
                }
            } while (true);
        }
        AST __t89 = _t;
        PtalonAST tmp42_AST = null;
        tmp42_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp42_AST);
        ASTPair __currentAST89 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, NEXT);
        _t = _t.getFirstChild();
        n = (PtalonAST) _t;
        n_AST = (PtalonAST) astFactory.create(n);
        astFactory.addASTChild(currentAST, n_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST89;
        _t = __t89;
        _t = _t.getNextSibling();
        currentAST = __currentAST83;
        _t = __t83;
        _t = _t.getNextSibling();

        if (ready) {
            info.evaluateForScope();
        }
        info.exitForScope();

        iterative_statement_AST = (PtalonAST) currentAST.root;
        returnAST = iterative_statement_AST;
        _retTree = _t;
    }

    public final void iterative_statement_evaluator(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST iterative_statement_evaluator_AST = null;
        PtalonAST f = null;
        PtalonAST f_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST b = null;
        PtalonAST b_AST = null;
        PtalonAST c = null;
        PtalonAST c_AST = null;
        PtalonAST n = null;
        PtalonAST n_AST = null;

        AST __t91 = _t;
        f = _t == ASTNULL ? null : (PtalonAST) _t;
        f_AST = (PtalonAST) astFactory.create(f);
        astFactory.addASTChild(currentAST, f_AST);
        ASTPair __currentAST91 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, FOR);
        _t = _t.getFirstChild();
        AST __t92 = _t;
        PtalonAST tmp43_AST = null;
        tmp43_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp43_AST);
        ASTPair __currentAST92 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, VARIABLE);
        _t = _t.getFirstChild();
        a = (PtalonAST) _t;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        match(_t, ID);
        _t = _t.getNextSibling();
        currentAST = __currentAST92;
        _t = __t92;
        _t = _t.getNextSibling();
        AST __t93 = _t;
        PtalonAST tmp44_AST = null;
        tmp44_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp44_AST);
        ASTPair __currentAST93 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, INITIALLY);
        _t = _t.getFirstChild();
        b = (PtalonAST) _t;
        b_AST = (PtalonAST) astFactory.create(b);
        astFactory.addASTChild(currentAST, b_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST93;
        _t = __t93;
        _t = _t.getNextSibling();
        AST __t94 = _t;
        PtalonAST tmp45_AST = null;
        tmp45_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp45_AST);
        ASTPair __currentAST94 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, SATISFIES);
        _t = _t.getFirstChild();
        c = (PtalonAST) _t;
        c_AST = (PtalonAST) astFactory.create(c);
        astFactory.addASTChild(currentAST, c_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST94;
        _t = __t94;
        _t = _t.getNextSibling();
        {
            _loop96: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case PORT:
                case INPORT:
                case OUTPORT:
                case PARAMETER:
                case ACTOR:
                case RELATION:
                case TRANSPARENT:
                case NEGATE:
                case OPTIONAL:
                case REMOVE:
                case PRESERVE:
                case ACTOR_DECLARATION:
                case MULTIPORT:
                case MULTIINPORT:
                case MULTIOUTPORT:
                case PARAM_EQUALS:
                case ACTOR_EQUALS: {
                    atomic_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case IF: {
                    conditional_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case FOR: {
                    iterative_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                default: {
                    break _loop96;
                }
                }
            } while (true);
        }
        AST __t97 = _t;
        PtalonAST tmp46_AST = null;
        tmp46_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp46_AST);
        ASTPair __currentAST97 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, NEXT);
        _t = _t.getFirstChild();
        n = (PtalonAST) _t;
        n_AST = (PtalonAST) astFactory.create(n);
        astFactory.addASTChild(currentAST, n_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST97;
        _t = __t97;
        _t = _t.getNextSibling();
        currentAST = __currentAST91;
        _t = __t91;
        _t = _t.getNextSibling();
        iterative_statement_evaluator_AST = (PtalonAST) currentAST.root;
        returnAST = iterative_statement_evaluator_AST;
        _retTree = _t;
    }

    public final void transformation(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST transformation_AST = null;

        boolean incremental = false;

        AST __t99 = _t;
        PtalonAST tmp47_AST = null;
        tmp47_AST = (PtalonAST) astFactory.create(_t);
        astFactory.addASTChild(currentAST, tmp47_AST);
        ASTPair __currentAST99 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, TRANSFORMATION);
        _t = _t.getFirstChild();
        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case PLUS: {
                PtalonAST tmp48_AST = null;
                tmp48_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp48_AST);
                match(_t, PLUS);
                _t = _t.getNextSibling();
                incremental = true;
                break;
            }
            case 3:
            case PORT:
            case INPORT:
            case OUTPORT:
            case PARAMETER:
            case ACTOR:
            case RELATION:
            case TRANSPARENT:
            case IF:
            case FOR:
            case NEGATE:
            case OPTIONAL:
            case REMOVE:
            case PRESERVE:
            case ACTOR_DECLARATION:
            case MULTIPORT:
            case MULTIINPORT:
            case MULTIOUTPORT:
            case PARAM_EQUALS:
            case ACTOR_EQUALS: {
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }

        info.enterTransformation(incremental);

        {
            _loop102: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case PORT:
                case INPORT:
                case OUTPORT:
                case PARAMETER:
                case ACTOR:
                case RELATION:
                case TRANSPARENT:
                case NEGATE:
                case OPTIONAL:
                case REMOVE:
                case PRESERVE:
                case ACTOR_DECLARATION:
                case MULTIPORT:
                case MULTIINPORT:
                case MULTIOUTPORT:
                case PARAM_EQUALS:
                case ACTOR_EQUALS: {
                    atomic_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case IF: {
                    conditional_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case FOR: {
                    iterative_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                default: {
                    break _loop102;
                }
                }
            } while (true);
        }
        currentAST = __currentAST99;
        _t = __t99;
        _t = _t.getNextSibling();

        info.exitTransformation();

        transformation_AST = (PtalonAST) currentAST.root;
        returnAST = transformation_AST;
        _retTree = _t;
    }

    public final void actor_definition(AST _t, PtalonEvaluator info)
            throws RecognitionException, PtalonRuntimeException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST actor_definition_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;

        this.info = info;
        this.info.startAtTop();

        AST __t104 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST104 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, ACTOR_DEFINITION);
        _t = _t.getFirstChild();
        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case DANGLING_PORTS_OKAY: {
                PtalonAST tmp49_AST = null;
                tmp49_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp49_AST);
                match(_t, DANGLING_PORTS_OKAY);
                _t = _t.getNextSibling();
                break;
            }
            case 3:
            case PORT:
            case INPORT:
            case OUTPORT:
            case PARAMETER:
            case ACTOR:
            case RELATION:
            case TRANSPARENT:
            case IF:
            case FOR:
            case ATTACH_DANGLING_PORTS:
            case NEGATE:
            case OPTIONAL:
            case REMOVE:
            case PRESERVE:
            case ACTOR_DECLARATION:
            case TRANSFORMATION:
            case MULTIPORT:
            case MULTIINPORT:
            case MULTIOUTPORT:
            case PARAM_EQUALS:
            case ACTOR_EQUALS: {
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }
        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case ATTACH_DANGLING_PORTS: {
                PtalonAST tmp50_AST = null;
                tmp50_AST = (PtalonAST) astFactory.create(_t);
                astFactory.addASTChild(currentAST, tmp50_AST);
                match(_t, ATTACH_DANGLING_PORTS);
                _t = _t.getNextSibling();
                break;
            }
            case 3:
            case PORT:
            case INPORT:
            case OUTPORT:
            case PARAMETER:
            case ACTOR:
            case RELATION:
            case TRANSPARENT:
            case IF:
            case FOR:
            case NEGATE:
            case OPTIONAL:
            case REMOVE:
            case PRESERVE:
            case ACTOR_DECLARATION:
            case TRANSFORMATION:
            case MULTIPORT:
            case MULTIINPORT:
            case MULTIOUTPORT:
            case PARAM_EQUALS:
            case ACTOR_EQUALS: {
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }

        this.info.setActiveBranch(true);

        {
            _loop108: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                switch (_t.getType()) {
                case PORT:
                case INPORT:
                case OUTPORT:
                case PARAMETER:
                case ACTOR:
                case RELATION:
                case TRANSPARENT:
                case NEGATE:
                case OPTIONAL:
                case REMOVE:
                case PRESERVE:
                case ACTOR_DECLARATION:
                case MULTIPORT:
                case MULTIINPORT:
                case MULTIOUTPORT:
                case PARAM_EQUALS:
                case ACTOR_EQUALS: {
                    atomic_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case IF: {
                    conditional_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                case FOR: {
                    iterative_statement(_t);
                    _t = _retTree;
                    astFactory.addASTChild(currentAST, returnAST);
                    break;
                }
                default: {
                    break _loop108;
                }
                }
            } while (true);
        }
        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case TRANSFORMATION: {
                transformation(_t);
                _t = _retTree;
                astFactory.addASTChild(currentAST, returnAST);
                break;
            }
            case 3: {
                break;
            }
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }
        currentAST = __currentAST104;
        _t = __t104;
        _t = _t.getNextSibling();
        actor_definition_AST = (PtalonAST) currentAST.root;
        returnAST = actor_definition_AST;
        _retTree = _t;
    }

    public static final String[] _tokenNames = { "<0>", "EOF", "<2>",
            "NULL_TREE_LOOKAHEAD", "\"port\"", "LBRACKET", "RBRACKET",
            "\"inport\"", "\"outport\"", "ID", "\"parameter\"", "EQUALS",
            "\"actor\"", "\"actorparameter\"", "\"relation\"",
            "\"transparent\"", "\"reference\"", "COLON", "DOT", "\"import\"",
            "\"true\"", "\"false\"", "\"if\"", "\"else\"", "\"is\"", "\"for\"",
            "\"initially\"", "\"next\"", "\"danglingPortsOkay\"",
            "\"attachDanglingPorts\"", "ASSIGN", "RPAREN", "COMMA",
            "EXPRESSION", "LPAREN", "SEMI", "\"negate\"", "\"optional\"",
            "\"remove\"", "\"preserve\"", "LCURLY", "RCURLY", "TRANSFORM",
            "PLUS", "TRUEBRANCH", "FALSEBRANCH", "QUALID", "ATTRIBUTE",
            "ACTOR_DECLARATION", "ACTOR_DEFINITION", "TRANSFORMATION",
            "NEGATIVE_SIGN", "POSITIVE_SIGN", "ARITHMETIC_FACTOR",
            "BOOLEAN_FACTOR", "LOGICAL_BUFFER", "ARITHMETIC_EXPRESSION",
            "BOOLEAN_EXPRESSION", "MULTIPORT", "MULTIINPORT", "MULTIOUTPORT",
            "PARAM_EQUALS", "ACTOR_EQUALS", "SATISFIES", "VARIABLE",
            "DYNAMIC_NAME", "ACTOR_LABEL", "QUALIFIED_PORT", "ACTOR_ID", "ESC",
            "NUMBER_LITERAL", "STRING_LITERAL", "WHITE_SPACE", "LINE_COMMENT",
            "COMMENT" };

}
