/*
 * Copyright 2015 Gergő Pintér.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pintergreg.bloomfilter;

import java.io.Serializable;

/**
 * Common methods for Bloom Filter implementations
 *
 * @author Gergő Pintér
 */
public class BloomFilterUtils implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /**
     * Calculates k pseudo hash for the given key and maps them to an m long
     * vector
     *
     * @param key - the item to be hashed (byte[])
     * @param k - number of hash functions
     * @param m - is the real size of the BitSet in the Bloom Filter, the length
     * where the hashing maps
     * @return an array of indexes where the item is hashed to
     */
    public static int[] multiHash(byte[] key, int k, int m) {
        int[] result = new int[k];
        long h = redis.clients.util.MurmurHash.hash64A(key, 42); // get a 64 bit Murmur hash
        int a = (int) ((h & 0xFFFFFFFF00000000L) >> 32); // get higher bits
        int b = (int) (h & 0xFFFFFFFF); // get lower bits

        // create k pseudo hash
        for (int i = 0; i < k; i++) {
            // a + b * (i + 1) is a signed 32 bit integer, but an unsigned value 
            // required in the [0, m] interval, so mod m and Math.abs used
            result[i] = Math.abs((a + b * (i + 1)) % m);
        }

        return result;
    }

    /**
     * Determinesthe bitvector size for the Bloom Filter based on capacity and
     * false positive probability
     *
     * @param n - number of elements to be stored
     * @param p - false positive probability
     * @return the bitvector size
     */
    public static int determineSize(int n, double p) {
        // casted to integer, because it should not be larger than an integer
        // Math.log(2) * Math.log(2) = 0.4804530139182014D
        return (int) Math.ceil((n * Math.log(p)) / -0.4804530139182014D);
        //TODO throw exception if it is larger than maxint
    }

    public static int determineHashNumber(int m, int n) {
        // casted to integer, because it should not be a large number
        // Math.log(2) = 0.6931471805599453
        return (int) Math.ceil((m / n) * 0.6931471805599453D);
    }

}
