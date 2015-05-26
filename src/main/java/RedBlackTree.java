import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a
 *
 * @param <E>
 */
public class RedBlackTree<E extends Comparable<E>> implements Dictionary<E> {

    /**
     * The constant log message format, used in {@link #getLogString()}
     */
    private static final String LOG_MSG = "Operation %s completed " +
            "using %d comparison(s).%n";

    /**
     * Empty 'null' node used to make code cleaner - more convenient than
     * using null pointers as using this empty node means null checking isn't
     * as frequently required.
     */
    private final Node nil;

    /**
     * The current log string consisting of the history of method calls made,
     * along with the number of calls to {@link Comparable#compareTo(Object)}
     * made during execution of each operation. This log is cleared (reset)
     * each time {@link #getLogString()} is called.
     */
    private StringBuilder log;

    /**
     * Holds the root node of the tree. If the root node is null, then the
     * tree is empty. The root node has undefined {@link Node#parent}.
     */
    private Node root;

    /**
     * References to the current minimum and maximum element in the
     * dictionary. Undefined if the dictionary is empty. If the dictionary
     * contains a single element, min and max will both point to that element.
     */
    private Node min, max;

    /**
     * A convenience instance variable, that keeps track of the number of
     * calls to {@link Comparable#compareTo(Object)} for the currently
     * executing method. A call to {@link #reset()} is made at the start of
     * each method implemented from  the dictionary interface, to set this
     * variable back to zero.
     */
    public int comparisons;

    /**
     * Creates a new red-black tree, representing a dictionary, with no
     * elements.
     */
    public RedBlackTree() {
        nil = new Node(null);
        log = new StringBuilder();
        root = min = max = nil;
        comparisons = 0;
    }

    private void reset() {
        comparisons = 0;
    }

    /**
     * Checks if the dictionary is empty.
     *
     * @return true if and only if the dictionary contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return isEmpty(false);
    }

    /**
     * Internal method for checking if the dictionary is empty. See
     * documentation of method {@link #isEmpty()}. This method is also called
     * from other methods, in which case it should <i>not</i> be appended to
     * the log string. In the case that the client calls the
     * {@link #isEmpty()} method, it <i>should</i> be appended to the log
     * string.
     *
     * @param quiet if this parameter is true, the call will not be appended
     *              to the log string. If false, it will log the call.
     * @return true iff the dictionary contains no elements.
     */
    private boolean isEmpty(boolean quiet) {
        if (!quiet) {
            reset();
            log("isEmpty()");
        }
        return root == nil;
    }

    /**
     * Checks if the dictionary contains the given element. Runs in
     * logarithmic time.
     *
     * @param item the item to be checked.
     * @return true if and only if the dictionary contains the item
     */
    @Override
    public boolean contains(E item) {
        reset();
        boolean ret = locate(new Node(item)) != null;
        log(String.format("contains(%s)", item));
        return ret;
    }

    /**
     * Checks if the given item has a predecessor in the dictionary, that is,
     * if the item is in the dictionary and there exists a smaller element in
     * the dictionary. Generally should be called before calling
     * {@link #predecessor(Comparable)} in order to avoid a
     * {@link NoSuchElementException} in the case that the item does not have
     * a predecessor.
     *
     * @param item the item to be checked
     * @return true if and only if the item has a predecessor
     */
    @Override
    public boolean hasPredecessor(E item) {
        reset();
        boolean ret = compare(new Node(item), min) > 0; //TODO update doc
        log(String.format("hasPredecessor(%s)", item));
        return ret;
    }

    /**
     * Checks if the given item has a successor in the dictionary, that is,
     * if the item is in the dictionary and there exists a larger element in
     * the dictionary. Generally should be called before calling
     * {@link #successor(Comparable)} in order to avoid a
     * {@link NoSuchElementException} in the case that the item does not have
     * a successor.
     *
     * @param item the item to be checked
     * @return true if and only if the item has a successor
     */
    @Override
    public boolean hasSuccessor(E item) {
        reset();
        boolean ret = compare(new Node(item), max) < 0; //TODO update doc
        log(String.format("hasSuccessor(%s)", item));
        return ret;
    }

