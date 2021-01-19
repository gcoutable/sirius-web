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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.sirius.diagram.description.ContainerMapping;
import org.eclipse.sirius.diagram.description.DescriptionFactory;
import org.eclipse.sirius.diagram.description.DiagramDescription;
import org.eclipse.sirius.diagram.description.Layer;
import org.eclipse.sirius.diagram.description.style.FlatContainerStyleDescription;
import org.eclipse.sirius.diagram.description.style.StyleFactory;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.viewpoint.description.Group;
import org.eclipse.sirius.viewpoint.description.UserColorsPalette;
import org.eclipse.sirius.viewpoint.description.UserFixedColor;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.sirius.web.api.configuration.IStereotypeDescriptionRegistry;
import org.eclipse.sirius.web.api.configuration.IStereotypeDescriptionRegistryConfigurer;
import org.eclipse.sirius.web.api.configuration.StereotypeDescription;
import org.eclipse.sirius.web.emf.services.SiriusWebJSONResourceFactoryImpl;
import org.eclipse.sirius.web.emf.utils.EMFResourceUtils;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the document stereotypes used to define new domain models and new diagram definitions.
 *
 * @author pcdavid
 */
@Configuration
public class StudioStereotypesConfigurer implements IStereotypeDescriptionRegistryConfigurer {
    public static final String SAMPLE_DOMAIN_ID = "sample_domain"; //$NON-NLS-1$

    public static final String SAMPLE_DOMAIN_LABEL = "Sample Domain Definition"; //$NON-NLS-1$

    public static final String DIAGRAM_DESCRIPTION_ID = "diagram_description"; //$NON-NLS-1$

    public static final String DIAGRAM_DESCRIPTION_LABEL = "Sample Diagram Description"; //$NON-NLS-1$

    @Override
    public void addStereotypeDescriptions(IStereotypeDescriptionRegistry registry) {
        registry.add(new StereotypeDescription(SAMPLE_DOMAIN_ID, SAMPLE_DOMAIN_LABEL, this::getSampleDomainContent));
        registry.add(new StereotypeDescription(DIAGRAM_DESCRIPTION_ID, DIAGRAM_DESCRIPTION_LABEL, this::getSampleDiagramDescription));
    }

    private String getSampleDomainContent() {
        EClass task = EcoreFactory.eINSTANCE.createEClass();
        task.setName("Task"); //$NON-NLS-1$
        EAttribute taskName = EcoreFactory.eINSTANCE.createEAttribute();
        taskName.setName("name"); //$NON-NLS-1$
        taskName.setEType(EcorePackage.Literals.ESTRING);
        task.getEStructuralFeatures().add(taskName);
        EAttribute taskComplete = EcoreFactory.eINSTANCE.createEAttribute();
        taskComplete.setName("completed"); //$NON-NLS-1$
        taskComplete.setEType(EcorePackage.Literals.EBOOLEAN);
        task.getEStructuralFeatures().add(taskComplete);

        EClass participant = EcoreFactory.eINSTANCE.createEClass();
        participant.setName("Participant"); //$NON-NLS-1$
        EAttribute participantName = EcoreFactory.eINSTANCE.createEAttribute();
        participantName.setName("name"); //$NON-NLS-1$
        participantName.setEType(EcorePackage.Literals.ESTRING);
        participant.getEStructuralFeatures().add(participantName);
        EReference workingOn = EcoreFactory.eINSTANCE.createEReference();
        workingOn.setName("workingOn"); //$NON-NLS-1$
        workingOn.setEType(task);
        participant.getEStructuralFeatures().add(workingOn);

        EClass project = EcoreFactory.eINSTANCE.createEClass();
        project.setName("Project"); //$NON-NLS-1$
        EReference tasks = EcoreFactory.eINSTANCE.createEReference();
        tasks.setName("tasks"); //$NON-NLS-1$
        tasks.setEType(task);
        tasks.setUpperBound(-1);
        tasks.setContainment(true);
        project.getEStructuralFeatures().add(tasks);
        EReference participants = EcoreFactory.eINSTANCE.createEReference();
        participants.setName("participants"); //$NON-NLS-1$
        participants.setEType(participant);
        participants.setUpperBound(-1);
        participants.setContainment(true);
        project.getEStructuralFeatures().add(participants);

        EPackage domain = EcoreFactory.eINSTANCE.createEPackage();
        domain.setName("projectDomain"); //$NON-NLS-1$
        domain.setNsURI("https://www.obeosoft.com/ocp/samples/domain/project"); //$NON-NLS-1$
        domain.getEClassifiers().add(project);
        domain.getEClassifiers().add(task);
        domain.getEClassifiers().add(participant);

        Resource res = new ResourceImpl();
        res.getContents().add(domain);
        try {
            return this.saveAsJSON(URI.createURI(domain.getNsURI()), res);
        } catch (IOException e) {
            return ""; //$NON-NLS-1$
        }
    }

