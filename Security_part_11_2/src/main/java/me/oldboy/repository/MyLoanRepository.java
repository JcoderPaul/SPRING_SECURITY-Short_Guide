package me.oldboy.repository;

import me.oldboy.models.money.Loan;

import java.util.List;

public interface MyLoanRepository {
    boolean saveAllMyLoans(List<Loan> loanList);
}
