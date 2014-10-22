/* An enumeration class that specifies all the valid prefixes
 * (e.g, kilo-, centi-, milli-) for SI units.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

package ptolemy.data.ontologies.lattice.unit;

///////////////////////////////////////////////////////////////////
//// SIUnitPrefixes

/** An enumeration class that specifies all the valid prefixes
 *  (e.g, kilo-, centi-, milli-) for SI units.
@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public enum SIUnitPrefixes {

    ///////////////////////////////////////////////////////////////////
    ////                         public enumeration values         ////

    YOTTA(1e24, "yotta", "Y"), ZETTA(1e21, "zetta", "Z"), EXA(1e18, "exa", "E"), PETA(
            1e15, "peta", "P"), TERA(1e12, "tera", "T"), GIGA(1e9, "giga", "G"), MEGA(
            1e6, "mega", "M"), KILO(1e3, "kilo", "k"), HECTO(100.0, "hecto",
            "h"), DEKA(10.0, "deka", "da"), DECI(0.1, "deci", "d"), CENTI(0.01,
            "centi", "c"), MILLI(1e-3, "milli", "m"), MICRO(1e-6, "micro", "u"), NANO(
            1e-9, "nano", "n"), PICO(1e-12, "pico", "p"), FEMTO(1e-15, "femto",
            "f"), ATTO(1e-18, "atto", "a"), ZEPTO(1e-21, "zepto", "z"), YOCTO(
            1e-24, "yocto", "y");

    ///////////////////////////////////////////////////////////////////
    ////                         private enumeration constructor   ////

    /** Create a new SI unit prefix with the given conversion factor, name,
     *  and symbol.
     *  @param unitFactor The unit factor as a double value.
     *  @param prefixName The name of the unit prefix as a string.
     *  @param prefixSymbol The short symbol for the unit prefix as a string.
     */
    private SIUnitPrefixes(double unitFactor, String prefixName,
            String prefixSymbol) {
        _unitFactor = unitFactor;
        _prefixName = prefixName;
        _prefixSymbol = prefixSymbol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the unit factor associated with the given unit prefix.
     *  @return The unit factor as a double value.
     */
    public double unitFactor() {
        return _unitFactor;
    }

    /** Return the name of the given unit prefix as a string.
     *  @return The name of the unit prefix as a string.
     */
    public String prefixName() {
        return _prefixName;
    }

    /** Return the short symbol representing the given unit prefix as a string.
     *  @return The short symbol for the unit prefix as a string.
     */
    public String prefixSymbol() {
        return _prefixSymbol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The unit conversion factor as a double value for the unit prefix. */
    private final double _unitFactor;

    /** The name of the unit prefix as a string. */
    private final String _prefixName;

    /** The short symbol that represents the unit prefix as a string. */
    private final String _prefixSymbol;
}
