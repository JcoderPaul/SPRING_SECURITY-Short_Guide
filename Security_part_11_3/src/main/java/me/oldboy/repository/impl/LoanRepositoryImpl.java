package me.oldboy.repository.impl;

import lombok.RequiredArgsConstructor;
import me.oldboy.models.money.Loan;
import me.oldboy.repository.MyLoanRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@RequiredArgsConstructor
public class LoanRepositoryImpl implements MyLoanRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SAVE_LOAN = """
        INSERT INTO loans (client_id, start_dt, loan_type, total_loan, amount_paid, outstanding_amount, create_dt) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    @Override
    public boolean saveAllMyLoans(List<Loan> loanList) {
        int updateResult = 0;
        List<Object[]> args = loanList
                .stream()
                .map(loan -> new Object[]{loan.getClient().getId(),
                                          loan.getStartDate(),
                                          loan.getLoanType(),
                                          loan.getTotalLoan(),
                                          loan.getAmountPaid(),
                                          loan.getOutstandingAmount(),
                                          loan.getCreateDate()})
                .toList();

        for(int i = 0; i < loanList.size(); i++){
            updateResult = jdbcTemplate.update(SAVE_LOAN, args.get(i));
        }
        return updateResult > 0 ? true:false;
    }
}