package loanbroker.aggregator;

import messaging.requestreply.RequestReply;

import javax.jms.JMSException;

public interface AggregationListener {
    void onNoMoreRemainingMessages(RequestReply requestReply) throws JMSException;
}
