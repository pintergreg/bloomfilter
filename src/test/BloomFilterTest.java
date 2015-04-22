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
package test;

import java.nio.ByteBuffer;
import pintergreg.bloomfilter.A2BloomFilter;
import pintergreg.bloomfilter.BloomFilter;
import pintergreg.bloomfilter.ScalableBloomFilter;

public class BloomFilterTest {
    
    public static void main(String[] args) throws InterruptedException {
        
        System.out.println("Testing Basic Bloom Filter\n\texpected output: True, True, False");
        basicBloomFilterTest();
        System.out.println("\nTesting Scalable Bloom Filter\n\texpected output: 2");
        ScalableBloomFilterTest();
        System.out.println("\nTesting A2 Bloom Filter\n\texpected output: True, False, True, False");
        A2BloomFilterTest();
    }

    /**
     * Test for the basic Bloom Filter class
     */
    private static void basicBloomFilterTest() {
        // Create a Bloom Filter for 1000 elements with 0.001 false positive rate
        BloomFilter bf = new BloomFilter(1000, 0.001);

        // Add two elements
        bf.add("alma".getBytes());
        bf.add("körte".getBytes());

        // Search the above added two elements and a never added one
        System.out.println(bf.include("alma".getBytes())); // Expected output: True
        System.out.println(bf.include("körte".getBytes())); // Expected output: True
        System.out.println(bf.include("szilva".getBytes())); // Expected output: False
    }

    /**
     * Test for Scalable Bloom Filter class
     */
    private static void ScalableBloomFilterTest() {
        // Create a scalable Bloom Filter for storing 1000 elements with 0.001 false positive rate
        ScalableBloomFilter sbf = new ScalableBloomFilter(1000, 0.001);
        
        // Add more than 1000 elements and the scalable Bloom Filter should scale
        for (int i = 0; i < 1200; i++) {
            sbf.add(ByteBuffer.allocate(4).putInt(i).array());
        }
        
        System.out.println(sbf.getSize()); // Expected output: 2
    }

    /**
     * Test for A2 Bloom Filter
     *
     * @throws InterruptedException because of the timer thread in the A2 Bloom
     * Filter
     */
    private static void A2BloomFilterTest() throws InterruptedException {
        // Create an A2 Bloom Filter for storing 1000 elements 
        // with 0.001 false positive rate and 1000 msec time to live parameter
        A2BloomFilter bf = new A2BloomFilter(1000, 0.001, 1000);

        // Start timer
        //bf.startTimer();

        // Add two elements to the Bloom Filter
        bf.add("alma".getBytes());
        bf.add("körte".getBytes());

        // Test for two elements, the second added and a never added one
        System.out.println(bf.include("körte".getBytes())); // Expected output: True
        System.out.println(bf.include("szilva".getBytes())); //Expected output: False

        // Wait for 0.75 seconds
        Thread.sleep(750);
        System.out.println(bf.include("körte".getBytes())); // Expected output: True
        // Wait for 1.5 seconds
        Thread.sleep(1500);

        // And test the second added element
        System.out.println(bf.include("körte".getBytes())); //Expected output: False

        // Finally Stop timer
        bf.stopTimer();
        
    }
    
}
