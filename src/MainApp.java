import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class MainApp extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;
	
	private ButtonGroup group = new ButtonGroup();
	private IPPanel ipPanel = new IPPanel();
	
	public JRadioButton serverRadioButton =  new JRadioButton("server");
	public JRadioButton clientRadioButton = new JRadioButton("client");
	public JButton confirmButton = new JButton("Confirm");
	public boolean isConfirm = false;
	public boolean isClient = false;
	public boolean isServer = false;
	
	
	public static void main(String args[]) throws Exception {
		MainApp frame = new MainApp();

		while (frame.isConfirm == false)
			Thread.sleep(1000);
		
		if (frame.isServer == true) {
			frame.serverRadioButton.setEnabled(false);
			frame.clientRadioButton.setEnabled(false);
			frame.confirmButton.setEnabled(false);
			frame.drawIPAddr();
			try {
				Server server = new Server();
				server.gameStart();
			}  catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Server Close (Exception Occurs)", 
						"Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}  else if (frame.isClient == true) {
			frame.setVisible(false);
			Client clientPlayer = new Client();
			clientPlayer.gameStart();
			clientPlayer.close();
		}
			
	}
	
	public MainApp() {
		super("Run As");
		
		this.setSize(270, 170);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		clientRadioButton.addItemListener(this);
		clientRadioButton.setSelected(true);
		clientRadioButton.setBounds(20, 20, 80, 30);
		group.add(clientRadioButton);
		this.add(clientRadioButton);
		
		serverRadioButton.addItemListener(this);
		serverRadioButton.setBounds(20, 60, 80, 30);
		group.add(serverRadioButton);
		this.add(serverRadioButton);
		
		confirmButton.addActionListener(this);
		confirmButton.setBounds(20, 110, 90, 25);
		this.add(confirmButton);
		
	}
	
	public void drawIPAddr() {
		ipPanel.setBounds(0, 0, 270, 170);
		
		this.add(ipPanel);
		this.repaint();
	}
	
	class IPPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.drawRect(100, 30, 150, 55);
			try {
				g.drawString("IP: " + getIPAddr(), 110, 50);
				g.drawString("Port: 8383", 110, 75);
			}  catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		public String getIPAddr() throws Exception {
			String temp = "(See Readme)";
			
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();)
			{
				NetworkInterface e = n.nextElement();
				
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();)
				{
					InetAddress addr = a.nextElement();
					if (isIPFormat(addr.getHostAddress()) && 
							!addr.getHostAddress().substring(0, 4).equals("127."))
						temp = addr.getHostAddress();
				}
			}

			return temp;
		}
		
		public boolean isIPFormat(String str) {
			int count = 0;
			for (int i = 0; i < str.length(); i++) {
				if ((str.charAt(i) < '0' || str.charAt(i) > '9') && str.charAt(i) != '.')
					return false;
				else if(str.charAt(i) == '.')
					count++;
			}
			
			if (count != 3)
				return false;
			else return true;
		}
	}

	
	@Override
	public void actionPerformed(ActionEvent event) {
		isConfirm = true;
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() == serverRadioButton) {
			isServer = true;
			isClient = false;
		}  else if (event.getSource() == clientRadioButton) {
			isClient = true;
			isServer = false;
		}
	}

}
