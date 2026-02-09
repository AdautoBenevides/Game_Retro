package com.daugames.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;
import com.daugames.world.GameState;
import com.daugames.world.World;
import com.daugames.world.WorldType;

public class Player extends Entity {

    public boolean right, up, left, down;
    public int dir = 0; // 0 = right | 1 = left
    public double speed = 1.4;

    // VIDA
    public static int life = 5, maxlife = 5;
    private int damageCooldown = 0;
    private final int DAMAGE_DELAY = 60;

    // HITBOX
    private int maskx = 4, masky = 8, maskw = 8, maskh = 8;

    // ANIMAÇÃO
    private int frames = 0, maxFrames = 5, index = 0, maxIndex = 3;
    private boolean moved = false;

    private BufferedImage[] rightSprites;
    private BufferedImage[] leftSprites;
    private BufferedImage playerDamage_girl;
    private BufferedImage playerDamage_boy;

    public static int ammo = 5;
    public static int maxammo = 10;

    public boolean isDamaged = false;
    private int damageFrames = 0;

    private boolean hasGun = false;
    public boolean isShooting = false;
    
    // ✅ CONSTRUTOR ÚNICO CORRETO
    public Player(int x, int y, int w, int h) {
        super(x, y, w, h, null);
        loadCharacterSprites();
        playerDamage_girl = Game.spritesheet.getSprite(96, 48, 16, 16);
        playerDamage_boy = Game.spritesheet.getSprite(112, 96, 16, 16);
    }

    // ✅ CARREGA SPRITES BASEADO NO PERSONAGEM ESCOLHIDO
    private void loadCharacterSprites() {
        int startYRight;
        int startYLeft;

        if (Game.selectedCharacter == CharacterType.BOY) {
            startYRight = 0;
            startYLeft  = 16;
        } else {
            startYRight = 32;
            startYLeft  = 48;
        }

        rightSprites = new BufferedImage[4];
        leftSprites  = new BufferedImage[4];

        for (int i = 0; i < 4; i++) {
            rightSprites[i] = Game.spritesheet.getSprite(32 + i * 16, startYRight, 16, 16);
            leftSprites[i]  = Game.spritesheet.getSprite(32 + i * 16, startYLeft, 16, 16);
        }
    }

    @Override
    public void update() {
        if (damageCooldown > 0) {
            damageCooldown--;
        }

        moved = false;

        // CORREÇÃO: Chama canMove com coordenadas do SPRITE
        if (right && canMove((int)(x + speed), getY())) {
            x += speed;
            dir = 0;
            moved = true;
        } else if (left && canMove((int)(x - speed), getY())) {
            x -= speed;
            dir = 1;
            moved = true;
        }

        if (up && canMove(getX(), (int)(y - speed))) {
            y -= speed;
            moved = true;
        } else if (down && canMove(getX(), (int)(y + speed))) {
            y += speed;
            moved = true;
        }

        if (moved) {
            frames++;
            if (frames >= maxFrames) {
                frames = 0;
                index = (index + 1) % (maxIndex + 1);
            }
        }

        if (isDamaged) {
            damageFrames++;
            if (damageFrames == 20) {
                damageFrames = 0;
                isDamaged = false;
            }
        }

        if (isShooting) {
            isShooting = false;
            if (hasGun && ammo > 0 && Game.bullets.size() == 0) {
                ammo--;
                int dx = (dir == 0) ? 1 : -1;
                int px = (dir == 0) ? 11 : 1;
                int py = 3;

                BulletShoot bullet = new BulletShoot(
                        this.getX() + px,
                        this.getY() + py,
                        3, 3, null, dx, 0);

                Game.bullets.add(bullet);
            }
        }

        Camera.x = Camera.clamp(getX() - Game.WIDTH / 2, 0,
                Math.max(0, World.WIDTH * World.TILE_SIZE - Game.WIDTH));
        Camera.y = Camera.clamp(getY() - Game.HEIGHT / 2, 0,
                Math.max(0, World.HEIGHT * World.TILE_SIZE - Game.HEIGHT));
    }

    // CORREÇÃO: Método canMove simplificado
    private boolean canMove(int nx, int ny) {
        // nx e ny são coordenadas do SPRITE
        
        // 1. Verifica colisão com tiles do mundo (usa dimensões do SPRITE)
        if (!World.isFree(nx, ny, width, height)) {
            return false;
        }
        
        // 2. Verifica colisão com inimigos (usa máscara para maior precisão)
        Rectangle nextMask = new Rectangle(nx + maskx, ny + masky, maskw, maskh);
        for (Entity e : Game.entities) {
            if (e instanceof Enemy) {
                Enemy enemy = (Enemy) e;
                if (nextMask.intersects(enemy.getMask())) {
                    return false;
                }
            }
        }
        return true;
    }

    public void takeDamage() {
        if (damageCooldown == 0) {
            life--;
            isDamaged = true;
            damageCooldown = DAMAGE_DELAY;

            if (life <= 0) {
                Game.state = GameState.GAME_OVER;
            }
        }
    }

    public Rectangle getMask() {
        return new Rectangle(getX() + maskx, getY() + masky, maskw, maskh);
    }

    @Override
    public void render(Graphics g) {
        if (!isDamaged) {
            BufferedImage img = (dir == 0 ? rightSprites[index] : leftSprites[index]);
            g.drawImage(img, getX() - Camera.x, getY() - Camera.y, null);

            if (hasGun) {
                if (dir == 0) {
                    g.drawImage(Entity.WEAPON_RIGHT,
                            this.getX() + 2 - Camera.x,
                            this.getY() + 2 - Camera.y, null);
                } else {
                    g.drawImage(Entity.WEAPON_LEFT,
                            this.getX() - 2 - Camera.x,
                            this.getY() + 2 - Camera.y, null);
                }
            }
        } else {
            if (Game.selectedCharacter == CharacterType.BOY) {
                g.drawImage(playerDamage_boy, getX() - Camera.x, getY() - Camera.y, null);
            } else if(Game.selectedCharacter == CharacterType.GIRL) {
                g.drawImage(playerDamage_girl, getX() - Camera.x, getY() - Camera.y, null);
            }
        }
    }

    // ✅ RESET CORRETO USANDO PERSONAGEM ESCOLHIDO
    public static void restartGame() {
        Game.entities.clear();
        Game.player = new Player(0, 0, 16, 16);
        life = maxlife;
        Game.entities.add(Game.player);
        Game.world = new World("/map_house.png", WorldType.HOUSE);
    }
}