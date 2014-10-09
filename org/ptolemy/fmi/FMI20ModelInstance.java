/* A Java Native Access (JNA) interface to the Functional Mock-up Interface 2.0 ModelInstance struct.

   Copyright (c) 2014 The Regents of the University of California.
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
package org.ptolemy.fmi;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
/**
 * A Java Native Access (JNA) interface to the Functional Mock-up Interface 2.0 ModelInstance struct.
 * @author Christopher Brooks
 * @version $Id: FMIModelDescription.java 70265 2014-10-01 15:03:22Z cxh $
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMI20ModelInstance extends Structure {
    /** Instantiate a Java structure that that represents the C
     * structure that contains information about events.
     */
    public FMI20ModelInstance() {
        super();
    }

    /** Instantiate a Java structure that that represents the C
     * structure that contains information about events.
     * @param peer The peer
     */
    public FMI20ModelInstance(Pointer peer) {
        super(peer);
    }

    /** Access the structure by reference.
     */
    public static class ByReference extends FMI20ModelInstance implements Structure.ByReference {
    };

    /** Access the structure by value.
     */
    public static class ByValue extends FMI20ModelInstance implements Structure.ByValue {
    };


    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** C type : double*. */
    public DoubleByReference r;

    /** C type : int*. */
    public IntByReference i;

    /** C type : int*. */
    public IntByReference b;

    /** C type : const char**. */
    public PointerByReference s;

    /** C type : int*. */
    public IntByReference isPositive;

    /** The time. */
    public double time;

    /** C type : const char**. */
    public PointerByReference instanceName;

    /**
     * The type.
     * C type : fmi2Type
     * @see fmi2Type
     */
    public int type;

    /** C type : const char**. */
    public PointerByReference GUID;

    /** C type : const fmi2CallbackFunctions*. */
    public Pointer /*org.ptolemy.fmi.fmi2CallbackFunctions.ByReference*/ functions;

    /** True if logging is on. */
    public int loggingOn;

    /** C type : int[4]. */
    public int[] logCategories = new int[4];

    /** C type : fmi2ComponentEnvironment. */
    public Pointer componentEnvironment;

    /**
     * The state of the model.
     * C type : ModelState
     * @see ModelState
     */
    public int state;

    /** C type : fmi2EventInfo. */
    public FMI20EventInfo eventInfo;

    /** 1 if the values are dirty. */
    public int isDirtyValues;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected List<? > getFieldOrder() {
        return Arrays.asList("r", "i", "b", "s", "isPositive", "time", "instanceName", "type", "GUID", "functions", "loggingOn", "logCategories", "componentEnvironment", "state", "eventInfo", "isDirtyValues");
    }
}
