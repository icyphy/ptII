package ptolemy.lang.java;

import java.util.LinkedList;
import java.util.ListIterator;

import ptolemy.lang.*;

public class ResolveClassVisitor extends ResolveVisitorBase {
    public ResolveClassVisitor() {
        super();
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _pkgDecl = (PackageDecl) node.getDefinedProperty("thePackage");

        _initLazyFlag(node);

        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // enclosing class decl
        childArgs.add(NullValue.instance); // enclosing class environ

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());
        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {

        if (_isSkippable(node)) return null;

        ClassDecl me = (ClassDecl) JavaDecl.getDecl(node);

        TreeNode superClass = node.getSuperClass();

        ClassDecl superDecl =
         (superClass == AbsentTreeNode.instance) ?
         StaticResolution.OBJECT_DECL :
         (ClassDecl) JavaDecl.getDecl((NamedNode) superClass);

        if (superDecl.category != JavaDecl.CG_CLASS) {
           ApplicationUtility.error("class " + node.getName().getIdent() +
            " cannot extend interface " + superDecl.getName()); 
        }

        node.setSuperClass(superDecl.getDefType());
        me.setSuperClass(superDecl);

        // initialize the implements list.
        // FIXME : what about the interfaces implemented by the base class, and
        // the interfaces in the list?

        LinkedList declInterfaceList = new LinkedList();

        ListIterator interfaceItr = node.getInterfaces().listIterator();

        while (interfaceItr.hasNext()) {
            ClassDecl intf = (ClassDecl) JavaDecl.getDecl((NamedNode) interfaceItr.next());

            if (intf.category != JavaDecl.CG_INTERFACE) {
               ApplicationUtility.error("class " + node.getName().getIdent() +
                " cannot implement class " + intf.getName());
            }
            declInterfaceList.addLast(intf);
        }

        me.setInterfaces(declInterfaceList);

        // add this declaration to outer class's environment, if applicable
        _addToEnclosingClassEnviron(args.get(1), me);

        Environ myEnviron = (Environ) me.getDefinedProperty("environ");

        // have members add themselves to this class's environment
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(myEnviron);

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        // add a default constructor if necessary

        if (myEnviron.lookupProper(me.getName(), JavaDecl.CG_CONSTRUCTOR) ==
            null) {
           ConstructorDeclNode defConstructor =
            _makeDefaultConstructor(node);

           node.getMembers().addFirst(defConstructor);

           defConstructor.accept(this, childArgs);
        }

        return null;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        if (_isSkippable(node)) return null;

        int modifiers = node.getModifiers();

        String nameString = node.getName().getIdent();

        // Leftover from Titanium. Why??
        // dtype()->resolveClass(package, cclass, fileEnv);

        Environ encEnviron = (Environ) args.get(1);

        Decl d = encEnviron.lookupProper(nameString, JavaDecl.CG_FIELD);

        if (d != null) {
           ApplicationUtility.error("redeclaration of " + d.getName());
           return null;
        }

        d = new FieldDecl(nameString, node.getDtype(), modifiers,
            node, (ClassDecl) args.get(0));

        _addToEnclosingClassEnviron(encEnviron, d);

        node.getName().setProperty("decl", d);

        // now that we have anonymous classes, we should visit the init
        // expression
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(NullTypeNode.instance); // enclosing class decl
        childArgs.addLast(NullTypeNode.instance); // enclosing class environ
        node.getInitExpr().accept(this, childArgs);

        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        if (_isSkippable(node)) return null;

        ClassDecl me = (ClassDecl) JavaDecl.getDecl(node);

        // initialize the implements list.
        // FIXME : what about the interfaces implemented by the interfaces in the list?

        LinkedList declInterfaceList = new LinkedList();

        ListIterator interfaceItr = node.getInterfaces().listIterator();

        while (interfaceItr.hasNext()) {
            ClassDecl intf = (ClassDecl) JavaDecl.getDecl((NamedNode) interfaceItr.next());

            if (intf.category != JavaDecl.CG_INTERFACE) {
               ApplicationUtility.error("class " + node.getName().getIdent() +
                " cannot implement class " + intf.getName());
            }
            declInterfaceList.addLast(intf);
        }

        me.setInterfaces(declInterfaceList);

        // add this declaration to outer class's environment, if applicable
        _addToEnclosingClassEnviron(args.get(1), me);

        Environ myEnviron = (Environ) me.getDefinedProperty("environ");

        // have members add themselves to this class's environment
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(myEnviron);

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        // Set implied modifiers of interface members
        ListIterator declItr = myEnviron.allProperDecls();

        while (declItr.hasNext()) {
            JavaDecl decl = (JavaDecl) declItr.next();

            int modifiers = decl.getModifiers();

            switch (decl.category) {

            case JavaDecl.CG_METHOD:
            modifiers |= (Modifier.PUBLIC_MOD | Modifier.ABSTRACT_MOD);
            break;

            case JavaDecl.CG_FIELD:
            case JavaDecl.CG_CLASS:
            modifiers |=
             (Modifier.PUBLIC_MOD | Modifier.FINAL_MOD | Modifier.STATIC_MOD);
            break;

            case JavaDecl.CG_INTERFACE:
            modifiers |= Modifier.STATIC_MOD;
            break;

            }
            decl.setModifiers(modifiers);
        }

        return null;
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return null;
    }

    /** The default visit method. Visits all child nodes with no enclosing
     *  class declaration.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // no enclosing class decl
        childArgs.add(NullValue.instance); // no enclosing class environ
        TNLManip.traverseList(this, node, args, node.children());
        return null;
    }

    protected void _addToEnclosingClassEnviron(Object encClassEnvironObject,
     Decl decl) {
        if (encClassEnvironObject != NullValue.instance) {
           // this is an inner class, add to outer class's environment
           Environ encClassEnviron = (Environ) encClassEnvironObject;
           encClassEnviron.add(decl);
        }
    }

    /** Return  a default constructor for the class declared by ClassDeclNode,
     *  as it would be produced by the parser, had it been written
     *  explicitly:  [public] Foo() { super(); }
     */
    public static ConstructorDeclNode _makeDefaultConstructor(ClassDeclNode cl) {
        return new ConstructorDeclNode(cl.getModifiers() & Modifier.PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, cl.getName().getIdent()),
         new LinkedList(), new LinkedList(),
         new BlockNode(new LinkedList()),
         new SuperConstructorCallNode(new LinkedList()));
    }

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;
}
