package com.daugames.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.daugames.entities.CharacterType;
import com.daugames.entities.Player;
import com.daugames.world.GameState;
import com.daugames.world.World;
import com.daugames.world.WorldType;

public class Menu {

    // opções do menu principal (exibidas)
    private final String[] options = { "novo jogo", "carregar jogo", "sair" };
    private int currentOption = 0;

    // estado do menu
    private enum Mode { MAIN, CHAR_SELECT }
    private Mode mode = Mode.MAIN;

    // seleção de personagem
    private int charIndex = 0; // 0 = menino, 1 = menina

    // controles (setados externamente pela leitura de teclas)
    public boolean up, down, left, right, enter;

    // se for pause ou menu principal (para escrever "continuar")
    public boolean pause = false;

    // --- coordenadas dos sprites no spritesheet (ajuste se preciso)
//    private static final int SPR_W = 16, SPR_H = 16;
//    // exemplo: o player padrão usado no seu Game era spritesheet.getSprite(32,0,16,16)
//    private static final int BOY_SPRITE_X = 32;
//    private static final int BOY_SPRITE_Y = 0;
//    // tente ajustar GIRL conforme seu spritesheet (ex.: 32, 32)
//    private static final int GIRL_SPRITE_X = 32;
//    private static final int GIRL_SPRITE_Y = 32;
    
    private BufferedImage menu_bg;
    private BufferedImage boy_image;
    private BufferedImage girl_image;

    // fontes

    private final Font titleFont = new Font("Serif", Font.BOLD, 36);
    private final Font optionFont = new Font("Arial", Font.BOLD, 24);
    private final Font hintFont = new Font("Arial", Font.PLAIN, 12);
    
