/* Modal models.

 Copyright (c) 1999-2002 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.actor.*;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.Recorder;
import ptolemy.actor.lib.io.ExpressionWriter;
import ptolemy.actor.gui.*;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.HSDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.*;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.*;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.io.*;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// MobileModel
/**
This actor extends the TypedCompositeActor. It contains another model
that is defined as a Ptolemy composite actor to its inputs. Rather than
specified before executing, the inside model can be dynamically changed
either locally or remotely. Currently, the model that dynamically applied
to this actor is specified by a URL string.
//FIXME: should factor the parser part out to another actor that takes
//a moml string or URL string and output an ActorToken. and have the
//second input take an actor token...
@author Yang Zhao
@version $Id:
*/
public class MobileModel extends TypedCompositeActor{

    /** Construct a model manager in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public MobileModel (Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        input = new TypedIOPort(this, "input", true, false);
        modelString = new TypedIOPort(this, "modelString", true, false);
        modelString.setTypeEquals(BaseType.STRING);
        output = new TypedIOPort(this, "output", false, true);
        new ptolemy.domains.de.kernel.DEDirector(this, "director");
        getMoMLInfo().className = "ptolemy.actor.MobileModel";
        //TypedIORelation r0 = new TypedIORelation(this, "" + getName() + "_r1");
        connect(input, output);
    }

    /** Construct a model manager with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MobileModel (CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        modelString = new TypedIOPort(this, "modelString", true, false);
        modelString.setTypeEquals(BaseType.STRING);
        output = new TypedIOPort(this, "output", false, true);
        new ptolemy.domains.de.kernel.DEDirector(this, "director");
        getMoMLInfo().className = "ptolemy.actor.MobileModel";
        getMoMLInfo().className = "ptolemy.actor.MobileModel";
        //TypedIORelation r0 = new TypedIORelation(this, "r1");
        connect(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    //public Parameter modelURL;

    public TypedIOPort input, modelString, output;

    public MoMLParser parser;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

     /** Initialize this actor. create a new moml parser for passing
      * the applied model to it.
     *  @exception IllegalActionException If there is no director, or
     *  if the director's initialize() method throws it, or if the
     *  actor is not opaque.
     */
     public void initialize() throws IllegalActionException {
         if (_debugging) {
            _debug("Invoking init");
        }
        _model = null;

        try {
            parser = new MoMLParser();
            parser.setMoMLFilters(BackwardCompatibility.allFilters());
            parser.addMoMLFilter(new RemoveGraphicalClasses());
        }catch (Exception ex) {
            throw new IllegalActionException(this, ex.getMessage());
        }

        super.initialize();
    }

    /** save the model here if there is a new model to apply. and then call
     * super.fire().
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     *
     */
    public void fire() throws IllegalActionException  {
        if (_debugging) {
            _debug("Invoking fire");
        }
        if (modelString.hasToken(0)) {
            try {
                StringToken str = (StringToken) modelString.get(0);
                URL url = new URL(str.stringValue());
                _model = (CompositeActor) parser.parse(null, url.openStream());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalActionException(this, ex.getMessage());
            }
        }
        super.fire();
    }

    /** return true if the actor either of its input port has token.
     *  @exception IllegalActionException should never be throwed
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking prefire");
        }
        if (input.hasToken(0) || modelString.hasToken(0)) {
            return true;
        }
        return false;
    }

    /** update the model here to achieve consistency.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's postfire() method throws it, or if this actor
     *   is not opaque.
     */
    public boolean postfire() throws IllegalActionException {
        if (!_stopRequested && _model != null) {
            //remove the old model inside first, if there is one.
            String delete = _requestToRemoveAll(this);
            MoMLChangeRequest removeRequest = new MoMLChangeRequest(
                           this,            // originator
                           this,            // context
                           delete,          // MoML code
                           null);
            requestChange(removeRequest);
            //add the entity represented by the new model string.
            //Fixme: the reason I do the change by two change request is because
            //when I tried to group them in one, I got a parser error...
            MoMLChangeRequest request = new MoMLChangeRequest(
                               this,            // originator
                               this,          // context
                               _model.exportMoML(), // MoML code
                               null);
            requestChange(request);
            //connect the model.
            StringBuffer moml = new StringBuffer("<group>");
            moml.append("<relation name=\"newR1\" class=\"ptolemy.actor.TypedIORelation\">");
            moml.append("</relation>");
            moml.append("<relation name=\"newR2\" class=\"ptolemy.actor.TypedIORelation\">");
            moml.append("</relation>");
            moml.append("<link port=\"input\" relation=\"newR1\"/>");
            moml.append("<link port=\"" + _model.getName() + ".input\" relation=\"newR1\"/>");
            moml.append("<link port=\"" + _model.getName() + ".output\" relation=\"newR2\"/>");
            moml.append("<link port=\"output\" relation=\"newR2\"/>");
            moml.append("</group>");
            MoMLChangeRequest request2 = new MoMLChangeRequest(
                                this,            // originator
                                this,          // context
                                moml.toString(), // MoML code
                                null);
            requestChange(request2);
            if (_debugging) {
                _debug("issues change request to modify the model");
            }
            _model = null;
        }
        return super.postfire();
    }
    /**return true.
     *
     */
    public boolean isOpaque() {
        return true;
    }
    /** clean up tha changes that have been made.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's wrapup() method throws it, or if this
     *   actor is not opaque.
     */
    public void wrapup() throws IllegalActionException {
        //clean the inside content.
        String delete = _requestToRemoveAll(this);
        MoMLChangeRequest removeRequest = new MoMLChangeRequest(
                               this,            // originator
                               this,            // context
                               delete,          // MoML code
                               null);
        requestChange(removeRequest);
        super.wrapup();
    }

    /** export moml description.
     *
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
            Iterator attributes = attributeList().iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();
                attribute.exportMoML(output, depth);
            }
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                port.exportMoML(output, depth);
            }

        // Next write the links.
        output.write(exportLinks(depth, null));
    }

   ///////////////////////////////////////////////////////////////////
   ////                         private methods                 ////
    /** construct a modl string for the composite actor to delete
     * of its entities and relations.
     *  @param actor The composite actor.
     */
    private String _requestToRemoveAll (CompositeActor actor) {
        StringBuffer delete = new StringBuffer("<group>");
        Iterator entities = actor.entityList().iterator();
        while (entities.hasNext()) {
            Entity entity = (Entity) entities.next();
            delete.append("<deleteEntity name=\"" + entity.getName() + "\" class=\"" + entity.getClass().getName() + "\"/>");
            //System.out.println("the name of the entity is: " + entity.getName());
        }
        Iterator relations = actor.relationList().iterator();
        while (relations.hasNext()) {
            IORelation relation = (IORelation) relations.next();
            delete.append("<deleteRelation name=\"" + relation.getName() + "\" class=\"ptolemy.actor.TypedIORelation\"/>");
            //System.out.println("the name of the relations is: " + relation.getName());
        }
        delete.append("</group>");
        return delete.toString();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //the modle that dynamically defined and contained by this.
    private CompositeActor _model;

}
