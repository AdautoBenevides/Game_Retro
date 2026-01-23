package com.daugames.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;
import com.daugames.world.World;

public class Enemy extends Entity {

    private EnemyType type;

    private double speed = 0.8;

    // HITBOX
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

    private int life = 2;

    // DANO
    public boolean isDamaged = false;
    private int damageFrames = 0;
    private final int DAMAGE_TIME = 10;

    // ===============================
    // CONSTRUTOR SIMPLES
    // ===============================
    public Enemy(int x, int y, int width, int height, BufferedImage sprite, EnemyType type) {
        super(x, y, width, height, null);

        this.type = type;

        this.spritesLeft  = new BufferedImage[] { sprite, sprite };
        this.spritesRight = this.spritesLeft;
        this.currentSprites = spritesLeft;
        this.maxIndex = spritesLeft.length - 1;
    }

    // ===============================
    // CONSTRUTOR COMPLETO
    // ===============================
    public Enemy(int x, int y, int width, int height,
                 BufferedImage[] left,
                 BufferedImage[] right,
                 int maskx, int masky, int maskw, int maskh,
                 EnemyType type) {

        super(x, y, width, height, null);

        this.spritesLeft = left;
        this.spritesRight = (right != null) ? right : left;
        this.currentSprites = spritesLeft;
        this.maxIndex = Math.max(spritesLeft.length, this.spritesRight.length) - 1;

        this.maskx = maskx;
        this.masky = masky;
        this.maskw = maskw;
        this.maskh = maskh;

        this.type = type;
    }

    // ===============================
    // UPDATE
    // ===============================
    @Override
    public void update() {

        double dx = 0;
        double dy = 0;

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

        if (dx != 0 && World.isFree((int)(x + dx), getY()) && !collidingWithEnemy((int)(x + dx), getY())) {
            x += dx;
        }

        if (dy != 0 && World.isFree(getX(), (int)(y + dy)) && !collidingWithEnemy(getX(), (int)(y + dy))) {
            y += dy;
        }

        if (getMask().intersects(Game.player.getMask())) {
            Game.player.takeDamage();
        }

        currentSprites = movingRight ? spritesRight : spritesLeft;

        if (moved) {
            walkCounter += Math.abs(dx) + Math.abs(dy);
            if (walkCounter >= pixelsPerFrame) {
                walkCounter = 0;
                index = (index + 1) % (maxIndex + 1);
            }
        }

        // controle do feedback de dano
        if (isDamaged) {
            damageFrames++;
            if (damageFrames >= DAMAGE_TIME) {
                damageFrames = 0;
                isDamaged = false;
            }
        }

        collidingBullet();

        if (life <= 0) {
            destroySelf();
        }
    }

    // ===============================
    // COLISÃO COM TIRO
    // ===============================
    public void collidingBullet() {
        for (int i = 0; i < Game.bullets.size(); i++) {
            Entity e = Game.bullets.get(i);

            if (e instanceof BulletShoot) {
                if (this.getMask().intersects(e.getBounds())) {
                    life--;
                    isDamaged = true;
                    Game.bullets.remove(i);
                    return;
                }
            }
        }
    }

    public void destroySelf() {
        Game.entities.remove(this);
    }

    // ===============================
    // HITBOX
    // ===============================
    public Rectangle getMask() {
        return new Rectangle(getX() + maskx, getY() + masky, maskw, maskh);
    }

    private boolean collidingWithEnemy(int nx, int ny) {
        Rectangle nextMask = new Rectangle(nx + maskx, ny + masky, maskw, maskh);

        for (Entity e : Game.entities) {
            if (e instanceof Enemy && e != this) {
                if (nextMask.intersects(((Enemy)e).getMask())) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===============================
    // RENDER
    // ===============================
    @Override
    public void render(Graphics g) {

        if (!isDamaged) {

            int idx = Math.max(0, Math.min(index, currentSprites.length - 1));
            g.drawImage(currentSprites[idx], getX() - Camera.x, getY() - Camera.y, null);

        } else {

            BufferedImage feedback = null;

            if (type == EnemyType.ENEMY1) {
                feedback = movingRight
                        ? Entity.ENEMY1_FEEDBACK
                        : Entity.ENEMY1_FEEDBACK;

            } else if (type == EnemyType.ENEMY2) {
                feedback = movingRight
                        ? Entity.ENEMY2_FEEDBACK_RIGHT
                        : Entity.ENEMY2_FEEDBACK_LEFT;
            }

            g.drawImage(feedback, getX() - Camera.x, getY() - Camera.y, null);
        }
    }
}
