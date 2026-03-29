package com.ecommerce.payment.adapter.in.presenter;

import com.ecommerce.payment.usecase.PayOrderOutput;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Presenter for Payment module.
 * Transforms UseCase ResponseModels into View-friendly ViewModels.
 */
@Component
public class PaymentPresenter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Map<String, Object> presentPayOrder(PayOrderOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", output.success());
        viewModel.put("message", output.message());
        viewModel.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return viewModel;
    }
}
