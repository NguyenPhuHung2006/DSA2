import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;

import java.util.*;

public class WordNet {

    private Map<String, List<Integer>> nounToIds;
    private ArrayList<String> idToSynset;
    private Digraph graph;
    private SAP sap;

    private void checkCycle(Digraph g, int[] deg) {
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < g.V(); i++) {
            if (deg[i] == 0) {
                q.add(i);
            }
        }
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : g.adj(u)) {
                deg[v]--;
                if (deg[v] == 0) {
                    q.add(v);
                }
            }
        }
        for (int i = 0; i < g.V(); i++) {
            if (deg[i] > 0) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void checkRooted(Digraph g) {
        int roots = 0;

        for (int v = 0; v < g.V(); v++) {
            if (!g.adj(v).iterator().hasNext()) {
                roots++;
            }
        }

        if (roots != 1) {
            throw new IllegalArgumentException();
        }
    }

    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException();
        In in = new In(synsets);
        nounToIds = new HashMap<>();
        idToSynset = new ArrayList<>();

        while (in.hasNextLine()) {
            String line = in.readLine();
            String[] fields = line.split(",");

            int id = Integer.parseInt(fields[0]);
            String synset = fields[1];

            while (idToSynset.size() <= id)
                idToSynset.add(null);

            idToSynset.set(id, synset);

            for (String noun : synset.split(" ")) {
                nounToIds.putIfAbsent(noun, new ArrayList<>());
                nounToIds.get(noun).add(id);
            }
        }

        graph = new Digraph(idToSynset.size());
        int[] deg = new int[idToSynset.size()];
        in = new In(hypernyms);
        while (in.hasNextLine()) {
            String[] fields = in.readLine().split(",");

            int v = Integer.parseInt(fields[0]);

            for (int i = 1; i < fields.length; i++) {
                int w = Integer.parseInt(fields[i]);
                graph.addEdge(v, w);
                deg[w]++;
            }
        }
        checkCycle(graph, deg);
        checkRooted(graph);

        sap = new SAP(graph);

    }

    public Iterable<String> nouns() {
        return nounToIds.keySet();
    }

    public boolean isNoun(String word) {
        if (word == null)
            throw new IllegalArgumentException();

        return nounToIds.containsKey(word);
    }

    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();

        return sap.length(nounToIds.get(nounA), nounToIds.get(nounB));
    }

    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();

        int id = sap.ancestor(nounToIds.get(nounA), nounToIds.get(nounB));

        if (id == -1)
            return null;

        return idToSynset.get(id);
    }
}