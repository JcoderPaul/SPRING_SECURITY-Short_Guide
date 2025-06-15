package me.oldboy.unit.services;

import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.models.money.Loan;
import me.oldboy.repository.LoanRepository;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class LoanServiceTest {

    @Mock
    private LoanRepository mockLoanRepository;
    @InjectMocks
    private LoanService loanService;

    private List<Loan> testList;
    private Loan testLoan;
    private Long testId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testLoan = Loan.builder()
                .loanId(testId)
                .amountPaid(1000)
                .loanType("Кольцо всевластия")
                .totalLoan(12000)
                .build();

        testList = List.of(testLoan, new Loan(), new Loan());
    }

    @Test
    void shouldReturnRightListSize_ReadAllLoansByUserId_Test() {
        when(mockLoanRepository.findAllByClientId(testId)).thenReturn(Optional.of(testList));

        var originalSize = testList.size();
        var expectedSize = loanService.readAllLoansByUserId(testId).size();

        assertThat(originalSize).isEqualTo(expectedSize);
    }

    @Test
    void shouldReturnEmptyList_ReadAllLoansByUserId_Test() {
        when(mockLoanRepository.findAllByClientId(testId)).thenReturn(Optional.empty());

        var expectedSize = loanService.readAllLoansByUserId(testId).size();

        assertThat(0).isEqualTo(expectedSize);
    }

    @Test
    void shouldReturnDtoListElement_ReadAllLoansByUserId_Test() {
        when(mockLoanRepository.findAllByClientId(testId)).thenReturn(Optional.of(testList));
        assertThat(loanService.readAllLoansByUserId(testId).get(0) instanceof LoanReadDto).isTrue();
    }
}