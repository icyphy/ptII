package ptolemy.lang;

import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

class GenerateVisitor {

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
       System.out.println("Usage : GenerateVisitor pkgName typeNameListFile [visitor class name] [base node name]");
    }

    File fsrc  = new File(args[1]);

    String visitorClassName;
    String visitorOutFile;
    if (args.length < 3) {
       visitorClassName = "Visitor";
       visitorOutFile = "Visitor.java";
    } else {
       visitorClassName = args[2];
       visitorOutFile = visitorClassName + ".java";
    }

    String baseNodeName;
    if (args.length < 4) {
       baseNodeName = "TreeNode";
    } else {
       baseNodeName = args[3];
    }

    File fdest = new File(visitorOutFile);

    if (!fdest.createNewFile()) {
       fdest.delete();
       fdest = new File(visitorOutFile);
       fdest.createNewFile();
    }

    FileWriter ofs;
    LineNumberReader ifs;

    try {
      ofs = new FileWriter(fdest);
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't open destination file.");
      return;
    }

    try {
      ifs = new LineNumberReader(new FileReader(fsrc));
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't open input file.");
      return;
    }

    pkgName = args[0];

    ofs.write(copyrightStr);
    String s = "package " + pkgName + ";\n\n" +
               "import java.util.LinkedList;\n" +
               "import ptolemy.lang.TreeNode;\n" + // hack!!!
               "import ptolemy.lang.IVisitor;\n\n" +
               "class "+ visitorClassName + " implements IVisitor {\n" +
               "    public " + visitorClassName + "() {\n" +
               "        this(TM_CHILDREN_FIRST);\n" +
               "    }\n\n" +
               "    public " + visitorClassName + "(int traversalMethod) {\n" +
               "        if (traversalMethod > TM_CUSTOM) {\n" +
               "           throw new RuntimeException(\"Illegal traversal method\");\n" +
               "        }\n" +
               "        _traversalMethod = traversalMethod;\n" +
               "    }\n\n";
    ofs.write(s);

    LinkedList typeList = new LinkedList();
    LinkedList parentTypeList = new LinkedList();
    LinkedList concreteList = new LinkedList();
    LinkedList singletonList = new LinkedList();

    LinkedList methodListList = new LinkedList();

    StringTokenizer strTokenizer;
    String className;
    String marker;

    // get common header
    s = ifs.readLine();

    String header = "";

    if (s.equals("header")) {
       boolean endHeader = false;
       StringBuffer headerBuf = new StringBuffer();
       do {
         s = ifs.readLine();
         endHeader = s.equals("header");

         if (!endHeader) {
            headerBuf.append(s);
            headerBuf.append('\n');
         }
       } while (!endHeader);

       headerBuf.append('\n');
       header = headerBuf.toString();
       s = ifs.readLine();
    }

    if (s.startsWith("mplace")) {
       defaultPlacement = s.charAt(7);
       s = ifs.readLine();
    }

    // get class info
    do {
      if ((s != null) && (s.length() > 4) &&
          !(s.startsWith("//"))) {

         strTokenizer = new StringTokenizer(s);

         className = strTokenizer.nextToken();

         try {
            typeList.addLast(className);
         } catch (NullPointerException e) {
            System.err.println("Not enough parameters in line : " + s);
            return;
         }

         String nextToken = strTokenizer.nextToken();

         try {
            parentTypeList.addLast(nextToken);
         } catch (NullPointerException e) {
            System.err.println("Not enough parameters in line : " + s);
            return;
         }

         nextToken = strTokenizer.nextToken();

         boolean single;
         try {
            single = nextToken.equals("S");
            concreteList.addLast(new Boolean(single || nextToken.equals("C")));
            singletonList.addLast(new Boolean(single));
         } catch (NullPointerException e) {
            System.err.println("Not enough parameters in line : " + s);
            return;
         }

         LinkedList methodList = new LinkedList();

         if (single) {
            ClassField cf  = new ClassField(className, "instance",
             "public static final", "new " + className + "()");

            methodList.addLast(cf);

            MethodSignature ms = new MethodSignature(className);

            methodList.addLast(ms);
         }

         while (strTokenizer.hasMoreTokens()) {
           marker = strTokenizer.nextToken();
           char markChar = marker.charAt(0);
           switch (markChar) {

           case 'c':
           case 'm':
           {
             MethodSignature ms =
               new MethodSignature(markChar, strTokenizer, className);
             methodList.addLast(ms);
           }
           break;

           case 'k':
           {
             MethodSignature ms =
               new MethodSignature(markChar, strTokenizer, className);

             methodList.addLast(ms);

             LinkedList accessorMethodList = ms.accessors();

             methodList.addAll(accessorMethodList);
           }
           break;

           default:
           throw new RuntimeException("Unrecognized marker : " + marker);
           }
         }

         methodListList.addLast(methodList);
      }
      s = ifs.readLine();
    } while (s != null);

    ifs.close();

    ListIterator itr = typeList.listIterator();
    ListIterator parItr = parentTypeList.listIterator();
    ListIterator concreteItr = concreteList.listIterator();
    ListIterator singletonItr = singletonList.listIterator();
    ListIterator methodListItr = methodListList.listIterator();

    while (itr.hasNext()) {
      String typeName = (String) itr.next();
      String parentTypeName = (String) parItr.next();
      boolean concrete = ((Boolean) concreteItr.next()).booleanValue();
      boolean singleton = ((Boolean) singletonItr.next()).booleanValue();
      LinkedList methodList = (LinkedList) methodListItr.next();

      if (concrete) {
         s = "\n" +
             "    public Object visit" + typeName + "(" + typeName +
             " node, LinkedList args) {\n" +
             "        return _defaultVisit(node, args);\n" +
             "    }\n";
         ofs.write(s);
      }

      generateNodeFile(typeName, parentTypeName, concrete, singleton, methodList, header);
    }

    s = "\n" +
        "    /** Specify the order in visiting the nodes. */\n" +
        "    public final int traversalMethod() { return _traversalMethod; }\n";

    s = s +
        "\n" +
        "    /** The default visit method. */\n" +
        "    protected Object _defaultVisit(" + baseNodeName + " node, LinkedList args) {\n" +
        "        return null;\n" +
        "    }\n";

    /*
    String pfsi = "    public static final int ";
    s = s +
        "\n" +
        pfsi + "TM_CHILDREN_FIRST = 0;\n" +
        pfsi + "TM_SELF_FIRST = 1;\n" +
        pfsi + "TM_CUSTOM = 2;\n";
    */

    s = s +
        "\n" +
        "    protected final int _traversalMethod;\n" +
        "}\n";

    ofs.write(s);
    ofs.close();
  }

  public static void generateNodeFile(String typeName, String parentTypeName,
   boolean concrete, boolean singleton, LinkedList methodList, String header)
   throws IOException {
    File fdest = new File(typeName + ".java");

    if (!fdest.createNewFile()) {
       fdest.delete();
       fdest = new File(typeName + ".java");
       fdest.createNewFile();
    }

    FileWriter fw = new FileWriter(fdest);

    StringBuffer sb = new StringBuffer();

    sb.append(header);

    if (!concrete) {
       sb.append("abstract ");
    }

    if (singleton) {
       sb.append("final ");
    }

    sb.append("class ");
    sb.append(typeName);
    sb.append(" extends ");
    sb.append(parentTypeName);
    sb.append(" {\n");

    // do methods first
    ListIterator methodItr = methodList.listIterator();

    while (methodItr.hasNext()) {
      Object o = methodItr.next();

      if (o instanceof MethodSignature) {
         sb.append(o.toString());
         sb.append('\n');
      }
    }

    // now do fields
    methodItr = methodList.listIterator();

    while (methodItr.hasNext()) {
      Object o = methodItr.next();

      if (o instanceof ClassField) {
         sb.append(o.toString());
         sb.append('\n');
      }
    }

    sb.append("}\n");

    fw.write(sb.toString());
    fw.close();
  }


  private static String pkgName;

  private static final String copyrightStr =
