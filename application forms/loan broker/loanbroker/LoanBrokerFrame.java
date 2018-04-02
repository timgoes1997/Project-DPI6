package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import loanbroker.recipientList.LoanBrokerRecipientList;
import messaging.requestreply.RequestReply;
import model.bank.*;
import model.gateways.AppGateway;
import model.interfaces.ClientInterface;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;


public class LoanBrokerFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
    private JList<JListLine> list;
    private AppGateway<LoanRequest, LoanReply> loanClientGateWay;
    private LoanBrokerRecipientList bankRecipientList;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LoanBrokerFrame frame = new LoanBrokerFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Create the frame.
     */
    public LoanBrokerFrame() {
        setTitle("Loan Broker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
        gbl_contentPane.rowHeights = new int[]{233, 23, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 7;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        contentPane.add(scrollPane, gbc_scrollPane);

        list = new JList<JListLine>(listModel);
        scrollPane.setViewportView(list);

        try {
            loanClientGateWay = new AppGateway<LoanRequest, LoanReply>(new ClientInterface() {
                @Override
                public void receivedAction(RequestReply requestReply) throws JMSException {
                    LoanRequest req = (LoanRequest)requestReply.getRequest();
                    add(req);
                    BankInterestRequest bir = new BankInterestRequest(req.getAmount(), req.getTime());
                    add(req, bir);
                    RequestReply rr = new RequestReply<BankInterestRequest,BankInterestReply>(bir, null);
                    bankRecipientList.send(rr);
                }
            }, "loanClientReceive", "loanClientSend", LoanRequest.class, LoanReply.class);

            bankRecipientList = new LoanBrokerRecipientList(new ClientInterface() {
                @Override
                public void receivedAction(RequestReply requestReply) throws JMSException {
                    RequestReply<BankInterestRequest, BankInterestReply> req = (RequestReply<BankInterestRequest, BankInterestReply>)requestReply;
                    JListLine rr = add(req.getRequest(), req.getReply());
                    if(rr != null) {
                        loanClientGateWay.send(new RequestReply<>(rr.getLoanRequest(), new LoanReply(req.getReply().getInterest(), req.getReply().getQuoteId())));
                    }
                }
            }, 1);
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
        }
    }

    private JListLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getLoanRequest() == request) {
                return rr;
            }
        }

        return null;
    }

    private JListLine getRequestReply(BankInterestRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getBankRequest().equals(request)) {
                return rr;
            }
        }

        return null;
    }

    public void add(LoanRequest loanRequest) {
        listModel.addElement(new JListLine(loanRequest));
    }


    public void add(LoanRequest loanRequest, BankInterestRequest bankRequest) {
        JListLine rr = getRequestReply(loanRequest);
        if (rr != null && bankRequest != null) {
            rr.setBankRequest(bankRequest);
            list.repaint();
        }
    }

    public JListLine add(BankInterestRequest bankRequest, BankInterestReply bankReply) {
        JListLine rr = getRequestReply(bankRequest);
        if (rr != null && bankReply != null) {
            rr.setBankReply(bankReply);
            list.repaint();
        }
        return rr;
    }
}
