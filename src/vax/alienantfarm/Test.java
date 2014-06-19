package vax.alienantfarm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeSet;
import javax.swing.JFrame;
import vax.sqvaardcraft.ui.SC_Image;

/**

 @author toor
 */
public class Test {
  final static protected int //
          MAX_SIZE = constant.STRIDE * 50,
          ITERATIONS = 5;

  static public class CounterAntObserver implements AntObserver {
    protected class result {
      public int it_start_sum, it_sum, it_end_sum;

      public void reset() {
        it_start_sum = 0;
        it_sum = 0;
        it_end_sum = 0;
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
    protected int delay;
    protected SC_Image parent;

    public BufferedImageAntObserver( int size_x, int size_y, int delay, SC_Image parent ) {
      this.size_x = size_x;
      this.size_y = size_y;
      this.delay = delay;
      this.parent = parent;
    }

    public BufferedImage getBufferedImage() {
      return bi;
    }

    @Override
    public void init( AntBoard.ant a ) {
      bi = new BufferedImage( size_x, size_y, BufferedImage.TYPE_INT_ARGB );
      parent.set_image( bi );
      AntBoard ab = a.get_board();
      for( int x = 0; x < MAX_SIZE; x++ ) {
        boolean[] abbpb_x = ab.board_proto.block[x];
        for( int y = 0; y < MAX_SIZE; y++ )
          bi.setRGB( x, y, abbpb_x[y] ? BLOCK_COLOR : BOARD_COLOR );
      }
      bi.setRGB( ab.exit_x, ab.exit_y, SPECIAL_COLOR );
      last_x = a.pos_x;
      last_y = a.pos_y;
      bi.setRGB( last_x, last_y, ANT_COLOR );
      if ( parent != null )
        parent.repaint();
    }

    @Override
    public void step( AntBoard.ant a ) {
      bi.setRGB( last_x, last_y, TRACE_COLOR );
      last_x = a.pos_x;
      last_y = a.pos_y;
      bi.setRGB( last_x, last_y, ANT_COLOR );
      if ( parent != null )
        parent.repaint();
      try {
        Thread.sleep( delay );
      } catch (InterruptedException ex) {
      }
    }

    @Override
    public void finish( AntBoard.ant a ) {
      //System.out.println( "success!" );
      AntBoard ab = a.get_board();
      bi = new BufferedImage( MAX_SIZE, MAX_SIZE, BufferedImage.TYPE_INT_ARGB );
      parent.set_image( bi );
      for( int x = 0; x < MAX_SIZE; x++ )
        for( int y = 0; y < MAX_SIZE; y++ )
          bi.setRGB( x, y, ab.get_color( x, y ) );
      if ( parent != null )
        parent.repaint();
      try {
        Thread.sleep( 100 * delay );
      } catch (InterruptedException ex) {
      }
    }
  }

  static public void test1() {
    JFrame jf = new JFrame();
    SC_Image img = new SC_Image( MAX_SIZE, MAX_SIZE );
    BufferedImageAntObserver biao = new BufferedImageAntObserver( MAX_SIZE, MAX_SIZE, 100, img );
    jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    jf.add( img );
    jf.pack();
    jf.setVisible( true );

    AntBoard.proto //
            p1 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 ),
            p2 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 ),
            p3 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 );
    p2.set_block( 300, 100, 400, 600, true );
    p2.set_block( 100, 300, 600, 400, true );
    p3.set_block( 100, 500, 600, 600, true );
    p3.set_block( 100, 100, 600, 200, true );
    p3.set_block( 300, 100, 400, 600, true );

    AntBoard ab = new AntBoard( p2, biao );

