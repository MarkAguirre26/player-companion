package com.virtual.app.sicbo.module.model;

public class PairPattern<T, U,V> {
    public final T first;
    public final U second;
    public final V third;

    public PairPattern(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}