package org.monroe.team.chekit.services;

import org.monroe.team.chekit.common.Closure;
import org.monroe.team.chekit.uc.entity.check.ActionStep;
import org.monroe.team.chekit.uc.entity.check.Check;
import org.monroe.team.chekit.uc.entity.check.CheckSuite;
import org.monroe.team.chekit.uc.entity.check.Step;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import static org.monroe.team.chekit.services.TextRecognitionCore.*;

public class CheckSuitParser {

    static Pattern ROW_TITLE = buildSimple("[ ]*\\[Title\\][ ]*+(.+)");
    static Pattern ROW_COMMENT = buildSimple("[ ]*#(.*)");
    static Pattern ROW_CHECK = buildSimple("([ ]*)-(.*)");
    static Pattern ROW_ACTION = buildSimple("([ ]*)([^- ]{1}.*){1}");
    static Pattern ROW_GROUP = buildSimple("[ ]*\\[(.*)\\][ ]*");
    static Pattern ROW_CHECK_REF = ROW_GROUP;

    public CheckSuite parse(File file) throws IOException, FormatException {
        CheckItDocument checkItDocument = parseImpl(file);
        if (checkItDocument.title == null){
            throw new FormatException(-1,"No title founded",null);
        }
        if (checkItDocument.rootActionNode == null){
            throw new FormatException(-1,"No check tree founded",null);
        }
        while (updateGroupInjections(checkItDocument));
        String suiteId = Integer.toString(file.getAbsolutePath().hashCode());
        CheckSuite suite = new CheckSuite(suiteId, checkItDocument.title, file.getAbsolutePath());
        suite.rootActionStep = (ActionStep) convertNode(checkItDocument.rootActionNode, checkItDocument, suite).get(0);
        return suite;
    }

    private List<Step> convertNode(CheckItDocument.Node node, CheckItDocument checkItDocument, CheckSuite suite) throws FormatException {
        List<Step> result = new ArrayList<>();
        List<Step> children = new ArrayList<>();
        switch (node.type){
            case ACTION:
                ActionStep actionStep = new ActionStep(suite.generateId(),node.data,children);
                result.add(actionStep);
            break;
            case CHECK:
                Check check = new Check(suite.generateId(),node.data);
                result.add(check);
            break;
            case CHECK_REF:
                String groupId = node.data;
                CheckItDocument.Node group = checkItDocument.groups.get(groupId);
                if (group == null){
                    throw new FormatException(-1,"No group with id: "+groupId, null);
                }
                for (CheckItDocument.Node childrenNode : group.childrenNodes) {
                    result.addAll(convertNode(childrenNode,checkItDocument,suite));
                }
                break;
        }
        for (CheckItDocument.Node childrenNode : node.childrenNodes) {
            children.addAll(convertNode(childrenNode,checkItDocument,suite));
        }
        return result;
    }

    private boolean updateGroupInjections(CheckItDocument checkItDocument) throws FormatException {
        boolean groupUpdated = false;
        for (CheckItDocument.Node group : checkItDocument.groups.values()) {
            List<CheckItDocument.Node> updatedChildList = new ArrayList<>();
            for (CheckItDocument.Node childrenNode : group.childrenNodes) {
                if (childrenNode.type == CheckItDocument.Node.NodeType.CHECK){
                    updatedChildList.add(childrenNode);
                } else if (childrenNode.type == CheckItDocument.Node.NodeType.CHECK_REF){
                    CheckItDocument.Node groupToInsert = checkItDocument.groups.get(childrenNode.data);
                    if (groupToInsert == null){
                        throw new FormatException(-1,"No group with name: "+childrenNode.data,null);
                    }
                    if (group.data.equals(childrenNode.data)){
                        throw new FormatException(-1,"Recursive groups found starting with: "+childrenNode.data,null);
                    }
                    groupUpdated = true;
                    updatedChildList.addAll(groupToInsert.childrenNodes);
                }
            }
            group.childrenNodes = updatedChildList;
        }
        return groupUpdated;
    }

