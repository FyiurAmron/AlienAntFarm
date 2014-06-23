package vax.alienantfarm;

import java.util.HashSet;
import java.util.Random;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Color;

import static vax.alienantfarm.constant.*;
import static vax.alienantfarm.util.in_range;

/**

 @author toor
 */
public class AntBoard {
//
  static public class genome {
    protected Gene focus, smell, taste, aggro, chaos, phero;

    public genome() {
      this.focus = new Gene();
      this.smell = new Gene();
      this.taste = new Gene();
      this.aggro = new Gene();
      this.chaos = new Gene();
      this.phero = new Gene();
    }

    public genome( Gene focus, Gene smell, Gene taste, Gene aggro, Gene chaos, Gene phero ) {
      this.focus = focus;
      this.smell = smell;
      this.taste = taste;
      this.aggro = aggro;
      this.chaos = chaos;
      this.phero = phero;
    }

    public genome( genome g ) {
      this.focus = new Gene( g.focus );
      this.smell = new Gene( g.smell );
      this.taste = new Gene( g.taste );
      this.aggro = new Gene( g.aggro );
      this.chaos = new Gene( g.chaos );
      this.phero = new Gene( g.phero );
    }
    /*
     public genome( genome g, boolean single_mut ) {

     }
     */

    public genome( genome g1, genome g2 ) {
      this.focus = new Gene( g1.focus, g2.focus );
      this.smell = new Gene( g1.smell, g2.smell );
      this.taste = new Gene( g1.taste, g2.taste );
      this.aggro = new Gene( g1.aggro, g2.aggro );
      this.chaos = new Gene( g1.chaos, g2.chaos );
      this.phero = new Gene( g1.phero, g2.phero );
    }

    final static int MAX_LINE_LEN = 1024, TYPE_LEN = 3;

    @SuppressWarnings( "empty-statement" )
    static public genome factory( InputStream is ) throws IOException {
      Gene focus = null, smell = null, taste = null, aggro = null, chaos = null, phero = null;
      double d1, d2;
      int in;
      char[] char_buf = new char[3];
      while( true ) {
        while( true ) {
          in = is.read();
          if ( in == '#' ) {
            while( is.read() != '\n' );
            continue;
          } else if ( Character.isWhitespace( in ) )
            continue;
          else if ( in == '^' ) {
            if ( focus == null || smell == null || taste == null || aggro == null || chaos == null || phero == null )
              throw new AntException( "incomplete genome data" );
            return new genome( focus, smell, taste, aggro, chaos, phero );
          } else if ( in == -1 )
            return null;
          char_buf[0] = (char) in;
          char_buf[1] = (char) is.read();
          char_buf[2] = (char) is.read();
          break;
        }
        while( is.read() != '(' );
        d1 = util.get_double( is );
        //while( br.read() != ',' ); // already read by get_double()
        d2 = util.get_double( is );
        while( is.read() != '\n' );
        Gene g = new Gene( d1, d2 );
        switch ( new String( char_buf ) ) {
          case "FOC":
            focus = g;
            break;
          case "SME":
            smell = g;
            break;
          case "TAS":
            taste = g;
            break;
          case "AGG":
            aggro = g;
            break;
          case "CHA":
            chaos = g;
            break;
          case "PHE":
            phero = g;
            break;
          default:
            throw new AntException( "unknown ant gene '" + new String( char_buf ) + "'" );
        }
      }
    }

    public String toOutputString() {
      return toString() + "^\n";
    }

    @Override
    public String toString() {
      return "FOC " + focus + "\nSME " + smell + "\nTAS " + taste + "\nAGG " + aggro + "\nCHA " + chaos + "\nPHE " + phero + "\n";
    }

  }

  static public class proto {
    protected int size_x, size_y;
    protected int start_x, start_y;
    protected int exit_x, exit_y;
    final protected boolean[][] block;

    public proto( int size_x, int size_y, int start_x, int start_y, int exit_x, int exit_y ) {
      this.size_x = size_x;
      this.size_y = size_y;
      this.start_x = start_x;
      this.start_y = start_y;
      this.exit_x = exit_x;
      this.exit_y = exit_y;
      block = new boolean[size_x][size_y];
    }

    public void set_block( int x_min, int y_min, int x_max, int y_max, boolean value ) {
      for( int x = x_min; x <= x_max; x++ )
        for( int y = y_min; y <= y_max; y++ )
          block[x][y] = value;
    }
  } // class proto

