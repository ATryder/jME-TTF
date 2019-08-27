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

import com.atr.jme.font.util.Point3d;


/**
 * Stores the 3D coordinates, normal and color of a point in the triangulation,
 * for use in 3D meshes derived from the triangulation. The <tt>pair</tt>
 * field can be used to encode symmetry information about the mesh.
 *
 * @author Mark Howison
 */
public final class Coords3d extends Point3d {

    public Coords3d pair;
    public Point3d normal = new Point3d();

    /**
     * Sets the 3D coordinates to (0,0,0).
     */
    public Coords3d() {
    }

    /**
     * Sets the 3D coordinates to those of <tt>p</tt>.
     *
     * @param p
     */
    public Coords3d(Point3d p) {
        super(p);
    }

    /**
     * Sets the 3D coordinates to <tt>(x,y,z)</tt>.
     *
     * @param x
     * @param y
     * @param z
     */
    public Coords3d(float x, float y, float z) {
        super(x, y, z);
    }
}
