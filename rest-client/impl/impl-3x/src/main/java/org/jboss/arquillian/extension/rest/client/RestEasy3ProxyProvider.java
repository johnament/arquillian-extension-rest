/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.extension.rest.client;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.lang.annotation.Annotation;

/**
 * A provider implementation for RestEasy 3.x proxies.
 *
 */
public class RestEasy3ProxyProvider extends BaseRestProvider {

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        Class<?> classToProxy = super.getClassToProvide();
        Client client = ResteasyClientBuilder.newClient();
        WebTarget webTarget = client.target(getBaseURL() + super.getContextRoot(arquillianResource,annotations));
        ResteasyWebTarget resteasyWebTarget = (ResteasyWebTarget) webTarget;
        final ProxyBuilder<?> proxyBuilder = resteasyWebTarget.proxyBuilder(classToProxy);
        final String consumes = super.getConsumes(annotations);
        final String produces = super.getProduces(annotations);
        if (null != consumes ) {
            proxyBuilder.defaultConsumes(consumes);
        }
        if (null != produces) {
            proxyBuilder.defaultProduces(produces);
        }
        return proxyBuilder.build();
    }
}
