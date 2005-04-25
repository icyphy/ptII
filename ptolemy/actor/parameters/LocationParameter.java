/* A parameter that specifies the location of its container.

Copyright (c) 2002-2005 The Regents of the University of California.
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
package ptolemy.actor.parameters;

import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// LocationParameter

/**
   A parameter that specifies the location of its container.
   This location is used when rendering the container in a Vergil diagram.
   The parameter value is a double matrix with one row and two columns.
   The default value is [0.0, 0.0].

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 3.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class LocationParameter extends Parameter implements Locatable {
    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public LocationParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setTypeEquals(BaseType.DOUBLE_MATRIX);
        setExpression("[0.0, 0.0]");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the location in some Cartesian coordinate system.
     *  @return The location.
     *  @see #setLocation(double [])
     */
    public double[] getLocation() {
        try {
            DoubleMatrixToken token = (DoubleMatrixToken) getToken();
            double[][] value = token.doubleMatrix();
            return value[0];
        } catch (IllegalActionException ex) {
            // Should not occur.
            throw new InternalErrorException(ex);
        }
    }

    /** Set the location in some Cartesian coordinate system, and notify
     *  the container and any value listeners of the new location. This
     *  also propagates the value to derived objects.
     *  @param location The location.
     *  @exception IllegalActionException If the location is rejected.
     *  @see #getLocation()
     */
    public void setLocation(double[] location) throws IllegalActionException {
        double[][] value = new double[1][2];
        value[0][0] = location[0];
        value[0][1] = location[1];
        setToken(new DoubleMatrixToken(value, DoubleMatrixToken.DO_NOT_COPY));
        propagateValue();
    }
}
