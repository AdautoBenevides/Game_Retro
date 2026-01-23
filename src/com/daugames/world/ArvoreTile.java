package com.daugames.world;

import java.awt.Graphics;
import java.awt.Rectangle;

public class ArvoreTile {
	
	private int x, y;
	private int tree_type = 1;

    public ArvoreTile(int x, int y, int tree_type) {
        this.x = x;
        this.y = y;
        this.tree_type = tree_type;
    }

    public void render(Graphics g) {

        // CIMA
    	if (tree_type == 1) {
    		g.drawImage(Tile.ARVORE_TL, x - Camera.x, y - Camera.y, null);
    		g.drawImage(Tile.ARVORE_TR, x + 16 - Camera.x, y - Camera.y, null);
    		
    		// BAIXO	
    		g.drawImage(Tile.ARVORE_BL, x - Camera.x, y + 16 - Camera.y, null);
    		g.drawImage(Tile.ARVORE_BR, x + 16 - Camera.x, y + 16 - Camera.y, null);
    		
    	}else if(tree_type == 2) {
    		g.drawImage(Tile.ARVORE2_TL, x - Camera.x, y - Camera.y, null);
    		g.drawImage(Tile.ARVORE2_TR, x + 16 - Camera.x, y - Camera.y, null);
    		
    		// BAIXO	
    		g.drawImage(Tile.ARVORE2_BL, x - Camera.x, y + 16 - Camera.y, null);
    		g.drawImage(Tile.ARVORE2_BR, x + 16 - Camera.x, y + 16 - Camera.y, null);
    	}
    }

    // 🔒 COLISÃO 32x32
    public Rectangle getBounds() {
        return new Rectangle(x, y, 32, 32);
    }
}
