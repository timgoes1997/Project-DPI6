package model.gateways;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Consumer;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageReceiverGateway {
    private Connection connection;
    protected Session session;
    private Destination destination;
    private MessageConsumer consumer;

    public MessageReceiverGateway(String channelName) throws NamingException, JMSException {
        setup(channelName);
    }

    private void setup(String channelName) throws NamingException, JMSException {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        // connect to the Destination called “myFirstChannel”
        // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
        props.put(("queue." + channelName), channelName);

        Context jndiContext = new InitialContext(props);
        ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiContext
                .lookup("ConnectionFactory");
        connectionFactory.setTrustAllPackages(true);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // connect to the receiver destination
        destination = (Destination) jndiContext.lookup(channelName);
        consumer = session.createConsumer(destination);

        connection.start(); // this is needed to start receiving messages
    }

    public void setListener(MessageListener ml) throws JMSException {
        consumer.setMessageListener(ml);
    }
}
