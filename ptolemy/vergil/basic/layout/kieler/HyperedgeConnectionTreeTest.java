/*
@Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.vergil.basic.layout.kieler;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KLayoutDataFactory;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;

public class HyperedgeConnectionTreeTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        float[] p1 = { 1 };
        KEdge e1 = edge(p1);
        float[] p2 = { 1, 2 };
        KEdge e2 = edge(p2);
        float[] p3 = { 1, 3 };
        KEdge e3 = edge(p3);
        float[] p4 = { 1 };
        KEdge e4 = edge(p4);
        float[] p5 = { 1, 3 };
        KEdge e5 = edge(p5);

        HyperedgeConnectionTree tree = new HyperedgeConnectionTree();
        tree.addEdge(e1);
        tree.addEdge(e2);
        tree.addEdge(e3);
        tree.addEdge(e4);
        tree.addEdge(e5);
        System.out.println("Tree: " + tree.toString());
    }

    static KPoint point(float x, float y) {
        KPoint p = KLayoutDataFactory.eINSTANCE.createKPoint();
        p.setX(x);
        p.setY(y);
        return p;
    }

    static KPoint p(float i) {
        return point(i, i);
    }

    static void bendpoint(KEdge e, KPoint p) {
        KEdgeLayout l = KimlLayoutUtil.getEdgeLayout(e);
        l.getBendPoints().add(p);
    }

    static KEdge edge(float[] pa) {
        KEdge e = KimlLayoutUtil.createInitializedEdge();
        for (int i = 0; i < pa.length; i++) {
            bendpoint(e, p(pa[i]));
        }
        return e;
    }
}
