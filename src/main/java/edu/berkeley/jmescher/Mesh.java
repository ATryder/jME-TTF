/* Copyright (c) 2008-2009 Mark L. Howison
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  (1) Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *  (2) Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  (3) The name of the copyright holder may not be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.jmescher;

import com.atr.jme.font.util.Point2d;
import com.atr.jme.font.util.Point3d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a 2D triangulation using points (vertices) and halfedges.
 * Triangulation is performed by an interactive constrained Delaunay
 * algorithm. Point insertion uses Lawson's algorithm. Constraint insertion
 * and removal, as well as vertex removal and relocation are supported.
 *
 * For full details, see the "Interactive Constrained Delaunay Triangulation"
 * section of:
 *
 * Howison, M. CAD Tools for Creating Space-filling 3D Escher Tiles.
 * Master’s thesis, U.C. Berkeley, Berkeley, CA, May 2009.
 * (also Tech Report EECS-2009-56,
 *     http://www.eecs.berkeley.edu/Pubs/TechRpts/2009/EECS-2009-56.html)
 *
 * Or contact the author: mark.howison@gmail.com
 *
 * @author Mark Howison
 */
public class Mesh {

    /* drawing options */
    protected final static boolean DRAW_PT_FULL = true;
    protected final static boolean DRAW_PT_INDICES = false;
    protected final static boolean DRAW_HE_INDICES = false;
    protected final static boolean DRAW_HALFEDGES = false;

    /* typical size of polygons, for initializing ArrayLists */
    private final static int TYPICAL_POLYGON_SIZE = 16;

    /* errors */
    protected final static String E_EXHAUSTED
            = "Exhausted halfedges or points!";
    protected final static String E_MISSING
            = "Missing halfedge or point!";
    protected final static String E_IDENTICAL
            = "Identical halfedges or points!";
    protected final static String E_COINCIDENT
            = "Coincident points!";
    protected final static String E_TYPE
            = "Incorrect type!";
    protected final static String E_POLYGON
            = "Illegal polygonal region!";
    protected final static String E_HALFEDGE
            = "Mismatched halfedge!";
    protected final static int NULL_VALUE = -100000;

    /* stdout/debug/test flags */
    public final static boolean MESSAGES = false;
    //public final static boolean DEBUG = false;
    public final static boolean TEST = false;

    /* debug */
    public boolean toggleDebugVisual = false;
    public boolean toggleDebugInteractive = false;
    public boolean toggleDebugSuspend = false;
    //protected HashSet debugObjects;
    /*protected JFrame debugFrame = null;
    protected JPanel debugPanel = null;*/
    private boolean testing = false;

    /* epsilon squared (i.e. for use in squared distance comparisons) */
    public final float epsilon;

    /* floating point filter for robust predicates */
    private final float orientErrorBound;
    private final float incircleErrorBound;

    /* mesh data */
    final protected List<Point> points
            = (List) Collections.synchronizedList(new LinkedList());
    final protected List<HalfEdge> halfEdges
            = (List) Collections.synchronizedList(new LinkedList());
    protected int nBoundary;

    /* queues */
    protected LinkedList<HalfEdge> delaunayQueue = new LinkedList();
    protected LinkedList<Point> removedConstraints = new LinkedList();
    protected LinkedList<Point> deleteQueue = new LinkedList();
    private Point removeConstraintPeg = null;

    /* name and colors */
    protected String name = new String("(unnamed mesh)");
    /*protected Color highlightColor = Color.RED;
    protected Color warnColor = new Color(255, 240, 240);
    protected Color grayColor = new Color(127, 127, 127, 127);*/

    /******************************************************************************/
    /* Constructors etc.
/******************************************************************************/
    public Mesh(float epsilon) {
        this.epsilon = epsilon;
        float e = initMachineEpsilon();
        orientErrorBound = (3.0f + 16.0f * e) * e;
        incircleErrorBound = (10.0f + 96.0f * e) * e;
        /*if (DEBUG) {
            debugObjects = new HashSet();
        }*/
    }

    /*
     * Find the machine epsilon, which is used in initializing the floating
     * point filter, the threshold at which predicate calculations become
     * unreliable and exact arithmetic is neccessary.
     *
     * This routine is adapted from Jonathan Shewchuk's predicates.c
     * exactinit() routine.
     *
     * @return  machine epsilon
     */
    float initMachineEpsilon() {
        float half, e, check, lastcheck;

        half = 0.5f;
        e = 1.0f;
        check = 1.0f;
        /* Repeatedly divide `epsilon' by two until it is too small to add to    */
 /*   one without causing roundoff.  (Also check if the sum is equal to   */
 /*   the previous sum, for machines that round up instead of using exact */
 /*   rounding.  Not that this library will work on such machines anyway. */
        do {
            lastcheck = check;
            e *= half;
            check = 1.0f + e;
        } while ((check != 1.0) && (check != lastcheck));
        return e;
    }

    /******************************************************************************/
    /* Accessors & Mutators
/******************************************************************************/
    public int size() {
        return points.size();
    }

    public int boundarySize() {
        return nBoundary;
    }

    public void clear() {
        points.clear();
        halfEdges.clear();
        delaunayQueue.clear();
    }

    public void clearFlags(int flag) {
        for (HalfEdge he : halfEdges) {
            he.unflag(flag);
        }
    }

    public boolean contains(Point p) {
        return points.contains(p);
    }

    public int indexOf(Point p) {
        return points.indexOf(p);
    }

    public Point getPoint(int i) {
        return points.get(i);
    }

    public Point3d[] getPointCoordinates3d() {
        Point3d[] coords = new Point3d[points.size()];
        int i = 0;
        for (Point p : points) {
            coords[i++] = p.coords3d;
        }
        return coords;
    }

    public Point2d[] getFaceCoordinates() {
        ArrayList<Point2d> facePoints = new ArrayList();
        // reset 'used' flags
        clearFlags(HalfEdge.FLAG_READ);
        // find the faces
        for (HalfEdge he0 : halfEdges) {
            if (he0.isFlagged(HalfEdge.FLAG_READ)) {
                continue;
            }
            HalfEdge he1 = he0.next;
            HalfEdge he2 = he1.next;
            // the face is oriented CCW
            facePoints.add(new Point2d(he0.origin));
            facePoints.add(new Point2d(he1.origin));
            facePoints.add(new Point2d(he2.origin));
            // mark these half edges as used
            he0.flag(HalfEdge.FLAG_READ);
            he1.flag(HalfEdge.FLAG_READ);
            he2.flag(HalfEdge.FLAG_READ);
        }
        return facePoints.toArray(new Point2d[0]);
    }

    public Point3d[] getFaceCoordinates3d() {
        ArrayList<Point3d> coords = new ArrayList();
        // reset 'used' flags
        clearFlags(HalfEdge.FLAG_READ);
        // find the faces
        for (HalfEdge he0 : halfEdges) {
            if (he0.isFlagged(HalfEdge.FLAG_READ)) {
                continue;
            }
            HalfEdge he1 = he0.next;
            HalfEdge he2 = he1.next;
            // the face is oriented CCW
            coords.add(new Point3d(he0.origin.coords3d));
            coords.add(new Point3d(he1.origin.coords3d));
            coords.add(new Point3d(he2.origin.coords3d));
            // mark these half edges as used
            he0.flag(HalfEdge.FLAG_READ);
            he1.flag(HalfEdge.FLAG_READ);
            he2.flag(HalfEdge.FLAG_READ);
        }
        assert coords.size() % 3 == 0 : error("Illegal face list!");
        return coords.toArray(new Point3d[0]);
    }

    public Edge[] getEdges() {
        ArrayList<Edge> edges = new ArrayList();
        clearFlags(HalfEdge.FLAG_READ);
        // find the faces
        for (HalfEdge he : halfEdges) {
            if (he.isFlagged(HalfEdge.FLAG_READ)) {
                continue;
            }
            Edge e = new Edge(
                    new Point(he.origin),
                    new Point(he.next.origin));
            e.type = he.getType();
            edges.add(e);
            he.flag(HalfEdge.FLAG_READ);
        }
        return edges.toArray(new Edge[0]);
    }

    public void setName(String s) {
        name = s;
    }

    /*public void setDebugFrame(JFrame f) {
        debugFrame = f;
    }

    public void setDebugPanel(JPanel p) {
        debugPanel = p;
    }*/

    public void translate(float x, float y) {
        for (Point p : points) {
            p.x += x;
            p.y += y;
        }
    }

    /******************************************************************************/
    /* Insertion methods
/******************************************************************************/
    public static void linkBoundary(BPoint[] pts) {
        int s = pts.length;
        for (int i = 0; i < s; i++) {
            pts[i].next = pts[(i + 1) % s];
            pts[i].prev = pts[(i - 1 + s) % s];
        }
    }

    // expects a list of points in *clockwise* order for specifiying
    // an initial boundary for the mesh
    public void init(Point[] pts) {
        int s = pts.length;
        assert s >= 3 : error("Initialization requires at least 3 points!");
        ArrayList<HalfEdge> polygon = new ArrayList(s);
        clear();
        for (Point p : pts) {
            points.add(p);
            p.setType(Point.BOUNDARY);
            p.he = new HalfEdge(p, HalfEdge.BOUNDARY);
            halfEdges.add(p.he);
        }
        for (int i = 0; i < s; i++) {
            halfEdges.get(i).next = halfEdges.get((s + i - 1) % s);
        }
        nBoundary = s;
        for (int i = 0; i < s; i++) {
            polygon.add(halfEdges.get(s - 1 - i));
        }
        fillGeneralPolygon(polygon);
        delaunayQueue.addAll(polygon);
        updateDelaunay();
    }

    private HalfEdge addHalfEdge(Point origin, Point destination) {
        HalfEdge he1 = new HalfEdge(origin);
        HalfEdge he2 = new HalfEdge(destination);
        he1.sibling = he2;
        he2.sibling = he1;
        halfEdges.add(he1);
        halfEdges.add(he2);
        if (MESSAGES) {
            message("Added halfedges %d and %d.",
                    halfEdges.indexOf(he1),
                    halfEdges.indexOf(he2));
        }
        return he1;
    }

