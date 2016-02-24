/*
*   NAMES:      TONY WONG;  MARIAM ZIKARIA
*   NETIDS:     wong14;     mzikar2
*   COURSE:     CS 342 - TROY
*   PROJECT 2:  MINESWEEPER
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.ImageIcon.*;
import java.util.*;

/*
*   Monolith class that contains every element of the game.
*   It is the JFrame for which all the other containers get thrown in.
*/
public class Minesweeper extends JFrame implements	MenuListener,
													ActionListener,
													KeyListener,
													MouseListener {
    // variable declarations for the jframe
    static private JFrame game;
    private final int nHeight = 590;
	private final int nWidth = 500;

    // variable declarations for the game state
    static private int nTimer = 0;
    static private int nMines = 10;
    static private javax.swing.Timer timer;
	//static private boolean firstClick = true;

    // variable declarations for the menubar
    static private JMenuBar menuBar;
    static private JMenu    jmGame,         // the first menu that controls game state actions
                            jmHelp;         // the second menu that displays helpful information
    static private JMenuItem    jmiReset, jmiScores, jmiErase, jmiExit, // part of the Game menu
                                jmiHelp, jmiAbout;                      // part of the Help menu

    // variable declarations for the mines-left and timer jpanel
    static private JPanel info;
    static private JLabel labMine;
	static private JLabel labTime; 

    // variable declarations for the minefield
    static private Field field;
    private final int ICONS = 15;		//	the amount of custom icons
	private final int ICON_SIZE = 50;	//	the size of each icon in pixels
    private final int UNCLEARED = 10;
	private final int CLEARED = 0;
	private final int FLAG = 10;
	private final int GUESS = 10;
	private final int MINE = 9;
	private final int UNCLEARED_MINE = MINE + UNCLEARED;
	private final int FLAG_MINE = UNCLEARED_MINE + FLAG;
	private final int GUESS_MINE = FLAG_MINE + GUESS;
    private final int D_MINE = 9;
	private final int D_UNCLEARED = 10;
	private final int D_FLAG = 11;
	private final int D_BAD_FLAG = 12;
	private final int D_GUESS = 13; 
	private final int D_WIN_MINE = 14;
	private Image[] icons;
    private final int MINES = 10;
	private final int ROWS = 10;
	private final int COLS = 10;
    private int[] grid;			    // an array of ints representing the mine field
	private int cells;			    // the total number of cells in the grid
	private boolean sweeping;	    // is the user sweeping for mines
	private boolean firstClick;     // so the user doesn't click on a mine on first turn
	private int winningState = ROWS * COLS - MINES;	// how many cells need to be cleared to win
	private int cellsCleared = 0;                   // how many cells that are currently cleared

    // creates the menubar
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // creates the first menu
        jmGame = new JMenu("Game");
		jmGame.setMnemonic(KeyEvent.VK_G);
		jmGame.addMenuListener(this);
		jmGame.addActionListener(this);
		menuBar.add(jmGame);
        // items for the first menu
		jmiReset = new JMenuItem("Reset");
		jmiReset.setMnemonic(KeyEvent.VK_R);
		jmiReset.addActionListener(this);
		jmGame.add(jmiReset);

		jmiScores = new JMenuItem("Top Ten");
		jmiScores.setMnemonic(KeyEvent.VK_T);
		jmiScores.addActionListener(this);
		jmGame.add(jmiScores);

		jmiErase = new JMenuItem("Reset Scores");
		jmiErase.setMnemonic(KeyEvent.VK_S);
		jmiErase.addActionListener(this);
		jmGame.add(jmiErase);

		jmiExit = new JMenuItem("Exit");
		jmiExit.setMnemonic(KeyEvent.VK_X);
		jmiExit.addActionListener(this);
		jmGame.add(jmiExit);

        // creates the second menu
		jmHelp = new JMenu("Help");
		jmHelp.setMnemonic(KeyEvent.VK_L);
		jmHelp.addMenuListener(this);
		jmHelp.addActionListener(this);
		menuBar.add(jmHelp);
		// items for the second menu
		jmiHelp = new JMenuItem("Help");
		jmiHelp.setMnemonic(KeyEvent.VK_H);
		jmiHelp.addActionListener(this);
		jmHelp.add(jmiHelp);

		jmiAbout = new JMenuItem("About");
		jmiAbout.setMnemonic(KeyEvent.VK_A);
		jmiAbout.addActionListener(this);
		jmHelp.add(jmiAbout);
    }

    // creates the mines-left and timer panel
    private void createInfo() {
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 80, 10);
        info = new JPanel(fl);
		info.setBackground(Color.darkGray);

        // creates mines-left jlabel
		ImageIcon iconMine = new ImageIcon("14.png");
        labMine = new JLabel(   " " + nMines,
								iconMine,
								JLabel.CENTER);
        labMine.setVerticalTextPosition(JLabel.CENTER);
		labMine.setHorizontalTextPosition(JLabel.RIGHT);
		labMine.setForeground(Color.WHITE);
		labMine.setFont(new Font("Comic Sans", Font.PLAIN, 30));
        info.add(labMine);

        // creates time elasped jlabel
		ImageIcon iconTime = new ImageIcon("15.png");
		labTime = new JLabel(   " 000",
								iconTime,
								JLabel.CENTER);
		labTime.setVerticalTextPosition(JLabel.CENTER);
		labTime.setHorizontalTextPosition(JLabel.RIGHT);
		labTime.setForeground(Color.WHITE);
		labTime.setFont(new Font("Comic Sans", Font.PLAIN, 30));
		info.add(labTime);
    }

    // creates the minefield
    private void createField() {
        field = new Field();
        icons = new Image[ICONS];
		for(int i = 0; i < ICONS; i++) {
			icons[i] = (new ImageIcon(i + ".png")).getImage();
		}
		new_game();
        field.addMouseListener(new myAdapter());
        field.addKeyListener(this);
    }
    
    class myAdapter extends MouseAdapter {
	    @Override
	    public void mousePressed(MouseEvent me) {
		    //	retrieves mouse coordinates
		    int x = me.getX();
		    int y = me.getY();

		    //	what row and column?
		    int col = x / ICON_SIZE;
		    int row = y / ICON_SIZE;
		
		    //	does the game state need to be visually updated
		    boolean redraw = false;

		    //	initialize a new game if user is not currently playing
		    if(!sweeping) {
			    new_game();
			    repaint();
                
		    }
		
		    if( (x < COLS * ICON_SIZE) &&		//	if the user clicked within the grid
			    (y < ROWS * ICON_SIZE) ) {
			    int pos = row * COLS + col;		//	index grid position
			    if(me.getButton() == MouseEvent.BUTTON3) {		//	right click?
				    if(grid[pos] > MINE) {						//	right click on valid cell
					    redraw = true;							//	since it's valid, redraw
					
					    //	places a flag if there are mines left when user
					    //	right clicks on uncleared cell
					    if(grid[pos] <= UNCLEARED_MINE) {
						    if(nMines > 0) {
							    grid[pos] += FLAG;
							    --nMines;
                                if(nMines < 10)
                                    labMine.setText(" 0" + nMines);
                                else
                                    labMine.setText(" " + nMines);
						    }
					    }
					    //	places a question mark if there was a flag previously in cell
					    else if(grid[pos] <= FLAG_MINE) {
						    grid[pos] += GUESS;
						    ++nMines;			//	allows for another flag to be placed down
                            if(nMines < 10)
                                labMine.setText(" 0" + nMines);
                            else
                                labMine.setText(" " + nMines);
					    }
					    else {
						    grid[pos] -= (FLAG + GUESS);	//	return cell to uncleared state
					    }
				    }
			    }
			    else {		//	left click
				    if(grid[pos] > UNCLEARED_MINE) {		//	do nothing if cell is marked
					    return;
				    }
				    //	keep resesting the back-end grid until the first click isn't a mine
				    while(	firstClick &&
						    grid[pos] == UNCLEARED_MINE ) {
					    new_game();
                        labTime.setText(" 000");
                        labMine.setText(" " + MINES);
				    }
				    firstClick = false;						//	time to sweep!
                    timer.start();
				    if(	(grid[pos] > MINE) &&				//	either a mine or a number
					    (grid[pos] < FLAG_MINE)	) {
					    grid[pos] -= UNCLEARED;				//	clearing a cell
					    ++cellsCleared;
					    if(grid[pos] == MINE) {
						    sweeping = false;				//	your sweeping days are over
                            nTimer = 0;
                            timer.stop();
					    }
					    if(grid[pos] == CLEARED) {
						    clear(pos);						//	clear adjacent cells
					    }
					    redraw = true;						//	move has been made, GUI needs refresh
				    }
			    }
			    if(redraw) {		//	if there was a valid action, refresh the GUI
				    repaint();
			    }
		    }
	    }
    }

        //	recursive mine clearing algorithm
    public void clear(int pos) {
	    int col = pos % COLS;
	    int cell;

	    if(col > 0) {							
		    cell = pos - COLS - 1;				//	clearing NW cell
		    if(cell >= 0) {
			    if(check_clear(cell)) {
				    clear(cell);
			    }	
		    }
		    cell = pos - 1;						//	clearing W cell
		    if(cell >= 0) {
			    if(check_clear(cell)) {
				    clear(cell);
			    }
		    }
		    cell = pos + COLS - 1;				//	clearing SW cell
		    if(cell < cells) {				
			    if(check_clear(cell)) {
				    clear(cell);
			    }
		    }
	    }
	    cell = pos - COLS;						//	clearing N cell
	    if(cell >= 0) {
		    if(check_clear(cell)) {
			    clear(cell);
		    }
	    }
	    cell = pos + COLS;						//	clearing S cell
	    if(cell < cells) {
		    if(check_clear(cell)) {
			    clear(cell);
		    }
	    }
	    if(col < (COLS - 1)) {					
		    cell = pos - COLS + 1;				//	clearing NE cell
		    if(cell >= 0) {
			    if(check_clear(cell)) {
				    clear(cell);
			    }
		    }
		    cell = pos + COLS + 1;				//	clearing SE cell
		    if(cell < cells) {
			    if(check_clear(cell)) {
				    clear(cell);
			    }
		    }
		    cell = pos + 1;						//	clearing E cell
		    if(cell < cells) {
			    if(check_clear(cell)) {
				    clear(cell);
			    }
		    }
	    }
    }

    //	helper function for clearing mines recursively
    public boolean check_clear(int pos) {
	    if(	(grid[pos] > MINE) && (grid[pos] < UNCLEARED_MINE) ) {
		    grid[pos] -= UNCLEARED;
		    ++cellsCleared;
		    if(grid[pos] == CLEARED) {
			    return true;
		    }
	    }
	    return false;
    }	


    public void new_game() {
	    Random rng = new Random();
	    int col;
	    int count = 0;
	    int pos = 0;
	    int cell = 0;
	    sweeping = true;
	    firstClick = true;
	    cellsCleared = 0;
	    nMines = MINES;
	    cells = ROWS * COLS;
	    grid = new int[cells];
	    for(int i = 0; i < cells; i++) {
		    grid[i] = UNCLEARED;
	    }
	    //	seed the field with mines
	    while(count < MINES) {
		    pos = rng.nextInt(cells);
		    if(grid[pos] != UNCLEARED_MINE) {
			    grid[pos] = UNCLEARED_MINE;
			    ++count;
			    col = pos % COLS;

			    //	updates the mine count of adjacent cells
			    if(col > 0) {
				    cell = pos - 1 - COLS;					//	NW cell mine count increases
				    if(cell >= 0) {
					    if(grid[cell] != UNCLEARED_MINE) {
						    ++(grid[cell]);
					    }
				    }
				    cell = pos - 1;							//	W cell mine count increases
				    if(cell >= 0) {
					    if(grid[cell] != UNCLEARED_MINE) {
						    ++(grid[cell]);
					    }
				    }
				    cell = pos + COLS - 1;					//	SW cell mine count increases
				    if(cell < cells) {
					    if(grid[cell] != UNCLEARED_MINE) {
						    ++(grid[cell]);
					    }
				    }
			    }
			    cell = pos - COLS;							//	N cell mine count increases
			    if(cell >= 0) {
				    if(grid[cell] != UNCLEARED_MINE) {
					    ++(grid[cell]);
				    }
			    }
			    cell = pos + COLS;							//	S cell mine count increases
			    if(cell < cells) {
				    if(grid[cell] != UNCLEARED_MINE) {
					    ++(grid[cell]);
				    }
			    }
			    if(col < (COLS - 1)) {
				    cell = pos - COLS + 1;					//	NE cell mine count increases
				    if(cell >= 0) {
					    if(grid[cell] != UNCLEARED_MINE) {
						    ++(grid[cell]);
					    }
				    }
				    cell = pos + COLS + 1;					//	SE cell mine count increases
				    if(cell < cells) {
					    if(grid[cell] != UNCLEARED_MINE) {
						    ++(grid[cell]);
					    }
				    }
				    cell = pos + 1;							//	E cell mine count increases
				    if(cell < cells) {
					    if(grid[cell] != UNCLEARED_MINE) {
						    ++(grid[cell]);
					    }
				    }
			    }
		    }
	    }
    }

    class Field extends JPanel {
	    
	    @Override
	    public void paintComponent(Graphics gfx) {
		    int cell;

		    for(int i = 0; i < ROWS; i++) {
			    for(int j = 0; j < COLS; j++) {
				    cell = grid[i * COLS + j];
				    if(sweeping && cell == MINE) {
					    sweeping = false;
				    }
				    if(sweeping && cellsCleared != winningState) {	//	the game is in play
					    if(cell > FLAG_MINE) {
						    cell = D_GUESS;
					    }
					    else if(cell > UNCLEARED_MINE) {
						    cell = D_FLAG;
					    }
					    else if(cell > MINE) {
						    cell = D_UNCLEARED;
					    }
				    }
				    else if(cellsCleared == winningState) {	//	this is a winning game
					    if(	(cell == UNCLEARED_MINE) ||
						    (cell == FLAG_MINE) ||
						    (cell == GUESS_MINE) ) {
						     cell = D_WIN_MINE;
					    }
				    }
				    else if(cellsCleared != winningState) {	//	this is a losing game
					    if(cell == UNCLEARED_MINE) {
						    cell = D_MINE;
					    }
					    else if(cell == GUESS_MINE) {
						    cell = D_WIN_MINE;
					    }
					    else if(cell == FLAG_MINE) {
						    cell = D_WIN_MINE;
					    }
					    else if(cell > UNCLEARED_MINE) {
						    cell = D_BAD_FLAG;
					    }
					    else if(cell > MINE) {
						    cell = D_UNCLEARED;
					    }
				    }
				    gfx.drawImage(icons[cell], j * ICON_SIZE, i * ICON_SIZE, this);
			    }
		    }
		    //System.out.println(cellsCleared);		//	used for debugging purposes
		    if(cellsCleared == winningState && sweeping) {
			    sweeping = false;
                timer.stop(); 
                System.out.println(nTimer);
		    }
	    }
    }

	    

    /*
    *   Constructor for the entire game.
    *   Meshes together the menubar, minefield, timer, mines left, and scores.
    */
    public Minesweeper() {
        //	closing the game via "X" button will end the program
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//	creating the game window and not allowing resizing due to custom icons
		setSize(nWidth, nHeight);
		setResizable(false);
		setLocationRelativeTo(null);
		setTitle("CS 342 - MINESWEEPER");

        addKeyListener(this);
        addMouseListener(this);

        createMenuBar();
        setJMenuBar(menuBar);
        
        createField();
        field.setDoubleBuffered(true);
        getContentPane().add(BorderLayout.CENTER, field);

        createInfo();
        getContentPane().add(BorderLayout.SOUTH, info);

        timer = new javax.swing.Timer(1000, this);
    }

    /*
    *   Main method that initializes and runs the game.
    */
    public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				game = new Minesweeper();
				game.setVisible(true);
			}
		});
	}

    @Override
	public void mouseExited(MouseEvent me) {

	}
	
	@Override
	public void mouseEntered(MouseEvent me) {

	}

	@Override
	public void mouseReleased(MouseEvent me) {

	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
        if(me.getButton() == MouseEvent.BUTTON1) {
			if(firstClick) {
				firstClick = false;
				timer.start();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent ke) {
		//not used
	}
	
	@Override
	public void keyPressed(KeyEvent ke) {
		if(ke.getKeyChar() == 'x') {
			System.exit(0);
		}
		if(ke.getKeyChar() == 'a') {
			JOptionPane.showMessageDialog(game,
				"Members - Tony Wong; Mariam Zikaria\n" +
				"NetIDs  - wong14; mzikar2",
				"CS 342 - Project 2 - MINESWEEPER",
				JOptionPane.PLAIN_MESSAGE);
		}
		if(ke.getKeyChar() == 'h') {
			JOptionPane.showMessageDialog(game,
				"Start by clicking anywhere in the grid.\n" +
				"The number represents how many mines are nearby.\n" +
				"Use logic and a little bit of luck to sweep the minefield.\n" +
				"You win if you can clear all the tiles, but be careful!\n" +
				"If you click on a mine, then you...well...we'll leave you to find out.\n",
				"Helpful Information for the Mine-Defusing Trainees",
				JOptionPane.WARNING_MESSAGE);
		}
        if(ke.getKeyChar() == 'r') {
            new_game();
            field.repaint();
            firstClick = true;
            nTimer = 0;
            labTime.setText(" 000");
            labMine.setText(" " + MINES);
            timer.stop();
        }
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		//not used
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource().equals(jmiExit)) {
			System.exit(0);
		}
		if(ae.getSource().equals(jmiHelp)) {
			JOptionPane.showMessageDialog(game,
				"Start by clicking anywhere in the grid.\n" +
				"The number represents how many mines are nearby.\n" +
				"Use logic and a little bit of luck to sweep the minefield.\n" +
				"You win if you can clear all the tiles, but be careful!\n" +
				"If you click on a mine, then you...well...we'll leave you to find out.\n",
				"Helpful Information for the Mine-Defusing Trainees",
				JOptionPane.WARNING_MESSAGE);
		}
		if(ae.getSource().equals(jmiAbout)) {
			JOptionPane.showMessageDialog(game,
				"Members - Tony Wong; Mariam Zikaria\n" +
				"NetIDs  - wong14; mzikar2",
				"CS 342 - Project 2 - MINESWEEPER",
				JOptionPane.PLAIN_MESSAGE);
		}
        if(ae.getSource().equals(jmiReset)) {  
            new_game();
            field.repaint();
            firstClick = true;
            nTimer = 0;
            labTime.setText(" 000");
            labMine.setText(" " + MINES);
            timer.stop();
        }
        if(ae.getSource() == timer) {
            ++nTimer;
			if(nTimer < 10)
				labTime.setText(" 00" + nTimer);
			else if(nTimer < 100)
				labTime.setText(" 0" + nTimer);
			else
				labTime.setText(" " + nTimer);
		}
	}

	@Override
	public void menuSelected(MenuEvent me) {
		
	}
	
	@Override
	public void menuDeselected(MenuEvent me) {
		//not used
	}
	
	@Override
	public void menuCanceled(MenuEvent me) {
		//not used
	}
}
