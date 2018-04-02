package loanbroker.aggregator;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;

public class Aggregation {
    private int aggregationID;
    private int remainingMessages;
    private AggregationListener aggregationInterface;
    private RequestReply<BankInterestRequest, BankInterestReply> requestReply;

    public Aggregation(AggregationListener aggregationInterface, int aggregationID, int remainingMessages){
        this.aggregationInterface = aggregationInterface;
        this.aggregationID = aggregationID;
        this.remainingMessages = remainingMessages;
    }

    public void decrease(RequestReply requestReply) throws JMSException {
        RequestReply<BankInterestRequest, BankInterestReply> rr = (RequestReply<BankInterestRequest, BankInterestReply>) requestReply;
        remainingMessages--;
        if(this.requestReply == null) {
            this.requestReply = rr;
        }else{
            if(this.requestReply.getReply().getInterest() > rr.getReply().getInterest()){
                this.requestReply = rr;
            }
        }

        if(remainingMessages <= 0){
            aggregationInterface.onNoMoreRemainingMessages(this.requestReply);
        }
    }

    public int getAggregationID() {
        return aggregationID;
    }

    public RequestReply<BankInterestRequest, BankInterestReply> getRequestReply() {
        return requestReply;
    }
}
