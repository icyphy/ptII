package ptolemy.data.properties.lattice;

import ptolemy.data.Token;

public interface TypeProperty {

    public boolean hasMinMaxValue();

    public Token getMaxValue();
    public Token getMinValue();
}
