package com.daugames.world;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.daugames.main.Game;

public class Tile {

    // ===== TAMANHO PADRÃO =====
    public static final int TILE_SIZE = 16;

    // ===== MUNDO =====
    public static BufferedImage TILE_FLOOR = Game.spritesheet.getSprite(0, 0, 16, 16);
    public static BufferedImage TILE_WAY = Game.spritesheet.getSprite(64, 80, 16, 16);
    public static BufferedImage TILE_WALL  = Game.spritesheet.getSprite(16, 0, 16, 16);
    
    // ===== ARVORE 1 ====
    public static BufferedImage ARVORE_TL = Game.spritesheet.getSprite(96, 128, 16, 16);
    public static BufferedImage ARVORE_TR = Game.spritesheet.getSprite(112, 128, 16, 16);
    public static BufferedImage ARVORE_BL = Game.spritesheet.getSprite(96, 144, 16, 16);
    public static BufferedImage ARVORE_BR =  Game.spritesheet.getSprite(112, 144, 16, 16);
    
    // ===== ARVORE 2 ====
    public static BufferedImage ARVORE2_TL = Game.spritesheet.getSprite(128, 128, 16, 16);
    public static BufferedImage ARVORE2_TR = Game.spritesheet.getSprite(144, 128, 16, 16);
    public static BufferedImage ARVORE2_BL = Game.spritesheet.getSprite(128, 144, 16, 16);
    public static BufferedImage ARVORE2_BR =  Game.spritesheet.getSprite(144, 144, 16, 16);
    
    // ===== CASA =====
    public static BufferedImage TILE_FLOOR_HOUSE = Game.spritesheet.getSprite(0, 16, 16, 16);
    public static BufferedImage TILE_ENTRY = Game.spritesheet.getSprite(64, 64, 16, 16);
    public static BufferedImage TILE_CARPET = Game.spritesheet.getSprite(80, 64, 16, 16);
    public static BufferedImage TILE_CHEST = Game.spritesheet.getSprite(16, 16, 16, 16);
    public static BufferedImage PICTURE = Game.spritesheet.getSprite(48, 80, 16, 16);

    // ===== MESA =====
    public static BufferedImage MESA_TL = Game.spritesheet.getSprite(0, 32, 16, 16);
    public static BufferedImage MESA_TR = Game.spritesheet.getSprite(16, 32, 16, 16);
    public static BufferedImage MESA_BL = Game.spritesheet.getSprite(0, 48, 16, 16);
    public static BufferedImage MESA_BR = Game.spritesheet.getSprite(16, 48, 16, 16);

    // ===== PAREDES CASA =====
    public static BufferedImage WALL_TL = Game.spritesheet.getSprite(0, 64, 16, 16);
    public static BufferedImage WALL_TOP = Game.spritesheet.getSprite(32, 64, 16, 16);
    public static BufferedImage WALL_TR = Game.spritesheet.getSprite(16, 64, 16, 16);

    public static BufferedImage WALL_LEFT = Game.spritesheet.getSprite(0, 76, 16, 16);
    public static BufferedImage WALL_RIGHT = Game.spritesheet.getSprite(16, 76, 16, 16);

    public static BufferedImage WALL_BL = Game.spritesheet.getSprite(0, 80, 16, 16);
    public static BufferedImage WALL_BOTTOM = Game.spritesheet.getSprite(10, 80, 16, 16);
    public static BufferedImage WALL_BR = Game.spritesheet.getSprite(16, 80, 16, 16);

    public static BufferedImage TILE_PAINT = Game.spritesheet.getSprite(32, 80, 16, 16);
    
    public static BufferedImage HOUSE_WALL = Game.spritesheet.getSprite(48, 64, 16, 16);
    
    // ===== ESCRIVANINHA =====
    public static BufferedImage ESCRIVANINHA_TL = Game.spritesheet.getSprite(0, 96, 16, 16);
    public static BufferedImage ESCRIVANINHA_TR = Game.spritesheet.getSprite(16, 96, 16, 16);
    public static BufferedImage ESCRIVANINHA_BL = Game.spritesheet.getSprite(0, 112, 16, 16);
    public static BufferedImage ESCRIVANINHA_BR = Game.spritesheet.getSprite(16, 112, 16, 16);
    
    // ===== CAMA =====	
    public static BufferedImage BED_TL = Game.spritesheet.getSprite(0, 128, 16, 16);
    public static BufferedImage BED_TR = Game.spritesheet.getSprite(16, 128, 16, 16);
    public static BufferedImage BED_BL = Game.spritesheet.getSprite(0, 144, 16, 16);
    public static BufferedImage BED_BR = Game.spritesheet.getSprite(16, 144, 16, 16);
    
    
    // ===== ATRIBUTOS =====
    protected BufferedImage sprite;
    protected int x, y;
    protected boolean solid = false;

    public Tile(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
    }

    // ===== RENDER =====
    public void render(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x - Camera.x, y - Camera.y, null);
        }
    }

    // ===== COLISÃO =====
    public boolean isSolid() {
        return solid;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
    }
}
