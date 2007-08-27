package ptolemy.actor.gt.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gt.MatchCallback;
import ptolemy.actor.gt.RecursiveGraphMatcher;
import ptolemy.actor.gt.data.MatchResult;

public class AllMatchingTestHelper {

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Iterator<MatchResult> iterator = _results.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                buffer.append('\n');
            }
            buffer.append(iterator.next());
        }
        return buffer.toString();
    }

    public final MatchCallback callback = new MatchCallback() {
        public boolean foundMatch(RecursiveGraphMatcher matcher) {
            _results.add((MatchResult) matcher.getMatchResult().clone());
            return false;
        }
    };

    private List<MatchResult> _results = new LinkedList<MatchResult>();
}
