package com.nbu.loan.calcs;

import com.nbu.loan.model.Loan;
import com.nbu.loan.model.Payment;

import java.math.BigDecimal;

public abstract class AbstractLoanCalculator implements LoanCalculator {

    public static final BigDecimal BIG_DECIMAL = new BigDecimal("100");

    protected void addPaymentWithCommission(Loan loan, Payment payment, BigDecimal bDecimalPayment) {
        BigDecimal monthlyCommission = loan.getMonthlyCommission();
        if (monthlyCommission != null && monthlyCommission.compareTo(BigDecimal.ZERO) != 0) {
            if (loan.getMonthlyCommissionType() == Loan.PERCENT) {
                monthlyCommission = monthlyCommission.multiply(bDecimalPayment).divide(BIG_DECIMAL, SCALE, MODE);
            }
            payment.setCommission(monthlyCommission);
            payment.setAmount(bDecimalPayment.add(monthlyCommission));
        } else {
            payment.setAmount(bDecimalPayment);
        }
    }

    protected void addDisposableCommission(Loan loan, BigDecimal amount) {
        BigDecimal disposableCommission = loan.getDisposableCommission();
        if (disposableCommission != null && disposableCommission.compareTo(BigDecimal.ZERO) != 0) {
            if (loan.getDisposableCommissionType() == Loan.PERCENT) {
                disposableCommission = disposableCommission.multiply(amount).divide(BIG_DECIMAL, SCALE, MODE);
            }
            loan.setDisposableCommissionPayment(disposableCommission);
        }
    }

    protected BigDecimal calculateAmountWithDownPayment(Loan loan) {
        BigDecimal amount = loan.getAmount();

        BigDecimal downPayment = loan.getDownPayment();
        if (downPayment != null && downPayment.compareTo(BigDecimal.ZERO) != 0) {
            if (loan.getDownPaymentType() == Loan.PERCENT) {
                downPayment = downPayment.multiply(amount).divide(BIG_DECIMAL, SCALE, MODE);
            }
            loan.setDownPaymentPayment(downPayment);
            amount = amount.subtract(downPayment);
        }
        return amount;
    }

    protected BigDecimal calculateEffectiveInterestRate(Loan loan) {

        double loanAmount = loan.getAmount().doubleValue() -
                (loan.getDownPaymentPayment() != null ? loan.getDownPaymentPayment().doubleValue() : 0) -
                (loan.getDisposableCommissionPayment() != null ? loan.getDisposableCommissionPayment().doubleValue() : 0);
        double realInterest = loan.getInterest().doubleValue() / 100;

        double[] payments = new double[loan.getPeriod()];
        int i = 0;
        for (Payment payment : loan.getPayments()) {
            payments[i++] = payment.getAmount().doubleValue();
        }

        double x = calcEffRateUsingIterativeApproach(realInterest, loanAmount, payments, 1);
        return new BigDecimal(x * 100);

    }

    protected double calcEffRateUsingIterativeApproach(double realInterest, double loanAmount, double[] payments, int periodBetweenPayments) {
        int i;
        double x1 = 0;
        double x2 = realInterest * 10;
        double lastKnownX = 0;
        for (i = 0; i < 100; i++) {
            double x = (x1 + x2) / 2;
            if (Math.round(lastKnownX * 100000) == Math.round(x * 100000)) {
                System.out.println("Done in " + i + " iterations");
                break;
            }
            lastKnownX = x;
            double a = calcLoanAmountUsingEffectiveRate(payments, x, periodBetweenPayments);

            if (loanAmount < a) {
                x1 = x;
            } else {
                x2 = x;
            }
        }
        return (x1 + x2) / 2;
    }

    private double calcLoanAmountUsingEffectiveRate(double[] payments, double effectiveRate, int periodBetweenPayments) {
        double result = 0;

        for (int i = 0; i < payments.length; i++) {
            result += payments[i] / (Math.pow(1 + effectiveRate, ((double) (i + 1) * periodBetweenPayments) / (double) 12));
        }

        return result;
    }


    protected BigDecimal getResiduePayment(Loan loan) {
        boolean hasResidue = loan.getResidue() != null && loan.getResidue().compareTo(BigDecimal.ZERO) > 0;
        if (hasResidue) {
            return loan.getResidueType() == Loan.PERCENT ? loan.getAmount().multiply(loan.getResidue()).divide(BIG_DECIMAL, SCALE, MODE) : loan.getResidue();
        } else {
            return BigDecimal.ZERO;
        }
    }
}
