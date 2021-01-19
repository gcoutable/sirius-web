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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sirius.web.api.configuration.IRepresentationDescriptionRegistry;
import org.eclipse.sirius.web.api.configuration.IRepresentationDescriptionRegistryConfigurer;
import org.eclipse.sirius.web.core.api.IObjectService;
import org.eclipse.sirius.web.diagrams.INodeStyle;
import org.eclipse.sirius.web.diagrams.LineStyle;
import org.eclipse.sirius.web.diagrams.NodeType;
import org.eclipse.sirius.web.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.web.diagrams.description.DiagramDescription;
import org.eclipse.sirius.web.diagrams.description.LabelDescription;
import org.eclipse.sirius.web.diagrams.description.LabelStyleDescription;
import org.eclipse.sirius.web.diagrams.description.NodeDescription;
import org.eclipse.sirius.web.diagrams.description.SynchronizationPolicy;
import org.eclipse.sirius.web.representations.IRepresentationDescription;
import org.eclipse.sirius.web.representations.Status;
import org.eclipse.sirius.web.representations.VariableManager;
import org.springframework.stereotype.Component;

/**
 * Provides a simple diagram to display domain models (entities, attributes, references).
 *
 * @author pcdavid
 */
@Component
public class DomainDiagramConfigurer implements IRepresentationDescriptionRegistryConfigurer {
    private final IObjectService objectService;

    public DomainDiagramConfigurer(IObjectService objectService) {
        this.objectService = Objects.requireNonNull(objectService);
    }

    @Override
    public void addRepresentationDescriptions(IRepresentationDescriptionRegistry registry) {
        registry.add(this.getDomainDiagramDescription());
    }

    private DiagramDescription getDomainDiagramDescription() {
        // @formatter:off
        Function<VariableManager, String> semanticTargetIdProvider = variableManager -> {
            return variableManager.get(VariableManager.SELF, Object.class).map(this.objectService::getId).orElse(null);
        };
        Function<VariableManager, String> semanticTargetKindProvider = variableManager -> {
            return variableManager.get(VariableManager.SELF, Object.class).map(this.objectService::getKind).orElse(null);
        };
        Function<VariableManager, String> semanticTargetLabelProvider = variableManager -> {
            return variableManager.get(VariableManager.SELF, Object.class).map(this.objectService::getLabel).orElse(null);
        };
        Function<VariableManager, INodeStyle> styleProvider = variableManager -> {
            return RectangularNodeStyle.newRectangularNodeStyle()
                    .color("#4e90d2") //$NON-NLS-1$
                    .borderColor("") //$NON-NLS-1$
                    .borderSize(0)
                    .borderStyle(LineStyle.Solid)
                    .build();
        };

        Function<VariableManager, List<Object>> allDomainTypes = variableManager -> variableManager.get(VariableManager.SELF, EPackage.class)
                                                                                                   .map(this::getAllDomainTypes)
                                                                                                   .orElse(List.of());

        NodeDescription domainTypeNode = NodeDescription.newNodeDescription(UUID.nameUUIDFromBytes("domain-type".getBytes())) //$NON-NLS-1$
                                                        .semanticElementsProvider(allDomainTypes)
                                                        .targetObjectIdProvider(semanticTargetIdProvider)
                                                        .targetObjectKindProvider(semanticTargetKindProvider)
                                                        .targetObjectLabelProvider(semanticTargetLabelProvider)
                                                        .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                                                        .typeProvider(variableManager -> NodeType.NODE_RECTANGLE)
                                                        .styleProvider(styleProvider)
                                                        .labelDescription(this.createLabelDescription())
                                                        .labelEditHandler((variableManager, newValue) -> Status.OK)
                                                        .deleteHandler(variableManager -> Status.OK)
                                                        .borderNodeDescriptions(List.of())
                                                        .childNodeDescriptions(List.of())
                                                        .build();
       // @formatter:on

        // @formatter:off
        return DiagramDescription.newDiagramDescription(UUID.nameUUIDFromBytes("domain-diagram".getBytes())) //$NON-NLS-1$
                                 .label("Domain Diagram") //$NON-NLS-1$
                                 .labelProvider(variableManager -> variableManager.get(VariableManager.SELF, EPackage.class).map(EPackage::getName).orElse("Anonymous Domain")) //$NON-NLS-1$
                                 .canCreatePredicate(variableManager -> variableManager.get(IRepresentationDescription.CLASS, Object.class)
                                                                                       .filter(klass -> klass.equals(EcorePackage.Literals.EPACKAGE))
                                                                                       .isPresent())
                                 .targetObjectIdProvider(semanticTargetIdProvider)
                                 .nodeDescriptions(List.of(domainTypeNode))
                                 .edgeDescriptions(List.of())
                                 .toolSections(List.of())
                                 .build();
        // @formatter:on
    }

    private List<Object> getAllDomainTypes(EPackage ePackage) {
        List<Object> result = new ArrayList<>();
        result.addAll(ePackage.getEClassifiers());
        return result;
    }

    private LabelDescription createLabelDescription() {
        // @formatter:off
        var styleDescription = LabelStyleDescription.newLabelStyleDescription()
                                                    .colorProvider(variableManager -> "#051e37") //$NON-NLS-1$
                                                    .fontSizeProvider(variableManager -> 16)
                                                    .boldProvider(variableManager -> false)
                                                    .italicProvider(variableManager -> false)
                                                    .underlineProvider(variableManager -> false)
                                                    .strikeThroughProvider(variableManager -> false)
                                                    .iconURLProvider(variableManager -> "") //$NON-NLS-1$
                                                    .build();

        Function<VariableManager, String> labelIdProvider = variableManager -> {
            Object parentId = variableManager.getVariables().get(LabelDescription.OWNER_ID);
            return String.valueOf(parentId) + LabelDescription.LABEL_SUFFIX;
        };

        return LabelDescription.newLabelDescription("domain-type-label") //$NON-NLS-1$
                .idProvider(labelIdProvider)
                .textProvider(variableManager -> variableManager.get(VariableManager.SELF, EClass.class).map(EClass::getName).orElse("")) //$NON-NLS-1$
                .styleDescription(styleDescription)
                .build();
        // @formatter:on
    }

}
