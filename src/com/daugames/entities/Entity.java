package com.daugames.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;

public class Entity {

    // Ícones genéricos (dependem de Game.spritesheet; certifique-se que Game.spritesheet
    // seja inicializado antes de criar Worlds / Tiles que usam essas imagens)
    public static java.awt.image.BufferedImage LIFEPACK_EN = Game.spritesheet.getSprite(6*16, 0, 16, 16);
    public static java.awt.image.BufferedImage WEAPON_EN = Game.spritesheet.getSprite(7*16, 0, 16, 16);
    public static java.awt.image.BufferedImage BULLET_EN = Game.spritesheet.getSprite(6*16, 16, 16, 16);
    public static java.awt.image.BufferedImage WEAPON_RIGHT = Game.spritesheet.getSprite(112, 48, 16, 16);
    public static java.awt.image.BufferedImage WEAPON_LEFT = Game.spritesheet.getSprite(128, 48, 16, 16);
    public static java.awt.image.BufferedImage ENEMY1_FEEDBACK = Game.spritesheet.getSprite(128, 64, 16, 16);
    public static java.awt.image.BufferedImage ENEMY2_FEEDBACK_LEFT = Game.spritesheet.getSprite(96, 64, 16, 16);
    public static java.awt.image.BufferedImage ENEMY2_FEEDBACK_RIGHT = Game.spritesheet.getSprite(112, 64, 16, 16);
    
    //INIMIGO 1
    public static BufferedImage[] ENEMY1_LEFT = {
    		Game.spritesheet.getSprite(7*16, 16, 16, 16),
    		Game.spritesheet.getSprite(8*16, 16, 16, 16)
    };
    
    //INIMIGO 2
    public static BufferedImage[] ENEMY2_LEFT = {
    	    Game.spritesheet.getSprite(6*16, 32, 16, 16),
    	    Game.spritesheet.getSprite(7*16, 32, 16, 16)
    };

	public static BufferedImage[] ENEMY2_RIGHT = {
	    Game.spritesheet.getSprite(8*16, 32, 16, 16),
	    Game.spritesheet.getSprite(9*16, 32, 16, 16)
	};


    protected double x;
    protected double y;
    protected int width;
    protected int height;
    
    private int maskx, masky, mwidth, mheight;

    private java.awt.image.BufferedImage sprite;

    public Entity(int x, int y, int width, int heigth, java.awt.image.BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = heigth;
        this.sprite = sprite;
    }
    public void setMask(int maskx, int masky, int mwidth, int mheight) {
    	this.maskx = maskx;
    	this.masky = masky;
    	this.mwidth = mwidth;
    	this.mheight = mheight;
    }

    public int getX() {
        return (int)x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return (int)y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void render(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, getX() - Camera.x, getY() - Camera.y, null);
        }
    }

    public void update() { }

    // Bound "total" da entidade (útil para alguns usos); hitboxes específicas
    // devem ser implementadas em Player / Enemy com offsets (masks).
    public Rectangle getBounds() {
        return new Rectangle((int)(x), (int)(y), width, height);
    }
    
    
    public static boolean isColidding(Entity e1, Entity e2) {
    	Rectangle e1Mask = new Rectangle(e1.getX() + e1.maskx, e1.getY() +e1.masky, e1.mwidth, e1.mheight);
    	Rectangle e2Mask = new Rectangle(e2.getX() + e2.maskx, e2.getY() +e2.masky, e2.mwidth, e2.mheight);
    	
    	return e1Mask.intersects(e2Mask);
    }
}
