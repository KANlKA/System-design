package algorithms;
import models.Node;
import models.Request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

//LOAD BALANCING ALGO
public class ConsistentHashing implements Router {
    private final Map<Node, List<Long>> nodePositions;//list of all virtual node positions so we know when a node is added or removed, the exact pos it was associated with
    private final ConcurrentSkipListMap<Long, Node> nodeMappings;//ConcurrentSkipListMap automatically keeps keys sorted because consistent hashing always searches for the next greater hash value
    private final Function<String, Long> hashFunction;//Instead of hardcoding MD5/SHA-1, the constructor accepts any hash function
    private final int pointMultiplier;//One physical node can appear many times on the ring. These are called virtual nodes
//MORE VIRTUAL NODES=BETTER LOAD BALANCING

    public ConsistentHashing(final Function<String, Long> hashFunction,
                             final int pointMultiplier) {
        if (pointMultiplier == 0) { //IF NO VIRTUAL NODES->Initializes the below
            throw new IllegalArgumentException();
        }
        this.pointMultiplier = pointMultiplier;
        this.hashFunction = hashFunction;
        this.nodePositions = new ConcurrentHashMap<>();
        this.nodeMappings = new ConcurrentSkipListMap<>();
    }

    public void addNode(Node node) {
        nodePositions.put(node, new CopyOnWriteArrayList<>());//first creates and empty list
        for (int i = 0; i < pointMultiplier; i++) {//if multiplier is 3 then each iteration creates a virtual node
            for (int j = 0; j < node.getWeight(); j++) {//weight controls how many pos each virtual node gets. High wt->more entry->more traffic
                final var point = hashFunction.apply((i * pointMultiplier + j) + node.getId());
                nodePositions.get(node).add(point);//saving pos
                nodeMappings.put(point, node);//store in ring
            }
        }
    }

    public void removeNode(Node node) {
        for (final Long point : nodePositions.remove(node)) {
            nodeMappings.remove(point);
        }
    }
//Now Node A disappears from the ring. Only requests previously mapped near those positions will move to another node. This is the key advantage of consistent hashing: removing a node only remaps a small portion of the keys instead of all of them.
    public Node getAssignedNode(Request request) {
        final var key = hashFunction.apply(request.getId());
        final var entry = nodeMappings.higherEntry(key);//next server find 
        if (entry == null) {//in this case suppose the last server is 9000 and your hash is 9800 so theres no larger val from that, so there will be a wrap around where this hash will go to the 1st server as its a ring
            return nodeMappings.firstEntry().getValue();
        } else {
            return entry.getValue();
        }
    }
}
