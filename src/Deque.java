import tester.*;

// an abstract node in a Deque
abstract class ANode<T> {
  // the next Node in the Deque
  ANode<T> next;

  // the previus Node in the Deque
  ANode<T> prev;

  // EFFECT: updates the reference to the next node
  void updateNext(ANode<T> next) {
    this.next = next;
  }

  // EFFECT: updates the reference to the previous node
  void updatePrev(ANode<T> prev) {
    this.prev = prev;
  }

  // returns the value of the next node in the Deque after this one
  ANode<T> getNext() {
    return this.next;
  }

  // returns the value of the previous node in the Deque before this one
  ANode<T> getPrev() {
    return this.prev;
  }

  // increment the count of the running total number of elements in a Deque
  // according to whether
  // this ANode is a Node or a Sentinel
  abstract int incElements();

  // return this ANode if it matches the given predicate
  // otherwise, continue searching the Deque
  // if this is a Sentinel, return this
  abstract ANode<T> findMatch(IPred<T> pred);

  // EFFECT: removes this ANode from the Deque
  abstract void remove();

  // EFFECT: adds a node after the this ANode
  public void addAfter(T t) {
    new Node<>(t, this.next, this);
  }

  // get the data stored in this ANode before removing it
  // if this node is a Sentinel, error
  abstract T getData();
}

// a Node that has data in a Deque
class Node<T> extends ANode<T> {

  // the data that this Node contains
  T data;

  // constructor
  Node(T data) {
    this.data = data;
    this.next = null;
    this.prev = null;
  }

  // convenience constructor
  Node(T data, ANode<T> next, ANode<T> prev) {
    if (next == null || prev == null) {
      throw new IllegalArgumentException("The given nodes can't be null.");
    } else {
      this.data = data;
      this.next = next;
      this.prev = prev;
      this.next.updatePrev(this);
      this.prev.updateNext(this);
    }
  }

  // increment the count of the running total number of elements in a Deque
  int incElements() {
    return 1 + this.next.incElements();
  }

  // return this Node's data matches the given predicate
  // otherwise, continue searching the Deque
  ANode<T> findMatch(IPred<T> pred) {
    if (pred.apply(this.data)) {
      return this;
    } else {
      return this.next.findMatch(pred);
    }
  }

  // EFFECT: remove this Node from the Deque
  void remove() {
    this.next.updatePrev(this.prev);
    this.prev.updateNext(this.next);
  }

  // get the data stored in this Node
  T getData() {
    return this.data;
  }
}

// A header node that references the head and tail of the entire Deque
class Sentinel<T> extends ANode<T> {

  // constructor
  Sentinel() {
    this.next = this;
    this.prev = this;
  }

  // find the total length of this deque
  // return 0, since this is the last element in the list
  int incElements() {
    return 0;
  }

  // finds the first node within the deque that matches the given predicate
  // returns this node, since no nodes matched the given predicate
  ANode<T> findMatch(IPred<T> pred) {
    return this;
  }

  // EFFECT: remove this Sentinel from the deque
  void remove() {
    // does nothing because this Sentinel should never be removed
  }

  // get data from this Sentinel before removing
  // error because this method will be called when attempting to remove a Sentinel
  T getData() {
    throw new RuntimeException("Attempting to remove from an empty list.");
  }
}

// a circular (in both directions) list of nodes
class Deque<T> {
  
  // the header node for this Deque
  Sentinel<T> header;
  //  Iterator<T> iterator;

  // constructor
  Deque() {
    this.header = new Sentinel<T>();
    //    this.iterator = new ForwardDequeIterator<T>(this.header);
  }

  // convenience constructor
  Deque(Sentinel<T> header) {
    this.header = header;
  }

  // return the total number of non-sentinel nodes in this Deque
  int size() {
    return this.header.getNext().incElements();
  }

  // EFFECT: add the given data into a new node at the beginning of this Deque
  void addAtHead(T t) {
    this.header.addAfter(t);
  }

  // EFFECT: add the given data into a new node at the end of this Deque
  void addAtTail(T t) {
    // new Node<>(t, this.header, this.header.getPrev());
    // this.add(t, this.header.getPrev());
    this.header.getPrev().addAfter(t);
  }

  // EFFECT: remove the first node in this Deque and return the data that it
  // contained
  // error if this Deque is empty
  T removeFromHead() {
    T data = this.header.getNext().getData();
    this.header.getNext().remove();
    return data;
  }

  // EFFECT: remove the last node in this Deque and return the data that it
  // contained
  // error if this Deque is empty
  T removeFromTail() {
    T data = this.header.getPrev().getData();
    this.header.getPrev().remove();
    return data;
  }

  // find the first node in this Deque that matches the given predicate
  // if no node matches, return the predicate
  ANode<T> find(IPred<T> pred) {
    return this.header.getNext().findMatch(pred);
  }

  // EFFECT: removes the given node from this Deque
  void removeNode(ANode<T> node) {
    node.remove();
  }
}

