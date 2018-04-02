package model.interfaces;

import messaging.requestreply.RequestReply;

import javax.jms.JMSException;

public interface ClientInterface {
    void receivedAction(RequestReply requestReply) throws JMSException;
}