  final static protected int[][] ANGLES = {
    { 0, 14 },
    { 5, 13 },
    { 10, 10 },
    { 13, 5 },
    { 14, 0 },
    { 13, -5 },
    { 10, -10 },
    { 5, -13 },
    { 0, -14 },
    { -5, -13 },
    { -10, -10 },
    { -13, -5 },
    { -14, 0 },
    { -13, 5 },
    { -10, 10 },
    { -5, 13 }
  };
  final static protected int ANGLE_STEPS = ANGLES.length;
  final static protected double ANGLE_RATE_STEP = 2.0 / ANGLE_STEPS;
  final static protected double[] ANGLE_RATE = new double[ANGLE_STEPS * 2];
  final static protected Random RNG = new Random();

  static {
    double rate = 0;
    int i = 0;
    for( ; rate < 1; rate += ANGLE_RATE_STEP, i++ )
      ANGLE_RATE[i] = rate;
    ANGLE_RATE[i] = rate;
    for( ; rate > 0; rate -= ANGLE_RATE_STEP, i++ )
      ANGLE_RATE[i] = rate;
    ANGLE_RATE[i] = rate;
    for( ; rate < 1; rate += ANGLE_RATE_STEP, i++ )
      ANGLE_RATE[i] = rate;
    ANGLE_RATE[i] = rate;
    for( ; rate > 0; rate -= ANGLE_RATE_STEP, i++ )
      ANGLE_RATE[i] = rate;
  }
//
  final protected proto board_proto;
  final protected double[][] phero_fresh, phero_old, phero_bad;
  final protected int size_x, size_y;
  final protected AntObserver ao;
  final protected HashSet<ant> ants = new HashSet<>(); // currently unused
  protected int start_x, start_y;
  protected int exit_x, exit_y;
  protected long time_sum;
  protected int time_sum_count;
  //protected double base_dist;
  //protected int best_time = Integer.MAX_VALUE;

  public AntBoard( proto p, AntObserver ao ) {
    board_proto = p;
    size_x = p.size_x;
    size_y = p.size_y;
    phero_fresh = new double[size_x][size_y];
    phero_old = new double[size_x][size_y];
    phero_bad = new double[size_x][size_y];
    this.ao = ao;
    //base_dist = util.dist( exit_x - start_x, exit_y - start_y );
  }

  public void set_block( int x_min, int y_min, int x_max, int y_max, boolean value ) {
    board_proto.set_block( x_min, y_min, x_max, y_max, value );
  }

  public void init_phero( ant a ) {
    time_sum = a.age;
    time_sum_count = 1;
    //time_factor *= time_factor;
    double phero_base = a.my_genome.phero.base, phero_a = 1.0 + 1.0 / ( phero_base - 1 );

    for( int x = 0, x_max = board_proto.size_x; x < x_max; x++ ) {
      @SuppressWarnings( { "MismatchedReadAndWriteOfArray", "UnusedAssignment" } )
      double[] pho_x = phero_old[x], phf_x = phero_fresh[x], phb_x = phero_bad[x];
      for( int y = 0, y_max = board_proto.size_y; y < y_max; y++ ) {
        if ( phf_x[y] <= phero_base ) {
          pho_x[y] = phf_x[y];
        } else {
          pho_x[y] = phero_a * ( phf_x[y] - 1 );
          phb_x[y] -= ( pho_x[y] - phf_x[y] );
        }
        phf_x[y] = 0;
      }
    }
  }

