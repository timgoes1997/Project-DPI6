package model.gateways;

import com.google.gson.reflect.TypeToken;
import jdk.nashorn.internal.ir.RuntimeNode;
import messaging.requestreply.RequestReply;
import model.gateways.MessageReceiverGateway;
import model.gateways.MessageSenderGateway;
import model.gateways.Serializer;
import model.interfaces.ClientInterface;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public class AppGateway<REQUEST, REPLY> {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiverGateway;
    private Serializer serializer;
    private ClientInterface clientInterface;

    private final Class<REQUEST> requestClass;
    private final Class<REPLY> replyClass;

    public AppGateway(ClientInterface clientInterface, String senderChannel, String receiverChannel, Class<REQUEST> requestClass, Class<REPLY> replyClass) throws JMSException, NamingException {
        this.sender = new MessageSenderGateway(senderChannel);
        this.requestClass = requestClass;
        this.replyClass = replyClass;
        this.serializer = new Serializer<REQUEST, REPLY>(requestClass, replyClass);
        this.receiverGateway = new MessageReceiverGateway(receiverChannel);
        this.clientInterface = clientInterface;
        this.receiverGateway.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    onReplyArrived(serializer.requestReplyFromString(((TextMessage) message).getText()));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send(RequestReply rr) throws JMSException {
        sender.send(sender.createTextMessage(serializer.requestReplyToString(rr)));
    }

    public void onReplyArrived(RequestReply rr) throws JMSException {
        if(rr != null) {
            clientInterface.receivedAction(rr);
        }else{
            throw new JMSException("Received a message with a null value");
        }
    }
}
