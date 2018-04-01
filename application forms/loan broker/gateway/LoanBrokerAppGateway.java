package gateway;

import model.gateways.MessageReceiverGateway;
import model.gateways.MessageSenderGateway;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiverGateway;
    private LoanSerializer loanSerializer;

    public LoanBrokerAppGateway(String senderChannel, String receiverChannel) throws JMSException, NamingException {
        this.sender = new MessageSenderGateway(senderChannel);
        this.loanSerializer = new LoanSerializer();
        this.receiverGateway = new MessageReceiverGateway(receiverChannel);
    }
    
    public void applyForLoan(LoanRequest request){

    }

}
