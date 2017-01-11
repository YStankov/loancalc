package com.nbu.loan.calcs;

import com.nbu.loan.model.Loan;

import java.math.RoundingMode;

public interface LoanCalculator {
  static final RoundingMode MODE = RoundingMode.HALF_UP;
  static final int SCALE = 8;
	void calculate(Loan loan);
}
