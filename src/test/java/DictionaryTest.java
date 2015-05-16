import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Sam Marsh
 */
public class DictionaryTest {

    private Dictionary<Integer> dictionary;

    @Before
    public void initialise() {
        dictionary = new RedBlackTree<Integer>();
    }

    @Test
    public void isEmptyAfterConstruction() {
        assertThat(dictionary.isEmpty(), is(true));
    }

    @Test
    public void notEmptyAfterItemAdded() {
        dictionary.add(1);
        assertThat(dictionary.isEmpty(), is(false));
    }

    @Test
    public void isEmptyAfterSoleItemRemoved() {
        dictionary.add(1);
        dictionary.delete(1);
        assertThat(dictionary.isEmpty(), is(true));
    }

    @Test
    public void containsItemAfterItemAdded() {
        dictionary.add(1);
        assertThat(dictionary.contains(1), is(true));
    }

    @Test
    public void soleItemIsMinimumAndMaximum() {
        dictionary.add(5);
        assertThat(dictionary.min(), is(5));
        assertThat(dictionary.max(), is(5));
    }

    @Test
    public void minimumAndMaximumUpdateCorrectly() {
        dictionary.add(3);
        dictionary.add(7);
        dictionary.add(1);
        assertThat(dictionary.min(), is(1));
        assertThat(dictionary.max(), is(7));
        dictionary.delete(7);
        dictionary.delete(1);
        assertThat(dictionary.min(), is(3));
        assertThat(dictionary.max(), is(3));
    }

    @Test
    public void hasPredecessorReturnsFalseForMinItem() {
        dictionary.add(1);
        assertThat(dictionary.hasPredecessor(1), is(false));
        dictionary.add(0);
        assertThat(dictionary.hasPredecessor(1), is(true));
    }

    @Test
    public void hasSuccessorReturnsFalseForMaxItem() {
        dictionary.add(0);
        assertThat(dictionary.hasSuccessor(0), is(false));
        dictionary.add(1);
        assertThat(dictionary.hasSuccessor(0), is(true));
    }

    @Test
    public void correctPredecessor() {
        for (int i = 0; i < 100; ++i) dictionary.add(i);
        assertThat(dictionary.predecessor(50), is(49));
    }

    @Test
    public void correctSuccessor() {
        for (int i = 0; i < 100; ++i) dictionary.add(i);
        assertThat(dictionary.successor(50), is(51));
    }

    @Test
    public void iteratesCorrectlyOverEntireDictionary() {
        for (int i = 0; i < 1000; ++i) dictionary.add(i);
        int expected = 0;
        for (int i : dictionary) {
            assertThat(i, is(expected++));
        }
    }

    @Test
    public void iteratesCorrectlyOverPartOfDictionary() {
        for (int i = 0; i < 1000; ++i) dictionary.add(i);
        int expected = 50;
        Iterator<Integer> iterator = dictionary.iterator(50);
        while (iterator.hasNext()) {
            assertThat(iterator.next(), is(expected++));
        }
    }

    @After
    public void printLog() throws IOException {
        System.out.println(dictionary.getLogString());
    }
}
