package loanbroker.aggregator;

import messaging.requestreply.RequestReply;

import javax.jms.*;

public interface AggregatorInterface {

    void setWaitingMessage(Aggregation aggregation);

    void decreaseAggregation(RequestReply requestReply) throws JMSException;
}
