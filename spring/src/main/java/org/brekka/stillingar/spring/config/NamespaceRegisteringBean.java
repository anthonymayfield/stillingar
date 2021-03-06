/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.stillingar.spring.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.dom.NamespaceAware;
import org.brekka.stillingar.spring.bpp.ConfigurationBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * A simple bean that registers the specified namespace uri and prefix on load. At this time it is abusing the {@link BeanPostProcessor}
 * mechanism in order to run before {@link ConfigurationBeanPostProcessor}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NamespaceRegisteringBean implements BeanPostProcessor, Ordered {

    private static final Log log = LogFactory.getLog(NamespaceRegisteringBean.class);
    
    private final NamespaceAware namespaceAware;
    private Map<String, String> namespaceToPrefixMap;
    
    /**
     * @param loader
     * @param uri
     * @param prefix
     */
    public NamespaceRegisteringBean(NamespaceAware namespaceAware, String uri, String prefix) {
        this(namespaceAware, asMap(uri, prefix));
    }

    /**
     * @param loader
     * @param uri
     * @param prefix
     */
    public NamespaceRegisteringBean(NamespaceAware namespaceAware, Map<String, String> namespaceToPrefixMap) {
        this.namespaceAware = namespaceAware;
        this.namespaceToPrefixMap = namespaceToPrefixMap;
    }
    

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (namespaceToPrefixMap != null) {
            Set<Entry<String,String>> entrySet = namespaceToPrefixMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
                if (log.isInfoEnabled()) {
                    log.info(String.format("Registering prefix '%s' to namespace '%s'", entry.getValue(), entry.getKey()));
                }
                namespaceAware.registerNamespace(entry.getValue(), entry.getKey());
            }
            namespaceToPrefixMap = null;
        }
        return bean;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.core.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        // Need to run before ConfigurationBeanPostProcessor
        return 9;
    }
    
    /**
     * @param uri
     * @param prefix
     * @return
     */
    private static Map<String, String> asMap(String uri, String prefix) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(uri, prefix);
        return map;
    }

}
