package com.insidemovie.backend;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerArchitectureTest {

    @Test
    void controllersShouldNotDependOnRepositoryDirectly() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        List<String> violations = new ArrayList<>();
        for (var candidate : scanner.findCandidateComponents("com.insidemovie.backend.api")) {
            String className = candidate.getBeanClassName();
            if (className == null) {
                continue;
            }
            Class<?> controllerClass = Class.forName(className);
            for (Field field : controllerClass.getDeclaredFields()) {
                String typeName = field.getType().getSimpleName();
                if (typeName.endsWith("Repository")) {
                    violations.add(controllerClass.getName() + "#" + field.getName() + " -> " + field.getType().getName());
                }
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Controller must not inject Repository directly: " + String.join(", ", violations)
        );
    }
}

