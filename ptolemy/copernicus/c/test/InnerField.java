/*
A simple program to test if inner classes can access public, protected and
private fields of outer classes.

Copyright (c) 2001-2003 The University of Maryland.
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

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ankush@eng.umd.edu)
*/


/// NestedExceptions
/***
A simple program to exceptions across methods.

@author Ankush Varma
@version $Id$
*/

// This is the outer class.
public class InnerField {
    public int publicField;
    protected int _protectedField;
    private int _privateField;

    public static void main(String[] args) {
        InnerField outer = new InnerField();
        outer.publicField = 0;
        outer._protectedField = 1;
        outer._privateField = 2;
        Inner a = outer.new Inner();
        a.checkAccess();
    }

    // This is the inner class.
    class Inner {
        public void checkAccess() {
            System.out.println(publicField);
            System.out.println(_protectedField);
            System.out.println(_privateField);
        }
    }

}





