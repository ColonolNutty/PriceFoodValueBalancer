package com.company.locators;

import com.company.DebugLog;
import com.company.models.ConfigSettings;

import java.io.File;
import java.util.ArrayList;

/**
 * User: Jack's Computer
 * Date: 09/16/2017
 * Time: 11:11 AM
 */
public class FileLocator {

    private DebugLog _log;
    private ConfigSettings _settings;
    private ArrayList<String> _includedFileTypes;
    private ArrayList<String> _savedFileNames;

    public FileLocator(DebugLog log,
                       ConfigSettings settings) {
        _log = log;
        _settings = settings;
        _includedFileTypes = new ArrayList<String>();
        _includedFileTypes.add(".item");
        _includedFileTypes.add(".consumable");
        _includedFileTypes.add(".patch");
        _includedFileTypes.add(".object");
        _includedFileTypes.add(".matitem");
        _includedFileTypes.add(".liquid");
        _includedFileTypes.add(".projectile");
        _includedFileTypes.add(".recipe");
    }

    public ArrayList<String> getFilePaths() {
        if(_savedFileNames != null) {
            return _savedFileNames;
        }
        ArrayList<String> filePaths = new ArrayList<String>();
        for(int i = 0; i < _settings.locationsToUpdate.length; i++) {
            File directory = new File(_settings.locationsToUpdate[i]);
            ArrayList<String> subFilePaths = getFilePaths(directory);
            filePaths.addAll(subFilePaths);
        }
        for(int i = 0; i < _settings.includeLocations.length; i++) {
            File directory = new File(_settings.includeLocations[i]);
            ArrayList<String> subFilePaths = getFilePaths(directory);
            filePaths.addAll(subFilePaths);
        }
        _savedFileNames = filePaths;
        return _savedFileNames;
    }

    private ArrayList<String> getFilePaths(File directory) {
        ArrayList<String> filePaths = new ArrayList<String>();
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
            if (file.isFile() && isValidFileType(file.getName())){
                filePaths.add(file.getAbsolutePath());
            }
            else if (file.isDirectory()){
                ArrayList<String> subPaths = getFilePaths(file);
                for(int i = 0; i < subPaths.size(); i++) {
                    filePaths.add(subPaths.get(i));
                }
            }
        }
        return filePaths;
    }

    private boolean isValidFileType(String fileName) {
        boolean included = false;
        for(int i = 0; i < _includedFileTypes.size(); i++){
            String fileType = _includedFileTypes.get(i);
            if(fileName.endsWith(fileType)) {
                included = true;
                i = _includedFileTypes.size();
            }
        }
        return included;
    }
}
