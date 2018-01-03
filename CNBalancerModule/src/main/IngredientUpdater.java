package main;

import com.colonolnutty.module.shareddata.debug.CNLog;
import com.colonolnutty.module.shareddata.utils.CNFileUtils;
import com.colonolnutty.module.shareddata.JsonManipulator;
import com.colonolnutty.module.shareddata.locators.IngredientStore;
import main.settings.BalancerSettings;
import com.colonolnutty.module.shareddata.models.Ingredient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * User: Jack's Computer
 * Date: 09/12/2017
 * Time: 11:25 AM
 */
public class IngredientUpdater {
    protected CNLog _log;
    protected BalancerSettings _settings;
    protected JsonManipulator _manipulator;
    protected IngredientStore _ingredientStore;
    protected IngredientDataCalculator _ingredientDataCalculator;
    protected ArrayList<String> _fileTypesIgnoreFoodValues;

    public IngredientUpdater(CNLog log,
                             BalancerSettings settings,
                             JsonManipulator manipulator,
                             IngredientStore ingredientStore,
                             IngredientDataCalculator ingredientDataCalculator) {
        _log = log;
        _settings = settings;
        _manipulator = manipulator;
        _ingredientStore = ingredientStore;
        _ingredientDataCalculator = ingredientDataCalculator;
        _fileTypesIgnoreFoodValues = new ArrayList<String>();
        _fileTypesIgnoreFoodValues.add(".item");
        _fileTypesIgnoreFoodValues.add(".object");
        _fileTypesIgnoreFoodValues.add(".projectile");
        _fileTypesIgnoreFoodValues.add(".matitem");
        _fileTypesIgnoreFoodValues.add(".liquid");
        _fileTypesIgnoreFoodValues.add(".liqitem");
        _fileTypesIgnoreFoodValues.add(".material");
    }

    public String update(String ingredientFilePath) {
        try {
            File ingredientFile = new File(ingredientFilePath);
            String messageOne = "Calculating values for: " + ingredientFile.getName();
            _log.startSubBundle(messageOne);
            _log.debug(messageOne);
            Ingredient ingredient = _ingredientStore.getIngredientWithFilePath(ingredientFilePath);
            if(ingredient == null) {
                _log.debug("No ingredient found in store for: " + ingredientFilePath);
                return null;
            }
            Ingredient updatedIngredient = _ingredientDataCalculator.updateIngredient(ingredient);
            Ingredient originalIngredient = _manipulator.readIngredient(ingredientFilePath);
            if(!_settings.forceUpdate
                    && meetsMinimumValues(updatedIngredient)
                    && ingredientsAreEqual(originalIngredient, updatedIngredient)) {
                _log.debug("    Skipping, values were the same as the ingredient on disk: " + ingredientFile.getName());
                return null;
            }
            return ingredient.getName();
        }
        catch(IOException e) {
            _log.error("[IOE] While attempting to update: " + ingredientFilePath, e);
        }
        finally {
            _log.endSubBundle();
        }
        return null;
    }

    private boolean ingredientsAreEqual(Ingredient one, Ingredient two) {
        if(one == null || two == null) {
            return true;
        }
        String filePathToCheck = one.filePath;
        if(filePathToCheck == null) {
            filePathToCheck = two.filePath;
        }
        if(filePathToCheck == null) {
            return true;
        }
        if(CNFileUtils.fileEndsWith(filePathToCheck, _fileTypesIgnoreFoodValues)
                || CNFileUtils.fileEndsWith(filePathToCheck, _fileTypesIgnoreFoodValues)) {
            _log.debug("Comparing using only price: " + one.getName(), 4);
            return one.priceEquals(two);
        }
        boolean shouldCheckOther = filePathToCheck.endsWith(".consumable") || filePathToCheck.endsWith(".consumable");
        if(!shouldCheckOther) {
            return true;
        }
        _log.debug("Comparing using both price, foodValue, and effects: " + one.getName(), 4);
        return one.equals(two) && one.effectsAreEqual(two.effects);
    }

    private boolean meetsMinimumValues(Ingredient ingredient) {
        if(ingredient.foodValue == null) {
            return true;
        }
        return ingredient.foodValue >= _settings.minimumFoodValue;
    }
}
