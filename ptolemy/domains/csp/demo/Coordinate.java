/**
 * Class to store a simple Cartesian Coordinate.
 *
 * @author John Hall
 */
package ptolemy.domains.csp.demo;

public class Coordinate {
	/**
	 * The X coordinate.
	 */
	public int X = 0;
	/**
	 * The Y coordinate.
	 */
	public int Y = 0;

	/**
	 * Constructs a new Coordinate initialised as the origin (0, 0).
	 */
	public Coordinate() {}

	/**
	 * Constructs a new Coordinate with the specified values.
	 *
	 * @param  x the X coordinate.
	 * @param  y the Y coordinate.
	 */
	public Coordinate(int x, int y) {
		X = x;
		Y = y;
	}

	/**
	 * Tests another object for equality with this instance.
	 *
	 * @return whether it is equal or not.
	 */
	public boolean equals(Object o) {
		Coordinate c;

		if (o instanceof Coordinate) {
			c = (Coordinate) o;
			if (this.X == c.X && this.Y == c.Y) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Returns a string representation of the Coordinate in the form "(X, Y)".
	 *
	 * @returns a string representation of the Coordinates.
	 */
	public String toString() {
		return "(" + X + ", " + Y + ")";
	}
}
