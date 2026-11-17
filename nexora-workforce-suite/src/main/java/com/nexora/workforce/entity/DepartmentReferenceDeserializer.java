package com.nexora.workforce.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class DepartmentReferenceDeserializer extends JsonDeserializer<Department> {

    @Override
    public Department deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        Department department = new Department();

        if (node.isTextual()) {
            department.setName(node.textValue());
            return department;
        }

        if (!node.isObject()) {
            throw JsonMappingException.from(
                    parser, "department must be a department name or an object");
        }

        JsonNode id = node.get("id");
        if (id != null && !id.isNull()) {
            if (!id.canConvertToLong()) {
                throw JsonMappingException.from(parser, "department id must be a number");
            }
            department.setId(id.longValue());
        }

        JsonNode name = node.get("name");
        if (name != null && !name.isNull()) {
            if (!name.isTextual()) {
                throw JsonMappingException.from(parser, "department name must be a string");
            }
            department.setName(name.textValue());
        }

        return department;
    }
}
