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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Example code that uses jmEscher to create a square boundary then randomly
 * insert points and constrain edges.
 *
 * @author Mark Howison
 */
public class Example extends JPanel
{
    float epsilon = 1.0f;
    int size = 600; /* size of square in pixels */
    int nsides = 16; /* number of sides in the boundary polygon */
    int npts = 100; /* number of points to insert */
    int nedges = 10; /* number of edges to constrain */
    float min = 10;
    float max = size - 10;
    Mesh mesh;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("jmEscher Example");
        Example example = new Example(frame);
        frame.getContentPane().add(example);
        frame.pack();
        frame.setVisible(true);
        example.createMesh();
        example.repaint();
    }
    
    public Example(JFrame frame)
    {
        setPreferredSize(new Dimension(size,size));
        setBackground(Color.WHITE);
        mesh = new Mesh(epsilon);
        //mesh.setDebugFrame(frame);
        //mesh.setDebugPanel(this);
        mesh.setName("Example Mesh");
    }

    public void createMesh()
    {
        /* create a square boundary */
        BPoint[] boundary = new BPoint[4];
        boundary[0] = new BPoint(min,min);
        boundary[1] = new BPoint(min,max);
        boundary[2] = new BPoint(max,max);
        boundary[3] = new BPoint(max,min);
        Mesh.linkBoundary(boundary);
        mesh.init(boundary);
        
        /* randomly insert interior points */
        for (int i=0; i<npts; i++) {
            Point p = new Point();
            p.x = min + epsilon + (float)Math.random() * (max - min - 2*epsilon);
            p.y = min + epsilon + (float)Math.random() * (max - min - 2*epsilon);
            mesh.addInteriorPoint(p);
        }

        /* randomly constrain edges */
        int s = mesh.size() - 1;
        for (int i=0; i<nedges; ) {
            Point p1 = mesh.getPoint( (int) (Math.random()*s) );
            Point p2 = mesh.getPoint( (int) (Math.random()*s) );
            if (p1 != p2) {
                try {
                    mesh.addConstraint(p1,p2);
                } catch (Exception e) {
                    
                }
                i++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        //mesh.drawPoints(g2);
        //mesh.drawLines(g2);
        //mesh.drawDebug(g2);
    }
}
