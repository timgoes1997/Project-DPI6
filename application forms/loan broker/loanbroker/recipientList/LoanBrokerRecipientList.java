package loanbroker.recipientList;

import loanbroker.aggregator.Aggregation;
import loanbroker.aggregator.AggregationListener;
import loanbroker.aggregator.LoanBrokerAggregator;
import messaging.requestreply.RequestReply;
import model.bank.Bank;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.interfaces.ClientInterface;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

public class LoanBrokerRecipientList {

    private LoanBrokerAggregator aggregator;
    private List<BankGateway> bankGateways;
    private ClientInterface clientInterface;
    private int aggregationID;


    public LoanBrokerRecipientList(ClientInterface clientInterface, int aggregationID) throws NamingException, JMSException {
        this.clientInterface = clientInterface;
        this.aggregator = new LoanBrokerAggregator();
        this.aggregationID = aggregationID;
        this.bankGateways = new ArrayList<>();
        setup();
    }

    public void setup() throws JMSException, NamingException {
        for (Bank b : Bank.values()) {
            bankGateways.add(new BankGateway(b, getEvaluation(b), aggregator));
        }
    }

    public void send(RequestReply<BankInterestRequest, BankInterestReply> requestReply) {
        Evaluator evaluator = new Evaluator();
        evaluator.putVariable("amount", Integer.toString(requestReply.getRequest().getAmount()));
        evaluator.putVariable("time", Integer.toString(requestReply.getRequest().getTime()));

        try {
            List<BankGateway> gatewaysToSendFrom = new ArrayList<>();
            for (BankGateway bankGateway : bankGateways) {
                String result = evaluator.evaluate(bankGateway.getEvaluation());
                if(result.equals("1.0")){
                    gatewaysToSendFrom.add(bankGateway);
                }
            }
            if(gatewaysToSendFrom.size() > 0) {
                send(requestReply, gatewaysToSendFrom);
            } //In a real application  you should send back that the request unfortunately didn't get accepted.
        } catch (EvaluationException | JMSException e) {
            e.printStackTrace();
        }
    }

    private void send(RequestReply<BankInterestRequest, BankInterestReply> rr, List<BankGateway> bankGateways) throws JMSException {
        aggregator.setWaitingMessage(new Aggregation(new AggregationListener() {
            @Override
            public void onNoMoreRemainingMessages(RequestReply requestReply) throws JMSException {
                clientInterface.receivedAction(requestReply);
            }
        }, aggregationID, bankGateways.size()));

        for(BankGateway b : bankGateways){
            b.send(rr, aggregationID);
        }
        aggregationID++;
    }

    private String getEvaluation(Bank b) {
        switch (b) {
            case ING:
                return "#{amount} <= 100000 && #{time} <= 10";
            case ABN_AMRO:
                return "#{amount} >= 200000 && #{amount} <= 300000  && #{time} <= 20";
            case RABO_BANK:
                return "#{amount} <= 250000 && #{time} <= 15";
        }
        return "";
    }
}
