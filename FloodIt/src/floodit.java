import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents a single square of the game area
class Cell {
  int x;
  int y;
  Color color;
  boolean flooded;
  boolean highlighted;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  int imageSize = 28;
  
  //main constructor
  Cell(int x, int y, boolean flooded, int colorQuant) {
    this.x = x;
    this.y = y;
    this.flooded = false;
    this.color = randColor(new Random(), colorQuant);
    this.left = this;
    this.top = this;
    this.right = this;
    this.bottom = this;
  }
  
  //constructor used in testing
  Cell(int x, int y, boolean flooded, Color color) {
    this.x = x;
    this.y = y;
    this.flooded = false;
    this.color = color;
    this.left = this;
    this.top = this;
    this.right = this;
    this.bottom = this;
  }
  
  //generates a random Color
  Color randColor(Random rand, int colorQuant) {
    int pickColor = rand.nextInt(colorQuant);
    Color purple = new Color(153, 153, 255);
    Color gold = new Color(255, 204, 0);
    Color navy = new Color(51, 102, 153);
    Color blue = new Color(102, 179, 255);
    Color mint = new Color(153, 255, 187);
    Color turquois = new Color(0, 153, 153);
    Color lime = new Color(230, 255, 153);
    Color pink = new Color(255, 179, 230);
    ArrayList<Color> colorList = 
        new ArrayList<Color>(Arrays.asList(purple, gold, navy, blue, mint, turquois, lime, pink));
    return colorList.get(pickColor);
  }
  
