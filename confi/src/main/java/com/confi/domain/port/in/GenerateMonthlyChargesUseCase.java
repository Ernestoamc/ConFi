package com.confi.domain.port.in;

import com.confi.domain.model.SubscriptionCharge;
import java.util.List;

public interface GenerateMonthlyChargesUseCase {

    List<SubscriptionCharge> execute(int mes, int anio);
}