    AntBoard.genome g_pheroless = new AntBoard.genome(
            new Gene( 1, 0 ), new Gene( 1, 0 ), Gene.NULL_GENE,
            Gene.NULL_GENE, Gene.NULL_GENE, Gene.NULL_GENE );
    AntBoard.genome g_pheroagg = new AntBoard.genome(
            new Gene( 1, 0 ), new Gene( 1, 0 ), Gene.NULL_GENE,
            new Gene( 1, 0 ), Gene.NULL_GENE, new Gene( 1, 0 ) );
    //AntBoard.genome g = g_pheroagg;
    //AntBoard.genome g = new AntBoard.genome( new Gene( 1, 0 ), Gene.NULL_GENE, new Gene( 1, 0 ), new Gene( 1, 0 ), new Gene( 1, 0 ), new Gene( 1, 0 ) );
    AntBoard.genome g = new AntBoard.genome();
    System.out.println( g );
    ab.run_iterations( ITERATIONS, g, true );

  }

  protected static class genome_rater implements Comparable<genome_rater> {
    public AntBoard.genome g;
    public double rating;

    protected genome_rater( AntBoard.genome g, double rating ) {
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

  static public CounterAntObserver.result rate_genome( AntBoard.genome g, AntBoard.proto abp, int iterations, int hard_limit ) {
    System.out.println(
            "#============================================================\n"
            + g + "^" );
    CounterAntObserver cao = new CounterAntObserver( iterations, iterations / 5, hard_limit );
    AntBoard ab = new AntBoard( abp, cao );
    try {
      ab.run_iterations( iterations, g, true );
    } catch (AntException ex) {
      System.out.println( ex );
    }
    System.out.println( "# Iterations: " + iterations
            + "\n# rating START = " + cao.r.r_start()
            + "\n# rating AVG = " + cao.r.r_avg()
            + "\n# rating END = " + cao.r.r_end()
            + "\n" );
    return cao.r;
  }

  static public void test2() {
    AntBoard.proto p1 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 );

    p1.set_block( 300, 100, 400, 600, true );
    p1.set_block( 100, 300, 600, 400, true );
    /*
     AntBoard.genome g_pheroless = new AntBoard.genome(
     new Gene( 1, 0 ), new Gene( 1, 0 ), Gene.NULL_GENE,
     Gene.NULL_GENE, Gene.NULL_GENE, Gene.NULL_GENE );
     AntBoard.genome g_pheroagg = new AntBoard.genome(
     new Gene( 1, 0 ), new Gene( 1, 0 ), Gene.NULL_GENE,
     new Gene( 1, 0 ), Gene.NULL_GENE, new Gene( 1, 0 ) );
     */
    //AntBoard.genome g = g_pheroagg;
    //AntBoard.genome g = g_pheroless;
    //AntBoard.genome g = new AntBoard.genome( new Gene( 1, 0 ), Gene.NULL_GENE, new Gene( 1, 0 ), new Gene( 1, 0 ), new Gene( 1, 0 ), new Gene( 1, 0 ) );
    TreeSet<genome_rater> start = new TreeSet<>(), avg = new TreeSet<>(), end = new TreeSet<>();
    for( int i = 0; i < 100; i++ ) {
      AntBoard.genome g = new AntBoard.genome();
      CounterAntObserver.result caor = rate_genome( g, p1, ITERATIONS, MAX_SIZE * MAX_SIZE );
      start.add( new genome_rater( g, caor.r_start() ) );
      avg.add( new genome_rater( g, caor.r_avg() ) );
      end.add( new genome_rater( g, caor.r_end() ) );
    }
    System.out.println( "start:\n" + start.first() + "avg:\n" + avg.first() + "end:\n" + end.first() );
  }

  static public void test3() throws IOException {
    AntBoard.proto p1 = new AntBoard.proto( MAX_SIZE, MAX_SIZE, MAX_SIZE - 1, MAX_SIZE / 2, 0, MAX_SIZE / 2 );

    p1.set_block( 300, 100, 400, 600, true );
    p1.set_block( 100, 300, 600, 400, true );

    FileInputStream fis = new FileInputStream( "genes.aaf" );
    AntBoard.genome abga[] = { new AntBoard.genome( fis ), new AntBoard.genome( fis ), new AntBoard.genome( fis ) };
    for( AntBoard.genome abg : abga )
      rate_genome( abg, p1, 10000, MAX_SIZE * MAX_SIZE );
  }

  private Test() {
  }

}
