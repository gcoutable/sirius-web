/*******************************************************************************
 * Copyright (c) 2021 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.sample.studio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sirius.web.api.configuration.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.web.api.configuration.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.web.forms.description.AbstractControlDescription;
import org.eclipse.sirius.web.forms.description.FormDescription;
import org.eclipse.sirius.web.forms.description.GroupDescription;
import org.eclipse.sirius.web.forms.description.PageDescription;
import org.eclipse.sirius.web.forms.description.TextfieldDescription;
import org.eclipse.sirius.web.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.representations.Status;
import org.eclipse.sirius.web.representations.VariableManager;
import org.springframework.stereotype.Component;

/**
 * Provides simple properties view for Domain Definition elements.
 *
 * @author pcdavid
 */
@Component
public class DomainPropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    private static final String UNNAMED = "<unnamed>"; //$NON-NLS-1$

    private static final String UNTYPED = "<untyped>"; //$NON-NLS-1$

    private final Function<VariableManager, List<Object>> semanticElementsProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).stream().collect(Collectors.toList());

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        registry.add(this.getDomainProperties());
        registry.add(this.getEntityProperties());
        registry.add(this.getAttributeProperties());
        registry.add(this.getReferenceProperties());
    }

    private FormDescription getDomainProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("domain".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, EPackage.class)
                                                                                                     .map(EPackage::getNsURI)
                                                                                                     .orElse(null);

        List<AbstractControlDescription> controls = List.of(
                this.createTextField("package.name", "Name", //$NON-NLS-1$ //$NON-NLS-2$
                                     pkg -> ((EPackage) pkg).getName(),
                                     (pkg, newName) -> ((EPackage) pkg).setName(newName)));

        return FormDescription.newFormDescription(formDescriptionId)
                .label("Domain") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, EPackage.class).map(EPackage::getName).orElse(UNNAMED))
                .canCreatePredicate(this.getVariableEqualsPredicate(IRepresentationDescription.CLASS, EPackage.class))
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(this.createSimpleGroupDescription(controls), this.getVariableTypePredicate(VariableManager.SELF, EPackage.class))))
                .groupDescriptions(List.of(this.createSimpleGroupDescription(controls)))
                .build();
        // @formatter:on
    }

    private FormDescription getEntityProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("entity".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, EClass.class)
                                                                                                     .map(EClass::getName)
                                                                                                     .orElse(null);

        List<AbstractControlDescription> controls = List.of(
                this.createTextField("entity.name", "Name", //$NON-NLS-1$ //$NON-NLS-2$
                                     klass -> Optional.ofNullable(((EClass) klass).getName()).orElse(UNNAMED),
                                     (klass, newName) -> ((EClass) klass).setName(newName)));

        return FormDescription.newFormDescription(formDescriptionId)
                .label("Entity") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, EClass.class).map(EClass::getName).orElse(null))
                .canCreatePredicate(this.getVariableEqualsPredicate(IRepresentationDescription.CLASS, EClass.class))
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(this.createSimpleGroupDescription(controls), this.getVariableTypePredicate(VariableManager.SELF, EClass.class))))
                .groupDescriptions(List.of(this.createSimpleGroupDescription(controls)))
                .build();
        // @formatter:on
    }

    private FormDescription getAttributeProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("attribute".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, EAttribute.class)
                                                                                                     .map(EAttribute::getName)
                                                                                                     .orElse(null);


        List<AbstractControlDescription> controls = List.of(

                this.createTextField("attribute.name", "Name",  //$NON-NLS-1$ //$NON-NLS-2$
                                     attr -> Optional.ofNullable(((EAttribute) attr).getName()).orElse(UNNAMED),
                                     (attr, newName) -> ((EAttribute) attr).setName(newName)),

                this.createTextField("attribute.type", "Type",  //$NON-NLS-1$ //$NON-NLS-2$
                                     attr -> Optional.ofNullable(((EAttribute) attr).getEType()).map(EClassifier::getName).orElse(UNTYPED),
                                     (attr, newType) -> EcorePackage.eINSTANCE.eContents().stream()
                                                                              .filter(EDataType.class::isInstance)
                                                                              .map(EDataType.class::cast)
                                                                              .filter(dataType -> dataType.getName().equals(newType))
                                                                              .findFirst()
                                                                              .ifPresent(((EAttribute) attr)::setEType)
                ));

        return FormDescription.newFormDescription(formDescriptionId)
                .label("Attribute") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, EAttribute.class).map(EAttribute::getName).orElse(null))
                .canCreatePredicate(this.getVariableEqualsPredicate(IRepresentationDescription.CLASS, EAttribute.class))
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(this.createSimpleGroupDescription(controls), this.getVariableTypePredicate(VariableManager.SELF, EAttribute.class))))
                .groupDescriptions(List.of(this.createSimpleGroupDescription(controls)))
                .build();
        // @formatter:on
    }

    private FormDescription getReferenceProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("reference".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, EReference.class)
                                                                                                     .map(EReference::getName)
                                                                                                     .orElse(null);


        List<AbstractControlDescription> controls = List.of(

                this.createTextField("reference.name", "Name",  //$NON-NLS-1$ //$NON-NLS-2$
                                     ref ->  Optional.ofNullable(((EReference) ref).getName()).orElse(UNNAMED),
                                     (ref, newName) -> ((EReference) ref).setName(newName)),

                this.createTextField("reference.type", "Type", //$NON-NLS-1$ //$NON-NLS-2$
                                     ref -> Optional.ofNullable(((EReference) ref).getEType()).map(EClassifier::getName).orElse(UNTYPED),
                                     (ref, newTypeName) -> {
                                         var newType = ((EReference) ref).getEContainingClass().getEPackage().getEClassifier(newTypeName);
                                         if (newType != null) {
                                             ((EReference) ref).setEType(newType);
                                         }
                                     }));

        return FormDescription.newFormDescription(formDescriptionId)
                .label("Reference") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, EReference.class).map(EReference::getName).orElse(null))
                .canCreatePredicate(this.getVariableEqualsPredicate(IRepresentationDescription.CLASS, EReference.class))
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(this.createSimpleGroupDescription(controls), this.getVariableTypePredicate(VariableManager.SELF, EReference.class))))
                .groupDescriptions(List.of(this.createSimpleGroupDescription(controls)))
                .build();

        // @formatter:on
    }

    private Predicate<VariableManager> getVariableEqualsPredicate(String variableName, Object value) {
        return variableManager -> variableManager.get(variableName, Object.class).filter(value::equals).isPresent();
    }

    private Predicate<VariableManager> getVariableTypePredicate(String variableName, Class<?> klass) {
        return variableManager -> variableManager.get(variableName, klass).isPresent();
    }

    private PageDescription createSimplePageDescription(GroupDescription groupDescription, Predicate<VariableManager> canCreatePredicate) {
        // @formatter:off
        return PageDescription.newPageDescription("page") //$NON-NLS-1$
                              .idProvider(variableManager -> "page") //$NON-NLS-1$
                              .labelProvider(variableManager -> "Properties") //$NON-NLS-1$
                              .semanticElementsProvider(this.semanticElementsProvider)
                              .canCreatePredicate(canCreatePredicate)
                              .groupDescriptions(List.of(groupDescription))
                              .build();
        // @formatter:on
    }

    private GroupDescription createSimpleGroupDescription(List<AbstractControlDescription> controls) {
        // @formatter:off
        return GroupDescription.newGroupDescription("group") //$NON-NLS-1$
                               .idProvider(variableManager -> "group") //$NON-NLS-1$
                               .labelProvider(variableManager -> "General") //$NON-NLS-1$
                               .semanticElementsProvider(this.semanticElementsProvider)
                               .controlDescriptions(controls)
                               .build();
        // @formatter:on
    }

    private TextfieldDescription createTextField(String id, String title, Function<Object, String> reader, BiConsumer<Object, String> writer) {
        Function<VariableManager, String> valueProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).map(reader).orElse(""); //$NON-NLS-1$
        BiFunction<VariableManager, String, Status> newValueHandler = (variableManager, newValue) -> {
            var optionalDiagramMapping = variableManager.get(VariableManager.SELF, Object.class);
            if (optionalDiagramMapping.isPresent()) {
                writer.accept(optionalDiagramMapping.get(), newValue);
                return Status.OK;
            } else {
                return Status.ERROR;
            }
        };
        // @formatter:off
        return TextfieldDescription.newTextfieldDescription(id)
                                   .idProvider(variableManager -> id)
                                   .labelProvider(variableManager -> title)
                                   .valueProvider(valueProvider)
                                   .newValueHandler(newValueHandler)
                                   .build();
        // @formatter:on
    }
}
