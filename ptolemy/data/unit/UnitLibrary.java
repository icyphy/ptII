/* The Unit Library

 Copyright (c) 1999-2003 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_3
                                        COPYRIGHTENDKEY
@ProposedRating Red (rowland@eecs.berkeley.edu)
@AcceptedRating Red (rowland@eecs.berkeley.edu)
*/
package ptolemy.data.unit;

import java.util.Iterator;
import java.util.Vector;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

/** A Library containing definitions of commonly used units.
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitLibrary {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static Unit Identity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a unit to the Library.
     * @param unit
     */
    public static void addToLibrary(Unit unit) {
        _unitsLibrary.add(unit);
    }

    /** Search Library to find Unit that has the same type and is the closest to
     *  a unit in terms of the scalars.
     * @return The Unit closest to this the argument. Null, if there are no
     * Units in the Library with the same type.
     */
    public static Unit getClosestUnit(Unit unit) {
        Vector possibles = getUnitsByType(unit);
        if (possibles.isEmpty()) {
            return null;
        }
        double scalarDistance = Double.MAX_VALUE;
        Unit retv = null;
        for (int i = 0; i < possibles.size(); i++) {
            Unit possible = (Unit) (possibles.elementAt(i));
            double distance =
                Math.abs(
                    possible.getScale() - unit.getScale());
            if (distance < scalarDistance) {
                scalarDistance = distance;
                retv = possible;
            }
        }
        return retv;
    }

    /** Return the Library.
     * @return The Library
     */
    public static Vector getLibrary() {
        return _unitsLibrary;
    }

    /** Return the number of categories.
     * @return Number of categories.
     */
    public static int getNumCategories() {
        return _numCats;
    }

    /** Return the Parser.
     * @return The Parser.
     */
    public static UParser getParser() {
        return _parser;
    }

    /** Search Library for Unit equal to a particular unit. That is, both the
     *  type and scalar must be equal to the argument.
     * @return Unit in Library equal to this one. Null if none found.
     */
    public static Unit getUnit(Unit unit) {
        Unit retv = getClosestUnit(unit);
        if (retv == null)
            return null;
        if (Math.abs(retv.getScale() - unit.getScale())
            < 1.0E-8) {
            return retv;
        }
        return null;
    }

    /** Search the Library for a unit with a particular name.
     * @param name The name of the desired unit.
     * @return The unit with name equal to the argument. Null, if the Library
     * doesn't have a unit with that name.
     */
    public static Unit getUnitByName(String name) {
        Vector library = getLibrary();
        for (int i = 0; i < library.size(); i++) {
            Unit lUnit = (Unit) (library.elementAt(i));
            Vector names = lUnit.getNames();
            for (int j = 0; j < names.size(); j++) {
                if (((String) (names.elementAt(j))).equals(name)) {
                    return lUnit;
                }
            }
        }
        return null;
    }

    /** Search Library for all Units with type equal to this one.
     * @param unit
     * @return Vector of Units with type equal to the argument.
     */
    public static Vector getUnitsByType(Unit unit) {
        Vector retv = new Vector();
        Vector library = UnitLibrary.getLibrary();
        for (int i = 0; i < library.size(); i++) {
            Unit lUnit = (Unit) (library.elementAt(i));
            if (lUnit.hasSameType(unit)) {
                retv.add(lUnit);
            }
        }
        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static boolean _debug = false;
    private static int _numCats;
    private static UParser _parser;
    private static Vector _unitsLibrary;

    static {
        _parser = new UParser();
        MoMLParser momlParser = new MoMLParser();
        UnitSystem us = null;
        try {
            NamedObj container = new NamedObj();
            momlParser.setContext(container);
            momlParser.parseFile("ptolemy/data/unit/SI.xml");
            us = (UnitSystem) (container.getAttribute("SI"));
        } catch (Exception e) {
            KernelException.stackTraceToString(e);
        } catch (Throwable t) {
            KernelException.stackTraceToString(t);
        }

        // Initialize the Library.
        _unitsLibrary = new Vector();
        _numCats = UnitUtilities.getNumCategories();
        Identity = new Unit("Identity");
        _unitsLibrary.add(Identity);

        Iterator oldStyleUnits = us.attributeList().iterator();
        UnitLibrary enclosingObject = new UnitLibrary();
        Vector pairs = new Vector();

        while (oldStyleUnits.hasNext()) {
            Object oldStyleUnit = oldStyleUnits.next();
            if (oldStyleUnit instanceof BaseUnit) {
                BaseUnit baseUnit = (BaseUnit) oldStyleUnit;
                Unit basicUnit = new Unit(baseUnit);
                addToLibrary(basicUnit);
            } else if (oldStyleUnit instanceof Parameter) {
                String name = ((Parameter) oldStyleUnit).getName();
                String expr = ((Parameter) oldStyleUnit).getExpression();
                UnitNameExprPair pair =
                    enclosingObject.new UnitNameExprPair(name, expr);
                pairs.add(pair);
            }
        }
        boolean madeChange = true;
        while (!pairs.isEmpty() && madeChange) {
            madeChange = false;
            Iterator iter = pairs.iterator();
            while (iter.hasNext()) {
                UnitNameExprPair pair = (UnitNameExprPair) (iter.next());
                String expr = pair.getUExpr();
                try {
                    UnitExpr uExpr = _parser.parseUnitExpr(expr);
                    Unit unit = uExpr.eval(null);
                    if (unit != null) {
                        unit.setName(pair.getName());
                        iter.remove();
                        madeChange = true;
                        addToLibrary(unit);
                    }
                } catch (ParseException e1) {
                    // OK here.
                }
            }
        }
        if (_debug) {
            Vector units = getLibrary();
            for (int i = 0; i < units.size(); i++) {
                System.out.println(((Unit) (units.elementAt(i))).toString());
            }
        }
    }

    /** UnitNameExprPair
     * @author Rowland R Johnson
     * @version $Id$
     * @since Ptolemy II 3.1
     *
     */
    private class UnitNameExprPair {

        public UnitNameExprPair(String n, String ue) {
            _name = n;
            _uExpr = ue;
        }

        public String getName() {
            return _name;
        }

        public String getUExpr() {
            return _uExpr;
        }

        private String _name;
        private String _uExpr;
    }
}
