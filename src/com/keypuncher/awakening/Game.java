package com.keypuncher.awakening;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import com.keypuncher.awakening.pathfinding.*;

public class Game {

	// System
	private boolean paused = false;
	private boolean inventory = false;
	private double tick = 0;

	private enum State {
		GAME, MAINMENU, INTRO
	}

	private State state = State.INTRO;

	// HUD
	private final static int INFOBARHEIGHT = 36;

	// Level
	public static final int CHARWIDTH = 8, CHARHEIGHT = 12;
	public static final int CHARSPERWIDTH = (Awakening.WIDTH - INFOBARHEIGHT)
			/ CHARWIDTH + 5;
	public static final int CHARSPERHEIGHT = (Awakening.HEIGHT - INFOBARHEIGHT)
			/ CHARHEIGHT;
	public static final char PLAYER = '@', ENEMY = 'E', WALLHOR = '═',
			WALLVER = '║', WALLTOPLCOR = '╔', WALLTOPRCOR = '╗',
			WALLBOTLCOR = '╚', WALLBOTRCOR = '╝', FLOOR = '·', WATER = '~',
			PLANT1 = '♌', PLANT2 = '♈', PLANT3 = '♑', GRASS = '\"',
			WALLTOPBCOR = '╦', WALLBOTBCOR = '╩', WALLLEFTBCOR = '╣',
			WALLALLCOR = '╬', WALLRIGHTBCOR = '╠', STAIRSDOWN = '>',
			STAIRSUP = '<',

			NULL = 0, PATH = '#';

	public Color[] foregroundColorMap = new Color[Character.MAX_VALUE];
	public Color[] backgroundColorMap = new Color[Character.MAX_VALUE];

	private int level = 0;
	private int maxLevel = 24;
	private char[][][] map = new char[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];

	// Level generation
	private Color hypnoColor = Color.blue;
	private int maxRooms = 10;
	private static final int WALL = 1;

	// Player
	private boolean dead = false;
	private char[] playerChar = { '@' };
	private int px = 0, py = 0;
	private String playerName = "";
	private int attack = 1, defaultAttack = 1;
	private int health = 10, maxHealth = 10, defaultMaxHealth = 10;
	private double xp = 0;
	private int poisonCount = 0;
	private int madness = 0;

	private int weapon = 0X01000000 | '/';
	private int armour = NULL;
	private int equipment = NULL;

	private int playerLevel = 1;
	private int playerClass = 0;
	private String[] classes = { "Explorer", "Soldier" };
	private String[] victoryMsg = { "defeated", "killed", "slew", "destroyed",
			"decimated", "vanquished" };
	private String[] attackMsg = { "hit", "struck", "stabbed", "chopped",
			"brutalised", "damaged" };

	// Items
	/*
	 * 12345678 0X00000000
	 * 
	 * 12 = location in array 3 = type - 0 = weapon - 1 = armour - 2 = equipment
	 * (other) 4 = colour 56 = enchantment 78 = char
	 */
	private final int WEAPON = 0, ARMOUR = 1, EQUIPMENT = 2;
	private Color[] itemColors = { new Color(0XFFFFFF), new Color(0XC0C0C0),
			new Color(0X0000AA), new Color(0X00AA00), new Color(0XAA0000),
			new Color(0XAA00AA), new Color(0X00AAAA), new Color(0XAAAA00),
			new Color(0X0000AA), new Color(0X0000AA), new Color(0X0000AA),
			new Color(0X0000AA), new Color(0X0000AA), new Color(0X0000AA),
			new Color(0XCC5333), new Color(0XFF5333) };
	private int[][][] itemMap = new int[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];

	private String[] weaponNames = { "Fists", "Wooden Sword", "Wooden Axe",
			"Wooden Pike", "Bronze Dagger", "Bronze Axe", "Bronze Flail",
			"Iron Dagger", "Iron Axe", "Iron Flail", "Steel Battleaxe",
			"Steel Lance", "Steel Sword", "Cobalt Sword", "Cobalt Mace",
			"Cobalt Shiv" };
	private double[] weaponDmg = { 0, 2, 2, 2, 3, 3.5, 3.5, 5.5, 4, 5.5, 7.6,
			6, 7, 8.5, 8, 7 };

	private double[] armourDef = { 0, 1, 2, 3, 3.5, 3.5, 4, 4.5, 4.5, 5, 5.5, };
	private String[] armourNames = { "no armour", "Leather Cuirass",
			"Bronze Chainmail", "Bronze Cuirass", "Bronze Platemail",
			"Iron Chainmail", "Iron Cuirass", "Iron Platemail",
			"Steel Chainmail", "Steel Cuirass", "Steel Platemail",
			"Cobalt Chainmail", "Cobalt Cuirass", "Cobalt Platemail", };

	private String[] equipmentNames = { "no equipment", "Ring of Hiding",
			"Ring of the Shadows", "Ring of Strength", "Ring of Steel",
			"Ring of Health", "Red Potion", "Blue Potion", "Green Potion",
			"Orange Potion", "Mysterious Potion", "Scroll" };
	private String[] equipmentDescriptions = { "",
			"Reduces detectable distance by 30%",
			"Reduces detectable distance by 50%", "Increases attack by 15%",
			"Increases defence by 15%", "Increases health regen by 33%",
			"Unknown", "Unknown", "Unknown", "Unknown",
			"(I wouldn't drink it...)",
			"The scroll is adorned with strange runes." };
	private int GREENPOTION = 0X08230000 | '?', BLUEPOTION = 0X07220000 | '?',
			REDPOTION = 0X06240000 | '?', ORANGEPOTION = 0X092F0000 | '?',
			MYSTERYPOTION = 0X0a200000 | '?';

	private int HEALTHPOTION, POISON, GREATERHEALTHPOTION, GREATERPOISON;
	private int SCROLL = 0X0B0E0000 | '»' | (EQUIPMENT << 20);
	private int[] potions = { GREENPOTION, BLUEPOTION, REDPOTION, ORANGEPOTION,
			MYSTERYPOTION };
	private boolean[] usedPotions;
	private String[] potionDescriptions;

	// TODO add scrolls

	private String[] enchantmentWeaponNames = { "No Enchantment", "Sharp",
			"Keen", "Icy", "Ethereal", "Remorseless", "Spectral", "Flaming",
			"Malevolent", "Omnipotent", "Draconic",

	};
	private double[] enchantmentWeaponBonus = { 0, 1.5, 3, 6, 7.25, 10.5, 12,
			15, 18, 22.5, 30 };

	private String[] enchantmentArmourNames = { "No Enchantment", "Hard",
			"Tough", "Imbuned", "Ethereal", "Mighty", "Spectral",
			"Immalleable", "Malevolent", "Omnipotent", "Draconic",

	};
	private double[] enchantmentArmourBonus = { 0, 1.5, 3, 6, 7.25, 10.5, 12,
			15, 18, 22.5, 30 };

	// Controls
	private int LEFT = KeyEvent.VK_A;
	private int RIGHT = KeyEvent.VK_D;
	private int UP = KeyEvent.VK_W;
	private int DOWN = KeyEvent.VK_S;
	private int HIT = KeyEvent.VK_SPACE;
	private int INVENTORY = KeyEvent.VK_E;
	private int USE = KeyEvent.VK_Q;

