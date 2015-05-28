import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A dictionary implementation using a type of balanced binary search tree
 * called a red-black tree.
 * The dictionary is sorted using the natural ordering of the elements. No
 * duplicate values are allowed - if two elements are considered the same by
 * their natural ordering ({@link Comparable#compareTo(Object)} returns 0)
 * then they are considered equal by the dictionary.
 * The implementation provides guaranteed logarithmic time for adding,
 * deleting and searching.
 * This implementation is primarily adapted from the book 'Introduction to
 * Algorithms: Third Edition' by T. Cormen, C. E. Leiserson, R. L. Rivest and
 * C. Stein.
 *
 * @author Samuel Marsh
 *
 * @param <E> the type of elements that the dictionary holds
 */
public class RedBlackTree<E extends Comparable<E>> implements Dictionary<E> {

    /**
     * The constant log message format, used in {@link #getLogString()}
     */
    private static final String LOG_MSG = "Operation %s completed " +
            "using %d comparison(s).%n";

    /**
     * Empty sentinel node used to make code cleaner - more convenient than
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
     * An integer to keep track of the total number of modifications on
     * the dictionary - this is used to ensure a fail-fast iterator. Note:
     * this does not include modifications made by an iterator, only through
     * the {@link Dictionary#add(Comparable)} and
     * {@link Dictionary#delete(Comparable)} methods. So it is not an
     * way of determining the correct total number of modifications to the
     * dictionary, it is only for ensuring a fail-fast iterator.
     */
    private int operations;

    /**
     * Creates a new red-black tree, representing a dictionary, with no
     * elements.
     */
    public RedBlackTree() {
        nil = new Node(null);
        log = new StringBuilder();
        root = min = max = nil;
        comparisons = 0;
        operations = 0;
    }

    /**
     * Resets the comparison counter. Used at the start of each public method
     * defined in the {@link Dictionary} interface, so that the number of
     * node comparisons can be kept track of (for the log string).
     */
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
        boolean ret = locate(new Node(item)) != nil;
        log(String.format("contains(%s)", item));
        return ret;
    }

    /**
     * Checks if the given item has a predecessor in the dictionary, that is,
     * if there exists a smaller element in the dictionary. Generally should
     * be called before calling {@link #predecessor(Comparable)} in order to
     * avoid a {@link NoSuchElementException} in the case that the item does
     * not have a predecessor.
     *
     * @param item the item to be checked
     * @return true if and only if the item has a predecessor
     */
    @Override
    public boolean hasPredecessor(E item) {
        reset();
        boolean ret = !isEmpty(true) && compare(new Node(item), min) > 0;
        log(String.format("hasPredecessor(%s)", item));
        return ret;
    }

    /**
     * Checks if the given item has a successor in the dictionary, that is,
     * if there exists a larger element in the dictionary. Generally should
     * be called before calling {@link #successor(Comparable)} in order to
     * avoid a {@link NoSuchElementException} in the case that the item does
     * not have a successor.
     *
     * @param item the item to be checked
     * @return true if and only if the item has a successor
     */
    @Override
    public boolean hasSuccessor(E item) {
        reset();
        boolean ret = !isEmpty(true) && compare(new Node(item), max) < 0;
        log(String.format("hasSuccessor(%s)", item));
        return ret;
    }

    /**
     * Finds the greatest element less than the specified element.
     *
     * @param item the item to be checked
     * @return the item 'directly before' the specified item - i.e. the
     * greatest element less than the specified element
     * @throws NoSuchElementException if the item does not have a predecessor
     * (i.e. if it is the minimum element)
     */
    @Override
    public E predecessor(E item) throws NoSuchElementException {
        if (!hasPredecessor(item))
            throw new NoSuchElementException("Argument does not have a " +
                    "predecessor");
        reset();
        Node pre = below(new Node(item));
        log(String.format("predecessor(%s)", item));
        return pre.key;
    }

    /**
     * Finds the smallest element greater than the specified element.
     *
     * @param item the item to be checked
     * @return the item 'directly after' the specified item - i.e. the
     * smallest element greater than the specified element
     * @throws NoSuchElementException if the item does not have a successor
     * (i.e. if it is the maximum element)
     */
    @Override
    public E successor(E item) throws NoSuchElementException {
        if (!hasSuccessor(item))
            throw new NoSuchElementException("Argument does not have a " +
                    "successor");
        reset();
        Node suc = above(new Node(item));
        log(String.format("successor(%s)", item));
        return suc.key;
    }

    /**
     * Finds the successor of a node. Used in the
     * {@link TreeIterator#next()} method when iterating over the dictionary.
     *
     * @param node the node to find the successor for
     * @return nil if the node doesn't have a successor, otherwise the
     * least node greater than the argument
     */
    private Node successor(Node node) {
        //fairly basic strategy - if the node has a right child, the
        //successor is the minimum node of that subtree, otherwise move up
        //the tree until we find a node such that the node is a left child -
        //then the successor will be the parent of that node
        if (node == nil || node == max) return nil;
        if (node.right != nil) return minimum(node.right);
        Node parent = node.parent;
        while (parent != nil && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    /**
     * Finds the successor of a node. Used in the
     * {@link TreeIterator#next()} method when iterating over the dictionary.
     *
     * @param node the node to find the successor for
     * @return nil if the node doesn't have a successor, otherwise the
     * least node greater than the argument
     */
    private Node predecessor(Node node) {
        //fairly basic strategy - if the node has a left child, the
        //predecessor is the minimum node of that subtree, otherwise move up
        //the tree until we find a node such that the node is a right child -
        //then the predecessor will be the parent of that node
        if (node == nil || node == max) return nil;
        if (node.left != nil) return maximum(node.left);
        Node parent = node.parent;
        while (parent != nil && node == parent.left) {
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
        boolean tmp = insert(node);
        if (tmp) ++operations; //we successfully added an item
        log(String.format("add(%s)", item));
        return tmp;
    }

    /**
     * Internal method to insert a node into the red-black tree, and
     * re-balance/restore red-black tree properties if necessary.
     *
     * @param toInsert the node to insert into the dictionary
     * @return true if the node was successfully inserted - that is, if the
     * dictionary didn't already contain the node.
     */
    private boolean insert(Node toInsert) {
        if (toInsert.key == null) return false;
        Node curr = root;
        //if the tree is empty, we simply set up the root node and then
        //return early, since we don't need to do any further
        //fixing/comparisons.
        if (isEmpty(true)) {
            root = toInsert;
            toInsert.color = Node.COLOUR_BLACK;
            toInsert.parent = nil;
            min = max = root;
            return true;
        } else {
            toInsert.color = Node.COLOUR_RED;
            //locate the position to insert the new node
            while (true) {
                int cmp = compare(toInsert, curr);
                if (cmp < 0) {
                    if (curr.left == nil) {
                        curr.left = toInsert;
                        toInsert.parent = curr;
                        break;
                    } else curr = curr.left;
                } else if (cmp > 0) {
                    if (curr.right == nil) {
                        curr.right = toInsert;
                        toInsert.parent = curr;
                        break;
                    } else curr = curr.right;
                } else if (cmp == 0) return false;
            }
            //after insertion, we re-balance/restore red-black tree properties
            fixInsert(toInsert);
        }
        //the below code uses two more comparisons to check if the new node
        //is the new max/min of the tree, and updating if necessary.
        if (min == nil || max == nil) {
            min = max = toInsert;
        } else {
            if (compare(toInsert, min) < 0) min = toInsert;
            else if (compare(toInsert, max) > 0) max = toInsert;
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
        if (z == nil) return false;
        delete(z);
        ++operations; //we successfully deleted an item
        log(String.format("delete(%s)", item));
        return true;
    }

    /**
     * Internal method to delete a node from the red-black tree, and restore
     * red-black tree properties if necessary.
     *
     * @param toDelete the node to remove from the dictionary
     */
    private void delete(Node toDelete) {
        //the node that moves into curr's original position in the tree
        Node move;
        //the node either removed from the tree or moved within the tree
        Node curr = toDelete;
        //the original colour of the node to delete - we need to save this in
        //order to check the colour at the end of the method - if it is black
        //then we need to fix possible violations of the red-black tree
        //properties.
        int yOrigColour = curr.color;

        //check the cases - dependent on how many children the node has
        if (toDelete.left == nil) {
            //if the node to delete doesn't have a left child, just replace it
            //by the right child
            move = toDelete.right;
            transplant(toDelete, toDelete.right);
        } else if (toDelete.right == nil) {
            //and vice versa for the left child
            move = toDelete.left;
            transplant(toDelete, toDelete.left);
        } else {
            //if it has two children, find the successor
            curr = minimum(toDelete.right);
            //update the colour before any changes to the tree structure occur
            yOrigColour = curr.color;
            move = curr.right;
            if (curr.parent == toDelete) {
                move.parent = curr;
            } else {
                transplant(curr, curr.right);
                curr.right = toDelete.right;
                curr.right.parent = curr;
            }
            transplant(toDelete, curr);
            curr.left = toDelete.left;
            curr.left.parent = curr;
            curr.color = toDelete.color;
        }

        //if the node colour was black then we might have violated the
        //properties of a red-black tree - so fix up the tree
        if (yOrigColour == Node.COLOUR_BLACK)
            fixDelete(move);

        //finally, update the references to the min/max if necessary
        if (isEmpty(true)) min = max = nil;
        else if (toDelete == min) min = minimum(root);
        else if (toDelete == max) max = maximum(root);
    }

    /**
     * Gives the result of {@link Comparable#compareTo(Object)} when
     * comparing the first node to the second, and also increments the number
     * of comparisons used. This method is used instead of {@link
     * Comparable#compareTo(Object)} to keep track of comparisons for the log
     * string.
     *
     * @param n1 the first node
     * @param n2 the node to compare the first node to
     * @return the result of n1.compareTo(n2)
     */
    private int compare(Node n1, Node n2) {
        ++comparisons;
        return n1.compareTo(n2);
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
                ceiling(new Node(start)));
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

    /**
     * Finds the node <it>contained</it> in the tree that has the same
     * value as the given node.
     *
     * @param toFind the node reference to find in the tree
     * @return the valid node, with parent/left child/right child etc. values
     * filled in, that has the equal element to the argument (or nil if no
     * such node is found)
     */
    private Node locate(Node toFind) {
        //if the tree is empty, no node exists
        if (isEmpty(true)) return nil;
        Node curr = root;
        //move down the tree until we find an element with the same value (as
        //defined by their comparative values)
        while (curr != nil) {
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
            } else if (cmp == 0) {
                return curr;
            }
            curr = nil;
        }
        //didn't find anything, return the nil sentinel
        return nil;
    }

    /**
     * Finds the least node strictly greater than a given node. Used in the
     * {@link #successor(Comparable)} method.
     *
     * @param node the node to find the 'successor' for
     * @return the least node greater than the argument, or nil if there
     * isn't one.
     */
    private Node above(Node node) {
        Node curr = root;
        while (curr != nil) {
            int cmp = compare(node, curr);
            if (cmp < 0) {
                if (curr.left != nil) {
                    curr = curr.left;
                } else {
                    return curr;
                }
            } else {
                //the argument is greater than or equal to the current node, so
                //we either continue moving to the right down the tree or, if
                //no right child exists, the node below will just be the
                //successor of the current node
                if (curr.right != nil) {
                    curr = curr.right;
                } else {
                    return successor(curr);
                }
            }
        }
        return nil;
    }

    /**
     * Finds the greatest node strictly less than a given node. Used in the
     * {@link #predecessor(Comparable)} method.
     *
     * @param node the node to find the 'predecessor' for
     * @return the greatest node less than the argument, or nil if there
     * isn't one.
     */
    private Node below(Node node) {
        Node curr = root;
        while (curr != nil) {
            int cmp = compare(node, curr);
            if (cmp > 0) {
                if (curr.right != nil) {
                    curr = curr.right;
                } else {
                    return curr;
                }
            } else {
                //the argument is less than or equal to the current node, so
                //we either continue moving to the left down the tree or, if
                //no left child exists, the node below will just be the
                //predecessor of the current node
                if (curr.left != nil) {
                    curr = curr.left;
                } else {
                    return predecessor(curr);
                }
            }
        }
        return nil;
    }

    /**
     * Finds the least node greater than or equal to the given node. That is,
     * if the argument's key is in the dictionary it will return that node,
     * otherwise it will return the smallest node with key greater than the
     * argument's key.
     *
     * @param toFind the key to find the ceiling for
     * @return the least key greater than or equal to the argument, or the
     * nil sentinel if no such key exists.
     */
    private Node ceiling(Node toFind) {
        Node curr = root;
        while (curr != nil) {
            int cmp = compare(toFind, curr);
            if (cmp < 0) {
                if (curr.left != nil) {
                    curr = curr.left;
                } else {
                    return curr;
                }
            } else if (cmp > 0) {
                if (curr.right != nil) {
                    curr = curr.right;
                } else {
                    return successor(curr);
                }
            } else {
                //we found the element in the dictionary, so just return the
                //reference to the valid node
                return curr;
            }
        }
        return nil;
    }

    /**
     * After an insert, restores the properties of a red-black tree after any
     * possible violations. After an insert, we may need to fix the following
     * red-black tree properties:
     * -the root must be black
     * -a red node must have black children
     *
     * @param node the node we have just inserted into the tree
     */
    private void fixInsert(Node node) {
        //continue until the parent is black
        while (node.parent.color == Node.COLOUR_RED) {
            Node uncle;
            if (node.parent == node.parent.parent.left) {
                //set the uncle to be the right child
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
                //set the uncle to be the left child
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

    /**
     * Left-rotates the subtree around a given node. Runs in constant time.
     * Transforms the subtree in the following way:
     *
     *       |                     |
     *       x                     y
     *     /  \                   / \
     *    a   y      ===>        x  c
     *       / \                / \
     *      b  c               a  b
     *
     * The rotation preserves the properties of the red-black tree.
     *
     * @param node the node to rotate about
     */
    private void rotateLeft(Node node) {
        if (node.parent != nil) {
            if (node == node.parent.left)
                node.parent.left = node.right;
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

    /**
     * Right-rotates the subtree around a given node. Runs in constant time.
     * Transforms the subtree in the following way:
     *
     *       |                 |
     *       y                 x
     *      / \     ===>      / \
     *     x  c              a  y
     *    / \                  / \
     *   a  b                 b  c
     *
     * The rotation preserves the properties of the red-black tree.
     *
     * @param node the node to rotate about
     */
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

    /**
     * Convenience method to move subtrees around within the red-black tree.
     * Replaces one subtree as a child of its parent with another subtree.
     * Node u's parent becomes node v's parent, and u's parent has v as the
     * appropriate child.
     *
     * @param u the node to transplant
     * @param v the node to transplant u with
     */
    private void transplant(Node u, Node v) {
        //handle the case when u is the root
        if (u.parent == nil) root = v;
        //if u is the left child, update accordingly
        else if (u == u.parent.left) u.parent.left = v;
        //otherwise u is the right child
        else u.parent.right = v;
        //we can assign to the parent of v even if v is the sentinel
        v.parent = u.parent;
    }

    /**
     * Restores the following properties to the red-black tree after deletion
     * of a node:
     * -every node is either red or black
     * -the root is black
     * -if a node is red then both of its children are black
     *
     * @param x the node that occupies the deleted node's original position
     */
    private void fixDelete(Node x) {
        //move up the TODO
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
        log.append(String.format(LOG_MSG, method, comparisons));
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
    private class Node implements Comparable<Node> {

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
         *
         * Adapted from: http://stackoverflow.com/a/8948691/2251226
         *
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

        /**
         * Wraps the compareTo call of the key of this node for convenience.
         *
         * @param node the node to compare this node with
         * @return the result of comparing the two node's keys
         */
        @Override
        public int compareTo(Node node) {
            return key.compareTo(node.key);
        }

    }

    /**
     * An in-order iterator over the elements of the dictionary, starting at
     * a given node. If any modifications are made to the dictionary after
     * the construction of the dictionary, the iterator is invalidated and
     * any method calls to any of the iterator's methods will cause a
     * {@link ConcurrentModificationException}.
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
         * The <it>original</it> number of modifications made on the
         * dictionary when this iterator was created - if this is different
         * from the current number of modifications on the dictionary, then
         * we know that it has been modified.
         */
        private int ops;

        /**
         * Creates a new iterator starting at the given element.
         * @param start the 'start' element. Will be returned by the
         *              iterator first.
         */
        private TreeIterator(Node start) {
            last = nil;
            next = start;
            ops = operations;
        }

        /**
         * Checks if the iterator has any more elements.
         * @return true if and only if the iterator has more elements.
         * @throws ConcurrentModificationException if any modifications have
         * been made to the backing dictionary since this iterator's
         * construction
         */
        @Override
        public boolean hasNext() throws ConcurrentModificationException {
            if (ops != operations)
                throw new ConcurrentModificationException("backing dictionary" +
                        " has been modified");
            return next != nil;
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
         * @throws ConcurrentModificationException if any modifications have
         * been made to the backing dictionary since this iterator's
         * construction
         */
        @Override
        public E next() throws NoSuchElementException,
                ConcurrentModificationException {
            if (!hasNext())
                throw new NoSuchElementException("No further elements");
            if (ops != operations)
                throw new ConcurrentModificationException("backing dictionary" +
                        " has been modified");
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
         * @throws ConcurrentModificationException if any modifications have
         * been made to the backing dictionary since this iterator's
         * construction
         */
        @Override
        public void remove() throws IllegalStateException {
            if (ops != operations)
                throw new ConcurrentModificationException("backing dictionary" +
                        " has been modified");
            if (last == nil)
                throw new IllegalStateException("");
            delete(last);
            //set last to nil so that if this method is called again without
            //calling next first, an exception will be thrown
            last = nil;
        }

    }

}