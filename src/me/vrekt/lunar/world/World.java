package me.vrekt.lunar.world;

import me.vrekt.lunar.entity.Entity;
import me.vrekt.lunar.location.Location;
import me.vrekt.lunar.raycast.RayCast;
import me.vrekt.lunar.tile.Tile;
import me.vrekt.lunar.world.dir.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class World {
    protected final Map<Location, Tile> worldInfo = new HashMap<>();
    protected final List<Entity> worldEntities = new ArrayList<>();
    protected final List<Entity> worldEntitiesAdd = new ArrayList<>();
    protected final List<Entity> worldEntitiesRemove = new ArrayList<>();
    protected String name;

    protected int width, height, tileWidth, tileHeight, worldAnchorX, worldAnchorY;
    private WorldGrid grid;

    /**
     * Initialize the world.
     *
     * @param name   Name of the world
     * @param width  Width of the world
     * @param height Height of the world
     */
    public World(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;

        worldAnchorX = 0;
        worldAnchorY = 0;

    }

    /**
     * Initialize the world. Use this constructor for grids.
     *
     * @param name       Name of the world
     * @param width      Width of the world
     * @param height     Height of the world
     * @param tileWidth  the width of the tiles.
     * @param tileHeight the height of the tiles.
     */
    public World(String name, int width, int height, int tileWidth, int tileHeight) {
        this(name, width, height);

        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;

        this.grid = new WorldGrid(width, height, tileWidth, tileHeight);

    }

    /**
     * Initialize the world. Use this constructor for grids.
     *
     * @param name   Name of the world.
     * @param width  Width of the world.
     * @param height Height of the world.
     * @param grid   the WorldGrid
     */
    public World(String name, int width, int height, WorldGrid grid) {
        this(name, width, height);

        this.grid = grid;

    }

    /**
     * Gets the name of the world
     */
    public final String getName() {
        return name;
    }

    /**
     * Add an entity to the world.
     */
    public final void addEntity(Entity entity) {
        worldEntities.add(entity);
    }

    /**
     * Remove the entity from the world.
     *
     * @param entity the entity that should be removed from the world.
     */
    public final void removeEntity(Entity entity) {
        worldEntities.remove(entity);
    }

    /**
     * Add the entity to the list for removal. Will be removed at the beginning
     * of the next world tick.
     */
    public void queueEntityForRemoval(Entity entity) {
        worldEntitiesRemove.add(entity);
    }

    /**
     * Add the entity to the list for addition. Will be added at the beginning
     * of the next world tick.
     */
    public void queueEntityForAdd(Entity entity) {
        worldEntitiesAdd.add(entity);
    }

    /**
     * Remove all entities in the removal list from the world.
     */
    public void removeQueuedEntities() {
        worldEntitiesRemove.forEach(this::removeEntity);
        worldEntitiesRemove.clear();
    }

    /**
     * Add all entities in the add list to the world.
     */
    public void addQueuedEntities() {
        worldEntitiesAdd.forEach(this::addEntity);
        worldEntitiesAdd.clear();
    }

    /**
     * Add a tile
     */
    public final void addTile(int x, int y, Tile tile) {
        worldInfo.put(new Location(x, y), tile);
    }

    /**
     * Add a tile.
     */
    public final void addTile(Tile tile) {
        worldInfo.put(new Location(tile.getX(), tile.getY()), tile);
    }

    /**
     * Add multiple tiles in one direction, easier for making worlds/maps.
     *
     * @param x          coordinate of the tile
     * @param y          coordinate of the tile
     * @param direction  the direction to draw the tiles to
     * @param tileAmount indicates how many tiles to draw in the direction.
     */
    public final void addBatchTiles(Tile tile, int x, int y, Direction direction, int tileAmount) {
        int width = tile.getWidth();
        int height = tile.getHeight();

        while (tileAmount > 0) {
            tileAmount--;

            worldInfo.put(new Location(x, y), tile);
            x = direction == Direction.RIGHT ? x + width : direction == Direction.LEFT ? x - width : x;
            y = direction == Direction.DOWN ? y + height : direction == Direction.UP ? y - height : y;

        }
    }

    /**
     * Remove the tile.
     */
    public final void removeTileAt(int x, int y) {
        Location loc = new Location(x, y);
        if (worldInfo.containsKey(loc)) {
            worldInfo.remove(loc);
        }
    }

    /**
     * Get an entity by ID.
     */
    public final Entity getEntity(int entityID) {
        return worldEntities.stream().filter(entity -> entity.getEntityID() == entityID).findAny().orElse(null);
    }

    /**
     * Get the tile the entity is standing on.
     */
    public final Tile getTileFromEntity(Entity entity) {
        return getTileAt(entity.getX(), entity.getY());
    }

    /**
     * Draw all world entities
     */
    public final void drawAllEntities(Graphics graphics) {
        worldEntities.forEach(entity -> graphics.drawImage(entity.getTexture(), entity.getX(), entity.getY(), null));
    }

    /**
     * Draw all tiles.
     */
    public final void drawAllTiles(Graphics graphics) {
        for (Location key : worldInfo.keySet()) {
            Tile tile = worldInfo.get(key);
            graphics.drawImage(tile.getTexture(), key.getX(), key.getY(), null);
        }
    }

    /**
     * Check if an entity is at this X and Y.
     */
    public final boolean isEntityAt(int x, int y) {
        return worldEntities.stream().anyMatch(entity -> entity.getX() == x && entity.getY() == y);
    }

    /**
     * Get the entity at the X and Y.
     */
    public final Entity getEntityAt(int x, int y) {
        return worldEntities.stream().filter(entity -> entity.getX() == x && entity.getY() == y).findAny().orElse(null);
    }

    /**
     * Gets a list of all entities in the world
     *
     * @return the entities in the world
     */
    public final List<Entity> getWorldEntities() {
        return worldEntities;
    }

    /**
     * Get the tile at the specified X and Y.
     */
    public final Tile getTileAt(int x, int y) {
        Optional<Location> stream = worldInfo.keySet().stream()
                .filter(location -> location.getX() == x && location.getY() == y).findAny();
        return stream.isPresent() ? worldInfo.get(stream.get()) : null;
    }

    /**
     * Returns the width of the world.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Returns the height of the world.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Get the tile width
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Get the tile height
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * @return the world grid.
     */
    public WorldGrid getGrid() {
        return grid;
    }

    /**
     * Set the world grid.
     *
     * @param grid the world grid.
     */
    public void setGrid(WorldGrid grid) {
        this.grid = grid;
    }

    /**
     * Perform a ray cast from (originX, originY) in the direction of (dirX, dirY) for the given distance.
     */
    public RayCast.RayCastResult rayCast(int x, int y, int dirX, int dirY, float distance) {
        return new RayCast(this).doRayCast(x, y, dirX, dirY, distance);
    }

    /**
     * Perform a ray cast from (originX, originY) to (targetX, targetY).
     */
    public RayCast.RayCastResult rayCast(int x, int y, int targetX, int targetY) {
        return new RayCast(this).doRayCast(x, y, targetX, targetY);
    }

    /**
     * Set the anchor point of the world, this is the top left most point in the world
     * as an offset from the top left point of the screen.
     */
    public void setWorldAnchorX(int worldAnchorX) {
        this.worldAnchorX = worldAnchorX;
    }

    /**
     * Set the anchor point of the world, this is the top left most point in the world
     * as an offset from the top left point of the screen.
     */
    public void setWorldAnchorY(int worldAnchorY) {
        this.worldAnchorY = worldAnchorY;
    }

    /**
     * Get the location of the x location of the top left most point in the world
     * as an offset from the top left x point of the world.
     */
    public int getWorldAnchorX() {
        return worldAnchorX;
    }

    /**
     * Get the location of the y location of the top left most point in the world
     * as an offset from the top left y point of the world.
     */
    public int getWorldAnchorY() {
        return worldAnchorY;
    }

    /**
     * Translate from screen space to world space and check if the tile that contains
     * the pixel at the given coordinate is passable.
     */
    public boolean isPointPassable(int pixelX, int pixelY) {
        int tileX = (pixelX - worldAnchorX) / tileWidth;
        int tileY = (pixelY - worldAnchorY) / tileHeight;
        if (tileX < 0 || tileX >= width
                || tileY < 0 || tileY >= height) {
            return false;
        }

        Tile tile = getTileAt(tileX, tileY);
        if (tile == null) {
            // No tile, return true?
            return true;
        }

        // Right now solidity is the only measure we have of "passability"
        return !tile.isSolid();
    }

    /**
     * Translate from world space to screen space
     */
    public Location worldToScreenLocation(Location worldLocation) {
        return worldToScreenLocation(worldLocation.getX(), worldLocation.getY());
    }

    /**
     * Translate from world space to screen space
     */
    public Location worldToScreenLocation(int worldX, int worldY) {
        return new Location(worldAnchorX + worldX * tileWidth, worldAnchorY + worldY * tileHeight);
    }

    /**
     * Translate from screen space to world space
     */
    public Location screenToWorldLocation(int pixelX, int pixelY) {
        return new Location((pixelX - worldAnchorX) / tileWidth, (pixelY - worldAnchorY) / tileHeight);
    }

    /**
     * Gets executed when you the world is drawn.
     */
    public abstract void onDraw(Graphics graphics);

    /**
     * Gets executed when the world ticks
     */
    public void onTick() {
        removeQueuedEntities();
        addQueuedEntities();
    }
}