	// Lighting
	private BufferedImage lightMap = new BufferedImage(Awakening.WIDTH,
			Awakening.HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private int[] lightPixels;
	private boolean[][][] shadowTiles = new boolean[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
	private boolean[][][] lightTiles = new boolean[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];

	// Console
	private ArrayList<String> consoleText = new ArrayList<String>();
	private int consoleTick = 0;

	// Entity spawning
	private final double ENEMYSPAWNRATE = 50;

	// Entities
	private int MAXDETDIST = 10;
	private final int DEFAULTMAXDETDIST = 10;;
	private final int MAXHEALTH = 10;
	private final int ENEMYID = 9;
	private final int DRAGON = 'D';
	private final int ZOMBIE = 'Z', SKELETON = 'S', SNAKE = 's', GIANT = 'G',
			FOOTMAN = 'F', GUARD = 'X', WOLF = 'w', LION = 'L', SLIME = 'I',
			BEAST = 'B', WEREWOLF = 'W';
	private final int[] enemies = { ZOMBIE, SKELETON, SNAKE, GIANT, FOOTMAN,
			GUARD, WOLF, LION, SLIME, BEAST, WEREWOLF };
	private String[] enemyNames = new String[Character.MAX_VALUE];
	private int[][][] entities = new int[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];

	// Pathfinding
	private Pathfinder pathfinder;
	private int[] impassable = { WALL, WALLVER, WALLHOR, WALLTOPLCOR,
			WALLTOPRCOR, WALLBOTLCOR, WALLBOTRCOR, WALLTOPBCOR, WALLLEFTBCOR,
			WALLRIGHTBCOR, WALLBOTBCOR, WALLALLCOR, NULL, 0, WATER };

	// GameOver
	private String[] gameOverText = {
			"   ,___  _,  _ _ _  ______     ___  _,   _______ _ __ ",
			"   /   / / | ( / ) )(  /       /  ) ( |  /(  /   ( /  )",
			"  /  __ /--|  / / /   /--     /   /   | /   /--   /--< ",
			" (___/_/   |_/ / (_ (/____/  (___/    |/  (/____//   \\_",

	};
	private String[] gameOverArt = {
			"                                  /   \\       ",
			" _                        )      ((   ))     (",
			"(@)                      /|\\      ))_((     /|\\                        _     ",
			"|-|                     / | \\    (/\\|/\\)   / | \\                      (@)  ",
			"| | -------------------/--|-voV---\\`|\'/--Vov-|--\\---------------------|-|   ",
			"|-|                         \'^`   (o o)  \'^`                          | |    ",
			"| |                               `\\Y/\'                               |-|    ",
			"|-|                                                                   | |      ",
			"| |                                                                   |-|      ",
			"|-|                                                                   | |      ",
			"| |                                                                   |-|      ",
			"|-|                                                                   | |      ",
			"| |                                                                   |-|      ",
			"|_|___________________________________________________________________| |      ",
			"(@)              l   /\\ /         ( (       \\ /\\   l                `\\|-|  ",
			"                 l /   V           \\ \\       V   \\ l                  (@)   ",
			"                 l/                _) )_          \\I                          ",
			"                                   `\\ /\'                                     ",
			"                                     `                                         ", };

	// Intro
	private String[] introText = {
			"  ___ _ __    _,   ,______ _ __    _ __ ______   _,  __  _ _ _ __,",
			" ( / ( /  )  / |  /   /  (| /  )  ( /  |  /     / | ( / ( / ) |   ",
			"  /  //--<  /--| /  _/   / /  /    /--<  /--   /--|  /   / / / `. ",
			"(/\\_//   \\_/   |(___(___/ /  (_   /   \\(/____//   |(/___/ / ((___)",

	};
	private String[] introArt = { "                                      ",
			"             \\                  /",
			"    _________))                ((__________",
			"   /.-------./\\\\    \\    /    //\\.--------.\\",
			"  //#######//##\\\\   ))  ((   //##\\\\########\\\\",
			" //#######//###((  ((    ))  ))###\\\\########\\\\",
			"((#######((#####\\\\  \\\\  //  //#####))########))",
			" \\##\' `###\\######\\\\  \\)(/  //######/####\' `##/",
			"  )\'    ``#)\'  `##\\`->oo<-\'/##\'  `(#\'\'     `(",
			"          (       ``\\`..\'/\'\'       )",
			"                     \\\"\"(", "                      `- )",
			"                      / /", "                     ( /\\",
			"                     /\\| \\", "                    (  \\",
			"                        )", "                       /",
			"                      (", "                      ``", };

	// Stats
	private int kills = 0, turn = 0;

	// Main menu
	private String[] mainMenuText = { "To Sir Greidawl,", "",
			"Two months ago you sealed the entrance to mistkeep,",
			"and two months ago I would have agreed with this",
			"decision. However, our situation is dire, every day",
			"the lands are defiled with increasing amounts of",
			"goblins and other foul beasts, without any evidence",
			"to suggest where they are coming from, there can only",
			"be one explanation, he is active again. Therefore it",
			"is with my sincerest apologies that I must venture",
			"back through the keep into the dragon's realm, and",
			"destroy the miscreant forever.", "",
			"                forever your loyal servant,", "", "(NAME):      ",

	};

	public Game() {
		enemyNames[ZOMBIE] = "Zombie";
		enemyNames[SKELETON] = "Skeleton";
		enemyNames[SNAKE] = "Snake";
		enemyNames[GIANT] = "Giant";
		enemyNames[FOOTMAN] = "Footman";
		enemyNames[GUARD] = "Guard";
		enemyNames[WOLF] = "Wolf";
		enemyNames[LION] = "Lion";
		enemyNames[SLIME] = "Slime";
		enemyNames[BEAST] = "Beast";
		enemyNames[WEREWOLF] = "Werewolf";
		enemyNames[DRAGON] = "Dragon";

		foregroundColorMap[ZOMBIE] = new Color(0X990000);
		foregroundColorMap[SKELETON] = new Color(0XCCCCCC);
		foregroundColorMap[SNAKE] = new Color(0X338833);
		foregroundColorMap[GIANT] = new Color(0XFF0000);
		foregroundColorMap[FOOTMAN] = new Color(0X77FF77);
		foregroundColorMap[GUARD] = new Color(0X669966);
		foregroundColorMap[WOLF] = new Color(0X777777);
		foregroundColorMap[LION] = new Color(0X77332F);
		foregroundColorMap[SLIME] = new Color(0X009900);
		foregroundColorMap[BEAST] = new Color(0X664422);
		foregroundColorMap[WEREWOLF] = new Color(0X777777);
		foregroundColorMap[DRAGON] = new Color(0X990000);

		foregroundColorMap[PLAYER] = new Color(0XFFFFFF);
		foregroundColorMap[ENEMY] = new Color(0XFF0000);
		foregroundColorMap[WALLHOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLTOPLCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLTOPRCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLBOTLCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLTOPBCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLBOTBCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLBOTRCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLRIGHTBCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLALLCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLLEFTBCOR] = new Color(0XC0C0C0);
		foregroundColorMap[WALLVER] = new Color(0XC0C0C0);
		foregroundColorMap[FLOOR] = new Color(0XC0C0C0);
		foregroundColorMap[WATER] = new Color(0X000066);
		foregroundColorMap[PATH] = new Color(0X222222);
		foregroundColorMap[STAIRSUP] = new Color(0XFFFFFF);
		foregroundColorMap[STAIRSDOWN] = new Color(0XFFFFFF);
		foregroundColorMap[GRASS] = new Color(0X008800);
		foregroundColorMap[PLANT1] = new Color(0X007800);
		foregroundColorMap[PLANT2] = new Color(0X007800);
		foregroundColorMap[PLANT3] = new Color(0X007800);

		backgroundColorMap[WATER] = new Color(0X0000FF);
		backgroundColorMap[DRAGON] = new Color(0X220000);
		pathfinder = new Pathfinder(impassable);

	}

	public void render(Graphics2D g) {
		if (state == State.GAME) {
			if (dead && !paused) {
				renderDeath(g);
			} else {
				renderMap(g);
				renderLighting(g);
				if (madness > 0) {
					renderMadness(g);
				}
				renderInfoBar(g);
				renderConsole(g);
				if (inventory)
					renderInventory(g);
			}
		} else if (state == State.MAINMENU) {
			renderMainMenu(g);
		} else if (state == State.INTRO) {
			renderIntro(g);
		}
	}

	public void renderMadness(Graphics2D g) {
		for (int x = 0; x < CHARSPERHEIGHT; x++) {
			for (int y = 0; y < CHARSPERWIDTH; y++) {
				int col3 = (int) (Math.random() * 0XFFFFFF);
				Color madnessColor = new Color((col3) & 0X0000FF,
						((col3) & 0X00FF00) >> 8, ((col3) & 0XFF0000) >> 16,
						200);
				g.setColor(madnessColor);
				g.fillRect((y) * CHARWIDTH, x * CHARHEIGHT, CHARWIDTH,
						CHARHEIGHT);
			}
		}
	}

	public void renderMainMenu(Graphics2D g) {
		g.setFont(Awakening.font.deriveFont(16f));
		g.setColor(new Color(0XC0C0C0));
		for (int i = 0; i < mainMenuText.length; i++) {
			g.drawString(mainMenuText[i], 14 * CHARWIDTH, (i + 5) * CHARHEIGHT);
		}
		g.drawString(playerName, 22 * CHARWIDTH, (mainMenuText.length + 4)
				* CHARHEIGHT);

		g.drawString(" -- enter -- ", 33 * CHARWIDTH,
				(int) (Awakening.HEIGHT - CHARHEIGHT * 1.5));
	}

	public void renderIntro(Graphics2D g) {
		g.setFont(Awakening.font.deriveFont(16f));
		g.setColor(new Color(0XC0C0C0));
		for (int i = 0; i < introArt.length; i++) {
			g.drawString(introArt[i], 17 * CHARWIDTH, (i + 6) * CHARHEIGHT);
		}

		for (int i = 0; i < introText.length; i++) {
			g.drawString(introText[i], 7 * CHARWIDTH, (i + 2) * CHARHEIGHT);
		}

		g.drawString(" -- space -- ", 33 * CHARWIDTH,
				(int) (Awakening.HEIGHT - CHARHEIGHT * 1.5));

	}

	public void renderDeath(Graphics2D g) {
		g.setColor(new Color(0XC0C0C0));
		g.setFont(Awakening.font.deriveFont(16f));
		for (int i = 0; i < gameOverArt.length; i++) {
			if (i == 7) {
				for (int q = 0; q < gameOverText.length; q++) {
					g.drawString(gameOverText[q], 11 * CHARWIDTH, (i + q + 3)
							* CHARHEIGHT);
				}
			}

			g.drawString(gameOverArt[i], 3 * CHARWIDTH, (i + 3) * CHARHEIGHT);
		}

		String[] info = {
				playerName + ", lvl " + playerLevel + " "
						+ classes[playerClass] + " died on level " + level
						+ " with " + getWeaponName(weapon) + ", ",

				getArmourName(armour) + " and "
						+ equipmentNames[(equipment & 0XFF000000) >> 24] + ".",
				playerName + " killed " + kills + " monsters, and lasted "
						+ turn + " turns.", "space to continue..." };

		for (int i = 0; i < info.length; i++) {
			g.drawString(info[i], CHARWIDTH, Awakening.HEIGHT - CHARHEIGHT
					* info.length + i * CHARHEIGHT);
		}

	}

	public void renderConsole(Graphics2D g) {
		ArrayList<String> consoleArray = new ArrayList<String>();

		String console = "";
		for (int i = 0; i < consoleText.size(); i++) {
			if ((console + " " + consoleText.get(i)).length() > CHARSPERWIDTH) {
				consoleArray.add(console);
				console = "";
			}
			console += " " + consoleText.get(i);
		}
		consoleArray.add(console);

		g.setColor(Color.black);
		for (int i = 0; i < consoleArray.size(); i++) {
			g.fillRect(0, 0, consoleArray.get(i).length() * CHARWIDTH,
					CHARHEIGHT * (i + 1));
		}

		g.setFont(Awakening.font.deriveFont(16f));
		g.setColor(new Color(0XC0C0C0));
		for (int i = 0; i < consoleArray.size(); i++) {
			g.drawString(consoleArray.get(i), 0, CHARHEIGHT * (i + 1));
		}
	}

	public void renderInfoBar(Graphics2D g) {
		g.setColor(new Color(0XC0C0C0));
		g.setFont(Awakening.font.deriveFont(16f));

		String playerInfo = playerName + ", lvl " + playerLevel + " "
				+ classes[playerClass] + "  Weapon:" + getWeaponName(weapon);

		double playerAttack = (attack + weaponDmg[(weapon & 0XFF000000) >> 24] + enchantmentWeaponBonus[(weapon & 0X00000FF0) >> 8]);
		if ((equipment & 0XFF000000) >> 24 == 3) {
			playerAttack *= 1.15;
			playerAttack = Math.ceil(playerAttack * 100) / 100;
		}
		
		double playerDefence = armourDef[(armour & 0XFF000000) >> 24]
				+ enchantmentArmourBonus[(armour & 0X00000FF0) >> 8];
		if ((equipment & 0XFF000000) >> 24 == 4) {
			playerDefence *= 1.15;
			playerDefence = Math.ceil(playerAttack * 100) / 100;
		}
		
		String playerStats = "Dlvl:"
				+ level
				+ " Exp:"
				+ xp
				+ "<"
				+ (playerLevel * (playerLevel + 1))
				* 10
				+ ">"
				+ " Hp:"
				+ health
				+ "<"
				+ maxHealth
				+ ">"
				+ " Att:"
				+ (attack)
				+ " Dmg:"
				+ (playerAttack)
				+ " Def:"
				+ (playerDefence);

		g.drawString(playerInfo, 0, Awakening.HEIGHT - 24);
		g.drawString(playerStats, 0, Awakening.HEIGHT - 12);
	}

	public void renderMap(Graphics2D g) {
		g.setFont(Awakening.font.deriveFont(16f));
		for (int x = 0; x < map[level].length; x++) {
			for (int y = 0; y < map[level][0].length; y++) {
				g.setColor(Color.black);
				if (x == px && y == py) {
					if (backgroundColorMap[PLAYER] != null) {
						g.setColor(backgroundColorMap[PLAYER]);
						g.fillRect(y * CHARWIDTH, x * CHARHEIGHT, CHARWIDTH,
								CHARHEIGHT);
					}
					g.setColor(foregroundColorMap[PLAYER]);
					g.drawChars(playerChar, 0, 1, y * CHARWIDTH, (x + 1)
							* CHARHEIGHT);
				} else {
					if (entities[level][x][y] != NULL
							&& lightTiles[level][x][y]) {
						char e = (char) ((entities[level][x][y] & 0X0FF0) >> 4);

						if (backgroundColorMap[e] != null) {
							g.setColor(backgroundColorMap[e]);
							g.fillRect(y * CHARWIDTH, x * CHARHEIGHT,
									CHARWIDTH, CHARHEIGHT);
						}
						g.setColor(foregroundColorMap[e]);
						g.drawChars(new char[] { e }, 0, 1, y * CHARWIDTH,
								(x + 1) * CHARHEIGHT);
					} else if (itemMap[level][x][y] != NULL) {
						if ((itemMap[level][x][y] & 0X0000FF00) >> 8 != 0
								&& lightTiles[level][x][y]) {
							g.setColor(hypnoColor);
							g.fillRect(y * CHARWIDTH, x * CHARHEIGHT,
									CHARWIDTH, CHARHEIGHT);
						}

						g.setColor(itemColors[(itemMap[level][x][y] & 0X000F0000) >> 16]);
						if ((itemMap[level][x][y]) == MYSTERYPOTION)
							g.setColor(hypnoColor);
						char c = (char) (itemMap[level][x][y] & 0X000000FF);
						g.drawChars(new char[] { c }, 0, 1, y * CHARWIDTH,
								(x + 1) * CHARHEIGHT);
					} else {
						if (backgroundColorMap[map[level][x][y]] != null) {
							g.setColor(backgroundColorMap[map[level][x][y]]);
							g.fillRect(y * CHARWIDTH, x * CHARHEIGHT,
									CHARWIDTH, CHARHEIGHT);
						}
						g.setColor(foregroundColorMap[map[level][x][y]]);
						g.drawChars(map[level][x], y, 1, y * CHARWIDTH, (x + 1)
								* CHARHEIGHT);
					}
				}
			}
		}
	}

	public void renderInventory(Graphics2D g) {
		g.setFont(Awakening.font.deriveFont(16f));

		g.setColor(new Color(0X000000));
		g.fillRect(0, 0, Awakening.WIDTH, Awakening.HEIGHT);

		g.setColor(new Color(0XFFFFFF));
		g.drawString("Inventory", CHARWIDTH * 2, CHARHEIGHT * 2);
		g.drawString("_________", CHARWIDTH * 2, CHARHEIGHT * 2 + 2);

		g.setColor(new Color(0XC0C0C0));

		if ((weapon & 0X0000FF00) >> 8 != 0)
			g.setColor(hypnoColor);
		g.drawString("Weapon: " + getWeaponName(weapon), CHARWIDTH * 2,
				CHARHEIGHT * 4);
		g.setColor(new Color(0XC0C0C0));

		g.drawString(
				"	- "
						+ (weaponDmg[(weapon & 0XFF000000) >> 24] + enchantmentWeaponBonus[(weapon & 0X00000FF0) >> 8])
						+ " damage.", CHARWIDTH * 2, CHARHEIGHT * 5);
		if ((armour & 0X0000FF00) >> 8 != 0)
			g.setColor(hypnoColor);
		g.drawString("Armour: " + getArmourName(armour), CHARWIDTH * 2,
				CHARHEIGHT * 7);
		g.setColor(new Color(0XC0C0C0));

		g.drawString(
				"	- "
						+ (armourDef[(armour & 0XFF000000) >> 24] + enchantmentArmourBonus[(armour & 0X00000FF0) >> 8])
						+ " defence.", CHARWIDTH * 2, CHARHEIGHT * 8);

		g.drawString("Equipment: "
				+ equipmentNames[(equipment & 0XFF000000) >> 24],
				CHARWIDTH * 2, CHARHEIGHT * 10);

		boolean isPotion = false;
		for (int potion : potions) {
			if (equipment == potion) {
				isPotion = true;
				break;
			}
		}
		if (isPotion) {
			if (usedPotions[(equipment & 0XFF000000) >> 24]) {
				g.drawString("   - "
						+ potionDescriptions[(equipment & 0XFF000000) >> 24],
						CHARWIDTH * 2, CHARHEIGHT * 11);
			} else {
				g.drawString(
						"   - "
								+ equipmentDescriptions[(equipment & 0XFF000000) >> 24],
						CHARWIDTH * 2, CHARHEIGHT * 11);
			}
		} else if (equipment != NULL) {
			g.drawString("   - "
					+ equipmentDescriptions[(equipment & 0XFF000000) >> 24],
					CHARWIDTH * 2, CHARHEIGHT * 11);
		}

		g.setColor(new Color(0X606060));
		g.drawString(" -- space -- ", 33 * CHARWIDTH,
				(int) (Awakening.HEIGHT - CHARHEIGHT * 1.5));
	}

	public void renderLighting(Graphics2D g) {
		for (int i = 0; i < lightPixels.length; i++)
			lightPixels[i] = -0XFFFFFF;

		for (int x = 0; x < map[level].length; x++) {
			for (int y = 0; y < map[level][0].length; y++) {
				lightTiles[level][x][y] = false;
				if (shadowTiles[level][x][y]) {
					for (int x2 = 0; x2 < CHARHEIGHT; x2++) {
						for (int y2 = 0; y2 < CHARWIDTH; y2++) {
							if ((int) ((x2 + ((x) * CHARHEIGHT))
									* Awakening.WIDTH + (y2 + (y) * CHARWIDTH)) < lightPixels.length) {
								lightPixels[(int) ((x2 + ((x) * CHARHEIGHT))
										* Awakening.WIDTH + (y2 + (y)
										* CHARWIDTH))] = 0Xd0000000;
							}
						}
					}
				}
			}
		}
		for (double theta = 0; theta < 360; theta += 0.25) {
			double vx = Math.sin(Math.toRadians(theta));
			double vy = Math.cos(Math.toRadians(theta));

			double magnitude = Math.sqrt(vx * vx + vy * vy);
			vx *= 1.0 / magnitude;
			vy *= 1.0 / magnitude;

			double x = px, y = py;
			while (Math.floor(x) >= 0 && Math.ceil(x) < map[level].length
					&& Math.floor(y) >= 0
					&& Math.ceil(y) < map[level][(int) Math.floor(x)].length) {

				for (int x2 = 0; x2 < CHARHEIGHT; x2++) {
					for (int y2 = 0; y2 < CHARWIDTH; y2++) {
						if ((int) ((x2 + (Math.floor(x) * CHARHEIGHT))
								* Awakening.WIDTH + (y2 + Math.floor(y)
								* CHARWIDTH)) < lightPixels.length) {
							lightPixels[(int) ((x2 + (Math.floor(x) * CHARHEIGHT))
									* Awakening.WIDTH + (y2 + Math.floor(y)
									* CHARWIDTH))] = 0X00000000;
						}
					}
				}

				shadowTiles[level][(int) Math.floor(x)][(int) Math.floor(y)] = true;
				lightTiles[level][(int) Math.floor(x)][(int) Math.floor(y)] = true;
				if (isWall(map[level][(int) Math.floor(x)][(int) Math.floor(y)])) {
					break;
				}

				x += vx;
				y += vy;

			}

		}

		g.drawImage(lightMap, 0, 0, null);
	}

	public void print(String text) {
		consoleTick = 0;
		consoleText.add(text);
	}

	public void update() {
		tick += 1;
		if (tick >= 0XFFFFFFF)
			tick = 0;
		int col1 = 0, col2 = 0;
		if ((Math.sin(Math.toRadians(tick))) < 0) {
			col1 = (int) (Math.abs(Math.sin(Math.toRadians(tick))) * 0XFF0000) & 0XFF0000;
		} else {
			col1 = (int) (Math.abs(Math.sin(Math.toRadians(tick))) * 0X0000FF) & 0X0000FF;
		}
		if ((Math.cos(Math.toRadians(tick))) < 0) {
			col2 = (int) (Math
					.abs(Math.cos(Math.toRadians(tick + Math.PI / 2))) * 0X000000) & 0X000000;
		} else {
			col2 = (int) (Math
					.abs(Math.cos(Math.toRadians(tick + Math.PI / 2))) * 0X00FF00) & 0X00FF00;
		}
		hypnoColor = new Color(col1 | col2);

		if (state == State.GAME) {
			if (!dead && !paused) {
				consoleTick++;
				if (consoleTick > 200) {
					consoleTick = 0;
					if (consoleText.size() > 0)
						consoleText.remove(0);
				}
			}
		}
	}

	public void updatePlayerClass() {
		if (kills >= 30 && playerClass == 0) {
			playerClass = 1;
		}
	}

	public void updateAffects() {
		if (madness > 0) {
			madness--;
			if (madness <= 0) {
				print("The strange effects wear off...");
			}
		}
		if ((equipment & 0XFF000000) >> 24 == 1) {
			MAXDETDIST = (int) (0.7 * DEFAULTMAXDETDIST);
		} else if ((equipment & 0XFF000000) >> 24 == 2) {
			MAXDETDIST = (int) (0.5 * DEFAULTMAXDETDIST);
		} else {
			MAXDETDIST = DEFAULTMAXDETDIST;
		}
	}

	public void nextTurn() {
		if (!dead && !paused) {

			updateAffects();
			updatePlayerClass();

			if (xp > (playerLevel * (playerLevel + 1)) * 10) {
				xp = 0;
				playerLevel++;
				maxHealth += 1;
				if (playerLevel % 10 == 0) {
					attack += 2;
					maxHealth += 2;
				} else if (playerLevel % 4 == 0) {
					attack++;
					maxHealth++;
				} else {
					if (playerLevel % 2 == 0)
						attack++;
					if (playerLevel % 2 != 0)
						maxHealth++;
				}

			}

			if (itemMap[level][px][py] != NULL) {
				switch ((itemMap[level][px][py] & 0X00F00000) >> 20) {
				case WEAPON:
					String item = getWeaponName(itemMap[level][px][py]);
					if ("aeiouAEIOU".indexOf(item.toCharArray()[0]) >= 0)
						print("You see an "
								+ getWeaponName(itemMap[level][px][py]) + ".");
					else
						print("You see a "
								+ getWeaponName(itemMap[level][px][py]) + ".");
					break;
				case ARMOUR:
					String item1 = getArmourName(itemMap[level][px][py]);
					if ("aeiouAEIOU".indexOf(item1.toCharArray()[0]) >= 0)
						print("You see an "
								+ getArmourName(itemMap[level][px][py]) + ".");
					else
						print("You see a "
								+ getArmourName(itemMap[level][px][py]) + ".");
					break;
				case EQUIPMENT:
					String item11 = equipmentNames[(itemMap[level][px][py] & 0XFF000000) >> 24];
					if ("aeiouAEIOU".indexOf(item11.toCharArray()[0]) >= 0)
						print("You see an "
								+ equipmentNames[(itemMap[level][px][py] & 0XFF000000) >> 24]
								+ ".");
					else
						print("You see a "
								+ equipmentNames[(itemMap[level][px][py] & 0XFF000000) >> 24]
								+ ".");
					break;
				}

			}

			if (turn % 15 == 0
					|| (((equipment & 0XFF000000) >> 24 == 5) && turn % 10 == 0)) {
				health += 1;
				if (health > maxHealth) {
					health = maxHealth;
				}
			}

			updateAI();

			if (poisonCount > 0 && turn % 2 == 0) {
				poisonCount--;
				health--;
				print("The poison still lingers, -1 health");
				if (health <= 0 && !dead) {
					print("The poison finally kills you.");
					die();
				}
			}

			turn++;
		}
	}

	public void updateAI() {
		for (int x = 0; x < entities[level].length; x++) {
			for (int y = 0; y < entities[level][0].length; y++) {
				if (entities[level][x][y] != NULL) {
					entities[level][x][y] = entities[level][x][y] & 0X0FFFFF;
				}
			}
		}

		for (int x = 0; x < entities[level].length; x++) {
			for (int y = 0; y < entities[level][0].length; y++) {
				if ((entities[level][x][y] & 0X000F) == ENEMYID
						&& (entities[level][x][y] & 0XF00000) == 0) {
					entities[level][x][y] = entities[level][x][y] | 0X100000;
					if ((entities[level][x][y] & 0XF0000) >> 16 == 1) {
						Node[] nodes = pathfinder.calculatePath(
								convertMapToInt(), x, y, px, py);
						if (nodes.length > 1) {
							Node n = nodes[1];
							if (walk(n.x, n.y) && n.x == px && n.y == py) {
								hitPlayer(x, y);
							} else if (walk(n.x, n.y)) {
								entities[level][n.x][n.y] = entities[level][x][y];
								entities[level][x][y] = NULL;
							}
						}
					} else if (distToPlayer(x, y) < MAXDETDIST
							&& lightTiles[level][x][y]) {
						entities[level][x][y] = entities[level][x][y] | 0X10000;
						Node[] nodes = pathfinder.calculatePath(
								convertMapToInt(), x, y, px, py);
						if (nodes.length > 1) {
							Node n = nodes[1];
							if (walk(n.x, n.y) && n.x == px && n.y == py) {
								hitPlayer(x, y);
							} else if (walk(n.x, n.y)) {
								entities[level][n.x][n.y] = entities[level][x][y];
								entities[level][x][y] = NULL;
							}
						}
					} else {
						int choice = ((int) (Math.random() * 4));
						if (choice == 0 && walk(x - 1, y)) {
							entities[level][x - 1][y] = entities[level][x][y];
							entities[level][x][y] = NULL;
						} else if (choice == 1 && walk(x + 1, y)) {
							entities[level][x + 1][y] = entities[level][x][y];
							entities[level][x][y] = NULL;
						} else if (choice == 2 && walk(x, y - 1)) {
							entities[level][x][y - 1] = entities[level][x][y];
							entities[level][x][y] = NULL;
						} else if (choice == 3 && walk(x, y + 1)) {
							entities[level][x][y + 1] = entities[level][x][y]; 
							entities[level][x][y] = NULL;
						}
					}
				}
			}
		}
	}

	public void die() {
		dead = true;
		consoleText.clear();
		print("You have died!");
		pause();
	}

	public void pause() {
		paused = true;
		print(" -- space --");
	}

	public boolean walk(int x, int y) {
		return (x >= 0
				&& y >= 0
				&& x < map[level].length
				&& y < map[level][0].length
				&& (entities[level][x][y] == NULL || entities[level][x][y] == 0)
				&& !isWall(map[level][x][y]) && map[level][x][y] != WATER);
	}

	public int distToPlayer(int x, int y) {
		return Math.abs(px - x) + Math.abs(py - y);
	}

	public void use() {
		if (equipment != NULL) {
			if (equipment == HEALTHPOTION) {
				usedPotions[(equipment & 0XFF000000) >> 24] = true;
				equipment = NULL;
				health += maxHealth / 2;
				if (health > maxHealth)
					health = maxHealth;
				print("The health potion heals you for " + maxHealth / 2
						+ " points.");
			}
			if (equipment == GREATERHEALTHPOTION) {
				usedPotions[(equipment & 0XFF000000) >> 24] = true;
				equipment = NULL;
				health = maxHealth;
				print("The greater health potion heals you, you feel much better.");
			}
			if (equipment == POISON) {
				usedPotions[(equipment & 0XFF000000) >> 24] = true;
				equipment = NULL;
				poisonCount += maxHealth / 2;
				print("The potion poisons you.");
			}
			if (equipment == GREATERPOISON) {
				usedPotions[(equipment & 0XFF000000) >> 24] = true;
				equipment = NULL;
				poisonCount += maxHealth;
				print("The potion poisons you, you feel very ill.");
			}
			if (equipment == MYSTERYPOTION) {
				equipment = NULL;
				madness += 20;
				print("You feel very strange...");
			}
			if (equipment == SCROLL) {
				equipment = NULL;
				int choice = (int) (Math.random() * 4);
				switch (choice) {
				case 0:
					print("It seems it was a scroll of teleportation...");
					for (int x = 0; x < map[level].length; x++) {
						for (int y = 0; y < map[level][x].length; y++) {
							if (map[level][x][y] == STAIRSDOWN) {
								px = x;
								py = y;
							}
						}
					}
					break;
				case 1:
					print("It appears it was a scroll of summoning...");
					int baseHealth = MAXHEALTH << 12;
					int DETECTED = 0;
					int enemyCount = 0;
					int enemy = enemies[(int) (Math.random() * enemies.length)];
					for (int x = px - 1; x < map[level].length && x <= px + 1; x++) {
						for (int y = py - 1; y < map[level][x].length
								&& y <= py + 1; y++) {
							if (!isWall(map[level][x][y]) && walk(x, y)
									& enemyCount < 3) {
								entities[level][x][y] = (ENEMYID | (enemy << 4)
										| baseHealth | (DETECTED << 16));
								enemyCount++;
							}
						}
					}
					break;
				case 2:
					print("The scroll glows, you feel much stronger.");
					xp = (playerLevel * (playerLevel + 1)) * 10 + 10;
					break;
				case 3:
					print("The scroll glows, for a brief moment a map of the dungeon appears in your mind.");
					for (int x = 0; x < map[level].length; x++) {
						for (int y = 0; y < map[level][x].length; y++) {
							if (map[level][x][y] != NULL) {
								shadowTiles[level][x][y] = true;
							}
						}
					}
					break;
				}
			}
		}
	}

	public void hit() {
		if (map[level][px][py] == STAIRSUP) {
			setLevel(level - 1);
		} else if (map[level][px][py] == STAIRSDOWN) {
			setLevel(level + 1);
		} else if (itemMap[level][px][py] != NULL) {
			if ((int) (Math.random() * 50) == 0) {
				madness += 20;
				print("The item had a curse upon it!");
			}
			int buffer;
			switch ((itemMap[level][px][py] & 0X00F00000) >> 20) {
			case WEAPON:
				buffer = weapon;
				weapon = itemMap[level][px][py];
				if ((buffer & 0XFF000000) >> 24 != 0)
					itemMap[level][px][py] = buffer;
				else
					itemMap[level][px][py] = NULL;
				break;
			case ARMOUR:
				buffer = armour;
				armour = itemMap[level][px][py];
				itemMap[level][px][py] = buffer;
				break;
			case EQUIPMENT:
				buffer = equipment;
				equipment = itemMap[level][px][py];
				itemMap[level][px][py] = buffer;
				break;
			}
		}

	}

	public void hitPlayer(int x, int y) {
		double enemyAttack = level * 1.5 + 2;
		double playerDefence = armourDef[(armour & 0XFF000000) >> 24]
				+ enchantmentArmourBonus[(armour & 0X00000FF0) >> 8];

		if((entities[level][x][y] & 0X0FF0) >> 4 == DRAGON) {
			enemyAttack *= 3;
		}
		
		if ((equipment & 0XFF000000) >> 24 == 4) {
			playerDefence *= 1.15;
		}

		int attack = (int) ((Math.random() * (enemyAttack)) - (Math.random() * (playerDefence + 1)));

		if (attack > 0) {
			health -= attack;
			print(enemyNames[(entities[level][x][y] & 0X0FF0) >> 4] + " "
					+ attackMsg[(int) (Math.random() * attackMsg.length)] + " "
					+ "you" + " for " + attack + " damage " + "<on " + health
					+ "/" + maxHealth + ">.");

			if (health - attack <= 0) {
				die();
			}
		} else {
			print("The " + enemyNames[(entities[level][x][y] & 0X0FF0) >> 4]
					+ " missed.");
		}

	}

	public void hitEntity(int x, int y) {
		double enemyDefence = level * 1.3 + 1;
		double playerAttack = attack + weaponDmg[(weapon & 0XFF000000) >> 24]
				+ enchantmentWeaponBonus[(weapon & 0X00000FF0) >> 8];
		
		if((entities[level][x][y] & 0X0FF0) >> 4 == DRAGON) {
			enemyDefence *= 3;
		}
		
		if ((equipment & 0XFF000000) >> 24 == 3) {
			playerAttack *= 1.15;
		}

		if ((entities[level][x][y] & 0X000F) == ENEMYID) {
			int attack = (int) ((Math.random() * (playerAttack + 1)) - (Math
					.random() * (enemyDefence)));
			if (attack > 0) {
				if (((entities[level][x][y] & 0XF000) >> 12) - attack > 0) {
					entities[level][x][y] = (char) ((entities[level][x][y] & 0X0FFF) | ((((entities[level][x][y] & 0XF000) >> 12) - attack) << 12));
					print("You"
							+ " "
							+ attackMsg[(int) (Math.random() * attackMsg.length)]
							+ " the "
							+ enemyNames[(entities[level][x][y] & 0X0FF0) >> 4]
							+ " with a " + getWeaponName(weapon) + " for "
							+ attack + " damage " + "<on "
							+ ((entities[level][x][y] & 0XF000) >> 12) + "/"
							+ MAXHEALTH + ">.");
				} else {
					double xpGain = (int) ((((((level + 1.0) * (level + 8.0))) * Math
							.random()) + (((level + 1.0) * (level + 8.0)) / 2.0)) * 10.0) / 10.0;
					xp += xpGain;
					xp = ((int) (xp * 10)) / 10.0;
					print("You"
							+ " "
							+ victoryMsg[((int) (Math.random() * victoryMsg.length))]
							+ " the "
							+ enemyNames[(entities[level][x][y] & 0X0FF0) >> 4]
							+ "!" + " <+" + xpGain + "xp>");
					entities[level][x][y] = NULL;
					kills++;
				}
			} else {
				print("You" + " missed.");
			}
		}
	}

	public void setLevel(int level) {
		if (level < maxLevel && level >= 0) {
			if(level == maxLevel-1) {
				print("You feel and evil presence near you, the Dragon must be on this level.");
			}
			outerloop: for (int x = 0; x < map[level].length; x++) {
				for (int y = 0; y < map[level][x].length; y++) {
					if (map[level][x][y] == STAIRSDOWN && level < this.level) {
						px = x;
						py = y;
						break outerloop;
					}
					if (map[level][x][y] == STAIRSUP && level > this.level) {
						px = x;
						py = y;
						break outerloop;
					}
				}
			}
			this.level = level;
		}
	}

	public void generateMap() {
		map = new char[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
		entities = new int[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
		for (int z = 0; z < map.length; z++) {

			GenerationNode n = new GenerationNode(0, 0, CHARSPERHEIGHT,
					CHARSPERWIDTH);
			for (int i = 0; i < maxRooms; i++) {
				n.populateSubGenerationNodes();
			}
			for (int i = 1; i < n.subGenerationNodes.size(); i++) {
				n.linkSubGenerationNodes(map[z], i - 1, i);
			}
			n.fillSubGenerationNodes(map[z]);

			char[][][] buffer = cloneMap();
			char[][][] buffer2 = cloneMap();
			for (int x = 0; x < buffer[z].length; x++) {
				for (int y = 0; y < buffer[z][x].length; y++) {
					for (int x2 = x - 1; x2 <= x + 1; x2++) {
						for (int y2 = y - 1; y2 <= y + 1; y2++) {
							if (x2 >= 0 && y2 >= 0 && x2 < map[z].length
									&& y2 < map[z][x2].length) {
								if (buffer2[z][x][y] == 0
										&& buffer[z][x2][y2] != 0)
									buffer2[z][x][y] = WALL;
							}
						}
					}
				}
			}

			buffer = buffer2;
			for (int x = 0; x < buffer[z].length; x++) {
				for (int y = 0; y < buffer[z][x].length; y++) {
					if ((y > 0 && buffer[z][x][y - 1] != 0)
							|| (x > 0 && buffer[z][x - 1][y] != 0)
							|| (y + 1 < buffer[z][x].length && buffer[z][x][y + 1] != 0)
							|| (x + 1 < buffer[z].length && buffer[z][x + 1][y] != 0)) {
						if (isWall(buffer[z][x][y])) {

							if ((y > 0 && isWall(buffer[z][x][y - 1]))
									|| (y + 1 < buffer[z][x].length && isWall(buffer[z][x][y + 1]))) {
								map[z][x][y] = WALLHOR;
							} else if ((x >= 0 && isWall(buffer[z][x - 1][y]))
									|| (x + 1 < buffer[z].length && isWall(buffer[z][x + 1][y]))) {
								map[z][x][y] = WALLVER;
							}

							if ((y + 1 < map[z][x].length
									&& isWall(buffer[z][x][y + 1]) && (x + 1 < map[z].length && isWall(buffer[z][x + 1][y])))) {
								map[z][x][y] = WALLTOPLCOR;
							}

							if ((y - 1 > 0 && isWall(buffer[z][x][y - 1]) && (x + 1 < map[z].length && isWall(buffer[z][x + 1][y])))) {
								map[z][x][y] = WALLTOPRCOR;
							}

							if ((y + 1 < map[z][x].length
									&& isWall(buffer[z][x][y + 1]) && (x - 1 >= 0 && isWall(buffer[z][x - 1][y])))) {
								map[z][x][y] = WALLBOTLCOR;
							}

							if ((y - 1 > 0 && isWall(buffer[z][x][y - 1]) && (x - 1 >= 0 && isWall(buffer[z][x - 1][y])))) {
								map[z][x][y] = WALLBOTRCOR;
							}

						}
					}

				}
			}

			for (int x = 0; x < map[z].length; x++) {
				for (int y = 0; y < map[z][x].length; y++) {
					if ((y > 0 && map[z][x][y - 1] != 0)
							|| (x > 0 && map[z][x - 1][y] != 0)
							|| (y + 1 < map[z][x].length && map[z][x][y + 1] != 0)
							|| (x + 1 < map[z].length && map[z][x + 1][y] != 0)) {

						if (isWall(map[z][x][y])) {
							if (y + 1 < map[z][x].length
									&& isWall(map[z][x][y + 1]) && y - 1 >= 0
									&& isWall(map[z][x][y - 1])
									&& x + 1 < map[z].length
									&& isWall(map[z][x + 1][y])) {
								map[z][x][y] = WALLTOPBCOR;
							}
						}

						if (isWall(map[z][x][y])) {
							if (y + 1 < map[z][x].length
									&& isWall(map[z][x][y + 1]) && y - 1 >= 0
									&& isWall(map[z][x][y - 1]) && x - 1 >= 0
									&& isWall(map[z][x - 1][y])) {
								map[z][x][y] = WALLBOTBCOR;
							}
						}

						if (isWall(map[z][x][y])) {
							if (y + 1 < map[z][x].length
									&& isWall(map[z][x][y + 1]) && x - 1 >= 0
									&& isWall(map[z][x - 1][y])
									&& x + 1 < map[z].length
									&& isWall(map[z][x + 1][y])) {
								map[z][x][y] = WALLRIGHTBCOR;
							}
						}

						if (isWall(map[z][x][y])) {
							if (y - 1 >= 0 && isWall(map[z][x][y - 1])
									&& x - 1 >= 0 && isWall(map[z][x - 1][y])
									&& x + 1 < map[z].length
									&& isWall(map[z][x + 1][y])) {
								map[z][x][y] = WALLLEFTBCOR;
							}
						}

					}
				}
			}

			for (int x = 0; x < map[z].length; x++) {
				for (int y = 0; y < map[z][x].length; y++) {
					if ((y > 0 && map[z][x][y - 1] != 0)
							|| (x > 0 && map[z][x - 1][y] != 0)
							|| (y + 1 < map[z][x].length && map[z][x][y + 1] != 0)
							|| (x + 1 < map[z].length && map[z][x + 1][y] != 0)) {

						if (isWall(map[z][x][y])) {
							if (y + 1 < map[z][x].length
									&& isWall(map[z][x][y + 1]) && y - 1 >= 0
									&& isWall(map[z][x][y - 1])
									&& x + 1 < map[z].length
									&& isWall(map[z][x + 1][y]) && x - 1 >= 0
									&& isWall(map[z][x - 1][y])) {
								map[z][x][y] = WALLALLCOR;
							}
						}

						if (map[z][x][y] == WALLBOTBCOR) {
							if (x - 1 >= 0 && (map[z][x - 1][y] == WALLTOPBCOR)) {
								map[z][x][y] = WALLHOR;
								map[z][x - 1][y] = WALLHOR;
							}
						}
						if (map[z][x][y] == WALLTOPBCOR) {
							if (x + 1 < map[z].length
									&& (map[z][x + 1][y] == WALLBOTBCOR)) {
								map[z][x][y] = WALLHOR;
								map[z][x + 1][y] = WALLHOR;
							}
						}

						if (map[z][x][y] == WALLLEFTBCOR) {
							if (y - 1 >= 0
									&& (map[z][x][y - 1] == WALLRIGHTBCOR)) {
								map[z][x][y] = WALLVER;
								map[z][x][y - 1] = WALLVER;
							}
						}
						if (map[z][x][y] == WALLRIGHTBCOR) {
							if (y + 1 < map[z][x].length
									&& (map[z][x][y + 1] == WALLLEFTBCOR)) {
								map[z][x][y] = WALLVER;
								map[z][x][y + 1] = WALLVER;
							}
						}

					}
				}
			}

			spawnEntities(z);
			spawnItems(z);

			for (int x = 0; x < map[z].length; x++) {
				for (int y = 0; y < map[z][x].length; y++) {
					if (map[z][x][y] == NULL
							&& ((x - 1 >= 0 && map[z][x - 1][y] != NULL && !isWall(map[z][x - 1][y]))
									|| (x + 1 < map[z].length
											&& map[z][x + 1][y] != NULL && !isWall(map[z][x + 1][y]))
									|| (y - 1 >= 0 && map[z][x][y - 1] != NULL && !isWall(map[z][x][y - 1])) || (y + 1 < map[z][x].length
									&& map[z][x][y + 1] != NULL && !isWall(map[z][x][y + 1]))

							))
						map[z][x][y] = FLOOR;
				}
			}

		}
	}

	public void spawnEntities(int z) {
		for (int x = 0; x < entities[z].length; x++) {
			for (int y = 0; y < entities[z][x].length; y++) {
				if (map[z][x][y] == FLOOR || map[z][x][y] == PATH) {
					if ((int) (Math.random() * (2.0 + ENEMYSPAWNRATE)) == 0) {
						int baseHealth = MAXHEALTH << 12;
						int DETECTED = 0;
						int enemy = enemies[(int) (Math.random() * enemies.length)];
						entities[z][x][y] = (ENEMYID | (enemy << 4)
								| baseHealth | (DETECTED << 16));
					}
				}
			}
		}
	}

	public void spawnItems(int z) {
		/*
		 * 12345678 0X00000000
		 * 
		 * 12 = location in array 3 = type - 0 = weapon - 1 = armour - 2 =
		 * equipment (other) 4 = colour 67 = enchantment 78 = char
		 */
		for (int x = 0; x < itemMap[z].length; x++) {
			for (int y = 0; y < itemMap[z][x].length; y++) {
				if (!isWall(map[z][x][y]) && map[z][x][y] != WATER
						&& map[z][x][y] != NULL) {
					int choice = (int) (Math.random() * 160);
					int item = 1;
					int enchantment = 0;
					switch (choice) {
					case WEAPON:
						item = (((int) (Math.random() * 2) + (z + 1) + 1) << 24);
						itemMap[z][x][y] = 0X00000000 | '/' | (WEAPON << 20)
								| item;

						if ((int) (Math.random() * 6) == 0 && (maxLevel / (enchantmentArmourNames.length)) > 0) {
							enchantment = z
									/ (maxLevel / (enchantmentWeaponNames.length))
									+ (int) (Math.random() * 2);
							if (enchantment == 0)
								enchantment = 1;
							if (enchantment >= enchantmentWeaponNames.length)
								enchantment = enchantmentWeaponNames.length - 1;
							itemMap[z][x][y] = itemMap[z][x][y]
									| (enchantment << 8);
						}

						break;
					case EQUIPMENT:
						if ((int) (Math.random() * 5) == 0) {
							item = ((int) (Math.random() * 4) + 1) << 24;
							itemMap[z][x][y] = 0X00000000 | 'o'
									| (EQUIPMENT << 20) | item;
						}
						break;
					case ARMOUR:
						item = (((int) (Math.random() * 2) + (z) + 1) << 24);
						itemMap[z][x][y] = 0X00000000 | '[' | (ARMOUR << 20)
								| item;

						if ((int) (Math.random() * 6) == 0 && (maxLevel / (enchantmentArmourNames.length)) > 0) {
							enchantment = z
									/ (maxLevel / (enchantmentArmourNames.length))
									+ (int) (Math.random() * 2);
							if (enchantment == 0)
								enchantment = 1;
							if (enchantment >= enchantmentArmourNames.length)
								enchantment = enchantmentArmourNames.length - 1;
							itemMap[z][x][y] = itemMap[z][x][y]
									| (enchantment << 8);
						}
						break;
					case 3:
						if ((int) (Math.random() * 3) == 0) {
							itemMap[z][x][y] = potions[(int) (Math.random() * potions.length)];
						}
						break;
					case 4:
						if ((int) (Math.random() * 6) == 0) {
							itemMap[z][x][y] = SCROLL;
						}
						break;

					}
				}
			}
		}

	}

	public String getWeaponName(int weapon) {
		if ((weapon & 0X00000FF0) >> 8 != 0)
			return enchantmentWeaponNames[(weapon & 0X00000FF0) >> 8] + " "
					+ weaponNames[(weapon & 0XFF000000) >> 24] + " (+"
					+ enchantmentWeaponBonus[(weapon & 0X00000FF0) >> 8] + ")";
		else
			return weaponNames[(weapon & 0XFF000000) >> 24];
	}

	public String getArmourName(int armour) {
		if ((armour & 0X00000FF0) >> 8 != 0)
			return enchantmentArmourNames[(armour & 0X00000FF0) >> 8] + " "
					+ armourNames[(armour & 0XFF000000) >> 24] + " (+"
					+ enchantmentArmourBonus[(armour & 0X00000FF0) >> 8] + ")";
		else
			return armourNames[(armour & 0XFF000000) >> 24];
	}

	public char[][][] cloneMap() {
		char[][][] buffer = new char[map.length][map[0].length][map[0][0].length];
		for (int z = 0; z < map.length; z++) {
			for (int x = 0; x < map[0].length; x++) {
				for (int y = 0; y < map[0][0].length; y++) {
					buffer[z][x][y] = map[z][x][y];
				}
			}
		}
		return buffer;
	}

	public boolean isWall(char c) {
		return (c == WALL || c == WALLVER || c == WALLHOR || c == WALLTOPLCOR
				|| c == WALLTOPRCOR || c == WALLBOTLCOR || c == WALLBOTRCOR
				|| c == WALLTOPBCOR || c == WALLLEFTBCOR || c == WALLRIGHTBCOR
				|| c == WALLBOTBCOR || c == WALLALLCOR);
	}

	public void reset() {
		usedPotions = new boolean[0XFF];
		potionDescriptions = new String[0XFF];
		int choice = (int) (Math.random() * 4);
		switch (choice) {
		case 0:
			HEALTHPOTION = REDPOTION;
			GREATERHEALTHPOTION = ORANGEPOTION;
			POISON = BLUEPOTION;
			GREATERPOISON = GREENPOTION;
			break;
		case 1:
			HEALTHPOTION = ORANGEPOTION;
			GREATERHEALTHPOTION = REDPOTION;
			POISON = GREENPOTION;
			GREATERPOISON = BLUEPOTION;
			break;
		case 2:
			HEALTHPOTION = GREENPOTION;
			GREATERHEALTHPOTION = BLUEPOTION;
			POISON = ORANGEPOTION;
			GREATERPOISON = REDPOTION;
			break;
		case 3:
			HEALTHPOTION = BLUEPOTION;
			GREATERHEALTHPOTION = GREENPOTION;
			POISON = REDPOTION;
			GREATERPOISON = ORANGEPOTION;
			break;
		}
		potionDescriptions[(HEALTHPOTION & 0XFF000000) >> 24] = "Heals half of your health.";
		potionDescriptions[(GREATERHEALTHPOTION & 0XFF000000) >> 24] = "A wonderous elixer that heals all of your health.";
		potionDescriptions[(POISON & 0XFF000000) >> 24] = "Damages you for half of your health.";
		potionDescriptions[(GREATERPOISON & 0XFF000000) >> 24] = "A deadly poison that spares only those with all their health.";

		lightMap = new BufferedImage(Awakening.WIDTH, Awakening.HEIGHT,
				BufferedImage.TYPE_INT_ARGB);
		lightPixels = ((DataBufferInt) lightMap.getRaster().getDataBuffer())
				.getData();
		itemMap = new int[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
		entities = new int[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
		shadowTiles = new boolean[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
		lightTiles = new boolean[maxLevel][CHARSPERHEIGHT][CHARSPERWIDTH];
		health = defaultMaxHealth;
		maxHealth = defaultMaxHealth;
		attack = defaultAttack;
		level = 0;
		playerLevel = 1;
		armour = NULL;
		equipment = NULL;
		weapon = 0X01000000 | '/';
		xp = 0;
		playerClass = 0;
		dead = false;
		paused = false;
		inventory = false;
		consoleText.clear();
		consoleTick = 0;
		kills = 0;
		turn = 0;

		generateMap();
		outerloop: for (int x = 0; x < map[level].length; x++) {
			for (int y = 0; y < map[level][x].length; y++) {
				if (map[level][x][y] == STAIRSUP) {
					px = x;
					py = y;
					if (level == 0)
						map[level][x][y] = FLOOR;
					break outerloop;
				}
			}
		}
		
		outerloop: for (int x = 0; x < map[maxLevel-1].length; x++) {
			for (int y = 0; y < map[maxLevel-1][x].length; y++) {
				if (map[maxLevel-1][x][y] == STAIRSDOWN) {
					int baseHealth = MAXHEALTH << 12;
					int DETECTED = 0;
					int enemy = 'D';
					entities[maxLevel-1][x][y] = (ENEMYID | (enemy << 4)
							| baseHealth | (DETECTED << 16));
					map[maxLevel-1][x][y] = FLOOR;
					break outerloop;
				}
			}
		}
	}

	public void input(KeyEvent e) {
		if (state == State.MAINMENU) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				reset();
				state = State.GAME;
			} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				if (playerName.length() > 0)
					playerName = playerName.substring(0,
							playerName.length() - 1);
			} else if (Character.isDefined(e.getKeyChar())) {
				playerName += e.getKeyChar();
			}
		}
	}

	public void input(boolean[] keys) {
		if (state == State.INTRO) {
			if (keys[HIT]) {
				state = State.MAINMENU;
				playerName = "";
			}
		}

		if (state == State.GAME) {
			if (!dead && !paused) {
				if (keys[INVENTORY]) {
					paused = true;
					inventory = true;
				}
				if (keys[UP] || keys[DOWN]) {
					consoleText.clear();
					if (keys[UP]) {
						if (entities[level][px - 1][py] != NULL) {
							hitEntity(px - 1, py);
						} else if (px - 1 >= 0
								&& !isWall(map[level][px - 1][py])
								&& walk(px - 1, py)) {
							px--;
						}
					}
					if (keys[DOWN]) {
						if (entities[level][px + 1][py] != NULL) {
							hitEntity(px + 1, py);
						} else if (px + 1 < map[level].length
								&& !isWall(map[level][px + 1][py])
								&& walk(px + 1, py)) {
							px++;
						}
					}
					nextTurn();
				} else if (keys[LEFT] || keys[RIGHT]) {
					consoleText.clear();
					if (keys[LEFT]) {
						if (entities[level][px][py - 1] != NULL) {
							hitEntity(px, py - 1);
						} else if (py - 1 >= 0
								&& !isWall(map[level][px][py - 1])
								&& walk(px, py - 1))
							py--;
					}
					if (keys[RIGHT]) {
						if (entities[level][px][py + 1] != NULL) {
							hitEntity(px, py + 1);
						} else if (px + 1 < map[level][px].length
								&& !isWall(map[level][px][py + 1])
								&& walk(px, py + 1))
							py++;
					}
					nextTurn();
				} else if (keys[HIT]) {
					consoleText.clear();
					hit();
					nextTurn();
				} else if (keys[USE]) {
					use();
				}
			} else if (keys[HIT]) {
				if (dead && !paused) {
					state = State.MAINMENU;
					playerName = "";
				}
				paused = false;
				inventory = false;
			}
		}

	}

	private int[][] convertMapToInt() {
		int[][] intMap = new int[map[level].length][map[level][0].length];
		for (int x = 0; x < intMap.length; x++) {
			for (int y = 0; y < intMap[0].length; y++) {
				intMap[x][y] = (int) (map[level][x][y]);
			}
		}
		return intMap;
	}

	private class GenerationNode {
		public int x, y, width, height;
		public ArrayList<GenerationNode> subGenerationNodes = new ArrayList<GenerationNode>();

		public GenerationNode(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public GenerationNode getGenerationNode(int n) {
			return subGenerationNodes.get(n);
		}

		public void populateSubGenerationNodes() {
			int width = (int) (Math.random() * 4 + 3);
			int height = (int) (Math.random() * 4 + 3);
			int rx = 1 + (int) (Math.random() * (CHARSPERHEIGHT - height - 2));
			int ry = 1 + (int) (Math.random() * (CHARSPERWIDTH - width - 2));

			subGenerationNodes.add(new GenerationNode(rx, ry, width, height));
		}

		public void fillSubGenerationNodes(char[][] map) {
			for (GenerationNode n : subGenerationNodes) {
				if ((int) (Math.random() * 4) == 0
						&& n.width > 3
						&& n.height > 3
						&& subGenerationNodes.indexOf(n) != 0
						&& subGenerationNodes.indexOf(n) != subGenerationNodes
								.size() - 1) {
					for (int x2 = n.x; x2 < n.x + n.width; x2++) {
						for (int y2 = n.y; y2 < n.y + n.height; y2++) {
							if (x2 >= 0 && y2 >= 0 && x2 < map.length - 1
									&& y2 < map[x2].length - 1
									&& map[x2][y2] != STAIRSDOWN
									&& map[x2][y2] != STAIRSUP) {
								if (x2 > n.x && x2 < n.x + n.width - 1
										&& y2 > n.y && y2 < n.y + n.height - 1) {
									map[x2][y2] = WATER;
								} else if ((int) (Math.random() * 3) == 0)
									map[x2][y2] = FLOOR;
								else
									map[x2][y2] = GRASS;
							}
						}
					}
				} else {
					for (int x2 = n.x; x2 < n.x + n.width; x2++) {
						for (int y2 = n.y; y2 < n.y + n.height; y2++) {
							if (x2 >= 0 && y2 >= 0 && x2 < map.length - 1
									&& y2 < map[x2].length - 1
									&& map[x2][y2] != STAIRSDOWN
									&& map[x2][y2] != STAIRSUP) {
								if ((int) (Math.random() * 8) == 0)
									map[x2][y2] = GRASS;
								else if ((int) (Math.random() * 400) == 0)
									map[x2][y2] = PLANT1;
								else if ((int) (Math.random() * 400) == 0)
									map[x2][y2] = PLANT2;
								else if ((int) (Math.random() * 400) == 0)
									map[x2][y2] = PLANT3;
								else
									map[x2][y2] = FLOOR;
							}
						}
					}
				}
				if (subGenerationNodes.indexOf(n) == 0) {
					map[n.x + n.width / 2][n.y + n.height / 2] = STAIRSDOWN;
				}
				if (subGenerationNodes.indexOf(n) == subGenerationNodes.size() - 1) {
					map[n.x + n.width / 2][n.y + n.height / 2] = STAIRSUP;
				}
			}

		}

		public void linkSubGenerationNodes(char[][] map, int n1, int n2) {
			if (subGenerationNodes.size() > 0) {
				int x2 = getGenerationNode(n1).x + getGenerationNode(n1).width
						/ 2;
				int y2 = getGenerationNode(n1).y + getGenerationNode(n1).height
						/ 2;
				int x3 = getGenerationNode(n2).x + getGenerationNode(n2).width
						/ 2;
				int y3 = getGenerationNode(n2).y + getGenerationNode(n2).height
						/ 2;

				while (x2 != x3 || y2 != y3) {
					if ((int) (Math.random() * 2) == 0) {
						if (x2 > x3)
							x2--;
						else if (x2 < x3)
							x2++;
					} else {
						if (y2 > y3)
							y2--;
						else if (y2 < y3)
							y2++;
					}
					if (x2 >= 0 && y2 >= 0 && x2 < map.length
							&& y2 < map[x2].length)
						map[x2][y2] = PATH;
				}
			}
		}

	}

}