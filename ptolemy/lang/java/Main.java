package ptolemy.lang.java;

import java.util.LinkedList;
import ptolemy.lang.*;

class Main {
  public static void main(String[] args) {
    int files = args.length;
    int fileStart = 0;
    boolean debug = false;

    if (files >= 1) {
       debug = args[0].equals("-d");
       if (debug) {
          fileStart++;
          files--;
       }
    }

    if (files < 1) {
       System.out.println(
        "usage : ptolemy.lang.java.Main [-d] f1.java [f2.java ...]");
    }

    /*
    for (int f = 0; f < files; f++) {
        parser p = new parser();

        try {
          p.init(args[f + fileStart]);

        } catch (Exception e) {
          System.err.println("error opening " + args[f + fileStart]);
          System.err.println(e.toString());
        }

        p.yydebug = debug;

        p.yyparse();

        CompileUnitNode ast = p.getAST();

        System.out.println(ast.toString());


        //JavaVisitor v = new TypeVisitor();

        //ast.accept(v);

        //v = new JavaCodeGenerator();

        //System.out.println((String) ast.accept(v));

        ast.accept(new PackageResolutionVisitor());
    } */

    ApplicationUtility.enableTrace = debug;
    
    for (int f = 0; f < files; f++) {
        StaticResolution.load(args[fileStart + f], true);
        StaticResolution.declResolution();
    }

    LinkedList unitList = StaticResolution.fullyResolvedFiles;
    
    NumberDeclVisitor.numberDecls(unitList);
    
    Iterator unitItr = unitList.iterator();

    while (unitItr.hasNext()) {
        CompileUnitNode ast = (CompileUnitNode) unitItr.next();
        System.out.println(ast.toString());
    }
  }
}
