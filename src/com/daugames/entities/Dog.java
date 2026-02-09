package com.daugames.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;
import com.daugames.world.World;

public class Dog extends Entity {

    private BufferedImage[] rightSprites = new BufferedImage[3];
    private BufferedImage[] leftSprites  = new BufferedImage[3];

    private int dir = 0; // 0 = right | 1 = left

    private double speed = 0.6;

    private int frames = 0, maxFrames = 10, index = 0;

    // controle de patrulha
    private int walkedPixels = 0;
    private final int patrolDistance = 4 * World.TILE_SIZE;

    // mask de colisão
    private int maskx = 3, masky = 6, maskw = 10, maskh = 8;

    public Dog(int x, int y) {
        super(x, y, 16, 16, null);
        loadSprites();
       
    }

    private void loadSprites() {
        // direita
        rightSprites[0] = Game.spritesheet.getSprite(48, 144, 16, 16);
        rightSprites[1] = Game.spritesheet.getSprite(64, 144, 16, 16);
        rightSprites[2] = Game.spritesheet.getSprite(80, 144, 16, 16);

        // esquerda
        leftSprites[0] = Game.spritesheet.getSprite(112, 112, 16, 16);
        leftSprites[1] = Game.spritesheet.getSprite(128, 112, 16, 16);
        leftSprites[2] = Game.spritesheet.getSprite(144, 112, 16, 16);
    }

    @Override
    public void update() {

        double nx = x;
        if (dir == 0) {
			nx += speed;
		} else {
			nx -= speed;
		}

        Rectangle nextMask = new Rectangle(
                (int)nx + maskx,
                (int)y + masky,
                maskw,
                maskh
        );

        // usa o mesmo sistema do player
        if (World.isFree(nextMask.x, nextMask.y, nextMask.width, nextMask.height)) {
            x = nx;
            walkedPixels += speed;
        } else {
            // se bater em algo, inverte também
            invertDirection();
        }

        // troca direção após 4 tiles
        if (walkedPixels >= patrolDistance) {
            invertDirection();
        }

        // animação
        frames++;
        if (frames >= maxFrames) {
            frames = 0;
            index = (index + 1) % 3;
        }
    }

    private void invertDirection() {
        dir = (dir == 0) ? 1 : 0;
        walkedPixels = 0;
    }

    @Override
    public void render(Graphics g) {
    	
        BufferedImage img = (dir == 0)
                ? rightSprites[index]
                : leftSprites[index];

        g.drawImage(img, (int)x - Camera.x, (int)y - Camera.y, null);
    }
}
