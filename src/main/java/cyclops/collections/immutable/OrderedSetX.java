package cyclops.collections.immutable;


import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPOrderedSetX;
import com.aol.cyclops2.data.collections.extensions.persistent.PersistentCollectionX;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.OnEmptySwitch;
import com.aol.cyclops2.types.To;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface OrderedSetX<T> extends To<OrderedSetX<T>>,POrderedSet<T>, PersistentCollectionX<T>, OnEmptySwitch<T, POrderedSet<T>> {


    /**
     * Narrow a covariant OrderedSetX
     * 
     * <pre>
     * {@code 
     *  OrderedSetX<? extends Fruit> set = OrderedSetX.of(apple,bannana);
     *  OrderedSetX<Fruit> fruitSet = OrderedSetX.narrowK(set);
     * }
     * </pre>
     * 
     * @param setX to narrowK generic type
     * @return OrderedSetX with narrowed type
     */
    public static <T> OrderedSetX<T> narrow(final OrderedSetX<? extends T> setX) {
        return (OrderedSetX<T>) setX;
    }
    /**
     * Create a OrderedSetX that contains the Integers between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range OrderedSetX
     */
    public static OrderedSetX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .toPOrderedSetX();
    }

    /**
     * Create a OrderedSetX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range OrderedSetX
     */
    public static OrderedSetX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .toPOrderedSetX();
    }

    /**
     * Unfold a function into a OrderedSetX
     * 
     * <pre>
     * {@code 
     *  OrderedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return OrderedSetX generated by unfolder function
     */
    static <U, T> OrderedSetX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toPOrderedSetX();
    }

    /**
     * Generate a OrderedSetX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate OrderedSetX elements
     * @return OrderedSetX generated from the provided Supplier
     */
    public static <T> OrderedSetX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .toPOrderedSetX();
    }

    /**
     * Create a OrderedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return OrderedSetX generated by iterative application
     */
    public static <T> OrderedSetX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .toPOrderedSetX();

    }

    public static <T> OrderedSetX<T> of(final T... values) {
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.from(Arrays.asList(values)));
    }

    public static <T> OrderedSetX<T> empty() {
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.empty());
    }

    public static <T> OrderedSetX<T> singleton(final T value) {
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.singleton(value));
    }

    /**
     * Reduce a Stream to a OrderedSetX,
     * 
     * 
     * <pre>
     * {@code 
     *    OrderedSetX<Integer> set = OrderedSetX.fromStream(Stream.of(1,2,3));
     * 
     *  //set = [1,2,3]
     * }</pre>
     * 
     * 
     * @param stream to convert 
     * @return
     */
    public static <T> OrderedSetX<T> fromStream(final Stream<T> stream) {
        return Reducers.<T> toPOrderedSetX()
                       .mapReduce(stream);
    }

    public static <T> OrderedSetX<T> fromCollection(final Collection<T> stream) {
        if (stream instanceof OrderedSetX)
            return (OrderedSetX) stream;
        if (stream instanceof POrderedSet)
            return new LazyPOrderedSetX<>(
                                          (POrderedSet) stream);
        return new LazyPOrderedSetX<>(
                                      OrderedPSet.from(stream));
    }

    /**
     * Construct a OrderedSetX from an Publisher
     * 
     * @param publisher
     *            to construct OrderedSetX from
     * @return OrderedSetX
     */
    public static <T> OrderedSetX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher)
                          .toPOrderedSetX();
    }

    public static <T> OrderedSetX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof OrderedSetX)
            return (OrderedSetX) iterable;
        if (iterable instanceof POrderedSet)
            return new LazyPOrderedSetX<>(
                                          (POrderedSet) iterable);


        return new LazyPOrderedSetX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPOrderedSet());
    }

    public static <T> OrderedSetX<T> toPOrderedSet(final Stream<T> stream) {
        return Reducers.<T> toPOrderedSetX()
                       .mapReduce(stream);
    }


    @Override
    default OrderedSetX<T> materialize() {
        return (OrderedSetX<T>)PersistentCollectionX.super.materialize();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> OrderedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                    BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                    Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (OrderedSetX)PersistentCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> OrderedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                    BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                    Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                    Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (OrderedSetX)PersistentCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> OrderedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (OrderedSetX)PersistentCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> OrderedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (OrderedSetX)PersistentCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> OrderedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (OrderedSetX)PersistentCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> OrderedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (OrderedSetX)PersistentCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     OrderedSetX.of(1,2,3)
     *                 .map(i->i*2)
     *                 .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *     //OrderedSetX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed OrderedSetX
     */
    default <R> OrderedSetX<R> coflatMap(Function<? super OrderedSetX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);

    }

    @Override
    default OrderedSetX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default OrderedSetX<T> drop(final long num) {

        return skip(num);
    }
    @Override
    default OrderedSetX<T> toPOrderedSetX() {
        return this;
    }

    @Override
    default <R> OrderedSetX<R> unit(final Collection<R> col) {
        return fromCollection(col);
    }

    @Override
    default <R> OrderedSetX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> OrderedSetX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    @Override
    default <R> OrderedSetX<R> emptyUnit() {
        return empty();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /**
    * Combine two adjacent elements in a OrderedSetX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code 
    *  OrderedSetX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),Semigroups.intSum)
                 .toListX()
                 
    *  //ListX(3,4) 
    * }</pre>
    * 
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced OrderedSetX
    */
    @Override
    default OrderedSetX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (OrderedSetX<T>) PersistentCollectionX.super.combine(predicate, op);
    }
    @Override
    default OrderedSetX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (OrderedSetX<T>)PersistentCollectionX.super.combine(op,predicate);
    }


    @Override
    default <X> OrderedSetX<X> from(final Collection<X> col) {
        return fromCollection(col);
    }

    @Override
    default <T> Reducer<POrderedSet<T>> monoid() {
        return Reducers.toPOrderedSet();
    }

    /* (non-Javadoc)
     * @see org.pcollections.PSet#plus(java.lang.Object)
     */
    @Override
    public OrderedSetX<T> plus(T e);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#plusAll(java.util.Collection)
     */
    @Override
    public OrderedSetX<T> plusAll(Collection<? extends T> list);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#minus(java.lang.Object)
     */
    @Override
    public OrderedSetX<T> minus(Object e);

    /* (non-Javadoc)
     * @see org.pcollections.PSet#minusAll(java.util.Collection)
     */
    @Override
    public OrderedSetX<T> minusAll(Collection<?> list);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#reverse()
     */
    @Override
    default OrderedSetX<T> reverse() {
        return (OrderedSetX<T>) PersistentCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> filter(final Predicate<? super T> pred) {
        return (OrderedSetX<T>) PersistentCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> OrderedSetX<R> map(final Function<? super T, ? extends R> mapper) {
        return (OrderedSetX<R>) PersistentCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> OrderedSetX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (OrderedSetX<R>) PersistentCollectionX.super.flatMap(mapper);
    }

    @Override
    default OrderedSetX<T> takeRight(final int num) {
        return (OrderedSetX<T>) PersistentCollectionX.super.takeRight(num);
    }

    @Override
    default OrderedSetX<T> dropRight(final int num) {
        return (OrderedSetX<T>) PersistentCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limit(long)
     */
    @Override
    default OrderedSetX<T> limit(final long num) {
        return (OrderedSetX<T>) PersistentCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skip(long)
     */
    @Override
    default OrderedSetX<T> skip(final long num) {
        return (OrderedSetX<T>) PersistentCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> takeWhile(final Predicate<? super T> p) {
        return (OrderedSetX<T>) PersistentCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> dropWhile(final Predicate<? super T> p) {
        return (OrderedSetX<T>) PersistentCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> takeUntil(final Predicate<? super T> p) {
        return (OrderedSetX<T>) PersistentCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> dropUntil(final Predicate<? super T> p) {
        return (OrderedSetX<T>) PersistentCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> OrderedSetX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (OrderedSetX<R>) PersistentCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
     */
    @Override
    default OrderedSetX<T> slice(final long from, final long to) {
        return (OrderedSetX<T>) PersistentCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> OrderedSetX<T> sorted(final Function<? super T, ? extends U> function) {
        return (OrderedSetX<T>) PersistentCollectionX.super.sorted(function);
    }

    @Override
    default OrderedSetX<ListX<T>> grouped(final int groupSize) {
        return (OrderedSetX<ListX<T>>) PersistentCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> OrderedSetX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
                                                        final Collector<? super T, A, D> downstream) {
        return (OrderedSetX) PersistentCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> OrderedSetX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (OrderedSetX) PersistentCollectionX.super.grouped(classifier);
    }

    @Override
    default <U> OrderedSetX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (OrderedSetX) PersistentCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> OrderedSetX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (OrderedSetX<R>) PersistentCollectionX.super.zip(other, zipper);
    }


    @Override
    default <U, R> OrderedSetX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (OrderedSetX<R>) PersistentCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#permutations()
     */
    @Override
    default OrderedSetX<ReactiveSeq<T>> permutations() {

        return (OrderedSetX<ReactiveSeq<T>>) PersistentCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#combinations(int)
     */
    @Override
    default OrderedSetX<ReactiveSeq<T>> combinations(final int size) {

        return (OrderedSetX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#combinations()
     */
    @Override
    default OrderedSetX<ReactiveSeq<T>> combinations() {

        return (OrderedSetX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations();
    }

    @Override
    default OrderedSetX<VectorX<T>> sliding(final int windowSize) {
        return (OrderedSetX<VectorX<T>>) PersistentCollectionX.super.sliding(windowSize);
    }

    @Override
    default OrderedSetX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (OrderedSetX<VectorX<T>>) PersistentCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default OrderedSetX<T> scanLeft(final Monoid<T> monoid) {
        return (OrderedSetX<T>) PersistentCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> OrderedSetX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (OrderedSetX<U>) PersistentCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default OrderedSetX<T> scanRight(final Monoid<T> monoid) {
        return (OrderedSetX<T>) PersistentCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> OrderedSetX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (OrderedSetX<U>) PersistentCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default OrderedSetX<T> plusInOrder(final T e) {

        return (OrderedSetX<T>) PersistentCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
    * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycle(int)
    */
    @Override
    default LinkedListX<T> cycle(final long times) {

        return this.stream()
                   .cycle(times)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default LinkedListX<T> cycle(final Monoid<T> m, final long times) {

        return this.stream()
                   .cycle(m, times)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleWhile(predicate)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleUntil(predicate)
                   .toPStackX();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> OrderedSetX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (OrderedSetX) PersistentCollectionX.super.zipS(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> OrderedSetX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (OrderedSetX) PersistentCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> OrderedSetX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                 final Iterable<? extends T4> fourth) {

        return (OrderedSetX) PersistentCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
     */
    @Override
    default OrderedSetX<Tuple2<T, Long>> zipWithIndex() {

        return (OrderedSetX<Tuple2<T, Long>>) PersistentCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#distinct()
     */
    @Override
    default OrderedSetX<T> distinct() {

        return (OrderedSetX<T>) PersistentCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#sorted()
     */
    @Override
    default OrderedSetX<T> sorted() {

        return (OrderedSetX<T>) PersistentCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default OrderedSetX<T> sorted(final Comparator<? super T> c) {

        return (OrderedSetX<T>) PersistentCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> skipWhile(final Predicate<? super T> p) {

        return (OrderedSetX<T>) PersistentCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> skipUntil(final Predicate<? super T> p) {

        return (OrderedSetX<T>) PersistentCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> limitWhile(final Predicate<? super T> p) {

        return (OrderedSetX<T>) PersistentCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> limitUntil(final Predicate<? super T> p) {

        return (OrderedSetX<T>) PersistentCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default OrderedSetX<T> intersperse(final T value) {

        return (OrderedSetX<T>) PersistentCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#shuffle()
     */
    @Override
    default OrderedSetX<T> shuffle() {

        return (OrderedSetX<T>) PersistentCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
     */
    @Override
    default OrderedSetX<T> skipLast(final int num) {

        return (OrderedSetX<T>) PersistentCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
     */
    @Override
    default OrderedSetX<T> limitLast(final int num) {

        return (OrderedSetX<T>) PersistentCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default OrderedSetX<T> onEmptySwitch(final Supplier<? extends POrderedSet<T>> supplier) {
        if (this.isEmpty())
            return OrderedSetX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default OrderedSetX<T> onEmpty(final T value) {

        return (OrderedSetX<T>) PersistentCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default OrderedSetX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (OrderedSetX<T>) PersistentCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> OrderedSetX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (OrderedSetX<T>) PersistentCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
     */
    @Override
    default OrderedSetX<T> shuffle(final Random random) {

        return (OrderedSetX<T>) PersistentCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> OrderedSetX<U> ofType(final Class<? extends U> type) {

        return (OrderedSetX<U>) PersistentCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default OrderedSetX<T> filterNot(final Predicate<? super T> fn) {

        return (OrderedSetX<T>) PersistentCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#notNull()
     */
    @Override
    default OrderedSetX<T> notNull() {

        return (OrderedSetX<T>) PersistentCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.reactiveStream.Stream)
     */
    @Override
    default OrderedSetX<T> removeAllS(final Stream<? extends T> stream) {

        return (OrderedSetX<T>) PersistentCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default OrderedSetX<T> removeAllI(final Iterable<? extends T> it) {

        return (OrderedSetX<T>) PersistentCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default OrderedSetX<T> removeAll(final T... values) {

        return (OrderedSetX<T>) PersistentCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default OrderedSetX<T> retainAllI(final Iterable<? extends T> it) {

        return (OrderedSetX<T>) PersistentCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#retainAllI(java.util.reactiveStream.Stream)
     */
    @Override
    default OrderedSetX<T> retainAllS(final Stream<? extends T> seq) {

        return (OrderedSetX<T>) PersistentCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default OrderedSetX<T> retainAll(final T... values) {

        return (OrderedSetX<T>) PersistentCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> OrderedSetX<U> cast(final Class<? extends U> type) {

        return (OrderedSetX<U>) PersistentCollectionX.super.cast(type);
    }

    @Override
    default <C extends Collection<? super T>> OrderedSetX<C> grouped(final int size, final Supplier<C> supplier) {

        return (OrderedSetX<C>) PersistentCollectionX.super.grouped(size, supplier);
    }

    @Override
    default OrderedSetX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (OrderedSetX<ListX<T>>) PersistentCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default OrderedSetX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (OrderedSetX<ListX<T>>) PersistentCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default OrderedSetX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (OrderedSetX<ListX<T>>) PersistentCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> OrderedSetX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (OrderedSetX<C>) PersistentCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> OrderedSetX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (OrderedSetX<C>) PersistentCollectionX.super.groupedUntil(predicate, factory);
    }
    @Override
    default <R> OrderedSetX<R> retry(final Function<? super T, ? extends R> fn) {
        return (OrderedSetX<R>)PersistentCollectionX.super.retry(fn);
    }

    @Override
    default <R> OrderedSetX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (OrderedSetX<R>)PersistentCollectionX.super.retry(fn);
    }

    @Override
    default <R> OrderedSetX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (OrderedSetX<R>)PersistentCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> OrderedSetX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (OrderedSetX<R>)PersistentCollectionX.super.flatMapP(fn);
    }

    @Override
    default OrderedSetX<T> prependS(Stream<? extends T> stream) {
        return (OrderedSetX<T>)PersistentCollectionX.super.prependS(stream);
    }

    @Override
    default OrderedSetX<T> append(T... values) {
        return (OrderedSetX<T>)PersistentCollectionX.super.append(values);
    }

    @Override
    default OrderedSetX<T> append(T value) {
        return (OrderedSetX<T>)PersistentCollectionX.super.append(value);
    }

    @Override
    default OrderedSetX<T> prepend(T value) {
        return (OrderedSetX<T>)PersistentCollectionX.super.prepend(value);
    }

    @Override
    default OrderedSetX<T> prepend(T... values) {
        return (OrderedSetX<T>)PersistentCollectionX.super.prepend(values);
    }

    @Override
    default OrderedSetX<T> insertAt(int pos, T... values) {
        return (OrderedSetX<T>)PersistentCollectionX.super.insertAt(pos,values);
    }

    @Override
    default OrderedSetX<T> deleteBetween(int start, int end) {
        return (OrderedSetX<T>)PersistentCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default OrderedSetX<T> insertAtS(int pos, Stream<T> stream) {
        return (OrderedSetX<T>)PersistentCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default OrderedSetX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (OrderedSetX<T>)PersistentCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> OrderedSetX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (OrderedSetX<T>)PersistentCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default OrderedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (OrderedSetX<T>)PersistentCollectionX.super.plusLoop(max,value);
    }

    @Override
    default OrderedSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (OrderedSetX<T>)PersistentCollectionX.super.plusLoop(supplier);
    }


}
