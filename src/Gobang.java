import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.LinkedList;
import java.util.List;

public class Gobang
{
	public static void main(String[] args)
	{
		Gobang game=new Gobang();
	}

	public static final int WIDTH=19;		//横向最多棋子数
	public static final int HEIGHT=19;		//纵向最多棋子数

	private JFrame mainFrame;

	private Board board;

	public Gobang()
	{
		board=new Board(WIDTH,HEIGHT);
		board.init();

		mainFrame=new JFrame("五子棋");
		mainFrame.setSize(WIDTH,HEIGHT);
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add(board,BorderLayout.CENTER);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
}

class Point
{
	public int r,c;
	public Point(int r,int c)
	{
		this.r=r;
		this.c=c;
	}
}

class Board extends JPanel
{
	private static final int SIDE_LEN=20;		//方格边长
	private static final int R=8;				//棋子半径
	private static final int BORDER_WIDTH=20;	//边缘空白
	private int width;
	private int height;
	private MapData map;

	private boolean inturn;

	public Board(int width,int height)
	{
		inturn=true;
		this.width=width;
		this.height=height;
		map=new MapData(width,height);

		addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						if(inturn)
						{
							if(e.getButton()==e.BUTTON1)
								map.step(getCoord(new Point(e.getY(),e.getX())));
							else
								map.regret();
							refresh(map.getHistory());
						}
						else
						{
							//AI in turn
						}
					}
				});
	}

	/**
	 * 画空白棋盘
	 */
	public void init()
	{
		Graphics g=getGraphics();
		g.clearRect(0,0,getWidth(),getHeight());
		g.setColor(Color.white);
		for(int i=0;i<Gobang.WIDTH;i++)
		{
			Point p1=getPosition(new Point(0,i));
			Point p2=getPosition(new Point(Gobang.HEIGHT-1,i));
			g.drawLine(p1.c,p1.r,p2.c,p2.r);
		}
		for(int i=0;i<Gobang.HEIGHT;i++)
		{
			Point p1=getPosition(new Point(i,0));
			Point p2=getPosition(new Point(i,Gobang.WIDTH-1));
			g.drawLine(p1.c,p1.r,p2.c,p2.r);
		}
	}

	/**
	 * 在给定坐标画指定颜色的棋子
	 *
	 * @param coord 坐标
	 * @param color 以整数指定的颜色(1为黑色，-1为白色)
	 */
	public void draw(Point coord,int color)
	{
		if(color==0) return;
		Graphics g=getGraphics();
		g.setColor(color==1?Color.black:Color.white);
		Point position=getPosition(coord);
		g.fillOval(position.c,position.r,2*R,2*R);
	}

	/**
	 * 以给定历史棋步画所有棋子，即刷新整个棋盘
	 *
	 * @param history 历史棋步
	 */
	public void refresh(List<Point> history)
	{
		init();
		int color=1;
		for(Point p:history)
		{
			draw(p,color);
			color*=-1;
		}
	}

	/**
	 * 根据棋子坐标得到画图坐标
	 *
	 * @param coord 棋子坐标
	 * @return 画图坐标
	 */
	private static Point getPosition(Point coord)
	{
		return new Point(BORDER_WIDTH+coord.r*SIDE_LEN,BORDER_WIDTH+coord.c*SIDE_LEN);
	}

	/**
	 * 根据画图坐标得到棋子坐标
	 *
	 * @paran positoin 画图坐标
	 * @return 棋子坐标
	 */
	private static Point getCoord(Point position)
	{
		return new Point((position.r-BORDER_WIDTH+SIDE_LEN/2)/SIDE_LEN,(position.c-BORDER_WIDTH+SIDE_LEN/2)/SIDE_LEN);
	}
}

class MapData
{
	private int height;
	private int width;
	private int[][] map;
	private LinkedList<Point> history;

	public MapData(int width,int height)
	{
		this.height=height;
		this.width=width;
		init(width,height);
	}

	public void init(int width,int height)
	{
		map=new int[height][width];
		history=new LinkedList<Point>();
	}

	public int getPoint(Point coord)
	{
		if(coord.r<0||coord.r>=height||coord.c<0||coord.r>=width)
			return 0;
		return map[coord.r][coord.c];
	}

	public List<Point> getHistory()
	{
		return history;
	}

	public void step(Point coord)
	{
		if(coord.r<0||coord.r>=height||coord.c<0||coord.r>=width)
			return;
		if(getPoint(coord)!=0) return;
		history.push(coord);
		map[coord.r][coord.c]=history.size()%2;
	}

	public void regret()
	{
		if(history.size()==0) return;
		Point coord=history.pop();
		map[coord.r][coord.c]=0;
	}
}
