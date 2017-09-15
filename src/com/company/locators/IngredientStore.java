package com.company.locators;

import com.company.DebugLog;
import com.company.JsonManipulator;
import com.company.models.ConfigSettings;
import com.company.models.Ingredient;
import com.company.models.IngredientOverrides;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * User: Jack's Computer
 * Date: 09/11/2017
 * Time: 1:28 PM
 */
public class IngredientStore {
    private Hashtable<String, Ingredient> _ingredients;
    private JsonManipulator _manipulator;
    private ConfigSettings _settings;
    private DebugLog _log;
    private PatchLocator _patchLocator;

    public IngredientStore(DebugLog log, ConfigSettings settings, JsonManipulator manipulator, PatchLocator patchLocator) {
        _ingredients = new Hashtable<String, Ingredient>();
        _log = log;
        _settings = settings;
        _manipulator = manipulator;
        _patchLocator = patchLocator;
        initializeIngredientStore();
    }

    public void overrideIngredients(Ingredient[] ingredients) {
        if(ingredients == null) {
            return;
        }
        for(int i = 0; i < ingredients.length; i++) {
            Ingredient ingredient = ingredients[i];
            _log.logDebug("Overriding ingredient: " + ingredient.getName() + " with p: " + ingredient.price + " and fv: " + ingredient.foodValue);
            updateIngredient(ingredient, true);
        }
    }

    public Ingredient getIngredient(String itemName) {
        if(_ingredients.isEmpty()) {
            initializeIngredientStore();
        }
        if(_ingredients.containsKey(itemName)) {
            return _ingredients.get(itemName);
        }
        else {
            Ingredient ingredient = new Ingredient(itemName);
            _ingredients.put(itemName, ingredient);
            return ingredient;
        }
    }

    public Ingredient getIngredientWithFilePathAndPatch(String filePath) {
        Enumeration<Ingredient> ingredients = _ingredients.elements();
        Ingredient found = null;
        while(found == null && ingredients.hasMoreElements()) {
            Ingredient ingredient = ingredients.nextElement();
            if(ingredient.patchFile != null && ingredient.filePath.equals(filePath)) {
                found = ingredient;
            }
        }
        return found;
    }

    public void updateIngredient(Ingredient ingredient) {
        if(ingredient == null) {
            return;
        }
        updateIngredient(ingredient, true);
    }

    private void initializeIngredientStore() {
        _log.logInfo("Loading ingredients from disk");
        ArrayList<String> filePaths = getIngredientPaths();
        for(int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            if(!filePath.endsWith(".patch")) {
                String patchFile = _patchLocator.locatePatchFileFor(filePath, filePaths);
                addIngredient(filePath, patchFile);
            }
        }
        overrideIngredients(readOverrides());
    }

    private ArrayList<String> getIngredientPaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for(int i = 0; i < _settings.ingredientLocations.length; i++) {
            String path = _settings.ingredientLocations[i];
            File directory = new File(path);
            ArrayList<String> foundFilePaths = findIngredients(directory);
            filePaths.addAll(foundFilePaths);
        }
        return filePaths;
    }

    private ArrayList<String> findIngredients(File directory) {
        ArrayList<String> filePaths = new ArrayList<String>();
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
            if (file.isFile() && isFileIncluded(file.getName())){
                filePaths.add(file.getAbsolutePath());
            }
            else if (file.isDirectory()){
                ArrayList<String> foundFilePaths = findIngredients(file);
                filePaths.addAll(foundFilePaths);
            }
        }
        return filePaths;
    }

    private void addIngredient(String filePath, String patchFilePath) {
        try {
            _log.logDebug("File found at: " + filePath);
            Ingredient ingredient = _manipulator.readIngredient(filePath);
            if(ingredient != null && ingredient.hasName()) {
                ingredient.filePath = filePath;
                ingredient.patchFile = patchFilePath;
                String itemName = ingredient.getName();
                if(!_ingredients.containsKey(itemName)) {
                    Ingredient patchedIngredient = _manipulator.patch(ingredient, patchFilePath, Ingredient.class);
                    _log.logDebug("Pre-patch ingredient values: " + itemName + " p: " + ingredient.price + " fv: " + ingredient.foodValue);
                    if(patchedIngredient != null) {
                        patchedIngredient.filePath = filePath;
                        patchedIngredient.patchFile = patchFilePath;
                        updateIngredient(patchedIngredient, true);
                    }
                    else {
                        updateIngredient(ingredient, false);
                    }
                }
                else {
                    updateIngredient(ingredient, false);
                }
            }
        }
        catch(IOException e) {
            _log.logDebug("{IOE] Problem encountered reading recipe at path: " + filePath + "\n" + e.getMessage());
        }
    }

    private void updateIngredient(Ingredient ingredient, boolean isOverride) {
        String ingredientName = ingredient.getName();
        if(_ingredients.containsKey(ingredientName)) {
            Ingredient existing = _ingredients.get(ingredientName);
            if(ingredient.foodValue != null && (isOverride || existing.foodValue == null)) {
                _log.logDebug("Overriding ingredient foodValue: " + existing.getName() + " with " + ingredient.foodValue);
                existing.foodValue = ingredient.foodValue;
            }
            if(ingredient.price != null && (isOverride || existing.price == null)) {
                _log.logDebug("Overriding ingredient price: " + existing.getName() + " with " + ingredient.price);
                existing.price = ingredient.price;
            }
            _log.logDebug("New values: " + existing.getName() + " p: " + existing.price + " fv: " + existing.foodValue);
            existing.itemName = ingredient.itemName;
            existing.objectName = ingredient.objectName;
            existing.description = ingredient.description;
            existing.inventoryIcon = ingredient.inventoryIcon;
            existing.shortdescription = ingredient.shortdescription;
            existing.stages = ingredient.stages;
            existing.interactData = ingredient.interactData;
            _ingredients.put(existing.getName(), existing);
        }
        else {
            _log.logDebug("No ingredient found, so adding: " + ingredientName + " p: " + ingredient.price + " fv: " + ingredient.foodValue);
            _ingredients.put(ingredientName, ingredient);
        }
    }

    private boolean isFileIncluded(String fileName) {
        boolean isIncluded = false;
        String[] exclusionList = _settings.ingredientExtensionInclusionList;
        for(int i = 0; i < exclusionList.length; i++) {
            if(fileName.endsWith(".patch") || fileName.endsWith(exclusionList[i])) {
                isIncluded = true;
                i = exclusionList.length;
            }
        }
        return isIncluded;
    }

    private Ingredient[] readOverrides() {
        try {
            IngredientOverrides replacementIngredientValues = _manipulator.read(_settings.ingredientValueOverridePath, IngredientOverrides.class);
            return replacementIngredientValues.ingredients;
        }
        catch(FileNotFoundException e) { }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
