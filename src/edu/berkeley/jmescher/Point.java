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
import com.jme3.math.Vector2f;

/**
 * Represents a 2D vertex in a triangulation. Contains references to a halfedge
 * in the star of the vertex, to 3D coordinates (for 3D meshing), and to
 * a "paired" point (for encoding symmetry information).
 *
 * @author Mark Howison
 */
public class Point extends Point2d implements Cloneable {

    public final static int INTERIOR = 0;
    public final static int BOUNDARY = 1;
    public final static int DELETED = 2;
    public final static int TRANSLATED = 3;
    public final static int XSECTION = 4;
    public final static int BOUNDS = 5;

    public HalfEdge he = null;
    public int type = INTERIOR;
    public Coords3d coords3d = new Coords3d();
    public Point pair;
    
    private Vector2f uv;
    
    public int users = 1;

    /**
     * Constructs a new point with 2D coordinates (0,0).
     */
    public Point() {
        super();
    }

    /**
     * Constructs a new point with the same 2D coordinates as <tt>p</tt>.
     *
     * @param p
     */
    public Point(Point2d p) {
        super(p);
    }

    /**
     * Constructs a new point with 2D coordinates <tt>(x,y)</tt>.
     *
     * @param x
     * @param y
     */
    public Point(float x, float y) {
        super(x, y);
    }

    /**
     * Shallow copy of point <tt>p</tt>. All pointers are copied, but not
     * the underlying objects. 2D and 3D coordinates are set to those of
     * <tt>p</tt>.
     *
     * @param p
     */
    public Point(Point p) {
        super(p);
        he = p.he;
        type = p.type;
        coords3d.set(p.coords3d);
        pair = p.pair;
        if (p.getUV() != null)
            uv = p.getUV().clone();
    }
    
    @Override
    public Point clone() {
        return new Point(this);
    }
    
    public void setUV(float u, float v) {
        uv = new Vector2f(u, v);
    }
    
    public Vector2f getUV() {
        return uv;
    }

    /**
     * Sets the point type (interior, boundary, etc.). Use the static int flags
     * to specify type.
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Tests if this point is of <tt>type</tt>. Use the static int flags
     * to specify type.
     *
     * @param type
     * @return
     */
    public boolean isType(int type) {
        if (this.type == type) {
            return true;
        }
        if (type == Point.INTERIOR && this.type == Point.TRANSLATED) {
            return true;
        }
        if (type == Point.BOUNDARY && this.type == Point.XSECTION) {
            return true;
        }
        return false;
    }
}
