package com.daugames.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;
import com.daugames.world.World;

public class Enemy extends Entity {

    private double speed = 0.8;

    // HITBOX (configurável por inimigo)
    private int maskx = 4, masky = 4, maskw = 8, maskh = 8;

    // ANIMAÇÃO
    private int index = 0;
    private int maxIndex = 0;
    private double walkCounter = 0;
    private final double pixelsPerFrame = 6;

    private boolean movingRight = false;

    private BufferedImage[] spritesLeft;
    private BufferedImage[] spritesRight;
    private BufferedImage[] currentSprites;

    // -----------------------
    // Construtor simples: recebe um único sprite (usado para spawn rápido)
    // spritesLeft/right serão gerados com o mesmo sprite (2 frames iguais).
    // -----------------------
    public Enemy(int x, int y, int width, int height, BufferedImage sprite) {
        super(x, y, width, height, null);

        // monta um "array" simples (2 frames identicos para não quebrar animação)
        this.spritesLeft = new BufferedImage[] { sprite, sprite };
        this.spritesRight = this.spritesLeft;
        this.currentSprites = spritesLeft;
        this.maxIndex = spritesLeft.length - 1;

        // hitbox padrão (ajustável)
        this.maskx = 4;
        this.masky = 4;
        this.maskw = 8;
        this.maskh = 8;
    }

    // -----------------------
    // Construtor completo: sprites left/right + hitbox personalizado
    // -----------------------
    public Enemy(int x, int y, int width, int height,
                 BufferedImage[] left,
                 BufferedImage[] right,
                 int maskx, int masky, int maskw, int maskh) {
        super(x, y, width, height, null);

        this.spritesLeft = left;
        this.spritesRight = right != null ? right : left;
        this.currentSprites = spritesLeft;
        this.maxIndex = Math.max(spritesLeft.length, this.spritesRight.length) - 1;

        this.maskx = maskx;
        this.masky = masky;
        this.maskw = maskw;
        this.maskh = maskh;
    }

    @Override
    public void update() {

        double dx = 0;
        double dy = 0;

        // IA simples: com alguma aleatoriedade decide perseguição
        if (Game.rand.nextInt(100) < 30) {

            if (this.getX() < Game.player.getX() && World.isFree(this.getX() + (int)speed, this.getY())) {
                dx = speed;
                movingRight = true;

            } else if (this.getX() > Game.player.getX() && World.isFree(this.getX() - (int)speed, this.getY())) {
                dx = -speed;
                movingRight = false;

            } else if (this.getY() < Game.player.getY() && World.isFree(this.getX(), this.getY() + (int)speed)) {
                dy = speed;

            } else if (this.getY() > Game.player.getY() && World.isFree(this.getX(), this.getY() - (int)speed)) {
                dy = -speed;
            }
        }

        boolean moved = (dx != 0 || dy != 0);

        // movimento por eixo com checagem de colisão com outros inimigos
        // X
        if (dx != 0 &&
            World.isFree((int)(x + dx), getY()) &&
            !collidingWithEnemy((int)(x + dx), getY())) {
            x += dx;
        }

        // Y
        if (dy != 0 &&
            World.isFree(getX(), (int)(y + dy)) &&
            !collidingWithEnemy(getX(), (int)(y + dy))) {
            y += dy;
        }

        // dano independente do movimento (se intersecta, causa dano ao player)
        if (getMask().intersects(Game.player.getMask())) {
            Game.player.takeDamage();
        }

        // seleção dos sprites e animação por deslocamento
        if (dx > 0) {
            movingRight = true;
        } else if (dx < 0) {
            movingRight = false;
        }

        currentSprites = movingRight ? spritesRight : spritesLeft;


        if (moved) {
            walkCounter += Math.abs(dx) + Math.abs(dy);
            if (walkCounter >= pixelsPerFrame) {
                walkCounter = 0;
                index++;
                if (index > maxIndex) {
					index = 0;
				}
            }
        }
    }

    // movimento usado por versões alternativas (mantido)
    private void move(double dx, double dy) {
        // X
        if (dx != 0 &&
            World.isFree((int)(x + dx), getY()) &&
            !collidingWithEnemy((int)(x + dx), getY()) &&
            !getMask((int)(x + dx), getY()).intersects(Game.player.getMask())) {
            x += dx;
        }

        // Y
        if (dy != 0 &&
            World.isFree(getX(), (int)(y + dy)) &&
            !collidingWithEnemy(getX(), (int)(y + dy)) &&
            !getMask(getX(), (int)(y + dy)).intersects(Game.player.getMask())) {
            y += dy;
        }
    }

    public Rectangle getMask() {
        return getMask(getX(), getY());
    }

    private Rectangle getMask(int px, int py) {
        return new Rectangle(px + maskx, py + masky, maskw, maskh);
    }

    private boolean collidingWithEnemy(int nx, int ny) {
        Rectangle nextMask = getMask(nx, ny);

        for (Entity e : Game.entities) {
            if (e instanceof Enemy && e != this) {
                if (nextMask.intersects(((Enemy)e).getMask())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(Graphics g) {
        // garante índice válido
        int idx = Math.max(0, Math.min(index, currentSprites.length - 1));
        g.drawImage(currentSprites[idx], getX() - Camera.x, getY() - Camera.y, null);

//        // DEBUG hitbox
//        Rectangle r = getMask();
//        g.drawRect(r.x - Camera.x, r.y - Camera.y, r.width, r.height);
    }

}
