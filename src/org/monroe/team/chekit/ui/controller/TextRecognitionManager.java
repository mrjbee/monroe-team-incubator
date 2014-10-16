package org.monroe.team.chekit.ui.controller;

import org.monroe.team.chekit.common.Closure;
import org.monroe.team.chekit.services.TextRecognitionCore;
import org.monroe.team.chekit.uc.presentations.Actions;
import org.monroe.team.chekit.uc.presentations.Screen;
import org.monroe.team.chekit.uc.presentations.StepRunRepresentation;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TextRecognitionManager {

    private final TextRecognitionCore core;

    public TextRecognitionManager(TextRecognitionCore core) {
        this.core = core;
    }

    public Prediction predict(final Screen screen, String userInput) {
        final Prediction response = new Prediction();
        if (userInput.length() == 0) return response;
        switch (screen){
            case STARTUP:
                core.checkAgainstAndCall(
                                userInput,
                                putInResponseIfMatch(response),
                                core.command_runSuite,
                                core.command_continueRun,
                                core.command_exit);
                List<String> runSuiteExtract = core.extractUsing(userInput,
                        core.extract_runSuite_file_as_first);
                if (!runSuiteExtract.isEmpty()){
                    pathSelection(
                            core.command_runSuite.command,
                            response.completions,
                            runSuiteExtract.get(0),".checkit");
                }

                runSuiteExtract = core.extractUsing(userInput,
                        core.extract_continueRun_file_as_first);
                if (!runSuiteExtract.isEmpty()){
                    pathSelection(
                            core.command_continueRun.command,
                            response.completions,
                            runSuiteExtract.get(0),".runit");
                }
                break;
            case RUNNER:
                    core.checkAgainstAndCall(
                            userInput,
                            putInResponseIfMatch(response),
                            core.command_save,
                            core.command_saveAs,
                            core.command_UpdateRunDetails,
                            core.command_UpdateStepComment,
                            core.command_goBack,
                            core.command_markAs,
                            core.command_pass,
                            core.command_skip,
                            core.command_fail,

                            core.command_markAs_failed,
                            core.command_markAs_passed,
                            core.command_markAs_skipped,

                            core.command_pass_andComment,
                            core.command_skip_andComment,
                            core.command_fail_andComment,

                            core.command_markAs_failed_andComment,
                            core.command_markAs_passed_andComment,
                            core.command_markAs_skipped_andComment);

                List<String> saveAsExtract = core.extractUsing(userInput,
                        core.extract_saveAs_file_as_first);
                if (!saveAsExtract.isEmpty()){
                    pathSelection(
                            core.command_saveAs.command,
                            response.completions,
                            saveAsExtract.get(0),".runit",".checkit");
                }
                break;
        }
        return response;
    }


    private Closure<Void, TextRecognitionCore.TextCommand> putInResponseIfMatch(final Prediction response) {
        return new Closure<Void, TextRecognitionCore.TextCommand>(){
            @Override
            public Void call(TextRecognitionCore.TextCommand textCommand) {
                response.completions.add(textCommand.command);
                return null;
            }
        };
    }

    private void pathSelection(String predictionPrefix, List<String> response, String supposedText, final String... fileExt) {

        if (supposedText.isEmpty() || supposedText.endsWith(" ") || endsWith(supposedText, fileExt)){
            return;
        }

        File origFile = new File(supposedText);
        File folder = null;
        String mask = null;
        if (supposedText.endsWith("/") || supposedText.endsWith("\\")){
            folder = origFile;
        } else {
            folder = origFile.getParentFile();
            mask = origFile.getName();
        }

        if (folder == null) return;

        final String finalMask = mask;
        File[] suggestions = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String simpleName = pathname.getName();
                if (!pathname.isDirectory() && !(endsWith(simpleName,fileExt))){
                    return false;
                }
                if(finalMask != null && !simpleName.toLowerCase().startsWith(finalMask.toLowerCase())){
                    return false;
                }
                return true;
            }
        });
        if (suggestions == null) return;
        for (File suggestion : suggestions) {
            if (suggestion.isDirectory()){
                response.add(predictionPrefix+suggestion.getPath()+ File.separator);
            }else {
                response.add(predictionPrefix+suggestion.getPath());
            }
        }
    }

    private boolean endsWith(String supposedText, String[] fileExt) {
        for (String ext : fileExt) {
            if (supposedText.endsWith(ext)) return true;
        }
        return false;
    }

    public Actions.Action parse(final Screen screen,final String userInput, final Map<String,Object> dataMap) {
        switch (screen){
            case STARTUP:

                if (core.checkExactAgainst(
                        userInput,
                        core.command_exit)){
                    return new Actions.Application.QuitApplication();
                }

                List<String> extractedRunAs = core.extractUsing(userInput, core.extract_runSuite_file_as_first);
                if (!extractedRunAs.isEmpty()){
                    File file = new File(extractedRunAs.get(0).trim());
                    if (!file.exists()){
                        return new Actions.Toast("File not exists");
                    }

                    if (file.isDirectory()){
                        return new Actions.Toast("Please select file not an directory");
                    }

                    return new Actions.Suite.OpenCheckSuite(file);
                }
                extractedRunAs = core.extractUsing(userInput, core.extract_continueRun_file_as_first);
                if (!extractedRunAs.isEmpty()){
                    File file = new File(extractedRunAs.get(0).trim());
                    if (!file.exists()){
                        return new Actions.Toast("File not exists");
                    }

                    if (file.isDirectory()){
                        return new Actions.Toast("Please select file not an directory");
                    }

                    return new Actions.Suite.OpenRun(file);
                }
                break;
            case RUNNER:


                if (core.checkExactAgainst(
                        userInput,
                        core.command_save)){
                    return new Actions.Suite.SaveRun(null);
                }

                if (core.checkExactAgainst(
                        userInput,
                        core.command_goBack)){
                    return new Actions.Application.ChangeScreen(Screen.STARTUP);
                }

                if (core.checkExactAgainst(
                        userInput,
                        core.command_UpdateRunDetails)){
                    return new Actions.Suite.UpdateRunDetails();
                }


                if (core.checkExactAgainst(
                        userInput,
                        core.command_UpdateStepComment)){
                    return new Actions.Suite.UpdateStepComment();
                }

                Actions.Action resultAction = runSuiteRunStepUpdate(userInput);
                if (resultAction != null) return resultAction;

                List<String> extractedSaveAs = core.extractUsing(userInput, core.extract_saveAs_file_as_first);
                if (!extractedSaveAs.isEmpty()){
                    File file = new File(extractedSaveAs.get(0).trim());
                    if (file.isDirectory()){
                        return new Actions.Toast("Please select file not an directory");
                    }
                    return new Actions.Suite.SaveRun(extractedSaveAs.get(0).trim());
                }
        }
        return null;
    }


    private Actions.Action runSuiteRunStepUpdate(String userInput){
        Actions.Action resultAction = null;
        Pattern[] patterns = {
                //comment
                core.extract_markAs_status_as_first_and_comment_as_second,
                core.extract_markAs_short_status_as_first_and_comment_as_second,
                //status only
                core.extract_markAs_status_as_first,
                core.extract_markAs_short_status_as_first
        };

        for (Pattern pattern:patterns){
            resultAction = checkIfExtracted(userInput,pattern,
                    new Closure<Actions.Action, List<String>>() {
                        @Override
                        public Actions.Action call(List<String> strings) {
                            if (strings.size() == 1){
                                return new Actions.Suite.UpdateRunStatusRequest(convertType(strings.get(0)),null);
                            }
                            return new Actions.Suite.UpdateRunStatusRequest(convertType(strings.get(0)),strings.get(1));
                        }
                    });
            if (resultAction != null){
                return resultAction;
            }
        }
        return resultAction;
    }


    private StepRunRepresentation.Type convertType(String byName) {
        return core.typeByHuman(byName);
    }

    private Actions.Action checkIfExtracted(String text, Pattern extractionPattern, Closure<Actions.Action,List<String>> actionBuildClosure){
        List<String> extracted = core.extractUsing(text, extractionPattern);
        if (!extracted.isEmpty()){
            return actionBuildClosure.call(extracted);
        }
        return null;
    }

    public static class Prediction {
        public List<String> completions = new ArrayList<>();
    }
}
