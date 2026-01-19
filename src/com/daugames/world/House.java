package com.daugames.world;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Representa uma house "pré-fabricada" que é desenhada sobre o mapa principal.
 * A imagem do spritesheet da casa deve estar em resources e cada tile tem 16x16.
 * Procura automaticamente por um pixel amarelo (o "brilho" da porta) para localizar a porta.
 */
public class House {

    private final int anchorX; // pixel world x onde a casa começa (tile 0,0) em pixels
    private final int anchorY; // pixel world y
    private final BufferedImage sheet;
    private final int tilesW;
    private final int tilesH;
    private final BufferedImage[][] tiles;

    // coordenadas do tile da porta (local na imagem da casa, em tiles)
    private int doorLocalTileX = -1;
    private int doorLocalTileY = -1;

    public House(int anchorX, int anchorY, String resourcePath) throws IOException {
        this.anchorX = anchorX;
        this.anchorY = anchorY;

        sheet = ImageIO.read(getClass().getResource(resourcePath));
        if (sheet == null) {
			throw new IOException("House tilesheet not found: " + resourcePath);
		}

        tilesW = sheet.getWidth() / Tile.TILE_SIZE;
        tilesH = sheet.getHeight() / Tile.TILE_SIZE;

        tiles = new BufferedImage[tilesW][tilesH];

        // extrai cada tile 16x16
        for (int ty = 0; ty < tilesH; ty++) {
            for (int tx = 0; tx < tilesW; tx++) {
                tiles[tx][ty] = sheet.getSubimage(tx * Tile.TILE_SIZE, ty * Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE);
            }
        }

        // tenta encontrar pixel "amarelo" (o destaque / brilho sob a porta) para definir a porta
        detectDoorFromHighlight();

        // se não encontrou, usa default: centro inferior
        if (doorLocalTileX == -1) {
            doorLocalTileX = tilesW / 2;
            doorLocalTileY = tilesH - 2; // um pouco acima do chão (ajuste se necessário)
        }
    }

    private void detectDoorFromHighlight() {
        int sw = sheet.getWidth();
        int sh = sheet.getHeight();
        int[] pixels = new int[sw * sh];
        sheet.getRGB(0, 0, sw, sh, pixels, 0, sw);

        // busca por um pixel "amarelo" aproximado: r alto, g alto, b baixo
        outer:
        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
                int rgb = pixels[x + y * sw];
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (isYellowish(r, g, b)) {
                    doorLocalTileX = x / Tile.TILE_SIZE;
                    doorLocalTileY = y / Tile.TILE_SIZE;
                    break outer;
                }
            }
        }
    }

    private boolean isYellowish(int r, int g, int b) {
        // critério permissivo para amarelo / brilho (ajuste se necessário)
        return r >= 180 && g >= 140 && b <= 100 && (r - b) > 60;
    }

    public int getTilesW() {
        return tilesW;
    }

    public int getTilesH() {
        return tilesH;
    }

    public int getAnchorX() {
        return anchorX;
    }

    public int getAnchorY() {
        return anchorY;
    }

    public Rectangle getBounds() {
        return new Rectangle(anchorX, anchorY, tilesW * Tile.TILE_SIZE, tilesH * Tile.TILE_SIZE);
    }

    /**
     * Retorna a posição em pixels (mundo) do tile da porta.
     */
    public int getDoorWorldX() {
        return anchorX + doorLocalTileX * Tile.TILE_SIZE;
    }

    public int getDoorWorldY() {
        return anchorY + doorLocalTileY * Tile.TILE_SIZE;
    }

    /**
     * Renderiza a casa no mundo, em pixels (ancorada em anchorX/anchorY).
     */
    public void render(Graphics g) {
        for (int ty = 0; ty < tilesH; ty++) {
            for (int tx = 0; tx < tilesW; tx++) {
                BufferedImage img = tiles[tx][ty];
                if (img != null) {
                    g.drawImage(img, anchorX + tx * Tile.TILE_SIZE - Camera.x,
                                   anchorY + ty * Tile.TILE_SIZE - Camera.y,
                                   null);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "House[anchor=(" + anchorX + "," + anchorY + "), tiles=" + tilesW + "x" + tilesH + ", doorLocal=(" + doorLocalTileX + "," + doorLocalTileY + ")]";
    }
}
