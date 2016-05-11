package vax.alienantfarm;

import java.io.IOException;
import java.io.InputStream;

/**

 @author toor
 */
public class Util {
  static public double fast_pow( final double a, final double b ) {
    return Double.longBitsToDouble(
            (long) ( b * ( Double.doubleToLongBits( a ) - 4606921280493453312L ) ) + 4606921280493453312L );
  }

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

  final static double divs[] = {
    1E-1, 1E-2, 1E-3, 1E-4,
    1E-5, 1E-6, 1E-7, 1E-8,
    1E-9, 1E-10, 1E-11, 1E-12,
    1E-13, 1E-14, 1E-15, 1E-16,
    1E-17, 1E-18, 1E-19, 1E-20
  };

  static public double get_double( InputStream is ) throws IOException {
    boolean sign = false;
    int c;
    double d = 0.0, d2 = 0.0;
    int div_nr = -1;
    c = is.read();
    if ( c == '-' ) {
      sign = true;
      c = is.read();
    }
    if ( c != '0' )
      while( Character.isDigit( c ) ) {
        d *= 10;
        d += c - '0';
        c = is.read();
      }//
    else
      c = is.read();
    if ( c != '.' )
      return sign ? -d : d;
    c = is.read();
    while( Character.isDigit( c ) ) {
      div_nr++;
      d2 *= 10;
      d2 += c - '0';
      c = is.read();
    }
    return sign ? -d - d2 * divs[div_nr] : d + d2 * divs[div_nr];
  }

  static public String read_len( InputStream is, int length ) throws IOException {
    byte[] buf = new byte[length];
    is.read( buf, 0, length );
    return new String( buf );
  }

  private Util() {
  }

}
