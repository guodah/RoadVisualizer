package com.sim.random;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sim.util.ModelExporterUtils;

public class GeneralTest {
	public static void main(String args[]) throws IOException{
		BufferedImage im = ImageIO.read(new File("model\\height.bmp"));
		System.out.printf("%d\n",
				(new Color(im.getRGB(115,425))).getRed());;
		
	}
}
