/*
UPD 11.01.2018:
        Я попытался реализовать простейшую версию кэширования;
        вместо вызова по 3 раза getRed, getBlue... вызываю getRGB по одному разу (но дело было не в этом).
        Всё это не помогло. Падают пара Performance тестов: слишком много раз программой вызываются методы из Picture, Color.
        Техника кеширования должна быть ещё более продвинутой: после удаления шва, копировать большую часть
        энергий из старого кэша. А пересчитывать только смежные с обновленными пикселями после удаления шва.
        БОльшую часть энергий сможем переиспользовать, только важно не напутать с индексами.
*/

// I got 96/100 for this solution. In order to get 100, I think one need to decrease getColor (getGreen, getBlue ...)
// calls (the methods are in Picture class). They are called from energy(...) my method. Decrease energy(..) calls
// to achieve this. They are called from other methods like get seams. It is easy to precalculate energies for all
// vertex, save it in some new array for afterwards usage

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.AcyclicSP;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// import edu.princeton.cs.algs4.Picture;
// import edu.princeton.cs.algs4.StdOut;
// import edu.princeton.cs.algs4.DirectedEdge;
// import edu.princeton.cs.algs4.AcyclicSP;
// import edu.princeton.cs.algs4.EdgeWeightedDigraph;
// import java.awt.Color;
// import java.util.ArrayList;
// import java.util.List;

public class SeamCarver
{
    private Picture miPic;
    private double[] energy_cache;
    private boolean energyCached;
    private boolean cachingTurnOn;

    public SeamCarver(Picture picture)                // create a seam carver object based on the given picture
    {
        if (picture == null)
        {
            throw new java.lang.IllegalArgumentException();
        }

        miPic = new Picture(picture);
        if (cachingTurnOn)
        {
            energyCached = false;
            cacheEnergy();
            energyCached = true;
        }
    }

    public Picture picture()                          // current picture
    {
        Picture oPic = new Picture(miPic);
        return oPic;
    }

    public     int width()                            // width of current picture
    { return miPic.width(); }

    public     int height()                           // height of current picture
    { return miPic.height(); }

    private void cacheEnergy()
    {
        energyCached = false;
        energy_cache = new double[miPic.width() * miPic.height()];
        for (int x = 0; x < miPic.width(); ++x)
            for (int y = 0; y < miPic.height(); ++y)
            {
                energy_cache[y * width() + x] = energy(x, y);
            }
    }

    public  double energy(int x, int y)               // energy of pixel at column x and row y
    {
        if (x < 0 || x > miPic.width() - 1 || y < 0 || y > miPic.height() - 1)
        {
            throw new java.lang.IllegalArgumentException();
        }

        if (cachingTurnOn && energyCached)
        {
            return energy_cache[y * width() + x];
        }

        if (x == 0 || x == miPic.width() - 1 || y == 0 || y == miPic.height() - 1)
        {
            return 1000;
        }

        Color prevColor = miPic.get(x - 1, y);
        Color nextColor = miPic.get(x + 1, y);

        int rgb = prevColor.getRGB();
        int prevBlue = rgb & 255;
        int prevGreen = (rgb & (255 << 8)) >> 8;
        int prevRed = (rgb & (255 << 16)) >> 16;

        rgb = nextColor.getRGB();
        int nextBlue = rgb & 255;
        int nextGreen = (rgb & (255 << 8)) >> 8;
        int nextRed = (rgb & (255 << 16)) >> 16;

        int Rx = nextRed - prevRed;
        int Gx = nextGreen - prevGreen;
        int Bx = nextBlue - prevBlue;
        int deltaX2 = Rx * Rx + Gx * Gx + Bx * Bx;

        prevColor = miPic.get(x, y - 1);
        nextColor = miPic.get(x, y + 1);

        rgb = prevColor.getRGB();
        prevBlue = rgb & 255;
        prevGreen = (rgb & (255 << 8)) >> 8;
        prevRed = (rgb & (255 << 16)) >> 16;

        rgb = nextColor.getRGB();
        nextBlue = rgb & 255;
        nextGreen = (rgb & (255 << 8)) >> 8;
        nextRed = (rgb & (255 << 16)) >> 16;

        int Ry = nextRed - prevRed;
        int Gy = nextGreen - prevGreen;
        int By = nextBlue - prevBlue;
        int deltaY2 = Ry * Ry + Gy * Gy + By * By;

        return Math.sqrt(deltaX2 + deltaY2);
    }

