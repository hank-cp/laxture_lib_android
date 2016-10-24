package com.laxture.lib.java8;

import java.io.Serializable;

/**
 * @author bartosz walacik
 */
public class Optional<T> implements Serializable {

    private static Optional EMPTY = new Optional();

    private T value;

    private Optional() {
    }

    private Optional(T value) {
        if (value == null) {
            throw new IllegalArgumentException("argument should not be null");
        }
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    public static <T> Optional<T> of(T reference) {
        return new Optional<>(reference);
    }

    public static <T> Optional<T> ofNullable(T nullOrReference) {
        if (nullOrReference == null) {
            return empty();
        }

        return of(nullOrReference);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isPresent(){
        return !isEmpty();
    }

    /**
     *
     * @throws IllegalStateException if the instance is empty
     */
    public T get(){
        if (isEmpty()) {
            throw new IllegalStateException("can't get() from empty optional");
        }
        return value;
    }

    public void ifPresent(Consumer<T> consumer) {
        if (value != null) consumer.consume(value);
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper should not be null");
        }
        if (!isPresent())
            return empty();
        else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    @Override
    public String toString() {
        return isPresent() ? String.format("Optional[%s]", this.value) : "Optional.empty";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optional other = (Optional) o;

        if (this.isEmpty() && other.isEmpty()){
            return true;
        }

        if (this.isPresent() && other.isPresent()){
            return value.equals(other.value);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
