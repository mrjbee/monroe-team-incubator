package org.monroe.team.jfeature.dependency;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/18/13 Time: 10:27 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface DependencyGraph<Content> {
    public void addDependency(Content content, Content required);
    public List<Content> asTopologicalSortedList() throws GraphDependencyCycleException;
}
