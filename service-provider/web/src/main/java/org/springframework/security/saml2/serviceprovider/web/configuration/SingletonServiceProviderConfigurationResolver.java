/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.security.saml2.serviceprovider.web.configuration;

import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.saml2.configuration.HostedSaml2ServiceProviderConfiguration;
import org.springframework.security.saml2.configuration.HostedSaml2ServiceProviderConfiguration.Builder;
import org.springframework.security.saml2.serviceprovider.Saml2ServiceProviderConfigurationResolver;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.hasText;

public class SingletonServiceProviderConfigurationResolver
	implements Saml2ServiceProviderConfigurationResolver<HttpServletRequest> {

	public static SingletonServiceProviderConfigurationResolver fromConfiguration(Consumer<Builder> config) {
		Builder builder = HostedSaml2ServiceProviderConfiguration.builder();
		config.accept(builder);
		return fromConfiguration(builder.build());
	}

	public static SingletonServiceProviderConfigurationResolver fromConfiguration(
		HostedSaml2ServiceProviderConfiguration c) {
		return new SingletonServiceProviderConfigurationResolver(c);
	}

	private final HostedSaml2ServiceProviderConfiguration configuration;

	public SingletonServiceProviderConfigurationResolver(HostedSaml2ServiceProviderConfiguration configuration) {
		this.configuration = configuration;
		notNull(configuration, "HostedServiceProviderConfiguration must not be null");
		notNull(configuration.getPathPrefix(), "HostedServiceProviderConfiguration.pathPrefix must not be null");
	}

	@Override
	public String getConfiguredPathPrefix() {
		return configuration.getPathPrefix();
	}

	@Override
	public HostedSaml2ServiceProviderConfiguration getConfiguration(HttpServletRequest request) {
		Builder builder =
			HostedSaml2ServiceProviderConfiguration.builder(configuration);
		if (request != null) {
			String basePath = getBasePath(request, false);
			if (!hasText(configuration.getEntityId())) {
				builder.entityId(basePath);
			}
			if (!hasText(configuration.getAlias())) {
				builder.alias(request.getServerName());
			}
			if (!hasText(configuration.getBasePath())) {
				builder.basePath(basePath);
			}
		}
		return builder.build();
	}

	private String getBasePath(HttpServletRequest request, boolean includeStandardPorts) {
		boolean includePort = true;
		if (443 == request.getServerPort() && "https".equals(request.getScheme())) {
			includePort = includeStandardPorts;
		}
		else if (80 == request.getServerPort() && "http".equals(request.getScheme())) {
			includePort = includeStandardPorts;
		}
		return request.getScheme() +
			"://" +
			request.getServerName() +
			(includePort ? (":" + request.getServerPort()) : "") +
			request.getContextPath();
	}

}
