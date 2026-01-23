package com.daugames.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.daugames.entities.BulletShoot;
import com.daugames.entities.Enemy;
import com.daugames.entities.Entity;
import com.daugames.entities.Player;
import com.daugames.graficos.Spritesheet;
import com.daugames.graficos.UI;
import com.daugames.world.Camera;
import com.daugames.world.World;
import com.daugames.world.WorldType;

public class Game extends Canvas implements Runnable, KeyListener {

    private static final long serialVersionUID = 1L;
    public static JFrame frame;
    public static final int WIDTH = 240;
    public static final int HEIGHT = 160;
    private final int SCALE = 4;
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

    // cooldown para evitar spam de entrada/saida
    private int doorCooldown = 0;
    private final int DOOR_COOLDOWN_FRAMES = 20;

    public Game() {
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
        
        // Player criado e adicionado
        player = new Player(0, 0, 16, 16, spritesheet.getSprite(32, 0, 16, 16));
        entities.add(player);

        // inicia no mapa principal para você ver inimigos por padrão
        world = new World("/map_house.png", WorldType.HOUSE);
        
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
    	world = new World("/map_city.png", WorldType.CITY);
    	Camera.x = 0;
    	Camera.y = 0;

    }


    public static void main(String args[]) {
        Game game = new Game();
        game.start();
    }
    
    public void update() {
        if (doorCooldown > 0) {
			doorCooldown--;
		}

        if (doorCooldown == 0 && World.playerOnDoor()) {
    	  if (world != null) {

    	        if (world.getType() == WorldType.MAIN) {

    	            // MAIN → HOUSE
    	            enterHouse();

    	        } else if (world.getType() == WorldType.HOUSE) {

    	            // HOUSE → CITY
    	            enterCity();

    	        } else if (world.getType() == WorldType.CITY) {

    	            // CITY → HOUSE (ou MAIN, você decide)
    	            enterHouse(); // ou enterMain()
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
    } 

    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = image.getGraphics();
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        world.render(g);
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            e.render(g);
        }
        
        //renderizando as balas
        for(int i = 0; i<bullets.size(); i++ ) {
        	bullets.get(i).render(g);
        }
        
        ui.render(g);

        g.dispose();
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
        bs.show();
    }
    public void changeWorld(String path, WorldType type) {

        // remove tudo EXCETO o player
        entities.removeIf(e -> !(e instanceof Player));

        // cria novo mundo (ele vai spawnar inimigos do mapa)
        world = new World(path, type);
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
                System.out.println("FPS:" + frames);
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
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            player.left = false;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            player.up = false;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            player.down = false;
        }
    }
}