// is the given integer even?
class Even implements IPred<Integer> {
  public boolean apply(Integer num) {
    return num % 2 == 0;
  }
}

// is the given integer odd?
class Odd implements IPred<Integer> {
  public boolean apply(Integer num) {
    return num % 2 == 1;
  }
}

// does the given string have a length of 3?
class ThreeLettersLong implements IPred<String> {
  public boolean apply(String str) {
    return str.length() == 3;
  }
}

// does the given string have a length of 4?
class FourLettersLong implements IPred<String> {
  public boolean apply(String str) {
    return str.length() == 4;
  }
}

//represents an IFunc which specifically returns a boolean
interface IPred<T> {
  boolean apply(T t);
}

// tester method
class ExamplesDeque {
  Deque<Boolean> deque1;
  Deque<String> deque2;
  Deque<Integer> deque3;

  void initData() {
    this.deque1 = new Deque<Boolean>();
    this.deque2 = new Deque<String>();
    this.deque3 = new Deque<Integer>();

    this.deque2.addAtTail("abc");
    this.deque2.addAtTail("bcd");
    this.deque2.addAtTail("cde");
    this.deque2.addAtTail("def");

    this.deque3.addAtTail(6);
    this.deque3.addAtTail(2);
    this.deque3.addAtTail(8);
    this.deque3.addAtTail(5);
    this.deque3.addAtTail(1);
  }

  /*
   * Tests for helper methods
   */

  // test updateNext and getNext method in class ANode
  void testUpdateAndGetNext(Tester t) {
    this.initData();

    t.checkExpect(this.deque1.header.getNext(), this.deque1.header);
    t.checkExpect(this.deque2.header.getNext().getData(), "abc");

    this.deque2.header.updateNext(this.deque2.header.getNext());
    t.checkExpect(this.deque2.header.getNext().getData(), "abc");
    this.deque2.header.updateNext(this.deque2.header.getPrev());
    t.checkExpect(this.deque2.header.getNext().getData(), "def");
  }

  // test updatePrev and getPrev method in class ANode
  void testUpdateAndGetPrev(Tester t) {
    this.initData();

    t.checkExpect(this.deque1.header.getPrev(), this.deque1.header);
    t.checkExpect(this.deque2.header.getPrev().getData(), "def");

    this.deque2.header.updatePrev(this.deque2.header.getPrev());
    t.checkExpect(this.deque2.header.getPrev().getData(), "def");
    this.deque2.header.updatePrev(this.deque2.header.getNext());
    t.checkExpect(this.deque2.header.getPrev().getData(), "abc");

  }

  // test incElements method in class ANode
  void testIncElements(Tester t) {
    this.initData();

    t.checkExpect(deque2.header.incElements(), 0);
    t.checkExpect(deque2.header.getNext().incElements(), 4);
    t.checkExpect(deque2.header.getNext().getNext().incElements(), 3);

  }

  // test the findMatch method in class ANode
  void testFindMatch(Tester t) {
    this.initData();

    t.checkExpect(deque2.header.getNext().findMatch(new ThreeLettersLong()),
        deque2.header.getNext());
    t.checkExpect(deque3.header.getNext().findMatch(new Odd()),
        deque3.header.getNext().getNext().getNext().getNext());
    t.checkExpect(deque2.header.findMatch(new ThreeLettersLong()), deque2.header);
    t.checkExpect(deque2.header.getNext().findMatch(new FourLettersLong()), deque2.header);
  }

  // test the remove method in class ANode
  void testRemove(Tester t) {
    this.initData();

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");
    t.checkExpect(this.deque2.header.getPrev().getData(), "def");
    // literally does nothing
    this.deque2.header.remove();

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");
    t.checkExpect(this.deque2.header.getPrev().getData(), "def");

    t.checkExpect(this.deque3.header.getNext().getData(), 6);
    t.checkExpect(this.deque3.header.getNext().getNext().getData(), 2);

    this.deque3.header.getNext().remove();

    t.checkExpect(this.deque3.header.getNext().getData(), 2);
    t.checkExpect(this.deque3.header.getNext().getPrev(), this.deque3.header);

  }

  // test the addAfter method in class ANode
  void testAddAfter(Tester t) {
    this.initData();

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");

    this.deque2.header.addAfter("hello");

    t.checkExpect(this.deque2.header.getNext().getData(), "hello");
    t.checkExpect(this.deque2.header.getNext().getNext().getData(), "abc");
    t.checkExpect(this.deque2.header.getNext().getNext().getPrev(), this.deque2.header.getNext());
    t.checkExpect(this.deque2.header.getNext().getPrev(), this.deque2.header);

  }

  // test the getData method in class ANode
  void testGetData(Tester t) {
    this.initData();
    t.checkException(new RuntimeException("Attempting to remove from an empty list."),
        deque1.header, "getData");
    t.checkExpect(deque2.header.getNext().getData(), "abc");
  }

  /*
   * Tests for assignment 6 specified methods
   */