"/* Copyright (c) 1998-1999 The Regents of the University of California.\n" +
"All rights reserved.\n\n" +
"Permission is hereby granted, without written agreement and without\n" +
"license or royalty fees, to use, copy, modify, and distribute this\n" +
"software and its documentation for any purpose, provided that the above\n" +
"copyright notice and the following two paragraphs appear in all copies\n" +
"of this software.\n\n" +
"IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY\n" +
"FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES\n" +
"ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF\n" +
"THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF\n" +
"SUCH DAMAGE.\n\n" +
"THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,\n" +
"INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n" +
"MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE\n" +
"PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF\n" +
"CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,\n" +
"ENHANCEMENTS, OR MODIFICATIONS.\n\n" +
"                                        PT_COPYRIGHT_VERSION_2\n" +
"                                        COPYRIGHTENDKEY\n\n" +
"@ProposedRating Red (ctsay@eecs.berkeley.edu)\n" +
"@AcceptedRating Red (ctsay@eecs.berkeley.edu)\n\n" +
"*/\n\n";

  private static final String ident = "    ";

  public static class MethodSignature {
    public MethodSignature() {}

    // a singleton constructor
    public MethodSignature(String className) {
      _defConstruct = false;
      _construct = true;
      _singleton = true;
      _name = className;
      _returnType = "";
    }

    // a constructor or method
    public MethodSignature(char sigType, StringTokenizer strToken, String className)
     throws IOException {

      System.out.println("method sig constructor begin");

      _defConstruct = (sigType == 'k');
      _construct = (sigType == 'c') || _defConstruct;

      if (_construct) { // constructor
         _name = className;
         _returnType = "";

         _superParams = Integer.parseInt(strToken.nextToken());

      } else if (sigType == 'm') { // method
         _superParams = 0;

         _returnType = strToken.nextToken();

         _name = strToken.nextToken();
      } else {
         throw new RuntimeException("Invalid token for MethodSignature : " +
          sigType);
      }

      String s = strToken.nextToken();

      System.out.println("first s : " + s);

      while (!(s.equals("c") || s.equals("m") || s.equals("k"))) {
         if (s.charAt(0) == '{') {
           // explicit placement, don't check
           _varPlacements.addLast(new Character(s.charAt(1)));
            s = s.substring(3);
         }  else {

            if ((defaultPlacement == 'l') && isJavaType(s)) {
               // make it a member if it's a Java type and we default to put it in a list
               _varPlacements.addLast(new Character('m'));
            } else {
               _varPlacements.addLast(new Character(defaultPlacement));
            }
         }

         _paramTypes.addLast(s);

         String paramName = strToken.nextToken();

         _paramNames.addLast(paramName);

         s = strToken.nextToken();

         System.out.println("next s : " + s);
      }

      System.out.println("method sig constructor end");
    }

    public String methodBody() {

      if (_construct) {

        StringBuffer sb = new StringBuffer();

        if (_superParams > 0) {

           ListIterator nameItr = _paramNames.listIterator();

           sb.append(ident);
           sb.append(ident);
           sb.append("super(");

           for (int i = 0; i < _superParams; i++) {
               sb.append((String) nameItr.next());

               if (i < (_superParams - 1)) {
                  sb.append(", ");
               }
           }

           sb.append(");\n");
        }

        if (_defConstruct) {

           ListIterator typeItr = _paramTypes.listIterator();
           ListIterator nameItr = _paramNames.listIterator();
           ListIterator varPlaceItr = _varPlacements.listIterator();

           int varCount = 0;

           do {
              String typeStr = (String) typeItr.next();
              String nameStr = (String) nameItr.next();
              char placement = ((Character) varPlaceItr.next()).charValue();

              if (varCount >= _superParams) {

                 sb.append(ident);
                 sb.append(ident);

                 switch (placement) {

                 case 'l':
                 sb.append("_childList.addLast(");
                 sb.append(nameStr);
                 sb.append(");");
                 break;

                 case 'm':
                 sb.append('_');
                 sb.append(nameStr);
                 sb.append(" = ");
                 sb.append(nameStr);
                 sb.append(';');
                 break;

                 case 'p':
                 sb.append("setProperty(\"");
                 sb.append(nameStr);
                 sb.append("\", ");
                 sb.append(nameStr);
                 sb.append(");");
                 break;

                 case 'n':
                 // do nothing
                 break;

                 default:
                 throw new RuntimeException("unknown variable placment");
                 }

                 if (typeItr.hasNext()) {
                    sb.append('\n');
                 }
              }

              varCount++;
           } while (typeItr.hasNext());
        }
        return sb.toString();
      } // if _construct

      if (_methodBody != null) {
         return ident + ident + _methodBody;
      }

      if (_returnType.equals("void") || _returnType.equals("")) {
         return "";
      }

      if (_returnType.equals("long")) {
         return ident + ident + "return 0L;";
      }

      if (_returnType.equals("int") || _returnType.equals("short") ||
          _returnType.equals("byte")) {
         return ident + ident + "return 0;";
      }

      if (_returnType.equals("char")) {
         return ident + ident + "return '\\0';";
      }

      if (_returnType.equals("float")) {
         return ident + ident + "return 0.0f;";
      }

      if (_returnType.equals("double")) {
         return ident + ident + "return 0.0d";
      }

      if (_returnType.equals("boolean")) {
         return ident + ident + "return false";
      }

      return ident + ident + "return null;";
    }

    public String toString() {
      StringBuffer sb = new StringBuffer(ident);


      sb.append(_singleton ? "private " : "public ");


      if (!_construct) { // if !constructor
         sb.append("final ");
         sb.append(_returnType);
         sb.append(" ");
      }

      sb.append(_name);
      sb.append('(');

      ListIterator typeItr = _paramTypes.listIterator();
      ListIterator nameItr = _paramNames.listIterator();

      while (typeItr.hasNext()) {
        sb.append((String) typeItr.next());
        sb.append(' ');
        sb.append((String) nameItr.next());

        if (typeItr.hasNext()) {
           sb.append(", ");
        }
      }

      sb.append(") {\n");

      sb.append(methodBody());
      sb.append('\n');

      sb.append(ident);
      sb.append("}\n");

      return sb.toString();
    }

    // a getter or a setter method for a child in the list
    public MethodSignature(String returnType, String name, int childIndex, boolean setter) {

      Character firstLetter = new Character(Character.toUpperCase(name.charAt(0)));

      String partName = firstLetter.toString() + name.substring(1);

      if (setter) {
        _returnType = "void";
        _name = "set" + partName;

        _paramTypes.addLast(returnType);
        _paramNames.addLast(name);

        _methodBody = "_childList.set(" + childIndex + ", " + name + ");";

      } else {
        _returnType = returnType;
        _name = "get" + partName;


        _methodBody = "return (" + _returnType + ") _childList.get(" +
                      childIndex + ");";
      }
    }

    // a getter or a setter method for data not in the list
    public MethodSignature(String returnType, String name, boolean setter) {
      Character firstLetter = new Character(Character.toUpperCase(name.charAt(0)));

      String partName = firstLetter.toString() + name.substring(1);

      if (setter) {
         _returnType = "void";
         _name = "set" + partName;

        _paramTypes.addLast(returnType);
        _paramNames.addLast(name);

        _methodBody = "_" + name + " = " + name + ";";
      } else {
        _returnType = returnType;
        _name = "get" + partName;

        _methodBody = "return _" + name + ";";
      }
    }

    public LinkedList accessors() {
      LinkedList retval = new LinkedList();

      int varCount = 0;
      int childIndex = 0;

      ListIterator typeItr = _paramTypes.listIterator();
      ListIterator nameItr = _paramNames.listIterator();
      ListIterator varPlaceItr = _varPlacements.listIterator();

      while (typeItr.hasNext()) {

        String typeStr = (String) typeItr.next();
        String nameStr = (String) nameItr.next();
        char placement = ((Character) varPlaceItr.next()).charValue();

        if (varCount >= _superParams) {

           switch (placement) {

           case 'l':
           // getter
           retval.addLast(new MethodSignature(typeStr, nameStr, childIndex, false));

           //setter
           retval.addLast(new MethodSignature(typeStr, nameStr, childIndex, true));

           // getter index
           retval.addLast(new ClassField("int",
            "CHILD_INDEX_" + nameStr.toUpperCase(),
            "public static final", Integer.toString(childIndex)));
           break;

           case 'm':
           retval.addLast(new ClassField(typeStr, nameStr));

           // getter
           retval.addLast(new MethodSignature(typeStr, nameStr, false));

           // setter
           retval.addLast(new MethodSignature(typeStr, nameStr, true));
           break;

           case 'n':
           // do nothing
           break;

           case 'p':
           // do nothing
           break;

           default:
           throw new RuntimeException("unknown variable placement");

           }
        }

        if (placement == 'l') {
           childIndex++;
        }

        varCount++;


      }

      return retval;
    }

    protected String _returnType;
    protected String _name;

    protected LinkedList _paramTypes = new LinkedList();
    protected LinkedList _paramNames = new LinkedList();
    protected LinkedList _varPlacements = new LinkedList();

    protected int _superParams = 0;

    protected String _methodBody = null;

    protected boolean _singleton = false;
    protected boolean _construct = false;
    protected boolean _defConstruct = false;
  }

  public static class ClassField {
    public ClassField(String type, String name, String modifiers, String init) {
      _type = type;
      _name = name;
      _modifiers = modifiers;
      _init = init;
    }

    public ClassField(String type, String name) {
      this(type, name, "protected", null);
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(ident);
      sb.append(_modifiers);
      sb.append(' ');
      sb.append(_type);
      sb.append(' ');

      if (!_modifiers.startsWith("public")) {
         sb.append(" _");
      }

      sb.append(_name);

      if (_init != null) {
         sb.append(" = ");
         sb.append(_init);
      }

      sb.append(';');
      return sb.toString();
    }

    String _type;
    String _name;
    String _modifiers;
    String _init;
  }

  public static final boolean isPrimitiveType(String s) {
    return (s.startsWith("int") || s.startsWith("char") ||
            s.startsWith("long") || s.startsWith("byte") ||
            s.startsWith("float") || s.startsWith("double") ||
            s.startsWith("boolean"));
  }

  public static final boolean isJavaType(String s) {
    return (isPrimitiveType(s) || s.startsWith("String"));
  }

  public static boolean flatMembers = false;

  public static char defaultPlacement = 'l';
}
