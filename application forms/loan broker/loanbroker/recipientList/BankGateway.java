package loanbroker.recipientList;

import loanbroker.aggregator.Aggregation;
import loanbroker.aggregator.AggregationListener;
import loanbroker.aggregator.LoanBrokerAggregator;
import messaging.requestreply.RequestReply;
import model.bank.Bank;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.gateways.AppGateway;
import model.interfaces.ClientInterface;
import model.loan.LoanReply;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class BankGateway {
    private Bank bank;
    private String evaluation;
    private AppGateway<BankInterestRequest, BankInterestReply> bankGateway;
    private LoanBrokerAggregator aggregator;

    public BankGateway(Bank bank, String evaluation, LoanBrokerAggregator aggregator) throws NamingException, JMSException {
        this.bank = bank;
        this.evaluation = evaluation;
        this.aggregator = aggregator;
        this.bankGateway = new AppGateway<>(new ClientInterface() {
            @Override
            public void receivedAction(RequestReply requestReply) throws JMSException {
                aggregator.decreaseAggregation(requestReply);
            }
        },
                bank.toString() + "BankClientReceive", bank.toString() + "BankClientSend", BankInterestRequest.class, BankInterestReply.class);
    }

    void send(RequestReply requestReply, int aggregationID) throws JMSException {
        requestReply.setAggregationID(aggregationID);
        bankGateway.send(requestReply);
    }

    public String getEvaluation() {
        return evaluation;
    }
}
