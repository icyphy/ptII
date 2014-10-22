/*
 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptserver.test.junit;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;

import org.junit.Test;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.injection.PtolemyModule;
import ptolemy.kernel.util.Attribute;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.util.ProxyModelBuilder;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.ServerUtility;

///////////////////////////////////////////////////////////////////
//// ServerTest

/**
 * ServerTest class.
 *
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ServerTest {

    static {
        // FIXME remove PTServerModule after SysOutActor is deleted
        // or create a proper initializer for it
        ArrayList<PtolemyModule> modules = new ArrayList<PtolemyModule>();
        modules.addAll(ActorModuleInitializer.getModules());
        modules.add(new PtolemyModule(ResourceBundle
                .getBundle("ptserver.util.PTServerModule")));
        PtolemyInjector.createInjector(modules);
    }

    @Test
    public void testServerGeneration() throws MalformedURLException,
            URISyntaxException, Exception {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        CompositeActor model = (CompositeActor) parser.parse(null,
                ServerTest.class.getResource("junitmodel.xml").toURI().toURL());
        CompositeActor layout = (CompositeActor) parser.parse(null,
                ServerTest.class.getResource("junitmodel_test.layout.xml")
                        .toURI().toURL());
        HashSet<String> remoteAttributes = new HashSet<String>();
        remoteAttributes.add(ServerUtility.REMOTE_OBJECT_TAG);
        ServerUtility.mergeModelWithLayout(model, layout,
                new HashSet<Class<? extends Attribute>>(), remoteAttributes);
        ProxyModelBuilder builder = new ProxyModelBuilder(
                ProxyModelType.SERVER, model);
        builder.build();
        System.out.println(model.exportMoML());
    }
}
