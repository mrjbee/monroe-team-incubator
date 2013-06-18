package org.monroe.team.jfeature.dependency;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Closure;
import org.junit.Before;
import org.junit.Test;
import org.monroe.team.jfeature.description.TSupport;
import org.monroe.team.jfeature.utils.Null;
import org.monroe.team.jfeature.utils.Pair;

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

    @Test public void shouldReturnSortedListWithASimpleGraph() throws GraphDependencyCycleException {
        define.a("1").on("2").on("3").on("5").end();
        should(define.asTopologicalSortedString(),"5,3,2,1");
    }

    @Test public void shouldReturnSortedListWithAFewSimpleSeparatedGraphs() throws GraphDependencyCycleException {
        define.a("1").on("2").on("3").on("5").end();
        define.a("6").on("7").on("8").on("9").end();
        should(define.asTopologicalSortedString(),"9,8,5,7,6,3,2,1");
    }

    @Test public void shouldReturnSortedListWithNormalGraphs() throws GraphDependencyCycleException {
        define.a("1").on("2").on("3").on("5").end();
        define.a("6").on("7").on("8").on("9").end();
        define.a("7").on("1");
        should(define.asTopologicalSortedString(), "9,8,5,3,2,1,7,6");
    }

    @Test public void shouldDetectSimpleCycle() throws GraphDependencyCycleException {
        define.a("1").on("2").on("3").on("5").end();
        //here comes cycle over 6
        define.a("1").on("6").on("7").on("8").on("6").end();
        try{
            define.asTopologicalSortedString();
            shouldFail();
        } catch (GraphDependencyCycleException e){
            should("7,6,8,",extractCycle(e));
        }
    }

    @Test public void shouldDetectSimpleCycle2() throws GraphDependencyCycleException {
        define.a("1").on("2").on("3").on("5").on("3").end();
        //here comes cycle over 6
        define.a("1").on("6").on("7").on("8").on("6").end();
        try{
            define.asTopologicalSortedString();
            shouldFail();
        } catch (GraphDependencyCycleException e){
            should("3,2,1,5,",extractCycle(e));
        }
    }


    private String extractCycle(GraphDependencyCycleException e) {
        StringBuilder builder = new StringBuilder();
        for (String value : e.getCycleObjectsList(String.class)) {
            builder.append(value+",");
        }
        return builder.toString();
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

        public String asTopologicalSortedString() throws GraphDependencyCycleException {
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
