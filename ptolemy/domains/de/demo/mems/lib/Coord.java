package ptolemy.domains.de.demo.mems.lib;

import ptolemy.domains.de.demo.mems.lib.*;

/* this object is immutable */
public class Coord {
  private double _x, _y, _z;
  
  public Coord(double x, double y, double z) {
    _x = x; _y = y; _z = z;
  }
  
  public double dist(double x, double y, double z) {
    double dx = (x-_x);
    double dy = (y-_y);
    double dz = (z-_z);

    double result = Math.sqrt(dx*dx + dy*dy + dz*dz);
    Debug.log(2, "Coord: measured distance = " + result);
    return result;
  }

  public double dist(Coord other) {
    return dist(other.getX(), other.getY(), other.getZ());
  }

  public double getX() { return _x; }
  public double getY() { return _y; }
  public double getZ() { return _z; }
}
