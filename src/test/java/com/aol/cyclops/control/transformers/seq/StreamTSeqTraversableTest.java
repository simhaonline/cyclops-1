package com.aol.cyclops.control.transformers.seq;

import java.util.Optional;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.values.StreamTValue;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class StreamTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return StreamT.fromOptional(Optional.of(ReactiveSeq.of(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return StreamTValue.emptyOptional();
    }

}
