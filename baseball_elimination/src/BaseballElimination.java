import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;

public class BaseballElimination {
    private final int n;
    private final String[] teams;
    private final Map<String, Integer> teamIndex;

    private final int[] w;
    private final int[] lossesArr;
    private final int[] r;
    private final int[][] g;

    public BaseballElimination(String filename) {
        try {
            Scanner sc = new Scanner(new File(filename), "UTF-8");

            n = sc.nextInt();

            teams = new String[n];
            teamIndex = new HashMap<>();

            w = new int[n];
            lossesArr = new int[n];
            r = new int[n];
            g = new int[n][n];

            for (int i = 0; i < n; i++) {
                String name = sc.next();
                teams[i] = name;
                teamIndex.put(name, i);

                w[i] = sc.nextInt();
                lossesArr[i] = sc.nextInt();
                r[i] = sc.nextInt();

                for (int j = 0; j < n; j++) {
                    g[i][j] = sc.nextInt();
                }
            }

            sc.close();

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public int numberOfTeams() {
        return n;
    }

    public Iterable<String> teams() {
        return Arrays.asList(teams);
    }

    private void validateTeam(String team) {
        if (team == null || !teamIndex.containsKey(team)) {
            throw new IllegalArgumentException();
        }
    }

    public int wins(String team) {
        validateTeam(team);
        return w[teamIndex.get(team)];
    }

    public int losses(String team) {
        validateTeam(team);
        return lossesArr[teamIndex.get(team)];
    }

    public int remaining(String team) {
        validateTeam(team);
        return r[teamIndex.get(team)];
    }

    public int against(String team1, String team2) {
        validateTeam(team1);
        validateTeam(team2);
        return g[teamIndex.get(team1)][teamIndex.get(team2)];
    }

    private class FlowData {
        FlowNetwork network;
        FordFulkerson ff;
        int[] teamNode;
        int totalGames;

        FlowData(FlowNetwork network, FordFulkerson ff, int[] teamNode, int totalGames) {
            this.network = network;
            this.ff = ff;
            this.teamNode = teamNode;
            this.totalGames = totalGames;
        }
    }

    private FlowData buildFlow(int x) {
        int gameCount = ((n - 2) * (n - 1)) / 2;
        int teamCount = n - 1;

        int V = gameCount + teamCount + 2;
        int source = 0;
        int sink = V - 1;

        FlowNetwork network = new FlowNetwork(V);

        int gameStart = 1;
        int teamStart = 1 + gameCount;

        int[] teamNode = new int[n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (i == x) continue;
            teamNode[i] = teamStart + idx++;
        }

        int gameIndex = 0;
        int totalGames = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i == x || j == x) continue;

                int games = g[i][j];
                int gameNode = gameStart + gameIndex++;

                if (games > 0) {
                    network.addEdge(new FlowEdge(source, gameNode, games));
                    totalGames += games;

                    network.addEdge(new FlowEdge(gameNode, teamNode[i], Double.POSITIVE_INFINITY));
                    network.addEdge(new FlowEdge(gameNode, teamNode[j], Double.POSITIVE_INFINITY));
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (i == x) continue;

            int cap = Math.max(0, w[x] + r[x] - w[i]);
            network.addEdge(new FlowEdge(teamNode[i], sink, cap));
        }

        FordFulkerson ff = new FordFulkerson(network, source, sink);

        return new FlowData(network, ff, teamNode, totalGames);
    }

    public boolean isEliminated(String team) {
        validateTeam(team);
        int x = teamIndex.get(team);

        // trivial
        for (int i = 0; i < n; i++) {
            if (i != x && w[x] + r[x] < w[i]) {
                return true;
            }
        }

        FlowData fd = buildFlow(x);
        return fd.ff.value() != fd.totalGames;
    }

    public Iterable<String> certificateOfElimination(String team) {
        validateTeam(team);
        int x = teamIndex.get(team);

        List<String> result = new ArrayList<>();

        // trivial
        for (int i = 0; i < n; i++) {
            if (i != x && w[x] + r[x] < w[i]) {
                result.add(teams[i]);
                return result;
            }
        }

        FlowData fd = buildFlow(x);

        if (fd.ff.value() == fd.totalGames) {
            return null;
        }

        // min cut
        for (int i = 0; i < n; i++) {
            if (i == x) continue;

            if (fd.ff.inCut(fd.teamNode[i])) {
                result.add(teams[i]);
            }
        }

        return result;
    }
}