package com.daugames.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class House {

    private final int anchorX;
    private final int anchorY;
    private final BufferedImage sheet;

    // colisão real da casa (pixel perfect)
    private final List<Area> collisionAreas = new ArrayList<>();

    // porta
    private Rectangle doorArea;
    private int doorLocalTileX = -1;
    private int doorLocalTileY = -1;

    public static boolean showCollisionDebug = false;
    
    private final int houseId;
    private final String interiorMapPath;

    // ---------------------------------------------------------

    // Construtor COMPLETO (usado para casas com interior personalizado)
    public House(int anchorX, int anchorY, String resourcePath, 
                 String interiorPath, int houseId) throws IOException {
        this(anchorX, anchorY, resourcePath, interiorPath, houseId, -1, -1);
    }

    // Construtor com override de porta (se precisar)
    public House(int anchorX, int anchorY, String resourcePath,
                 String interiorPath, int houseId,
                 int overrideDoorTileX, int overrideDoorTileY) throws IOException {

        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.interiorMapPath = interiorPath;
        this.houseId = houseId;

        sheet = ImageIO.read(getClass().getResource(resourcePath));
        if (sheet == null) {
            throw new IOException("House image not found: " + resourcePath);
        }

        if (overrideDoorTileX >= 0 && overrideDoorTileY >= 0) {
            doorLocalTileX = overrideDoorTileX;
            doorLocalTileY = overrideDoorTileY;
        } else {
            detectDoorFromHighlight();
        }

        createCollisionFromBitmap();
    }
    
    // Getters para os novos campos
    public int getHouseId() {
        return houseId;
    }
    
    public String getInteriorMapPath() {
        return interiorMapPath;
    }

    // ---------------------------------------------------------
    // Detecta porta pelo pixel amarelo
    private void detectDoorFromHighlight() {
        int sw = sheet.getWidth();
        int sh = sheet.getHeight();

        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
                int rgb = sheet.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (r >= 180 && g >= 140 && b <= 110 && (r - b) > 60) {
                    doorLocalTileX = x / Tile.TILE_SIZE;
                    doorLocalTileY = y / Tile.TILE_SIZE;
                    return;
                }
            }
        }
    }

    // ---------------------------------------------------------
    // CRIA COLISÃO EXATA BASEADA NOS PIXELS DA IMAGEM
    private void createCollisionFromBitmap() {

        collisionAreas.clear();

        int sw = sheet.getWidth();
        int sh = sheet.getHeight();

        boolean[][] solid = new boolean[sw][sh];

        // máscara de pixels sólidos pelo alpha
        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
                int argb = sheet.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                solid[x][y] = alpha > 20;
            }
        }

        int[][] visited = new int[sw][sh];
        int label = 1;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {

                if (!solid[x][y] || visited[x][y] != 0) {
					continue;
				}

                Queue<Point> q = new LinkedList<>();
                List<Point> pixels = new ArrayList<>();

                q.add(new Point(x, y));
                visited[x][y] = label;

                while (!q.isEmpty()) {
                    Point p = q.poll();
                    pixels.add(p);

                    for (int k = 0; k < 4; k++) {
                        int nx = p.x + dx[k];
                        int ny = p.y + dy[k];

                        if (nx >= 0 && nx < sw && ny >= 0 && ny < sh) {
                            if (solid[nx][ny] && visited[nx][ny] == 0) {
                                visited[nx][ny] = label;
                                q.add(new Point(nx, ny));
                            }
                        }
                    }
                }

                Area area = buildAreaFromPixels(pixels);
                if (!area.isEmpty()) {
                    collisionAreas.add(area);
                }

                label++;
            }
        }

        // cria área da porta em world coords
        doorArea = new Rectangle(getDoorWorldX(), getDoorWorldY(),
                Tile.TILE_SIZE, Tile.TILE_SIZE);
    }

    // otimização: cria Area agrupando pixels contíguos horizontalmente
    private Area buildAreaFromPixels(List<Point> pixels) {

        Map<Integer, List<Integer>> rows = new TreeMap<>();

        for (Point p : pixels) {
            rows.computeIfAbsent(p.y, k -> new ArrayList<>()).add(p.x);
        }

        Area area = new Area();

        for (Map.Entry<Integer, List<Integer>> e : rows.entrySet()) {
            int y = e.getKey();
            List<Integer> xs = e.getValue();
            Collections.sort(xs);

            int start = xs.get(0);
            int prev = start;

            for (int i = 1; i < xs.size(); i++) {
                int cur = xs.get(i);
                if (cur == prev + 1) {
                    prev = cur;
                } else {
                    addRun(area, start, prev, y);
                    start = cur;
                    prev = cur;
                }
            }
            addRun(area, start, prev, y);
        }

        return area;
    }

    public boolean playerIsOnDoor(Rectangle playerMask) {
        return doorArea != null && doorArea.intersects(playerMask);
    }

    
    private void addRun(Area area, int startX, int endX, int y) {
        Rectangle2D.Double rect = new Rectangle2D.Double(
                anchorX + startX,
                anchorY + y,
                endX - startX + 1,
                1
        );
        area.add(new Area(rect));
    }

    // ---------------------------------------------------------
    // COLISÃO
    public boolean collidesWith(Rectangle rect) {

        if (doorArea != null && doorArea.intersects(rect)) {
            return false;
        }

        for (Area a : collisionAreas) {
            if (a.getBounds2D().intersects(rect) && a.intersects(rect)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------
    // RENDER
    public void render(Graphics g) {
        g.drawImage(sheet, anchorX - Camera.x, anchorY - Camera.y, null);
        renderCollisionDebug(g);
    }

    private void renderCollisionDebug(Graphics g) {
        if (!showCollisionDebug) {
			return;
		}

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(255, 0, 0, 110));
        for (Area a : collisionAreas) {
            Area copy = (Area) a.clone();
            copy.transform(AffineTransform.getTranslateInstance(-Camera.x, -Camera.y));
            g2.fill(copy);
            g2.setColor(Color.RED);
            g2.draw(copy);
            g2.setColor(new Color(255, 0, 0, 110));
        }

        if (doorArea != null) {
            g2.setColor(new Color(0, 255, 0, 140));
            g2.fillRect(doorArea.x - Camera.x, doorArea.y - Camera.y,
                    doorArea.width, doorArea.height);
            g2.setColor(Color.GREEN);
            g2.drawRect(doorArea.x - Camera.x, doorArea.y - Camera.y,
                    doorArea.width, doorArea.height);
        }
    }

    // ---------------------------------------------------------
    public int getDoorWorldX() {
        return anchorX + doorLocalTileX * Tile.TILE_SIZE;
    }

    public int getDoorWorldY() {
        return anchorY + doorLocalTileY * Tile.TILE_SIZE;
    }

    public Rectangle getDoorArea() {
        return doorArea;
    }
}