    public Menu() {
    	 try {
 			menu_bg = ImageIO.read(getClass().getResource("/menu_bg.png"));
 			boy_image = ImageIO.read(getClass().getResource("/boy_image.png"));
 			girl_image = ImageIO.read(getClass().getResource("/girl_image.png"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    }

    public void update() {
        if (mode == Mode.MAIN) {
            handleMainInput();
        } else if (mode == Mode.CHAR_SELECT) {
            handleCharSelectInput();
        }
    }

    private void handleMainInput() {
        if (up) {
            up = false;
            currentOption--;
            if (currentOption < 0) {
				currentOption = options.length - 1;
			}
        }
        if (down) {
            down = false;
            currentOption++;
            if (currentOption > options.length - 1) {
				currentOption = 0;
			}
        }
        if (enter) {
            enter = false;
            String opt = options[currentOption];
            if ("novo jogo".equals(opt) || "continuar".equals(opt)) {
                // abre seleção de personagem
                mode = Mode.CHAR_SELECT;
            } else if ("carregar jogo".equals(opt)) {
                // TODO: implementar carregar
                System.out.println("Carregar jogo (não implementado)");
            } else if ("sair".equals(opt)) {
                System.exit(0);
            }
        }
    }

    private void handleCharSelectInput() {
        if (left) {
            left = false;
            charIndex--;
            if (charIndex < 0) {
				charIndex = 1;
			}
        }
        if (right) {
            right = false;
            
            charIndex++;
            if (charIndex > 1) {
				charIndex = 0;
			}
        }
        if (enter) {
            enter = false;
            // confirma escolha: cria player, inicia mundo (entra na casa)
            Game.selectedCharacter = (charIndex == 0) 
                    ? CharacterType.BOY 
                    : CharacterType.GIRL;

            startNewGame();
            mode = Mode.MAIN;
            pause = false;
            Game.state = GameState.IN_GAME;
        }
    }

    private void startNewGame() {
    	
        Game.entities.clear();
        Game.bullets.clear();
        
        Player.life = Player.maxlife;
        Player.ammo = Player.maxammo;
        
        Game.player = new Player(0, 0, 16, 16);
        Game.entities.add(Game.player);

        Game.world = new World("/map_house.png", WorldType.HOUSE);
    }


    public void render(Graphics g) {
        // fundo simples com moldura
        int sw = Game.WIDTH * Game.SCALE;
        int sh = Game.HEIGHT * Game.SCALE;

        // fundo escuro
//        g.setColor(new Color(10, 10, 17));
//        g.fillRect(0, 0, sw, sh);
        
        g.drawImage(menu_bg,0,0, Game.WIDTH * Game.SCALE, Game.HEIGHT * Game.SCALE, null);

        // título
        g.setFont(titleFont);
        g.setColor(new Color(120, 200, 120));
        String title = "> adventure sla <";
        int tx = (sw - g.getFontMetrics().stringWidth(title)) / 2;
        g.drawString(title, tx, 80);

        if (mode == Mode.MAIN) {
            renderMainMenu(g, sw, sh);
        } else if (mode == Mode.CHAR_SELECT) {
            renderCharSelect(g, sw, sh);
        }

        // rodapé: dicas
        g.setFont(hintFont);
        g.setColor(Color.LIGHT_GRAY);
        String hint = "↑ ↓ mover  •  ENTER selecionar  •  ← → trocar personagem (no menu)";
        g.drawString(hint, (sw - g.getFontMetrics().stringWidth(hint)) / 2, sh - 10);
    }

    private void renderMainMenu(Graphics g, int sw, int sh) {
        g.setFont(optionFont);
        int baseX = sw / 2 - 80;
        int baseY = sh / 2 - 20;
        int lineHeight = 50;

        // mostra "Continuar" se for pause
        String opt0 = (pause ? "continuar" : "novo jogo");
        g.setColor(Color.WHITE);
        g.drawString(opt0, baseX, baseY + 0 * lineHeight);
        g.drawString("carregar jogo", baseX, baseY + 1 * lineHeight);
        g.drawString("sair", baseX, baseY + 2 * lineHeight);

        // cursor (espada estilizada -> aqui simples '>')
        g.setColor(new Color(220, 200, 50));
        int cursorX = baseX - 40;
        int cursorY = baseY + currentOption * lineHeight;
        g.drawString(">", cursorX, cursorY);
    }
    private void renderCharSelect(Graphics g, int sw, int sh) {

        int boxW = 700;
        int boxH = 420;
        int bx = (sw - boxW) / 2;
        int by = (sh - boxH) / 2;

        // fundo do painel
        g.setColor(new Color(30, 30, 40));
        g.fillRect(bx, by, boxW, boxH);

        g.setFont(optionFont);
        g.setColor(Color.WHITE);
        g.drawString("Escolha seu herói", bx + 20, by + 40);

        // ===== TAMANHO DAS IMAGENS =====
        int imgSize = 220; // diminui levemente dos 250 para caber bonito
        int padding = 30;

        int leftX = bx + padding;
        int rightX = bx + boxW - imgSize - padding;
        int imgY = by + 90;

        Color selected = new Color(230, 200, 70); // amarelo
        Color normal = new Color(90, 90, 90);     // cinza

        // ===== MENINO =====
        g.setColor(charIndex == 0 ? selected : normal);
        g.fillRect(leftX - 15, imgY - 15, imgSize + 30, imgSize + 30);

        g.drawImage(boy_image, leftX, imgY, imgSize, imgSize, null);

        // ===== MENINA =====
        g.setColor(charIndex == 1 ? selected : normal);
        g.fillRect(rightX - 15, imgY - 15, imgSize + 30, imgSize + 30);

        g.drawImage(girl_image, rightX, imgY, imgSize, imgSize, null);

        // nomes
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Menino", leftX + 60, imgY + imgSize + 30);
        g.drawString("Menina", rightX + 60, imgY + imgSize + 30);

        // dica
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Use ← → para escolher. ENTER para confirmar.", bx + 20, by + boxH - 20);
    }

}
