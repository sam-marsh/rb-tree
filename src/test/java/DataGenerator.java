import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sam Marsh
 */
public class DataGenerator {

    private static final int NUM_TRIALS = 100;
    private static final int SIZE = 10000;

    public static void main(String[] args) throws IOException {
        File f = new File("/Users/Sam/Desktop/add.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        RedBlackTree<Integer> dictionary;
        List<Integer> collection = new ArrayList<Integer>();
        double cmp = 0;
        for (int i = 0; i < SIZE; ++i)
            collection.add(i);
        for (int trial = 0; trial < NUM_TRIALS; ++trial) {
            Collections.shuffle(collection);
            dictionary = new RedBlackTree<Integer>();
            for (int i = 0; i < SIZE; ++i) {
                dictionary.add(collection.get(i));
            }
            dictionary.contains((int) (Math.random() * SIZE));
            cmp += dictionary.comparisons;
        }
        System.out.println(cmp / NUM_TRIALS);
    }
}
