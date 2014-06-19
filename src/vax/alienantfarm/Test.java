package vax.alienantfarm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import vax.sqvaardcraft.ui.SC_Image;
import static vax.alienantfarm.AntBoard.genome;

/**

 @author toor
 */
public class Test {
  final static protected int //
          MAX_SIZE = constant.STRIDE * 50,
          ITERATIONS = 5;

  // general note on iterations:
  // 100 iterations: order-of-magnitude estimate only
  // 1000 iterations: approx +/- 10% of the real average
  // 10k iterations: approx 98% confidence interval
  static public class CounterAntObserver implements AntObserver {
    protected class result {
      public int it_start_sum, it_sum, it_end_sum;

      protected result() {
      }

      protected result( int it_start_sum, int it_sum, int it_end_sum ) {
        this.it_start_sum = it_start_sum;
        this.it_sum = it_sum;
        this.it_end_sum = it_end_sum;
      }

      public double r_start() {
        return (double) it_start_sum / edge_i;
      }

      public double r_avg() {
        return (double) it_sum / ( 2 * i_max );
      }

      public double r_end() {
        return (double) it_end_sum / edge_i;
      }
    }

    protected int i, i_max, edge_i, edge_up, hard_limit;
    protected result r = new result();

    public CounterAntObserver( int i_max, int edge_i, int hard_limit ) {
      this.i_max = i_max;
      this.edge_i = edge_i;
      edge_up = 2 * i_max - edge_i;
      this.hard_limit = hard_limit;
    }

    public void setResultFailure() {
      r = new result() {
        @Override
        public double r_start() {
          return Double.POSITIVE_INFINITY;
        }

        @Override
        public double r_avg() {
          return Double.POSITIVE_INFINITY;
        }

        @Override
        public double r_end() {
          return Double.POSITIVE_INFINITY;
        }
      };
    }

    @Override
    public void init( AntBoard.ant a ) {
    }

    @Override
    public void step( AntBoard.ant a ) {
      if ( a.age > hard_limit )
        throw new AntException( "hard limit reached" );
    }

    @Override
    public void finish( AntBoard.ant a ) {
      if ( i < edge_i )
        r.it_start_sum += a.age;
      else if ( i >= edge_up )
        r.it_end_sum += a.age;
      r.it_sum += a.age;
      i++;
    }

    @Override
    public String toString() {
      return "# Iterations: " + i_max
              + "\n# rating START = " + r.r_start()
              + "\n# rating AVG = " + r.r_avg()
              + "\n# rating END = " + r.r_end()
              + "\n";
    }
  }

  static public class MeasuringAntObserver extends CounterAntObserver {
    protected PrintStream ps;

    public MeasuringAntObserver( int i_max, int edge_i, int hard_limit, PrintStream os ) {
      super( i_max, edge_i, hard_limit );
      this.ps = os;
    }

    public MeasuringAntObserver( int i_max, int edge_i, int hard_limit ) {
      this( i_max, edge_i, hard_limit, System.out );
    }

    @Override
    public void finish( AntBoard.ant a ) {
      super.finish( a ); //To change body of generated methods, choose Tools | Templates.
      ps.println( a.age );
    }

  }

  static public class BufferedImageAntObserver implements AntObserver {
    final static int ANT_COLOR = Color.RED.getRGB(),
            BOARD_COLOR = Color.BLACK.getRGB(),//Color.WHITE.getRGB(),
            BLOCK_COLOR = Color.WHITE.getRGB(),//Color.BLACK.getRGB(),
            TRACE_COLOR = Color.YELLOW.getRGB(),
            SPECIAL_COLOR = Color.GREEN.getRGB();
    protected BufferedImage bi;
    protected int size_x, size_y;
    protected int last_x, last_y;
    protected int delay, big_iteration_delay;
    protected SC_Image parent;

    public BufferedImageAntObserver( int size_x, int size_y, int delay, int finish_delay, SC_Image parent ) {
      this.size_x = size_x;
      this.size_y = size_y;
      this.delay = delay;
      this.big_iteration_delay = finish_delay;
      this.parent = parent;
    }

