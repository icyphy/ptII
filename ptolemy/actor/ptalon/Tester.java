package ptolemy.actor.ptalon;

import java.util.LinkedList;

public class Tester {

    public static void main(String args[]) {
        String in = "ptalonActor:a(x := <1/>, y := <2/>)(z := ptalonActor:b(y := <2/>, z := <2/>))";
        String[] out = _parseActorExpression(in);
        for (int i = 0; i < out.length; i++) {
            System.out.println(out[i]);
        }
    }

    public static String[] _parseActorExpression(String expression) {
        expression = expression.replaceAll("\\)(\\p{Blank})*\\(", ",");
        String[] actorSeparated = expression.split("\\(", 2);
        String actor = actorSeparated[0];
        String remains = actorSeparated[1];
        remains = remains.trim().substring(0, remains.length() - 1);
        LinkedList<Integer> markers = new LinkedList<Integer>();
        int parenthesis = 0;
        for (int i = 0; i < remains.length() - 1; i++) {
            if (remains.charAt(i) == '(') {
                parenthesis++;
            } else if (remains.charAt(i) == ')') {
                parenthesis--;
            } else if ((remains.charAt(i) == ',') && (parenthesis == 0)) {
                markers.add(i);
            }
        }
        String[] assignments = new String[markers.size() + 1];
        int lastMarker = -1;
        int index = 0;
        for (int thisMarker : markers) {
            assignments[index] = remains.substring(lastMarker + 1, thisMarker);
            index++;
            lastMarker = thisMarker;
        }
        assignments[index] = remains
                .substring(lastMarker + 1, remains.length());
        String[] output = new String[2 * assignments.length + 1];
        output[0] = actor;
        for (int i = 0; i < assignments.length; i++) {
            String[] equation = assignments[i].split(":=", 2);
            output[2 * i + 1] = equation[0].trim();
            output[2 * i + 2] = equation[1].trim();
        }
        return output;
    }

}
