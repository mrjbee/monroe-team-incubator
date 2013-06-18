package org.monroe.team.jfeature.dependency;

import java.util.*;

/**
 * User: MisterJBee
 * Date: 6/18/13 Time: 10:39 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DefaultDependencyGraph<ContentType> implements DependencyGraph<ContentType> {

    private Map<ContentType, GraphNode<ContentType>> registeredContent
            = new HashMap<ContentType, GraphNode<ContentType>>();

    @Override
    public void addDependency(ContentType content, ContentType requiredContent) {
      GraphNode<ContentType> graphNode = getNodeByContent(content);
      GraphNode<ContentType> requiredGraphNode = getNodeByContent(requiredContent);
      //TODO: think about should it fail or its ok
      if(requiredGraphNode.edgeList.contains(graphNode)) return;
      requiredGraphNode.edgeList.add(graphNode);
    }

    @Override
    public List<ContentType> asTopologicalSortedList() throws GraphDependencyCycleException{
        Map<ContentType,GraphNode<ContentType>>  graphNodeMap = cloneRegistry();
        return doDepthFirstSearch(graphNodeMap);
    }

    private List<ContentType> doDepthFirstSearch(Map<ContentType, GraphNode<ContentType>> graphNodeMap) throws GraphDependencyCycleException {
        List<GraphNode<ContentType>> answer = new LinkedList<GraphNode<ContentType>>();
        Set<GraphNode<ContentType>> markedNodes = new HashSet<GraphNode<ContentType>>(graphNodeMap.size());
        GraphNode<ContentType> node;
        while((node = getUnmarkedNode(graphNodeMap, markedNodes)) != null){
           visitNode(node, answer, markedNodes, new ArrayList<GraphNode<ContentType>>());
        }
        return convert(answer);
    }

    private GraphNode<ContentType> getUnmarkedNode(Map<ContentType, GraphNode<ContentType>> graphNodeMap, Set<GraphNode<ContentType>> markedNodes)  {
        for (GraphNode<ContentType> graphNode : graphNodeMap.values()) {
             if (!markedNodes.contains(graphNode)) return  graphNode;
        }
        return null;
    }

    private List<ContentType> convert(List<GraphNode<ContentType>> convert) {
        List<ContentType> answer = new ArrayList<ContentType>(convert.size());
        for (GraphNode<ContentType> graphNode :convert) {
            answer.add(graphNode.contentType);
        }
        return answer;
    }

    private void visitNode(GraphNode<ContentType> node,
                           List<GraphNode<ContentType>> answer,
                           Set<GraphNode<ContentType>> markedNodes,
                           List<GraphNode<ContentType>> temporaryMarkedNodes) throws GraphDependencyCycleException {
           if (temporaryMarkedNodes.contains(node)) {
               throw new GraphDependencyCycleException(convert(temporaryMarkedNodes));
           }
           if (!markedNodes.contains(node)){
               temporaryMarkedNodes.add(node);
               for (GraphNode<ContentType> edgeNode: node.edgeList){
                   visitNode(edgeNode, answer, markedNodes, temporaryMarkedNodes);
               }
               markedNodes.add(node);
               answer.add(0, node);
           }

    }

    private Map<ContentType,GraphNode<ContentType>> cloneRegistry() {
        //TODO: implement deep clone of
        return registeredContent;
    }

    private GraphNode<ContentType> getNodeByContent(ContentType contentType) {
        GraphNode<ContentType> dependencyGraphNode = registeredContent.get(contentType);
        if (dependencyGraphNode == null){
              dependencyGraphNode = new GraphNode<ContentType>(contentType);
              registeredContent.put(contentType, dependencyGraphNode);
        }
        return dependencyGraphNode;
    }

    private static class GraphNode<ContentType>{

        private final ContentType contentType;
        private final List<GraphNode<ContentType>> edgeList
                                = new LinkedList<GraphNode<ContentType>>();

        private GraphNode(ContentType contentType) {
            this.contentType = contentType;
        }
    }

}
