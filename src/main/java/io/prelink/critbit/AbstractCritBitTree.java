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
        Node<K,V> mkShortBoth(int diffBit, K lk, V lv, K rk, V rv);
        Node<K,V> mkShortRight(int diffBit, Node<K,V> left, K k, V v);
        Node<K,V> mkShortLeft(int diffBit, K k, V v, Node<K,V> right);
        Node<K,V> mkTall(int diffBit, Node<K,V> left, Node<K,V> right);
        Node<K,V> mkLeaf(K key, V val);
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
        Node<K,V> insert(int diffBit, K key, V val, Context<K,V> ctx);

        boolean isInternal();

        //Should only be called for internal nodes.
        int bit();
        Direction next(K key, Context<K,V> ctx);
        Node<K,V> nextNode(K key, Context<K,V> ctx);
        Node<K,V> left(Context<K,V> ctx);
        Node<K,V> right(Context<K,V> ctx);
        Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx);
        Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx);
        boolean hasExternalLeft();
        boolean hasExternalRight();

        //Should only be called for external nodes.
        K key();
        V value();
    }

    static enum Direction {
        LEFT, RIGHT
    }

    static abstract class BaseNode<K,V> implements Node<K,V> {
        public Node<K, V> insert(int diffBit, K key, V val, Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        //Everything should implement this.
        //public boolean isInternal() {
        //    throw new UnsupportedOperationException();
        //}
        public int bit() {
            throw new UnsupportedOperationException();
        }
        public Direction next(K key, Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        public Node<K, V> nextNode(K key, Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        public Node<K, V> left(Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        public Node<K, V> right(Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        public Node<K, V> setLeft(int diffBit, K key, V val, Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        public Node<K, V> setRight(int diffBit, K key, V val, Context<K, V> ctx) {
            throw new UnsupportedOperationException();
        }
        public boolean hasExternalLeft() {
            throw new UnsupportedOperationException();
        }
        public boolean hasExternalRight() {
            throw new UnsupportedOperationException();
        }
        public K key() {
            throw new UnsupportedOperationException();
        }
        public V value() {
            throw new UnsupportedOperationException();
        }
    }

    static abstract class AbstractInternal<K,V> extends BaseNode<K,V> {
        private final int bit;

        AbstractInternal(int bit) {
            this.bit = bit;
        }

        public final int bit() { return bit; }

        public final Node<K,V> insert(int diffBit, K k, V v, Context<K,V> ctx) {
            if(diffBit >= 0 && diffBit < bit()) {
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

        public final Direction next(K key, Context<K,V> ctx) {
            return ctx.chk.isSet(key, bit()) ? Direction.RIGHT
                                             : Direction.LEFT;
        }

        public final Node<K,V> nextNode(K key, Context<K,V> ctx) {
            switch(next(key, ctx)) {
            case LEFT: return left(ctx);
            default: return right(ctx);
            }
        }

        public final boolean isInternal() { return true; }

        protected final Node<K,V> mkShortBothChild(int diffBit,
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

    static final class LeafNode<K,V> extends BaseNode<K,V> {
        private final K key;
        private final V value;
        public LeafNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public K key() { return this.key; }
        public V value() { return this.value; }
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

    static final class ShortBothNode<K,V> extends AbstractInternal<K,V> {
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
        public Node<K,V> left(Context<K,V> ctx) { return ctx.nf.mkLeaf(leftKey, leftVal); }
        public Node<K,V> right(Context<K,V> ctx) { return ctx.nf.mkLeaf(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                return ctx.nf.mkShortBoth(bit(), key, val, rightKey, rightVal);
            }
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, ctx);
            return ctx.nf.mkShortRight(bit(), newLeft, rightKey, rightVal);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                return ctx.nf.mkShortBoth(bit(), leftKey, leftVal, key, val);
            }
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, ctx);
            return ctx.nf.mkShortLeft(bit(), leftKey, leftVal, newRight);
        }
        public boolean hasExternalLeft() { return true; }
        public boolean hasExternalRight() { return true; }
    }

    private final Context<K,V> ctx;

    AbstractCritBitTree(Context<K,V> context) {
        this.ctx = context;
    }

    abstract Node<K,V> root();

    Context<K,V> ctx() {
        return ctx;
    }

    static final class SearchResult<K,V> {
        final Node<K,V> parent;
        final Direction pDirection;
        final Node<K,V> result;
        final Direction rDirection;
        public SearchResult(Node<K,V> parent,
                            Direction pDirection,
                            Node<K,V> result,
                            Direction rDirection) {
            this.parent = parent;
            this.pDirection = pDirection;
            this.result = result;
            this.rDirection = rDirection;
        }
        K compKey(Context<K,V> ctx) {
            switch(rDirection) {
            case LEFT:
                return result.left(ctx).key();
            default: //case RIGHT:
                return result.right(ctx).key();
            }
        }
    }

    final SearchResult<K,V> search(Node<K,V> start, K key) {
        Node<K,V> par = null;
        Direction parDirection = null;
        Node<K,V> cur = start;
        for(;;) {
            switch(cur.next(key, ctx)) {
            case LEFT:
                if(cur.hasExternalLeft()) {
                    return new SearchResult<K,V>(par, parDirection, cur, Direction.LEFT);
                }
                par = cur;
                parDirection = Direction.LEFT;
                cur = cur.left(ctx);
                break;
            case RIGHT:
                if(cur.hasExternalRight()) {
                    return new SearchResult<K,V>(par, parDirection, cur, Direction.RIGHT);
                }
                par = cur;
                parDirection = Direction.RIGHT;
                cur = cur.right(ctx);
                break;
            }
        }
    }

    public final V get(K key) {
        if(root() == null) {
            return null;
        }
        if(!root().isInternal()) {
            return root().value();
        }
        SearchResult<K,V> sr = search(root(), key);
        switch(sr.rDirection) {
        case LEFT:
            return sr.result.left(ctx).value();
        default: //case RIGHT:, but we need to convince compiler we return.
            return sr.result.right(ctx).value();
        }
    }

    public final List<V> fetchPrefixed(K key) {
        if(root() == null) {
            return Collections.emptyList();
        }

        int keyLen = ctx.chk.bitLength(key);
        Node<K,V> current = root();
        Node<K,V> top = current;
        while(current.isInternal()) {
            switch(current.next(key,ctx)) {
            case LEFT:
                current = current.left(ctx);
                break;
            case RIGHT:
                current = current.right(ctx);
                break;
            }
            if(current.bit() < keyLen) {
                top = current;
            }
        }
        if(!ctx.chk.startsWith(current.key() , key)) {
            return Collections.emptyList();
        } else {
            List<V> out = new ArrayList<V>();
            traverse(top, out);
            return out;
        }
    }

    private final void traverse(Node<K,V> top, final List<V> list) {
        if(top.isInternal()) {
            traverse(top.left(ctx), list);
            traverse(top.right(ctx), list);
        } else {
            list.add(top.value());
        }
    }
}
