package CrabGraph;
import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class Result {

    /*
     * Complete the 'crabGraphs' function below.
     *
     * The function is expected to return an INTEGER.
     * The function accepts following parameters:
     *  1. INTEGER n
     *  2. INTEGER t
     *  3. 2D_INTEGER_ARRAY graph
     */

    public static void addEdge(List<List<Integer>> g, int[][] capacity, int u, int v, int cap) {
        g.get(u).add(v);
        g.get(v).add(u);
        capacity[u][v] = cap;
        capacity[v][u] = 0;
    }

    public static int bfs(List<List<Integer>> g, int[][] capacity, int[] parent, int[] flow, int s, int t) {
        Arrays.fill(parent, -1);
        Arrays.fill(flow, Integer.MAX_VALUE);
        Queue<Integer> q = new LinkedList<>();
        q.add(s);
        parent[s] = -2;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : g.get(u)) {
                if (parent[v] == -1 && capacity[u][v] > 0) {
                    parent[v] = u;
                    flow[v] = Math.min(flow[u], capacity[u][v]);
                    if (v == t) {
                        return flow[t];
                    }
                    q.add(v);
                }
            }
        }
        return -1;
    }

    public static void backward(List<List<Integer>> g, int[][] capacity, int[] parent, int curFlow, int s, int t) {
        int cur = t;
        while (cur != s) {
            int prev = parent[cur];
            capacity[prev][cur] -= curFlow;
            capacity[cur][prev] += curFlow;
            cur = prev;
        }
    }

    public static void flowing(List<List<Integer>> g, int[][] capacity, int s, int t) {
        int[] parent = new int[g.size()];
        int[] flow = new int[g.size()];
        while (true) {
            int curFlow = bfs(g, capacity, parent, flow, s, t);
            if (parent[t] == -1 || curFlow == -1) {
                return;
            }
            backward(g, capacity, parent, curFlow, s, t);
        }
    }

    public static int crabGraphs(int n, int t, List<List<Integer>> graph) {
        int V = 2 * n + 2;

        List<List<Integer>> g = new ArrayList<>();
        int[][] capacity = new int[V][V];
        
        for (int i = 0; i < V; i++) {
            g.add(new ArrayList<>());
        }

        Integer INF = Integer.MAX_VALUE;
        for (List<Integer> edge : graph) {
            int u = edge.get(0);
            int v = edge.get(1);
            addEdge(g, capacity, u, v + n, INF);
            addEdge(g, capacity, v, u + n, INF);
        }

        for (int i = 1; i < n + 1; i++) {
            addEdge(g, capacity, 0, i, t);
        }

        for (int i = n + 1; i < V; i++) {
            addEdge(g, capacity, i, V - 1, 1);
        }

        flowing(g, capacity, 0, V - 1);

        int ans = 0;
        for (int u : g.get(V - 1)) {
            ans += capacity[V - 1][u];
        }

        return ans;

    }

}

public class Solution {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int c = Integer.parseInt(bufferedReader.readLine().trim());

        IntStream.range(0, c).forEach(cItr -> {
            try {
                String[] firstMultipleInput = bufferedReader.readLine().replaceAll("\\s+$", "").split(" ");

                int n = Integer.parseInt(firstMultipleInput[0]);

                int t = Integer.parseInt(firstMultipleInput[1]);

                int m = Integer.parseInt(firstMultipleInput[2]);

                List<List<Integer>> graph = new ArrayList<>();

                IntStream.range(0, m).forEach(i -> {
                    try {
                        graph.add(
                            Stream.of(bufferedReader.readLine().replaceAll("\\s+$", "").split(" "))
                                .map(Integer::parseInt)
                                .collect(toList())
                        );
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                int result = Result.crabGraphs(n, t, graph);

                bufferedWriter.write(String.valueOf(result));
                bufferedWriter.newLine();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        bufferedReader.close();
        bufferedWriter.close();
    }
}
