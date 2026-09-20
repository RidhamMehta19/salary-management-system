package com.incubyte.salarymanagement.seed;

import com.incubyte.salarymanagement.department.Department;
import com.incubyte.salarymanagement.department.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {
    @Mock JdbcTemplate jdbcTemplate;
    @Mock DepartmentRepository departmentRepository;
    @Mock TransactionTemplate transactionTemplate;

    @Test
    void seedsTenChunksAndRetriesTransientChunkFailure() {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Long.class)).thenReturn(0L);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM salary_history", Long.class)).thenReturn(0L);
        when(departmentRepository.findAll()).thenReturn(List.of(
                new Department("Engineering"), new Department("Product"), new Department("Sales"),
                new Department("Marketing"), new Department("Finance"), new Department("People"),
                new Department("Operations"), new Department("Customer Success")));
        AtomicInteger transactionAttempts = new AtomicInteger();
        doAnswer(invocation -> {
            if (transactionAttempts.getAndIncrement() == 0) {
                throw new DataAccessResourceFailureException("temporary connection failure");
            }
            @SuppressWarnings("unchecked")
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        DemoDataSeeder seeder = new DemoDataSeeder(jdbcTemplate, departmentRepository, transactionTemplate);
        seeder.seed();

        verify(transactionTemplate, times(11)).executeWithoutResult(any());
        verify(jdbcTemplate, times(20)).batchUpdate(any(String.class), any(BatchPreparedStatementSetter.class));
    }
}
