/* A class that that stores InvokeGraph-based information to a disk file.

 Copyright (c) 2003 The University of Maryland.
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

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.invoke.InvokeGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// InvokeGraphCache
/**
A class that that stores InvokeGraph-based information to a disk file. It
stores a mapping between method signatures and signatures of their targets.
This class needs to be used because InvokeGraphs are not serializable, and
so cannot be stored to disk.

@author Ankush Varma
@version $Id$
*/
public class InvokeGraphCache{


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Returns a LinkedList containing the targets of a given method, as
     * stored in the cache.
     * @param method The method whose targets are needed.
     * @return The targets of this method.
     */
    public LinkedList getTargetsOf(SootMethod method) {
        LinkedList targets = new LinkedList();
        LinkedList methodNameList = (LinkedList)_data.map
            .get(method.getSignature());
        Iterator methodNames =  methodNameList.iterator();

        while (methodNames.hasNext()) {
            String methodName = (String) methodNames.next();
            SootMethod target = Scene.v().getMethod(methodName);
            targets.add(target);
        }
        return targets;
    }



    /** Checks whether methods from the given class are in the cache.
     * @param source The class to be checked.
     * @return True if methods from this class are in the cache.
     */
    public boolean isCached(SootClass source) {
        if (_data.classSet.contains(source.getName())) {
            return true;
        }
        else {
            return false;
        }
    }

    /** Checks whether the targets of a given method are cached.
     * @param method The method to be looked up.
     * @return True if its targets are in the cache..
     */
    public boolean isCached(SootMethod method) {
        if (_data.map.containsKey(method.getSignature())) {
            return true;
        }
        else {
            return false;
        }
    }


    /** Checks whether the cache has already been generated.
     * @return True if the cache file already exists.
     */
    public boolean isPrecomputed() {
        return FileHandler.exists(_cacheFileName);
    }

    /** Load data from the cache file.
     */
    public void load() {
        _data = (CacheData)FileHandler.readObject(_cacheFileName);
    }

    /** Stores a mapping between method signatures and the signatures of
     * their target methods for all methods in the given invokeGraph.
     * @param invokeGraph The invokeGraph whose data is to be stored.
     */
    public void store(InvokeGraph invokeGraph) {
        CacheData data = _generateData(invokeGraph);
        FileHandler.write(_cacheFileName, data);
    }



    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generates the data to be written from the given InvokeGraph.
     */
    private CacheData _generateData(InvokeGraph invokeGraph) {
        HashMap map = new HashMap();
        HashSet classes = new HashSet();
        CacheData data = new CacheData();

        LinkedList targetSignatures;

        Iterator methods = invokeGraph.getReachableMethods().iterator();
        while (methods.hasNext()) {
            SootMethod method = (SootMethod)methods.next();
            classes.add(method.getDeclaringClass().getName());
            Iterator targets = invokeGraph.getTargetsOf(method).iterator();
            targetSignatures = new LinkedList();

            while (targets.hasNext()) {
                SootMethod target = (SootMethod)targets.next();
                targetSignatures.add(target.getSignature());
            }

            map.put(method.getSignature(), targetSignatures);
        }

        data.map = map;
        data.classSet = classes;
        _data = data;
        return data;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    /** The name of the file to be used as a cache.*/
    private String _cacheFileName = "invoke.cache";


    /** The actual data read from/written to the file. */
    private CacheData _data;

}
