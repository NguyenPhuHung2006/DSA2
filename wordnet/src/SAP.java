import edu.princeton.cs.algs4.Digraph;

import java.util.LinkedList;
import java.util.Queue;

public class SAP {

    private final Digraph graph;
    private final int numVertices;
    private final int inf;

    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException();
        }
        graph = new Digraph(G);
        numVertices = graph.V();
        inf = Integer.MAX_VALUE;
    }

    private int[] bfs(int s) {
        int[] dist = new int[numVertices];
        for (int i = 0; i < numVertices; i++) {
            dist[i] = inf;
        }
        Queue<Integer> q = new LinkedList<>();
        dist[s] = 0;
        q.add(s);
        return getDists(dist, q);
    }

    private int[] bfs(Iterable<Integer> vertices) {
        int[] dist = new int[numVertices];
        for (int i = 0; i < numVertices; i++) {
            dist[i] = inf;
        }
        Queue<Integer> q = new LinkedList<>();
        for (Integer v : vertices) {
            dist[v] = 0;
            q.add(v);
        }
        return getDists(dist, q);
    }

    private int[] getDists(int[] dist, Queue<Integer> q) {
        while (!q.isEmpty()) {
            int v = q.poll();
            for (int w : graph.adj(v)) {
                if (dist[w] == inf) {
                    dist[w] = dist[v] + 1;
                    q.add(w);
                }
            }
        }
        return dist;
    }

    private void checkException(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException();

        for (Integer x : v) {
            if (x == null || x < 0 || x >= numVertices) {
                throw new IllegalArgumentException();
            }
        }
        for (Integer y : w) {
            if (y == null || y < 0 || y >= numVertices) {
                throw new IllegalArgumentException();
            }
        }
    }

    private int getMinLength(int[] dist1, int[] dist2) {
        int min = inf;

        for (int i = 0; i < numVertices; i++) {
            if (dist1[i] < inf && dist2[i] < inf) {
                int d = dist1[i] + dist2[i];
                if (d < min)
                    min = d;
            }
        }

        if (min == Integer.MAX_VALUE)
            return -1;

        return min;
    }

    private int getAncestor(int[] dist1, int[] dist2) {
        int anc = -1;
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < numVertices; i++) {
            if (dist1[i] < inf && dist2[i] < inf) {
                int d = dist1[i] + dist2[i];
                if (d < min) {
                    min = d;
                    anc = i;
                }
            }
        }

        if (min == Integer.MAX_VALUE)
            return -1;

        return anc;
    }

    public int length(int v, int w) {
        if (v < 0 || v >= numVertices || w < 0 || w >= numVertices) {
            throw new IllegalArgumentException();
        }
        return getMinLength(bfs(v), bfs(w));
    }

    public int ancestor(int v, int w) {
        if (v < 0 || v >= numVertices || w < 0 || w >= numVertices) {
            throw new IllegalArgumentException();
        }
        return getAncestor(bfs(v), bfs(w));
    }

    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        checkException(v, w);
        return getMinLength(bfs(v), bfs(w));
    }

    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        checkException(v, w);
        return getAncestor(bfs(v), bfs(w));
    }
}