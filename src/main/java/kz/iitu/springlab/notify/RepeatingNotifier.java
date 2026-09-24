package kz.iitu.springlab.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component("repeating")
@Order(3)
public class RepeatingNotifier implements Notifier {

    private static final Logger log =
            LoggerFactory.getLogger(RepeatingNotifier.class);

    private final int repeatCount;

    public RepeatingNotifier(
            @Value("${app.repeating-count:3}") int repeatCount) {
        this.repeatCount = repeatCount;
    }

    @PostConstruct
    void init() {
        log.info("REPEATING notifier initialized, count={}", repeatCount);
    }

    @Override
    public String send(String message) {
        return message.repeat(repeatCount);
    }

    @Override
    public String channel() {
        return "repeating";
    }
}