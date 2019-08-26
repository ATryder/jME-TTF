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

/**
 * Represents a boundary point, which has additional fields <tt>next</tt>
 * and <tt>prev</tt> to maintain a doubley-linked list of the boundary.
 * The <tt>side</tt> field can be used for encoding symmetry information
 * about the boundary.
 *
 * @author Mark Howison
 */
public final class BPoint extends Point {

    public int side;
    public BPoint next;
    public BPoint prev;

    /**
     * Creates a point with 'boundary' type.
     */
    public BPoint() {
        super();
        type = Point.BOUNDARY;
    }

    /**
     * Sets the 2D coordinates to those of <tt>p</tt>.
     *
     * @param p
     */
    public BPoint(Point2d p) {
        super(p);
        type = Point.BOUNDARY;
    }

    /**
     * Sets the 2D coordinates to <tt>(x,y)</tt>.
     *
     * @param x
     * @param y
     */
    public BPoint(float x, float y) {
        super(x, y);
        type = Point.BOUNDARY;
    }

    /**
     * Creates a shallow copy of <tt>bp</tt>, copying the pointers and not
     * the objects.
     *
     * @param bp
     */
    public BPoint(BPoint bp) {
        super(bp);
        type = Point.BOUNDARY;
        side = bp.side;
        pair = bp.pair;
        next = bp.next;
        prev = bp.prev;
    }

    /**
     * Up-casts this point's pair to a <tt>BPoint</tt>.
     *
     * @return
     */
    public BPoint getPair() {
        return (BPoint) pair;
    }
}
