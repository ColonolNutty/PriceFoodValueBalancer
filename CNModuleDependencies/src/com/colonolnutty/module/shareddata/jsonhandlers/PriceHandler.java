package com.colonolnutty.module.shareddata.jsonhandlers;

import com.colonolnutty.module.shareddata.DefaultNodeProvider;
import com.colonolnutty.module.shareddata.models.Ingredient;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * User: Jack's Computer
 * Date: 12/18/2017
 * Time: 3:15 PM
 */
public class PriceHandler extends DefaultNodeProvider implements IJsonHandler {
    private String _pathName;

    public PriceHandler() {
        super();
        _pathName = "price";
    }

    @Override
    public JsonNode createTestNode(Ingredient ingredient) {
        if(ingredient.price == null || ingredient.price < 0.0) {
            return null;
        }
        return _nodeProvider.createTestAddDoubleNode(_pathName);
    }

    @Override
    public JsonNode createReplaceNode(Ingredient ingredient) {
        return _nodeProvider.createReplaceDoubleNode(_pathName, ingredient.price);
    }

    @Override
    public boolean canHandle(String pathName) {
        return pathName.equals(_pathName);
    }

    @Override
    public boolean needsUpdate(JsonNode node, Ingredient ingredient) {
        if(node == null || ingredient.price == null) {
            return false;
        }

        if(node.isArray()) {
            return false;
        }

        if(!node.has("value")) {
            return false;
        }

        Double nodeVal = node.get("value").asDouble();
        return nodeVal != ingredient.price;
    }
}