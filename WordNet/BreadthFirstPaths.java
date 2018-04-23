import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;


public class BreadthFirstPaths
{
    private boolean[] marked;
    private int[] edgeTo;
    private int[] distTo;
    private static boolean m_aux_dbg = false;

    public BreadthFirstPaths(Digraph G, int s)
    {
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        distTo = new int[G.V()];

        // I want to make explicit initializations to values that I need
        for (int i = 0; i < marked.length; ++i)
        {
            marked[i] = false;
        }
        for (int i = 0; i < edgeTo.length; ++i)
        {
            edgeTo[i] = -1;
        }
        for (int i = 0; i < distTo.length; ++i)
        {
            distTo[i] = -1;
        }

        BFS(G, s);
        if (m_aux_dbg)
        {
            int i = 0;
            for (boolean b : marked)
                System.out.println(i++ + ": " + b);

            i = 0;
            for (int e : edgeTo)
                System.out.println(i++ + ": " + e);

            System.out.println("Distances: ");
            i = 0;
            for (int d : distTo)
                System.out.println(i++ + ": " + d);
        }
    }

    private class WeigtedVertex
    {
        public int number;
        public int distance;
        WeigtedVertex(int i_num, int i_dist)
        {
            number = i_num;
            distance = i_dist;
        }
    }

    private void BFS(Digraph G, int s)
    {
        Queue<WeigtedVertex> q = new Queue<>();
        WeigtedVertex source = new WeigtedVertex(s, 0);
        q.enqueue(source);
        marked[source.number] = true;
        edgeTo[source.number] = source.number;
        distTo[source.number] = 0;
        while (!q.isEmpty())
        {
            WeigtedVertex v = q.dequeue();
            for (int w : G.adj(v.number))
            {
                if (!marked[w])
                {
                    WeigtedVertex currWVW = new WeigtedVertex(w, v.distance + 1);
                    q.enqueue(currWVW);
                    marked[currWVW.number] = true;
                    edgeTo[currWVW.number] = v.number;
                    distTo[currWVW.number] = currWVW.distance;
                }
            }
        }
    }

    public int[] getEdgesList()
    {
        int[] edgeToCopy = edgeTo.clone();
        return edgeToCopy;
    }

    public boolean[] getMarkedBoolList()
    {
        boolean[] markedToCopy = marked.clone();
        return markedToCopy;
    }

    public int[] getDistancesList()
    {
        int[] distToCopy = distTo.clone();
        return distToCopy;
    }

    public static void main(String[] args)
    {
        int s = 11;
        In in = new In("digraph1.txt");
        Digraph g = new Digraph(in);
        BreadthFirstPaths bfp = new BreadthFirstPaths(g, s);
    }
}