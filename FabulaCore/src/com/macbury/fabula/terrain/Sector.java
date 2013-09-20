package com.macbury.fabula.terrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

public class Sector extends Renderable implements Disposable {
  public final static int ROW_COUNT               = 5;
  public final static int COLUMN_COUNT            = 5;
  public final static int VERTEX_PER_BOX_COUNT    = 4;
  public final static int VERTEX_ATTRIBUTE_COUNT  = 6;
  public final static int TOTAL_ATTRIBUTES_COUNT  = VERTEX_PER_BOX_COUNT * VERTEX_ATTRIBUTE_COUNT;
  public final static int VERTEX_PER_ROW          = VERTEX_PER_BOX_COUNT * COLUMN_COUNT;
  
  private Vector3 bottomRightCorner;
  private Vector3 topLeftCorner;
  private Terrain terrain;
  private BoundingBox boundingBox;
  
  private TriangleGrid triangleGrid;
  
  public Sector(Vector3 pos, Terrain terrain) {
    this.terrain           = terrain;
    this.topLeftCorner     = pos;
    this.bottomRightCorner = pos.cpy().add(COLUMN_COUNT, 0, ROW_COUNT);
    this.triangleGrid      = new TriangleGrid(COLUMN_COUNT, ROW_COUNT, false); //TODO: change to static for non world edit
    this.primitiveType     = GL20.GL_TRIANGLES;
  }

  public int getStartX() {
    return (int)this.topLeftCorner.x;
  }
  
  public int getStartZ() {
    return (int)this.topLeftCorner.z;
  }
  
  public int getEndX() {
    return (int)this.bottomRightCorner.x;
  }
  
  public int getEndZ() {
    return (int)this.bottomRightCorner.z;
  }
  
  public void build() {
    float minHeight = 0.0f;
    float maxHeight = 1.0f;
    
    short rowEnd    = (short) (ROW_COUNT + topLeftCorner.z);
    short columnEnd = (short) (COLUMN_COUNT+topLeftCorner.x);
    
    TileTransformer transformer = new TileTransformer();
    this.mesh = null;
    triangleGrid.begin();
      for (int z = (int) topLeftCorner.z; z < rowEnd; z++) {
        for (int x = (int) topLeftCorner.x; x < columnEnd; x++) {
          Tile tile = terrain.getTile(x, z);
          transformer.setTile(tile);
          tile.calculateHeight();
          maxHeight    = Math.max(tile.getY(), maxHeight);
          minHeight    = Math.min(tile.getY(), minHeight);
          if (tile.getAutoTiles().isSlope()) {
            switch (tile.getSlope()) {
              case CornerTopLeft:
                createCornerTopLeftMeshTile(tile);
              break;
              
              case CornerTopRight:
                createCornerTopRightMeshTile(tile);
              break;
              
              case CornerBottomLeft:
                createCornerBottomLeftMeshTile(tile);
              break;
              
              case CornerBottomRight:
                createCornerBottomRightMeshTile(tile);
              break;
              
              case EdgeBottomRight:
                createEdgeBottomRightMeshTile(tile);
              break;
              
              case EdgeBottomLeft:
                createEdgeBottomLeftMeshTile(tile);
              break;
              
              case EdgeTopLeft:
                createEdgeTopLeftMeshTile(tile);
              break;
              
              case EdgeTopRight:
                createEdgeTopRightMeshTile(tile);
              break;
              
              case Up:
                createTopMeshTile(tile);
              break;
              
              case Down:
                createBottomMeshTile(tile);
              break;
              
              case Left:
                createLeftMeshTile(tile);
              break;
              
              case Right:
                createRightMeshTile(tile);
              break;
              
              default:
                createBottomMeshTile(tile);
              break;
            }
          } else {
            createBottomMeshTile(tile);
          }
        }
      }
    triangleGrid.end();
    
    Vector3 firstCorner = this.topLeftCorner.cpy();
    firstCorner.y = maxHeight;
    this.boundingBox = new BoundingBox(firstCorner, this.bottomRightCorner.cpy().add(0, minHeight, 0));
  }

  private void createEdgeTopRightMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    //bottom
    /* Top Right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    ////triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    ////triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Top left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Bottom right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createEdgeBottomLeftMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    
    /* bottom right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top right Vertex */
    n2 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom left Vertex */
    n3 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createEdgeTopLeftMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    
    /* bottom right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top right Vertex */
    n2 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom left Vertex */
    n3 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createEdgeBottomRightMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    //bottom
    /* Top Right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Top left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Bottom right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createCornerBottomRightMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    //bottom
    /* Top Right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Top left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Bottom right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createCornerBottomLeftMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    
    /* bottom right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top right Vertex */
    n2 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom left Vertex */
    n3 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createCornerTopRightMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    
    /* bottom right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top right Vertex */
    n2 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom left Vertex */
    n3 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createCornerTopLeftMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    
    /* Top right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom Right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createLeftMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
 // horizontal left
    /* bottom right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top right Vertex */
    n2 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom left Vertex */
    n3 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* top left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n2,n1,n3);
  }

  private void createRightMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
 // horizontal right
    /* Top left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* bottom left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* top right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* bottom left Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createTopMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    // Top
    /* Bottom Right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Bottom left Vertex */
    n3 = triangleGrid.addVertex(x, tile.getY2(), z+1);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Top right Vertex */
    n2 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Top left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createBottomMeshTile(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    //bottom
    /* Top Right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Top left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Bottom right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }

  private void createCornerTop(Tile tile) {
    TextureRegion uvMap = tile.getTextureRegion();
    float x  = tile.getX();
    float z  = tile.getZ();
    short n1 = 0;
    short n2 = 0;
    short n3 = 0;
    
    /* Top left Vertex */
    n1 = triangleGrid.addVertex(x, tile.getY1(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Bottom left Vertex */
    n2 = triangleGrid.addVertex(x, tile.getY2(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    /* Top Right Vertex */
    n3 = triangleGrid.addVertex(x+1f, tile.getY3(), z);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n1,n2,n3);
    
    /* Bottom right Vertex */
    n1 = triangleGrid.addVertex(x+1f, tile.getY4(), z+1f);
    //triangleGrid.addColorToVertex(255, 255, 255, 255);
    triangleGrid.addUVMap(uvMap.getU2(), uvMap.getV2());
    //triangleGrid.addNormal();
    if (terrain.isDebuging()) {
      triangleGrid.addTilePos(tile.getX(), tile.getZ());
    }
    
    triangleGrid.addIndices(n3,n2,n1);
  }


  public boolean visibleInCamera(Camera camera) {
    return camera.frustum.boundsInFrustum(boundingBox);
  }

  public Vector3 getPositionForRay(Ray ray, Vector3 mouseTilePosition) {
    if (Intersector.intersectRayTriangles(ray, triangleGrid.getVerties(), triangleGrid.getIndices(), triangleGrid.getVertexSize(), mouseTilePosition)) {
      return mouseTilePosition;
    } else {
      return null;
    }
  }

  public Mesh getMesh() {
    if (mesh == null) {
      this.mesh = triangleGrid.getMesh();
      this.meshPartOffset = 0;
      this.meshPartSize = mesh.getNumIndices();
    }
    return this.mesh;
  }

  @Override
  public void dispose() {
    this.triangleGrid.dispose();
    if (this.mesh != null) {
      this.mesh.dispose();
    }
    
    if (this.shader != null) {
    }
  }
  
  public BoundingBox getBounds() {
    return boundingBox;
  }
}
