/* 

Copyright (c) 2005-2006 The Regents of the University of California.
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

package ptolemy.backtrack.manual.ptolemy.actor.lib;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// Backtrack
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Backtrack extends TypedAtomicActor {
    public Backtrack(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        moveToFirst();
    }
    
    public void preinitialize() throws IllegalActionException {
        try {
            _replaceEntities();
        } catch (IllegalActionException e1) {
            throw e1;
        } catch (Exception e2) {
            throw new IllegalActionException(e2.getMessage());
        }
        super.initialize();
    }
    
    /*protected void _replaceEntities() throws Exception {
        CompositeEntity container = (CompositeEntity)getContainer();
        StringWriter moml = new StringWriter();
        container.toplevel().exportMoML(moml, 0);
        
        MoMLParser parser = new MoMLParser();
        RenameClassMoMLFilter filter = new RenameClassMoMLFilter();
        parser.addMoMLFilter(filter);
        NamedObj topLevel = parser.parse(moml.toString());
        
        parser.getMoMLFilters().remove(filter);
    }*/

    protected void _replaceEntities() throws NameDuplicationException,
            IllegalActionException {
        final String backtrackPrefix = "ptolemy.backtrack.automatic"; 
        
        CompositeEntity container = (CompositeEntity)getContainer();
        Iterator siblings = container.containedObjectsIterator();
        while (siblings.hasNext()) {
            NamedObj sibling = (NamedObj)siblings.next();
            if (sibling instanceof Entity) {
                Entity entity = (Entity)sibling;
                String oldClassName = entity.getClassName();
                String newClassName = backtrackPrefix + "." + oldClassName;
                if (_classExists(newClassName)) {
                    _changeEntityClass(entity, oldClassName, newClassName);
                }
            }
        }
    }
    
    protected void _changeEntityClass(Entity entity, String oldClassName,
            String newClassName) {
        CompositeEntity container = (CompositeEntity)getContainer();
        
        try {
            StringWriter moml = new StringWriter();
            moml.write("<group name=\"auto\">");
            entity.exportMoML(moml, 0);
            moml.write("</group>");
            
            List relations = entity.linkedRelationList();
            Iterator relationsIterator = relations.iterator();
            while (relationsIterator.hasNext()) {
                TypedIORelation relation =
                    (TypedIORelation)relationsIterator.next();
            }
            
            String momlString = moml.toString().replace(
                    "\"" + oldClassName + "\"",
                    "\"" + newClassName + "\"");

            MoMLChangeRequest change =
                new MoMLChangeRequest(this, container, momlString) {
                // Override this method to offset the locations of pasted objects.
                protected void _postParse(MoMLParser parser) {
                    Iterator topObjects = parser.topObjectsCreated().iterator();
                    while (topObjects.hasNext()) {
                        NamedObj topObject = (NamedObj)topObjects.next();
                        try {
                            Iterator locations = topObject.attributeList(Locatable.class).iterator();
                            while (locations.hasNext()) {
                                Locatable location = (Locatable)locations.next();
                                double[] locationValue = location.getLocation();
                                for (int i = 0; i < locationValue.length; i++) {
                                    locationValue[i] += 10;
                                }
                                location.setLocation(locationValue);
                            }
                        } catch (IllegalActionException e) {
                            MessageHandler.error("Paste failed", e);
                        }
                    }
                    parser.clearTopObjectsList();
                }

                // Override this method to clear the list of top objects.
                protected void _preParse(MoMLParser parser) {
                    super._preParse(parser);
                    parser.clearTopObjectsList();
                }
            };
             
            change.setUndoable(true);
            container.requestChange(change);
        } catch (Exception ex) {
            MessageHandler.error("Paste failed", ex);
        }
    }
    
    private boolean _classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
