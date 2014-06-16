package vax.alienantfarm;

/**

 @author toor
 */
public class util {
  static public double clamp( double d, double min, double max ) {
    return ( d < min ) ? min : ( ( d > max ) ? max : d );
  }

  static public boolean in_range( double d, double min, double max ) {
    return d >= min && d < max;
  }

  static public double dot( double x1, double y1, double x2, double y2 ) {
    return x1 * x2 + y1 * y2;
  }

  static public double dist( double dx, double dy ) {
    return Math.sqrt( dx * dx + dy * dy );
  }

  private util() {
  }

}
