package io.prelink.critbit;

public interface BitChecker<K> {
    boolean isSet(K key, int i);
    int firstDiff(K k1, K k2);
    int bitLength(K key);
    boolean startsWith(K key, K prefix);
}
