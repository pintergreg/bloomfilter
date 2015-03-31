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

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Implementation of Scalable Bloom Filter that extends its capacity dynamically
 * if the Bloom Filter gets saturated. This extension means creating new Bloom
 * Filters that are linked through a LinkedList.
 *
 * @author Gergő Pintér
 */
public class ScalableBloomFilter {

    private LinkedList<ExtendedBloomFilter> bloomFilters = new LinkedList<>();
    ListIterator<ExtendedBloomFilter> listIterator;
    private final int m;
    private final int k;

    /**
     * Create Scalable Bloom Filter based on item number and false positive
     * probability
     *
     * @param m - size of the bitvector
     * @param k - number of the hash functions
     */
    public ScalableBloomFilter(int m, int k) {
        this.m = m;
        this.k = k;
        bloomFilters.add(new ExtendedBloomFilter(this.m, this.k));
    }

    /**
     * Create Scalable Bloom Filter based on item number and false positive
     * probability
     *
     * @param n - number of elements to be stored
     * @param p - false positive probability
     */
    public ScalableBloomFilter(int n, double p) {
        this.m = BloomFilterUtils.determineSize(n, p);
        this.k = BloomFilterUtils.determineHashNumber(this.m, n);
        bloomFilters.add(new ExtendedBloomFilter(this.m, this.k));
    }

    /**
     * Add item to Scalable Bloom Filter
     *
     * @param key - an item to be added to the Bloom Filter
     */
    public void add(byte[] key) {
        bloomFilters.getLast().add(key);
        if (bloomFilters.getLast().isFull()) {
            bloomFilters.add(new ExtendedBloomFilter(m, k));
        }
    }

    /**
     * Search item in the Scalable Bloom Filter
     *
     * @param key - an item to be searched in the Scalable Bloom Filter
     * @return True if the element is found either Bloom Filter of the Linked
     * List, False otherwise.
     */
    public boolean include(byte[] key) {
        boolean result = false;
        if (bloomFilters.size() == 1) {
            result = bloomFilters.getFirst().include(key);
        } else {
            // Calculate indexes only once, then uses the index-based search method
            int[] indexes = BloomFilterUtils.multiHash(key, k, m);
            listIterator = bloomFilters.listIterator();
            while (listIterator.hasNext()) {
                result |= listIterator.next().include(indexes);
            }
        }
        return result;
    }
    
    /**
     * Search item in the Scalable Bloom Filter
     *
     * @param key - an item to be searched in the Scalable Bloom Filter
     * @return True if the element is found either Bloom Filter of the Linked
     * List, False otherwise.
     */
    boolean include(int[] indexes) {
        boolean result = false;
        if (bloomFilters.size() == 1) {
            // Indexes come from outside
            result = bloomFilters.getFirst().include(indexes);
        } else {
            listIterator = bloomFilters.listIterator();
            while (listIterator.hasNext()) {
                // Indexes come from outside
                result |= listIterator.next().include(indexes);
            }
        }
        return result;
    }

    /**
     * Clear the Bloom Filter, set every bit to zero in the bitvector
     */
    public void clear() {
        // Keep in mind that the Scalable Bloom Filter can be consist of more Extended Bloom Filter
        // I don't want to create new instance, because it is slow, I want to keep the first and drop the rest
        while (bloomFilters.size() > 1) {
            bloomFilters.removeLast();
        }
        bloomFilters.getFirst().clear();

    }
}
