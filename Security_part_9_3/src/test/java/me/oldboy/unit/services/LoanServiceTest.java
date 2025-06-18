package me.oldboy.unit.services;

import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.models.money.Loan;
import me.oldboy.repository.LoanRepository;
import me.oldboy.services.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository mockLoanRepository;
    @InjectMocks
    private LoanService loanService;

    private Loan testLoan;
    private LoanReadDto testLoanReadDto;
    private Long testId;
    private List<Loan> testLoanList;
    private List<LoanReadDto> testDtoList;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testLoan = Loan.builder()
                .createDate(LocalDate.of(2011, 06,12))
                .outstandingAmount(30000)
                .amountPaid(40000)
                .totalLoan(40000)
                .loanType("GoodPlace")
                .startDate(LocalDate.of(2011, 06, 12))
                .build();
        testLoanReadDto = LoanReadDto.builder()
                .createDate(LocalDate.of(2011, 06,12))
                .outstandingAmount(30000)
                .amountPaid(40000)
                .totalLoan(40000)
                .loanType("GoodPlace")
                .startDate(LocalDate.of(2011, 06, 12))
                .build();

        testDtoList = List.of(testLoanReadDto);
        testLoanList = List.of(testLoan);
    }

    @Test
    void shouldReturn_EqDtoList_ReadAllLoansByUserId_Test() {
        when(mockLoanRepository.findAllByClientId(testId)).thenReturn(Optional.of(testLoanList));

        assertThat(loanService.readAllLoansByUserId(testId)).isEqualTo(testDtoList);

        verify(mockLoanRepository, times(1)).findAllByClientId(testId);
    }

    @Test
    void shouldReturn_EmptyDtoList_ReadAllLoansByUserId_Test() {
        when(mockLoanRepository.findAllByClientId(testId)).thenReturn(Optional.empty());

        assertThat(loanService.readAllLoansByUserId(testId)).isEqualTo(List.of());

        verify(mockLoanRepository, times(1)).findAllByClientId(testId);
    }
}