    /*
     * Adds an edge connecting the origins of he1 and he2.
     *
     * @return  the halfedge from he1.origin to he2.origin
     */
    private HalfEdge addEdge(
            HalfEdge he1,
            HalfEdge he2,
            HalfEdge he1prev,
            HalfEdge he2prev) {
        assert he1prev.next == he1 : error(E_HALFEDGE, he1, he1prev);
        assert he2prev.next == he2 : error(E_HALFEDGE, he2, he2prev);
        assert he1 != he2 : error(E_COINCIDENT);
        assert he1.origin != he2.origin : error(E_COINCIDENT);

        HalfEdge heAdd = addHalfEdge(he1.origin, he2.origin);
        delaunayQueue.add(heAdd);
        heAdd.next = he2;
        he1prev.next = heAdd;
        heAdd.sibling.next = he1;
        he2prev.next = heAdd.sibling;
        return heAdd;
    }

    public Point addBoundaryPoint(Point p, HalfEdge he0) {
        assert halfEdges.contains(he0) : error(E_MISSING);
        assert between(he0.origin, he0.next.origin, p) :
                error("Adding boundary point that doesn't lie on boundary!");

        HalfEdge he1, he2, he3;

        /* check for coincidence with the endpoints of he0 */
        if (coincident(p, he0.origin)) {
            if (MESSAGES) {
                message(
                        "Boundary point is within epsilon of %d.",
                        points.indexOf(he0.origin));
            }
            return he0.origin;
        }
        if (coincident(p, he0.next.origin)) {
            if (MESSAGES) {
                message(
                        "Boundary point is within epsilon of %d.",
                        points.indexOf(he0.next.origin));
            }
            return he0.next.origin;
        }
        points.add(p);
        if (MESSAGES) {
            message("Adding boundary point %d.", points.indexOf(p));
        }
        he2 = he0.next;
        he3 = he2.next;
        /* split the existing boundary */
        he1 = new HalfEdge(p, HalfEdge.BOUNDARY);
        halfEdges.add(he1);
        /* link halfedges */
        p.he = he1;
        he0.next = he1;
        he1.next = he2;
        fillQuadrilateral(he0);
        updateDelaunay();
        nBoundary++;
        if (TEST) {
            test();
        }
        return p;
    }

    public Point addInteriorPoint(Point p) {
        float dist, min;
        Point pNearest = null;
        FaceWalk walk;
        
        /*if (DEBUG) {
            debugView(p, "addInteriorPoint: p");
        }*/
        /* find the closest point to p */
        min = Float.MAX_VALUE;
        for (Point pTest : points) {
            dist = p.distanceSquared(pTest);
            /* abort if the point is within epsilon of an existing point */
            if (dist < epsilon) {
                if (MESSAGES) {
                    message(
                            "Point is within epsilon of %d.",
                            points.indexOf(pTest));
                }
                /*if (DEBUG) {
                    debugView(pTest, "addInteriorPoint: within epsilon");
                }*/
                pTest.users ++;
                return pTest;
            } else if (dist < min) {
                min = dist;
                pNearest = pTest;
            }
        }
        if (MESSAGES) {
            message("Closest point is ", points.indexOf(pNearest));
        }
        /*if (DEBUG) {
            debugView(pNearest, "addInteriorPoint: closest point");
        }*/
        /* find face containing p, starting at pNearest */
        walk = findFace(pNearest.he, p);
        if (MESSAGES) {
            message(
                    "Point is within face with halfedge %d.",
                    halfEdges.indexOf(walk.he));
        }
        points.add(p);
        if (walk.status == FaceWalk.COINCIDENT) {
            splitEdge(p, walk.he);
        } else {
            splitFace(p, walk.he);
        }
        updateDelaunay();
        return p;
    }

    public HalfEdge addConstraint(Point pStart, Point pEnd) throws Exception {
        assert points.contains(pStart) : error(E_MISSING);
        assert points.contains(pEnd) : error(E_MISSING);
        assert pStart != pEnd : error("Identical points!");
        assert !coincident(pStart, pEnd) : error("Coincident points!");
        
        int i;
        Point pSearch0, pSearch1;
        HalfEdge heSearch, heStart, heStartPrev;
        FaceWalk walk;

        if (MESSAGES) {
            message(
                    "Constraining %d -> %d.",
                    points.indexOf(pStart),
                    points.indexOf(pEnd));
        }
        /* find the halfedge at pStart that lies on or below the constraint */
        walk = startFaceWalk(pStart, pEnd);
        /* check for trivial condition where the edge already exists */
        if (walk.status == FaceWalk.COINCIDENT) {
            //return constrainEdge(walk.he);
            if (constrainEdge(walk.he)) {
                return walk.he;
            } else
                return null;
        }
        /* clear edges that intersect the constraint */
        heStart = walk.he;
        heStartPrev = findPrevious(heStart);
        heSearch = heStart.next;
        for (i = 0; i <= halfEdges.size(); i++) {
            pSearch0 = heSearch.origin;
            pSearch1 = heSearch.next.origin;
            /*if (DEBUG) {
                debugView(heSearch, "addConstraint: walking");
            }*/
            /* check for termination */
            if (pSearch1 == pEnd) {
                if (MESSAGES) {
                    message(
                            "Found constraint end at halfedge %d.",
                            halfEdges.indexOf(heSearch));
                }
                break;
            }
            assert !coincident(pSearch1, pStart) :
                    error(E_COINCIDENT, pSearch1, pStart);
            assert !coincident(pSearch1, pEnd) :
                    error(E_COINCIDENT, pSearch1, pEnd);
            /* check for collinearity */
            //if (between(pStart,pEnd,pSearch1)) {
            /* split the constraint in two with pSearch1 as the midpoint */
 /*addConstraintEdge(heStart,heSearch.next,heStartPrev,heSearch);
                return addConstraint(pSearch1,pEnd);
            }*/
 /* check for intersection */
            if (intersect(pStart, pEnd, pSearch0, pSearch1)) {
                assert !heSearch.isType(HalfEdge.BOUNDARY) :
                        error("Constraint crosses boundary edge!");
                if (heSearch.isType(HalfEdge.AUXILARY)) {
                    removeEdge(heSearch);
                    heSearch = heSearch.sibling;
                } else if (heSearch.isType(HalfEdge.CONSTRAINT)) {
                    if (MESSAGES) {
                        message(
                                "Constraint-constraint intersection found.");
                    }
                    Point2d p = intersection(pStart, pEnd, pSearch0, pSearch1);
                    splitConstraint(heSearch, p);
                    addConstraintEdge(heStart, heSearch.next, heStartPrev, heSearch);
                    /* reset the starting point */
                    heSearch = heSearch.sibling;
                    heStart = heSearch;
                    heStartPrev = findPrevious(heStart);
                    pStart = heSearch.origin;
                }
            }
            heSearch = heSearch.next;
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED, pStart, pEnd);
        HalfEdge heAdd = addConstraintEdge(heStart, heSearch.next, heStartPrev, heSearch);
        updateDelaunay();
        return heAdd;
    }

    private HalfEdge addConstraintEdge(
            HalfEdge he1,
            HalfEdge he2,
            HalfEdge he1prev,
            HalfEdge he2prev) {
        assert he1prev.next == he1 : error(E_HALFEDGE);
        assert he2prev.next == he2 : error(E_HALFEDGE);

        HalfEdge heAdd = addEdge(he1, he2, he1prev, he2prev);
        constrainEdge(heAdd);
        fillEdgeVisiblePolygon(heAdd);
        fillEdgeVisiblePolygon(heAdd.sibling);
        
        return heAdd;
    }

    /******************************************************************************/
    /* Polygon filling methods
/******************************************************************************/
    protected void fillQuadrilateral(HalfEdge he1) {
        HalfEdge he2, he3, he4;

        he2 = he1.next;
        he3 = he2.next;
        he4 = he3.next;
        assert he4.next == he1;

        if (orient(he1.origin, he3.origin, he2.origin)
                * orient(he1.origin, he3.origin, he4.origin) < 0) {
            addEdge(he1, he3, he4, he2);
        } else {
            addEdge(he2, he4, he1, he3);
        }
    }

    protected ArrayList<HalfEdge> constructPolygon(HalfEdge he) {
        assert halfEdges.contains(he) : error(E_MISSING);

        int i;
        HalfEdge heSearch;
        ArrayList<HalfEdge> polygon = new ArrayList(TYPICAL_POLYGON_SIZE);

        polygon.add(he);
        heSearch = he.next;
        for (i = 0; i <= halfEdges.size(); i++) {
            if (heSearch == he) {
                break;
            }
            polygon.add(heSearch);
            /*if (DEBUG) {
                debugView(heSearch, "constructPolygon: next");
            }*/
            heSearch = heSearch.next;
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED, he, he.origin);

        return polygon;
    }

    protected void fillGeneralPolygon(HalfEdge he) {
        fillGeneralPolygon(constructPolygon(he));
    }

    protected void fillGeneralPolygon(ArrayList<HalfEdge> polygon) {
        fillGeneralPolygonRecurse(polygon);
        delaunayQueue.addAll(polygon);
    }

    private void fillGeneralPolygonRecurse(ArrayList<HalfEdge> polygon) {
        assert polygon.size() >= 3 : error("Illegal size!");

        int n, s;
        Point p0, p1, p2;
        HalfEdge heTest0, heTest1, heAdd;

        /* Assumes a Jordan (simple) polygon with n>3 sides!
         * (i.e. no sides intersect) */
        if (MESSAGES) {
            message(
                    "Filling polygon with %d sides.",
                    polygon.size());
        }
        /*if (DEBUG) {
            debugView(polygon.get(0), "fillPolygon: start");
        }*/
        s = polygon.size();
        if (s > 3) {
            /* A Jordan polygon always has two non-overlapping ears.
             * We iterate over all possible ear edges,
             * i.e. those between vertices i and i+2 in the polygon. */
            p0 = p1 = p2 = null;
            heTest0 = null;
            n = 0;
            edgeWalk:
            for (int i = 0; i < s; i++) {
                n = i;
                heTest0 = polygon.get(i);
                p0 = heTest0.origin;
                p1 = heTest0.next.origin;
                p2 = polygon.get((i + 2) % s).origin;
                // check that the ear edge p0->p2 lies strictly
                // inside the polygon, i.e. to the left of p0->p1
                if (orient(p0, p1, p2) > 0 && (!between(p0, p2, p1))) {
                    // check for intersections or points that lie too
                    // close to the ear edge
                    heTest1 = heTest0.next.next;
                    for (int j = 0; j < (s - 3); j++) {
                        if (intersectProper(
                                p0,
                                p2,
                                heTest1.origin,
                                heTest1.next.origin)) {
                            continue edgeWalk;
                        }
                        heTest1 = heTest1.next;
                    }
                    break;
                }
            }
            heAdd = addHalfEdge(p0, p2);
            delaunayQueue.add(heAdd);
            // link halfedges in the ear
            heAdd.sibling.next = heTest0;
            polygon.get((n + 1) % s).next = heAdd.sibling;
            // link halfedges in the remaining polygon of size s-1
            heAdd.next = polygon.get((n + 2) % s);
            polygon.get((n + s - 1) % s).next = heAdd;
            if (s > 4) {
                ArrayList<HalfEdge> polygon0 = new ArrayList();
                for (int j = 0; j < (s - 1); j++) {
                    polygon0.add(heAdd);
                    heAdd = heAdd.next;
                }
                fillGeneralPolygonRecurse(polygon0);
            }
        }
        if (TEST) {
            test();
        }
    }

