🏰 Lane Clash Tower Defense

A Java Strategy Game built with Swing

🎮 Overview

Lane Clash Tower Defense is a real-time, lane-based strategy game created in Java using the Swing framework. Players deploy troops, manage resources, and defend their bases across three lanes. The game includes two fully functional modes:

Survival Mode (Single Player)

PvP Mode (Two Players on Keyboard)

Troops move automatically, attack enemies in their lane, and interact using stats such as HP, attack damage, cooldowns, and range.

The game runs at ~60 FPS using Java’s Timer loop.

🧠 Game Modes
🟦 1. Survival Mode (1 Player)

You control three bases on the left side of the screen. Enemy waves spawn automatically at timed intervals.

Difficulty increases as you play:

Waves spawn faster

Stronger units (Tanks, Snipers) appear more often

You earn passive income every second

The game ends only when all three of your bases are destroyed, and it displays how many waves you survived.

🟥 2. PvP Mode (Player vs Player)

Two human players battle each other.

Player 1 spawns troops on the left

Player 2 spawns troops on the right

Each player has their own bases and coins

Units collide and fight automatically

The match lasts 1 minute

Win conditions:

Destroy all three enemy bases

OR have more total base HP when the timer hits zero

⚔️ Troop Types
Troop	Color	Stats
Normal	Blue / Red	Low cost, balanced stats
Tank	Dark Blue / Dark Red	Very high HP, slow movement
Fast	Cyan / Pink	High speed, moderate attack
Sniper	Purple / Magenta	Long range, high damage, slow fire rate

Each troop has:

HP bar

Cooldown timer

Attack range

Movement speed

Attack damage

Snipers also display a white aiming line.

🧱 Bases

Each lane has its own base with:

300 HP per base

Centered HP bar

Unique colors for left and right side

Player 1 = Yellow bases
Player 2 = Dark Red bases (PvP mode only)

Enemies damage the base when they reach melee range.

⌨️ Controls
🟦 Player 1 (Left Side — both modes)

Normal Troop:
1 = Lane 1
2 = Lane 2
3 = Lane 3

Tank:
Q = Lane 1
W = Lane 2
E = Lane 3

Fast:
A = Lane 1
S = Lane 2
D = Lane 3

Sniper:
Z = Lane 1
X = Lane 2
C = Lane 3

🟥 Player 2 (Right Side — PvP mode only)

Normal:
T = L1
Y = L2
U = L3

Tank:
G = L1
H = L2
J = L3

Fast:
B = L1
N = L2
M = L3

Sniper:
I = L1
O = L2
P = L3

🆘 Help Menu

Press H during the game to open the help overlay.
It shows:

Controls for Player 1 & Player 2

Troop descriptions

Cube color legend

Gameplay reminders

Press H again to close it.

🏗️ Technical Features

Java Swing graphics (custom rendering, HP bars, sprites as squares)

60 FPS game loop using Timer

OOP structure with Troop, GamePanel, MenuPanel, GameWindow, GameMode

Lane-based movement + targeting system

Real-time combat interaction

Increasing wave difficulty logic

Multi-player input handling

Game states (menu, gameplay, help overlay, game over screen)

Fully functional two-player competitive mode