    public BufferedImage getBufferedImage() {
      return bi;
    }

    @Override
    public void init( AntBoard.ant a ) {
      bi = new BufferedImage( size_x, size_y, BufferedImage.TYPE_INT_ARGB );
      AntBoard ab = a.get_board();
      for( int x = 0; x < size_x; x++ )
        for( int y = 0; y < size_y; y++ )
          bi.setRGB( x, y, ab.get_color( x, y ) );
      if ( parent != null ) {
        parent.set_image( bi );
        parent.repaint();
      }
      try {
        Thread.sleep( big_iteration_delay );
      } catch (InterruptedException ex) {
      }

      for( int x = 0; x < size_x; x++ ) {
        boolean[] abbpb_x = ab.board_proto.block[x];
        for( int y = 0; y < size_y; y++ )
          bi.setRGB( x, y, abbpb_x[y] ? BLOCK_COLOR : BOARD_COLOR );
      }
      bi.setRGB( ab.exit_x, ab.exit_y, SPECIAL_COLOR );
      last_x = a.pos_x;
      last_y = a.pos_y;
      bi.setRGB( last_x, last_y, ANT_COLOR );
      if ( parent != null ) {
        parent.set_image( bi );
        parent.repaint();
      }
    }

    @Override
    public void step( AntBoard.ant a ) {
      try {
        bi.setRGB( last_x, last_y, TRACE_COLOR );
        last_x = a.pos_x;
        last_y = a.pos_y;
        bi.setRGB( last_x, last_y, ANT_COLOR );
        if ( parent != null )
          parent.repaint();
        Thread.sleep( delay );
      } catch (Exception ex) {
        System.out.println( ex );
      }
    }

    @Override
    public void finish( AntBoard.ant a ) {
      System.out.println( "success @ " + a.age );
      AntBoard ab = a.get_board();
      for( int x = 0; x < size_x; x++ )
        for( int y = 0; y < size_y; y++ )
          bi.setRGB( x, y, ab.get_color( x, y ) );
      if ( parent != null ) {
        parent.set_image( bi );
        parent.repaint();
      }
      try {
        Thread.sleep( big_iteration_delay );
      } catch (InterruptedException ex) {
      }
    }
  }

  static public void display_genome_test( genome g, AntBoard.proto abp, int iterations, int delay, int big_iteration_delay ) {
    JFrame jf = new JFrame();
    SC_Image img = new SC_Image( MAX_SIZE, MAX_SIZE );
    BufferedImageAntObserver biao = new BufferedImageAntObserver( MAX_SIZE, MAX_SIZE, delay, big_iteration_delay, img );
    jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    jf.add( img );
    jf.pack();
    jf.setVisible( true );

    AntBoard ab = new AntBoard( abp, biao );

    System.out.println( g );
    ab.run_iterations( iterations, g, true );
  }

  protected static class genome_rater implements Comparable<genome_rater> {
    public genome g;
    public double rating;

    protected genome_rater( genome g, double rating ) {
      this.g = g;
      this.rating = rating;
    }

    @Override
    public int compareTo( genome_rater o ) {
      return ( rating < o.rating ) ? -1 : ( ( rating > o.rating ) ? 1 : 0 );
    }

    @Override
    public String toString() {
      return g + " rating " + rating + "\n";
    }
  }

  static public CounterAntObserver.result measure_genome( genome g, AntBoard.proto abp, int iterations, int hard_limit )
          throws IOException {
    System.out.println(
            "#============================================================\n"
            + g + "^" );
    try (PrintStream ps = new PrintStream( "genome_measurement.aaf" )) {
      CounterAntObserver cao = new MeasuringAntObserver( iterations, iterations / 5, hard_limit, ps );
      AntBoard ab = new AntBoard( abp, cao );
      ab.run_iterations( iterations, g, true );
      System.out.println( cao );
      return cao.r;
    } catch (AntException ex) {
      System.out.println( ex );
      return null;
    }
  }

