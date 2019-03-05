package tech.claudioed.register.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.claudioed.register.domain.Payment;
import tech.claudioed.register.domain.event.NotifyPaymentEvent;
import tech.claudioed.register.domain.service.data.EventRequest;
import tech.claudioed.register.domain.service.data.PaymentCallback;

/**
 * @author claudioed on 2019-03-05.
 * Project register
 */
@Component
public class NotifyCrmListener implements ApplicationListener<NotifyPaymentEvent> {

  private final RestTemplate restTemplate;

  private final ObjectMapper objectMapper;

  public NotifyCrmListener(RestTemplate restTemplate,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void onApplicationEvent(NotifyPaymentEvent event) {
    final Payment payment = event.getOrderData().getPayment();
    final PaymentCallback paymentCallback = PaymentCallback.builder().customerId(payment.getCustomerId())
        .orderId(payment.getOrderId()).paymentId(payment.getId()).status(payment.getStatus())
        .value(payment.getValue()).build();
    final Map<String,Object> data = this.objectMapper.convertValue(paymentCallback, Map.class);
    final EventRequest eventRequest = EventRequest.builder().type(payment.getStatus()).data(data).build();
    final String path = event.getOrderData().getCrmUrl() + "api/orders/{id}/events";
    this.restTemplate.postForEntity(path,eventRequest,String.class,payment.getOrderId());
  }

}