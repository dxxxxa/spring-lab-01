package kz.iitu.springlab.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kz.iitu.springlab.lifecycle.LifecycleDemo;
import kz.iitu.springlab.notify.NotificationService;
import kz.iitu.springlab.scope.TicketOffice;
import kz.iitu.springlab.notify.Notifier;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;
    private final Notifier repeating;
    private final LifecycleDemo lifecycle;

    public Lab2Controller(NotificationService notifications,
                      LifecycleDemo lifecycle,
                      @Qualifier("repeating") Notifier repeating) {
    this.notifications = notifications;
    this.lifecycle = lifecycle;
    this.repeating = repeating;
}

    @GetMapping("/notify")
    public Map<String, Object> notify(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of(
                "primary", notifications.viaPrimary(text),
                "console", notifications.viaConsole(text),
                "all", notifications.viaAll(text),
                "beanNames", notifications.names()
        );
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }
    @GetMapping("/custom")
public String custom(
        @RequestParam(defaultValue = "Hello") String text) {
    return repeating.send(text);
}
}