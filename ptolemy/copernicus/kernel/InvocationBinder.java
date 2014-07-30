/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam, Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
/*
 * Modified by the Sable Research Group and others 1997-2003.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */
/* Reference Version: $SootVersion: 1.2.3.dev.4 $ */

/* This class is probably based on soot/jimple/toolkits/invoke/StaticMethodBinder.java, which
 * is why it is LGPL'd.  See
 * http://www.java2s.com/Open-Source/Java-Document/Code-Analyzer/soot/soot/jimple/toolkits/invoke/StaticMethodBinder.java.htm
 */

package ptolemy.copernicus.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.InstanceInvokeEdgesPred;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.invoke.AccessManager;

/** Uses the Scene's currently-active InvokeGraph to statically bind monomorphic call sites. */
public class InvocationBinder extends SceneTransformer {
    private static InvocationBinder instance = new InvocationBinder();

    private InvocationBinder() {
    }

    public static InvocationBinder v() {
        return instance;
    }

    public String getDefaultOptions() {
        return "insert-null-checks insert-redundant-casts allowed-modifier-changes:unsafe VTA-passes:0";
    }

    public String getDeclaredOptions() {
        return "insert-null-checks insert-redundant-casts allowed-modifier-changes VTA-passes";
    }

    @Override
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InvocationBinder.internalTransform(" + phaseName
                + ", " + options + ")");

        Filter instanceInvokesFilter = new Filter(new InstanceInvokeEdgesPred());
        String modifierOptions = "unsafe";
        //HashMap instanceToStaticMap = new HashMap();

        Scene.v().setCallGraph(new CallGraph());
        CallGraph cg = Scene.v().getCallGraph();

        Iterator classesIt = Scene.v().getApplicationClasses().iterator();

        while (classesIt.hasNext()) {
            SootClass c = (SootClass) classesIt.next();

            LinkedList methodsList = new LinkedList();
            methodsList.addAll(c.getMethods());

            while (!methodsList.isEmpty()) {
                SootMethod container = (SootMethod) methodsList.removeFirst();

                if (!container.isConcrete()) {
                    // System.out.println("skipping " + container + ": not concrete");
                    continue;
                }

                if (!instanceInvokesFilter.wrap(cg.edgesOutOf(container))
                        .hasNext()) {
                    continue;
                }

                JimpleBody b = (JimpleBody) container.getActiveBody();

                List unitList = new ArrayList();
                unitList.addAll(b.getUnits());

                Iterator unitIt = unitList.iterator();

                while (unitIt.hasNext()) {
                    Stmt s = (Stmt) unitIt.next();

                    if (!s.containsInvokeExpr()) {
                        continue;
                    }

                    InvokeExpr ie = s.getInvokeExpr();

                    if (ie instanceof StaticInvokeExpr
                            || ie instanceof SpecialInvokeExpr) {
                        // System.out.println("skipping " + container + ":" +
                        //        s + ": not virtual");
                        continue;
                    }

                    Iterator targets = new Targets(cg.edgesOutOf(s));

                    if (!targets.hasNext()) {
                        continue;
                    }

                    SootMethod target = (SootMethod) targets.next();

                    if (targets.hasNext()) {
                        continue;
                    }

                    // Ok, we have an Interface or VirtualInvoke going to 1.
                    if (!AccessManager.ensureAccess(container, target,
                            modifierOptions)) {
                        // System.out.println("skipping: no access");
                        continue;
                    }

                    if (!target.isConcrete()) {
                        //  System.out.println("skipping: not concrete");
                        continue;
                    }

                    // HACK! because the callgraph seems to be
                    // incorrect in soot 2.0.1
                    if (!Scene.v().getApplicationClasses()
                            .contains(target.getDeclaringClass())) {
                        continue;
                    }

                    // Change the InterfaceInvoke or VirtualInvoke to
                    // a new VirtualInvoke.
                    ValueBox box = s.getInvokeExprBox();
                    box.setValue(Jimple.v().newVirtualInvokeExpr(
                            (Local) ((InstanceInvokeExpr) ie).getBase(),
                            target.makeRef(), ie.getArgs()));
                }
            }
        }
    }
}
