/*
 * Copyright 2010-2019 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.schema.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.SchemaVertexTypeUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * /**
 * This describes a type of attribute whose values are {@link SchemaVertexType}
 * objects.
 * <p>
 * Attribute values can be set either directly using
 * {@link #setObject setObject()} or using {@link #setString setString()} which
 * will try to resolve the type by name using the schema. See {@link #stringify}
 * for more information.
 * <p>
 * Attribute values can be retrieved either directly using
 * {@link #getObject getObject()} or using {@link #getString getString()} which
 * will utilise {@link SchemaVertexType#getName SchemaVertexType.getName()}.
 *
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class VertexTypeAttributeDescription extends AbstractObjectAttributeDescription<SchemaVertexType> {

    public static final String ATTRIBUTE_NAME = "vertex_type";
    public static final Class<SchemaVertexType> NATIVE_CLASS = SchemaVertexType.class;
    public static final SchemaVertexType DEFAULT_VALUE = null;

    public VertexTypeAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected SchemaVertexType convertFromString(final String value) {
        if (value == null) {
            return defaultValue;
        } else {
            return SchemaVertexTypeUtilities.getTypeOrBuildNew(value);
        }
    }

    @Override
    public void setDefault(final Object value) throws IllegalArgumentException {
        super.setDefault(value);

        // Ensure that the default is a registered type.
        if (defaultValue != null && defaultValue instanceof SchemaVertexType
                && ((SchemaVertexType) defaultValue).isIncomplete()) {
            defaultValue = DEFAULT_VALUE;
        }
    }
}