  @SuppressWarnings( { "MismatchedReadAndWriteOfArray", "UnusedAssignment" } )
  public void age_phero( ant a ) {
    double time = a.age, time_avg = time_sum / time_sum_count, time_ratio = time / time_avg;
    double time_factor = -Math.log( time_ratio ) + 1;
    //System.out.println( time_factor );
    //time_factor *= time_factor; // add gene here?
    time_sum_count++;
    if ( time_ratio < 1 ) {
      time_sum += time;
    } else {
      time_sum += ( time_avg + ( time - time_avg ) * E_INV );
    }

    double phero_base = a.my_genome.phero.base, phero_a = 1.0 + 1.0 / ( phero_base - 1 );
    if ( time_factor < TIME_AMP_THRESHOLD ) { // slow (bad) solution
      for( int x = 0, x_max = board_proto.size_x; x < x_max; x++ ) {
        double[] pho_x = phero_old[x], phf_x = phero_fresh[x], phb_x = phero_bad[x];
        for( int y = 0, y_max = board_proto.size_y; y < y_max; y++ ) {
          pho_x[y] *= constant.PHERO_DISP_RATE_REDUCED;
          if ( phf_x[y] > phero_base )
            phb_x[y] -= ( phero_a * ( phf_x[y] - 1 ) - phf_x[y] ) * ( 1 - phb_x[y] );
          phf_x[y] = 0;
        }
      }
      return;
    }
    if ( time_factor <= 1 ) { // medium (mediocre) solution; time_factor in (TIME_AMP_THRESHOLD,1), no clamping needed
      for( int x = 0, x_max = board_proto.size_x; x < x_max; x++ ) {
        double[] pho_x = phero_old[x], phf_x = phero_fresh[x], phb_x = phero_bad[x];
        for( int y = 0, y_max = board_proto.size_y; y < y_max; y++ ) {
          pho_x[y] *= constant.PHERO_DISP_RATE;
          phb_x[y] *= constant.PHERO_DISP_RATE_REDUCED;
          if ( phf_x[y] <= phero_base ) {
            pho_x[y] += time_factor * phf_x[y] * ( 1 - pho_x[y] );
          } else {
            double gradient = phf_x[y] - 1;
            pho_x[y] += time_factor * phero_a * gradient * ( 1 - pho_x[y] );
            phb_x[y] -= ( phero_a * gradient - phf_x[y] ) * ( 1 - phb_x[y] );
          }
          phf_x[y] = 0;
        }
      }
    } else // fast (good) solution
      for( int x = 0, x_max = board_proto.size_x; x < x_max; x++ ) {
        double[] pho_x = phero_old[x], phf_x = phero_fresh[x], phb_x = phero_bad[x];
        for( int y = 0, y_max = board_proto.size_y; y < y_max; y++ ) {
          pho_x[y] *= constant.PHERO_DISP_RATE;
          phb_x[y] *= constant.PHERO_DISP_RATE;
          if ( phf_x[y] <= phero_base ) {
            pho_x[y] += time_factor * phf_x[y] * ( 1 - pho_x[y] );
          } else {
            double gradient = phf_x[y] - 1;
            pho_x[y] += time_factor * phero_a * gradient * ( 1 - pho_x[y] );
            phb_x[y] -= ( phero_a * gradient - phf_x[y] ) * ( 1 - phb_x[y] );
          }
          if ( pho_x[y] > 1 ) // clamp
            pho_x[y] = 1;
          phf_x[y] = 0;
        }
      }
  }

  public void add_phero( ant a ) {
    int dx, dy_start, x_min, y_min;

    if ( a.pos_x < PHERO_SPREAD_RADIUS ) {
      x_min = 0;
      dx = a.pos_x;
    } else {
      x_min = a.pos_x - PHERO_SPREAD_RADIUS;
      dx = PHERO_SPREAD_RADIUS;
    }
    if ( a.pos_y < PHERO_SPREAD_RADIUS ) {
      y_min = 0;
      dy_start = a.pos_y;
    } else {
      y_min = a.pos_y - PHERO_SPREAD_RADIUS;
      dy_start = PHERO_SPREAD_RADIUS;
    }
    int x_max = a.pos_x + PHERO_SPREAD_RADIUS,
            y_max = a.pos_y + PHERO_SPREAD_RADIUS;
    if ( x_max >= size_x )
      x_max = size_x - 1;
    if ( y_max >= size_y )
      y_max = size_y - 1;

    for( int i_x = x_min; i_x <= x_max; i_x++, dx-- ) {
      double dx_sq = dx * dx;
      for( int i_y = y_min, dy = dy_start; i_y <= y_max; i_y++, dy-- ) {
        double dist_sq = dx_sq + dy * dy;
        if ( dist_sq >= PHERO_SPREAD_RADIUS_SQ )
          continue;
        if ( board_proto.block[i_x][i_y] )
          continue;
        phero_fresh[i_x][i_y] += a.my_genome.phero.epsilon( 1 - Math.sqrt( dist_sq ) * PHERO_SPREAD_RADIUS_INV )
                * ( 1 - phero_fresh[i_x][i_y] );
      }
    }
  }

  public int get_color( int x, int y ) {
    return board_proto.block[x][y] ? Color.WHITE.getRGB()
            : new Color( (float) phero_bad[x][y], (float) phero_old[x][y], (float) phero_fresh[x][y] ).getRGB();
  }

  protected void reverse_path() {
    int tmp_x = exit_x, tmp_y = exit_y;
    exit_x = start_x;
    exit_y = start_y;
    start_x = tmp_x;
    start_y = tmp_y;
  }