    protected void fillEdgeVisiblePolygon(HalfEdge he) {
        ArrayList<HalfEdge> polygon = constructPolygon(he);
        fillEdgeVisiblePolygonRecurse(polygon);
        delaunayQueue.addAll(polygon);
    }

    private void fillEdgeVisiblePolygonRecurse(ArrayList<HalfEdge> polygon) {
        assert polygon.size() >= 3 : error("Illegal size!");

        int i, c, s;
        Point pa, pb, pc;
        HalfEdge heAdd;

        if (MESSAGES) {
            message(
                    "Filling polygon with %d sides.",
                    polygon.size());
        }
        /*if (DEBUG) {
            debugView(polygon.get(0), "fillPolygon: start");
        }*/
        s = polygon.size();
        if (s > 3) {
            pa = polygon.get(0).origin;
            pb = polygon.get(1).origin;
            pc = polygon.get(2).origin;
            c = 2;
            for (i = 3; i < s; i++) {
                Point p = polygon.get(i).origin;
                if (incircle(pa, pb, pc, p) > 0) {
                    pc = p;
                    c = i;
                }
            }
            /*if (DEBUG) {
                debugView(polygon.get(c),
                        "fillPolygon: largest circumcircle");
            }*/
            /* add edge pa -> pc */
            if (c < (s - 1)) {
                heAdd = addEdge(
                        polygon.get(0),
                        polygon.get(c),
                        polygon.get(s - 1),
                        polygon.get(c - 1));
                fillEdgeVisiblePolygonRecurse(constructPolygon(heAdd));
            }
            /* add edge pb -> pc */
            if (c > 2) {
                heAdd = addEdge(
                        polygon.get(1),
                        polygon.get(c - 1).next,
                        polygon.get(0),
                        polygon.get(c - 1));
                fillEdgeVisiblePolygonRecurse(constructPolygon(heAdd.sibling));
            }
        }
        if (TEST) {
            test();
        }
    }

    /******************************************************************************/
    /* Splitting methods
/******************************************************************************/

 /*
     * Splits the two faces sharing edge he into four faces by inserting
     * point p.
     */
    private void splitEdge(Point p, HalfEdge he) {
        HalfEdge he1, he2, he3;
        HalfEdge heAdd1, heAdd2, heAdd3;

        if (MESSAGES) {
            message("Splitting edge %d.", halfEdges.indexOf(he));
        }
        assert !he.isType(HalfEdge.BOUNDARY) :
                error("Attempting to split boundary edge!");
        //he1 = he.next.next;
        he1 = findPrevious(he);
        he2 = he.sibling.next;
        //he3 = he2.next;
        he3 = findPrevious(he.sibling);
        // split the halfedge
        he.origin = p;
        p.he = he;
        // add halfedges
        heAdd1 = addHalfEdge(p, he1.origin);
        heAdd2 = addHalfEdge(p, he2.origin);
        heAdd3 = addHalfEdge(p, he3.origin);
        // link halfedges
        heAdd1.next = he1;
        heAdd2.next = he2;
        heAdd3.next = he3;
        he.next.next = heAdd1.sibling;
        he1.next = heAdd2.sibling;
        he2.next = heAdd3.sibling;
        heAdd1.sibling.next = he;
        heAdd2.sibling.next = heAdd1;
        heAdd3.sibling.next = heAdd2;
        he.sibling.next = heAdd3;
        // update the point->halfedge pointers
        updateHalfEdge(he2);
        // add halfedges to delaunay test
        delaunayQueue.add(he);
        delaunayQueue.add(he1);
        delaunayQueue.add(he2);
        delaunayQueue.add(he3);
    }

    /*
     * Insert point p into the face with halfedge he1, splitting it into
     * three faces.
     */
    private void splitFace(Point p, HalfEdge he1) {
        assert halfEdges.contains(he1) : error(E_MISSING);

        HalfEdge he2, he3;
        HalfEdge heAdd1, heAdd2, heAdd3;

        if (MESSAGES) {
            message("Adding interior point inside face.");
        }
        he2 = he1.next;
        he3 = he2.next;
        /* add new halfedges */
        heAdd1 = addHalfEdge(p, he1.origin);
        heAdd2 = addHalfEdge(p, he2.origin);
        heAdd3 = addHalfEdge(p, he3.origin);
        /* link half edges */
        p.he = heAdd1;
        heAdd1.next = he1;
        heAdd2.next = he2;
        heAdd3.next = he3;
        he1.next = heAdd2.sibling;
        he2.next = heAdd3.sibling;
        he3.next = heAdd1.sibling;
        heAdd1.sibling.next = heAdd3;
        heAdd3.sibling.next = heAdd2;
        heAdd2.sibling.next = heAdd1;
        /* add halfedges to delaunay test */
        delaunayQueue.add(heAdd1);
        delaunayQueue.add(heAdd2);
        delaunayQueue.add(heAdd3);
        delaunayQueue.add(he1);
        delaunayQueue.add(he2);
        delaunayQueue.add(he3);
    }

    private void splitConstraint(HalfEdge he, Point2d p) {
        int i;
        Point p0;
        HalfEdge he0, heTest;

        if (MESSAGES) {
            message(
                    "Splitting constraint edge %d.",
                    halfEdges.indexOf(he));
        }
        assert !he.isType(HalfEdge.BOUNDARY) :
                error("Attempting to split a boundary edge!");
        // add point
        p0 = new Point(p);
        points.add(p0);
        if (MESSAGES) {
            message(
                    "Adding constraint intersection point %d.",
                    points.indexOf(p0));
        }
        // add halfedge
        he0 = addHalfEdge(p0, he.sibling.origin);
        he0.constrain();
        he0.sibling.constrain();
        he.sibling.origin = p0;
        // update point->halfedge pointers
        p0.he = he0;
        updateHalfEdge(he0.sibling);
        // link halfedges
        he0.next = he.next;
        he.next = he0;
        he0.sibling.next = he.sibling;
        // find halfedge pointing to he.sibling
        heTest = he.sibling;
        for (i = 0; i <= halfEdges.size(); i++) {
            if (heTest.next == he.sibling) {
                heTest.next = he0.sibling;
                break;
            }
            heTest = heTest.next;
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED);
    }

    /******************************************************************************/
    /* Removal methods
/******************************************************************************/
    /*public void removeBoundaryPoint(BPoint bp) {
        BPoint bp1, bp2;
        /* The boundary is oriented clockwise, opposite of the halfedge
         * orientation (counter-clockwise). Therefore, the "previous" point
         * to bp is actually the next point along the boundary. */
        //removeBoundaryPoint(bp, bp.next);
        /* relink boundary */
        /*bp1 = bp.prev;
        bp2 = bp.next;
        bp1.next = bp2;
        bp2.prev = bp1;
    }*/

    /*private void removeBoundaryPoint(Point p, Point pPrev) {
        int i;
        Point pNext;
        HalfEdge he;

        if (MESSAGES) {
            message(
                    "Removing boundary point %d.",
                    points.indexOf(p));
        }
        pNext = p.he.next.origin;
        he = p.he.getPrev();
        deleteQueue.clear();
        for (i = 0; i <= halfEdges.size(); i++) {
            if (he.isType(HalfEdge.BOUNDARY)) {
                break;
            }
            if (orient(pPrev, pNext, he.origin) <= 0) {
                deleteQueue.add(he.origin);
            }
            removeEdge(he);
            he = p.he.getPrev();
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED);
        if (MESSAGES) {
            message(
                    "Performing flood delete to remove interior points.");
        }
        Point[] bounds = new Point[3];
        bounds[0] = pPrev;
        bounds[1] = pNext;
        bounds[2] = p;
        floodDelete(bounds);
        pPrev.he.next = p.he.next;
        halfEdges.remove(p.he);
        p.he = null;
        points.remove(p);
        p.setType(Point.DELETED);
        /* stitch the polygon back together */
        /*fillEdgeVisiblePolygon(pPrev.he);
        updateDelaunay();
        nBoundary--;
    }*/

    public void removeInteriorPoint(Point p) {
        assert points.contains(p) : error(E_MISSING);

        int i;
        Point p1, p2, p3;
        HalfEdge heSearch, heFlip;
        LinkedList<HalfEdge> star = new LinkedList();

        if (MESSAGES) {
            message("Removing interior point.");
        }
        /* construct the star of halfedges around p */
        heSearch = p.he;
        for (i = 0; i <= halfEdges.size(); i++) {
            star.add(heSearch);
            heSearch = heSearch.next.next.sibling;
            if (heSearch == p.he) {
                break;
            }
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED);
        assert star.size() >= 3;
        if (star.size() == 3) {
            for (HalfEdge he : star) {
                removeEdge(he);
            }
        } else {
            while (star.size() > 4) {
                heFlip = star.pop();
                p1 = heFlip.sibling.origin;
                p2 = heFlip.next.next.origin;
                p3 = heFlip.sibling.next.next.origin;
                if (orient(p2, p3, p) * orient(p2, p3, p1) < 0) {
                    flipEdge(heFlip);
                } else {
                    star.add(heFlip);
                }
            }
            assert star.size() == 4;
            heSearch = star.get(0).next;
            for (HalfEdge he : star) {
                removeEdge(he);
            }
            fillQuadrilateral(heSearch);
        }
        p.he = null;
        points.remove(p);
        p.setType(Point.DELETED);
        updateDelaunay();
    }

