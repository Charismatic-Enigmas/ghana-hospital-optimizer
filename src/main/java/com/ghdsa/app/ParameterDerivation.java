package com.ghdsa.app;



public final class ParameterDerivation {

    
    private static final long[] MEMBER_INDEX_NUMBERS = {
        22407542L, 22387357L, 22065837L, 22059983L, 22297380L, 22408351L, 22150993L, 22368027L, 22046431L, 22375768L,
        22387934L, 22059210L, 22411279L, 22057575L, 22078485
    };

    /** Example: priority weight derived from the sum of index-number digits mod 10, plus 1. */
    public static int priorityWeight() {
        long sum = 0;
        for (long idx : MEMBER_INDEX_NUMBERS) sum += digitSum(idx);
        return (int) (sum % 10) + 1;
    }

    /** Example: route penalty derived from the last two digits of the first member's index number. */
    public static double routePenalty() {
        long last2 = MEMBER_INDEX_NUMBERS[0] % 100;
        return 1.0 + (last2 / 100.0); // e.g. 1.00 - 1.99
    }

    /** Example: hash table size — nearest prime above (sum of all index numbers mod 500). */
    public static int hashTableSize() {
        long sum = 0;
        for (long idx : MEMBER_INDEX_NUMBERS) sum += idx;
        long base = (sum % 500) + 101; // keep it reasonably sized
        return nextPrime((int) base);
    }

    /** Example: random seed for your data generator (see db/seed/generate_data.py) */
    public static long randomSeed() {
        long product = 1;
        for (long idx : MEMBER_INDEX_NUMBERS) product = (product * (idx % 97 + 1)) % 1_000_000;
        return product;
    }

    private static long digitSum(long n) {
        long sum = 0;
        while (n > 0) { sum += n % 10; n /= 10; }
        return sum;
    }

    private static int nextPrime(int from) {
        int n = from;
        while (!isPrime(n)) n++;
        return n;
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    private ParameterDerivation() {}
}
