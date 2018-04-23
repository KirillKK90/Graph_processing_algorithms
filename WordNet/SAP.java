import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;

import java.util.ArrayList;
import java.util.List;


public class SAP
{
    private final Digraph dg;
    // private int[] sap_vertex_list;

    private class SapProperties
    {
        int length;
        int ancestor;
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G)
    {
        if (G == null)
        {
            throw new java.lang.IllegalArgumentException();
        }
        dg = new Digraph(G);
    }

    private void getSapProperties(int v, int w, SapProperties sp)
    {
        BreadthFirstDirectedPaths bfpV = new BreadthFirstDirectedPaths(dg, v);
        BreadthFirstDirectedPaths bfpW = new BreadthFirstDirectedPaths(dg, w);

        int[] totalDists = new int[dg.V()];

        boolean[] reachableFromBothVandW = new boolean[dg.V()];
        for (int i = 0; i < dg.V(); ++i)
        {
            reachableFromBothVandW[i] = bfpV.hasPathTo(i) && bfpW.hasPathTo(i);
        }

        for (int i = 0; i < totalDists.length; ++i)
        {
            if (reachableFromBothVandW[i])
            {
                totalDists[i] = bfpV.distTo(i) + bfpW.distTo(i);
            }
            else
            {
                totalDists[i] = Integer.MAX_VALUE;
            }
        }

        int minVal = Integer.MAX_VALUE; // length
        int minIdx = Integer.MAX_VALUE; // ancestor
        for (int i = 0; i < totalDists.length; ++i)
        {
            if (totalDists[i] < minVal)
            {
                minVal = totalDists[i];
                minIdx = i;
            }
        }

        sp.length = (minVal == Integer.MAX_VALUE) ? -1 : minVal;
        sp.ancestor = (minIdx == Integer.MAX_VALUE) ? -1 : minIdx;
    }

//    private boolean getSapVertexList(int v, int w, List<Integer> o_sapVertexList)
//    {
//        BreadthFirstPaths bfp = new BreadthFirstPaths(dg, v);
//        int[] edgesTo = bfp.getEdgesList();
//        boolean[] fromVpossibleToGetTo = bfp.getMarkedBoolList();
//        if (fromVpossibleToGetTo[w])
//        {
//            int x = w;
//            o_sapVertexList.add(x);
//            while (x != v)
//            {
//                x = edgesTo[x];
//                o_sapVertexList.add(x);
//            }
//
//            return true;
//        }
//        else
//            return false;
//    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w)
    {
        if (v > dg.V() - 1 || v < 0 || w > dg.V() - 1 || w < 0)
        {
            throw new java.lang.IllegalArgumentException();
        }

        SapProperties sp = new SapProperties();
        getSapProperties(v, w, sp);
        return sp.length;
    }

    private void checkInputVertexInBounds(int v)
    {
        if (v > dg.V() - 1 || v < 0)
        {
            throw new java.lang.IllegalArgumentException();
        }
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w)
    {
        checkInputVertexInBounds(v);
        checkInputVertexInBounds(w);

        SapProperties sp = new SapProperties();
        getSapProperties(v, w, sp);
        return sp.ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w)
    {
        if (v == null || w == null)
        {
            throw new java.lang.IllegalArgumentException();
        }
        for (int i: v)
        {
            checkInputVertexInBounds(i);
        }
        for (int i: w)
        {
            checkInputVertexInBounds(i);
        }

        int minLength = Integer.MAX_VALUE;
        for (int i: v)
            for (int j: w)
            {
                int tmpCurrLen = length(i, j);
                if (tmpCurrLen < minLength)
                    minLength = tmpCurrLen;
            }

        if (minLength == Integer.MAX_VALUE)
        {
            minLength = -1;
        }
        return minLength;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w)
    {
        if (v == null || w == null)
        {
            throw new java.lang.IllegalArgumentException();
        }
        for (int i: v)
        {
            checkInputVertexInBounds(i);
        }
        for (int i: w)
        {
            checkInputVertexInBounds(i);
        }

        int minV = -1;
        int minW = -1;
        int minLength = Integer.MAX_VALUE;
        for (int i: v)
            for (int j: w)
            {
                int tmpCurrLen = length(i, j);
                if (tmpCurrLen < minLength)
                {
                    minLength = tmpCurrLen;
                    minV = i;
                    minW = j;
                }
            }
        if (minV == -1 || minW == -1)
        {
            return -1;
        }

        return ancestor(minV, minW);
    }

    // do unit testing of this class
    public static void main(String[] args)
    {
        In in = new In("digraph1.txt");
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();

            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }

//        In in = new In("digraph-wordnet.txt");
//        Digraph g = new Digraph(in);
//        SAP msap = new SAP(g);
//
//        List<Integer> v = new ArrayList<>();
//        v.add(35083);
//        v.add(22);
//        v.add(220);
//        List<Integer> w = new ArrayList<>();
//        w.add(48629);
//        w.add(33);
//        v.add(1111);
//
//        System.out.println("Length: ");
//        System.out.println(msap.length(v, w));
//        System.out.println("Ancestor: ");
//        System.out.println(msap.ancestor(v, w));
    }
}