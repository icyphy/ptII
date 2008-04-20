/* Property hierarchy.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
import ptolemy.graph.DirectedAcyclicGraph;
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
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class Lattice extends PropertyLattice {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Property CONFLICT = new Conflict(this);
    
    public Property DOUBLEDOUBLE = new Double(this);
    public Property DOUBLE = new Double(this);
    public Property FLOAT = new Float(this);

    public Property LONGLONG = new LongLong(this);
    public Property LONG = new Long(this);
    public Property INT = new Int(this);
    public Property SHORT = new Short(this);
    public Property CHAR = new Char(this);

    public Property ULONGLONG = new ULongLong(this);
    public Property ULONG = new ULong(this);
    public Property UINT = new UInt(this);
    public Property USHORT = new UShort(this);
    public Property UCHAR = new UChar(this);

    public Property BOOLEAN = new Boolean(this);

    public Property VOID = new Void(this);

    public Property UNKNOWN = new Unknown(this);

    public Property getInitialProperty() {
        return UNKNOWN;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    
    // The infinite property lattice
    public Lattice() {
        super();
        _lattice.setBasicLattice(new DirectedAcyclicGraph());

        DirectedAcyclicGraph basicLattice = 
            (DirectedAcyclicGraph) _lattice.basicLattice();
        
// FIXME: how to convert from Ptolemy type system to EDC type system?         
        basicLattice.addNodeWeight(CONFLICT);

        basicLattice.addNodeWeight(DOUBLEDOUBLE);
        basicLattice.addNodeWeight(DOUBLE);
        basicLattice.addNodeWeight(FLOAT);

        basicLattice.addNodeWeight(LONGLONG);
        basicLattice.addNodeWeight(LONG);
        basicLattice.addNodeWeight(INT);
        basicLattice.addNodeWeight(SHORT);
        basicLattice.addNodeWeight(CHAR);

        basicLattice.addNodeWeight(ULONGLONG);
        basicLattice.addNodeWeight(ULONG);
        basicLattice.addNodeWeight(UINT);
        basicLattice.addNodeWeight(USHORT);
        basicLattice.addNodeWeight(UCHAR);

        basicLattice.addNodeWeight(BOOLEAN);

        basicLattice.addNodeWeight(VOID);

        basicLattice.addNodeWeight(UNKNOWN);


        basicLattice.addEdge(UNKNOWN, VOID);        
        basicLattice.addEdge(UNKNOWN, BOOLEAN);        
        basicLattice.addEdge(UNKNOWN, UCHAR);
        basicLattice.addEdge(UNKNOWN, CHAR);
        basicLattice.addEdge(UNKNOWN, FLOAT);
       
        basicLattice.addEdge(CHAR, SHORT);
        basicLattice.addEdge(SHORT, INT);
        basicLattice.addEdge(INT, LONG);
        basicLattice.addEdge(LONG, LONGLONG);

        basicLattice.addEdge(UCHAR, USHORT);
        basicLattice.addEdge(USHORT, UINT);
        basicLattice.addEdge(UINT, ULONG);
        basicLattice.addEdge(ULONG, ULONGLONG);
        
        basicLattice.addEdge(UCHAR, SHORT);
        basicLattice.addEdge(USHORT, INT);
        // UINT and ULONG have same range
        basicLattice.addEdge(UINT, LONGLONG);
        basicLattice.addEdge(ULONG, LONGLONG);

        // FIXME: Is it possible to convert boolean to anything but boolean? 
        // basicLattice.addEdge(BOOLEAN, SINT8);

        basicLattice.addEdge(FLOAT, DOUBLE);
        basicLattice.addEdge(DOUBLE, DOUBLEDOUBLE);

        // FIXME: convert boolean to REAL32?
//        basicLattice.addEdge(BOOLEAN, REAL32);
  
        // FIXME: automatic conversion from integer to real types valid?
        // do we need explicit modeling of int -> real and real -> int?
        // does not work since UINT32 and SINT32 need to have single LUB (INVALID)
//        basicLattice.addEdge(SINT32, REAL32);
//        basicLattice.addEdge(UINT32, REAL32);

      basicLattice.addEdge(VOID, CONFLICT);
      basicLattice.addEdge(BOOLEAN, CONFLICT);
      basicLattice.addEdge(ULONGLONG, CONFLICT);
      basicLattice.addEdge(LONGLONG, CONFLICT);
      basicLattice.addEdge(DOUBLEDOUBLE, CONFLICT);

        // FIXME: Replace this with an assert when we move to 1.5
        if (!basicLattice.isLattice()) {
            throw new InternalErrorException("ThePropertyLattice: The "
                    + "property hierarchy is not a lattice.");
        }
    }

    public Property convertJavaToCtype(Type type, Token token) throws IllegalActionException {
        TypeProperty cType = (TypeProperty)UNKNOWN;
        
        // FIXME: consider ShortToken, UnsignedByteToken, ...
        // FIXME: what is the criteria for bit-values? 
        if (type.equals(BaseType.BOOLEAN)) {
            cType = (TypeProperty)BOOLEAN;
        } else if ((type.equals(BaseType.UNSIGNED_BYTE)) || (type.equals(BaseType.SHORT)) || (type.equals(BaseType.INT)) || (type.equals(BaseType.LONG))) {
//            if (token == null) {
                if (type.equals(BaseType.UNSIGNED_BYTE)) {
                    cType = (TypeProperty)UCHAR;
                } else if (type.equals(BaseType.SHORT)) {
                   cType = (TypeProperty)SHORT;
                } else if (type.equals(BaseType.INT)) {
                    cType = (TypeProperty)INT;
                } else if (type.equals(BaseType.LONG)) {
                    cType = (TypeProperty)LONGLONG;
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
                    cType = (TypeProperty)DOUBLE;
/*            } else {
                if (((ScalarToken)token).isGreaterThan(((ScalarToken)(((TypeProperty)FLOAT).getMaxValue()))).booleanValue()) {
                    cType = (TypeProperty)DOUBLE;
                } else { 
                    cType = (TypeProperty)FLOAT;   
                }
            }
*/
        } else if (type.equals(BaseType.FLOAT)) {
            cType = (TypeProperty)FLOAT;   
        } else if (type.equals(BaseType.NIL)) {
            cType = (TypeProperty)VOID;   
        }
        
        return (Property)cType;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
