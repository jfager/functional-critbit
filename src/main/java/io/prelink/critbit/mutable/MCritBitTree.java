package io.prelink.critbit.mutable;

import io.prelink.critbit.BitChecker;


/**
 * Like immutable.CritBitTree, except w/ nodes that are mutable where it makes
 * sense, hypothesis being we can cut down on garbage collection a bit.
 */
public class MCritBitTree<K,V> {

    private static class SearchResult<K,V> {
        public final K key;
        public final V val;
        public SearchResult(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    private static interface Node<K,V> {
        int bit();
        SearchResult<K,V> search(K key, BitChecker<K> chk);
        Node<K,V> insert(int diffBit, K key, V val, BitChecker<K> checker);
        Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk);
        Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk);
    }

    private static abstract class AbstractNode<K,V> implements Node<K,V> {

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

    private static class InitialNode<K,V> implements Node<K,V> {
        private final K key;
        private final V value;
        public InitialNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public SearchResult<K,V> search(K key, BitChecker<K> chk) {
            return new SearchResult<K,V>(this.key, this.value);
        }
        public Node<K,V> insert(int diffBit, K key, V val, BitChecker<K> chk) {
            if(diffBit < 0) {
                return new InitialNode<K,V>(key, val);
            }
            else if(chk.isSet(key, diffBit)) { //new key goes right
                return new ShortBothNode<K,V>(diffBit, this.key, this.value, key, val);
            } else { //new key goes left
                return new ShortBothNode<K,V>(diffBit, key, val, this.key, this.value);
            }
        }
        public int bit() { throw new UnsupportedOperationException(); }
        public Node<K, V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            throw new UnsupportedOperationException();
        }
        public Node<K, V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            throw new UnsupportedOperationException();
        }
    }

    private static class ShortBothNode<K,V> extends AbstractNode<K,V> {
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
        public SearchResult<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return new SearchResult<K,V>(this.rightKey, this.rightVal);
            } else {
                return new SearchResult<K,V>(this.leftKey, this.leftVal);
            }
        }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            //returning different kind of node, can't use mutability.
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, chk);
            return new ShortRightNode<K,V>(this.bit, newLeft, rightKey, rightVal);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            //returning different kind of node, can't use mutability.
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, chk);
            return new ShortLeftNode<K,V>(this.bit, leftKey, leftVal, newRight);
        }
    }

    private static class ShortLeftNode<K,V> extends AbstractNode<K,V> {
        private final int bit;
        private final K leftKey;
        private final V leftVal;
        private Node<K,V> right;
        public ShortLeftNode(int bit, K leftKey, V leftVal, Node<K,V> right) {
            this.bit = bit;
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.right = right;
        }
        public int bit() {
            return bit;
        }
        public SearchResult<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return right.search(key, chk);
            } else {
                return new SearchResult<K,V>(this.leftKey, this.leftVal);
            }
        }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            //returning different kind of node, can't use mutability.
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, chk);
            return new TallNode<K,V>(this.bit, newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            this.right = right.insert(diffBit, key, val, chk);
            return this;
        }

    }
    private static class ShortRightNode<K,V> extends AbstractNode<K,V> {
        private final int bit;
        private final K rightKey;
        private final V rightVal;
        private Node<K,V> left;
        public ShortRightNode(int bit, Node<K,V> left, K rightKey, V rightVal) {
            this.bit = bit;
            this.left = left;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }
        public int bit() {
            return bit;
        }
        public SearchResult<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return new SearchResult<K,V>(this.rightKey, this.rightVal);
            } else {
                return left.search(key, chk);
            }
        }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            this.left = left.insert(diffBit, key, val, chk);
            return this;
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            //returning different kind of node, can't use mutability.
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, chk);
            return new TallNode<K,V>(this.bit, left, newRight);
        }
    }
    private static class TallNode<K,V> extends AbstractNode<K,V> {
        private final int bit;
        private Node<K,V> left;
        private Node<K,V> right;
        public TallNode(int bit, Node<K,V> left, Node<K,V> right) {
            this.bit = bit;
            this.left = left;
            this.right = right;
        }
        public int bit() {
            return bit;
        }
        public SearchResult<K,V> search(K key, BitChecker<K> chk) {
            if(chk.isSet(key, bit)) {
                return right.search(key, chk);
            } else {
                return left.search(key, chk);
            }
        }
        public Node<K,V> setLeft(int diffBit, K key, V val, BitChecker<K> chk) {
            this.left = left.insert(diffBit, key, val, chk);
            return this;
        }
        public Node<K,V> setRight(int diffBit, K key, V val, BitChecker<K> chk) {
            this.right = right.insert(diffBit, key, val, chk);
            return this;
        }
    }

    private final BitChecker<K> bitChecker;
    private Node<K,V> root = null;

    public MCritBitTree(BitChecker<K> bitChecker) {
        this.bitChecker = bitChecker;
    }

    public void insert(K key, V val) {
        if(root == null) {
            root = new InitialNode<K,V>(key, val);
            return;
        }
        SearchResult<K,V> sr = root.search(key, bitChecker);
        int i = bitChecker.firstDiff(key, sr.key);
        root = root.insert(i, key, val, bitChecker);
    }

    public V search(K key) {
        return root.search(key, bitChecker).val;
    }
}
