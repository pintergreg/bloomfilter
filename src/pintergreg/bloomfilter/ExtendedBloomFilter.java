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
 * Extended version of @{link BloomFilter}, that adds size parameter to follow
 * the number of included elements, and the relevant methods.
 *
 * @author Gergő Pintér
 */
public class ExtendedBloomFilter extends BloomFilter implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private int size;
    private final int n;

    /**
     * Create Extended Bloom Filter based on bitvector size and the numbers of
     * hash functions
     *
     * @param m - size of the bitvector
     * @param k - number of the hash functions
     *
     * It is not recommended to use this constructor, unless you really know
     * what you do
     */
    public ExtendedBloomFilter(int m, int k) {
        super(m, k);
        this.n = (int) Math.floor(m * 0.6931471805599453D / k);
        this.size = 0;
    }

    /**
     * Create Extended Bloom Filter based on item number and false positive
     * probability
     *
     * @param n - number of elements to be stored
     * @param p - false positive probability
     */
    public ExtendedBloomFilter(int n, double p) {
        super(n, p);
        this.n = n;
        this.size = 0;
    }

    /**
     * @return the number of stored elements
     */
    public int getSize() {
        return size;
    }

    /**
     * Add item to Bloom Filter
     *
     * @param key - an item to be added to the Bloom Filter
     */
    @Override
    public void add(byte[] key) {
        this.size++;
        super.add(key);
    }

    /**
     * @return True if Extended Bloom Filter is full, False otherwise
     */
    public boolean isFull() {
        return size == this.n;
    }

    /**
     * Search item in the Bloom Filter based on indexes. This method is used for
     * a more optimal search in @{link ScalableBloomFilter}
     *
     * @param indexes - check whether indexes point to one value bits int the
     * Bloom Filter bitvector
     * @return True if all the indexes points to one value bit, False otherwise
     */
    public boolean include(int[] indexes) {
        boolean result = true;

        for (int i : indexes) {
            if (super.bitSet.get(i) == false) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Clear the Bloom Filter, set every bit to zero in the bitvector
     */
    public void clear() {
        this.size = 0;
        super.bitSet.clear();
    }

}
