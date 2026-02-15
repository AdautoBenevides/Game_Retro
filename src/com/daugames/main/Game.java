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
    
    private House lastEnteredHouse = null;

    
    private BufferedImage image;

    public static List<Entity> entities;
    public static Spritesheet spritesheet;
    public static List<BulletShoot> bullets;
    
    public static World world;

    public static Player player;

    public static Random rand;
    
	public static Enemy enemies;

    public UI ui;
    
    public static GameState state = GameState.MENU;
    
    private BufferedImage go_girl;
    private BufferedImage go_boy;
    
    private boolean restartGame = false;
    
    public Menu menu;
    
    public static CharacterType selectedCharacter;

    // cooldown para evitar spam de entrada/saida
    private int doorCooldown = 0;
    private final int DOOR_COOLDOWN_FRAMES = 20;
    


    // ========== STATIC INITIALIZER ==========
    // Garante que o spritesheet esteja criado antes que outras classes (Tile, Entity, etc.)
    // acionem Game.spritesheet em seus blocos estáticos.
    static {
        try {
            spritesheet = new Spritesheet("/spritesheet.png");
            System.out.println("[Game] spritesheet criado no static init");
        } catch (Exception e) {
            // Se falhar aqui, tentamos novamente no construtor. Apenas log para debug.
            spritesheet = null;
            System.err.println("[Game] falha ao criar spritesheet no static init: " + e.getMessage());
        }
    }

    public Game() {
    	// se ainda não tivermos spritesheet (por algum motivo), cria aqui
    	if (spritesheet == null) {
            try {
                spritesheet = new Spritesheet("/spritesheet.png");
                System.out.println("[Game] spritesheet criado no construtor");
            } catch (Exception e) {
                System.err.println("[Game] ERRO criando spritesheet no construtor: " + e.getMessage());
                // continue; outras partes podem falhar, mas pelo menos mostramos o erro
            }
    	}

    	// tenta ligar música - se Sound estiver ok
    	try {
	    	Sound.MUSIC_BG.loop();
	    	Sound.MUSIC_BG.setVolume(-30.0f);
    	} catch (Throwable t) {
            // não falhar por causa do som
            System.err.println("[Game] Sound init falhou: " + t.getMessage());
    	}

        rand = new Random();
        addKeyListener(this);
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        initFrame();

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        ui = new UI(); // UI normalmente usa imagens do spritesheet (se usar)
        entities = new ArrayList<Entity>();
        bullets = new ArrayList<BulletShoot>();
        
        
        // Define um personagem padrão só para iniciar o jogo
        selectedCharacter = CharacterType.BOY;

        // Player criado (hitbox e sprites carregados no construtor de Player)
        player = new Player(0, 0, 16, 16);
        entities.add(player);

        // inicia no mapa
        world = new World("/house1_interior.png", WorldType.HOUSE);
       

        
        // ===== FIM DA SOLUÇÃO =====
        
        System.out.println("[Game] mundo inicializado: /map_house.png (HOUSE)");

        menu = new Menu();

        // carrega a imagem de game over (se existir)
        try {
			go_girl = ImageIO.read(getClass().getResource("/game_over_girl.png"));
			go_boy = ImageIO.read(getClass().getResource("/game_over_boy.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }

    public synchronized void start() {
        thread = new Thread(this);
        isRuning = true;
        thread.start();
        System.out.println("[Game] thread iniciada");
    }

    public synchronized void stop() {
        isRuning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("[Game] thread parada");
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
    public void enterHouse(House house) {
        System.out.println("[Game] enterHouse() id=" + house.getHouseId());
        lastEnteredHouse = house;
        changeWorld(house.getInteriorMapPath(), WorldType.HOUSE);
    }

    public void exitHouse() {
        System.out.println("[Game] exitHouse()");
        changeWorld("/map.png", WorldType.MAIN);
    }
    
    public void enterCity() {
        enterCity(null);
    }

    public void enterCity(House fromHouse) {
        System.out.println("[Game] enterCity()");
        changeWorld("/map_city.png", WorldType.CITY);
        if (fromHouse != null) {
            // posiciona o jogador exatamente na porta da casa correspondente
            player.setX(fromHouse.getDoorWorldX());
            player.setY(fromHouse.getDoorWorldY());
            Camera.x = Math.max(0, player.getX() - (WIDTH / 2));
            Camera.y = Math.max(0, player.getY() - (HEIGHT / 2) );
            lastEnteredHouse = null;
        } else {
            // usa a PRIMEIRA casa criada no mapa como padrão
            if (World.houses != null && !World.houses.isEmpty()) {
                House defaultHouse = World.houses.get(0);

                player.setX(defaultHouse.getDoorWorldX());
                player.setY(defaultHouse.getDoorWorldY());

                Camera.x = Math.max(0, player.getX() - (WIDTH / 2));
                Camera.y = Math.max(0, player.getY() - (HEIGHT / 2));
            }
        }

    }

    
    private void enterMain() {
        System.out.println("[Game] enterMain()");
        changeWorld("/map.png", WorldType.MAIN);
        // se quiser posicionar o jogador em um ponto fixo do MAIN você pode ajustar aqui,
        // por enquanto changeWorld já tentará posicionar o player num "door" do novo mapa.
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
		
	        // entrada/saída por porta (com cooldown)
			// dentro de update(), no bloco que trata portas
			if (doorCooldown == 0 && World.playerOnDoor()) {
			    System.out.println("[Game] World.playerOnDoor() == true, world type = " + (world != null ? world.getType() : "null"));
			    if (world != null) {
			        Rectangle pm = Game.player.getMask();
			        House houseAtDoor = World.getHouseAtPlayerDoor(pm); // novo helper (ver World.java abaixo)

			        if (world.getType() == WorldType.HOUSE) {
			            // saindo de uma casa -> voltar para CITY (e posicionar na casa correspondente, se soubermos)
			            if (lastEnteredHouse != null) {
			                enterCity(lastEnteredHouse);
			            } else {
			                // fallback: apenas entra na city
			                enterCity();
			            }
			        } else if (world.getType() == WorldType.CITY) {
			            // se for porta de uma house -> entrar nela
			            if (houseAtDoor != null) {
			                lastEnteredHouse = houseAtDoor;
			                enterHouse(houseAtDoor);  // <-- PASSA A CASA ESPECÍFICA
			            } else {
			                // provavelmente é a porta/estrada que leva ao MAIN
			                enterMain();
			            }
			        } else if (world.getType() == WorldType.MAIN) {
			            // voltar da MAIN para cidade
			            enterCity();
			        }
			    }
			    doorCooldown = DOOR_COOLDOWN_FRAMES;
			    System.out.println("[Game] doorCooldown set to " + doorCooldown);
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
        System.out.println("[Game] changeWorld -> " + path + "  type=" + type);

        if (World.doors != null && !World.doors.isEmpty()) {
            Rectangle d = World.doors.get(0); // por enquanto usa a primeira porta do mapa
            player.setX(d.x);
            player.setY(d.y);
            // centraliza câmera aproximadamente no player
            Camera.x = Math.max(0, d.x - (WIDTH / 2));
            Camera.y = Math.max(0, d.y - (HEIGHT / 2));
            System.out.println("[Game] player pos set to door tile: " + d);
        } else if (World.houses != null && !World.houses.isEmpty()) {
            House h = World.houses.get(0); // fallback: primeira house encontrada
            Rectangle da = h.getDoorArea();
            if (da != null) {
                player.setX(da.x);
                player.setY(da.y);
                Camera.x = Math.max(0, da.x - (WIDTH / 2));
                Camera.y = Math.max(0, da.y - (HEIGHT / 2));
                System.out.println("[Game] player pos set to house door: " + da);
            }
        } else {
            // fallback genérico: mantém player onde o World anterior deixou (ou 0,0)
            Camera.x = Math.max(0, player.getX() - (WIDTH / 2));
            Camera.y = Math.max(0, player.getY() - (HEIGHT / 2));
            System.out.println("[Game] changeWorld fallback camera centering used");
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
            System.out.println("[Game] House.showCollisionDebug = " + House.showCollisionDebug);
        }
    }
}
