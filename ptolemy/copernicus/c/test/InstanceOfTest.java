/* A simple class for testing that the instanceof operator works.

Copyright (c) 2001-2003 The University of Maryland
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// InstanceOfTest
/**

 A simple class for testing that the instanceof operator works.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0

*/

public class  InstanceOfTest{

    public static void main(String args[]) {
        LinkedList list = new LinkedList();
        Object object = list;
        System.out.println(object instanceof LinkedList);
        System.out.println(object instanceof AbstractSequentialList);
        System.out.println(object instanceof List);
        System.out.println(object instanceof Collection);
        System.out.println(object instanceof Object);
        System.out.println(object instanceof HashSet);
        System.out.println(object instanceof Iterator);
    }
}
