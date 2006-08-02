package ptolemy.actor.ptalon.test;

import java.util.*;
import java.io.*;
import antlr.collections.*;
import antlr.debug.misc.ASTFrame;
import ptolemy.actor.ptalon.*;


public class PtalonTester {

    /**
     * Test the PtalonFiles to see what they parse.
     * @param args 
     */
    public static void main(String[] args) {
        try {
            for (String arg : Arrays.asList(args)) {
                File file = new File(arg);
                FileReader reader = new FileReader(file);
                PtalonLexer lexer = new PtalonLexer(reader);
                PtalonRecognizer rec = new PtalonRecognizer(lexer);
                rec.actor_definition();
                AST ast = rec.getAST();
                PtalonScopeChecker check = new PtalonScopeChecker();
                check.actor_definition(ast);
                PtalonCompilerInfo info = check.getCompilerInfo();
                ast = check.getAST();
                ASTFrame frame = new ASTFrame("Checked AST", ast);
                frame.setVisible(true);
                System.out.println(info);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}
