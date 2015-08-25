package com.sim.random;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class Stroke1 extends JFrame {

    Stroke drawingStroke = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, 
    		BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
    Line2D line = new Line2D.Double(20, 40, 100, 40);

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(drawingStroke);
        g2d.draw(line);
    }

    public static void main(String args[]) {
        JFrame frame = new Stroke1();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(200, 100);
        frame.setVisible(true);
    }
}