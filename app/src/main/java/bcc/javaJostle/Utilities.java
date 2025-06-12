package bcc.javaJostle;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.io.File;

public class Utilities {
    public static BufferedImage WALL_IMAGE;
    public static BufferedImage GRASS_IMAGE;
    public static BufferedImage MUD_IMAGE;
    public static BufferedImage ROBOT_ERROR;
    public static BufferedImage DEFAULT_PROJECTILE_IMAGE;
    public static BufferedImage HEALTH_PACK_IMAGE;
    public static BufferedImage SPEED_PACK_IMAGE;
    public static BufferedImage ATTACK_PACK_IMAGE;    
    public static BufferedImage CHAOS_BOT_IMAGE;
    public static BufferedImage CHAOS_BULLET_IMAGE;

    public final static int WALL = 0;
    public final static int GRASS = 1;
    public final static int MUD = 2;

    public final static double POWER_UP_SPAWN_CHANCE = .003;

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int TILE_SIZE = 32; // Default tile size
    public static final int PROJECTILE_SIZE = 10; // Default projectile size
    public static final int POWER_UP_SIZE = 20; // Default power-up size
    public static final int ROBOT_SIZE = 28; // Default robot size

    public static ArrayList<Integer> keysPressed = new ArrayList<Integer>();

    public static BufferedImage loadImage(String imgName) {
        try {
            // Ensure the full path is constructed correctly
            BufferedImage img = ImageIO.read(new File("app/src/main/resources/images/" + imgName));
            return cropToContent(img);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + imgName + " - " + e.getMessage());
            e.printStackTrace();
            return null; // Return null if loading fails
        }
    }

    public static void loadImages() {
        // Use the loadImage method for each image
        WALL_IMAGE = loadImage("wall.png");
        GRASS_IMAGE = loadImage("grass.png");
        MUD_IMAGE = loadImage("mud.png");
        ROBOT_ERROR = loadImage("robotError.png");
        DEFAULT_PROJECTILE_IMAGE = loadImage("defaultProjectile.png");
        CHAOS_BOT_IMAGE = loadImage("2020Bot.png");
        CHAOS_BULLET_IMAGE = loadImage("2020Bullet.png");

        // Optional: Add checks here if any image failed to load, though loadImage
        // already prints errors
        if (WALL_IMAGE == null) {
            System.err.println("WALL_IMAGE could not be loaded.");
        }
        if (GRASS_IMAGE == null) {
            System.err.println("GRASS_IMAGE could not be loaded.");
        }
        if (MUD_IMAGE == null) {
            System.err.println("MUD_IMAGE could not be loaded.");
        }
    }

    public static BufferedImage cropToContent(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        int minX = width;
        int minY = height;
        int maxX = 0;
        int maxY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = src.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                if (alpha > 0) {
                    if (x < minX)
                        minX = x;
                    if (y < minY)
                        minY = y;
                    if (x > maxX)
                        maxX = x;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            // No visible content
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        return src.getSubimage(minX, minY, (maxX - minX + 1), (maxY - minY + 1));
    }

    public static void handleKeyPressed(int keyCode) {
        if (!keysPressed.contains(keyCode)) {
            keysPressed.add(keyCode);
        }
    }

    public static void handleKeyReleased(int keyCode) {
        for (int i = 0; i < keysPressed.size(); i++) {
            if (keysPressed.get(i) == keyCode) {
                keysPressed.remove(i); // Remove using index for clarity
                break;
            }
        }
    }

    // For testing purposes only
    private static boolean[] testKeyStates = new boolean[256];

    // Test helper methods
    public static void resetKeyStates() {
        for (int i = 0; i < testKeyStates.length; i++) {
            testKeyStates[i] = false;
        }
    }

    public static void setKeyPressed(int keyCode, boolean isPressed) {
        if (keyCode < testKeyStates.length) {
            testKeyStates[keyCode] = isPressed;
        }
    }

    // Modify the existing isKeyPressed method to use the test states in test mode
    private static boolean inTestMode = false;

    public static void setTestMode(boolean enabled) {
        inTestMode = enabled;
    }

    // Update the existing isKeyPressed method
    public static boolean isKeyPressed(int keyCode) {
        if (inTestMode) {
            return keyCode < testKeyStates.length && testKeyStates[keyCode];
        } else {
            // Original implementation
            return keysPressed.contains(keyCode);
        }
    }

    public static Robot createRobot(int x, int y, String className) {
        File robotsDir = new File("app/src/main/resources/robots");
        if (!robotsDir.exists() || !robotsDir.isDirectory()) {
            System.err.println("Robots directory not found: " + robotsDir.getAbsolutePath());
        }
        if (className.equals("MyRobot")) {
            return new MyRobot(x, y);
        } else {
            try {
                URL[] urls = { robotsDir.toURI().toURL() };
                URLClassLoader classLoader = new URLClassLoader(urls, Game.class.getClassLoader());
                System.out.println("Attempting to load robot class: " + className + " at  (" + x + "," + y + ")");
                Class<?> loadedClass = classLoader.loadClass("bcc.javaJostle." + className);

                if (Robot.class.isAssignableFrom(loadedClass)) {
                    Constructor<?> constructor = loadedClass.getConstructor(int.class, int.class);
                    Robot robot = (Robot) constructor.newInstance(x, y);
                    return robot;
                    // System.out.println("Successfully loaded and instantiated: " + className);
                } else {
                    System.err.println("Class " + className + " does not extend Robot.");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Robot class not found: " + className + " - " + e.getMessage());
                return null; // Return null if the class is not found
            } catch (NoSuchMethodException e) {
                System.err.println(
                        "Constructor (int, int) not found for " + className + " - " + e.getMessage());
                return null; // Return null if the constructor is not found
            } catch (Exception e) {
                System.err.println("Error loading or instantiating " + className + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null; // Return null if the robot could not be created
    }
}