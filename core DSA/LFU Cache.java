import java.util.HashMap;
import java.util.Map;

class LFUCache {

    class Node {
        int key;
        int value;
        int freq = 1;

        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    class DoublyLinkedList {

        Node head = new Node(0, 0);
        Node tail = new Node(0, 0);

        int size = 0;

        DoublyLinkedList() {
            head.next = tail;
            tail.prev = head;
        }

        void add(Node node) {

            node.next = head.next;
            node.prev = head;

            head.next.prev = node;
            head.next = node;

            size++;
        }

        void remove(Node node) {

            node.prev.next = node.next;
            node.next.prev = node.prev;

            size--;
        }

        Node removeLast() {

            if (size == 0)
                return null;

            Node last = tail.prev;
            remove(last);

            return last;
        }
    }

    private final int capacity;
    private int minFreq = 0;

    private final Map<Integer, Node> keyMap;
    private final Map<Integer, DoublyLinkedList> freqMap;

    public LFUCache(int capacity) {

        this.capacity = capacity;

        keyMap = new HashMap<>();
        freqMap = new HashMap<>();
    }

    public int get(int key) {

        if (!keyMap.containsKey(key))
            return -1;

        Node node = keyMap.get(key);

        update(node);

        return node.value;
    }

    public void put(int key, int value) {

        if (capacity == 0)
            return;

        if (keyMap.containsKey(key)) {

            Node node = keyMap.get(key);

            node.value = value;

            update(node);

            return;
        }

        if (keyMap.size() == capacity) {

            DoublyLinkedList list = freqMap.get(minFreq);

            Node remove = list.removeLast();

            keyMap.remove(remove.key);
        }

        Node node = new Node(key, value);

        minFreq = 1;

        DoublyLinkedList list =
                freqMap.getOrDefault(1, new DoublyLinkedList());

        list.add(node);

        freqMap.put(1, list);

        keyMap.put(key, node);
    }

    private void update(Node node) {

        int freq = node.freq;

        DoublyLinkedList list = freqMap.get(freq);

        list.remove(node);

        if (freq == minFreq && list.size == 0)
            minFreq++;

        node.freq++;

        DoublyLinkedList newList =
                freqMap.getOrDefault(node.freq,
                        new DoublyLinkedList());

        newList.add(node);

        freqMap.put(node.freq, newList);
    }
}
