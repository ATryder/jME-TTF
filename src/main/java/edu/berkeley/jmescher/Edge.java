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

/**
 * A helper class that pairs the endpoints of an edge.
 *
 * @author Mark Howison
 */
public final class Edge
{
    public Point p1,p2;
    public int type = 0;

    /**
     * Constructs an edge with endpoints <tt>p1</tt> and <tt>p2</tt>.
     *
     * @param p1
     * @param p2
     */
    public Edge(Point p1, Point p2)
    {
        this.p1 = p1;
        this.p2 = p2;
    }
    
    /**
     * Copy constructor for an existing edge <tt>e</tt>. The pointers to the
     * endpoints are copied, but not the points themsleves (shallow copy).
     *
     * @param e
     */
    public Edge(Edge e)
    {
        p1 = e.p1;
        p2 = e.p2;
    }

    /**
     * Returns <tt>true</tt> if edges <tt>e1</tt> and <tt>e2</tt> have any
     * endpoint in common.
     *
     * @param e1
     * @param e2
     * @return
     */
    public static boolean adjacent(Edge e1, Edge e2)
    {
        if (e1.p1 == e2.p1) return true;
        if (e1.p1 == e2.p2) return true;
        if (e1.p2 == e2.p1) return true;
        if (e1.p2 == e2.p2) return true;
        return false;
    }

    /**
     * Returns <tt>true</tt> if this edge has the same endpoints as <tt>e</tt>.
     *
     * @param e
     * @return
     */
    public boolean equals (Edge e) {
        if (e.p1 == p1 && e.p2 == p2) return true;
        if (e.p1 == p2 && e.p2 == p1) return true;
        return false;
    }
}
