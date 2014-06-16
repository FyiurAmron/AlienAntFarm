package vax.alienantfarm;

/**

 @author toor
 */
public interface AntObserver {
  void init( AntBoard.ant a );

  void step( AntBoard.ant a );

  void finish( AntBoard.ant a );

  AntObserver NULL_OBSERVER = new AntObserver() {

    @Override
    public void init( AntBoard.ant a ) {
    }

    @Override
    public void step( AntBoard.ant a ) {
    }

    @Override
    public void finish( AntBoard.ant a ) {
    }

  };
}
