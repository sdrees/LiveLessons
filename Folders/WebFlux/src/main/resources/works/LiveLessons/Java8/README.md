This directory contains all the modern Java source code examples from
my LiveLessons course on [Java
Concurrency](http://www.dre.vanderbilt.edu/~schmidt/LiveLessons/CPiJava),
as well as some other examples from my Safari Live Training courses.
Here's an overview of what's included:

. ex0 - This example two zap() method implementations that removes
        strings from a list of strings.  One method uses basic Java 7
        features and the other uses basic Java 8 features.

. ex1 - This example shows how to use Java 8 lambda expressions and
        method references to sort elements of a collection.  It also
        shows how to use the Java 8 forEach() method.
  
. ex2 - This example shows the use of a simple lambda expression in
        the context of a Java List/ArrayList removeIf() method.
  
. ex3 - This example shows how Function objects can be composed
        together via andThen().
  
. ex4 - This example shows how a Java 7 BiFunction lambda can be used
        to replace all the values of all keys in a ConcurrentHashMap.
        It also contrasts the Java 8 BiFunction with a conventional
        Java 7 solution using a foreach loop.

. ex5 - This example shows how a Java 8 Consumer interface can be used
        with forEach() to print out the values in a list by binding
        the System.out println() method to the forEach() Consumer
        parameter.  It also shows how to sort a list in ascending and
        descending order using a Comparator and a Function interface.

. ex6 - This example shows how a Java 8 Supplier interface can be used
        to print a default value if a key is not found in a map.  It also
        shows how to use the Java 8 Optional class.
  
. ex7 - This example of shows how the Java 8 functional interfaces
        (including Supplier and a custom functional interface) can be
        used in conjunction with Java 8 constructor references.
  
. ex8 - This example shows how to reduce and/or multiply big fractions
        using a wide range of features in the Java completable futures
        framework, including many factory methods, completion stage
        methods, arbitrary-arity methods, and exception handling
        methods.

. ex9 - This example showcases the use of a Java 8 ConcurrentHashMap
        and a Java SynchronizedMap together with Java 8 Function-based
        method references to compute/cache/retrieve prime numbers.
        This example also demonstrates the use of stream slicing with
        takeWhile() and dropWhile().

. ex10 - This example shows the use of predicate lambda expressions in
         the context of a Java ConcurrentHashMap removeIf() method.

. ex11 - This example shows the improper use of the Stream.peek()
         aggregate operation to interfere with a running stream.

. ex12 - This program provides several examples of Java streams that
         show how it can be used with "pure" functions, i.e.,
         functions whose return values are only determined by their
         input values, without observable side effects.  This program
         also shows various stream terminal operations, including
         forEach(), collect(), and several variants of reduce().  In
         addition, it includes a non-Java streams example as a
         baseline.

. ex13 - This example shows several examples of using Java
         Spliterators and streams to traverse each word in a list
         containing a quote from a famous Shakespeare play.

. ex14 - This example shows the difference in overhead for using a
         parallel spliterator to split a Java LinkedList and an
         ArrayList into chunks.  It also shows the difference in
         overhead between combining and collecting LinkedList results
         in a parallel stream vs. sequential stream using concurrent
         and non-concurrent collectors.

. ex15 - This example shows the limitations of using inherently
         sequential Java 8 streams operations (such as iterate() and
         limit()) in the context of parallel streams.

. ex16 - This program implements various ways of computing factorials
         to demonstrate the performance of alternative techniques and
         the dangers of sharing unsynchronized state between threads.

. ex17 - This example shows various issues associated with using the
         Java streams reduce() terminal operation, including the need
         to use the correct identity value and to ensure operations
         are associative.  It also demonstrates what goes wrong when
         reduce() performs a mutable reduction on a parallel stream.

. ex18 - This program shows how to wait for the results of a stream of
         completable futures using (1) a custom collector and (2) the
         StreamsUtils.joinAll() method (which is a wrapper for
         CompletableFuture.allOf()).

. ex19 - This example shows how to count the number of images in a
         recursively-defined folder structure using a range of
         features in the Java completable future framework.

. ex20 - This example shows how to combine Java parallel streams with
         and without the ForkJoinPool.ManagedBlocker interface and the
         Java fork-join framework to download multiple images from a
         remote server.

. ex21 - This program shows the difference between stream sources
         (such as List) that enforce encounter order and stream
         sources (such as HashSet) that do not in the context of
         various order-sensitive aggregate operations, such as limit()
         and distinct().

. ex22 - This example shows how to reduce and multiply big fractions
         using the Java fork-join pool framework.

. ex23 - This example shows the difference between calling join() on
         intermediate completable futures (which block and thus
         degrade performance) vs. simply making one call to join()
         (and thus enhancing greater parallelism).  These tests
         demonstrate why join() shouldn't be used in a stream pipeline
         on a CompletableFuture that hasn't completed since it may
         impede parallelism.

. ex24 - This example shows the difference between a reentrant lock
         (e.g., Java ReentrantLock) and a non-reentrant lock (e.g.,
         StampedLock) when used in a framework that allows callbacks
         when a lock protecting state in the framework is held.

. ex25 - This example shows various ways to implement and apply
         synchronous and asynchronous memoizers using Java
         ExecutorService, functional interfaces, streams, and
         completable futures.  Memoization is described at
         https://en.wikipedia.org/wiki/Memoization.

. ex26 - This example shows various ways to apply one-shot and cyclic
         Java Phasers.

. ex27 - This example shows how to apply Java 9 timeouts with the Java
         completable futures framework.

. ex28 - This example shows how to implement the Singleton pattern
         via Java AtomicReference.

. ex29 - This example shows how to combine the Java sequential streams
         and completable futures framework to generate and reduce
         random big fractions.  It also demonstrates the lazy
         processing of streams.
