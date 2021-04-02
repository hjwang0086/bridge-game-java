import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Login extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private LoginPanel panel = new LoginPanel();
	
	public JButton buttonLogin = new JButton("Log in");
	public JTextField IPField = new JTextField();
	public JTextField PortField = new JTextField("8383");
	public String IPAddr = "";
	public String PortNum = "";
	public boolean loginSuccess = false;
	
	public Login() {
		super("Login");
		
		this.setSize(230, 245);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		IPField.setBounds(120, 40, 80, 20);
		this.add(IPField);
		
		PortField.setBounds(120, 90, 80, 20);
		PortField.setEnabled(false);
		this.add(PortField);
		
		buttonLogin.addActionListener(this);
		buttonLogin.setBounds(65, 160, 100, 25);
		buttonLogin.setFont(new Font("TimesRoman", Font.BOLD, 12));
		buttonLogin.setVisible(true);
		this.add(buttonLogin);
		
		panel.setBounds(0, 0, 230, 255);
		this.add(panel);
		this.repaint();
	}

	class LoginPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.drawRect(10, 25, 205, 100);
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 11));
			g.drawString("IP Address:", 20, 55);
			g.drawString("Port Number:", 20, 105);
			if (loginSuccess)
				g.drawString("Waiting for others login...", 35, 210);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		IPAddr = IPField.getText();
		PortNum = PortField.getText();
	}

}
