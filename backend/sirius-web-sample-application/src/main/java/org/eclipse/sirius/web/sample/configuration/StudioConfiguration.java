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
package org.eclipse.sirius.web.sample.configuration;

import java.util.UUID;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sirius.web.api.configuration.IStereotypeDescriptionRegistry;
import org.eclipse.sirius.web.api.configuration.IStereotypeDescriptionRegistryConfigurer;
import org.eclipse.sirius.web.api.configuration.StereotypeDescription;
import org.eclipse.sirius.web.domain.Domain;
import org.eclipse.sirius.web.domain.DomainFactory;
import org.eclipse.sirius.web.domain.DomainPackage;
import org.eclipse.sirius.web.domain.provider.DomainItemProviderAdapterFactory;
import org.eclipse.sirius.web.view.ViewFactory;
import org.eclipse.sirius.web.view.ViewPackage;
import org.eclipse.sirius.web.view.provider.ViewItemProviderAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Registers the EMF metamodels and stereotypes needed for studio authoring.
 *
 * @author pcdavid
 */
@Configuration
public class StudioConfiguration {

    public static final UUID EMPTY_DOMAIN_ID = UUID.nameUUIDFromBytes("empty_domain".getBytes()); //$NON-NLS-1$

    public static final String EMPTY_DOMAIN_LABEL = "Empty Domain Definition"; //$NON-NLS-1$

    public static final UUID EMPTY_VIEW_ID = UUID.nameUUIDFromBytes("empty_view".getBytes()); //$NON-NLS-1$

    public static final String EMPTY_VIEW_LABEL = "Empty View Definition"; //$NON-NLS-1$

    private final StereotypeBuilder stereotypeBuilder;

    private static final String TIMER_NAME = "siriusweb_studio_stereotype_load"; //$NON-NLS-1$

    public StudioConfiguration(MeterRegistry meterRegistry) {
        this.stereotypeBuilder = new StereotypeBuilder(TIMER_NAME, meterRegistry);
    }

    @Bean
    public EPackage domainEPackage() {
        return DomainPackage.eINSTANCE;
    }

    @Bean
    public AdapterFactory domainAdapterFactory() {
        return new DomainItemProviderAdapterFactory();
    }

    @Bean
    public EPackage viewEPackage() {
        return ViewPackage.eINSTANCE;
    }

    @Bean
    public AdapterFactory viewAdapterFactory() {
        return new ViewItemProviderAdapterFactory();
    }

    @Bean
    public IStereotypeDescriptionRegistryConfigurer studioStereotypesConfigurer() {
        return (IStereotypeDescriptionRegistry registry) -> {
            registry.add(new StereotypeDescription(EMPTY_DOMAIN_ID, EMPTY_DOMAIN_LABEL, this::getEmptyDomainContent));
            registry.add(new StereotypeDescription(EMPTY_VIEW_ID, EMPTY_VIEW_LABEL, this::getEmptyViewContent));
        };
    }

    private String getEmptyDomainContent() {
        Domain domain = DomainFactory.eINSTANCE.createDomain();
        domain.setName("Sample Domain"); //$NON-NLS-1$
        domain.setUri("domain://sample"); //$NON-NLS-1$
        return this.stereotypeBuilder.getStereotypeBody(domain);
    }

    private String getEmptyViewContent() {
        return this.stereotypeBuilder.getStereotypeBody(ViewFactory.eINSTANCE.createView());
    }
}
