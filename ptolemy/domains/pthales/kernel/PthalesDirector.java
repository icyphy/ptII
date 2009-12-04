/* A director for multidimensional dataflow.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

package ptolemy.domains.pthales.kernel;

import java.util.ArrayList;

import ptolemy.actor.Receiver;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;

// FIXME: Change the parameters to be ordered record types
// so that the full expression language is supported.
// They should not be strings, and they need not be parsed.
// Moreover, record types provide nice support for merging in defaults, etc.

// FIXME: The value of the size parameter could optionally be inferred
// if there is no torroidal wrap around desired.  Perhaps there should
// be parameter indicating to do such inference.

/** 
 * A director for multidimensional dataflow.
 * This is based on Array-OL, as described by:
 * <ol>
 * <li>
 * Boulet, P. (2007). "Array-OL Revisited, Multidimensional Intensive
 * Signal Processing Specification," Technical Report 6113,
 * INRIA, Orsay, France.
 * </ol>
 * The notation used here is intended to follow the spirit of
 * Spear [FIXME: Reference?], from Thales, a software system
 * based on Array-OL. In this notation, unlike Boulet's,
 * domains are named, and patterns for reading and writing
 * arrays are given using those names.
 * <p>
 * The execution is governed by the following parameters
 * in the model. In all cases, the parameters are strings
 * of the form "x = n, y = m, ...", where x and y are arbitrary
 * dimension names and n and m are non-negative integers.
 * For those parameters that support strides, n and m
 * can be replaced by n.s or m.s, where s is the stride.
 * The stride defaults to 1 in such cases. Unless otherwise
 * stated, the parameters do not support strides.
 * Ports contain the following parameters:
 * <ol>
 * 
 * <li> <i>size</i>: This is a parameter of each output port
 * that specifies the size of the array written by that output
 * port. All dimensions must be specified, and every output
 * port must have such a parameter. In addition, every input
 * port of a composite actor that contains a PthalesDirector
 * must also have such a parameter.
 * 
 * <li> <i>offset</i>: This optional parameter gives the base location
 * at which writing begins for each iteration of this director.
 * Its value defaults to 0 for any dimension that is not specified.
 * 
 * <li> <i>pattern</i>: This is a parameter of each port that
 * specifies the shape of the array produced or consumed on that
 * port. Moreover, if an actor reads from or writes to the port
 * sequentially (using get() and send() methods), then the pattern
 * specifies the order in which the array is filled.
 * For example, if you send tokens with values 1, 2, 3, 4, 5, 6
 * using a pattern x=3, y=2, then the array is filled as
 * follows:
 * <table>
 * <tr> <td> x </td> <td> y </td> <td> value </td></tr>
 * <tr> <td> 0 </td> <td> 0 </td> <td> 1 </td></tr>
 * <tr> <td> 1 </td> <td> 0 </td> <td> 2 </td></tr>
 * <tr> <td> 2 </td> <td> 0 </td> <td> 3 </td></tr>
 * <tr> <td> 0 </td> <td> 1 </td> <td> 4 </td></tr>
 * <tr> <td> 1 </td> <td> 1 </td> <td> 5 </td></tr>
 * <tr> <td> 2 </td> <td> 1 </td> <td> 6 </td></tr>
 * </table>
 * If on the other hand you specify a pattern
 * y=2, x=3, then an array of the same shape is used,
 * but it is now filled as follows:
 * <table>
 * <tr> <td> x </td> <td> y </td> <td> value </td></tr>
 * <tr> <td> 0 </td> <td> 0 </td> <td> 1 </td></tr>
 * <tr> <td> 0 </td> <td> 1 </td> <td> 2 </td></tr>
 * <tr> <td> 1 </td> <td> 0 </td> <td> 3 </td></tr>
 * <tr> <td> 1 </td> <td> 1 </td> <td> 4 </td></tr>
 * <tr> <td> 2 </td> <td> 0 </td> <td> 5 </td></tr>
 * <tr> <td> 2 </td> <td> 1 </td> <td> 6 </td></tr>
 * </table>
 * If a stride is given, then the pattern may have gaps
 * in it. For example, "x = 2.2" specifies that two values
 * are produced in the x dimension, and that they are separated
 * by one value that is not produced. Values that are not
 * produced default to zero (the value of zero depends on the
 * data type; for example, zero for strings is the empty string,
 * whereas zero for doubles is 0.0).
 *
 * <li> <i>tiling</i>: This parameter gives the increment of the
 * base location in each dimension for each successive iteration
 * of the actor. This is a property of an output or an input
 * port of an actor.
 * 
 * </ol>
 * 
 * In addition, actors must contain the following parameter:
 * <ol>
 * <li> <i>repetitions</i>: This required parameter specifies the
 * number of iterations in each dimension of
 * the actor in a single firing of the composite actor
 * containing this director. This parameter also defines the
 * order in which dimensions are traversed.
 * </ol>
 * <p>
 * In all cases, when indexes are incremented, they are incremented
 * in a toroidal fashion, wrapping around when they reach the size
 * of the array. Thus, it is always possible (though rarely useful)
 * for an array size to be 1 in every dimension.
 * <p>
 * NOTE: It should be possible to define a PtalesPort and
 * PtalesCompositeActor that contain the above parameters, as
 * a convenience. These could be put in a library.
 * <p>
 * NOTE: It should be possible to create a single interface
 * for this director so that when double clicked, it
 * brings up an interactive dialog that has the form of a Spear
 * table. It would have one row per port, plus a header row
 * to specify the iterations.
 * <p>
 * FIXME: Need to export production and consumption data for
 * SDF, allowing these Pthales models to be nested within SDF
 * or within Pthales, which will also allow it to be nested
 * within modal models.
 * 
 * @author Edward A. Lee, Eric Lenormand, Stavros Tripakis
 *
 */
public class PthalesDirector extends SDFDirector {

    public PthalesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setScheduler(new PthalesScheduler(this, "PtalesScheduler"));
        
        if (getAttribute("library") == null) {
            library = new StringParameter(this,
                    "library");
            library.setExpression("");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Receiver newReceiver() {
        PthalesReceiver recv =  new PthalesReceiver();
        _receivers.add(recv);
        
        return recv;
    }
    
    /** Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly, since the act of computing the
     *  schedule sets the rate parameters of the external ports.  In
     *  addition, performing scheduling during preinitialization
     *  enables it to be present during code generation.  The order in
     *  which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        // Load library needed to project
        if (!(_library.length() == 0)) {
            System.loadLibrary(_library);
        }
        
        // Empties list of receivers before filling it
        _receivers.removeAll(_receivers);

        super.preinitialize();
    }
    
    /** Attribute update
     * @see ptolemy.domains.sdf.kernel.SDFDirector#attributeChanged(ptolemy.kernel.util.Attribute)
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == library) {
            _library = library.getExpression();
        }
    }
    
     /**
     * @return the name of the library to use
     * @throws IllegalActionException
     */
    public String getLibName()
            throws IllegalActionException {
        return _library;
    }

    
    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule.
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  NOTE: This method does not conform with the strict actor semantics
     *  because it calls postfire() of actors. Thus, it should not be used
     *  in domains that require a strict actor semantics, such as SR or
     *  Continuous.
     *  @exception IllegalActionException If any actor executed by this
     *   actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
        for (PthalesReceiver recv : _receivers)
        {
            recv.reset();
        }
        super.fire();
    }
    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** Buffer memory. */
    protected StringParameter library;

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The dimensions relevant to this receiver. */
    private ArrayList<PthalesReceiver> _receivers = new ArrayList<PthalesReceiver>();
    
    private String _library = "";
}
