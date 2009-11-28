/* Property hierarchy.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.typeSystem_C;

import ptolemy.data.Token;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.data.properties.lattice.TypeProperty;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// PropertyLattice

/**
 Property hierarchy base class.
 Note that all public methods are synchronized.
 There are more than one instances of a property lattice.
 Although the property lattice is constructed once and then typically
 does not change during execution, the methods need to be synchronized
 because there are various data structures used to cache results that
 are expensive to compute. These data structures do change during
 execution. Multiple threads may be accessing the property lattice
 simultaneously and modifying these data structures. To ensure
 thread safety, the methods need to be synchronized.

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class Lattice extends PropertyLattice {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    private Property CONFLICT = new Conflict(this);

    private Property DOUBLEDOUBLE = new Double(this);
    private Property DOUBLE = new Double(this);
    private Property FLOAT = new Float(this);

    private Property LONGLONG = new LongLong(this);
    private Property LONG = new Long(this);
    private Property INT = new Int(this);
    private Property SHORT = new Short(this);
    private Property CHAR = new Char(this);

    private Property ULONGLONG = new ULongLong(this);
    private Property ULONG = new ULong(this);
    private Property UINT = new UInt(this);
    private Property USHORT = new UShort(this);
    private Property UCHAR = new UChar(this);

    private Property BOOLEAN = new Boolean(this);

    private Property VOID = new Void(this);

    private Property UNKNOWN = new Unknown(this);

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // The infinite property lattice
    public Lattice() {
        super();

        // FIXME: how to convert from Ptolemy type system to EDC type system?
        addNodeWeight(CONFLICT);

        addNodeWeight(DOUBLEDOUBLE);
        addNodeWeight(DOUBLE);
        addNodeWeight(FLOAT);

        addNodeWeight(LONGLONG);
        addNodeWeight(LONG);
        addNodeWeight(INT);
        addNodeWeight(SHORT);
        addNodeWeight(CHAR);

        addNodeWeight(ULONGLONG);
        addNodeWeight(ULONG);
        addNodeWeight(UINT);
        addNodeWeight(USHORT);
        addNodeWeight(UCHAR);

        addNodeWeight(BOOLEAN);

        addNodeWeight(VOID);

        addNodeWeight(UNKNOWN);

        addEdge(UNKNOWN, VOID);
        addEdge(UNKNOWN, BOOLEAN);
        addEdge(UNKNOWN, UCHAR);
        addEdge(UNKNOWN, CHAR);
        addEdge(UNKNOWN, FLOAT);

        addEdge(CHAR, SHORT);
        addEdge(SHORT, INT);
        addEdge(INT, LONG);
        addEdge(LONG, LONGLONG);

        addEdge(UCHAR, USHORT);
        addEdge(USHORT, UINT);
        addEdge(UINT, ULONG);
        addEdge(ULONG, ULONGLONG);

        addEdge(UCHAR, SHORT);
        addEdge(USHORT, INT);
        // UINT and ULONG have same range
        addEdge(UINT, LONGLONG);
        addEdge(ULONG, LONGLONG);

        // FIXME: Is it possible to convert boolean to anything but boolean?
        // addEdge(BOOLEAN, SINT8);

        addEdge(FLOAT, DOUBLE);
        addEdge(DOUBLE, DOUBLEDOUBLE);

        // FIXME: convert boolean to REAL32?
        //        addEdge(BOOLEAN, REAL32);

        // FIXME: automatic conversion from integer to real types valid?
        // do we need explicit modeling of int -> real and real -> int?
        // does not work since UINT32 and SINT32 need to have single LUB (INVALID)
        //        addEdge(SINT32, REAL32);
        //        addEdge(UINT32, REAL32);

        addEdge(VOID, CONFLICT);
        addEdge(BOOLEAN, CONFLICT);
        addEdge(ULONGLONG, CONFLICT);
        addEdge(LONGLONG, CONFLICT);
        addEdge(DOUBLEDOUBLE, CONFLICT);

        // FIXME: Replace this with an assert when we move to 1.5
        if (!isLattice()) {
            throw new InternalErrorException("ThePropertyLattice: The "
                    + "property hierarchy is not a lattice.");
        }
    }

    public Property convertJavaToCtype(Type type, Token token)
            throws IllegalActionException {
        TypeProperty cType = (TypeProperty) UNKNOWN;

        // FIXME: consider ShortToken, UnsignedByteToken, ...
        // FIXME: what is the criteria for bit-values?
        if (type.equals(BaseType.BOOLEAN)) {
            cType = (TypeProperty) BOOLEAN;
        } else if ((type.equals(BaseType.UNSIGNED_BYTE))
                || (type.equals(BaseType.SHORT)) || (type.equals(BaseType.INT))
                || (type.equals(BaseType.LONG))) {
            //            if (token == null) {
            if (type.equals(BaseType.UNSIGNED_BYTE)) {
                cType = (TypeProperty) UCHAR;
            } else if (type.equals(BaseType.SHORT)) {
                cType = (TypeProperty) SHORT;
            } else if (type.equals(BaseType.INT)) {
                cType = (TypeProperty) INT;
            } else if (type.equals(BaseType.LONG)) {
                cType = (TypeProperty) LONGLONG;
            }
            /*            } else {
                            if (((ScalarToken)token).isGreaterThan(((ScalarToken)(((TypeProperty)UINT).getMaxValue()))).booleanValue()) {
                // FIXME: throw exception
                //               throw ;
                            } else if (((ScalarToken)token).isGreaterThan(((ScalarToken)(((TypeProperty)USHORT).getMaxValue()))).booleanValue()) {
                                cType = (TypeProperty)UINT;
                            } else if (((ScalarToken)token).isGreaterThan(((ScalarToken)(((TypeProperty)UCHAR).getMaxValue()))).booleanValue()) {
                                cType = (TypeProperty)USHORT;
                            } else if (((ScalarToken)token).isLessThan(((ScalarToken)(((TypeProperty)INT).getMinValue()))).booleanValue()) {
                //              FIXME: throw exception
                //              throw ;
                            } else if (((ScalarToken)token).isLessThan(((ScalarToken)(((TypeProperty)SHORT).getMinValue()))).booleanValue()) {
                                cType = (TypeProperty)INT;
                            } else if (((ScalarToken)token).isLessThan(((ScalarToken)(((TypeProperty)CHAR).getMinValue()))).booleanValue()) {
                                cType = (TypeProperty)SHORT;
                            } else if (((ScalarToken)token).isLessThan(((ScalarToken)(((TypeProperty)UCHAR).getMinValue()))).booleanValue()) {
                                cType = (TypeProperty)CHAR;
                            } else {
                                cType = (TypeProperty)UCHAR;
                            }
                        }
            */
        } else if (type.equals(BaseType.DOUBLE)) {
            // FIXME: Consider range and precision for type assignment!
            //            if (token == null) {
            cType = (TypeProperty) DOUBLE;
            /*            } else {
                            if (((ScalarToken)token).isGreaterThan(((ScalarToken)(((TypeProperty)FLOAT).getMaxValue()))).booleanValue()) {
                                cType = (TypeProperty)DOUBLE;
                            } else {
                                cType = (TypeProperty)FLOAT;
                            }
                        }
            */
        } else if (type.equals(BaseType.FLOAT)) {
            cType = (TypeProperty) FLOAT;
        } else if (type.equals(BaseType.NIL)) {
            cType = (TypeProperty) VOID;
        }

        return (Property) cType;
    }
}
