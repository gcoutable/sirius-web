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
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.sirius.viewpoint.description.Group;
import org.eclipse.sirius.web.api.configuration.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.web.api.configuration.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.web.forms.description.AbstractControlDescription;
import org.eclipse.sirius.web.forms.description.FormDescription;
import org.eclipse.sirius.web.forms.description.GroupDescription;
import org.eclipse.sirius.web.forms.description.PageDescription;
import org.eclipse.sirius.web.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.representations.VariableManager;

/**
 * Provides simple properties view for Diagram Definition elements.
 *
 * @author pcdavid
 */
public class DiagramConfigurationPropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    private static final String UNNAMED = "<unnamed>"; //$NON-NLS-1$

    private final Function<VariableManager, List<Object>> semanticElementsProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).stream().collect(Collectors.toList());

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        registry.add(this.getGroupProperties());
        // Group
        // Viewpoint
        // DiagramDescription
        // ContainerMapping
    }

    private FormDescription getGroupProperties() {
        UUID formDescriptionId = UUID.nameUUIDFromBytes("group".getBytes()); //$NON-NLS-1$

        // @formatter:off
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Group.class)
                                                                                                     .map(Group::getName)
                                                                                                     .orElse(null);

        List<AbstractControlDescription> controls = List.of();

        return FormDescription.newFormDescription(formDescriptionId)
                .label("Group") //$NON-NLS-1$
                .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, Group.class).map(Group::getName).orElse(UNNAMED))
                .canCreatePredicate(this.getVariableEqualsPredicate(IRepresentationDescription.CLASS, Group.class))
                .idProvider(new GetOrCreateRandomIdProvider())
                .targetObjectIdProvider(targetObjectIdProvider)
                .pageDescriptions(List.of(this.createSimplePageDescription(this.createSimpleGroupDescription(controls), this.getVariableTypePredicate(VariableManager.SELF, Group.class))))
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
}
