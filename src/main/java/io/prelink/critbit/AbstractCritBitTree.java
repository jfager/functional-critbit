package io.prelink.critbit;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An OO/Functional Java crit-bit tree, inspired by
 * djb (http://cr.yp.to/critbit.html),
 * Adam Langley (https://github.com/agl/critbit),
 * and Okasaki (http://www.eecs.usma.edu/webs/people/okasaki/pubs.html)
 */
abstract class AbstractCritBitTree<K,V> {

    static interface NodeFactory<K,V> {
        Internal<K,V> mkShortBoth(int diffBit, K lk, V lv, K rk, V rv);
        Internal<K,V> mkShortRight(int diffBit, Node<K,V> left, K k, V v);
        Internal<K,V> mkShortLeft(int diffBit, K k, V v, Node<K,V> right);
        Internal<K,V> mkTall(int diffBit, Node<K,V> left, Node<K,V> right);
        External<K,V> mkLeaf(K key, V val);
    }

    static class Context<K,V> {
        final BitChecker<K> chk;
        final NodeFactory<K,V> nf;
        Context(BitChecker<K> chk, NodeFactory<K,V> nf) {
            this.chk = chk;
            this.nf = nf;
        }
    }

    static interface Node<K,V> {
        External<K,V> search(K key, Context<K,V> ctx);
        Node<K,V> insert(int diffBit, K key, V val, Context<K,V> ctx);
        Node<K,V> next(K key, Context<K,V> ctx);
        boolean isInternal();
    }

    static interface Internal<K,V> extends Node<K,V> {
        int bit();
        Node<K,V> left(Context<K,V> ctx);
        Node<K,V> right(Context<K,V> ctx);
        Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx);
        Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx);
    }

    static interface External<K,V> extends Node<K,V> {
        K key();
        V value();
    }

    static abstract class AbstractInternal<K,V> implements Internal<K,V> {
        private final int bit;

        AbstractInternal(int bit) {
            this.bit = bit;
        }

        public int bit() { return bit; }

        public Node<K,V> insert(int diffBit, K k, V v, Context<K,V> ctx) {
            if(diffBit < bit()) {
                if(ctx.chk.isSet(k, diffBit)) {
                    return ctx.nf.mkShortRight(diffBit, this, k, v);
                } else {
                    return ctx.nf.mkShortLeft(diffBit, k, v, this);
                }
            } else {
                if(ctx.chk.isSet(k, bit())) {
                    return setRight(diffBit, k, v, ctx);
                } else {
                    return setLeft(diffBit, k, v, ctx);
                }
            }
        }

        public Node<K,V> next(K key, Context<K,V> ctx) {
            return ctx.chk.isSet(key, bit()) ? right(ctx) : left(ctx);
        }

        public boolean isInternal() { return true; }

        protected Node<K,V> mkShortBothChild(int diffBit,
                                             K newKey, V newVal,
                                             K oldKey, V oldVal,
                                             Context<K,V> ctx) {
            boolean newGoesRight = ctx.chk.isSet(newKey, diffBit);
            K rKey = newGoesRight ? newKey : oldKey;
            V rVal = newGoesRight ? newVal : oldVal;
            K lKey = newGoesRight ? oldKey : newKey;
            V lVal = newGoesRight ? oldVal : newVal;
            return ctx.nf.mkShortBoth(diffBit, lKey, lVal, rKey, rVal);
        }
    }

    static class LeafNode<K,V> implements External<K,V> {
        private final K key;
        private final V value;
        public LeafNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public K key() { return this.key; }
        public V value() { return this.value; }
        public Node<K,V> next(K key, Context<K,V> ctx) { return null; }
        public External<K,V> search(K key, Context<K,V> ctx) {
            return this;
        }
        public Node<K,V> insert(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                return ctx.nf.mkLeaf(key, val);
            }
            else if(ctx.chk.isSet(key, diffBit)) { //new key goes right
                return ctx.nf.mkShortBoth(diffBit, this.key, this.value, key, val);
            } else { //new key goes left
                return ctx.nf.mkShortBoth(diffBit, key, val, this.key, this.value);
            }
        }
        public boolean isInternal() { return false; }
    }

    static class ShortBothNode<K,V> extends AbstractInternal<K,V> {
        private final K leftKey;
        private final V leftVal;
        private final K rightKey;
        private final V rightVal;
        public ShortBothNode(int bit, K leftKey, V leftVal, K rightKey, V rightVal) {
            super(bit);
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }
        public External<K,V> search(K key, Context<K,V> ctx) {
            if(ctx.chk.isSet(key, bit())) {
                return ctx.nf.mkLeaf(this.rightKey, this.rightVal);
            } else {
                return ctx.nf.mkLeaf(this.leftKey, this.leftVal);
            }
        }
        public Node<K,V> left(Context<K,V> ctx) { return ctx.nf.mkLeaf(leftKey, leftVal); }
        public Node<K,V> right(Context<K,V> ctx) { return ctx.nf.mkLeaf(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, ctx);
            return ctx.nf.mkShortRight(bit(), newLeft, rightKey, rightVal);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, ctx);
            return ctx.nf.mkShortLeft(bit(), leftKey, leftVal, newRight);
        }
    }

    private final Context<K,V> ctx;

    AbstractCritBitTree(Context<K,V> context) {
        this.ctx = context;
    }

    abstract Node<K,V> root();

    Context<K,V> ctx() {
        return ctx;
    }

    public V search(K key) {
        return root().search(key, ctx).value();
    }

    public List<V> fetchPrefixed(K key) {
        if(root() == null) {
            return Collections.emptyList();
        }

        int keyLen = ctx.chk.bitLength(key);
        Node<K,V> current = root();
        Node<K,V> top = current;
        while(current.isInternal()) {
            Internal<K,V> internal = (Internal<K,V>)current;
            current = internal.next(key, ctx);
            if(internal.bit() < keyLen) {
                top = current;
            }
        }
        External<K,V> external = (External<K,V>)current;
        if(!ctx.chk.startsWith(external.key() , key)) {
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
            traverse(internal.left(ctx), list);
            traverse(internal.right(ctx), list);
        } else {
            list.add(((External<K,V>)top).value());
        }
    }
}
