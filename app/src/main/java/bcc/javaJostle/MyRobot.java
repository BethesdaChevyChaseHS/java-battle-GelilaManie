package bcc.javaJostle;

import java.util.ArrayList;

public class MyRobot extends Robot {
    private int prevTargetX = -1;
    private int prevTargetY = -1;

    public MyRobot(int x, int y) {
        super(x, y, 3, 4, 2, 1, "iusecheatcodes2020", "2020Bot.png", "2020Bullet.png");
    }

    public void think(ArrayList<Robot> robots, ArrayList<Projectile> projectiles, Map map, ArrayList<PowerUp> powerups) {
        System.out.println("Thinking...");

        // Movement options
        Boolean moveLeft = canMoveTo(this.getX() - getSpeed(), this.getY(), map, robots);
        Boolean moveRight = canMoveTo(this.getX() + getSpeed(), this.getY(), map, robots);
        Boolean moveUp = canMoveTo(this.getX(), this.getY() - getSpeed(), map, robots);
        Boolean moveDown = canMoveTo(this.getX(), this.getY() + getSpeed(), map, robots);

        this.xMovement = 0;
        this.yMovement = 0;

        // Go for power-up if close or health is low
        PowerUp nearestPowerUp = null;
        int powerUpDist = Integer.MAX_VALUE;
        for (PowerUp p : powerups) {
            int dist = Math.abs(p.getX() - this.getX()) + Math.abs(p.getY() - this.getY());
            if (dist < powerUpDist) {
                powerUpDist = dist;
                nearestPowerUp = p;
            }
        }

        if (nearestPowerUp != null && (this.getHealth() < 2 || powerUpDist < 100)) {
            if (nearestPowerUp.getX() < this.getX() && moveLeft) {
                this.xMovement = -1;
            } else if (nearestPowerUp.getX() > this.getX() && moveRight) {
                this.xMovement = 1;
            } else if (nearestPowerUp.getY() < this.getY() && moveUp) {
                this.yMovement = -1;
            } else if (nearestPowerUp.getY() > this.getY() && moveDown) {
                this.yMovement = 1;
            }
            return;
        }

        // Avoid nearby projectiles
        for (Projectile proj : projectiles) {
            if (proj.getOwner() != this) {
                if (proj.getX() == this.getX() - 1) moveLeft = false;
                else if (proj.getX() == this.getX() + 1) moveRight = false;
                else if (proj.getY() == this.getY() - 1) moveUp = false;
                else if (proj.getY() == this.getY() + 1) moveDown = false;
            }
        }

        // Find target
        Robot target = null;
        for (Robot robo : robots) {
            if (!robo.getName().equals(this.getName())) {
                target = robo;
                break;
            }
        }

        if (target == null) return;

        boolean shoot = canAttack();

        // Predictive shooting
        if (shoot) {
            int predictedX = target.getX();
            int predictedY = target.getY();

            if (prevTargetX != -1 && prevTargetY != -1) {
                int velocityX = target.getX() - prevTargetX;
                int velocityY = target.getY() - prevTargetY;

                predictedX = target.getX() + velocityX * 2;
                predictedY = target.getY() + velocityY * 2;
            }

            shootAtLocation(predictedX, predictedY);
            System.out.println("Shooting at predicted location: (" + predictedX + ", " + predictedY + ")");
        }

        // Save previous target position
        prevTargetX = target.getX();
        prevTargetY = target.getY();

        // Movement toward target
        int xDiff = target.getX() - this.getX();
        int yDiff = target.getY() - this.getY();

        System.out.println("Targeting " + target.getName() + ": xDiff: " + xDiff + ", yDiff: " + yDiff);

        if (Math.abs(xDiff) >= Math.abs(yDiff)) {
            if (xDiff < 0 && moveLeft) {
                this.xMovement = -1;
            } else if (xDiff > 0 && moveRight) {
                this.xMovement = 1;
            } else if (yDiff < 0 && moveUp) {
                this.yMovement = -1;
            } else if (yDiff > 0 && moveDown) {
                this.yMovement = 1;
            }
        } else {
            if (yDiff < 0 && moveUp) {
                this.yMovement = -1;
            } else if (yDiff > 0 && moveDown) {
                this.yMovement = 1;
            } else if (xDiff < 0 && moveLeft) {
                this.xMovement = -1;
            } else if (xDiff > 0 && moveRight) {
                this.xMovement = 1;
            }
        }
    }

    public boolean isPointOkay(int pX, int pY, Map gameMap, ArrayList<Robot> allRobots) {
        if (gameMap == null || gameMap.getTiles() == null)
            return false;
        int[][] mapTiles = gameMap.getTiles();
        int mapRows = mapTiles.length;
        if (mapRows == 0)
            return false;
        int mapCols = mapTiles[0].length;
        if (mapCols == 0)
            return false;

        if (pX < 0 || pY < 0 || pX >= mapCols * Utilities.TILE_SIZE || pY >= mapRows * Utilities.TILE_SIZE) {
            return false;
        }

        int tileCol = (int) (pX / Utilities.TILE_SIZE);
        int tileRow = (int) (pY / Utilities.TILE_SIZE);
        if (tileRow < 0 || tileRow >= mapRows || tileCol < 0 || tileCol >= mapCols) {
            return false;
        }
        if (mapTiles[tileRow][tileCol] == Utilities.WALL) {
            return false;
        }

        if (allRobots != null) {
            for (Robot otherRobot : allRobots) {
                if (otherRobot == this || !otherRobot.isAlive()) {
                    continue;
                }
                if (pX >= otherRobot.getX() && pX < (otherRobot.getX() + Utilities.ROBOT_SIZE) &&
                        pY >= otherRobot.getY() && pY < (otherRobot.getY() + Utilities.ROBOT_SIZE)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canMoveTo(int targetX, int targetY, Map gameMap, ArrayList<Robot> allRobots) {
        int c1x = targetX;
        int c1y = targetY;
        int c2x = targetX + Utilities.ROBOT_SIZE - 1;
        int c2y = targetY;
        int c3x = targetX;
        int c3y = targetY + Utilities.ROBOT_SIZE - 1;
        int c4x = targetX + Utilities.ROBOT_SIZE - 1;
        int c4y = targetY + Utilities.ROBOT_SIZE - 1;

        return (isPointOkay(c1x, c1y, gameMap, allRobots) &&
                isPointOkay(c2x, c2y, gameMap, allRobots) &&
                isPointOkay(c3x, c3y, gameMap, allRobots) &&
                isPointOkay(c4x, c4y, gameMap, allRobots));
    }
}