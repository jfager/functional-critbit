package io.prelink.critbit;

import org.ardverk.collection.KeyAnalyzer;

/**
 * An OO/Functional Java crit-bit tree, inspired by
 * djb (http://cr.yp.to/critbit.html),
 * Adam Langley (https://github.com/agl/critbit),
 * and Okasaki (http://www.eecs.usma.edu/webs/people/okasaki/pubs.html)
 */
public final class CritBitTree<K,V> extends AbstractCritBitTree<K,V> {

    static final class ShortLeftNode<K,V> extends AbstractInternal<K,V> {
        private final K leftKey;
        private final V leftVal;
        private final Node<K,V> right;
        public ShortLeftNode(int bit, K leftKey, V leftVal, Node<K,V> right) {
            super(bit);
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.right = right;
        }
        public Node<K,V> left(Context<K,V> ctx) { return ctx.nf.mkLeaf(leftKey, leftVal); }
        public Node<K,V> right(Context<K,V> ctx) { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                return ctx.nf.mkShortLeft(bit(), key, val, right);
            }
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, ctx);
            return ctx.nf.mkTall(bit(), newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newRight = right.insert(diffBit, key, val, ctx);
            return ctx.nf.mkShortLeft(bit(), leftKey, leftVal, newRight);
        }
        protected Node<K,V> removeLeft(K key, Context<K,V> ctx, boolean force) {
            if(force || ctx.chk.bitIndex(key, this.leftKey) < 0) {
                return right;
            }
            return this;
        }
        protected Node<K,V> removeRight(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newRight = right.remove(key, ctx, force);
            if(right == newRight) {
                return this;
            }
            if(newRight.isInternal()) {
                return ctx.nf.mkShortLeft(bit(), leftKey, leftVal, newRight);
            } else {
                return ctx.nf.mkShortBoth(bit(), leftKey, leftVal, newRight.key(), newRight.value());
            }
        }
        public boolean hasExternalLeft() { return true; }
        public boolean hasExternalRight() { return false; }
    }
    static final class ShortRightNode<K,V> extends AbstractInternal<K,V> {
        private final Node<K,V> left;
        private final K rightKey;
        private final V rightVal;
        public ShortRightNode(int bit, Node<K,V> left, K rightKey, V rightVal) {
            super(bit);
            this.left = left;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }
        public Node<K,V> left(Context<K,V> ctx) { return left; }
        public Node<K,V> right(Context<K,V> ctx) { return ctx.nf.mkLeaf(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newLeft = left.insert(diffBit, key, val, ctx);
            return ctx.nf.mkShortRight(bit(), newLeft, rightKey, rightVal);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                return ctx.nf.mkShortRight(bit(), left, key, val);
            }
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, ctx);
            return ctx.nf.mkTall(bit(), left, newRight);
        }
        protected Node<K,V> removeLeft(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newLeft = left.remove(key, ctx, force);
            if(left == newLeft) {
                return this;
            }
            if(newLeft.isInternal()) {
                return ctx.nf.mkShortRight(bit(), newLeft, rightKey, rightVal);
            } else {
                return ctx.nf.mkShortBoth(bit(), newLeft.key(), newLeft.value(), rightKey, rightVal);
            }
        }
        protected Node<K,V> removeRight(K key, Context<K,V> ctx, boolean force) {
            if(force || ctx.chk.bitIndex(key, this.rightKey) < 0) {
                return left;
            }
            return this;
        }
        public boolean hasExternalLeft() { return false; }
        public boolean hasExternalRight() { return true; }
    }
    static final class TallNode<K,V> extends AbstractInternal<K,V> {
        private final Node<K,V> left;
        private final Node<K,V> right;
        public TallNode(int bit, Node<K,V> left, Node<K,V> right) {
            super(bit);
            this.left = left;
            this.right = right;
        }
        public Node<K,V> left(Context<K,V> ctx) { return left; }
        public Node<K,V> right(Context<K,V> ctx) { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newLeft = left.insert(diffBit, key, val, ctx);
            return ctx.nf.mkTall(bit(), newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newRight = right.insert(diffBit, key, val, ctx);
            return ctx.nf.mkTall(bit(), left, newRight);
        }
        protected Node<K,V> removeLeft(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newLeft = left.remove(key, ctx, force);
            if(left == newLeft) {
                return this;
            }
            if(newLeft.isInternal()) {
                return ctx.nf.mkTall(bit(), newLeft, right);
            } else {
                return ctx.nf.mkShortLeft(bit(), newLeft.key(), newLeft.value(), right);
            }
        }
        protected Node<K,V> removeRight(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newRight = right.remove(key, ctx, force);
            if(right == newRight) {
                return this;
            }
            if(newRight.isInternal()) {
                return ctx.nf.mkTall(bit(), left, newRight);
            } else {
                return ctx.nf.mkShortRight(bit(), left, newRight.key(), newRight.value());
            }
        }
        public boolean hasExternalLeft() { return false; }
        public boolean hasExternalRight() { return false; }
    }

    static final class ImmutableNodeFactory<K,V> implements NodeFactory<K,V> {
        public Node<K,V> mkShortBoth(int diffBit, K lk, V lv, K rk, V rv) {
            return new ShortBothNode<K,V>(diffBit, lk, lv, rk, rv);
        }
        public Node<K,V> mkShortRight(int diffBit, Node<K,V> left, K k, V v) {
            return new ShortRightNode<K,V>(diffBit, left, k, v);
        }
        public Node<K,V> mkShortLeft(int diffBit, K k, V v, Node<K,V> right) {
            return new ShortLeftNode<K,V>(diffBit, k, v, right);
        }
        public Node<K,V> mkTall(int diffBit, Node<K,V> left, Node<K,V> right) {
            return new TallNode<K,V>(diffBit, left, right);
        }
        public Node<K,V> mkLeaf(K key, V val) {
            return new LeafNode<K,V>(key, val);
        }
    }

    private final Node<K,V> root;
    private final int size;

    public CritBitTree(KeyAnalyzer<K> analyzer) {
        this(null, 0,
             new Context<K,V>(analyzer, new ImmutableNodeFactory<K,V>()));
    }

    private CritBitTree(Node<K,V> root, int size, Context<K,V> context) {
        super(context);
        this.root = root;
        this.size = size;
    }

    Node<K,V> root() { return root; }

    public CritBitTree<K,V> put(K key, V val) {
        if(root() == null) {
            return new CritBitTree<K,V>(ctx().nf.mkLeaf(key, val), 1, ctx());
        }
        K compKey;
        if(root().isInternal()) {
            SearchResult<K,V> sr = search(root(), key);
            compKey = sr.key(ctx());
        } else {
            compKey = root().key();
        }

        int diffBit = ctx().chk.bitIndex(key, compKey);
        return new CritBitTree<K,V>(root().insert(diffBit, key, val, ctx()),
                                    (diffBit < 0) ? size : size + 1,
                                    ctx());
    }

    public CritBitTree<K,V> remove(K key) {
        if(root == null) {
            return this;
        }
        Node<K,V> removed = root.remove(key, ctx(), false);
        if(removed == root) {
            return this;
        } else {
            return new CritBitTree<K,V>(removed, size - 1, ctx());
        }
    }

    public int size() { return size; }

}
