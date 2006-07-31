package ptolemy.actor.ptalon;

import antlr.CommonAST;
import antlr.debug.misc.ASTFrame;
import java.io.File;
import java.io.FileReader;

public class TreeTester {

    /**
     * @param The absolute filename of the file
     * to test.
     */
    public static void main(String[] args) {
        try {
            File file = new File(args[0]);
            FileReader reader = new FileReader(file);
            PtalonLexer lexer = new PtalonLexer(reader);
            PtalonRecognizer parser = new PtalonRecognizer(lexer);
            parser.actor_definition();
            CommonAST ast = (CommonAST)parser.getAST();
            PtalonWalker walker = new PtalonWalker();
            walker.actor_definition(ast);
            ASTFrame frame = new ASTFrame("Viewer", ast);
            frame.setVisible(true);
            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
