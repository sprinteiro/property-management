package com.property.management.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.property.management")
public class CleanArchitectureTest {

    @ArchTest
    public static final ArchRule layersMustRespectCleanArchitecture = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Domain").definedBy("..business.core..")
        .layer("Application").definedBy("..business.app..")
        .layer("Infrastructure").definedBy("..persistence..", "..infrastructure..")
        .layer("Rest").definedBy("..rest..", "..app..")

        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Rest")
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Rest")
        .whereLayer("Domain").mayNotAccessAnyLayer()
        .as("The Dependency Law: Inner layers must not depend on outer layers.");

    @ArchTest
    public static final ArchRule domainMustBeFrameworkFree = noClasses()
        .that().resideInAPackage("..business.core..")
        .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
        .as("Business Core must remain a 'Pure' Java domain, free of Spring or JPA annotations.");
}