  // test size method in class Deque
  void testSize(Tester t) {
    this.initData();

    t.checkExpect(this.deque1.size(), 0);
    t.checkExpect(this.deque2.size(), 4);
    t.checkExpect(this.deque3.size(), 5);
  }

  // test addAtHead method in class Deque
  void testAddAtHead(Tester t) {
    this.initData();
    t.checkExpect(this.deque2.header.getNext().getData(), "abc");
    t.checkExpect(this.deque2.header.getNext().getNext().getData(), "bcd");
    t.checkExpect(this.deque2.header.getNext().getNext().getNext().getData(), "cde");
    t.checkExpect(this.deque2.header.getNext().getNext().getNext().getNext().getData(), "def");

    this.deque2.addAtHead("aaa");

    t.checkExpect(this.deque2.header.getNext().getData(), "aaa");
    t.checkExpect(this.deque2.header.getNext().getNext().getData(), "abc");

    t.checkExpect(this.deque1.header.getNext(), this.deque1.header);
    t.checkExpect(this.deque1.header.getPrev(), this.deque1.header);

    this.deque1.addAtHead(true);

    t.checkExpect(this.deque1.header.getNext().getData(), true);
    t.checkExpect(this.deque1.header.getPrev().getData(), true);
    t.checkExpect(this.deque1.header.getNext().getNext(), this.deque1.header);
    t.checkExpect(this.deque1.header.getPrev().getPrev(), this.deque1.header);
  }

  // test addAtTail method in class Deque
  void testAddAtTail(Tester t) {
    this.initData();

    // Node<T> n4 = new Node<>("def", s2, n3);

    t.checkExpect(this.deque2.header.getPrev().getData(), "def");

    this.deque2.addAtTail("zzz");

    t.checkExpect(this.deque2.header.getPrev().getData(), "zzz");
    t.checkExpect(this.deque2.header.getPrev().getPrev().getData(), "def");

    t.checkExpect(this.deque1.header.getNext(), this.deque1.header);
    t.checkExpect(this.deque1.header.getPrev(), this.deque1.header);

    this.deque1.addAtTail(false);

    t.checkExpect(this.deque1.header.getPrev().getData(), false);
    t.checkExpect(this.deque1.header.getNext().getData(), false);
    t.checkExpect(this.deque1.header.getPrev().getPrev(), this.deque1.header);
    t.checkExpect(this.deque1.header.getNext().getNext(), this.deque1.header);
  }

  // test removeFromHead method in class Deque
  void testRemoveFromHead(Tester t) {
    this.initData();

    t.checkException(new RuntimeException("Attempting to remove from an empty list."), this.deque1,
        "removeFromHead");

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");

    this.deque2.addAtHead("aaa");
    String removed1 = this.deque2.removeFromHead();
    t.checkExpect(removed1, "aaa");

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");

    String removed2 = this.deque2.removeFromHead();
    t.checkExpect(removed2, "abc");

    t.checkExpect(this.deque2.header.getNext().getData(), "bcd");

  }

  // test removeFromTail method in class Deque
  void testRemoveFromTail(Tester t) {
    this.initData();

    t.checkException(new RuntimeException("Attempting to remove from an empty list."), this.deque1,
        "removeFromTail");

    t.checkExpect(this.deque2.header.getPrev().getData(), "def");

    this.deque2.addAtTail("aaa");
    String removed1 = this.deque2.removeFromTail();
    t.checkExpect(removed1, "aaa");

    t.checkExpect(this.deque2.header.getPrev().getData(), "def");

    String removed2 = this.deque2.removeFromTail();
    t.checkExpect(removed2, "def");

    t.checkExpect(this.deque2.header.getPrev().getData(), "cde");
  }

  // test find method in class Deque
  void testFind(Tester t) {
    this.initData();

    t.checkExpect(this.deque2.find(new ThreeLettersLong()).getData(), "abc");
    t.checkExpect(this.deque3.find(new Even()).getData(), 6);
    t.checkExpect(this.deque3.find(new Odd()).getData(), 5);

    Sentinel<String> s = new Sentinel<>();
    Deque<String> testDeque = new Deque<>(s);

    t.checkExpect(testDeque.find(new FourLettersLong()), s);
  }

  // test removeNode method in class Deque
  void testRemoveNode(Tester t) {
    this.initData();

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");
    t.checkExpect(this.deque2.header.getPrev().getData(), "def");
    // literally does nothing
    this.deque2.removeNode(this.deque2.header);

    t.checkExpect(this.deque2.header.getNext().getData(), "abc");
    t.checkExpect(this.deque2.header.getPrev().getData(), "def");

    t.checkExpect(this.deque3.header.getNext().getData(), 6);
    t.checkExpect(this.deque3.header.getNext().getNext().getData(), 2);

    this.deque3.removeNode(this.deque3.header.getNext());

    t.checkExpect(this.deque3.header.getNext().getData(), 2);
    t.checkExpect(this.deque3.header.getNext().getPrev(), this.deque3.header);
  }
}