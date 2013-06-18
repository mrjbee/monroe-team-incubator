package org.monroe.team.jfeature.dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/19/13 Time: 12:13 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class GraphDependencyCycleException extends Exception {

    public final List<?> cycleObjectsList;

    public GraphDependencyCycleException(List<?> cycleObjectsList) {
        this.cycleObjectsList = cycleObjectsList;
    }

    public <ExpectedType> List<ExpectedType> getCycleObjectsList(Class<ExpectedType> expectedTypeClass) {
        List<ExpectedType> answer = new ArrayList<ExpectedType>(cycleObjectsList.size());
        for (Object o : cycleObjectsList) {
            answer.add((ExpectedType) o);
        }
        return answer;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dependency  cycle [ ");
        for (Object o : cycleObjectsList) {
            builder.append(o);
            builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }


}
