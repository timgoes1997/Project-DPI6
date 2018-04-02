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
import java.util.Enumeration;

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
                int messageId = -1;
                try {
                    if(message.propertyExists("aggregationID")){
                        messageId = message.getIntProperty("aggregationID");
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                try {
                    RequestReply rr = serializer.requestReplyFromString(((TextMessage) message).getText());
                    if(messageId >= 0){
                        rr.setAggregationID(messageId);
                    }
                    onReplyArrived(rr);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send(RequestReply rr) throws JMSException {
        sender.send(sender.createTextMessage(serializer.requestReplyToString(rr)));
    }

    public void send(RequestReply rr, int aggregationID) throws JMSException {
        Message msg = sender.createTextMessage(serializer.requestReplyToString(rr));
        msg.setIntProperty("aggregationID", aggregationID);
        sender.send(msg);
    }

    public void onReplyArrived(RequestReply rr) throws JMSException {
        if(rr != null) {
            clientInterface.receivedAction(rr);
        }else{
            throw new JMSException("Received a message with a null value");
        }
    }
}
