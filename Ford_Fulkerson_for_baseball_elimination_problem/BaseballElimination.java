import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

//import edu.princeton.cs.algs4.Bag;
//import edu.princeton.cs.algs4.FlowEdge;
//import edu.princeton.cs.algs4.FlowNetwork;
//import edu.princeton.cs.algs4.FordFulkerson;
//import edu.princeton.cs.algs4.In;

public class BaseballElimination {
    private final HashMap<String, PlayTeamData> teamsData;
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final boolean dbgPrintOn = false;

    private class PlayTeamData {
        String name;
        int teamNumber;

        int won;
        int lost;
        int totalGamesRemainNum;
        List<Integer> gamesWithTheTeamLeftNum; // no reduntant data; unique team-team values

        boolean eliminated;
        Bag<String> eliminatedBy;

        PlayTeamData(String i_name, int i_teamNum, int i_wins, int i_losses, int i_totalGames) {
            name = i_name;
            teamNumber = i_teamNum;
            won = i_wins;
            lost = i_losses;
            totalGamesRemainNum = i_totalGames;
            gamesWithTheTeamLeftNum = new ArrayList<>();
            eliminated = false;
            eliminatedBy = null;
        }

        int getGamesLeftEntry(int num) {
            return gamesWithTheTeamLeftNum.get(num);
        }

        void setEliminated() {
            eliminated = true;
            if (eliminatedBy == null)
                eliminatedBy = new Bag<>();
        }

        void setEliminatedBy(String strongerTeam) {
            eliminated = true;
            if (eliminatedBy == null)
                eliminatedBy = new Bag<>();
            eliminatedBy.add(strongerTeam);
        }
    }

    private void readDataFromFile(String filename) {
        In in = new In(filename);
        int totalTeamsNumber = in.readInt();

        for (int teamCounter = 0; teamCounter < totalTeamsNumber; ++teamCounter) {
            String teamName = in.readString();
            int wins = in.readInt();
            int losses = in.readInt();
            int totalGamesRemain = in.readInt();

            PlayTeamData ptd = new PlayTeamData(teamName, teamCounter, wins, losses, totalGamesRemain);
            for (int j = 0; j <= teamCounter; ++j) // + we know that team don't play with itself, so skip this entry
                in.readInt(); // skip redundant, already taken into account data
            for (int j = teamCounter + 1; j < totalTeamsNumber; ++j)
                ptd.gamesWithTheTeamLeftNum.add(in.readInt());

            teamsData.put(teamName, ptd);
        }
    }

    private void checkForTrivialEliminations() {
        // check for Trivial eliminations:
        for (String currTeamName : teamsData.keySet()) {
            PlayTeamData currData = teamsData.get(currTeamName);
            for (String currTeamName2 : teamsData.keySet()) {
                if (currTeamName == currTeamName2) {
                    continue;
                }
                PlayTeamData currData2 = teamsData.get(currTeamName2);
                if (currData.won + currData.totalGamesRemainNum < currData2.won) {
                    currData.setEliminatedBy(currData2.name);
                }
            }
        }
    }

    private int getTeamsVertexNumber(int teamNumber)   // get Team's Vertex corresponding Number
    {
        int n = numberOfTeams();
        return n * (n - 1) / 2 - n + 2 + teamNumber;
        // if n == 4: (3 + teamNumber)
    }

    private int getVertexTeamNumber(int vertexNumber)   // get vertex' team corresponding number
    {
        int n = numberOfTeams();
        return vertexNumber - (n * (n - 1) / 2 - n + 2);
    }

