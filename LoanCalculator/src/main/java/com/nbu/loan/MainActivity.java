package com.nbu.loan;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nbu.loan.calcs.AnnuityLoanCalculator;
import com.nbu.loan.calcs.LoanCalculator;
import com.nbu.loan.calcs.DifferentiatedLoanCalculator;
import com.nbu.loan.calcs.FixedPaymentLoanCalculator;
import com.nbu.loan.model.Loan;
import com.nbu.loan.utils.*;

import java.math.BigDecimal;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String SETTINGS_NAME = MainActivity.class.getName();

    public static final LoanCalculator[] CALCULATORS = new LoanCalculator[]{new AnnuityLoanCalculator(), new DifferentiatedLoanCalculator(), new FixedPaymentLoanCalculator()};

    private static final String ZERO = "0";

    private static final int LOAN_INIT = 0;
    private static final int LOAN_INVALID = 1;
    private static final int LOAN_VALID = 2;
    private static final int LOAN_CALCULATED = 3;
    protected static final PriceInputFilter PRICE_INPUT_FILTER = new PriceInputFilter();

    private boolean isAdvancedShowed = true;
    private int loanState = LOAN_INIT;

    private TextView fixedPaymentLabel;
    private TextView periodLabel;
    private TextView resultPeriodTotalText;
    private TextView resultMonthlyPaymentText;
    private TextView resultAmountTotalText;
    private TextView resultInterestTotalText;
    private TextView moreText;
    private TextView resultDownPaymentTotalText;
    private TextView resultCommissionsTotalText;
    private TextView effectiveInterestText;
    private EditText amountEdit;
    private EditText interestEdit;
    private EditText fixedPaymentEdit;
    private EditText periodYearEdit;
    private EditText periodMonthEdit;
    private EditText downPaymentEdit;
    private EditText disposableCommissionEdit;
    private EditText monthlyCommissionEdit;
    private EditText effectiveRateEdit;
    private EditText residueEdit;
    private Spinner loanTypeSpinner;
    private Button calculateButton;
    private Button typeHelpButton;
    private Button periodYearPlusButton;
    private Button periodYearMinusButton;
    private Button periodMonthPlusButton;
    private Button periodMonthMinusButton;
    private Button effectiveRateBtn;
    private PercentValueSwitchButton downPaymentButton;
    private PercentValueSwitchButton disposableCommissionButton;
    private PercentValueSwitchButton monthlyCommissionButton;
    private PercentValueSwitchButton residueButton;
    private ScrollView mainScrollView;
    private ViewGroup resultContainer;
    private ViewGroup periodLayout;
    private ViewGroup advancedViewGroup;
    private ViewGroup resultDownPaymentGroupView;
    private ViewGroup resultCommissionsGroupView;

    private Loan loan = new Loan();

    private LoanCalculator calculator;
    private AlertDialog effectiveRateToNominalDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        setPriceInputFilter(amountEdit, interestEdit, fixedPaymentEdit, periodYearEdit, periodMonthEdit, downPaymentEdit, disposableCommissionEdit, monthlyCommissionEdit, effectiveRateEdit, residueEdit);
        setIconsToButtons();
        registerEventListeners();
        if (isLoanReadyForCalculation(loan)) {
            calculate();
        }
    }

    private void setupAdvancedButton() {
        boolean show = isAdvancedShowed;
        isAdvancedShowed = !isAdvancedShowed;
        advancedViewGroup.setVisibility(show ? View.VISIBLE : View.GONE);
        moreText.setText(show ? R.string.hide : R.string.expand);
        int arrow = show ? R.drawable.arrowup : R.drawable.arrowdown;
        Drawable img = getApplicationContext().getResources().getDrawable(arrow);
        moreText.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void init() {
        effectiveRateEdit = new EditText(this);
        effectiveRateEdit.setInputType(InputType.TYPE_CLASS_PHONE);

        amountEdit = (EditText) findViewById(R.id.amountEdit);
        interestEdit = (EditText) findViewById(R.id.interestEdit);
        fixedPaymentEdit = (EditText) findViewById(R.id.fixedPaymentEdit);
        periodYearEdit = (EditText) findViewById(R.id.periodYearEdit);
        periodMonthEdit = (EditText) findViewById(R.id.periodMonthEdit);
        downPaymentEdit = (EditText) findViewById(R.id.downPaymentEdit);
        disposableCommissionEdit = (EditText) findViewById(R.id.disposableCommissionEdit);
        monthlyCommissionEdit = (EditText) findViewById(R.id.monthlyCommissionEdit);
        residueEdit = (EditText) findViewById(R.id.residueEdit);

        resultMonthlyPaymentText = (TextView) findViewById(R.id.resultMonthlyPaymentText);
        resultAmountTotalText = (TextView) findViewById(R.id.resultAmountTotalText);
        resultInterestTotalText = (TextView) findViewById(R.id.resultIterestTotalText);
        resultPeriodTotalText = (TextView) findViewById(R.id.resultPeriodTotalText);
        fixedPaymentLabel = (TextView) findViewById(R.id.fixedPaymentLabel);
        periodLabel = (TextView) findViewById(R.id.periodLabel);
        moreText = (TextView) findViewById(R.id.moreText);
        resultDownPaymentTotalText = (TextView) findViewById(R.id.resultDownPaymentTotalText);
        resultCommissionsTotalText = (TextView) findViewById(R.id.resultCommissionsTotalText);
        effectiveInterestText = (TextView) findViewById(R.id.effectiveInterestText);

        loanTypeSpinner = (Spinner) findViewById(R.id.loanTypeSpinner);

        calculateButton = (Button) findViewById(R.id.calculateButton);
        periodYearPlusButton = (Button) findViewById(R.id.periodYearPlusButton);
        periodYearMinusButton = (Button) findViewById(R.id.periodYearMinusButton);
        periodMonthPlusButton = (Button) findViewById(R.id.periodMonthPlusButton);
        periodMonthMinusButton = (Button) findViewById(R.id.periodMonthMinusButton);
        typeHelpButton = (Button) findViewById(R.id.loanTypeHelpButton);
        effectiveRateBtn = (Button) findViewById(R.id.effectiveRateBtn);

        mainScrollView = (ScrollView) findViewById(R.id.mainScrollView);
        resultContainer = (ViewGroup) findViewById(R.id.resultContainer);
        periodLayout = (ViewGroup) findViewById(R.id.periodLayout);
        advancedViewGroup = (ViewGroup) findViewById(R.id.advancedViewGroup);
        resultDownPaymentGroupView = (ViewGroup) findViewById(R.id.resultDownPaymentGroupView);
        resultCommissionsGroupView = (ViewGroup) findViewById(R.id.resultCommissionsGroupView);

        downPaymentButton = (PercentValueSwitchButton) findViewById(R.id.downPaymentButton);
        disposableCommissionButton = (PercentValueSwitchButton) findViewById(R.id.disposableCommissionButton);
        monthlyCommissionButton = (PercentValueSwitchButton) findViewById(R.id.monthlyCommissionButton);
        residueButton = (PercentValueSwitchButton) findViewById(R.id.residueButton);

        AlertDialog.Builder effectiveRateToNominalDialogBuilder = new AlertDialog.Builder(this);
        effectiveRateToNominalDialogBuilder.setTitle(this.getString(R.string.eff2NominalConvTitle));
        effectiveRateToNominalDialogBuilder.setMessage(this.getString(R.string.eff2NominalConvMessage));
        effectiveRateToNominalDialogBuilder.setView(effectiveRateEdit);
        effectiveRateToNominalDialogBuilder.setPositiveButton(this.getString(R.string.calc), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    BigDecimal effectiveRate = ViewUtil.getBigDecimalValue(effectiveRateEdit);
                    BigDecimal nominalRate = new BigDecimal(1200 * (Math.pow(1 + effectiveRate.doubleValue() / 100, (double) 1 / (double) 12) - 1));
                    interestEdit.setText(ViewUtil.formatBigDecimal(nominalRate));

                } catch (Exception e) {
                    showError(e);
                }

            }
        });

        effectiveRateToNominalDialogBuilder.setNegativeButton(this.getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        effectiveRateToNominalDialog = effectiveRateToNominalDialogBuilder.create();
    }


    private void setIconsToButtons() {
        typeHelpButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.help), null, null, null);
    }

    private void registerEventListeners() {
        loanTypeSpinner.setOnItemSelectedListener(this);
        calculateButton.setOnClickListener(this);
        typeHelpButton.setOnClickListener(this);
        periodYearPlusButton.setOnClickListener(this);
        periodYearMinusButton.setOnClickListener(this);
        periodMonthPlusButton.setOnClickListener(this);
        periodMonthMinusButton.setOnClickListener(this);
        effectiveRateBtn.setOnClickListener(this);
        moreText.setOnClickListener(this);

        MyTextWatcher myYearTextWatcher = new MyTextWatcher() {
            public void onChange(Editable editable) {
                checkFixPeriod(periodYearEdit, editable);
                invalidateLoan();
            }
        };

        MyTextWatcher myMonthTextWatcher = new MyTextWatcher() {
            public void onChange(Editable editable) {
                checkFixPeriod(periodMonthEdit, editable);
                invalidateLoan();
            }
        };
        periodYearEdit.addTextChangedListener(myYearTextWatcher);
        periodMonthEdit.addTextChangedListener(myMonthTextWatcher);

        MyTextWatcher invalidateWatcher = new MyTextWatcher() {
            public void onChange(Editable editable) {
                invalidateLoan();
            }
        };


        downPaymentEdit.addTextChangedListener(invalidateWatcher);
        disposableCommissionEdit.addTextChangedListener(invalidateWatcher);
        monthlyCommissionEdit.addTextChangedListener(invalidateWatcher);
        residueEdit.addTextChangedListener(invalidateWatcher);

        downPaymentButton.setOnClickListener(this);
        disposableCommissionButton.setOnClickListener(this);
        monthlyCommissionButton.setOnClickListener(this);
        residueButton.setOnClickListener(this);


    }

    private void setPriceInputFilter(EditText... fields) {
        for (EditText field : fields) {
            field.setFilters(new InputFilter[]{PRICE_INPUT_FILTER});
        }

    }

    private void invalidateLoan() {
        loanState = LOAN_INVALID;
    }

    public void onClick(View view) {
        try {
            if (view == calculateButton) {
                calculate();
                if (loanState == LOAN_CALCULATED) {
                    mainScrollView.scrollTo(resultContainer.getLeft(), resultContainer.getTop());
                }
            } else if (view == typeHelpButton) {
                startActivity(new Intent(MainActivity.this, TypeHelpActivity.class));
            } else if (view == periodYearPlusButton) {
                periodYearEdit.setText(Integer.valueOf(ViewUtil.getIntegerValue(periodYearEdit) + 1).toString());
            } else if (view == periodYearMinusButton) {
                periodYearEdit.setText(Integer.valueOf(ViewUtil.getIntegerValue(periodYearEdit) - 1).toString());
            } else if (view == periodMonthPlusButton) {
                periodMonthEdit.setText(Integer.valueOf(ViewUtil.getIntegerValue(periodMonthEdit) + 1).toString());
            } else if (view == periodMonthMinusButton) {
                periodMonthEdit.setText(Integer.valueOf(ViewUtil.getIntegerValue(periodMonthEdit) - 1).toString());
            } else if (view == effectiveRateBtn) {

                effectiveRateButtonPressed();

            } else if (view == moreText) {
                setupAdvancedButton();
            } else if (view == downPaymentButton || view == disposableCommissionButton || view == monthlyCommissionButton || view == residueButton) {
                invalidateLoan();
            }
        } catch (EditTextNumberFormatException e) {
            if (e.editText == periodYearEdit) {
                periodYearEdit.setText(ZERO);
            } else if (e.editText == periodMonthEdit) {
                periodMonthEdit.setText(ZERO);
            }
        }

    }

    private void effectiveRateButtonPressed() {
        effectiveRateToNominalDialog.show();
    }

    private void checkFixPeriod(EditText editText, Editable editable) {
        Integer max = editText == periodMonthEdit ? 12 : 50;
        try {
            Integer value = ViewUtil.getIntegerValue(editText);
            if (value > max) {
                editable.clear();
                editable.append(max.toString());
            } else if (value < 0) {
                editable.clear();
                editable.append(ZERO);
            }
        } catch (EditTextNumberFormatException e) {
            editable.clear();
            editable.append(ZERO);
        }
    }

    private void changeCalculatorType() {
        setTitle((String) loanTypeSpinner.getSelectedItem());
        calculator = CALCULATORS[loanTypeSpinner.getSelectedItemPosition()];
        boolean isFixedPayment = calculator instanceof FixedPaymentLoanCalculator;
        fixedPaymentLabel.setVisibility(isFixedPayment ? View.VISIBLE : View.GONE);
        fixedPaymentEdit.setVisibility(isFixedPayment ? View.VISIBLE : View.GONE);
        periodLabel.setVisibility(isFixedPayment ? View.GONE : View.VISIBLE);
        periodLayout.setVisibility(isFixedPayment ? View.GONE : View.VISIBLE);
    }


    protected void showError(Exception e) {
        new ErrorDialogWrapper(this, e).show();
    }

    protected void showError(int id) {
        new ErrorDialogWrapper(this, getResources().getString(id)).show();
    }

    private void calculate() {
        try {
            invalidateLoan();
            loan = new Loan();
            loadLoanDataFromUI();
            if (isLoanReadyForCalculation(loan)) {
                loanState = LOAN_VALID;
                calculator.calculate(loan);
                showCalculatedData();
                loanState = LOAN_CALCULATED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(e);
        }
    }

    private boolean loadLoanDataFromUI() {
        boolean isFixedPayment = calculator instanceof FixedPaymentLoanCalculator;
        try {
            loan.setLoanType(loanTypeSpinner.getSelectedItemPosition());
            loan.setAmount(ViewUtil.getBigDecimalValue(amountEdit));
            loan.setInterest(ViewUtil.getBigDecimalValue(interestEdit));

            if (isFixedPayment) {
                loan.setFixedPayment(ViewUtil.getBigDecimalValue(fixedPaymentEdit));
            } else {
                int months = ViewUtil.getIntegerValue(periodMonthEdit);
                int years = ViewUtil.getIntegerValue(periodYearEdit);
                loan.setPeriod(years * 12 + months);
            }

            if (loan.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                showError(R.string.errorAmount);
                return false;
            } else if (loan.getInterest().compareTo(BigDecimal.ZERO) <= 0) {
                showError(R.string.errorInterest);
                return false;
            } else if (!isFixedPayment && loan.getPeriod() <= 0) {
                showError(R.string.errorPeriod);
                return false;
            } else if (isFixedPayment && loan.getFixedPayment().compareTo(BigDecimal.ZERO) <= 0) {
                showError(R.string.errorFixedAmount);
                return false;
            }

            loan.setDownPaymentType(downPaymentButton.isPercent() ? Loan.PERCENT : Loan.VALUE);
            loan.setDisposableCommissionType(disposableCommissionButton.isPercent() ? Loan.PERCENT : Loan.VALUE);
            loan.setMonthlyCommissionType(monthlyCommissionButton.isPercent() ? Loan.PERCENT : Loan.VALUE);
            loan.setResidueType(residueButton.isPercent() ? Loan.PERCENT : Loan.VALUE);

            loan.setDownPayment(ViewUtil.getBigDecimalValue(downPaymentEdit));
            loan.setDisposableCommission(ViewUtil.getBigDecimalValue(disposableCommissionEdit));
            loan.setMonthlyCommission(ViewUtil.getBigDecimalValue(monthlyCommissionEdit));
            loan.setResidue(ViewUtil.getBigDecimalValue(residueEdit));


        } catch (EditTextNumberFormatException e) {
            if (e.editText == amountEdit) {
                showError(R.string.errorAmount);
            } else if (e.editText == interestEdit) {
                showError(R.string.errorInterest);
            } else if (e.editText == fixedPaymentEdit) {
                showError(R.string.errorFixedAmount);
            } else if (e.editText == downPaymentEdit) {
                showError(R.string.errorDownPayment);
            } else if (e.editText == disposableCommissionEdit) {
                showError(R.string.errorDispCommission);
            } else if (e.editText == monthlyCommissionEdit) {
                showError(R.string.errorMonthlyCommission);
            } else if (e.editText == residueEdit) {
                showError(R.string.errorResidue);
            }
            e.editText.requestFocus();
            mainScrollView.scrollTo(e.editText.getLeft(), e.editText.getTop());
            return false;
        }
        return true;
    }

    private void showCalculatedData() {
        String monthlyPayment = "";
        BigDecimal max = loan.getMaxMonthlyPayment();
        BigDecimal min = loan.getMinMonthlyPayment();
        if (max.compareTo(min) == 0) {
            monthlyPayment = ViewUtil.formatBigDecimal(max);
        } else {
            monthlyPayment = ViewUtil.formatBigDecimal(max) + " - " + ViewUtil.formatBigDecimal(min);
        }
        resultMonthlyPaymentText.setText(monthlyPayment);

        BigDecimal totalAmount = loan.getTotalAmount();

        if (loan.getDownPaymentPayment() != null && loan.getDownPaymentPayment().compareTo(BigDecimal.ZERO) != 0) {
            resultDownPaymentGroupView.setVisibility(View.VISIBLE);
            resultDownPaymentTotalText.setText(ViewUtil.formatBigDecimal(loan.getDownPaymentPayment()));
        } else {
            resultDownPaymentGroupView.setVisibility(View.GONE);
        }

        if (loan.getCommissionsTotal() != null && loan.getCommissionsTotal().compareTo(BigDecimal.ZERO) != 0) {
            resultCommissionsGroupView.setVisibility(View.VISIBLE);
            resultCommissionsTotalText.setText(ViewUtil.formatBigDecimal(loan.getCommissionsTotal()));
        } else {
            resultCommissionsGroupView.setVisibility(View.GONE);
        }


        resultAmountTotalText.setText(ViewUtil.formatBigDecimal(totalAmount));
        resultInterestTotalText.setText(ViewUtil.formatBigDecimal(loan.getTotalInterests()));
        resultPeriodTotalText.setText(loan.getPeriod().toString());

        effectiveInterestText.setText(ViewUtil.formatBigDecimal(loan.getEffectiveInterestRate()));

        Toast.makeText(this, getResources().getText(R.string.msgCalculated), Toast.LENGTH_SHORT).show();
    }

    private boolean isLoanReadyForCalculation(Loan loan) {
        if (loan.getAmount() == null || loan.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (loan.getInterest() == null || loan.getInterest().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        boolean isFixedPayment = calculator instanceof FixedPaymentLoanCalculator;
        if (isFixedPayment && (loan.getFixedPayment() == null || loan.getFixedPayment().compareTo(BigDecimal.ZERO) <= 0)) {
            return false;
        }

        if (!isFixedPayment && (loan.getPeriod() == null || loan.getPeriod() <= 0)) {
            return false;
        }
        return true;
    }


    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        changeCalculatorType();
        invalidateLoan();
        if (isLoanReadyForCalculation(loan)) {
            loanState = LOAN_VALID;
            calculate();
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void cleanForm() {
        cleanEdit("", amountEdit, interestEdit, fixedPaymentEdit, downPaymentEdit, disposableCommissionEdit, monthlyCommissionEdit, effectiveRateEdit, residueEdit);
        cleanEdit("0", periodYearEdit, periodMonthEdit);
    }

    private void cleanEdit(String value, EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.getText().clear();
            editText.getText().append(value);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            setInputType(InputType.TYPE_NULL, amountEdit, interestEdit, fixedPaymentEdit, periodYearEdit, periodMonthEdit, downPaymentEdit, disposableCommissionEdit, monthlyCommissionEdit, effectiveRateEdit, residueEdit);
        } else {
            setInputType(InputType.TYPE_CLASS_PHONE, amountEdit, interestEdit, fixedPaymentEdit, periodYearEdit, periodMonthEdit, downPaymentEdit, disposableCommissionEdit, monthlyCommissionEdit, effectiveRateEdit, residueEdit);
        }

        super.onConfigurationChanged(newConfig);
    }

    private void setInputType(int type, EditText... fields) {
        for (EditText field : fields) {
            field.setInputType(type);
        }
    }
}