    /*
     * Removes an interior point without refilling.
     *
     * @params p
     */
    private void removePoint(Point p) {
        assert points.contains(p) : error(E_MISSING);
        assert !p.isType(Point.DELETED) : error("Re-removing point!");

        int i;
        HalfEdge he = p.he;

        if (MESSAGES) {
            message("Removing point %d.", points.indexOf(p));
        }
        /*if (DEBUG) {
            debugView(he, "removePoint: he");
        }*/

        for (i = 0; i <= halfEdges.size(); i++) {
            assert he.origin == p : error("Mismatched halfedge!");
            removeEdge(he);
            if (p.he == null) {
                break;
            }
            he = he.sibling.next;
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED);

        points.remove(p);
        p.setType(Point.DELETED);
        p.he = null;
    }

    private void removeEdge(HalfEdge he) {
        assert halfEdges.contains(he) : error(E_MISSING);
        assert !he.isType(HalfEdge.BOUNDARY) : error(E_TYPE);

        HalfEdge hePrev, heSibPrev;

        /*if (DEBUG) {
            debugView(he, "removeEdge: he");
        }*/
        if (MESSAGES) {
            message("Removing edge %d.", halfEdges.indexOf(he));
        }

        hePrev = findPrevious(he);
        heSibPrev = findPrevious(he.sibling);
        /* remove halfedges */
        halfEdges.remove(he);
        halfEdges.remove(he.sibling);
        delaunayQueue.remove(he);
        delaunayQueue.remove(he.sibling);
        /* cache the constraints */
        if (he.isType(HalfEdge.CONSTRAINT)) {
            removedConstraints.add(he.next.origin);
        }
        /* update point->halfedge pointers */
        if (he.sibling == hePrev) {
            /* this was the last halfedge eminating from he.origin */
            /*if (DEBUG) {
                debugView(he.origin, "removeEdge: orphan point");
            }*/
            he.origin.he = null;
            updateHalfEdge(he.next);
        } else if (he.next == he.sibling) {
            /* this was the last halfedge eminating from he.sibling.origin */
            /*if (DEBUG) {
                debugView(he.next.origin, "removeEdge: orphan point");
            }*/
            he.next.origin.he = null;
            updateHalfEdge(he.sibling.next);
        } else {
            updateHalfEdge(he.next);
            updateHalfEdge(he.sibling.next);
        }
        /* relink halfedges */
        hePrev.next = he.sibling.next;
        heSibPrev.next = he.next;
    }

    /*private void floodDelete(Point[] bounds) {
        assert bounds.length >= 3 : error("Illegal bounds!");

        int i;
        int[] types;
        Boolean inside;
        Point p1, p2;
        HalfEdge heTest;
        GeneralPath boundsPath = new GeneralPath();

        types = new int[bounds.length];
        for (i = 0; i < bounds.length; i++) {
            types[i] = bounds[i].type;
        }
        for (Point p : bounds) {
            p.type = Point.BOUNDS;
        }
        boundsPath.moveTo(bounds[0].x, bounds[0].y);
        for (i = 1; i < bounds.length; i++) {
            boundsPath.lineTo(bounds[i].x, bounds[i].y);
        }
        boundsPath.closePath();
        while (deleteQueue.size() > 0) {
            p1 = deleteQueue.pop();
            if (boundsPath.contains(p1.x, p1.y) && !p1.isType(Point.BOUNDS)) {
                heTest = p1.he;
                for (i = 0; i <= halfEdges.size(); i++) {
                    if (p1.he == null) {
                        break;
                    }
                    p2 = heTest.next.origin;
                    if (!p2.isType(Point.BOUNDS)) {
                        inside = true;
                        for (int j = 1; j < bounds.length; j++) {
                            if (intersect(p1, p2, bounds[j - 1], bounds[j])) {
                                /* check if p2 lies strictly outside the bounds
                                 * (i.e. is not colinear with the bounds) */
                                /*if (orient(bounds[j - 1], bounds[j], p2) != 0) {
                                    inside = false;
                                }
                                break;
                            }
                        }
                        if (inside) {
                            deleteQueue.add(p2);
                        }
                    }
                    removeEdge(heTest);
                    heTest = heTest.sibling.next;
                }
                assert i < halfEdges.size() : error(E_EXHAUSTED);
                points.remove(p1);
                p1.type = Point.DELETED;
                p1.he = null;
            }
        }
        for (i = 0; i < bounds.length; i++) {
            bounds[i].type = types[i];
        }
    }*/

    private void clearNewBoundaryEdge(Point pStart, Point pEnd) {
        assert points.contains(pStart) : error(E_MISSING);
        assert points.contains(pEnd) : error(E_MISSING);
        assert pStart != pEnd : error(E_IDENTICAL);

        FaceWalk walk;

        if (MESSAGES) {
            message("Clearing new boundary edge.");
        }
        walk = startFaceWalk(pStart, pEnd);
        // check for trivial case
        if (walk.status == FaceWalk.COINCIDENT) {
            removeEdge(walk.he);
        } else {
            int i;
            Point pSearch0, pSearch1;
            HalfEdge heSearch;

            heSearch = walk.he;
            /*if (DEBUG) {
                debugView(
                        heSearch,
                        "clearNewBoundaryEdge: start face walk");
            }*/
            for (i = 0; i <= halfEdges.size(); i++) {
                /*if (DEBUG) {
                    debugView(heSearch, "clearNewBoundaryEdge: walking");
                }*/
                pSearch0 = heSearch.origin;
                pSearch1 = heSearch.next.origin;
                // check for termination
                if (pSearch1 == pEnd) {
                    break;
                }
                // check for collinearity
                if (between(pStart, pEnd, pSearch1)) {
                    if (pSearch1.isType(Point.BOUNDARY)) {
                        heSearch = heSearch.next;
                    } else {
                        deleteQueue.add(pSearch1);
                        heSearch = heSearch.sibling.next;
                    }
                } // check for intersection
                else if (intersectProper(pStart, pEnd, pSearch0, pSearch1)) {
                    removeEdge(heSearch);
                    deleteQueue.add(pSearch0);
                    heSearch = heSearch.sibling.next;
                } else {
                    heSearch = heSearch.next;
                }
            }
            assert i < halfEdges.size() : error(E_EXHAUSTED);
        }
    }

    /******************************************************************************/
    /* Update methods
/******************************************************************************/
    protected final void updateHalfEdge(HalfEdge he) {
        assert halfEdges.contains(he) : error(E_MISSING);
        if (he.origin.isType(Point.INTERIOR)) {
            he.origin.he = he;
        }
    }

    public void updateDelaunay() {
        Point2d p1, p2, p3, p4;

        if (MESSAGES) {
            message(
                    "Testing Delaunay queue with %d halfedges.",
                    delaunayQueue.size());
        }
        while (delaunayQueue.size() > 0) {
            HalfEdge he = delaunayQueue.pop();
            if (he.isType(HalfEdge.AUXILARY)) {
                /*if (DEBUG) {
                    debugView(he, "updateDelaunay: test edge");
                }*/
                p1 = he.next.origin;
                p2 = he.next.next.origin;
                p3 = he.origin;
                p4 = he.sibling.next.next.origin;
                if (incircle(p1, p2, p3, p4) > 0 || incircle(p3, p4, p1, p2) > 0) {
                    flipEdge(he);
                    /*if (DEBUG) {
                        debugView(he, "updateDelaunay: flipped");
                    }*/
                }
            }
        }
        if (TEST) {
            test();
        }
    }

    public void updateDelaunayAll() {
        delaunayQueue.addAll(halfEdges);
        updateDelaunay();
    }

    public void updateInteriorPoint(Point p) {
        assert points.contains(p) : error(E_MISSING);
        initRemoveConstraints(p);
        removeInteriorPoint(p);
        p = addInteriorPoint(p);
        p.setType(Point.INTERIOR);
        restoreConstraints(p);
        updateDelaunay();
    }

    /*    public boolean updateBoundaryPoint(BPoint bp, Point2d pNew)
    {
        assert points.contains(bp) : error(E_MISSING);

        if (between(bp.prev,bp,pNew)) {
            return updateBoundaryPointAlong(bp,bp.he,pNew);
        }
        if (between(bp,bp.next,pNew)) {
            return updateBoundaryPointAlong(bp,bp.next.he,pNew);
        }
        return false;
    }*/
    /*private boolean updateBoundaryPointAlong(
            BPoint bp,
            HalfEdge he,
            Point2d pNew) {
        BPoint bpNew;

        bpNew = new BPoint(pNew);
        addBoundaryPoint(bpNew, he);
        removeBoundaryPoint(bp, bp.next);
        bp.set(bpNew);
        bp.setType(Point.BOUNDARY);
        bp.prev = bpNew.prev;
        bp.prev.next = bp;
        bp.next = bpNew.next;
        bp.next.prev = bp;
        bp.he = bpNew.he;
        bp.he.origin = bp;

        return true;
    }*/

    public void updateBoundaryPointOutside(Point p) {
        assert points.contains(p) : error(E_MISSING);

        int i;
        HalfEdge he;

        if (MESSAGES) {
            message(
                    "Updating boundary point outside existing boundary.");
        }

        initRemoveConstraints(p);
        he = p.he.next.next;
        assert he.next == p.he : error(E_POLYGON);

        for (i = 0; i <= halfEdges.size(); i++) {
            if (he.isType(HalfEdge.BOUNDARY)) {
                break;
            }
            removeEdge(he.sibling);
            /* walk around p counter-clockwise */
            he = he.sibling.next.next;
        }
        assert i < halfEdges.size() : E_EXHAUSTED;

        fillGeneralPolygon(p.he);
        restoreConstraints(p);
        updateDelaunay();
        if (TEST) {
            test();
        }
    }

