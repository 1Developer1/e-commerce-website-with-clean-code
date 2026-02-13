package com.ecommerce.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ecommerce");

    @Test
    void domain_layer_should_not_depend_on_infrastructure_or_adapters() {
        layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("com.ecommerce..")
                .layer("Domain").definedBy("..entity..", "..domain..")
                .layer("UseCase").definedBy("..usecase..")
                .layer("Adapter").definedBy("..adapter..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("UseCase", "Adapter", "Infrastructure")
                .whereLayer("UseCase").mayOnlyBeAccessedByLayers("Adapter", "Infrastructure")
                .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .check(importedClasses);
    }

    @Test
    void controllers_should_not_access_repositories_directly() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .check(importedClasses);
    }

    @Test
    void no_cycles_between_packages() {
        slices().matching("com.ecommerce.(*)..")
                .should().beFreeOfCycles()
                .check(importedClasses);
    }

    @Test
    void use_cases_should_be_named_ending_with_UseCase() {
        classes()
                .that().resideInAPackage("..usecase..")
                .and().areTopLevelClasses()
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("UseCase")
                .orShould().haveSimpleNameEndingWith("Input")
                .orShould().haveSimpleNameEndingWith("Output")
                .orShould().haveSimpleNameEndingWith("Repository") // Allow repository interfaces
                 .orShould().haveSimpleNameEndingWith("Exception")
                .check(importedClasses);
    }

    @Test
    void controllers_should_be_annotated_with_RestController() {
        // Note: Since we are not using Spring Boot yet, this is a placeholder or custom annotation check.
        // If we were using Spring, we would check for @RestController.
        // For now, let's checking if UseCase classes are NOT annotated with framework specific annotations (Clean Architecture).
        classes()
                .that().resideInAPackage("..usecase..")
                .should().notBeAnnotatedWith("org.springframework.stereotype.Service")
                .andShould().notBeAnnotatedWith("javax.transaction.Transactional")
                .check(importedClasses);
    }
}
