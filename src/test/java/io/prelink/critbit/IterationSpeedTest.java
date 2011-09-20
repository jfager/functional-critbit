package io.prelink.critbit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.ardverk.collection.Cursor;
import org.ardverk.collection.Cursor.Decision;
import org.ardverk.collection.StringKeyAnalyzer;

//import org.ardverk.collection.PatriciaTrie;
//import org.ardverk.collection.StringKeyAnalyzer;

public class IterationSpeedTest {
	private static final int ITEMS = 10000;
    private static final int WARMUPS = 10000;
    private static final int ITERS = 10000;
    private static final int SEED = 42;

    private static interface Iterer {
        String name();
        void put(String key, String val);
        int iterate();
    }
    private static String randomString(Random rand, int i) {
        int len = RandomUtils.nextInt(rand, 100) + 1;
        return RandomStringUtils.random(len, 0, 0, false, false, null, rand) + i;
    }
    private static void iterTest(Iterer iter) {
        Random rand = new Random(SEED);
        for(int i=0; i<ITEMS; i++) {
        	String s = randomString(rand, i);
        	iter.put(s, s);
        }

        System.out.print("Warming up " + iter.name() + "... ");
        for(int i=0; i<WARMUPS; i++) {
        	int j = iter.iterate();
        	if(j != ITEMS) {
        		throw new RuntimeException("wtf?");
        	}
        }
        System.out.println("Done!");
        System.out.print(String.format("Starting test for %s...", iter.name()));
        final long start = System.currentTimeMillis();
        for(int i=0; i<ITERS; i++) {
            int j = iter.iterate();
            if(j != ITEMS) {
            	throw new RuntimeException("wtf?");
            }
        }
        final long end = System.currentTimeMillis();
        System.out.println(
            String.format("Done! Iterated over %s keys %s times in %s ms", ITEMS, ITERS, end-start));
    }

    static class CountCursor<K,V> implements Cursor<K,V> {
    	public int count = 0;
    	public Decision select(Entry<? extends K, ? extends V> entry) {
    		count++;
    		return Decision.CONTINUE;
    	}
    }

    public static void main(final String[] args) throws Exception {
    	System.out.println("Waiting to start");
    	//Thread.sleep(30000);
    	System.out.println("Starting");

//        final Map<String, String> hm = new HashMap<String, String>();
//        iterTest(new Iterer() {
//            public String name() { return "HashMap"; }
//            public void put(String k, String v) { hm.put(k, v); }
//            public int iterate() {
//            	int out = 0;
//            	for(Entry<String,String> e: hm.entrySet()) {
//            		out++;
//            	}
//            	return out;
//            }
//        });
//
//        final Map<String, String> tm = new TreeMap<String, String>();
//        iterTest(new Iterer() {
//            public String name() { return "TreeMap"; }
//            public void put(String k, String v) { tm.put(k, v); }
//            public int iterate() {
//            	int out = 0;
//            	for(Entry<String,String> e: tm.entrySet()) {
//            		out++;
//            	}
//            	return out;
//            }
//        });

        final MCritBitTree<String, String> mcbc =
            new MCritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);
        iterTest(new Iterer() {
            public String name() { return "Mutable Crit Bit - Cursor"; }
            public void put(String k, String v) { mcbc.put(k, v); }
            public int iterate() {
            	CountCursor<String, String> c = new CountCursor<String, String>();
            	mcbc.traverse(c);
            	return c.count;
            }
        });

        final MCritBitTree<String, String> mcbi =
        	new MCritBitTree<String, String>(StringKeyAnalyzer.INSTANCE);
        iterTest(new Iterer() {
        	public String name() { return "Mutable Crit Bit - Iterator"; }
        	public void put(String k, String v) { mcbi.put(k, v); }
            public int iterate() {
            	int out = 0;
            	for(Entry<String,String> e: mcbi.entrySet()) {
            		out++;
            	}
            	return out;
            }
        });

        //Give us a chance to look at the heap.
        for(int i=0;i<Integer.MAX_VALUE;i++) Thread.sleep(1000);

        //Added to avoid garbage collection.
        //System.out.println(hm);
        //System.out.println(tm);
        System.out.println(mcbc);
        System.out.println(mcbi);
//      System.out.println(ptrie);
    }

}