    private String getSampleDiagramDescription() {
        UserFixedColor reddish = this.createColor("reddish", 200, 100, 100); //$NON-NLS-1$
        UserFixedColor greenish = this.createColor("greenish", 100, 200, 150); //$NON-NLS-1$
        UserFixedColor black = this.createColor("black", 0, 0, 0); //$NON-NLS-1$
        UserFixedColor lightGray = this.createColor("light gray", 209, 209, 209); //$NON-NLS-1$

        DiagramDescription desc = DescriptionFactory.eINSTANCE.createDiagramDescription();
        desc.setName("Diagram Definition"); //$NON-NLS-1$
        desc.setDomainClass("domain::Project"); //$NON-NLS-1$

        Layer layer = DescriptionFactory.eINSTANCE.createLayer();
        layer.setName("Default"); //$NON-NLS-1$
        desc.setDefaultLayer(layer);

        ContainerMapping taskMapping = DescriptionFactory.eINSTANCE.createContainerMapping();
        taskMapping.setName("Task Mapping"); //$NON-NLS-1$
        taskMapping.setDomainClass("project::Task"); //$NON-NLS-1$
        taskMapping.setSemanticCandidatesExpression("aql:self.tasks"); //$NON-NLS-1$

        FlatContainerStyleDescription taskStyle = StyleFactory.eINSTANCE.createFlatContainerStyleDescription();
        taskStyle.setLabelExpression("aql:self.name"); //$NON-NLS-1$
        taskStyle.setBorderColor(black);
        taskStyle.setLabelColor(black);
        taskStyle.setForegroundColor(lightGray);
        taskStyle.setBackgroundColor(reddish);
        taskMapping.setStyle(taskStyle);
        layer.getContainerMappings().add(taskMapping);

        ContainerMapping participantMapping = DescriptionFactory.eINSTANCE.createContainerMapping();
        participantMapping.setName("Participant Mapping"); //$NON-NLS-1$
        participantMapping.setDomainClass("project::Participant"); //$NON-NLS-1$
        participantMapping.setSemanticCandidatesExpression("aql:self.participant"); //$NON-NLS-1$

        FlatContainerStyleDescription participantStyle = StyleFactory.eINSTANCE.createFlatContainerStyleDescription();
        participantStyle.setLabelExpression("aql:self.name"); //$NON-NLS-1$
        participantStyle.setBorderColor(black);
        participantStyle.setLabelColor(black);
        participantStyle.setForegroundColor(lightGray);
        participantStyle.setBackgroundColor(greenish);
        participantMapping.setStyle(participantStyle);
        layer.getContainerMappings().add(participantMapping);

        Group group = org.eclipse.sirius.viewpoint.description.DescriptionFactory.eINSTANCE.createGroup();
        UserColorsPalette palette = org.eclipse.sirius.viewpoint.description.DescriptionFactory.eINSTANCE.createUserColorsPalette();
        group.getUserColorsPalettes().add(palette);
        palette.getEntries().add(reddish);
        palette.getEntries().add(greenish);
        palette.getEntries().add(black);
        palette.getEntries().add(lightGray);

        Viewpoint viewpoint = org.eclipse.sirius.viewpoint.description.DescriptionFactory.eINSTANCE.createViewpoint();
        viewpoint.setName("Sample Studio"); //$NON-NLS-1$
        viewpoint.getOwnedRepresentations().add(desc);
        group.getOwnedViewpoints().add(viewpoint);

        Resource res = new ResourceImpl();
        res.getContents().add(group);
        try {
            return this.saveAsJSON(URI.createURI(desc.getName()), res);
        } catch (IOException e) {
            return ""; //$NON-NLS-1$
        }
    }

    private UserFixedColor createColor(String name, int red, int green, int blue) {
        UserFixedColor color = org.eclipse.sirius.viewpoint.description.DescriptionFactory.eINSTANCE.createUserFixedColor();
        color.setName(name);
        color.setRed(red);
        color.setGreen(green);
        color.setBlue(blue);
        return color;
    }

    private String saveAsJSON(URI uri, Resource inputResource) throws IOException {
        String content;
        JsonResource ouputResource = new SiriusWebJSONResourceFactoryImpl().createResource(uri);
        ouputResource.getContents().addAll(inputResource.getContents());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Map<String, Object> jsonSaveOptions = new EMFResourceUtils().getFastJSONSaveOptions();
            jsonSaveOptions.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
            jsonSaveOptions.put(JsonResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
            ouputResource.save(outputStream, jsonSaveOptions);
            content = outputStream.toString(StandardCharsets.UTF_8);
        }
        return content;
    }
}
