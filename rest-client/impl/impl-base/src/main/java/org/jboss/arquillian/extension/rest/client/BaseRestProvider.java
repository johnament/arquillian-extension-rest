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

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: johndament
 * Date: 11/11/13
 * Time: 8:45 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseRestProvider implements ResourceProvider {

    private static final String DEFAULT_VALUE = "__default__";

    private Class<?> aClassToProvide;

    @Inject
    private Instance<ProtocolMetaData> metaDataInst;


    protected boolean isProxyable() {
        final Class<?> aClass = this.getClassToProvide();
        if(aClass.getAnnotation(Path.class) != null) {
            return true;
        }
        for(Method m : aClass.getMethods()) {
            if(m.getAnnotation(Path.class) != null) {
                return true;
            }
            else if(m.getAnnotation(GET.class) != null) {
                return true;
            }
            else if(m.getAnnotation(PUT.class) != null) {
                return true;
            }
            else if(m.getAnnotation(POST.class) != null) {
                return true;
            }
            else if(m.getAnnotation(DELETE.class) != null) {
                return true;
            }
        }
        return false;
    }


    protected String getContextRoot(final ArquillianResource arquillianResource, Annotation... otherAnnotations) {
        final Class<?> annotated = arquillianResource.value();
        final RestClient restClient = this.getRestClient(otherAnnotations);
        final ApplicationPath applicationPath = this.getApplicationPath(annotated);
        String path = "/";
        if(restClient != null && !DEFAULT_VALUE.equals(restClient.contextRoot())) {
            path = restClient.contextRoot();
        }
        else if(restClient != null && !DEFAULT_VALUE.equals(restClient.value())) {
            path = restClient.value();
        }
        else if(applicationPath != null) {
            path = applicationPath.value();
        }
        return path;
    }

    protected String getConsumes(final Annotation... otherAnnotations) {
        final RestClient restClient = this.getRestClient(otherAnnotations);
        String consumes = null;
        if(restClient != null && !DEFAULT_VALUE.equals(restClient.consumes())) {
            consumes = restClient.consumes();
        }
        return consumes;
    }

    protected String getProduces(final Annotation... otherAnnotations) {
        final RestClient restClient = this.getRestClient(otherAnnotations);
        String produces = null;
        if(restClient != null && !DEFAULT_VALUE.equals(restClient.produces())) {
            produces = restClient.produces();
        }
        return produces;
    }

    protected ApplicationPath getApplicationPath(final Class<?> annotated) {
        return annotated.getAnnotation(ApplicationPath.class);
    }

    protected RestClient getRestClient(final Annotation... annotations) {
        for(Annotation a : annotations) {
            if(a instanceof RestClient) {
                return (RestClient)a;
            }
        }
        return null;
    }

    protected Class<?> getClassToProvide() {
        return aClassToProvide;
    }

    protected boolean allInSameContext(List<Servlet> servlets) {
        Set<String> context = new HashSet<String>();
        for (Servlet servlet : servlets) {
            context.add(servlet.getContextRoot());
        }
        return context.size() == 1;
    }

    protected URI getBaseURL() {
        HTTPContext context = metaDataInst.get().getContext(HTTPContext.class);
        if (allInSameContext(context.getServlets())) {
            return context.getServlets().get(0).getBaseURI();
        }
        throw new IllegalStateException("No baseURL found in HTTPContext");
    }

    /**
     * Whether or not the class can be provided.
     * This is for non proxy based {@see ArquillianResource} implementations.
     *
     * @return
     */
    protected boolean isProvidable(){
        return false;
    }

    @Override
    public final boolean canProvide(Class<?> aClass) {
        this.aClassToProvide = aClass;
        if(isProxyable()) {
            return true;
        }
        else {
            return isProvidable();
        }
    }
}
