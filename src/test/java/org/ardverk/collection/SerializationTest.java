/*
 * Copyright 2005-2008 Roger Kapsi, Sam Berlin
 *
 * Modified 2011 Jason Fager
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection;

import io.prelink.critbit.MCritBitTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.junit.Test;

public class SerializationTest {

    @Test
    public void serialize() throws IOException, ClassNotFoundException {
        MCritBitTree<String, String> trie1
            = new MCritBitTree<String, String>(
                StringKeyAnalyzer.INSTANCE);
        trie1.put("Hello", "World");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trie1);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        @SuppressWarnings("unchecked")
        MCritBitTree<String, String> trie2 =
            (MCritBitTree<String, String>)ois.readObject();
        ois.close();

        TestCase.assertEquals(trie1.size(), trie2.size());
        TestCase.assertEquals("World", trie2.get("Hello"));
    }

    @Test
    public void prefixMap() throws IOException, ClassNotFoundException {
        MCritBitTree<String, String> trie1
            = new MCritBitTree<String, String>(
                StringKeyAnalyzer.INSTANCE);
        trie1.put("Hello", "World");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trie1);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        @SuppressWarnings("unchecked")
        MCritBitTree<String, String> trie2 =
            (MCritBitTree<String, String>)ois.readObject();
        ois.close();

//        TestCase.assertEquals(1, trie1.prefixMap("Hello").size());
//        TestCase.assertEquals(1, trie2.prefixMap("Hello").size());
    }
}
