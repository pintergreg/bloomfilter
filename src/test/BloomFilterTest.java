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

import pintergreg.bloomfilter.A2BloomFilter;
import pintergreg.bloomfilter.BloomFilter;
import pintergreg.timedbloomfilter.TBF;
import pintergreg.timedbloomfilter.TimedBloomFilter;

public class BloomFilterTest {
    
    public static void main(String[] args) throws InterruptedException{

        A2BFTest();
        
    }
    
    private static void simpleBFTest(){
        BloomFilter bf=new BloomFilter(1000, 3);
        bf.add("alma".getBytes());
        bf.add("körte".getBytes());
        
        System.out.println(bf.include("alma".getBytes()));
        System.out.println(bf.include("körte".getBytes()));
        System.out.println(bf.include("szilva".getBytes()));
        
        System.out.println("-----------");
    }
    
    private static void simpleTimingTest() throws InterruptedException{
        TimedBloomFilter tbf=new TimedBloomFilter(1000, 3, 2);
        
//        System.out.println(tbf.include("alma"));
//        System.out.println(tbf.include("körte"));
//        System.out.println(tbf.include("szilva"));
        
        tbf.startTimer();
        tbf.add("alma");
        tbf.add("körte");
        System.out.println(tbf.include("körte"));
        System.out.println(tbf.include("szilva"));
        System.out.println(tbf.getTime());
        Thread.sleep(3000);
        System.out.println(tbf.getTime());
        
        System.out.println(tbf.include("körte"));
        System.out.println(tbf.include("szilva"));
        tbf.stopTimer();
    }
    
    private static void TBFTest() throws InterruptedException{
        TBF tbf=new TBF(1000, 3, 2);
        
        tbf.startTimer();
        tbf.add("alma");
        tbf.add("körte");
        System.out.println(tbf.include("körte"));
        System.out.println(tbf.include("szilva"));
        System.out.println(tbf.getTime());
        Thread.sleep(3000);
        System.out.println(tbf.getTime());
        
        System.out.println(tbf.include("körte"));
        //System.out.println(tbf.include("szilva"));
        tbf.stopTimer();
        
    }
    
    /**
     * Test for A2 Bloom Filter
     * @throws InterruptedException 
     */
    private static void A2BFTest() throws InterruptedException{
        // Create an A2 Bloom Filter for storing 1000 elements 
        // with 0.001 false positive rate and 1000 msec time to live parameter
        A2BloomFilter bf=new A2BloomFilter(1000, 0.001, 1000);
        
        // Start timer
        bf.startTimer();
        
        // Add two elements to the Bloom Filter
        bf.add("alma".getBytes());
        bf.add("körte".getBytes());
        
        // Test for two elements, the second added and a never added one
        System.out.println(bf.include("körte".getBytes())); // Expected output: True
        System.out.println(bf.include("szilva".getBytes())); //Expected output: False
        
        // Wait for 2 seconds
        Thread.sleep(2000);

        // And test the second added element
        System.out.println(bf.include("körte".getBytes())); //Expected output: False
        
        // Finally Stop timer
        bf.stopTimer();
        
    }
    
}
