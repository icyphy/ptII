// Simple test model for testing for leaks in Manager.
/*
   Copyright (c) 2017-2018 The Regents of the University of California.
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

package ptolemy.actor.test;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Ramp;
import ptolemy.actor.lib.Recorder;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;

/** Simple test for leaks in Manager.
 *
 *  See https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/MemoryLeaks
 *
 *  See https://projects.ecoinformatics.org/ecoinfo/issues/7190
 *
 *  @author Christpher Brooks, based on leak.java by Daniel Crawl
 *  @version $Id$
 */
public class ManagerLeak {

    public void go() throws Throwable {
        TypedCompositeActor top = new TypedCompositeActor();
        Manager manager = new Manager();
        SDFDirector director = new SDFDirector();
        top.setDirector(director);
        top.setName("top");
        top.setManager(manager);

        Parameter iterations = (Parameter) director.getAttribute("iterations");
        iterations.setToken(new IntToken(3));

        Ramp ramp = new Ramp(top, "ramp");
        Recorder recorder = new Recorder(top, "recorder");
        top.connect(ramp.output, recorder.input);

        manager.run();

        manager.execute();

        manager.run();

        top.setManager(null);
        top.setContainer(null);

        manager = null;
    }

    public static void main(String[] args) {
        try {
            ManagerLeak managerLeak = new ManagerLeak();
            managerLeak.go();
            System.gc();

            System.out.println("Sleeping...");
            Thread.sleep(100000L);
        } catch (Throwable t) {
            System.err.println("caught: " + t);
        }
    }

}
