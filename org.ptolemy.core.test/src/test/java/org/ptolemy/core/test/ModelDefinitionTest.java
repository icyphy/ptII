/* A trivial example of how to test a model definition.

 Copyright (c) 2014 The Regents of the University of California; iSencia Belgium NV.

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
package org.ptolemy.core.test;

import java.net.URL;

import junit.framework.TestCase;

import org.ptolemy.testsupport.ModelDefinitionAssertion;

import com.microstar.xml.XmlException;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.Const;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

/**
 * A trivial example of a Junit {@link TestCase} to assert the contents of a defined model.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class ModelDefinitionTest extends TestCase {

  /**
   *
   * @throws NameDuplicationException when the model definition fails because elements are added with duplicate names
   * @throws IllegalActionException when the model definition fails for some other reason
   */
  public void testModelDefinition1() throws NameDuplicationException, IllegalActionException {
    CompositeActor model = new CompositeActor();
    new Const(model, "const");
    new ModelDefinitionAssertion().expectActor("const").assertModel(model);
  }

  /**
   * First test on the updated MomlParser
   * @throws Exception
   */
  public void testModelDefinitionFromMOML() throws Exception {
    MoMLParser parser = new MoMLParser();
    URL momlURL = getClass().getResource("/HelloConst.xml");
    NamedObj model = parser.parse(null, momlURL);
    new ModelDefinitionAssertion().expectActor("const").assertModel((CompositeActor) model);
  }

  public void testModelDefinitionFromMOMLWithVersion() throws Exception {
    MoMLParser parser = new MoMLParser();
    URL momlURL = getClass().getResource("/HelloConstWithVersion.xml");
    NamedObj model = parser.parse(null, momlURL);
    new ModelDefinitionAssertion().expectActor("const").assertModel((CompositeActor) model);
  }

  public void testModelDefinitionWithCustomActorFromMOMLWithVersion() throws Exception {
    MoMLParser parser = new MoMLParser();
    URL momlURL = getClass().getResource("/HelloMyConstWithVersion.xml");
    NamedObj model = parser.parse(null, momlURL);
    new ModelDefinitionAssertion().expectActor("const").assertModel((CompositeActor) model);
  }

  public void testMultiVertexModelDefinitionFromMOML() throws Exception {
    MoMLParser parser = new MoMLParser();
    URL momlURL = getClass().getResource("/MultiVertexModel.xml");
    CompositeActor model = (CompositeActor) parser.parse(null, momlURL);
    new ModelDefinitionAssertion().expectLink("Const.output", "relation").assertModel( model);
  }


  public void testModelDefinitionFromMOMLWithInvalidVersion() throws Exception {
    MoMLParser parser = new MoMLParser();
    URL momlURL = getClass().getResource("/HelloConstWithInvalidVersion.xml");
    try {
      NamedObj model = parser.parse(null, momlURL);
      new ModelDefinitionAssertion().expectActor("const").assertModel((CompositeActor) model);
      fail("Parser failed to see invalid version in moml");
    } catch (XmlException e) {
      // this is what we need for an invalid version spec in the moml!
      assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

}
