package com.confi.domain.port.out;

import com.confi.domain.model.CashEntry;

import java.time.Instant;
import java.util.List;

public interface CashEntryRepository {

    CashEntry save(CashEntry cashEntry);

    List<CashEntry> findByPeriodo(Instant desde, Instant hasta);
}
