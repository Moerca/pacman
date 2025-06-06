# Pac-Man Java

A classic Pac-Man remake written in Java using Swing.  
Control Pac-Man, avoid ghosts, eat all the food, grab cherries for power-ups, and try to achieve the highest score possible.  
Now with highscore saving, a win screen, and smooth, buffered controls for an authentic arcade feel!
Soon multiple maps!

---
![image](https://github.com/user-attachments/assets/da782c24-0b33-4b29-92e4-5026bc1499a5)

![pac2](https://github.com/user-attachments/assets/e5aef24f-004a-4581-9853-1bb926c8523d)

![pac1](https://github.com/user-attachments/assets/490d9d18-d822-4fda-aa3f-c63399cef31e)


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
