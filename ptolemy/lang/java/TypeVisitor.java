package ptolemy.lang.java;

import ptolemy.lang.*;
import java.util.LinkedList;

public class TypeVisitor extends JavaVisitor {
    public TypeVisitor() {
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        node.setProperty("type", IntTypeNode.instance);
        return null;
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        node.setProperty("type", LongTypeNode.instance);
        return null;
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        node.setProperty("type", FloatTypeNode.instance);
        return null;
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        node.setProperty("type", DoubleTypeNode.instance);
        return null;
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        node.setProperty("type", BoolTypeNode.instance);
        return null;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        node.setProperty("type", CharTypeNode.instance);
        return null;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        node.setProperty("type",
         new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String")));
        return null;
    }
}
