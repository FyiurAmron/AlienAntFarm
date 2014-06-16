package vax.alienantfarm;

import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import static vax.alienantfarm.constant.*;
import static vax.alienantfarm.util.in_range;

/**

 @author toor
 */
public class AntBoard {
  static protected class field {
    protected boolean block;
    protected double phero_fresh, phero_old;

    public int getColor() {
      return block ? Color.BLACK.getRGB() : new Color( 0, (float) phero_old, (float) phero_fresh ).getRGB();
    }
  }

  /**
   Immutable. Create new instance if a derived genome is needed.
   */
  static public class genome {
    final protected Gene agility, smell, taste, aggro, chaos, phero;

    public genome() {
      this.agility = new Gene();
      this.smell = new Gene();
      this.taste = new Gene();
      this.aggro = new Gene();
      this.chaos = new Gene();
      this.phero = new Gene();
    }

    public genome( Gene agility, Gene smell, Gene taste, Gene aggro, Gene chaos, Gene phero ) {
      this.agility = agility;
      this.smell = smell;
      this.taste = taste;
      this.aggro = aggro;
      this.chaos = chaos;
      this.phero = phero;
    }

    public genome( genome g1, genome g2 ) {
      this.agility = new Gene( g1.agility, g2.agility );
      this.smell = new Gene( g1.smell, g2.smell );
      this.taste = new Gene( g1.taste, g2.taste );
      this.aggro = new Gene( g1.aggro, g2.aggro );
      this.chaos = new Gene( g1.chaos, g2.chaos );
      this.phero = new Gene( g1.phero, g2.phero );
    }

    @Override
    public String toString() {
      return "AGI " + agility + "\nSME " + smell + "\nTAS " + taste + "\nAGG " + aggro + "\nCHA " + chaos + "\nPHE " + phero;
    }

  }

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
  protected HashSet<ant> ants = new HashSet<>();
  protected AntObserver ao;
  protected int size_x, size_y;
  protected int start_x, start_y;
  protected int exit_x, exit_y;
  protected int best_time = Integer.MAX_VALUE;
  protected field[][] board;

  public AntBoard( int size_x, int size_y, int start_x, int start_y, int exit_x, int exit_y, AntObserver ao ) {
    this.size_x = size_x;
    this.size_y = size_y;
    this.start_x = start_x;
    this.start_y = start_y;
    this.exit_x = exit_x;
    this.exit_y = exit_y;
    this.ao = ao;
    board = new field[size_x][size_y];
    for( field[] fs : board )
      for( int i = 0, max = fs.length; i < max; i++ )
        fs[i] = new field();
  }

  public void set_block( int x_min, int y_min, int x_max, int y_max, boolean value ) {
    for( int x = x_min; x <= x_max; x++ )
      for( int y = y_min; y <= y_max; y++ )
        board[x][y].block = value;
  }

  public boolean is_blocked( int x, int y ) {
    return board[x][y].block;
  }

  public double get_phero_fresh( int x, int y ) {
    return board[x][y].phero_fresh;
  }

  public double get_phero_old( int x, int y ) {
    return board[x][y].phero_old;
  }

  public void age_phero( int time ) {
    double time_factor = (double) best_time / time;
    if ( time < best_time )
      best_time = time;
    for( field[] fs : board )
      for( field f : fs ) {
        f.phero_old *= constant.PHERO_DISP_RATE;
        f.phero_old += time_factor * f.phero_fresh;
        if ( f.phero_old > 1 ) // clamp
          f.phero_old = 1;
        f.phero_fresh = 0;
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
        field f = board[i_x][i_y];
        if ( f.block )
          continue;
        f.phero_fresh += a.my_genome.phero.epsilon( 1 - Math.sqrt( dist_sq ) * PHERO_SPREAD_RADIUS_INV ) * ( 1 - f.phero_fresh );
      }
    }
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
    age_phero( a.age );
    return a.age;
  }

  @SuppressWarnings( "empty-statement" )
  public int run_iterations( int count, genome g ) {
    int angle = 0; // var
    int angle2 = ( angle + 8 ) % ANGLE_STEPS;
    int it_total = 0;
    ant a = new ant( g, angle, start_x, start_y );
    while( a.step() );
    best_time = a.age;
    age_phero( a.age );
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
        field f = board[x][y];
        if ( f.block ) {
          values[i] = 0;
          continue;
        }
        double dx = exit_x - x, dy = exit_y - y;
        double dist = util.dist( dx, dy );
        if ( dist <= DIST_THRESHOLD ) {
          pos_x += x_ang;
          pos_y += y_ang;
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
                + my_genome.taste.epsilon( f.phero_old )
                - my_genome.aggro.epsilon( f.phero_fresh )
                - my_genome.agility.epsilon( ANGLE_RATE[i_ang] );

        values[i] = ( base_weight < RELATIVELY_BLOCKED_WEIGHT ) ? RELATIVELY_BLOCKED_WEIGHT : base_weight;
        sum_value += values[i];
      }
      if ( sum_value < RELATIVELY_BLOCKED_WEIGHT )
        throw new RuntimeException( "ant blocked!" );
      double pick = RNG.nextFloat() * sum_value;
      int i = 0;
      for( ; i < ANGLE_STEPS && pick >= values[i]; i++ )
        pick -= values[i];
      pos_x += ANGLES[i][0];
      pos_y += ANGLES[i][1];
      angle = i;
      add_phero( this );
      age++;
      cur_age_exp *= AGE_EXP;
      return true;
    }

  }

}
