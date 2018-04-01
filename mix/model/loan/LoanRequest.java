package model.loan;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * This class stores all information about a
 * request that a client submits to get a loan.
 *
 */
public class LoanRequest implements Serializable {

    @XmlTransient
    private int ssn; // unique client number.

    @XmlTransient
    private int amount; // the ammount to borrow

    @XmlTransient
    private int time; // the time-span of the loan

    public LoanRequest() {
        super();
        this.ssn = 0;
        this.amount = 0;
        this.time = 0;
    }

    public LoanRequest(int ssn, int amount, int time) {
        super();
        this.ssn = ssn;
        this.amount = amount;
        this.time = time;
    }

    public int getSsn() {
        return ssn;
    }

    public void setSsn(int ssn) {
        this.ssn = ssn;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ssn=" + String.valueOf(ssn) + " amount=" + String.valueOf(amount) + " time=" + String.valueOf(time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanRequest request = (LoanRequest) o;
        return ssn == request.ssn &&
                amount == request.amount &&
                time == request.time;
    }

    @Override
    public int hashCode() {

        return Objects.hash(ssn, amount, time);
    }
}