    /**
     * Finds the greatest element less than the specified element.
     *
     * @param item the item to be checked
     * @return the item 'directly before' the specified item - i.e. the
     * greatest element less than the specified element
     * @throws NoSuchElementException if the item is not in the dictionary or
     *                                if it does not have a predecessor (i.e.
     *                                if it is the minimum element)
     */
    @Override
    public E predecessor(E item) throws NoSuchElementException {
        reset();
        Node pre = predecessor(locate(new Node(item)));
        if (pre == null)
            throw new NoSuchElementException("Argument does not have a " +
                "predecessor");
        log(String.format("predecessor(%s)", item));
        return pre.key;
    }

    private Node predecessor(Node node) {
        if (node == null || node == min) return null;
        if (node.left != nil) return maximum(node.left);
        Node parent = node.parent;
        while (parent != nil && node == parent.left) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    /**
     * Finds the smallest element greater than the specified element.
     *
     * @param item the item to be checked
     * @return the item 'directly after' the specified item - i.e. the
     * smallest element greater than the specified element
     * @throws NoSuchElementException if the item is not in the dictionary or
     *                                if it does not have a successor (i.e.
     *                                if it is the maximum element)
     */
    @Override
    public E successor(E item) throws NoSuchElementException {
        reset();
        Node suc = successor(locate(new Node(item)));
        if (suc == null)
            throw new NoSuchElementException("Argument does not have a " +
                    "successor");
        log(String.format("successor(%s)", item));
        return suc.key;
    }

    private Node successor(Node node) {
        if (node == null || node == max) return null;
        if (node.right != nil) return minimum(node.right);
        Node parent = node.parent;
        while (parent != nil && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    /**
     * Finds the least element in the dictionary. Runs in constant time.
     *
     * @return the minimum (smallest) item in the dictionary
     * @throws NoSuchElementException if the dictionary is empty
     */
    @Override
    public E min() throws NoSuchElementException {
        if (isEmpty(true)) throw new NoSuchElementException("Dictionary is " +
                "empty");
        reset();
        log("min()");
        return min.key;
    }

    /**
     * Finds the greatest element in the dictionary. Runs in constant time.
     *
     * @return the greatest (maximum) item in the dictionary
     * @throws NoSuchElementException if the dictionary is empty
     */
    @Override
    public E max() throws NoSuchElementException {
        if (isEmpty(true))
            throw new NoSuchElementException("Dictionary is empty");
        reset();
        log("max()");
        return max.key;
    }

    /**
     * Adds the specified item to the dictionary, provided the item is not
     * null and is not already in the dictionary.
     *
     * @param item the item to be added
     * @return true if and only if the item was added successfully - that is,
     * if the item is not null and not already in the dictionary
     */
    @Override
    public boolean add(E item) {
        reset();
        Node node = new Node(item);
        boolean tmp = add(node);
        log(String.format("add(%s)", item));
        return tmp;
    }

    private boolean add(Node node) {
        Node temp = root;
        if (isEmpty(true)) {
            root = node;
            node.color = Node.COLOUR_BLACK;
            node.parent = nil;
        } else {
            node.color = Node.COLOUR_RED;
            while (true) {
                int cmp = compare(node, temp);
                if (cmp < 0) {
                    if (temp.left == nil) {
                        temp.left = node;
                        node.parent = temp;
                        break;
                    } else {
                        temp = temp.left;
                    }
                } else if (cmp > 0) {
                    if (temp.right == nil) {
                        temp.right = node;
                        node.parent = temp;
                        break;
                    } else {
                        temp = temp.right;
                    }
                } else if (cmp == 0) {
                    return false;
                }
            }
            fixTree(node);
        }
        if (min == nil || max == nil) {
            min = max = node;
        } else {
            if (compare(node, min) < 0) min = node;
            else if (compare(node, max) > 0) max = node;
        }
        return true;
    }

    /**
     * Removes an item from the dictionary, if it is contained in the
     * dictionary.
     *
     * @param item the element to be removed
     * @return true if and only if the element was in the dictionary and has
     * been removed
     */
    @Override
    public boolean delete(E item) {
        reset();
        Node z = locate(new Node(item));
        if (z == null) return false;
        delete(z);
        log(String.format("delete(%s)", item));
        return true;
    }

    private void delete(Node z) {
        Node x;
        Node y = z;
        int y_original_color = y.color;

        if (z.left == nil) {
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == nil) {
            x = z.left;
            transplant(z, z.left);
        } else {
            y = minimum(z.right);
            y_original_color = y.color;
            x = y.right;
            if (y.parent == z)
                x.parent = y;
            else {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
            y.color = z.color;
        }
        if (y_original_color == Node.COLOUR_BLACK)
            fixDelete(x);
        if (isEmpty(true)) min = max = nil;
        else if (z == min) min = minimum(root);
        else if (z == max) max = maximum(root);
    }

    private int compare(Node n1, Node n2) {
        ++comparisons;
        return n1.key.compareTo(n2.key);
    }

    /**
     * Returns an in-order iterator over all the elements in the dictionary.
     * That is, the elements in the iterator will be returned in sorted
     * order, from least element to greatest element.
     *
     * @return an iterator whose next element is the least element in the
     * dictionary, and which will iterate through all the elements in the
     * dictionary in ascending order
     */
    @Override
    public Iterator<E> iterator() {
        reset();
        log("iterator()");
        return new TreeIterator(min);
    }

    /**
     * Returns an in-order iterator over the elements in the dictionary,
     * starting from the least element that is greater than or equal to the
     * given element.
     *
     * @param start the element at which to start iterating at.
     * @return an iterator whose next element is the least element greater
     * than or equal to start in the dictionary, and which will iterate
     * through all the elements in the Dictionary in ascending order
     */
    @Override
    public Iterator<E> iterator(E start) {
        reset();
        Iterator<E> ret = new TreeIterator(
                locateMinNodeGreaterThan(new Node(start)));
        log(String.format("iterator(%s)", start));
        return ret;
    }

    /**
     * Provides a string that describes all operations performed on the
     * dictionary since its creation, or since the last time that the log
     * string was retrieved. Each line of the string will give the method name
     * of the operation and the parameter values, along with the number of
     * comparisons (calls to {@link Comparable#compareTo(Object)}) made by
     * the method. The most recent method call will be the last line of the
     * string, and the oldest method call will be the first line of the
     * string. Each time this method is called it clears the log string for
     * next time.
     *
     * @return a string listing all operations called on the dictionary, and
     * how many comparisons were required to complete each operation.
     */
    @Override
    public String getLogString() {
        String logString = log.toString();
        log = new StringBuilder();
        return logString;
    }

    /**
     * Provides a string representation of the dictionary, in tree form.
     *
     * @return a string with the structure of the dictionary along with the
     * associated values in the dictionary.
     */
    @Override
    public String toString() {
        reset();
        String ret = isEmpty(true) ? "└── \n" : root.toString();
        log("toString()");
        return ret;
    }

    private Node locate(Node toFind) {
        if (isEmpty(true)) return null;
        Node curr = root;
        while (curr != null) {
            int cmp = compare(toFind, curr);
            if (cmp < 0) {
                if (curr.left != nil) {
                    curr = curr.left;
                    continue;
                }
            } else if (cmp > 0) {
                if (curr.right != nil) {
                    curr = curr.right;
                    continue;
                }
            } else if (toFind.key.equals(curr.key)) {
                return curr;
            }
            curr = null;
        }
        return null;
    }

    private Node locateMinNodeGreaterThan(Node toFind) {
        if (isEmpty(true)) return null;
        Node curr = root;
        Node smallest = max;
        while (curr != null) {
            int cmp = compare(toFind, curr);
            if (cmp < 0) {
                if (compare(curr, smallest) < 0) smallest = curr;
                if (curr.left != nil) {
                    curr = curr.left;
                    continue;
                }
            } else if (cmp > 0) {
                if (curr.right != nil) {
                    curr = curr.right;
                    continue;
                }
            } else if (toFind.key.equals(curr.key)) {
                return curr;
            }
            curr = null;
        }
        return smallest;
    }

    private void fixTree(Node node) {
        while (node.parent.color == Node.COLOUR_RED) {
            Node uncle;
            if (node.parent == node.parent.parent.left) {
                uncle = node.parent.parent.right;

                if (uncle != nil && uncle.color == Node.COLOUR_RED) {
                    node.parent.color = Node.COLOUR_BLACK;
                    uncle.color = Node.COLOUR_BLACK;
                    node.parent.parent.color = Node.COLOUR_RED;
                    node = node.parent.parent;
                    continue;
                }
                if (node == node.parent.right) {
                    node = node.parent;
                    rotateLeft(node);
                }
                node.parent.color = Node.COLOUR_BLACK;
                node.parent.parent.color = Node.COLOUR_RED;
                rotateRight(node.parent.parent);
            } else {
                uncle = node.parent.parent.left;
                if (uncle != nil && uncle.color == Node.COLOUR_RED) {
                    node.parent.color = Node.COLOUR_BLACK;
                    uncle.color = Node.COLOUR_BLACK;
                    node.parent.parent.color = Node.COLOUR_RED;
                    node = node.parent.parent;
                    continue;
                }
                if (node == node.parent.left) {
                    node = node.parent;
                    rotateRight(node);
                }
                node.parent.color = Node.COLOUR_BLACK;
                node.parent.parent.color = Node.COLOUR_RED;
                rotateLeft(node.parent.parent);
            }
        }
        root.color = Node.COLOUR_BLACK;
    }

    private void rotateLeft(Node node) {
        if (node.parent != nil) {
            if (node == node.parent.left) node.parent.left = node.right;
            else node.parent.right = node.right;
            node.right.parent = node.parent;
            node.parent = node.right;
            if (node.right.left != nil) node.right.left.parent = node;
            node.right = node.right.left;
            node.parent.left = node;
        } else {
            Node right = root.right;
            root.right = right.left;
            right.left.parent = root;
            root.parent = right;
            right.left = root;
            right.parent = nil;
            root = right;
        }
    }

    private void rotateRight(Node node) {
        if (node.parent != nil) {
            if (node == node.parent.left) node.parent.left = node.left;
            else node.parent.right = node.left;
            node.left.parent = node.parent;
            node.parent = node.left;
            if (node.left.right != nil) node.left.right.parent = node;
            node.left = node.left.right;
            node.parent.right = node;
        } else {
            Node left = root.left;
            root.left = root.left.right;
            left.right.parent = root;
            root.parent = left;
            left.right = root;
            left.parent = nil;
            root = left;
        }
    }

    private void transplant(Node target, Node with) {
        if (target.parent == nil) root = with;
        else if (target == target.parent.left) target.parent.left = with;
        else target.parent.right = with;
        with.parent = target.parent;
    }

    private void fixDelete(Node x) {
        while (x != root && x.color == Node.COLOUR_BLACK) {
            if (x == x.parent.left) {
                Node w = x.parent.right;
                if (w.color == Node.COLOUR_RED) {
                    w.color = Node.COLOUR_BLACK;
                    x.parent.color = Node.COLOUR_RED;
                    rotateLeft(x.parent);
                    w = x.parent.right;
                }
                if (w.left.color == Node.COLOUR_BLACK && w.right.color ==
                        Node.COLOUR_BLACK) {
                    w.color = Node.COLOUR_RED;
                    x = x.parent;
                    continue;
                } else if (w.right.color == Node.COLOUR_BLACK) {
                    w.left.color = Node.COLOUR_BLACK;
                    w.color = Node.COLOUR_RED;
                    rotateRight(w);
                    w = x.parent.right;
                }
                if (w.right.color == Node.COLOUR_RED) {
                    w.color = x.parent.color;
                    x.parent.color = Node.COLOUR_BLACK;
                    w.right.color = Node.COLOUR_BLACK;
                    rotateLeft(x.parent);
                    x = root;
                }
            } else {
                Node w = x.parent.left;
                if (w.color == Node.COLOUR_RED) {
                    w.color = Node.COLOUR_BLACK;
                    x.parent.color = Node.COLOUR_RED;
                    rotateRight(x.parent);
                    w = x.parent.left;
                }
                if (w.right.color == Node.COLOUR_BLACK && w.left.color ==
                        Node.COLOUR_BLACK) {
                    w.color = Node.COLOUR_RED;
                    x = x.parent;
                    continue;
                } else if (w.left.color == Node.COLOUR_BLACK) {
                    w.right.color = Node.COLOUR_BLACK;
                    w.color = Node.COLOUR_RED;
                    rotateLeft(w);
                    w = x.parent.left;
                }
                if (w.left.color == Node.COLOUR_RED) {
                    w.color = x.parent.color;
                    x.parent.color = Node.COLOUR_BLACK;
                    w.left.color = Node.COLOUR_BLACK;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }
        x.color = Node.COLOUR_BLACK;
    }

    /**
     * Adds a new line to the log string describing the method that was just
     * called and the number of comparisons made.
     * @param method the method name.
     */
    private void log(String method) {
        //log.append(String.format(LOG_MSG, method, comparisons));
    }

    /**
     * Finds the maximum node in a subtree.
     * @param node the root node of this subtree
     * @return the maximum node in the subtree
     */
    private Node maximum(Node node) {
        while (node.right != nil) node = node.right;
        return node;
    }

    /**
     * Finds the minimum node in a subtree.
     * @param node the root node of this subtree
     * @return the minimum node in the subtree
     */
    private Node minimum(Node node) {
        while (node.left != nil) node = node.left;
        return node;
    }

    /**
     * An internal class representing an internal red-black tree node. Each
     * node instance is coloured either red or black.
     */
    private class Node {

        /**
         * The byte value representing the colour red.
         */
        private static final byte COLOUR_RED = 0;

        /**
         * The byte value representing the colour black.
         */
        private static final byte COLOUR_BLACK = 1;

        /**
         * The 'data' held by the node.
         */
        private E key;

        /**
         * The current colour of the node.
         */
        private int color = COLOUR_BLACK;

        /**
         * References to the left child of the node, the right child of the
         * node, and the node's parent. The left child is less than this node,
         * and the right child is greater.
         */
        private Node left, right, parent;

        /**
         * Creates a new black node with undefined children and parent.
         *
         * @param key the element (key/value) for this node to hold.
         */
        Node(E key) {
            this.key = key;
            left = nil;
            right = nil;
            parent = nil;
        }

        /**
         * Returns a string representing the internal state of the subtree,
         * with this node as root.
         * @return a string representation of this node's subtree
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString("", sb, true);
            return sb.toString();
        }

        /**
         * Internal recursive method for filling in the {@link
         * StringBuilder}, as used in {@link #toString()}.
         * @param prefix the character directly before this node
         * @param sb the current string representation of the tree, appended
         *           to by this method
         * @param tail whether this node is the last sibling
         */
        private void toString(String prefix, StringBuilder sb, boolean tail) {
            sb.append(prefix)
                    .append(tail ? "└── " : "├── ")
                    .append(key)
                    .append('\n');
            if (left != nil) {
                left.toString(
                        prefix + (tail ? "    " : "│   "),
                        sb,
                        right == nil
                );
            }
            if (right != nil) {
                right.toString(
                        prefix + (tail ? "    " : "│   "),
                        sb,
                        true
                );
            }
        }

    }

    /**
     * An in-order iterator over the elements of the dictionary.
     */
    private class TreeIterator implements Iterator<E> {

        /**
         * The node most recently returned from {@link #next()} - used in the
         * {@link #remove()} method.
         */
        private Node last;

        /**
         * The node that will be returned next.
         */
        private Node next;

        /**
         * Creates a new iterator starting at the given element.
         * @param start the 'start' element. Will be returned by the
         *              iterator first.
         */
        private TreeIterator(Node start) {
            last = null;
            next = start;
        }

        /**
         * Checks if the iterator has any more elements.
         * @return true if and only if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /**
         * Provides the next element in the dictionary, guaranteed to be
         * >= than the previous element returned and <= than
         * future elements returned. i.e. this method will return all
         * elements from least to greatest in order.
         *
         * @return the next element in the dictionary
         * @throws NoSuchElementException if all elements have already been
         * returned, that is, if the iterator is 'used up'.
         */
        @Override
        public E next() throws NoSuchElementException {
            if (!hasNext())
                throw new NoSuchElementException("No further elements");
            last = next;
            next = successor(next);
            return last.key;
        }

        /**
         * Deletes the last item returned by {@link #next()} from the
         * dictionary. {@link #next()} needs to have been called at least
         * once on this iterator, and this method can only be called once per
         * item returned from {@link #next()}.
         *
         * @throws IllegalStateException if the {@link #next()} method has not
         * yet been called, or the remove method has already been called
         * after the last call to the {@link #next()} method
         */
        @Override
        public void remove() throws IllegalStateException {
            if (last == null)
                throw new IllegalStateException("");
            delete(last);
            //set last to null so that if this method is called again without
            //calling next first, an exception will be thrown
            last = null;
        }

    }

}