    public BaseballElimination(String filename)                    // create a baseball division from given filename in format specified below
    {
        teamsData = new HashMap<>();
        readDataFromFile(filename);

        checkForTrivialEliminations();

        int n = teamsData.size();
        // Games number in each Flow Network:
        // games vertex - games with team X number + teams vertex + 2 fake vertex
        // n * (n - 1) / 2 - (n - 1) + n + 2  = n * (n - 1) / 2 + 3
        int vertexInFNetworkNum = n * (n - 1) / 2 + 3;

        for (String teamToCheckEliminationForName : teamsData.keySet()) {
            PlayTeamData teamsToCheckData = teamsData.get(teamToCheckEliminationForName);
            if (teamsToCheckData.eliminated)
                continue;

            int totalGames = 0;
            FlowNetwork fn = new FlowNetwork(vertexInFNetworkNum);

            // vertex 1 through (n * (n - 1) / 2 - (n - 1)) - games vertex (0-1, 0-2, etc...)
            int currVertexNumber = 1;
            for (String currTeamName : teamsData.keySet()) {
                if (currTeamName.equals(teamToCheckEliminationForName)) // target team not to be mentioned in FlowNetwork
                {
                    continue;
                }

                PlayTeamData currData = teamsData.get(currTeamName);
                int otherTeamNumber = currData.teamNumber + 1;
                for (int c : currData.gamesWithTheTeamLeftNum) {
                    if (otherTeamNumber == teamsToCheckData.teamNumber) {
                        otherTeamNumber++;
                        continue;
                    }
                    totalGames += c;
                    // edge from fake 1st vertex S to game vertex
                    FlowEdge fe = new FlowEdge(0, currVertexNumber, c);
                    fn.addEdge(fe);

                    // edges from game vertex to teams vertex
                    int playingTeam1VertexNum = getTeamsVertexNumber(currData.teamNumber);
                    fe = new FlowEdge(currVertexNumber, playingTeam1VertexNum, Integer.MAX_VALUE);
                    fn.addEdge(fe);
                    int playingTeam2VertexNum = getTeamsVertexNumber(otherTeamNumber);
                    fe = new FlowEdge(currVertexNumber, playingTeam2VertexNum, Integer.MAX_VALUE);
                    fn.addEdge(fe);

                    if (dbgPrintOn) // && teamToCheckEliminationForName.equals("Philadelphia"))
                    {
                        // game of vertex currVertexNumber is between: currData.teamNumber and otherTeamNumber
                        StringBuilder s = new StringBuilder();
                        s.append("Game of vertex: " + currVertexNumber + " is between teams: " + currData.teamNumber +
                                " and " + otherTeamNumber + "(vertex: " + getTeamsVertexNumber(currData.teamNumber) +
                                ", " + getTeamsVertexNumber(otherTeamNumber) + " )." +
                                " Now team for which we check elimination (and build FlowNetwork) is: " +
                                teamsToCheckData.teamNumber + " (vertex: " + getTeamsVertexNumber(teamsToCheckData.teamNumber)
                                + " ). " + NEWLINE);
                        s.append("Remaining games should be: " + c + NEWLINE);
                        System.out.println(s.toString());
                    }

                    currVertexNumber++;
                    otherTeamNumber++;
                }
            }

            int currVertexNumberToCheck = n * (n - 1) / 2 - (n - 1) + 1;
            assert (currVertexNumber == currVertexNumberToCheck);

            int constTerm = teamsToCheckData.won + teamsToCheckData.totalGamesRemainNum;

            // vertex (n * (n - 1) / 2 - (n - 1) + 1) through (n * (n - 1) / 2 + 1) - team's vertex
            for (int currTeamNum = 0; currTeamNum < numberOfTeams(); ++currTeamNum, ++currVertexNumber)
            {
                if (currTeamNum == teamsToCheckData.teamNumber)
                {
                    // don't create any edges from (and to) vertex corresponding to team being checked for elimination, just skip it !
                    continue;
                }

                int currTeamWinsNum = 0;
                // get team name by number ? how ?
                // Answer: would be better by hashtable. Ugly linear time selection here: ~O(n).
                for (PlayTeamData data: teamsData.values())
                {
                    if (data.teamNumber == currTeamNum)
                    {
                        currTeamWinsNum = data.won;
                        break;
                    }
                }

                FlowEdge fe = new FlowEdge(currVertexNumber, vertexInFNetworkNum - 1, constTerm - currTeamWinsNum);
                fn.addEdge(fe);
            }

            // assert that no edges from and to skipped team vertex
            ensureNoEdgesFromSkippedTeamVertex(fn, teamsToCheckData.teamNumber);

            currVertexNumber++;

            int toCheck = n * (n - 1) / 2 + 3;
            assert (currVertexNumber == toCheck);
            assert (currVertexNumber == vertexInFNetworkNum);

            FordFulkerson ff = new FordFulkerson(fn, 0, vertexInFNetworkNum - 1);

            if (dbgPrintOn && teamToCheckEliminationForName.equals("Philadelphia"))
                System.out.println(fn.toString());

            if (ff.value() < totalGames) {
                teamsToCheckData.setEliminated();

                for (int currTeamNum = 0; currTeamNum < numberOfTeams(); ++currTeamNum) {
                    if (currTeamNum == teamsToCheckData.teamNumber) {
                        continue;
                    }

                    if (ff.inCut(getTeamsVertexNumber(currTeamNum))) {
                        for (PlayTeamData data: teamsData.values())
                        {
                            if (data.teamNumber == currTeamNum)
                            {
                                String team = data.name;
                                teamsToCheckData.eliminatedBy.add(team);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        int n = numberOfTeams();
        StringBuilder s = new StringBuilder();
        s.append("Total number of teams: " + n + NEWLINE);
        s.append("Vertex 0 - vertex S (start)." + NEWLINE);
        int lastGameVertexNum = n * (n - 1) / 2 - (n - 1);
        s.append("Vertex 1 through " + lastGameVertexNum + " are games vertices." + NEWLINE);
        int firstTeamVertexNum = lastGameVertexNum + 1;
        int lastTeamVertexNum = n * (n - 1) / 2 + 1;
        s.append("Vertex " + firstTeamVertexNum + " through " + lastTeamVertexNum + " are teams vertices." + NEWLINE);
        int t = lastTeamVertexNum + 1;
        s.append("Vertex " + t + " - vertex t (target) (last terminal vertex)." + NEWLINE);

        return s.toString();
    }

    public int numberOfTeams()                        // number of teams
    {
        return teamsData.size();
    }

    public Iterable<String> teams()                                // all teams
    {
        return teamsData.keySet();
    }

    public int wins(String team)                      // number of wins for given team
    {
        if (!teamsData.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        PlayTeamData p = teamsData.get(team);
        return p.won;
    }

    public int losses(String team)                    // number of losses for given team
    {
        if (!teamsData.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        PlayTeamData p = teamsData.get(team);
        return p.lost;
    }

    public int remaining(String team)                 // number of remaining games for given team
    {
        if (!teamsData.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        PlayTeamData p = teamsData.get(team);
        return p.totalGamesRemainNum;
    }

    public int against(String team1, String team2)    // number of remaining games between team1 and team2
    {
        if (!teamsData.containsKey(team1) || !teamsData.containsKey(team2)) {
            throw new java.lang.IllegalArgumentException();
        }

        if (team1 == team2) {
            return 0;
        }

        PlayTeamData data1 = teamsData.get(team1);
        PlayTeamData data2 = teamsData.get(team2);

        PlayTeamData minNumTeamData = (data1.teamNumber < data2.teamNumber) ? data1 : data2;
        PlayTeamData otherNumTeamData = (data1.teamNumber < data2.teamNumber) ? data2 : data1;

        return minNumTeamData.getGamesLeftEntry(otherNumTeamData.teamNumber - minNumTeamData.teamNumber - 1);
    }

    public boolean isEliminated(String team)              // is given team eliminated?
    {
        if (!teamsData.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        return teamsData.get(team).eliminated;
    }

    public Iterable<String> certificateOfElimination(String team)  // subset R of teams that eliminates given team; null if not eliminated
    {
        if (!teamsData.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        return teamsData.get(team).eliminatedBy;
    }


    private static void runAllTests()
    {
        for (int i = 0; i < 4; ++i)
            runTest(i, false);
    }


    private static void runTest(int num, boolean printClass)
    {
        BaseballElimination be;
        switch (num)
        {
            case 0:
                be = new BaseballElimination("teams4.txt");
                System.out.println(be.isEliminated("Atlanta") == false);
                System.out.println(be.isEliminated("Philadelphia") == true);
                System.out.println(be.isEliminated("New_York") == false);
                System.out.println(be.isEliminated("Montreal") == true);

                BaseballElimination division = be;
                for (String team : division.teams()) {
                    if (division.isEliminated(team)) {
                        StdOut.print(team + " is eliminated by the subset R = { ");
                        for (String t : division.certificateOfElimination(team)) {
                            StdOut.print(t + " ");
                        }
                        StdOut.println("}");
                    }
                    else {
                        StdOut.println(team + " is not eliminated");
                    }
                }
                break;
            case 1:
                be = new BaseballElimination("teams5.txt");
                System.out.println(be.isEliminated("Detroit") == true);
                System.out.println(be.isEliminated("Toronto") == false);
                System.out.println(be.isEliminated("Boston") == false);
                System.out.println(be.isEliminated("Baltimore") == false);
                System.out.println(be.isEliminated("New_York") == false);
                break;
            case 2:
                be = new BaseballElimination("teams10.txt");
                System.out.println(be.isEliminated("Indiana") == false);
                break;
            case 3:
                be = new BaseballElimination("teams5c.txt");
                System.out.println(be.isEliminated("Philadelphia"));
                break;
            default:
                be = new BaseballElimination("teams1.txt");
        }

        if (printClass)
            System.out.println(be.toString());
    }

    private void ensureNoEdgesFromSkippedTeamVertex(FlowNetwork fn, int skippedTeamNum)
    {
        int skippedTeamVertexNum = getTeamsVertexNumber(skippedTeamNum);
        Iterable<FlowEdge> ife = fn.adj(skippedTeamVertexNum);
        Iterator<FlowEdge> it = ife.iterator();
        int num = 0;
        while (it.hasNext()) {
            it.next();
            num++;
        }

        // System.out.println(fn.toString());
        assert(num == 0);
    }

    public static void main(String[] args) {
        // runTest(0, false);
        runAllTests();
    }
}