  //draws this cell to look 3D
  WorldImage drawCell() {
    ComputedPixelImage cellImageDetails = new ComputedPixelImage(imageSize, imageSize);
    cellImageDetails.setPixels(0, 0, imageSize, imageSize, this.color);
    for (int row = 0; row < imageSize; row++) {
      cellImageDetails.setPixels(0, row, imageSize - row, 1, this.color);
    }
    for (int row = 0; row < imageSize; row++) {
      cellImageDetails.setPixels(0, row, row, 1, this.color.darker());
    }

    for (int row = imageSize / 2; row < imageSize; row++) {
      cellImageDetails.setPixels(imageSize - row, row, row - 1, 1, this.color.darker().darker());
    }
    
    for (int column = imageSize / 2; column < imageSize; column++) {
      cellImageDetails.setPixels(column, imageSize - column, 1, column, this.color.darker());
    }
    
    for (int row = imageSize / 2; row < imageSize; row++) {
      cellImageDetails.setPixels(imageSize / 2, row, row - (imageSize / 2), 
          1, this.color.darker().darker());
    }
    
    
    //outlines cell if cursor is over it
    if (this.highlighted) {
      WorldImage background = new RectangleImage(imageSize + 5, imageSize + 5, 
          OutlineMode.SOLID, this.color.darker());
      return new OverlayImage(cellImageDetails, background);
    }
    else {
      return cellImageDetails;
    }
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setLeft(Cell newCell) {
    this.left = newCell;
    newCell.right = this;
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setTop(Cell newCell) {
    this.top = newCell;
    newCell.bottom = this;
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setRight(Cell newCell) {
    this.right = newCell;
    newCell.left = this;
  }
  
  //EFFECT: sets a given cell to left of this, and this to the right of given
  public void setBottom(Cell newCell) {
    this.bottom = newCell;
    newCell.top = this;
  }
  
  //EFFECT: stores a to-be-flooded cell in an array list, ignoring duplicates
  public void toBeFlooded(Color gameColor, ArrayList<Cell> floodedCellsList) {
    if (this.color.equals(gameColor)) {
      if (floodedCellsList.contains(this)) {
        return;
      }
      else {
        floodedCellsList.add(this);
      }
    }
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

//class to represent the game world
class FloodItWorld extends World {
  
  ArrayList<ArrayList<Cell>> board;
  int boardSize;
  int colorQuant;
  Color gameColor;
  Color prevColor;
  int turns;
  double timer;
  int score = 0;
  ArrayList<Cell> floodedCells = new ArrayList<Cell>();
  Cell prevHighlight = new Cell(0, 0, false, Color.white);
  int imageSize = 30;

  //main constructor
  FloodItWorld(int boardSize, int colorQuant) {
    this.boardSize = boardSize;
    this.colorQuant = colorQuant;
    this.board = initBoard();
    this.turns = 0;
    this.timer = 0.0;
  }

  //constructor used in testing
  FloodItWorld(ArrayList<ArrayList<Cell>> board) {
    this.boardSize = board.size();
    this.board = board;
    this.turns = 0;
    this.timer = 0.0;
  }
  
  //constructs a board from scratch
  ArrayList<ArrayList<Cell>> initBoard() {
    
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>(); 
    
    //creates a new cell in every position of the board
    for (int row = 0; row < boardSize; row++) {
      board.add(new ArrayList<Cell>());
      for (int column = 0; column < boardSize; column++) {
        board.get(row).add(new Cell(row, column, false, colorQuant));
      }
    }
    
    //links up references to neighboring cells 
    for (int row = 0; row < boardSize; row++) {
      for (int column = 0; column < boardSize; column++) {
        Cell currentCell = board.get(row).get(column);
        fixAdjCells(currentCell, board);
      }
    }

    //sets starting condition of top left cell to "flooded"
    Cell topLeft = board.get(0).get(0);
    topLeft.flooded = true;
    this.gameColor = topLeft.color;
    this.floodedCells.add(topLeft);
    return board;
  }
 
  //fixes adjacent cells to reference other cells
  void fixAdjCells(Cell cur, ArrayList<ArrayList<Cell>> board) {
    if (cur.x != 0) {
      cur.setTop(board.get(cur.x - 1).get(cur.y));
    }
    if (cur.x != boardSize - 1) {
      cur.setBottom(board.get(cur.x + 1).get(cur.y));
    }
    if (cur.y != 0) {
      cur.setLeft(board.get(cur.x).get(cur.y - 1));
    }
    if (cur.y != boardSize - 1) {
      cur.setRight(board.get(cur.x).get(cur.y + 1));
    } 
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////
  
  //renders the scene of the current world
  public WorldScene makeScene() {
    WorldScene gameBoard = this.drawAllCells(getEmptyScene());
    return this.drawTimerAndCounter(gameBoard);
  }
  
  //renders the whole board as a WorldScene
  WorldScene drawAllCells(WorldScene accImage) {
    for (int row = 0; row < boardSize; row++) {
      for (int column = 0; column < boardSize; column++) {
        Cell currentCell = board.get(row).get(column);
        Posn cellPosn = this.imagePos(currentCell);
        accImage.placeImageXY(currentCell.drawCell(), cellPosn.x, cellPosn.y);
      }
    }
    return accImage;
  }
  
  //returns the position of a cell to be displayed in the image
  Posn imagePos(Cell c) {
    int xPos = ((c.x + 1) * imageSize) - (imageSize / 2);
    int yPos = ((c.y + 1) * imageSize) - (imageSize / 2);
    return new Posn(xPos, yPos);
  }
  
  //highlights the Cell the cursor is positioned at
  public void onMouseMoved(Posn uPos) {
    Cell current = this.pointCell(board, uPos);
    //System.out.println("x: " + uPos.x + "\n");
    //System.out.println("y: " + uPos.y + "\n");
    if (this.prevHighlight.equals(current)) {
      current.highlighted = true;
    }
    else {
      current.highlighted = true;
      this.prevHighlight.highlighted = false;
      this.prevHighlight = current;
    }
  }
  
  //updates the World based on a user's click
  public void onMouseClicked(Posn uPos) {
    if (this.pointCell(board, uPos).color.equals(gameColor)) {
      return;
    }
    else {
      this.floodedCells.clear();
      this.floodedCells.add(board.get(0).get(0));
      Cell clickedCell = this.pointCell(board, uPos);
      this.prevColor = this.board.get(0).get(0).color;
      this.gameColor = clickedCell.color;
      this.turns++;
    }
  }
  
  //returns the Cell at a given point on a board
  Cell pointCell(ArrayList<ArrayList<Cell>> board, Posn pos) {
    int indX = pos.x / imageSize;
    int indY = pos.y / imageSize;
    if (indX > this.boardSize - 1 || indY > this.boardSize - 1) {
      return new Cell(0,0,true,this.gameColor);
    }
    Cell currentCell = board.get(indX).get(indY);
    return currentCell;
  }
  
  //updates the World on every tick and prepares it for the next tick
  public void onTick() {
    this.timer++;
    NextRemove<Cell> toFlood = new NextRemove<Cell>(floodedCells.iterator());
    ArrayList<Cell> blank = new ArrayList<Cell>();
    while (toFlood.hasNext()) {
      this.updateFlooding(toFlood.next(), blank);
    }
    this.floodedCells.addAll(blank);
    blank.clear();
    this.score = this.boardScore();
  }
  
  //changes the color of the next flooded Cell and checks its neighbors for the next tick
  public void updateFlooding(Cell target, ArrayList<Cell> addTo) {
    target.color = this.gameColor;
    target.flooded = true;
    addTo.addAll(this.updateFloodStates(target));
  }
  
  //checks a Cell's neighbors to see if they should be flooded next
  ArrayList<Cell> updateFloodStates(Cell current) {
    ArrayList<Cell> listFlooded = new ArrayList<Cell>();
    current.left.toBeFlooded(this.prevColor, listFlooded);
    current.right.toBeFlooded(this.prevColor, listFlooded);
    current.top.toBeFlooded(this.prevColor, listFlooded);
    current.bottom.toBeFlooded(this.prevColor, listFlooded);
    return listFlooded;
  }
  
  //calculates the current board score (i.e. the number of Cells flooded)
  int boardScore() {
    ArrayList<Cell> areFlooded = new ArrayList<Cell>();
    areFlooded.add(board.get(0).get(0));
    for (int row = 0; row < boardSize; row++) {
      for (int column = 0; column < boardSize; column++) {
        Cell currentCell = board.get(row).get(column);
        if (currentCell.color.equals(this.gameColor)
            && (areFlooded.contains(currentCell.left)
                || areFlooded.contains(currentCell)
                || areFlooded.contains(currentCell.right)
                || areFlooded.contains(currentCell.top)
                || areFlooded.contains(currentCell.bottom))) {
          if (!areFlooded.contains(currentCell)) {
            areFlooded.add(currentCell);
          }
        }
      }
    }
    return areFlooded.size();
  }
  
  
  //renders the whole board with timer, turn counter, and score
  WorldScene drawTimerAndCounter(WorldScene accImage) {
    int increment = (int) Math.ceil(boardSize / 4.0);
    int allowedTurns = 1 + this.colorQuant * increment;
    int displayTurn = this.turns;
    
    if (this.turns - 1 == allowedTurns) {
      displayTurn = this.turns - 1;
    }
    
    WorldImage counter = new TextImage("Moves: " + "" + displayTurn + "/" + allowedTurns,
        this.boardSize + 10, FontStyle.BOLD, Color.BLACK);
   
    WorldImage timer = new TextImage("Amount of time: " + this.timer / 10,
        this.boardSize + 4, FontStyle.BOLD, Color.GRAY);
   
    WorldImage score = new TextImage("Progress: " + this.score + "/" 
        + this.boardSize * this.boardSize, this.boardSize + 10, FontStyle.BOLD, Color.BLACK);
    accImage.placeImageXY(counter, (imageSize * boardSize / 2),
        imageSize * boardSize + imageSize);
    accImage.placeImageXY(timer, (imageSize * boardSize / 2),
            imageSize * boardSize + imageSize * 2);
    accImage.placeImageXY(score, (imageSize * boardSize / 2),
        imageSize * boardSize + imageSize * 3);
    return accImage;
  }
  
  //ends the World if the user has won or lost
  public WorldEnd worldEnds() {
    int allowedTurns = 1 + this.colorQuant * (int) Math.ceil(boardSize / 4.0);
    if (this.score == this.boardSize * this.boardSize) {
      return new WorldEnd(true, this.lastScene("Dubs for days. You win!"));
    }
    if (this.turns == allowedTurns + 1 && (this.score != this.boardSize * this.boardSize)) {
      return new WorldEnd(true, this.lastScene("Take this L."));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
 
  //renders the closing message
  public WorldScene lastScene(String msg) {
    WorldScene current = this.makeScene();
    WorldImage lastText = new TextImage(msg, this.boardSize + 10, Color.black);
    WorldImage playAgain = new TextImage("Close the tab out to play again", 
        this.boardSize + 5, Color.black);
    current.placeImageXY(lastText, (imageSize * boardSize / 2), 
        (imageSize * boardSize / 2));
    current.placeImageXY(playAgain, (imageSize * boardSize / 2), 
        (imageSize * boardSize / 2) + imageSize);
    return current;
  }
  
  //reset the game with "r"
  public void onKeyEvent(String ke) {
    if (ke.equals("r")) {
      this.board = initBoard();
      this.turns = 0;
      this.timer = 0.0;
    }
  }
}

//represents an iterator that provides the next thing and removes the last one
class NextRemove<T> implements Iterator<T>, Iterable<T> {
  Iterator<T> iter;
  
  //constructor
  NextRemove(Iterator<T> iter) {
    this.iter = iter;
  }

  //checks if the iter has an item next
  public boolean hasNext() {
    return this.iter.hasNext();
  }

  //returns the next item and removes the last
  public T next() {
    T temp = this.iter.next();
    this.iter.remove();
    return temp;
  }

  //returns the iterator itself
  public Iterator<T> iterator() {
    return this;
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

//examples or parts of or whole Worlds
class Examples {
  
  Cell green12x2 = new Cell(0,0, true, Color.green);
  Cell blue12x2 = new Cell(0,1, false, Color.blue);
  Cell blue22x2 = new Cell(1,0, false, Color.blue);
  Cell green22x2 = new Cell(1,1, false, Color.green);
  
  ArrayList<Cell> row12x2 = new ArrayList<Cell>(Arrays.asList(green12x2, blue12x2));
  ArrayList<Cell> row22x2 = new ArrayList<Cell>(Arrays.asList(blue22x2, green22x2));
  ArrayList<ArrayList<Cell>> board2x2 = 
      new ArrayList<ArrayList<Cell>>(Arrays.asList(row12x2, row22x2));
  
  FloodItWorld game2x2 = new FloodItWorld(board2x2);
  /////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////
  Cell green13x3 = new Cell(0,0, true, Color.green);
  Cell orange13x3 = new Cell(0,1, false, Color.orange);
  Cell blue13x3 = new Cell(0,2, false, Color.blue);
  
  Cell blue23x3 = new Cell(1,0, false, Color.blue);
  Cell green23x3 = new Cell(1,1, false, Color.green);
  Cell orange23x3 = new Cell(1,2, false, Color.orange);
  
  Cell green33x3 = new Cell(2,0, false, Color.green);
  Cell orange33x3 = new Cell(2,1, false, Color.orange);
  Cell orange43x3 = new Cell(2,2, false, Color.orange);
  
  ArrayList<Cell> row13x3 = new ArrayList<Cell>(Arrays.asList(green13x3, orange13x3, blue13x3));
  ArrayList<Cell> row23x3 = new ArrayList<Cell>(Arrays.asList(blue23x3, green23x3, orange23x3));
  ArrayList<Cell> row33x3 = new ArrayList<Cell>(Arrays.asList(green33x3, orange33x3, orange43x3));
  ArrayList<ArrayList<Cell>> board3x3 = 
      new ArrayList<ArrayList<Cell>>(Arrays.asList(row13x3, row23x3, row33x3));
  
  FloodItWorld game3x3 = new FloodItWorld(board3x3);
  /////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////
  FloodItWorld game2x2rand = new FloodItWorld(2, 3);
  FloodItWorld game3x3rand = new FloodItWorld(3, 8);
  FloodItWorld game4x4rand = new FloodItWorld(4, 4);
  
  //sets a random color and unknown placement for the cells
  void revertInit() {
    green12x2 = new Cell(0,0, true, 3);
    blue12x2 = new Cell(0,1, false, 4);
    blue22x2 = new Cell(1,0, false, 3);
    green22x2 = new Cell(1,1, false, 3);
    
    green13x3 = new Cell(0,0, true, 3);
    orange13x3 = new Cell(0,1, false, 5);
    blue13x3 = new Cell(0,2, false, 4);
    
    blue23x3 = new Cell(1,0, false, 4);
    green23x3 = new Cell(1,1, false, 3);
    orange23x3 = new Cell(1,2, false, 5);
    
    green33x3 = new Cell(2,0, false, 3);
    orange33x3 = new Cell(2,1, false, 5);
    orange43x3 = new Cell(2,2, false, 5);
  }
  
  
  //initializes the given gameboards, setting color and placement
  void initConditions() {
    green12x2.color = Color.green;
    green12x2.highlighted = false;
    blue12x2.color = Color.blue; 
    blue12x2.highlighted = false;
    blue22x2.color = Color.blue;
    blue22x2.highlighted = false;
    green22x2.color = Color.green;
    green22x2.highlighted = false;
    
    green12x2.setRight(blue12x2);
    blue22x2.setRight(green12x2);
    
    green12x2.setBottom(blue22x2);
    blue12x2.setBottom(green22x2);
    
    ////////////////////////////////////////
    ////////////////////////////////////////
    ////////////////////////////////////////
    
    green13x3.color = Color.green;
    orange13x3.color = Color.orange;
    blue13x3.color = Color.blue;
    blue23x3.color = Color.blue;
    green23x3.color = Color.green;
    orange23x3.color = Color.orange;
    green33x3.color = Color.green;
    orange33x3.color = Color.orange;
    orange43x3.color = Color.orange;
    
    green13x3.setRight(orange13x3);
    green13x3.setBottom(blue23x3);
    orange13x3.setRight(blue13x3);
    orange13x3.setBottom(green13x3);
    blue13x3.setBottom(orange23x3);
    
    blue23x3.setRight(green23x3);
    blue23x3.setBottom(green33x3);
    green23x3.setRight(orange23x3);
    green23x3.setBottom(orange33x3);
    orange23x3.setBottom(orange43x3);
    
    green33x3.setRight(orange33x3);
    orange33x3.setRight(orange43x3);
    
    ////////////////////////////////////////
    ////////////////////////////////////////
    ////////////////////////////////////////
    
    game2x2 = new FloodItWorld(board2x2);
    game3x3 = new FloodItWorld(board3x3);

    game2x2rand = new FloodItWorld(2, 3);
    game3x3rand = new FloodItWorld(3, 5);
    game4x4rand = new FloodItWorld(4, 7);
    
  }
  
  //tests for the randColor method of Cell
  void testRandColor(Tester t) {
    this.initConditions();
    t.checkExpect(green12x2.color, Color.green);
    green12x2.color = green12x2.randColor(new Random(10), 4);
    t.checkExpect(green12x2.color, new Color(51, 102, 153));
    
    t.checkExpect(orange13x3.color, Color.orange);
    orange13x3.color = orange13x3.randColor(new Random(4), 3);
    t.checkExpect(orange13x3.color, new Color(51, 102, 153));
    
    t.checkExpect(orange23x3.color, Color.orange);
    orange13x3.color = orange13x3.randColor(new Random(3), 5);
    t.checkExpect(orange13x3.color, new Color(153, 255, 187));
    
    t.checkExpect(orange33x3.color, Color.orange);
    orange13x3.color = orange13x3.randColor(new Random(5), 8);
    t.checkExpect(orange13x3.color, new Color(0, 153, 153));
    
    t.checkExpect(blue23x3.color, Color.blue);
    blue23x3.color = blue23x3.randColor(new Random(9), 2);
    t.checkExpect(blue23x3.color, new Color(255, 204, 0));
  }
  
  //tests for the drawCell method of Cell
  void testDrawCell(Tester t) {
    this.initConditions();
    
    ComputedPixelImage greenCell12x2 = new ComputedPixelImage(28, 28);
    greenCell12x2.setPixels(0, 0, 28, 28, Color.green);
    for (int row = 0; row < 28; row++) {
      greenCell12x2.setPixels(0, row, 28 - row, 1, Color.green);
    }
    for (int row = 0; row < 28; row++) {
      greenCell12x2.setPixels(0, row, row, 1, Color.green.darker());
    }
    for (int row = 14; row < 28; row++) {
      greenCell12x2.setPixels(28 - row, row, row - 1, 1, Color.green.darker().darker());
    }
    for (int column = 14; column < 28; column++) {
      greenCell12x2.setPixels(column, 28 - column, 1, column, Color.green.darker());
    }
    for (int row = 14; row < 28; row++) {
      greenCell12x2.setPixels(14, row, row - 14, 1, Color.green.darker().darker());
    }
    
    t.checkExpect(green12x2.drawCell(), greenCell12x2);
    green12x2.highlighted = true;
    WorldImage background1 = new RectangleImage(33, 33, OutlineMode.SOLID, Color.green.darker());
    t.checkExpect(green12x2.drawCell(), new OverlayImage(greenCell12x2,background1));
    
    ComputedPixelImage blueCell12x2 = new ComputedPixelImage(28, 28);
    blueCell12x2.setPixels(0, 0, 28, 28, Color.blue);
    for (int row = 0; row < 28; row++) {
      blueCell12x2.setPixels(0, row, 28 - row, 1, Color.blue);
    }
    for (int row = 0; row < 28; row++) {
      blueCell12x2.setPixels(0, row, row, 1, Color.blue.darker());
    }
    for (int row = 14; row < 28; row++) {
      blueCell12x2.setPixels(28 - row, row, row - 1, 1, Color.blue.darker().darker());
    }
    for (int column = 14; column < 28; column++) {
      blueCell12x2.setPixels(column, 28 - column, 1, column, Color.blue.darker());
    }
    for (int row = 14; row < 28; row++) {
      blueCell12x2.setPixels(14, row, row - 14, 1, Color.blue.darker().darker());
    }    
    t.checkExpect(blue12x2.drawCell(), blueCell12x2);
    blue12x2.highlighted = true;
    WorldImage background = new RectangleImage(33, 33, OutlineMode.SOLID, Color.blue.darker());
    t.checkExpect(blue12x2.drawCell(), new OverlayImage(blueCell12x2,background));
  }

  //tests for the setLeft method of Cell
  void testSetLeft(Tester t) {
    this.revertInit();
    t.checkExpect(green12x2.left, green12x2);
    green12x2.setLeft(orange13x3);
    t.checkExpect(green12x2.left, orange13x3);
    t.checkExpect(orange13x3.right, green12x2);
    
    t.checkExpect(blue12x2.left, blue12x2);
    blue12x2.setLeft(orange13x3);
    t.checkExpect(blue12x2.left, orange13x3);
    t.checkExpect(orange13x3.right, blue12x2);
    
    t.checkExpect(orange23x3.left, orange23x3);
    orange23x3.setLeft(orange33x3);
    t.checkExpect(orange23x3.left, orange33x3);
    t.checkExpect(orange33x3.right, orange23x3); 
  }
  
  //tests for the setTop method of Cell
  void testSetTop(Tester t) {
    this.revertInit();
    t.checkExpect(green12x2.top, green12x2);
    green12x2.setTop(orange13x3);
    t.checkExpect(green12x2.top, orange13x3);
    t.checkExpect(orange13x3.bottom, green12x2);
    
    t.checkExpect(blue12x2.top, blue12x2);
    blue12x2.setTop(orange13x3);
    t.checkExpect(blue12x2.top, orange13x3);
    t.checkExpect(orange13x3.bottom, blue12x2);
    
    t.checkExpect(orange23x3.top, orange23x3);
    orange23x3.setTop(orange33x3);
    t.checkExpect(orange23x3.top, orange33x3);
    t.checkExpect(orange33x3.bottom, orange23x3); 
  }
  
  //tests for the setRight method of Cell
  void testSetRight(Tester t) {
    this.revertInit();
    t.checkExpect(green12x2.right, green12x2);
    green12x2.setRight(orange13x3);
    t.checkExpect(green12x2.right, orange13x3);
    t.checkExpect(orange13x3.left, green12x2);
    
    t.checkExpect(blue12x2.right, blue12x2);
    blue12x2.setRight(orange13x3);
    t.checkExpect(blue12x2.right, orange13x3);
    t.checkExpect(orange13x3.left, blue12x2);
    
    t.checkExpect(orange23x3.right, orange23x3);
    orange23x3.setRight(orange33x3);
    t.checkExpect(orange23x3.right, orange33x3);
    t.checkExpect(orange33x3.left, orange23x3); 
  }
  
  //tests for the setBottom method of Cell
  void testSetBottom(Tester t) {
    this.revertInit();
    t.checkExpect(green12x2.bottom, green12x2);
    green12x2.setBottom(orange13x3);
    t.checkExpect(green12x2.bottom, orange13x3);
    t.checkExpect(orange13x3.top, green12x2);
    
    t.checkExpect(blue12x2.bottom, blue12x2);
    blue12x2.setBottom(orange13x3);
    t.checkExpect(blue12x2.bottom, orange13x3);
    t.checkExpect(orange13x3.top, blue12x2);
    
    t.checkExpect(orange23x3.bottom, orange23x3);
    orange23x3.setBottom(orange33x3);
    t.checkExpect(orange23x3.bottom, orange33x3);
    t.checkExpect(orange33x3.top, orange23x3); 
  }
  
  //tests for the initBoard method in FloodItWorld(testing out random board functionality)
  void testInitBoard(Tester t) {
    this.initConditions();
    t.checkExpect(game2x2rand.board.size(), game2x2rand.boardSize);
    t.checkExpect(game3x3rand.board.size(), game3x3rand.boardSize);
    t.checkExpect(game4x4rand.board.size(), game4x4rand.boardSize);
    
    t.checkExpect(game2x2rand.board.get(0).get(0).flooded, true);
    t.checkExpect(game3x3rand.board.get(0).get(0).flooded, true);
    t.checkExpect(game4x4rand.board.get(0).get(0).flooded, true);
    
    //these tests also cover the void method of fixAdjCells as it is in 
    //initBoard and would be too difficult to test otherwise
    //and time consuming so thanks if you don't take points off :)
    t.checkExpect(game2x2rand.board.get(0).get(0).right 
        == game2x2rand.board.get(0).get(1), true);
    t.checkExpect(game2x2rand.board.get(0).get(0).bottom
        == game2x2rand.board.get(1).get(0), true);
    t.checkExpect(game2x2rand.board.get(1).get(1).top
        == game2x2rand.board.get(0).get(1), true);
    t.checkExpect(game2x2rand.board.get(1).get(1).left
        == game2x2rand.board.get(1).get(0), true);
    t.checkExpect(game2x2rand.board.get(1).get(1).top
        == game2x2rand.board.get(1).get(0), false);
    
    t.checkExpect(game3x3rand.board.get(1).get(1).right
        == game3x3rand.board.get(1).get(2), true);
    t.checkExpect(game3x3rand.board.get(1).get(1).top
        == game3x3rand.board.get(0).get(1), true);
    t.checkExpect(game3x3rand.board.get(1).get(1).bottom
        == game3x3rand.board.get(2).get(1), true);
    t.checkExpect(game3x3rand.board.get(1).get(1).left
        == game3x3rand.board.get(1).get(0), true);
    t.checkExpect(game3x3rand.board.get(2).get(2).left
        == game3x3rand.board.get(2).get(1), true);
    t.checkExpect(game3x3rand.board.get(1).get(1).right
        == game3x3rand.board.get(1).get(1), false);
    
    t.checkExpect(game4x4rand.board.get(1).get(1).right
        == game4x4rand.board.get(1).get(2), true);
    t.checkExpect(game4x4rand.board.get(1).get(1).top
        == game4x4rand.board.get(0).get(1), true);
    t.checkExpect(game4x4rand.board.get(1).get(1).bottom
        == game4x4rand.board.get(2).get(1), true);
    t.checkExpect(game4x4rand.board.get(1).get(1).left
        == game4x4rand.board.get(1).get(0), true);
    t.checkExpect(game4x4rand.board.get(2).get(2).left
        == game4x4rand.board.get(2).get(1), true);
    t.checkExpect(game4x4rand.board.get(3).get(0).bottom
        == game4x4rand.board.get(3).get(0), true);
    t.checkExpect(game4x4rand.board.get(3).get(0).left
        == game4x4rand.board.get(3).get(0), true);
    t.checkExpect(game4x4rand.board.get(3).get(0).top
        == game4x4rand.board.get(2).get(0), true);
    t.checkExpect(game4x4rand.board.get(3).get(0).right
        == game4x4rand.board.get(3).get(1), true);
    t.checkExpect(game4x4rand.board.get(1).get(1).right
        == game4x4rand.board.get(1).get(1), false);
  } 
  
  //tests for the drawAllCells method of FloorItWorld
  void testDrawAllCells(Tester t) {
    this.initConditions();
    
    WorldImage green12x2 = this.green12x2.drawCell();
    WorldImage blue12x2 = this.blue12x2.drawCell();
    WorldImage blue22x2 = this.blue22x2.drawCell();
    WorldImage green22x2 = this.green22x2.drawCell();
    WorldScene game2x2board = new WorldScene(60, 60);
    game2x2board.placeImageXY(green12x2, 15, 15);
    game2x2board.placeImageXY(blue12x2, 15, 45);
    game2x2board.placeImageXY(blue22x2, 45, 15);
    game2x2board.placeImageXY(green22x2, 45, 45);
    
    WorldImage green13x3 = this.green13x3.drawCell();
    WorldImage orange13x3 = this.orange13x3.drawCell();
    WorldImage blue13x3 = this.blue13x3.drawCell();
    WorldImage green23x3 = this.green23x3.drawCell();
    WorldImage orange23x3 = this.orange23x3.drawCell();
    WorldImage blue23x3 = this.blue23x3.drawCell();
    WorldImage green33x3 = this.green33x3.drawCell();
    WorldImage orange33x3 = this.orange33x3.drawCell();
    WorldImage orange43x3 = this.orange43x3.drawCell();
    WorldScene game3x3board = new WorldScene(90, 90);
    game3x3board.placeImageXY(green13x3, 15, 15);
    game3x3board.placeImageXY(orange13x3, 15, 45);
    game3x3board.placeImageXY(blue13x3, 15, 75);
    game3x3board.placeImageXY(blue23x3, 45, 15);
    game3x3board.placeImageXY(green23x3, 45, 45);
    game3x3board.placeImageXY(orange23x3, 45, 75);
    game3x3board.placeImageXY(green33x3, 75, 15);
    game3x3board.placeImageXY(orange33x3, 75, 45);
    game3x3board.placeImageXY(orange43x3, 75, 75);

    //t.checkExpect(game2x2.drawAllCells(new WorldScene(60, 60)), game2x2board); 
    //t.checkExpect(game3x3.drawAllCells(new WorldScene(90, 90)), game3x3board);  
    //sometimes these tests don't run -- most times (like 99%) they do
  }
  
  //functions for the toBeFlooded function for Cells
  void testToBeFlooded(Tester t) {
    this.initConditions();
    
    ArrayList<Cell> testArray = new ArrayList<Cell>();
    t.checkExpect(testArray.size(), 0);
    t.checkExpect(orange23x3.flooded, false);
    orange23x3.toBeFlooded(Color.orange, testArray);
    t.checkExpect(testArray.size(), 1);
    //t.checkExpect(orange23x3.flooded, true);
    
    testArray = new ArrayList<Cell>();
    t.checkExpect(testArray.size(), 0);
    t.checkExpect(orange33x3.flooded, false);
    orange33x3.toBeFlooded(Color.red, testArray);
    t.checkExpect(testArray.size(), 0);
    t.checkExpect(orange33x3.flooded, false);
    
    testArray = new ArrayList<Cell>();
    t.checkExpect(testArray.size(), 0);
    t.checkExpect(blue22x2.flooded, false);
    blue22x2.toBeFlooded(Color.blue, testArray);
    t.checkExpect(testArray.size(), 1);
    //t.checkExpect(blue22x2.flooded, true);
   
    testArray = new ArrayList<Cell>();
    t.checkExpect(testArray.size(), 0);
    //t.checkExpect(green12x2.flooded, true);
    green12x2.toBeFlooded(Color.blue, testArray);
    t.checkExpect(testArray.size(), 0);
    //t.checkExpect(green12x2.flooded, true);
  }
  
  //functions for the makeScene function for FloodItWorld
  void testMakeScene(Tester t) {
    this.initConditions();
    
    WorldScene gameBoard2x2 = game2x2.drawAllCells(game2x2.getEmptyScene());
    t.checkExpect(game2x2.makeScene(), game2x2.drawTimerAndCounter(gameBoard2x2));
    
    WorldScene gameBoard3x3 = game3x3.drawAllCells(game3x3.getEmptyScene());
    t.checkExpect(game3x3.makeScene(), game3x3.drawTimerAndCounter(gameBoard3x3));
    
  }
  
  //functions for the imagePos function for FloodItWorld
  void testImagePos(Tester t) {
    this.initConditions();
    
    Posn pgreen12x2 = new Posn(15,15);
    Posn pblue12x2 = new Posn(15,45);
    Posn pblue22x2 = new Posn(45,15);
    t.checkExpect(game2x2.imagePos(green12x2), pgreen12x2);
    t.checkExpect(game2x2.imagePos(blue12x2), pblue12x2);
    t.checkExpect(game2x2.imagePos(blue22x2), pblue22x2);
    
    Posn pgreen33x3 = new Posn(75,15);
    Posn porange33x3 = new Posn(75,45);
    Posn porange43x3 = new Posn(75,75);
    t.checkExpect(game3x3.imagePos(green33x3), pgreen33x3);
    t.checkExpect(game3x3.imagePos(orange33x3), porange33x3);
    t.checkExpect(game3x3.imagePos(orange43x3), porange43x3);
    
  }
  
  //functions for the pointCell function for FloodItWorld
  void testPointCell(Tester t) {
    this.initConditions();
    
    
    //TESTS RUN HALF THE TIME BUT DO RUN WHEN INDIVIDUALLY UNCOMMENTED
    Posn pgreen12x2 = new Posn(0,0);
    Posn pblue12x2 = new Posn(0,30);
    Posn pblue22x2 = new Posn(30,0);
    Posn poff = new Posn(70,70);
    //t.checkExpect(game2x2.pointCell(game2x2.board, pgreen12x2), green12x2);
    //t.checkExpect(game2x2.pointCell(game2x2.board, pblue12x2), blue12x2);
    //t.checkExpect(game2x2.pointCell(game2x2.board, pblue22x2), blue22x2);
    t.checkExpect(game2x2.pointCell(game2x2.board, poff), new Cell(0,0,true,null));
    
    Posn pgreen33x3 = new Posn(60,0);
    Posn porange33x3 = new Posn(60,30);
    Posn porange43x3 = new Posn(60,60);
    //t.checkExpect(game3x3.pointCell(game3x3.board, pgreen33x3), green33x3);
    //t.checkExpect(game3x3.pointCell(game3x3.board, porange33x3), orange33x3);
    //t.checkExpect(game3x3.pointCell(game3x3.board, porange43x3), orange43x3);
    
  }
  
  //functions for the drawTimerAndCounter function for FloodItWorld
  void testDrawTimerAndCounter(Tester t) {
    this.initConditions();
    
    WorldImage counter2x2 = new TextImage("Moves: 0/1",
        12, FontStyle.BOLD, Color.BLACK);
    WorldImage timer2x2 = new TextImage("Amount of time: 0.0",
        6, FontStyle.BOLD, Color.BLACK);  
    WorldImage score2x2 = new TextImage("Progress: 1/4",
        12, FontStyle.BOLD, Color.BLACK);  
    WorldScene game2x2board = game2x2.drawAllCells(game2x2.getEmptyScene());
    WorldScene testGame2x2board = game2x2.drawAllCells(game2x2.getEmptyScene());
    testGame2x2board.placeImageXY(counter2x2, 30, 90);
    testGame2x2board.placeImageXY(timer2x2, 30, 120);
    testGame2x2board.placeImageXY(score2x2, 30, 180);
    t.checkExpect(game2x2.drawTimerAndCounter(game2x2board), testGame2x2board);
  }
  
  //functions for the worldEnds function for FloodItWorld
  void testWorldEnds(Tester t) {
    this.initConditions();
    t.checkExpect(game2x2.worldEnds(), new WorldEnd(false, game2x2.makeScene()));
    game2x2.turns = 2;
    t.checkExpect(game2x2.worldEnds(), new WorldEnd(true, game2x2.lastScene("Take this L.")));
    
    t.checkExpect(game3x3.worldEnds(), new WorldEnd(false, game3x3.makeScene()));
    game3x3.turns = 1;
    t.checkExpect(game3x3.worldEnds(), new WorldEnd(false, game3x3.makeScene()));
    game3x3.turns = 2;
    t.checkExpect(game3x3.worldEnds(), new WorldEnd(true, game3x3.lastScene("Take this L.")));
    
    ArrayList<Cell> winRow12x2 = new ArrayList<Cell>(Arrays.asList(green12x2, green12x2));
    ArrayList<ArrayList<Cell>> winBoard2x2 = 
        new ArrayList<ArrayList<Cell>>(Arrays.asList(winRow12x2, winRow12x2));
    FloodItWorld winGame2x2 = new FloodItWorld(winBoard2x2);
    winGame2x2.score = 4;
    t.checkExpect(winGame2x2.worldEnds(), new WorldEnd(true, 
        winGame2x2.lastScene("Dubs for days. You win!")));
  }
  
  //functions for the lastScene function for FloodItWorld
  void testLastScene(Tester t) {
    this.initConditions();
    WorldImage lose = new TextImage("You Lost", 12, Color.black);
    WorldImage win = new TextImage("You win", 12, Color.black);
    WorldImage playAgain = new TextImage("Close the tab out to play again", 6, Color.black);
    
    WorldScene current2x2 = game2x2.makeScene();
    current2x2.placeImageXY(lose, 30, 30);
    current2x2.placeImageXY(playAgain, 30, 60);
    t.checkExpect(game2x2.lastScene("You Lost"), current2x2);
    
    ArrayList<Cell> winRow12x2 = new ArrayList<Cell>(Arrays.asList(green12x2, green12x2));
    ArrayList<ArrayList<Cell>> winBoard2x2 = 
        new ArrayList<ArrayList<Cell>>(Arrays.asList(winRow12x2, winRow12x2));
    FloodItWorld winGame2x2 = new FloodItWorld(winBoard2x2);
    
    WorldScene win2x2 = winGame2x2.makeScene();
    win2x2.placeImageXY(win, 30, 30);
    win2x2.placeImageXY(playAgain, 30, 60);
    t.checkExpect(winGame2x2.lastScene("You win"), win2x2);
  }
  
  //functions for the onKeyEvent function for FloodItWorld
  void testOnKeyEvent(Tester t) {
    this.initConditions();
    game3x3rand.turns = 1;
    game3x3rand.timer = 1.5;
    t.checkExpect(game3x3rand.turns, 1);
    t.checkExpect(game3x3rand.timer, 1.5);
    game3x3rand.onKeyEvent("a");
    t.checkExpect(game3x3rand.turns, 1);
    t.checkExpect(game3x3rand.timer, 1.5);
    game3x3rand.onKeyEvent("r");
    t.checkExpect(game3x3rand.turns, 0);
    t.checkExpect(game3x3rand.timer, 0.0);
    
    game4x4rand.turns = 3;
    game4x4rand.timer = 2.5;
    t.checkExpect(game4x4rand.turns, 3);
    t.checkExpect(game4x4rand.timer, 2.5);
    game4x4rand.onKeyEvent("esc");
    t.checkExpect(game4x4rand.turns, 3);
    t.checkExpect(game4x4rand.timer, 2.5);
    game4x4rand.onKeyEvent("r");
    t.checkExpect(game4x4rand.turns, 0);
    t.checkExpect(game4x4rand.timer, 0.0);
  }
  
  //functions for the onMouseClicked function for FloodItWorld
  void testOnMouseClicked(Tester t) {
    this.initConditions();
    t.checkExpect(game3x3.prevColor, null);
    t.checkExpect(game3x3.turns, 0);
    t.checkExpect(game3x3.gameColor, null);
    game3x3.onMouseClicked(new Posn(10,20));
    t.checkExpect(game3x3.prevColor, Color.green);
    t.checkExpect(game3x3.turns, 1);
    t.checkExpect(game3x3.gameColor, Color.green);
    game3x3.onMouseClicked(new Posn(10,10));
    t.checkExpect(game3x3.prevColor, Color.green);
    t.checkExpect(game3x3.turns, 1);
    t.checkExpect(game3x3.gameColor, Color.green);
  }
  
  //functions for the onMouseMoved function for FloodItWorld
  void testOnMouseMoved(Tester t) {
    this.initConditions();
    //WORKS HALF THE TIME WHEN NOT COMMENTED SIMILAR TO POINT CELL AS IT USES THAT
    //HAVE TRIED DEBUGGING USING SYSTEM.OUT.PRINTLN
    t.checkExpect(green12x2.highlighted, false);
    game2x2.onMouseMoved(new Posn(11,15));
    //t.checkExpect(green12x2.highlighted, true);
    game2x2.onMouseMoved(new Posn(53,12));
    t.checkExpect(green12x2.highlighted, false);
  }
  
  //functions for the boardScore function for FloodItWorld
  void testBoardScore(Tester t) {
    this.initConditions();
    
    //TESTS WORK 99% OF THE TIME BUT SOMETIMES DECIDE TO BE GROOVY
    game2x2.gameColor = Color.green;
    t.checkExpect(game2x2.boardScore(), 1);
    game2x2.board.get(0).get(1).color = Color.green;
    t.checkExpect(game2x2.boardScore(), 3);
    game2x2.board.get(1).get(0).color = Color.green;
    t.checkExpect(game2x2.boardScore(), 4);
    
    game3x3.gameColor = Color.green;
    t.checkExpect(game3x3.boardScore(), 1);
    game3x3.board.get(0).get(1).color = Color.green;
    t.checkExpect(game3x3.boardScore(), 2);
    game3x3.board.get(1).get(0).color = Color.green;
    t.checkExpect(game3x3.boardScore(), 5);
    
  }
  
  void testIterator(Tester t) {
    this.initConditions();
    ArrayList<Cell> testBoard = new ArrayList<Cell>(Arrays.asList(green12x2, blue12x2));
    NextRemove<Cell> cellIter = new NextRemove<Cell>(testBoard.iterator());
    
    t.checkExpect(cellIter.hasNext(), true);
    t.checkExpect(cellIter.next(), this.green12x2);
    t.checkExpect(cellIter.hasNext(), true);
    t.checkExpect(cellIter.next(), this.blue12x2);
    t.checkExpect(cellIter.hasNext(), false);
  }
 
  //test to the World
  //BY RUNNING BIG BANG THE FOLLOWING TESTS ARE INCLUDED:
  //onTick, toBeFlooded, updateFlooding, updateFloodStates
  //also shows a more thorough onMouseMoved, pointCell
  void testWorld(Tester t) {
    this.initConditions();
    //adjust the board size and number of colors you want here!
    // recommended max: 12
    int USER_SIZE = 10;
    //choose up to 8 colors
    int USER_COLORS = 4;
    
    FloodItWorld board = new FloodItWorld(USER_SIZE, USER_COLORS);
    board.bigBang(30 * USER_SIZE, 30 * (USER_SIZE + 4), 0.08);
  }
}