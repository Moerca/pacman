# Pac-Man Java

A classic Pac-Man remake written in Java using Swings.

![image](https://github.com/user-attachments/assets/d19da676-d981-4418-bf03-f5d437d3ace8)

![image](https://github.com/user-attachments/assets/b680232f-ff76-4961-979c-9d0f2b896a63)


## Features

- **Classic Pac-Man gameplay**: Eat pellets, avoid ghosts, grab cherries for power-ups.
- **Power-Up Mode**: Eat a cherry to turn the tables and eat the ghosts.
- **Cherry and Scared Ghost Logic**: Timed cherry spawn, ghosts turn blue and can be eaten.
- **Buffered Controls**: Responsive movement—hold a direction and Pac-Man turns when possible.
- **Game Over & Win Screens**: Clear overlays with scores and restart prompts.
- **Highscore Saving**: Best score is saved in `highscore.txt`.
- **Multiple Maps**: Easily extendable for map rotation on level win.

---

## Controls

- **Arrow keys**: Move Pac-Man.
- **P** or **Space**: Pause/Unpause the game.
- **Any key**: Restart after Game Over or proceed after winning.

---

## How to Run

1. **Clone or Download** this repository.
2. **Ensure you have Java (JDK 8+) installed**.
3. **Place images** in an `img/` directory in the root folder:
    - `wall.png`
    - `blueGhost.png`
    - `orangeGhost.png`
    - `pinkGhost.png`
    - `redGhost.png`
    - `scaredGhost.png`
    - `pacmanUp.png`
    - `pacmanDown.png`
    - `pacmanLeft.png`
    - `pacmanRight.png`
    - `cherry.png`
4. **Compile and run:**
    ```sh
    javac PacMan.java
    java PacMan
    ```
5. **Enjoy!**

---

## Customization

- **Maps**:  
  You can add more maps by defining new `String[]` arrays and rotating them after each win.
- **Sprites**:  
  Swap out images in `/img` for your own art.
- **Gameplay Tweaks**:  
  Adjust constants (`cherryAppearInterval`, `scaredDuration`, etc.) for difficulty.
