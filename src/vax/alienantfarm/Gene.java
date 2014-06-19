package vax.alienantfarm;

import java.util.Random;
import static vax.alienantfarm.constant.*;

/**
 Immutable. Create new instance if a derived gene is needed.

 @author toor
 */
public class Gene {
  final static protected Random RNG = new Random();
  final static public Gene //
          NULL_GENE = new Gene( 0, 0 ) {
            @Override
            public double epsilon( double x ) {
              return 0;
            }
          },
          LINEAR_GENE = new Gene( 1, 0.5 ) {
            @Override
            public double epsilon( double x ) {
              return x;
            }
          };
  final protected double base, twist, twist_factor;

  static protected double rndMix() {
    return RNG.nextFloat() * MIX_RANGE + MIX_MIN;
  }

  static protected double rndMut() {
    return RNG.nextFloat() * BASE_MUT_RANGE - BASE_MUT_MAX;
  }

  static protected double mix( double d1, double d2 ) {
    double rd = rndMix();
    return rd * d1 + ( 1 - rd ) * d2 + rndMut();
  }

  static protected double clamp( double d ) {
    return util.clamp( d, GENE_MIN, GENE_MAX );
  }

  public Gene( double base, double twist ) {
    this.base = clamp( base );
    this.twist = clamp( twist );
    twist_factor = twist * twist + 0.5 * twist + 0.5; // 0.0/0.5/1.0 -> 0.5/1.0/2.0 range
  }

  public Gene( Gene g1, Gene g2 ) {
    this( mix( g1.base, g2.base ), mix( g1.twist, g2.twist ) );
  }

  public Gene() {
    this( RNG.nextFloat() * BASE_RND_RANGE + BASE_RND_MIN, RNG.nextFloat() * BASE_RND_RANGE + BASE_RND_MIN );
  }

  public double getBase() {
    return base;
  }

  public double getTwist() {
    return twist_factor;
  }

  public double epsilon( double x ) {
    return base * Math.pow( x, twist_factor );
    //return base * util.fast_pow( x, twist_factor );
  }

  @Override
  public String toString() {
    return "(" + base + "," + twist + ")";
  }


}
