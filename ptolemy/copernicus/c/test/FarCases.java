/*
A simple program to test the switch construct with a variable that can take
well-separated values. This causes a LookupSwitch instead of a TableSwitch.

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


//// Cases
/** A simple program to test the switch construct with a variable that can take
    well-separated values. This causes a LookupSwitch instead of a TableSwitch.

    The output of the program would be:
    Today is Friday.

   @author Paul C. Yang, Ankush Varma
   @version $Id$
*/


public class FarCases {

    /** Outputs the appropriate string.
     *  @param args Application arguments.
     */
    public static void main(String[] args) {

        int choice = 5;

        // Selects an appropriate message.
        switch(choice) {
            case 4:
            System.out.println("Today is Thursday.");
            break;

            case 5:
            System.out.println("Today is Friday.");
            break;

            case 1729:
            System.out.println("Today is Saturday.");
            break;

            default:
            System.out.println("Invalid entry.");
        }
    }
}


