import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
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

		mainFrame=new JFrame("五子棋");
		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add(board,BorderLayout.CENTER);
		mainFrame.pack();
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);

		board.init();
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
	private BufferedImage canva;
	private Graphics2D g;
	private static final Color bgColor=new Color(187,102,0);

	private boolean winned;
	private boolean inturn;

	public Board(int width,int height)
	{
		inturn=true;
		winned=false;

		this.width=width;
		this.height=height;
		this.setPreferredSize(new Dimension((width-1)*SIDE_LEN+2*BORDER_WIDTH,(height-1)*SIDE_LEN+2*BORDER_WIDTH));

		map=new MapData(width,height);
		canva=new BufferedImage((width-1)*SIDE_LEN+2*BORDER_WIDTH,(height-1)*SIDE_LEN+2*BORDER_WIDTH,BufferedImage.TYPE_INT_ARGB);
		g=canva.createGraphics();
		init();

		addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						if(winned)
						{
							inturn=true;
							winned=false;
							map.init();
						}
						if(inturn)
						{
							if(e.getButton()==e.BUTTON1)
							{
								int result=map.step(getCoord(new Point(e.getY(),e.getX())));
								if(result!=0)
								{
									System.out.println("win");
									winned=true;
									drawWinner(map.getWinnerList());
									return;
								}
							}
							else
							{
								map.regret();
							}
							refresh(map.getHistory());
						}
						else
						{
							//AI in turn
						}
					}
				});
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		g.drawImage(canva,0,0,this);
	}

	/**
	 * 画空白棋盘
	 */
	public void init()
	{
		g.setColor(bgColor);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.black);
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
	 * @param color 以整数指定的颜色(1为黑色，-1为白色，2为红色)
	 */
	public void draw(Point coord,int color)
	{
		if(color==0) return;
		else if(color==1) g.setColor(Color.black);
		else if(color==-1) g.setColor(Color.white);
		else if(color==2) g.setColor(Color.red);
		Point position=getPosition(coord);
		g.fillOval(position.c-R,position.r-R,2*R,2*R);
		updateUI();
	}

	/**
	 * 以给定历史棋步画所有棋子，即刷新整个棋盘
	 *
	 * @param history 历史棋步
	 */
	public void refresh(List<Point> history)
	{
		init();
		int color=(history.size()%2)==1?1:-1;
		for(Point p:history)
		{
			draw(p,color);
			color*=-1;
		}
	}

	/**
	 * 将给定的五子连珠变色。
	 *
	 * @param winner 相连五子的坐标列表
	 */
	public void drawWinner(List<Point> winner)
	{
		for(Point p:winner)
			draw(p,2);
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
	 * @param position 画图坐标
	 * @return 棋子坐标
	 */
	private static Point getCoord(Point position)
	{
		return new Point((position.r-BORDER_WIDTH+SIDE_LEN/2)/SIDE_LEN,(position.c-BORDER_WIDTH+SIDE_LEN/2)/SIDE_LEN);
	}
}

class MapData
{
	private static final int WIN_N=5;

	private int height;
	private int width;
	private int[][] map;
	private LinkedList<Point> history;
	private LinkedList<Point> winnerList;

	public MapData(int width,int height)
	{
		this.height=height;
		this.width=width;
		init();
	}

	/**
	 * 初始化地图数据
	 */
	public void init()
	{
		map=new int[height][width];
		history=new LinkedList<Point>();
		winnerList=new LinkedList<Point>();
	}

	/**
	 * 获得给定坐标处的棋子颜色。若无棋子返回0。
	 *
	 * @param coord 坐标
	 * @return 棋子颜色，黑色为1，白色为-1，无棋子为0
	 */
	public int getPoint(Point coord)
	{
		if(coord.r<0||coord.r>=height||coord.c<0||coord.r>=width)
			return 0;
		return map[coord.r][coord.c];
	}

	/**
	 * 获取历史棋步列表
	 *
	 * @return 历史棋步列表
	 */
	public List<Point> getHistory()
	{
		return history;
	}

	/**
	 * 在给定坐标处走一步棋
	 *
	 * @param coord 下棋坐标
	 * @return 获胜方。黑棋获胜为1，白棋为-1，无获胜方为0。
	 */
	public int step(Point coord)
	{
		if(coord.r<0||coord.r>=height||coord.c<0||coord.r>=width)
			return 0;
		if(getPoint(coord)!=0) return 0;
		history.push(coord);
		map[coord.r][coord.c]=(history.size()%2)==1?1:-1;
		return check();
	}

	/**
	 * 进行一步悔棋
	 */
	public void regret()
	{
		if(history.size()==0) return;
		Point coord=history.pop();
		map[coord.r][coord.c]=0;
	}

	/**
	 * 获取五子连珠列表
	 *
	 * @return 五子连珠列表
	 */
	public List<Point> getWinnerList()
	{
		return winnerList;
	}

	/**
	 * 判定胜方
	 *
	 * @return 获胜方。黑棋获胜为1，白棋为-1，无获胜方为0
	 */
	private int check()
	{
		for(int i=0;i<=height-WIN_N;i++)
		{
			int result;
			for(int j=0;j<=width-WIN_N;j++)
			{
				result=checkv(i,j);
				if(result!=0) return result;
				result=checkh(i,j);
				if(result!=0) return result;
				result=checkd1(i,j);
				if(result!=0) return result;
			}
			for(int j=WIN_N-1;j<width;j++)
			{
				result=checkd2(i,j);
				if(result!=0) return result;
			}
		}
		return 0;
	}

	private int checkv(int r,int c)
	{
		int result=0;
		for(int i=0;i<WIN_N;i++)
			result+=map[r+i][c];
		result/=WIN_N;
		if(result!=0)
			for(int i=0;i<WIN_N;i++)
				winnerList.add(new Point(r+i,c));
		return result;
	}

	private int checkh(int r,int c)
	{
		int result=0;
		for(int i=0;i<WIN_N;i++)
			result+=map[r][c+i];
		result/=WIN_N;
		if(result!=0)
			for(int i=0;i<WIN_N;i++)
				winnerList.add(new Point(r,c+i));
		return result;
	}

	private int checkd1(int r,int c)
	{
		int result=0;
		for(int i=0;i<WIN_N;i++)
			result+=map[r+i][c+i];
		result/=WIN_N;
		if(result!=0)
			for(int i=0;i<WIN_N;i++)
				winnerList.add(new Point(r+i,c+i));
		return result;
	}

	private int checkd2(int r,int c)
	{
		int result=0;
		for(int i=0;i<WIN_N;i++)
			result+=map[r+i][c-i];
		result/=WIN_N;
		if(result!=0)
			for(int i=0;i<WIN_N;i++)
				winnerList.add(new Point(r+i,c-i));
		return result;
	}
}
