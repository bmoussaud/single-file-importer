/*
 * @(#)Predicates.java     2 Oct 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.plugin.api.util;

import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.DESTROY;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getSubtypes;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Deployed;

public class Predicates {
    
    public static Predicate<Type> subtypeOf(Type type) {
        return new IsSubtypeOf(type);
    }
    
    public static Predicate<ConfigurationItem> instanceOf(Type type) {
        return com.google.common.base.Predicates.compose(subtypeOf(type), 
                new Function<ConfigurationItem, Type>() {
                    @Override
                    public Type apply(ConfigurationItem input) {
                        return input.getType();
                    }
                });
    }
    
    public static Predicate<Delta> deltaOf(Type type) {
        return com.google.common.base.Predicates.compose(instanceOf(type), 
                extractDeployed());
    }
    
    public static Function<Delta, Deployed<?, ?>> extractDeployed() {
        return new ExtractDeployed();
    }
    
    public static Predicate<Delta> operationIs(Operation operationToMatch) {
        return new OperationEquals(operationToMatch);
    }
    
    private static class OperationEquals implements Predicate<Delta> {
        private final Operation operationToMatch;
        
        protected OperationEquals(Operation operationToMatch) {
            this.operationToMatch = operationToMatch;
        }

        @Override
        public boolean apply(Delta input) {
            return input.getOperation().equals(operationToMatch);
        }
    }
    
    private static class IsSubtypeOf implements Predicate<Type> {
        private final Collection<Type> subtypes;
        
        public IsSubtypeOf(Type typeToMatch) {
            subtypes = getSubtypes(typeToMatch);
            subtypes.add(typeToMatch);
        }

        @Override
        public boolean apply(Type input) {
            return subtypes.contains(input);
        }
    }
    
    private static class ExtractDeployed implements Function<Delta, Deployed<?, ?>> {
        @Override
        public Deployed<?, ?> apply(Delta input) {
            return (input.getOperation().equals(DESTROY) 
                    ? input.getPrevious() 
                    : input.getDeployed());
        }
    }
}
