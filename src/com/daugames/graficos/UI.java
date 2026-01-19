package com.daugames.graficos;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.daugames.entities.Player;
import com.daugames.main.Game;

public class UI {

    public void render(Graphics g) {

        if (Game.spritesheet == null) {
			return;
		}

        BufferedImage heart = Game.spritesheet.getSprite(
            128, 0, 16, 16 // ajuste para o seu lifepack
        );
        
        BufferedImage ammo = Game.spritesheet.getSprite(
            144, 0, 16, 16 // ajuste para o seu lifepack
        );

        int x = 0;
        int y = 0;
        int spacing = 10;
        
        int x_ammo = Game.WIDTH - 10 - 16;
        int y_ammo = 10;
        

        for (int i = 0; i < Player.life; i++) {
            g.drawImage(
                heart,
                x + i * spacing,
                y,
                16,
                16,
                null
            );
        }
        for (int i = 0; i < Player.ammo; i++) {
            g.drawImage(
                ammo,
                x_ammo - i * spacing,
                y_ammo,
                16,
                16,
                null
            );
        }
    }
}
