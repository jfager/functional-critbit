package io.prelink.critbit;

/**
 * Like immutable.CritBitTree, except w/ nodes that are mutable where it makes
 * sense, hypothesis being we can cut down on garbage collection a bit.
 */
public class MCritBitTree<K,V> extends AbstractCritBitTree<K,V> {

    static class MShortLeftNode<K,V> extends AbstractInternal<K,V> {
        private final K leftKey;
        private final V leftVal;
        private Node<K,V> right;
        public MShortLeftNode(int bit, K leftKey, V leftVal, Node<K,V> right) {
            super(bit);
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.right = right;
        }
        public External<K,V> search(K key, Context<K,V> ctx) {
            if(ctx.chk.isSet(key, bit())) {
                return right.search(key, ctx);
            } else {
                return ctx.nf.mkLeaf(this.leftKey, this.leftVal);
            }
        }
        public Node<K,V> left(Context<K,V> ctx) { return ctx.nf.mkLeaf(leftKey, leftVal); }
        public Node<K,V> right(Context<K,V> ctx) { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, ctx);
            return ctx.nf.mkTall(bit(), newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            this.right = right.insert(diffBit, key, val, ctx);
            return this;
        }
    }

    static class MShortRightNode<K,V> extends AbstractInternal<K,V> {
        private Node<K,V> left;
        private final K rightKey;
        private final V rightVal;
        public MShortRightNode(int bit, Node<K,V> left, K rightKey, V rightVal) {
            super(bit);
            this.left = left;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }
        public External<K,V> search(K key, Context<K,V> ctx) {
            if(ctx.chk.isSet(key, bit())) {
                return ctx.nf.mkLeaf(this.rightKey, this.rightVal);
            } else {
                return left.search(key, ctx);
            }
        }
        public Node<K,V> left(Context<K,V> ctx) { return left; }
        public Node<K,V> right(Context<K,V> ctx) { return ctx.nf.mkLeaf(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            this.left = left.insert(diffBit, key, val, ctx);
            return this;
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, ctx);
            return ctx.nf.mkTall(bit(), left, newRight);
        }
    }

    static class MTallNode<K,V> extends AbstractInternal<K,V> {
        private Node<K,V> left;
        private Node<K,V> right;
        public MTallNode(int bit, Node<K,V> left, Node<K,V> right) {
            super(bit);
            this.left = left;
            this.right = right;
        }
        public External<K,V> search(K key, Context<K,V> ctx) {
            if(ctx.chk.isSet(key, bit())) {
                return right.search(key, ctx);
            } else {
                return left.search(key, ctx);
            }
        }
        public Node<K,V> left(Context<K,V> ctx) { return left; }
        public Node<K,V> right(Context<K,V> ctx) { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            this.left = left.insert(diffBit, key, val, ctx);
            return this;
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            this.right = right.insert(diffBit, key, val, ctx);
            return this;
        }
    }

    static class MutableNodeFactory<K,V> implements NodeFactory<K,V> {
        public Internal<K,V> mkShortBoth(int diffBit, K lk, V lv, K rk, V rv) {
            return new ShortBothNode<K,V>(diffBit, lk, lv, rk, rv);
        }
        public Internal<K,V> mkShortRight(int diffBit, Node<K,V> left, K k, V v) {
            return new MShortRightNode<K,V>(diffBit, left, k, v);
        }
        public Internal<K,V> mkShortLeft(int diffBit, K k, V v, Node<K,V> right) {
            return new MShortLeftNode<K,V>(diffBit, k, v, right);
        }
        public Internal<K,V> mkTall(int diffBit, Node<K,V> left, Node<K,V> right) {
            return new MTallNode<K,V>(diffBit, left, right);
        }
        public External<K,V> mkLeaf(K key, V val) {
            return new LeafNode<K,V>(key, val);
        }
    }

    private Node<K,V> root;

    public MCritBitTree(BitChecker<K> bitChecker) {
        this(null,
             new Context<K,V>(bitChecker, new MutableNodeFactory<K,V>()));
    }

    private MCritBitTree(Node<K,V> root, Context<K,V> ctx) {
        super(ctx);
        this.root = root;
    }

    Node<K,V> root() { return root; }

    public void put(K key, V val) {
        if(root() == null) {
            root = new LeafNode<K,V>(key, val);
            return;
        }
        External<K,V> ext = root().search(key, ctx());
        int i = ctx().chk.firstDiff(key, ext.key());
        root = root().insert(i, key, val, ctx());
    }

}
