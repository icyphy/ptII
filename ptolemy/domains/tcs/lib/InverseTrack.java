/* A model of a track in Train control systems.

 Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.domains.tcs.lib;


import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
////InverseTrack

/** This actor extends AbstractTrack with input in east side and output in west side.
 * Shape of the train in this actor is reverse of the shape of the train in track.
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public class InverseTrack extends AbstractTrack {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public InverseTrack(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        StringAttribute cardinality = new StringAttribute(input, "_cardinal");
        cardinality.setExpression("EAST");

        cardinality = new StringAttribute(output, "_cardinal");
        cardinality.setExpression("WEST");

        _shape.vertices.setExpression("{-50.0,-10.0,-50.0,0.0,48.0,0.0,48.0,-27.0,40.01484104004581,-27.321614804199694,40.0,-20.0," +
                        "32.0,-20.0,32.002033150772704,-27.464700659365285,24.27539697183078,-27.464700659365285,24.0," +
                        "-20.0,16.0,-20.0,15.976417372226495,-27.464700659365285,8.0,-27.4647,8.0,-20.02424,0.0,-20.0," +
                        "0.09388744884587494,-27.464700659365285,-8.062006295592822,-27.464700659365285,-8.0,-20.0,-16.0,-20.0," +
                        "-16.074814184865925,-27.321614804199694,-24.0,-27.32161,-24.0,-20.0,-32.67,-20.0,-32.67277,-27.0,-32.81585923924009," +
                        "-27.321614804199694,47.74147721898773,-27.321614804199694,47.88456307415333,-28.609387500690012," +
                        "-33.10203094957127,-28.752473355855603}");
    }
}