    public   int[] findHorizontalSeam()               // sequence of indices for horizontal seam
    {
        List<Integer> seam = new ArrayList<>();
        int[] xy = new int[2];

        int vertexNumber = miPic.height() * miPic.width();
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(vertexNumber + 2);

        // count from 0 inclusive
        for (int idx = 0; idx < vertexNumber; ++idx) {
            if ((idx + 1) % miPic.width() == 0)
            {
                continue;
            }
            convert1DIndexTo2D(idx, xy);
            int nextLeftPointIdx = idx - miPic.width() + 1;
            int nextBelowPointIdx = idx + 1;
            int nextRightPointIdx = idx + miPic.width() + 1;

            if (xy[1] > 0)
                G.addEdge(new DirectedEdge(idx, nextLeftPointIdx, energy(xy[0] + 1, xy[1] - 1)));

            G.addEdge(new DirectedEdge(idx, nextBelowPointIdx, energy(xy[0] + 1, xy[1])));

            if (xy[1] < miPic.height() - 1)
                G.addEdge(new DirectedEdge(idx, nextRightPointIdx, energy(xy[0] + 1, xy[1] + 1)));
        }

        // create miPic.height() fake edges from fake left extreme vertex to the left row vertex
        for (int idx = 0; idx < vertexNumber; idx += miPic.width()) {
            convert1DIndexTo2D(idx, xy);
            // vertexNumber - fake left extreme vertex
            G.addEdge(new DirectedEdge(vertexNumber, idx, energy(xy[0], xy[1])));
        }

        // create miPic.height() fake edges from each of the right level vertex to fake right extreme vertex
        for (int idx = miPic.width() - 1; idx < vertexNumber; idx += miPic.width()) {
            // (vertexNumber + 1) - fake right extreme vertex
            G.addEdge(new DirectedEdge(idx, vertexNumber + 1, 0));
        }

        int currTo = -1;
        seam.clear();
        AcyclicSP asp = new AcyclicSP(G, vertexNumber);
        double currDist = asp.distTo(vertexNumber + 1);
        for (DirectedEdge e : asp.pathTo(vertexNumber + 1)) {
            currTo = e.to();
            seam.add(currTo);
        }
        seam.remove(seam.size() - 1); // remove fake last vertex

        int[] o_seam = new int[seam.size()];
        int i = 0;
        for (int p : seam) {
            convert1DIndexTo2D(p, xy);
            o_seam[i++] = xy[1];
        }

        return o_seam;
    }

    private void convert1DIndexTo2D(int idx, int[] coords)
    {
        coords[0] = idx % miPic.width(); // x
        coords[1] = idx / miPic.width(); // y
    }

    public int[] findVerticalSeam()                 // sequence of indices for vertical seam
    {
        List<Integer> seam = new ArrayList<>();
        int[] xy = new int[2];

        int vertexNumber = miPic.height() * miPic.width();
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(vertexNumber + 2);

        // count from 0 inclusive
        for (int idx = 0; idx < (miPic.height() - 1) * miPic.width(); ++idx) {
            convert1DIndexTo2D(idx, xy);
            int nextLeftPointIdx = idx + miPic.width() - 1;
            int nextBelowPointIdx = idx + miPic.width();
            int nextRightPointIdx = idx + miPic.width() + 1;

            if (xy[0] > 0)
                G.addEdge(new DirectedEdge(idx, nextLeftPointIdx, energy(xy[0] - 1, xy[1] + 1)));

            G.addEdge(new DirectedEdge(idx, nextBelowPointIdx, energy(xy[0], xy[1] + 1)));

            if (xy[0] < miPic.width() - 1)
                G.addEdge(new DirectedEdge(idx, nextRightPointIdx, energy(xy[0] + 1, xy[1] + 1)));
        }

        // create miPic.width() fake edges from fake top upper vertex to the lower (1st) level
        for (int idx = 0; idx < miPic.width(); ++idx) {
            convert1DIndexTo2D(idx, xy);
            // vertexNumber - fake upper top vertex
            G.addEdge(new DirectedEdge(vertexNumber, idx, energy(xy[0], xy[1])));
        }

        // create miPic.width() fake edges from each of the lower (last) level vertex to fake bottom lower vertex
        for (int idx = vertexNumber - miPic.width(); idx < vertexNumber; ++idx) {
            // (vertexNumber + 1) - fake lower bottom vertex
            G.addEdge(new DirectedEdge(idx, vertexNumber + 1, 0));
        }

        int currTo = -1;
        seam.clear();
        AcyclicSP asp = new AcyclicSP(G, vertexNumber);
        double currDist = asp.distTo(vertexNumber + 1);
        for (DirectedEdge e : asp.pathTo(vertexNumber + 1)) {
            currTo = e.to();
            seam.add(currTo);
        }
        seam.remove(seam.size() - 1); // remove fake last vertex

        int[] o_seam = new int[seam.size()];
        int i = 0;
        for (int p : seam) {
            convert1DIndexTo2D(p, xy);
            o_seam[i++] = xy[0];
        }

        return o_seam;
    }

