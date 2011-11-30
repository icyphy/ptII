/* Ptides port.

@Copyright (c) 2008-2011 The Regents of the University of California.
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



package ptolemy.domains.ptides.lib.io;

import java.awt.Shape;
import java.util.List;

import diva.util.java2d.Polygon2D;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.ShapeIcon;

/**
 *  This abstract class implements common functionality for ptides
 *  ports such as sensors, actuators and network ports.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public abstract class PtidesPort extends TypedIOPort {

    /** Create a new PtidesPort with a given container and a name.
     * @param container The container of the port. 
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public PtidesPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _iconDescription = new ShapeIcon(this, "_iconDescription"); 
        
        Shape shape;
        Polygon2D.Double polygon = new Polygon2D.Double();  
        polygon.moveTo(-8, 4); 
        List<Integer[]> list = getCoordinatesForShape();
        for (int i = 0; i < list.size(); i++) {
            polygon.lineTo(list.get(i)[0], list.get(i)[1]);  
        }      
        polygon.closePath();
        shape = polygon;     
        ((ShapeIcon)_iconDescription).setShape(shape); 
    }
    
    /** Return coordinates for the port icon. */
    public abstract List<Integer[]> getCoordinatesForShape();
    
    /** IconDescription parameter used for representation in the library */
    public Attribute _iconDescription;
    
    /** Override this method with an empty implementation to allow for
     *  adding this port to the library.
     */
    protected void _checkContainer(Entity container)
        throws IllegalActionException {
        
        //if (!(container instanceof TypedActor) && (container != null)) {
        //    throw new IllegalActionException(container, this,
        //            "TypedIOPort can only be contained by objects "
        //                    + "implementing the TypedActor interface.");
        //}
    }
}
