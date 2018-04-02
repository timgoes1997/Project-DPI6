package loanbroker.aggregator;

import messaging.requestreply.RequestReply;
import model.interfaces.ClientInterface;
import javax.jms.*;

import java.util.ArrayList;
import java.util.List;

public class LoanBrokerAggregator implements AggregatorInterface{

    private List<Aggregation> aggregations;

    public LoanBrokerAggregator(){
        aggregations = new ArrayList<>();
    }

    @Override
    public void setWaitingMessage(Aggregation aggregation) {
        aggregations.add(aggregation);
    }

    @Override
    public void decreaseAggregation(RequestReply requestReply) throws JMSException {
        for (Aggregation a:
             aggregations) {
            if(a.getAggregationID() == requestReply.getAggregationID()){
                a.decrease(requestReply);
            }
        }
    }
}
