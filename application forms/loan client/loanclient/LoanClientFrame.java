package loanclient;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import messaging.requestreply.RequestReply;
import model.loan.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class LoanClientFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfSSN;
	private DefaultListModel<RequestReply<LoanRequest,LoanReply>> listModel = new DefaultListModel<RequestReply<LoanRequest,LoanReply>>();
	private JList<RequestReply<LoanRequest,LoanReply>> requestReplyList;

	private JTextField tfAmount;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JTextField tfTime;

	/**
	 * Create the frame.
	 */
	public LoanClientFrame() {
		setTitle("Loan Client");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 684, 619);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {0, 0, 30, 30, 30, 30, 0};
		gbl_contentPane.rowHeights = new int[] {30,  30, 30, 30, 30};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblBody = new JLabel("ssn");
		GridBagConstraints gbc_lblBody = new GridBagConstraints();
		gbc_lblBody.insets = new Insets(0, 0, 5, 5);
		gbc_lblBody.gridx = 0;
		gbc_lblBody.gridy = 0;
		contentPane.add(lblBody, gbc_lblBody);
		
		tfSSN = new JTextField();
		GridBagConstraints gbc_tfSSN = new GridBagConstraints();
		gbc_tfSSN.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfSSN.insets = new Insets(0, 0, 5, 5);
		gbc_tfSSN.gridx = 1;
		gbc_tfSSN.gridy = 0;
		contentPane.add(tfSSN, gbc_tfSSN);
		tfSSN.setColumns(10);
		
		lblNewLabel = new JLabel("amount");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfAmount = new JTextField();
		GridBagConstraints gbc_tfAmount = new GridBagConstraints();
		gbc_tfAmount.anchor = GridBagConstraints.NORTH;
		gbc_tfAmount.insets = new Insets(0, 0, 5, 5);
		gbc_tfAmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfAmount.gridx = 1;
		gbc_tfAmount.gridy = 1;
		contentPane.add(tfAmount, gbc_tfAmount);
		tfAmount.setColumns(10);
		
		lblNewLabel_1 = new JLabel("time");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		contentPane.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		tfTime = new JTextField();
		GridBagConstraints gbc_tfTime = new GridBagConstraints();
		gbc_tfTime.insets = new Insets(0, 0, 5, 5);
		gbc_tfTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfTime.gridx = 1;
		gbc_tfTime.gridy = 2;
		contentPane.add(tfTime, gbc_tfTime);
		tfTime.setColumns(10);
		
		JButton btnQueue = new JButton("send loan request");
		btnQueue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int ssn = Integer.parseInt(tfSSN.getText());
				int amount = Integer.parseInt(tfAmount.getText());
				int time = Integer.parseInt(tfTime.getText());				
				
				LoanRequest request = new LoanRequest(ssn,amount,time);
				listModel.addElement( new RequestReply<LoanRequest,LoanReply>(request, null));	
				// to do:  send the JMS with request to Loan Broker
				sendLoan(request);
			}
		});
		GridBagConstraints gbc_btnQueue = new GridBagConstraints();
		gbc_btnQueue.insets = new Insets(0, 0, 5, 5);
		gbc_btnQueue.gridx = 2;
		gbc_btnQueue.gridy = 2;
		contentPane.add(btnQueue, gbc_btnQueue);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 7;
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		requestReplyList = new JList<RequestReply<LoanRequest,LoanReply>>(listModel);
		scrollPane.setViewportView(requestReplyList);
		setupLoanReplyReceiver();
	}

	private void setupLoanReplyReceiver() {
		Connection connection; // to connect to the JMS
		Session session; // session for creating consumers

		Destination receiveDestination; //reference to a queue/topic destination
		MessageConsumer consumer; // for receiving messages

		try {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

			// connect to the Destination called “myFirstChannel”
			// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
			props.put(("queue.loanReply"), " loanReply");

			Context jndiContext = new InitialContext(props);
			ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			connectionFactory.setTrustAllPackages(true);
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// connect to the receiver destination
			receiveDestination = (Destination) jndiContext.lookup("loanReply");
			consumer = session.createConsumer(receiveDestination);

			connection.start(); // this is needed to start receiving messages

		} catch (NamingException | JMSException e) {
			e.printStackTrace();
			return;
		}

		try {
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message msg) {
					System.out.println("received message: " + msg);
					ObjectMessage objectMessage = (ObjectMessage) msg;
					try {
						RequestReply<LoanRequest, LoanReply> req = (RequestReply<LoanRequest, LoanReply>) objectMessage.getObject();
						add(req);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}


	private void sendLoan(LoanRequest request) {
		Connection connection; // to connect to the ActiveMQ
		Session session; // session for creating messages, producers and

		Destination sendDestination; // reference to a queue/topic destination
		MessageProducer producer; // for sending messages

		try {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

			// connect to the Destination called “myFirstChannel”
			// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
			props.put(("queue.loanDestination"), "loanDestination");

			Context jndiContext = new InitialContext(props);
			ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			connectionFactory.setTrustAllPackages(true);
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// connect to the sender destination
			sendDestination = (Destination) jndiContext.lookup("loanDestination");
			producer = session.createProducer(sendDestination);

			//String body = "Hello, this is my first message!"; //or serialize an object!
			// create a text message
			Message msg = session.createObjectMessage(request);
			// send the message
			producer.send(msg);

		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method returns the RequestReply line that belongs to the request from requestReplyList (JList). 
	 * You can call this method when an reply arrives in order to add this reply to the right request in requestReplyList.
	 * @param request
	 * @return
	 */
   private RequestReply<LoanRequest,LoanReply> getRequestReply(LoanRequest request){    
     
     for (int i = 0; i < listModel.getSize(); i++){
    	 RequestReply<LoanRequest,LoanReply> rr =listModel.get(i);
    	 if (rr.getRequest().equals(request)){
    		 return rr;
    	 }
     }
     
     return null;
   }

	public void add(RequestReply<LoanRequest, LoanReply> receivedReply){
		RequestReply<LoanRequest,LoanReply> rr = getRequestReply(receivedReply.getRequest());
		if (rr!= null && receivedReply != null){
			rr.setReply(receivedReply.getReply());
			requestReplyList.repaint();
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanClientFrame frame = new LoanClientFrame();
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
