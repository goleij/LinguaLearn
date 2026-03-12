package com.germanlearning;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("germanlearning")
public class GermanLearningApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(GermanLearningApplication.class, args);
    }
}
