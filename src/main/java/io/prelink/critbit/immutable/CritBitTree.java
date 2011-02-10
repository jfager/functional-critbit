package io.prelink.critbit.immutable;

import io.prelink.critbit.BitChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An OO/Functional Java crit-bit tree, inspired by
 * djb (http://cr.yp.to/critbit.html),
 * Adam Langley (https://github.com/agl/critbit),
 * and Okasaki (http://www.eecs.usma.edu/webs/people/okasaki/pubs.html)
 */
public class CritBitTree<K,V> {


    private static interface Node<K,V> {
        External<K,V> search(K key, BitChecker<K> chk);
        Node<K,V> insert(int diffBit, K key, V val, BitChecker<K> checker);
        Node<K,V> next(K key, BitChecker<K> checker);
        boolean isInternal();
    }

    private static interface Internal<K,V> extends Node<K,V> {
        int bit();
        Node<K,V> left();
        Node<K,V> right();
        Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk);
        Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk);
    }

    private static interface External<K,V> extends Node<K,V> {
        K key();
        V value();
    }

    private static abstract class AbstractInternal<K,V> implements Internal<K,V> {

        public Node<K,V> insert(int diffBit, K k, V v, BitChecker<K> chk) {
            if(diffBit < bit()) {
                if(chk.isSet(k, diffBit)) {
                    return new ShortRightNode<K,V>(diffBit, this, k, v);
                } else {
                    return new ShortLeftNode<K,V>(diffBit, k, v, this);
                }
            } else {
                if(chk.isSet(k, bit())) {
                    return setRight(diffBit, k, v, chk);
                } else {
                    return setLeft(diffBit, k, v, chk);
                }
            }
        }

        public Node<K,V> next(K key, BitChecker<K> chk) {
            return chk.isSet(key, bit()) ? right() : left();
        }

        public boolean isInternal() { return true; }

        protected Node<K,V> mkShortBothChild(int diffBit,
                                             K newKey, V newVal,
                                             K oldKey, V oldVal,
                                             BitChecker<K> chk) {
            boolean newGoesRight = chk.isSet(newKey, diffBit);
            K rKey = newGoesRight ? newKey : oldKey;
            V rVal = newGoesRight ? newVal : oldVal;
            K lKey = newGoesRight ? oldKey : newKey;
            V lVal = newGoesRight ? oldVal : newVal;
            return new ShortBothNode<K,V>(diffBit, lKey, lVal, rKey, rVal);
        }
    }

    private static class LeafNode<K,V> implements External<K,V> {
        private final K key;
        private final V value;
        public LeafNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public K key() { return this.key; }
        public V value() { return this.value; }
        public Node<K,V> next(K key, BitChecker<K> checker) { return null; }
        public External<K,V> search(K key, BitChecker<K> chk) {
            return this;
        }
        public Node<K,V> insert(int diffBit, K key, V val, BitChecker<K> chk) {
            if(diffBit < 0) {
                return new LeafNode<K,V>(key, val);
            }
            else if(chk.isSet(key, diffBit)) { //new key goes right
                return new ShortBothNode<K,V>(diffBit, this.key, this.value, key, val);
            } else { //new key goes left
                return new ShortBothNode<K,V>(diffBit, key, val, this.key, this.value);
            }
        }
        public boolean isInternal() { return false; }
    }

    private static class ShortBothNode<K,V> extends AbstractInternal<K,V> {
        private final int bit;
        private final K leftKey;
        private final V leftVal;
        private final K rightKey;
        private final V rightVal;

        public ShortBothNode(int bit, K leftKey, V leftVal, K rightKey, V rightVal) {
            this.bit = bit;
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }

        public int bit() {
            return bit;
        }
        public External<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return new LeafNode<K,V>(this.rightKey, this.rightVal);
            } else {
                return new LeafNode<K,V>(this.leftKey, this.leftVal);
            }
        }
        public Node<K,V> left() { return new LeafNode<K,V>(leftKey, leftVal); }
        public Node<K,V> right() { return new LeafNode<K,V>(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, chk);
            return new ShortRightNode<K,V>(this.bit, newLeft, rightKey, rightVal);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, chk);
            return new ShortLeftNode<K,V>(this.bit, leftKey, leftVal, newRight);
        }
    }

    private static class ShortLeftNode<K,V> extends AbstractInternal<K,V> {
        private final int bit;
        private final K leftKey;
        private final V leftVal;
        private final Node<K,V> right;
        public ShortLeftNode(int bit, K leftKey, V leftVal, Node<K,V> right) {
            this.bit = bit;
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.right = right;
        }
        public int bit() {
            return bit;
        }
        public External<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return right.search(key, chk);
            } else {
                return new LeafNode<K,V>(this.leftKey, this.leftVal);
            }
        }
        public Node<K,V> left() { return new LeafNode<K,V>(leftKey, leftVal); }
        public Node<K,V> right() { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, chk);
            return new TallNode<K,V>(this.bit, newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newRight = right.insert(diffBit, key, val, chk);
            return new ShortLeftNode<K,V>(this.bit, leftKey, leftVal, newRight);
        }

    }
    private static class ShortRightNode<K,V> extends AbstractInternal<K,V> {
        private final int bit;
        private final Node<K,V> left;
        private final K rightKey;
        private final V rightVal;
        public ShortRightNode(int bit, Node<K,V> left, K rightKey, V rightVal) {
            this.bit = bit;
            this.left = left;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }
        public int bit() {
            return bit;
        }
        public External<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return new LeafNode<K,V>(this.rightKey, this.rightVal);
            } else {
                return left.search(key, chk);
            }
        }
        public Node<K,V> left() { return left; }
        public Node<K,V> right() { return new LeafNode<K,V>(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newLeft = left.insert(diffBit, key, val, chk);
            return new ShortRightNode<K,V>(this.bit, newLeft, rightKey, rightVal);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, chk);
            return new TallNode<K,V>(this.bit, left, newRight);
        }
    }
    private static class TallNode<K,V> extends AbstractInternal<K,V> {
        private final int bit;
        private final Node<K,V> left;
        private final Node<K,V> right;
        public TallNode(int bit, Node<K,V> left, Node<K,V> right) {
            this.bit = bit;
            this.left = left;
            this.right = right;
        }
        public int bit() {
            return bit;
        }
        public External<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return right.search(key, chk);
            } else {
                return left.search(key, chk);
            }
        }
        public Node<K,V> left() { return left; }
        public Node<K,V> right() { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newLeft = left.insert(diffBit, key, val, chk);
            return new TallNode<K,V>(this.bit, newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            Node<K,V> newRight = right.insert(diffBit, key, val, chk);
            return new TallNode<K,V>(this.bit, left, newRight);
        }
    }

    private final BitChecker<K> bitChecker;
    private final Node<K,V> root;

    public CritBitTree(BitChecker<K> bitChecker) {
        this.bitChecker = bitChecker;
        this.root = null;
    }

    private CritBitTree(Node<K,V> root, BitChecker<K> bitChecker) {
        this.bitChecker = bitChecker;
        this.root = root;
    }

    public CritBitTree<K,V> insert(K key, V val) {
        if(root == null) {
            return new CritBitTree<K,V>(new LeafNode<K,V>(key, val),
                                        bitChecker);
        }
        External<K,V> ext = root.search(key, bitChecker);
        int i = bitChecker.firstDiff(key, ext.key());
        return new CritBitTree<K,V>(root.insert(i, key, val, bitChecker),
                                    bitChecker);
    }

    public V search(K key) {
        return root.search(key, bitChecker).value();
    }

    public List<V> fetchPrefixed(K key) {
        if(root == null) {
            return Collections.emptyList();
        }

        int keyLen = bitChecker.bitLength(key);
        Node<K,V> current = root;
        Node<K,V> top = root;
        while(current.isInternal()) {
            Internal<K,V> internal = (Internal<K,V>)current;
            current = internal.next(key, bitChecker);
            if(internal.bit() < keyLen) {
                top = current;
            }
        }
        External<K,V> external = (External<K,V>)current;
        if(!bitChecker.startsWith(external.key() , key)) {
            return Collections.emptyList();
        } else {
            List<V> out = new ArrayList<V>();
            traverse(top, out);
            return out;
        }
    }

    private void traverse(Node<K,V> top, final List<V> list) {
        if(top.isInternal()) {
            Internal<K,V> internal = (Internal<K,V>)top;
            traverse(internal.left(), list);
            traverse(internal.right(), list);
        } else {
            list.add(((External<K,V>)top).value());
        }
    }
}
