package com.daugames.entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;

public class BulletShoot extends Entity{

	
	private int dx;
	private int dy;
	private double spd = 2;
	
	private int life = 60, curlife =0;
	
	private BufferedImage arrowRight;
	private BufferedImage arrowLeft;
	
	public BulletShoot(int x, int y, int width, int heigth, BufferedImage sprite, int dx, int dy) {
		super(x, y, width, heigth, sprite);
		this.dx = dx;
		this.dy = dy;
		
		arrowRight = Game.spritesheet.getSprite(144, 16, 16, 16); 
	    arrowLeft  = Game.spritesheet.getSprite(144, 48, 16, 16); 
	}

	@Override
	public void update() {
		x += dx * spd;
		y += dy * spd;
		curlife++;
		if (curlife == life) {
			Game.bullets.remove(this);
			return;
		}
	}
	@Override
	public void render(Graphics g) {
	    BufferedImage img = (dx > 0) ? arrowRight : arrowLeft;

	    g.drawImage(
	        img,
	        this.getX() - Camera.x,
	        this.getY() - Camera.y,
	        null
	    );
	}
	
}
