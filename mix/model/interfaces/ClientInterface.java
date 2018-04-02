package model.interfaces;

import messaging.requestreply.RequestReply;

import javax.jms.JMSException;
import javax.jms.Message;

public interface ClientInterface {
    void receivedAction(RequestReply requestReply) throws JMSException;
}
