package org.monroe.team.chekit.services;

import org.monroe.team.chekit.common.Closure;
import org.monroe.team.chekit.uc.presentations.StepRunRepresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextRecognitionCore {

    public Pattern extract_markAs_status_as_first = buildSimple("[ ]*mark as: (passed|failed|skipped)[ ]*");
    public Pattern extract_markAs_status_as_first_and_comment_as_second = buildSimple("[ ]*mark as: (passed|failed|skipped) and comment: (.*)");
    public Pattern extract_markAs_short_status_as_first = buildSimple("[ ]*(pass|fail|skip)[ ]*");
    public Pattern extract_markAs_short_status_as_first_and_comment_as_second = buildSimple("[ ]*(pass|fail|skip) and comment: (.*)");
    public Pattern extract_runSuite_file_as_first = buildSimple("[ ]*run suite:[ ]*(.*)");
    public Pattern extract_continueRun_file_as_first = buildSimple("[ ]*continue run:[ ]*(.*)");
    public Pattern extract_saveAs_file_as_first = buildSimple("[ ]*save as:[ ]*(.*)");

    public TextCommand command_save = buildTextCommand(
            "save",
            ptrns(
                    buildForMatching("[ ]*","save"),
                    buildSimple(" ")
            ),
            ptrns(
                    buildSimple("[ ]*save[ ]*")
            )
    );


    public TextCommand command_saveAs = buildTextCommand(
            "save as:",ptrns(
                    buildForMatching("[ ]*save"," as:"),
                    buildSimple(" "))
    );

    public TextCommand command_UpdateRunDetails = buildTextCommand(
            "update run details",
            ptrns(
                    buildForMatching("[ ]*","update run details"),
                    buildSimple(" ")
            ),
            ptrns(
                    buildSimple("[ ]*update run details[ ]*")
            )
    );


    public TextCommand command_UpdateStepComment = buildTextCommand(
            "update step comment",
            ptrns(
                    buildForMatching("[ ]*","update step comment"),
                    buildSimple(" ")
            ),
            ptrns(
                    buildSimple("[ ]*update step comment[ ]*")
            )
    );


    public TextCommand command_goBack = buildTextCommand(
            "go back",
            ptrns(
                buildForMatching("[ ]*","go back"),
                buildForMatching(" go back"),
                buildSimple(" ")
            ),
            ptrns(
                buildSimple("[ ]*go back[ ]*")
            )
    );

    public TextCommand command_reload = buildTextCommand(
            "reload",
            ptrns(
                    buildForMatching("[ ]*","reload"),
                    buildForMatching(" reload"),
                    buildSimple(" ")
            ),
            ptrns(
                    buildSimple("[ ]*reload[ ]*")
            )
    );


    public TextCommand command_exit = buildTextCommand(
            "exit application",ptrns(
                    buildForMatching("[ ]*","exit application"),
                    buildForMatching(" exit application"),
                    buildSimple(" ")),
            ptrns(buildSimple("[ ]*exit application[ ]*"))
    );

    public TextCommand command_runSuite = buildTextCommand(
            "run suite:",ptrns(
                    buildForMatching("[ ]*","run suite:"),
                    buildForMatching(" run suite:"),
                    buildSimple(" "))
    );

    public TextCommand command_continueRun = buildTextCommand(
            "continue run:",ptrns(
                    buildForMatching("[ ]*","continue run:"),
                    buildForMatching(" continue run:"),
                    buildSimple(" "))
    );

    public TextCommand command_markAs = buildTextCommand(
            "mark as:",ptrns(
                    buildForMatching("[ ]*","mark as:"),
                    buildSimple(" "))
    );

    public TextCommand command_pass = buildTextCommand(
            "pass",ptrns(
                    buildForMatching("[ ]*","pass"),
                    buildSimple(" "))
    );


    public TextCommand command_pass_andComment = buildTextCommand(
            "pass and comment:",ptrns(
                    buildForMatching("[ ]*pass", " and comment:"))
    );


    public TextCommand command_skip = buildTextCommand(
            "skip",ptrns(
                    buildForMatching("[ ]*","skip"),
                    buildSimple(" "))
    );


    public TextCommand command_skip_andComment = buildTextCommand(
            "skip and comment:",ptrns(
                    buildForMatching("[ ]*skip", " and comment:"))
    );

    public TextCommand command_fail = buildTextCommand(
            "fail",ptrns(
                    buildForMatching("[ ]*","fail"),
                    buildSimple(" "))
    );


    public TextCommand command_fail_andComment = buildTextCommand(
            "fail and comment:",ptrns(
                    buildForMatching("[ ]*fail", " and comment:"))
    );

    public TextCommand command_markAs_passed = buildTextCommand(
            "mark as: passed",ptrns(
                    buildForMatching("[ ]*mark as:", " passed")
            )
    );


    public TextCommand command_markAs_skipped = buildTextCommand(
            "mark as: skipped",ptrns(
                    buildForMatching("[ ]*mark as:", " skipped"))
    );


    public TextCommand command_markAs_failed = buildTextCommand(
            "mark as: failed",ptrns(
                    buildForMatching("[ ]*mark as:", " failed"))
    );

    public TextCommand command_markAs_passed_andComment = buildTextCommand(
            "mark as: passed and comment:",ptrns(
                    buildForMatching("[ ]*mark as: passed", " and comment:"))
    );

    public TextCommand command_markAs_failed_andComment = buildTextCommand(
            "mark as: failed and comment:",ptrns(
                    buildForMatching("[ ]*mark as: failed", " and comment:"))
    );

    public TextCommand command_markAs_skipped_andComment = buildTextCommand(
            "mark as: skipped and comment:",ptrns(
                    buildForMatching("[ ]*mark as: skipped", " and comment:"))
    );


    private Pattern[] ptrns(Pattern... patterns){
        return patterns;
    }

    private TextCommand buildTextCommand(String command, Pattern[] patterns, Pattern[] exact) {
        return new TextCommand(command,patterns, exact);
    }

    private TextCommand buildTextCommand(String command, Pattern[] patterns) {
        return new TextCommand(command,patterns, new Pattern[]{});
    }


    private Pattern buildForMatching(String command) {
       return buildForMatching("",command,"");
    }

    private Pattern buildForMatching(String preffix, String command) {
        return buildForMatching(preffix,command,"");
    }

    public boolean checkAgainst(String text, TextCommand command){
        for (Pattern pattern : command.patterns) {
            if (checkAgainst(text,pattern)){
                return true;
            }
        }
        return false;
    }

    public boolean checkAgainst(String text, Pattern commandPattern) {
        return commandPattern.matcher(text).matches();
    }

    private Pattern buildForMatching(String preffix, String command, String postfix) {
        StringBuilder patterBuilder = new StringBuilder(preffix);
        StringBuilder currentCommand = new StringBuilder();
        patterBuilder.append("(");
        for (int i=0; i < command.length()-1;i++){
            patterBuilder.append("|");
            currentCommand.append(command.charAt(i));
            patterBuilder.append(currentCommand);
        }
        patterBuilder.append(")");
        //Delete first or
        patterBuilder.deleteCharAt(preffix.length()+1);
        //System.out.println("Pattern for:" +patterBuilder.toString());
        return Pattern.compile(patterBuilder.toString(),Pattern.CASE_INSENSITIVE);
    }

    //TODO: Move to regular utils
    public static Pattern buildSimple(String s) {
        return Pattern.compile(s,Pattern.CASE_INSENSITIVE);
    }

    public void checkAgainstAndCall(String text, Closure<Void, TextCommand> doWithCommand, TextCommand... commands) {
        for (TextCommand command : commands) {
            if (checkAgainst(text,command)){
                doWithCommand.call(command);
            }
        }
    }

    public boolean checkExactAgainst(String text, TextCommand command) {
        if (command.exactPatterns == null) return false;
        for (Pattern exactPattern : command.exactPatterns) {
            if (checkAgainst(text,exactPattern)) return true;
        }
        return false;
    }
    //TODO: Move to regular utils
    public static List<String> extractUsing(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) return Collections.emptyList();
        int groupCount = matcher.groupCount();
        if (groupCount < 1) return Collections.emptyList();
        List<String> answer = new ArrayList<>();
        for (int i = 1; i<=groupCount; i++){
            answer.add(matcher.group(i));
        }
        return answer;
    }

    public StepRunRepresentation.Type typeByHuman(String human) {
        if ("passed".equals(human.toLowerCase())
             || "pass".equals(human.toLowerCase())){
            return StepRunRepresentation.Type.CHECK_PASSED;
        }
        if ("failed".equals(human.toLowerCase())
                || "fail".equals(human.toLowerCase())){
            return StepRunRepresentation.Type.CHECK_FAILED;
        }
        if ("skipped".equals(human.toLowerCase())
                || "skip".equals(human.toLowerCase())){
            return StepRunRepresentation.Type.CHECK_SKIPPED;
        }
        throw new RuntimeException();
    }


    public static class TextCommand{

        public final Pattern[] patterns;
        public final Pattern[] exactPatterns;
        public final String command;

        public TextCommand(String command, Pattern[] patterns, Pattern[] execPatterns) {
            this.patterns = patterns;
            this.command = command;
            this.exactPatterns = execPatterns;
        }
    }

}
