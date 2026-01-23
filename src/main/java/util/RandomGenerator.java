package util;

import java.util.Random;

/**
 * Utility class for generating random numbers following various distributions.
 * Used for simulating patient arrivals and service times.
 */
public class RandomGenerator {
    private static final Random random = new Random();
    
    /**
     * Generates a random number following an exponential distribution.
     * Used for inter-arrival times and service times in queueing models.
     * 
     * @param rate The rate parameter (lambda) of the exponential distribution
     * @return A random value from the exponential distribution
     */
    public static double generateExponential(double rate) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be positive");
        }
        // Generate uniform random number between 0 and 1
        double u = random.nextDouble();
        // Avoid log(0) by ensuring u is not exactly 1
        if (u >= 1.0) {
            u = 0.999999;
        }
        // Transform to exponential distribution: -ln(1-u) / rate
        return -Math.log(1.0 - u) / rate;
    }
    
    /**
     * Generates a random number following a uniform distribution.
     * 
     * @param min Minimum value
     * @param max Maximum value
     * @return A random value uniformly distributed between min and max
     */
    public static double generateUniform(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("Min must be less than max");
        }
        return min + random.nextDouble() * (max - min);
    }
}
