package com.daugames.world;

import java.awt.Graphics;
import java.awt.Rectangle;

public class EscrivaninhaTile {
    private int x, y;

    public EscrivaninhaTile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(Graphics g) {

        // CIMA
        g.drawImage(Tile.ESCRIVANINHA_TL, x - Camera.x, y - Camera.y, null);
        g.drawImage(Tile.ESCRIVANINHA_TR, x + 16 - Camera.x, y - Camera.y, null);

        // BAIXO	
        g.drawImage(Tile.ESCRIVANINHA_BL, x - Camera.x, y + 16 - Camera.y, null);
        g.drawImage(Tile.ESCRIVANINHA_BR, x + 16 - Camera.x, y + 16 - Camera.y, null);
    }

    // 🔒 COLISÃO 32x32
    public Rectangle getBounds() {
        return new Rectangle(x, y, 32, 32);
    }
}

