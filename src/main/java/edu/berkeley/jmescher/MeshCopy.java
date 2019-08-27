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

import java.util.HashMap;

/**
 * Represents a copy of a Mesh, with special provisions for preserving and
 * looking-up the original point and halfedge data, and for returning only
 * the boundary "shell" of the original mesh.
 *
 * @author Mark Howison
 */
public class MeshCopy extends Mesh
{
    // lookup tables for copy construction
    private HashMap<Point,Point> pointLookup;
    private HashMap<HalfEdge,HalfEdge> halfEdgeLookup;

    public MeshCopy(float epsilon)
    {
        super(epsilon);
    }

    // copy constructor
    public MeshCopy(Mesh m, String name)
    {
        super(m.epsilon);

        Point p1;
        HalfEdge he1;
        pointLookup = new HashMap();
        halfEdgeLookup = new HashMap();

        this.name = name;
        /*if (DEBUG) {
            toggleDebugVisual = m.toggleDebugVisual;
            toggleDebugInteractive = m.toggleDebugInteractive;
            toggleDebugSuspend = m.toggleDebugSuspend;
            debugFrame = m.debugFrame;
            assert debugFrame != null;
        }*/
        // clone points
        for (Point p : m.points) {
            p1 = new PointCopy(p);
            points.add(p1);
            pointLookup.put(p,p1);
        }
        // clone halfedges
        for (HalfEdge he : m.halfEdges) {
            he1 = new HalfEdge(he);
            halfEdges.add(he1);
            halfEdgeLookup.put(he,he1);
        }
        // fix pointers
        for (Point p : points) {
            p.he = halfEdgeLookup.get(p.he);
        }
        for (HalfEdge he : halfEdges) {
            he.origin = pointLookup.get(he.origin);
            he.next = halfEdgeLookup.get(he.next);
            if (! he.isType(HalfEdge.BOUNDARY)) {
                he.sibling = halfEdgeLookup.get(he.sibling);
            }
        }
        nBoundary = m.boundarySize();
    }

    public void boundaryOnly()
    {
        deleteQueue.clear();
        for (Point p : points) {
            if (p.isType(Point.INTERIOR)) {
                deleteQueue.add(p);
                // should deal with pointLookup here
            }
        }
        for (Point p : deleteQueue) {
            removeInteriorPoint(p);
        }
    }

    public void updateCopy()
    {
        for (Point p : points) {
            if (p instanceof PointCopy) {
                PointCopy pc = (PointCopy)p;
                pc.set(pc.origin);
                pc.coords3d.set(pc.origin.coords3d);
            }
        }
    }

    public Point lookup(Point p)
    {
        Point p0 = pointLookup.get(p);
        if (p0 != null) return p0;
        else if (contains(p)) return p;
        return null;
    }

    public Point[] getPoints()
    {
        return points.toArray(new Point[0]);
    }

    public HalfEdge[] getHalfEdges()
    {
        return halfEdges.toArray(new HalfEdge[0]);
    }
}

/******************************************************************************/
/* Class for copied points
/******************************************************************************/

final class PointCopy extends Point
{
    public Point origin;

    public PointCopy(Point p)
    {
        super(p);
        assert this.coords3d != p.coords3d;
        origin = p;
    }
}