    CheckItDocument parseImpl(File file) throws IOException, FormatException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            int lineNumber = 0;
            CheckItDocument document = new CheckItDocument();
            while ((line = reader.readLine()) != null){
                try {
                    lineNumber++;
                    line = line.replace("\t","    ");
                    parseLine(document, line);
                } catch (ParseException ex){
                    throw new FormatException(lineNumber, ex.getMessage(),ex);
                }
            }
            return document;
        } catch (IOException | FormatException e){
            if (reader != null){
                try {
                    reader.close();
                }catch (IOException ex){ex.printStackTrace();}
            }
            throw e;
        }
    }

    private void parseLine(final CheckItDocument document, String line) {
        if (extractAndDo(line,ROW_COMMENT,new Closure<Boolean, List<String>>() {
            @Override
            public Boolean call(List<String> strings) {
                //Do nothing with comment lines
                return true;
            }
        })) return;

        if (extractAndDo(line,ROW_TITLE,new Closure<Boolean, List<String>>() {
            @Override
            public Boolean call(List<String> strings) {
                document.setTitle(strings.get(0).trim());
                return true;
            }
        })) return;

        if (extractAndDo(line,ROW_GROUP,new Closure<Boolean, List<String>>() {
            @Override
            public Boolean call(List<String> strings) {
                document.startGroup(strings.get(0).trim().toLowerCase());
                return true;
            }
        })) return;

        if (extractAndDo(line,ROW_ACTION,new Closure<Boolean, List<String>>() {
            @Override
            public Boolean call(List<String> strings) {
                document.action(strings.get(0).length(), strings.get(1).trim());
                return true;
            }
        })) return;

        if (extractAndDo(line,ROW_CHECK,new Closure<Boolean, List<String>>() {
            @Override
            public Boolean call(List<String> strings) {
                String name =  strings.get(1).trim();
                List<String> refExtracted = extractUsing(name,ROW_CHECK_REF);
                if (!refExtracted.isEmpty()){
                    document.check(strings.get(0).length(),refExtracted.get(0).trim().toLowerCase(),true);
                }else {
                    document.check(strings.get(0).length(),name,false);
                }
                return true;
            }
        })) return;

        if (line.trim().isEmpty()) {
            document.empty();
            return;
        }
        throw ParseException.issue("Unable to parse line",line);
    }


    final static class CheckItDocument{

        private String title = null;
        private Node rootActionNode;
        private Node currentActionNode;
        private Node currentGroupNode;
        private Map<String,Node> groups = new HashMap<>();

        public void setTitle(String title) {
            if (this.title != null){
                throw ParseException.issue("Second title found");
            }
            this.title = title;
        }

        public void startGroup(String groupName) {
            System.out.println("Start group: "+groupName);
            if (currentGroupNode != null){
                throw ParseException.issue("New line expected");
            }
            currentGroupNode = new Node(groupName, Node.NodeType.GROUP, 0);
            if (groups.put(currentGroupNode.data, currentGroupNode)!=null){
                throw ParseException.issue("Action group with name "+groupName+" exists");
            }
        }

        public void action(int before, String actionName) {
            System.out.println("Action: "+actionName);
            if (rootActionNode != null && currentActionNode == null){
                throw ParseException.issue("There are unexpected empty line");
            }

            Node actionNode = new Node(actionName, Node.NodeType.ACTION,before);
            if (rootActionNode == null){
                rootActionNode = actionNode;
                if (currentActionNode != null) throw ParseException.issue("Can`t be because of check above");
                currentActionNode = actionNode;
            } else {
                // main logic here
                if (currentActionNode.startOffset < actionNode.startOffset){
                    addActionNodeChild(actionNode);
                } else {
                    //find parent for action node
                    Node foundParent = findParent(actionNode.startOffset);
                    if (foundParent == null) throw ParseException.issue("Not enough spaces. No parent");
                    currentActionNode = foundParent;
                    addActionNodeChild(actionNode);
                }
            }
        }

        private void addActionNodeChild(Node actionNode) {
            actionNode.parentNode = currentActionNode;
            currentActionNode.childrenNodes.add(actionNode);
            currentActionNode = actionNode;
        }

        private Node findParent(int startOffset) {
            Node answer = currentActionNode;
            while (answer != null && answer.startOffset >= startOffset){
                answer = answer.parentNode;
            }
            return answer;
        }

        public void check(int before, String checkName, boolean isReference) {
            System.out.println("Check: "+checkName);
            Node checkNode = new Node(checkName, isReference? Node.NodeType.CHECK_REF: Node.NodeType.CHECK, before);
            if (currentGroupNode != null){
                currentGroupNode.childrenNodes.add(checkNode);
                return;
            }


            if (rootActionNode != null && currentActionNode == null){
                throw ParseException.issue("[1] Seems that it was unexpected empty line before");
            }
            if(currentActionNode == null){
                throw ParseException.issue("[2] Seems that it was unexpected empty line before");
            }


            // main logic here
            if (currentActionNode.startOffset < checkNode.startOffset){
                checkNode.parentNode = currentActionNode;
                currentActionNode.childrenNodes.add(checkNode);
            } else {
                //find parent for action node
                Node foundParent = findParent(checkNode.startOffset);
                if (foundParent == null) throw ParseException.issue("Not enough spaces. No parent");
                currentActionNode = foundParent;
                checkNode.parentNode = currentActionNode;
                currentActionNode.childrenNodes.add(checkNode);
            }
        }

        public void empty() {
            System.out.println("empty");
            currentGroupNode = null;
            currentActionNode = null;
        }

        private static class Node{

            String data;
            NodeType type;
            int startOffset;
            List<Node> childrenNodes = new ArrayList<>();
            Node parentNode;

            private Node(String data, NodeType type, int offset) {
                this.data = data;
                this.type = type;
                this.startOffset = offset;
            }

            private enum NodeType{
                ACTION, CHECK, GROUP, CHECK_REF
            }
        }

    }


    boolean extractAndDo(String line, Pattern pattern, Closure<Boolean,List<String>> doWith) {
        line =line.replace("\t","    ");
        List<String> extractedList = extractUsing(line,pattern);
        if (!extractedList.isEmpty()){
            return doWith.call(extractedList);
        }
        return false;
    }

    public static class FormatException extends Exception{
        public FormatException(int row, String message, Throwable cause) {
            super("Line "+row+":"+message,cause);
        }
    }

    private static class ParseException extends RuntimeException{
        public ParseException(String message,Throwable cause) {
            super(message, cause);
        }

        public static ParseException issue(String msg, String line){
            return new ParseException(msg, new IllegalStateException("Unsupported line:"+line));
        }

        public static ParseException issue(String msg){
            return new ParseException(msg, null);
        }
    }
}
