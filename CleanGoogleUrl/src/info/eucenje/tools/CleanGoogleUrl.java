/**
 * CleanGoogleUrl v1.0
 * Application that cleans URLs from Google search results
 * 
 * @author 		Krunoslav Zubrinic 2011.
 * @version		1.0
 * @since		2011-04-30   
 * 
 * This application is licensed under a 
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 
 * Unported License
 * 
 * Application uses icons from Human-O2 Iconset 
 * by Oliver Scholtz (and others)
 * 		http://schollidesign.deviantart.com/art/Human-O2-Iconset-105344123
 * 
 */

package info.eucenje.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.DefaultEditorKit;

public class CleanGoogleUrl {
	private JTextArea urlSource, urlClean;
	private JLabel labelSource, labelResult, labelInfo;
	private JMenuItem menuCut, menuCopy, menuPaste;
	private JPopupMenu popup;
	private JButton bRun, bInfo, bExit;
	private JScrollPane sourceScrollPane,cleanScrollPane;
	private JToolBar bar;
	private JPanel panel;
	private JFrame g;
	private static String appIcon = "/info/eucenje/tools/images/ico_small.png";
	private static String runIcon = "/info/eucenje/tools/images/run.png";
	private static String exitIcon = "/info/eucenje/tools/images/exit.png";
	private static String infoIcon = "/info/eucenje/tools/images/info.png";
	private static String iniInfo = "<html>Paste URL from Google's result in the <i>Source URL</i> field</html>";
	private BufferedImage image, imageRun, imageInfo, imageExit;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Set look-and-feel to Nimbus or system
				try {
					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
				} catch (Exception e) {
					try {
						UIManager.setLookAndFeel(
								UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e1) {
						e1.printStackTrace();
					} 
				}

				CleanGoogleUrl r = new CleanGoogleUrl();
				r.start(); 
			}
		});
	}

	// start method 
	private void start(){
		g = new JFrame("Cleaner of Google's search URL results");
		
		readAppImage();

		setComponents();

		// define cut-copy-paste popup menu
		definePopupMenu();

		setLayout(0);

		g.pack();
		g.setLocationRelativeTo(null);
		g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		g.setVisible(true);

		// events: automatic select of source text
		urlSource.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent fEvt) {
				selectSource();
			}
		});

		// events: popup menu or automatic select of source text
		urlSource.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				// if right click then activate popup
				if ( evt.isPopupTrigger() ) {  
					openPopup(evt); 
				}
				else{
					selectSource();
				}
			}
			public void mousePressed(MouseEvent evt) {
				if ( evt.isPopupTrigger() ) {  
					openPopup(evt); 
				}
				else{
					selectSource();
				}
			}
			public void mouseReleased(MouseEvent evt) {
				if ( evt.isPopupTrigger() ) {  
					openPopup(evt); 
				}
			}
			// right click = activate popup
			private void openPopup(MouseEvent evt){
				popup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		});

		// events: click on button = "real" action of cleaning		
		bRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanUrl();
			}
		});
		
		// events: Ctrl+R on field		
		urlSource.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R)
					cleanUrl();
			}
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R)
					cleanUrl();
			}
			@Override
			public void keyReleased(KeyEvent arg0) {				
			}
		});

		// automatic focus on Source URL field
		g.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				urlSource.requestFocusInWindow();
			}
		});

		// application info
		bInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String title = "Cleaner of Google's search URL results - Info and user manual";
				String message = "<html>This program cleans URLs that <i>Google</i> " +
						"search returns.<br>It takes value of url element, " +
						"shows it in field and put it in a clipboard.<br><hr>" +
						"E.g. source URL:<br><a href=\"http://www.google.com/\">" +
						"https://www.google.com/url?sa=t&rct=j&q=&esrc=s&"+
						"source=web&cd=<br>1&cad=rja&ved=0CCsQFjAA&url=http%3A%2F%2F"+
						"www.google.com%<br>2F&ei=2S6CUZG9EbOL4gS8zoHQBw&usg="+
						"AFQjCNGNMl3o1VFNDzL<br>N45woD0kYThZadg&sig2=9SD6X6Zn3lEmTbY2" +
						"ZcxkYw&bvm=bv.4596<br>0087,bs.1,d.Yms</a><br><br>" +
						"App will convert to clean URL: <br>"+
						"<a href=\"http://www.google.com/\">http://www.google.com/</a>" +
						"<br><hr><h3>Instructions for use</h3><ul><li>Paste URL from "+
						"Google's result in the <i>Source URL</i> field.</li>"+
						"<li>Click on the <i>Run</i> button (gear icon).</li>" +
						"<li>Cleaned URL will be shown in the <i>Clean URL</i> " +
						"field and copied in the<br> clipboard.</li></ul><br>" +
						"<p>This application is licensed under a Creative Commons "+ 
						"Attribution-NonCommercial-<br>ShareAlike Unported 3.0 licence</p><br>"+
						"<p>Author: Krunoslav Zubrinic, 2011, <i>ver. "+
						"1.0</i> <p><html>";
				JOptionPane.showMessageDialog (null, message, title, JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// exit
		bExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeProgram();
			}
		});

	}

	// set all components
	private void setComponents(){
		setRunButton();
		setInfoButton();
		setExitButton();
		setButtonBar();
		setElements();
	}

	// read icon of main frame
	private void readAppImage(){
		try {
			image = ImageIO.read(g.getClass().getResource(appIcon));
			g.setIconImage(image); 
		} catch (Exception e) {
			// Continue and use default app icon
			//e.printStackTrace();
		}
	}

	// set run button with or without image
	private void setRunButton(){
		try {
			imageRun = ImageIO.read(g.getClass().getResource(runIcon));
			bRun= new JButton(new ImageIcon(imageRun));
			bRun.setMargin(new Insets(0,0,0,0));
		} catch (Exception e) {
			bRun= new JButton("Run");
		}
		finally {
			bRun.setToolTipText("Run Cleaning");
		}
	}

	// set info button with or without image
	private void setInfoButton(){
		try {
			imageInfo = ImageIO.read(g.getClass().getResource(infoIcon));
			bInfo= new JButton(new ImageIcon(imageInfo));
			bInfo.setMargin(new Insets(0,0,0,0));
		} catch (Exception e) {
			bInfo= new JButton("Info");
		}
		finally {
			bInfo.setToolTipText("Info and user manual");
		}
	}

	// set exit button with or without image
	private void setExitButton(){
		try {
			imageExit = ImageIO.read(g.getClass().getResource(exitIcon));
			bExit= new JButton(new ImageIcon(imageExit));
			bExit.setMargin(new Insets(0,0,0,0));
		} catch (Exception e) {
			bExit= new JButton("Exit");
		}
		finally {
			bExit.setToolTipText("Exit");
		}
	}

	// set all elements except buttons
	private void setElements(){
		labelSource = new JLabel("Source URL");
		labelResult = new JLabel("Clean URL");
		labelInfo = new JLabel(iniInfo);
		urlClean = new JTextArea(4, 60);
		urlClean.setEditable(false);
		urlClean.setToolTipText("Clean URL code");
		urlSource = new JTextArea(4,60);
		urlSource.setLineWrap(true);
		urlSource.setWrapStyleWord(false);
		urlSource.setToolTipText("Paste Google's source URL code");
		sourceScrollPane = new JScrollPane(urlSource);
		sourceScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		cleanScrollPane = new JScrollPane(urlClean);
		cleanScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

	}
	
	private void setButtonBar(){
		bar = new JToolBar();
		setLayoutButBar();
		bar.setFloatable(false);
		bar.add(bRun);
		bar.add(bExit);
		bar.add(bInfo,Alignment.LEADING);
	}

	// define cut-copy-paste popup menu
	private void definePopupMenu(){
		popup = new JPopupMenu();
		menuCut = new JMenuItem(new DefaultEditorKit.CutAction());
		menuCut.setText("Cut");
		menuCut.setMnemonic(KeyEvent.VK_T);
		popup.add(menuCut);
		menuCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
		menuCopy.setText("Copy");
		menuCopy.setMnemonic(KeyEvent.VK_C);
		popup.add(menuCopy);
		menuPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
		menuPaste.setText("Paste");
		menuPaste.setMnemonic(KeyEvent.VK_P);
		popup.add(menuPaste);
	}

	private void setLayoutButBar(){
		bInfo.setHorizontalAlignment(SwingConstants.TRAILING);
		GroupLayout layout = new GroupLayout(bar);
		bar.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addComponent(bRun)
						.addGap(0)
						.addComponent(bExit)
						.addPreferredGap(ComponentPlacement.RELATED, 309, Short.MAX_VALUE)
						.addComponent(bInfo))
			);
			layout.setVerticalGroup(
				layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(bRun)
							.addComponent(bExit)
							.addComponent(bInfo))
						.addGap(3))
			);
	}
	// set main layout of window
	private void setLayout(int i){
		panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(cleanScrollPane, Alignment.LEADING)
								.addComponent(sourceScrollPane, Alignment.LEADING)
								.addComponent(bar, Alignment.LEADING)
								.addComponent(labelSource, Alignment.LEADING)
								.addComponent(labelResult, Alignment.LEADING)
								.addComponent(labelInfo, Alignment.LEADING)
						)
				)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(bar)
						.addComponent(labelSource)
						.addComponent(sourceScrollPane)
						.addComponent(labelResult)
						.addComponent(cleanScrollPane)
						.addComponent(labelInfo)
				)
		);
		g.add(panel,BorderLayout.CENTER);
	}
	

	// select text in source URL field
	private void selectSource(){
		labelInfo.setForeground(Color.BLACK);
		labelInfo.setText(iniInfo);
		urlSource.selectAll();
	}

	// exit from application
	private void closeProgram() {
		WindowEvent wev = new WindowEvent(g, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}
	
	// this run when user click on Run button
	private void cleanUrl(){
		try {
			labelInfo.setText(iniInfo);
			urlClean.setText("");
			// set clean url to field
			String pom = returnUrl(urlSource.getText());
			if (pom!=null && !pom.equals("0")){
				urlClean.setText(returnUrl(urlSource.getText()));
				// put clean url in clipboard
				StringSelection stringSelection = new StringSelection (urlClean.getText());
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents (stringSelection, null);
				labelInfo.setText("Source URL successfully cleaned and copied in clipboard!");
				labelInfo.setForeground(new Color(0, 100, 0));
			}
			else if (pom==null){
				labelInfo.setText("Source URL is empty!");
				labelInfo.setForeground(Color.RED);
			}
			else {
				labelInfo.setText("Source URL is already clean!");
				labelInfo.setForeground(new Color(0, 100, 0));
			}
		}catch (MalformedURLException e1) {
			// if source url is not correct
			labelInfo.setText("ERROR! Source URL is malformed!");
			labelInfo.setForeground(Color.RED);
		}catch (Exception e1) {
			labelInfo.setText("ERROR! "+e1.getMessage());
			labelInfo.setForeground(Color.RED);
			e1.printStackTrace();
		}
	}

	// clean Google URL
	private String returnUrl(String dirty) throws MalformedURLException, UnsupportedEncodingException {
		if (dirty==null || dirty.isEmpty())
			return null;
		URL u = new URL(dirty);
		// read URL parameters
		String query = u.getQuery();
		// if there is no parameters
		if (query==null)
			return "0";
		// split parameters
		String[] pairs = query.split("&");
		if (pairs==null)
			return "0";
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			// "clean" url is decoded value of "url" parameter
			if (pair.substring(0, idx).equals("url"))
				return URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
		}
		return dirty;
	}
}