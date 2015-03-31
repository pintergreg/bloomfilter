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
package pintergreg.timedbloomfilter;

import pintergreg.timedbloomfilter.TimedBloomFilter;
import ie.ucd.murmur.MurmurHash;
import static java.lang.Thread.sleep;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gergő Pintér
 */
public class TBF {
    private final int m;
    private final int k;
    private final int ttl;

    private AtomicInteger time = new AtomicInteger(0);
    public static boolean stop = false;

    private int[] buckets;

    public TBF(int m, int k, int ttl) {
        this.m = m;
        this.k = k;
        this.ttl = ttl;
        
        this.buckets = new int[m];
        for (int i = 0; i < this.m; i++) {
            buckets[i] = 0;
        }

    }

    public void add(String key) {
        int c = time.get();
        for (int i : multiHash(key, this.k)) {
            this.buckets[i] = c;
        }
    }

    public boolean include(String key) {
        boolean result = true;
        int currTime = time.get();

        for (int i : multiHash(key, this.k)) {

            int b=this.buckets[i];
            
                if (b == 0 || b+this.ttl+1 < currTime){
                    result = false;
                    this.buckets[i]=0;
                }
            
            System.err.println("TIME::"+currTime+", BUCKET:"+b);
        }

        return result;
    }
    
    /*
       .......... TIMEING .......... 
    */

    public void startTimer() {
        this.time.set(1);
        new Thread("") {
            public void run() {

                while (!stop) {
                    try {
                        sleep(ttl*1000);
                        incTime();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TimedBloomFilter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }.start();

    }

    public void stopTimer() {
        stop = true;
    }

    public int getTime() {
        return this.time.get();
    }

    private void incTime() {
        if (time.incrementAndGet() == 8) {
            time.set(1);
        }
    }

    /*
       .......... HASHING .......... 
    */
    
    /*
     * @param key 
     * @param k number of hash functions
     */
    private int[] multiHash(String key, int k) {
        int[] result = new int[k];
        long h = MurmurHash.hash64(key); //get a 64 bit murmur hash
        int a = (int) ((h & 0xFFFFFFFF00000000L) >> 32); //higher bits
        int b = (int) (h & 0xFFFFFFFF); //lower bits

        //create k pseudo hash
        for (int i = 0; i < k; i++) {
            //result[i]=a+b*(i+1);
            //this hash is a signed 32 bit integer, but an unsigned value required in the [0,m] interval.
            //so mod m and Math.abs used
            result[i] = Math.abs((a + b * (i + 1)) % this.m);
        }

        return result;
    }
}
