package com.propertymanagement.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.propertymanagement", importOptions = {ImportOption.DoNotIncludeTests.class})
public class CleanArchitectureTest {

    @ArchTest
    public static final ArchRule layersMustRespectCleanArchitecture = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Domain").definedBy(resideInAnyPackage("org.propertymanagement.domain.."))
        .layer("Application").definedBy(resideInAnyPackage("org.propertymanagement.associationmeeting..", "org.propertymanagement.notification..")
            .and(resideOutsideOfPackages("..web..", "..persistence..", "..config..", "..infrastructure..")))
        .layer("Infrastructure").definedBy(resideInAnyPackage("..persistence..", "..config..", "..infrastructure.."))
        .layer("Rest").definedBy(resideInAnyPackage("..web..", "..controller.."))
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Rest")
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Rest")
        .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
        .whereLayer("Rest").mayNotBeAccessedByAnyLayer()
        .allowEmptyShould(true)
        .as("The Dependency Law: Inner layers must not depend on outer layers.");

    @ArchTest
    public static final ArchRule domainMustBeFrameworkFree = noClasses()
        .that().resideInAPackage("org.propertymanagement.domain..")
        .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
        .as("Business Core must remain a 'Pure' Java domain, free of Spring or JPA annotations.");
}
