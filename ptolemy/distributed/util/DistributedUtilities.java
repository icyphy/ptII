/* Utilities for the distributed package.

@Copyright (c) 2005 The Regents of Aalborg University.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

*/

package ptolemy.distributed.util;

import java.util.LinkedList;

import ptolemy.actor.Receiver;
import ptolemy.distributed.domains.sdf.kernel.DistributedSDFReceiver;

//////////////////////////////////////////////////////////////////////////
//// DistributedUtilities

/**
Utilities for the distributed package. This includes different conversions
and printing facilities.

@author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
@version
@since
@Pt.ProposedRating Red (kapokasa)
@Pt.AcceptedRating
*/

public class DistributedUtilities {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Converts a bidimensional array of receivers into a bidimensional array
     *  containing only the integer IDs of the receivers.
     *
     *  @param receivers A bidimensional array of receivers.
     *  @return Integer[][] A bidimensional array of receivers IDs.
     */

    public static Integer[][] convertReceiversToIntegers(Receiver[][] receivers) {
        Integer[][] receiversIntegers = new Integer[receivers.length][];
        for (int i = 0; i < receivers.length; i++) {
            receiversIntegers[i] = new Integer[receivers[i].length];
            for (int j = 0; j < receivers[i].length; j++) {
                if (receivers[i][j] != null) {
                    receiversIntegers[i][j] = ((DistributedSDFReceiver)receivers[i][j]).getID();
                } else {
                    receiversIntegers[i][j] = null;
                }
            }
        }
        return receiversIntegers;
    }

    /** Converts a bidimensional array of Integer into a linked list of Integers.
     *
     *  @param receiversIntegers A bidimensional array of Integers.
     *  @return LinkedList A list.
     */

    public static LinkedList convertIntegersToList(Integer[][] receiversIntegers) {
//        System.out.println("convertIntegersToList: \n" + integersArrayToString(receiversIntegers));
        LinkedList list = new LinkedList();
        for (int i = 0; i < receiversIntegers.length; i++) {
            for (int j = 0; j < receiversIntegers[i].length; j++) {
                if (receiversIntegers[i][j] != null) {
                    list.add(receiversIntegers[i][j]);
                }
            }
        }
        return list;
    }

    /** Converts a bidimensional array of Receivers into a String for printing purposes.
     *
     *  @param array A bidimensional array of Receiver.
     *  @return String A string.
     */

    public static String receiversArrayToString(Receiver[][] array) {
        if (array == null) {
            return "null";
        }
        String print = "";
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                print += ((DistributedSDFReceiver)array[i][j]).getID() + " ";
            }
            print += "\n";
        }
        return print;
    }

    /** Converts a bidimensional array of Integer a String for printing purposes.
     *
     *  @param array A bidimensional array of Integer.
     *  @return String A string.
     */

    public static String integersArrayToString(Integer[][] array) {
        if (array == null) {
            return "null";
        }
        String print = "";
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                print += array[i][j] + " ";
            }
            print += "\n";
        }
        return print;
    }

}

/*
public static Receiver[][] convertIntegersToReceivers(Integer[][] receiversIntegers) {
    System.out.println("convertIntegersToReceivers: \n" + integersArrayToString(receiversIntegers));
    Receiver[][] receivers = new Receiver[receiversIntegers.length][];
    for (int i = 0; i < receiversIntegers.length; i++) {
        receivers[i] = new Receiver[receiversIntegers[i].length];
        for (int j = 0; j < receivers[i].length; j++) {
            if (receiversIntegers[i][j] != null) {
                System.out.println("receiversIntegers[" + i + "][" + j + "] = " + receiversIntegers[i][j]);
                receivers[i][j] = new DistributedSDFReceiver(receiversIntegers[i][j]);
            }
        }
    }
    return receivers;
}
*/
