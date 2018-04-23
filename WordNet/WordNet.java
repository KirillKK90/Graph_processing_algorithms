import java.util.HashMap;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Topological;

public class WordNet
{
    private final Digraph dg;
    private final SAP dgSap;
    private final HashMap<String, Integer> nounKeyHashMap;
    private final HashMap<Integer, String> vertexNumKeyHashMapWithSynsets;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms)
    {
        if (synsets == null || hypernyms == null)
        {
            throw new java.lang.IllegalArgumentException();
        }

        nounKeyHashMap = new HashMap<String, Integer>();
        vertexNumKeyHashMapWithSynsets = new HashMap<Integer, String>();

        In in = new In(synsets);
        String[] synsetArr = in.readAllLines();
        for (String synsetEntry: synsetArr)
        {
            String[] entryVals = synsetEntry.split(",");
            int v = Integer.parseInt(entryVals[0]);

            vertexNumKeyHashMapWithSynsets.put(v, entryVals[1]);
            String[] nouns = entryVals[1].split(" ");
            for (String noun : nouns)
            {
                nounKeyHashMap.put(noun, v);
            }
            // entryVals[2] - description
        }

        dg = new Digraph(synsetArr.length);

        in = new In(hypernyms);
        String[] hynymArr = in.readAllStrings();
        for (String edgeEntry: hynymArr)
        {
            String[] numbersStrings = edgeEntry.split(",");
            int v = Integer.parseInt(numbersStrings[0]);
            for (int i = 1; i < numbersStrings.length; ++i)
            {
                int w = Integer.parseInt(numbersStrings[i]);
                dg.addEdge(v, w);
            }
        }

        int rootCount = 0;
        for (int i = 0; i < dg.V(); ++i)
        {
            if (dg.outdegree(i) == 0)
                ++rootCount;
        }

        Topological topologyTestClass = new Topological(dg);
        if (!topologyTestClass.hasOrder() || rootCount != 1)
        {
            // NOT a DAG - directed acyclic graph; or not a rooted graph
            throw new java.lang.IllegalArgumentException();
        }

        dgSap = new SAP(dg);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns()
    {
        return nounKeyHashMap.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word)
    {
        if (word == null)
        {
            throw new java.lang.IllegalArgumentException();
        }

        return nounKeyHashMap.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB)
    {
        if (nounA == null || nounB == null)
        {
            throw new java.lang.IllegalArgumentException();
        }

        if (!isNoun(nounA) || !isNoun(nounB))
        {
            throw new java.lang.IllegalArgumentException();
        }

        int v = nounKeyHashMap.get(nounA);
        int w = nounKeyHashMap.get(nounB);

        return dgSap.length(v, w);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB)
    {
        if (nounA == null || nounB == null)
        {
            throw new java.lang.IllegalArgumentException();
        }

        if (!isNoun(nounA) || !isNoun(nounB))
        {
            throw new java.lang.IllegalArgumentException();
        }

        int v = nounKeyHashMap.get(nounA);
        int w = nounKeyHashMap.get(nounB);
        int ancestor = dgSap.ancestor(v, w);
        String ancestorName = "sap";

        ancestorName = vertexNumKeyHashMapWithSynsets.get(ancestor);

        return ancestorName;
    }


    public static void main(String[] args)
    {
        WordNet mwn = new WordNet("synsets.txt","hypernyms.txt");
        String nounA = "bangle";
        String nounB = "fetlock";
        System.out.println("Distance: ");
        System.out.println(mwn.distance(nounA, nounB));
        System.out.println("SAP: ");
        System.out.println(mwn.sap(nounA, nounB));
    }
}