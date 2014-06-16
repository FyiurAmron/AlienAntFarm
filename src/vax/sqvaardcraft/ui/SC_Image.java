package vax.sqvaardcraft.ui;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

//import vax.sqvaardcraft.util.Disposable;

/**
 *   JComponent extension suitable for flexible displaying of BufferedImages with given alpha
 *   @author poponuro
 */
public class SC_Image extends JComponent /*implements Disposable*/ {
  final static public int DEFAULT_IMG_SIZE = 100;
  protected BufferedImage local_image;
  protected float local_alpha = 1.0f; // default alpha; float due to AlphaComposite.getInstance() requirements
  protected AlphaComposite local_composite;
  protected int local_AlphaComposite_rule = AlphaComposite.SRC_OVER; // default value - draw over the source

  public SC_Image() {
    local_composite = AlphaComposite.getInstance( local_AlphaComposite_rule, local_alpha );
  }

  public SC_Image( BufferedImage buf_img ) {
    this( buf_img, false );
  }

  public SC_Image( BufferedImage buf_img, boolean copy_image ) {
    this();
    set_local_image( buf_img, copy_image );
  }

  public SC_Image( int new_x_size, int new_y_size ) {
    this();
    //local_image = ImageToolkit.new_BufferedImage( new_x_size, new_y_size );
    local_image = new BufferedImage( new_x_size, new_y_size, BufferedImage.TYPE_INT_ARGB );
    set_JComponent( new_x_size, new_y_size );
  }

  public void set_local_image( BufferedImage buf_img ) {
    set_local_image( buf_img, false );
  }

  final public void set_local_image( BufferedImage buf_img, boolean copy_image ) {
    local_image = buf_img;
    set_JComponent( local_image.getWidth(), local_image.getHeight() );
    //if ( copy_image )
    //  local_image = ImageToolkit.copy( local_image );
  }

  final protected void set_JComponent( int x_size, int y_size ) {
    Dimension tmp_dim = new Dimension( x_size, y_size );
    setBounds( 0, 0, x_size, y_size );
    setMinimumSize( tmp_dim );
    setPreferredSize( tmp_dim );
  }
/*
  @Override
  public void dispose() {
    local_image = null;
  }
*/
  public BufferedImage get_image() {
    return local_image;
  }

  public void set_image( BufferedImage new_image ) {
    local_image = new_image;
  }

  public void set_alpha( float alpha ) {
    local_alpha = alpha;
    local_composite = AlphaComposite.getInstance( local_AlphaComposite_rule, local_alpha );
  }

  public void set_AlphaComposite_rule( int new_rule ) {
    local_AlphaComposite_rule = new_rule;
    local_composite = AlphaComposite.getInstance( local_AlphaComposite_rule, local_alpha );
  }

  public Graphics2D get_g2d() {
    return local_image.createGraphics();
  }

  @Override
  protected void paintComponent( Graphics g ) {
    super.paintComponent( g );
    Graphics2D g2d = (Graphics2D) g;
    g2d.setComposite( local_composite );
    g2d.drawImage( local_image, null, 0, 0 ); // no g2d.dispose() due to dispose's doc saying this is not the case for it
  }
}
