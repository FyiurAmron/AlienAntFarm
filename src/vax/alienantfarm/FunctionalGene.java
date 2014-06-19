package vax.alienantfarm;

/**

 @author toor
 */
public interface FunctionalGene {
  double epsilon( double x );

  FunctionalGene //
          NULL_FUNCTIONAL_GENE = (double x) -> 0,
          LINEAR_FUNCTIONAL_GENE = (double x) -> x;
}
