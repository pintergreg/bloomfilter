# Java Bloom Filter library

![Apache v2](https://img.shields.io/hexpm/l/plug.svg)

My Bloom Filter<sup>[1](#fn1)</sup>, Scalable Bloom Filter<sup>[2](#fn2)</sup> and A2 Bloom Filter<sup>[3](#fn3)</sup> implemetation in Java for my Master's Thesis. The repository contains a NetBeans project and it is not under active development anymore.
 

## Containing classes
* BloomFilter
	* Basic Bloom Filter implementation
* ExtendedBloomFilter
	* Extends the basic one and adds size parameter to follow the number of included elements, and the relevant methods.
* ScalableBloomFilter
	* Implementation of Scalable Bloom Filter that extends its capacity dynamically if the Bloom Filter gets saturated. This extension means creating new Bloom Filters that are linked through a LinkedList.
* A2BloomFilter
	* A2 Bloom Filter consists of two ScalableBloomFilter. Elements added to the active one and at one time only one of them is active, but both of them is read when an element is searched. Active is changed after the given time and the new active is cleared. In this way an element is surely in the Bloom Filter at least for the specified time.
* BloomFilterUtils
	* Common methods for Bloom Filter implementations

### Class diagram generated with easyUML Netbeans plugin
![Bloom Filter library UML Class diagram](/markdown/easyUML.png "Bloom Filter library UML Class diagram")

## Bibliography
- <a name="fn1">[1] </a>: Bloom, Burton H. "Space/time trade-offs in hash coding with allowable errors." Communications of the ACM 13.7 (1970): 422-426.
-  <a name="fn2">[2] </a>: Almeida, Paulo Sérgio, et al. "Scalable bloom filters." Information Processing Letters 101.6 (2007): 255-261.
-  <a name="fn3">[3] </a>: Yoon, MyungKeun. "Aging bloom filter with two active buffers for dynamic sets." Knowledge and Data Engineering, IEEE Transactions on 22.1 (2010): 134-138.