    // private void checkSeamContentsValidity()
    public    void removeHorizontalSeam(int[] seam)   // remove horizontal seam from current picture
    {
        if (seam == null || miPic.height() <= 1 || seam.length != miPic.width())
        {
            throw new java.lang.IllegalArgumentException();
        }

        int prev = -1;
        for (int idxY: seam)
        {
            if (idxY < 0 || idxY > miPic.height() - 1 || (prev != -1 && Math.abs(idxY - prev) > 1))
            {
                throw new java.lang.IllegalArgumentException();
            }
            prev = idxY;
        }

        Picture oPic = new Picture(miPic.width(), miPic.height() - 1);

        List<Integer> targetCol = new ArrayList<>();
        for (int row = 0; row < miPic.height(); ++row) {
            targetCol.add(row);
        }

        for (int col = 0; col < miPic.width(); ++col) {
            targetCol.remove(seam[col]);
            int row = 0;
            for (int rowSource: targetCol) {
                oPic.set(col, row++, miPic.get(col, rowSource));
            }
            targetCol.add(seam[col], seam[col]);
        }

        miPic = oPic;
        if (cachingTurnOn) cacheEnergy();
    }

    public    void removeVerticalSeam(int[] seam)     // remove vertical seam from current picture
    {
        if (seam == null || miPic.width() <= 1 || seam.length != miPic.height())
        {
            throw new java.lang.IllegalArgumentException();
        }

        int prev = -1;
        for (int idxX: seam)
        {
            if (idxX < 0 || idxX > miPic.width() - 1 || (prev != -1 && Math.abs(idxX - prev) > 1))
            {
                throw new java.lang.IllegalArgumentException();
            }
            prev = idxX;
        }

        Picture oPic = new Picture(miPic.width() - 1, miPic.height());

        List<Integer> targetRow = new ArrayList<>();
        for (int col = 0; col < miPic.width(); ++col) {
            targetRow.add(col);
        }

        for (int row = 0; row < miPic.height(); ++row) {
            targetRow.remove(seam[row]);
            int col = 0;
            for (int colSource: targetRow) {
                oPic.set(col++, row, miPic.get(colSource, row));
            }
            targetRow.add(seam[row], seam[row]);
        }

        miPic = oPic;
        if (cachingTurnOn) cacheEnergy();
    }

    public static void main(String[] args)
    {
        Picture picture = new Picture("P:\\IDEA_projects\\SeamCarving\\seam-testing\\seam\\chameleon.png");
        StdOut.printf("image is %d pixels wide by %d pixels high.\n", picture.width(), picture.height());

        SeamCarver sc = new SeamCarver(picture);

        for (int i = 0; i < 0.3 * picture.width(); ++i)
        {
            int[] foundSeam = sc.findVerticalSeam();
            sc.removeVerticalSeam(foundSeam);
        }

        sc.picture().save("P:\\IDEA_projects\\SeamCarving\\seam-testing\\out\\produced_kkk.png");
    }
}
