package io.prelink.critbit;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.Cursor.Decision;
import org.ardverk.collection.KeyAnalyzer;

/**
 * Like immutable.CritBitTree, except w/ nodes that are mutable where it makes
 * sense, hypothesis being we can improve performance and cut down on garbage
 * collection a bit.
 */
public final class MCritBitTree<K,V> extends AbstractCritBitTree<K,V> implements Map<K,V> {

    private static final long serialVersionUID = 20110212L;

    static final class MShortLeftNode<K,V> extends AbstractInternal<K,V> {
        private static final long serialVersionUID = 20110212L;
        private final K leftKey;
        private V leftVal;
        private Node<K,V> right;
        public MShortLeftNode(int bit, K leftKey, V leftVal, Node<K,V> right) {
            super(bit);
            this.leftKey = leftKey;
            this.leftVal = leftVal;
            this.right = right;
        }
        public Node<K,V> left(Context<K,V> ctx) { return ctx.nf.mkLeaf(leftKey, leftVal); }
        public Node<K,V> right(Context<K,V> ctx) { return right; }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                this.leftVal = val;
                return this;
            }
            Node<K,V> newLeft = mkShortBothChild(diffBit, key, val, leftKey, leftVal, ctx);
            return ctx.nf.mkTall(bit(), newLeft, right);
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            this.right = right.insert(diffBit, key, val, ctx);
            return this;
        }
        public boolean hasExternalLeft() { return true; }
        public boolean hasExternalRight() { return false; }
        protected Node<K,V> removeLeft(K key, Context<K,V> ctx, boolean force) {
            if(force || ctx.chk.bitIndex(key, this.leftKey) < 0) {
                return right;
            }
            return this;
        }
        protected Node<K,V> removeRight(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newRight = right.remove(key, ctx, force);
            if(newRight.isInternal()) {
                this.right = newRight;
                return this;
            } else {
                return ctx.nf.mkShortBoth(bit(), leftKey, leftVal, newRight.getKey(), newRight.getValue());
            }
        }
    }

    static final class MShortRightNode<K,V> extends AbstractInternal<K,V> {
        private static final long serialVersionUID = 20110212L;
        private Node<K,V> left;
        private final K rightKey;
        private V rightVal;
        public MShortRightNode(int bit, Node<K,V> left, K rightKey, V rightVal) {
            super(bit);
            this.left = left;
            this.rightKey = rightKey;
            this.rightVal = rightVal;
        }
        public Node<K,V> left(Context<K,V> ctx) { return left; }
        public Node<K,V> right(Context<K,V> ctx) { return ctx.nf.mkLeaf(rightKey, rightVal); }
        public Node<K,V> setLeft(int diffBit, K key, V val, Context<K,V> ctx) {
            this.left = left.insert(diffBit, key, val, ctx);
            return this;
        }
        public Node<K,V> setRight(int diffBit, K key, V val, Context<K,V> ctx) {
            if(diffBit < 0) {
                this.rightVal = val;
                return this;
            }
            Node<K,V> newRight = mkShortBothChild(diffBit, key, val, rightKey, rightVal, ctx);
            return ctx.nf.mkTall(bit(), left, newRight);
        }
        protected Node<K,V> removeLeft(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newLeft = left.remove(key, ctx, force);
            if(newLeft.isInternal()) {
                this.left = newLeft;
                return this;
            } else {
                return ctx.nf.mkShortBoth(bit(), newLeft.getKey(), newLeft.getValue(), rightKey, rightVal);
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

    static final class MTallNode<K,V> extends AbstractInternal<K,V> {
        private static final long serialVersionUID = 20110212L;
        private Node<K,V> left;
        private Node<K,V> right;
        public MTallNode(int bit, Node<K,V> left, Node<K,V> right) {
            super(bit);
            this.left = left;
            this.right = right;
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
        protected Node<K,V> removeLeft(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newLeft = left.remove(key, ctx, force);
            if(newLeft.isInternal()) {
                this.left = newLeft;
                return this;
            } else {
                return ctx.nf.mkShortLeft(bit(), newLeft.getKey(), newLeft.getValue(), right);
            }
        }
        protected Node<K,V> removeRight(K key, Context<K,V> ctx, boolean force) {
            Node<K,V> newRight = right.remove(key, ctx, force);
            if(newRight.isInternal()) {
                this.right = newRight;
                return this;
            } else {
                return ctx.nf.mkShortRight(bit(), left, newRight.getKey(), newRight.getValue());
            }
        }
        public boolean hasExternalLeft() { return false; }
        public boolean hasExternalRight() { return false; }
    }

    static final class MutableNodeFactory<K,V> implements NodeFactory<K,V> {
        private static final long serialVersionUID = 20110212L;
        public Node<K,V> mkShortBoth(int diffBit, K lk, V lv, K rk, V rv) {
            return new ShortBothNode<K,V>(diffBit, lk, lv, rk, rv);
        }
        public Node<K,V> mkShortRight(int diffBit, Node<K,V> left, K k, V v) {
            return new MShortRightNode<K,V>(diffBit, left, k, v);
        }
        public Node<K,V> mkShortLeft(int diffBit, K k, V v, Node<K,V> right) {
            return new MShortLeftNode<K,V>(diffBit, k, v, right);
        }
        public Node<K,V> mkTall(int diffBit, Node<K,V> left, Node<K,V> right) {
            return new MTallNode<K,V>(diffBit, left, right);
        }
        public Node<K,V> mkLeaf(K key, V val) {
            return new LeafNode<K,V>(key, val);
        }
    }

    private Node<K,V> root;
    private int size = 0;

    public MCritBitTree(KeyAnalyzer<K> analyzer) {
        this(null,
             new Context<K,V>(analyzer, new MutableNodeFactory<K,V>()));
    }

    private MCritBitTree(Node<K,V> root, Context<K,V> ctx) {
        super(ctx);
        this.root = root;
    }

    Node<K,V> root() { return root; }
    public int size() { return size; }

    public V put(K key, V val) {
        if(root == null) {
            root = ctx().nf.mkLeaf(key, val);
            size++;
            return null;
        }
        if(!root.isInternal()) {
            int diffBit = ctx().chk.bitIndex(key, root.getKey());
            V oldVal = root.getValue();
            root = root.insert(diffBit, key, val, ctx());
            if(diffBit >= 0) {
                size++;
                return null;
            } else {
                return oldVal;
            }
        }

        final SearchResult<K,V> sr = search(root, key);
        final int diffBit = ctx().chk.bitIndex(key, sr.key(ctx()));
        final V out;
        if(diffBit >= 0) {
            out = null;
            size++;
        } else {
            out = sr.value(ctx());
        }

        if(sr.parent == null) {
            root = root.insert(diffBit, key, val, ctx());
            return out;
        } else if(diffBit < 0 || diffBit >= sr.parent.bit()) {
            switch(sr.pDirection) {
            case LEFT:
                sr.parent.setLeft(diffBit, key, val, ctx());
                return out;
            case RIGHT:
                sr.parent.setRight(diffBit, key, val, ctx());
                return out;
            }
        }

        if(diffBit < root.bit()) {
            root = root.insert(diffBit, key, val, ctx());
            return out;
        }

        Node<K,V> prev = root;
        Node<K,V> current = prev.nextNode(key, ctx());
        for(;;) {
            if(diffBit < current.bit()) {
                if(ctx().chk.isBitSet(key, prev.bit())) {
                    prev.setRight(diffBit, key, val, ctx());
                } else {
                    prev.setLeft(diffBit, key, val, ctx());
                }
                return out;
            } else {
                prev = current;
                current = current.nextNode(key, ctx());
            }
        }
    }

    public void putAll(Map<? extends K, ? extends V> otherMap) {
        for(Map.Entry<? extends K, ? extends V> me: otherMap.entrySet()) {
            put(me.getKey(), me.getValue());
        }
    }

    public V remove(Object k) {
        if(root == null) {
            return null;
        }
        final K key = AbstractCritBitTree.<K>cast(k);
        if(!root.isInternal()) {
            if(ctx().chk.bitIndex(key, root.getKey()) < 0) {
                V out = root.getValue();
                root = null;
                size--;
                return out;
            } else {
                return null;
            }
        }

        Node<K,V> grandparent = null;
        Node<K,V> parent = null;
        Node<K,V> cur = root;
        for(;;) {
            switch(cur.next(key, ctx())) {
            case LEFT:
                if(cur.hasExternalLeft()) {
                    Node<K,V> leftNode = cur.left(ctx());
                    if(ctx().chk.bitIndex(key, leftNode.getKey()) < 0) {
                        if(grandparent == null) {
                            root = root.remove(key, ctx(), true);
                        } else {
                            grandparent.remove(key, ctx(), true);
                        }
                        size--;
                        return leftNode.getValue();
                    } else {
                        return null;
                    }
                }
                grandparent = parent;
                parent = cur;
                cur = cur.left(ctx());
                break;
            case RIGHT:
                if(cur.hasExternalRight()) {
                    Node<K,V> rightNode = cur.right(ctx());
                    if(ctx().chk.bitIndex(key, rightNode.getKey()) < 0) {
                        if(grandparent == null) {
                            root = root.remove(key, ctx(), true);
                        } else {
                            grandparent.remove(key, ctx(), true);
                        }
                        size--;
                        return rightNode.getValue();
                    } else {
                        return null;
                    }
                }
                grandparent = parent;
                parent = cur;
                cur = cur.right(ctx());
                break;
            }
        }
    }

    public void clear() {
        this.root = null;
        this.size = 0;
    }

    protected final Decision doTraverse(Node<K,V> top,
                                        Cursor<? super K, ? super V> cursor) {
        if(top.isInternal()) {
            Decision d = doTraverse(top.left(ctx()), cursor);
            switch(d) {
            case REMOVE_AND_EXIT: //fall through
            case EXIT:
                return Decision.EXIT;
            case REMOVE: //fall through
            case CONTINUE:
            default:
                return doTraverse(top.right(ctx()), cursor);
            }
        } else {
            Map.Entry<K,V> e = AbstractCritBitTree.<Map.Entry<K,V>>cast(top);
            return cursor.select(e);
        }
    }

	@Override
    public Set<Map.Entry<K, V>> entrySet() {
		return new MCritBitEntrySet<K,V>(this);
    }

	@Override
    public Set<K> keySet() {
	    return new MCritBitKeySet<K,V>(this);
    }

	@Override
    public Collection<V> values() {
	    return new MCritBitValueSet<K,V>(this);
    }

	public Iterator<Node<K,V>> iterator() {
		return new NodeIterator<K,V>(this);
	}

	protected static class NodeIterator<K,V> implements Iterator<Node<K,V>> {
    	//TODO: initialize list to max depth of tree
		private final MCritBitTree<K,V> tree;
    	private final List<Node<K,V>> stack = new ArrayList<Node<K,V>>();
    	private Node<K,V> next = null;

    	public NodeIterator(MCritBitTree<K,V> tree) {
    		this.tree = tree;
    		primeStack();
    	}

    	private void primeStack() {
    		Context<K,V> ctx = tree.ctx();
    		if(tree.isEmpty()) {
    			return;
    		}
    		Node<K,V> curr = tree.root();
    		while(curr.isInternal()) {
    			push(curr);
    			curr = curr.left(ctx);
    		}
    		next = curr;
    	}

    	private final void push(final Node<K,V> node) {
    		stack.add(node);
    	}

    	private final Node<K,V> pop() {
    		return stack.remove(stack.size() - 1);
    	}

    	@Override
        public boolean hasNext() {
    		if(next != null) {
    			return true;
    		}
    		if(stack.isEmpty()) {
    			return false;
    		}

    		final Context<K,V> ctx = tree.ctx();
    		Node<K,V> curr = pop().right(ctx);
			while(curr.isInternal()) {
				push(curr);
				curr = curr.left(ctx);
			}
			next = curr;
    		return next != null;
        }

		@Override
        public Node<K,V> next() {
			if(hasNext()) {
				final Node<K,V> out = next;
				next = null;
				return out;
			}
            throw new NoSuchElementException();
        }

		@Override
        public void remove() {
			throw new UnsupportedOperationException();
        }
    }

	private static class MCritBitEntrySet<K,V> extends AbstractSet<Entry<K,V>> {
		private final MCritBitTree<K,V> tree;
		public MCritBitEntrySet(MCritBitTree<K,V> tree) {
			super();
			this.tree = tree;
		}
		@Override
		public Iterator<Entry<K, V>> iterator() {
		    return new EntryIterator<K,V>(tree.iterator());
	    }
		@Override
		public int size() { return tree.size(); }
		private static class EntryIterator<K,V> implements Iterator<Entry<K,V>> {
			private final Iterator<Node<K,V>> base;
			public EntryIterator(Iterator<Node<K,V>> base) {
				this.base = base;
			}
			@Override
			public Entry<K, V> next() {
				return AbstractCritBitTree.<Entry<K,V>>cast(base.next());
			}
			@Override public boolean hasNext() { return base.hasNext(); }
			@Override public void remove() { base.remove(); }
		}
	}

	private static class MCritBitKeySet<K,V> extends AbstractSet<K> {
		private final MCritBitTree<K,V> tree;
		public MCritBitKeySet(MCritBitTree<K,V> tree) {
			super();
			this.tree = tree;
		}
		@Override
		public Iterator<K> iterator() {
		    return new KeyIterator<K,V>(tree.iterator());
	    }
		@Override
		public int size() { return tree.size(); }
		private static class KeyIterator<K,V> implements Iterator<K> {
			private final Iterator<Node<K,V>> base;
			public KeyIterator(Iterator<Node<K,V>> base) {
				this.base = base;
			}
			@Override
			public K next() {
				return base.next().getKey();
			}
			@Override public boolean hasNext() { return base.hasNext(); }
			@Override public void remove() { base.remove(); }
		}
	}

	private static class MCritBitValueSet<K,V> extends AbstractSet<V> {
		private final MCritBitTree<K,V> tree;
		public MCritBitValueSet(MCritBitTree<K,V> tree) {
			super();
			this.tree = tree;
		}
		@Override
		public Iterator<V> iterator() {
		    return new ValueIterator<K,V>(tree.iterator());
	    }
		@Override
	    public int size() { return tree.size(); }
		private static class ValueIterator<K,V> implements Iterator<V> {
			private final Iterator<Node<K,V>> base;
			public ValueIterator(Iterator<Node<K,V>> base) {
				this.base = base;
			}
			@Override
			public V next() {
				return base.next().getValue();
			}
			@Override public boolean hasNext() { return base.hasNext(); }
			@Override public void remove() { base.remove(); }
		}
	}
}
