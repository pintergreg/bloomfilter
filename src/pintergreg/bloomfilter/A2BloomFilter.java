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
import static java.lang.Thread.sleep;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A2 Bloom Filter consists of two @{link ScalableBloomFilter}. Elements added
 * to the active one and at one time only one of them is active, but both of
 * them is read when an element is searched. Active is changed after the given
 * time and the new active is cleared. In this way an element is surely in the
 * Bloom Filter at least for the specified time.
 *
 * @author Gergő Pintér
 */
public class A2BloomFilter implements Serializable{

    private static final long serialVersionUID = 1L;
    private ScalableBloomFilter[] bloomFilters = new ScalableBloomFilter[2];
    private final int m;
    private final int k;
    private final int ttl;
    public static boolean stop = false;
    private AtomicInteger active = new AtomicInteger(0);

    /**
     * Create A2 Bloom Filter based on bitvector size, the numbers of hash
     * functions and the time to live parameter.
     *
     * @param m - size of the bitvector
     * @param k - number of the hash functions
     * @param ttl - time to live, it determines in milliseconds how long the
     * elements need to be alive, be in the Bloom Filter
     *
     * It is not recommended to use this constructor, unless you really know
     * what you do
     */
    public A2BloomFilter(int m, int k, int ttl) {
        this.m = m;
        this.k = k;
        this.ttl = ttl;
        bloomFilters[0] = new ScalableBloomFilter(this.m, this.k);
        bloomFilters[1] = new ScalableBloomFilter(this.m, this.k);
    }

    /**
     * Create A2 Bloom Filter based on item number, false positive probability
     * and the time to live parameter.
     *
     * @param n - number of elements to be stored
     * @param p - false positive probability
     * @param ttl - time to live, it determines in milliseconds how long the
     * elements need to be alive, be in the Bloom Filter
     */
    public A2BloomFilter(int n, double p, int ttl) {
        this.ttl = ttl;
        // the user given p should be valid for the two Bloom Filter, 
        // so q means the false posizitive probablity for one Bloom Filter 
        double q = 1 - Math.sqrt(1 - p);
        this.m = BloomFilterUtils.determineSize(n, q);
        this.k = BloomFilterUtils.determineHashNumber(this.m, n);

        bloomFilters[0] = new ScalableBloomFilter(this.m, this.k);
        bloomFilters[1] = new ScalableBloomFilter(this.m, this.k);
        
        startTimer();
    }

    /**
     * Add item to A2 Bloom Filter
     *
     * @param key - an item to be added to the Bloom Filter
     */
    public void add(byte[] key) {
        bloomFilters[active.get()].add(key);
    }

    /**
     * Add item to A2 Bloom Filter
     *
     * @param key - a Long item to be added to the Bloom Filter
     */
    public void add(long key) {
        add(ByteBuffer.allocate(8).putLong(key).array());
    }

    /**
     * Search item in the A2 Bloom Filter
     *
     * @param key - an item to be searched in the A2 Bloom Filter
     * @return True if either Bloom Filter contains the given item, False
     * otherwise
     */
    public boolean include(byte[] key) {
        // Calculate indexes only once, then uses the index-based search method
        int[] indexes = BloomFilterUtils.multiHash(key, k, m);
        return bloomFilters[0].include(indexes) || bloomFilters[1].include(indexes);
    }

    /**
     * Search item in the A2 Bloom Filter
     *
     * @param key - a Long item to be searched in the A2 Bloom Filter
     * @return True if either Bloom Filter contains the given item, False
     * otherwise
     */
    public boolean include(long key) {
        return include(ByteBuffer.allocate(8).putLong(key).array());
    }

    /* .......... TIMEING .......... */
    /**
     * Starts the timer that ages the element according to the givel Time To
     * Live value
     */
    private void startTimer() {
        new Thread("") {
            @Override
            public void run() {

                while (!stop) {
                    try {
                        sleep(ttl);
                        switchActive();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(A2BloomFilter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }.start();

    }

    /**
     * Stops the timer that ages the element according to the givel Time To Live
     * value
     */
    public void stopTimer() {
        stop = true;
    }

    /**
     * Switches between the two Bloom Filter
     */
    private void switchActive() {
        int nextActive = this.active.get() == 0 ? 1 : 0;
        // Next Bloom Filter needs to be cleared
        this.bloomFilters[nextActive].clear();
        this.active.set(nextActive);
    }

}
