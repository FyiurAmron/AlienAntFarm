package vax.alienantfarm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import vax.sqvaardcraft.ui.SC_Image;

/**

 @author toor
 */
public class main {
  /**
   @param args the command line arguments
   */
  public static void main( String[] args ) {
    int max_size = constant.STRIDE * 50;
    JFrame jf = new JFrame();
    BufferedImage[] bi = { new BufferedImage( max_size, max_size, BufferedImage.TYPE_INT_ARGB ) };
    SC_Image img = new SC_Image( bi[0] );
    jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    jf.add( img );
    jf.pack();
    jf.setVisible( true );
    int ANT_COLOR = Color.RED.getRGB(),
            BOARD_COLOR = Color.BLACK.getRGB(),//Color.WHITE.getRGB(),
            BLOCK_COLOR = Color.WHITE.getRGB(),//Color.BLACK.getRGB(),
            TRACE_COLOR = Color.YELLOW.getRGB(),
            SPECIAL_COLOR = Color.GREEN.getRGB();
    AntBoard ab = new AntBoard( max_size, max_size, max_size - 1, max_size / 2, 0, max_size / 2, null );
    AntObserver ao = new AntObserver() {
      private int last_x, last_y;

      @Override
      public void init( AntBoard.ant a ) {
        last_x = a.pos_x;
        last_y = a.pos_y;
        bi[0].setRGB( last_x, last_y, ANT_COLOR );
      }

      @Override
      public void step( AntBoard.ant a ) {
        bi[0].setRGB( last_x, last_y, TRACE_COLOR );
        last_x = a.pos_x;
        last_y = a.pos_y;
        try {
          bi[0].setRGB( last_x, last_y, ANT_COLOR );
          Thread.sleep( 50 );
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        //System.out.println( last_x + " " + last_y );
        img.repaint();
      }

      @Override
      public void finish( AntBoard.ant a ) {
        System.out.println( "success!" );
        bi[0] = new BufferedImage( max_size, max_size, BufferedImage.TYPE_INT_ARGB );
        img.set_image( bi[0] );
        for( int x = 0; x < max_size; x++ )
          for( int y = 0; y < max_size; y++ )
            bi[0].setRGB( x, y, ab.is_blocked( x, y ) ? BLOCK_COLOR : BOARD_COLOR );
        bi[0].setRGB( ab.exit_x, ab.exit_y, SPECIAL_COLOR );
      }
    };
    ab.ao = ao;
    ab.set_block( 300, 100, 400, 600, true );
    ab.set_block( 100, 300, 600, 400, true );
    for( int x = 0; x < max_size; x++ )
      for( int y = 0; y < max_size; y++ )
        bi[0].setRGB( x, y, ab.is_blocked( x, y ) ? BLOCK_COLOR : BOARD_COLOR );
    bi[0].setRGB( ab.exit_x, ab.exit_y, SPECIAL_COLOR );
    //genome g = new genome( new Gene( 1, 0 ), new Gene( 1, 0 ), Gene.NULL_GENE, Gene.NULL_GENE, Gene.NULL_GENE, Gene.NULL_GENE );
    AntBoard.genome g = new AntBoard.genome();
    System.out.println( g );
    //AntBoard.genome g = new AntBoard.genome( new Gene( 1, 0 ), Gene.NULL_GENE, new Gene( 1, 0 ), new Gene( 1, 0 ), new Gene( 1, 0 ), new Gene( 1, 0 ) );
    ab.run_iterations( 100, g );
  }

}
