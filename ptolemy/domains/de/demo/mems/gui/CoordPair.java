package ptolemy.domains.de.demo.mems.gui;

import ptolemy.domains.de.demo.mems.lib.*;

public class CoordPair {
  public Coord one, two;
  public int color;

  public CoordPair(Coord one, Coord two, int color) {
    this.one = one;
    this.two = two;
    this.color = color;
  }
}
