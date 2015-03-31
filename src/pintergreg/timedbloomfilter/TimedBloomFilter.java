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

import ie.ucd.murmur.MurmurHash;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimedBloomFilter {

    private int m;
    private int k;
    //let it be ms
    private int ttl;
    private double precision;
    private int current;
    ////private BitSet[] buckets;
    private Object lock = new Object();

    private AtomicInteger time = new AtomicInteger(0);
    public static boolean stop = false;

    private byte[] buckets;

    public TimedBloomFilter(int m, int k, int ttl) {
        this.m = m;
        this.k = k;
        this.ttl = ttl;
        this.current = 1;
        
        this.buckets = new byte[m];
        for (int i = 0; i < this.m; i++) {
            buckets[i] = 0;
        }

    }

    public void add(String key) {

        int c = time.get();
        for (int i : multiHash(key, this.k)) {
            ////this.buckets[i].set(c);
            this.buckets[i] = (byte) c;
            //System.out.println(Math.abs(i % this.m));
        }
    }

    public boolean include(String key) {
        boolean result = true;
        int c = time.get()*1000;

        for (int i : multiHash(key, this.k)) {

            byte b=this.buckets[i];
            //if (b == 0 || b+ttl<c){
            if (b+this.ttl>255){
                if (b == 0 || (b+this.ttl-255) <c){
                    result = false;
                    this.buckets[i]=0;
                }
            }else{
                if (b == 0 || b+this.ttl <c){
                    result = false;
                    this.buckets[i]=0;
                }
            }
            System.err.println("TIME::"+this.time.get()+", B:"+b);
        }

        return result;
    }

    public void startTimer() {
        this.time.set(1);
        new Thread("") {
            public void run() {

                while (!stop) {
                    try {
                        sleep(1000);
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
        time.incrementAndGet();
    }

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
