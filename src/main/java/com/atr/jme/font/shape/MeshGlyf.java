/*
 * Free Public License 1.0.0
 * Permission to use, copy, modify, and/or distribute this software
 * for any purpose with or without fee is hereby granted.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.atr.jme.font.shape;

import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.sfntly.AnchorTable;
import com.google.typography.font.sfntly.table.truetype.CompositeGlyph;
import com.google.typography.font.sfntly.table.truetype.Glyph;
import com.google.typography.font.sfntly.table.truetype.SimpleGlyph;
import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import edu.berkeley.jmescher.BPoint;
import edu.berkeley.jmescher.HalfEdge;
import edu.berkeley.jmescher.Point;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mesh representing the contour of a glyph in a true type font file.
 * 
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class MeshGlyf extends Mesh {
    public static final float EPSILON = 0.0000001f;
    
    private float maxX = Float.MIN_VALUE;
    private float minX = Float.MAX_VALUE;
    private float maxY = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE;
    
    public MeshGlyf(final TrueTypeMesh ttm, final AnchorTable ankr, final Glyph glyf) {
        processContours(ttm, ankr, glyf);
    }
    
    public float getMaxX() {
        return maxX;
    }
    
    public float getMinX() {
        return minX;
    }
    
    public float getMaxY() {
        return maxY;
    }
    
    public float getMinY() {
        return minY;
    }
    
    /**
     * Begin processing the glyph.
     * 
     * @param ttm
     * @param ankr
     * @param glyf 
     */
    private void processContours(final TrueTypeMesh ttm, final AnchorTable ankr, final Glyph glyf) {
        List<Tri> innerTriangles = new LinkedList<Tri>();
        List<Tri> bTriangles = new LinkedList<Tri>();
        
        if (glyf.glyphType() == Glyph.GlyphType.Composite) {
            /*
            * The glyph is a composite containing more than one glyph some of which
            * may be transformed by a transformation matrix.
            */
            CompositeGlyph g = (CompositeGlyph)glyf;
            int flags = g.flags(0);
            
            float[] matrix = new float[]{1, 0, 0, 1, 0, 0};
            if ((flags & CompositeGlyph.FLAG_WE_HAVE_A_SCALE)
                    == CompositeGlyph.FLAG_WE_HAVE_A_SCALE) {
                //The glyph has a single value used to scale both
                //the x and y-axis.
                matrix[0] = f2dot14(g.transformation(0));
                matrix[3] = matrix[0];
            } else if ((flags & CompositeGlyph.FLAG_WE_HAVE_AN_X_AND_Y_SCALE)
                    == CompositeGlyph.FLAG_WE_HAVE_AN_X_AND_Y_SCALE) {
                //The glyph has seperate scales for the x and y-axis.
                byte[] transform = g.transformation(0);
                matrix[0] = f2dot14(new byte[]{transform[0], transform[1]});
                matrix[3] = f2dot14(new byte[]{transform[2], transform[3]});
            } else if ((flags & CompositeGlyph.FLAG_WE_HAVE_A_TWO_BY_TWO)
                    == CompositeGlyph.FLAG_WE_HAVE_A_TWO_BY_TWO) {
                //The glyph is transformed by a 2x2 transformation matrix
                byte[] transform = g.transformation(0);
                matrix[0] = f2dot14(new byte[]{transform[0], transform[1]});
                matrix[1] = f2dot14(new byte[]{transform[2], transform[3]});
                matrix[2] = f2dot14(new byte[]{transform[4], transform[5]});
                matrix[3] = f2dot14(new byte[]{transform[6], transform[7]});
            }
            
            if ((flags & CompositeGlyph.FLAG_ARGS_ARE_XY_VALUES)
                == CompositeGlyph.FLAG_ARGS_ARE_XY_VALUES) {
                //The glyph is translated by two short values
                matrix[4] = (short)g.argument1(0);
                matrix[5] = (short)g.argument2(0);
            }
            
            triangulate(ttm, (SimpleGlyph)ttm.getGlyph(g.glyphIndex(0)),
                    innerTriangles, bTriangles, matrix);
            
            float[] lastMatrix = new float[]{
                matrix[0],
                matrix[1],
                matrix[2],
                matrix[3],
                matrix[4],
                matrix[5]
            };
            
            for (int i = 1; i < g.numGlyphs(); i++) {
                matrix[0] = 1;
                matrix[1] = 0;
                matrix[2] = 0;
                matrix[3] = 1;
                matrix[4] = 0;
                matrix[5] = 0;
                
                SimpleGlyph glyph = (SimpleGlyph)ttm.getGlyph(g.glyphIndex(i));
                flags = g.flags(i);
                
                if ((flags & CompositeGlyph.FLAG_WE_HAVE_A_SCALE)
                        == CompositeGlyph.FLAG_WE_HAVE_A_SCALE) {
                    //The glyph has a single value used to scale both
                    //the x and y-axis.
                    matrix[0] = f2dot14(g.transformation(i));
                    matrix[3] = matrix[0];
                } else if ((flags & CompositeGlyph.FLAG_WE_HAVE_AN_X_AND_Y_SCALE)
                        == CompositeGlyph.FLAG_WE_HAVE_AN_X_AND_Y_SCALE) {
                    //The glyph has seperate scales for the x and y-axis.
                    byte[] transform = g.transformation(i);
                    matrix[0] = f2dot14(new byte[]{transform[0], transform[1]});
                    matrix[3] = f2dot14(new byte[]{transform[2], transform[3]});
                } else if ((flags & CompositeGlyph.FLAG_WE_HAVE_A_TWO_BY_TWO)
                        == CompositeGlyph.FLAG_WE_HAVE_A_TWO_BY_TWO) {
                    //The glyph is transformed by a 2x2 transformation matrix
                    byte[] transform = g.transformation(i);
                    matrix[0] = f2dot14(new byte[]{transform[0], transform[1]});
                    matrix[1] = f2dot14(new byte[]{transform[2], transform[3]});
                    matrix[2] = f2dot14(new byte[]{transform[4], transform[5]});
                    matrix[3] = f2dot14(new byte[]{transform[6], transform[7]});
                }
                
                if ((flags & CompositeGlyph.FLAG_ARGS_ARE_XY_VALUES)
                    == CompositeGlyph.FLAG_ARGS_ARE_XY_VALUES) {
                    //The glyph is translated by two short values
                    matrix[4] = (short)g.argument1(i);
                    matrix[5] = (short)g.argument2(i);
                } else if ((flags & CompositeGlyph.FLAG_ARG_1_AND_2_ARE_WORDS)
                        == CompositeGlyph.FLAG_ARG_1_AND_2_ARE_WORDS) {
                    //The glyph is placed relative to an anchor point
                    int[] p1 = ankr.getAnchor(g.glyphIndex(i - 1), g.argument1(i));
                    int[] p2 = ankr.getAnchor(g.glyphIndex(i), g.argument2(i));
                    
                    float x = (p1[0] * lastMatrix[0]) + (p1[1] * lastMatrix[2]) + lastMatrix[4];
                    float y = (p1[0] * lastMatrix[1]) + (p1[1] * lastMatrix[3]) + lastMatrix[5];
                    
                    float x2 = (p2[0] * matrix[0]) + (p2[1] * matrix[2]);
                    float y2 = (p2[0] * matrix[1]) + (p2[1] * matrix[3]);
                    
                    matrix[4] = x - x2;
                    matrix[5] = y - y2;
                }
                
                triangulate(ttm, glyph, innerTriangles, bTriangles, matrix);
                lastMatrix[0] = matrix[0];
                lastMatrix[1] = matrix[1];
                lastMatrix[2] = matrix[2];
                lastMatrix[3] = matrix[3];
                lastMatrix[4] = matrix[4];
                lastMatrix[5] = matrix[5];
            }
        } else {
            /*
            * Simple glyph, just one glyph no fancy footwork here.
            */
            triangulate(ttm, (SimpleGlyph)glyf, innerTriangles, bTriangles, null);
        }
        
        createMesh(innerTriangles, bTriangles);
    }
    
    private float f2dot14(byte[] value) {
        return ByteBuffer.wrap(value).getShort() * (float)Math.pow(2, -14);
    }
    
    /**
     * Triangulate a glyph contour, parses out curved sections and the interior
     * area of the shape into triangles.
     * 
     * @param ttm
     * @param glyf
     * @param innerTriangles
     * @param bTriangles
     * @param matrix 
     */
    private void triangulate(TrueTypeMesh ttm, SimpleGlyph glyf,
            List<Tri> innerTriangles, List<Tri> bTriangles, float[] matrix) {
        edu.berkeley.jmescher.Mesh mesh = new edu.berkeley.jmescher.Mesh(EPSILON);
        BPoint[] boundary = new BPoint[4];
        boundary[0] = new BPoint(ttm.getMinCharX(), ttm.getMinCharY());
        boundary[1] = new BPoint(ttm.getMinCharX()
                + ((ttm.getMaxCharY() / ttm.getItalicRef()) * ttm.getItalicAngle()), ttm.getMaxCharY());
        boundary[2] = new BPoint(ttm.getMaxCharX()
                + ((ttm.getMaxCharY() / ttm.getItalicRef()) * ttm.getItalicAngle()), ttm.getMaxCharY());
        boundary[3] = new BPoint(ttm.getMaxCharX(), ttm.getMinCharY());
        edu.berkeley.jmescher.Mesh.linkBoundary(boundary);
        mesh.init(boundary);
        
        List<Seg> segs = new LinkedList<Seg>();
        List<Tri> bTris = new LinkedList<Tri>();
        
        int numContours = glyf.numberOfContours();
        if (matrix == null) {
            for (int contour = 0; contour < numContours; contour++) {
                getSimpleContours(glyf, contour, ttm, segs, bTris, mesh);
            }
        } else {
            for (int contour = 0; contour < numContours; contour++) {
                getSimpleContours(glyf, matrix, contour, ttm, segs, bTris, mesh);
            }
        }
        
        //Search the list of bezier triangles looking for instances where a segment
        //or control segment from another bezier triangle crosses the base of the curve
        //and split the bezier triangle into two new bezier triangles.
        List<Tri> tmpTris = new LinkedList<Tri>();
        triOverlap: for (ListIterator<Tri> it = bTris.listIterator(); it.hasNext();) {
            Tri t = it.next();
            if (tmpTris.contains(t)) {
                it.remove();
                tmpTris.remove(t);
                continue;
            }
            for (Seg s : segs) {
                if (s.p1 != t.ac.p1 && s.p1 != t.ac.p2
                        && s.p2 != t.ac.p1 && s.p2 != t.ac.p2) {
                    if (mesh.intersect(t.ac.p1, t.ac.p2, s.p1, s.p2)) {
                        //split the triangle that was intersected
                        it.remove();
                        float mx = t.ab.p1.x + ((t.ab.p2.x - t.ab.p1.x) / 2);
                        float my = t.ab.p1.y + ((t.ab.p2.y - t.ab.p1.y) / 2);
                        if (t.ab.p2.users == 1) {
                            mesh.removeInteriorPoint(t.ab.p2);
                        } else
                            t.ab.p2.users --;
                        Point b = mesh.addInteriorPoint(new Point(mx, my));
                        Seg newAB = new Seg(t.ab.p1, b);

                        mx = t.bc.p1.x + ((t.bc.p2.x - t.bc.p1.x) / 2);
                        my = t.bc.p1.y + ((t.bc.p2.y - t.bc.p1.y) / 2);
                        Point b2 = mesh.addInteriorPoint(new Point(mx, my));

                        mx = b.x + ((b2.x - b.x) / 2);
                        my = b.y + ((b2.y - b.y) / 2);
                        Point c2 = mesh.addInteriorPoint(new Point(mx, my));
                        Seg newBC = new Seg(b, c2);

                        Tri newTri = new Tri(newAB, newBC, new Seg(t.ab.p1, c2));
                        it.add(newTri);
                        //it.previous();

                        newTri = new Tri(new Seg(c2, b2), new Seg(b2, t.ac.p2), new Seg(c2, t.ac.p2));
                        it.add(newTri);
                        //it.previous();

                        continue triOverlap;
                    }
                }
            }
            
            for (Tri tri : bTris) {
                if (tmpTris.contains(tri))
                    continue;
                if (tri.ac.p1 == t.ac.p1 || tri.ac.p1 == t.ac.p2
                        || tri.ac.p2 == t.ac.p1 || tri.ac.p2 == t.ac.p2)
                    continue;
                
                if (mesh.intersect(t.ac.p1, t.ac.p2, tri.ab.p1, tri.ab.p2)) {
                    //split the triangle that was intersected.
                    //using it.previous() would cause the newly created triangles
                    //to be checked again and split again if necessary, but
                    //this leads to an infinite loop on characters where splitting
                    //the triangles doesn't resolve the issue. Namely this happens
                    //when the triangle is intersected near one of the corners.
                    it.remove();
                    float mx = t.ab.p1.x + ((t.ab.p2.x - t.ab.p1.x) / 2);
                    float my = t.ab.p1.y + ((t.ab.p2.y - t.ab.p1.y) / 2);
                    if (tri.ab.p2.users == 1) {
                        mesh.removeInteriorPoint(t.ab.p2);
                    } else
                        t.ab.p2.users --;
                    Point b = mesh.addInteriorPoint(new Point(mx, my));
                    Seg newAB = new Seg(t.ab.p1, b);

                    mx = t.bc.p1.x + ((t.bc.p2.x - t.bc.p1.x) / 2);
                    my = t.bc.p1.y + ((t.bc.p2.y - t.bc.p1.y) / 2);
                    Point b2 = mesh.addInteriorPoint(new Point(mx, my));

                    mx = b.x + ((b2.x - b.x) / 2);
                    my = b.y + ((b2.y - b.y) / 2);
                    Point c2 = mesh.addInteriorPoint(new Point(mx, my));
                    Seg newBC = new Seg(b, c2);

                    Tri newTri = new Tri(newAB, newBC, new Seg(t.ab.p1, c2));
                    it.add(newTri);
                    //it.previous();

                    newTri = new Tri(new Seg(c2, b2), new Seg(b2, t.ac.p2), new Seg(c2, t.ac.p2));
                    it.add(newTri);
                    //it.previous();
                    
                    //split the other triangle
                    tmpTris.add(tri);
                    mx = tri.ab.p1.x + ((tri.ab.p2.x - tri.ab.p1.x) / 2);
                    my = tri.ab.p1.y + ((tri.ab.p2.y - tri.ab.p1.y) / 2);
                    if (tri.ab.p2.users == 1) {
                        mesh.removeInteriorPoint(tri.ab.p2);
                    } else
                        tri.ab.p2.users --;
                    b = mesh.addInteriorPoint(new Point(mx, my));
                    newAB = new Seg(tri.ab.p1, b);

                    mx = tri.bc.p1.x + ((tri.bc.p2.x - tri.bc.p1.x) / 2);
                    my = tri.bc.p1.y + ((tri.bc.p2.y - tri.bc.p1.y) / 2);
                    b2 = mesh.addInteriorPoint(new Point(mx, my));

                    mx = b.x + ((b2.x - b.x) / 2);
                    my = b.y + ((b2.y - b.y) / 2);
                    c2 = mesh.addInteriorPoint(new Point(mx, my));
                    newBC = new Seg(b, c2);

                    newTri = new Tri(newAB, newBC, new Seg(tri.ab.p1, c2));
                    it.add(newTri);
                    //it.previous();

                    newTri = new Tri(new Seg(c2, b2), new Seg(b2, tri.ac.p2), new Seg(c2, tri.ac.p2));
                    it.add(newTri);
                    //it.previous();
                    
                    continue triOverlap;
                }
            }
        }
        bTris.removeAll(tmpTris);
        tmpTris.clear();
        
        //Add constraints to the triangulation
        for (Iterator<Seg> it = segs.iterator(); it.hasNext();) {
            Seg s = it.next();
            if (!s.p1.isType(Point.DELETED) && !s.p2.isType(Point.DELETED)) {
                try {
                    s.he = mesh.addConstraint(s.p1, s.p2);
                } catch (Exception e) {
                    it.remove();
                }
            } else
                it.remove();
        }
        
        for (Iterator<Tri> it = bTris.iterator(); it.hasNext();) {
            Tri t = it.next();
            try {
                if (t.ac.onRight(t.bc.p1)) {
                    t.ab.he = mesh.addConstraint(t.ab.p1, t.ab.p2);
                    t.ab.he.flag(HalfEdge.FLAG_BEZIER);
                    t.ab.he.sibling.flag(HalfEdge.FLAG_BEZIER);
                    t.bc.he = mesh.addConstraint(t.bc.p1, t.bc.p2);
                    t.bc.he.flag(HalfEdge.FLAG_BEZIER);
                    t.bc.he.sibling.flag(HalfEdge.FLAG_BEZIER);
                    mesh.addConstraint(t.ac.p1, t.ac.p2);
                    segs.add(t.ab);
                    segs.add(t.bc);
                } else {
                    mesh.addConstraint(t.ab.p1, t.ab.p2);
                    mesh.addConstraint(t.bc.p1, t.bc.p2);
                    t.ac.he = mesh.addConstraint(t.ac.p1, t.ac.p2);
                    segs.add(t.ac);
                    t.ac.he.flag(HalfEdge.FLAG_BEZIER);
                    t.ac.he.sibling.flag(HalfEdge.FLAG_BEZIER);
                }
            } catch (Exception e) {
                try {
                    t.ac.he = mesh.addConstraint(t.ac.p1, t.ac.p2);
                    segs.add(t.ac);
                } catch(Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error inserting"
                            + " bezier constraint.", ex);
                }
                it.remove();
            }
        }
        mesh = null;
        
        //Create a list of triangles on the shape's interior.
        //In a true type font file the interior of the shape is
        //to the right of travel.
        List<Tri> innerTris = new LinkedList<Tri>();
        for (Seg s : segs) {
            s.he.flag(HalfEdge.FLAG_CONTOUR);
            s.he.sibling.flag(HalfEdge.FLAG_CONTOUR);
            if (s.onRight(s.he.next.next.origin)) {
                if (s.he.next.isFlagged(HalfEdge.FLAG_CONTOUR)
                        || s.he.next.next.isFlagged(HalfEdge.FLAG_CONTOUR))
                    continue;
                Tri t = new Tri(new Seg(s.he.origin.clone(), s.he.next.origin.clone()),
                                new Seg(s.he.next.origin.clone(), s.he.next.next.origin.clone()),
                                new Seg(s.he.origin.clone(), s.he.next.next.origin.clone()));
                t.ab.he = s.he;
                if (!s.he.isFlagged(HalfEdge.FLAG_BEZIER)) {
                    t.ab.p1.setUV(0, 0);
                    t.ab.p2.setUV(0, 0);
                    t.ac.p2.setUV(1, 1);
                } else {
                    t.ab.p1.setUV(1, 1);
                    t.ab.p2.setUV(1, 1);
                    t.ac.p2.setUV(1, 1);
                }
                innerTris.add(t);
            } else {
                if (s.he.sibling.next.isFlagged(HalfEdge.FLAG_CONTOUR)
                        || s.he.sibling.next.next.isFlagged(HalfEdge.FLAG_CONTOUR))
                    continue;
                Tri t = new Tri(new Seg(s.he.sibling.origin.clone(), s.he.sibling.next.origin.clone()),
                                new Seg(s.he.sibling.next.origin.clone(), s.he.sibling.next.next.origin.clone()),
                                new Seg(s.he.sibling.origin.clone(), s.he.sibling.next.next.origin.clone()));
                t.ab.he = s.he.sibling;
                if (!s.he.isFlagged(HalfEdge.FLAG_BEZIER)) {
                    t.ab.p1.setUV(0, 0);
                    t.ab.p2.setUV(0, 0);
                    t.ac.p2.setUV(1, 1);
                } else {
                    t.ab.p1.setUV(1, 1);
                    t.ab.p2.setUV(1, 1);
                    t.ac.p2.setUV(1, 1);
                }
                innerTris.add(t);
            }
        }
        segs.clear();
        
        //Pick up any additional interior triangles that don't
        //share an edge with the shape.
        List<Tri> newTris = new LinkedList<Tri>();
        for (Tri t : innerTris) {
            innerTriSearch(newTris, t.ab.he.next);
            innerTriSearch(newTris, t.ab.he.next.next);
        }
        
         //Look for contour triangles that share two edges with the contour and
         //split into two triangles so our shader based anti-aliasing works.
        for (ListIterator<Tri> it = innerTris.listIterator(); it.hasNext();) {
            Tri t = it.next();
            if (t.ab.he.next.isFlagged(HalfEdge.FLAG_CONTOUR)) {
                if (t.ab.he.next.isFlagged(HalfEdge.FLAG_BEZIER)) {
                    if (t.ab.he.isFlagged(HalfEdge.FLAG_BEZIER))
                        continue;
                    t.ab.p1.setUV(0, 0);
                    t.ab.p2.setUV(0, 0);
                    t.ac.p2.setUV(1, 1);
                    continue;
                } else if (t.ab.he.isFlagged(HalfEdge.FLAG_BEZIER)) {
                    if (t.ab.he.next.isFlagged(HalfEdge.FLAG_BEZIER))
                        continue;
                    t.ab.p2.setUV(0, 0);
                    t.ac.p2.setUV(0, 0);
                    t.ac.p1.setUV(1, 1);
                    continue;
                }
                it.remove();
                Point p = new Point(t.ab.he.origin.x
                        + ((t.ab.he.next.next.origin.x - t.ab.he.origin.x) / 2),
                        t.ab.he.origin.y
                        + ((t.ab.he.next.next.origin.y - t.ab.he.origin.y) / 2));
                
                Seg ab = new Seg(t.ab.he.origin, t.ab.he.next.origin);
                Seg bc = new Seg(t.ab.he.next.origin, p);
                Seg ac = new Seg(t.ab.he.origin, p);
                Tri newTri = new Tri(ab, bc, ac);
                newTri.ab.p1.setUV(0, 0);
                newTri.ab.p2.setUV(0, 0);
                newTri.ac.p2.setUV(1, 1);
                it.add(newTri);
                
                ab = new Seg(t.ab.he.next.origin.clone(), t.ab.he.next.next.origin);
                bc = new Seg(t.ab.he.next.next.origin, p.clone());
                ac = new Seg(t.ab.he.next.origin.clone(), p.clone());
                newTri = new Tri(ab, bc, ac);
                newTri.ab.p1.setUV(0, 0);
                newTri.ab.p2.setUV(0, 0);
                newTri.ac.p2.setUV(1, 1);
                it.add(newTri);
            } else if (t.ab.he.next.next.isFlagged(HalfEdge.FLAG_CONTOUR)) {
                if (t.ab.he.next.next.isFlagged(HalfEdge.FLAG_BEZIER)) {
                    if (t.ab.he.isFlagged(HalfEdge.FLAG_BEZIER))
                        continue;
                    t.ab.p1.setUV(0, 0);
                    t.ab.p2.setUV(0, 0);
                    t.ac.p2.setUV(1, 1);
                    continue;
                } else if (t.ab.he.isFlagged(HalfEdge.FLAG_BEZIER)) {
                    if (t.ab.he.next.next.isFlagged(HalfEdge.FLAG_BEZIER))
                        continue;
                    t.ac.p1.setUV(0, 0);
                    t.ac.p2.setUV(0, 0);
                    t.ab.p2.setUV(1, 1);
                    continue;
                }
                it.remove();
                Point p = new Point(t.ab.he.next.origin.x
                        + ((t.ab.he.next.next.origin.x - t.ab.he.next.origin.x) / 2),
                        t.ab.he.next.origin.y
                        + ((t.ab.he.next.next.origin.y - t.ab.he.next.origin.y) / 2));
                
                Seg ab = new Seg(t.ab.he.next.next.origin, t.ab.he.origin);
                Seg bc = new Seg(t.ab.he.origin, p);
                Seg ac = new Seg(t.ab.he.next.next.origin, p);
                Tri newTri = new Tri(ab, bc, ac);
                newTri.ab.p1.setUV(0, 0);
                newTri.ab.p2.setUV(0, 0);
                newTri.ac.p2.setUV(1, 1);
                it.add(newTri);
                
                ab = new Seg(t.ab.he.origin.clone(), t.ab.he.next.origin.clone());
                bc = new Seg(t.ab.he.next.origin.clone(), p.clone());
                ac = new Seg(t.ab.he.origin.clone(), p.clone());
                newTri = new Tri(ab, bc, ac);
                newTri.ab.p1.setUV(0, 0);
                newTri.ab.p2.setUV(0, 0);
                newTri.ac.p2.setUV(1, 1);
                it.add(newTri);
            }
        }
        innerTris.addAll(newTris);
        newTris.clear();
        
        if (maxX == Float.MIN_VALUE) {
            maxX = 0;
            minX = 0;
        }
        if (maxY == Float.MIN_VALUE) {
            maxY = 0;
            minY = 0;
        }
        
        innerTriangles.addAll(innerTris);
        bTriangles.addAll(bTris);
    }
    
    /**
     * Searches for interior triangles that don't share an edge with the contour.
     * 
     * @param newTris
     * @param he 
     */
    private void innerTriSearch(final List<Tri> newTris, HalfEdge he) {
        if (he.sibling == null)
            return;
        if (!he.sibling.isFlagged(HalfEdge.FLAG_READ)
                && !he.sibling.next.isFlagged(HalfEdge.FLAG_READ)
                && !he.sibling.next.next.isFlagged(HalfEdge.FLAG_READ)
                && !he.sibling.isFlagged(HalfEdge.FLAG_CONTOUR)
                && !he.sibling.next.isFlagged(HalfEdge.FLAG_CONTOUR)
                && !he.sibling.next.next.isFlagged(HalfEdge.FLAG_CONTOUR)) {
            Tri tri = new Tri(new Seg(he.sibling.origin.clone(), he.sibling.next.origin.clone()),
                            new Seg(he.sibling.next.origin.clone(), he.sibling.next.next.origin.clone()),
                            new Seg(he.sibling.origin.clone(), he.sibling.next.next.origin.clone()));
            he.sibling.flag(HalfEdge.FLAG_READ);
            tri.ab.p1.setUV(1, 1);
            tri.ab.p2.setUV(1, 1);
            tri.ac.p2.setUV(1, 1);
            newTris.add(tri);
            
            /*
            * This was recursive, but ran into problems where the glyph had overlapping
            * triangles. Hopefully this won't be an issue.
            */
            //innerTriSearch(newTris, he.sibling.next);
            //innerTriSearch(newTris, he.sibling.next.next);
        }
    }
    
    /**
     * Reads the contours of a glyph and parses them into line segments and
     * triangles.
     * 
     * @param glyf
     * @param contour
     * @param ttm
     * @param segments
     * @param triangles
     * @param mesh
     * @return 
     */
    private edu.berkeley.jmescher.Mesh getSimpleContours(SimpleGlyph glyf,
            int contour, TrueTypeMesh ttm, List<Seg> segments, List<Tri> triangles,
            edu.berkeley.jmescher.Mesh mesh) {
        Point a = new Point();
        int numPoints = glyf.numberOfPoints(contour);

        float last1X = glyf.xCoordinate(contour, 0) * ttm.getPointScale();
        float last1Y = glyf.yCoordinate(contour, 0) * ttm.getPointScale();

        last1X += (last1Y / ttm.getItalicRef()) * ttm.getItalicAngle();

        float firstOnCurveX = last1X;
        float firstOnCurveY = last1Y;
        boolean last1OnCurve = glyf.onCurve(contour, 0);
        if (last1OnCurve)
            a = mesh.addInteriorPoint(new Point(last1X, last1Y));

        if (last1X > maxX)
            maxX = last1X;
        if (last1X < minX)
            minX = last1X;
        if (last1Y > maxY)
            maxY = last1Y;
        if (last1Y < minY)
            minY = last1Y;

        float firstX = last1X;
        float firstY = last1Y;
        boolean firstOnCurve = last1OnCurve;

        for (int point = 1; point < numPoints; point++) {
            boolean onCurve = glyf.onCurve(contour, point);
            float x = glyf.xCoordinate(contour, point) * ttm.getPointScale();
            float y = glyf.yCoordinate(contour, point) * ttm.getPointScale();

            x += (y / ttm.getItalicRef()) * ttm.getItalicAngle();

            if (x > maxX)
                maxX = x;
            if (x < minX)
                minX = x;
            if (y > maxY)
                maxY = y;
            if (y < minY)
                minY = y;

            if (!onCurve && !last1OnCurve) {
                if (point == 1) {
                    last1X = x;
                    last1Y = y;

                    x = last1X + ((x - last1X) / 2f);
                    y = last1Y + ((y - last1Y) / 2f);

                    firstOnCurveX = x;
                    firstOnCurveY = y;
                    a = mesh.addInteriorPoint(new Point(x, y));

                    last1OnCurve = false;

                    continue;
                }
                x = last1X + ((x - last1X) / 2f);
                y = last1Y + ((y - last1Y) / 2f);

                onCurve = true;
                point--;
            } else if (!last1OnCurve && point == 1) {
                firstOnCurveX = x;
                firstOnCurveY = y;
                a = mesh.addInteriorPoint(new Point(x, y));

                last1X = x;
                last1Y = y;
                last1OnCurve = true;

                continue;
            }

            if (onCurve && !last1OnCurve) {
                Point b = new Point(last1X, last1Y);
                //Point b = mesh.addInteriorPoint(new Point(last1X, last1Y));
                Point c = mesh.addInteriorPoint(new Point(x, y));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
                a = c;
            } else if (onCurve) {
                Point b = mesh.addInteriorPoint(new Point(x, y));
                segments.add(new Seg(a, b));
                a = b;
            }

            last1X = x;
            last1Y = y;
            last1OnCurve = onCurve;
        }

        if (last1OnCurve) {
            if (firstOnCurve) {
                //Both the first and last points are on-curve. We
                //create an edge between the two.
                Point b = mesh.addInteriorPoint(new Point(firstX, firstY));
                segments.add(new Seg(a, b));
            } else {
                //The last point is on-curve, but the first point is not.
                //We create a bezier curve between the last and second
                //points with the first point as the middle control.
                Point b = new Point(firstX, firstY);
                Point c = mesh.addInteriorPoint(new Point(firstOnCurveX, firstOnCurveY));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
            }
        } else {
            if (firstOnCurve) {
                //The first point is on-curve, but the last point
                //was not. We create a bezier curve between the
                //second to last point and the first point with
                //the last point as the middle control.
                Point b = new Point(last1X, last1Y);
                Point c = mesh.addInteriorPoint(new Point(firstX, firstY));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
            } else {
                //The first and last contour points are both off-curve.
                //We add an on-curve point between them and create two
                //new quadratic bezier curves, one between the second to
                //last point and the new mid-point with the last point as
                //the middle control point and another between the new
                //mid-point and the second point with the first point as
                //the middle control point.
                float x = last1X + ((firstX - last1X) / 2f);
                float y = last1Y + ((firstY - last1Y) / 2f);
                Point b = new Point(last1X, last1Y);
                Point c = mesh.addInteriorPoint(new Point(x, y));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
                a = c;
                
                b = new Point(firstX, firstY);
                c = mesh.addInteriorPoint(new Point(firstOnCurveX, firstOnCurveY));
                //ab = new Seg(a, b);
                //bc = new Seg(b, c);
                ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
            }
        }
        
        return mesh;
    }
    
    /**
     * Reads the contours of a transformed glyph and parses them into
     * line segments and triangles.
     * 
     * @param glyf
     * @param matrix
     * @param contour
     * @param ttm
     * @param segments
     * @param triangles
     * @param mesh 
     */
    private void getSimpleContours(SimpleGlyph glyf, float[] matrix, 
            int contour, TrueTypeMesh ttm, List<Seg> segments, List<Tri> triangles,
            edu.berkeley.jmescher.Mesh mesh) {
        Point a = new Point();
        int numPoints = glyf.numberOfPoints(contour);

        float ox = glyf.xCoordinate(contour, 0);
        float last1Y = glyf.yCoordinate(contour, 0);
        //Transform our points by the supplied matrix
        float last1X = ((ox * matrix[0]) + (last1Y * matrix[2]) + matrix[4]) * ttm.getPointScale();
        last1Y = ((ox * matrix[1]) + (last1Y * matrix[3]) + matrix[5]) * ttm.getPointScale();

        last1X += (last1Y / ttm.getItalicRef()) * ttm.getItalicAngle();

        float firstOnCurveX = last1X;
        float firstOnCurveY = last1Y;
        boolean last1OnCurve = glyf.onCurve(contour, 0);
        if (last1OnCurve)
            a = mesh.addInteriorPoint(new Point(last1X, last1Y));

        if (last1X > maxX)
            maxX = last1X;
        if (last1X < minX)
            minX = last1X;
        if (last1Y > maxY)
            maxY = last1Y;
        if (last1Y < minY)
            minY = last1Y;

        float firstX = last1X;
        float firstY = last1Y;
        boolean firstOnCurve = last1OnCurve;

        for (int point = 1; point < numPoints; point++) {
            boolean onCurve = glyf.onCurve(contour, point);
            ox = glyf.xCoordinate(contour, point);
            float y = glyf.yCoordinate(contour, point);
            //transform by the supplied matrix
            float x = ((ox * matrix[0]) + (y * matrix[2]) + matrix[4]) * ttm.getPointScale();
            y = ((ox * matrix[1]) + (y * matrix[3]) + matrix[5]) * ttm.getPointScale();

            x += (y / ttm.getItalicRef()) * ttm.getItalicAngle();

            if (x > maxX)
                maxX = x;
            if (x < minX)
                minX = x;
            if (y > maxY)
                maxY = y;
            if (y < minY)
                minY = y;

            if (!onCurve && !last1OnCurve) {
                if (point == 1) {
                    last1X = x;
                    last1Y = y;

                    x = last1X + ((x - last1X) / 2f);
                    y = last1Y + ((y - last1Y) / 2f);

                    firstOnCurveX = x;
                    firstOnCurveY = y;
                    a = mesh.addInteriorPoint(new Point(x, y));

                    last1OnCurve = false;

                    continue;
                }
                x = last1X + ((x - last1X) / 2f);
                y = last1Y + ((y - last1Y) / 2f);

                onCurve = true;
                point--;
            } else if (!last1OnCurve && point == 1) {
                firstOnCurveX = x;
                firstOnCurveY = y;
                a = mesh.addInteriorPoint(new Point(x, y));

                last1X = x;
                last1Y = y;
                last1OnCurve = true;

                continue;
            }

            if (onCurve && !last1OnCurve) {
                Point b = new Point(last1X, last1Y);
                Point c = mesh.addInteriorPoint(new Point(x, y));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
                a = c;
            } else if (onCurve) {
                Point b = mesh.addInteriorPoint(new Point(x, y));
                segments.add(new Seg(a, b));
                a = b;
            }

            last1X = x;
            last1Y = y;
            last1OnCurve = onCurve;
        }

        if (last1OnCurve) {
            if (firstOnCurve) {
                //Both the first and last points are on-curve. We
                //create an edge between the two.
                Point b = mesh.addInteriorPoint(new Point(firstX, firstY));
                segments.add(new Seg(a, b));
            } else {
                //The last point is on-curve, but the first point is not.
                //We create a bezier curve between the last and second
                //points with the first point as the middle control.
                Point b = new Point(firstX, firstY);
                Point c = mesh.addInteriorPoint(new Point(firstOnCurveX, firstOnCurveY));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
            }
        } else {
            if (firstOnCurve) {
                //The first point is on-curve, but the last point
                //was not. We create a bezier curve between the
                //second to last point and the first point with
                //the last point as the middle control.
                Point b = new Point(last1X, last1Y);
                Point c = mesh.addInteriorPoint(new Point(firstX, firstY));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
            } else {
                //The first and last contour points are both off-curve.
                //We add an on-curve point between them and create two
                //new quadratic bezier curves, one between the second to
                //last point and the new mid-point with the last point as
                //the middle control point and another between the new
                //mid-point and the second point with the first point as
                //the middle control point.
                float x = last1X + ((firstX - last1X) / 2f);
                float y = last1Y + ((firstY - last1Y) / 2f);
                Point b = new Point(last1X, last1Y);
                Point c = mesh.addInteriorPoint(new Point(x, y));
                //Seg ab = new Seg(a, b);
                //Seg bc = new Seg(b, c);
                Seg ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
                a = c;
                
                b = new Point(firstX, firstY);
                c = mesh.addInteriorPoint(new Point(firstOnCurveX, firstOnCurveY));
                //ab = new Seg(a, b);
                //bc = new Seg(b, c);
                ac = new Seg(a, c);
                if (ac.onLine(b)) {
                    segments.add(ac);
                    //mesh.removeInteriorPoint(b);
                } else {
                    b = mesh.addInteriorPoint(b);
                    Seg ab = new Seg(a, b);
                    Seg bc = new Seg(b, c);
                    triangles.add(new Tri(ab, bc, ac));
                }
            }
        }
    }
    
    /**
     * Finally we take our triangles and build a mesh for rendering.
     * 
     * @param innerTris
     * @param bTris 
     */
    private void createMesh(List<Tri> innerTris, List<Tri> bTris) {
        FloatBuffer verts = BufferUtils.createFloatBuffer(((innerTris.size() + bTris.size()) * 3) * 3);
        FloatBuffer tex1 = BufferUtils.createFloatBuffer((verts.capacity() / 3) * 2);
        FloatBuffer tex2 = BufferUtils.createFloatBuffer(tex1.capacity());
        ShortBuffer indices = BufferUtils.createShortBuffer(verts.capacity() / 3);
        
        float[] angles = new float[3];
        Point[] points = new Point[3];
        Point[] result = new Point[3];
        Point tmp = new Point();
        
        short count = 0;
        for (Tri t : innerTris) {
            points[0] = t.ab.p1;
            points[1] = t.ab.p2;
            points[2] = t.ac.p2;
            orderVerts(points, angles, tmp, result);
            verts.put(result[0].x);
            verts.put(result[0].y);
            verts.put(0);
            tex1.put(result[0].getUV().x);
            tex1.put(result[0].getUV().y);
            tex2.put(2);
            tex2.put(0);
            
            verts.put(result[1].x);
            verts.put(result[1].y);
            verts.put(0);
            tex1.put(result[1].getUV().x);
            tex1.put(result[1].getUV().y);
            tex2.put(2);
            tex2.put(0);
            
            verts.put(result[2].x);
            verts.put(result[2].y);
            verts.put(0);
            tex1.put(result[2].getUV().x);
            tex1.put(result[2].getUV().y);
            tex2.put(2);
            tex2.put(0);
            
            indices.put(count);
            indices.put((short)(count + 1));
            indices.put((short)(count + 2));
            
            count += 3;
        }
        
        for (Tri t : bTris) {
            t.calcUV();
            
            points[0] = t.ab.p1.clone();
            points[0].setUV(0, 0);
            points[1] = t.ab.p2.clone();
            points[1].setUV(0.5f, 0);
            points[2] = t.ac.p2.clone();
            points[2].setUV(1, 1);
            orderVerts(points, angles, tmp, result);
            verts.put(result[0].x);
            verts.put(result[0].y);
            verts.put(0);
            tex1.put(result[0].getUV().x);
            tex1.put(result[0].getUV().y);
            
            verts.put(result[1].x);
            verts.put(result[1].y);
            verts.put(0);
            tex1.put(result[1].getUV().x);
            tex1.put(result[1].getUV().y);
            
            verts.put(result[2].x);
            verts.put(result[2].y);
            verts.put(0);
            tex1.put(result[2].getUV().x);
            tex1.put(result[2].getUV().y);
            
            if (t.pointingRight()) {
                tex2.put(1);
                tex2.put(1);
                tex2.put(1);
                tex2.put(1);
                tex2.put(1);
                tex2.put(1);
            } else {
                tex2.put(1);
                tex2.put(-1);
                tex2.put(1);
                tex2.put(-1);
                tex2.put(1);
                tex2.put(-1);
            }
            
            indices.put(count);
            indices.put((short)(count + 1));
            indices.put((short)(count + 2));
            
            count += 3;
        }
        
        setBuffer(VertexBuffer.Type.Position, 3, verts);
        setBuffer(VertexBuffer.Type.TexCoord, 2, tex1);
        setBuffer(VertexBuffer.Type.TexCoord2, 2, tex2);
        setBuffer(VertexBuffer.Type.Index, 3, indices);
        
        updateBound();
    }
    
    /**
     * For internal use only. This method takes in a list of 3 points from a triangle
     * and returns an array of the same points ordered counter-clockwise.
     * 
     * @param p
     * @param result
     * @return 
     */
    private Point[] orderVerts(Point[] points, float[] angles, Point tmp, Point[] result) {
        tmp.x = (points[0].x + points[1].x + points[2].x) / 3;
        tmp.y = (points[0].y + points[1].y + points[2].y) / 3;
        
        angles[0] = angle(tmp, points[0]);
        angles[1] = angle(tmp, points[1]);
        angles[2] = angle(tmp, points[2]);
        
        if (angles[0] < angles[1]) {
            if (angles[0] < angles[2]) {
                result[0] = points[0];
                if (angles[1] < angles[2]) {
                    result[1] = points[1];
                    result[2] = points[2];
                } else {
                    result[2] = points[1];
                    result[1] = points[2];
                }
            } else {
                result[0] = points[2];
                result[1] = points[0];
                result[2] = points[1];
            }
        } else if (angles[1] < angles[2]) {
            result[0] = points[1];
            if (angles[0] < angles[2]) {
                result[1] = points[0];
                result[2] = points[2];
            } else {
                result[2] = points[0];
                result[1] = points[2];
            }
        } else {
            result[0] = points[2];
            result[1] = points[1];
            result[2] = points[0];
        }
        
        return result;
    }
    
    /**
     * Calculates the angle between two points relative to the positive
     * x-axis.
     * 
     * @param p1
     * @param p2
     * @return 
     */
    public static float angle(Point p1, Point p2) {
        return FastMath.atan2(p2.y - p1.y, p2.x - p1.x);
    }
    
    /**
     * A helper class representing a line segment.
     * 
     * @author Adam T. Ryder
     */
    private class Seg {
        public final Point p1;
        public final Point p2;
        public HalfEdge he;
        
        private Seg(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
        
        /**
         * Determines if the supplied point is on the right side of
         * the line segment.
         * 
         * @param p The point to check against.
         * @return True if the point is on the right side otherwise false.
         */
        public final boolean onRight(Point p) {
            return (p2.x - p1.x) * (p.y - p1.y) - (p2.y - p1.y) * (p.x - p1.x) < 0;
        }
        
        /**
         * Determines if the supplied point is on the segment.
         * 
         * @param p The point to check against.
         * @return True if the point is on the line segment otherwise false.
         */
        public final boolean onLine(Point p) {
            float ab = FastMath.sqrt(FastMath.sqr(p2.x - p1.x) + FastMath.sqr(p2.y - p1.y));
            float ap = FastMath.sqrt(FastMath.sqr(p.x - p1.x) + FastMath.sqr(p.y - p1.y));
            float pb = FastMath.sqrt(FastMath.sqr(p2.x - p.x) + FastMath.sqr(p2.y - p.y));
            float result = ab - (ap + pb);
            
            return (result >= 0) ? result < EPSILON : result > -EPSILON;
        }
    }
    
    /**
     * A helper class representing a triangle.
     * 
     * @author Adam T. Ryder
     */
    private class Tri {
        public final Seg ab;
        public final Seg bc;
        public final Seg ac;
        
        private Tri(Seg ab, Seg bc, Seg ac) {
            this.ab = ab;
            this.bc = bc;
            this.ac = ac;
        }
        
        /**
         * Calculates the uv coordinates for each point in
         * a bezier triangle.
         */
        public void calcUV() {
            ab.p1.setUV(0, 0);
            ab.p2.setUV(0.5f, 0);
            ac.p2.setUV(1, 1);
        }
        
        /**
         * Determines if this bezier triangle's control point is to the
         * right or left of the base.
         * 
         * @return True if the control point, ab.p2, is to the right
         * of the base otherwise false.
         */
        public boolean pointingRight() {
            return ac.onRight(ab.p2);
        }
    }
}