    /*public boolean updateBoundaryPointInside(Point p, Point pPrev, Point pTemp) throws Exception {
        assert points.contains(p) : error(E_MISSING);
        assert points.contains(pPrev) : error(E_MISSING);

        int i;
        Point pNext;
        HalfEdge heTest;

        if (MESSAGES) {
            message(
                    "Updating boundary point inside existing boundary.");
        }

        // locate the previous and next boundary points
        pNext = p.he.next.origin;
        // check for coincidence
        /*if (coincident(p,pTemp)) {
            p.x = pTemp.x;
            p.y = pTemp.y;
            return updateBoundaryPointAlong(p,pPrev);
        }*/
        // relocate p to ensure that the quadrilateral (p,pPrev,pTemp,pNext)
        // is a simple polygon
        /*if (intersectProper(pPrev, p, pNext, pTemp)) {
            if (MESSAGES) {
                message(
                        "Moving point to ensure simple quadrilateral.");
            }
            Point2d pp = intersection(pPrev, p, pNext, pTemp);
            p.x = pp.x + 0.5f * (pPrev.x - pp.x);
            p.y = pp.y + 0.5f * (pPrev.y - pp.y);
            updateBoundaryPointOutside(p);
        } else if (intersectProper(pNext, p, pPrev, pTemp)) {
            if (MESSAGES) {
                message(
                        "Moving point to ensure simple quadrilateral.");
            }
            Point2d pp = intersection(pNext, p, pPrev, pTemp);
            p.x = pp.x + 0.5f * (pNext.x - pp.x);
            p.y = pp.y + 0.5f * (pNext.y - pp.y);
            updateBoundaryPointOutside(p);
        }
        // insert an interior point where the new boundary point will
        // eventually be
        pTemp = addInteriorPoint(pTemp);
        if (MESSAGES) {
            message("Added temporary interior point.");
        }
        // flip edge pPrev->pNext if it exists
        heTest = pPrev.he;
        for (i = 0; i <= halfEdges.size(); i++) {
            if (heTest.next.origin == pNext) {
                if (MESSAGES) {
                    message("Flipping edge pPrev->pNext.");
                }
                flipEdge(heTest);
                break;
            }
            heTest = heTest.next.next;
            if (heTest.isType(HalfEdge.BOUNDARY)) {
                break;
            }
            heTest = heTest.sibling;
            if (heTest == pPrev.he) {
                break;
            }
        }
        assert i < halfEdges.size() : E_EXHAUSTED;
        // sweep along the new boundary edges to remove intersecting edges
        deleteQueue.clear();
        clearNewBoundaryEdge(pPrev, pTemp);
        clearNewBoundaryEdge(pTemp, pNext);
        /* flood fill to remove points and edges inside the quad */
        /*if (MESSAGES) {
            message(
                    "Performing flood delete to remove outside points.");
        }
        Point[] bounds = new Point[4];
        bounds[0] = pPrev;
        bounds[1] = pTemp;
        bounds[2] = pNext;
        bounds[3] = p;
        floodDelete(bounds);
        if (MESSAGES) {
            message("Removing placeholder interior point.");
        }
        removePoint(pTemp);
        if (MESSAGES) {
            message("Relocating boundary point and boundary edges.");
        }
        p.x = pTemp.x;
        p.y = pTemp.y;
        fillGeneralPolygon(p.he);
        updateDelaunay();
        return true;
    }*/

    private boolean validateBoundary(BPoint bp) {
        BPoint bpTest;
        bpTest = bp.next;
        for (int j = 3; j < nBoundary; j++) {
            if (intersect(
                    bp.prev,
                    bp,
                    bpTest,
                    bpTest.next)) {
                return false;
            }
            bpTest = bpTest.next;
        }
        bpTest = bp.prev;
        for (int j = 3; j < nBoundary; j++) {
            if (intersect(
                    bp,
                    bp.next,
                    bpTest.prev,
                    bpTest)) {
                return false;
            }
            bpTest = bpTest.prev;
        }
        return true;
    }

    private boolean constrainEdge(HalfEdge he) {
        assert halfEdges.contains(he) : error(E_MISSING);
        if (MESSAGES) {
            message("Constraining edge %d.", halfEdges.indexOf(he));
        }
        if (he.isType(HalfEdge.BOUNDARY)) {
            if (MESSAGES) {
                message("Ignoring boundary edge constraint.");
            }
            return false;
        }
        he.constrain();
        he.sibling.constrain();
        return true;
    }

    public void constrainAllEdges() {
        for (HalfEdge he : halfEdges) {
            if (!he.isType(HalfEdge.BOUNDARY)) {
                he.constrain();
            }
        }
    }

    public void initRemoveConstraints(Point p) {
        assert points.contains(p) : error(E_MISSING);
        removeConstraintPeg = p;
        removedConstraints.clear();
    }

