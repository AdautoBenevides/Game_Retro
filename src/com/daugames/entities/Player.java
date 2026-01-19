package com.daugames.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;
import com.daugames.world.Camera;
import com.daugames.world.World;
import com.daugames.world.WorldType;

public class Player extends Entity {

    public boolean right, up, left, down;
    public int dir = 0; // 0 = right | 1 = left
    public double speed = 1.4;

    // VIDA
    public static int life = 5, maxlife = 5;
    private int damageCooldown = 0;
    private final int DAMAGE_DELAY = 60; // frames (~1s)

    // HITBOX
    private int maskx = 4, masky = 8, maskw = 8, maskh = 8;

    // ANIMAÇÃO
    private int frames = 0, maxFrames = 5, index = 0, maxIndex = 3;
    private boolean moved = false;

    private BufferedImage[] rightPlayer;
    private BufferedImage[] leftPlayer;
    
    private BufferedImage playerDamage;
    public static int ammo = 0;
    public static int maxammo = 10;
    
    public boolean isDamaged = false;
    private int damageFrames = 0;
    
    private Boolean hasGun = false;

    public Player(int x, int y, int w, int h, BufferedImage sprite) {
        super(x, y, w, h, sprite);

        rightPlayer = new BufferedImage[4];
        leftPlayer  = new BufferedImage[4];
        playerDamage = Game.spritesheet.getSprite(96, 48, 16, 16);
        for (int i = 0; i < 4; i++) {
            rightPlayer[i] = Game.spritesheet.getSprite(32 + i*16, 32, 16, 16);
            leftPlayer[i]  = Game.spritesheet.getSprite(32 + i*16, 48, 16, 16);
        }
    }

    @Override
    public void update() {

        if (damageCooldown > 0) {
            damageCooldown--;
        }

        moved = false;

        // X
        if (right && canMove((int)(x + speed), getY())) {
            x += speed;
            dir = 0;
            moved = true;
        } else if (left && canMove((int)(x - speed), getY())) {
            x -= speed;
            dir = 1;
            moved = true;
        }

        // Y
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

        checkCollisionLifePack();
        checkCollisionAmmo();
        checkCollisionGun();
        if(isDamaged) {
        	damageFrames ++;
        	if(damageFrames == 20) {
        		damageFrames = 0;
        		isDamaged = false;
        	}
        }
        
        Camera.x = Camera.clamp(getX() - Game.WIDTH / 2, 0, Math.max(0, World.WIDTH * World.TILE_SIZE - Game.WIDTH));
        Camera.y = Camera.clamp(getY() - Game.HEIGHT / 2, 0, Math.max(0, World.HEIGHT * World.TILE_SIZE - Game.HEIGHT));
    }
    
    public void checkCollisionAmmo() {
    	for(int i = 0; i < Game.entities.size(); i++) {
    		Entity atual = Game.entities.get(i);
    		if (atual instanceof Bullet) {
    			if (this.getMask().intersects(atual.getBounds())) {
    				ammo++;
    				if (ammo > maxammo) {
                        ammo = maxammo;
                    }
    				Game.entities.remove(atual);
    				return;
    			}
    		}
    	}
    }
    public void checkCollisionLifePack() {
    	for(int i = 0; i < Game.entities.size(); i++) {
    		Entity atual = Game.entities.get(i);
    		if (atual instanceof LifePack) {
    			if (this.getMask().intersects(atual.getBounds())) {
    				life = Math.min(life + 1, maxlife);
    				Game.entities.remove(atual);
    				return;
    			}
    		}
    	}
    }
    public void checkCollisionGun() {
    	for(int i = 0; i < Game.entities.size(); i++) {
    		Entity atual = Game.entities.get(i);
    		if (atual instanceof Weapon) {
    			if (this.getMask().intersects(atual.getBounds())) {
    				hasGun = true;
    				Game.entities.remove(atual);
    				return;
    			}
    		}
    	}
    }
    private boolean canMove(int nx, int ny) {
        Rectangle nextMask = new Rectangle(nx + maskx, ny + masky, maskw, maskh);

        // Mapa
        if (!World.isFree(nx, ny)) {
            return false;
        }

        // Inimigos: evita atravessar. Se estiver em cooldown de dano, allow walking through (opcional)
        for (Entity e : Game.entities) {
            if (e instanceof Enemy) {
                if (damageCooldown > 0) {
					continue; // se estiver invulnerável, não bloqueia
				}
                Enemy enemy = (Enemy) e;
                if (nextMask.intersects(enemy.getMask())) {
                    return false;
                }
            }
        }
        return true;
    }

    // chamável por Enemy quando intersecta
    public void takeDamage() {
        if (damageCooldown == 0) {
            life--;
            isDamaged = true;
            damageCooldown = DAMAGE_DELAY;
            System.out.println("PLAYER HIT! Vida atual: " + life);
            if (life <= 0) {
                //restartGame();
            }
        }
    }
    
    // RESETA O GAME
    public static void restartGame() {

        // limpa entidades
        Game.entities.clear();

        // recria player
        Game.player = new Player(0, 0, 16, 16,
                Game.spritesheet.getSprite(32, 0, 16, 16));

        Player.life = Player.maxlife;

        Game.entities.add(Game.player);

        // recria mundo inicial
        Game.world = new World("/map_house.png", WorldType.HOUSE);
    }

    public Rectangle getMask() {
        return new Rectangle(getX() + maskx, getY() + masky, maskw, maskh);
    }

    
    @Override
    public void render(Graphics g) {
        
        if (!isDamaged) {
        	BufferedImage img = (dir == 0 ? rightPlayer[index] : leftPlayer[index]);
            g.drawImage(img, getX() - Camera.x, getY() - Camera.y, null);
            if(hasGun && dir == 0) {
            	g.drawImage(Entity.WEAPON_RIGHT, this.getX() + 2 - Camera.x, this.getY() +2 - Camera.y, null);
            }else if(hasGun && dir == 1) {
            	g.drawImage(Entity.WEAPON_LEFT, this.getX()-2 - Camera.x, this.getY() + 2 - Camera.y, null);
            }
        }else {
        	g.drawImage(playerDamage, getX() - Camera.x, getY() - Camera.y, null);
        }
//        // DEBUG HITBOX
//        Rectangle r = getMask();
//        g.drawRect(r.x - Camera.x, r.y - Camera.y, r.width, r.height);
    }
}