  static public CounterAntObserver.result rate_genome( genome g, AntBoard.proto abp, int iterations, int hard_limit ) {
    System.out.println(
            "#============================================================\n"
            + g + "^" );
    CounterAntObserver cao = new CounterAntObserver( iterations, iterations / 5, hard_limit );
    AntBoard ab = new AntBoard( abp, cao );
    try {
      ab.run_iterations( iterations, g, true );
    } catch (AntException ex) {
      cao.setResultFailure();
      System.out.println( ex );
      return null;
    }
    System.out.println( cao );
    return cao.r;
  }

  static protected void soup_output( String type, TreeSet<genome_rater> tsgr, int target_count, PrintStream ps ) {
    ps.println( "### best " + type + " ###" );
    Iterator<genome_rater> it = tsgr.iterator();
    if ( !it.hasNext() )
      throw new RuntimeException( "empty TreeSet provided!" );
    genome_rater gr = it.next();
    for( int i = 0; i < target_count && gr != null; gr = it.hasNext() ? it.next() : null, i++ )
      ps.println( gr.g.toOutputString() + "# rating: " + gr.rating );
  }

  static public void primordial_soup( AntBoard.proto abp, int genome_count, int target_count, int iterations, int hard_limit ) {
    TreeSet<genome_rater> start = new TreeSet<>(), avg = new TreeSet<>(), end = new TreeSet<>();
    for( int i = 0; i < genome_count; i++ ) {
      genome g = new genome();
      CounterAntObserver.result caor = rate_genome( g, abp, iterations, hard_limit );
      if ( caor != null ) {
        start.add( new genome_rater( g, caor.r_start() ) );
        avg.add( new genome_rater( g, caor.r_avg() ) );
        end.add( new genome_rater( g, caor.r_end() ) );
      }
    }
    System.out.println( "=== HALL OF FAME ===\nstart:\n" + start.first() + "avg:\n" + avg.first() + "end:\n" + end.first() );
    try (PrintStream ps = new PrintStream( "genome_soup.aaf" )) {
      soup_output( "START", start, target_count, ps );
      soup_output( "AVG", avg, target_count, ps );
      soup_output( "END", end, target_count, ps );
    } catch (IOException ex) {
      System.out.println( ex );
    }
  }

  static public void test() throws IOException {
    ArrayList<genome> alg = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream( "genes.aaf" )) {
      genome g = genome.factory( fis );
      while( g != null ) {
        alg.add( g );
        g = genome.factory( fis );
      }
    }

    AntBoard.proto //
            p1 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 ),
            p2 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 ),
            p3 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 );

    p2.set_block( 300, 100, 400, 600, true );
    p2.set_block( 100, 300, 600, 400, true );
    p3.set_block( 100, 500, 600, 600, true );
    p3.set_block( 100, 100, 600, 200, true );
    p3.set_block( 300, 100, 400, 600, true );

    //primordial_soup( p3, 100, 10, 100, 10000 );
    //for( genome abg : alg )
    //  rate_genome( abg, p3, 100, 10000 );
    display_genome_test( alg.get( 0 ), p3, 100, 10, 2000 );
    //rate_genome( alg.get( 0 ), p3, 100, 10000 );
    //rate_genome( alg.get( 6 ), p3, 100, 10000 );
    //rate_genome( alg.get( 7 ), p3, 100, 10000 );
    //measure_genome( alg.get( 0 ), p3, 1000, MAX_SIZE * MAX_SIZE );
//    display_genome_test( alg.get( 0 ), p3, ITERATIONS * 10, 5, 3000 );
    //if ( true )
//      return;
    /*
     genome g_hard = new genome( new Gene( 1.0, 1.0 ) {
     @Override
     public double epsilon( double x ) {
     return x * x * x;
     }
     }, new Gene( 1.0, 1.0 ) {
     @Override
     public double epsilon( double x ) {
     return x * x * x;
     }
     }, Gene.NULL_GENE, Gene.NULL_GENE, Gene.NULL_GENE, Gene.NULL_GENE );
     */
  }

  private Test() {
  }

}
