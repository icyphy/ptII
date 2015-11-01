/*
 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.caltrop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.caltrop.util.IntegerList;
import ptolemy.caltrop.util.PtArrayList;
import ptolemy.caltrop.util.PtCalFunction;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import caltrop.interpreter.Context;
import caltrop.interpreter.Function;
import caltrop.interpreter.InterpreterException;
import caltrop.interpreter.Procedure;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.environment.HashEnvironment;
import caltrop.interpreter.java.ClassObject;
import caltrop.interpreter.java.MethodObject;
import caltrop.interpreter.util.Platform;

//////////////////////////////////////////////////////////////////////////
//// PtolemyPlatform

/**
 The PtolemyPlatform class contains code that configures the CAL
 interpreter infrastructure for use inside the Ptolemy II software. In
 particular, it contains a context and a method that creates the global
 environment to be used with Ptolemy.

 @author J&#246;rn W. Janneck, Christopher Chang, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public class PtolemyPlatform implements Platform {
    @Override
    public Context context() {
        return _theContext;
    }

    @Override
    public Environment createGlobalEnvironment() {
        return createGlobalEnvironment(null);
    }

    @Override
    public Environment createGlobalEnvironment(Environment parent) {
        Environment env = new HashEnvironment(parent, context());

        env.bind("println", _theContext.createProcedure(new Procedure() {
            @Override
            public void call(Object[] args) {
                System.out.println(args[0]);
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("SOP", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    System.out.println(args[0]);
                    return args[0];
                } catch (Exception ex) {
                    throw new FunctionCallException("SOP", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("logValue", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    PrintStream output = new PrintStream(new FileOutputStream(
                            _theContext.stringValue(args[0]), true));
                    output.println(args[1].toString());
                    output.close();
                    return args[1];
                } catch (Exception ex) {
                    throw new FunctionCallException("logValue", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("Integers", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    int a = _theContext.intValue(args[0]);
                    int b = _theContext.intValue(args[1]);
                    List res = b < a ? Collections.EMPTY_LIST
                            : new IntegerList(_theContext, a, b);
                    return _theContext.createList(res);
                } catch (Exception ex) {
                    throw new FunctionCallException("Integers", args[0],
                            args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$not", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    BooleanToken b = (BooleanToken) args[0];
                    return b.not();
                } catch (Exception ex) {
                    throw new FunctionCallException("$not", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("$and", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    BooleanToken a = (BooleanToken) args[0];
                    BooleanToken b = (BooleanToken) args[1];
                    return a.and(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$and", args[0], args[1],
                            ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$or", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    BooleanToken a = (BooleanToken) args[0];
                    BooleanToken b = (BooleanToken) args[1];
                    return a.or(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$or", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$eq", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.isEqualTo(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$eq", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$ne", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.isEqualTo(b).not();
                } catch (Exception ex) {
                    throw new FunctionCallException("$ne", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$lt", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    return a.isLessThan(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$lt", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$le", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    return a.isGreaterThan(b).not();
                } catch (Exception ex) {
                    throw new FunctionCallException("$le", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$gt", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    return a.isGreaterThan(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$gt", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$ge", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];
                    ScalarToken b = (ScalarToken) args[1];
                    return a.isLessThan(b).not();
                } catch (Exception ex) {
                    throw new FunctionCallException("$ge", args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$negate", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    ScalarToken a = (ScalarToken) args[0];

                    return a.zero().subtract(a);
                } catch (Exception ex) {
                    throw new FunctionCallException("$negate", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("$add", _theContext.createFunction(new Function() {
            // Compute the add operation on scalar arguments, the
            // list concatenation operation on lists, or the set
            // union on sets.
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];

                    if (a instanceof ObjectToken && b instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        Object ob = ((ObjectToken) b).getValue();

                        if (oa instanceof Collection
                                && ob instanceof Collection) {
                            Collection result;

                            if (oa instanceof Set) {
                                result = new HashSet((Set) oa);
                            } else if (oa instanceof List) {
                                result = new ArrayList((List) oa);
                            } else {
                                throw new Exception(
                                        "Unknown object type: expected Set or List.");
                            }

                            result.addAll((Collection) ob);
                            return new ObjectToken(result);
                        } else {
                            throw new Exception(
                                    "Unknown object types: expected Collection.");
                        }
                    } else {
                        return a.add(b);
                    }
                } catch (Exception ex) {
                    throw new FunctionCallException("$add", args[0], args[1],
                            ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$mul", _theContext.createFunction(new Function() {
            // Compute the multiply operation on scalar arguments,
            // or the set intersection operation on sets.
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];

                    if (a instanceof ObjectToken && b instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        Object ob = ((ObjectToken) b).getValue();

                        if (oa instanceof Set && ob instanceof Collection) {
                            Set result = new HashSet((Set) oa);
                            result.retainAll((Collection) ob);
                            return new ObjectToken(result);
                        } else {
                            throw new InterpreterException(
                                    "Unknown object types: expected Set and Collection.");
                        }
                    } else {
                        return a.multiply(b);
                    }
                } catch (Exception ex) {
                    throw new FunctionCallException("$mul", args[0], args[1],
                            ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$sub", _theContext.createFunction(new Function() {
            // Compute the subtraction operation on scalar arguments,
            // or the set subtraction operation on sets.
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];

                    if (a instanceof ObjectToken && b instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();
                        Object ob = ((ObjectToken) b).getValue();

                        if (oa instanceof Collection
                                && ob instanceof Collection) {
                            Collection result;

                            if (oa instanceof Set) {
                                result = new HashSet((Set) oa);
                            } else if (oa instanceof List) {
                                result = new ArrayList((List) oa);
                            } else {
                                throw new Exception(
                                        "Unknown object type: expected Set or List.");
                            }

                            result.removeAll((Collection) ob);
                            return new ObjectToken(result);
                        } else {
                            throw new InterpreterException(
                                    "Unknown object types: expected Collection.");
                        }
                    } else {
                        return a.subtract(b);
                    }
                } catch (Exception ex) {
                    throw new FunctionCallException("$sub", args[0], args[1],
                            ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$div", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.divide(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$div", args[0], args[1],
                            ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$mod", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];
                    Token b = (Token) args[1];
                    return a.modulo(b);
                } catch (Exception ex) {
                    throw new FunctionCallException("$mod", args[0], args[1],
                            ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$size", _theContext.createFunction(new Function() {
            // Compute the number of elements in the given set,
            // list, or array.
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];

                    if (a instanceof ObjectToken) {
                        Object oa = ((ObjectToken) a).getValue();

                        if (oa instanceof Collection) {
                            return new IntToken(((Collection) oa).size());
                        } else {
                            throw new InterpreterException(
                                    "Unknown object type: expected Collection.");
                        }
                    } else if (a instanceof ArrayToken) {
                        return _theContext.createInteger(((ArrayToken) a)
                                .length());
                    } else {
                        throw new InterpreterException(
                                "Unknown type: expected Array, Set, or List");
                    }
                } catch (Exception ex) {
                    throw new FunctionCallException("$size", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("$createList", _theContext.createFunction(new Function() {
            // Create a list that contains the results of applying
            // the second argument (a one argument function) to
            // every element in the first argument (a collection).
            @Override
            public Object apply(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    FunctionToken f = (FunctionToken) args[1];
                    Object[] argument = new Object[1];
                    List res = new ArrayList();

                    for (Iterator i = c.iterator(); i.hasNext();) {
                        argument[0] = i.next();

                        Object listFragment = _theContext.applyFunction(f,
                                argument);
                        res.addAll(_theContext.getCollection(listFragment));
                    }

                    return _theContext.createList(res);
                } catch (Exception ex) {
                    throw new FunctionCallException("Failed to create list.",
                            args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$createSet", _theContext.createFunction(new Function() {
            // Create a set that contains the results of applying
            // the second argument (a one argument function) to
            // every element in the first argument (a collection).
            @Override
            public Object apply(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    FunctionToken f = (FunctionToken) args[1];
                    Object[] argument = new Object[1];
                    Set res = new HashSet();

                    for (Iterator i = c.iterator(); i.hasNext();) {
                        argument[0] = i.next();

                        Object setFragment = _theContext.applyFunction(f,
                                argument);
                        res.addAll(_theContext.getCollection(setFragment));
                    }

                    return _theContext.createSet(res);
                } catch (Exception ex) {
                    throw new FunctionCallException("Failed to create set.",
                            args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$createMap", _theContext.createFunction(new Function() {
            // Create a map that contains the results of applying
            // the second argument (a one argument function) to
            // every element in the first argument (a collection).
            @Override
            public Object apply(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    FunctionToken f = (FunctionToken) args[1];
                    Object[] argument = new Object[1];
                    Map res = new HashMap();

                    for (Iterator i = c.iterator(); i.hasNext();) {
                        argument[0] = i.next();

                        Object mapFragment = _theContext.applyFunction(f,
                                argument);
                        res.putAll(_theContext.getMap(mapFragment));
                    }

                    return _theContext.createMap(res);
                } catch (Exception ex) {
                    throw new FunctionCallException("Failed to create map.",
                            args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("$iterate", _theContext.createProcedure(new Procedure() {
            // Invoke the second argument (a one argument
            // procedure) on every element of the first argument
            // (a collection).
            @Override
            public void call(Object[] args) {
                try {
                    Collection c = _theContext.getCollection(args[0]);
                    Object proc = args[1];
                    Object[] argument = new Object[1];

                    for (Iterator i = c.iterator(); i.hasNext();) {
                        argument[0] = i.next();
                        _theContext.callProcedure(proc, argument);
                    }
                } catch (Exception ex) {
                    throw new FunctionCallException("Iteration failed.",
                            args[0], args[1], ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("listToArray", _theContext.createFunction(new Function() {
            // Convert the given list to an array.
            @Override
            public Object apply(Object[] args) {
                try {
                    ObjectToken input = (ObjectToken) args[0];
                    List inputList = (List) input.getValue();
                    Token[] tokens = new Token[inputList.size()];
                    tokens = (Token[]) inputList.toArray(tokens);
                    return new ArrayToken(tokens);
                } catch (Exception ex) {
                    throw new FunctionCallException("listToArray", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("listToMatrix", _theContext.createFunction(new Function() {
            // Convert the given list to an array.
            @Override
            public Object apply(Object[] args) {
                try {
                    ObjectToken input = (ObjectToken) args[0];
                    List inputList = (List) input.getValue();
                    Token[] tokens = new Token[inputList.size()];
                    tokens = (Token[]) inputList.toArray(tokens);

                    int rows = _theContext.intValue(args[1]);
                    int columns = _theContext.intValue(args[2]);
                    return MatrixToken.arrayToMatrix(tokens, rows, columns);
                } catch (Exception ex) {
                    throw new FunctionCallException("listToArray", args[0], ex);
                }
            }

            @Override
            public int arity() {
                return 3;
            }
        }));

        //
        // Xilinx SystemBuilder
        //
        //        constant YSCALE:   INT19 := conv_signed( integer( 1.164 * 256), 19 );
        //        constant RSCALE:   INT19 := conv_signed( integer( 1.596 * 256), 19 );
        //        constant GUSCALE:  INT19 := conv_signed( integer(-0.392 * 256), 19 );
        //        constant GVSCALE:  INT19 := conv_signed( integer(-0.813 * 256), 19 );
        //        constant BSCALE:   INT19 := conv_signed( integer( 2.017 * 256), 19 );
        //        constant YOFFSET:  INT19 := conv_signed(                    16, 19 );
        //        constant UVOFFSET: INT19 := conv_signed(                   128, 19 );
        //
        //        constant UINT9_zero: UINT9 := (others => '0' );
        //
        //        function INT19_mul( a: INT19; b: INT19 ) return INT19;
        //        function RGBCLIP( a: INT19 ) return UINT8;
        env.bind("UINT9_zero", _theContext.createInteger(0));
        env.bind("YSCALE", _theContext.createInteger((int) (1.164 * 256)));
        env.bind("RSCALE", _theContext.createInteger((int) (1.596 * 256)));
        env.bind("GUSCALE", _theContext.createInteger((int) (-0.392 * 256)));
        env.bind("GVSCALE", _theContext.createInteger((int) (-0.813 * 256)));
        env.bind("BSCALE", _theContext.createInteger((int) (2.017 * 256)));

        env.bind("YOFFSET", _theContext.createInteger(16));
        env.bind("UVOFFSET", _theContext.createInteger(128));

        env.bind("INT19_mul", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    IntToken a = (IntToken) args[0];
                    IntToken b = (IntToken) args[1];
                    int res = a.intValue() * b.intValue(); // & 0x7ffff;
                    return _theContext.createInteger(res);
                } catch (Exception ex) {
                    throw new InterpreterException(
                            "Function 'RGBCLIP': Cannot apply.", ex);
                }
            }

            @Override
            public int arity() {
                return 2;
            }
        }));

        env.bind("RGBCLIP", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];

                    if (a instanceof IntToken) {
                        int n = ((IntToken) a).intValue() / 256;
                        int res = n > 255 ? 255 : n < 0 ? 0 : n;
                        return _theContext.createInteger(res);
                    } else {
                        throw new InterpreterException(
                                "RGBCLIP needs an IntToken.");
                    }
                } catch (Exception ex) {
                    throw new InterpreterException(
                            "Function 'RGBCLIP': Cannot apply.", ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("readByte", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];

                    if (a instanceof ObjectToken) {
                        InputStream s = (InputStream) ((ObjectToken) a)
                                .getValue();
                        return _theContext.createInteger(s.read());
                    } else {
                        throw new InterpreterException("readByte needs a file.");
                    }
                } catch (Exception ex) {
                    throw new InterpreterException(
                            "Function 'readByte': Cannot apply.", ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        env.bind("openFile", _theContext.createFunction(new Function() {
            @Override
            public Object apply(Object[] args) {
                try {
                    Token a = (Token) args[0];

                    if (a instanceof StringToken) {
                        InputStream s = new FileInputStream(((StringToken) a)
                                .stringValue());
                        return new ObjectToken(s);
                    } else {
                        throw new InterpreterException(
                                "openFile needs a StringToken.");
                    }
                } catch (Exception ex) {
                    throw new InterpreterException(
                            "Function 'openFile': Cannot apply.", ex);
                }
            }

            @Override
            public int arity() {
                return 1;
            }
        }));

        // END SystemBuilder
        return env;
    }

    /** The singleton platform. */
    public final static Platform thePlatform = new PtolemyPlatform();

    /**
     * This Context represents the Ptolemy II system of data objects
     * in a way that can be used by the {@link
     * ptolemy.caltrop.ddi.util.DataflowActorInterpreter
     * DataflowActorInterpreter}.  The interpreter infrastructure
     * relies on a client-provided context for any manipulation of
     * data values.
     *
     * @see ptolemy.caltrop.ddi.util.DataflowActorInterpreter
     * @see caltrop.interpreter.ExprEvaluator
     * @see caltrop.interpreter.StmtEvaluator
     */
    private static final Context _theContext = new Context() {
        @Override
        public Object createNull() {
            try {
                return new ObjectToken(null);
            } catch (IllegalActionException ex) {
                throw new InterpreterException("Failed to create null.", ex);
            }
        }

        @Override
        public boolean isNull(Object o) {
            return o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() == null;
        }

        @Override
        public Object createBoolean(boolean b) {
            return b ? BooleanToken.TRUE : BooleanToken.FALSE;
        }

        @Override
        public boolean isBoolean(Object o) {
            return o instanceof BooleanToken;
        }

        @Override
        public boolean booleanValue(Object b) {
            try {
                return ((BooleanToken) b).booleanValue();
            } catch (Exception ex) {
                throw new InterpreterException(
                        "Failed to retrieve boolean value.", ex);
            }
        }

        @Override
        public Object createCharacter(char c) {
            try {
                return new ObjectToken(Character.valueOf(c));
            } catch (IllegalActionException iae) {
                throw new InterpreterException(
                        "Failed to create character value.");
            }
        }

        @Override
        public boolean isCharacter(Object o) {
            return o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() instanceof Character;
        }

        @Override
        public char charValue(Object o) {
            return ((Character) ((ObjectToken) o).getValue()).charValue();
        }

        @Override
        public Object createInteger(String s) {
            try {
                return new IntToken(s);
            } catch (IllegalActionException ex) {
                throw new InterpreterException(
                        "Failed to create integer value from string: '" + s
                        + "'.", ex);
            }
        }

        @Override
        public Object createInteger(int n) {
            return new IntToken(n);
        }

        @Override
        public boolean isInteger(Object o) {
            return o instanceof IntToken;
        }

        @Override
        public int intValue(Object o) {
            try {
                return ((IntToken) o).intValue();
            } catch (Exception ex) {
                throw new InterpreterException(
                        "Failed to retrieve integer value.", ex);
            }
        }

        @Override
        public Object createReal(double d) {
            return new DoubleToken(d);
        }

        @Override
        public Object createReal(String s) {
            try {
                return new DoubleToken(s);
            } catch (IllegalActionException ex) {
                throw new InterpreterException(
                        "Failed to create real value from string: '" + s + "'.",
                        ex);
            }
        }

        @Override
        public boolean isReal(Object o) {
            return o instanceof DoubleToken;
        }

        @Override
        public double realValue(Object o) {
            try {
                return ((DoubleToken) o).doubleValue();
            } catch (Exception ex) {
                throw new InterpreterException(
                        "Failed to retrieve real value.", ex);
            }
        }

        @Override
        public Object createString(String s) {
            return new StringToken(s);
        }

        @Override
        public boolean isString(Object o) {
            return o instanceof StringToken;
        }

        @Override
        public String stringValue(Object o) {
            try {
                return ((StringToken) o).stringValue();
            } catch (Exception ex) {
                throw new InterpreterException(
                        "Failed to retrieve string value.", ex);
            }
        }

        ///////// Collections
        @Override
        public Object createList(List a) {
            try {
                return new ObjectToken(a);
            } catch (IllegalActionException ex) {
                throw new InterpreterException("Failed to create list value.",
                        ex);
            }
        }

        @Override
        public boolean isList(Object o) {
            return o instanceof PtArrayList || o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() instanceof List;
        }

        @Override
        public List getList(Object o) {
            if (o instanceof ArrayToken) {
                return new PtArrayList((ArrayToken) o);
            } else {
                try {
                    return (List) ((ObjectToken) o).getValue();
                } catch (Exception ex) {
                    throw new InterpreterException(
                            "Failed to retrieve list value.", ex);
                }
            }
        }

        @Override
        public Object createSet(Set s) {
            try {
                return new ObjectToken(s);
            } catch (IllegalActionException ex) {
                throw new InterpreterException("Failed to create set value.",
                        ex);
            }
        }

        @Override
        public boolean isSet(Object o) {
            return o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() instanceof Set;
        }

        @Override
        public Set getSet(Object o) {
            return (Set) ((ObjectToken) o).getValue();
        }

        @Override
        public Object createMap(Map m) {
            try {
                return new ObjectToken(m);
            } catch (IllegalActionException ex) {
                throw new InterpreterException("Failed to create map value.",
                        ex);
            }
        }

        @Override
        public boolean isMap(Object o) {
            return o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() instanceof Map;
        }

        @Override
        public Map getMap(Object a) {
            try {
                return (Map) ((ObjectToken) a).getValue();
            } catch (Exception ex) {
                throw new InterpreterException("Failed to retrieve map value",
                        ex);
            }
        }

        @Override
        public Object applyMap(Object map, Object arg) {
            Map m = getMap(map);
            return m.get(arg);
        }

        @Override
        public boolean isCollection(Object o) {
            return o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() instanceof Collection;
        }

        @Override
        public Collection getCollection(Object a) {
            try {
                return (Collection) ((ObjectToken) a).getValue();
            } catch (Exception ex) {
                throw new InterpreterException(
                        "Failed to retrieve collection value.", ex);
            }
        }

        ///////// Functional and procedural closures
        @Override
        public Object createFunction(Function f) {
            Type[] argTypes = new Type[f.arity()];

            for (int i = 0; i < argTypes.length; i++) {
                argTypes[i] = BaseType.UNKNOWN;
            }

            return new FunctionToken(new PtCalFunction(f), new FunctionType(
                    argTypes, BaseType.UNKNOWN));
        }

        @Override
        public boolean isFunction(Object a) {
            return a instanceof FunctionToken || a instanceof ObjectToken
                    && ((ObjectToken) a).getValue() instanceof Function
                    || a instanceof Function;
        }

        @Override
        public Object applyFunction(Object function, Object[] args) {
            // TODO: perhaps need to optimize array creation
            try {
                if (function instanceof FunctionToken) {
                    Token[] tokenArgs = new Token[args.length];
                    System.arraycopy(args, 0, tokenArgs, 0, args.length);
                    return ((FunctionToken) function).apply(tokenArgs);
                } else if (function instanceof Function) {
                    return ((Function) function).apply(args);
                } else {
                    return ((Function) ((ObjectToken) function).getValue())
                            .apply(args);
                }
            } catch (Exception ex) {
                throw new InterpreterException("Function application failed.",
                        ex);
            }
        }

        @Override
        public Object createProcedure(Procedure p) {
            try {
                return new ObjectToken(p);
            } catch (IllegalActionException ex) {
                throw new InterpreterException("Failed to create procedure.",
                        ex);
            }
        }

        @Override
        public boolean isProcedure(Object a) {
            return a instanceof ObjectToken
                    && ((ObjectToken) a).getValue() instanceof Procedure;
        }

        @Override
        public void callProcedure(Object procedure, Object[] args) {
            try {
                ObjectToken pToken = (ObjectToken) procedure;
                Procedure p = (Procedure) pToken.getValue();
                p.call(args);
            } catch (Exception ex) {
                throw new InterpreterException("Procedure call failed.", ex);
            }
        }

        ///////// Class
        @Override
        public Object createClass(Class c) {
            try {
                return new ObjectToken(new ClassObject(c, this));
            } catch (IllegalActionException ex) {
                throw new InterpreterException("Cannot create class token.", ex);
            }
        }

        @Override
        public boolean isClass(Object o) {
            return o instanceof ObjectToken
                    && ((ObjectToken) o).getValue() instanceof ClassObject;
        }

        @Override
        public Class getJavaClass(Object o) {
            try {
                return ((ClassObject) ((ObjectToken) o).getValue())
                        .getClassObject();
            } catch (ClassCastException e) {
                if (o instanceof ObjectToken) {
                    throw new RuntimeException(
                            "Expected ClassObject, got instance of '"
                                    + ((ObjectToken) o).getValue().getClass()
                                    .getName() + "'.", e);
                } else {
                    throw new RuntimeException(
                            "Expected ClassObject inside ObjectToken, got instance of '"
                                    + o.getClass().getName()
                                    + "' as a token, with value: " + o + ".", e);
                }
            }
        }

        ///////// Misc.
        @Override
        public Object getLocation(Object structure, Object[] location) {
            if (location.length == 1 && isInteger(location[0])) {
                int index = intValue(location[0]);

                if (structure instanceof ArrayToken) {
                    return ((ArrayToken) structure).getElement(index);
                } else if (structure instanceof ObjectToken) {
                    try {
                        ObjectToken input = (ObjectToken) structure;

                        if (input.getValue() instanceof List) {
                            List inputList = (List) input.getValue();
                            return inputList.get(index);
                        } else if (input.getValue() instanceof Object[]) {
                            Object[] inputList = (Object[]) input.getValue();
                            return inputList[index];
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Failed to index into structure:" + structure,
                                ex);
                    }
                }
            } else if (location.length == 2 && isInteger(location[0])
                    && isInteger(location[1])) {
                int index1 = intValue(location[0]);
                int index2 = intValue(location[1]);

                if (structure instanceof MatrixToken) {
                    return ((MatrixToken) structure).getElementAsToken(index1,
                            index2);
                }
            }

            throw new RuntimeException("Failed to index into structure:"
                    + structure);
        }

        @Override
        public void setLocation(Object structure, Object[] location,
                Object value) {
            if (location.length == 1 && isInteger(location[0])) {
                int index = intValue(location[0]);

                if (structure instanceof ObjectToken) {
                    try {
                        ObjectToken input = (ObjectToken) structure;

                        if (input.getValue() instanceof List) {
                            List inputList = (List) input.getValue();
                            inputList.set(index, value);
                            return;
                        } else if (input.getValue() instanceof Object[]) {
                            Object[] inputList = (Object[]) input.getValue();
                            inputList[index] = value;
                            return;
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Failed to assign at index into structure:"
                                        + structure, ex);
                    }
                }
            }

            throw new RuntimeException("Failed to assign at index "
                    + location[0] + " into structure:" + structure
                    + " of class " + structure.getClass().getName());
        }

        @Override
        public Class getJavaClassOfObject(Object o) {
            // FIXME very preliminary. what about FunctionToken?
            // also, how will reflection work on methods that
            // need bytes, etc.
            if (o == null) {
                return Object.class;
            } else if (o instanceof BooleanToken) {
                return Boolean.class;
            } else if (o instanceof DoubleToken) {
                return Double.class;
            } else if (o instanceof IntToken) {
                return Integer.class;
            } else if (o instanceof StringToken) {
                return String.class;
            } else if (o instanceof ObjectToken) {
                Object v = ((ObjectToken) o).getValue();

                if (v instanceof ClassObject) {
                    return Class.class;
                } else {
                    return v.getClass();
                }
            } else if (o instanceof Token) {
                return o.getClass();
            } else {
                throw new InterpreterException(
                        "Unrecognized Token type in toClass:"
                                + o.getClass().toString());
            }
        }

        @Override
        public Object toJavaObject(Object o) {
            if (o instanceof BooleanToken) {
                //return new Boolean(booleanValue(o));
                return Boolean.valueOf(booleanValue(o));
            } else if (o instanceof DoubleToken) {
                return Double.valueOf(realValue(o));
            } else if (o instanceof IntToken) {
                return Integer.valueOf(intValue(o));
            } else if (o instanceof StringToken) {
                return stringValue(o);
            } else if (o instanceof ObjectToken) {
                Object v = ((ObjectToken) o).getValue();

                if (v instanceof ClassObject) {
                    return ((ClassObject) v).getClassObject();
                } else {
                    return ((ObjectToken) o).getValue();
                }
            } else if (o instanceof Token) {
                return o;
            } else {
                throw new InterpreterException(
                        "Unrecognized Token type in toClass:"
                                + o.getClass().toString());
            }
        }

        @Override
        public Object fromJavaObject(Object o) {
            try {
                if (o instanceof Token) {
                    return o;
                } else if (o instanceof Boolean) {
                    return new BooleanToken(((Boolean) o).booleanValue());
                } else if (o instanceof Double) {
                    return new DoubleToken(((Double) o).doubleValue());
                } else if (o instanceof Integer) {
                    return new IntToken(((Integer) o).intValue());
                } else if (o instanceof String) {
                    return new StringToken((String) o);
                } else if (o instanceof Class) {
                    return new ObjectToken(new ClassObject((Class) o, this));
                } else {
                    return new ObjectToken(o);
                }
            } catch (IllegalActionException ex) {
                throw new InterpreterException(
                        "Couldn't create ObjectToken from Java Object "
                                + o.toString(), ex);
            }
        }

        @Override
        public Object selectField(Object composite, String fieldName) {
            Class c = getJavaClassOfObject(composite);
            Field f;

            try {
                f = c.getField(fieldName);
                return fromJavaObject(f.get(toJavaObject(composite)));
            } catch (IllegalAccessException iae) {
                throw new InterpreterException("Tried to access field "
                        + fieldName + " in " + composite.toString(), iae);
            } catch (NoSuchFieldException nsfe1) {
                // maybe the enclosing object is a Class?
                if (isClass(composite)) {
                    try {
                        f = getJavaClass(composite).getField(fieldName);
                        return fromJavaObject(f.get(null));
                    } catch (NoSuchFieldException nsfe2) {
                        return new MethodObject(toJavaObject(composite),
                                fieldName, this);
                    } catch (IllegalAccessException iae) {
                        throw new InterpreterException("Tried to access field "
                                + fieldName + " in " + composite.toString(),
                                iae);
                    }
                } else {
                    // assume it is a method
                    try {
                        return new ObjectToken(new MethodObject(
                                toJavaObject(composite), fieldName, this));
                    } catch (IllegalActionException iae) {
                        throw new InterpreterException("Tried to access field "
                                + fieldName + " in " + composite.toString(),
                                iae);
                    }
                }
            }
        }
    };
}
