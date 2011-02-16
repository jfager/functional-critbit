package io.prelink.critbit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.ardverk.collection.Cursor;
import org.ardverk.collection.StringKeyAnalyzer;
import org.junit.Test;

public class CritBitTest extends TestCase {


    @Test
    public void testTraverse() {
        MCritBitTree<String, String> intTrie =
            new MCritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);

        intTrie.put("u", "u");
        intTrie.put("un", "un");
        intTrie.put("unh", "unh");
        intTrie.put("uni", "uni");
        intTrie.put("unj", "unj");
        intTrie.put("unim", "unim");
        intTrie.put("unin", "unin");
        intTrie.put("unio", "unio");
        intTrie.put("uninc", "uninc");
        intTrie.put("unind", "unind");
        intTrie.put("unine", "unine");
        intTrie.put("unindd", "unindd");
        intTrie.put("uninde", "uninde");
        intTrie.put("unindf", "unindf");
        intTrie.put("unindew", "unindew");
        intTrie.put("unindex", "unindex");
        intTrie.put("unindey", "unindey");
        intTrie.put("a", "a");
        intTrie.put("z", "z");

        final List<String> target
            = Arrays.asList(new String[]{"unin", "uninc", "unind", "unindd",
                    "uninde", "unindew", "unindex", "unindey",
                    "unindf", "unine"});

        final List<String> gathered = new ArrayList<String>();
        intTrie.traverseWithPrefix("unin", new ValueListCursor<String, String>(gathered));
        assertEquals(target, gathered);
    }

    private class ValueListCursor<K,V> implements Cursor<K,V> {
        private final List<V> list;
        public ValueListCursor(List<V> basket) {
            this.list = basket;
        }
        public Decision select(Map.Entry<? extends K, ? extends V> entry) {
            list.add(entry.getValue());
            return Decision.CONTINUE;
        }
    }
}
