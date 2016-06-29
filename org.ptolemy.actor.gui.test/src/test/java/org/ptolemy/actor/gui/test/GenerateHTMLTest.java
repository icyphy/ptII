/* Test the GenerateHTML class, which provides us with a list of copyrights

 Copyright (c) 2015 The Regents of the University of California; iSencia Belgium NV.

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
package org.ptolemy.actor.gui.test;

import junit.framework.TestCase;
import ptolemy.actor.gui.GenerateCopyrights;

/**
 * Invoke generateHTML(null) so that we can see the licenses.
 *
 * @author Christopher Brooks, based on org.ptolemy.core.test.ModelDefinitionTest by ErwinDL
 * @version $Id$
 * @since Ptolemy II 10.1
 * @Pt.ProposedRating Red (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class GenerateHTMLTest extends TestCase {

  /**
   * Invoke the generateHTML() method with a null Configuration, which
   * prints out the packages that have been registered in
   * $PTII/ptolemy/actor/lib/gui/GenerateHTML.java.  Note that the
   * links probably won't work.
   */
  public void testGenerateHTML()  {
      System.out.println(GenerateCopyrights.generateHTML(null));
  }
}
