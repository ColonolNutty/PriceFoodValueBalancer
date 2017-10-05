package com.company.recipecreator;

import com.company.CNLog;
import com.company.JsonManipulator;
import com.company.StopWatchTimer;
import com.company.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * User: Jack's Computer
 * Date: 10/04/2017
 * Time: 9:43 AM
 */
public class MassRecipeCreator {

    private CNLog _log;
    private RecipeCreatorSettings _settings;
    private JsonManipulator _manipulator;
    private ArrayList<CNCrafter> _crafters;

    public MassRecipeCreator(RecipeCreatorSettings settings,
                             CNLog log) {
        _settings = settings;
        _log = log;
        _manipulator = new JsonManipulator(log);
        _crafters = new ArrayList<CNCrafter>();
        _crafters.add(new RecipeCrafter(log, settings, _manipulator));
        _crafters.add(new IngredientCrafter(log, settings, _manipulator));
    }

    public void create() {
        if(_settings == null) {
            _log.error("No configuration file found, exiting.");
            return;
        }
        StopWatchTimer timer = new StopWatchTimer(_log);
        timer.start("Running");

        IngredientListItem[] ingredientList = read(_settings.ingredientListFile, IngredientListItem[].class);
        if(ingredientList == null) {
            return;
        }

        ArrayList<String> outputNames = createFromTemplate(ingredientList);
        writeToConfigurationFile(outputNames);
        timer.logTime();
    }

    private void writeToConfigurationFile(ArrayList<String> names) {
        String recipeConfigFile = _settings.recipeConfigFileName;
        RecipesConfig recipesConfig = read(recipeConfigFile, RecipesConfig.class);
        if(recipesConfig == null) {
            recipesConfig = new RecipesConfig();
            recipesConfig.possibleOutput = _manipulator.createArrayNode();
        }
        for(String name : names) {
            if(!contains(recipesConfig.possibleOutput, name)) {
                recipesConfig.possibleOutput.add(name);
            }
        }
        _manipulator.writeNew(recipeConfigFile, recipesConfig);
    }

    private boolean contains(ArrayNode node, String name) {
        boolean contains = false;
        for(int i = 0; i < node.size(); i++){
            if(node.get(i).asText().equals(name)) {
                contains = true;
                i = node.size();
            }
        }
        return contains;
    }

    public ArrayList<String> createFromTemplate(IngredientListItem[] ingredientNames) {
        int numberPerRecipe = _settings.numberOfIngredientsPerRecipe;

        _log.startSubBundle("Ingredients");
        ArrayList<String> newNames = createIngredients(ingredientNames, new ArrayList<IngredientListItem>(), -1, numberPerRecipe);
        _log.endSubBundle();
        return newNames;
    }

    private ArrayList<String> createIngredients(IngredientListItem[] ingredientList,
                                   ArrayList<IngredientListItem> currentIngredients,
                                   int currentIngredientIndex,
                                   int ingredientsLeft) {
        ArrayList<String> names = new ArrayList<String>();
        if(ingredientsLeft == 0) {
            return names;
        }
        for(int i = currentIngredientIndex + 1; i < ingredientList.length; i++) {
            ArrayList<IngredientListItem> ingredients = new ArrayList<IngredientListItem>();
            ingredients.addAll(currentIngredients);
            IngredientListItem nextIngredient = ingredientList[i];
            _log.startSubBundle(nextIngredient.name);
            ingredients.add(nextIngredient);
            if(ingredientsLeft != 0) {
                names.addAll(createIngredients(ingredientList, ingredients, i, ingredientsLeft - 1));
            }

            String outputName = _settings.filePrefix;
            for(IngredientListItem ingred : ingredients) {
                outputName += ingred.shortName;
            }
            outputName += _settings.fileSuffix;
            for(CNCrafter crafter : _crafters) {
                crafter.craft(outputName, ingredients, _settings.countPerIngredient);
            }
            names.add(outputName);
            _log.endSubBundle();
        }
        return names;
    }

    private <T> T read(String path, Class<T> classOfT){
        try {
            return _manipulator.read(path, classOfT);
        }
        catch(IOException e) {
            _log.error("[IOE] Failed to read: " + path, e);
        }
        return null;
    }
}