  @SuppressWarnings( "empty-statement" )
  protected int iteration( genome g, int angle ) {
    ant a = new ant( g, angle, start_x, start_y );
    while( a.step() );
    age_phero( a );
    return a.age;
  }

  @SuppressWarnings( "empty-statement" )
  public int run_iterations( int count, genome g, boolean bidi ) {
    start_x = board_proto.start_x;
    start_y = board_proto.start_y;
    exit_x = board_proto.exit_x;
    exit_y = board_proto.exit_y;
    int angle = RNG.nextInt( ANGLE_STEPS );
    int angle2 = ( angle + 8 ) % ANGLE_STEPS;
    int it_total = 0;
    ant a = new ant( g, angle, start_x, start_y );
    while( a.step() );
    init_phero( a );
    it_total += a.age;
    reverse_path();
    it_total += iteration( g, angle2 );
    reverse_path();
    for( int i = 1; i < count; i++ ) {
      it_total += iteration( g, angle );
      reverse_path();
      it_total += iteration( g, angle2 );
      reverse_path();
    }
    return it_total;
  }

  public class ant {
    final protected genome my_genome;
    protected int pos_x, pos_y;
    protected int angle; // clockwise, 0 at noon
    protected int age;
    protected double cur_age_exp = 1.0; // currently unused
    protected double[] values = new double[ANGLE_STEPS]; // so it's locally static

    protected ant( genome my_genome, int angle, int pos_x, int pos_y ) {
      this.my_genome = my_genome;
      this.angle = angle;
      this.pos_x = pos_x;
      this.pos_y = pos_y;
      ao.init( this );
    }

    protected AntBoard get_board() {
      return AntBoard.this;
    }

    protected boolean step() {
      boolean ret = step_internal();
      ao.step( this );
      return ret;
    }

    protected boolean step_internal() {
      double sum_value = 0;
      for( int i = 0, i_ang = ANGLE_STEPS - angle; i < ANGLE_STEPS; i++, i_ang++ ) {
        int x_ang = ANGLES[i][0], x = pos_x + x_ang,
                y_ang = ANGLES[i][1], y = pos_y + y_ang;
        if ( !in_range( x, 0, size_x ) || !in_range( y, 0, size_y ) ) {
          values[i] = 0;
          continue;
        }
        if ( board_proto.block[x][y] ) {
          values[i] = 0;
          continue;
        }
        double dx = exit_x - x, dy = exit_y - y;
        double dist = util.dist( dx, dy );
        if ( dist <= DIST_THRESHOLD ) {
          pos_x = exit_x;
          pos_y = exit_y;
          angle = i;
          add_phero( this );
          age++;
          cur_age_exp *= AGE_EXP;
          ao.finish( this );
          return false;
        }

        double cos_alpha = util.dot( x_ang, y_ang, dx, dy ) / ( STRIDE * dist );
        if ( cos_alpha > 1 )
          cos_alpha = 1;
        else if ( cos_alpha < -1 )
          cos_alpha = -1;

        double base_weight
                = my_genome.chaos.epsilon( RNG.nextFloat() )
                + my_genome.smell.epsilon( 1.0 - PI_INV * Math.acos( cos_alpha ) )
                + my_genome.taste.epsilon( phero_old[x][y] )
                - my_genome.aggro.epsilon( phero_fresh[x][y] )
                - my_genome.aggro.epsilon( phero_bad[x][y] )
                - my_genome.focus.epsilon( ANGLE_RATE[i_ang] );

        //if ( base_weight != base_weight )
        //  throw new AntException( "base_weight is NaN!" );
        values[i] = ( base_weight < RELATIVELY_BLOCKED_WEIGHT ) ? RELATIVELY_BLOCKED_WEIGHT : base_weight;
        sum_value += values[i];
      }
      if ( sum_value < RELATIVELY_BLOCKED_WEIGHT )
        throw new AntException( "ant blocked!" );
      double pick = RNG.nextFloat() * sum_value;
      int i = 0;
      for( ; i < ANGLE_STEPS && pick >= values[i]; i++ )
        pick -= values[i];
      pos_x += ANGLES[i][0];
      pos_y += ANGLES[i][1];
      //if ( pos_x >= size_x || pos_y >= size_y )
      //  throw new AntException( "??? out of range..." );
      angle = i;
      add_phero( this );
      age++;
      cur_age_exp *= AGE_EXP;
      return true;
    }

  }

}
