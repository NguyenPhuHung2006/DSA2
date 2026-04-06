import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class BaseballElimination2 {
    private final int n;
    private final String[] teams;
    private final Map<String, Integer> teamIndex;

    private final int[] w;
    private final int[] l;
    private final int[] r;
    private final int[][] g;

    private MaxFlow mf;

    private static class FlowData {
        MaxFlow mf;
        int source, sink;
        int[] teamNode;
        int totalGames;

        FlowData(MaxFlow mf, int source, int sink, int[] teamNode, int totalGames) {
            this.mf = mf;
            this.source = source;
            this.sink = sink;
            this.teamNode = teamNode;
            this.totalGames = totalGames;
        }
    }

    private static class MaxFlow {
        int V;
        List<List<Integer>> graph;
        int[][] capacity;

        MaxFlow(int n) {
            V = n;
            graph = new ArrayList<>();
            capacity = new int[n][n];

            for (int i = 0; i < n; i++) {
                graph.add(new ArrayList<>());
            }
        }

        void addEdge(int u, int v, int cap) {
            graph.get(u).add(v);
            graph.get(v).add(u);
            capacity[u][v] += cap;
        }

        int bfs(int s, int t, int[] parent) {
            Arrays.fill(parent, -1);
            parent[s] = -2;

            Queue<Integer> q = new LinkedList<>();
            q.add(s);

            int[] flow = new int[V];
            flow[s] = Integer.MAX_VALUE;

            while (!q.isEmpty()) {
                int u = q.poll();

                for (int v : graph.get(u)) {
                    if (parent[v] == -1 && capacity[u][v] > 0) {
                        parent[v] = u;
                        flow[v] = Math.min(flow[u], capacity[u][v]);

                        if (v == t) return flow[v];

                        q.add(v);
                    }
                }
            }
            return 0;
        }

        int maxFlow(int s, int t) {
            int flow = 0;
            int[] parent = new int[V];

            int newFlow;
            while ((newFlow = bfs(s, t, parent)) != 0) {
                flow += newFlow;

                int cur = t;
                while (cur != s) {
                    int prev = parent[cur];
                    capacity[prev][cur] -= newFlow;
                    capacity[cur][prev] += newFlow;
                    cur = prev;
                }
            }
            return flow;
        }
    }

    public BaseballElimination2(String filename) {
        try {
            Scanner sc = new Scanner(new File(filename));

            n = sc.nextInt();

            teams = new String[n];
            teamIndex = new HashMap<>();

            w = new int[n];
            l = new int[n];
            r = new int[n];
            g = new int[n][n];

            for (int i = 0; i < n; i++) {
                String name = sc.next();
                teams[i] = name;
                teamIndex.put(name, i);

                w[i] = sc.nextInt();
                l[i] = sc.nextInt();
                r[i] = sc.nextInt();

                for (int j = 0; j < n; j++) {
                    g[i][j] = sc.nextInt();
                }
            }

            sc.close();

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException();
        }
    }

    public int numberOfTeams() {
        return n;
    }

    public Iterable<String> teams() {
        return Arrays.asList(teams);
    }

    public int wins(String team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }
        return w[teamIndex.get(team)];
    }
    public int losses(String team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }
        return l[teamIndex.get(team)];
    }
    public int remaining(String team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }
        return r[teamIndex.get(team)];
    }
    public int against(String team1, String team2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException();
        }
        return g[teamIndex.get(team1)][teamIndex.get(team2)];
    }

    private FlowData buildFlow(int x) {
        int gameCount = ((n - 2) * (n - 1)) / 2;
        int teamCount = n - 1;

        int V = gameCount + teamCount + 2;
        int source = 0;
        int sink = V - 1;

        MaxFlow mf = new MaxFlow(V);

        int gameStart = 1;
        int teamStart = 1 + gameCount;

        int[] teamNode = new int[n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (i == x) {
                continue;
            }
            teamNode[i] = teamStart + idx++;
        }

        int INF = Integer.MAX_VALUE;
        int gameIndex = 0;
        int totalGames = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i == x || j == x) {
                    continue;
                }

                int games = g[i][j];
                int gameNode = gameStart + gameIndex++;

                if (games > 0) {
                    mf.addEdge(source, gameNode, games);
                    totalGames += games;

                    mf.addEdge(gameNode, teamNode[i], INF);
                    mf.addEdge(gameNode, teamNode[j], INF);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (i == x) {
                continue;
            }

            int cap = Math.max(0, w[x] + r[x] - w[i]);
            mf.addEdge(teamNode[i], sink, cap);
        }
        return new FlowData(mf, source, sink, teamNode, totalGames);
    }

    public boolean isEliminated(String team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }
        int x = teamIndex.get(team);

        for (int i = 0; i < n; i++) {
            if (i != x && w[x] + r[x] < w[i]) {
                return true;
            }
        }

        FlowData fd = buildFlow(x);
        int flow = fd.mf.maxFlow(fd.source, fd.sink);

        return flow != fd.totalGames;
    }

    public Iterable<String> certificateOfElimination(String team) {
        if (team == null) {
            throw new IllegalArgumentException();
        }
        int x = teamIndex.get(team);
        List<String> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (i != x && w[x] + r[x] < w[i]) {
                result.add(teams[i]);
                return result;
            }
        }

        FlowData fd = buildFlow(x);
        int flow = fd.mf.maxFlow(fd.source, fd.sink);

        if (flow == fd.totalGames) {
            return null;
        }

        boolean[] visited = new boolean[fd.mf.V];
        Queue<Integer> q = new LinkedList<>();

        q.add(fd.source);
        visited[fd.source] = true;

        while (!q.isEmpty()) {
            int u = q.poll();

            for (int v : fd.mf.graph.get(u)) {
                if (!visited[v] && fd.mf.capacity[u][v] > 0) {
                    visited[v] = true;
                    q.add(v);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (i == x) {
                continue;
            }

            if (visited[fd.teamNode[i]]) {
                result.add(teams[i]);
            }
        }

        return result;
    }

}
