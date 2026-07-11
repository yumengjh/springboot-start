package com.yumg.starter;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

class ArchitectureRulesTest {
    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.yumg.starter");

    @Test
    void controllersDoNotAccessRepositoriesOutsideTheirModule() {
        ArchRule rule = classes().that().areAnnotatedWith(RestController.class)
                .should(notDependOnAnotherModulesRepository());

        rule.allowEmptyShould(true).check(productionClasses);
    }

    @Test
    void sharedContainsOnlyCrossCuttingPackages() {
        ArchRule packageRule = classes().that().resideInAPackage("com.yumg.starter.shared..")
                .should().resideInAnyPackage(
                        "com.yumg.starter.shared.api..",
                        "com.yumg.starter.shared.error..",
                        "com.yumg.starter.shared.infrastructure..",
                        "com.yumg.starter.shared.pagination..",
                        "com.yumg.starter.shared.test..",
                        "com.yumg.starter.shared.time..",
                        "com.yumg.starter.shared.validation..",
                        "com.yumg.starter.shared.web..");
        ArchRule dependencyRule = noClasses().that().resideInAPackage("com.yumg.starter.shared..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.yumg.starter.identity..",
                        "com.yumg.starter.system..",
                        "com.yumg.starter.example..");

        packageRule.allowEmptyShould(true).check(productionClasses);
        dependencyRule.allowEmptyShould(true).check(productionClasses);
    }

    private static ArchCondition<JavaClass> notDependOnAnotherModulesRepository() {
        return new ArchCondition<>("not depend on another module's repositories") {
            @Override
            public void check(JavaClass controller, ConditionEvents events) {
                String controllerModule = topLevelModule(controller);
                controller.getDirectDependenciesFromSelf().stream()
                        .filter(dependency -> isRepository(dependency.getTargetClass()))
                        .filter(dependency -> !controllerModule.equals(topLevelModule(dependency.getTargetClass())))
                        .forEach(dependency -> events.add(SimpleConditionEvent.violated(
                                controller, dependency.getDescription())));
            }
        };
    }

    private static boolean isRepository(JavaClass type) {
        return type.getPackageName().startsWith("com.yumg.starter.")
                && (type.getPackageName().contains(".repository")
                        || type.getSimpleName().endsWith("Repository"));
    }

    private static String topLevelModule(JavaClass type) {
        String root = "com.yumg.starter.";
        String relativePackage = type.getPackageName().substring(root.length());
        int separator = relativePackage.indexOf('.');
        return separator < 0 ? relativePackage : relativePackage.substring(0, separator);
    }
}
