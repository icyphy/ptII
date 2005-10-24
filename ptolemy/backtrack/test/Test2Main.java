/*

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.test;

import ptolemy.backtrack.test.ptolemy.backtrack.test.test2.Test2;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Test2Main

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Test2Main {
    /**
     *  @param args
     */
    public static void main(String[] args) {
        for (int i = 2; i <= 8; i++) {
            long handle = _modify(i);
            _test2.$GET$CHECKPOINT().rollback(handle, true);
            _print(i);
        }

        System.out.println();
    }

    private static long _modify(int index) {
        long handle = -1;
        _test2.setT(_record(new Test2[10]));

        if (index == 1) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print((_objects.size() - 1) + " ");
        }

        _test2.setT(1, _record(new Test2()));

        if (index == 2) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print((_objects.size() - 1) + " ");
        }

        _test2.getT()[1].setI(_record(new Integer(10)));

        if (index == 3) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print(_objects.indexOf(new Integer(10)) + " ");
        }

        _test2.getT()[1].setI(_record(new Integer(20)));

        if (index == 4) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print(_objects.indexOf(new Integer(20)) + " ");
        }

        _test2.setT(_record(new Test2[1]));

        if (index == 5) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print((_objects.size() - 1) + " ");
        }

        _test2.setT(0, _record(new Test2()));

        if (index == 6) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print((_objects.size() - 1) + " ");
        }

        _test2.getT()[0].setI(_record(new Integer(30)));

        if (index == 7) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print(_objects.indexOf(new Integer(30)) + " ");
        }

        _test2.setT(0, _record((Test2) null));

        if (index == 8) {
            handle = _test2.$GET$CHECKPOINT().createCheckpoint();
            System.out.print(_objects.indexOf(null) + " ");
        }

        return handle;
    }

    private static void _print(int index) {
        if (index == 2) {
            System.out.print(_objects.indexOf(_test2.getT(1)));
        }

        if (index == 3) {
            System.out.print(_objects.indexOf(_test2.getT(1).getI()));
        }

        if (index == 4) {
            System.out.print(_objects.indexOf(_test2.getT(1).getI()));
        }

        if (index == 5) {
            System.out.print(_objects.indexOf(_test2.getT()));
        }

        if (index == 6) {
            System.out.print(_objects.indexOf(_test2.getT(0)));
        }

        if (index == 7) {
            System.out.print(_objects.indexOf(_test2.getT(0).getI()));
        }

        if (index == 8) {
            System.out.print(_objects.indexOf(_test2.getT(0)));
        }

        System.out.print(" ");
    }

    private static Integer _record(Integer i) {
        _objects.add(i);
        return i;
    }

    private static Test2 _record(Test2 t) {
        _objects.add(t);
        return t;
    }

    private static Test2[] _record(Test2[] t) {
        _objects.add(t);
        return t;
    }

    private static Test2 _test2 = new Test2();

    private static List _objects = new LinkedList();
}
