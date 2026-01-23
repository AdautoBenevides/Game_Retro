package com.daugames.world;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.daugames.entities.Bullet;
import com.daugames.entities.Enemy;
import com.daugames.entities.EnemyType;
import com.daugames.entities.Entity;
import com.daugames.entities.LifePack;
import com.daugames.entities.Weapon;
import com.daugames.main.Game;

public class World {

    public static Tile[] tiles;
    public static int WIDTH, HEIGHT;
    public static final int TILE_SIZE = 16;

    // portas / casas / objetos
    public static int doorX = -1;
    public static int doorY = -1;
    public static List<House> houses;

    public static List<MesaTile> mesas;
    public static List<EscrivaninhaTile> escrivaninhas;
    public static List<BedTile> camas;
    public static List<ArvoreTile> arvores;
    public static List<Rectangle> doors;

    private WorldType type;

    public World(String path, WorldType type) {
        this.type = type;

        try {
            BufferedImage map = ImageIO.read(getClass().getResource(path));
            WIDTH = map.getWidth();
            HEIGHT = map.getHeight();

            tiles = new Tile[WIDTH * HEIGHT];
            mesas = new ArrayList<>();
            escrivaninhas = new ArrayList<>();
            camas = new ArrayList<>();
            doors = new ArrayList<>();
            houses = new ArrayList<>();
            arvores = new ArrayList<>();

            int[] pixels = new int[WIDTH * HEIGHT];
            map.getRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);

            for (int yy = 0; yy < HEIGHT; yy++) {
                for (int xx = 0; xx < WIDTH; xx++) {

                    int pos = xx + (yy * WIDTH);
                    int pixel = pixels[pos];
                    int wx = xx * TILE_SIZE;
                    int wy = yy * TILE_SIZE;

                    // default floor
                    tiles[pos] = new FloorTile(wx, wy, Tile.TILE_FLOOR);

                    if (type == WorldType.HOUSE) {

                        tiles[pos] = new FloorTile(wx, wy, Tile.TILE_FLOOR_HOUSE);

                        if (pixel == 0xFFFF0000) {
                            Game.player.setX(wx);
                            Game.player.setY(wy);
                        } else if (pixel == 0xFFFF6A00) {
                            mesas.add(new MesaTile(wx, wy));
                        } else if (pixel == 0xFF7F6A00) {
                            tiles[pos] = new WallTile(wx, wy, Tile.TILE_CHEST);
                        } else if (pixel == 0xFF7F3300) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_BOTTOM);
                        } else if (pixel == 0xFF404040) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_BL);
                        } else if (pixel == 0xFFD35400) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_LEFT);
                        } else if (pixel == 0xFF606060) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_TL);
                        } else if (pixel == 0xFF512000) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_TOP);
                        } else if (pixel == 0xFFC0C0C0) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_TR);
                        } else if (pixel == 0xFFB54800) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_RIGHT);
                        } else if (pixel == 0xFF303030) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_BR);
                        } else if (pixel == 0xFF0026FF) {
                            tiles[pos] = new WallTile(wx, wy, Tile.TILE_PAINT);
                        } else if (pixel == 0xFF260F00) {
                            tiles[pos] = new WallTile(wx, wy, Tile.HOUSE_WALL);
                        } else if (pixel == 0xFFFF3F00) {
                            tiles[pos] = new FloorTile(wx, wy, Tile.TILE_CARPET);
                        } else if (pixel == 0xFF3F1FFF) {
                            tiles[pos] = new FloorTile(wx, wy, Tile.TILE_ENTRY);
                            doors.add(new Rectangle(wx, wy, TILE_SIZE, TILE_SIZE));
                        } else if (pixel == 0xFF00EA13) {
                            tiles[pos] = new WallTile(wx, wy, Tile.WALL_BOTTOM);
                            escrivaninhas.add(new EscrivaninhaTile(wx, wy));
                        } else if (pixel == 0xFF2C5127) {
                            tiles[pos] = new FloorTile(wx, wy, Tile.PICTURE);
                        } else if (pixel == 0xFF4B51A0) {
                            camas.add(new BedTile(wx, wy));
                        }

                    } else { // MAIN / WORLD (externo) - aqui spawnam inimigos e itens

                        tiles[pos] = new FloorTile(wx, wy, Tile.TILE_FLOOR);

                        if (pixel == 0xFFFFFFFF) {
                            tiles[pos] = new WallTile(wx, wy, Tile.TILE_WALL);
                        } else if (pixel == 0xFF000000) {
                            tiles[pos] = new FloorTile(wx, wy, Tile.TILE_FLOOR);
                        } else if (pixel == 0xFFFB9C38) {
                            tiles[pos] = new FloorTile(wx, wy, Tile.TILE_WAY);
                        }
                        // arvores 
                        
                        if (pixel == 0xFF006605) {
                        	 tiles[pos] = new FloorTile(wx, wy, Tile.TILE_FLOOR);

                        	    // adiciona a árvore como objeto
                    	    arvores.add(new ArvoreTile(wx, wy, 1));
                        }else if (pixel == 0xFF003F02) {
                       	 	tiles[pos] = new FloorTile(wx, wy, Tile.TILE_FLOOR);

                 	    // adiciona a árvore como objeto
                       	 	arvores.add(new ArvoreTile(wx, wy, 2));
                        }
                        
                        // itens
                        if (pixel == 0xFFFFB27F) {
                            Game.entities.add(new LifePack(wx, wy, 16, 16, Entity.LIFEPACK_EN));
                        } else if (pixel == 0xFFFFD800) {
                            Game.entities.add(new Bullet(wx, wy, 16, 16, Entity.BULLET_EN));
                        } else if (pixel == 0xFFFF6A00) {
                            Game.entities.add(new Weapon(wx, wy, 16, 16, Entity.WEAPON_EN));
                        }

                        // Inimigo tipo 1 (FF0000)
                        if (pixel == 0xFF7F0000) {
                            // usa o sprite único (Entity.ENEMY_EN)
                        	Game.entities.add(
                        		    new Enemy(
                        		        xx * 16,
                        		        yy * 16,
                        		        16,
                        		        16,
                        		        Entity.ENEMY1_LEFT,
                        		        null,
                        		        4, 4, 8, 8, 
                        		        EnemyType.ENEMY1
                        		    )
                        		);

                        }

                        // Inimigo tipo 2 (7F0000)
                        if (pixel == 0xFFFF0000) {
                        	Game.entities.add(
                        		    new Enemy(
                        		        xx * 16,
                        		        yy * 16,
                        		        16,
                        		        16,
                        		        Entity.ENEMY2_LEFT,
                        		        Entity.ENEMY2_RIGHT,
                        		        4, 4, 8, 8,
                        		        EnemyType.ENEMY2
                        		    )
                        		);
                        }

                        // spawn do player no mapa do mundo
                        if (pixel == 0xFF00137F) {
                            Game.player.setX(wx);
                            Game.player.setY(wy);
                        }

                        // porta no mundo (visual)
                        if (pixel == 0xFF3F1FFF) {
                            tiles[pos] = new FloorTile(wx, wy, Tile.TILE_ENTRY);
                            doors.add(new Rectangle(wx, wy, TILE_SIZE, TILE_SIZE));
                        }

                        // âncora casa (exemplo)
                        if (pixel == 0xFF21007F) {
                            House house = new House(wx, wy, "/house_tiles.png");
                            houses.add(house);
                            doorX = house.getDoorWorldX();
                            doorY = house.getDoorWorldY();
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WorldType getType() {
        return this.type;
    }

    public static boolean isFree(int xnext, int ynext) {

        int x1 = xnext / TILE_SIZE;
        int y1 = ynext / TILE_SIZE;

        int x2 = (xnext + TILE_SIZE - 1) / TILE_SIZE;
        int y2 = y1;

        int x3 = x1;
        int y3 = (ynext + TILE_SIZE - 1) / TILE_SIZE;

        int x4 = x2;
        int y4 = y3;

        if (x1 < 0 || y1 < 0 || x4 >= WIDTH || y4 >= HEIGHT) {
            return false;
        }

        if (
            tiles[x1 + (y1 * WIDTH)].isSolid() ||
            tiles[x2 + (y2 * WIDTH)].isSolid() ||
            tiles[x3 + (y3 * WIDTH)].isSolid() ||
            tiles[x4 + (y4 * WIDTH)].isSolid()
        ) {
            return false;
        }

        for (MesaTile mesa : mesas) {
            if (mesa.getBounds().intersects(xnext, ynext, TILE_SIZE, TILE_SIZE)) {
                return false;
            }
        }

        for (EscrivaninhaTile escrivaninha : escrivaninhas) {
            if (escrivaninha.getBounds().intersects(xnext, ynext, TILE_SIZE, TILE_SIZE)) {
                return false;
            }
        }

        for (BedTile cama : camas) {
            if (cama.getBounds().intersects(xnext, ynext, TILE_SIZE, TILE_SIZE)) {
                return false;
            }
        }
        
        for (ArvoreTile arvore : arvores) {
            if (arvore.getBounds().intersects(xnext, ynext, TILE_SIZE, TILE_SIZE)) {
                return false;
            }
        }

        return true;
    }

    public void render(Graphics g) {

        int xstart = Camera.x / TILE_SIZE;
        int ystart = Camera.y / TILE_SIZE;

        int xfinal = xstart + (Game.WIDTH / TILE_SIZE) + 2;
        int yfinal = ystart + (Game.HEIGHT / TILE_SIZE) + 2;

        xstart = Math.max(0, xstart);
        ystart = Math.max(0, ystart);
        xfinal = Math.min(WIDTH, xfinal);
        yfinal = Math.min(HEIGHT, yfinal);

        for (int yy = ystart; yy < yfinal; yy++) {
            for (int xx = xstart; xx < xfinal; xx++) {
                tiles[xx + (yy * WIDTH)].render(g);
            }
        }

        for (MesaTile mesa : mesas) {
			mesa.render(g);
		}
        for (EscrivaninhaTile e : escrivaninhas) {
			e.render(g);
		}
        for (BedTile b : camas) {
			b.render(g);
		}

        for (House h : houses) {
			h.render(g);
		}
        for (ArvoreTile a : arvores) {
			a.render(g);
		}
    }

    // verifica se o player está sobre qualquer porta detectada neste world
    public static boolean playerOnDoor() {
        if (doors == null || doors.isEmpty()) {
			return false;
		}
        Rectangle pm = Game.player.getMask();
        for (Rectangle door : doors) {
            if (pm.intersects(door)) {
				return true;
			}
        }
        return false;
    }
}
