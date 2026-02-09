package com.daugames.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.daugames.entities.BulletShoot;
import com.daugames.entities.CharacterType;
import com.daugames.entities.Enemy;
import com.daugames.entities.Entity;
import com.daugames.entities.Player;
import com.daugames.graficos.Spritesheet;
import com.daugames.graficos.UI;
import com.daugames.world.Camera;
import com.daugames.world.GameState;
import com.daugames.world.House;
import com.daugames.world.World;
import com.daugames.world.WorldType;

public class Game extends Canvas implements Runnable, KeyListener {

    private static final long serialVersionUID = 1L;
    public static JFrame frame;
    public static final int WIDTH = 240;
    public static final int HEIGHT = 160;
    public static final int SCALE = 4;
    private Thread thread;
    private boolean isRuning = true;

    private BufferedImage image;

    public static List<Entity> entities;
    public static Spritesheet spritesheet;
    public static List<BulletShoot> bullets;
    
    public static World world;

    public static Player player;

    public static Random rand;
    
	public static Enemy enemies;

    public UI ui;
    
    public static GameState state = GameState.IN_GAME;
    
    private BufferedImage go_girl;
    private BufferedImage go_boy;
    
    private boolean restartGame = false;
    
    public Menu menu;
    
    public static CharacterType selectedCharacter;

    // cooldown para evitar spam de entrada/saida
    private int doorCooldown = 0;
    private final int DOOR_COOLDOWN_FRAMES = 20;

