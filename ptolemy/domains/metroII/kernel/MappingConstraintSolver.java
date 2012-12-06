package ptolemy.domains.metroII.kernel;

public class MappingConstraintSolver implements ConstraintSolver {

    int _mapping[][];
    int _size;
    int _current_max_id;

    public MappingConstraintSolver(int size) {
        _mapping = new int[size][size];
        _size = size;
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _size; j++) {
                _mapping[i][j] = 0;
            }
        }
        _current_max_id = 0;
    }

    public String toString() {
        String result = "";
        for (int i = 0; i <= _current_max_id; i++) {
            for (int j = 0; j <= _current_max_id; j++) {
                result = result + " " + _mapping[i][j];
            }
            result = result + "\n";
        }
        return result;
    }

    @Override
    public void presentM2Event(int id) {
        // TODO Auto-generated method stub

        // System.out.print("present M2Event: "); 
        // System.out.println(id);
        assert (id < _size);
        assert (id > 0);
        if (id > _size) {
            return;
        }
        for (int i = 0; i < _size; i++) {
            if (_mapping[id][i] > 0) {
                _mapping[id][i]++;
            }
            if (_mapping[i][id] > 0) {
                _mapping[i][id]++;
            }
        }
    }

    @Override
    public boolean isSatisfied(int id) {
        // System.out.print("check M2Event: "); 
        // System.out.println(id);
        // TODO Auto-generated method stub
        // return true; 
        assert (id > 0);
        if (id > _size) {
            return true;
        }
        for (int i = 0; i < _size; i++) {
            if (_mapping[id][i] > 0 && _mapping[id][i] != 3) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _size; j++) {
                if (_mapping[i][j] > 0) {
                    _mapping[i][j] = 1;
                }
            }
        }
    }

    public void add(int id1, int id2) {
        _mapping[id1][id2] = 1;
        _mapping[id2][id1] = 1;

        if (id1 > _current_max_id) {
            _current_max_id = id1;
        }
        if (id2 > _current_max_id) {
            _current_max_id = id2;
        }
    }

}
