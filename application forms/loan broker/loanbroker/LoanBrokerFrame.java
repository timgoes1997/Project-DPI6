package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import messaging.requestreply.RequestReply;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;


public class LoanBrokerFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
    private JList<JListLine> list;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LoanBrokerFrame frame = new LoanBrokerFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Create the frame.
     */
    public LoanBrokerFrame() {
        setTitle("Loan Broker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
        gbl_contentPane.rowHeights = new int[]{233, 23, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 7;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        contentPane.add(scrollPane, gbc_scrollPane);

        list = new JList<JListLine>(listModel);
        scrollPane.setViewportView(list);

        setupLoanReceiver();
        setupRequestReplyReceiver();
    }

    private void setupLoanReceiver() {
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
            props.put(("queue.loanDestination"), " loanDestination");

            Context jndiContext = new InitialContext(props);
            ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup("loanDestination");
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
                        LoanRequest req = (LoanRequest) objectMessage.getObject();
                        add(req);
                        BankInterestRequest bir = new BankInterestRequest(req.getAmount(), req.getTime());
                        add(req, bir);
                        sendBankInterestRequest(bir);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void sendBankInterestRequest(BankInterestRequest bir) {
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
            props.put(("queue.bankInterestDestination"), "bankInterestDestination");

            Context jndiContext = new InitialContext(props);
            ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            sendDestination = (Destination) jndiContext.lookup("bankInterestDestination");
            producer = session.createProducer(sendDestination);

            //String body = "Hello, this is my first message!"; //or serialize an object!
            // create a text message
            Message msg = session.createObjectMessage(bir);
            // send the message
            producer.send(msg);

        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }


    private void setupRequestReplyReceiver() {
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
            props.put(("queue.bankReply"), " bankReply");

            Context jndiContext = new InitialContext(props);
            ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup("bankReply");
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
                        RequestReply<BankInterestRequest, BankInterestReply> req = (RequestReply<BankInterestRequest, BankInterestReply>) objectMessage.getObject();
                        JListLine rr = add(req.getRequest(), req.getReply());
                        if(rr != null) {
                            sendLoanReply(new RequestReply<>(rr.getLoanRequest(), new LoanReply(req.getReply().getInterest(), req.getReply().getQuoteId())));
                        }
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void sendLoanReply(RequestReply<LoanRequest, LoanReply> lr) {
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
            props.put(("queue.loanReply"), "loanReply");

            Context jndiContext = new InitialContext(props);
            ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            sendDestination = (Destination) jndiContext.lookup("loanReply");
            producer = session.createProducer(sendDestination);

            //String body = "Hello, this is my first message!"; //or serialize an object!
            // create a text message
            Message msg = session.createObjectMessage(lr);
            // send the message
            producer.send(msg);

        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    private JListLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getLoanRequest() == request) {
                return rr;
            }
        }

        return null;
    }

    private JListLine getRequestReply(BankInterestRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getBankRequest().equals(request)) {
                return rr;
            }
        }

        return null;
    }

    public void add(LoanRequest loanRequest) {
        listModel.addElement(new JListLine(loanRequest));
    }


    public void add(LoanRequest loanRequest, BankInterestRequest bankRequest) {
        JListLine rr = getRequestReply(loanRequest);
        if (rr != null && bankRequest != null) {
            rr.setBankRequest(bankRequest);
            list.repaint();
        }
    }

    public JListLine add(BankInterestRequest bankRequest, BankInterestReply bankReply) {
        JListLine rr = getRequestReply(bankRequest);
        if (rr != null && bankReply != null) {
            rr.setBankReply(bankReply);
            list.repaint();
        }
        return rr;
    }


}
