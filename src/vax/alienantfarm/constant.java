package vax.alienantfarm;

/**

 @author toor
 */
public interface Constant {
  double PI_INV = 1.0 / Math.PI;
  double E_INV = 1.0 / Math.E;
  int STRIDE = 14;
  double GENE_MIN = 0, GENE_MAX = 1,
          MIX_MIN = 0.25, MIX_MAX = 0.75, MIX_RANGE = MIX_MAX - MIX_MIN,
          BASE_MUT_MAX = 0.1, BASE_MUT_RANGE = 2 * BASE_MUT_MAX,
          BASE_RND_MIN = 0, BASE_RND_MAX = 1, BASE_RND_RANGE = BASE_RND_MAX - BASE_RND_MIN;
  double PHERO_DISP_RATE = 1.0 - E_INV, PHERO_DISP_RATE_REDUCED = 1.0 - E_INV * E_INV,
          PHERO_STRIDE_SPREAD = Math.E, PHERO_RATE_ALPHA = 1;
  int PHERO_SPREAD_RADIUS = (int) ( STRIDE * PHERO_STRIDE_SPREAD * 0.5 ), // *0.5 to convert from diameter to radius
          PHERO_SPREAD_RADIUS_SQ = PHERO_SPREAD_RADIUS * PHERO_SPREAD_RADIUS;
  double PHERO_SPREAD_RADIUS_INV = 1.0 / PHERO_SPREAD_RADIUS;
  double DIST_THRESHOLD = STRIDE;
  double AGE_EXP = 0.95;
  double RELATIVELY_BLOCKED_WEIGHT = 1E-6;
  double MAX_PHERO_AMP = Math.E, NEG_PHERO_TRESHOLD = Math.E, TIME_AMP_THRESHOLD = 1E-2;
}
