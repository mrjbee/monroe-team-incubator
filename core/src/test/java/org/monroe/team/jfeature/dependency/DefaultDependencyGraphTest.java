package org.monroe.team.jfeature.dependency;

import org.junit.Before;
import org.junit.Test;
import org.monroe.team.jfeature.description.TSupport;

/**
 * User: MisterJBee
 * Date: 6/18/13 Time: 10:53 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DefaultDependencyGraphTest extends TSupport{

    DefaultDependencyGraph<String> dependencyGraph;
    GraphBuilder define;

    @Before public void init(){
      dependencyGraph = new DefaultDependencyGraph<String>();
      define = new GraphBuilder(dependencyGraph);
    }

    @Test public void shouldReturnSortedListWithASimpleGraph(){
        define.a("1").on("2").on("3").on("5").end();
        should(define.asTopologicalSortedString(),"5,3,2,1");
    }

    @Test public void shouldReturnSortedListWithAFewSimpleSeparatedGraphs(){
        define.a("1").on("2").on("3").on("5").end();
        define.a("6").on("7").on("8").on("9").end();
        should(define.asTopologicalSortedString(),"9,8,5,7,6,3,2,1");
    }

    @Test public void shouldReturnSortedListWithNormalGraphs(){
        define.a("1").on("2").on("3").on("5").end();
        define.a("6").on("7").on("8").on("9").end();
        define.a("7").on("1");
        should(define.asTopologicalSortedString(),"9,8,5,3,2,1,7,6");
    }

    private static class GraphBuilder {

        private final DefaultDependencyGraph<String> graph;
        private String lastNode=null;

        private GraphBuilder(DefaultDependencyGraph<String> graph) {
            this.graph = graph;
        }

        public GraphBuilder a (String node){
            lastNode = node;
            return this;
        }

        public GraphBuilder on (String node){
            if (lastNode != null){
                graph.addDependency(lastNode, node);
            }
            lastNode = node;
            return this;
        }

        public void end(){
           lastNode = null;
        }

        public String asTopologicalSortedString(){
            StringBuilder builder = new StringBuilder();
            for (String st:graph.asTopologicalSortedList()){
                builder.append(st);
                builder.append(",");
            }
            if(builder.length()!=0) builder.deleteCharAt(builder.length()-1);
            return builder.toString();
        }
    }

}
