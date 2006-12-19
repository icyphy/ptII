// $ANTLR : "populator.g" -> "PtalonPopulator.java"$
/* 

 Copyright (c) 2006 The Regents of the University of California.
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

public class PtalonPopulator extends antlr.TreeParser implements
        PtalonPopulatorTokenTypes {

    private NestedActorManager info;

    public NestedActorManager getCodeManager() {
        return info;
    }

    private String scopeName;

    public PtalonPopulator() {
        tokenNames = _tokenNames;
    }

    public final void port_declaration(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        PtalonAST port_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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
                AST __t431 = _t;
                PtalonAST tmp1_AST = null;
                PtalonAST tmp1_AST_in = null;
                tmp1_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp1_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp1_AST);
                ASTPair __currentAST431 = currentAST.copy();
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
                        PtalonAST a_AST_in = null;
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
                        AST __t433 = _t;
                        PtalonAST tmp2_AST = null;
                        PtalonAST tmp2_AST_in = null;
                        tmp2_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp2_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp2_AST);
                        ASTPair __currentAST433 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        g = (PtalonAST) _t;
                        PtalonAST g_AST_in = null;
                        g_AST = (PtalonAST) astFactory.create(g);
                        astFactory.addASTChild(currentAST, g_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        h = (PtalonAST) _t;
                        PtalonAST h_AST_in = null;
                        h_AST = (PtalonAST) astFactory.create(h);
                        astFactory.addASTChild(currentAST, h_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST433;
                        _t = __t433;
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
                currentAST = __currentAST431;
                _t = __t431;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case INPORT: {
                AST __t434 = _t;
                PtalonAST tmp3_AST = null;
                PtalonAST tmp3_AST_in = null;
                tmp3_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp3_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp3_AST);
                ASTPair __currentAST434 = currentAST.copy();
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
                        PtalonAST b_AST_in = null;
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
                        AST __t436 = _t;
                        PtalonAST tmp4_AST = null;
                        PtalonAST tmp4_AST_in = null;
                        tmp4_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp4_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp4_AST);
                        ASTPair __currentAST436 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        i = (PtalonAST) _t;
                        PtalonAST i_AST_in = null;
                        i_AST = (PtalonAST) astFactory.create(i);
                        astFactory.addASTChild(currentAST, i_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        j = (PtalonAST) _t;
                        PtalonAST j_AST_in = null;
                        j_AST = (PtalonAST) astFactory.create(j);
                        astFactory.addASTChild(currentAST, j_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST436;
                        _t = __t436;
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
                currentAST = __currentAST434;
                _t = __t434;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case OUTPORT: {
                AST __t437 = _t;
                PtalonAST tmp5_AST = null;
                PtalonAST tmp5_AST_in = null;
                tmp5_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp5_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp5_AST);
                ASTPair __currentAST437 = currentAST.copy();
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
                        PtalonAST c_AST_in = null;
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
                        AST __t439 = _t;
                        PtalonAST tmp6_AST = null;
                        PtalonAST tmp6_AST_in = null;
                        tmp6_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp6_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp6_AST);
                        ASTPair __currentAST439 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        k = (PtalonAST) _t;
                        PtalonAST k_AST_in = null;
                        k_AST = (PtalonAST) astFactory.create(k);
                        astFactory.addASTChild(currentAST, k_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        l = (PtalonAST) _t;
                        PtalonAST l_AST_in = null;
                        l_AST = (PtalonAST) astFactory.create(l);
                        astFactory.addASTChild(currentAST, l_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST439;
                        _t = __t439;
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
                currentAST = __currentAST437;
                _t = __t437;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case MULTIPORT: {
                AST __t440 = _t;
                PtalonAST tmp7_AST = null;
                PtalonAST tmp7_AST_in = null;
                tmp7_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp7_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp7_AST);
                ASTPair __currentAST440 = currentAST.copy();
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
                        PtalonAST d_AST_in = null;
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
                        AST __t442 = _t;
                        PtalonAST tmp8_AST = null;
                        PtalonAST tmp8_AST_in = null;
                        tmp8_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp8_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp8_AST);
                        ASTPair __currentAST442 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        m = (PtalonAST) _t;
                        PtalonAST m_AST_in = null;
                        m_AST = (PtalonAST) astFactory.create(m);
                        astFactory.addASTChild(currentAST, m_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        n = (PtalonAST) _t;
                        PtalonAST n_AST_in = null;
                        n_AST = (PtalonAST) astFactory.create(n);
                        astFactory.addASTChild(currentAST, n_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST442;
                        _t = __t442;
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
                currentAST = __currentAST440;
                _t = __t440;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case MULTIINPORT: {
                AST __t443 = _t;
                PtalonAST tmp9_AST = null;
                PtalonAST tmp9_AST_in = null;
                tmp9_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp9_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp9_AST);
                ASTPair __currentAST443 = currentAST.copy();
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
                        PtalonAST e_AST_in = null;
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
                        AST __t445 = _t;
                        PtalonAST tmp10_AST = null;
                        PtalonAST tmp10_AST_in = null;
                        tmp10_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp10_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp10_AST);
                        ASTPair __currentAST445 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        o = (PtalonAST) _t;
                        PtalonAST o_AST_in = null;
                        o_AST = (PtalonAST) astFactory.create(o);
                        astFactory.addASTChild(currentAST, o_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        p = (PtalonAST) _t;
                        PtalonAST p_AST_in = null;
                        p_AST = (PtalonAST) astFactory.create(p);
                        astFactory.addASTChild(currentAST, p_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST445;
                        _t = __t445;
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
                currentAST = __currentAST443;
                _t = __t443;
                _t = _t.getNextSibling();
                port_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case MULTIOUTPORT: {
                AST __t446 = _t;
                PtalonAST tmp11_AST = null;
                PtalonAST tmp11_AST_in = null;
                tmp11_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp11_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp11_AST);
                ASTPair __currentAST446 = currentAST.copy();
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
                        PtalonAST f_AST_in = null;
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
                        AST __t448 = _t;
                        PtalonAST tmp12_AST = null;
                        PtalonAST tmp12_AST_in = null;
                        tmp12_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp12_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp12_AST);
                        ASTPair __currentAST448 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        q = (PtalonAST) _t;
                        PtalonAST q_AST_in = null;
                        q_AST = (PtalonAST) astFactory.create(q);
                        astFactory.addASTChild(currentAST, q_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        r = (PtalonAST) _t;
                        PtalonAST r_AST_in = null;
                        r_AST = (PtalonAST) astFactory.create(r);
                        astFactory.addASTChild(currentAST, r_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST448;
                        _t = __t448;
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
                currentAST = __currentAST446;
                _t = __t446;
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

        PtalonAST parameter_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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
                AST __t450 = _t;
                PtalonAST tmp13_AST = null;
                PtalonAST tmp13_AST_in = null;
                tmp13_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp13_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp13_AST);
                ASTPair __currentAST450 = currentAST.copy();
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
                        PtalonAST a_AST_in = null;
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
                        AST __t452 = _t;
                        PtalonAST tmp14_AST = null;
                        PtalonAST tmp14_AST_in = null;
                        tmp14_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp14_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp14_AST);
                        ASTPair __currentAST452 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        c = (PtalonAST) _t;
                        PtalonAST c_AST_in = null;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        d = (PtalonAST) _t;
                        PtalonAST d_AST_in = null;
                        d_AST = (PtalonAST) astFactory.create(d);
                        astFactory.addASTChild(currentAST, d_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST452;
                        _t = __t452;
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
                currentAST = __currentAST450;
                _t = __t450;
                _t = _t.getNextSibling();
                parameter_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case ACTOR: {
                AST __t453 = _t;
                PtalonAST tmp15_AST = null;
                PtalonAST tmp15_AST_in = null;
                tmp15_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp15_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp15_AST);
                ASTPair __currentAST453 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR);
                _t = _t.getFirstChild();
                b = (PtalonAST) _t;
                PtalonAST b_AST_in = null;
                b_AST = (PtalonAST) astFactory.create(b);
                astFactory.addASTChild(currentAST, b_AST);
                match(_t, ID);
                _t = _t.getNextSibling();

                if (info.isReady() && !info.isCreated(b.getText())) {
                    info.addActorParameter(b.getText());
                }

                currentAST = __currentAST453;
                _t = __t453;
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

        PtalonAST assigned_parameter_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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
        PtalonAST q_AST = null;
        PtalonAST q = null;

        boolean dynamic_name = false;

        try { // for error handling
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case PARAM_EQUALS: {
                AST __t455 = _t;
                PtalonAST tmp16_AST = null;
                PtalonAST tmp16_AST_in = null;
                tmp16_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp16_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp16_AST);
                ASTPair __currentAST455 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, PARAM_EQUALS);
                _t = _t.getFirstChild();
                AST __t456 = _t;
                PtalonAST tmp17_AST = null;
                PtalonAST tmp17_AST_in = null;
                tmp17_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp17_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp17_AST);
                ASTPair __currentAST456 = currentAST.copy();
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
                        PtalonAST a_AST_in = null;
                        a_AST = (PtalonAST) astFactory.create(a);
                        astFactory.addASTChild(currentAST, a_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        break;
                    }
                    case DYNAMIC_NAME: {
                        AST __t458 = _t;
                        PtalonAST tmp18_AST = null;
                        PtalonAST tmp18_AST_in = null;
                        tmp18_AST = (PtalonAST) astFactory
                                .create((PtalonAST) _t);
                        tmp18_AST_in = (PtalonAST) _t;
                        astFactory.addASTChild(currentAST, tmp18_AST);
                        ASTPair __currentAST458 = currentAST.copy();
                        currentAST.root = currentAST.child;
                        currentAST.child = null;
                        match(_t, DYNAMIC_NAME);
                        _t = _t.getFirstChild();
                        c = (PtalonAST) _t;
                        PtalonAST c_AST_in = null;
                        c_AST = (PtalonAST) astFactory.create(c);
                        astFactory.addASTChild(currentAST, c_AST);
                        match(_t, ID);
                        _t = _t.getNextSibling();
                        d = (PtalonAST) _t;
                        PtalonAST d_AST_in = null;
                        d_AST = (PtalonAST) astFactory.create(d);
                        astFactory.addASTChild(currentAST, d_AST);
                        match(_t, EXPRESSION);
                        _t = _t.getNextSibling();
                        currentAST = __currentAST458;
                        _t = __t458;
                        _t = _t.getNextSibling();

                        dynamic_name = true;

                        break;
                    }
                    default: {
                        throw new NoViableAltException(_t);
                    }
                    }
                }
                currentAST = __currentAST456;
                _t = __t456;
                _t = _t.getNextSibling();
                e = (PtalonAST) _t;
                PtalonAST e_AST_in = null;
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

                currentAST = __currentAST455;
                _t = __t455;
                _t = _t.getNextSibling();
                assigned_parameter_declaration_AST = (PtalonAST) currentAST.root;
                break;
            }
            case ACTOR_EQUALS: {
                AST __t459 = _t;
                PtalonAST tmp19_AST = null;
                PtalonAST tmp19_AST_in = null;
                tmp19_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp19_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp19_AST);
                ASTPair __currentAST459 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR_EQUALS);
                _t = _t.getFirstChild();
                AST __t460 = _t;
                PtalonAST tmp20_AST = null;
                PtalonAST tmp20_AST_in = null;
                tmp20_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp20_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp20_AST);
                ASTPair __currentAST460 = currentAST.copy();
                currentAST.root = currentAST.child;
                currentAST.child = null;
                match(_t, ACTOR);
                _t = _t.getFirstChild();
                b = (PtalonAST) _t;
                PtalonAST b_AST_in = null;
                b_AST = (PtalonAST) astFactory.create(b);
                astFactory.addASTChild(currentAST, b_AST);
                match(_t, ID);
                _t = _t.getNextSibling();
                currentAST = __currentAST460;
                _t = __t460;
                _t = _t.getNextSibling();
                q = _t == ASTNULL ? null : (PtalonAST) _t;
                qualified_identifier(_t);
                _t = _retTree;
                q_AST = (PtalonAST) returnAST;
                astFactory.addASTChild(currentAST, returnAST);

                if (info.isReady() && !info.isCreated(b.getText())) {
                    info.addActorParameter(b.getText(), q.getText());
                }

                currentAST = __currentAST459;
                _t = __t459;
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

        PtalonAST qualified_identifier_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST qualified_identifier_AST = null;

        PtalonAST tmp21_AST = null;
        PtalonAST tmp21_AST_in = null;
        tmp21_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp21_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp21_AST);
        match(_t, QUALID);
        _t = _t.getNextSibling();
        qualified_identifier_AST = (PtalonAST) currentAST.root;
        returnAST = qualified_identifier_AST;
        _retTree = _t;
    }

    public final void relation_declaration(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        PtalonAST relation_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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
            AST __t462 = _t;
            PtalonAST tmp22_AST = null;
            PtalonAST tmp22_AST_in = null;
            tmp22_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
            tmp22_AST_in = (PtalonAST) _t;
            astFactory.addASTChild(currentAST, tmp22_AST);
            ASTPair __currentAST462 = currentAST.copy();
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
                    PtalonAST a_AST_in = null;
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
                    AST __t464 = _t;
                    PtalonAST tmp23_AST = null;
                    PtalonAST tmp23_AST_in = null;
                    tmp23_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                    tmp23_AST_in = (PtalonAST) _t;
                    astFactory.addASTChild(currentAST, tmp23_AST);
                    ASTPair __currentAST464 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    c = (PtalonAST) _t;
                    PtalonAST c_AST_in = null;
                    c_AST = (PtalonAST) astFactory.create(c);
                    astFactory.addASTChild(currentAST, c_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    d = (PtalonAST) _t;
                    PtalonAST d_AST_in = null;
                    d_AST = (PtalonAST) astFactory.create(d);
                    astFactory.addASTChild(currentAST, d_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();
                    currentAST = __currentAST464;
                    _t = __t464;
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
            currentAST = __currentAST462;
            _t = __t462;
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

        PtalonAST transparent_relation_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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
            AST __t466 = _t;
            PtalonAST tmp24_AST = null;
            PtalonAST tmp24_AST_in = null;
            tmp24_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
            tmp24_AST_in = (PtalonAST) _t;
            astFactory.addASTChild(currentAST, tmp24_AST);
            ASTPair __currentAST466 = currentAST.copy();
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
                    PtalonAST a_AST_in = null;
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
                    AST __t468 = _t;
                    PtalonAST tmp25_AST = null;
                    PtalonAST tmp25_AST_in = null;
                    tmp25_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                    tmp25_AST_in = (PtalonAST) _t;
                    astFactory.addASTChild(currentAST, tmp25_AST);
                    ASTPair __currentAST468 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    c = (PtalonAST) _t;
                    PtalonAST c_AST_in = null;
                    c_AST = (PtalonAST) astFactory.create(c);
                    astFactory.addASTChild(currentAST, c_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    d = (PtalonAST) _t;
                    PtalonAST d_AST_in = null;
                    d_AST = (PtalonAST) astFactory.create(d);
                    astFactory.addASTChild(currentAST, d_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();
                    currentAST = __currentAST468;
                    _t = __t468;
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
            currentAST = __currentAST466;
            _t = __t466;
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

        PtalonAST assignment_AST_in = (_t == ASTNULL) ? null : (PtalonAST) _t;
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
            AST __t471 = _t;
            PtalonAST tmp26_AST = null;
            PtalonAST tmp26_AST_in = null;
            tmp26_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
            tmp26_AST_in = (PtalonAST) _t;
            astFactory.addASTChild(currentAST, tmp26_AST);
            ASTPair __currentAST471 = currentAST.copy();
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
                    PtalonAST tmp27_AST_in = null;
                    tmp27_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                    tmp27_AST_in = (PtalonAST) _t;
                    astFactory.addASTChild(currentAST, tmp27_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    break;
                }
                case DYNAMIC_NAME: {
                    AST __t473 = _t;
                    PtalonAST tmp28_AST = null;
                    PtalonAST tmp28_AST_in = null;
                    tmp28_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                    tmp28_AST_in = (PtalonAST) _t;
                    astFactory.addASTChild(currentAST, tmp28_AST);
                    ASTPair __currentAST473 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    left = (PtalonAST) _t;
                    PtalonAST left_AST_in = null;
                    left_AST = (PtalonAST) astFactory.create(left);
                    astFactory.addASTChild(currentAST, left_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    leftExp = (PtalonAST) _t;
                    PtalonAST leftExp_AST_in = null;
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

                    currentAST = __currentAST473;
                    _t = __t473;
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
                    PtalonAST b_AST_in = null;
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
                    AST __t475 = _t;
                    d = _t == ASTNULL ? null : (PtalonAST) _t;
                    PtalonAST d_AST_in = null;
                    d_AST = (PtalonAST) astFactory.create(d);
                    astFactory.addASTChild(currentAST, d_AST);
                    ASTPair __currentAST475 = currentAST.copy();
                    currentAST.root = currentAST.child;
                    currentAST.child = null;
                    match(_t, DYNAMIC_NAME);
                    _t = _t.getFirstChild();
                    i = (PtalonAST) _t;
                    PtalonAST i_AST_in = null;
                    i_AST = (PtalonAST) astFactory.create(i);
                    astFactory.addASTChild(currentAST, i_AST);
                    match(_t, ID);
                    _t = _t.getNextSibling();
                    e = (PtalonAST) _t;
                    PtalonAST e_AST_in = null;
                    e_AST = (PtalonAST) astFactory.create(e);
                    astFactory.addASTChild(currentAST, e_AST);
                    match(_t, EXPRESSION);
                    _t = _t.getNextSibling();

                    if (addAssignment) {
                        info.addPortAssign(name, i.getText(), e.getText());
                    }

                    currentAST = __currentAST475;
                    _t = __t475;
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
                    PtalonAST p_AST_in = null;
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
            currentAST = __currentAST471;
            _t = __t471;
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

        PtalonAST nested_actor_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST nested_actor_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST b_AST = null;
        PtalonAST b = null;

        AST __t481 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        PtalonAST a_AST_in = null;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST481 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, ACTOR_DECLARATION);
        _t = _t.getFirstChild();

        info.enterActorDeclaration(a.getText());

        {
            _loop483: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                if ((_t.getType() == ASSIGN)) {
                    b = _t == ASTNULL ? null : (PtalonAST) _t;
                    assignment(_t);
                    _t = _retTree;
                    b_AST = (PtalonAST) returnAST;
                    astFactory.addASTChild(currentAST, returnAST);
                } else {
                    break _loop483;
                }

            } while (true);
        }

        info.exitActorDeclaration();

        currentAST = __currentAST481;
        _t = __t481;
        _t = _t.getNextSibling();
        nested_actor_declaration_AST = (PtalonAST) currentAST.root;
        returnAST = nested_actor_declaration_AST;
        _retTree = _t;
    }

    /**
     * This is for a top level actor declaration, which 
     * requires seperate treatement from a nested actor
     * declaration.
     */
    public final void actor_declaration(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        PtalonAST actor_declaration_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST actor_declaration_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST b_AST = null;
        PtalonAST b = null;

        boolean oldEvalBool = false;

        AST __t477 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        PtalonAST a_AST_in = null;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST477 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, ACTOR_DECLARATION);
        _t = _t.getFirstChild();

        info.enterActorDeclaration(a.getText());

        {
            _loop479: do {
                if (_t == null) {
                    _t = ASTNULL;
                }
                if ((_t.getType() == ASSIGN)) {
                    b = _t == ASTNULL ? null : (PtalonAST) _t;
                    assignment(_t);
                    _t = _retTree;
                    b_AST = (PtalonAST) returnAST;
                    astFactory.addASTChild(currentAST, returnAST);
                } else {
                    break _loop479;
                }

            } while (true);
        }

        if (info.isActorReady()) {
            info.addActor(a.getText());
        }
        info.exitActorDeclaration();

        currentAST = __currentAST477;
        _t = __t477;
        _t = _t.getNextSibling();
        actor_declaration_AST = (PtalonAST) currentAST.root;
        returnAST = actor_declaration_AST;
        _retTree = _t;
    }

    public final void atomic_statement(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        PtalonAST atomic_statement_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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
            default: {
                throw new NoViableAltException(_t);
            }
            }
        }
        atomic_statement_AST = (PtalonAST) currentAST.root;
        returnAST = atomic_statement_AST;
        _retTree = _t;
    }

    public final void conditional_statement(AST _t)
            throws RecognitionException, PtalonRuntimeException {

        PtalonAST conditional_statement_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST conditional_statement_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;
        PtalonAST e = null;
        PtalonAST e_AST = null;

        boolean ready;

        AST __t487 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        PtalonAST a_AST_in = null;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST487 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, IF);
        _t = _t.getFirstChild();

        info.enterIfScope(a.getText());
        ready = info.isIfReady();

        e = (PtalonAST) _t;
        PtalonAST e_AST_in = null;
        e_AST = (PtalonAST) astFactory.create(e);
        astFactory.addASTChild(currentAST, e_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();

        if (ready) {
            info.setActiveBranch(info.evaluateBoolean(e.getText()));
        }

        AST __t488 = _t;
        PtalonAST tmp29_AST = null;
        PtalonAST tmp29_AST_in = null;
        tmp29_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp29_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp29_AST);
        ASTPair __currentAST488 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, TRUEBRANCH);
        _t = _t.getFirstChild();

        if (ready) {
            info.setCurrentBranch(true);
        }

        {
            _loop490: do {
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
                    break _loop490;
                }
                }
            } while (true);
        }
        currentAST = __currentAST488;
        _t = __t488;
        _t = _t.getNextSibling();
        AST __t491 = _t;
        PtalonAST tmp30_AST = null;
        PtalonAST tmp30_AST_in = null;
        tmp30_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp30_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp30_AST);
        ASTPair __currentAST491 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, FALSEBRANCH);
        _t = _t.getFirstChild();

        if (ready) {
            info.setCurrentBranch(false);
        }

        {
            _loop493: do {
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
                    break _loop493;
                }
                }
            } while (true);
        }
        currentAST = __currentAST491;
        _t = __t491;
        _t = _t.getNextSibling();
        currentAST = __currentAST487;
        _t = __t487;
        _t = _t.getNextSibling();

        info.exitIfScope();

        conditional_statement_AST = (PtalonAST) currentAST.root;
        returnAST = conditional_statement_AST;
        _retTree = _t;
    }

    public final void iterative_statement(AST _t) throws RecognitionException,
            PtalonRuntimeException {

        PtalonAST iterative_statement_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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

        AST __t495 = _t;
        f = _t == ASTNULL ? null : (PtalonAST) _t;
        PtalonAST f_AST_in = null;
        f_AST = (PtalonAST) astFactory.create(f);
        astFactory.addASTChild(currentAST, f_AST);
        ASTPair __currentAST495 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, FOR);
        _t = _t.getFirstChild();
        AST __t496 = _t;
        PtalonAST tmp31_AST = null;
        PtalonAST tmp31_AST_in = null;
        tmp31_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp31_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp31_AST);
        ASTPair __currentAST496 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, VARIABLE);
        _t = _t.getFirstChild();
        a = (PtalonAST) _t;
        PtalonAST a_AST_in = null;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        match(_t, ID);
        _t = _t.getNextSibling();
        currentAST = __currentAST496;
        _t = __t496;
        _t = _t.getNextSibling();
        AST __t497 = _t;
        PtalonAST tmp32_AST = null;
        PtalonAST tmp32_AST_in = null;
        tmp32_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp32_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp32_AST);
        ASTPair __currentAST497 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, INITIALLY);
        _t = _t.getFirstChild();
        b = (PtalonAST) _t;
        PtalonAST b_AST_in = null;
        b_AST = (PtalonAST) astFactory.create(b);
        astFactory.addASTChild(currentAST, b_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST497;
        _t = __t497;
        _t = _t.getNextSibling();
        AST __t498 = _t;
        PtalonAST tmp33_AST = null;
        PtalonAST tmp33_AST_in = null;
        tmp33_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp33_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp33_AST);
        ASTPair __currentAST498 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, SATISFIES);
        _t = _t.getFirstChild();
        c = (PtalonAST) _t;
        PtalonAST c_AST_in = null;
        c_AST = (PtalonAST) astFactory.create(c);
        astFactory.addASTChild(currentAST, c_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST498;
        _t = __t498;
        _t = _t.getNextSibling();

        info.enterForScope(f.getText(), inputAST, this);
        ready = info.isForReady();
        if (ready) {
            info.setActiveBranch(true);
            info.setCurrentBranch(false);
        }

        {
            _loop500: do {
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
                    break _loop500;
                }
                }
            } while (true);
        }
        AST __t501 = _t;
        PtalonAST tmp34_AST = null;
        PtalonAST tmp34_AST_in = null;
        tmp34_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp34_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp34_AST);
        ASTPair __currentAST501 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, NEXT);
        _t = _t.getFirstChild();
        n = (PtalonAST) _t;
        PtalonAST n_AST_in = null;
        n_AST = (PtalonAST) astFactory.create(n);
        astFactory.addASTChild(currentAST, n_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST501;
        _t = __t501;
        _t = _t.getNextSibling();
        currentAST = __currentAST495;
        _t = __t495;
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

        PtalonAST iterative_statement_evaluator_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
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

        AST __t503 = _t;
        f = _t == ASTNULL ? null : (PtalonAST) _t;
        PtalonAST f_AST_in = null;
        f_AST = (PtalonAST) astFactory.create(f);
        astFactory.addASTChild(currentAST, f_AST);
        ASTPair __currentAST503 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, FOR);
        _t = _t.getFirstChild();
        AST __t504 = _t;
        PtalonAST tmp35_AST = null;
        PtalonAST tmp35_AST_in = null;
        tmp35_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp35_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp35_AST);
        ASTPair __currentAST504 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, VARIABLE);
        _t = _t.getFirstChild();
        a = (PtalonAST) _t;
        PtalonAST a_AST_in = null;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        match(_t, ID);
        _t = _t.getNextSibling();
        currentAST = __currentAST504;
        _t = __t504;
        _t = _t.getNextSibling();
        AST __t505 = _t;
        PtalonAST tmp36_AST = null;
        PtalonAST tmp36_AST_in = null;
        tmp36_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp36_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp36_AST);
        ASTPair __currentAST505 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, INITIALLY);
        _t = _t.getFirstChild();
        b = (PtalonAST) _t;
        PtalonAST b_AST_in = null;
        b_AST = (PtalonAST) astFactory.create(b);
        astFactory.addASTChild(currentAST, b_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST505;
        _t = __t505;
        _t = _t.getNextSibling();
        AST __t506 = _t;
        PtalonAST tmp37_AST = null;
        PtalonAST tmp37_AST_in = null;
        tmp37_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp37_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp37_AST);
        ASTPair __currentAST506 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, SATISFIES);
        _t = _t.getFirstChild();
        c = (PtalonAST) _t;
        PtalonAST c_AST_in = null;
        c_AST = (PtalonAST) astFactory.create(c);
        astFactory.addASTChild(currentAST, c_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST506;
        _t = __t506;
        _t = _t.getNextSibling();
        {
            _loop508: do {
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
                    break _loop508;
                }
                }
            } while (true);
        }
        AST __t509 = _t;
        PtalonAST tmp38_AST = null;
        PtalonAST tmp38_AST_in = null;
        tmp38_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
        tmp38_AST_in = (PtalonAST) _t;
        astFactory.addASTChild(currentAST, tmp38_AST);
        ASTPair __currentAST509 = currentAST.copy();
        currentAST.root = currentAST.child;
        currentAST.child = null;
        match(_t, NEXT);
        _t = _t.getFirstChild();
        n = (PtalonAST) _t;
        PtalonAST n_AST_in = null;
        n_AST = (PtalonAST) astFactory.create(n);
        astFactory.addASTChild(currentAST, n_AST);
        match(_t, EXPRESSION);
        _t = _t.getNextSibling();
        currentAST = __currentAST509;
        _t = __t509;
        _t = _t.getNextSibling();
        currentAST = __currentAST503;
        _t = __t503;
        _t = _t.getNextSibling();
        iterative_statement_evaluator_AST = (PtalonAST) currentAST.root;
        returnAST = iterative_statement_evaluator_AST;
        _retTree = _t;
    }

    public final void actor_definition(AST _t, NestedActorManager info)
            throws RecognitionException, PtalonRuntimeException {

        PtalonAST actor_definition_AST_in = (_t == ASTNULL) ? null
                : (PtalonAST) _t;
        returnAST = null;
        ASTPair currentAST = new ASTPair();
        PtalonAST actor_definition_AST = null;
        PtalonAST a = null;
        PtalonAST a_AST = null;

        this.info = info;
        this.info.startAtTop();

        AST __t511 = _t;
        a = _t == ASTNULL ? null : (PtalonAST) _t;
        PtalonAST a_AST_in = null;
        a_AST = (PtalonAST) astFactory.create(a);
        astFactory.addASTChild(currentAST, a_AST);
        ASTPair __currentAST511 = currentAST.copy();
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
                PtalonAST tmp39_AST = null;
                PtalonAST tmp39_AST_in = null;
                tmp39_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp39_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp39_AST);
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
        {
            if (_t == null) {
                _t = ASTNULL;
            }
            switch (_t.getType()) {
            case ATTACH_DANGLING_PORTS: {
                PtalonAST tmp40_AST = null;
                PtalonAST tmp40_AST_in = null;
                tmp40_AST = (PtalonAST) astFactory.create((PtalonAST) _t);
                tmp40_AST_in = (PtalonAST) _t;
                astFactory.addASTChild(currentAST, tmp40_AST);
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

        this.info.setActiveBranch(true);

        {
            _loop515: do {
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
                    break _loop515;
                }
                }
            } while (true);
        }
        currentAST = __currentAST511;
        _t = __t511;
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
            "EXPRESSION", "LPAREN", "SEMI", "COMMENT", "LCURLY", "RCURLY",
            "TRUEBRANCH", "FALSEBRANCH", "QUALID", "ATTRIBUTE",
            "ACTOR_DECLARATION", "ACTOR_DEFINITION", "NEGATIVE_SIGN",
            "POSITIVE_SIGN", "ARITHMETIC_FACTOR", "BOOLEAN_FACTOR",
            "LOGICAL_BUFFER", "ARITHMETIC_EXPRESSION", "BOOLEAN_EXPRESSION",
            "MULTIPORT", "MULTIINPORT", "MULTIOUTPORT", "PARAM_EQUALS",
            "ACTOR_EQUALS", "SATISFIES", "VARIABLE", "DYNAMIC_NAME",
            "ACTOR_LABEL", "QUALIFIED_PORT", "ESC", "NUMBER_LITERAL",
            "ATTRIBUTE_MARKER", "STRING_LITERAL", "WHITE_SPACE" };

}