    public Game() {
    	Sound.MUSIC_BG.loop();
    	Sound.MUSIC_BG.setVolume(-30.0f);
        rand = new Random();
        addKeyListener(this);
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        initFrame();

        // Inicializando recursos na ordem correta:
        spritesheet = new Spritesheet("/spritesheet.png"); // primeiro spritesheet
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        ui = new UI(); // UI normalmente usa imagens do spritesheet (se usar)
        entities = new ArrayList<Entity>();
        bullets = new ArrayList<BulletShoot>();
        
        
        // Define um personagem padrão só para iniciar o jogo
        selectedCharacter = CharacterType.BOY;

        // Player criado SEM sprite (ele decide sozinho)
        player = new Player(0, 0, 16, 16);
        entities.add(player);

        // inicia no mapa
        world = new World("/map_house.png", WorldType.HOUSE);

        menu = new Menu();


        
        //carrega a imagem de game over
        try {
			go_girl = ImageIO.read(getClass().getResource("/game_over_girl.png"));
			go_boy = ImageIO.read(getClass().getResource("/game_over_boy.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    public synchronized void start() {
        thread = new Thread(this);
        isRuning = true;
        thread.start();
    }

    public synchronized void stop() {
        isRuning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void initFrame() {
        frame = new JFrame("Game #1");
        frame.add(this);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
    }

    // entrar / sair — recria World (World ajusta spawn automaticamente)
    public void enterHouse() {
        changeWorld("/map_house.png", WorldType.HOUSE);
    }

    public void exitHouse() {
        changeWorld("/map.png", WorldType.MAIN);
    }
    
    public void enterCity() {
    	changeWorld("/map_city.png", WorldType.CITY);
    	Camera.x = 0;
    	Camera.y = 0;

    }
    
    private void enterMain() {
        world = new World("/map.png", WorldType.MAIN);
        player.setX(100);
        player.setY(100);
    }


    public static void main(String args[]) {
        Game game = new Game();
        game.start();
    }
    
    public void update() {
    	if (state == GameState.IN_GAME) {
			if (doorCooldown > 0) {
				doorCooldown--;
			}
		

	        if (doorCooldown == 0 && World.playerOnDoor()) {
	    	  if (world != null) {
	
	    		  if (World.playerOnDoor()) {

	    			    if (world.getType() == WorldType.HOUSE) {

	    			        enterCity();      // HOUSE → CITY

	    			    } else if (world.getType() == WorldType.CITY) {

	    			        enterMain();      // CITY → MAIN

	    			    } else if (world.getType() == WorldType.MAIN) {

	    			        enterCity();      // MAIN → CITY
	    			    }
	    			}


	    	    }
	            doorCooldown = DOOR_COOLDOWN_FRAMES; 
	        }
	
	        for (int i = 0; i < entities.size(); i++) { 
	            Entity e = entities.get(i);
	            e.update();
	        } 
	        
	        for(int i = 0; i<bullets.size(); i++ ) {
	        	bullets.get(i).update();
	        }
    	}else if(state == GameState.MENU) {
    		menu.update();
    	}
    } 

    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        // ===== desenha no buffer interno =====
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if (state == GameState.IN_GAME) {

            world.render(g);

            for (Entity e : entities) {
                e.render(g);
            }

            for (BulletShoot b : bullets) {
                b.render(g);
            }

            ui.render(g);

        }

        g.dispose();

        // ===== desenha na tela (com escala) =====
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);

        // ===== GAME OVER por cima =====
        if (state == GameState.GAME_OVER) {
            Graphics2D g2 = (Graphics2D) g;
            int goWidth = 	WIDTH * SCALE;
            int goHeigth = HEIGHT *SCALE;

            if(selectedCharacter == CharacterType.GIRL) {
				g2.drawImage(
				    go_girl,0, 0,
				    goWidth,
				    goHeigth,
				    null
				);
			}else if(selectedCharacter == CharacterType.BOY) {
				g2.drawImage(go_boy, 0, 0, goWidth, goHeigth,null);
			}
            
            if(restartGame) {
            	this.restartGame = false;
            	state = GameState.IN_GAME;
            	Player.restartGame();
            }
        } else if(state == GameState.MENU) {
        	menu.render(g);
        	
        }

        g.dispose();
        bs.show();
    }

    public void changeWorld(String path, WorldType type) {

        // remove tudo EXCETO o player
        entities.removeIf(e -> !(e instanceof Player));

        // cria novo mundo (ele vai spawnar inimigos do mapa)
        world = new World(path, type);

        // --- posiciona o player em uma "porta" do novo mapa, se houver ---
        // Preferência: portas maptiles (World.doors). Se não tiver, tenta portas das casas.
        if (World.doors != null && !World.doors.isEmpty()) {
            Rectangle d = World.doors.get(0); // por enquanto usa a primeira porta do mapa
            player.setX(d.x);
            player.setY(d.y);
            // centraliza câmera aproximadamente no player
            Camera.x = Math.max(0, d.x - (WIDTH / 2));
            Camera.y = Math.max(0, d.y - (HEIGHT / 2));
        } else if (World.houses != null && !World.houses.isEmpty()) {
            House h = World.houses.get(0); // fallback: primeira house encontrada
            Rectangle da = h.getDoorArea();
            if (da != null) {
                player.setX(da.x);
                player.setY(da.y);
                Camera.x = Math.max(0, da.x - (WIDTH / 2));
                Camera.y = Math.max(0, da.y - (HEIGHT / 2));
            }
        } else {
            // fallback genérico: mantém player onde o World anterior deixou (ou 0,0)
            Camera.x = Math.max(0, player.getX() - (WIDTH / 2));
            Camera.y = Math.max(0, player.getY() - (HEIGHT / 2));
        }
    }



    @Override
    public void run() {
        long LasTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int frames = 0;
        double timer = System.currentTimeMillis();
        requestFocus();
        while (isRuning) {
            long now = System.nanoTime();
            delta += (now - LasTime) / ns;
            LasTime = now;
            if (delta >= 1) {
                update();
                render();
                frames++;
                delta--;
            }

            if (System.currentTimeMillis() - timer >= 1000) {
//                System.out.println("FPS:" + frames);
                frames = 0;
                timer += 1000;
            }
        }
        stop();
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        // tecla alternativa para entrar/ sair
        if ((e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ENTER)) {
            if (World.playerOnDoor()) {
                if (world != null && world.getType() == WorldType.MAIN) {
                    enterHouse();
                } else {
                    exitHouse();
                }
                doorCooldown = DOOR_COOLDOWN_FRAMES;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            player.right = true;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            player.left = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            player.up = true;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            player.down = true;
        }
        
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
        	player.isShooting = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            player.right = false;
            if (state == GameState.MENU){
            	menu.right = true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            player.left = false;
            if (state == GameState.MENU){
            	menu.left = true;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            player.up = false;
            
            if(state == GameState.MENU) {
            	menu.up = true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            player.down = false;
            
            if(state == GameState.MENU) {
            	menu.down = true;
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        	
        	if(state == GameState.GAME_OVER) {        		
        		restartGame = true;
        	}
        	
        	if(state == GameState.MENU) {
        		menu.enter = true;
        	}
        	
        }
        
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        	state = GameState.MENU;
        	menu.pause = true;
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F1) {
            House.showCollisionDebug = !House.showCollisionDebug;
        }
    }
}
