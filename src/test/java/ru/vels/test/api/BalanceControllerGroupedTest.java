package ru.vels.test.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.support.TransactionTemplate;
import ru.vels.test.entities.BalanceEntity;
import ru.vels.test.repo.BalanceRepository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "balance-grouped", "test-grouped"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BalanceControllerGroupedTest {

    private static final UUID walletId = UUID.randomUUID();

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        transactionTemplate.executeWithoutResult((tx) -> {
            balanceRepository.deleteAll();
        });
    }

    @Test
    void changeBalance_withdraw_new() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "walletId": "%s",
                            "operationType": "WITHDRAW",
                            "amount": 100
                        }
                        """, walletId))
        ).andExpect(MockMvcResultMatchers.status().is(409));

        transactionTemplate.executeWithoutResult(tx -> {
            Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
            assertFalse(balanceOpt.isPresent());
        });
    }

    @Test
    void changeBalance_deposit_new() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "walletId": "%s",
                            "operationType": "DEPOSIT",
                            "amount": 100
                        }
                        """, walletId))
        ).andExpect(MockMvcResultMatchers.status().is(200));

        transactionTemplate.executeWithoutResult(tx -> {
            Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
            assertTrue(balanceOpt.isPresent());
            assertEquals(walletId, balanceOpt.get().getWalletId());
            assertEquals(10_000L, balanceOpt.get().getBalance().longValueExact());
            assertEquals(0, balanceOpt.get().getVersion());
        });
    }

    @Test
    void changeBalance_withdraw_exists_tooMuchSum() throws Exception {
        transactionTemplate.executeWithoutResult(tx -> {
            BalanceEntity entity = new BalanceEntity();
            entity.setWalletId(walletId);
            entity.setBalance(BigInteger.valueOf(5000));
            balanceRepository.save(entity);
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "walletId": "%s",
                            "operationType": "WITHDRAW",
                            "amount": 100
                        }
                        """, walletId))
        ).andExpect(MockMvcResultMatchers.status().is(409));

        transactionTemplate.executeWithoutResult(tx -> {
            Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
            assertTrue(balanceOpt.isPresent());
            assertEquals(walletId, balanceOpt.get().getWalletId());
            assertEquals(5_000L, balanceOpt.get().getBalance().longValueExact());
            assertEquals(0, balanceOpt.get().getVersion());
        });
    }

    @Test
    void changeBalance_withdraw_exists_enoughSum() throws Exception {
        transactionTemplate.executeWithoutResult(tx -> {
            BalanceEntity entity = new BalanceEntity();
            entity.setWalletId(walletId);
            entity.setBalance(BigInteger.valueOf(10000));
            balanceRepository.save(entity);
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "walletId": "%s",
                            "operationType": "WITHDRAW",
                            "amount": 100
                        }
                        """, walletId))
        ).andExpect(MockMvcResultMatchers.status().is(200));

        transactionTemplate.executeWithoutResult(tx -> {
            Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
            assertTrue(balanceOpt.isPresent());
            assertEquals(walletId, balanceOpt.get().getWalletId());
            assertEquals(0L, balanceOpt.get().getBalance().longValueExact());
            assertEquals(1, balanceOpt.get().getVersion());
        });
    }

    @Test
    void changeBalance_deposit_exists() throws Exception {
        transactionTemplate.executeWithoutResult(tx -> {
            BalanceEntity entity = new BalanceEntity();
            entity.setWalletId(walletId);
            entity.setBalance(BigInteger.valueOf(10000));
            balanceRepository.save(entity);
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "walletId": "%s",
                            "operationType": "DEPOSIT",
                            "amount": 100
                        }
                        """, walletId))
        ).andExpect(MockMvcResultMatchers.status().is(200));

        transactionTemplate.executeWithoutResult(tx -> {
            Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
            assertTrue(balanceOpt.isPresent());
            assertEquals(walletId, balanceOpt.get().getWalletId());
            assertEquals(20_000L, balanceOpt.get().getBalance().longValueExact());
            assertEquals(1, balanceOpt.get().getVersion());
        });
    }

    @Test
    void fetchBalance_notFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallets/{walletId}", walletId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is(404));
    }

    @Test
    void fetchBalance_exists() throws Exception {
        transactionTemplate.executeWithoutResult(tx -> {
            BalanceEntity entity = new BalanceEntity();
            entity.setWalletId(walletId);
            entity.setBalance(BigInteger.valueOf(10000));
            balanceRepository.save(entity);
        });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallets/{walletId}", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "amount": 100.0
                        }
                        """));
    }
}