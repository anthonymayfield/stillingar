/*
 * Copyright 2011 the original author or authors.
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

package org.brekka.stillingar.core;

import java.util.List;

/**
 * Encapsulates the details of a configuration value that should be listened for, and the action that should be taken
 * when it is updated.
 * 
 * @author Andrew Taylor
 */
public class ValueDefinition<T, Listener extends ValueChangeListener<?>> {

    /**
     * The type of the value that is used to define what is expected to be returned. In the absence of an expression it
     * can also be used to determine what value will be returned. When actual return type is a {@link List}, this value
     * will be the type of value within the list.
     */
    private final Class<T> type;

    /**
     * The expression that should be used to determine what value is to be returned.
     */
    private final String expression;

    /**
     * The listener that will be called in response to this value being changed.
     */
    private final Listener listener;
    
    /**
     * Determines whether this value definition must configured. Can be set to false to indicate that this value
     * is optional, ie if no configuration is found, it will not be considered an error.
     */
    private boolean required = true;

    /**
     * @param type
     *            The type of the value that is used to define what is expected to be returned. In the absence of an
     *            expression it can also be used to determine what value will be returned. When actual return type is a
     *            {@link List}, this value will be the type of value within the list.
     * @param expression
     *            The expression that should be used to determine what value is to be returned.
     * @param listener
     *            The listener that will be called in response to this value being changed.
     */
    ValueDefinition(Class<T> type, String expression, Listener listener) {
        this.type = type;
        if (expression != null && expression.isEmpty()) {
            expression = null;
        }
        this.expression = expression;
        this.listener = listener;
    }

    /**
     * @param type
     *            The type of the value that is used to define what is expected to be returned. In the absence of an
     *            expression it can also be used to determine what value will be returned. When actual return type is a
     *            {@link List}, this value will be the type of value within the list.
     * @param listener
     *            The listener that will be called in response to this value being changed.
     */
    ValueDefinition(Class<T> type, Listener listener) {
        this(type, null, listener);
    }

    /**
     * The type of the value that is used to define what is expected to be returned. In the absence of an expression it
     * can also be used to determine what value will be returned. When actual return type is a {@link List}, this value
     * will be the type of value within the list.
     * 
     * @return
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * The expression that should be used to determine what value is to be returned.
     * @return
     */
    public String getExpression() {
        return expression;
    }

    /**
     * The listener that will be called in response to this value being changed.
     * @return
     */
    public Listener getChangeListener() {
        return listener;
    }
    
    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }
}
