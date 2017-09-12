package com.company.locators;

import com.company.DebugLog;
import com.company.JsonManipulator;
import com.company.models.ConfigSettings;
import com.company.models.IngredientValues;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * User: Jack's Computer
 * Date: 09/11/2017
 * Time: 1:28 PM
 */
public class IngredientLocator {
    private Hashtable<String, IngredientValues> _itemValues;
    private JsonManipulator _manipulator;
    private ConfigSettings _settings;
    private DebugLog _log;

    public IngredientLocator(DebugLog log, ConfigSettings settings, JsonManipulator manipulator) {
        _log = log;
        _settings = settings;
        _manipulator = manipulator;
        _itemValues = new Hashtable<String, IngredientValues>();
    }

    public IngredientValues getValuesOf(String itemName) {
        if(_itemValues.isEmpty()) {
            storeValues();
        }
        if(_itemValues.containsKey(itemName)) {
            return _itemValues.get(itemName);
        }
        return null;
    }

    public void updateValue(String itemName, IngredientValues values) {
        if(_itemValues.containsKey(itemName)) {
            _itemValues.remove(itemName);
        }
        _itemValues.put(itemName, values);
    }

    private void storeValues() {
        int locationCount = _settings.ingredientLocations.length;
        for(int i = 0; i < locationCount; i++) {
            String path = _settings.ingredientLocations[i];
            File directory = new File(path);
            findIngredients(directory);
        }
    }

    private void findIngredients(File directory) {
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
            if (file.isFile() && isFileIncluded(file.getName())){
                addIngredientValues(file.getAbsolutePath());
            }
            else if (file.isDirectory()){
                findIngredients(file);
            }
        }
    }

    private void addIngredientValues(String filePath) {
        try {
            IngredientValues ingredientValues = _manipulator.readIngredientVal(filePath);
            if(ingredientValues != null && ingredientValues.itemName != null) {
                String itemName = ingredientValues.itemName;
                if(!_itemValues.containsKey(itemName)) {
                    _itemValues.put(itemName, ingredientValues);
                }
            }
        }
        catch(IOException e) {
            _log.logDebug("{IOE] Problem encountered reading recipe at path: " + filePath + "\n" + e.getMessage());
        }
    }

    private boolean isFileIncluded(String fileName) {
        boolean isIncluded = false;
        String[] exclusionList = _settings.ingredientExtensionInclusionList;
        for(int i = 0; i < exclusionList.length; i++) {
            if(fileName.endsWith(exclusionList[i])) {
                isIncluded = true;
                i = exclusionList.length;
            }
        }
        return isIncluded;
    }
}