package socket_chat;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Window {

	private JFrame frame;

	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final int DEFAULT_AES_SIZE = 16;
	public static final int DEFAULT_SIGN_SIZE = 256;

	public static boolean isServer = false;
	JRadioButton radio_svr = new JRadioButton("Server");
	JRadioButton radio_cnt = new JRadioButton("Client");
	JLabel portNum = new JLabel("8888");
	JLabel porttitle;
	static JTextArea chatArea;
	JTextField chatArea2;
	JTextField file_loc;
	JTextArea fileArea;
	JTextArea keyArea;
	JTextArea keystatArea;
	JLabel statetitle;
	JLabel iptitle;
	JLabel state;
	JLabel ipadress;
	JButton btn_conn;

	JButton keygenbtn = new JButton("Key Generation");
	JButton loadbtn = new JButton("Load from a file");
	JButton savebtn = new JButton("Save into a file");
	JButton sendpubbtn = new JButton("Send Public key");

	ServerSocket serverSocket;
	Socket socket;

	DataInputStream instream_data;
	DataOutputStream outstream_data;

	OutputStream outstream = null;
	ObjectOutputStream outstream_obj = null;
	InputStream instream = null;
	ObjectInputStream instream_obj = null;

	BufferedOutputStream outstream_buff = null;
	BufferedInputStream instream_buff = null;
	FileInputStream inputstream_file = null;
	FileOutputStream outputstream_file = null;

	// key parameters
	private KeyPair keypair = null;
	PublicKey publickey = null;
	PrivateKey privatekey = null;
	PublicKey recpublickey = null;

	private String encoded_pubKey = null;
	private String encoded_priKey = null;

	private SecretKey secretkey = null;
	private String encodedSecretkey = null;
	private byte[] encrypted_AESkey = null;
	private SecretKeySpec skeySpec = null;
	private boolean safeConnect = false;

	HashMap<String, String> keymap; // key database
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// class Window constructor: only call method initialize
	public Window() {
		initialize();
	}

	// method initialize : create GUI components with actions
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Simple Public Key System");
		frame.getContentPane().setLayout(null);

		//===========================User Area============================
		JLabel usrmode = new JLabel("User mode");
		usrmode.setBounds(29, 21, 170, 27);
		usrmode.setFont(new Font("Dialog", Font.BOLD, 18));
		frame.getContentPane().add(usrmode);
		
		// Radio Button: server mode
		radio_svr.setBounds(29, 54, 75, 25);
		radio_svr.setFont(new Font("Courier", Font.PLAIN, 15));
		frame.getContentPane().add(radio_svr);
				
		// Radio Button: client mode
		radio_cnt.setBounds(104, 54, 75, 25);
		radio_cnt.setFont(new Font("Courier", Font.PLAIN, 15));
		frame.getContentPane().add(radio_cnt);

		// User mode: status, ip address, port no
		statetitle = new JLabel("User mode:  ");
		state = new JLabel(" ");
		statetitle.setBounds(233, 45, 88, 25);
		state.setBounds(333, 45, 107, 25);
		frame.getContentPane().add(statetitle);
		frame.getContentPane().add(state);

		iptitle = new JLabel("IP Address:  ");
		ipadress = new JLabel();
		iptitle.setBounds(233, 70, 88, 25);
		ipadress.setBounds(333, 70, 107, 25);
		frame.getContentPane().add(iptitle);
		frame.getContentPane().add(ipadress);

		porttitle = new JLabel("Port Number:  ");
		porttitle.setBounds(233, 95, 88, 25);
		portNum.setBounds(333, 95, 107, 25);
		frame.getContentPane().add(porttitle);
		frame.getContentPane().add(portNum);
		
		// Button: click to connect
		btn_conn = new JButton("Connect");
		btn_conn.setBounds(29, 85, 170, 27);
		btn_conn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//if server is selected
				if (radio_svr.getSelectedObjects() != null) {
					//server get ready for wait client
					ServerThread serverThread = new ServerThread();
					serverThread.setDaemon(true);
					serverThread.start();
					System.out.println("Btn clicked, SERVER OPENED");
					state.setText("Server");
					chatArea.append("Server opened: port number 8888.\n");
					isServer = true;

				} else if(radio_cnt.getSelectedObjects() != null){
					//if client is selected
					//socket access to server port 8888
					ClientThread clientThread = new ClientThread();
					clientThread.setDaemon(true);
					clientThread.start();
					System.out.println("Btn clicked, CLIENT OPENED");
					state.setText("Client");
					isServer = false;
				}

			}
		});
		frame.getContentPane().add(btn_conn);


		//===========================Key Area============================
		JPanel panel = new JPanel();
		panel.setBounds(12, 130, 474, 228);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		keyArea = new JTextArea(4, 10);
		keyArea.setEditable(false);
		JScrollPane keyscrollPane = new JScrollPane(keyArea);
		keyscrollPane.setBounds(141, 10, 322, 156);
		panel.add(keyscrollPane);
		keygenbtn.setBounds(2, 44, 131, 23);
		panel.add(keygenbtn);
		loadbtn.setBounds(2, 77, 131, 23);
		panel.add(loadbtn);
		savebtn.setBounds(2, 110, 131, 23);
		panel.add(savebtn);
		sendpubbtn.setBounds(2, 143, 131, 23);
		panel.add(sendpubbtn);
		
		JLabel lbl_keyarea = new JLabel("Key Area");
		lbl_keyarea.setFont(new Font("Dialog", Font.BOLD, 18));
		lbl_keyarea.setBounds(8, 16, 110, 20);
		panel.add(lbl_keyarea);

		keystatArea = new JTextArea(1, 10);
		keystatArea.setEditable(false);
		JScrollPane statscrollPane = new JScrollPane(keystatArea);
		statscrollPane.setBounds(2, 176, 461, 52);
		panel.add(statscrollPane);
		
		//key generation button generates RSA key pair, then save it into local directory
		keygenbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Btn Key Generation clicked");
				keypair = generateRSAKey();
				publickey = keypair.getPublic();
				privatekey = keypair.getPrivate();
				encoded_pubKey = Base64.getEncoder().encodeToString(publickey.getEncoded());
				encoded_priKey = Base64.getEncoder().encodeToString(privatekey.getEncoded());

				// key information saved into local directory
				if (isServer == true) {
					savePublickey("Server", encoded_pubKey);
				} else {
					savePublickey("Client", encoded_pubKey);
				}
				keyArea.append("Keypair Generated!\n");

			}

		});
		
		//send public key button should be only available on server mode
		sendpubbtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("sendpubbtn key clicked");

				if (isServer == false) {
					chatArea.append("Send Public key button only available on Server.");
					return;
				}
				sendPublickey(keypair);
			}
		});

		//save into a file button saves public key in local directory
		savebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("savebtn key clicked");

				String encodedpubkey = null;
				for (String d : keymap.keySet()) {
					encodedpubkey = keymap.get(d);
					savePublickey(d, encodedpubkey);
				}
				keyArea.append("\nSavebtn key clicked, keys are saved in file\n");
			}

		});
		
		//load from a file button get key information from local directory
		loadbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("loadbtn key clicked");
				String targetpubkey = loadPublickey();
				  keymap.put("8888", targetpubkey);
				  keyArea.append("Other's public key has been loaded from a file\n");
				  keystatArea.append("\n[port no.8888's public key]: " );
				  keystatArea.append(targetpubkey);
			}
		});
		

		//===========================Chat Area============================
		JPanel chatPanel = new JPanel(); // 1
		chatPanel.setBounds(12, 368, 462, 176);
		chatPanel.setBackground(Color.LIGHT_GRAY);
		frame.getContentPane().add(chatPanel);
		chatPanel.setLayout(null);

		JLabel lbl_chatarea = new JLabel("Chat Area");
		lbl_chatarea.setFont(new Font("Dialog", Font.BOLD, 18));
		lbl_chatarea.setBounds(10, 7, 101, 15);
		chatPanel.add(lbl_chatarea);
		
		chatArea = new JTextArea(5, 50);
		chatArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(chatArea);
		scrollPane.setBounds(10, 25, 440, 110);
		chatPanel.add(scrollPane);

		chatArea2 = new JTextField();
		chatArea2.setBounds(12, 145, 350, 21);
		chatPanel.add(chatArea2);

		JButton btnSend = new JButton("send"); 
		btnSend.setHorizontalAlignment(SwingConstants.LEFT);
		btnSend.setBounds(374, 145, 67, 23);
		chatPanel.add(btnSend);
		//send button sends chat message to receiver which is written in chatArea2
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (safeConnect == false) {
					chatArea.append("Please exchange key to have safe connection \n");
					return;
				}
				 sendMessage();
			}
		});
		frame.getContentPane().add(chatPanel);
		
		
		//===========================File Area============================
		JPanel filePanel = new JPanel();
		filePanel.setBackground(Color.LIGHT_GRAY);
		filePanel.setBounds(12, 562, 462, 176);
		frame.getContentPane().add(filePanel);
		filePanel.setLayout(null);
		
		JLabel lbl_filearea = new JLabel("File Area");
		lbl_filearea.setFont(new Font("Dialog", Font.BOLD, 18));
		lbl_filearea.setBounds(12, 10, 84, 15);
		filePanel.add(lbl_filearea);
		
		fileArea = new JTextArea(5, 50);
		fileArea.setEditable(false);
		JScrollPane filescrollPane = new JScrollPane(fileArea);
		filescrollPane.setBounds(10, 30, 440, 110);
		filePanel.add(filescrollPane);
		
		JButton btnFileSend = new JButton("file search");
		btnFileSend.setBounds(355, 145, 95, 23);
		filePanel.add(btnFileSend);
		
		file_loc = new JTextField();
		file_loc.setBounds(12, 145, 331, 21);
		filePanel.add(file_loc);
		file_loc.setColumns(10);
		file_loc.setEditable(false);
		
		//file search button sends file using file server
		btnFileSend.addActionListener( new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				if(safeConnect == false) {
					fileArea.append("Please exchange key to have safe connection \n");
					return;
				}
				String folderPath = "";
		        
		        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		        chooser.setCurrentDirectory(new File("/")); // Current local directory
		        chooser.setAcceptAllFileFilterUsed(true); 
		        chooser.setDialogTitle("File Explorer");
		        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); //file selection mode
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary File", "cd11");
		        chooser.setFileFilter(filter);
		        int returnVal = chooser.showOpenDialog(null); 
		        if(returnVal == JFileChooser.APPROVE_OPTION) { // if user click '열기'
		            folderPath = chooser.getSelectedFile().toString();
		        }else if(returnVal == JFileChooser.CANCEL_OPTION){ // if user click '취소'
		            System.out.println("cancel"); 
		            folderPath = "";
		        }
				file_loc.setText(folderPath);
				sendFile(folderPath);
			}
		});
	}
	// constructor of Window ended

	// inner class ServerThread
	class ServerThread extends Thread {

		@Override
		public void run() {
			try {
				File makeFolder = new File("Server");
				// if directory doesn't exist:
				if (!makeFolder.exists()) {
					// make folder named as folderName(server)
					makeFolder.mkdir();
					System.out.println("Folder created: " + makeFolder.exists());
				} else {
					System.out.println("Folder exists: " + makeFolder.exists());
				}
				
				keymap = new HashMap<>();
				serverSocket = new ServerSocket(8888);// port number can be changed
				String ip = InetAddress.getLocalHost().getHostAddress();
				ipadress.setText(ip);
				
				//server waits for client socket
				socket = serverSocket.accept();
				chatArea.append(socket.getInetAddress().getHostAddress() + "has accessed.\n");
				btn_conn.setText("Connected!");
				
				//stream creation
				instream = socket.getInputStream();
				instream_buff = new BufferedInputStream(instream);
				outstream = socket.getOutputStream();
				outstream_buff = new BufferedOutputStream(outstream);
				instream_data = new DataInputStream(instream_buff);
				outstream_data = new DataOutputStream(outstream_buff);
				String filename = null;

				while (true) {
					if (encrypted_AESkey == null) {
						//get encrypted AES key from client
						instream_obj = new ObjectInputStream(instream);
						encrypted_AESkey = (byte[]) instream_obj.readObject();
						String encryted_AESkey_string = new String(Base64.getEncoder().encode(encrypted_AESkey));
						keyArea.append("\n[Received encrypted AES key from Client]\n");
						keyArea.append(encryted_AESkey_string + "\n");
					} else {
						//save decrypted AES key
						skeySpec = new SecretKeySpec(decryptRSA(encrypted_AESkey, privatekey), "AES");
						encodedSecretkey = Base64.getEncoder().encodeToString(skeySpec.getEncoded());

						keyArea.append("[AES Secretkey ready!]\n");
						keyArea.append(encodedSecretkey+"\n");
						safeConnect = true;//AES encryption secure mode
						chatArea.append("Text encryption started\n");
						break;
					}
				}

				while (true) {
					//data decryption: buffer size 1040
					byte[] data = new byte[DEFAULT_BUFFER_SIZE];
					byte[] encryptedData = new byte[1040];
					int initsize = instream_data.read(encryptedData);
					byte[] rawdata = new byte[initsize];

					for (int i = 0; i < initsize; i++) {
						rawdata[i] = encryptedData[i];
					}

					String isize = Integer.toString(initsize);
					chatArea.append("Initial message byte len: " + isize+"\n");

					data = decryptAES(rawdata, skeySpec);

					int size = data.length;
					String csize = Integer.toString(size);
					String protocol = new String(data, 0, 6); // 패킷 헤더에서 데이터 타입 가져오기

					if (protocol.equals("[MESG]")) { // 메세지 타입
						chatArea.append("[Client] : " + (new String(data, 6, size - 6).trim()) + "\n");
						chatArea.setCaretPosition(chatArea.getText().length());
					} else if (protocol.equals("[SFNE]")) { // 파일일떄
						try {
							filename = new String(data, 6, size - 6).trim();
							fileArea.append(filename + " will be transmitted\n");
							if (filename != null)
								outputstream_file = new FileOutputStream("Server//" + filename);
						} catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}
					} else if (protocol.equals("[SFIL]")) { // 파일일떄 2
						try {//헤더 뺀 데이터만큼 바이트 저장
							outputstream_file.write(data, 8, (int) data[6] * 100 + data[7]);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						if ((int) data[6] * 100 + data[7] < 1016) {  // 버퍼 크기가 헤더 뺀 크기보다 작음: 마지막버퍼
							try {
								if (outputstream_file != null)
									outputstream_file.close();
								fileArea.append(filename + " transmitted successfully!\n");
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}
					}
					chatArea.setCaretPosition(chatArea.getText().length());
				} // while end

			} catch (IOException e) {
				chatArea.append("\n================\nDisconnected.\n");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// method run() end
	}
	// inner class ServerThread end

	// inner class ClientThread
	class ClientThread extends Thread {
		@Override
		public void run() {
			try {
				File makeFolder = new File("Client");
				if (!makeFolder.exists()) {
					// make folder named as folderName(Client)
					makeFolder.mkdir();
					System.out.println("Folder created: " + makeFolder.exists());
				} else {
					System.out.println("Folder exists: " + makeFolder.exists());
				}
				keymap = new HashMap<>();

				socket = new Socket(InetAddress.getLocalHost(), 8888);
				chatArea.append("Connect successfully.\n");
				btn_conn.setText("Connected!");

				String ip = InetAddress.getLocalHost().getHostAddress();
				ipadress.setText(ip);
				//stream creation
				instream = socket.getInputStream();
				outstream = socket.getOutputStream();
				instream_buff = new BufferedInputStream(instream);
				outstream_buff = new BufferedOutputStream(outstream);
				instream_data = new DataInputStream(instream_buff);
				outstream_data = new DataOutputStream(outstream_buff);

				String filename = null;

				while (true) {
					if (recpublickey == null) {
						instream_obj = new ObjectInputStream(instream);
						recpublickey = (PublicKey) instream_obj.readObject();
						encoded_pubKey = Base64.getEncoder().encodeToString(recpublickey.getEncoded());

						// save server's pubkey
						keymap.put(portNum.getText(), encoded_pubKey);

						// print out pubkey
						keystatArea.append("\n[RSA PublicKey from server]\n");
						keystatArea.append(encoded_pubKey);
					} else {
						// AES secret key created, encrypted by server's public key
						secretkey = generateAESkey();
						skeySpec = new SecretKeySpec(secretkey.getEncoded(), "AES");
						encrypted_AESkey = encryptRSA(secretkey.getEncoded(), recpublickey);
						String encodedKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
						keyArea.append("\n[Encrypted Secretkey has been sent to server]");
						keyArea.append(encodedKey);

						sendEncryptedAESkey(encrypted_AESkey);
						safeConnect = true;
						chatArea.append("Text encryption started\n");
						break;
					}
				}

				while (true) {

					//verification: sign used
					byte[] firstdata = new byte[DEFAULT_BUFFER_SIZE + DEFAULT_AES_SIZE + DEFAULT_SIGN_SIZE];
					byte[] data = new byte[DEFAULT_BUFFER_SIZE];
					byte[] sign = new byte[DEFAULT_SIGN_SIZE];
					byte[] encryptedData = new byte[DEFAULT_BUFFER_SIZE + DEFAULT_AES_SIZE];

					int initsize = instream_data.read(firstdata); // 1024 + 16 + 256 = 1296

					String isize = Integer.toString(initsize);
					chatArea.append("Initial message byte len: " + isize + "\n");

					if (initsize > 1024) {
						// sign extraction
						System.arraycopy(firstdata, 0, encryptedData, 0, encryptedData.length);
						System.arraycopy(firstdata, encryptedData.length, sign, 0, sign.length);

						fileArea.append("Get Encrypted Length: " + encryptedData.length); // 1040
						fileArea.append(" | Get Sign Length: " + sign.length + " | "); // 256

						//verification
						verifySign(encryptedData, sign);
						data = decryptAES(encryptedData, skeySpec);
					} else {
						//AES decryption
						byte[] rawdata = new byte[initsize];
						for (int i = 0; i < initsize; i++) {
							rawdata[i] = firstdata[i];
						}
						data = decryptAES(rawdata, skeySpec);
					}
					int size = data.length;
					String csize = Integer.toString(size);
					String protocol = new String(data, 0, 6); // 패킷 헤더에서 데이터 타입 가져오기

					if (protocol.equals("[MESG]")) { // 메세지일때
						chatArea.append("[Server] : " + (new String(data, 6, size - 6).trim()) + "\n");
						chatArea.setCaretPosition(chatArea.getText().length());
					} else if (protocol.equals("[SFNE]")) { // 파일일때
						try {
							filename = new String(data, 6, size - 6).trim();
							fileArea.append(filename + " will be transmitted.\n");
							if (filename != null)
								outputstream_file = new FileOutputStream("Client//" + filename);
						} catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}
					} else if (protocol.equals("[SFIL]")) { // 파일일때2
						try {
							outputstream_file.write(data, 8, (int) data[6] * 100 + data[7]); // 헤더 뺀 바이트만 저장
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						if ((int) data[6] * 100 + data[7] < 1016) { // 버퍼 크기가 헤더 뺀 크기보다 작음: 마지막버퍼
							try {
								if (outputstream_file != null)
									outputstream_file.close();
								fileArea.append("\n" + filename + " transmitted successfully!\n");
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}

					}
					chatArea.setCaretPosition(chatArea.getText().length());
				} // while end

			} catch (UnknownHostException e) {
				chatArea.append("\n\nError raised: Unknown Host Exception.\n");

			} catch (IOException e) {
				chatArea.append("\n\nFailed to connect: check the port no.\n");

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
		// method run() end
	}
	// inner class ClientThread end
	
	// method for send message
	void sendMessage() {
		Thread t = new Thread() {
			@Override
			public void run() {
				String msg = chatArea2.getText(); 
				chatArea2.setText("");
				chatArea.append("[SEND] : " + msg + "\n");// show message on TextArea
				chatArea.setCaretPosition(chatArea.getText().length());

				try {
					msg = "[MESG]" + msg;
					//message AES encryption
					byte[] encryptedMsg = encryptAES(msg, skeySpec);
					chatArea.append("Encrypted message byte length: " + encryptedMsg.length+"\n"); // 1040
					outstream_data.write(encryptedMsg);
					outstream_data.flush();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		};
		t.start();

	}

	// method for send file
	void sendFile(String fileLocation) {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					File file = new File(fileLocation);
					int n = 0;
					long fileSize = file.length();
					String fileName = file.getName();
					if (fileName == "") {
						fileArea.append("Please choose file to send\n");
						return;
					}
					fileArea.append(fileName + " starts transmit.\n");

					String str = "[SFNE]" + fileName;
					
					//file AES encryption + file transfer
					byte[] encryptedFilename = encryptAES(str, skeySpec);
					outstream_data.write(encryptedFilename);
					outstream_data.flush();

					byte[] strData = "[SFIL]".getBytes();
					byte[] data = new byte[DEFAULT_BUFFER_SIZE];

					for (int i = 0; i < 6; i++) {
						data[i] = strData[i];
					}
					String finalLen;
					int testint;
					try {
						inputstream_file = new FileInputStream(file);
						while (true) {
							//file data saved onto data[8~1024]
							testint = inputstream_file.read(data, 8, DEFAULT_BUFFER_SIZE - 8);
							if (testint == -1)//after file all transmitted
								break;
							data[6] = (byte) (testint / 100);
							data[7] = (byte) (testint % 100);

							fileArea.append("\nData Length: " + testint); // 1024

							//data AES encryption
							byte[] encryptedData = encryptByteAES(data, skeySpec);
							finalLen = Integer.toString(encryptedData.length);
							fileArea.append("\nEncrypted Length: " + finalLen + "  "); // 1040

							if (isServer == true) {
								byte[] sign = addSigniture(encryptedData);

								fileArea.append("\nSigniture Length: " + sign.length); // 256

								byte[] sendbytes = new byte[DEFAULT_BUFFER_SIZE + DEFAULT_AES_SIZE + DEFAULT_SIGN_SIZE];
								fileArea.append("\nFinal before Length: " + sendbytes.length); // 1040
								//AES encrypted data
								System.arraycopy(encryptedData, 0, sendbytes, 0, encryptedData.length);
								//AES encrypted digital sign
								System.arraycopy(sign, 0, sendbytes, encryptedData.length, sign.length); 
								fileArea.append("\nFinal Length: " + sendbytes.length); // 1040 + 256 => 1296

								outstream_data.write(sendbytes);
								outstream_data.flush();

							} else {
								outstream_data.write(encryptedData);
								outstream_data.flush();
							}
						}
						fileArea.append("\nfile transmitted!\n");

					} catch (FileNotFoundException fe) {
						System.out.println(fe.getMessage());
					}
				} catch (IOException ie) {
					System.out.println(ie.getMessage());
				} finally {
					try {

						if (inputstream_file != null)
							inputstream_file.close();
					} catch (IOException ie) {
						System.out.println(ie.getMessage());
					}
				}
			}
		};
		t.start();
	}

	//message for add signature
	protected byte[] addSigniture(byte[] encryptedData) {
		Signature sig2;
		byte[] signatureBytes2 = null;

		try {
			sig2 = Signature.getInstance("SHA512WithRSA");
			sig2.initSign(keypair.getPrivate());

			System.out.println("Data Length Original: " + encryptedData.length);
			System.out.print("Data original: ");
			for (byte b : encryptedData)
				System.out.printf("%02X ", b);
			sig2.update(encryptedData);
			System.out.println("\nData Length update: " + encryptedData.length);
			System.out.print("Data updated: ");
			for (byte b : encryptedData)
				System.out.printf("%02X ", b);
			signatureBytes2 = sig2.sign(); //return: signatured data

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return signatureBytes2;

	}

	//method for verify signature
	protected boolean verifySign(byte[] encryptedData, byte[] sign) {
		boolean tf = false;

		try {

			Signature sig2 = Signature.getInstance("SHA512WithRSA");
			sig2.initVerify(recpublickey);
			sig2.update(encryptedData);
			fileArea.append("Verification: ");

			tf = sig2.verify(sign);

			fileArea.append(String.valueOf(tf)+"\n");
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

		return tf;

	}

	//method for send public key
	public void sendPublickey(KeyPair pair) {

		try {
			if (keypair == null) {
				keyArea.append("Please create keypair first!!");
				return;
			} else {
				outstream = socket.getOutputStream();
				outstream_obj = new ObjectOutputStream(outstream);
				outstream_obj.writeObject(publickey);
				outstream_obj.flush();

				keyArea.append("[Send my RSA Publickey to the Other.]");
				keyArea.append("\n Public Key : ");
				keyArea.append(encoded_pubKey);
				keyArea.append("\n Public Key Length : " + publickey.getEncoded().length + " byte");
				keyArea.append("\n Public key has sent to Client.");
			}
		} catch (Exception e) {
			
		}
	}

	//method for save public key into local directory
	public void savePublickey(String usercode, String encoded_pubKey2) {
		keymap.put(usercode, encoded_pubKey2);
		if (isServer == true) {
			usercode = "Server//" + usercode + ".txt";
		} else {
			usercode = "Client//" + usercode + ".txt";
		}
		File makeFolder = new File(usercode);
		try {
			if (makeFolder.exists()) {
				keyArea.append("This key is already saved...");
				return;
			}
			BufferedWriter fw = new BufferedWriter(new FileWriter(usercode, true));

			fw.write(encoded_pubKey2);
			fw.flush();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	//method for load public key from local directory
	public String loadPublickey() {
		String encodedPubkey = null;
		JfileChooserUtil file = new JfileChooserUtil();
		String filepath = file.jFileChooserUtil();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			encodedPubkey = br.readLine(); 
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return encodedPubkey;
	}

	//method for send encrypted AESkey
	public void sendEncryptedAESkey(byte[] encrypted_AESkey) {
		try {
			if (encrypted_AESkey == null) {
				keyArea.append("Please create AES key and encrypt it with Publickey");
				return;
			} else {
				outstream = socket.getOutputStream();
				outstream_obj = new ObjectOutputStream(outstream);
				outstream_obj.writeObject(encrypted_AESkey);
				outstream_obj.flush();
				String encryted_AESkey_string = new String(Base64.getEncoder().encode(encrypted_AESkey));
				keyArea.append("[encrypted AES key]\n");
				keyArea.append(encryted_AESkey_string + "\n");
				keyArea.append("Encrypted AES key has sent to Server.");
			}
		} catch (Exception e) {

		}
	}

//method for generate RSA key pair
	public KeyPair generateRSAKey() {

		KeyPair keyPair = null;
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			keyPair = generator.generateKeyPair();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyPair;
	}


	public byte[] decryptRSA(byte[] encrypted, PrivateKey privateKey) {
		byte[] b1 = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");

			System.out.println("=== RSA Decryption ==="); // 전달받은 암호문을 privatekey로 복호화
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			b1 = cipher.doFinal(encrypted);
			System.out.print("\n Recovered Plaintext : " + new String(b1) + "\n");
			for (byte b : b1)
				System.out.printf("%02x ", b);
			System.out.println("\n Recovered Plaintext Length : " + b1.length + " byte");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return b1;
	}

	public static byte[] encryptAES(String text, SecretKey key) {
		byte[] ciphertext2 = null;
		try {
			System.out.println("\n\nAES Encryption ");
			Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher2.init(Cipher.ENCRYPT_MODE, key);
			byte[] plaintext = text.getBytes(); 
			//plaintext encrptyion
			ciphertext2 = cipher2.doFinal(plaintext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ciphertext2;
	}

	public static byte[] encryptByteAES(byte[] data, SecretKey key) {
		byte[] ciphertext2 = null;
		try {
			System.out.println("\n\nAES Encryption ");
			Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher2.init(Cipher.ENCRYPT_MODE, key);
			System.out.print("\nData Length: " + data.length + " byte");
			//plaintext encrptyion
			ciphertext2 = cipher2.doFinal(data); 
			System.out.print("\nEncryptedData Length: " + ciphertext2.length + " byte");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ciphertext2;
	}

	public static byte[] decryptAES(byte[] ciphertext, SecretKey key) {
		String output2 = null;
		byte[] decrypttext2 = new byte[DEFAULT_BUFFER_SIZE];
		try {
			chatArea.append("Ciphertext: " + ciphertext.toString() + "\n");
			Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
			// decryption
			cipher2.init(Cipher.DECRYPT_MODE, key); // 암호문 복호화
			decrypttext2 = cipher2.doFinal(ciphertext);
			output2 = new String(decrypttext2, "UTF8");
			System.out.print("\nDecrypted Text:" + output2 + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decrypttext2;
	}

	public static byte[] encryptRSA(byte[] plaintext, PublicKey publicKey) {
		byte[] b0 = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			b0 = cipher.doFinal(plaintext);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return b0;
	}

	public static SecretKey generateAESkey() {
		System.out.println("\n\nAES Key Generation ");
		SecretKey key2 = null;
		try {
			KeyGenerator keyGen2 = KeyGenerator.getInstance("AES");
			keyGen2.init(128);
			key2 = keyGen2.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return key2;
	}
}
