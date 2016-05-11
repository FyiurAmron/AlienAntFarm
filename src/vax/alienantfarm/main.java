package vax.alienantfarm;

/**

 @author toor
 */
public class Main {
  /**
   @param args the command line arguments
   @throws java.lang.Exception
   */
  public static void main( String[] args ) throws Exception {
    if ( args.length >= 1 ) {
      switch ( args[0] ) {
        case "--help":
        case "/?":
          System.out.println( "usage: java -jar AlienAntFarm.jar mode_nr board_nr genome_nr\n"
                  + "  mode_nr:\n"
                  + "    0: display_genome_test\n"
                  + "    1: display_fast_genome_test\n"
                  + "    2: measure_genome\n"
                  + "    3: rate_genome\n"
                  + "    4: rate_genome ALL\n"
                  + "    5: primordial_soup\n" );
          return;
      }
      if ( args.length >= 3 ) {
        Test.test( Integer.parseInt( args[0] ), Integer.parseInt( args[1] ), Integer.parseInt( args[2] ) );
        return;
      }
    }
    Test.test( 0, 2, 0 );
  }

}
