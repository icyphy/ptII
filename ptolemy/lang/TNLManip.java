package ptolemy.lang;

import java.util.LinkedList;
import java.util.ListIterator;

class TNLManip {

    private TNLManip() {}

    public static final LinkedList traverseList(IVisitor v, TreeNode parent,
     LinkedList args, LinkedList childList) {
       boolean anyNonNullRetval = false;
       Object retval;
       LinkedList retList = new LinkedList();

       ListIterator itr = childList.listIterator();

       while (itr.hasNext()) {
         Object obj = itr.next();

         if (obj instanceof TreeNode) {
            TreeNode node = (TreeNode) obj;

            /*     // parent is not used!!!
            if (parent != null) {
               args.addFirst((Object) parent);
            } else {
               args.addFirst((Object) NullValue.instance);
            } */

            retval = node.accept(v, args);

            // args.removeFirst();

            if (retval == null) {
               retList.addLast(NullValue.instance);
            } else {
               retList.addLast(retval);
            }

         } else if (obj instanceof LinkedList) {
            retval = (Object) traverseList(v, null, args, (LinkedList) obj);

            retList.addLast(retval);
         } else {
            throw new RuntimeException("unknown object in list : " + obj.getClass());
         }
       }

       return retList;
    }

    public static final String toString(LinkedList list, String indent) {
       if (list.isEmpty()) {
          return "<empty list>\n";
       }

       StringBuffer sb = new StringBuffer();

       sb.append('\n');
       sb.append(indent);
       sb.append("list\n");

       ListIterator itr = list.listIterator();

       while (itr.hasNext()) {
          Object child = itr.next();

          if (child instanceof TreeNode) {
             TreeNode childNode = (TreeNode) child;
             sb.append(childNode.toString(indent + "  "));
          } else if (child instanceof LinkedList) {
             sb.append(TNLManip.toString((LinkedList) child, indent + "  "));
          } else {
             throw new RuntimeException("unknown object in list : " + child.getClass());
          }
       }

       sb.append(indent);
       sb.append("END list\n");

       return sb.toString();
    }
}