    public void restoreConstraints(Point p) {
        assert points.contains(p) : error(E_MISSING);
        assert p == removeConstraintPeg : error("Race condition!");
        removedConstraints.remove(p);
        for (Point p0 : removedConstraints) {
            if (p0.isType(Point.DELETED)) {
                continue;
            }
            try {
                addConstraint(p, p0);
            } catch (Exception e) {
                if (MESSAGES) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error adding constraint: ", e);
                }
            }
        }
        removeConstraintPeg = null;
    }

    private boolean flipEdge(HalfEdge he) {
        assert halfEdges.contains(he) : error(E_MISSING);
        assert !he.isType(HalfEdge.BOUNDARY) : error(E_TYPE);

        HalfEdge he1, he2, he3, he4;

        if (MESSAGES) {
            message("Flipping edge %d.", halfEdges.indexOf(he));
        }
        // locate halfedges
        he1 = he.next;
        he2 = he1.next;
        he3 = he.sibling.next;
        he4 = he3.next;
        // flip the origins
        he.origin = he2.origin;
        he.sibling.origin = he4.origin;
        // update point->halfedge pointers
        updateHalfEdge(he3);
        updateHalfEdge(he1);
        // link halfedges
        he1.next = he;
        he.next = he4;
        he4.next = he1;
        he3.next = he.sibling;
        he.sibling.next = he2;
        he2.next = he3;
        // add halfedges to the delaunay test
        delaunayQueue.add(he1);
        delaunayQueue.add(he2);
        delaunayQueue.add(he3);
        delaunayQueue.add(he4);
        return true;
    }

    /******************************************************************************/
    /* Locator methods
/******************************************************************************/
    /**
     * This brute force approach works for *any* non-intersecting boundary,
     * concave or convex. If the boundary is guaranteed to be a convex, a
     * smarter face-walking algorithm could be used.
     */
    public FaceWalk findFaceBruteForce(HalfEdge heStart, Point p) {
        float[] ccw = new float[3];
        HalfEdge he1, he2;

        clearFlags(HalfEdge.FLAG_ALGORITHM);
        for (HalfEdge he0 : halfEdges) {
            if (he0.isFlagged(HalfEdge.FLAG_ALGORITHM)) {
                continue;
            }
            he1 = he0.next;
            he2 = he1.next;
            assert he2.next == he0 : error("Found non-face!");
            he0.flag(HalfEdge.FLAG_ALGORITHM);
            he1.flag(HalfEdge.FLAG_ALGORITHM);
            he2.flag(HalfEdge.FLAG_ALGORITHM);
            ccw[0] = orient(he0.origin, he1.origin, p);
            if (ccw[0] < 0) {
                continue;
            }
            ccw[1] = orient(he1.origin, he2.origin, p);
            if (ccw[1] < 0) {
                continue;
            }
            ccw[2] = orient(he2.origin, he0.origin, p);
            if (ccw[2] < 0) {
                continue;
            }
            if (ccw[0] == 0) {
                return new FaceWalk(he0, FaceWalk.COINCIDENT);
            }
            if (ccw[1] == 0) {
                return new FaceWalk(he1, FaceWalk.COINCIDENT);
            }
            if (ccw[2] == 0) {
                return new FaceWalk(he2, FaceWalk.COINCIDENT);
            }
            return new FaceWalk(he0, FaceWalk.CLOCKWISE);
        }
        /*if (DEBUG) {
            error("Exhausted faces!");
        }*/
        return null;
    }

    /**
     * A slightly smarter face walk routine that resorts to brute force
     * only when it gets confused by an concave boundary.
     */
    public FaceWalk findFace(HalfEdge heStart, Point p) {
        int i;
        float[] ccw = new float[3];
        HalfEdge he0, he1, he2;
        LinkedList<HalfEdge> queue = new LinkedList();

        clearFlags(HalfEdge.FLAG_ALGORITHM);
        queue.add(heStart);
        queue.addAll(halfEdges);
        he0 = queue.pop();
        for (i = 0; i <= halfEdges.size(); i++) {
            if (he0.isFlagged(HalfEdge.FLAG_ALGORITHM)) {
                he0 = queue.pop();
                continue;
            }
            he1 = he0.next;
            he2 = he1.next;
            assert he2.next == he0 : error("Found non-face!");
            he0.flag(HalfEdge.FLAG_ALGORITHM);
            he1.flag(HalfEdge.FLAG_ALGORITHM);
            he2.flag(HalfEdge.FLAG_ALGORITHM);
            ccw[0] = orient(he0.origin, he1.origin, p);
            if (ccw[0] < 0) {
                if (he0.sibling == null) {
                    //not sure if this is right, but was receiving null siblings
                    return new FaceWalk(he0, FaceWalk.CLOCKWISE);
                } else {
                    he0 = he0.sibling;
                    continue;
                }
            }
            ccw[1] = orient(he1.origin, he2.origin, p);
            if (ccw[1] < 0) {
                if (he1.sibling == null) {
                    //not sure if this is right, but was receiving null siblings
                    return new FaceWalk(he0, FaceWalk.CLOCKWISE);
                } else {
                    he0 = he1.sibling;
                    continue;
                }
            }
            ccw[2] = orient(he2.origin, he0.origin, p);
            if (ccw[2] < 0) {
                if (he2.sibling == null) {
                    //not sure if this is right, but was receiving null siblings
                    return new FaceWalk(he0, FaceWalk.CLOCKWISE);
                } else {
                    he0 = he2.sibling;
                    continue;
                }
            }
            if (ccw[0] == 0) {
                return new FaceWalk(he0, FaceWalk.COINCIDENT);
            }
            if (ccw[1] == 0) {
                return new FaceWalk(he1, FaceWalk.COINCIDENT);
            }
            if (ccw[2] == 0) {
                return new FaceWalk(he2, FaceWalk.COINCIDENT);
            }
            return new FaceWalk(he0, FaceWalk.CLOCKWISE);
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED);
        /*if (DEBUG) {
            error("Exhausted faces!");
        }*/
        return null;
    }

    protected final HalfEdge findPrevious(HalfEdge he) {
        assert halfEdges.contains(he) : error(E_MISSING, he);

        int i;
        HalfEdge heSearch;

        heSearch = he.next;
        for (i = 0; i <= halfEdges.size(); i++) {
            /*if (DEBUG) {
                debugView(heSearch, "findPrevious: searching forward");
            }*/
            if (heSearch.next == he) {
                break;
            }
            heSearch = heSearch.next;
        }
        assert i < halfEdges.size() : error(E_EXHAUSTED, he);

        assert halfEdges.contains(heSearch) : error(E_MISSING, heSearch);

        return heSearch;
    }

    // robust with non-triangular regions
    FaceWalk startFaceWalk(Point pStart, Point pEnd) {
        assert points.contains(pStart) : error(E_MISSING);
        assert points.contains(pEnd) : error(E_MISSING);
        assert pStart != pEnd : error(E_IDENTICAL, pStart, pEnd);
        assert !coincident(pStart, pEnd) : error(E_COINCIDENT, pStart, pEnd);

        int i;
        float ccwTrailing, ccwLeading;
        Point pTrailing, pLeading;
        HalfEdge he;
        HalfEdge hePrev = null;
        
        he = pStart.he;
        pTrailing = he.next.origin;
        ccwTrailing = orient(pStart, pEnd, pTrailing);
        // special case for boundary starting points
        if (pStart.isType(Point.BOUNDARY)) {
            // check for coincidence with boundary
            if (pTrailing == pEnd) {
                return new FaceWalk(he, FaceWalk.COINCIDENT);
            }
            // check whether pStart->pEnd is within epsilon of the boundary
            assert ccwTrailing <= 0 :
                    error("Point lies outside boundary!", pEnd);
            // it is safe to assume that he is clockwise because of the above
            // assertion
            if (ccwTrailing == 0) {
                if (betweenProper(pStart, pEnd, pTrailing)
                        || betweenProper(pStart, pTrailing, pEnd)) {
                    return new FaceWalk(he, FaceWalk.CLOCKWISE);
                }
            }
        }
        for (i = 0; i <= halfEdges.size(); i++) {
            // this face may be a polygon, so search forward to find
            // the second test edge
            hePrev = findPrevious(he);
            pTrailing = he.next.origin;
            /*if (DEBUG) {
                debugView(pTrailing, "startFaceWalk: trailing point");
            }*/
            pLeading = hePrev.origin;
            /*if (DEBUG) {
                debugView(pLeading, "startFaceWalk: leading point");
            }*/
            // check for coincidence of either star edge
            if (pTrailing == pEnd) {
                return new FaceWalk(he, FaceWalk.COINCIDENT);
            } else if (pLeading == pEnd) {
                return new FaceWalk(hePrev, FaceWalk.COINCIDENT);
            }
            assert !coincident(pTrailing, pEnd) :
                    error(E_COINCIDENT, pTrailing, pEnd);
            assert !coincident(pLeading, pEnd) :
                    error(E_COINCIDENT, pLeading, pEnd);
            // check if the leading point is counter-clockwise/collinear and the
            // trailing point clockwise of pStart->pEnd
            ccwLeading = orient(pStart, pEnd, pLeading);
            if (ccwLeading >= 0
                    && ccwTrailing < 0) {
                return new FaceWalk(he, FaceWalk.CLOCKWISE);
            }
            ccwTrailing = ccwLeading;
            he = hePrev.sibling;
        }
        assert i < halfEdges.size() : E_EXHAUSTED;
        
        // return a failed walk at this point
        return new FaceWalk(null, FaceWalk.FAILED);
    }

    /******************************************************************************/
    /* Drawing methods
/******************************************************************************/
    /*public void drawPoints(Graphics2D g2) {
        synchronized (points) {
            for (Point p : points) {
                g2.setColor(p.getColor());
                drawPoint(g2, p);
            }
        }
    }

    public void drawLines(Graphics2D g2) {
        synchronized (halfEdges) {
            clearFlags(HalfEdge.FLAG_DRAW);
            for (HalfEdge he : halfEdges) {
                g2.setColor(he.getColor());
                if (DRAW_HALFEDGES) {
                    drawHalfEdge(g2, he);
                }
                if (he.isFlagged(HalfEdge.FLAG_DRAW)) {
                    continue;
                }
                drawLine(g2, he);
                he.flagEdge(HalfEdge.FLAG_DRAW);
            }
        }
    }

    public void drawLinesGray(Graphics2D g2) {
        synchronized (halfEdges) {
            clearFlags(HalfEdge.FLAG_DRAW);
            g2.setColor(grayColor);
            for (HalfEdge he : halfEdges) {
                if (he.isFlagged(HalfEdge.FLAG_DRAW)) {
                    continue;
                }
                drawLine(g2, he.origin, he.next.origin);
                he.flagEdge(HalfEdge.FLAG_DRAW);
            }
        }
    }

    public void drawBoundary(Graphics2D g2) {
        synchronized (halfEdges) {
            g2.setColor(HalfEdge.COLORS[(HalfEdge.BOUNDARY)]);
            for (HalfEdge he : halfEdges) {
                if (he.isType(HalfEdge.BOUNDARY)) {
                    drawLine(g2, he);
                }
            }
        }
    }

    protected void drawPoint(Graphics2D g2, Point2d p) {
        if (DRAW_PT_FULL) {
            g2.fillOval((int) p.x - 3, (int) p.y - 3, 5, 5);
            g2.drawOval((int) p.x - 6, (int) p.y - 5, 10, 10);
        } else {
            g2.fillOval((int) p.x, (int) p.y, 1, 1);
        }
        if (DRAW_PT_INDICES) {
            g2.drawString(
                    String.valueOf(points.indexOf(p)),
                    (int) p.x + 5,
                    (int) p.y + 5
            );
        }
    }

    protected void drawLine(Graphics2D g2, HalfEdge he) {
        Point2d p1 = he.origin;
        Point2d p2 = he.next.origin;
        g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
        if (DRAW_HE_INDICES) {
            int x1, y1, x2, y2;
            float x, y;
            Vector2d v = new Vector2d(p2.x - p1.x, p2.y - p1.y);
            v.normalize();
            v.scale(5);
            x = v.x - v.y;
            y = v.x + v.y;
            x1 = (int) (p1.x + x);
            y1 = (int) (p1.y + y);
            x2 = (int) (p2.x + x);
            y2 = (int) (p2.y + y);
            g2.drawString(String.valueOf(halfEdges.indexOf(he)),
                    (int) (0.5 * (x1 + x2)),
                    (int) (0.5 * (y1 + y2)));
        }
    }

    protected void drawLine(Graphics2D g2, Point2d p1, Point2d p2) {
        g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
    }

    protected void drawHalfEdge(Graphics2D g2, HalfEdge he) {
        drawHalfEdge(g2, he.next.origin, he.origin);
    }

    protected void drawHalfEdge(Graphics2D g2, Point2d p1, Point2d p2) {
        int x1, y1, x2, y2;
        Vector2d v = new Vector2d(p2.x - p1.x, p2.y - p1.y);
        v.scale(0.1f);
        x1 = x2 = (int) (p1.x + v.x);
        y1 = y2 = (int) (p1.y + v.y);
        v.normalize();
        v.scale(4);
        x2 += (int) (v.x + v.y);
        y2 += (int) (v.y - v.x);
        g2.drawLine(x1, y1, x2, y2);
    }

    public void drawDebug(Graphics2D g2) {
        if (DEBUG) {
            g2.setColor(highlightColor);
            for (Object obj : debugObjects) {
                if (obj instanceof Point) {
                    drawPoint(g2, (Point) obj);
                } else if (obj instanceof HalfEdge) {
                    drawHalfEdge(g2, (HalfEdge) obj);
                    drawLine(g2, (HalfEdge) obj);
                } else if (obj instanceof Edge) {
                    Edge e = (Edge) obj;
                    drawHalfEdge(g2, e.p2, e.p1);
                    drawLine(g2, e.p1, e.p2);
                }
            }
        }
    }*/

    /******************************************************************************/
    /* Computational geometry methods and predicates
/******************************************************************************/
    // compute the normalized projection of ac onto ab
    private final static float projNorm(
            Point2d a,
            Point2d b,
            Point2d c) {
        float x1, x2, y1, y2;
        x1 = b.x - a.x;
        x2 = c.x - a.x;
        y1 = b.y - a.y;
        y2 = c.y - a.y;
        return (x1 * x2 + y1 * y2) / (x1 * x1 + y1 * y1);
    }

    // compute the magnitude of the cross product of ab and ac
    private final static float cross(
            Point2d a,
            Point2d b,
            Point2d c) {
        float x1, x2, y1, y2;
        x1 = b.x - a.x;
        x2 = c.x - a.x;
        y1 = b.y - a.y;
        y2 = c.y - a.y;
        return x1 * y2 - y1 * x2;
    }

    // compute the squared perpendicular distance of c onto ab
    private final static float perpDistSq(
            Point2d a,
            Point2d b,
            Point2d c) {
        float x1, x2, y1, y2, cross, lenSq;
        x1 = b.x - a.x;
        x2 = c.x - a.x;
        y1 = b.y - a.y;
        y2 = c.y - a.y;
        cross = x1 * y2 - y1 * x2;
        lenSq = cross * cross;
        lenSq /= x1 * x1 + y1 * y1;
        return lenSq;
    }

    public final boolean coincident(Point2d a, Point2d b) {
        if (a.distanceSquared(b) < epsilon) {
            return true;
        }
        return false;
    }

    // project the point p orthogonally onto the segment p1->p2
    // and return the scaled, parameterized position in [0,1]
    public final static float projection(
            Point2d p1,
            Point2d p2,
            Point2d p) {
        float ax, ay, bx, by;
        ax = p.x - p1.x;
        ay = p.y - p1.y;
        bx = p2.x - p1.x;
        by = p2.y - p1.y;
        return (ax * bx + ay * by) / (bx * bx + by * by);
    }

    // returns the intersection point of segments ab and cd
    // as a point on ab
    public final static Point2d intersection(
            Point2d a,
            Point2d b,
            Point2d c,
            Point2d d) throws Exception {
        float t, l1, l2;
        float cdx, cdy;
        Point2d p;

        cdx = c.x - d.x;
        cdy = c.y - d.y;
        // distance from a to cd
        l1 = Math.abs((a.x - d.x) * cdy - (a.y - d.y) * cdx);
        // distance from b to cd
        l2 = Math.abs((b.x - d.x) * cdy - (b.y - d.y) * cdx);
        // need to handle case where l1+l2 = 0
        // if this method could be called on parallel segments
        // that overlap
        if (l1 + l2 == 0) {
            /*System.err.println(
                    "Intersection called on parallel overlapping segments!");*/
            throw new Exception("Intersection called on parallel overlapping segments!");
        }
        t = l1 / (l1 + l2);
        p = new Point2d(
                (1 - t) * a.x + t * b.x,
                (1 - t) * a.y + t * b.y);
        return p;
    }

    // from O'Rourke's Computational Geometry in C
    public final boolean intersect(
            Point2d a,
            Point2d b,
            Point2d c,
            Point2d d) {
        if (intersectProper(a, b, c, d)) {
            return true;
        } else if (between(a, b, c)
                || between(a, b, d)
                || between(c, d, a)
                || between(c, d, b)) {
            return true;
        } else {
            return false;
        }
    }

    // from O'Rourke's Computational Geometry in C
    public final boolean intersectProper(
            Point2d a,
            Point2d b,
            Point2d c,
            Point2d d) {
        /* Eliminate improper cases. */
        if (orient(a, b, c) == 0
                || orient(a, b, d) == 0
                || orient(c, d, a) == 0
                || orient(c, d, b) == 0) {
            return false;
        } else if (orient(a, b, c) * orient(a, b, d) > 0
                || orient(c, d, a) * orient(c, d, b) > 0) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Tests whether c is within the epsilon tubular neighborhood
     * around segment ab.
     */
    public final boolean between(
            Point2d a,
            Point2d b,
            Point2d c) {
        /* check the epsilon neighborhood at the endpoints */
        if (coincident(a, c)) {
            return true;
        } else if (coincident(b, c)) {
            return true;
        } else /* check the epsilon neighborhood along the segment */ if (perpDistSq(a, b, c) < epsilon) {
            float d = projNorm(a, b, c);
            if (0 < d && d < 1) {
                return true;
            }
        }
        return false;
    }

    /*
     * Tests whether c is within the epsilon tubular neighborhood
     * around segment ab, but excludes the epsilon neighborhoods
     * around a and b.
     */
    public final boolean betweenProper(
            Point2d a,
            Point2d b,
            Point2d c) {
        /* reject the epsilon neighborhood at the endpoints */
        if (coincident(a, c)) {
            return false;
        } else if (coincident(b, c)) {
            return false;
        } else /* check the epsilon neighborhood along the segment */ if (perpDistSq(a, b, c) < epsilon) {
            float d = projNorm(a, b, c);
            if (0 < d && d < 1) {
                return true;
            }
        }
        return false;
    }

    /*
     * Orientation predicate that returns positive if ray a->b must
     * turn counter-clockwise to intersect vertex c.
     *
     * Adapted from Jonathan Shewchuk's predicates.c orient2d() routine.
     * This implementation uses only a single-level floating-point filter,
     * which is a simplified and less efficient version of his multi-level
     * adaptive method.
     */
    public final float orient(
            Point2d pa,
            Point2d pb,
            Point2d pc) {
        float detleft, detright, det;
        float detsum, errbound;

        detleft = (pa.x - pc.x) * (pb.y - pc.y);
        detright = (pa.y - pc.y) * (pb.x - pc.x);
        det = detleft - detright;

        if (detleft > 0.0) {
            if (detright <= 0.0) {
                return det;
            } else {
                detsum = detleft + detright;
            }
        } else if (detleft < 0.0) {
            if (detright >= 0.0) {
                return det;
            } else {
                detsum = -detleft - detright;
            }
        } else {
            return det;
        }

        /*errbound = orientErrorBound * detsum;
        if ((det >= errbound) || (-det >= errbound)) {
            return det;
        }
        if (MESSAGES) {
            message("orient = %g\n", det);
        }*/

        /* resort to exact arithmetic */
        //return orientExact(pa, pb, pc);
        
        return det;
    }

    /*
     * Orientation predicate that returns positive if ray a->b must
     * turn counter-clockwise to intersect vertex c.
     *
     * Performs an exact calculation using the Apfloat arbitrary-precision
     * library.
     */
    /*public final float orientExact(
            Point2d pa,
            Point2d pb,
            Point2d pc) {
        Apfloat ax, ay, bx, by, cx, cy;
        Apfloat acx, bcx, acy, bcy;
        Apfloat detleft, detright, det;

        det = Apfloat.ZERO;

        try {
            ax = new Apfloat(pa.x);
            ay = new Apfloat(pa.y);
            bx = new Apfloat(pb.x);
            by = new Apfloat(pb.y);
            cx = new Apfloat(pc.x);
            cy = new Apfloat(pc.y);

            acx = org.apfloat.ApfloatMath.sum(ax, cx.negate());
            bcx = org.apfloat.ApfloatMath.sum(bx, cx.negate());
            acy = org.apfloat.ApfloatMath.sum(ay, cy.negate());
            bcy = org.apfloat.ApfloatMath.sum(by, cy.negate());

            detleft = org.apfloat.ApfloatMath.product(acx, bcy);
            detright = org.apfloat.ApfloatMath.product(acy, bcx);

            det = org.apfloat.ApfloatMath.sum(detleft, detright.negate());
        } catch (ApfloatRuntimeException e) {
            System.err.println("Unable to complete exact orient calculation!");
            System.err.println(e.getMessage());
        }
        if (MESSAGES) {
            message("orientExact = %g\n", det.floatValue());
        }

        return (float) det.compareTo(Apfloat.ZERO);
    }*/

    /*
     * Non-robust, deprecated orientation predicate.
     */
    public final float orientNonRobust(
            Point2d a,
            Point2d b,
            Point2d c) {
        if (perpDistSq(a, b, c) < epsilon) {
            return 0;
        }
        return cross(a, b, c);
    }

    /*
     * Incircle predicate that returns positive if point pd lies inside
     * the circumcircle through pa, pb, and pc.
     *
     * Adapted from Jonathan Shewchuk's predicates.c incircle() routine.
     * This implementation uses only a single-level floating-point filter,
     * which is a simplified and less efficient version of his multi-level
     * adaptive method.
     */
    public final float incircle(
            Point2d pa,
            Point2d pb,
            Point2d pc,
            Point2d pd) {
        float adx, ady, bdx, bdy, cdx, cdy;
        float bdxcdy, cdxbdy, cdxady, adxcdy, adxbdy, bdxady;
        float alift, blift, clift;
        float det;
        float permanent, errbound;

        adx = pa.x - pd.x;
        bdx = pb.x - pd.x;
        cdx = pc.x - pd.x;
        ady = pa.y - pd.y;
        bdy = pb.y - pd.y;
        cdy = pc.y - pd.y;

        bdxcdy = bdx * cdy;
        cdxbdy = cdx * bdy;
        alift = adx * adx + ady * ady;

        cdxady = cdx * ady;
        adxcdy = adx * cdy;
        blift = bdx * bdx + bdy * bdy;

        adxbdy = adx * bdy;
        bdxady = bdx * ady;
        clift = cdx * cdx + cdy * cdy;

        det = alift * (bdxcdy - cdxbdy)
                + blift * (cdxady - adxcdy)
                + clift * (adxbdy - bdxady);

        if (bdxcdy < 0) {
            bdxcdy = -bdxcdy;
        }
        if (cdxbdy < 0) {
            cdxbdy = -cdxbdy;
        }
        if (cdxady < 0) {
            cdxady = -cdxady;
        }
        if (adxcdy < 0) {
            adxcdy = -adxcdy;
        }
        if (adxbdy < 0) {
            adxbdy = -adxbdy;
        }
        if (bdxady < 0) {
            bdxady = -bdxady;
        }

        /*permanent = (bdxcdy + cdxbdy) * alift
                + (cdxady + adxcdy) * blift
                + (adxbdy + bdxady) * clift;
        errbound = incircleErrorBound * permanent;
        if ((det > errbound) || (-det > errbound)) {
            return det;
        }*/

        //return incircleExact(pa, pb, pc, pd);
        return det;
    }

    /*
     * Incircle predicate that returns positive if point pd lies inside
     * the circumcircle through pa, pb, and pc.
     *
     * Performs an exact calculation using the Apfloat arbitrary-precision
     * library.
     */
    /*public final float incircleExact(
            Point2d pa,
            Point2d pb,
            Point2d pc,
            Point2d pd) {
        Apfloat ax, ay, bx, by, cx, cy, dx, dy;
        Apfloat adx, ady, bdx, bdy, cdx, cdy;
        Apfloat bdxcdy, cdxbdy, cdxady, adxcdy, adxbdy, bdxady;
        Apfloat alift, blift, clift;
        Apfloat det;

        det = Apfloat.ZERO;

        try {
            ax = new Apfloat(pa.x);
            ay = new Apfloat(pa.y);
            bx = new Apfloat(pb.x);
            by = new Apfloat(pb.y);
            cx = new Apfloat(pc.x);
            cy = new Apfloat(pc.y);
            dx = new Apfloat(pd.x);
            dy = new Apfloat(pd.y);

            dx = dx.negate();
            dy = dy.negate();

            adx = org.apfloat.ApfloatMath.sum(ax, dx);
            bdx = org.apfloat.ApfloatMath.sum(bx, dx);
            cdx = org.apfloat.ApfloatMath.sum(cx, dx);
            ady = org.apfloat.ApfloatMath.sum(ay, dy);
            bdy = org.apfloat.ApfloatMath.sum(by, dy);
            cdy = org.apfloat.ApfloatMath.sum(cy, dy);

            bdxcdy = org.apfloat.ApfloatMath.product(bdx, cdy);
            cdxbdy = org.apfloat.ApfloatMath.product(cdx, bdy);

            cdxady = org.apfloat.ApfloatMath.product(cdx, ady);
            adxcdy = org.apfloat.ApfloatMath.product(adx, cdy);

            adxbdy = org.apfloat.ApfloatMath.product(adx, bdy);
            bdxady = org.apfloat.ApfloatMath.product(bdx, ady);

            adx = org.apfloat.ApfloatMath.product(adx, adx);
            ady = org.apfloat.ApfloatMath.product(ady, ady);
            alift = org.apfloat.ApfloatMath.sum(adx, ady);

            bdx = org.apfloat.ApfloatMath.product(bdx, bdx);
            bdy = org.apfloat.ApfloatMath.product(bdy, bdy);
            blift = org.apfloat.ApfloatMath.sum(bdx, bdy);

            cdx = org.apfloat.ApfloatMath.product(cdx, cdx);
            cdy = org.apfloat.ApfloatMath.product(cdy, cdy);
            clift = org.apfloat.ApfloatMath.sum(cdx, cdy);

            alift = org.apfloat.ApfloatMath.product(alift,
                    org.apfloat.ApfloatMath.sum(bdxcdy, cdxbdy.negate()));
            blift = org.apfloat.ApfloatMath.product(blift,
                    org.apfloat.ApfloatMath.sum(cdxady, adxcdy.negate()));
            clift = org.apfloat.ApfloatMath.product(clift,
                    org.apfloat.ApfloatMath.sum(adxbdy, bdxady.negate()));

            det = org.apfloat.ApfloatMath.sum(alift, blift, clift);
        } catch (ApfloatRuntimeException e) {
            System.err.println("Unable to complete exact incircle calculation!");
            System.err.println(e.getMessage());
        }
        if (MESSAGES) {
            message("incircleExact = %g", det.floatValue());
        }

        return (float) det.compareTo(Apfloat.ZERO);
    }*/

    /*
     * Non-robust, deprecated incircle predicate.
     */
    public final static int incircleNonRobust(
            Point2d a,
            Point2d b,
            Point2d c,
            Point2d d) {
        float adx, ady, bdx, bdy, cdx, cdy;
        float abdet, bcdet, cadet;
        float alift, blift, clift;

        adx = a.x - d.x;
        ady = a.y - d.y;
        bdx = b.x - d.x;
        bdy = b.y - d.y;
        cdx = c.x - d.x;
        cdy = c.y - d.y;

        abdet = adx * bdy - bdx * ady;
        bcdet = bdx * cdy - cdx * bdy;
        cadet = cdx * ady - adx * cdy;
        alift = adx * adx + ady * ady;
        blift = bdx * bdx + bdy * bdy;
        clift = cdx * cdx + cdy * cdy;

        return (int) Math.signum(alift * bcdet + blift * cadet + clift * abdet);
    }

    // adapted from java.awt.geom.Line2d.ptSegDistSq()
    public final static float edgeDistanceSq(
            Point2d p1,
            Point2d p2,
            Point2d p) {
        float x2, y2, px, py;
        // Adjust vectors relative to x1,y1
        // x2,y2 becomes relative vector from x1,y1 to end of segment
        x2 = p2.x - p1.x;
        y2 = p2.y - p1.y;
        // px,py becomes relative vector from x1,y1 to test point
        px = p.x - p1.x;
        py = p.y - p1.y;
        float dotprod = px * x2 + py * y2;
        float projlenSq;
        if (dotprod <= 0.0) {
            // px,py is on the side of x1,y1 away from x2,y2
            // distance to segment is length of px,py vector
            // "length of its (clipped) projection" is now 0.0
            projlenSq = 0.0f;
        } else {
            // switch to backwards vectors relative to x2,y2
            // x2,y2 are already the negative of x1,y1=>x2,y2
            // to get px,py to be the negative of px,py=>x2,y2
            // the dot product of two negated vectors is the same
            // as the dot product of the two normal vectors
            px = x2 - px;
            py = y2 - py;
            dotprod = px * x2 + py * y2;
            if (dotprod <= 0.0) {
                // px,py is on the side of x2,y2 away from x1,y1
                // distance to segment is length of (backwards) px,py vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq = 0.0f;
            } else {
                // px,py is between x1,y1 and x2,y2
                // dotprod is the length of the px,py vector
                // projected on the x2,y2=>x1,y1 vector times the
                // length of the x2,y2=>x1,y1 vector
                projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
            }
        }
        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        // (which is zero if the projection falls outside the range
        //  of the line segment).
        float lenSq = px * px + py * py - projlenSq;
        if (lenSq < 0) {
            lenSq = 0;
        }
        return lenSq;
    }

    /******************************************************************************/
    /* Debug, test, and output methods
/******************************************************************************/
    public final void test() {
        message("Testing.");
        testing = true;
        testPoints();
        testFaces();
        testHalfEdges();
        testHalfEdgePointers();
        testing = false;
    }

    private final void testPoints() {
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            for (int j = 0; j < points.size(); j++) {
                Point p2 = points.get(j);
                if (p1 != p2 && coincident(p1, p2)) {
                    System.err.printf("%s: Coincident points: %d, %d!\n",
                            name, i, j);
                }
            }
            /*for (int j=0; j<halfEdges.size(); j++)  {
                HalfEdge he = halfEdges.get(j);
                if (between(he.origin,he.next.origin,p1) &&
                    he.origin != p1 &&
                    he.next.origin != p1)
                {
                    System.err.printf("%s: Point %d overlaps halfedge %d!\n",
                            name,i,j);
                    if (DEBUG) debugView(p1,"Point overlaps halfedge");
                    if (DEBUG) debugView(he,"Point overlaps halfedge");
                }
            }*/
        }
    }

    private final void testFaces() {
        int i;
        HalfEdge heTest;
        LinkedList<HalfEdge> used = new LinkedList();
        LinkedList<HalfEdge> face = new LinkedList();

        for (HalfEdge he : halfEdges) {
            if (used.contains(he)) {
                continue;
            }
            face.clear();
            heTest = he;
            for (i = 0; i <= halfEdges.size(); i++) {
                used.add(heTest);
                face.add(heTest);
                heTest = heTest.next;
                if (heTest == he) {
                    break;
                }
            }
            assert i < halfEdges.size() : error(E_EXHAUSTED);
            if (face.size() > 3) {
                System.out.printf("%s: polygon: (", name);
                for (HalfEdge he0 : face) {
                    System.out.printf(" %d ", points.indexOf(he0.origin));
                }
                System.out.println(")");
            }
        }
    }

    private final void testHalfEdges() {
        for (int i = 0; i < halfEdges.size(); i++) {
            HalfEdge he = halfEdges.get(i);
            if (points.contains(he.origin) == false) {
                System.err.printf(
                        "%s: Missing origin for halfedge %d!\n",
                        name, i);
            }
            if (halfEdges.contains(he.next) == false) {
                System.err.printf(
                        "%s: Missing next pointer for halfedge %d!\n",
                        name, i);
            }
            if (coincident(he.origin, he.next.origin)) {
                System.err.printf(
                        "%s: Coincident endpoints for halfedge %d!\n",
                        name, i);
            }
            if (!he.isType(HalfEdge.BOUNDARY)) {
                if (halfEdges.contains(he.sibling) == false) {
                    System.err.printf(
                            "%s: Missing sibling for halfedge %d!\n",
                            name, i);
                }
                if (he.sibling.sibling != he) {
                    System.err.printf(
                            "%s: Mismatched sibling for halfedge %d!\n",
                            name, i);
                }
                if (he.next.origin != he.sibling.origin) {
                    System.err.printf(
                            "%s: Unaligned next/sibling for halfedge %d\n",
                            name, i);
                }
            }
        }
    }

    private final void testHalfEdgePointers() {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (halfEdges.contains(p.he) == false) {
                System.err.printf(
                        "%s: Missing halfedge for point %d!\n",
                        name, i);
            }
            if (p.he.origin != p) {
                System.err.printf(
                        "%s: Mismatched halfedge for point %d!\n",
                        name, i);
            }
        }
    }

    public void listPoints() {
        message("### POINT LIST ###");
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (i % 20 == 0) {
                message("     ID | Halfedge |    Pair | Type");
            }
            int ih, ip;
            try {
                ih = halfEdges.indexOf(p.he);
            } catch (NullPointerException e) {
                ih = NULL_VALUE;
            }
            try {
                ip = halfEdges.indexOf(p.he);
            } catch (NullPointerException e) {
                ip = NULL_VALUE;
            }
            message("%7d |  %7d | %7d |    %1d", i, ih, ip, p.type);
        }
    }

    public final void listHalfEdges() {
        message("### HALFEDGE LIST ###");
        for (int i = 0; i < halfEdges.size(); i++) {
            HalfEdge he = halfEdges.get(i);
            if (i % 20 == 0) {
                message("     ID ->    Next |  Origin ->    "
                        + "Next | Sibling | Type");
            }
            int in, io, ino, is;
            try {
                in = halfEdges.indexOf(he.next);
            } catch (NullPointerException e) {
                in = NULL_VALUE;
            }
            try {
                io = points.indexOf(he.origin);
            } catch (NullPointerException e) {
                io = NULL_VALUE;
            }
            try {
                ino = points.indexOf(he.next.origin);
            } catch (NullPointerException e) {
                ino = NULL_VALUE;
            }
            try {
                is = halfEdges.indexOf(he.sibling);
            } catch (NullPointerException e) {
                is = NULL_VALUE;
            }
            message("%7d -> %7d | %7d -> %7d | %7d | %1d",
                    i, in, io, ino, is, he.type);
        }
    }

    /*public void debugView() {
        debugView("(no message)");
    }

    public void debugView(String s) {
        if (toggleDebugVisual) {
            //debugFrame.repaint();
            debugPanel.repaint();
        }
        if (toggleDebugInteractive
                && !toggleDebugSuspend) {
            // pause for user response
            JOptionPane pane = new JOptionPane(
                    s + "\nContinue?\n",
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.YES_NO_CANCEL_OPTION);
            JDialog dialog = pane.createDialog(debugFrame, "Paused");
            dialog.setLocation(0, 0);
            dialog.setVisible(true);
            Integer selectedValue = (Integer) pane.getValue();
            if (selectedValue == JOptionPane.CANCEL_OPTION) {
                toggleDebugSuspend = true;
            } else if (selectedValue == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
    }

    public void debugView(Point p, String s) {
        debugObjects.add(p);
        debugView(s);
        debugObjects.remove(p);
    }

    public void debugView(HalfEdge he, String s) {
        debugObjects.add(he);
        debugView(s);
        debugObjects.remove(he);
    }

    public void debugView(String s, Object... args) {
        for (Object obj : args) {
            debugObjects.add(obj);
        }
        debugView(s);
        for (Object obj : args) {
            debugObjects.remove(obj);
        }
    }

    public void dvStart() {
        toggleDebugInteractive = toggleDebugVisual = true;
    }

    public void dvEnd() {
        toggleDebugInteractive = toggleDebugVisual = false;
    }

    public void debugPause(String s, Object... args) {
        dvStart();
        for (Object obj : args) {
            debugView(s, obj);
        }
        dvEnd();
    }*/

    public final void message(String s) {
        System.out.print(name);
        System.out.print(": ");
        System.out.print(s);
        System.out.print("\n");
    }

    public final void message(String s, Object... args) {
        message(String.format(s, args));
    }

    // used in assert statements to dump halfedge and point lists
    protected final String error(String s) {
        listHalfEdges();
        listPoints();
        if (!testing) {
            test();
        }
        return s;
    }

    protected final String error(String s, Object... args) {
        /*if (DEBUG) {
            debugPause(s, args);
        }*/
        return error(s);
    }
}
